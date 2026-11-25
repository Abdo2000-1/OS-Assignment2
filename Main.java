import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<Process> testCase1 = new ArrayList<>();
        testCase1.add(new Process("P1", 0, 4, 2));
        testCase1.add(new Process("P2", 1, 7, 3));
        testCase1.add(new Process("P3", 3, 2, 1));

        System.out.println(">>> RUNNING TEST CASE 1 <<<");
        SJFScheduler sjf1 = new SJFScheduler(testCase1, 0);
        sjf1.simulate();
        sjf1.printResults();

        List<Process> testCase2 = new ArrayList<>();
        testCase2.add(new Process("P1", 0, 5, 4));
        testCase2.add(new Process("P2", 1, 6, 2));
        testCase2.add(new Process("P3", 4, 4, 3));

        System.out.println(">>> RUNNING TEST CASE 2 <<<");
        SJFScheduler sjf2 = new SJFScheduler(testCase2, 1);
        sjf2.simulate();
        sjf2.printResults();
    }
}