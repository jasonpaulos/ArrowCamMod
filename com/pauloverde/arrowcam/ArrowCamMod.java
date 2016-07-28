package com.pauloverde.arrowcam;

import java.lang.reflect.Field;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod(
		modid = ArrowCamMod.MODID,
		useMetadata = true,
		clientSideOnly = true
)
public class ArrowCamMod{
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event){
		//TODO: Load a config if I want to have one
	}
	
	@EventHandler
	public void load(FMLInitializationEvent event){
		EntityRegistry.registerModEntity(EntityCamera.class, "ArrowCamera", 1, this, 256, 1, true);
		
		try {
			EntityArrow$inGround = EntityArrow.class.getDeclaredField("inGround");
			EntityArrow$inGround.setAccessible(true);
			
		} catch (Exception e) {
			EntityArrow$inGround = null;
		}
		
		if(event.getSide().equals(Side.CLIENT)){
			MinecraftForge.EVENT_BUS.register(new ArrowListener());
			
			ticker = new TickHandler();
			MinecraftForge.EVENT_BUS.register(ticker);
			
		}else if(!Minecraft.getMinecraft().isIntegratedServerRunning()){
			FMLLog.severe("The Arrow Cam Mod is a client only mod. Running it on a server will cause undefined behavior! Please remove this mod from your server ASAP.");
		}
	}
	
	/**
	 * TickHandler will run the task at the end of the current tick.
	 * Because of the thread-safe nature of queues, this method is also thread-safe
	 * @param task The task to be processed
	 */
	@SideOnly(Side.CLIENT)
	public void processAtTickEnd(Runnable task){
		ticker.tasks.offer(task);
	}
	
	/**
	 * Called when ArrowListener confirms the local player fires an arrow
	 * @param arrow The arrow fired
	 */
	@SideOnly(Side.CLIENT)
	public void startArrowCam(EntityArrow arrow){		
		if(!isInArrowCam()){
			camera = new EntityCamera(arrow);
			
			if(camera.worldObj.spawnEntityInWorld(camera)){
				Minecraft mc = Minecraft.getMinecraft();
				
				hideGUI = mc.gameSettings.hideGUI;
				fovSetting = mc.gameSettings.fovSetting;
				thirdPersonView = mc.gameSettings.thirdPersonView;
				
				mc.gameSettings.hideGUI = true;
				mc.gameSettings.fovSetting *= 1.1F;
				mc.gameSettings.thirdPersonView = thirdPersonView != 0 ? thirdPersonView : 1;
				mc.setRenderViewEntity(camera);
				mc.getRenderManager().renderViewEntity = camera;
			}else{
				camera = null;
			}
		}
	}
	
	/**
	 * Called when the EntityCamera has decided it can no longer follow its target arrow
	 */
	@SideOnly(Side.CLIENT)
	public void stopArrowCam(){
		if(isInArrowCam()){
			Minecraft mc = Minecraft.getMinecraft();
			mc.gameSettings.hideGUI = hideGUI;
			mc.gameSettings.fovSetting = fovSetting;
			mc.gameSettings.thirdPersonView = thirdPersonView;
			mc.setRenderViewEntity(mc.thePlayer);
			mc.getRenderManager().renderViewEntity = mc.thePlayer;
			
			if(!camera.isDead){
				camera.setDead();
			}
			camera = null;
		}
	}
	
	@SideOnly(Side.CLIENT)
	public boolean isInArrowCam(){
		return camera != null;
	}
	
	/**
	 * The "inGround" field in EntityArrow is private, so we have to get around that one way or another
	 * @param arrow Is this arrow in the ground?
	 * @return If the arrow is in the ground
	 */
	public boolean isArrowInGround(EntityArrow arrow){
		if(EntityArrow$inGround != null){
			try{
				return (Boolean)EntityArrow$inGround.get(arrow);
			}catch(Exception e){ }
		}
		
		NBTTagCompound tag = new NBTTagCompound();
		arrow.writeEntityToNBT(tag);
		
		return tag.getByte("inGround") == 1;
	}
	
	public static final String MODID = "arrowcammod";
	
	@Instance(ArrowCamMod.MODID)
	public static ArrowCamMod instance;
	
	/** There should only ever be one camera in the game */
	@SideOnly(Side.CLIENT)
	public EntityCamera camera;
	
	/** Basically just processes tasks at the end of each tick. See ArrowListener for why this is necessary */
	public TickHandler ticker;
	
	/** The normally-inaccessible EntityArrow.inGround field */
	private Field EntityArrow$inGround;
	
	/** Stores whether or not the GUI is hidden before entering arrow cam */
	private boolean hideGUI;
	
	/** Stores the FOV before entering arrow cam */
	private float fovSetting;
	
	/** Stores the POV before entering arrow cam */
	private int thirdPersonView;
}