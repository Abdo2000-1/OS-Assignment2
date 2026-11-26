package com.cpu.scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class GanttChartView extends VBox {

    private final List<String> sjfLog;
    private final List<String> acsLog;
    private final ComboBox<String> algorithmSelector = new ComboBox<>();
    private final HBox chart = new HBox(3);
    private final Label summary = new Label();

    private final String[] colors = {
        "#8B5CF6", "#2563EB", "#0891B2",
        "#059669", "#D97706", "#DB2777"
    };

    public GanttChartView(
        List<String> sjfLog,
        List<String> acsLog,
        String preferredAlgorithm
    ) {
        this.sjfLog = new ArrayList<>(sjfLog);
        this.acsLog = new ArrayList<>(acsLog);

        setSpacing(20);
        setPadding(new Insets(25));

        Label heading = new Label("CPU Gantt Chart");
        heading.setStyle(
            "-fx-text-fill: #F8FAFC;" +
            "-fx-font-size: 22px;" +
            "-fx-font-weight: bold;"
        );

        Label description = new Label(
            "Visual representation of CPU execution over time."
        );
        description.setStyle("-fx-text-fill: #94A3B8;");

        algorithmSelector.getItems().addAll(
            "Preemptive SJF",
            "ACS"
        );

        algorithmSelector.setStyle(
            "-fx-background-color: #1E293B;" +
            "-fx-text-fill: white;"
        );

        if (this.sjfLog.isEmpty()) {
            algorithmSelector.getItems().remove("Preemptive SJF");
        }

        if (this.acsLog.isEmpty()) {
            algorithmSelector.getItems().remove("ACS");
        }

        if (algorithmSelector.getItems().contains(preferredAlgorithm)) {
            algorithmSelector.setValue(preferredAlgorithm);
        } else if (!algorithmSelector.getItems().isEmpty()) {
            algorithmSelector.setValue(algorithmSelector.getItems().get(0));
        }

        Label algorithmLabel = new Label("Select Algorithm:");
        algorithmLabel.setStyle("-fx-text-fill: #F8FAFC;");

        HBox settings = new HBox(12, algorithmLabel, algorithmSelector);

        chart.setPadding(new Insets(20));
        chart.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        ScrollPane scrollPane = new ScrollPane(chart);
        scrollPane.setFitToHeight(true);
        scrollPane.setPrefHeight(180);
        scrollPane.setStyle(
            "-fx-background: #0F172A;" +
            "-fx-background-color: #0F172A;"
        );

        summary.setStyle(
            "-fx-text-fill: #94A3B8;" +
            "-fx-font-size: 14px;"
        );

        VBox chartCard = new VBox(15);
        chartCard.setPadding(new Insets(20));
        chartCard.setStyle(
            "-fx-background-color: #1E293B;" +
            "-fx-background-radius: 12;"
        );

        Label chartTitle = new Label("Execution Timeline");
        chartTitle.setStyle(
            "-fx-text-fill: #F8FAFC;" +
            "-fx-font-size: 17px;" +
            "-fx-font-weight: bold;"
        );

        chartCard.getChildren().addAll(chartTitle, scrollPane, summary);

        getChildren().addAll(
            heading,
            description,
            settings,
            chartCard
        );

        algorithmSelector.setOnAction(e -> renderChart());

        if (algorithmSelector.getValue() == null) {
            summary.setText("Run a simulation first to generate a Gantt Chart.");
        } else {
            renderChart();
        }
    }

    private void renderChart() {
        chart.getChildren().clear();

        String selected = algorithmSelector.getValue();

        if (selected == null) {
            summary.setText("No simulation results available.");
            return;
        }

        List<String> log = selected.equals("ACS") ? acsLog : sjfLog;
        List<Segment> segments = parseLog(log);

        if (segments.isEmpty()) {
            summary.setText("No execution data found.");
            return;
        }

        for (Segment segment : segments) {
            VBox block = new VBox(8);
            block.setAlignment(javafx.geometry.Pos.CENTER);
            block.setPadding(new Insets(10));

            double width = Math.max(85, segment.duration() * 85);

            Label process = new Label(segment.name);
            process.setStyle(
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 13px;"
            );

            Label time = new Label(
                segment.start + " - " + segment.end
            );
            time.setStyle(
                "-fx-text-fill: white;" +
                "-fx-font-size: 11px;"
            );

            block.setPrefWidth(width);
            block.setMinWidth(width);
            block.setMaxWidth(width);
            block.setPrefHeight(75);
            block.setStyle(
                "-fx-background-color: " + getColor(segment.name) + ";" +
                "-fx-background-radius: 7;"
            );

            block.getChildren().addAll(process, time);
            chart.getChildren().add(block);
        }

        int totalTime = segments.get(segments.size() - 1).end;

        summary.setText(
            "Algorithm: " + selected +
            "    |    Total Execution Time: " + totalTime +
            "    |    Segments: " + segments.size()
        );
    }

    private List<Segment> parseLog(List<String> log) {
        List<Segment> segments = new ArrayList<>();

        Pattern sjfPattern = Pattern.compile(
            "^Time\\s+(\\d+)\\s*:\\s*(.*)$"
        );

        Pattern acsPattern = Pattern.compile(
            "^Time\\s+(\\d+)\\s*\\|\\s*([^|]+)\\|.*$"
        );

        for (String line : log) {
            Matcher sjfMatcher = sjfPattern.matcher(line);
            Matcher acsMatcher = acsPattern.matcher(line);

            int time;
            String name;

            if (sjfMatcher.matches()) {
                time = Integer.parseInt(sjfMatcher.group(1));
                name = sjfMatcher.group(2).trim();

                if (name.startsWith("Running ")) {
                    name = name.substring(8);
                    int details = name.indexOf(" (Remaining:");
                    if (details >= 0) {
                        name = name.substring(0, details);
                    }
                } else if (name.startsWith("[Context Switch]")) {
                    name = "Context Switch";
                } else if (name.equals("CPU Idle")) {
                    name = "CPU Idle";
                } else {
                    continue;
                }

            } else if (acsMatcher.matches()) {
                time = Integer.parseInt(acsMatcher.group(1));
                name = acsMatcher.group(2).trim();

                if (name.equals("Done")) {
                    continue;
                }

            } else {
                continue;
            }

            addSegment(segments, name, time);
        }

        return segments;
    }

    private void addSegment(
        List<Segment> segments,
        String name,
        int time
    ) {
        if (!segments.isEmpty()) {
            Segment last = segments.get(segments.size() - 1);

            if (last.name.equals(name) && last.end == time) {
                last.end++;
                return;
            }
        }

        segments.add(new Segment(name, time, time + 1));
    }

    private String getColor(String name) {
        if (name.equals("CPU Idle")) {
            return "#475569";
        }

        if (name.equals("Context Switch")) {
            return "#B45309";
        }

        return colors[Math.floorMod(name.hashCode(), colors.length)];
    }

    private static class Segment {
        String name;
        int start;
        int end;

        Segment(String name, int start, int end) {
            this.name = name;
            this.start = start;
            this.end = end;
        }

        int duration() {
            return end - start;
        }
    }
}