package traffic;

import java.util.Scanner;

/*
Stage 3/6 Completed
Traffic Light Simulator with Java - https://hyperskill.org/projects/288/stages/1500/implement
Part of Hyperskill's Java Backend Developer (Spring Boot) course.
 */
public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to the traffic management system!");

        System.out.print("Input the number of roads:");
        int numberOfRoads = getValidInput(scanner);

        System.out.print("Input the interval:");
        int interval = getValidInput(scanner);

        navigateMenu(scanner, numberOfRoads, interval);

    }

    /**
     * Navigates the menu and performs actions based on that.
     * @param scanner {@link Scanner} to be used for input
     * @param numberOfRoads
     * @param interval
     */
    private static void navigateMenu(Scanner scanner, int numberOfRoads, int interval) {
        boolean quitSelected = false;
        while (!quitSelected) {
            printMenu();

            int input = -1;
            try {
                input = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException ignored) {
            }
            switch (input) {
                case 1 -> {
                    numberOfRoads++;
                    System.out.println("Road added");
                    scanner.nextLine();
                }
                case 2 -> {
                    numberOfRoads--;
                    System.out.println("Road deleted");
                    scanner.nextLine();
                }
                case 3 -> {
                    System.out.println("System opened");
                    scanner.nextLine();
                }
                case 0 -> {
                    System.out.println("Bye!");
                    quitSelected = true;
                }
                default -> {
                    System.out.println("Incorrect Option!");
                    scanner.nextLine();
                }
            }
        }
    }

    /**
     * Prompts the user to provide valid int input > 0
     * @param scanner {@link Scanner} to be used for the input
     * @return int input
     */
    private static int getValidInput(Scanner scanner) {
        int input = 0;
        while (true) {
            try {
                input = Integer.parseInt(scanner.nextLine());
                if (input > 0) {
                    break; // valid input, break the loop
                }
            } catch (NumberFormatException e) {
            }
            System.out.println("Incorrect input. Try again:");
        }
        return input;
    }


    /**
     * Prints the menu.
     */
    private static void printMenu() {
        System.out.println("""
                Menu:
                1. Add
                2. Delete
                3. System
                0. Quit""");
    }
}
