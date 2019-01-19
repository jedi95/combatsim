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

	//Ability details
	public static final String NAME = "Discharge";
	public static final double FORCE = 20;
	public static final double COOLDOWN = 0;
	public static final double DAMAGE_MULTI = 1.0;
	public static final double CRITICAL_BONUS = 0.0;
	public static final double SURGE_BONUS = 0.05;
	public static final int HIT_COUNT = 1;

	public static final AbilityDamage DAMAGE = 
			new AbilityDamage.AbilityDamageBuilder()
			.standardHealthPercentMin(0.053)
			.standardHealthPercentMax(0.093)
			.coefficient(0.73)
			.amountModifierPercent(0)
			.isForce(true)
			.isInternal(true)
			.build();
	
	public Discharge(Player player)
	{
		super(player, NAME, FORCE, COOLDOWN, DAMAGE_MULTI, CRITICAL_BONUS, SURGE_BONUS, HIT_COUNT);
		damage = DAMAGE;
	}

	//returns true if the ability can currently be used
	public boolean canUse(Target target) {
		Effect staticCharge = player.getEffect(Constants.Effects.StaticCharge);
		return forceOk() && isReady() && staticCharge.isActive(player.sim.time());
	}

	//Called to consume any effects/procs/buffs that benefit the ability
	public void consumeEffects(Hit hit) {
		Effect staticCharge = player.getEffect(Constants.Effects.StaticCharge);
		staticCharge.resetStacks();

		//Consume recklessness charge
		if (hit.crit) {
			Effect reck = player.getEffect(Constants.Effects.Recklessness);
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
			Effect es = player.getEffect(Constants.Effects.ExploitiveStrikes);
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
		Effect staticCharge = player.getEffect(Constants.Effects.StaticCharge);
		if (staticCharge.isActive(player.sim.time()) && staticCharge.getStacks() == 3)
		{
			return true;
		}

		return false;
	}

	//Need to override this to handle static charges increasing damage
	public Hit calculateHitDamage(Player player, Target target) {
		Hit hit = new Hit(this, Calc.calculateDamage(player, target, this.getDamage()) * getDamageMulti(), false, this.getDamage().isForce, getName());

		//handle crits
		double critChance = getCritBonus();
		critChance += Calc.getCritChance(player);

		boolean isCrit = player.random.nextDouble() <= critChance;
		if (isCrit)
		{
			hit.crit = true;
			hit.damage *= Calc.getSuperCritMultiplier(player, critChance) + getSurgeBonus();
		}

		//Apply static charge stack damage
		Effect staticCharge = player.getEffect(Constants.Effects.StaticCharge);
		hit.damage *= staticCharge.getStacks();

		return hit;
	}
}