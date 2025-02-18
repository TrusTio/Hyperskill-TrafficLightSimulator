package traffic;

import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/*
Completed Stage 4/6
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

        TimeElapsed timeElapsedThread = new TimeElapsed();
        timeElapsedThread.setName("QueueThread");
        timeElapsedThread.start();

        navigateMenu(scanner, numberOfRoads, interval, timeElapsedThread);

    }

    /**
     * Navigates the menu and performs actions based on that.
     * @param scanner       {@link Scanner} to be used for input
     * @param numberOfRoads
     * @param interval
     * @param timeElapsedThread {@link TimeElapsed} thread to be used to print the elapsed time since program start
     */
    private static void navigateMenu(Scanner scanner, int numberOfRoads, int interval, TimeElapsed timeElapsedThread) {
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
                    timeElapsedThread.setNumberOfRoads(numberOfRoads);
                    timeElapsedThread.setInterval(interval);
                    timeElapsedThread.setPrintInfo(true);
                    scanner.nextLine();
                    timeElapsedThread.setPrintInfo(false);
                }
                case 0 -> {
                    timeElapsedThread.stopThread();
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
     *
     * @param scanner {@link Scanner} to be used for the input
     * @return int input
     */
    private static int getValidInput(Scanner scanner) {
        int input;
        while (true) {
            try {
                input = Integer.parseInt(scanner.nextLine());
                if (input > 0) {
                    break; // valid input, break the loop
                }
            } catch (NumberFormatException ignore) {
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

/**
 * Counts the seconds elapsed since program start. Has a boolean printMenu that can be set to true to print
 */
class TimeElapsed extends Thread {
    private volatile boolean running = true;
    private boolean printInfo = false;
    private int numberOfRoads, interval;

    public void setPrintInfo(boolean printInfo) {
        this.printInfo = printInfo;
    }

    public void setNumberOfRoads(int numberOfRoads) {
        this.numberOfRoads = numberOfRoads;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    @Override
    public void run() {
        long startTime = System.nanoTime(); // Capture the exact start time

        while (running) {
            long elapsedTime = (System.nanoTime() - startTime) / 1_000_000_000; // Convert nanoseconds to seconds
            int secondsElapsed = (int) elapsedTime;
            if (printInfo) {
                System.out.printf("""
                        ! %ds. have passed since system startup !
                        ! Number of roads: %d !
                        ! Interval: %d !
                        ! Press "Enter" to open menu !
                        """, secondsElapsed, numberOfRoads, interval);
            }
            try {
                TimeUnit.MILLISECONDS.sleep(1000);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    public void stopThread() {
        running = false;
        this.interrupt(); // interrupt if sleeping
    }
}
