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

public class Maul extends Ability {

	//Ability details
	public static final String NAME = "Maul";
	public static final double FORCE = 40;
	public static final double COOLDOWN = 0;
	public static final double DAMAGE_MULTI = 1.0;
	public static final double CRITICAL_BONUS = 0.0;
	public static final double SURGE_BONUS = 0.3;
	public static final double DUPLICITY_DAMAGE_MULTI = 1.2;
	public static final double DUPLICITY_FORCE_REDUCTION_MULTI = 0.25;
	public static final int HIT_COUNT = 1;

	//Ability damage constants
	public static final AbilityDamage DAMAGE = 
			new AbilityDamage.AbilityDamageBuilder()
			.standardHealthPercentMin(0.233)
			.standardHealthPercentMax(0.233)
			.coefficient(2.33)
			.amountModifierPercent(0.55)
			.isForce(false)
			.isInternal(false)
			.build();
	
	public Maul(Player player)
	{
		super(player, NAME, FORCE, COOLDOWN, DAMAGE_MULTI, CRITICAL_BONUS, SURGE_BONUS, HIT_COUNT);
		damage = DAMAGE;
	}

	//Handle duplicity damage bonus
	public double getDamageMulti() {
		double multiplier = super.getDamageMulti();
		Effect duplicity = player.getEffect(Constants.Effects.Duplicity);
		if (duplicity.isActive(player.sim.time()))
		{
			return multiplier * DUPLICITY_DAMAGE_MULTI;
		}
		else
		{
			return multiplier;
		}
	}

	//Handle duplicity force cost reduction
	public double getForceCost() {
		Effect duplicity = player.getEffect(Constants.Effects.Duplicity);
		if (duplicity.isActive(player.sim.time()))
		{
			return forceCost * DUPLICITY_FORCE_REDUCTION_MULTI;
		}
		else
		{
			return forceCost;
		}
	}

	//Remove any stacks of duplicity
	public void consumeEffects(Hit hit) {
		player.getEffect(Constants.Effects.Duplicity).resetStacks();
		player.getEffect(Constants.Effects.StalkerCriticalBonus).resetStacks();
	}

	//Check for induction stacks
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
		
		//Check if we have duplicity
		Effect duplicity = player.getEffect(Constants.Effects.Duplicity);
		if (duplicity.isActive(time)) {
			return true;
		}
		
		return false;
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