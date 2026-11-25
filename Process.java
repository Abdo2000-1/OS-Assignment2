public class Process {
    public String name;
    public int arrivalTime;
    public int burstTime;
    public int priority;
    public int remainingTime;
    public int firstRunTime = -1;
    public int finishTime;
    public int turnaroundTime;
    public int waitingTime;
    public int responseTime;

    public Process(String name, int arrivalTime, int burstTime, int priority) {
        this.name = name;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.remainingTime = burstTime;
        this.priority = priority;
    }

    public Process copy() {
        return new Process(this.name, this.arrivalTime, this.burstTime, this.priority);
    }
}