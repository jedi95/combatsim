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

public class SurgingChargeMod extends Proc {

	//Damage constants
	public static final double standardHealthPercentMin = 0.05;
	public static final double standardHealthPercentMax = 0.05;
	public static final double coefficient = 0.5;
	public static final double amountModifierPercent = 0.01;
	public static final boolean IS_SPECIAL = false;
	public static final boolean IS_FORCE = true;
	public static final boolean IS_INTERNAL = true;

	//Effect constants
	public static final String NAME = "Surging Charge";
	public static final int COOLDOWN = 4500; //in ms
	public static final double CHANCE = 0.30;
	public static final int SABER_CONDUIT_ICD = 9000;
	public static final int SABER_CONDUIT_FORCE = 10 * Calc.FORCE_MULTI;

	public AbilityDamage damage;
	protected long lastSaberConduit = -1000000;

	public SurgingChargeMod(Player player) {
		super(player, NAME, COOLDOWN, CHANCE);
		damage = new AbilityDamage(standardHealthPercentMin, standardHealthPercentMax, coefficient, amountModifierPercent, IS_SPECIAL, IS_FORCE, IS_INTERNAL);
	}

	//Need this one to handle overcharge saber proc chance increase
	public double getProcChance() {
		Effect os = player.getEffect(OverchargeSaber.NAME);
		if (os.isActive(player.sim.time())) {
			return procChance + OverchargeSaber.SURGING_CHARGE_CHANCE_BONUS;
		}
		return procChance;
	}

	//Checks if the proc should activate
	public void check(Player player, Target target, long time, int hitCount) {
		//If not on ICD
		if (lastActive + cooldown <= time) {

			//Check chance
			for (int i = 0; i < hitCount; i++) {
				if (player.random.nextDouble() <= getProcChance()) {
					handleProc(player, target, time);
					lastActive = time; //reset timer for ICD
					break;
				}
			}
		}
	}

	//Handles the proc
	public void handleProc(Player player, Target target, long time) {

		//Add static charge
		Effect sc = player.getEffect("Static Charge");
		sc.addStacks(1, time);

		//Saber conduit
		if (lastSaberConduit + SABER_CONDUIT_ICD <= time) {
			player.addForce(SABER_CONDUIT_FORCE);
			lastSaberConduit = time;
		}

		//Apply damage
		Hit hit = getHitDamage(player);
		if (hit.crit) {
			Effect es = player.getEffect("Exploitive Strikes");
			es.addStacks(1, player.sim.time());
		}
		target.applyHit(hit);
	}

	public Hit getHitDamage(Player player) {

		//Using hardcoded damage for lack of a better option. No idea how this gets calculated.
		Hit hit = new Hit(NAME, Calc.calculateDamage(player, player.sim.getTarget(), damage), false);

		//Handle crits
		double critChance = Calc.getForceCritChance(player);
		Effect voltage = player.getEffect("Voltage");
		if (voltage.isActive(player.sim.time())) {
			if (Main.useMod) {
				critChance += VoltaicSlashMod.VOLTAGE_FORCE_CRIT_BONUS * voltage.getStacks();
			}
			else {
				critChance += VoltaicSlash.VOLTAGE_FORCE_CRIT_BONUS * voltage.getStacks();
			}
		}

		boolean isCrit = player.random.nextDouble() <= critChance;
		if (isCrit)
		{
			hit.damage *= Calc.getCriticalDamageMultiplier(player);
			hit.crit = true;
		}

		//Handle overcharge saber damage bonus
		Effect os = player.getEffect(OverchargeSaber.NAME);
		if (os.isActive(player.sim.time())) {
			hit.damage *= OverchargeSaber.SURGING_CHARGE_DAMAGE_MULTI;
		}

		return hit;
	}
}