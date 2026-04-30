/**
 * Write a description of class Typist here.
 *
 * Starter code generously abandoned by Ty Posaurus, your predecessor,
 * who typed with two fingers and considered that "good enough".
 * He left a sticky note: "the slide-back thing is optional probably".
 * It is not optional. Good luck.
 * 
 * The class that defines the competitors of a typing race. Allows them
 * to be represented as individuals, storing their individual data and 
 * executing specific methods onto them.
 *
 * @author Kyla Saunders
 * @version 2
 */
import java.math.BigDecimal;
public class Typist
{
    private final String name;
    private char symbol;
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
     * @param typistSymbol  a single Unicode character representing this typist (e.g. '①', '②', '③')
     * @param typistName    the name of the typist (e.g. "TURBOFINGERS")
     * @param typistAccuracy the typist's accuracy rating, between 0.0 and 1.0
     */
    @SuppressWarnings("UnnecessaryReturnStatement")
    public Typist(char typistSymbol, String typistName, BigDecimal typistAccuracy)
    {
        this.symbol = typistSymbol;
        this.name = typistName;
        this.accuracy = typistAccuracy;

        this.progress = 0;
        this.burntOut = false;
        this.burntTurns = 0;
        this.stats = new TypistStats(typistAccuracy);
        return;
    }


    // Methods of class Typist

    /**
     * Sets this typist into a burnout state for a given number of turns.
     * A burnt-out typist cannot type until their burnout has worn off.
     *
     * @param turns the number of turns the burnout will last
     */
    @SuppressWarnings("UnnecessaryReturnStatement")
    public void burnOut(int turns)
    {
        this.burntOut = true;
        this.burntTurns = turns;
        return;
    }

    /**
     * Reduces the remaining burnout counter by one turn.
     * When the counter reaches zero, the typist recovers automatically.
     * Has no effect if the typist is not currently burnt out.
     */
    @SuppressWarnings("UnnecessaryReturnStatement")
    public void recoverFromBurnout()
    {
        if ( this.burntOut == true ) {
            this.burntTurns -= 1;
            if ( this.burntTurns == 0 ) {
                this.burntOut = false;
            }
        }
        return;
    }

    /**
     * Returns the typist's accuracy rating.
     *
     * @return accuracy as a double between 0.0 and 1.0
     */
    public BigDecimal getAccuracy()
    {
        return this.accuracy; 
    }

    /**
     * Returns the typist's current progress through the passage.
     * Progress is measured in characters typed correctly so far.
     * Note: this value can decrease if the typist mistypes.
     *
     * @return progress as a non-negative integer
     */
    public int getProgress()
    {
        return this.progress; 
    }

    /**
     * Returns the typist's relevant TypistStats object
     * @return a TypistStats object
     */
    public TypistStats getStats() 
    {
        return this.stats;
    }

    /**
     * Returns the name of the typist.
     *
     * @return the typist's name as a String
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * Returns the character symbol used to represent this typist.
     *
     * @return the typist's symbol as a char
     */
    public char getSymbol()
    {
        return this.symbol;
    }

    /**
     * Returns the number of turns of burnout remaining.
     * Returns 0 if the typist is not currently burnt out.
     *
     * @return burnout turns remaining as a non-negative integer
     */
    public int getBurnoutTurnsRemaining()
    {
        return this.burntTurns; 
    }

    /**
     * Resets the typist to their initial state, ready for a new race.
     * Progress returns to zero, burnout is cleared entirely.
     */
    public void resetToStart()
    {
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
    public boolean isBurntOut()
    {
        return this.burntOut; // placeholder - replace with correct implementation
    }

    /**
     * Advances the typist forward by one character along the passage.
     * Should only be called when the typist is not burnt out.
     */
    @SuppressWarnings("UnnecessaryReturnStatement")
    public void typeCharacter()
    {
        this.progress += 1;
        this.getStats().deregisterMistype();
        return;
    }

    /**
     * Moves the typist backwards by a given number of characters (a mistype).
     * Progress cannot go below zero — the typist cannot slide off the start.
     *
     * @param amount the number of characters to slide back (must be positive)
     */
    @SuppressWarnings("UnnecessaryReturnStatement")
    public void slideBack(int amount)
    {
        int newPosition = this.progress - amount;
        if ( newPosition < 0 ) {
            newPosition = 0;
        }
        this.progress = newPosition;
        return;
    }

    /**
     * Sets the accuracy rating of the typist.
     * Values below 0.0 should be set to 0.0; values above 1.0 should be set to 1.0.
     *
     * @param newAccuracy the new accuracy rating
     */
    @SuppressWarnings("UnnecessaryReturnStatement")
    public void setAccuracy(BigDecimal newAccuracy)
    {
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
        return;
    }

    /**
     * Sets the symbol used to represent this typist.
     *
     * @param newSymbol the new symbol character
     */
    @SuppressWarnings("UnnecessaryReturnStatement")
    public void setSymbol(char newSymbol)
    {
        this.symbol = newSymbol;
        return;
    }

}
