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
	public static final int RECKLESSNESS_COOLDOWN_REDUCTION = 60;
	public static final double COOLDOWN = 75;

	public ForceCloak(Player player) {
		super(player, NAME, COOLDOWN, -72, false);
	}

	//Called to execute this ability on the specified target
	public void use(Target target) {

		//Set last used time
		lastUsedTime = player.sim.time();

		//reduce cooldown on recklessness
		player.getOffAbility(Constants.OffAbilities.Recklessness).reduceCooldown(RECKLESSNESS_COOLDOWN_REDUCTION);

		//Add dark embrace
		Effect de = player.getEffect(Constants.Effects.DarkEmbrace);
		de.addStacks(1, player.sim.time());
	}
	
	//Important: this handles the ability priority list! check subclasses.
	public boolean shouldUse(Target target) {
		if (!isReady()) {
			return false;
		}
		
		//If recklessness is not about to come off CD
		OffGCDAbility reck = player.getOffAbility(Constants.OffAbilities.Recklessness);
		double timeToReady = reck.getTimeToReady();
		if (timeToReady > 45) {
			return true;
		}
		
		return false;
	}
}