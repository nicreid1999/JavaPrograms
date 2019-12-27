/**
 * Class GuessNumber creates a new Guess objects and sets it to visible
 * @author nicreid
 */
public class GuessNumber{
    /**
     * Main creates a new Guess object as newGame and sets its size and visibility
     * @param args
     */
    public static void main(String[] args){
        Guess newGame = new Guess();
        newGame.GUI.setSize(600, 200);
        newGame.GUI.setVisible(true);
    }

}
