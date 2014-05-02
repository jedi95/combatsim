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

	//Damage constants
	public static final double standardHealthPercentMin = 0.309;
	public static final double standardHealthPercentMax = 0.309;
	public static final double coefficient = 3.09;
	public static final double amountModifierPercent = 1.06;
	public static final boolean IS_SPECIAL = true;
	public static final boolean IS_FORCE = false;
	public static final boolean IS_INTERNAL = false;

	//Ability details
	public static final String NAME = "Assassinate";
	public static final int FORCE = 13;
	public static final int COOLDOWN = 6000; //in ms
	public static final double DAMAGE_MULTI = 1.00;
	public static final double CRITICAL_BONUS = 0.0;
	public static final double SURGE_BONUS = 0.0;
	public static final int HIT_COUNT = 1;
	public static final double USABLE_HP_PERCENT = 0.3;

	public Assassinate(Player player)
	{
		super(player, NAME, FORCE, COOLDOWN, DAMAGE_MULTI, CRITICAL_BONUS, SURGE_BONUS, HIT_COUNT);
		damage = new AbilityDamage(standardHealthPercentMin, standardHealthPercentMax, coefficient, amountModifierPercent, IS_SPECIAL, IS_FORCE, IS_INTERNAL);
	}

	//Need to override this to implement under 30% HP condition.
	public boolean canUse(Target target) {
		return forceOk() && isReady() && (target.getHealth() <= target.getMaxHealth() * USABLE_HP_PERCENT);
	}

	public void checkProcs(Target target, Hit hit, int hitCount) {

		//Add induction
		Effect induction = player.getEffect("Induction");
		induction.addStacks(1, player.sim.time());

		//Call global handler
		super.checkProcs(target, hit, hitCount);
	}
}