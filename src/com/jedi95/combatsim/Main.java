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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.apache.commons.cli.*;
import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.*;

public class Main {

	//State
	public static Stats finalStats;
	public static int completedCount;
	public static ArrayList<Double> dpsResults;

	//Simulation setup
	public static boolean useArmorDebuff;
	public static double targetHP;
	public static boolean useMod;
	public static int simCount;
	public static int threads;
	public static boolean printLog;
	public static boolean compare;
	public static boolean charts;
	public static boolean printProgress;

	public static void main(String[] args) throws Exception {

		//Defaults
		simCount = 100000;
		printLog = false;
		targetHP = 1000000.0;
		useArmorDebuff = true;
		useMod = false;
		compare = false;
		charts = false;
		printProgress = true;

		//Get default number of cores to use
		threads = Runtime.getRuntime().availableProcessors();

		//Handle arguments
		Options options = new Options();
		options.addOption("h", false, "Displays command line help.");
		options.addOption("t", true, "Sets the number of threads to use. (default is CPU core count)");
		options.addOption("i", true, "The number of iterations to simulate. (default is 100,000)");
		options.addOption("p", false, "Runs one simulation only, and prints the combat log.");
		options.addOption("H", true, "Sets the target's HP. (default is 1,000,000)");
		options.addOption("D", false, "Disables the armor debuff on the target. (default is on)");
		options.addOption("g", false, "Generate comparison charts. (default is off)");
		options.addOption("c", false, "Runs a comparison. (default is off)");
		options.addOption("m", false, "Runs with balance changes applied. (default is off)");
		
		CommandLineParser parser = new BasicParser();
		CommandLine cmd = parser.parse(options, args);

		//Handle help
		if (cmd.hasOption("-h")) {
			HelpFormatter helpFormatter = new HelpFormatter();
			helpFormatter.printHelp("combatsim", options);
			return;
		}

		//Handle balance changes
		if (cmd.hasOption("-m")) {
			useMod = true;
		}

		//Handle comparison option
		if (cmd.hasOption("-c")) {
			compare = true;
			useMod = false;
		}

		//Handle printing charts option
		if (cmd.hasOption("-g")) {
			charts = true;
			useMod = false;
		}

		//Handle thread count
		if (cmd.hasOption("-t")) {
			int value = Integer.parseInt(cmd.getOptionValue("t"));
			if (value > 0) {
				threads = value;
			}
			else {
				System.out.println("Error: thread count must be at least 1");
				return;
			}
		}

		//Handle iteration count
		if (cmd.hasOption("-i")) {
			int value = Integer.parseInt(cmd.getOptionValue("i"));
			if (value > 0) {
				simCount = value;
			}
			else {
				System.out.println("Error: simulation iteration count must be at least 1");
				return;
			}
		}

		//Handle armor debuff
		if (cmd.hasOption("-D")) {
			System.out.println("Armor debuff disabled");
			useArmorDebuff = false;
		}

		//Handle setting target HP
		if (cmd.hasOption("-H")) {
			double value = Double.parseDouble(cmd.getOptionValue("H"));
			if (value > 0) {
				targetHP = value;
			}
			else {
				System.out.println("Error: Target HP must be positive");
				return;
			}
		}

		//Handle printing one log
		if (cmd.hasOption("-p")) {
			simCount = 1;
			threads = 1;
			charts = false;
			compare = false;
			printLog = true;
		}
		
		//Run simulation
		if (!charts) {
			//Handle normal run
			if (!compare) {
				run();
			}
			//Handle comparison run
			else {
				run();
				useMod = true;
				run();
			}
		}
		//Handle chart output run
		else {
			//Don't print logs or progress for chart generation
			printLog = false;
			printProgress = false;
			
			//Generate the DPS distribution chart
			generateDPSDistribution();
			
			//Generate the DPS scaling chart
			generateDPSScaling();
		}
	}

