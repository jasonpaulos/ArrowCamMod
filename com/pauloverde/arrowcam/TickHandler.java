package com.pauloverde.arrowcam;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;

public class TickHandler{
	
	public TickHandler(){
		tasks = new ConcurrentLinkedQueue<Runnable>();
	}
	
	@SubscribeEvent
	public void onTick(ClientTickEvent event){
		if(event.phase == Phase.START){
			Runnable task = tasks.poll();
			while(task != null){
				task.run();
				task = tasks.poll();
			}
		}
	}
	
	public Queue<Runnable> tasks;
}