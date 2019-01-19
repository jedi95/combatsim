package com.jedi95.combatsim;

public class DarkEmbrace extends Proc {

	public static final String NAME = "Dark Embrace Proc";
	public static final double COOLDOWN = 0;
	public static final double CHANCE = 1;

	public DarkEmbrace(Player player) {
		super(player, NAME, COOLDOWN, CHANCE, false);
	}

	//Checks if the proc should activate
	public void check(Player player, Target target, Hit hit, double time, int hitCount) {
		//Check if maul
		if (hit.ability != null && hit.ability == player.getAbility(Constants.Abilities.Maul)) {
			handleProc(player, target, time);
			lastActive = time; //reset timer for ICD
		}
	}
	
	//Handles the proc
	public void handleProc(Player player, Target target, double time) {
		player.getEffect(Constants.Effects.DarkEmbrace).addStacks(1, time);
	}

}
