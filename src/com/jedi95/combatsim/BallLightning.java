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

public class BallLightning extends Ability {

	//Ability details
	public static final String NAME = "Ball Lightning";
	public static final double FORCE = 32;
	public static final double COOLDOWN = 6;
	public static final double DAMAGE_MULTI = 1.0;
	public static final double CRITICAL_BONUS = 0.0;
	public static final double SURGE_BONUS = 0.05;
	public static final int HIT_COUNT = 1;

	public static final double INDUCTION_FORCE_REDUCTION = 8;
	public static final double VOLTAGE_CHANCE = 0.5;
	public static final double SECOND_HIT_CHANCE = 0.5;
	public static final double SECOND_HIT_DAMAGE_MULT = 0.5;
	
	//Ability damage constants
	public static final AbilityDamage DAMAGE = 
			new AbilityDamage.AbilityDamageBuilder()
			.standardHealthPercentMin(0.164)
			.standardHealthPercentMax(0.204)
			.coefficient(1.84)
			.amountModifierPercent(0)
			.isForce(true)
			.isInternal(false)
			.build();

	public BallLightning(Player player)
	{
		super(player, NAME, FORCE, COOLDOWN, DAMAGE_MULTI, CRITICAL_BONUS, SURGE_BONUS, HIT_COUNT);
		damage = DAMAGE;
	}

	//Handle induction force cost reduction
	public double getForceCost() {
		Effect induction = player.getEffect(Constants.Effects.Induction);
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
		Effect induction = player.getEffect(Constants.Effects.Induction);
		induction.resetStacks();

		//Consume recklessness charge
		if (hit.crit) {
			Effect reck = player.getEffect(Constants.Effects.Recklessness);
			if (reck.isActive(player.sim.time())){
				reck.consumeStacks(1);
			}
		}
	}

	//Add static charges
	public void checkProcs(Target target, Hit hit, int hitCount) {

		//Handle second shock hit
		if (player.random.nextDouble() <= SECOND_HIT_CHANCE) {

			//Get damage
			Hit secondShock = calculateHitDamage(player, target);
			secondShock.damage *= SECOND_HIT_DAMAGE_MULT;

			//Apply hit
			target.applyHit(secondShock);

			//Consume recklessness charge
			if (secondShock.crit) {
				Effect reck = player.getEffect(Constants.Effects.Recklessness);
				if (reck.isActive(player.sim.time())){
					reck.consumeStacks(1);
				}
			}
		}

		//Add static charges
		Effect voltage = player.getEffect(Constants.Effects.Voltage);
		if (voltage.isActive(player.sim.time())) {
			if (player.random.nextDouble() <= (VOLTAGE_CHANCE * voltage.getStacks())) {
				//Surging Charge
				player.getProc(2).handleProc(player, target, player.sim.time());
			}
		}

		//Handle ES
		if (hit.crit) {
			Effect es = player.getEffect(Constants.Effects.ExploitiveStrikes);
			es.addStacks(1, player.sim.time());
		}

		//Call global handler
		super.checkProcs(target, hit, hitCount);
	}

	//Important: this handles the ability priority list! check subclasses.
	public boolean shouldUse(Target target) {
		if (!canUse(target)) {
			return false;
		}

		Effect voltage = player.getEffect(Constants.Effects.Voltage);
		Effect induction = player.getEffect(Constants.Effects.Induction);
		double time = player.sim.time();

		//Check if voltage will fall off in the next 2 GCD
		if (voltage.getRemainingTime(time) < player.sim.getGCDLength() * 2) {
			return false;
		}
		
		//If we have 2 stacks of voltage
		if (voltage.isActive(time) && voltage.getStacks() == 2) {
			//If we have at least 1 induction stack
			if (induction.isActive(time) && induction.getStacks() >= 1) {
				return true;
			}
		}

		return false;
	}
}