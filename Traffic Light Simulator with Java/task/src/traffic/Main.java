package traffic;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/*
Completed Stage 5/6
Traffic Light Simulator with Java - https://hyperskill.org/projects/288/stages/1500/implement
Part of Hyperskill's Java Backend Developer (Spring Boot) course.
 */
public class Main {
    private static final String WELCOME_TEXT = "Welcome to the traffic management system!";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println(WELCOME_TEXT);
        System.out.print("Input the number of roads:");
        int numberOfRoads = getValidInput(scanner);

        System.out.print("Input the interval:");
        int interval = getValidInput(scanner);

        TrafficLightApplication trafficLightApplication = new TrafficLightApplication(scanner, numberOfRoads, interval);
        trafficLightApplication.navigateMenu();
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
}

/**
 * Handles everything related to the Traffic Light application.
 */
class TrafficLightApplication {
    private final Scanner scanner;
    private final int numberOfRoads, interval;
    private final TimeElapsed timeElapsedThread;

    private final CircularArrayDeque roadsQueue;


    public TrafficLightApplication(Scanner scanner, int numberOfRoads, int interval) {
        this.scanner = scanner;
        this.numberOfRoads = numberOfRoads;
        this.interval = interval;
        this.roadsQueue = new CircularArrayDeque(numberOfRoads);
        timeElapsedThread = new TimeElapsed(numberOfRoads, interval);
        timeElapsedThread.setName("QueueThread");
        timeElapsedThread.start();
    }

    /**
     * Navigates the menu and performs actions based on that.
     */
    public void navigateMenu() {
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
                    System.out.println("Input road name:");
                    String roadName = scanner.nextLine();
                    if (roadsQueue.enqueue(roadName)) {
                        System.out.println(roadName + " Added!");
                    } else {
                        System.out.println("Queue is full!");
                    }
                    scanner.nextLine();
                }
                case 2 -> {
                    String removedRoad = roadsQueue.dequeue();
                    if (removedRoad == null) {
                        System.out.println("Queue is empty!");
                    } else {
                        System.out.println(removedRoad + " deleted!");
                    }
                    scanner.nextLine();
                }
                case 3 -> {
                    timeElapsedThread.setRoadsQueue(roadsQueue);
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
     * Prints the menu.
     */
    private static void printMenu() {
        System.out.println("""
                Menu:
                1. Add road
                2. Delete road
                3. Open system
                0. Quit""");
    }
}

/**
 * Implements {@link ArrayDeque} to create a fixed size circular queue.
 * Doesn't allow to add any elements if the queue is full.
 */
class CircularArrayDeque {
    private final Queue<String> queue;
    private final int capacity;

    public CircularArrayDeque(int capacity) {
        this.capacity = capacity;
        this.queue = new ArrayDeque<>(capacity);
    }

    /**
     *
     * @param data String data to be added
     * @return true if added successfully, false if the queue is full
     */
    public boolean enqueue(String data) {
        if (queue.size() >= capacity) {
            return false;
        } else {
            queue.offer(data);
            return true;
        }
    }

    /**
     * Remove and return the front element
     */
    public String dequeue() {
        return queue.poll();
    }

    public void display() {
        for (String data : queue) {
            System.out.println(data);
        }
    }
}

/**
 * Counts the seconds elapsed since program start.
 * Has {@link #printInfo} that can be set to true to print time elapsed, number of roads, interval and the actual roads.
 * Need to set the {@link  CircularArrayDeque} for them to be printed via the {@link #setRoadsQueue(CircularArrayDeque)}
 */
class TimeElapsed extends Thread {
    private volatile boolean running = true;
    private boolean printInfo = false;
    private final int numberOfRoads, interval;
    private CircularArrayDeque roadsQueue;

    public void setPrintInfo(boolean printInfo) {
        this.printInfo = printInfo;
    }

    public void setRoadsQueue(CircularArrayDeque roadsQueue) {
        this.roadsQueue = roadsQueue;
    }

    public TimeElapsed(int numberOfRoads, int interval) {
        this.numberOfRoads = numberOfRoads;
        this.interval = interval;
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        while (running) {
            long secondsElapsed = (System.currentTimeMillis() - startTime) / 1000;

            if (printInfo) {
                System.out.printf("""
                        ! %ds. have passed since system startup !
                        ! Number of roads: %d !
                        ! Interval: %d !
                        
                        """, secondsElapsed, numberOfRoads, interval);
                roadsQueue.display();
                System.out.println("""
                        
                        ! Press "Enter" to open menu !""");
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
