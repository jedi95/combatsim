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

public class SurgingCharge extends Proc {

	//Effect constants
	public static final String NAME = "Surging Charge";
	public static final double COOLDOWN = 6;
	public static final double CHANCE = 0.5;
	public static final double SABER_CONDUIT_ICD = 12;
	public static final double SABER_CONDUIT_FORCE = 9;
	public static final double SABER_CONDUIT_DURATION = 9;
	
	protected double lastSaberConduit = -1000;
	public AbilityDamage damage;
	
	//Ability damage constants
	public static final AbilityDamage DAMAGE = 
			new AbilityDamage.AbilityDamageBuilder()
			.standardHealthPercentMin(0.045)
			.standardHealthPercentMax(0.045)
			.coefficient(0.45)
			.amountModifierPercent(0)
			.isForce(true)
			.isInternal(true)
			.build();
	
	public SurgingCharge(Player player) {
		super(player, NAME, COOLDOWN, CHANCE, true);
		damage = DAMAGE;
	}

	//Need this one to handle overcharge saber proc chance increase
	public double getProcChance() {
		double chance = procChance;
		Effect os = player.getEffect(Constants.Effects.OverchargeSaber);
		if (os.isActive(player.sim.time())) {
			chance += OverchargeSaber.SURGING_CHARGE_CHANCE_BONUS;
		}
		return chance;
	}

	//Checks if the proc should activate
	public void check(Player player, Target target, Hit hit, double time, int hitCount) {
		//If not on ICD
		if (isReady() && hit.ability != null && !hit.ability.getDamage().isForce) {

			//Check chance
			if (player.random.nextDouble() <= getProcChance()) {
				handleProc(player, target, time);
				lastActive = time; //reset timer for ICD
			}
		}
	}
	
	//Handles the proc
	public void handleProc(Player player, Target target, double time) {

		//Add static charge
		Effect sc = player.getEffect(Constants.Effects.StaticCharge);
		sc.addStacks(1, time);

		//Saber conduit
		if (lastSaberConduit + SABER_CONDUIT_ICD <= time) {
			player.addForce(SABER_CONDUIT_FORCE);
			lastSaberConduit = time;
		}

		//Apply damage
		Hit hit = getHitDamage(player);
		if (hit.crit) {
			Effect es = player.getEffect(Constants.Effects.ExploitiveStrikes);
			es.addStacks(1, player.sim.time());
		}
		target.applyHit(hit);
	}

	public Hit getHitDamage(Player player) {

		Hit hit = new Hit(null, Calc.calculateDamage(player, player.sim.getTarget(), damage), false, damage.isForce, getName());

		//Handle crits
		double critChance = Calc.getCritChance(player);

		boolean isCrit = player.random.nextDouble() <= critChance;
		if (isCrit)
		{
			hit.damage *= Calc.getSuperCritMultiplier(player, critChance);
			hit.crit = true;
		}

		//Handle overcharge saber damage bonus
		Effect os = player.getEffect(Constants.Effects.OverchargeSaber);
		if (os.isActive(player.sim.time())) {
			hit.damage *= OverchargeSaber.SURGING_CHARGE_DAMAGE_MULTI;
		}

		return hit;
	}
}