Combat Simulator
================
This is a combat simulator for the Deception spec of the Sith Assassin class in SWTOR.

I created this program as a means to test how things such as gear and balance changes affect the DPS of Deception.

For testing gear changes it is far more accurate than parsing within the game because it removes human error and RNG variance from the results. The average DPS will change by less than 0.005% between runs when using 1,000,000 iterations.

For class balance changes it provides more realistic results than purely theoretical calculations.


Included class changes
----------------------
This program includes a set of class balance changes that are aimed at increasing sustained DPS:
1. The base force cost of Voltaic Slash is reduced from 25 to 23
2. The ICD of Saber Conduit is reduced from 10s to 9s
3. Each stack of Voltage increases force critical chance by 5%
4. The proc chance of Surging Charge is increased from 25% to 30%

In my opinion, these changes would be enough to make Deception viable in all current PVE content. It will provide an alternative to Madness when burst or rapid target switching is required.

DISCLAIMER: These changes are only ideas, and they do not nessesarily reflect future class balance.

That said, feel free to expieriment with your own ideas!


Running
-------
The combat simulator can be run with the included run.bat file or manually from the command line.

Command line example:
>java -jar combatsim.jar -H 500000 -m

This would run the combat simulator on a target with 500K HP using the class changes.


Command line options
--------------------
 -c         Runs a comparison. (default is off)
 -D         Disables the armor debuff on the target. (default is on)
 -g         Generate comparison charts. (default is off)
 -H <arg>   Sets the target's HP. (default is 1,000,000)
 -h         Displays command line help.
 -i <arg>   The number of iterations to simulate. (default is 100,000)
 -m         Runs with balance changes applied. (default is off)
 -p         Runs one simulation only, and prints the combat log.
 -t <arg>   Sets the number of threads to use. (default is CPU core count)

Some options cause others to be ignored:
-p will ignore -t -i
-h will ignore everything else
-c will ignore -m
-g will ignore -c -m -p -H*
    *-H will only apply to the DPS distribution chart, the DPS scaling chart is not affected.


Making changes
--------------
If you want to test your own class balance changes or stat combinations I highly recommend using an IDE such as Eclipse that can build the project automatically. This will allow you to test ideas quickly without manually recompiling the code.


Dependencies
------------
Apache Commons CLI 1.2 (commons-cli-1.2.jar) http://commons.apache.org/proper/commons-cli/
JCommon 1.0.21 (jcommon-1.0.21.jar) http://www.jfree.org/jcommon/
JFreeChart 1.0.17 (jfreechart-1.0.17.jar) http://www.jfree.org/jfreechart/


Known Issues
------------
1. The simulator is currently unable to handle nonzero alacrity.
2. Changing the character stats requires recompiling the program.
3. The default iteration count may cause the simulation to take a long time on slower machines.
4. The progress messages may show values above 100% for some values of iteration count.


To-do list
----------
1. Add an option to load character stats from a text file.
2. Add more types of charts.


Balance change test requests
----------------------------
I understand that not every player will know how to modify this program to test their ideas for class balance changes. If you have an idea that you would like me to implement and test, contact me via email or ingame.


Contact info
------------
email: jedi95@gmail.com
ingame: Auldria (Empire, POT5 - US east PVP)