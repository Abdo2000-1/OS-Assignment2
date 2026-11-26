import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Standalone workload definitions for the assignment test cases.
 * This file does not modify or replace the original simulator entry point.
 */
public final class TestCases {
    private TestCases() {
    }

    public static List<Workload> all() {
        List<Workload> workloads = new ArrayList<>();

        workloads.add(new Workload(
                "Test Case 1 - Official", 3, 0,
                new Process("P1", 0, 4, 2),
                new Process("P2", 1, 7, 3),
                new Process("P3", 3, 2, 1)));

        workloads.add(new Workload(
                "Test Case 2 - Official", 3, 1,
                new Process("P1", 0, 5, 4),
                new Process("P2", 1, 6, 2),
                new Process("P3", 4, 4, 3)));

        workloads.add(new Workload(
                "Test Case 3 - Mixed Workload", 2, 1,
                new Process("P1", 0, 10, 3),
                new Process("P2", 1, 2, 1),
                new Process("P3", 2, 8, 2),
                new Process("P4", 3, 3, 4),
                new Process("P5", 6, 5, 2)));

        workloads.add(new Workload(
                "Test Case 4 - Idle and Preemption", 2, 1,
                new Process("P1", 0, 1, 3),
                new Process("P2", 5, 8, 3),
                new Process("P3", 6, 2, 1),
                new Process("P4", 8, 1, 2)));

        return Collections.unmodifiableList(workloads);
    }

    public static final class Workload {
        public final String name;
        public final int defaultQuantum;
        public final int defaultContextSwitch;
        private final List<Process> processes;

        private Workload(String name, int defaultQuantum, int defaultContextSwitch, Process... processes) {
            this.name = name;
            this.defaultQuantum = defaultQuantum;
            this.defaultContextSwitch = defaultContextSwitch;
            this.processes = Collections.unmodifiableList(Arrays.asList(processes));
        }

        public List<Process> newProcesses() {
            List<Process> copies = new ArrayList<>();
            for (Process process : processes) {
                copies.add(process.copy());
            }
            return copies;
        }

        public List<Process> definition() {
            return processes;
        }
    }
}
