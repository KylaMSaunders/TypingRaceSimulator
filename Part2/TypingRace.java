import java.awt.*;
import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

/**
 * A typing race simulation. Three typists race to complete a passage of text,
 * advancing character by character — or sliding backwards when they mistype.
 *
 * Originally written by Ty Posaurus, who left this project to "focus on his
 * two-finger technique". He assured us the code was "basically done".
 * We have found evidence to the contrary.
 *
 * @author TyPosaurus and Kyla Saunders
 * @version 4
 */
public class TypingRace
{
    // Variables particular to each race
    private final String passage;
    private final int passageLength;
    private final ArrayList<Typist> participants;
    private static TypingRace race = null;

    // Accuracy thresholds for mistype and burnout events
    private static final BigDecimal MISTYPE_BASE_CHANCE = BigDecimal.valueOf(0.15);
    private static final int SLIDE_BACK_AMOUNT = 2;
    private static final int BURNOUT_DURATION = 3;

    public static void main (String[] args) throws IOException {
        // Set up of the Typists, indication of whether they compete and the list of strings to print the race
        ArrayList<Typist> typists = TypingRace.declareTypists(8);
        ArrayList<JCheckBox> checkboxes = new ArrayList<>();
        ArrayList<String> printList = new ArrayList<>();
        
        // The base of each interface
        JFrame frame = new JFrame("Typing Race");
        JPanel racePanel = new JPanel();
        JPanel settingsPanel = new JPanel();
        JTabbedPane gameTabs = new JTabbedPane();

        // Adding and enabling each interface
        gameTabs.addTab("Typist Settings", settingsPanel);
        gameTabs.addTab("Start Race", racePanel);
        gameTabs.setEnabledAt(1, false);
        frame.add(gameTabs);
        frame.setSize(1250, 750);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Setting up the settings page
        settingsPanel.setLayout(new GridLayout(typists.size() + 2, 3));
        JLabel desc1 = new JLabel("Participating bots (min 2):");
        JLabel ph1 = new JLabel();
        JLabel ph2 = new JLabel();
        settingsPanel.add(desc1);
        settingsPanel.add(ph1);
        settingsPanel.add(ph2);
        for ( Typist t : typists ) {
            JLabel name = new JLabel("Competitor: " + t.getName() + " " + t.getSymbol());
            JLabel info = new JLabel("Accuracy: " + t.getAccuracy() + 
                "\n Win Rate: " + t.getStats().getWins() + " / " + t.getStats().getPlays() +
                "\n Tying Rate: " + t.getStats().getTypes() + " / " + t.getStats().getTypeAttempts());
            JCheckBox box = new JCheckBox();
            checkboxes.add(box);
            settingsPanel.add(name);
            settingsPanel.add(info);
            settingsPanel.add(box);
        }
        JLabel desc2 = new JLabel("Passage (max. 150 characters):");
        JTextField passage = new JTextField("The quick brown dog jumps over the lazy fox.");
        JButton submit = new JButton("Submit");
        submit.addActionListener(e -> submitSettings(checkboxes, typists, passage, gameTabs));
        settingsPanel.add(desc2);
        settingsPanel.add(passage);
        settingsPanel.add(submit);

        // Setting up the race page
        JButton start = new JButton("Start Race");
        start.addActionListener(e -> race.startRace(frame, racePanel, start, printList, typists, gameTabs));
        start.setPreferredSize(new Dimension(80, 50));
        racePanel.add(start);
    }

    /**
     * Constructor for objects of class TypingRace.
     * Sets up the race with a given passage and a list of participants
     *
     * @param pass the passage to be typed
     * @param part the list of participants in the race
     */
    public TypingRace (String pass, ArrayList<Typist> part) {
        this.passage = pass;
        this.passageLength = pass.length();
        this.participants = part;
    }

