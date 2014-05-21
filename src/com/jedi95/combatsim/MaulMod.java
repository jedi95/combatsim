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

public class MaulMod extends Ability {

	//Damage constants
	public static final double standardHealthPercentMin = 0.236;
	public static final double standardHealthPercentMax = 0.236;
	public static final double coefficient = 2.3705;
	public static final double amountModifierPercent = 0.58;
	public static final boolean IS_SPECIAL = true;
	public static final boolean IS_FORCE = false;
	public static final boolean IS_INTERNAL = false;

	//Ability details
	public static final String NAME = "Maul";
	public static final int FORCE = 40;
	public static final int COOLDOWN = 0; //in ms
	public static final double DAMAGE_MULTI = 1.06;
	public static final double CRITICAL_BONUS = 1.0; //Setting this here because we never use maul without duplicity anyway.
	public static final double SURGE_BONUS = 0.3;
	public static final double DUPLICITY_DAMAGE_MULTI = 1.3;
	public static final int DUPLICITY_FORCE_REDUCTION_FACTOR = 4; //newCost = originalCost / reduction
	public static final int HIT_COUNT = 1;

	public MaulMod(Player player)
	{
		super(player, NAME, FORCE, COOLDOWN, DAMAGE_MULTI, CRITICAL_BONUS, SURGE_BONUS, HIT_COUNT);
		damage = new AbilityDamage(standardHealthPercentMin, standardHealthPercentMax, coefficient, amountModifierPercent, IS_SPECIAL, IS_FORCE, IS_INTERNAL);
	}

	//Handle duplicity damage bonus
	public double getDamageMulti() {
		Effect duplicity = player.getEffect("Duplicity");
		if (duplicity.isActive(player.sim.time()))
		{
			return damageMulti * DUPLICITY_DAMAGE_MULTI;
		}
		else
		{
			return damageMulti;
		}
	}

	//Handle duplicity force cost reduction
	public int getForceCost() {
		Effect duplicity = player.getEffect("Duplicity");
		if (duplicity.isActive(player.sim.time()))
		{
			return forceCost / DUPLICITY_FORCE_REDUCTION_FACTOR;
		}
		else
		{
			return forceCost;
		}
	}

	//Remove any stacks of duplicity
	public void consumeEffects(Hit hit) {
		Effect duplicity = player.getEffect("Duplicity");
		duplicity.resetStacks();
	}

	//Check for induction stacks
	public void checkProcs(Target target, Hit hit, int hitCount) {

		//Add induction
		Effect induction = player.getEffect("Induction");
		induction.addStacks(1, player.sim.time());

		//Call global handler
		super.checkProcs(target, hit, hitCount);
	}

	public boolean shouldUse(Target target) {
		if (!canUse(target)) {
			return false;
		}

		long time = player.sim.time();

		//Only use if we have duplicity
		Effect duplicity = player.getEffect("Duplicity");
		if (duplicity.isActive(time))
		{
			Effect induction = player.getEffect("Induction");
			Effect voltage = player.getEffect("Voltage");

			//If the proc is going to fall off in the next GCD then use maul
			if (duplicity.isActive(time) && (duplicity.getRemainingTime(time) <= Simulator.GCD_LENGTH)) {
				return true;
			}
			//If we don't have 2 stacks of voltage then don't use maul
			else if (!(voltage.isActive(time) && voltage.getStacks() >= 2)) {
				return false;
			}
			//If Voltage will fall off this GCD don't use maul
			else if (voltage.getRemainingTime(time) <= Simulator.GCD_LENGTH) {
				return false;
			}
			//If we don't have 2 induction stacks then use Maul
			else if (!induction.isActive(time) || (induction.isActive(time) && induction.getStacks() < 2))
			{
				return true;
			}
			//If we will force cap
			else if (player.getForce() + player.sim.getForceRegen(Simulator.GCD_LENGTH) + (SaberStrike.SET_BONUS_FORCE_PER_HIT * SaberStrike.HIT_COUNT) >= Player.MAX_FORCE) {
				return true;
			}
		}

		return false;
	}
}