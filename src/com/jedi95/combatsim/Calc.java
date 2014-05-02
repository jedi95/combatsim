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

public class Calc {

	//constant for level 55, will need to change for players != level 55
	public static final double BASE_DAMAGE = 2685.0;

	//force values are stored as (force * FORCE_MULTI) to avoid FP math errors while allowing for decimal
	//force precision. This is done because exact force values are required for proper decision-making.
	public static final int FORCE_MULTI = 100;

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

		return (player.getWillpower() * 0.2 + player.getStrength() * 0.2 + player.getPower() * 0.23) * buffMulti;
	}

	public static double getForceBonusDamage(Player player) {

		//Handle warrior bonus damage buff
		double buffMulti = 1.0;
		if (player.getWarriorBuff())
		{
			buffMulti = 1.05;
		}

		return (player.getWillpower() * 0.2 + player.getForcePower() * 0.23 + player.getPower() * 0.23) * buffMulti;
	}

	public static double getMeleeCritChance(Player player) {

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

		//Add bonus from willpower
		critChance += 0.2 * (1 - Math.pow((1 - (0.01 / 0.2)), (player.getWillpower() / Math.max(player.getLevel(), 20)) / 5.5));

		//Add bonus from strength
		critChance += 0.2 * (1 - Math.pow((1 - (0.01 / 0.2)), (player.getStrength() / Math.max(player.getLevel(), 20)) / 5.5));

		//Add bonus from crit rating
		critChance += 0.3 * (1 - Math.pow((1 - (0.01 / 0.3)), (player.getCritRating() / Math.max(player.getLevel(), 20)) / 0.9));

		return critChance;
	}

	public static double getForceCritChance(Player player) {

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

		//Add bonus from willpower
		critChance += 0.2 * (1 - Math.pow((1 - (0.01 / 0.2)), (player.getWillpower() / Math.max(player.getLevel(), 20)) / 5.5));

		//Add bonus from crit rating
		critChance += 0.3 * (1 - Math.pow((1 - (0.01 / 0.3)), (player.getCritRating() / Math.max(player.getLevel(), 20)) / 0.9));

		return critChance;
	}

	public static double getCriticalDamageMultiplier(Player player) {

		//Base crit multiplier
		double critMulti = 1.5;

		//Add companion buff
		if (player.getCompSurgeBuff()){
			critMulti += 0.01;
		}

		//Calculate bonus from surge
		critMulti += 0.3 * (1 - Math.pow((1 - (0.01 / 0.3)), (player.getSurgeRating() / player.getLevel()) / 0.22));

		return critMulti;
	}

	//Basic accuracy for normal attacks (only saber strike for deception)
	public static double getBasicAccuracy(Player player, Target target){

		//Base 90% accuracy
		double accuracy = 0.9;

		//Add companion buff
		if (player.getCompAccBuff()){
			accuracy += 0.01;
		}

		//Add bonus from skill tree
		accuracy += player.getSkillAccBuff();

		//Add bonus from accuracy rating
		accuracy += 0.3 * (1 - Math.pow((1 - (0.01 / 0.3)), (player.getAccuracy() / target.getLevel()) / 1.2));

		return accuracy;
	}

	//Special accuracy for special/force attacks
	public static double getSpecialAccuracy(Player player, Target target){

		//Base 100% accuracy
		double accuracy = 1.0;

		//Add companion buff
		if (player.getCompAccBuff()){
			accuracy += 0.01;
		}

		//Add bonus from skill tree
		accuracy += player.getSkillAccBuff();

		//Add bonus from accuracy rating
		accuracy += 0.3 * (1 - Math.pow((1 - (0.01 / 0.3)), (player.getAccuracy() / target.getLevel()) / 1.2));

		return accuracy;
	}

	public static double calculateDamage(Player player, Target target, AbilityDamage damage) {

		//Calculate base ability damage
		double AbilityDmgMin = damage.getStandardHealthPercentMin() * BASE_DAMAGE;
		double AbilityDmgMax = damage.getStandardHealthPercentMax() * BASE_DAMAGE;
		double BonusDamage = 0.0;

		//handle force/melee
		if (damage.getForce()){

			//Adjust bonus damage for force power
			BonusDamage += damage.getCoefficient() * getForceBonusDamage(player);

			//Apply bonus damage
			AbilityDmgMin += BonusDamage;
			AbilityDmgMax += BonusDamage;
		}
		else {

			//Apply bonus damage
			BonusDamage += damage.getCoefficient() * getMeleeBonusDamage(player);

			//apply MH/OH damage and bonus
			AbilityDmgMin += ((damage.getAmountModifierPercent() + 1.0) * player.getMainhandMinDmg()) + BonusDamage;
			AbilityDmgMax += ((damage.getAmountModifierPercent() + 1.0) * player.getMainhandMaxDmg()) + BonusDamage;
		}

		//Handle armor damage reduction
		if (!damage.getInternal())
		{
			double damageReduction = getDamageReduction(player, target);
			AbilityDmgMin *= (1.0 - damageReduction);
			AbilityDmgMax *= (1.0 - damageReduction);
		}

		//Handle random damage within min/max
		return AbilityDmgMin + ((AbilityDmgMax - AbilityDmgMin) * player.random.nextDouble());
	}
}