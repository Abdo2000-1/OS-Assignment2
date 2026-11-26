# CS241 Assignment 2 - Cloud-Cluster CPU Scheduling

This repository contains a Java simulation for CPU scheduling in a node running mixed containerized workloads.

## Implemented schedulers

- **Preemptive Shortest-Job First (SJF/SRTF):** runs the ready process with the shortest remaining time.
- **Adaptive Container Scheduler (ACS):**
  - **Tier 1:** Round Robin with a fixed quantum.
  - **Tier 2:** Non-preemptive priority scheduling for processes that consume their full quantum.
  - **Tier 3:** Aging for processes waiting in Tier 2 to reduce starvation.

## Files

| File | Description |
| --- | --- |
| `Process.java` | Process data model. |
| `SJFScheduler.java` | Preemptive SJF implementation. |
| `ACSScheduler.java` | ACS implementation with Tier 1 and Tier 2 queues. |
| `AgingManager.java` | Aging and effective-priority logic. |
| `AgingDemo.java` | Small standalone aging demonstration. |
| `Main.java` | Runs the original assignment test cases and prints the schedules and metrics. |
| `TestCases.java` | Standalone definitions for the official and generated workloads. |
| `report/CS241_Assignment2_Report.pdf` | Assignment report. |
| `report/graphs/` | Generated waiting-time, turnaround-time, CPU-utilization, comparison, and heatmap graphs. |

## Test cases

The standalone `TestCases.java` file defines these workloads:

| Test case | Processes `(arrival, burst, priority)` | Default Q | Default CS |
| --- | --- | ---: | ---: |
| Test Case 1 - Official | P1 `(0,4,2)`, P2 `(1,7,3)`, P3 `(3,2,1)` | 3 | 0 |
| Test Case 2 - Official | P1 `(0,5,4)`, P2 `(1,6,2)`, P3 `(4,4,3)` | 3 | 1 |
| Test Case 3 - Mixed Workload | P1 `(0,10,3)`, P2 `(1,2,1)`, P3 `(2,8,2)`, P4 `(3,3,4)`, P5 `(6,5,2)` | 2 | 1 |
| Test Case 4 - Idle and Preemption | P1 `(0,1,3)`, P2 `(5,8,3)`, P3 `(6,2,1)`, P4 `(8,1,2)` | 2 | 1 |

## Build and run

From the repository directory:

```text
javac *.java
java Main
```

The program prints the execution timeline and the following metrics for each process:

- Waiting time
- Turnaround time
- Response time
- Average waiting, turnaround, and response time

To run the aging demonstration:

```text
java AgingDemo
```

## Metric definitions

- **Turnaround time** = finish time - arrival time
- **Waiting time** = turnaround time - burst time
- **Response time** = first CPU start time - arrival time

For ACS aging, lower numerical priority means higher priority. The effective priority is calculated using the waiting time and the aging interval described in the assignment.

## Report and graphs

The PDF report contains:

- Scheduler descriptions and implementation assumptions
- All test-case definitions
- Per-process and average scheduling metrics
- Context-switch and quantum analysis
- Graphs showing waiting time, turnaround time, CPU utilization, and Q/CS heatmaps
- ACS Tier 2 demotion trade-off discussion
- A sample per-time-step ACS demotion matrix

