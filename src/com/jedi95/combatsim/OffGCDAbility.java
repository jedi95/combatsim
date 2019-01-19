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

public class OffGCDAbility {
	protected String name;
	protected double cooldown;
	protected double lastUsedTime;
	protected boolean useAlacrity;

	protected Player player;

	public OffGCDAbility(Player player, String name, double cooldown, double initialUsedTime, boolean useAlacrity){
		this.player = player;
		this.name = name;
		this.cooldown = cooldown;
		this.useAlacrity = useAlacrity;
		this.lastUsedTime = initialUsedTime;
	}

	public String getName() {
		return name;
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
	
	public boolean shouldUseAlacrity() {
		return useAlacrity;
	}

	//Returns true if the ability is not currently on cooldown
	public boolean isReady() {
		return lastUsedTime + getCooldown() <= player.sim.time();
	}

	//Important: this handles the ability priority list! check subclasses.
	public boolean shouldUse(Target target) {
		return isReady(); //Parent simply returns true if usable
	}

	//Returns how long until the ability is ready
	public double getTimeToReady() {
		if (isReady()){
			return 0;
		}
		else
		{
			return (lastUsedTime + getCooldown()) - player.sim.time();
		}
	}

	//Called to execute this ability on the specified target
	public void use(Target target) {

		//Set last used time
		lastUsedTime = player.sim.time();

		//Handle procs
		checkProcs();
	}

	//Handles procs that can occur when this ability is used.
	public void checkProcs() {
		//FIXME: First 2 are relics
		player.getProc(0).check(player, player.sim.getTarget(), new Hit(), player.sim.time(), 1);
		player.getProc(1).check(player, player.sim.getTarget(), new Hit(), player.sim.time(), 1);
		return;
	}

	//Instantly finishes the cooldown of the ability
	public void finishCooldown() {
		lastUsedTime = player.sim.time() - getCooldown();
	}

	//Removes X seconds from the current cooldown
	public void reduceCooldown(double seconds) {
		lastUsedTime -= seconds;
	}
}