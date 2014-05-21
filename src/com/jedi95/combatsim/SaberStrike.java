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

public class SaberStrike extends Ability {
	//Damage constants
	public static final double standardHealthPercentMin = 0.0;
	public static final double standardHealthPercentMax = 0.0;
	public static final double coefficient = 0.989;
	public static final double amountModifierPercent = 0.0115;
	public static final boolean IS_SPECIAL = false;
	public static final boolean IS_FORCE = false;
	public static final boolean IS_INTERNAL = false;

	//Ability details
	public static final String NAME = "Saber Strike";
	public static final int FORCE = 0;
	public static final int COOLDOWN = 0; //in ms
	public static final double CRITICAL_BONUS = 0.0;
	public static final double SURGE_BONUS = 0.0;
	public static final int HIT_COUNT = 3;
	public static final double DAMAGE_MULTI = 1.0 / HIT_COUNT; //to account for 3 hits
	public static final int SET_BONUS_FORCE_PER_HIT = 1 * Calc.FORCE_MULTI;

	public SaberStrike(Player player)
	{
		super(player, NAME, FORCE, COOLDOWN, DAMAGE_MULTI, CRITICAL_BONUS, SURGE_BONUS, HIT_COUNT);
		damage = new AbilityDamage(standardHealthPercentMin, standardHealthPercentMax, coefficient, amountModifierPercent, IS_SPECIAL, IS_FORCE, IS_INTERNAL);
	}

	public void use(Target target) {

		//Consume force
		consumeForce();

		//Set last used time
		lastUsedTime = player.sim.time();

		//Calculate accuracy
		double accuracy;
		if (damage.isSpecial || damage.isForce){
			accuracy = Calc.getSpecialAccuracy(player, target);
		}
		else
		{
			accuracy = Calc.getBasicAccuracy(player, target);
		}

		//Loop through hits
		int actualHits = hitCount;
		Hit hit = new Hit(getName(), 0.0, false, false);
		for (int i = 0; i < hitCount; i++){

			//Calculate damage
			hit = calculateHitDamage(player, target);

			//hit check
			if (player.random.nextDouble() * 1.10 <= accuracy)
			{
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

				//Handle 2-piece set bonus force regen
				player.addForce(SET_BONUS_FORCE_PER_HIT);

				actualHits++;
				target.applyHit(hit);
			}
			else
			{
				break; //Exit loop if we miss!
			}
		}

		//Consume procs/buffs
		consumeEffects(hit);

		//Handle procs
		checkProcs(target, hit, actualHits);
	}

	//Need to override this so we can get non-crit damage
	public Hit calculateHitDamage(Player player, Target target) {
		Hit hit = new Hit(getName(), Calc.calculateDamage(player, target, this.getDamage()) * getDamageMulti(), false, this.getDamage().isForce);

		if (target.getHealth() <= target.getMaxHealth() * 0.30) {
			hit.damage *= BELOW_30_PERCENT_DAMAGE_MULTI;
		}

		return hit;
	}
}