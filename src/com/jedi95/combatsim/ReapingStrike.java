package com.jedi95.combatsim;

public class ReapingStrike extends Ability {

	public static final String NAME = "Reaping Strike";
	public static final double FORCE = 15;
	public static final double COOLDOWN = 18;
	public static final double DAMAGE_MULTI = 1.00;
	public static final double CRITICAL_BONUS = 0.0;
	public static final double SURGE_BONUS = 0.0;
	public static final int HIT_COUNT = 1;
	
	//Ability damage constants
	public static final AbilityDamage DAMAGE = 
			new AbilityDamage.AbilityDamageBuilder()
			.standardHealthPercentMin(0.275)
			.standardHealthPercentMax(0.275)
			.coefficient(2.75)
			.amountModifierPercent(0.83)
			.isForce(false)
			.isInternal(false)
			.build();
	
	public ReapingStrike(Player player)
	{
		super(player, NAME, FORCE, COOLDOWN, DAMAGE_MULTI, CRITICAL_BONUS, SURGE_BONUS, HIT_COUNT);
		damage = DAMAGE;
	}
	
	public boolean canUse(Target target) {
		if (isReady() && forceOk() && player.getEffect(Constants.Effects.ReapingStrikeCrit).isActive(player.sim.time())) {
			return true;
		}
		return false;
	}
	
	public boolean shouldUse(Target target) {
		if (!canUse(target)) {
			return false;
		}
		
		double time = player.sim.time();
		
		//Check if voltage will fall off in the next 2 GCD
		Effect voltage = player.getEffect(Constants.Effects.Voltage);
		if (voltage.getRemainingTime(time) < player.sim.getGCDLength() * 2) {
			return false;
		}
		
		//Check if we need autocrit ASAP in opener
		Effect autocrit = player.getEffect(Constants.Effects.StalkerCriticalBonus);
		Proc autocritproc = player.getProc(3);
		if (!autocrit.isActive(time) && autocritproc.isReady() && player.sim.time() < 10) {
			return false;
		}
		
		//If duplicity is close to 5 seconds remaining
		Effect duplicity = player.getEffect(Constants.Effects.Duplicity);
		if (duplicity.isActive(time) && duplicity.getRemainingTime(time) <= 5 + (2 * player.sim.getGCDLength()))
		{
			return false;
		}
		
		//if we have autocrit and maul glowing
		if (duplicity.isActive(time) && autocrit.isActive(time)) {
			return false;
		}
		
		//If below 30% HP and maul glowing
		if (target.getHealth() <= target.getMaxHealth() * Assassinate.USABLE_HP_PERCENT && duplicity.isActive(time)) {
			return false;
		}
		
		return true;
	}
}
