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

	public static final double BASE_GCD_LENGTH = 1.5;
	public static final double SIM_STEP = 0.1;

	private double _time; //Time in ms since simulator started

	private Player player;
	private Target target;
	private double nextgcd;
	private CombatLog log;

	public Simulator(boolean printLog)
	{
		_time = 0;
		nextgcd = 0;
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
		if (nextgcd <= _time) {

			//If we should use phantom stride
			PhantomStride phantomStride = player.getPhantomStride();
			if (phantomStride.shouldUse()) {
				phantomStride.use();
				
				//Update last GCD
				nextgcd = _time + PhantomStride.GCD_DELAY;
			}
			else {
				//Select and use ability
				for (Constants.Abilities e: Constants.Abilities.values())
				{
					Ability ability = player.abilities.get(e);
					if (ability.shouldUse(target)){
						ability.use(target);
						break;
					}
				}
				//Update next GCD
				nextgcd = _time + getGCDLength();
			}
		}
	}

	//Returns how much force would be generated over some period of time. NOTE: ignores dark embrace falling off! only valid for low duration.
	public double getForceRegen(double duration) {
		
		double forceToAdd = Player.BASE_REGEN * Calc.getAlacrity(player) * duration;
		Effect darkEmbrace = player.getEffect(Constants.Effects.DarkEmbrace);
		
		if (darkEmbrace.isActive(_time))
		{
			forceToAdd *= Player.DARK_EMBRACE_FORCE_REGEN_MULTI;
		}

		return forceToAdd;
	}

	public double getGCDLength() {
		return BASE_GCD_LENGTH / Calc.getAlacrity(player);
	}
	
	public double time(){
		return _time;
	}

	public CombatLog getLog() {
		return log;
	}

	public Target getTarget() {
		return target;
	}
}