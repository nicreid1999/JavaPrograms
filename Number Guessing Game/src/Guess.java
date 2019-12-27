import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static java.lang.Math.random;

/**
 * Class Guess creates the GUI and updates accordingly
 * @author nicreid
 */
public class Guess{
    /**
     * lastGuess holds the integer of the last guess
     */
    private int lastGuess = 0;
    /**
     * GUI creates the Guess The Number Frame
     */
    protected JFrame GUI = new JFrame("Guess The Number");
    /**
     * guesser is the panel that all pieces are stored in
     */
    private static JPanel guesser = new JPanel();
    /**
     * Intro has the beginning JLabel that will show throughout the game
     */
    private JLabel Intro = new JLabel("I have a number between 1 and 1000. Can you guess my number?");
    /**
     * hint shows whether the number is too high or too low
     */
    private static JLabel hint = new JLabel("Please enter your first guess.");
    /**
     * guessField is where guesses are input
     */
    private static JTextField guessField = new JTextField();
    /**
     * gameButton allows you to restart the game
     */
    private JButton gameButton = new JButton("Play Again");
    /**
     * number is the randomly chosen number for the game
     */
    private static int number = 0;

    /**
     * getNumber returns the hidden number
     * @return number
     */
    public static int getNumber() {
        return number;
    }

    /**
     * setNumber sets the number to a randomly assigned int between 1 and 1000
     */
    public void setNumber(){
        number = (int) (random()*1000+1);
    }

    /**
     * lockGuess sets the JTextField guessField to locked so that it cannot be edited
     * @param lock
     */
    public static void lockGuess(boolean lock){
        guessField.setEditable(!lock);
    }


    /**
     * restartGame resets the GUI to the original design
     */
    private static void restartGame(){
        hint.setText("Please enter your first guess.");
        guessField.setEditable(true);
        guessField.setText("");
        guesser.setBackground(Color.LIGHT_GRAY);
    }

    /**
     * setHint updates the JLabel to whichever hint is chosen
     * @param update
     */
    public static void setHint(String update){
        hint.setText(update);
    }


    /**
     * Guess is the constructor of the GUI
     */
    public Guess(){
        guesser.setLayout(new BoxLayout(guesser, BoxLayout.Y_AXIS));
        setNumber();
        GUI.add(guesser);
        guesser.add(Intro);
        guesser.add(hint);
        checkGuess GuessListener = new checkGuess();
        guessField.addActionListener(GuessListener);
        guesser.add(guessField);
        guesser.add(gameButton);
        buttonPressed newGame = new buttonPressed();
        gameButton.addActionListener(newGame);
    }

    /**
     * class checkGuess compares the Guess to the random number and updates the GUI accordingly
     * @see ActionListener
     * @author nicreid
     */
    class checkGuess implements ActionListener{
        /**
         * if enter is pressed, compare the guess and update the GUI accordingly
         * @param e
         */
        public void actionPerformed(ActionEvent e) {
            int theirGuess = Integer.parseInt(guessField.getText());

            if(Math.abs(lastGuess-number) > Math.abs(theirGuess-number)){
                guesser.setBackground(Color.RED);
            }
            else {
                guesser.setBackground(Color.BLUE);
            }

            if(number > theirGuess){
                setHint("Too low!");
            }

            else if(number < theirGuess){
                setHint("Too high!");
            }

            else{
                GUI.setBackground(Color.LIGHT_GRAY);
                setHint("Correct! Play Again Below!");
                lockGuess(true);
            }

            lastGuess = theirGuess;
    }
    }

    /**
     * Class buttonPressed determines if the Play Again button is pressed and restarts the game
     * @see ActionListener
     * @author nicreid
     */
    class buttonPressed implements ActionListener{
        /**
         * if the button is pressed, restart the game
         * @param e
         */
        public void actionPerformed(ActionEvent e) {
            restartGame();
        }
    }
}
