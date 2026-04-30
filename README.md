# CompSci S2 OOP class Coursework

# Part 1
Part 1 of the coursework uses a pre-set passage length of arbitrary contents. It runs in the command prompt. In order to run it, first download the file. Then, navigate to it in the command prompt. Next, run 'javac TypingRace.java Typist.java TypistStats.java'. The game will start to print in the command prompt. The game is set to have a passage length of 20 and the three Typists have an accuracy of 0.8, 0.6 and 0.3 each. The game itself has a mistype chance of 0.3, a slide back of 2 and burnout duration of 3. These can be altered by opening the java file and changing the numbers associated with these areas in the TypingRace class and main method, found on lines 25-33. To run the game with new variable values, go back into the file that the java files are stored in and compile the java files as shown above, but only 'TypingRace.java' needs to be included. This must be done, despite the class file already being present, to add the changes.

# Part 2
Part 2 of the coursework is much more customisable. There are 8 preset Typists, but 2-8 can compete. The passage is displayed and can have a variable length and contents. The easiest way to run this is to open the file in an IDE that supports java and run the TypingRace file. The only option that cannot be customized in the program itself is the number of Typists. This can be edited by adding to the txt file in the folder, in the form of:
typistName
typistSymbol
typistAccuracy
0
0
0
0
Afterwards, the parameter in declareTypists on line 33 of the TypingRace file must be changed to reflect the new number of Typists present.
