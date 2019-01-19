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
	private double defenseChance;
	private boolean _hasArmorDebuff;
	private boolean _hasDamageTakenBuff;
	private boolean _hasInternalDamageBuff;

	public Simulator sim; //reference to simulator that created the target

	public Target(int level, double initialHealth, double defenseChance, Simulator sim){
		this.level = level;
		this.health = initialHealth;
		this.maxHealth = initialHealth;
		this.armorRating = 0.0;
		this.defenseChance = defenseChance;
		this.sim = sim;
		this._hasArmorDebuff = false;
		this._hasDamageTakenBuff = false;
		this._hasInternalDamageBuff = false;
	}

	public int getLevel() {
		return level;
	}

	public double getHealth(){
		return health;
	}

	public double getArmorRating(){
		if (_hasArmorDebuff){
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

	public boolean hasArmorDebuff() {
		return _hasArmorDebuff;
	}

	public void setArmorDebuff(boolean value){
		_hasArmorDebuff = value;
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
	
	public boolean hasDamageTakenBuff() {
		return _hasDamageTakenBuff;
	}

	public void setDamageTakenBuff(boolean value){
		_hasDamageTakenBuff = value;
	}
	
	public boolean hasInternalDamageBuff() {
		return _hasInternalDamageBuff;
	}

	public void setInternalDamageBuff(boolean value){
		_hasInternalDamageBuff = value;
	}
}