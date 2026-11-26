package com.cpu.scheduler;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.beans.property.ReadOnlyStringWrapper;

public class ProcessManagementView extends VBox {

    private final ObservableList<Process> processes;
    private final TableView<Process> table = new TableView<>();

    private final TextField nameField = new TextField();
    private final TextField arrivalField = new TextField();
    private final TextField burstField = new TextField();
    private final TextField priorityField = new TextField();

    private final String cardColor = "#1E293B";
    private final String textColor = "#F8FAFC";
    private final String purple = "#8B5CF6";

    public ProcessManagementView(ObservableList<Process> processes) {
        this.processes = processes;

        setSpacing(20);
        setPadding(new Insets(30));

        Label heading = new Label("Manage Processes");
        heading.setStyle("-fx-text-fill: " + textColor + "; -fx-font-size: 22px; -fx-font-weight: bold;");

        Label description = new Label("Add, view and remove processes before running the simulation.");
        description.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 13px;");

        getChildren().addAll(heading, description, createForm(), createTable(), createActions());
        VBox.setVgrow(table, Priority.ALWAYS);
    }

    private VBox createForm() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.setStyle("-fx-background-color: " + cardColor + "; -fx-background-radius: 12;");

        Label title = new Label("Add New Process");
        title.setStyle("-fx-text-fill: " + textColor + "; -fx-font-size: 17px; -fx-font-weight: bold;");

        HBox fields = new HBox(12);

        configureField(nameField, "Process Name");
        configureField(arrivalField, "Arrival Time");
        configureField(burstField, "Burst Time");
        configureField(priorityField, "Priority");

        fields.getChildren().addAll(
                createField("Process Name", nameField),
                createField("Arrival Time", arrivalField),
                createField("Burst Time", burstField),
                createField("Priority", priorityField)
        );

        Button addButton = new Button("Add Process");
        addButton.setStyle(
                "-fx-background-color: " + purple + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 10 22;" +
                "-fx-cursor: hand;"
        );
        addButton.setOnAction(e -> addProcess());

        form.getChildren().addAll(title, fields, addButton);
        return form;
    }

    private VBox createField(String labelText, TextField field) {
        VBox box = new VBox(7);

        Label label = new Label(labelText);
        label.setStyle("-fx-text-fill: #CBD5E1; -fx-font-size: 12px;");

        box.getChildren().addAll(label, field);
        HBox.setHgrow(box, Priority.ALWAYS);

        return box;
    }

    private void configureField(TextField field, String prompt) {
        field.setPromptText(prompt);
        field.setStyle(
                "-fx-background-color: #0F172A;" +
                "-fx-text-fill: white;" +
                "-fx-prompt-text-fill: #64748B;" +
                "-fx-border-color: #334155;" +
                "-fx-border-radius: 6;" +
                "-fx-background-radius: 6;" +
                "-fx-padding: 10;"
        );
    }

    private TableView<Process> createTable() {
        table.setItems(processes);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Process, String> nameColumn = new TableColumn<>("Process");
        nameColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().name));

        TableColumn<Process, String> arrivalColumn = new TableColumn<>("Arrival Time");
        arrivalColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(String.valueOf(data.getValue().arrivalTime)));

        TableColumn<Process, String> burstColumn = new TableColumn<>("Burst Time");
        burstColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(String.valueOf(data.getValue().burstTime)));

        TableColumn<Process, String> priorityColumn = new TableColumn<>("Priority");
        priorityColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(String.valueOf(data.getValue().priority)));

        table.getColumns().addAll(nameColumn, arrivalColumn, burstColumn, priorityColumn);

        table.setPlaceholder(new Label("No processes added yet."));

        return table;
    }

    private HBox createActions() {
        HBox actions = new HBox(12);
        actions.setAlignment(Pos.CENTER_LEFT);

        Button deleteButton = new Button("Delete Selected");
        deleteButton.setStyle(
                "-fx-background-color: #DC2626;" +
                "-fx-text-fill: white;" +
                "-fx-padding: 10 18;" +
                "-fx-cursor: hand;"
        );
        deleteButton.setOnAction(e -> deleteSelected());

        Button clearButton = new Button("Clear All");
        clearButton.setStyle(
                "-fx-background-color: #334155;" +
                "-fx-text-fill: white;" +
                "-fx-padding: 10 18;" +
                "-fx-cursor: hand;"
        );
        clearButton.setOnAction(e -> clearAll());

        Label countLabel = new Label();
        countLabel.textProperty().bind(
                javafx.beans.binding.Bindings.size(processes).asString("Total Processes: %d")
        );
        countLabel.setStyle("-fx-text-fill: #94A3B8;");

        actions.getChildren().addAll(deleteButton, clearButton, countLabel);
        return actions;
    }

    private void addProcess() {
        String name = nameField.getText().trim();
        String arrivalText = arrivalField.getText().trim();
        String burstText = burstField.getText().trim();
        String priorityText = priorityField.getText().trim();

        if (name.isEmpty()) {
            showError("Please enter a process name.");
            return;
        }

        if (arrivalText.isEmpty()) {
            showError("Please enter the Arrival Time.");
            return;
        }

        if (burstText.isEmpty()) {
            showError("Please enter the Burst Time.");
            return;
        }

        if (priorityText.isEmpty()) {
            showError("Please enter the Priority.");
            return;
        }

        int arrival;
        int burst;
        int priority;

        try {
            arrival = Integer.parseInt(arrivalText);
        } catch (NumberFormatException ex) {
            showError("Arrival Time must be a valid integer.");
            return;
        }

        try {
            burst = Integer.parseInt(burstText);
        } catch (NumberFormatException ex) {
            showError("Burst Time must be a valid integer.");
            return;
        }

        try {
            priority = Integer.parseInt(priorityText);
        } catch (NumberFormatException ex) {
            showError("Priority must be a valid integer.");
            return;
        }

        if (arrival < 0) {
            showError("Arrival Time cannot be negative.");
            return;
        }

        if (burst <= 0) {
            showError("Burst Time must be greater than zero.");
            return;
        }

        if (priority < 0) {
            showError("Priority cannot be negative.");
            return;
        }

        for (Process process : processes) {
            if (process.name.equalsIgnoreCase(name)) {
                showError("A process with this name already exists.");
                return;
            }
        }

        processes.add(new Process(name, arrival, burst, priority));

        nameField.clear();
        arrivalField.clear();
        burstField.clear();
        priorityField.clear();
    }

    private void deleteSelected() {
        Process selected = table.getSelectionModel().getSelectedItem();

        if (selected != null) {
            processes.remove(selected);
        }
    }

    private void clearAll() {
        processes.clear();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Invalid Input");
        alert.setHeaderText("Unable to add process");
        alert.setContentText(message);
        alert.showAndWait();
    }
}