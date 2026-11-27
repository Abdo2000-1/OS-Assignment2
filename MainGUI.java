import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/** Minimal discussion-friendly GUI. The scheduling algorithms remain unchanged. */
public class MainGUI extends Application {
    private static final String BG = "#0B1120", PANEL = "#1E293B";
    private static final double UNIT = 52;

    private final Map<String, CaseData> cases = new LinkedHashMap<>();
    private final ComboBox<String> caseBox = new ComboBox<>();
    private final TextField quantum = new TextField();
    private final TextField context = new TextField();
    private final TextArea loaded = area(4);
    private final TextArea result = area(8);
    private final TextArea log = area(10);
    private final Label status = new Label();
    private final Label comparison = new Label("SJF: —    ACS: —");
    private final HBox sjfBlocks = new HBox(4), acsBlocks = new HBox(4);
    private final HBox sjfAxis = new HBox(), acsAxis = new HBox();
    private List<Process> processes = new ArrayList<>();
    private Run sjf, acs;

    @Override
    public void start(Stage stage) {
        makeCases();
        caseBox.getItems().setAll(cases.keySet());
        caseBox.setValue(caseBox.getItems().get(0));
        caseBox.setMaxWidth(Double.MAX_VALUE);
        caseBox.setOnAction(e -> loadCase());

        Button load = button("Load Example");
        load.setOnAction(e -> loadCase());
        Button runSjf = button("Run Preemptive SJF");
        runSjf.setOnAction(e -> run("SJF"));
        Button runAcs = button("Run ACS");
        runAcs.setOnAction(e -> run("ACS"));

        HBox settings = new HBox(8, text("Quantum"), quantum,
                text("Context switch"), context);
        HBox.setHgrow(quantum, Priority.ALWAYS);
        HBox.setHgrow(context, Priority.ALWAYS);
        HBox actions = new HBox(8, runSjf, runAcs);
        HBox.setHgrow(runSjf, Priority.ALWAYS);
        HBox.setHgrow(runAcs, Priority.ALWAYS);

        VBox input = panel("Ready-made examples", caseBox, load,
                text("Loaded processes"), loaded, settings, actions);
        VBox output = panel("Simulation output", status, comparison,
                text("Process results"), result, text("Execution log"), log,
                text("SJF Gantt Chart"), chart(sjfBlocks, sjfAxis),
                text("ACS Gantt Chart"), chart(acsBlocks, acsAxis));

        VBox pageContent = new VBox(10, title(), input, output);
        pageContent.setFillWidth(true);
        ScrollPane page = new ScrollPane(pageContent);
        page.setFitToWidth(true);
        page.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        page.setStyle("-fx-background: " + BG + "; -fx-background-color: " + BG + ";");

        BorderPane root = new BorderPane(page);
        root.setStyle("-fx-background-color: " + BG + ";");
        stage.setTitle("CPU Scheduler - MainGUI");
        stage.setScene(new Scene(root, 1000, 760));
        stage.setMinWidth(650);
        stage.setMinHeight(480);
        loadCase();
        stage.show();
    }

    private VBox title() {
        Label title = new Label("CPU Scheduling Simulator");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");
        Label sub = new Label("One file | Preemptive SJF | ACS with aging");
        sub.setStyle("-fx-text-fill: #94A3B8;");
        VBox box = new VBox(4, title, sub);
        box.setPadding(new Insets(18));
        return box;
    }

    private VBox panel(String heading, Object... children) {
        VBox box = new VBox(8);
        Label h = text(heading);
        h.setStyle("-fx-text-fill: #C4B5FD; -fx-font-size: 16px; -fx-font-weight: bold;");
        box.getChildren().add(h);
        for (Object child : children) box.getChildren().add((javafx.scene.Node) child);
        box.setPadding(new Insets(14));
        box.setStyle("-fx-background-color: " + PANEL + ";");
        VBox.setVgrow(box, Priority.NEVER);
        return box;
    }

    private ScrollPane chart(HBox blocks, HBox axis) {
        VBox timeline = new VBox(2, blocks, axis);
        timeline.setPadding(new Insets(6));
        timeline.setStyle("-fx-background-color: #111827;");
        ScrollPane pane = new ScrollPane(timeline);
        pane.setPrefHeight(135);
        pane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        pane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        return pane;
    }

