package traffic;

/*
Stage 1/6 Completed
Traffic Light Simulator with Java - https://hyperskill.org/projects/288/stages/1500/implement
Part of Hyperskill's Java Backend Developer (Spring Boot) course.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("Welcome to the traffic management system!");
        printMenu();
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
