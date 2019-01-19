package com.jedi95.combatsim;

public class ReapingStrikeProc extends Proc {

	public static final String NAME = "Reaping Strike Proc";
	public static final double COOLDOWN = 0;
	public static final double CHANCE = 1;

	public ReapingStrikeProc(Player player) {
		super(player, NAME, COOLDOWN, CHANCE, false);
	}

	//Checks if the proc should activate
	public void check(Player player, Target target, Hit hit, double time, int hitCount) {
		//Check if crit
		if (hit.crit) {
			handleProc(player, target, time);
			lastActive = time; //reset timer for ICD
		}
	}
	
	//Handles the proc
	public void handleProc(Player player, Target target, double time) {
		player.getEffect(Constants.Effects.ReapingStrikeCrit).addStacks(1, time);
	}
}
