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

public abstract class Proc {

	protected double cooldown;
	protected double procChance;
	protected double lastActive;
	protected String name;
	protected Player player;
	protected boolean useAlacrity;

	public Proc(Player player, String name, double cooldown, double procChance, boolean useAlacrity) {
		this.player = player;
		this.name = name;
		this.cooldown = cooldown;
		this.procChance = procChance;
		this.useAlacrity = useAlacrity;
		this.lastActive = -1000;
	}

	public String getName() {
		return name;
	}

	public double getProcChance() {
		return procChance;
	}

	public boolean isReady() {
		return lastActive + getCooldown() <= player.sim.time();
	}
	
	//Checks if the proc should activate
	public void check(Player player, Target target, Hit hit, double time, int hitCount) {
		//If not on ICD
		if (isReady()) {

			//Check chance
			if (player.random.nextDouble() <= getProcChance()) {
				handleProc(player, target, time);
				lastActive = time; //reset timer for ICD
			}
		}
	}

	public double getCooldown() {
		if (useAlacrity) {
			return cooldown / Calc.getAlacrity(player);
		}
		else
		{
			return cooldown;
		}
	}
	
	public abstract void handleProc(Player player, Target target, double time);

	public Hit getHitDamage(Player player) {
		return new Hit();
	}
	
	public boolean shouldUseAlacrity() {
		return useAlacrity;
	}
	
	//Returns how long until the ability is ready
	public double getTimeToReady() {
		if (isReady()){
			return 0;
		}
		else
		{
			return (lastActive + getCooldown()) - player.sim.time();
		}
	}
}