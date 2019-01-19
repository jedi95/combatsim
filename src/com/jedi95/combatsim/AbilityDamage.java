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

public final class AbilityDamage {

	//Ability damage constants
	public final double standardHealthPercentMin;
	public final double standardHealthPercentMax;
	public final double coefficient;
	public final double amountModifierPercent;
	public final boolean isForce;
	public final boolean isInternal;

	private AbilityDamage(AbilityDamageBuilder builder) {
		this.standardHealthPercentMin = builder.standardHealthPercentMin;
		this.standardHealthPercentMax = builder.standardHealthPercentMax;
		this.coefficient = builder.coefficient;
		this.amountModifierPercent = builder.amountModifierPercent;
		this.isForce = builder.isForce;
		this.isInternal = builder.isInternal;
	}

	public static class AbilityDamageBuilder {
		private double standardHealthPercentMin;
		private double standardHealthPercentMax;
		private double coefficient;
		private double amountModifierPercent;
		private boolean isForce;
		private boolean isInternal;
		
		public AbilityDamageBuilder() {
			
		}
		
		public AbilityDamageBuilder standardHealthPercentMin(double standardHealthPercentMin) {
			this.standardHealthPercentMin = standardHealthPercentMin;
			return this;
		}
		
		public AbilityDamageBuilder standardHealthPercentMax(double standardHealthPercentMax) {
			this.standardHealthPercentMax = standardHealthPercentMax;
			return this;
		}
		
		public AbilityDamageBuilder coefficient(double coefficient) {
			this.coefficient = coefficient;
			return this;
		}
		
		public AbilityDamageBuilder amountModifierPercent(double amountModifierPercent) {
			this.amountModifierPercent = amountModifierPercent;
			return this;
		}
		
		public AbilityDamageBuilder isForce(boolean isForce) {
			this.isForce = isForce;
			return this;
		}
		
		public AbilityDamageBuilder isInternal(boolean isInternal) {
			this.isInternal = isInternal;
			return this;
		}
		
		public AbilityDamage build() {
			return new AbilityDamage(this);
		}
	}
}