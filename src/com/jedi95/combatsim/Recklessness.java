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
	public static final double COOLDOWN = 75;

	public Recklessness(Player player) {
		super(player, NAME, COOLDOWN, -1.5, true); //Assumes recklessness used just before fight starts
	}

	public boolean shouldUse(Target target) {
		if (!isReady()) {
			return false;
		}

		//Only use when we have 0 static charges
		Effect staticCharge = player.getEffect(Constants.Effects.StaticCharge);
		if (!staticCharge.isActive(player.sim.time())) {
			return true;
		}
		
		return false;
	}

	//Called to activate this ability
	public void use(Target target) {

		//Set last used time
		lastUsedTime = player.sim.time();

		//Apply recklessness stacks
		Effect reck = player.getEffect(Constants.Effects.Recklessness);
		reck.addStacks(2, player.sim.time());

		//Apply static charges
		Effect staticCharge = player.getEffect(Constants.Effects.StaticCharge);
		staticCharge.addStacks(3, player.sim.time());
	}
}