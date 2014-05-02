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
import java.util.HashMap;
import java.util.Random;

public class Player {

	//Balance constants
	public static final int BASE_REGEN = 8 * Calc.FORCE_MULTI;
	public static final double DARK_EMBRACE_FORCE_REGEN_MULTI = 1.5;
	public static final int MAX_FORCE = 110 * Calc.FORCE_MULTI;

	//Character constants
	private int level;

	//Character stats
	private double willpower;
	private double strength;
	private double power;
	private double forcePower;
	private double accuracy;
	private double critRating;
	private double surgeRating;
	private double mainhandMinDmg;
	private double mainhandMaxDmg;

	//Class buffs
	private boolean hasWarriorBuff;
	private boolean hasInquisitorBuff;
	private boolean hasAgentBuff;
	private boolean hasBountyHunterBuff;

	//Companion buffs
	private boolean compAccuracyBuff;
	private boolean compSurgeBuff;
	private boolean compCritBuff;

	//Skill tree buffs
	private double skillAccBuff;
	private double armorPenetration;

	//State
	//The random generator is stored on the Player object instead of CalcUtil because Math.random()
	//is shared between threads. This should improve the multicore scaling of the simulator.
	public Random random;
	public Simulator sim; //reference to simulator that created the player

	private int force; //stored as (force * 100) to prevent FP math errors
	private HashMap<String, Effect> effects;
	public ArrayList<Ability> abilities;
	private HashMap<String, OffGCDAbility> offAbilities;
	public ArrayList<Proc> procs;

	//Constructor
	public Player(int newLevel, Simulator sim1){
		level = newLevel;
		sim = sim1;
		random = new Random();
		force = MAX_FORCE;
		effects = new HashMap<String, Effect>();
		abilities = new ArrayList<Ability>();
		offAbilities= new HashMap<String, OffGCDAbility>();
		procs = new ArrayList<Proc>();
	}

	//properties
	//level constant
	public int getLevel() {
		return level;
	}

	//stats
	public double getWillpower() {

		//handle relic bonus
		double bonus = 0.0;
		Effect fr = getEffect(FRRelic.NAME);
		if (fr.isActive(sim.time())) {
			bonus = FRRelic.MAINSTAT_BOOST;
		}

		//Handle buff
		double buffMulti = 1.0;
		if (hasInquisitorBuff){
			buffMulti = 1.05;
		}

		return (willpower + bonus) * buffMulti;
	}

	public void setWillpower(double value) {
		willpower = value;
	}

	public double getStrength() {
		double buffMulti = 1.0;
		if (hasInquisitorBuff){
			buffMulti = 1.05;
		}
		return strength * buffMulti;
	}

	public void setStrength(double value) {
		strength = value;
	}

	public double getPower() {
		//Handle adrenal boost
		double powerBonus = power;
		Effect pow = getEffect(PowerAdrenal.NAME);
		if (pow.isActive(sim.time())) {
			powerBonus += PowerAdrenal.POWER_BOOST;
		}

		//handle relic boost
		Effect sa = getEffect(SARelic.NAME);
		if (sa.isActive(sim.time())) {
			powerBonus += SARelic.POWER_BOOST;
		}

		return powerBonus;
	}

	public void setPower(double value) {
		power = value;
	}

	public double getForcePower() {
		return forcePower;
	}

	public void setForcePower(double value) {
		forcePower = value;
	}

	public double getAccuracy() {
		return accuracy;
	}

	public void setAccuracy(double value) {
		accuracy = value;
	}

	public double getCritRating() {
		return critRating;
	}

	public void setCritRating(double value) {
		critRating = value;
	}

	public double getSurgeRating() {
		return surgeRating;
	}

	public void setSurgeRating(double value) {
		surgeRating = value;
	}

	public double getMainhandMinDmg() {
		return mainhandMinDmg;
	}

	public void setMainhandMinDmg(double value) {
		mainhandMinDmg = value;
	}

	public double getMainhandMaxDmg() {
		return mainhandMaxDmg;
	}

	public void setMainhandMaxDmg(double value) {
		mainhandMaxDmg = value;
	}

	//buffs
	public boolean getWarriorBuff() {
		return hasWarriorBuff;
	}

	public void setWarriorBuff(boolean value) {
		hasWarriorBuff = value;
	}

	public boolean getInquisitorBuff() {
		return hasInquisitorBuff;
	}

	public void setInquisitorBuff(boolean value) {
		hasInquisitorBuff = value;
	}

	public boolean getAgentBuff() {
		return hasAgentBuff;
	}

	public void setAgentBuff(boolean value) {
		hasAgentBuff = value;
	}

	public boolean getBountyHunterBuff() {
		return hasBountyHunterBuff;
	}

	public void setBountyHunterBuff(boolean value) {
		hasBountyHunterBuff = value;
	}

	//companion buffs
	public boolean getCompAccBuff() {
		return compAccuracyBuff;
	}

	public void setCompAccBuff(boolean value) {
		compAccuracyBuff = value;
	}

	public boolean getCompSurgeBuff() {
		return compSurgeBuff;
	}

	public void setCompSurgeBuff(boolean value) {
		compSurgeBuff = value;
	}

	public boolean getCompCritBuff() {
		return compCritBuff;
	}

	public void setCompCritBuff(boolean value) {
		compCritBuff = value;
	}

	//Skill tree buffs
	public double getSkillAccBuff() {
		return skillAccBuff;
	}

	public void setSkillAccBuff(double value){
		skillAccBuff = value;
	}

	public double getArmorPenetration() {
		return armorPenetration;
	}

	public void setArmorPenetration(double value){
		armorPenetration = value;
	}

	//state access
	public int getForce() {
		return force;
	}

	public void consumeForce(int amount) {
		force -= amount;
	}

	public void addForce(int amount) {
		force += amount;
		force = Math.min(force, MAX_FORCE); //cap force at maximum
	}

	public HashMap<String, Effect> getEffects() {
		return effects;
	}

	public Effect getEffect(String name) {
		return effects.get(name);
	}

	public void addEffect(Effect e) {
		effects.put(e.getName(), e);
	}

	public OffGCDAbility getOffAbility(String name) {
		return offAbilities.get(name);
	}

	public void addOffAbility(OffGCDAbility a) {
		offAbilities.put(a.getName(), a);
	}

	public Proc getProc(int index) {
		return procs.get(index);
	}

	public void addProc(Proc p) {
		procs.add(p);
	}

	public void handleOffGCD(Target target) {
		for (OffGCDAbility a : offAbilities.values()) {
			if (a.shouldUse(target)) {
				a.use(target);
				sim.getLog().logAbility(a);
			}
		}
	}

	public void handleProcs(Target target, Hit hit, int hitCount) {
		for (int i = 0; i < procs.size(); i++) {
			Proc p = procs.get(i);
			p.check(this, target, hit, sim.time(), hitCount);
		}
	}

	public Ability getAbility(String name) {
		for (Ability a : abilities) {
			if (a.getName().equals(name)) {
				return a;
			}
		}
		return null;
	}
}