import java.util.ArrayList;
import java.util.List;

public class Main {
    static final String[] NAMES = {"Test Case 1", "Test Case 2", "Test Case 3"};
    static final int[] QUANTA = {3, 3, 2};
    static final int[] CS_TIMES = {0, 1, 1};

    public static List<List<Process>> buildTestCases() {
        List<List<Process>> cases = new ArrayList<>();

        // Test Case 1 (from the assignment): Q = 3, CS = 0
        List<Process> tc1 = new ArrayList<>();
        tc1.add(new Process("P1", 0, 4, 2));
        tc1.add(new Process("P2", 1, 7, 3));
        tc1.add(new Process("P3", 3, 2, 1));
        cases.add(tc1);

        // Test Case 2 (from the assignment): Q = 3, CS = 1
        List<Process> tc2 = new ArrayList<>();
        tc2.add(new Process("P1", 0, 5, 4));
        tc2.add(new Process("P2", 1, 6, 2));
        tc2.add(new Process("P3", 4, 4, 3));
        cases.add(tc2);

        // Test Case 3 (our own mixed workload): Q = 2, CS = 1
        List<Process> tc3 = new ArrayList<>();
        tc3.add(new Process("P1", 0, 10, 3));
        tc3.add(new Process("P2", 1, 2, 1));
        tc3.add(new Process("P3", 2, 8, 2));
        tc3.add(new Process("P4", 3, 3, 4));
        tc3.add(new Process("P5", 6, 5, 2));
        cases.add(tc3);

        return cases;
    }

    public static void main(String[] args) {
        List<List<Process>> cases = buildTestCases();

        for (int i = 0; i < cases.size(); i++) {
            System.out.println("##################################################");
            System.out.println(">>> " + NAMES[i] + "  (Q = " + QUANTA[i] + ", CS = " + CS_TIMES[i] + ") <<<");
            System.out.println("##################################################");

            SJFScheduler sjf = new SJFScheduler(cases.get(i), CS_TIMES[i]);
            sjf.simulate();
            sjf.printResults();

            ACSScheduler acs = new ACSScheduler(cases.get(i), QUANTA[i], CS_TIMES[i]);
            acs.simulate();
            acs.printResults();
        }
    }
}