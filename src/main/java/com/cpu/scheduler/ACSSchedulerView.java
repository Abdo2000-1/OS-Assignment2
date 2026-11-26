package com.cpu.scheduler;

import java.util.List;
import java.util.function.Consumer;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class ACSSchedulerView extends VBox {

    private final ObservableList<Process> inputProcesses;
    private final ObservableList<Process> results = FXCollections.observableArrayList();
    private final Consumer<List<String>> onSimulationComplete;
    private final Consumer<SimulationResult> onResultComplete;

    private final TextField quantumField = new TextField("4");
    private final TextField contextSwitchField = new TextField("0");

    private final TextArea executionLog = new TextArea();
    private final TableView<Process> resultsTable = new TableView<>();
    private final ObservableList<ACSMonitorEntry> monitorEntries = FXCollections.observableArrayList();
    private final TableView<ACSMonitorEntry> monitorTable = new TableView<>();

    private final Label averageWaiting = new Label("Average Waiting Time: —");
    private final Label averageTurnaround = new Label("Average Turnaround Time: —");
    private final Label averageResponse = new Label("Average Response Time: —");

    private final String cardColor = "#1E293B";
    private final String textColor = "#F8FAFC";
    private final String purple = "#8B5CF6";

    public ACSSchedulerView(
        ObservableList<Process> processes,
        Consumer<List<String>> onSimulationComplete,
        Consumer<SimulationResult> onResultComplete
    ) {
        inputProcesses = processes;
        this.onSimulationComplete = onSimulationComplete;
        this.onResultComplete = onResultComplete;

        setSpacing(18);
        setPadding(new Insets(25));

        Label heading = new Label("Adaptive CPU Scheduling Monitor");
        heading.setStyle("-fx-text-fill: " + textColor + "; -fx-font-size: 22px; -fx-font-weight: bold;");

        Label description = new Label(
            "Monitor Tier 1 Round Robin, Tier 2 Priority Scheduling and Aging."
        );
        description.setStyle("-fx-text-fill: #94A3B8;");

        HBox settings = new HBox(12);
        settings.setPadding(new Insets(15));
        settings.setStyle("-fx-background-color: " + cardColor + "; -fx-background-radius: 10;");

        Label quantumLabel = new Label("Quantum:");
        quantumLabel.setStyle("-fx-text-fill: " + textColor + ";");

        configureField(quantumField);

        Label contextLabel = new Label("Context Switching:");
        contextLabel.setStyle("-fx-text-fill: " + textColor + ";");

        configureField(contextSwitchField);

        Button runButton = new Button("Run ACS Simulation");
        runButton.setStyle(
            "-fx-background-color: " + purple + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 10 18;" +
            "-fx-cursor: hand;"
        );
        runButton.setOnAction(e -> runSimulation());

        settings.getChildren().addAll(
            quantumLabel,
            quantumField,
            contextLabel,
            contextSwitchField,
            runButton
        );

        Label logTitle = new Label("Execution Log and Queue Monitor");
        styleTitle(logTitle);

        executionLog.setEditable(false);
        executionLog.setWrapText(false);
        executionLog.setPrefRowCount(25);
        executionLog.setPrefHeight(400);
        executionLog.setMinHeight(280);
        executionLog.setStyle(
            "-fx-control-inner-background: #0F172A;" +
            "-fx-text-fill: #CBD5E1;" +
            "-fx-font-family: Consolas;"
        );

        Label resultsTitle = new Label("Process Results");
        styleTitle(resultsTitle);

        Label monitorTitle = new Label("ACS Heuristic Demotion Matrix Monitor");
        styleTitle(monitorTitle);

        createResultsTable();
        resultsTable.setPrefHeight(220);
        resultsTable.setMinHeight(160);

        createMonitorTable();
        monitorTable.setPrefHeight(300);
        monitorTable.setMinHeight(200);

        VBox metrics = new VBox(8);
        metrics.setPadding(new Insets(15));
        metrics.setStyle("-fx-background-color: " + cardColor + "; -fx-background-radius: 10;");

        averageWaiting.setStyle("-fx-text-fill: " + textColor + ";");
        averageTurnaround.setStyle("-fx-text-fill: " + textColor + ";");
        averageResponse.setStyle("-fx-text-fill: " + textColor + ";");

        metrics.getChildren().addAll(
            averageWaiting,
            averageTurnaround,
            averageResponse
        );

        getChildren().addAll(
            heading,
            description,
            settings,
            logTitle,
            executionLog,
            resultsTitle,
            resultsTable,
            monitorTitle,
            monitorTable,
            metrics
        );

        VBox.setVgrow(executionLog, Priority.ALWAYS);
        VBox.setVgrow(resultsTable, Priority.NEVER);
    }

    private void configureField(TextField field) {
        field.setPrefWidth(75);
        field.setStyle(
            "-fx-background-color: #0F172A;" +
            "-fx-text-fill: white;" +
            "-fx-border-color: #334155;" +
            "-fx-border-radius: 6;" +
            "-fx-background-radius: 6;" +
            "-fx-padding: 9;"
        );
    }

    private void styleTitle(Label label) {
        label.setStyle(
            "-fx-text-fill: " + textColor +
            "; -fx-font-size: 16px; -fx-font-weight: bold;"
        );
    }

    private void createResultsTable() {
        resultsTable.setItems(results);
        resultsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        addColumn("Process", p -> p.name);
        addColumn("Arrival", p -> String.valueOf(p.arrivalTime));
        addColumn("Burst", p -> String.valueOf(p.burstTime));
        addColumn("Finish", p -> String.valueOf(p.finishTime));
        addColumn("Turnaround", p -> String.valueOf(p.turnaroundTime));
        addColumn("Waiting", p -> String.valueOf(p.waitingTime));
        addColumn("Response", p -> String.valueOf(p.responseTime));

        resultsTable.setPlaceholder(
            new Label("Run the simulation to display results.")
        );
    }

    private void addColumn(
        String title,
        java.util.function.Function<Process, String> value
    ) {
        TableColumn<Process, String> column = new TableColumn<>(title);

        column.setCellValueFactory(data ->
            new ReadOnlyStringWrapper(value.apply(data.getValue()))
        );

        resultsTable.getColumns().add(column);
    }

    private void createMonitorTable() {
        monitorTable.setItems(monitorEntries);
        monitorTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        addMonitorColumn("Time", entry -> String.valueOf(entry.timeStep));
        addMonitorColumn("Process", entry -> entry.processName);
        addMonitorColumn("Tier", entry -> entry.currentTier);
        addMonitorColumn("Remaining Burst", entry -> String.valueOf(entry.remainingBurst));
        addMonitorColumn("Priority", entry -> String.valueOf(entry.originalPriority));
        addMonitorColumn("Aging Boost", entry -> String.valueOf(entry.agingBoost));
        addMonitorColumn("Effective Priority", entry -> String.valueOf(entry.effectivePriority));
        addMonitorColumn("Demotion", entry -> entry.demotionStatus);
        addMonitorColumn("Running / CPU", entry -> entry.runningProcess);
        addMonitorColumn("Tier 1 Queue", entry -> entry.tier1QueueState);
        addMonitorColumn("Tier 2 Queue", entry -> entry.tier2QueueState);

        monitorTable.setPlaceholder(
            new Label("Run ACS simulation to display monitor records.")
        );
    }

    private void addMonitorColumn(
        String title,
        java.util.function.Function<ACSMonitorEntry, String> value
    ) {
        TableColumn<ACSMonitorEntry, String> column = new TableColumn<>(title);

        column.setCellValueFactory(data ->
            new ReadOnlyStringWrapper(value.apply(data.getValue()))
        );

        monitorTable.getColumns().add(column);
    }

    private void runSimulation() {
        if (inputProcesses.isEmpty()) {
            showError("Please add processes before running the simulation.");
            return;
        }

        try {
            int quantum = Integer.parseInt(quantumField.getText().trim());
            int contextSwitch = Integer.parseInt(contextSwitchField.getText().trim());

            if (quantum <= 0) {
                showError("Quantum must be greater than zero.");
                return;
            }

            if (contextSwitch < 0) {
                showError("Context switching time cannot be negative.");
                return;
            }

            ACSScheduler scheduler = new ACSScheduler(
                inputProcesses,
                quantum,
                contextSwitch
            );

            scheduler.simulate();

            List<Process> completedProcesses = scheduler.getProcesses();
            results.setAll(completedProcesses);

            SimulationResult result = new SimulationResult(
                "ACS", completedProcesses, quantum, contextSwitch
            );

            if (onResultComplete != null) {
                onResultComplete.accept(result);
            }

            List<String> log = new java.util.ArrayList<>(scheduler.getExecutionLog());

            executionLog.setText(String.join("\n", log));
            executionLog.positionCaret(0);

            if (onSimulationComplete != null) {
                onSimulationComplete.accept(log);
            }

            double totalWaiting = 0;
            double totalTurnaround = 0;
            double totalResponse = 0;

            for (Process process : completedProcesses) {
                totalWaiting += process.waitingTime;
                totalTurnaround += process.turnaroundTime;
                totalResponse += process.responseTime;
            }

            int count = completedProcesses.size();

            averageWaiting.setText(
                String.format("Average Waiting Time: %.2f", totalWaiting / count)
            );

            averageTurnaround.setText(
                String.format("Average Turnaround Time: %.2f", totalTurnaround / count)
            );

            averageResponse.setText(
                String.format("Average Response Time: %.2f", totalResponse / count)
            );

        } catch (NumberFormatException ex) {
            showError("Quantum and Context Switching must be valid integers.");
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Simulation Error");
        alert.setHeaderText("Unable to run ACS simulation");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
