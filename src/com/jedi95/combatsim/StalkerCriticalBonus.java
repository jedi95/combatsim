package com.jedi95.combatsim;

public class StalkerCriticalBonus extends Proc {

	public static final String NAME = "Stalker's Critical Bonus";
	public static final double COOLDOWN = 60;
	public static final double CHANCE = 1;
	public static final double CRITICAL_CHANCE_BONUS = 1.0;
	
	public StalkerCriticalBonus(Player player) {
		super(player, NAME, COOLDOWN, CHANCE, true);
	}

	//Checks if the proc should activate
	public void check(Player player, Target target, Hit hit, double time, int hitCount) {
		//Check if voltaic slash
		if (lastActive + getCooldown() <= time && hit.ability != null && hit.ability == player.getAbility(Constants.Abilities.VoltaicSlash)) {
			handleProc(player, target, time);
			lastActive = time; //reset timer for ICD
		}
	}
	
	//Handles the proc
	public void handleProc(Player player, Target target, double time) {
		player.getEffect(Constants.Effects.StalkerCriticalBonus).addStacks(1, time);
	}
}
