package traffic;

import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/*
Stage 6/6 completed - passed all tests.
Traffic Light Simulator with Java - https://hyperskill.org/projects/288/stages/1500/implement
Part of Hyperskill's Java Backend Developer (Spring Boot) course.
 */
enum Color {
    ANSI_RED("\u001B[31m"), ANSI_YELLOW("\u001B[33m"), ANSI_GREEN("\u001B[32m"), ANSI_RESET("\u001B[0m");

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

        Thread.currentThread().setName("Main");
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
    private final CircularRoadQueue roadsQueue;
    private final ScheduledExecutorService scheduler;
    private final RoadTrafficTimer roadTrafficTimer;


    public TrafficLightApplication(Scanner scanner, int numberOfRoads, int interval) {
        this.scanner = scanner;
        this.roadsQueue = new CircularRoadQueue(numberOfRoads, interval);
        this.roadTrafficTimer = new RoadTrafficTimer(numberOfRoads, interval, System.currentTimeMillis());
        ThreadFactory namedThreadFactory = runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("QueueThread");
            return thread;
        };
        scheduler = Executors.newSingleThreadScheduledExecutor(namedThreadFactory);
        scheduler.scheduleAtFixedRate(roadTrafficTimer, 0, 1, TimeUnit.SECONDS);
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
                    roadsQueue.setSystemStartTime(System.currentTimeMillis());
                    roadTrafficTimer.setRoadsQueue(roadsQueue);
                    roadTrafficTimer.setPrintInfo(true);
                    scanner.nextLine();
                    roadTrafficTimer.setPrintInfo(false);
                }
                case 0 -> {
                    roadTrafficTimer.stopThread();
                    scheduler.shutdown();
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
 * Road class to store details about the roads.
 */
class Road {
    private boolean isOpen;
    private String name;
    private long timeTillNextSwitch;

    public Road() {
        isOpen = false;
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
class CircularRoadQueue {
    private static final Logger logger = Logger.getLogger(CircularRoadQueue.class.getName());

    private final Road[] queue;
    private int front, rear, size, capacity;
    private int currentOpenIndex = -1;
    boolean firstUpdatePassed = false;
    private long systemStartTime = -1;
    private long lastRoadUpdateTime = -1;
    private int interval;

    public CircularRoadQueue(int capacity, int interval) {
        this.capacity = capacity;
        this.interval = interval;
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

    public void setSystemStartTime(long systemStartTime) {
        if (this.systemStartTime < 0) { // set only if it hasn't been set before that
            logger.info(Color.ANSI_YELLOW + "setSystemStartTime set");
            this.systemStartTime = systemStartTime;
        }
    }

    // adds an element and move the rear to its new position, if full, return false
    public boolean enqueue(Road data) {
        if (isFull()) {
            return false;
        } else if (isEmpty()) {
            front = rear = 0;
            currentOpenIndex = front;
            queue[rear] = data;
            queue[rear].setOpen(true);

        } else {
            rear = (rear + 1) % capacity;
            queue[rear] = data;
            queue[rear].setOpen(false);

            // if previous road is open, time till next open is the same, otherwise previous + interval
            if (queue[rear - 1].isOpen()) {
                queue[rear].setTimeTillNextSwitch(queue[rear - 1].getTimeTillNextSwitch());

            } else {
                queue[rear].setTimeTillNextSwitch(queue[rear - 1].getTimeTillNextSwitch() + interval);
            }
        }

        size = Math.min(size + 1, capacity);
        return true;
    }

    // removes the oldest added element(the front) and move the front position to the next one
    // returns the removed element
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
        if (isEmpty()) {
            // no roads = no open index and system start time reset.
            currentOpenIndex = -1;
            systemStartTime = -1;
        }
        return data;
    }

    public Road peek() {
        if (isEmpty()) {
            return null;
        }
        return queue[front];
    }

    // iterates over the array in the correct queue order and display values
    public synchronized void display() {
        int i = front;
        for (int count = 0; count < size; count++) {
            long secondsLeft = queue[i].getTimeTillNextSwitch() - System.currentTimeMillis() / 1000;
            if (queue[i].isOpen()) {
                // logger.info(Color.ANSI_YELLOW + "Status of:" + queue[i].getName() + " is " + queue[rear].isOpen());
                System.out.println(Color.ANSI_GREEN + queue[i].getName() + " will be open for " + secondsLeft + "s." + Color.ANSI_RESET);
            } else {
                // logger.info(Color.ANSI_YELLOW + "Status of:" + queue[i].getName() + " is " + queue[rear].isOpen());
                System.out.println(Color.ANSI_RED + queue[i].getName() + " will be closed for " + secondsLeft + "s." + Color.ANSI_RESET);
            }
            i = (i + 1) % capacity; // move to the next index in the correct order
        }
    }

    /**
     * Updates the status and the seconds till the next switch of the roads in the Q accordingly
     */
    public synchronized void updateRoadStatuses() {
        long elapsedSinceStart = System.currentTimeMillis() - systemStartTime;
        long timeTillNextSwitch = (System.currentTimeMillis() / 1000) + interval;

        // first update that happens before the first interval, happens only once.
        if (!firstUpdatePassed && systemStartTime > 0 && !isEmpty()) {
            logger.info(Color.ANSI_YELLOW + "Performing first update " + elapsedSinceStart / 1000);

            int i = currentOpenIndex;
            for (int count = 0; count < size; count++) {
                logger.info(Color.ANSI_YELLOW + "Updating road statuses via loop for index " + i);
                if (count == 0) { // set the time of the first road
                    queue[currentOpenIndex].setTimeTillNextSwitch(timeTillNextSwitch);
                    logger.info(Color.ANSI_YELLOW + "Updated the currently open road" + i + " aka " + queue[i].getName() + " with status " + queue[i].isOpen());
                } else { // handle the rest of the roads
                    queue[i].setTimeTillNextSwitch(timeTillNextSwitch + interval * (count - 1));
                    logger.info(Color.ANSI_YELLOW + "Updated index " + i + " aka " + queue[i].getName() + " to status " + queue[i].isOpen());
                }
                i = (i + 1) % size; // Move to the next index in the correct order
            }
            firstUpdatePassed = true;
            lastRoadUpdateTime = System.currentTimeMillis();
        }


        // Do nothing until at least one interval has passed or system hasn't started
        if (elapsedSinceStart < interval * 1000 || systemStartTime < 0) {
            logger.info(Color.ANSI_YELLOW + "First interval hasn't passed yet");
            return;
        }

        //Not enough time has passed for update
        //dividing by 1000 before subtracting as otherwise small millisecond differences mess up the timing.
        if (lastRoadUpdateTime < 0 || (System.currentTimeMillis() / 1000 - lastRoadUpdateTime / 1000) < interval) {
            logger.info(Color.ANSI_YELLOW + "Not enough time has passed for update" +
                        "\nLast road update time value: " + lastRoadUpdateTime +
                        "\nCurrent time value: " + System.currentTimeMillis() +
                        "\n current/1000 - last/1000: " + ((System.currentTimeMillis() / 1000) - (lastRoadUpdateTime / 1000)) +
                        "\nInterval: " + interval);
            return;
        }

        // regular updates
        if (peek() != null) {
            logger.info(Color.ANSI_YELLOW + "Updating road statuses...");

            if (size == 1) { // if the queue has only a single road, keep it open at all times.
                queue[front].switchStatus(timeTillNextSwitch);
                queue[front].setOpen(true);
            } else {
                // switch status of CURRENTLY OPEN road to off and move to the next road in Q
                queue[currentOpenIndex].switchStatus(timeTillNextSwitch);
                currentOpenIndex = (currentOpenIndex + 1) % size;

                // handle the rest of the roads
                int i = currentOpenIndex;
                for (int count = 0; count < size; count++) {
                    logger.info(Color.ANSI_YELLOW + "Updating road statuses via loop for index " + i);
                    if (count == 0) {
                        queue[i].switchStatus(timeTillNextSwitch); //switch the status and set the time for the NEW/NEXT open road
                    } else { // handle the rest of the roads
                        queue[i].setTimeTillNextSwitch(timeTillNextSwitch + interval * (count - 1));
                    }
                    logger.info(Color.ANSI_YELLOW + "Updated index " + i + " aka " + queue[i].getName() + " to status " + queue[i].isOpen());
                    i = (i + 1) % size; // Move to the next index in the correct order
                }
            }
            lastRoadUpdateTime = System.currentTimeMillis();
            logger.info("Updating lastSwitchTime: " + lastRoadUpdateTime);
        }
    }
}

/**
 * Implements {@link Runnable} and is responsible to track time elapsed since it was started.
 * It's also responsible to update the road statuses and print system/road information when {@link #printInfo} is set to true
 */
class RoadTrafficTimer implements Runnable {
    private volatile boolean running = true;
    private boolean printInfo = false;
    private final int numberOfRoads, interval;
    private CircularRoadQueue roadsQueue;
    private final long startTime;

    public void setPrintInfo(boolean printInfo) {
        this.printInfo = printInfo;
    }

    public void setRoadsQueue(CircularRoadQueue roadsQueue) {
        this.roadsQueue = roadsQueue;
    }

    public RoadTrafficTimer(int numberOfRoads, int interval, long startTime) {
        this.numberOfRoads = numberOfRoads;
        this.interval = interval;
        this.startTime = startTime;
    }

    @Override
    public void run() {
        if (!running) return; // Stop execution if the thread is stopped

        long secondsElapsed = (System.currentTimeMillis() - startTime) / 1000;


        if (printInfo) {
            System.out.printf("""
                    ! %ds. have passed since system startup !
                    ! Number of roads: %d !
                    ! Interval: %d !
                    
                    """, secondsElapsed, numberOfRoads, interval);
            synchronized (this) {
                roadsQueue.updateRoadStatuses();
                roadsQueue.display();
            }
            System.out.println("""
                    
                    ! Press "Enter" to open menu !""");
        }
    }

    public void stopThread() {
        running = false;
    }
}