	public static void run() throws Exception {

		//Calculate how many iterations per thread
		int threadSimCount = simCount / threads;
		//Handles cases where simCount / threads is not even
		int remainder = simCount % threads;

		//Run simulation
		finalStats = new Stats();
		finalStats.time = 0;
		dpsResults = new ArrayList<Double>();
		dpsResults.ensureCapacity(simCount);
		completedCount = 0;
		System.out.println("Starting simulation using " + threads + " threads");
		System.out.println("Number of iterations = " + simCount + " (Target HP = " + targetHP + ")");
		if (useMod) {
			System.out.println("Class changes ARE applied.");
		}
		else {
			System.out.println("Class changes ARE NOT applied.");
		}

		//Start threads
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < threads; i++) {
			if (remainder > 0) {
				(new SimulationThread(printLog, threadSimCount + 1, i)).start();
				remainder--;
			}
			else
			{
				(new SimulationThread(printLog, threadSimCount, i)).start();
			}
		}

		//loop and wait for threads to finish
		while (completedCount < threads) {
			Thread.sleep(100);
		}

		//Record time required
		long timeTaken = System.currentTimeMillis() - startTime;

		//Sort DPS array
		Collections.sort(dpsResults);

		//Print combined stats
		System.out.println("Simulation finished, printing stats\n");
		if (!charts) {
			CombatLog.printStats(finalStats);
		}

		//Print min/avg/max
		System.out.println(String.format("100%% DPS: Min = %.2f  Avg = %.2f  Max = %.2f", dpsResults.get(0), finalStats.getDPS(), dpsResults.get(dpsResults.size() - 1)));
		System.out.println(String.format("99%% DPS: Min = %.2f  Avg = %.2f  Max = %.2f", dpsResults.get(simCount / 100), finalStats.getDPS(), dpsResults.get(dpsResults.size() - (1 + simCount / 100))));
		System.out.println(String.format("95%% DPS: Min = %.2f  Avg = %.2f  Max = %.2f", dpsResults.get(simCount / 20), finalStats.getDPS(), dpsResults.get(dpsResults.size() - (1 + simCount / 20))));
		System.out.println(String.format("90%% DPS: Min = %.2f  Avg = %.2f  Max = %.2f", dpsResults.get(simCount / 10), finalStats.getDPS(), dpsResults.get(dpsResults.size() - (1 + simCount / 10))));

