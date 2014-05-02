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

public class AbilityDamage {

	//Ability damage constants
	protected double standardHealthPercentMin;
	protected double standardHealthPercentMax;
	protected double coefficient;
	protected double amountModifierPercent;

	//Details
	protected boolean isSpecial;
	protected boolean isForce;
	protected boolean isInternal;

	public AbilityDamage(double stdHealthMin, double stdHealthMax, double coeff, double amtModifierPercent, boolean special, boolean force, boolean internal){
		standardHealthPercentMin = stdHealthMin;
		standardHealthPercentMax = stdHealthMax;
		coefficient = coeff;
		amountModifierPercent = amtModifierPercent;
		isSpecial = special;
		isForce = force;
		isInternal = internal;
	}

	public double getStandardHealthPercentMin() {
		return standardHealthPercentMin;
	}

	public double getStandardHealthPercentMax() {
		return standardHealthPercentMax;
	}

	public double getCoefficient() {
		return coefficient;
	}

	public double getAmountModifierPercent() {
		return amountModifierPercent;
	}

	public boolean getSpecial() {
		return isSpecial;
	}

	public boolean getForce() {
		return isForce;
	}

	public boolean getInternal() {
		return isInternal;
	}
}