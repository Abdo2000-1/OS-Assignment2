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

public class SJFSchedulerView extends VBox {

    private final ObservableList<Process> inputProcesses;
    private final ObservableList<Process> results = FXCollections.observableArrayList();
    private final Consumer<List<String>> onSimulationComplete;
    private final Consumer<SimulationResult> onResultComplete;

    private final TextField contextSwitchField = new TextField("0");
    private final TableView<Process> resultsTable = new TableView<>();
    private final TextArea executionLog = new TextArea();

    private final Label averageWaiting = new Label("Average Waiting Time: —");
    private final Label averageTurnaround = new Label("Average Turnaround Time: —");
    private final Label averageResponse = new Label("Average Response Time: —");

    private final String cardColor = "#1E293B";
    private final String textColor = "#F8FAFC";
    private final String purple = "#8B5CF6";

    public SJFSchedulerView(
        ObservableList<Process> processes,
        Consumer<List<String>> onSimulationComplete,
        Consumer<SimulationResult> onResultComplete
    ) {
        inputProcesses = processes;
        this.onSimulationComplete = onSimulationComplete;
        this.onResultComplete = onResultComplete;

        setSpacing(18);
        setPadding(new Insets(25));

        Label heading = new Label("Preemptive SJF Scheduler");
        heading.setStyle("-fx-text-fill: " + textColor + "; -fx-font-size: 22px; -fx-font-weight: bold;");

        Label description = new Label("Run Preemptive Shortest Job First and analyze its results.");
        description.setStyle("-fx-text-fill: #94A3B8;");

        HBox settings = new HBox(12);
        settings.setPadding(new Insets(15));
        settings.setStyle("-fx-background-color: " + cardColor + "; -fx-background-radius: 10;");

        Label contextLabel = new Label("Context Switching Time:");
        contextLabel.setStyle("-fx-text-fill: " + textColor + ";");

        contextSwitchField.setPrefWidth(100);
        contextSwitchField.setStyle(
            "-fx-background-color: #0F172A;" +
            "-fx-text-fill: white;" +
            "-fx-prompt-text-fill: #64748B;" +
            "-fx-padding: 9;"
        );

        Button runButton = new Button("Run Simulation");
        runButton.setStyle(
            "-fx-background-color: " + purple + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 10 20;" +
            "-fx-cursor: hand;"
        );
        runButton.setOnAction(e -> runSimulation());

        settings.getChildren().addAll(contextLabel, contextSwitchField, runButton);

        Label logTitle = new Label("Execution Log");
        styleTitle(logTitle);

        executionLog.setEditable(false);
        executionLog.setWrapText(true);
        executionLog.setPrefRowCount(7);
        executionLog.setStyle(
            "-fx-control-inner-background: #0F172A;" +
            "-fx-text-fill: #CBD5E1;" +
            "-fx-font-family: Consolas;"
        );

        Label resultsTitle = new Label("Process Results");
        styleTitle(resultsTitle);

        createResultsTable();

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
            metrics
        );

        VBox.setVgrow(resultsTable, Priority.ALWAYS);
    }

    private void styleTitle(Label label) {
        label.setStyle("-fx-text-fill: " + textColor + "; -fx-font-size: 16px; -fx-font-weight: bold;");
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

        resultsTable.setPlaceholder(new Label("Run the simulation to display results."));
    }

    private void addColumn(String title, java.util.function.Function<Process, String> value) {
        TableColumn<Process, String> column = new TableColumn<>(title);
        column.setCellValueFactory(data ->
            new ReadOnlyStringWrapper(value.apply(data.getValue()))
        );
        resultsTable.getColumns().add(column);
    }

    private void runSimulation() {
        if (inputProcesses.isEmpty()) {
            showError("Please add processes before running the simulation.");
            return;
        }

        try {
            int contextSwitch = Integer.parseInt(contextSwitchField.getText().trim());

            if (contextSwitch < 0) {
                showError("Context switching time cannot be negative.");
                return;
            }

            SJFScheduler scheduler = new SJFScheduler(
                inputProcesses,
                contextSwitch
            );

            scheduler.simulate();

            List<Process> completedProcesses = scheduler.getProcesses();
            results.setAll(completedProcesses);

            SimulationResult result = new SimulationResult(
                "Preemptive SJF", completedProcesses, 0, contextSwitch
            );

            if (onResultComplete != null) {
                onResultComplete.accept(result);
            }

            List<String> log = new java.util.ArrayList<>(scheduler.getExecutionLog());

            executionLog.setText(String.join("\n", log));

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
            showError("Context switching time must be a valid integer.");
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Simulation Error");
        alert.setHeaderText("Unable to run simulation");
        alert.setContentText(message);
        alert.showAndWait();
    }
}