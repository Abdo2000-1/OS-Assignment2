package com.cpu.scheduler;

import java.util.ArrayList;
import java.util.List;

public class SimulationResult {

    private final String algorithm;
    private final List<Process> processes;
    private final double averageWaiting;
    private final double averageTurnaround;
    private final double averageResponse;
    private final int quantum;
    private final int contextSwitch;

    public SimulationResult(
        String algorithm,
        List<Process> processes,
        int quantum,
        int contextSwitch
    ) {
        this.algorithm = algorithm;
        this.processes = new ArrayList<>();

        double totalWaiting = 0;
        double totalTurnaround = 0;
        double totalResponse = 0;

        for (Process process : processes) {
            Process copy = new Process(
                process.name,
                process.arrivalTime,
                process.burstTime,
                process.priority
            );

            copy.remainingTime = process.remainingTime;
            copy.firstRunTime = process.firstRunTime;
            copy.finishTime = process.finishTime;
            copy.turnaroundTime = process.turnaroundTime;
            copy.waitingTime = process.waitingTime;
            copy.responseTime = process.responseTime;

            this.processes.add(copy);

            totalWaiting += process.waitingTime;
            totalTurnaround += process.turnaroundTime;
            totalResponse += process.responseTime;
        }

        int count = processes.size();

        this.averageWaiting = count == 0 ? 0 : totalWaiting / count;
        this.averageTurnaround = count == 0 ? 0 : totalTurnaround / count;
        this.averageResponse = count == 0 ? 0 : totalResponse / count;
        this.quantum = quantum;
        this.contextSwitch = contextSwitch;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public List<Process> getProcesses() {
        return new ArrayList<>(processes);
    }

    public double getAverageWaiting() {
        return averageWaiting;
    }

    public double getAverageTurnaround() {
        return averageTurnaround;
    }

    public double getAverageResponse() {
        return averageResponse;
    }

    public int getQuantum() {
        return quantum;
    }

    public int getContextSwitch() {
        return contextSwitch;
    }
}