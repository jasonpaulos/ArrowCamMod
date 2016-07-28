package com.pauloverde.arrowcam;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityCamera extends Entity{
	
	public EntityCamera(World world){
		super(world);
		setSize(0.0F, 0.0F);
		setEntityInvulnerable(true);
		deathDelay = 20;
	}
	
	public EntityCamera(EntityArrow arrow){
		this(arrow.worldObj);
		setTarget(arrow);
		posX = target.posX;
		posY = target.posY;
		posZ = target.posZ;
		prevPosX = target.prevPosX;
		prevPosY = target.prevPosY;
		prevPosZ = target.prevPosZ;
		motionX = target.motionX;
		motionY = target.motionY;
		motionZ = target.motionZ;
		rotationYaw = (360.0F - target.rotationYaw) % 360.0F;
        rotationPitch = (360.0F - target.rotationPitch) % 360.0F;
        prevRotationYaw = (360.0F - target.prevRotationYaw) % 360.0F;
        prevRotationPitch = (360.0F - target.prevRotationPitch) % 360.0F;
		
		this.startRiding(target, true);
	}
	
	@Override
	protected void entityInit() {
		
	}
	
	@Override
	public void onUpdate(){
		super.onUpdate();
		
		Chunk chunk = worldObj.getChunkFromBlockCoords(new BlockPos((int)posX, (int)posY, (int)posZ));
		
		if(!chunk.isLoaded() || Minecraft.getMinecraft().thePlayer == null || Minecraft.getMinecraft().thePlayer.isDead || !Minecraft.getMinecraft().thePlayer.isSneaking()){
			ArrowCamMod.instance.stopArrowCam();
		}
		
		if(target != null){
			
			if(!target.isEntityAlive() || ArrowCamMod.instance.isArrowInGround(target)){
				motionX = motionY = motionZ = 0;
				prevRotationYaw = rotationYaw;
				prevRotationPitch = rotationPitch;
				
				if(--deathDelay <= 0){
					ArrowCamMod.instance.stopArrowCam();
				}
			}else{
				motionX = target.motionX;
				motionY = target.motionY;
				motionZ = target.motionZ;
				
				rotationYaw = (360.0F - target.rotationYaw) % 360.0F;
		        rotationPitch = (360.0F - target.rotationPitch) % 360.0F;
		        prevRotationYaw = (360.0F - target.prevRotationYaw) % 360.0F;
		        prevRotationPitch = (360.0F - target.prevRotationPitch) % 360.0F;
			}
		}
	}
	
	@Override
	public void setDead(){
		super.setDead();
		ArrowCamMod.instance.stopArrowCam();
	}
	
	public void setTarget(EntityArrow arrow){
		target = arrow;
	}
	
	public EntityArrow getTarget(){
		return target;
	}
	
	@Override
	public Entity changeDimension(int dimensionIn){
		return null;
	}
	
	@Override
	protected boolean canTriggerWalking(){
		return false;
	}
	
	@Override
	public boolean canBeCollidedWith(){
		return false;
	}
	
	@Override
	public boolean canBePushed(){
		return false;
	}
	
	@Override
    public boolean canBeAttackedWithItem(){
        return false;
    }
	
	@Override
    public float getEyeHeight(){
        return 0.0F;
    }
	
	@SideOnly(Side.CLIENT)
	@Override
	public boolean canRenderOnFire(){
		return false;
	}
	
	@Override
	protected void readEntityFromNBT(NBTTagCompound compound) {
		
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound compound) {
		
	}
	
	/** How many ticks the camera will stay around for after the arrow is dead or in a block */
	public int deathDelay;
	
	/** The arrow that the camera is following */
	public EntityArrow target;
}