    /**
     * Declares all Typists by fetching their save data
     * @param typistNum the number of Typists in the game
     * @return an ArrayList of every Typist
     */
    public static ArrayList<Typist> declareTypists (int typistNum) {
        ArrayList<Typist> typists = new ArrayList<>();
        try (BufferedReader istream = new BufferedReader(new FileReader("typistSaveData.txt"))) {
            for ( int c = 0; c < typistNum; c++ ) {
                String n = istream.readLine();
                String s = istream.readLine();
                BigDecimal a = new BigDecimal(istream.readLine());
                int w = Integer.parseInt(istream.readLine());
                int p = Integer.parseInt(istream.readLine());
                int at = Integer.parseInt(istream.readLine());
                int st = Integer.parseInt(istream.readLine());
                Typist t = new Typist(n, s, a, w, p, at, st);
                typists.add(t);
            }
        }
        catch ( IOException e ) { }
        return typists;
    }

    /**
     * Creates the race iteration with the given data and opens the race panel, as long as said data is approriate
     *
     * @param checkboxes the list of checkboxes, correlating to each Typist
     * @param typists the list of Typists that can participate
     * @param pass the passage of text to be typed
     * @param tabs the JTabbedPane holding each Panel
     */
    public static void submitSettings (ArrayList<JCheckBox> checkboxes, ArrayList<Typist> typists, JTextField pass, JTabbedPane tabs) {
        ArrayList<Typist> participants = new ArrayList<>();
        for ( int c = 0; c < checkboxes.size(); c++ ) {
            if ( checkboxes.get(c).isSelected() ) {
                participants.add(typists.get(c));
            }
        }
        String passage = pass.getText();
        if ( passage.equals("") || participants.size() < 2 || passage.length() > 150) {
            return;
        }
        tabs.setEnabledAt(1, true);
        TypingRace r =  new TypingRace(passage, participants);
        TypingRace.race = r;
    }

    /**
     * Starts the typing race by setting the frame to the correct state, resetting all Typists and feeding the strings of progress to a thread
     * 
     * @param frame the JFrame that the game is held in
     * @param panel the JPanel that the game is showed on
     * @param list the list of Strings that are given to the thread
     * @param typists the list of all typists
     * @param tabs the JTabbedPane holding all Panels
     */
    public void startRace (JFrame frame, JPanel panel, JButton start, ArrayList<String> list, ArrayList<Typist> typists, JTabbedPane tabs) {
        tabs.setEnabledAt(0, false);
        boolean finished = false;
        panel.remove(start);
        JTextArea textArea = new JTextArea();
        panel.add(textArea);

        // Reset all typists to the start of the passage
        for ( Typist t : this.participants ) {
            t.resetToStart();
        }
        
        while (!finished)
        {
            // Advance each typist by one turn
            for ( Typist t : this.participants) {
                advanceTypist(t);
            }

            // Print the current state of the race
            String round = printRace();
            list.add(round);

            // Check if any typist has finished the passage
            for ( Typist t : this.participants) {
                if ( raceFinishedBy(t) ) {
                    finished = true;
                }
            }
        }
        ArrayList<Typist> winners = new ArrayList<>();
        ArrayList<Typist> losers = new ArrayList<>();

        // Checks which Typists won
        for ( Typist t : this.participants) {
            if ( raceFinishedBy(t) ) {
                winners.add(t);
                t.getStats().registerGame(true);
            }
            else {
                losers.add(t);
                t.getStats().registerGame(false);
            }
        }
        
        BigDecimal newAccuracy;
        BigDecimal oldAccuracy;
        BigDecimal one = BigDecimal.valueOf(1);
        BigDecimal minAcc = BigDecimal.valueOf(0.1);

        // Prints the relevant message, acknowleging the winner(s) and all changes in accuracy
        if ( winners.size() == 1 ) {
            list.add("The winner is " + winners.get(0).getName() + "!\n");
            oldAccuracy = winners.get(0).getAccuracy();
            if ( oldAccuracy.compareTo(one) == 0) {
                list.add("Their accuracy cannot go past 1.0.\n");
                winners.get(0).setAccuracy(BigDecimal.valueOf(1.0));
            }
            else {
                newAccuracy = winners.get(0).getStats().getNewAccuracy('W');
                list.add("Their accuracy has increased to " + newAccuracy + " from " + oldAccuracy + ".\n");
                winners.get(0).setAccuracy(newAccuracy);
            }
            for ( Typist t : losers) {
                oldAccuracy = t.getAccuracy();
                if ( oldAccuracy.compareTo(minAcc) == 0 ) {
                    list.add( t.getName() + "'s accuracy cannot go below 0.1\n.");
                    t.setAccuracy(BigDecimal.valueOf(0.1));
                }
                else {
                    newAccuracy = t.getStats().getNewAccuracy('L');
                    list.add(t.getName() + "'s accuracy has decreased to " + newAccuracy + " from " + oldAccuracy + ".\n");
                    t.setAccuracy(newAccuracy);
                }
            }
        } 
        else if (winners.size() == participants.size() ) {
            list.add("All three competitiors tied!\n");
            list.add("Their accuracy is unchanged.\n");
        }
        else {
            list.add("It is a tie between:");
            for ( Typist t : winners) {
                oldAccuracy = t.getAccuracy();
                list.add(t.getName() + "! Their accuracy is unchanged from " + oldAccuracy + ".\n");
            }
            for ( Typist t : losers) {
                oldAccuracy = t.getAccuracy();
                if ( oldAccuracy.compareTo(minAcc) == 0 ) {
                    list.add( t.getName() + "'s accuracy cannot go below 0.1\n.");
                    t.setAccuracy(BigDecimal.valueOf(0.1));
                }
                else {
                    newAccuracy = t.getStats().getNewAccuracy('L');
                    list.add(t.getName() + "'s accuracy has decreased to " + newAccuracy + " from " + oldAccuracy + ".\n");
                    t.setAccuracy(newAccuracy);
                }
            }
        }
        startThread(textArea, list);
        winners.clear();
        losers.clear();
        try {
            clearFile();
            for ( Typist t : typists) {
                t.saveData();
            }
        }
        catch ( Exception e ) { }

        JButton reset = new JButton("Play Again");
        panel.add(reset);
        try {
            reset.addActionListener(e -> resetFrame(frame));
        }
        catch (Exception e) {}
    }

