# CS241 Assignment 2 — CPU Scheduling

This repository contains the console simulator, the integrated JavaFX GUI,
the assignment report, and the generated graphs for CS241 Assignment 2.

## Project layout

```text
OS-Assignment2/
├── *.java                         # console simulator, schedulers, and MainGUI
├── pom.xml                        # JavaFX/Maven build
├── mvnw.cmd                       # Windows Maven launcher
├── .vscode/                       # VS Code tasks and launch configurations
├── CS241_Assignment2_Report.pdf   # assignment report
└── graphs/                        # report graphs
```

Generated files (`target/`, `out/`, `.tools/`, and `*.class`) are ignored and
should not be committed.

## Implemented schedulers

- **Preemptive Shortest-Job First (SJF/SRTF):** runs the ready process with the
  shortest remaining time.
- **Adaptive Container Scheduler (ACS):** Tier 1 round robin, Tier 2
  non-preemptive priority scheduling, and aging to reduce starvation.

## Requirements

- JDK 27 (the Maven build uses Java release 27)
- Windows for `mvnw.cmd`
- VS Code Java Extension Pack is recommended for debugging

## Run the console simulator

From the repository root:

```powershell
javac -d out Process.java AgingManager.java SJFScheduler.java ACSScheduler.java Main.java AgingDemo.java TestCases.java
java -cp out Main
```

`Main` runs four workloads and prints each SJF/ACS execution timeline and the
waiting, turnaround, and response-time metrics. Run the aging demonstration with:

```powershell
java -cp out AgingDemo
```

## Run the JavaFX GUI

```powershell
.\mvnw.cmd javafx:run
```

The wrapper downloads Maven into `.tools/` when needed and handles the Arabic
parent path automatically. In VS Code, open the repository root and run:

- `Run CPU Scheduler GUI`, or
- `Run console simulator`.

The debug configuration is `Debug CPU Scheduler GUI`.

The GUI is intentionally kept in one minimal `MainGUI.java` file. It provides
four ready-made examples, SJF/ACS controls, execution logs, metrics, responsive
vertical scrolling, and two timeline-based Gantt charts with arrival labels.
The scheduler classes remain separate so the algorithm implementation is easy
to discuss and test.

## Test workloads

| Test case | Processes `(arrival, burst, priority)` | Quantum | Context switch |
| --- | --- | ---: | ---: |
| Test Case 1 — Official | P1 `(0,4,2)`, P2 `(1,7,3)`, P3 `(3,2,1)` | 3 | 0 |
| Test Case 2 — Official | P1 `(0,5,4)`, P2 `(1,6,2)`, P3 `(4,4,3)` | 3 | 1 |
| Test Case 3 — Mixed workload | P1 `(0,10,3)`, P2 `(1,2,1)`, P3 `(2,8,2)`, P4 `(3,3,4)`, P5 `(6,5,2)` | 2 | 1 |
| Test Case 4 — Idle and preemption | P1 `(0,1,3)`, P2 `(5,8,3)`, P3 `(6,2,1)`, P4 `(8,1,2)` | 2 | 1 |

## Metric definitions

- Turnaround time = finish time − arrival time
- Waiting time = turnaround time − burst time
- Response time = first CPU start time − arrival time

For ACS, lower numerical priority means higher priority. The effective
priority is reduced according to the aging interval implemented by
`AgingManager`.

## Report and graphs

The report contains scheduler descriptions, test-case definitions, per-process
and average metrics, context-switch/quantum analysis, ACS aging and demotion
discussion, and the following graphs:

- scheduler baseline comparison
- waiting/turnaround and CPU-utilization plots
- Q/CS heatmaps
- ACS turnaround and waiting heatmaps
