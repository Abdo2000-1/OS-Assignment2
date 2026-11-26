package com.cpu.scheduler;

import java.util.function.Function;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.beans.property.ReadOnlyStringWrapper;

public class PerformanceView extends BorderPane {

    public PerformanceView(
        SimulationResult sjfResult,
        SimulationResult acsResult
    ) {
        setStyle("-fx-background-color: #111827;");

        Label title = new Label("Performance Analysis");
        title.setStyle("-fx-font-size: 25px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label subtitle = new Label(
            "Scheduling performance, process metrics and algorithm comparison."
        );
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #9CA3AF;");

        VBox header = new VBox(8, title, subtitle);
        header.setPadding(new Insets(25));

        VBox content = new VBox(22);
        content.setPadding(new Insets(0, 25, 25, 25));

        content.getChildren().add(createSummary(sjfResult, acsResult));

        if (sjfResult != null || acsResult != null) {
            content.getChildren().add(createChart(sjfResult, acsResult));

            if (sjfResult != null) {
                content.getChildren().add(
                    createProcessSection("Preemptive SJF - Process Results", sjfResult)
                );
            }

            if (acsResult != null) {
                content.getChildren().add(
                    createProcessSection("ACS - Process Results", acsResult)
                );
            }
        } else {
            Label empty = new Label(
                "No simulation results available. Run SJF or ACS first."
            );
            empty.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 15px;");
            content.getChildren().add(empty);
        }

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle(
            "-fx-background: #111827; -fx-background-color: #111827;"
        );

        setTop(header);
        setCenter(scrollPane);
    }

    private VBox createSummary(
        SimulationResult sjfResult,
        SimulationResult acsResult
    ) {
        Label sectionTitle = new Label("Algorithm Comparison");
        sectionTitle.setStyle(
            "-fx-font-size: 19px; -fx-font-weight: bold; -fx-text-fill: white;"
        );

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(18));
        grid.setStyle(
            "-fx-background-color: #1F2937; -fx-background-radius: 10;"
        );

        String[] headers = {
            "Algorithm", "Processes", "Avg Waiting",
            "Avg Turnaround", "Avg Response", "Quantum", "Context Switch"
        };

        for (int i = 0; i < headers.length; i++) {
            grid.add(createCell(headers[i], true), i, 0);
        }

        if (sjfResult != null) {
            addSummaryRow(grid, sjfResult, 1);
        }

        if (acsResult != null) {
            addSummaryRow(grid, acsResult, sjfResult == null ? 1 : 2);
        }

        VBox cards = new VBox(12);

        if (sjfResult != null) {
            cards.getChildren().add(createMetricCard("Preemptive SJF", sjfResult));
        }

        if (acsResult != null) {
            cards.getChildren().add(createMetricCard("ACS", acsResult));
        }

        HBox metricCards = new HBox(15, cards);
        metricCards.setAlignment(Pos.CENTER_LEFT);