    /**
     * Simulates one turn for a typist.
     *
     * If the typist is burnt out, they recover one turn's worth and skip typing.
     * Otherwise:
     *   - They may type a character (advancing progress) based on their accuracy.
     *   - They may mistype (sliding back) — the chance of a mistype should decrease
     *     for more accurate typists.
     *   - They may burn out — more likely for very high-accuracy typists
     *     who are pushing themselves too hard.
     *
     * @param theTypist the typist to advance
     */
    private void advanceTypist (Typist theTypist) {
        if (theTypist.isBurntOut()) {
            // Recovering from burnout — skip this turn
            theTypist.recoverFromBurnout();
            theTypist.getStats().registerType(false);
            return;
        }

        // Attempt to type a character
        BigDecimal randomType = BigDecimal.valueOf(Math.random());
        if ( theTypist.getAccuracy().compareTo(randomType) == 1 ) {
            theTypist.typeCharacter();
        }

        // Mistype check — the probability should reflect the typist's accuracy
        randomType = BigDecimal.valueOf(Math.random());
        BigDecimal mistypeChance = theTypist.getAccuracy().multiply(MISTYPE_BASE_CHANCE);
        if ( mistypeChance.compareTo(randomType) == 1 ) {
            theTypist.slideBack(SLIDE_BACK_AMOUNT);
            theTypist.getStats().registerMistype();
            theTypist.getStats().registerType(false);
        }
        else {
            theTypist.getStats().registerType(true);
        }

        // Burnout check — pushing too hard increases burnout risk
        // (probability scales with accuracy squared, capped at ~0.05)
        randomType = BigDecimal.valueOf(Math.random());
        BigDecimal burnoutChance = theTypist.getAccuracy().multiply(theTypist.getAccuracy().multiply(mistypeChance));
        if ( ( burnoutChance.compareTo(randomType) == 1 ) && ( theTypist.getProgress() != this.passageLength ) ) {
            theTypist.burnOut(BURNOUT_DURATION);
        }
    }

    /**
     * Returns true if the given typist has completed the full passage.
     *
     * @param theTypist the typist to check
     * @return true if their progress has reached or passed the passage length
     */
    private boolean raceFinishedBy (Typist theTypist) {
        return theTypist.getProgress() >= passageLength;
    }

