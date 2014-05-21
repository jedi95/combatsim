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

public class Ability {

	public static final double BELOW_30_PERCENT_DAMAGE_MULTI = 1.06;
	public static final double EXPLOITIVE_STRIKES_CRIT_BONUS = 0.09;
	public static final double RECKLESSNESS_CRIT_BONUS = 0.6;

	protected String name;
	protected AbilityDamage damage;
	protected int cooldown; //Cooldown is stored as miliseconds
	protected int forceCost; //force values are stored as (force * FORCE_MULTI)
	protected long lastUsedTime; //timestamp at last use, in ms since simulation start
	protected double damageMulti;
	protected double critBonus;
	protected double surgeBonus;
	protected int hitCount;

	protected Player player;

	public Ability(Player player1, String newName, int force, int cd, double dmgMulti, double criticalBonus, double surBonus, int numberOfHits){
		player = player1;
		name = newName;
		cooldown = cd;
		forceCost = force * Calc.FORCE_MULTI;
		lastUsedTime = -1000000;
		damageMulti = dmgMulti;
		critBonus = criticalBonus;
		surgeBonus = surBonus;
		hitCount = numberOfHits;
	}

	public AbilityDamage getDamage() {
		return damage;
	}

	public String getName() {
		return name;
	}

	public int getForceCost() {
		return forceCost;
	}

	public int getCooldown() {
		return cooldown;
	}

	public double getDamageMulti() {
		return damageMulti;
	}

	public double getCritBonus() {
		double extraCrit = 0.0;
		if (damage.getForce()){
			Effect recklessness = player.getEffect("Recklessness");
			Effect voltage = player.getEffect("Voltage");
			if (recklessness.isActive(player.sim.time())) {
				extraCrit += RECKLESSNESS_CRIT_BONUS;
			}
			if (voltage.isActive(player.sim.time())) {
				if (Main.useMod) {
					extraCrit += VoltaicSlashMod.VOLTAGE_FORCE_CRIT_BONUS * voltage.getStacks();
				}
				else {
					extraCrit += VoltaicSlash.VOLTAGE_FORCE_CRIT_BONUS * voltage.getStacks();
				}
			}
		}
		else
		{
			Effect exploitiveStrikes = player.getEffect("Exploitive Strikes");
			if (exploitiveStrikes.isActive(player.sim.time())){
				extraCrit += EXPLOITIVE_STRIKES_CRIT_BONUS;
			}
		}
		return critBonus + extraCrit;
	}

	public double getSurgeBonus() {
		return surgeBonus;
	}

	public Hit calculateHitDamage(Player player, Target target) {
		Hit hit = new Hit(getName(), Calc.calculateDamage(player, target, this.getDamage()) * getDamageMulti(), false, this.getDamage().isForce);

		//handle crits
		double critChance = getCritBonus();
		if (damage.isForce) {
			critChance += Calc.getForceCritChance(player);
		}
		else
		{
			critChance += Calc.getMeleeCritChance(player);
		}

		boolean isCrit = player.random.nextDouble() <= critChance;
		if (isCrit)
		{
			hit.crit = true;
			hit.damage *= Calc.getCriticalDamageMultiplier(player) + getSurgeBonus();
		}

		if (target.getHealth() <= target.getMaxHealth() * 0.30) {
			hit.damage *= BELOW_30_PERCENT_DAMAGE_MULTI;
		}

		return hit;
	}

	//Returns true if the ability is not currently on cooldown
	public boolean isReady() {
		return lastUsedTime + cooldown <= player.sim.time();
	}

	//Returns true if we have sufficient force to use the ability
	public boolean forceOk() {
		return player.getForce() >= getForceCost();
	}

	//returns true if the ability can currently be used
	public boolean canUse(Target target) {
		return forceOk() && isReady();
	}

	//Called to consume any effects/procs/buffs that benefit the ability
	public void consumeEffects(Hit hit) {
		return; //parent class does nothing here
	}

	//Consumes force
	public void consumeForce() {
		player.consumeForce(getForceCost());
	}

	//Handles procs that can occur when this ability is used.
	public void checkProcs(Target target, Hit hit, int hitCount) {
		player.handleProcs(target, hit, hitCount);
	}

	//Important: this handles the ability priority list! check subclasses.
	public boolean shouldUse(Target target) {
		return canUse(target); //Parent simply returns true if usable
	}

	//Called to execute this ability on the specified target
	public void use(Target target) {

		//Consume force
		consumeForce();

		//Set last used time
		lastUsedTime = player.sim.time();

		//Calculate damage
		Hit hit = calculateHitDamage(player, target);

		//Calculate accuracy
		double accuracy;
		if (damage.isSpecial || damage.isForce){
			accuracy = Calc.getSpecialAccuracy(player, target);
		}
		else
		{
			accuracy = Calc.getBasicAccuracy(player, target);
		}

		//Apply damage to target
		for (int i = 0; i < hitCount; i++){
			//hit check
			if (player.random.nextDouble() * (1.0 + target.getDefenseChance()) <= accuracy)
			{
				target.applyHit(hit);
			}
		}

		//Consume procs/buffs
		consumeEffects(hit);

		//Handle procs
		checkProcs(target, hit, hitCount);
	}
}