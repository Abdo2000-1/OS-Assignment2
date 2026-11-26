import java.util.ArrayList;
import java.util.List;

public class ACSScheduler {
    private List<Process> processes;
    private List<Process> incoming;
    private List<Process> tier1;
    private List<Process> tier2;
    private int quantum;
    private int contextSwitchTime;
    private int currentTime;
    private AgingManager aging;
    private List<String> executionLog;

    public ACSScheduler(List<Process> inputProcesses, int quantum, int contextSwitchTime) {
        this.processes = new ArrayList<>();
        for (Process p : inputProcesses) {
            this.processes.add(p.copy());
        }
        this.incoming = new ArrayList<>(processes);
        this.tier1 = new ArrayList<>();
        this.tier2 = new ArrayList<>();
        this.quantum = quantum;
        this.contextSwitchTime = contextSwitchTime;
        this.currentTime = 0;
        this.aging = new AgingManager();
        this.executionLog = new ArrayList<>();
    }

    public void simulate() {
        int completedCount = 0;
        int n = processes.size();
        Process lastExecutedProcess = null;

        addArrivals();

        while (completedCount < n) {
            Process next = null;
            boolean fromTier1 = false;

            if (!tier1.isEmpty()) {
                next = tier1.remove(0);
                fromTier1 = true;
            } else if (!tier2.isEmpty()) {
                for (String line : aging.describeAging(tier2, currentTime)) {
                    executionLog.add("        " + line);
                }
                next = aging.selectNext(tier2, currentTime);
                tier2.remove(next);
                aging.leaveTier2(next);
                executionLog.add("        " + next.name + " selected from Tier 2 (runs until it finishes)");
            }

            if (next == null) {
                logStep("CPU Idle");
                currentTime++;
                addArrivals();
                continue;
            }

            if (lastExecutedProcess != null && lastExecutedProcess != next && contextSwitchTime > 0) {
                if (lastExecutedProcess.remainingTime > 0) {
                    executionLog.add("        Context switch saves " + lastExecutedProcess.name + " and loads " + next.name);
                } else {
                    executionLog.add("        Context switch loads " + next.name);
                }
                for (int i = 0; i < contextSwitchTime; i++) {
                    logStep("Context Switch");
                    currentTime++;
                    addArrivals();
                }
            }

            if (next.firstRunTime == -1) {
                next.firstRunTime = currentTime;
            }

            int runTime;
            if (fromTier1) {
                runTime = Math.min(quantum, next.remainingTime);
            } else {
                runTime = next.remainingTime;
            }

            for (int i = 0; i < runTime; i++) {
                logStep(next.name);
                next.remainingTime--;
                currentTime++;
                addArrivals();
            }
            lastExecutedProcess = next;

            if (next.remainingTime == 0) {
                next.finishTime = currentTime;
                completedCount++;
                executionLog.add("        " + next.name + " finished at T=" + currentTime);
            } else {
                tier2.add(next);
                aging.enterTier2(next, currentTime);
                executionLog.add("        " + next.name + " used its full quantum -> demoted to Tier 2 at T=" + currentTime);
            }
        }

        logStep("Done");
        calculateMetrics();
    }

    private void addArrivals() {
        int i = 0;
        while (i < incoming.size()) {
            Process p = incoming.get(i);
            if (p.arrivalTime <= currentTime) {
                tier1.add(p);
                incoming.remove(i);
            } else {
                i++;
            }
        }
    }

    private void logStep(String running) {
        executionLog.add(String.format("Time %-3d | %-14s | %s | %s",
                currentTime, running, formatTier1(), aging.formatTier2(tier2, currentTime)));
    }

    private String formatTier1() {
        if (tier1.isEmpty()) {
            return "Tier 1: [Empty]";
        }
        String s = "Tier 1: [";
        for (int i = 0; i < tier1.size(); i++) {
            Process p = tier1.get(i);
            if (i > 0) {
                s += ", ";
            }
            s += p.name + "(remaining:" + p.remainingTime + ", Priority:" + p.priority + ")";
        }
        return s + "]";
    }

    private void calculateMetrics() {
        for (Process p : processes) {
            p.turnaroundTime = p.finishTime - p.arrivalTime;
            p.waitingTime = p.turnaroundTime - p.burstTime;
            p.responseTime = p.firstRunTime - p.arrivalTime;
        }
    }

    public void printResults() {
        System.out.println("=========================================");
        System.out.println("  ACS Execution Log (Q = " + quantum + ", CS = " + contextSwitchTime + ")");
        System.out.println("=========================================");
        for (String log : executionLog) {
            System.out.println(log);
        }

        System.out.println("\n-------------------------------------------------------------------------");
        System.out.printf("%-10s | %-8s | %-6s | %-10s | %-8s | %-8s\n",
                "Process", "Arrival", "Finish", "Turnaround", "Waiting", "Response");
        System.out.println("-------------------------------------------------------------------------");

        double totalTAT = 0;
        double totalWT = 0;
        double totalRT = 0;

        for (Process p : processes) {
            System.out.printf("%-10s | %-8d | %-6d | %-10d | %-8d | %-8d\n",
                    p.name, p.arrivalTime, p.finishTime, p.turnaroundTime, p.waitingTime, p.responseTime);
            totalTAT += p.turnaroundTime;
            totalWT += p.waitingTime;
            totalRT += p.responseTime;
        }

        int n = processes.size();
        System.out.println("-------------------------------------------------------------------------");
        System.out.printf("Average Turnaround Time: %.2f\n", (totalTAT / n));
        System.out.printf("Average Waiting Time:    %.2f\n", (totalWT / n));
        System.out.printf("Average Response Time:   %.2f\n", (totalRT / n));
        System.out.println("=========================================\n");
    }

    public List<Process> getProcesses() { return processes; }
    public List<String> getExecutionLog() { return executionLog; }
}
