package com.cpu.scheduler;

import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {

    private BorderPane mainLayout;
    private Label pageTitle;

    private final ObservableList<Process> processes = FXCollections.observableArrayList();

    private List<String> sjfExecutionLog = new ArrayList<>();
    private List<String> acsExecutionLog = new ArrayList<>();

    private String lastAlgorithm = "";
    private SimulationResult sjfResult;
    private SimulationResult acsResult;

    private final String background = "#0B1120";
    private final String sidebarColor = "#111827";
    private final String cardColor = "#1E293B";
    private final String purple = "#8B5CF6";
    private final String textColor = "#F8FAFC";

    @Override
    public void start(Stage stage) {
        mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: " + background + ";");

        mainLayout.setLeft(createSidebar());
        mainLayout.setTop(createHeader());
        mainLayout.setCenter(createDashboard());

        Scene scene = new Scene(mainLayout, 1200, 750);

        stage.setTitle("CPU Scheduling Simulator");
        stage.setScene(scene);
        stage.setMinWidth(1000);
        stage.setMinHeight(650);
        stage.show();
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(15);
        sidebar.setPadding(new Insets(25, 15, 25, 15));
        sidebar.setPrefWidth(230);
        sidebar.setStyle("-fx-background-color: " + sidebarColor + ";");

        Label logo = new Label("CPU SCHEDULER");
        logo.setStyle("-fx-text-fill: " + purple + "; -fx-font-size: 19px; -fx-font-weight: bold;");

        Label menuTitle = new Label("MAIN MENU");
        menuTitle.setStyle("-fx-text-fill: #64748B; -fx-font-size: 11px; -fx-font-weight: bold;");

        Button dashboard = createNavButton("Dashboard");
        Button processesButton = createNavButton("Process Management");
        Button sjf = createNavButton("Preemptive SJF");
        Button acs = createNavButton("ACS Monitor");
        Button gantt = createNavButton("Gantt Chart");
        Button performance = createNavButton("Performance");

        dashboard.setOnAction(e -> showDashboard());
        processesButton.setOnAction(e -> showProcessManagement());
        sjf.setOnAction(e -> showSJF());
        acs.setOnAction(e -> showACS());
        gantt.setOnAction(e -> showGanttChart());
        performance.setOnAction(e -> showPerformance());

        sidebar.getChildren().addAll(
            logo,
            new Label(" "),
            menuTitle,
            dashboard,
            processesButton,
            sjf,
            acs,
            gantt,
            performance
        );

        return sidebar;
    }

    private Button createNavButton(String name) {
        Button button = new Button(name);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setPadding(new Insets(12));
        button.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #CBD5E1;" +
            "-fx-font-size: 13px;" +
            "-fx-cursor: hand;"
        );
        return button;
    }

    private VBox createHeader() {
        VBox header = new VBox(7);
        header.setPadding(new Insets(20, 30, 15, 30));

        pageTitle = new Label("Dashboard");
        pageTitle.setStyle("-fx-text-fill: " + textColor + "; -fx-font-size: 25px; -fx-font-weight: bold;");

        Label subtitle = new Label("CS241 - Operating System Assignment 2");
        subtitle.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 13px;");

        header.getChildren().addAll(pageTitle, subtitle);
        return header;
    }

    private VBox createDashboard() {
        VBox content = new VBox(25);
        content.setPadding(new Insets(20, 30, 30, 30));

        Label welcome = new Label("Welcome to CPU Scheduling Simulator");
        welcome.setStyle("-fx-text-fill: " + textColor + "; -fx-font-size: 22px; -fx-font-weight: bold;");

        Label description = new Label(
            "Simulate CPU scheduling algorithms and analyze their performance."
        );
        description.setStyle("-fx-text-fill: #94A3B8;");

        HBox cards = new HBox(15);

        VBox card1 = createCard("Processes", "—", "Awaiting input");
        VBox card2 = createCard("Algorithm", "—", "Not selected");
        VBox card3 = createCard("Simulation", "—", "Not started");
        VBox card4 = createCard("Results", "—", "Not available");

        cards.getChildren().addAll(card1, card2, card3, card4);

        for (var card : cards.getChildren()) {
            HBox.setHgrow(card, Priority.ALWAYS);
        }

        VBox information = new VBox(12);
        information.setPadding(new Insets(25));
        information.setStyle("-fx-background-color: " + cardColor + "; -fx-background-radius: 12;");

        Label sectionTitle = new Label("Simulation Overview");
        sectionTitle.setStyle("-fx-text-fill: " + textColor + "; -fx-font-size: 18px; -fx-font-weight: bold;");

        Label sectionDescription = new Label(
            "Start by adding processes. Then select a scheduling algorithm " +
            "and run the simulation to view the Gantt chart and performance metrics."
        );
        sectionDescription.setWrapText(true);
        sectionDescription.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 14px;");

        information.getChildren().addAll(sectionTitle, sectionDescription);
        content.getChildren().addAll(welcome, description, cards, information);

        return content;
    }

    private VBox createCard(String title, String value, String description) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.setMaxWidth(Double.MAX_VALUE);
        card.setStyle("-fx-background-color: " + cardColor + "; -fx-background-radius: 12;");

        Label cardTitle = new Label(title);
        cardTitle.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 13px;");

        Label cardValue = new Label(value);
        cardValue.setStyle("-fx-text-fill: " + purple + "; -fx-font-size: 25px; -fx-font-weight: bold;");

        Label cardDescription = new Label(description);
        cardDescription.setStyle("-fx-text-fill: #64748B; -fx-font-size: 11px;");

        card.getChildren().addAll(cardTitle, cardValue, cardDescription);
        return card;
    }

    private void showDashboard() {
        pageTitle.setText("Dashboard");
        mainLayout.setCenter(createDashboard());
    }

    private void showProcessManagement() {
        pageTitle.setText("Process Management");
        mainLayout.setCenter(new ProcessManagementView(processes));
    }

    private void showSJF() {
        pageTitle.setText("Preemptive SJF");

        mainLayout.setCenter(
            new SJFSchedulerView(
                processes,
                logs -> {
                    sjfExecutionLog = new ArrayList<>(logs);
                    lastAlgorithm = "Preemptive SJF";
                },
                result -> sjfResult = result
            )
        );
    }

    private void showACS() {
        pageTitle.setText("ACS Monitor");

        mainLayout.setCenter(
            new ACSSchedulerView(
                processes,
                logs -> {
                    acsExecutionLog = new ArrayList<>(logs);
                    lastAlgorithm = "ACS";
                },
                result -> acsResult = result
            )
        );
    }

    private void showPerformance() {
        pageTitle.setText("Performance"); //change
        mainLayout.setCenter(
            new PerformanceView(sjfResult, acsResult)
        );
    }
    private void showGanttChart() {
        pageTitle.setText("Gantt Chart");

        if (sjfExecutionLog.isEmpty() && acsExecutionLog.isEmpty()) {
            showPage(
                "Gantt Chart",
                "Please run an SJF or ACS simulation first."
            );
            return;
        }

        mainLayout.setCenter(
            new GanttChartView(
                sjfExecutionLog,
                acsExecutionLog,
                lastAlgorithm
            )
        );
    }

    private void showPage(String title, String description) {
        pageTitle.setText(title);

        VBox content = new VBox(15);
        content.setPadding(new Insets(30));

        Label heading = new Label(title);
        heading.setStyle("-fx-text-fill: " + textColor + "; -fx-font-size: 22px; -fx-font-weight: bold;");

        Label details = new Label(description);
        details.setWrapText(true);
        details.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 14px;");

        content.getChildren().addAll(heading, details);
        mainLayout.setCenter(content);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
