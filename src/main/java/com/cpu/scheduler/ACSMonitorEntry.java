package com.cpu.scheduler;

public class ACSMonitorEntry {
    public int timeStep;
    public String processName;
    public String currentTier;
    public int remainingBurst;
    public int originalPriority;
    public int agingBoost;
    public int effectivePriority;
    public String demotionStatus;
    public String runningProcess;
    public String tier1QueueState;
    public String tier2QueueState;

    public ACSMonitorEntry(
        int timeStep,
        String processName,
        String currentTier,
        int remainingBurst,
        int originalPriority,
        int agingBoost,
        int effectivePriority,
        String demotionStatus,
        String runningProcess,
        String tier1QueueState,
        String tier2QueueState
    ) {
        this.timeStep = timeStep;
        this.processName = processName;
        this.currentTier = currentTier;
        this.remainingBurst = remainingBurst;
        this.originalPriority = originalPriority;
        this.agingBoost = agingBoost;
        this.effectivePriority = effectivePriority;
        this.demotionStatus = demotionStatus;
        this.runningProcess = runningProcess;
        this.tier1QueueState = tier1QueueState;
        this.tier2QueueState = tier2QueueState;
    }
}