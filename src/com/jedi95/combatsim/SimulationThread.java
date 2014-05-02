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

import java.util.ArrayList;

public class SimulationThread extends Thread {

	private Stats combined;
	private int runCount;
	private int id;
	public static final int UPDATE_COUNT = 20; //How many progress updates to send
	private boolean printLog;

	//Set up
	public SimulationThread(boolean printLog, int timesToRun, int i) {

		this.printLog = printLog;

		//Set up how many times to run
		runCount = timesToRun;

		//Give this thread an ID
		id = i;

		//Create combined stats
		combined = new Stats();
		combined.time = 0;
	}

	//Run simulations
	public void run() {
		ArrayList<Double> dps = new ArrayList<Double>();
		dps.ensureCapacity(runCount);

		int updates = runCount / UPDATE_COUNT;
		int onePercent = runCount / 100;

		for (int i = 0; i < runCount; i++) {

			//Report progress
			if (runCount >= UPDATE_COUNT && i % updates == 0){
				Main.reportProgress(id, i / onePercent);
			}

			//Create simulator
			Simulator sim = new Simulator(printLog);

			//Create player
			Player player = Main.createPlayer(sim);

			//Create target
			Target target = Main.createTarget(sim);

			//Run sim
			sim.start(player, target);

			//Get stats
			Stats result = sim.getLog().getStats();

			//Add to global stats
			dps.add(result.getDPS());
			combined.mergeWith(result);
		}

		//Report stats back to the main thread
		Main.reportStats(id, combined, dps);
	}
}