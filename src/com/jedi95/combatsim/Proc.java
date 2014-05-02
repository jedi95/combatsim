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

public class Proc {

	protected int cooldown; //in ms
	protected double procChance;
	protected long lastActive;
	protected String name;
	protected Player player;

	public Proc(Player player1, String newName, int cd, double chance) {
		player = player1;
		name = newName;
		cooldown = cd;
		procChance = chance;
		lastActive = -1000000;
	}

	public String getName() {
		return name;
	}

	public double getProcChance() {
		return procChance;
	}

	//Checks if the proc should activate
	public void check(Player player, Target target, Hit hit, long time, int hitCount) {
		//If not on ICD
		if (lastActive + cooldown <= time) {

			//Check chance
			if (player.random.nextDouble() <= getProcChance()) {
				handleProc(player, target, time);
				lastActive = time; //reset timer for ICD
			}
		}
	}

	public void handleProc(Player player, Target target, long time) {
		return; //Parent class does nothing here.
	}

	public Hit getHitDamage(Player player) {
		return new Hit();
	}
}