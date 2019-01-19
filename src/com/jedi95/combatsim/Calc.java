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

import java.lang.Math;
import java.util.Map;
import java.util.HashMap;

public class Calc {

	//constant for level 70
	public static final double BASE_DAMAGE = 7525;

	//These don't change at all during a kill, so we cache them
	private static Double cacheAlacrity = null;
	private static Double cacheAccuracy = null;
	private static Double cacheCritRating = null;

	//These can change, but only have a few discreet values
	private static Map<Double, Double> masteryCache = new HashMap<Double, Double>();

	public static double getDamageReduction(Player player, Target target){
		double targetArmor = target.getArmorRating() * Math.max(0.0, 1.0 - player.getArmorPenetration());
		return Math.min(0.75, targetArmor / (targetArmor + (240.0 * player.getLevel() + 800)));
	}

	public static double getMeleeBonusDamage(Player player) {

		//Handle warrior bonus damage buff
		double buffMulti = 1.0;
		if (player.getWarriorBuff())
		{
			buffMulti = 1.05;
		}

		return (player.getMastery() * 0.2 + player.getPower() * 0.23) * buffMulti;
	}

	public static double getForceBonusDamage(Player player) {

		//Handle warrior bonus damage buff
		double buffMulti = 1.0;
		if (player.getWarriorBuff())
		{
			buffMulti = 1.05;
		}

		return (player.getMastery() * 0.2 + player.getForcePower() * 0.23 + player.getPower() * 0.23) * buffMulti;
	}

	public static double getCritChance(Player player) {

		//Base crit chance
		double critChance = 0.05;

		//Add agent buff
		if (player.getAgentBuff())
		{
			critChance += 0.05;
		}

		//Add companion buff
		if (player.getCompCritBuff()){
			critChance += 0.01;
		}

		//Add bonus from mastery
		Double cachedMasteryValue = masteryCache.get(player.getMastery());
		if (cachedMasteryValue == null) {
			cachedMasteryValue = 0.2 * (1 - Math.pow((1 - (0.01 / 0.2)), (player.getMastery() / player.getLevel()) / 5.5));
			masteryCache.put(player.getMastery(), cachedMasteryValue);
		}
		critChance += cachedMasteryValue;

		//Add bonus from crit rating
		if (cacheCritRating == null) {
			cacheCritRating = 0.3 * (1 - Math.pow((1 - (0.01 / 0.3)), (player.getCriticalRating() / player.getLevel()) / 0.8));
		}
		critChance += cacheCritRating;

		return critChance;
	}

	public static double getCriticalDamageMultiplier(Player player) {

		//Base crit multiplier
		double critMulti = 1.5;

		//Add companion buff
		if (player.getCompSurgeBuff()){
			critMulti += 0.01;
		}

		//Calculate bonus from crit rating
		if (cacheCritRating == null) {
			cacheCritRating = 0.3 * (1 - Math.pow((1 - (0.01 / 0.3)), (player.getCriticalRating() / player.getLevel()) / 0.8));
		}
		critMulti += cacheCritRating;

		return critMulti;
	}
	
	//Supercrits - any crit with chance > 100%
	public static double getSuperCritMultiplier(Player player, double critChance) {
		return Calc.getCriticalDamageMultiplier(player) * Math.max(1, critChance);
	}

	//Accuracy 
	public static double getAccuracy(Player player, Target target){

		if (cacheAccuracy == null) {
			//Base 100% accuracy
			double accuracy = 1.0;

			//Add companion buff
			if (player.getCompAccBuff()){
				accuracy += 0.01;
			}

			//Add bonus from accuracy rating
			accuracy += 0.3 * (1 - Math.pow((1 - (0.01 / 0.3)), (player.getAccuracyRating() / player.getLevel()) / 1));

			cacheAccuracy = accuracy;
		}

		return cacheAccuracy;
	}

	public static double getAlacrity(Player player) {

		if (cacheAlacrity == null) {
			//Base alacrity is 1.0 or 100% normal speed
			double alacrity = 1.0;

			//Add alacrity from alacrity rating
			alacrity += 0.3 * (1 - Math.pow((1 - (0.01 / 0.3)), (player.getAlacrityRating() / player.getLevel()) / 1.25));

			cacheAlacrity = alacrity;
		}
		return cacheAlacrity;
	}
	
	public static double calculateDamage(Player player, Target target, AbilityDamage damage) {

		//Calculate base ability damage
		double AbilityDmgMin = damage.standardHealthPercentMin * BASE_DAMAGE;
		double AbilityDmgMax = damage.standardHealthPercentMax * BASE_DAMAGE;
		double BonusDamage = 0.0;

		//handle force/melee
		if (damage.isForce){

			//Adjust bonus damage for force power
			BonusDamage += damage.coefficient * getForceBonusDamage(player);

			//Apply bonus damage
			AbilityDmgMin += BonusDamage;
			AbilityDmgMax += BonusDamage;
		}
		else {

			//Apply bonus damage
			BonusDamage += damage.coefficient * getMeleeBonusDamage(player);

			//apply MH/OH damage and bonus
			AbilityDmgMin += ((damage.amountModifierPercent + 1.0) * player.getMainhandMinDmg()) + BonusDamage;
			AbilityDmgMax += ((damage.amountModifierPercent + 1.0) * player.getMainhandMaxDmg()) + BonusDamage;
		}

		//Handle armor damage reduction
		if (!damage.isInternal)
		{
			double damageReduction = getDamageReduction(player, target);
			AbilityDmgMin *= (1.0 - damageReduction);
			AbilityDmgMax *= (1.0 - damageReduction);
		}
		
		double damageOut = AbilityDmgMin + ((AbilityDmgMax - AbilityDmgMin) * player.random.nextDouble());
		
		//Handle damage bonuses
		if (target.hasDamageTakenBuff()) {
			damageOut *= 1.05;
		}
		else if (target.hasInternalDamageBuff() && damage.isInternal) {
			damageOut *= 1.07;
		}

		//Handle random damage within min/max
		return damageOut;
	}
}