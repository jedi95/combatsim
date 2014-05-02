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

import java.util.HashMap;

public class Stats {

	public HashMap<String, AbilityStats> abilityList;
	public AbilityStats totals;
	public long time;

	public Stats() {
		abilityList = new HashMap<String, AbilityStats>();
		totals = new AbilityStats();
	}

	//Safe get method that handles NULL
	public AbilityStats getAbility(String name) {
		if (abilityList.containsKey(name)) {
			return abilityList.get(name);
		}
		else {
			AbilityStats stats = new AbilityStats();
			abilityList.put(name, stats);
			return stats;
		}
	}

	public void mergeWith(Stats s) {

		//Add time
		time += s.time;

		//Add totals
		totals = AbilityStats.addStats(totals, s.totals);

		//Loop through all abilities
		for (String name : s.abilityList.keySet()) {
			AbilityStats as1 = getAbility(name);
			AbilityStats as2 = s.getAbility(name);
			as1 = AbilityStats.addStats(as1, as2);
		}
	}

	public double getDPS() {
		return 1000.0 * totals.totalDamage / time;
	}
}