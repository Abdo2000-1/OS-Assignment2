package com.cpu.scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.application.Application;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Compact GUI for the assignment.
 *
 * The scheduler implementations remain in their own classes. This file only
 * contains the user interface, input validation, result display, and a small
 * Gantt chart renderer so the GUI is easy to discuss and run.
 */
public class Main extends Application {

    private static final String BACKGROUND = "#0B1120";
    private static final String PANEL = "#1E293B";
    private static final String FIELD = "#111827";
    private static final String PURPLE = "#8B5CF6";
    private static final String TEXT = "#F8FAFC";
    private static final String MUTED = "#94A3B8";

    private final ObservableList<Process> inputProcesses =
            FXCollections.observableArrayList();
    private final ObservableList<Process> displayedProcesses =
            FXCollections.observableArrayList();

    private final TextField nameField = new TextField();
    private final TextField arrivalField = new TextField();
    private final TextField burstField = new TextField();
    private final TextField priorityField = new TextField();
    private final TextField quantumField = new TextField("4");
    private final TextField contextSwitchField = new TextField("0");

    private final TableView<Process> inputTable = new TableView<>();
    private final TableView<Process> resultTable = new TableView<>();
    private final TextArea executionLog = new TextArea();
    private final HBox gantt = new HBox(8);
    private final Label statusLabel =
            new Label("Add processes, then run an algorithm.");
    private final Label averagesLabel =
            new Label("Waiting: —   Turnaround: —   Response: —");

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + BACKGROUND + ";");
        root.setTop(createHeader());
        root.setLeft(createInputPanel());
        root.setCenter(createOutputPanel());

        Scene scene = new Scene(root, 1250, 800);
        stage.setTitle("CPU Scheduling Simulator");
        stage.setMinWidth(1050);
        stage.setMinHeight(700);
        stage.setScene(scene);
        stage.show();
    }

    private VBox createHeader() {
        Label title = new Label("CPU Scheduling Simulator");
        title.setStyle("-fx-text-fill: " + TEXT
                + "; -fx-font-size: 25px; -fx-font-weight: bold;");

        Label subtitle = new Label(
                "CS241 Assignment 2  |  Preemptive SJF + Adaptive Container Scheduler");
        subtitle.setStyle("-fx-text-fill: " + MUTED + "; -fx-font-size: 13px;");

        VBox header = new VBox(6, title, subtitle);
        header.setPadding(new Insets(22, 25, 18, 25));
        return header;
    }

    private VBox createInputPanel() {
        Label heading = heading("Processes and Settings");

        nameField.setPromptText("Name");
        arrivalField.setPromptText("Arrival");
        burstField.setPromptText("Burst");
        priorityField.setPromptText("Priority");
        quantumField.setPromptText("Quantum");
        contextSwitchField.setPromptText("Context switch");

        GridPane form = new GridPane();
        form.setHgap(8);
        form.setVgap(8);
        addFormRow(form, 0, "Name", nameField);
        addFormRow(form, 1, "Arrival", arrivalField);
        addFormRow(form, 2, "Burst", burstField);
        addFormRow(form, 3, "Priority", priorityField);

        Button add = primaryButton("Add Process");
        add.setOnAction(event -> addProcess());

        inputTable.setItems(inputProcesses);
        inputTable.setPlaceholder(new Label("No processes"));
        inputTable.setPrefHeight(245);
        inputTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        addProcessColumns(inputTable);

        Button delete = secondaryButton("Delete Selected");
        delete.setOnAction(event -> {
            Process selected = inputTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                inputProcesses.remove(selected);
            }
        });

        Button clear = secondaryButton("Clear All");
        clear.setOnAction(event -> inputProcesses.clear());
        HBox processActions = new HBox(8, delete, clear);

        Label settingsTitle = heading("Simulation Settings");
        GridPane settings = new GridPane();
        settings.setHgap(8);
        settings.setVgap(8);
        addFormRow(settings, 0, "Quantum", quantumField);
        addFormRow(settings, 1, "Context Switch", contextSwitchField);

        Button sjf = primaryButton("Run Preemptive SJF");
        sjf.setOnAction(event -> runSjf());

        Button acs = primaryButton("Run ACS");
        acs.setOnAction(event -> runAcs());

        Button clearOutput = secondaryButton("Clear Output");
        clearOutput.setOnAction(event -> clearOutput());

        VBox panel = new VBox(
                12, heading, form, add, inputTable, processActions,
                settingsTitle, settings, sjf, acs, clearOutput);
        panel.setPadding(new Insets(20));
        panel.setPrefWidth(325);
        panel.setStyle("-fx-background-color: " + PANEL + ";");
        return panel;
    }

    private VBox createOutputPanel() {
        Label outputTitle = heading("Simulation Output");
        statusLabel.setStyle("-fx-text-fill: " + MUTED + ";");
        averagesLabel.setStyle("-fx-text-fill: #C4B5FD; -fx-font-weight: bold;");

        executionLog.setEditable(false);
        executionLog.setWrapText(false);
        executionLog.setPrefRowCount(8);
        executionLog.setStyle("-fx-control-inner-background: " + FIELD
                + "; -fx-text-fill: " + TEXT + ";");

        resultTable.setItems(displayedProcesses);
        resultTable.setPlaceholder(new Label("Run a simulation to display results."));
        resultTable.setPrefHeight(220);
        resultTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        addResultColumns();

        gantt.setAlignment(Pos.CENTER_LEFT);
        gantt.setPadding(new Insets(12));
        ScrollPane ganttScroll = new ScrollPane(gantt);
        ganttScroll.setFitToHeight(true);
        ganttScroll.setPrefHeight(120);
        ganttScroll.setStyle("-fx-background: " + FIELD
                + "; -fx-background-color: " + FIELD + ";");

        VBox output = new VBox(
                12, outputTitle, statusLabel, averagesLabel,
                sectionLabel("Execution Log"), executionLog,
                sectionLabel("Process Results"), resultTable,
                sectionLabel("Gantt Chart"), ganttScroll);
        output.setPadding(new Insets(20, 25, 25, 20));
        VBox.setVgrow(executionLog, Priority.ALWAYS);
        VBox.setVgrow(resultTable, Priority.SOMETIMES);
        VBox.setVgrow(ganttScroll, Priority.SOMETIMES);
        return output;
    }

    private void addProcess() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            showError("Process name is required.");
            return;
        }

        try {
            int arrival = parseInteger(arrivalField, "Arrival time");
            int burst = parseInteger(burstField, "Burst time");
            int priority = parseInteger(priorityField, "Priority");

            if (arrival < 0) {
                throw new IllegalArgumentException("Arrival time cannot be negative.");
            }
            if (burst <= 0) {
                throw new IllegalArgumentException("Burst time must be greater than zero.");
            }
            if (priority < 0) {
                throw new IllegalArgumentException("Priority cannot be negative.");
            }
            for (Process process : inputProcesses) {
                if (process.name.equalsIgnoreCase(name)) {
                    throw new IllegalArgumentException("Process names must be unique.");
                }
            }

            inputProcesses.add(new Process(name, arrival, burst, priority));
            nameField.clear();
            arrivalField.clear();
            burstField.clear();
            priorityField.clear();
            statusLabel.setText(inputProcesses.size() + " process(es) ready.");
        } catch (IllegalArgumentException error) {
            showError(error.getMessage());
        }
    }

    private void runSjf() {
        if (inputProcesses.isEmpty()) {
            showError("Add at least one process first.");
            return;
        }

        try {
            int contextSwitch = parseInteger(contextSwitchField, "Context switch");
            if (contextSwitch < 0) {
                throw new IllegalArgumentException(
                        "Context switch cannot be negative.");
            }

            SJFScheduler scheduler = new SJFScheduler(inputProcesses, contextSwitch);
            scheduler.simulate();
            RunResult result = new RunResult("Preemptive SJF", scheduler.getProcesses(),
                    scheduler.getExecutionLog(), 0, contextSwitch);
            display(result);
        } catch (IllegalArgumentException error) {
            showError(error.getMessage());
        }
    }

    private void runAcs() {
        if (inputProcesses.isEmpty()) {
            showError("Add at least one process first.");
            return;
        }

        try {
            int quantum = parseInteger(quantumField, "Quantum");
            int contextSwitch = parseInteger(contextSwitchField, "Context switch");
            if (quantum <= 0) {
                throw new IllegalArgumentException("Quantum must be greater than zero.");
            }
            if (contextSwitch < 0) {
                throw new IllegalArgumentException(
                        "Context switch cannot be negative.");
            }

            ACSScheduler scheduler = new ACSScheduler(
                    inputProcesses, quantum, contextSwitch);
            scheduler.simulate();
            RunResult result = new RunResult("ACS", scheduler.getProcesses(),
                    scheduler.getExecutionLog(), quantum, contextSwitch);
            display(result);
        } catch (IllegalArgumentException error) {
            showError(error.getMessage());
        }
    }

    private void display(RunResult result) {
        displayedProcesses.setAll(result.processes);
        executionLog.setText(String.join("\n", result.log));
        averagesLabel.setText(String.format(
                "Waiting: %.2f   Turnaround: %.2f   Response: %.2f",
                result.averageWaiting, result.averageTurnaround,
                result.averageResponse));
        statusLabel.setText(result.algorithm + " completed  |  Quantum: "
                + (result.quantum == 0 ? "N/A" : result.quantum)
                + "  |  Context switch: " + result.contextSwitch);
        renderGantt(result.log);
    }

    private void clearOutput() {
        displayedProcesses.clear();
        executionLog.clear();
        gantt.getChildren().clear();
        averagesLabel.setText("Waiting: —   Turnaround: —   Response: —");
        statusLabel.setText("Output cleared.");
    }

    private void renderGantt(List<String> log) {
        gantt.getChildren().clear();
        for (Segment segment : parseSegments(log)) {
            Label process = new Label(segment.name + "\n"
                    + segment.start + " - " + segment.end);
            process.setAlignment(Pos.CENTER);
            process.setTextFill(javafx.scene.paint.Color.WHITE);
            process.setStyle("-fx-font-weight: bold; -fx-background-color: "
                    + colorFor(segment.name)
                    + "; -fx-background-radius: 7; -fx-padding: 14;");
            double width = Math.max(85, segment.duration() * 48);
            process.setMinWidth(width);
            process.setPrefWidth(width);
            gantt.getChildren().add(process);
        }
    }

    private List<Segment> parseSegments(List<String> log) {
        List<Segment> segments = new ArrayList<>();
        Pattern sjfPattern = Pattern.compile("^Time\\s+(\\d+)\\s*:\\s*(.*)$");
        Pattern acsPattern = Pattern.compile(
                "^Time\\s+(\\d+)\\s*\\|\\s*([^|]+)\\|.*$");

        for (String line : log) {
            Matcher sjf = sjfPattern.matcher(line);
            Matcher acs = acsPattern.matcher(line);
            int time;
            String name;

            if (sjf.matches()) {
                time = Integer.parseInt(sjf.group(1));
                name = sjf.group(2).trim();
                if (name.startsWith("Running ")) {
                    name = name.substring(8);
                    int details = name.indexOf(" (Remaining:");
                    if (details >= 0) {
                        name = name.substring(0, details);
                    }
                } else if (name.startsWith("[Context Switch]")) {
                    name = "Context Switch";
                } else if (!name.equals("CPU Idle")) {
                    continue;
                }
            } else if (acs.matches()) {
                time = Integer.parseInt(acs.group(1));
                name = acs.group(2).trim();
                if (name.equals("Done")) {
                    continue;
                }
            } else {
                continue;
            }

            if (!segments.isEmpty()) {
                Segment previous = segments.get(segments.size() - 1);
                if (previous.name.equals(name) && previous.end == time) {
                    previous.end++;
                    continue;
                }
            }
            segments.add(new Segment(name, time, time + 1));
        }
        return segments;
    }

    private void addProcessColumns(TableView<Process> table) {
        addColumn(table, "Process", process -> process.name);
        addColumn(table, "Arrival", process -> String.valueOf(process.arrivalTime));
        addColumn(table, "Burst", process -> String.valueOf(process.burstTime));
        addColumn(table, "Priority", process -> String.valueOf(process.priority));
    }

    private void addResultColumns() {
        addColumn(resultTable, "Process", process -> process.name);
        addColumn(resultTable, "Arrival", process -> String.valueOf(process.arrivalTime));
        addColumn(resultTable, "Burst", process -> String.valueOf(process.burstTime));
        addColumn(resultTable, "Finish", process -> String.valueOf(process.finishTime));
        addColumn(resultTable, "Turnaround",
                process -> String.valueOf(process.turnaroundTime));
        addColumn(resultTable, "Waiting",
                process -> String.valueOf(process.waitingTime));
        addColumn(resultTable, "Response",
                process -> String.valueOf(process.responseTime));
    }

    private void addColumn(
            TableView<Process> table,
            String title,
            Function<Process, String> value
    ) {
        TableColumn<Process, String> column = new TableColumn<>(title);
        column.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(value.apply(data.getValue())));
        table.getColumns().add(column);
    }

    private void addFormRow(GridPane grid, int row, String label, TextField field) {
        Label text = new Label(label);
        text.setStyle("-fx-text-fill: " + MUTED + ";");
        styleField(field);
        grid.add(text, 0, row);
        grid.add(field, 1, row);
        GridPane.setHgrow(field, Priority.ALWAYS);
    }

    private void styleField(TextField field) {
        field.setStyle("-fx-background-color: " + FIELD
                + "; -fx-text-fill: " + TEXT + "; -fx-prompt-text-fill: #64748B;");
    }

    private Label heading(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: " + TEXT
                + "; -fx-font-size: 17px; -fx-font-weight: bold;");
        return label;
    }

    private Label sectionLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #C4B5FD; -fx-font-weight: bold;");
        return label;
    }

    private Button primaryButton(String text) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setStyle("-fx-background-color: " + PURPLE
                + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 9 12;");
        return button;
    }

    private Button secondaryButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: #334155; -fx-text-fill: white;");
        return button;
    }

    private int parseInteger(TextField field, String label) {
        String value = field.getText().trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException(label + " is required.");
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException error) {
            throw new IllegalArgumentException(label + " must be an integer.");
        }
    }

    private String colorFor(String name) {
        if (name.equals("CPU Idle")) {
            return "#475569";
        }
        if (name.equals("Context Switch")) {
            return "#B45309";
        }
        String[] colors = {"#0F766E", "#2563EB", "#7C3AED", "#BE123C", "#15803D"};
        return colors[Math.floorMod(name.hashCode(), colors.length)];
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("CPU Scheduler");
        alert.setHeaderText("Invalid input");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static class Segment {
        final String name;
        final int start;
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

    private static class RunResult {
        final String algorithm;
        final List<Process> processes;
        final List<String> log;
        final int quantum;
        final int contextSwitch;
        final double averageWaiting;
        final double averageTurnaround;
        final double averageResponse;

        RunResult(
                String algorithm,
                List<Process> source,
                List<String> log,
                int quantum,
                int contextSwitch
        ) {
            this.algorithm = algorithm;
            this.log = new ArrayList<>(log);
            this.quantum = quantum;
            this.contextSwitch = contextSwitch;
            this.processes = new ArrayList<>();

            double waiting = 0;
            double turnaround = 0;
            double response = 0;
            for (Process original : source) {
                Process copy = original.copy();
                copy.remainingTime = original.remainingTime;
                copy.firstRunTime = original.firstRunTime;
                copy.finishTime = original.finishTime;
                copy.turnaroundTime = original.turnaroundTime;
                copy.waitingTime = original.waitingTime;
                copy.responseTime = original.responseTime;
                processes.add(copy);
                waiting += copy.waitingTime;
                turnaround += copy.turnaroundTime;
                response += copy.responseTime;
            }

            int count = processes.size();
            averageWaiting = count == 0 ? 0 : waiting / count;
            averageTurnaround = count == 0 ? 0 : turnaround / count;
            averageResponse = count == 0 ? 0 : response / count;
        }
    }
}
