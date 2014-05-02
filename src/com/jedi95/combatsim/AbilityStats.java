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

public class AbilityStats {

	public int timesUsed;
	public int critHits;
	public int normalHits;
	public double totalDamage;
	public double normalDamage;
	public double critDamage;

	public AbilityStats() {
		timesUsed = 0;
		critHits = 0;
		normalHits = 0;
		totalDamage = 0.0;
		normalDamage = 0.0;
		critDamage = 0.0;
	}

	public static AbilityStats addStats(AbilityStats s1, AbilityStats s2) {

		//Add totals
		s1.timesUsed += s2.timesUsed;
		s1.normalHits += s2.normalHits;
		s1.critHits += s2.critHits;
		s1.totalDamage += s2.totalDamage;
		s1.normalDamage += s2.normalDamage;
		s1.critDamage += s2.critDamage;

		return s1;
	}
}