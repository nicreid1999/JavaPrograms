import java.util.Scanner;

/**
 * The Register class used to create Register objects
 */
public class Register {
    /**
     * double array to hold change integers
     */
    private static double[] change = {2000, 1000, 500, 100, 25, 10, 5, 1};

    /**
     * String array to hold names of change
     */
    private static String[] types = {"20-dollar bill(s)", "10-dollar bill(s)", "5-dollar bill(s)", "1-dollar bill(s)", "quarter(s)", "dime(s)", "nickel(s)", "penny(ies)"};
    /**
     * int array to hold amount of change left
     */
    private static int[] changeLeft = new int[8];

    /**
     * Constructs a Register with random variables between 1 and 15 for each type of change
     */
    public Register() {
        for (int k = 0; k < changeLeft.length; k++) {
            changeLeft[k] = (int) (Math.random() * 16);
        }
    }

    /**
     * Uses the register by displaying all available change and whether you would like a new purchase or to exit the program
     */
    static void useRegister() {
            System.out.println("\n\n------------------");
            System.out.println("- Cash Register: -");
            System.out.println("------------------\n");
            for (int n = 0; n < types.length; n++) {
                System.out.println(types[n] + ": " + changeLeft[n]);
            }
            System.out.println("0 - Exit Program\n1 - New Purchase");
            while (true) {
                Scanner response = new Scanner(System.in);
                int responseInt = response.nextInt();
                if (responseInt == 0) {
                    System.exit(0);
                } else if (responseInt == 1) {
                    break;
                } else {
                    System.out.println("That is not a valid input, please try again");
                }
            }
            returnChange();
    }

    /**
     * Calculates how much change must be returned based on cost and how much was paid
     */
    private static void returnChange(){
        int[] paidBack = new int[8];
        int i = 0;
        System.out.print("Cost of Item: ");
        Scanner input = new Scanner(System.in);
        double cost = input.nextDouble();
        double paid = 0;
        System.out.print("Amount Paid: ");
        for(int k = 0; k < changeLeft.length-1; k++){
            System.out.print(types[k] + ": ");
            Scanner input2 = new Scanner(System.in);
            double amount = input2.nextDouble();
            changeLeft[k]+=amount;
            paid+=(amount*(change[k]/100));
        }
        double toreturn = paid * 100 - cost * 100;

        while (toreturn != 0) {
            int holder = (int) Math.floor(toreturn / change[i]);
            if (holder > changeLeft[i] && i == changeLeft.length-1) {
                System.out.println("You do not have enough change to give");
                break;
            } else if (holder > changeLeft[i] || holder == 0) {
                i++;
            } else {
                toreturn = toreturn % change[i];
                paidBack[i] = holder;
                changeLeft[i] = changeLeft[i] - holder;
                i++;
            }
        }
        System.out.print("Give Back: ");
        for (int j = 0; j < paidBack.length; j++) {
            if (paidBack[j] != 0) {
                System.out.print(paidBack[j] + " " + types[j] + ", ");
            }
        }
        System.out.println("\n");
        useRegister();
    }
}
