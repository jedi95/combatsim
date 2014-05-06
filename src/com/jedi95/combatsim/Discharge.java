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

public class Discharge extends Ability {

	//Damage constants
	public static final double standardHealthPercentMin = 0.039;
	public static final double standardHealthPercentMax = 0.059;
	public static final double coefficient = 0.49;
	public static final double amountModifierPercent = 0.01;
	public static final boolean IS_SPECIAL = false;
	public static final boolean IS_FORCE = true;
	public static final boolean IS_INTERNAL = true;

	//Ability details
	public static final String NAME = "Discharge";
	public static final int FORCE = 20;
	public static final int COOLDOWN = 0; //in ms
	/*The damage for Discharge doesn't agree with what the tooltips specify.
	  According to the tooltips the damage should scale linearly with more static charges.
	  In reality, it appears to work like this:
	  1 charge = base damage * ~1 (working as intended)
	  2 charges = base damage * ~3 (obviously wrong)
	  3 charges = base damage * ~4 (obviously wrong)
	  Since the simulator will only ever use Discharge with 3 static charges,
	  I have accounted for the damage difference via a constant damage multiplier.
	*/
	public static final double DAMAGE_MULTI = 1.333333333;
	public static final double CRITICAL_BONUS = 0.0;
	public static final double SURGE_BONUS = 0.5;
	public static final int HIT_COUNT = 1;

	public Discharge(Player player)
	{
		super(player, NAME, FORCE, COOLDOWN, DAMAGE_MULTI, CRITICAL_BONUS, SURGE_BONUS, HIT_COUNT);
		damage = new AbilityDamage(standardHealthPercentMin, standardHealthPercentMax, coefficient, amountModifierPercent, IS_SPECIAL, IS_FORCE, IS_INTERNAL);
	}

	//returns true if the ability can currently be used
	public boolean canUse(Target target) {
		Effect staticCharge = player.getEffect("Static Charge");
		return forceOk() && isReady() && staticCharge.isActive(player.sim.time());
	}

	//Called to consume any effects/procs/buffs that benefit the ability
	public void consumeEffects(Hit hit) {
		Effect staticCharge = player.getEffect("Static Charge");
		staticCharge.resetStacks();

		//Consume recklessness charge
		if (hit.crit) {
			Effect reck = player.getEffect("Recklessness");
			if (reck.isActive(player.sim.time())){
				reck.consumeStacks(1);
			}
		}
		return;
	}

	//Add static charges
	public void checkProcs(Target target, Hit hit, int hitCount) {

		//Handle ES
		if (hit.crit) {
			Effect es = player.getEffect("Exploitive Strikes");
			es.addStacks(1, player.sim.time());
		}

		//Call global handler
		super.checkProcs(target, hit, hitCount);
	}

	public boolean shouldUse(Target target) {
		if (!canUse(target)) {
			return false;
		}

		//Always use if we have 3 static charges
		Effect staticCharge = player.getEffect("Static Charge");
		if (staticCharge.isActive(player.sim.time()) && staticCharge.getStacks() == 3)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	//Need to override this to handle static charges increasing damage
	public Hit calculateHitDamage(Player player, Target target) {
		Hit hit = new Hit(getName(), Calc.calculateDamage(player, target, this.getDamage()) * getDamageMulti(), false);

		//handle crits
		double critChance = getCritBonus();
		if (damage.isForce) {
			critChance += Calc.getForceCritChance(player);
		}
		else
		{
			critChance += Calc.getMeleeCritChance(player);
		}

		boolean isCrit = player.random.nextDouble() <= critChance;
		if (isCrit)
		{
			hit.crit = true;
			hit.damage *= Calc.getCriticalDamageMultiplier(player) + getSurgeBonus();
		}

		//Apply static charge stack damage
		Effect staticCharge = player.getEffect("Static Charge");
		hit.damage *= staticCharge.getStacks();

		return hit;
	}
}