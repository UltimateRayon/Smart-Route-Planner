package com.example.srp.ui;

import com.example.srp.algorithms.balancing.RouteEvaluator;
import com.example.srp.algorithms.clustering.ClusterAssigner;
import com.example.srp.algorithms.clustering.GreedyBalancedAssigner;
import com.example.srp.algorithms.pathfinding.DistanceMatrixBuilder;
import com.example.srp.algorithms.pathfinding.PathCache;
import com.example.srp.algorithms.routing.NearestNeighborTSP;
import com.example.srp.algorithms.routing.TSPSolver;
import com.example.srp.algorithms.routing.TwoOptTSP;
import com.example.srp.io.MapParser;
import com.example.srp.models.*;
import com.example.srp.traffic.JsonTrafficStore;
import com.example.srp.traffic.TrafficStore;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class FreshMainWindow extends JFrame {
    private Graph currentGraph;
    private List<RouteInfo> currentRoutes;

    private JComboBox<String> mapCombo;
    private JButton loadBtn;
    private FreshGraphCanvas graphCanvas;
    private JTextField startNodeField;
    private JTextField numBusesField;
    private JSlider hourSlider;
    private JLabel hourLabel;
    private VertexButtonPanel vertexPanel;
    private JButton calculateBtn;
    private JLabel statusLabel;

    public FreshMainWindow() {
        setTitle("Smart Route Planner");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        add(createTopPanel(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);

        setVisible(true);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        panel.setBackground(new Color(40, 40, 50));

        JLabel title = new JLabel("Smart Route Planner");
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(Color.WHITE);

        JLabel mapLabel = new JLabel("Map:");
        mapLabel.setForeground(Color.WHITE);

        mapCombo = new JComboBox<>(new String[]{"map-1"});
        mapCombo.setPreferredSize(new Dimension(100, 30));

        loadBtn = new JButton("Load");
        loadBtn.setPreferredSize(new Dimension(80, 30));
        loadBtn.addActionListener(e -> loadMap());

        panel.add(title);
        panel.add(mapLabel);
        panel.add(mapCombo);
        panel.add(loadBtn);

        return panel;
    }

    private JPanel createMainPanel() {
        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Left: Graph
        graphCanvas = new FreshGraphCanvas();
        graphCanvas.setPreferredSize(new Dimension(700, 600));
        graphCanvas.setBackground(Color.WHITE);
        graphCanvas.setBorder(BorderFactory.createTitledBorder("Graph"));

        // Right: Controls
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));

        JPanel configPanel = createConfigPanel();
        JScrollPane configScroll = new JScrollPane(configPanel);
        configScroll.setBorder(BorderFactory.createTitledBorder("Configuration"));

        vertexPanel = new VertexButtonPanel();
        JScrollPane vertexScroll = new JScrollPane(vertexPanel);
        vertexScroll.setBorder(BorderFactory.createTitledBorder("Select Nodes"));

        JSplitPane rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, configScroll, vertexScroll);
        rightSplit.setDividerLocation(250);

        rightPanel.add(rightSplit, BorderLayout.CENTER);

        // Main split
        JSplitPane main_split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, graphCanvas, rightPanel);
        main_split.setDividerLocation(700);

        main.add(main_split, BorderLayout.CENTER);
        return main;
    }

    private JPanel createConfigPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(Color.WHITE);

        // Start node
        JPanel p1 = new JPanel(new BorderLayout(5, 0));
        p1.setMaximumSize(new Dimension(300, 40));
        p1.add(new JLabel("Start Node:"), BorderLayout.WEST);
        startNodeField = new JTextField("N1", 10);
        p1.add(startNodeField, BorderLayout.CENTER);
        panel.add(p1);
        panel.add(Box.createVerticalStrut(10));

        // Buses
        JPanel p2 = new JPanel(new BorderLayout(5, 0));
        p2.setMaximumSize(new Dimension(300, 40));
        p2.add(new JLabel("Number of Buses:"), BorderLayout.WEST);
        numBusesField = new JTextField("2", 10);
        p2.add(numBusesField, BorderLayout.CENTER);
        panel.add(p2);
        panel.add(Box.createVerticalStrut(10));

        // Hour
        JPanel p3 = new JPanel(new BorderLayout(5, 0));
        p3.setMaximumSize(new Dimension(300, 80));
        p3.setLayout(new BoxLayout(p3, BoxLayout.Y_AXIS));
        p3.add(new JLabel("Hour (0-23):"));
        hourSlider = new JSlider(0, 23, 8);
        hourSlider.setMajorTickSpacing(6);
        hourSlider.setMinorTickSpacing(1);
        hourSlider.setPaintTicks(true);
        hourSlider.setPaintLabels(true);
        hourLabel = new JLabel("8");
        hourSlider.addChangeListener(e -> hourLabel.setText(String.valueOf(hourSlider.getValue())));
        p3.add(hourSlider);
        p3.add(hourLabel);
        panel.add(p3);
        panel.add(Box.createVerticalStrut(20));

        // Calculate button
        calculateBtn = new JButton("Calculate Routes");
        calculateBtn.setMaximumSize(new Dimension(200, 50));
        calculateBtn.setFont(new Font("Arial", Font.BOLD, 14));
        calculateBtn.setBackground(new Color(76, 175, 80));
        calculateBtn.setForeground(Color.WHITE);
        calculateBtn.setFocusPainted(false);
        calculateBtn.addActionListener(e -> calculateRoutes());
        panel.add(calculateBtn);

        panel.add(Box.createVerticalGlue());
        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        statusLabel = new JLabel("Ready");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        panel.add(statusLabel, BorderLayout.WEST);
        return panel;
    }

    private void loadMap() {
        try {
            String mapName = (String) mapCombo.getSelectedItem();
            currentGraph = new MapParser().parse(mapName);
            graphCanvas.setGraph(currentGraph);

            Set<String> nodeIds = new HashSet<>();
            for (Vertex v : currentGraph.getAllVertices()) {
                nodeIds.add(v.getId());
            }
            vertexPanel.setNodes(nodeIds);

            statusLabel.setText("✓ Map loaded: " + mapName);
        } catch (Exception ex) {
            statusLabel.setText("✗ Error: " + ex.getMessage());
        }
    }

    private void calculateRoutes() {
        if (currentGraph == null) {
            statusLabel.setText("✗ Load map first");
            return;
        }

        try {
            String startNode = startNodeField.getText().trim();
            int numBuses = Integer.parseInt(numBusesField.getText().trim());
            int hour = hourSlider.getValue();
            Set<String> mandatoryNodes = vertexPanel.getSelectedNodes();

            if (mandatoryNodes.isEmpty()) {
                statusLabel.setText("✗ Select at least one node");
                return;
            }

            statusLabel.setText("⏳ Calculating...");
            calculateBtn.setEnabled(false);

            new SwingWorker<List<RouteInfo>, Void>() {
                @Override
                protected List<RouteInfo> doInBackground() throws Exception {
                    TrafficStore traffic = new JsonTrafficStore(currentGraph);
                    DistanceMatrixBuilder dmb = new DistanceMatrixBuilder(currentGraph, traffic, hour);
                    PathCache cache = dmb.build(new ArrayList<>(currentGraph.getAllVertices()));

                    ClusterAssigner assigner = new GreedyBalancedAssigner(cache, 0.5);
                    List<NodeCluster> clusters = assigner.assignNodes(
                            new ArrayList<>(mandatoryNodes), startNode, numBuses
                    );

                    TSPSolver tsp = new TwoOptTSP(cache, new NearestNeighborTSP(cache));
                    RouteEvaluator eval = new RouteEvaluator(cache);
                    List<RouteInfo> routes = new ArrayList<>();

                    for (NodeCluster cluster : clusters) {
                        List<String> tour = tsp.solveTSP(cluster.getAllNodes(), startNode);
                        routes.add(eval.evaluateRoute(cluster.getBusId(), tour, hour));
                    }

                    return routes;
                }

                @Override
                protected void done() {
                    try {
                        currentRoutes = get();
                        graphCanvas.setRoutes(currentRoutes);

                        RouteEvaluator eval = new RouteEvaluator(new PathCache());
                        double makespan = eval.calculateMakespan(currentRoutes);
                        double total = eval.calculateTotalDistance(currentRoutes);

                        statusLabel.setText(String.format(
                                "✓ Routes calculated | Makespan: %.2f km | Total: %.2f km",
                                makespan, total
                        ));
                    } catch (Exception ex) {
                        statusLabel.setText("✗ Error: " + ex.getMessage());
                    } finally {
                        calculateBtn.setEnabled(true);
                    }
                }
            }.execute();

        } catch (NumberFormatException ex) {
            statusLabel.setText("✗ Invalid input");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(FreshMainWindow::new);
    }
}