import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class AgingManager {

    public static final int DEFAULT_AGING_INTERVAL = 4;

    private final int agingInterval;
    private final Map<Process, Integer> tier2EntryTime = new IdentityHashMap<>();

    public AgingManager() {
        this(DEFAULT_AGING_INTERVAL);
    }

    public AgingManager(int agingInterval) {
        if (agingInterval <= 0) {
            throw new IllegalArgumentException("agingInterval must be > 0");
        }
        this.agingInterval = agingInterval;
    }

    public void enterTier2(Process p, int currentTime) {
        tier2EntryTime.put(p, currentTime);
    }

    public void leaveTier2(Process p) {
        tier2EntryTime.remove(p);
    }

    public int waitedTime(Process p, int currentTime) {
        Integer entry = tier2EntryTime.get(p);
        if (entry == null) {
            throw new IllegalStateException(p.name + " is not registered in Tier 2");
        }
        return currentTime - entry;
    }

    public int agingBoost(Process p, int currentTime) {
        return waitedTime(p, currentTime) / agingInterval; 
    }

    public int effectivePriority(Process p, int currentTime) {
        return p.priority - agingBoost(p, currentTime);
    }

    public Process selectNext(List<Process> tier2, int currentTime) {
        if (tier2.isEmpty()) {
            return null;
        }
        List<Process> sorted = new ArrayList<>(tier2);
        sorted.sort(Comparator
                .comparingInt((Process p) -> effectivePriority(p, currentTime))
                .thenComparingInt(p -> tier2EntryTime.get(p))
                .thenComparingInt(p -> p.arrivalTime)
                .thenComparing(p -> p.name));
        return sorted.get(0);
    }

    public List<String> describeAging(List<Process> tier2, int currentTime) {
        List<String> lines = new ArrayList<>();
        for (Process p : tier2) {
            int entry = tier2EntryTime.get(p);
            int waited = currentTime - entry;
            int boost = waited / agingInterval;
            if (waited > 0) {
                lines.add(p.name + ": Waited from T=" + entry + " to T=" + currentTime
                        + " (" + waited + " units). Aging: floor(" + waited + "/" + agingInterval
                        + ") = " + boost + ". Effective Priority = " + p.priority + " - " + boost
                        + " = " + (p.priority - boost));
            } else {
                lines.add(p.name + ": Waited from T=" + entry + " to T=" + currentTime
                        + " (0 units). Effective Priority = " + p.priority);
            }
        }
        return lines;
    }

    public String formatTier2(List<Process> tier2, int currentTime) {
        if (tier2.isEmpty()) {
            return "Tier 2: [Empty]";
        }
        StringBuilder sb = new StringBuilder("Tier 2: [");
        for (int i = 0; i < tier2.size(); i++) {
            Process p = tier2.get(i);
            if (i > 0) sb.append(", ");
            sb.append(p.name).append("(remaining:").append(p.remainingTime)
              .append(", Priority:").append(effectivePriority(p, currentTime)).append(")");
        }
        return sb.append("]").toString();
    }
}