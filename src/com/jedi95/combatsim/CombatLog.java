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

public class CombatLog {

	private Simulator sim;
	private boolean doPrints;
	private Stats stats;

	public CombatLog(Simulator sim1, boolean printMessages) {
		sim = sim1;
		doPrints = printMessages;
		stats = new Stats();
	}

	public void logHit(Hit hit) {

		//Add to stats
		addHitToStats(hit);

		//Handle logging to stdout
		if (doPrints) {
			String timeStamp = String.format("[%.3f]", sim.time());

			String critString = "";
			if (hit.crit) {
				critString = "*";
			}

			String output = String.format("%s You hit Target with %s for %.2f%s damage", timeStamp, hit.name, hit.damage, critString);
			System.out.println(output);
		}
	}

	public void logAbility(OffGCDAbility ability) {
		//Handle logging to stdout
		if (doPrints) {
			String timeStamp = String.format("[%.3f]", sim.time());

			String output = String.format("%s You activate %s", timeStamp, ability.name);
			System.out.println(output);
		}
	}
	
	public void logStride() {
		if (doPrints) {
			String timeStamp = String.format("[%.3f]", sim.time());

			String output = String.format("%s You activate %s", timeStamp, "Phantom Stride");
			System.out.println(output);
		}
	}

	public void finishStats(double totalTime) {
		stats.time = totalTime;
	}

	public static void printStats(Stats stats) {

		//Loop through all abilities
		for (String name : stats.abilityList.keySet()) {

			AbilityStats as = stats.abilityList.get(name);

			//Print stats for each ability
			double percentDamage = as.totalDamage / stats.totals.totalDamage;
			double avgNormal = as.normalDamage / as.normalHits;
			double avgCrit = as.critDamage / as.critHits;
			double avgDmg = as.totalDamage / as.timesUsed;

			System.out.println("Ability: " + name);
			System.out.println(String.format("Normal hits: %d,  Crits: %d,  Total: %d", as.normalHits, as.critHits, as.timesUsed));
			System.out.println(String.format("Damage avg:  Normal: %.2f,  Crits: %.2f,  Combined: %.2f", avgNormal, avgCrit, avgDmg));
			System.out.println(String.format("Percent of hits that crit: %.2f%%", 100.0 * as.critHits / as.timesUsed));
			System.out.println(String.format("%.2f%% of total damage done", percentDamage * 100.0));
			System.out.println("");
		}

		//Print totals
		double avgNormalG = stats.totals.normalDamage / stats.totals.normalHits;
		double avgCritG = stats.totals.critDamage / stats.totals.critHits;
		double avgDmgG = stats.totals.totalDamage / stats.totals.timesUsed;
		System.out.println("Global stats:");
		System.out.println(String.format("Normal hits: %d,  Crits: %d,  Total: %d", stats.totals.normalHits, stats.totals.critHits, stats.totals.timesUsed));
		System.out.println(String.format("Damage avg:  Normal: %.2f,  Crits: %.2f,  Combined: %.2f", avgNormalG, avgCritG, avgDmgG));
		System.out.println(String.format("Percent of hits that crit: %.2f%%\n", 100.0 * stats.totals.critHits / stats.totals.timesUsed));
	}

	private void addHitToStats(Hit hit) {
		AbilityStats as = stats.getAbility(hit.name);
		as.totalDamage += hit.damage;
		as.timesUsed++;
		stats.totals.timesUsed++;
		stats.totals.totalDamage += hit.damage;
		if (hit.crit) {
			as.critHits++;
			as.critDamage += hit.damage;
			stats.totals.critHits++;
			stats.totals.critDamage += hit.damage;
		}
		else {
			as.normalHits++;
			as.normalDamage += hit.damage;
			stats.totals.normalHits++;
			stats.totals.normalDamage += hit.damage;
		}
	}

	public Stats getStats() {
		return stats;
	}
}