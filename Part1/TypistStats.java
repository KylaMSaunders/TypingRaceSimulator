/** 
 * A data structure used to store all data and methods relating to the accuracy of a Typist object
 * It stores whether its last letter was incorrect and variables to calculate if the Typist's accuracy should increase
 * 
 * @author Kyla Saunders
 * @version 1
*/
import java.math.BigDecimal;
public class TypistStats {
    private BigDecimal initialAccuracy;
    private boolean justMistyped;

    /** Constructor for a TypistStats object
     * @param acc The current accuracy of the Typist
     */ 

    public TypistStats (BigDecimal acc) {
        this.initialAccuracy = acc;
        this.justMistyped = false;
    }

    /** 
     * Returns whether the Typist has just mistyped
     * @return true if the Typist has just mistyped
     */ 
    public boolean getMistype () {
        return justMistyped;
    }

    // Acknowledges that the Typist has correctly typed a letter
    @SuppressWarnings("UnnecessaryReturnStatement")
    public void deregisterMistype () {
        this.justMistyped = false;
        return;
    }

    // Acknowledges that the Typist has mistyped
    @SuppressWarnings("UnnecessaryReturnStatement")
    public void registerMistype () {
        this.justMistyped = true;
        return;
    }

    /** 
     * Finds the accuracy the Typist had in the race and returns either it or 0, depending on whether they won or lost and if the accuracy changed
     * @param winner Whether the Typist has won the race
     * @return A changed accuracy depending on how lucky they were and whether they won or lost, with the winner never losing accuracy and the loser never gaining
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