		//Print simulation stats
		long simsPerSecond = 1000 * simCount / timeTaken;
		System.out.println(String.format("Simulation took %d ms  rate: %d/s\n", timeTaken, simsPerSecond));
	}

	//Generates the DPS scaling chart
	public static void generateDPSScaling() throws Exception {

		System.out.println("Generating DPS Scaling chart...");

		//Set up dataset
		DefaultCategoryDataset dps_chart = new DefaultCategoryDataset();

		//Generate live data
		useMod = false;
		targetHP = 100000.0; //100K
		run();
		dps_chart.addValue(finalStats.getDPS(), "Live", "100K");
		targetHP = 250000.0; //250K
		run();
		dps_chart.addValue(finalStats.getDPS(), "Live", "250K");
		targetHP = 500000.0; //500K
		run();
		dps_chart.addValue(finalStats.getDPS(), "Live", "500K");
		targetHP = 1000000.0; //1M
		run();
		dps_chart.addValue(finalStats.getDPS(), "Live", "1M");
		targetHP = 1500000.0; //1M
		run();
		dps_chart.addValue(finalStats.getDPS(), "Live", "1.5M");

		//Generate mod data
		useMod = true;
		targetHP = 100000.0; //100K
		run();
		dps_chart.addValue(finalStats.getDPS(), "Mod", "100K");
		targetHP = 250000.0; //250K
		run();
		dps_chart.addValue(finalStats.getDPS(), "Mod", "250K");
		targetHP = 500000.0; //500K
		run();
		dps_chart.addValue(finalStats.getDPS(), "Mod", "500K");
		targetHP = 1000000.0; //1M
		run();
		dps_chart.addValue(finalStats.getDPS(), "Mod", "1M");
		targetHP = 1500000.0; //1M
		run();
		dps_chart.addValue(finalStats.getDPS(), "Mod", "1.5M");

		//Create the chart
		JFreeChart chart = ChartFactory.createLineChart("DPS Scaling", "Target HP", "Average DPS", dps_chart, PlotOrientation.VERTICAL, true, true, false);

		//NOTE: bounds may need adjustment for larger changes
		CategoryPlot plot = (CategoryPlot) chart.getPlot();
		plot.getRangeAxis().setLowerBound(3000);
		plot.getRangeAxis().setUpperBound(5000);

		//Output to file
        File lineChart = new File("dps_scaling.png");              
        try {
			ChartUtilities.saveChartAsPNG(lineChart, chart, 1024, 768);
		} catch (IOException e) {
			e.printStackTrace();
		}
        System.out.println("DPS Scaling chart saved to: " + lineChart.getAbsolutePath() + "\n");
	}

	public static void generateDPSDistribution() throws Exception {

		System.out.println("Generating DPS Distribution chart...");

		//Compile data for live chart
		run();
		HashMap<Integer, Integer> testDataLive = new HashMap<Integer, Integer>();
		for (int i = 0; i < dpsResults.size(); i++) {
			int value = (int) Math.round(dpsResults.get(i));
			int key = ((value + 16) / 32) * 32; //want sets of 32 DPS
			if (testDataLive.containsKey(key)) {
				Integer newVal = testDataLive.get(key) + 1;
				testDataLive.put(key, newVal);
			}
			else
			{
				testDataLive.put(key, 1);
			}
		}

		//Compile data for mod chart
		useMod = true;
		run();
		HashMap<Integer, Integer> testDataMod = new HashMap<Integer, Integer>();
		for (int i = 0; i < dpsResults.size(); i++) {
			int value = (int) Math.round(dpsResults.get(i));
			int key = ((value + 16) / 32) * 32; //want sets of 32 DPS
			if (testDataMod.containsKey(key)) {
				Integer newVal = testDataMod.get(key) + 1;
				testDataMod.put(key, newVal);
			}
			else
			{
				testDataMod.put(key, 1);
			}
		}

		// create a dataset...
		XYSeriesCollection collection = new XYSeriesCollection();
		XYSeries xyLive = getSeriesFromMap("Live", testDataLive);
		XYSeries xyMod = getSeriesFromMap("Mod", testDataMod);
		collection.addSeries(xyLive);
		collection.addSeries(xyMod);

		//Get the chart
		JFreeChart myChart = ChartFactory.createXYLineChart("DPS Distribution", "DPS", "Parse count", collection, PlotOrientation.VERTICAL, true, true, false);

		//Save to file
        File lineChart = new File("dps_distribution.png");              
        try {
			ChartUtilities.saveChartAsPNG(lineChart,myChart,1024,768);
		} catch (IOException e) {
			System.out.println("Failed to save chart!");
			e.printStackTrace();
		}
        System.out.println("DPS Distributon chart saved to: " + lineChart.getAbsolutePath() + "\n");
	}

	public static XYSeries getSeriesFromMap(String name, HashMap<Integer, Integer> inputData) {
		// loop through input and populate the xy series
		XYSeries xy = new XYSeries(name);
		for (int x : inputData.keySet()) {
			int y = inputData.get(x);
			xy.add(x, y);
		}
		return xy;
	}

	//Worker threads call this once complete
	public static synchronized void reportStats(int id, Stats s, ArrayList<Double> dps) {
		//Handle stats
		finalStats.mergeWith(s);
		dpsResults.addAll(dps);

		//Increment completed thread count
		completedCount++;
	}

	//Worker threads call this to report progress
	public static synchronized void reportProgress(int id, int percentComplete) {
		if (printProgress) {
			String progress = String.format("[%d] Simulating... %d%%", id, percentComplete);
			System.out.println(progress);
		}
	}

	//Create target (values are for an ops training dummy)
	public static Target createTarget(Simulator sim1) {
		Target target = new Target(55, targetHP, 0.10, sim1);
		target.setArmorRating(7500.0);
		target.setArmorDebuff(useArmorDebuff);
		return target;
	}

	//Creates a player
	public static Player createPlayer(Simulator sim1) {
		//create player
		Player player = new Player(55, sim1);

		//set up player
		//buffs
		player.setAgentBuff(true);
		player.setBountyHunterBuff(true);
		player.setInquisitorBuff(true);
		player.setWarriorBuff(true);

		//companion buffs
		player.setCompAccBuff(true);
		player.setCompCritBuff(true);
		player.setCompSurgeBuff(true);

		//Stats
		//For the purpose of this simulator I'm using BIS Dread Forged.
		//This includes all buffs + datacrons and a blue Prototype Nano-Infused Resolve Stim.
		//The values set here should not include class buffs, but should include stims.
		//http://swtor.askmrrobot.com/character/e3085878-1b15-4fda-8a89-5ea2db28fd02
		player.setWillpower(3243.0);
		player.setStrength(148.0);
		player.setPower(1214.0);
		player.setForcePower(2302.0);
		player.setAccuracy(440.0);
		player.setCritRating(461.0);
		player.setSurgeRating(564.0);
		player.setMainhandMinDmg(690.0);
		player.setMainhandMaxDmg(1036.0);

		//Skill tree buffs
		player.setSkillAccBuff(0.03);
		player.setArmorPenetration(0.09);

		//Add abilities (NOTE: evaluated in the order added, highest priority first!)
		player.abilities.add(new Discharge(player));
		player.abilities.add(new Shock(player));
		player.abilities.add(new Assassinate(player));
		player.abilities.add(new Maul(player));
		if (useMod) {
			player.abilities.add(new VoltaicSlashMod(player));
		}
		else {
			player.abilities.add(new VoltaicSlash(player));
		}
		player.abilities.add(new SaberStrike(player));

		//Initialize off-GCD abilities
		player.addOffAbility(new Blackout(player));
		player.addOffAbility(new Recklessness(player));
		player.addOffAbility(new PowerAdrenal(player));
		player.addOffAbility(new OverchargeSaber(player));
		player.addOffAbility(new ForceCloak(player));

		//Initialize effects
		player.addEffect(new Effect(15000, -1000000, "Voltage", 0, 2));
		player.addEffect(new Effect(10000, -1000000, "Induction", 0, 2));
		player.addEffect(new Effect(30000, -7500, "Static Charge", 3, 3)); //init to 3 stacks, assumes precasting
		player.addEffect(new Effect(10000, -1000000, "Duplicity", 0, 1));
		player.addEffect(new Effect(6000, 0, "Dark Embrace", 1, 1)); //init to 1 stack, assumes starting from cloak
		player.addEffect(new Effect(10000, -1000000, "Exploitive Strikes", 0, 1));
		player.addEffect(new Effect(20000, -7500, "Recklessness", 2, 2)); //init to 2 stacks, assumes precasting
		player.addEffect(new Effect(15000, -1000000, PowerAdrenal.NAME, 0, 1));
		player.addEffect(new Effect(15000, -1000000, OverchargeSaber.NAME, 0, 1));
		player.addEffect(new Effect(6000, -1000000, SARelic.NAME, 0, 1));
		player.addEffect(new Effect(6000, -1000000, FRRelic.NAME, 0, 1));

		//Initialize procs
		//FIXME: First 2 must be relics, and 3rd must be surging charge
		player.addProc(new SARelic(player));
		player.addProc(new FRRelic(player));
		if (useMod) {
			player.addProc(new SurgingChargeMod(player));
		}
		else {
			player.addProc(new SurgingCharge(player));
		}
		player.addProc(new Duplicity(player));

		return player;
	}
}