package traffic;

import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/*
TODO: Fails on Test #7 of Stage 4/6 currently
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
     *
     * @param scanner       {@link Scanner} to be used for input
     * @param numberOfRoads
     * @param interval
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
                    System.out.println("System opened");
                    PrintSystem printSystem = new PrintSystem(timeElapsedThread, interval, numberOfRoads);
                    printSystem.start();

                    scanner.nextLine();

                    printSystem.stopThread();
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
        int input = 0;
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

class TimeElapsed extends Thread {
    private volatile boolean running = true;
    private int secondsElapsed = 0;

    public int getSecondsElapsed() {
        return secondsElapsed;
    }

    @Override
    public void run() {
        long startTime = System.nanoTime(); // Capture the exact start time

        while (running) {
            long elapsedTime = (System.nanoTime() - startTime) / 1_000_000_000; // Convert nanoseconds to seconds
            secondsElapsed = (int) elapsedTime;
            try {
                TimeUnit.MILLISECONDS.sleep(500);
                secondsElapsed++;
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

class PrintSystem extends Thread {
    private volatile boolean running = true; // Volatile flag to stop thread safely
    private TimeElapsed timeElapsedThread;
    private int interval, numberOfRoads;
    private int lastPrintedSeconds = -1; // Track last printed time

    public PrintSystem(TimeElapsed timeElapsedThread, int interval, int numberOfRoads) {
        this.timeElapsedThread = timeElapsedThread;
        this.interval = interval;
        this.numberOfRoads = numberOfRoads;
    }

    @Override
    public void run() {
        while (running) {
            int currentSeconds = timeElapsedThread.getSecondsElapsed();
            if (currentSeconds != lastPrintedSeconds) { // Only print if time has changed
                lastPrintedSeconds = currentSeconds;
                System.out.printf("""
                        ! %ds. have passed since system startup !
                        ! Number of roads: %d !
                        ! Interval: %d !
                        ! Press "Enter" to open menu !
                        """, timeElapsedThread.getSecondsElapsed(), numberOfRoads, interval);
            }
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    public void stopThread() {
        running = false; // Set flag to false to stop thread
        this.interrupt(); // interrupt if sleeping
    }
}
