package com.jedi95.combatsim;

public class StalkerDamageBonus extends Proc {

	public static final String NAME = "Stalker's Damage Bonus";
	public static final double COOLDOWN = 30;
	public static final double CHANCE = 1;
	public static final double DAMAGE_BONUS = 0.02;
	
	public StalkerDamageBonus(Player player) {
		super(player, NAME, COOLDOWN, CHANCE, true);
	}

	//Checks if the proc should activate
	public void check(Player player, Target target, Hit hit, double time, int hitCount) {
		//Check if ball lightning
		if (lastActive + getCooldown() <= time && hit.ability != null && hit.ability == player.getAbility(Constants.Abilities.BallLightning)) {
			handleProc(player, target, time);
			lastActive = time; //reset timer for ICD
		}
	}
	
	//Handles the proc
	public void handleProc(Player player, Target target, double time) {
		player.getEffect(Constants.Effects.StalkerDamageBonus).addStacks(1, time);
	}
}