    private void makeCases() {
        cases.put("Classic SRTF (textbook)", data(3, 1,
                p("P1", 0, 8, 2), p("P2", 1, 4, 1), p("P3", 2, 2, 3)));
        cases.put("Classic SJF (four processes)", data(3, 1,
                p("P1", 0, 6, 2), p("P2", 1, 8, 1), p("P3", 2, 7, 3), p("P4", 3, 3, 2)));
        cases.put("ACS Round Robin (four processes)", data(2, 1,
                p("P1", 0, 5, 2), p("P2", 0, 4, 1), p("P3", 1, 6, 3), p("P4", 2, 2, 2)));
        cases.put("Idle + preemption (assignment)", data(2, 1,
                p("P1", 0, 1, 3), p("P2", 5, 8, 3), p("P3", 6, 2, 1), p("P4", 8, 1, 2)));
    }

    private void loadCase() {
        CaseData c = cases.get(caseBox.getValue());
        if (c == null) return;
        processes = copy(c.processes);
        quantum.setText(String.valueOf(c.quantum));
        context.setText(String.valueOf(c.contextSwitch));
        loaded.setText(processText(processes));
        clearOutput();
        status.setText("Loaded: " + caseBox.getValue());
    }

    private void run(String algorithm) {
        try {
            int q = Integer.parseInt(quantum.getText().trim());
            int cs = Integer.parseInt(context.getText().trim());
            if (q <= 0 || cs < 0) throw new IllegalArgumentException();
            if (algorithm.equals("SJF")) {
                SJFScheduler scheduler = new SJFScheduler(processes, cs);
                scheduler.simulate();
                sjf = new Run("SJF", scheduler.getProcesses(), scheduler.getExecutionLog());
                draw(sjfBlocks, sjfAxis, sjf);
                show(sjf);
            } else {
                ACSScheduler scheduler = new ACSScheduler(processes, q, cs);
                scheduler.simulate();
                acs = new Run("ACS", scheduler.getProcesses(), scheduler.getExecutionLog());
                draw(acsBlocks, acsAxis, acs);
                show(acs);
            }
            comparison.setText("SJF: " + summary(sjf) + "    ACS: " + summary(acs));
        } catch (IllegalArgumentException e) {
            status.setText("Use a positive quantum and a non-negative context switch.");
        }
    }

    private void show(Run run) {
        result.setText(run.results());
        log.setText(String.join("\n", run.log));
        status.setText(run.name + " completed.");
    }

    private void draw(HBox blocks, HBox axis, Run run) {
        blocks.getChildren().clear();
        axis.getChildren().clear();
        Map<String, Integer> arrival = new LinkedHashMap<>();
        for (Process p : run.processes) arrival.put(p.name, p.arrivalTime);
        List<Segment> all = segments(run.log);
        for (Segment s : all) {
            String name = arrival.containsKey(s.name) ? s.name + " (A=" + arrival.get(s.name) + ")" : s.name;
            Label block = new Label(name + "\n" + s.start + " → " + s.end);
            block.setAlignment(Pos.CENTER);
            block.setWrapText(true);
            block.setTextFill(javafx.scene.paint.Color.WHITE);
            block.setStyle("-fx-background-color: " + color(s.name)
                    + "; -fx-background-radius: 5; -fx-font-size: 11px; -fx-padding: 7 3;");
            block.setMinWidth(Math.max(UNIT, s.duration() * UNIT));
            blocks.getChildren().add(block);
        }
        int end = all.isEmpty() ? 0 : all.get(all.size() - 1).end;
        for (int i = 0; i <= end; i++) {
            Label tick = new Label(String.valueOf(i));
            tick.setMinWidth(UNIT);
            tick.setStyle("-fx-text-fill: #94A3B8; -fx-border-color: #334155; -fx-border-width: 1 0 0 0;");
            axis.getChildren().add(tick);
        }
    }

