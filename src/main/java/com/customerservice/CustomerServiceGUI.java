
package com.customerservice;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerServiceGUI extends JFrame {

    private final DefaultTableModel agentModel =
        new DefaultTableModel(new String[]{"Agent Name"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

    private final DefaultTableModel queryModel =
        new DefaultTableModel(
            new String[]{"Customer Name", "Query", "Arrival Time", "Burst Time"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

    private final DefaultTableModel logModel =
        new DefaultTableModel(new String[]{
            "Query ID", "Customer", "Agent",
            "Start", "End", "Slice", "Remaining", "Status"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

    private final JTable agentTable = new JTable(agentModel);
    private final JTable queryTable = new JTable(queryModel);
    private final JTable logTable = new JTable(logModel);

    private final JTextField quantumField = new JTextField("3", 5);
    private final JTextArea metricsArea = new JTextArea(7, 30);
    private final GanttPanel ganttPanel = new GanttPanel();

    private JLabel agentCountValue;
    private JLabel queryCountValue;
    private JLabel completedValue;
    private JLabel totalTimeValue;

    private int nextQueryId = 1;

    private SchedulerResult lastResult;

    private final Color backgroundColor = new Color(245, 247, 251);
    private final Color accentColor = new Color(48, 91, 180);

    public CustomerServiceGUI() {
        setTitle("Customer Service Center | Round Robin Scheduler");
        setSize(1200, 780);
        setMinimumSize(new Dimension(900, 650));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        try {
            UIManager.setLookAndFeel(
                UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        buildInterface();
        addSampleData();
        updateSummaryCards();
    }

    private void buildInterface() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBackground(backgroundColor);
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        setContentPane(root);

        JPanel topPanel = new JPanel(new BorderLayout(0, 12));
        topPanel.setOpaque(false);

        JLabel heading = new JLabel("Customer Service Center");
        heading.setFont(new Font("SansSerif", Font.BOLD, 26));
        heading.setForeground(new Color(35, 45, 65));

        JLabel subtitle = new JLabel(
            "Round Robin Scheduling | Customer Query Processing");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(new Color(100, 110, 125));

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.add(heading);
        titlePanel.add(Box.createVerticalStrut(4));
        titlePanel.add(subtitle);

        topPanel.add(titlePanel, BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(1, 4, 12, 0));
        cards.setOpaque(false);

        agentCountValue = addSummaryCard(cards, "SERVICE AGENTS", "0");
        queryCountValue = addSummaryCard(cards, "CUSTOMER QUERIES", "0");
        completedValue = addSummaryCard(cards, "COMPLETED QUERIES", "0");
        totalTimeValue = addSummaryCard(cards, "SIMULATED TIME", "0 units");

        topPanel.add(cards, BorderLayout.CENTER);
        root.add(topPanel, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("SansSerif", Font.BOLD, 13));
        tabs.addTab("Setup", buildSetupPanel());
        tabs.addTab("Scheduling Results", buildResultsPanel());

        root.add(tabs, BorderLayout.CENTER);

        JButton runButton = new JButton("Run Scheduler");
        styleButton(runButton, accentColor);
        runButton.addActionListener(e -> runScheduler());

        JButton resetButton = new JButton("Reset Results");
        styleButton(resetButton, new Color(90, 100, 115));
        resetButton.addActionListener(e -> resetResults());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        bottom.setOpaque(false);
        bottom.add(new JLabel("Time Quantum:"));
        bottom.add(quantumField);
        bottom.add(runButton);
        bottom.add(resetButton);

        root.add(bottom, BorderLayout.SOUTH);
    }

    private JLabel addSummaryCard(
            JPanel parent, String title, String initialValue) {

        JPanel card = new JPanel(new BorderLayout(4, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(225, 230, 238)),
            BorderFactory.createEmptyBorder(14, 14, 14, 14)));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        titleLabel.setForeground(new Color(100, 110, 125));

        JLabel valueLabel = new JLabel(initialValue);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        valueLabel.setForeground(accentColor);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        parent.add(card);

        return valueLabel;
    }

    private void styleButton(JButton button, Color color) {
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setForeground(color);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private JPanel buildSetupPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 12, 12));
        panel.setBackground(backgroundColor);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 4, 4, 4));

        JPanel agentsPanel = createSectionPanel("Available Service Agents");
        configureTable(agentTable);
        agentsPanel.add(new JScrollPane(agentTable), BorderLayout.CENTER);

        JButton addAgent = new JButton("Add Agent");
        addAgent.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(
                this, "Enter agent name:");

            if (name == null) {
                return;
            }

            name = name.trim();

            if (name.isEmpty()) {
                showError("Agent name cannot be empty.");
                return;
            }

            for (int i = 0; i < agentModel.getRowCount(); i++) {
                if (name.equalsIgnoreCase(
                        String.valueOf(agentModel.getValueAt(i, 0)).trim())) {
                    showError("An agent with this name already exists.");
                    return;
                }
            }

            agentModel.addRow(new Object[]{name});
            updateSummaryCards();
        });

        JButton removeAgent = new JButton("Remove Selected");
        removeAgent.addActionListener(e -> {
            int row = agentTable.getSelectedRow();

            if (row >= 0) {
                agentModel.removeRow(agentTable.convertRowIndexToModel(row));
                updateSummaryCards();
            } else {
                showError("Select an agent to remove.");
            }
        });

        JPanel agentButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        agentButtons.setOpaque(false);
        agentButtons.add(addAgent);
        agentButtons.add(removeAgent);
        agentsPanel.add(agentButtons, BorderLayout.SOUTH);

        JPanel queriesPanel = createSectionPanel("Customer Queries");
        configureTable(queryTable);
        queriesPanel.add(new JScrollPane(queryTable), BorderLayout.CENTER);

        JButton addQuery = new JButton("Add Query");
        addQuery.addActionListener(e -> addQueryDialog());

        JButton removeQuery = new JButton("Remove Selected");
        removeQuery.addActionListener(e -> {
            int row = queryTable.getSelectedRow();

            if (row >= 0) {
                queryModel.removeRow(queryTable.convertRowIndexToModel(row));
                updateSummaryCards();
            } else {
                showError("Select a query to remove.");
            }
        });

        JPanel queryButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        queryButtons.setOpaque(false);
        queryButtons.add(addQuery);
        queryButtons.add(removeQuery);
        queriesPanel.add(queryButtons, BorderLayout.SOUTH);

        panel.add(agentsPanel);
        panel.add(queriesPanel);

        return panel;
    }

    private JPanel createSectionPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(title),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        return panel;
    }

    private void configureTable(JTable table) {
        table.setFillsViewportHeight(true);
        table.setRowHeight(28);
        table.setFont(new Font("SansSerif", Font.PLAIN, 12));
        table.setSelectionBackground(new Color(215, 229, 252));
        table.setSelectionForeground(Color.BLACK);
        table.setGridColor(new Color(230, 234, 240));
        table.setShowVerticalLines(false);
        table.setAutoCreateRowSorter(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 12));
        header.setBackground(new Color(235, 240, 248));
        header.setForeground(new Color(45, 55, 75));
        header.setReorderingAllowed(false);
    }

    private JPanel buildResultsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 10));
        panel.setBackground(backgroundColor);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 4, 4, 4));

        JPanel timelineSection = createSectionPanel(
            "Round Robin Execution Timeline");

        ganttPanel.setPreferredSize(new Dimension(700, 135));
        timelineSection.add(new JScrollPane(ganttPanel), BorderLayout.CENTER);

        JPanel logSection = createSectionPanel("Execution Log");
        configureTable(logTable);
        logTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        int[] widths = {75, 140, 120, 65, 65, 65, 85, 190};

        for (int i = 0; i < widths.length; i++) {
            logTable.getColumnModel().getColumn(i)
                .setPreferredWidth(widths[i]);
        }

        logSection.add(new JScrollPane(logTable), BorderLayout.CENTER);

        JPanel metricsPanel = createSectionPanel(
            "OS Scheduling Performance Metrics");

        metricsArea.setEditable(false);
        metricsArea.setEditable(false);
        metricsArea.setLineWrap(true);
        metricsArea.setWrapStyleWord(true);
        metricsArea.setFont(new Font("SansSerif", Font.PLAIN, 13));
        metricsArea.setForeground(new Color(45, 55, 72));
        metricsArea.setBackground(new Color(250, 251, 253));
        metricsArea.setMargin(new Insets(12, 12, 12, 12));
        metricsArea.setText(
            "Performance results will appear here after you run the scheduler.\n\n" +
            "Metrics include average waiting, turnaround and response times, " +
            "dispatch counts, total simulated time, and each agent's workload.");

        JScrollPane metricsScroll = new JScrollPane(metricsArea);
        metricsScroll.setBorder(BorderFactory.createLineBorder(new Color(225, 230, 238)));
        metricsScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        metricsPanel.add(metricsScroll, BorderLayout.CENTER);
        metricsPanel.setPreferredSize(new Dimension(320, 220));

        JSplitPane timelineLogSplit = new JSplitPane(
            JSplitPane.VERTICAL_SPLIT, timelineSection, logSection);
        timelineLogSplit.setResizeWeight(0.30);
        timelineLogSplit.setDividerSize(8);
        timelineLogSplit.setBorder(null);
        timelineLogSplit.setContinuousLayout(true);

        JSplitPane mainSplit = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT, timelineLogSplit, metricsPanel);
        mainSplit.setResizeWeight(0.70);
        mainSplit.setDividerSize(8);
        mainSplit.setBorder(null);
        mainSplit.setContinuousLayout(true);

        panel.add(mainSplit, BorderLayout.CENTER);

        return panel;
    }

    private void addQueryDialog() {
        JTextField customerField = new JTextField();
        JTextField descriptionField = new JTextField();
        JTextField arrivalField = new JTextField("0");
        JTextField burstField = new JTextField();

        JPanel form = new JPanel(new GridLayout(0, 1, 5, 5));
        form.add(new JLabel("Customer name:"));
        form.add(customerField);
        form.add(new JLabel("Query description:"));
        form.add(descriptionField);
        form.add(new JLabel("Arrival time (units, 0 or greater):"));
        form.add(arrivalField);
        form.add(new JLabel("Required service time (units):"));
        form.add(burstField);

        int choice = JOptionPane.showConfirmDialog(
            this, form, "Add Customer Query",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE);

        if (choice != JOptionPane.OK_OPTION) {
            return;
        }

        String customer = customerField.getText().trim();
        String description = descriptionField.getText().trim();

        if (customer.isEmpty() || description.isEmpty()) {
            showError("Customer name and query description are required.");
            return;
        }

        try {
            int arrival = Integer.parseInt(arrivalField.getText().trim());
            int burst = Integer.parseInt(burstField.getText().trim());

            if (arrival < 0) {
                showError("Arrival time cannot be negative.");
                return;
            }
            if (burst <= 0) {
                showError("Service time must be a positive whole number.");
                return;
            }

            queryModel.addRow(new Object[]{customer, description, arrival, burst});
            updateSummaryCards();

        } catch (NumberFormatException ex) {
            showError("Enter a valid whole number for service time.");
        }
    }

    private void addSampleData() {
        agentModel.addRow(new Object[]{"Agent A"});
        agentModel.addRow(new Object[]{"Agent B"});
        agentModel.addRow(new Object[]{"Agent C"});
        agentModel.addRow(new Object[]{"Agent D"});

        addSampleQuery("Alice", "Payment issue", 5);
        addSampleQuery("Bob", "Login problem", 3);
        addSampleQuery("Charlie", "Refund request", 7);
        addSampleQuery("Diana", "Account update", 4);
    }

    private void addSampleQuery(
            String customer, String description, int burst) {

        queryModel.addRow(new Object[]{customer, description, 0, burst});
    }

    private void runScheduler() {
        try {
            int quantum = Integer.parseInt(quantumField.getText().trim());

            if (quantum <= 0) {
                throw new IllegalArgumentException(
                    "Time quantum must be positive.");
            }

            List<Agent> agents = new ArrayList<>();

            for (int i = 0; i < agentModel.getRowCount(); i++) {
                String name = String.valueOf(
                    agentModel.getValueAt(i, 0)).trim();

                if (name.isEmpty()) {
                    throw new IllegalArgumentException(
                        "Agent names cannot be empty.");
                }

                agents.add(new Agent(i + 1, name));
            }

            List<CustomerQuery> queries = new ArrayList<>();

            for (int i = 0; i < queryModel.getRowCount(); i++) {
                String customer = String.valueOf(
                    queryModel.getValueAt(i, 0)).trim();

                String description = String.valueOf(
                    queryModel.getValueAt(i, 1)).trim();

                int arrival = Integer.parseInt(
                    String.valueOf(queryModel.getValueAt(i, 2)).trim());
                int burst = Integer.parseInt(
                    String.valueOf(queryModel.getValueAt(i, 3)).trim());

                if (customer.isEmpty() || description.isEmpty()) {
                    throw new IllegalArgumentException(
                        "Customer names and descriptions are required.");
                }

                if (arrival < 0) {
                    throw new IllegalArgumentException(
                        "All arrival times must be zero or greater.");
                }
                if (burst <= 0) {
                    throw new IllegalArgumentException(
                        "All burst times must be positive.");
                }

                queries.add(new CustomerQuery(
                    nextQueryId++, customer, description, arrival, burst));
            }

            if (agents.isEmpty()) {
                throw new IllegalArgumentException("Add at least one agent.");
            }

            if (queries.isEmpty()) {
                throw new IllegalArgumentException(
                    "Add at least one customer query.");
            }

            SchedulerResult result =
                new RoundRobinScheduler().schedule(queries, agents, quantum);

            lastResult = result;
            displayResult(result);
            updateSummaryCards();

        } catch (NumberFormatException ex) {
            showError("Arrival time, quantum, and burst time must be whole numbers.");
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void displayResult(SchedulerResult result) {
        logModel.setRowCount(0);

        for (String[] row : result.executionLog) {
            logModel.addRow(row);
        }

        StringBuilder text = new StringBuilder();

        text.append("PERFORMANCE OVERVIEW\n");
        text.append("────────────────────────────\n");
        text.append(String.format("Average waiting time     %.2f units%n", result.averageWaitingTime));
        text.append(String.format("Average turnaround time  %.2f units%n", result.averageTurnaroundTime));
        text.append(String.format("Average response time    %.2f units%n", result.averageResponseTime));
        text.append(String.format("Total simulated time     %d units%n", result.totalTime));
        text.append("\nSCHEDULER ACTIVITY\n");
        text.append("────────────────────────────\n");
        text.append(String.format("Scheduling dispatches    %d%n", result.executionLog.size()));
        text.append(String.format("Dispatch transitions     %d%n", result.contextSwitches));
        text.append("\nAGENT WORKLOADS\n");
        text.append("────────────────────────────\n");

        for (Agent agent : result.agents) {
            text.append(String.format("%s%n  Service time: %d units%n  Execution slices: %d%n\n",
                agent.getName(), agent.getTotalServiceTime(), agent.getQueriesHandled()));
        }

        text.append("Note: dispatch transitions are estimated. Actual OS context-switch overhead is not simulated.");

        metricsArea.setText(text.toString());
        metricsArea.setCaretPosition(0);

        ganttPanel.setExecutionLog(result.executionLog, result.totalTime);
    }

    private void updateSummaryCards() {
        agentCountValue.setText(String.valueOf(agentModel.getRowCount()));
        queryCountValue.setText(String.valueOf(queryModel.getRowCount()));

        int completed = 0;
        int totalTime = 0;

        if (lastResult != null) {
            completed = lastResult.completedQueries.size();
            totalTime = lastResult.totalTime;
        }

        completedValue.setText(String.valueOf(completed));
        totalTimeValue.setText(totalTime + " units");
    }

    private void resetResults() {
        lastResult = null;
        logModel.setRowCount(0);
        metricsArea.setText("Run the scheduler to view performance metrics.");
        ganttPanel.setExecutionLog(new ArrayList<>(), 0);
        updateSummaryCards();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(
            this, message, "Input Error", JOptionPane.ERROR_MESSAGE);
    }

    private static class GanttPanel extends JPanel {

        private List<String[]> entries = new ArrayList<>();
        private int totalTime;

        GanttPanel() {
            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(750, 135));
        }

        void setExecutionLog(List<String[]> log, int totalTime) {
            this.entries = new ArrayList<>(log);
            this.totalTime = totalTime;

            int requiredWidth = Math.max(750, entries.size() * 95 + 50);
            setPreferredSize(new Dimension(requiredWidth, 135));
            revalidate();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);

            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

            int left = 28;
            int top = 32;
            int barHeight = 42;

            if (entries.isEmpty() || totalTime <= 0) {
                g.setColor(new Color(110, 120, 135));
                g.setFont(new Font("SansSerif", Font.PLAIN, 13));
                g.setFont(new Font("SansSerif", Font.PLAIN, 13));
                g.drawString(
                    "Run the scheduler to see each query's execution slice and time interval.",
                    left, 62);
                g.dispose();
                return;
            }

            int availableWidth = Math.max(
                1, getPreferredSize().width - left - 25);

            double scale = availableWidth / (double) totalTime;

            g.setFont(new Font("SansSerif", Font.PLAIN, 10));

            for (String[] row : entries) {
                int start = Integer.parseInt(row[3]);
                int end = Integer.parseInt(row[4]);
                int queryId = Integer.parseInt(row[0]);

                int x = left + (int) Math.round(start * scale);
                int width = Math.max(
                    2, (int) Math.round((end - start) * scale));

                Color fill = Color.getHSBColor(
                    (queryId * 0.17f) % 1.0f, 0.45f, 0.88f);

                g.setColor(fill);
                g.fillRoundRect(x, top, width, barHeight, 7, 7);

                g.setColor(new Color(90, 100, 115));
                g.drawRoundRect(x, top, width, barHeight, 7, 7);

                if (width >= 35) {
                    g.setColor(Color.BLACK);
                    g.drawString("Q" + queryId, x + 5, top + 23);
                }

                g.setColor(new Color(70, 80, 95));
                g.drawLine(x, top + barHeight + 2,
                           x, top + barHeight + 7);

                g.drawString(String.valueOf(start),
                    x - 3, top + barHeight + 20);
            }

            g.setColor(new Color(70, 80, 95));
            int finalX = left + (int) Math.round(totalTime * scale);
            g.drawLine(finalX, top + barHeight + 2,
                       finalX, top + barHeight + 7);
            g.drawString(String.valueOf(totalTime),
                finalX - 5, top + barHeight + 20);

            g.setFont(new Font("SansSerif", Font.BOLD, 11));
            g.setColor(new Color(65, 75, 95));
            g.drawString("EXECUTION TIMELINE  •  Time (units)", left, 16);

            g.dispose();
        }
    }
}
