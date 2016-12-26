package com.pauloverde.arrowcam;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ArrowListener{
	
	@SubscribeEvent
	public void onEntityJoinWorld(EntityJoinWorldEvent event){
		if(event.getEntity() instanceof EntityArrow){
			//Why can't we just process the arrow here?
			//Well, if the client is playing on an integrated server, we can and it will work fine.
			//However, if the client is connected to any other type of server, the EntityJoinWorldEvent
			//gets called just before the EntityArrow.shootingEntity field is processed.
			//You can thank net.minecraft.client.network.NetHandlerPlayClient.handleSpawnObject(...) for that.
			//If in the future the shootingEntity field is processed before the event gets fired,
			//the work can be done here without consequence.
			ArrowCamMod.instance.processAtTickEnd(new VerifyArrowTask((EntityArrow)event.getEntity()));
		}
	}
	
	@SubscribeEvent
	public void onEntityInteract(EntityInteract event){
		
		//Just so that the player will never interact with the camera
		if(event.getTarget() instanceof EntityCamera){
			event.setCanceled(true);
		}
	}
	
	public static class VerifyArrowTask implements Runnable{
		
		public VerifyArrowTask(EntityArrow a){
			arrow = a;
		}
		
		public void run(){
			EntityPlayer player = FMLClientHandler.instance().getClient().thePlayer;
			
			if(
				arrow.shootingEntity != null &&
				!arrow.isDead && !ArrowCamMod.instance.isArrowInGround(arrow) &&
				arrow.shootingEntity.equals(player) &&
				player.isSneaking() &&
				arrow.getDistanceSqToEntity(player) <= 25.0
			){
				ArrowCamMod.instance.startArrowCam(arrow);
			}
		}
		
		public EntityArrow arrow;
	};
}