    private List<Segment> segments(List<String> lines) {
        List<Segment> out = new ArrayList<>();
        Pattern sjf = Pattern.compile("^Time\\s+(\\d+)\\s*:\\s*(.*)$");
        Pattern acs = Pattern.compile("^Time\\s+(\\d+)\\s*\\|\\s*([^|]+)\\|.*$");
        for (String line : lines) {
            Matcher a = sjf.matcher(line), b = acs.matcher(line);
            int time; String name;
            if (a.matches()) {
                time = Integer.parseInt(a.group(1)); name = a.group(2).trim();
                if (name.startsWith("Running ")) {
                    name = name.substring(8);
                    int end = name.indexOf(" (Remaining:");
                    if (end >= 0) name = name.substring(0, end);
                } else if (name.startsWith("[Context Switch]")) name = "Context Switch";
                else if (!name.equals("CPU Idle")) continue;
            } else if (b.matches()) {
                time = Integer.parseInt(b.group(1)); name = b.group(2).trim();
                if (name.equals("Done")) continue;
            } else continue;
            if (!out.isEmpty() && out.get(out.size() - 1).name.equals(name)
                    && out.get(out.size() - 1).end == time) out.get(out.size() - 1).end++;
            else out.add(new Segment(name, time, time + 1));
        }
        return out;
    }

    private void clearOutput() {
        sjf = acs = null;
        result.clear(); log.clear(); comparison.setText("SJF: —    ACS: —");
        sjfBlocks.getChildren().clear(); acsBlocks.getChildren().clear();
        sjfAxis.getChildren().clear(); acsAxis.getChildren().clear();
    }

    private static String processText(List<Process> list) {
        StringBuilder b = new StringBuilder();
        for (Process p : list) b.append(p.name).append("  arrival=").append(p.arrivalTime)
                .append("  burst=").append(p.burstTime).append("  priority=").append(p.priority).append('\n');
        return b.toString();
    }

    private static String summary(Run r) { return r == null ? "not run" : String.format("W %.2f / T %.2f / R %.2f", r.wait, r.turn, r.response); }
    private static List<Process> copy(List<Process> list) { List<Process> c = new ArrayList<>(); for (Process p : list) c.add(p.copy()); return c; }
    private static Process p(String n, int a, int b, int pr) { return new Process(n, a, b, pr); }
    private static CaseData data(int q, int cs, Process... p) { return new CaseData(q, cs, List.of(p)); }
    private static Label text(String value) { Label l = new Label(value); l.setStyle("-fx-text-fill: #CBD5E1;"); return l; }
    private static TextArea area(int rows) { TextArea a = new TextArea(); a.setEditable(false); a.setWrapText(false); a.setPrefRowCount(rows); a.setStyle("-fx-control-inner-background: #111827; -fx-text-fill: #F8FAFC;"); return a; }
    private static Button button(String value) { Button b = new Button(value); b.setMaxWidth(Double.MAX_VALUE); b.setStyle("-fx-background-color: #8B5CF6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 9;"); return b; }
    private static String color(String n) { if (n.equals("CPU Idle")) return "#475569"; if (n.equals("Context Switch")) return "#B45309"; return new String[]{"#0F766E", "#2563EB", "#7C3AED", "#15803D"}[Math.floorMod(n.hashCode(), 4)]; }

    private static class CaseData { int quantum, contextSwitch; List<Process> processes; CaseData(int q, int cs, List<Process> p) { quantum=q; contextSwitch=cs; processes=p; } }
    private static class Segment { String name; int start, end; Segment(String n, int s, int e) { name=n; start=s; end=e; } int duration() { return end-start; } }
    private static class Run {
        String name; List<Process> processes; List<String> log; double wait, turn, response;
        Run(String n, List<Process> p, List<String> l) { name=n; processes=p; log=l; for (Process x:p) { wait+=x.waitingTime; turn+=x.turnaroundTime; response+=x.responseTime; } int size=p.size(); wait/=size; turn/=size; response/=size; }
        String results() { StringBuilder b=new StringBuilder(String.format("Average: W %.2f | T %.2f | R %.2f%n%n",wait,turn,response)); for(Process p:processes) b.append(String.format("%s: finish=%d, wait=%d, turn=%d, response=%d%n",p.name,p.finishTime,p.waitingTime,p.turnaroundTime,p.responseTime)); return b.toString(); }
    }
}
