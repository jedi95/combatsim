package com.jedi95.combatsim;

public class PhantomStride {
	
	public static final double GCD_DELAY = 0.25;
	
	protected String name = "Phantom Stride";
	protected double cooldown = 30;
	protected double lastUsedTime;
	protected Player player;
	
	public PhantomStride(Player player, double lastUsedTime) {
		this.player = player;
		this.lastUsedTime = lastUsedTime;
	}

	public boolean isReady() {
		return lastUsedTime + cooldown <= player.sim.time();
	}
	
	public boolean shouldUse() {
		
		if (!isReady()) {
			return false;
		}
		
		double time = player.sim.time();
		Effect reck = player.getEffect(Constants.Effects.Recklessness);
		Effect charges = player.getEffect(Constants.Effects.StaticCharge);
		OffGCDAbility reckAbil = player.getOffAbility(Constants.OffAbilities.Recklessness);
		
		//If we have recklessness stacks and no static charges, use
		if (reck.isActive(time) && !charges.isActive(time)) {
			return true;
		}
		//If no static charges and >(30 - GCD) seconds to recklessness
		else if (!charges.isActive(time) && reckAbil.getTimeToReady() > cooldown - player.sim.getGCDLength()) {
			return true;
		}
		
		return false;
	}
	
	public void use() {
		player.getEffect(Constants.Effects.StaticCharge).addStacks(3, player.sim.time());
		player.getEffect(Constants.Effects.ReapersRush).addStacks(1, player.sim.time());
		lastUsedTime = player.sim.time();
		player.sim.getLog().logStride();
	}
}
