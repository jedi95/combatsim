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

public class ForceCloak extends OffGCDAbility {

	public static final String NAME = "Force Cloak";
	public static final int RECKLESSNESS_COOLDOWN_REDUCTION = 60000;
	//NOTE: Setting this to 102 seconds to save complexity.
	//The optimal force regen rotation looks like this:
	//Blackout -> 6s -> Force Cloak -> 6s -> Blackout -> 45s -> Blackout -> 45s -> repeat
	//This takes 102 seconds
	public static final int COOLDOWN = 102000;

	public ForceCloak(Player player) {
		super(player, NAME, COOLDOWN, 15000);
	}

	//Called to execute this ability on the specified target
	public void use(Target target) {

		//Set last used time
		lastUsedTime = player.sim.time();

		//Reset cooldown on Blackout
		player.getOffAbility(Blackout.NAME).finishCooldown();

		//reduce cooldown on recklessness
		player.getOffAbility(Recklessness.NAME).reduceCooldown(RECKLESSNESS_COOLDOWN_REDUCTION);

		//Add dark embrace
		Effect de = player.getEffect("Dark Embrace");
		de.addStacks(1, player.sim.time());
	}
}