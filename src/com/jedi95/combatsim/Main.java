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
	public static int simCount;
	public static int threads;
	public static boolean printLog;
	public static boolean charts;
	public static boolean printProgress;

	public static void main(String[] args) throws Exception {

		//Defaults
		simCount = 100000;
		printLog = false;
		targetHP = 2500000.0;
		useArmorDebuff = true;
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
		options.addOption("H", true, "Sets the target's HP. (default is 2,500,000)");
		options.addOption("D", false, "Disables the armor debuff on the target. (default is on)");
		options.addOption("g", false, "Generate comparison charts. (default is off)");
		
		CommandLineParser parser = new BasicParser();
		CommandLine cmd = parser.parse(options, args);

		//Handle help
		if (cmd.hasOption("-h")) {
			HelpFormatter helpFormatter = new HelpFormatter();
			helpFormatter.printHelp("combatsim", options);
			return;
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
			printLog = true;
		}
		
		//Run simulation
		if (!charts) {
			run();
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
		double averageDPS = calcAverage(simCount / 100, dpsResults.size() - (1 + simCount / 100));
		System.out.println(String.format("99%% DPS: Min = %.2f  Avg = %.2f  Max = %.2f", dpsResults.get(simCount / 100), averageDPS, dpsResults.get(dpsResults.size() - (1 + simCount / 100))));
		averageDPS = calcAverage(simCount / 20, dpsResults.size() - (1 + simCount / 20));
		System.out.println(String.format("95%% DPS: Min = %.2f  Avg = %.2f  Max = %.2f", dpsResults.get(simCount / 20), averageDPS, dpsResults.get(dpsResults.size() - (1 + simCount / 20))));
		averageDPS = calcAverage(simCount / 10, dpsResults.size() - (1 + simCount / 10));
		System.out.println(String.format("90%% DPS: Min = %.2f  Avg = %.2f  Max = %.2f", dpsResults.get(simCount / 10), averageDPS, dpsResults.get(dpsResults.size() - (1 + simCount / 10))));

		//Print simulation stats
		long simsPerSecond = 1000 * simCount / timeTaken;
		System.out.println(String.format("Simulation took %d ms  rate: %d/s\n", timeTaken, simsPerSecond));
	}

	private static double calcAverage(int startIndex, int endIndex) {
		double average = 0;
		for (int i = startIndex; i <= endIndex; i++) {
			average += dpsResults.get(i);
		}
		average = average / ((endIndex - startIndex) + 1);
		return average;
	}
	
	//Generates the DPS scaling chart
	public static void generateDPSScaling() throws Exception {

		System.out.println("Generating DPS Scaling chart...");

		//Set up datasets
		DefaultCategoryDataset dps_chart = new DefaultCategoryDataset();

		//Generate live data
		targetHP = 250000.0; //250K
		run();
		dps_chart.addValue(finalStats.getDPS(), "5.10", "250K");
		targetHP = 500000.0; //500K
		run();
		dps_chart.addValue(finalStats.getDPS(), "5.10", "500K");
		targetHP = 1000000.0; //1M
		run();
		dps_chart.addValue(finalStats.getDPS(), "5.10", "1M");
		targetHP = 1500000.0; //1.5M
		run();
		dps_chart.addValue(finalStats.getDPS(), "5.10", "1.5M");
		targetHP = 2500000.0; //2.5M
		run();
		dps_chart.addValue(finalStats.getDPS(), "5.10", "2.5M");

		//Create the charts
		JFreeChart chart1 = ChartFactory.createLineChart("DPS Scaling", "Target HP", "Average DPS", dps_chart, PlotOrientation.VERTICAL, true, true, false);

		//NOTE: bounds may need adjustment for larger changes
		CategoryPlot plot = (CategoryPlot) chart1.getPlot();
		plot.getRangeAxis().setLowerBound(3000);
		plot.getRangeAxis().setUpperBound(5000);

		//Output to file
		File lineChart1 = new File("dps_scaling.png");
		try {
			ChartUtilities.saveChartAsPNG(lineChart1, chart1, 1024, 768);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("DPS Scaling chart saved to: " + lineChart1.getAbsolutePath() + "\n");
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

		// create a dataset...
		XYSeriesCollection collection = new XYSeriesCollection();
		XYSeries xyLive = getSeriesFromMap("5.10", testDataLive);
		collection.addSeries(xyLive);

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
	public static Target createTarget(Simulator sim) {
		Target target = new Target(70, targetHP, 0.10, sim);
		target.setArmorRating(9475.0);
		target.setArmorDebuff(useArmorDebuff);
		target.setDamageTakenBuff(true);
		target.setInternalDamageBuff(true);
		return target;
	}

	//Creates a player
	public static Player createPlayer(Simulator sim1) {
		//create player
		Player player = new Player(70, sim1);

		//set up player
		//buffs
		player.setAgentBuff(true);
		player.setInquisitorBuff(true);
		player.setWarriorBuff(true);

		//companion buffs
		player.setCompAccBuff(true);
		player.setCompCritBuff(true);
		player.setCompSurgeBuff(true);

		//Stats
		//For the purpose of this simulator I'm using BIS 258
		//This includes all buffs + datacrons and a blue Prototype Nano-Infused Resolve Stim.
		//The values set here should not include class buffs, but should include stims.
		player.setMastery(7106.0);
		player.setPower(4936.0);
		player.setForcePower(5480.0);
		player.setAccuracyRating(746.0);
		player.setCriticalRating(1884.0);
		player.setAlacrityRating(1859.0);

		player.setMainhandMinDmg(1644.0);
		player.setMainhandMaxDmg(2466.0);

		//Skill tree buffs
		player.setArmorPenetration(0.1);

		//Add abilities (NOTE: evaluated in the order added, highest priority first!)
		player.abilities.put(Constants.Abilities.Discharge, new Discharge(player));
		player.abilities.put(Constants.Abilities.BallLightning, new BallLightning(player));
		player.abilities.put(Constants.Abilities.Assassinate, new Assassinate(player));
		player.abilities.put(Constants.Abilities.ReapingStrike, new ReapingStrike(player));
		player.abilities.put(Constants.Abilities.Maul, new Maul(player));
		player.abilities.put(Constants.Abilities.VoltaicSlash, new VoltaicSlash(player));
		player.abilities.put(Constants.Abilities.SaberStrike, new SaberStrike(player));

		//Initialize off-GCD abilities
		player.addOffAbility(Constants.OffAbilities.Recklessness, new Recklessness(player));
		player.addOffAbility(Constants.OffAbilities.PowerAdrenal, new PowerAdrenal(player));
		player.addOffAbility(Constants.OffAbilities.OverchargeSaber, new OverchargeSaber(player));
		player.addOffAbility(Constants.OffAbilities.ForceCloak, new ForceCloak(player));

		//Initialize effects
		player.addEffect(Constants.Effects.Voltage, new Effect(15, 0, "Voltage", 2, 2)); //Init to 2 stacks, assume lacerate pre-pull
		player.addEffect(Constants.Effects.Induction, new Effect(15, 0, "Induction", 2, 2)); //Init to 2 stacks, assume lacerate pre-pull
		player.addEffect(Constants.Effects.StaticCharge, new Effect(30, 0, "Static Charge", 3, 3)); //init to 3 stacks, assumes precasting recklessness
		player.addEffect(Constants.Effects.Duplicity, new Effect(10, -1000, "Duplicity", 0, 1));
		player.addEffect(Constants.Effects.DarkEmbrace, new Effect(15, 0, "Dark Embrace", 1, 1)); //init to 1 stack, assumes starting from cloak
		player.addEffect(Constants.Effects.ExploitiveStrikes, new Effect(10, -1000, "Exploitive Strikes", 0, 1));
		player.addEffect(Constants.Effects.Recklessness, new Effect(20, -1.5, "Recklessness", 3, 3)); //init to 3 stacks, assumes precasting
		player.addEffect(Constants.Effects.PowerAdrenal, new Effect(15, 0, PowerAdrenal.NAME, 1, 1)); //Assume using power adrenal at pull
		player.addEffect(Constants.Effects.OverchargeSaber, new Effect(15, -1000, OverchargeSaber.NAME, 0, 1));
		player.addEffect(Constants.Effects.SARelic, new Effect(6, -1000, SARelic.NAME, 0, 1));
		player.addEffect(Constants.Effects.FRRelic, new Effect(6, -1000, FRRelic.NAME, 0, 1));
		player.addEffect(Constants.Effects.ReapingStrikeCrit, new Effect(15, -1000, "Reaping Strike Crit", 0, 1));
		player.addEffect(Constants.Effects.StalkerCriticalBonus, new Effect(30, -1000, StalkerCriticalBonus.NAME, 0, 1));
		player.addEffect(Constants.Effects.StalkerDamageBonus, new Effect(15, -1000, StalkerDamageBonus.NAME, 0, 1));
		player.addEffect(Constants.Effects.ReapersRush, new Effect(10, -1000, "Reaper's Rush", 0, 1));

		//Add phantom stride
		player.setPhantomStride(new PhantomStride(player, -1000));
		
		//Initialize procs
		//FIXME: First 2 must be relics, 3rd must be surging charge, 4th must be autocrit
		player.addProc(new SARelic(player));
		player.addProc(new FRRelic(player));
		player.addProc(new SurgingCharge(player));
		player.addProc(new StalkerCriticalBonus(player));
		player.addProc(new Duplicity(player));
		player.addProc(new ReapingStrikeProc(player));
		player.addProc(new StalkerDamageBonus(player));
		player.addProc(new DarkEmbrace(player));

		return player;
	}
}