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

public class PowerAdrenal extends OffGCDAbility {

	public static final String NAME = "Power Adrenal";
	public static final double COOLDOWN = 180;
	//Using blue quality Prototype Nano-Infused Attack Adrenal
	public static final double POWER_BOOST = 870.0;

	public PowerAdrenal(Player player) {
		super(player, NAME, COOLDOWN, -1.0, true); //Assume used right before pull
	}

	public boolean shouldUse(Target target) {
		if (!isReady()) {
			return false;
		}

		//Save this for when recklessness is up
		OffGCDAbility reck = player.getOffAbility(Constants.OffAbilities.Recklessness);
		Effect reckEffect = player.getEffect(Constants.Effects.Recklessness);
		if (reck.getTimeToReady() < player.sim.getGCDLength() || reckEffect.isActive(player.sim.time())) {
			return true;
		}
		else {
			//However, if we are very close to killing the target use anyway
			if (target.getHealth() < 160000) {
				return true;
			}
			return false;
		}
	}

	//Called to execute this ability on the specified target
	public void use(Target target) {

		//Set last used time
		lastUsedTime = player.sim.time();

		//Apply power adrenal
		Effect pow = player.getEffect(Constants.Effects.PowerAdrenal);
		pow.addStacks(1, player.sim.time());
	}
}