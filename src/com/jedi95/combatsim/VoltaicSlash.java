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

public class VoltaicSlash extends Ability {

	//Ability details
	public static final String NAME = "Voltaic Slash";
	public static final double FORCE = 20;
	public static final double COOLDOWN = 0;
	public static final double DAMAGE_MULTI = 1.1; //From Thrashing blades + dominating slashes
	public static final double CRITICAL_BONUS = 0.0;
	public static final double SURGE_BONUS = 0.0;
	public static final int HIT_COUNT = 2;

	//Ability damage constants
	public static final AbilityDamage DAMAGE = 
			new AbilityDamage.AbilityDamageBuilder()
			.standardHealthPercentMin(0.075)
			.standardHealthPercentMax(0.075)
			.coefficient(0.75)
			.amountModifierPercent(-0.5)
			.isForce(false)
			.isInternal(false)
			.build();
	
	public VoltaicSlash(Player player)
	{
		super(player, NAME, FORCE, COOLDOWN, DAMAGE_MULTI, CRITICAL_BONUS, SURGE_BONUS, HIT_COUNT);
		damage = DAMAGE;
	}

	public void checkProcs(Target target, Hit hit, int hitCount) {

		//Add voltage and induction
		Effect voltage = player.getEffect(Constants.Effects.Voltage);
		voltage.addStacks(1, player.sim.time());
		Effect induction = player.getEffect(Constants.Effects.Induction);
		induction.addStacks(1, player.sim.time());

		//Call global handler
		super.checkProcs(target, hit, hitCount);
	}
}