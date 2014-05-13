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

public class VoltaicSlash extends Ability {

	//Damage constants
	public static final double standardHealthPercentMin = 0.089;
	public static final double standardHealthPercentMax = 0.089;
	public static final double coefficient = 0.89;
	public static final double amountModifierPercent = -0.41;
	public static final boolean IS_SPECIAL = true;
	public static final boolean IS_FORCE = false;
	public static final boolean IS_INTERNAL = false;

	//Ability details
	public static final String NAME = "Voltaic Slash";
	public static final int FORCE = 23;
	public static final int COOLDOWN = 0; //in ms
	public static final double DAMAGE_MULTI = 1.06; //From charge mastery
	public static final double CRITICAL_BONUS = 0.15; //From 4pc PVE set bonus
	public static final double SURGE_BONUS = 0.0;
	public static final int HIT_COUNT = 2;
	public static final double VOLTAGE_FORCE_CRIT_BONUS = 0.0;
	public static final double SURGING_CHARGE_PROC_CHANCE_BONUS = 0.0;

	public VoltaicSlash(Player player)
	{
		super(player, NAME, FORCE, COOLDOWN, DAMAGE_MULTI, CRITICAL_BONUS, SURGE_BONUS, HIT_COUNT);
		damage = new AbilityDamage(standardHealthPercentMin, standardHealthPercentMax, coefficient, amountModifierPercent, IS_SPECIAL, IS_FORCE, IS_INTERNAL);
	}

	public void checkProcs(Target target, Hit hit, int hitCount) {

		//Add voltage and induction
		Effect voltage = player.getEffect("Voltage");
		voltage.addStacks(1, player.sim.time());
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

		Effect induction = player.getEffect("Induction");
		Effect voltage = player.getEffect("Voltage");

		//If we need induction stacks
		if (!induction.isActive(time)) {
			return true;
		}
		else if (induction.getStacks() < 2) {
			return true;
		}
		//If we need voltage, use.
		else if (!voltage.isActive(time)) {
			return true;
		}
		//If we have less than 2 stacks of voltage, use.
		else if (voltage.getStacks() < 2) {
			return true;
		}
		//If we will force cap
		else if (player.getForce() + player.sim.getForceRegen(Simulator.GCD_LENGTH) + (SaberStrike.SET_BONUS_FORCE_PER_HIT * SaberStrike.HIT_COUNT) >= Player.MAX_FORCE) {
			return true;
		}
		//If voltage stacks will fall off within 3 GCD
		else if (voltage.getRemainingTime(time) <= Simulator.GCD_LENGTH * 3) {
			return true;
		}
		//Otherwise conserve force
		return false;
	}
}