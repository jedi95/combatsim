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

public class OverchargeSaber extends OffGCDAbility {

	public static final String NAME = "Overcharge Saber";
	public static final double COOLDOWN = 120;
	public static final double SURGING_CHARGE_CHANCE_BONUS = 0.25;
	public static final double SURGING_CHARGE_DAMAGE_MULTI = 1.5;

	public OverchargeSaber(Player player) {
		super(player, NAME, COOLDOWN, (0.25 + (Simulator.BASE_GCD_LENGTH / Calc.getAlacrity(player)) * 2) - COOLDOWN / Calc.getAlacrity(player), true);
	}

	//Called to execute this ability on the specified target
	public void use(Target target) {

		//Set last used time
		lastUsedTime = player.sim.time();

		//Add Effect
		Effect os = player.getEffect(Constants.Effects.OverchargeSaber);
		os.addStacks(1, player.sim.time());

		//Overcharge saber is a self heal, it can proc relics
		checkProcs();
	}
}