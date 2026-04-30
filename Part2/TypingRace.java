import java.awt.*;
import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
 * @version 2
 */
public class TypingRace
{
    private final int passageLength;   // Total characters in the passage to type
    private Typist seat1Typist;
    private Typist seat2Typist;
    private Typist seat3Typist;

    // Accuracy thresholds for mistype and burnout events
    // (Ty tuned these values "by feel". They may need adjustment.)
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
     * Starts the typing race.
     * All typists are reset to the beginning, then the simulation runs
     * turn by turn until one typist completes the full passage.
     *
     * Note from Ty: "I didn't bother printing the winner at the end,
     * you can probably figure that out yourself."
     */
    @SuppressWarnings("UseSpecificCatch")
    public void startRace()
    {
        boolean finished = false;

        // Reset all typists to the start of the passage
        seat1Typist.resetToStart();
        seat2Typist.resetToStart();
        seat3Typist.resetToStart();
        
        while (!finished)
        {
            // Advance each typist by one turn
            advanceTypist(seat1Typist);
            advanceTypist(seat2Typist);
            advanceTypist(seat3Typist);

            // Print the current state of the race
            printRace();

            // Check if any typist has finished the passage
            if ( raceFinishedBy(seat1Typist) || raceFinishedBy(seat2Typist) || raceFinishedBy(seat3Typist) )
            {
                finished = true;
            }

            // Wait 200ms between turns so the animation is visible
            try {
                TimeUnit.MILLISECONDS.sleep(1500);
            } catch (Exception e) {}
        }

        // Creates space to print the win message
        System.out.println();
        System.out.println();

        ArrayList<Typist> winners = new ArrayList<>();
        ArrayList<Typist> losers = new ArrayList<>();

        // Checks which Typists won
        if ( raceFinishedBy(seat1Typist) ) {
            winners.add(seat1Typist);
        }
        else {
            losers.add(seat1Typist);
        }
        if ( raceFinishedBy(seat2Typist) ) {
            winners.add(seat2Typist);
        }
        else {
            losers.add(seat2Typist);
        }
        if ( raceFinishedBy(seat3Typist) ) {
            winners.add(seat3Typist);
        }
        else {
            losers.add(seat3Typist);
        }
        
        BigDecimal newAccuracy;
        BigDecimal oldAccuracy;
        BigDecimal one = BigDecimal.valueOf(1);
        BigDecimal minAcc = BigDecimal.valueOf(0.1);

        // Prints the relevant message, acknowleging the winner(s) and all changes in accuracy
        switch ( winners.size() ) {
            case 1 -> {
                System.out.println("The winner is " + winners.get(0).getName() + "!");
                oldAccuracy = winners.get(0).getAccuracy();
                if ( oldAccuracy.compareTo(one) == 0) {
                    System.out.println("Their accuracy cannot go past 1.0.");
                }
                else {
                    newAccuracy = winners.get(0).getStats().getNewAccuracy('W');
                    System.out.println("Their accuracy has increased to " + newAccuracy + " from " + oldAccuracy + ".");
                    winners.get(0).setAccuracy(newAccuracy);
                }
                for ( Typist t : losers) {
                    oldAccuracy = t.getAccuracy();
                    if ( oldAccuracy.compareTo(minAcc) == 0 ) {
                        System.out.println( t.getName() + "'s accuracy cannot go below 0.1.");
                    }
                    else {
                        newAccuracy = t.getStats().getNewAccuracy('L');
                        System.out.println(t.getName() + "'s accuracy has decreased to " + newAccuracy + " from " + oldAccuracy + ".");
                        t.setAccuracy(newAccuracy);
                    }
                }
            }
            case 2 -> {
                System.out.println("It is a tie between " + winners.get(0).getName() + " and " + winners.get(1).getName() + "!");
                for ( Typist t : winners) {
                    oldAccuracy = t.getAccuracy();
                    System.out.println(t.getName() + "'s accuracy is unchanged from " + oldAccuracy + ".");
                }
                oldAccuracy = losers.get(0).getAccuracy();
                if ( oldAccuracy.compareTo(minAcc) == 0) {
                    System.out.println(losers.get(0).getName() + "'s accuracy cannot go below 0.1.");
                }
                else {
                    newAccuracy = winners.get(0).getStats().getNewAccuracy('L');
                    System.out.println(losers.get(0).getName() + "'s accuracy has decreased to " + newAccuracy + " from " + oldAccuracy + ".");
                    losers.get(0).setAccuracy(newAccuracy);
                }
            }
            case 3 -> {
                System.out.println("All three competitiors tied!");
                System.out.println("Their accuracy is unchanged.");
            }
        }
        winners.clear();
        losers.clear();
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
    private void advanceTypist(Typist theTypist)
    {
        if (theTypist.isBurntOut())
        {
            // Recovering from burnout — skip this turn
            theTypist.recoverFromBurnout();
            return;
        }

        // Attempt to type a character
        BigDecimal randomType = BigDecimal.valueOf(Math.random());
        if ( theTypist.getAccuracy().compareTo(randomType) == 1 ) 
        {
            theTypist.typeCharacter();
        }

        // Mistype check — the probability should reflect the typist's accuracy
        randomType = BigDecimal.valueOf(Math.random());
        BigDecimal mistypeChance = theTypist.getAccuracy().multiply(MISTYPE_BASE_CHANCE);
        if ( mistypeChance.compareTo(randomType) == 1 )
        {
            theTypist.slideBack(SLIDE_BACK_AMOUNT);
            theTypist.getStats().registerMistype();
        }

        // Burnout check — pushing too hard increases burnout risk
        // (probability scales with accuracy squared, capped at ~0.05)
        randomType = BigDecimal.valueOf(Math.random());
        BigDecimal burnoutChance = theTypist.getAccuracy().multiply(theTypist.getAccuracy().multiply(mistypeChance));
        if ( ( burnoutChance.compareTo(randomType) == 1 ) && ( theTypist.getProgress() != this.passageLength ) )
        {
            theTypist.burnOut(BURNOUT_DURATION);
        }
    }

    /**
     * Returns true if the given typist has completed the full passage.
     *
     * @param theTypist the typist to check
     * @return true if their progress has reached or passed the passage length
     */
    private boolean raceFinishedBy(Typist theTypist)
    {
        return theTypist.getProgress() >= passageLength;
    }

    /**
     * Prints the current state of the race to the terminal.
     * Shows each typist's position along the passage, burnout state,
     * and a WPM estimate based on current progress.
     */
    private void printRace()
    {
        System.out.print('\u000C'); // Clear terminal

        System.out.println("  TYPING RACE - passage length: " + passageLength + " chars");
        multiplePrint('=', passageLength + 3);
        System.out.println();

        printSeat(seat1Typist);
        System.out.println();

        printSeat(seat2Typist);
        System.out.println();

        printSeat(seat3Typist);
        System.out.println();

        multiplePrint('=', passageLength + 3);
        System.out.println();
        System.out.println("  [~] = burnt out    [<] = just mistyped");
    }

    /**
     * Prints a single typist's lane.
     *
     * Examples:
     *   |          ⌨           | TURBOFINGERS (Accuracy: 0.85)
     *   |    [zz]              | HUNT_N_PECK  (Accuracy: 0.40) BURNT OUT (2 turns)
     *
     * Note: Ty forgot to show when a typist has just mistyped. That would
     * be a nice improvement — perhaps a [<] marker after their symbol.
     *
     * @param theTypist the typist whose lane to print
     */
    private void printSeat(Typist theTypist)
    {
        int spacesBefore = theTypist.getProgress();
        int spacesAfter  = passageLength - theTypist.getProgress();

        System.out.print('|');
        multiplePrint(' ', spacesBefore);

        // Always show the typist's symbol so they can be identified on screen.
        // Append ~ when burnt out so the state is visible without hiding identity.
        System.out.print(theTypist.getSymbol());
        if (theTypist.isBurntOut())
        {
            System.out.print('~');
            spacesAfter--; // symbol + ~ together take two characters
        }
        else if (theTypist.getStats().getMistype())
        {
            System.out.print('<');
            spacesAfter--;
        }

        multiplePrint(' ', spacesAfter);
        System.out.print('|');
        System.out.print(' ');

        // Print name and accuracy
        if (theTypist.isBurntOut())
        {
            System.out.print(theTypist.getName()
                + " (Accuracy: " + theTypist.getAccuracy() + ")"
                + " BURNT OUT (" + theTypist.getBurnoutTurnsRemaining() + " turns)");
        }
        else
        {
            System.out.print(theTypist.getName()
                + " (Accuracy: " + theTypist.getAccuracy() + ")");
        }
    }

    /**
     * Prints a character a given number of times.
     *
     * @param aChar the character to print
     * @param times how many times to print it
     */
    private void multiplePrint(char aChar, int times)
    {
        int i = 0;
        while (i < times)
        {
            System.out.print(aChar);
            i = i + 1;
        }
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