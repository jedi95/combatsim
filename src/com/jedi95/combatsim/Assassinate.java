/* Copyright (C) 2014 by jedi95 <jedi95@gmail.com>

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
*/
package com.jedi95.combatsim;

public class Assassinate extends Ability {

	//Ability details
	public static final String NAME = "Assassinate";
	public static final double FORCE = 10;
	public static final double COOLDOWN = 6;
	public static final double DAMAGE_MULTI = 1.00;
	public static final double CRITICAL_BONUS = 0.0;
	public static final double SURGE_BONUS = 0.0;
	public static final int HIT_COUNT = 1;
	public static final double USABLE_HP_PERCENT = 0.3;

	//Ability damage constants
	public static final AbilityDamage DAMAGE = 
			new AbilityDamage.AbilityDamageBuilder()
			.standardHealthPercentMin(0.254)
			.standardHealthPercentMax(0.254)
			.coefficient(2.54)
			.amountModifierPercent(0.7)
			.isForce(false)
			.isInternal(false)
			.build();
	
	public Assassinate(Player player)
	{
		super(player, NAME, FORCE, COOLDOWN, DAMAGE_MULTI, CRITICAL_BONUS, SURGE_BONUS, HIT_COUNT);
		damage = Assassinate.DAMAGE;
	}

	public void consumeEffects() {
		player.getEffect(Constants.Effects.StalkerCriticalBonus).resetStacks();
	}
	
	//Need to override this to implement under 30% HP condition.
	public boolean canUse(Target target) {
		Effect reapersRush = player.getEffect(Constants.Effects.ReapersRush);
		return forceOk() && isReady() && ((target.getHealth() <= target.getMaxHealth() * USABLE_HP_PERCENT) || reapersRush.isActive(player.sim.time()));
	}

	public void checkProcs(Target target, Hit hit, int hitCount) {

		//Add induction
		Effect induction = player.getEffect(Constants.Effects.Induction);
		induction.addStacks(1, player.sim.time());

		//Call global handler
		super.checkProcs(target, hit, hitCount);
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
		
		//Check if reaper's rush will expire and HP >30%
		Effect reapersRush = player.getEffect(Constants.Effects.ReapersRush);
		if (target.getHealth() > target.getMaxHealth() * USABLE_HP_PERCENT && reapersRush.getRemainingTime(time) <= (player.sim.getGCDLength() * 4)) {
			return true;
		}
		
		//If duplicity is close to 5 seconds remaining, and maul glowing
		Effect duplicity = player.getEffect(Constants.Effects.Duplicity);
		if (duplicity.isActive(time) && duplicity.getRemainingTime(time) <= 5 + (2 * player.sim.getGCDLength()))
		{
			return false;
		}
		
		//If we can use reaping Strike, and target >30% HP
		Ability reapingStrike = player.getAbility(Constants.Abilities.ReapingStrike);
		if (target.getHealth() > target.getMaxHealth() * USABLE_HP_PERCENT && reapingStrike.canUse(target)) {
			return false;
		}
		
		//if we have autocrit and glowing maul
		if (duplicity.isActive(time) && autocrit.isActive(time)) {
			return false;
		}
		
		return true;
	}
	
	public double getCritBonus() {
		double critBonus = super.getCritBonus();
		boolean addSetBonusCrit = player.getEffect(Constants.Effects.StalkerCriticalBonus).isActive(player.sim.time());
		if (addSetBonusCrit) {
			critBonus += StalkerCriticalBonus.CRITICAL_CHANCE_BONUS;
		}
		return critBonus;
	}
}