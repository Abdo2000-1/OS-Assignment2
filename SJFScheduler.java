import java.util.ArrayList;
import java.util.List;

public class SJFScheduler {
    private List<Process> processes;
    private int contextSwitchTime;
    private List<String> executionLog;

    public SJFScheduler(List<Process> inputProcesses, int contextSwitchTime) {
        this.processes = new ArrayList<>();
        for (Process p : inputProcesses) {
            this.processes.add(p.copy());
        }
        this.contextSwitchTime = contextSwitchTime;
        this.executionLog = new ArrayList<>();
    }

    public void simulate() {
        int currentTime = 0;
        int completedCount = 0;
        int n = processes.size();
        Process lastExecutedProcess = null;

        while (completedCount < n) {
            Process shortest = getShortestRemainingProcess(currentTime);

            if (shortest == null) {
                executionLog.add("Time " + currentTime + ": CPU Idle");
                currentTime++;
                continue;
            }

            if (lastExecutedProcess != null && lastExecutedProcess != shortest && contextSwitchTime > 0) {
                for (int i = 0; i < contextSwitchTime; i++) {
                    executionLog.add("Time " + currentTime + ": [Context Switch] switching to " + shortest.name);
                    currentTime++;
                }
                shortest = getShortestRemainingProcess(currentTime);
            }

            if (shortest.firstRunTime == -1) {
                shortest.firstRunTime = currentTime;
            }

            executionLog.add("Time " + currentTime + ": Running " + shortest.name + " (Remaining: " + (shortest.remainingTime - 1) + ")");
            shortest.remainingTime--;
            lastExecutedProcess = shortest;
            currentTime++;

            if (shortest.remainingTime == 0) {
                shortest.finishTime = currentTime;
                completedCount++;
            }
        }

        calculateMetrics();
    }

    private Process getShortestRemainingProcess(int currentTime) {
        Process shortest = null;
        for (Process p : processes) {
            if (p.arrivalTime <= currentTime && p.remainingTime > 0) {
                if (shortest == null || p.remainingTime < shortest.remainingTime) {
                    shortest = p;
                } else if (p.remainingTime == shortest.remainingTime) {
                    if (p.arrivalTime < shortest.arrivalTime) {
                        shortest = p;
                    }
                }
            }
        }
        return shortest;
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
        System.out.println("  Preemptive SJF Execution Log (CS = " + contextSwitchTime + ")");
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