    /**
     * Gets the String for the current state of the race to be displayed
     * Shows each typist's position along the passage, burnout state, and whether they just mistyped
     * 
     * @return the String displaying the current progress and state of every Typist, along with race details
     */
    public String printRace () {
        String raceRound = "\n  TYPING RACE - passage length: " + passageLength + " chars \n" 
            + multiplePrint('=', passageLength + 3) + "\n ";

        for ( Typist t : this.participants) {
                raceRound = raceRound + "\n" + printSeat(t);
            }
        raceRound = raceRound + "\n" + multiplePrint('=', passageLength + 3) + "\n[~] = burnt out    [<] = just mistyped\n ";
        return raceRound;
    }

    /**
     * Gets the String for the current state of a single typist's lane.
     *
     * @param theTypist the typist whose lane to print
     * @return the String detailing the current progress and state of a Typist
     */
    private String printSeat (Typist theTypist) {
        //Creates the string to show the progress
        String[] splitPassage = passage.split("");
        int typedLetters = theTypist.getProgress();
        String totalString = "| ";
        for ( int c = 0; c < typedLetters; c++ ) {
            totalString = totalString + splitPassage[c];
        }

        totalString = totalString + theTypist.getSymbol();

        for ( int c = typedLetters; c < splitPassage.length; c++ ) {
            totalString = totalString + splitPassage[c];
        }
        totalString = totalString + " |   ";

        // Print name and accuracy
        if (theTypist.isBurntOut()) {
            totalString = totalString + (theTypist.getName()
                + " (Accuracy: " + theTypist.getAccuracy() + ")"
                + " BURNT OUT (" + theTypist.getBurnoutTurnsRemaining() + " turns)");
        }
        else {
            totalString = totalString + (theTypist.getName()
                + " (Accuracy: " + theTypist.getAccuracy() + ")");
                if ( theTypist.getStats().getMistype() ) {
                    int change = theTypist.getProgress() - theTypist.getStats().getLastProgress();
                    totalString = totalString + " SLID BACK (" + change + " SPACES)";
                }
        }
        return totalString;
    }

    /**
     * Prints a character a given number of times.
     *
     * @param aChar the character to print
     * @param times how many times to print it
     * @return the String of aChar the correct number of times
     */
    private String multiplePrint (char aChar, int times) {
        int i = 1;
        String total = Character.toString(aChar);
        while (i < times)
        {
            total = total + Character.toString(aChar);
            i = i + 1;
        }
        return total;
    }


    /**
     * Clears the save data file by deleting and recreating it
     */
    private void clearFile () {
        try {
            File data = new File("typistSaveData.txt");
            if ( data.delete() ) {
                data.createNewFile();
            }
        }
        catch ( IOException e ) { }
    }

    /**
     * Handles updating the GUI at a reasonable rate
     * 
     * @param txt the JTextArea added to the JPanel to display the state of the race
     * @param list the list of Strings that are printed to display the state of the race
     */
    @SuppressWarnings("rawtypes")
    private static void startThread (JTextArea txt, ArrayList<String> list) {
        @SuppressWarnings("unchecked")
        SwingWorker<Boolean, String> sw = new SwingWorker() {
            @Override
            // Passes the Strings added to the list of Strings to a list of chunks for process() to handle
            protected String doInBackground () throws Exception {
                for ( String s : list) {
                    publish(s);
                    Thread.sleep(1000);
                }
                return null;
            }
            @Override
            //Constantly runs in the background, emptying the JTextArea and adding the new state of the race, then pausing
            protected void process (List chunks) {
                for ( Object obj : chunks ) {
                    String newString = String.valueOf(obj);
                    if (newString.startsWith("\n  TYPING")) {
                        txt.selectAll(); 
                        txt.replaceSelection("");
                    }
                    txt.append(newString);
                }
            }
        };
        sw.execute();
    }
    /**
     * Resets the JFrame so the game can be replayed without closing and reopening the game
     * 
     * @param frame the JFrame the game is held in
     */
    public static void resetFrame (JFrame frame) {
        try {
            frame.dispose();
            main(null);
        }
        catch ( IOException e ) { }
    }
}