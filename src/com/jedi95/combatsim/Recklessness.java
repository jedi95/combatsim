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

public class Recklessness extends OffGCDAbility {

	public static final String NAME = "Recklessness";
	public static final int COOLDOWN = 90000;

	public Recklessness(Player player) {
		super(player, NAME, COOLDOWN, 82500); //Assumes recklessness used 7.5 seconds before fight starts
	}

	public boolean shouldUse(Target target) {
		if (!isReady()) {
			return false;
		}

		//Only use when we have 0 static charges OR we are about to cloak
		OffGCDAbility cloak = player.getOffAbility(ForceCloak.NAME);
		Effect staticCharge = player.getEffect("Static Charge");
		if (!staticCharge.isActive(player.sim.time()) || cloak.getTimeToReady() < Simulator.GCD_LENGTH) {
			return true;
		}
		else {
			return false;
		}
	}

	//Called to activate this ability
	public void use(Target target) {

		//Set last used time
		lastUsedTime = player.sim.time();

		//Apply recklessness stacks
		Effect reck = player.getEffect(NAME);
		reck.addStacks(2, player.sim.time());

		//Apply static charges
		Effect staticCharge = player.getEffect("Static Charge");
		staticCharge.addStacks(3, player.sim.time());
	}
}