import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Public class GUIFrame creates a new GUI for a Arabic to Roman and Roman to Arabic converter
 * @author nicreid
 */
public class GUIFrame {
    /**
     * character array with Roman Numerals
     */
    char[] Roman = {'M', 'D', 'C', 'L', 'X', 'V', 'I'};

    /**
     * String array for Roman combinations that are singular or subtract
     */
    String [] RomanTypes = new String[]{"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};

    /**
     * Integer array for all Roman combinations that are singular or subtract as arabic nubmers
     */
    int[] RomanButIntegers = new int[]{1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
    /**
     * String that holds Roman Numerals
     */
    String romanString = "MDCLXVI";
    /**
     * int array that holds integers compared to Roman numerals
     */
    int[] Amount = {1000, 500, 100, 50, 10, 5, 1};
    /**
     * Frame for holding the Converter
     */
    JFrame GUI = new JFrame("Arabic to Roman GUI");
    /**
     * JPanel that holds the contents of the GUI
     */
    JPanel Converter = new JPanel();
    /**
     * JLabel declaring the Arabic side
     */
    JLabel arabicLabel = new JLabel("Arabic:");
    /**
     * JTextField to hold Arabic input or Roman conversions
     */
    JTextField ArabicInput = new JTextField("", 10);
    /**
     * JLabel declaring the Roman side
     */
    JLabel romanLabel = new JLabel("Roman:");
    /**
     * JTextField to hold Roman input or Arabic conversions
     */
    JTextField RomanInput = new JTextField("", 10);

    /**
     * Constructor that forms the GUI and performs real-time conversions based on keys being pressed on either side,
     * and then displays the conversion on the other JTextField of the GUI
     */
    public GUIFrame(){
        GUI.setSize(400, 80);
        Converter.add(arabicLabel);
        Converter.add(ArabicInput);
        Converter.add(romanLabel);
        Converter.add(RomanInput);
        ArabicInput.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }
            @Override
            public void keyPressed(KeyEvent e) {
            }
            @Override
            public void keyReleased(KeyEvent e) {
                String output = "";
                try {
                    int type = Integer.parseInt(ArabicInput.getText());
                    if(type < 3999 && type > 0) {
                        for (int i = 0; i < RomanTypes.length; i++) {
                            while (type >= RomanButIntegers[i]) {
                                type -= RomanButIntegers[i];
                                output += RomanTypes[i];
                            }
                        }
                        RomanInput.setText(output);
                    }
                    else{
                        RomanInput.setText("Out of Bounds");
                    }
                }
                catch(Exception d){
                    RomanInput.setText("Error");
                }
            }
        });
        RomanInput.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
                int output = 0;

                String input = RomanInput.getText();
                //Source: https://www.oreilly.com/library/view/regular-expressions-cookbook/9780596802837/ch06s09.html
                if(input.matches("^(?=[MDCLXVI])M*(C[MD]|D?C{0,3})(X[CL]|L?X{0,3})(I[XV]|V?I{0,3})$")){
                    char[] charInput = input.toCharArray();
                    for (int i = 0; i < charInput.length - 1; i++) {
                        int location = romanString.indexOf(charInput[i]);
                        int otherLocation = romanString.indexOf(charInput[i + 1]);
                        //If the next character is larger than this, then do the next character minus this one
                        if (location != -1) {
                            if (otherLocation < location) {
                                output += (Amount[otherLocation] - Amount[location]);
                                i++;
                                charInput[i] = ' ';
                            } else {
                                output += Amount[location];
                            }
                        }
                    }
                    int last = romanString.indexOf(charInput[charInput.length - 1]);
                    if (last != -1) {
                        output += Amount[last];
                    }
                    ArabicInput.setText(Integer.toString(output));
            }
                else{
                    ArabicInput.setText("Error");
                }
        }
        });
        GUI.add(Converter);
        GUI.setVisible(true);
    }

}
