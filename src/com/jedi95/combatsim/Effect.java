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

public class Effect {

	//times are in ms
	private long appliedTime;
	private int duration;
	private int stacks;
	private int stackLimit;
	private String name;

	public Effect(int newDuration, long time, String effectName, int initialStacks, int maxStacks){
		duration = newDuration;
		appliedTime = time;
		name = effectName;
		stacks = initialStacks;
		stackLimit = maxStacks;
	}

	//Returns the name of the effect
	public String getName() {
		return name;
	}

	//Returns the duration that this effect will last
	public int getDuration() {
		return duration;
	}

	//Returns the time remaining before this effect falls off
	public long getRemainingTime(long time) {
		return Math.max(0, (appliedTime + duration) - time);
	}

	//indicates if this effect is currently active
	public boolean isActive(long time) {
		return (!shouldRemove(time)) && (stacks > 0);
	}

	//Indicates if this effect should fall off
	public boolean shouldRemove(long time){
		return appliedTime + duration <= time;
	}

	//Gets the current stack count NOTE: when using this make sure to check if the effect is active!
	public int getStacks() {
		return stacks;
	}

	//Used to add stacks (voltage, induction, static charge, recklessness)
	public void addStacks(int count, long time) {
		if (isActive(time)){
			stacks += count;
		}
		else
		{
			stacks = count;
		}
		stacks = Math.min(stacks, stackLimit);
		appliedTime = time;
	}

	//Used to remove all stacks and reset the duration (induction, duplicity, static charge)
	public void resetStacks() {
		appliedTime = 0 - duration;
		stacks = 0;
	}

	//consumes 1 stack (recklessness)
	public void consumeStacks(int count) {
		stacks -= count;
	}

	public int getMaxStacks() {
		return stackLimit;
	}
}