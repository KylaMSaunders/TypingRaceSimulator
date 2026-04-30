import java.io.*;
import java.math.BigDecimal;

/**
 * Write a description of class Typist here.
 * 
 * The class that defines the competitors of a typing race. Allows them
 * to be represented as individuals, storing their individual data and 
 * executing specific methods onto them.
 *
 * @author Kyla Saunders
 * @version 3
 */
public class Typist {
    private final String name;
    private final String symbol;
    private int progress;
    private boolean burntOut;
    private int burntTurns;
    private BigDecimal accuracy;
    private final TypistStats stats;

    // Constructor of class Typist
    /**
     * Constructor for objects of class Typist.
     * Creates a new typist with a given symbol, name, and accuracy rating.
     *
     * @param typistName the name of the typist
     * @param sym a single character representing this typist
     * @param typistAccuracy the typist's accuracy rating, between 0.0 and 1.0
     * @param wins the number of games the Typist has won or tied first 
     * @param plays the number of games the Typist has played
     * @param attemptT the number of rounds the Typist has tried to type
     * @param successT the number of times the Typist has successfully typed
     */
    public Typist (String typistName, String sym, BigDecimal typistAccuracy, int wins, int plays, int attemptT, int successT) {
        this.name = typistName;
        this.symbol = sym;
        this.accuracy = typistAccuracy;

        this.progress = 0;
        this.burntOut = false;
        this.burntTurns = 0;
        this.stats = new TypistStats(typistAccuracy, wins, plays, attemptT, successT);
    }

    /**
     * Sets this typist into a burnout state for a given number of turns.
     * A burnt-out typist cannot type until their burnout has worn off.
     *
     * @param turns the number of turns the burnout will last
     */
    public void burnOut (int turns) {
        this.burntOut = true;
        this.burntTurns = turns;
    }

    /**
     * Reduces the remaining burnout counter by one turn.
     * When the counter reaches zero, the typist recovers automatically.
     * Has no effect if the typist is not currently burnt out.
     */
    public void recoverFromBurnout () {
        if ( this.burntOut == true ) {
            this.burntTurns -= 1;
            if ( this.burntTurns == 0 ) {
                this.burntOut = false;
            }
        }
    }

    /**
     * Returns the typist's accuracy rating.
     *
     * @return accuracy as a double between 0.0 and 1.0
     */
    public BigDecimal getAccuracy () {
        return this.accuracy; 
    }

    /**
     * Returns the typist's current progress through the passage.
     * Progress is measured in characters typed correctly so far.
     * Note: this value can decrease if the typist mistypes.
     *
     * @return progress as a non-negative integer
     */
    public int getProgress () {
        return this.progress; 
    }

    /**
     * Returns the typist's relevant TypistStats object
     * @return a TypistStats object
     */
    public TypistStats getStats () {
        return this.stats;
    }

    /**
     * Returns the name of the typist.
     *
     * @return the typist's name as a String
     */
    public String getName () {
        return this.name;
    }

    /**
     * Returns the character symbol used to represent this typist.
     *
     * @return the typist's symbol as a string
     */
    public String getSymbol () {
        return this.symbol;
    }

    /**
     * Returns the number of turns of burnout remaining.
     * Returns 0 if the typist is not currently burnt out.
     *
     * @return burnout turns remaining as a non-negative integer
     */
    public int getBurnoutTurnsRemaining () {
        return this.burntTurns; 
    }

    /**
     * Resets the typist to their initial state, ready for a new race.
     * Progress returns to zero, burnout is cleared entirely.
     */
    public void resetToStart () {
        this.progress = 0;
        this.burntOut = false;
        this.burntTurns = 0;
        this.stats.deregisterMistype();
    }

    /**
     * Returns true if this typist is currently burnt out, false otherwise.
     *
     * @return true if burnt out
     */
    public boolean isBurntOut () {
        return this.burntOut; // placeholder - replace with correct implementation
    }

    /**
     * Advances the typist forward by one character along the passage.
     * Should only be called when the typist is not burnt out.
     */
    public void typeCharacter () {
        this.getStats().registerProgress(this.progress);
        this.progress += 1;
        this.getStats().deregisterMistype();
    }

    /**
     * Moves the typist backwards by a given number of characters (a mistype).
     * Progress cannot go below zero — the typist cannot slide off the start.
     *
     * @param amount the number of characters to slide back (must be positive)
     */
    public void slideBack (int amount) {
        int newPosition = this.progress - amount;
        if ( newPosition < 0 ) {
            newPosition = 0;
        }
        this.progress = newPosition;
    }

    /**
     * Sets the accuracy rating of the typist.
     * Values below 0.0 should be set to 0.0; values above 1.0 should be set to 1.0.
     *
     * @param newAccuracy the new accuracy rating
     */
    public void setAccuracy (BigDecimal newAccuracy) {
        BigDecimal zero = BigDecimal.valueOf(0);
        BigDecimal one = BigDecimal.valueOf(1);
        if ( newAccuracy.compareTo(one) == 1 ) {
            this.accuracy = one;
        }
        else if ( newAccuracy.compareTo(zero) == -1 ) {
            this.accuracy = zero;
        }
        else {
            this.accuracy = newAccuracy;
        }
    }

    //Saves the Typist's current data that needs to be carried over
    public void saveData () {
        try (PrintWriter ostream = new PrintWriter(new FileWriter("typistSaveData.txt", true))) {
            String n = this.getName() + "\n";
            String s = this.getSymbol() + "\n";
            BigDecimal a = this.getAccuracy();
            String aString = String.valueOf(a.doubleValue()) + "\n";
            String w = String.valueOf(this.getStats().getWins()) + "\n";
            String p = String.valueOf(this.getStats().getPlays()) + "\n";
            String at = String.valueOf(this.getStats().getTypeAttempts()) + "\n";
            String st = String.valueOf(this.getStats().getTypes()) + "\n";
            ostream.write(n);
            ostream.write(s);
            ostream.write(aString);
            ostream.write(w);
            ostream.write(p);
            ostream.write(at);
            ostream.write(st);
        }
        catch ( IOException e ) { }
    }
}