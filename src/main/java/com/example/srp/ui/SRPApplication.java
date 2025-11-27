package com.example.srp.ui;

import com.example.srp.algorithms.balancing.LoadBalancer;
import com.example.srp.algorithms.balancing.RouteEvaluator;
import com.example.srp.algorithms.clustering.ClusterAssigner;
import com.example.srp.algorithms.clustering.GreedyBalancedAssigner;
import com.example.srp.algorithms.expansion.RouteExpander;
import com.example.srp.algorithms.pathfinding.DistanceMatrixBuilder;
import com.example.srp.algorithms.pathfinding.PathCache;
import com.example.srp.algorithms.routing.NearestNeighborTSP;
import com.example.srp.algorithms.routing.TSPSolver;
import com.example.srp.algorithms.routing.TwoOptTSP;
import com.example.srp.io.MapParser;
import com.example.srp.models.DetailedRoute;
import com.example.srp.models.Graph;
import com.example.srp.models.RouteInfo;
import com.example.srp.models.Vertex;
import com.example.srp.traffic.JsonTrafficStore;
import com.example.srp.traffic.TrafficStore;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class SRPApplication extends JFrame {

    // Card Layout Constants
    private static final String CARD_LOAD = "Load";
    private static final String CARD_INPUT = "Input";
    private static final String CARD_RESULT = "Result";

    // Colors
    private static final Color COL_BG_DARK = new Color(45, 45, 48);
    private static final Color COL_BG_LIGHT = new Color(62, 62, 66);
    private static final Color COL_TEXT = new Color(240, 240, 240);
    private static final Color COL_ACCENT = new Color(0, 122, 204);
    private static final Color COL_SUCCESS = new Color(28, 156, 88);

    // Components
    private JPanel mainPanel;
    private CardLayout cardLayout;
    private MapCanvas mapCanvasInput;
    private MapCanvas mapCanvasResult;

    // Logic Data
    private Graph currentGraph;
    private TrafficStore trafficStore;
    private List<DetailedRoute> calculatedRoutes;

    // Inputs
    private JComboBox<String> startNodeCombo;

    // New Checkbox UI Components
    private JPanel checkBoxPanel;
    private List<JCheckBox> nodeCheckboxes;

    private JSpinner busCountSpinner;
    private JSlider hourSlider;
    private JTextArea resultArea;

    public SRPApplication() {
        setTitle("Smart Route Planner");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        initUI();
    }

    private void initUI() {
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(createLoadPanel(), CARD_LOAD);
        mainPanel.add(createInputPanel(), CARD_INPUT);
        mainPanel.add(createResultPanel(), CARD_RESULT);

        add(mainPanel);
    }

    // --- Screen 1: Load Map ---
    private JPanel createLoadPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(COL_BG_DARK);

        JPanel box = new JPanel(new GridLayout(4, 1, 10, 10));
        box.setBorder(new EmptyBorder(30, 40, 30, 40));
        box.setBackground(COL_BG_LIGHT);

        JLabel title = new JLabel("Smart Route Planner", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(COL_TEXT);

        JLabel desc = new JLabel("Select or type map name (e.g., 'test_map')", SwingConstants.CENTER);
        desc.setForeground(new Color(200, 200, 200));

        // --- CHANGED: JComboBox instead of JTextField ---
        // Pre-fill with common map names, but allow typing
        String[] defaultMaps = {"map-1", "test_map", "city_map", "dense_grid"};
        JComboBox<String> mapNameCombo = new JComboBox<>(defaultMaps);
        mapNameCombo.setEditable(true); // Allow user to type new names
        mapNameCombo.setSelectedItem("");

        JButton loadBtn = new JButton("Load Map");
        loadBtn.setBackground(COL_ACCENT);
        loadBtn.setForeground(Color.WHITE);
        loadBtn.setFocusPainted(false);
        loadBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));

        loadBtn.addActionListener(e -> {
            String selectedMap = (String) mapNameCombo.getSelectedItem();
            if (selectedMap != null) {
                loadMap(selectedMap.trim());
            }
        });

        box.add(title);
        box.add(desc);
        box.add(mapNameCombo);
        box.add(loadBtn);

        panel.add(box);
        return panel;
    }

    // --- Screen 2: Input Configuration ---
    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Left: Map Visualization
        mapCanvasInput = new MapCanvas();

        // Right: Inputs
        JPanel inputForm = new JPanel(new GridBagLayout());
        inputForm.setBackground(COL_BG_DARK); // Dark background
        inputForm.setBorder(new EmptyBorder(20, 20, 20, 20));
        inputForm.setPreferredSize(new Dimension(380, 0)); // Slightly wider

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.gridx = 0;

        // 1. Bus Count
        gbc.gridy = 0; inputForm.add(createLabel("Number of Buses:"), gbc);
        busCountSpinner = new JSpinner(new SpinnerNumberModel(2, 1, 10, 1));
        styleSpinner(busCountSpinner);
        gbc.gridy = 1; inputForm.add(busCountSpinner, gbc);

        // 2. Start Node
        gbc.gridy = 2; inputForm.add(createLabel("Start Depot Node:"), gbc);
        startNodeCombo = new JComboBox<>();
        gbc.gridy = 3; inputForm.add(startNodeCombo, gbc);

        // 3. Hour Slider
        gbc.gridy = 4; inputForm.add(createLabel("Departure Hour (0-23):"), gbc);
        hourSlider = new JSlider(0, 23, 8);
        hourSlider.setMajorTickSpacing(6);
        hourSlider.setMinorTickSpacing(1);
        hourSlider.setPaintTicks(true);
        hourSlider.setPaintLabels(true);
        hourSlider.setBackground(COL_BG_DARK);
        hourSlider.setForeground(COL_TEXT);
        gbc.gridy = 5; inputForm.add(hourSlider, gbc);

        // 4. Mandatory Nodes (Header + Helper Buttons)
        gbc.gridy = 6;
        JPanel labelPanel = new JPanel(new BorderLayout());
        labelPanel.setBackground(COL_BG_DARK);
        labelPanel.add(createLabel("Select Mandatory Waypoints:"), BorderLayout.WEST);

        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        btnPanel.setBackground(COL_BG_DARK);

        JButton btnAll = new JButton("All");
        styleMiniButton(btnAll);
        btnAll.addActionListener(e -> setAllCheckboxes(true));

        JButton btnNone = new JButton("None");
        styleMiniButton(btnNone);
        btnNone.addActionListener(e -> setAllCheckboxes(false));

        btnPanel.add(btnAll);
        btnPanel.add(btnNone);
        labelPanel.add(btnPanel, BorderLayout.EAST);

        inputForm.add(labelPanel, gbc);

        // --- NEW CHECKBOX LIST IMPLEMENTATION ---
        checkBoxPanel = new JPanel();
        checkBoxPanel.setLayout(new BoxLayout(checkBoxPanel, BoxLayout.Y_AXIS));
        checkBoxPanel.setBackground(COL_BG_LIGHT); // Lighter gray for list background

        nodeCheckboxes = new ArrayList<>();

        JScrollPane scrollPane = new JScrollPane(checkBoxPanel);
        scrollPane.setPreferredSize(new Dimension(300, 200));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(new LineBorder(Color.GRAY));

        gbc.gridy = 7;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        inputForm.add(scrollPane, gbc);

        // 5. Calculate Button
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JButton calcBtn = new JButton("Calculate Optimized Routes");
        calcBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        calcBtn.setBackground(COL_SUCCESS);
        calcBtn.setForeground(Color.WHITE);
        calcBtn.setFocusPainted(false);
        calcBtn.setBorderPainted(false);
        calcBtn.setPreferredSize(new Dimension(0, 40));
        calcBtn.addActionListener(e -> performCalculation());
        gbc.gridy = 8; inputForm.add(calcBtn, gbc);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mapCanvasInput, inputForm);
        splitPane.setResizeWeight(0.7);
        splitPane.setDividerLocation(800);
        splitPane.setDividerSize(2);

        panel.add(splitPane, BorderLayout.CENTER);
        return panel;
    }

    // --- Screen 3: Results ---
    private JPanel createResultPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Left: Result Map
        mapCanvasResult = new MapCanvas();

        // Right: Stats
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        resultArea.setMargin(new Insets(10, 10, 10, 10));
        resultArea.setBackground(COL_BG_LIGHT);
        resultArea.setForeground(COL_TEXT);

        JScrollPane scrollStats = new JScrollPane(resultArea);
        scrollStats.setBorder(null);

        JPanel sidePanel = new JPanel(new BorderLayout());
        sidePanel.setBackground(COL_BG_DARK);

        JLabel resTitle = new JLabel("  Optimization Results", SwingConstants.LEFT);
        resTitle.setForeground(COL_TEXT);
        resTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        resTitle.setPreferredSize(new Dimension(0, 40));

        sidePanel.add(resTitle, BorderLayout.NORTH);
        sidePanel.add(scrollStats, BorderLayout.CENTER);

        JButton backBtn = new JButton("Start Over");
        backBtn.setBackground(new Color(200, 60, 60));
        backBtn.setForeground(Color.WHITE);
        backBtn.setFocusPainted(false);
        backBtn.addActionListener(e -> cardLayout.show(mainPanel, CARD_INPUT));
        sidePanel.add(backBtn, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mapCanvasResult, sidePanel);
        splitPane.setResizeWeight(0.7);
        splitPane.setDividerLocation(800);
        splitPane.setDividerSize(2);

        panel.add(splitPane, BorderLayout.CENTER);
        return panel;
    }

    // --- Helper Methods for Styling ---

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(COL_TEXT);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return l;
    }

    private void styleMiniButton(JButton btn) {
        btn.setMargin(new Insets(2, 5, 2, 5));
        btn.setFont(new Font("SansSerif", Font.PLAIN, 11));
        btn.setBackground(Color.GRAY);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
    }

    private void styleSpinner(JSpinner spinner) {
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            ((JSpinner.DefaultEditor)editor).getTextField().setHorizontalAlignment(JTextField.CENTER);
        }
    }

    // --- Logic Implementation ---

    private void loadMap(String mapName) {
        try {
            MapParser parser = new MapParser();
            currentGraph = parser.parse(mapName);

            // Build Traffic Store immediately
            trafficStore = new JsonTrafficStore(currentGraph);

            // Populate Start Node Combo
            Vector<String> nodeIds = new Vector<>();
            currentGraph.getAllVertices().forEach(v -> nodeIds.add(v.getId()));
            nodeIds.sort(String::compareTo);
            startNodeCombo.setModel(new DefaultComboBoxModel<>(nodeIds));

            // --- POPULATE CHECKBOXES (Styled) ---
            checkBoxPanel.removeAll();
            nodeCheckboxes.clear();

            for (String nodeId : nodeIds) {
                JCheckBox cb = new JCheckBox(nodeId);
                cb.setBackground(COL_BG_LIGHT); // Match panel
                cb.setForeground(COL_TEXT);     // White text for visibility
                cb.setFocusPainted(false);
                cb.setSelected(true);

                nodeCheckboxes.add(cb);
                checkBoxPanel.add(cb);
            }

            // Refresh UI
            checkBoxPanel.revalidate();
            checkBoxPanel.repaint();

            // Update Input Canvas
            mapCanvasInput.setGraph(currentGraph);

            // Switch screen
            cardLayout.show(mainPanel, CARD_INPUT);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading map: " + ex.getMessage() +
                    "\nEnsure map JSON is in resources/maps/", "Load Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setAllCheckboxes(boolean selected) {
        for(JCheckBox cb : nodeCheckboxes) {
            cb.setSelected(selected);
        }
    }

    private void performCalculation() {
        List<String> selectedNodes = new ArrayList<>();
        for (JCheckBox cb : nodeCheckboxes) {
            if (cb.isSelected()) {
                selectedNodes.add(cb.getText());
            }
        }

        String startNode = (String) startNodeCombo.getSelectedItem();

        if (selectedNodes.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select at least one mandatory node.");
            return;
        }

        JDialog loading = new JDialog(this, "Processing", true);
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(20, 20, 20, 20));
        p.add(new JLabel("Calculating routes... Please wait.", SwingConstants.CENTER), BorderLayout.CENTER);
        loading.add(p);
        loading.setSize(300, 100);
        loading.setLocationRelativeTo(this);
        loading.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                int buses = (Integer) busCountSpinner.getValue();
                int hour = hourSlider.getValue();

                List<Vertex> relevantVertices = new ArrayList<>();
                for(String id : currentGraph.vertices.keySet()) {
                    relevantVertices.add(currentGraph.vertices.get(id));
                }

                DistanceMatrixBuilder builder = new DistanceMatrixBuilder(currentGraph, trafficStore, hour);
                PathCache pathCache = builder.build(relevantVertices);

                ClusterAssigner assigner = new GreedyBalancedAssigner(pathCache);
                TSPSolver tsp = new TwoOptTSP(pathCache, new NearestNeighborTSP(pathCache));
                RouteEvaluator evaluator = new RouteEvaluator(pathCache);

                LoadBalancer balancer = new LoadBalancer(pathCache, assigner, tsp, evaluator);

                List<RouteInfo> routeInfos = balancer.rebalance(
                        assigner.assignNodes(selectedNodes, startNode, buses),
                        startNode,
                        hour,
                        5
                );

                RouteExpander expander = new RouteExpander(pathCache);
                calculatedRoutes = expander.expandRoutes(routeInfos);

                String report = balancer.generateBalanceReport(routeInfos);

                StringBuilder sb = new StringBuilder(report);
                sb.append("\n\n=== TURN BY TURN ===\n");
                for(DetailedRoute dr : calculatedRoutes) {
                    sb.append(dr.getTurnByTurnDirections()).append("\n");
                }

                resultArea.setText(sb.toString());
                resultArea.setCaretPosition(0);

                return null;
            }

            @Override
            protected void done() {
                loading.dispose();
                try {
                    get();
                    mapCanvasResult.setGraph(currentGraph);

                    // --- ANIMATE THE ROUTES ---
                    mapCanvasResult.animateRoutes(calculatedRoutes);

                    cardLayout.show(mainPanel, CARD_RESULT);
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(SRPApplication.this, "Calculation failed: " + e.getMessage());
                }
            }
        };

        worker.execute();
        loading.setVisible(true);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> new SRPApplication().setVisible(true));
    }
}