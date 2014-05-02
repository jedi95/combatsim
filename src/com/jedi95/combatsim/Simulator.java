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

public class Simulator {

	public static final int GCD_LENGTH = 1500;
	public static final int SIM_STEP = 500;

	private long _time; //Time in ms since simulator started

	private Player player;
	private Target target;
	private long lastgcd;
	private CombatLog log;

	public Simulator(boolean printLog)
	{
		_time = 0;
		lastgcd = 0 - GCD_LENGTH;
		log = new CombatLog(this, printLog);
	}

	public void start(Player player1, Target target1) {
		//Reset time
		_time = 0;

		//Read player and target
		player = player1;
		target = target1;

		//Start sim
		run();
	}

	private void run() {
		while (!target.isDead()) {
			step();
			_time += SIM_STEP;
		}
		//Finalize stats
		log.finishStats(_time);
	}

	//Only call this after run() has finished!
	public void printStats() {
		CombatLog.printStats(log.getStats());
	}

	private void step() {

		//Handle force regen
		player.addForce(getForceRegen(SIM_STEP));

		//handle activating passive abilities
		player.handleOffGCD(target);

		//handle GCD
		if (lastgcd + GCD_LENGTH <= _time) {

			//Select and use ability
			for (int i = 0; i < player.abilities.size(); i++){
				Ability ability = player.abilities.get(i);
				if (ability.shouldUse(target)){
					ability.use(target);
					break;
				}
			}
			//Update last GCD
			lastgcd = _time;
		}
	}

	//Returns how much force would be generated over some period of time. NOTE: ignores dark embrace falling off! only valid for low duration.
	public int getForceRegen(int duration) {
		int forceToAdd;
		Effect darkEmbrace = player.getEffect("Dark Embrace");
		if (darkEmbrace.isActive(_time))
		{
			forceToAdd = (int) Math.round(Player.BASE_REGEN * Player.DARK_EMBRACE_FORCE_REGEN_MULTI * (duration / 1000.0));
		}
		else
		{
			forceToAdd = (int) Math.round(Player.BASE_REGEN * (duration / 1000.0));
		}
		return forceToAdd;
	}

	public long time(){
		return _time;
	}

	public CombatLog getLog() {
		return log;
	}

	public Target getTarget() {
		return target;
	}
}