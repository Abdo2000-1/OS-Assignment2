import java.util.ArrayList;
import java.util.List;

public class AgingDemo {
    public static void main(String[] args) {
        AgingManager aging = new AgingManager(); 

        Process p1 = new Process("P1", 0, 5, 4);
        Process p2 = new Process("P2", 1, 6, 2);
        Process p3 = new Process("P3", 4, 4, 3);
        List<Process> tier2 = new ArrayList<>();

        p1.remainingTime = 2; tier2.add(p1); aging.enterTier2(p1, 3);
        p2.remainingTime = 3; tier2.add(p2); aging.enterTier2(p2, 7);
        p3.remainingTime = 1; tier2.add(p3); aging.enterTier2(p3, 11);

        decide(aging, tier2, 11, "P2"); // expected: P2 (priority 1)
        decide(aging, tier2, 15, "P1"); // expected: P1 (priority 1), P3 priority 2
        decide(aging, tier2, 18, "P3"); // expected: P3
    }

    private static void decide(AgingManager aging, List<Process> tier2, int t, String expected) {
        System.out.println("--- Decision at T=" + t + " ---");
        for (String line : aging.describeAging(tier2, t)) System.out.println(line);
        System.out.println(aging.formatTier2(tier2, t));
        Process next = aging.selectNext(tier2, t);
        System.out.println("Selected: " + next.name + "  (expected " + expected + ")  "
                + (next.name.equals(expected) ? "OK" : "MISMATCH"));
        tier2.remove(next);
        aging.leaveTier2(next);
        System.out.println();
    }
}