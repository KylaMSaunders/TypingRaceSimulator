import java.math.BigDecimal;

/** 
 * A data structure used to store all data and methods relating to the accuracy of a Typist object
 * It stores whether its last letter was incorrect and variables to calculate if the Typist's accuracy should increase
 * Also stores data on the Typist's performance over all races
 * 
 * @author Kyla Saunders
 * @version 2
*/
public class TypistStats {
    private BigDecimal initialAccuracy;
    private boolean justMistyped;
    private int lastProgress;
    private int wins;
    private int plays;
    private int attemptedTypes;
    private int successfulTypes;

    /** Constructor for a TypistStats object
     * @param acc the current accuracy of the Typist
     * @param wins the number of games the Typist has won or tied first
     * @param plays the number of games the Typist has played
     * @param attemptT the number of rounds the Typist has tried to type
     * @param successT the number of times the Typist has successfully typed
     */ 

    public TypistStats (BigDecimal acc, int wins, int plays, int attemptT, int successT) {
        this.initialAccuracy = acc;
        this.justMistyped = false;
        this.lastProgress = -1;
        this.wins = wins;
        this.plays = plays;
        this.attemptedTypes = attemptT;
        this.successfulTypes = successT;
    }

    /** 
     * Returns whether the Typist has just mistyped
     * @return true if the Typist has just mistyped
     */ 
    public boolean getMistype () {
        return this.justMistyped;
    }

    /**
     * Returns the last stage of progress the user made
     * @return where the Typist was before its last movement
     */
    public int getLastProgress () {
        return this.lastProgress;
    }

    /**
     * Returns the number of wins the Typist has had
     * @return the number of wins
     */
    public int getWins () {
        return this.wins;
    }

    /**
     * Returns the number of games the Typist has played
     * @return the number of games played
     */
    public int getPlays () {
        return this.plays;
    }

    /**
     * Returns the number of letters the Typist has attempted to type
     * @return the number of attempts to type a letter
     */
    public int getTypeAttempts () {
        return this.attemptedTypes;
    }

    /**
     * Returns the number of letters the Typist has typed
     * @return the number of typed letters
     */
    public int getTypes () {
        return this.successfulTypes;
    }

    /**
     * Acknowledges that the Typist has correctly typed a letter
     */
    public void deregisterMistype () {
        this.justMistyped = false;
    }

    /**
     * Acknowledges that the Typist has mistyped
     */
    public void registerMistype () {
        this.justMistyped = true;
    }

    /**
     * Increments the previous stage of progress
     */
    public void registerProgress (int p) {
        this.lastProgress = p;
    }

    /**
     * Acknowleges that the Typist has played a game and whether they won
     * 
     * @param win whether the Typist won the game
     */
    public void registerGame (boolean win) {
        if ( win ) {
            this.wins += 1;
        }
        this.plays += 1;
    }

    /**
     * Acknowleges that the Typist has tried to type and whether they succeeded
     * 
     * @param success whether the Typist successfully typed
     */
    public void registerType (boolean success) {
        if ( success ) {
            this.successfulTypes += 1;
        }
        this.attemptedTypes += 1;
    }

    /** 
     * Finds the accuracy the Typist had in the race and returns either it or 0, depending on whether they won or lost and if the accuracy changed
     * 
     * @param winner whether the Typist has won the race
     * @return a changed accuracy depending on how lucky they were and whether they won or lost, with the winner never losing accuracy and the loser never gaining
     */ 
    public BigDecimal getNewAccuracy (char winner) {
        BigDecimal diff = BigDecimal.valueOf(0.025);
        if ( winner == 'W' ) {
            this.initialAccuracy = this.initialAccuracy.add(diff);
            return this.initialAccuracy;
        }
        else {
            this.initialAccuracy = this.initialAccuracy.subtract(diff);
            return this.initialAccuracy;
        }
    }
}