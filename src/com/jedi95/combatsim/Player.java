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
import java.util.EnumMap;
import java.util.Random;

public class Player {

	//Balance constants
	public static final double BASE_REGEN = 8;
	public static final double DARK_EMBRACE_FORCE_REGEN_MULTI = 1.25;
	public static final double MAX_FORCE = 100;

	//Character constants
	private int level;

	//Character stats
	private double mastery;
	private double power;
	private double forcePower;
	private double accuracy;
	private double critRating;
	private double alacrityRating;
	private double mainhandMinDmg;
	private double mainhandMaxDmg;

	//Class buffs
	private boolean hasWarriorBuff;
	private boolean hasInquisitorBuff;
	private boolean hasAgentBuff;

	//Companion buffs
	private boolean compAccuracyBuff;
	private boolean compSurgeBuff;
	private boolean compCritBuff;

	//Skill tree buffs
	private double armorPenetration;

	//State
	//The random generator is stored on the Player object instead of CalcUtil because Math.random()
	//is shared between threads. This should improve the multicore scaling of the simulator.
	public Random random;
	public Simulator sim; //reference to simulator that created the player

	private double force;
	private EnumMap<Constants.Effects, Effect> effects;
	public EnumMap<Constants.Abilities, Ability> abilities;
	private EnumMap<Constants.OffAbilities, OffGCDAbility> offAbilities;
	public ArrayList<Proc> procs;
	protected PhantomStride stride;

	//Constructor
	public Player(int newLevel, Simulator sim1){
		level = newLevel;
		sim = sim1;
		random = new Random();
		force = MAX_FORCE;
		effects = new EnumMap<Constants.Effects, Effect>(Constants.Effects.class);
		abilities = new EnumMap<Constants.Abilities, Ability>(Constants.Abilities.class);
		offAbilities= new EnumMap<Constants.OffAbilities, OffGCDAbility>(Constants.OffAbilities.class);
		procs = new ArrayList<Proc>();
	}

	//properties
	//level constant
	public int getLevel() {
		return level;
	}

	//stats
	public double getMastery() {

		//handle relic bonus
		double bonus = 0.0;
		Effect fr = getEffect(Constants.Effects.FRRelic);
		if (fr.isActive(sim.time())) {
			bonus = FRRelic.MAINSTAT_BOOST;
		}

		//Handle buff
		double buffMulti = 1.0;
		if (hasInquisitorBuff){
			buffMulti = 1.05;
		}

		return (mastery + bonus) * buffMulti;
	}

	public void setMastery(double value) {
		mastery = value;
	}

	public double getPower() {
		//Handle adrenal boost
		double powerBonus = power;
		Effect pow = getEffect(Constants.Effects.PowerAdrenal);
		if (pow.isActive(sim.time())) {
			powerBonus += PowerAdrenal.POWER_BOOST;
		}

		//handle relic boost
		Effect sa = getEffect(Constants.Effects.SARelic);
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

	public double getAccuracyRating() {
		return accuracy;
	}

	public void setAccuracyRating(double value) {
		accuracy = value;
	}

	public double getCriticalRating() {
		return critRating;
	}

	public void setCriticalRating(double value) {
		critRating = value;
	}

	public double getAlacrityRating() {
		return alacrityRating;
	}

	public void setAlacrityRating(double value) {
		alacrityRating = value;
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
	public double getArmorPenetration() {
		return armorPenetration;
	}

	public void setArmorPenetration(double value){
		armorPenetration = value;
	}

	//state access
	public double getForce() {
		return force;
	}

	public void consumeForce(double amount) {
		force -= amount;
	}

	public void addForce(double amount) {
		force += amount;
		force = Math.min(force, MAX_FORCE); //cap force at maximum
	}

	public Effect getEffect(Constants.Effects name) {
		return effects.get(name);
	}

	public void addEffect(Constants.Effects name, Effect e) {
		effects.put(name, e);
	}

	public OffGCDAbility getOffAbility(Constants.OffAbilities name) {
		return offAbilities.get(name);
	}

	public void addOffAbility(Constants.OffAbilities name, OffGCDAbility a) {
		offAbilities.put(name, a);
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

	public Ability getAbility(Constants.Abilities name) {
		return abilities.get(name);
	}
	
	public void setPhantomStride(PhantomStride stride) {
		this.stride = stride;
	}
	
	public PhantomStride getPhantomStride() {
		return stride;
	}
}