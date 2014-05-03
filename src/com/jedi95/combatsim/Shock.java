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

public class Shock extends Ability {

	//Damage constants
	public static final double standardHealthPercentMin = 0.136;
	public static final double standardHealthPercentMax = 0.176;
	public static final double coefficient = 1.56;
	public static final double amountModifierPercent = 0.01;
	public static final boolean IS_SPECIAL = false;
	public static final boolean IS_FORCE = true;
	public static final boolean IS_INTERNAL = false;

	//Ability details
	public static final String NAME = "Shock";
	public static final int FORCE = 39;
	public static final int COOLDOWN = 6000; //in ms
	public static final double DAMAGE_MULTI = 1.25; // from Assassin's Training
	//NOTE: Assassin's Training doesn't appear to affect tooltips. (which match perfectly without this)
	public static final double CRITICAL_BONUS = 0.0;
	public static final double SURGE_BONUS = 0.5;
	public static final int HIT_COUNT = 1;
	//NOTE: Induction's cost reduction uses 25% of the original force cost of Shock.
	//It does not take the base cost reduction from Torment into account.
	public static final int INDUCTION_FORCE_REDUCTION = 1125;
	public static final double VOLTAGE_CHANCE = 0.5;
	public static final double SECOND_SHOCK_CHANCE = 0.45;
	public static final double SECOND_SHOCK_DAMAGE_MULT = 0.5;

	public Shock(Player player)
	{
		super(player, NAME, FORCE, COOLDOWN, DAMAGE_MULTI, CRITICAL_BONUS, SURGE_BONUS, HIT_COUNT);
		damage = new AbilityDamage(standardHealthPercentMin, standardHealthPercentMax, coefficient, amountModifierPercent, IS_SPECIAL, IS_FORCE, IS_INTERNAL);
	}

	//Handle induction force cost reduction
	public int getForceCost() {
		Effect induction = player.getEffect("Induction");
		if (induction.isActive(player.sim.time()))
		{
			return forceCost - (INDUCTION_FORCE_REDUCTION * induction.getStacks());
		}
		else
		{
			return forceCost;
		}
	}

	//Handle removing induction stacks
	public void consumeEffects(Hit hit) {
		Effect induction = player.getEffect("Induction");
		induction.resetStacks();

		//Consume recklessness charge
		if (hit.crit) {
			Effect reck = player.getEffect("Recklessness");
			if (reck.isActive(player.sim.time())){
				reck.consumeStacks(1);
			}
		}
	}

	//Add static charges
	public void checkProcs(Target target, Hit hit, int hitCount) {

		//Handle second shock hit
		int actualHits = 1;
		if (player.random.nextDouble() <= SECOND_SHOCK_CHANCE) {

			//Count second hit
			actualHits++;

			//Get damage
			Hit secondShock = calculateHitDamage(player, target);
			secondShock.damage *= SECOND_SHOCK_DAMAGE_MULT;

			//Apply hit
			target.applyHit(secondShock);

			//Consume recklessness charge
			if (secondShock.crit) {
				Effect reck = player.getEffect("Recklessness");
				if (reck.isActive(player.sim.time())){
					reck.consumeStacks(1);
				}
			}
		}

		//Add static charges
		Effect voltage = player.getEffect("Voltage");
		if (voltage.isActive(player.sim.time())) {
			if (player.random.nextDouble() <= (VOLTAGE_CHANCE * voltage.getStacks())) {
				//Surging Charge
				player.getProc(2).handleProc(player, target, player.sim.time());
			}
		}

		//Handle ES
		if (hit.crit) {
			Effect es = player.getEffect("Exploitive Strikes");
			es.addStacks(1, player.sim.time());
		}

		//Call global handler
		super.checkProcs(target, hit, actualHits);
	}

	//Important: this handles the ability priority list! check subclasses.
	public boolean shouldUse(Target target) {
		if (!canUse(target)) {
			return false;
		}

		Effect voltage = player.getEffect("Voltage");
		Effect induction = player.getEffect("Induction");
		long time = player.sim.time();

		//If we have 2 stacks of voltage
		if (voltage.isActive(time) && voltage.getStacks() == 2) {
			//If we have 2 stacks of induction
			if (induction.isActive(time) && induction.getStacks() == 2) {
				return true;
			}
			//If we are going to force cap
			else if ((player.getForce() - getForceCost()) + player.sim.getForceRegen(Simulator.GCD_LENGTH) >= Player.MAX_FORCE) {
				return true;
			}
		}

		return false;
	}
}