        return new VBox(15, sectionTitle, grid, metricCards);
    }

    private void addSummaryRow(
        GridPane grid,
        SimulationResult result,
        int row
    ) {
        String[] values = {
            result.getAlgorithm(),
            String.valueOf(result.getProcesses().size()),
            format(result.getAverageWaiting()),
            format(result.getAverageTurnaround()),
            format(result.getAverageResponse()),
            result.getQuantum() == 0 ? "N/A" : String.valueOf(result.getQuantum()),
            String.valueOf(result.getContextSwitch())
        };

        for (int i = 0; i < values.length; i++) {
            grid.add(createCell(values[i], false), i, row);
        }
    }

    private Label createCell(String value, boolean header) {
        Label label = new Label(value);
        label.setMinWidth(95);
        label.setStyle(
            header
                ? "-fx-text-fill: #A78BFA; -fx-font-weight: bold; -fx-font-size: 12px;"
                : "-fx-text-fill: #E5E7EB; -fx-font-size: 12px;"
        );
        return label;
    }

    private VBox createMetricCard(
        String name,
        SimulationResult result
    ) {
        Label heading = new Label(name);
        heading.setStyle(
            "-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;"
        );

        Label waiting = metricLabel(
            "Average Waiting Time", result.getAverageWaiting()
        );

        Label turnaround = metricLabel(
            "Average Turnaround Time", result.getAverageTurnaround()
        );

        Label response = metricLabel(
            "Average Response Time", result.getAverageResponse()
        );

        Label settings = new Label(
            "Quantum: " +
            (result.getQuantum() == 0 ? "N/A" : result.getQuantum()) +
            "     Context Switch: " + result.getContextSwitch()
        );
        settings.setStyle("-fx-text-fill: #A78BFA; -fx-font-size: 12px;");

        VBox card = new VBox(
            10, heading, waiting, turnaround, response, settings
        );

        card.setPadding(new Insets(18));
        card.setPrefWidth(310);
        card.setStyle(
            "-fx-background-color: #1F2937; -fx-background-radius: 10;"
        );

        return card;
    }

    private Label metricLabel(String name, double value) {
        Label label = new Label(name + ": " + format(value));
        label.setStyle("-fx-text-fill: #D1D5DB; -fx-font-size: 13px;");
        return label;
    }

    private VBox createProcessSection(
        String title,
        SimulationResult result
    ) {
        Label heading = new Label(title);
        heading.setStyle(
            "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;"
        );

        TableView<Process> table = new TableView<>();
        table.setItems(
            javafx.collections.FXCollections.observableArrayList(
                result.getProcesses()
            )
        );

        addColumn(table, "Process", process -> process.name, 90);
        addColumn(table, "Arrival", process -> String.valueOf(process.arrivalTime), 90);
        addColumn(table, "Burst", process -> String.valueOf(process.burstTime), 90);
        addColumn(table, "Priority", process -> String.valueOf(process.priority), 90);
        addColumn(table, "First Run", process -> String.valueOf(process.firstRunTime), 100);
        addColumn(table, "Finish", process -> String.valueOf(process.finishTime), 90);
        addColumn(table, "Waiting", process -> String.valueOf(process.waitingTime), 90);
        addColumn(table, "Turnaround", process -> String.valueOf(process.turnaroundTime), 105);
        addColumn(table, "Response", process -> String.valueOf(process.responseTime), 95);

        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(Math.max(120, (result.getProcesses().size() + 1) * 32 + 5));
        table.setStyle(
            "-fx-background-color: #1F2937; -fx-control-inner-background: #1F2937;"
        );

        return new VBox(12, heading, table);
    }

    private void addColumn(
        TableView<Process> table,
        String title,
        Function<Process, String> value,
        double width
    ) {
        TableColumn<Process, String> column = new TableColumn<>(title);
        column.setPrefWidth(width);
        column.setCellValueFactory(
            data -> new ReadOnlyStringWrapper(value.apply(data.getValue()))
        );
        table.getColumns().add(column);
    }

    private VBox createChart(
        SimulationResult sjfResult,
        SimulationResult acsResult
    ) {
        Label title = new Label("Average Metrics Comparison");
        title.setStyle(
            "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;"
        );

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Performance Metrics");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Average Time");
        yAxis.setForceZeroInRange(true);

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("Preemptive SJF vs ACS");
        chart.setAnimated(false);
        chart.setPrefHeight(380);
        chart.setMinWidth(650);

        if (sjfResult != null) {
            XYChart.Series<String, Number> sjf = new XYChart.Series<>();
            sjf.setName("Preemptive SJF");
            addChartData(sjf, sjfResult);
            chart.getData().add(sjf);
        }

        if (acsResult != null) {
            XYChart.Series<String, Number> acs = new XYChart.Series<>();
            acs.setName("ACS");
            addChartData(acs, acsResult);
            chart.getData().add(acs);
        }

        VBox container = new VBox(12, title, chart);
        container.setPadding(new Insets(18));
        container.setStyle(
            "-fx-background-color: #1F2937; -fx-background-radius: 10;"
        );

        return container;
    }

    private void addChartData(
        XYChart.Series<String, Number> series,
        SimulationResult result
    ) {
        series.getData().add(
            new XYChart.Data<>("Waiting", result.getAverageWaiting())
        );
        series.getData().add(
            new XYChart.Data<>("Turnaround", result.getAverageTurnaround())
        );
        series.getData().add(
            new XYChart.Data<>("Response", result.getAverageResponse())
        );
    }

    private String format(double value) {
        return String.format("%.2f", value);
    }
}