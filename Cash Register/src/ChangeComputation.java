/*
    Name: Nicholas Reid
    Class: Intro to Software Design
    Date: 9/5/19
 */

/**
 * The class that creates a register object and runs the useRegister() method
 */
public class ChangeComputation {
    /**
     * Creates a new Register object and calls useRegister()
     * @param args
     */
    public static void main(String[] args) {
        Register business = new Register();
        Register.useRegister();
    }
}
