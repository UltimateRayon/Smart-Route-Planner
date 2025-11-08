package com.example.srp.ui;

import com.example.srp.io.MapParser;
import com.example.srp.models.Graph;
import com.example.srp.models.Vertex;
import com.example.srp.models.Edge;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class MainWindow extends JFrame {

    private JComboBox<String> mapSelector;
    private JButton selectButton;

    private JPanel graphContainer;
    private JLabel graphTitleLabel;
    private GraphPanel graphPanel;

    private VertexSelectionPanel vertexSelectionPanel;

    private JTextField busField;
    private JTextField startField;
    private JSlider hourSlider;
    private JLabel hourValueLabel;

    private final MapParser mapParser = new MapParser();

    public MainWindow() {
        setTitle("Smart Route Planner");
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ===== TOP PANEL =====
        JPanel topPanel = new JPanel(new BorderLayout());

        JLabel mainTitle = new JLabel("Smart Route Planner", SwingConstants.CENTER);
        mainTitle.setFont(new Font("Segoe UI", Font.BOLD, 40));
        mainTitle.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        topPanel.add(mainTitle, BorderLayout.NORTH);

        JPanel mapSelectPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
        JLabel mapLabel = new JLabel("Choose Map:");
        mapLabel.setFont(new Font("Segoe UI", Font.PLAIN, 24));

        mapSelector = new JComboBox<>(new String[]{"map-1", "map-2", "map-3"});
        mapSelector.setFont(new Font("Segoe UI", Font.PLAIN, 24));

        selectButton = new JButton("Load Map");
        selectButton.setFont(new Font("Segoe UI", Font.BOLD, 24));
        selectButton.setBackground(new Color(40, 80, 100));
        selectButton.setForeground(Color.WHITE);
        selectButton.setFocusPainted(false);

        mapSelectPanel.add(mapLabel);
        mapSelectPanel.add(mapSelector);
        mapSelectPanel.add(selectButton);
        topPanel.add(mapSelectPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);

        // ===== GRAPH PANEL =====
        graphContainer = new JPanel(new BorderLayout());
        graphTitleLabel = new JLabel("Map Visualization", SwingConstants.CENTER);
        graphTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        graphContainer.add(graphTitleLabel, BorderLayout.NORTH);

        graphPanel = new GraphPanel();
        graphContainer.add(graphPanel, BorderLayout.CENTER);

        // ===== VERTEX SELECTION PANEL =====
        vertexSelectionPanel = new VertexSelectionPanel();

        // ===== INPUT PANEL =====
        JPanel inputTopPanel = new JPanel(new GridLayout(4, 2, 5, 5));

        JLabel busLabel = new JLabel("Number of Buses:");
        busLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        busField = new JTextField();
        busField.setFont(new Font("Segoe UI", Font.PLAIN, 18));

        JLabel startLabel = new JLabel("Start Node:");
        startLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        startField = new JTextField();
        startField.setFont(new Font("Segoe UI", Font.PLAIN, 18));

        JLabel hourLabel = new JLabel("Hour (1-24):");
        hourLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));

        hourSlider = new JSlider(1, 24, 8); // default 8
        hourSlider.setMajorTickSpacing(6);
        hourSlider.setMinorTickSpacing(1);
        hourSlider.setPaintTicks(true);
        hourSlider.setPaintLabels(true);
        hourSlider.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        hourValueLabel = new JLabel("Selected: 8", SwingConstants.CENTER);
        hourValueLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        hourSlider.addChangeListener(e -> hourValueLabel.setText("Selected: " + hourSlider.getValue()));

        inputTopPanel.add(busLabel);
        inputTopPanel.add(busField);
        inputTopPanel.add(startLabel);
        inputTopPanel.add(startField);
        inputTopPanel.add(hourLabel);
        inputTopPanel.add(hourSlider);
        inputTopPanel.add(new JLabel()); // empty
        inputTopPanel.add(hourValueLabel);

        JLabel mandatoryTitle = new JLabel("Select Mandatory Nodes", SwingConstants.CENTER);
        mandatoryTitle.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        mandatoryTitle.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));

        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        rightPanel.add(inputTopPanel, BorderLayout.NORTH);
        rightPanel.add(mandatoryTitle, BorderLayout.CENTER);
        rightPanel.add(vertexSelectionPanel, BorderLayout.SOUTH);

        // ===== SPLIT PANE =====
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, graphContainer, rightPanel);
        splitPane.setDividerLocation(850);
        add(splitPane, BorderLayout.CENTER);

        // ===== BUTTON ACTION =====
        selectButton.addActionListener(e -> {
            String selectedMap = (String) mapSelector.getSelectedItem();
            loadMap(selectedMap);
        });
    }

    private void loadMap(String selectedMap) {
        try {
            Graph graph = mapParser.parse(selectedMap);

            // Update graph visualization
            graphPanel.setGraph(graph);
            graphTitleLabel.setText(selectedMap + " Visualization");

            // Pass vertex IDs to selection panel
            Set<String> vertexIds = new HashSet<>();
            for (Vertex v : graph.getVertices()) vertexIds.add(v.getId());
            vertexSelectionPanel.setVertices(vertexIds);

            // Refresh UI
            graphContainer.revalidate();
            graphContainer.repaint();

            // Read user inputs
            int numBuses = 0;
            try { numBuses = Integer.parseInt(busField.getText()); } catch (NumberFormatException ignored) {}
            String startNode = startField.getText();
            Set<String> mandatoryNodes = vertexSelectionPanel.getMandatoryVertices();

            int hour = hourSlider.getValue();

            // Here you can use the hour to get traffic multiplier later:
            // double multiplier = edge.getTraffic()[hour - 1];

            // Just show vertices, edges, mandatory nodes for now
            int totalEdges = 0;
            Set<String> countedEdges = new HashSet<>();
            for (Vertex vertex : graph.getVertices()) {
                for (Edge edge : graph.getNeighborEdge(vertex.getId())) {
                    String key = edge.getFrom() + "-" + edge.getTo();
                    String revKey = edge.getTo() + "-" + edge.getFrom();
                    if (!countedEdges.contains(key) && !countedEdges.contains(revKey)) {
                        totalEdges++;
                        countedEdges.add(key);
                        countedEdges.add(revKey);
                    }
                }
            }

            JOptionPane.showMessageDialog(this,
                    selectedMap + " loaded!\nVertices: " + graph.getVertices().size() +
                            "\nEdges: " + totalEdges +
                             String.join(", ", mandatoryNodes)
                            ,
                    "Map Loaded", JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Failed to load map: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainWindow().setVisible(true));
    }
}
