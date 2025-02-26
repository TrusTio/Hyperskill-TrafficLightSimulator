package traffic;

import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/*
Completed Stage 5/6
Traffic Light Simulator with Java - https://hyperskill.org/projects/288/stages/1500/implement
Part of Hyperskill's Java Backend Developer (Spring Boot) course.
 */

enum Color {
    ANSI_RED("\u001B[31m"), ANSI_GREEN("\u001B[32m"), ANSI_YELLOW("\u001B[33m"), ANSI_RESET("\u001B[0m");

    private final String code;

    Color(String ansiCode) {
        this.code = ansiCode;
    }

    @Override
    public String toString() {
        return code;
    }
}

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
    private final CircularArrayQ roadsQueue;


    public TrafficLightApplication(Scanner scanner, int numberOfRoads, int interval) {
        this.scanner = scanner;
        this.numberOfRoads = numberOfRoads;
        this.interval = interval;
        this.roadsQueue = new CircularArrayQ(numberOfRoads);
        timeElapsedThread = new TimeElapsed(numberOfRoads, interval);
        timeElapsedThread.setRoadsQueue(roadsQueue);
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
                    Road road = new Road();
                    road.setName(scanner.nextLine());
                    if (roadsQueue.enqueue(road)) {
                        System.out.println(road.getName() + " Added!");
                    } else {
                        System.out.println("Queue is full!");
                    }
                    scanner.nextLine();
                }
                case 2 -> {
                    Road removedRoad = roadsQueue.dequeue();
                    if (removedRoad == null) {
                        System.out.println("Queue is empty!");
                    } else {
                        System.out.println(removedRoad.getName() + " deleted!");
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

class Road {
    private boolean isOpen;
    private String name;
    private long timeTillNextSwitch;

    public Road(String name) {
        this.name = name;
    }

    public Road() {
    }

    public boolean isOpen() {
        return isOpen;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTimeTillNextSwitch(long timeTillNextSwitch) {
        this.timeTillNextSwitch = timeTillNextSwitch;
    }

    public long getTimeTillNextSwitch() {
        return timeTillNextSwitch;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }

    public void switchStatus(long timeTillNextSwitch) {
        if (isOpen) {
            this.timeTillNextSwitch = timeTillNextSwitch;
            isOpen = false;
        } else {
            this.timeTillNextSwitch = timeTillNextSwitch;
            isOpen = true;
        }

    }

}

/**
 * Circular queue implemented with array
 */
class CircularArrayQ {
    private Road[] queue;
    private int front, rear, size, capacity;
    private int currentOpenIndex = -1;
    boolean firstRoadAdded = false;
    private long systemStartTime = -1;

    public CircularArrayQ(int capacity) {
        this.capacity = capacity;
        this.queue = new Road[capacity];
        front = rear = -1;
        size = 0;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean isFull() {
        return size == capacity;
    }

    // add an element and move the rear to its new position, if full, return false
    public boolean enqueue(Road data) {
        if (isFull()) {
            return false;
        } else if (isEmpty()) {
            front = rear = 0;
            systemStartTime = System.currentTimeMillis();
            currentOpenIndex = front; // set to open if first element
            firstRoadAdded = true;
            queue[rear] = data;
        } else {
            rear = (rear + 1) % capacity;
            queue[rear] = data;
            queue[rear].setTimeTillNextSwitch(queue[rear - 1].getTimeTillNextSwitch());
        }

        size = Math.min(size + 1, capacity);
        return true;
    }

    // remove the oldest added element(the front) and move the front position to the next one
    public Road dequeue() {
        if (isEmpty()) {
            return null;
        }

        Road data = queue[front];
        queue[front] = null;
        if (front == rear) { //only one element present
            front = rear = -1;
        } else {
            front = (front + 1) % capacity;
        }
        size--;
        if (isEmpty()) currentOpenIndex = -1; // no roads = no open index
        return data;


    }

    public Road peek() {
        if (isEmpty()) {
            return null;
        }
        return queue[front];
    }

    // iterate over the array in the correct queue order and display values
    public void display() {
        int i = front;
        for (int count = 0; count < size; count++) {
            long secondsLeft = queue[i].getTimeTillNextSwitch() - System.currentTimeMillis() / 1000;
            if (queue[i].isOpen()) {
                System.out.println(Color.ANSI_GREEN + queue[i].getName() + " will be open for " + secondsLeft + "s." + Color.ANSI_RESET);
            } else {
                System.out.println(Color.ANSI_RED + queue[i].getName() + " will be closed for " + secondsLeft + "s." + Color.ANSI_RESET);
            }
            i = (i + 1) % capacity; // move to the next index in the correct order
        }
    }

    //TODO: when you add a new road, the closed timestamp is incorrect.
    public void updateRoadStatuses(int interval, long secondsElapsed) {
        if (firstRoadAdded) {
            queue[front].switchStatus((systemStartTime / 1000) + interval);
            queue[front].setOpen(true);
            firstRoadAdded = false;
        }

        long elapsedSinceStart = System.currentTimeMillis() - systemStartTime;
        if ((elapsedSinceStart / 1000) % interval == 0 && peek() != null) {
            if (size == 1) { // if the queue has only a single road, keep it open at all times.
                long timeTillNextSwitch = (System.currentTimeMillis() / 1000) + interval;
                queue[front].switchStatus(timeTillNextSwitch);
                queue[front].setOpen(true);
            } else {
                int i = front;
                long timeTillNextSwitch = (System.currentTimeMillis() / 1000) + interval;
                System.out.println("CURRENT OPEN INDEX: " + currentOpenIndex);
                System.out.println(queue[currentOpenIndex].getName() + " Status switched1.");
                queue[currentOpenIndex].switchStatus(timeTillNextSwitch);
                currentOpenIndex = (currentOpenIndex + 1) % size;

                System.out.println("Updating current open index to the next one " + currentOpenIndex);
                System.out.println(queue[currentOpenIndex].getName() + " Status switched2.");
                queue[currentOpenIndex].switchStatus(timeTillNextSwitch);
                for (int count = 0; count < size; count++) {
                    i = (i + 1) % capacity; // move to the next index in the correct order
                }

            }
        }
    }
}

/**
 * Counts the seconds elapsed since program start.
 * Has {@link #printInfo} that can be set to true to print time elapsed, number of roads, interval and the actual roads.
 * Need to set the {@link  CircularArrayQ} for them to be printed via the {@link #setRoadsQueue(CircularArrayQ)}
 */
class TimeElapsed extends Thread {
    private volatile boolean running = true;
    private boolean printInfo = false;
    private final int numberOfRoads, interval;
    private CircularArrayQ roadsQueue;

    public void setPrintInfo(boolean printInfo) {
        this.printInfo = printInfo;
    }

    public void setRoadsQueue(CircularArrayQ roadsQueue) {
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
            roadsQueue.updateRoadStatuses(interval, (System.currentTimeMillis() - startTime));

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
