package traffic;

import java.util.Scanner;

/*
Stage 1/6 Completed
Traffic Light Simulator with Java - https://hyperskill.org/projects/288/stages/1500/implement
Part of Hyperskill's Java Backend Developer (Spring Boot) course.
 */
public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to the traffic management system!");

        System.out.print("Input the number of roads:");
        int numberOfRoads = scanner.nextInt();

        System.out.print("Input the interval:");
        int interval = scanner.nextInt();

        boolean quitSelected = false;
        while (!quitSelected) {
            printMenu();
            int input = scanner.nextInt();

            switch (input) {
                case 1 -> {
                    numberOfRoads++;
                    System.out.println("Road added");
                }
                case 2 -> {
                    numberOfRoads--;
                    System.out.println("Road deleted");
                }
                case 3 -> {
                    System.out.println("System opened");
                }
                case 0 -> {
                    System.out.println("Bye!");
                    quitSelected = true;
                }
            }
        }
    }

    private static void printMenu() {
        System.out.println("""
                Menu:
                1. Add
                2. Delete
                3. System
                0. Quit""");
    }
}
