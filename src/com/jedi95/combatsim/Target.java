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

public class Target {

	private double armorRating;
	private int level;
	private double health;
	private double maxHealth;
	private boolean hasArmorDebuff;
	private double defenseChance;

	public Simulator sim; //reference to simulator that created the target

	public Target(int newLevel, double initialHealth, double dChance, Simulator sim1){
		level = newLevel;
		health = initialHealth;
		maxHealth = initialHealth;
		armorRating = 0.0;
		hasArmorDebuff = false;
		defenseChance = dChance;
		sim = sim1;
	}

	public int getLevel(){
		return level;
	}

	public double getHealth(){
		return health;
	}

	public double getArmorRating(){
		if (hasArmorDebuff){
			return armorRating * 0.8;
		}
		else
		{
			return armorRating;
		}
	}

	public double getDefenseChance() {
		return defenseChance;
	}

	public void setArmorRating(double value){
		armorRating = value;
	}

	public boolean isArmorDebuffed() {
		return hasArmorDebuff;
	}

	public void setArmorDebuff(boolean value){
		hasArmorDebuff = value;
	}

	public void applyHit(Hit hit) {
		health -= hit.damage;
		sim.getLog().logHit(hit);
	}

	public boolean isDead() {
		return health <= 0.0;
	}

	public double getMaxHealth() {
		return maxHealth;
	}
}