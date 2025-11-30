package com.example.srp.ui;

import com.example.srp.models.DetailedRoute;
import com.example.srp.models.Edge;
import com.example.srp.models.Graph;
import com.example.srp.models.Vertex;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Comparator;

public class MapCanvas extends JPanel {

    private Graph graph;
    private List<DetailedRoute> routes;
    private Map<String, Point> screenCoords;

    // Tracks which buses use a specific edge (for parallel line drawing)
    private Map<String, List<Integer>> segmentUsageMap;

    // Set of bus IDs that the user has chosen to hide
    private Set<Integer> hiddenBuses;

    // UI Components
    private JPanel legendPanel;

    // Animation Controls
    private int animationStep = Integer.MAX_VALUE;
    private Timer animationTimer;

    private final Color[] BUS_COLORS = {
            new Color(220, 20, 60),   // Crimson Red
            new Color(30, 144, 255),  // Dodger Blue
            new Color(50, 205, 50),   // Lime Green
            new Color(255, 165, 0),   // Orange
            new Color(153, 50, 204),  // Dark Orchid
            new Color(0, 191, 255),   // Deep Sky Blue
            new Color(255, 105, 180), // Hot Pink
            new Color(139, 69, 19)    // Saddle Brown
    };

    public MapCanvas() {
        this.screenCoords = new HashMap<>();
        this.segmentUsageMap = new HashMap<>();
        this.hiddenBuses = new HashSet<>();

        setBackground(Color.WHITE);
        setBorder(BorderFactory.createLineBorder(Color.GRAY));

        setLayout(new GridBagLayout());

        initLegendPanel();
    }

    private void initLegendPanel() {
        legendPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                // Semi-transparent white background
                g.setColor(new Color(255, 255, 255, 230));
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g);

                // Border
                g.setColor(Color.GRAY);
                g.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
            }
        };
        legendPanel.setLayout(new BoxLayout(legendPanel, BoxLayout.Y_AXIS));
        legendPanel.setOpaque(false); // Let custom paintComponent handle bg
        legendPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        legendPanel.setVisible(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.SOUTHWEST; // Position: BOTTOM LEFT
        gbc.insets = new Insets(10, 10, 10, 10);   // Margin from edges

        add(legendPanel, gbc);
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
        this.routes = null;
        this.segmentUsageMap.clear();
        this.hiddenBuses.clear();
        this.legendPanel.setVisible(false);
        stopAnimation();
        repaint();
    }

    // Initial route
    public void setRoutes(List<DetailedRoute> routes) {
        stopAnimation();
        this.routes = routes;
        this.animationStep = Integer.MAX_VALUE; // Show everything
        this.hiddenBuses.clear(); // Reset hidden state on new calculation

        rebuildLegend();
        analyzeSegmentUsage();
        repaint();
    }

    // Animated set (draws edges one by one)
    public void animateRoutes(List<DetailedRoute> routes) {
        stopAnimation();
        this.routes = routes;
        this.animationStep = 0;
        this.hiddenBuses.clear();

        rebuildLegend();
        analyzeSegmentUsage();

        // Calculate max steps needed
        int maxLen = 0;
        for(DetailedRoute r : routes) {
            maxLen = Math.max(maxLen, r.getFullNodeSequence().size());
        }
        final int finalMaxLen = maxLen;

        // Timer to increment step every 35ms
        animationTimer = new Timer(350, e -> { // Delay time
            animationStep++;
            repaint();
            if (animationStep >= finalMaxLen) {
                ((Timer)e.getSource()).stop();
            }
        });
        animationTimer.start();
    }

    private void rebuildLegend() {
        legendPanel.removeAll();

        if (routes == null || routes.isEmpty()) {
            legendPanel.setVisible(false);
            return;
        }

        JLabel title = new JLabel("Route Legend");
        title.setFont(new Font("SansSerif", Font.BOLD, 12));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        legendPanel.add(title);
        legendPanel.add(Box.createVerticalStrut(5));

        List<DetailedRoute> sortedRoutes = new ArrayList<>(routes);
        sortedRoutes.sort(Comparator.comparingInt(DetailedRoute::getBusId));

        for (DetailedRoute route : sortedRoutes) {
            int busId = route.getBusId();
            Color color = BUS_COLORS[busId % BUS_COLORS.length];

            JCheckBox cb = new JCheckBox("Bus " + busId);
            cb.setSelected(true);
            cb.setOpaque(false);
            cb.setFocusPainted(false);
            cb.setFont(new Font("SansSerif", Font.PLAIN, 12));
            cb.setAlignmentX(Component.LEFT_ALIGNMENT);

            // Custom Icon to show color
            cb.setIcon(createColorIcon(color, false));
            cb.setSelectedIcon(createColorIcon(color, true));

            cb.addActionListener(e -> {
                if (cb.isSelected()) {
                    hiddenBuses.remove(busId);
                } else {
                    hiddenBuses.add(busId);
                }
                // Re-calculate overlaps so lines shift if a neighbor is hidden
                analyzeSegmentUsage();
                repaint();
            });

            legendPanel.add(cb);
        }

        legendPanel.setVisible(true);
        legendPanel.revalidate();
    }

    // Helper to create checkbox icons
    private Icon createColorIcon(Color c, boolean selected) {
        int size = 12;
        return new Icon() {
            public int getIconWidth() { return size + 6; } // Extra spacing
            public int getIconHeight() { return size; }
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw Box
                if (selected) {
                    g2.setColor(Color.LIGHT_GRAY); // Checked box with Color
                } else {
                    g2.setColor(Color.GREEN); // Unchecked box
                }
                g2.drawRect(x, y, size, size);

                // Draw Inner Color
                if (selected) {
                    g2.setColor((Color) ((JComponent)c).getClientProperty("busColor")); // Use bound color
                }

                // Draw Color Line next to box
                g2.setColor(MapCanvas.this.BUS_COLORS[
                        Integer.parseInt(((JCheckBox)c).getText().replace("Bus ", "")) % BUS_COLORS.length
                        ]);
                g2.setStroke(new BasicStroke(3f));
                g2.drawLine(x + size + 4, y + size/2, x + size + 14, y + size/2);
            }
        };
    }

    private void stopAnimation() {
        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }
    }

    private void analyzeSegmentUsage() {
        segmentUsageMap.clear();
        if (routes == null) return;

        for (DetailedRoute route : routes) {
            // Skip hidden buses in the overlap calculation
            if (hiddenBuses.contains(route.getBusId())) continue;

            List<String> nodes = route.getFullNodeSequence();
            for (int i = 0; i < nodes.size() - 1; i++) {
                String u = nodes.get(i);
                String v = nodes.get(i + 1);
                String key = u.compareTo(v) < 0 ? u + "-" + v : v + "-" + u;
                segmentUsageMap.putIfAbsent(key, new ArrayList<>());
                segmentUsageMap.get(key).add(route.getBusId());
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if (graph == null || graph.getAllVertices().isEmpty()) {
            g2.setColor(Color.GRAY);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 16));
            g2.drawString("No Map Loaded", getWidth() / 2 - 50, getHeight() / 2);
            return;
        }

        calculateScreenCoordinates();

        // 1. Draw Base Infrastructure
        g2.setStroke(new BasicStroke(1.5f));
        g2.setColor(new Color(230, 230, 230));
        for (List<Edge> edges : graph.adjList.values()) {
            for (Edge e : edges) {
                drawSimpleEdge(g2, e.getFrom(), e.getTo());
            }
        }

        // 2. Draw Routes (Animated Limit applied)
        if (routes != null) {
            drawOffsetRoutes(g2);
        }

        // 3. Draw Vertices
        drawVertices(g2);
    }

    private void drawOffsetRoutes(Graphics2D g2) {
        g2.setStroke(new BasicStroke(2.5f));

        for (DetailedRoute route : routes) {
            // Skip drawing if bus is hidden
            if (hiddenBuses.contains(route.getBusId())) continue;

            g2.setColor(BUS_COLORS[route.getBusId() % BUS_COLORS.length]);
            List<String> sequence = route.getFullNodeSequence();

            int drawLimit = Math.min(sequence.size() - 1, animationStep);

            for (int i = 0; i < drawLimit; i++) {
                String u = sequence.get(i);
                String v = sequence.get(i + 1);

                Point p1 = screenCoords.get(u);
                Point p2 = screenCoords.get(v);

                if (p1 == null || p2 == null) continue;

                String key = u.compareTo(v) < 0 ? u + "-" + v : v + "-" + u;
                List<Integer> busesOnSegment = segmentUsageMap.getOrDefault(key, Collections.emptyList());

                int totalBuses = busesOnSegment.size();
                int myIndex = busesOnSegment.indexOf(route.getBusId());

                double offsetStep = 4.0;
                double centerOffset = (totalBuses - 1) * offsetStep / 2.0;
                double currentOffset = (myIndex * offsetStep) - centerOffset;

                double dx = p2.x - p1.x;
                double dy = p2.y - p1.y;
                double length = Math.sqrt(dx * dx + dy * dy);

                if (length == 0) continue;

                double ux = dx / length;
                double uy = dy / length;
                double perpX = -uy;
                double perpY = ux;

                double x1 = p1.x + perpX * currentOffset;
                double y1 = p1.y + perpY * currentOffset;
                double x2 = p2.x + perpX * currentOffset;
                double y2 = p2.y + perpY * currentOffset;

                g2.draw(new Line2D.Double(x1, y1, x2, y2));
            }
        }
    }

    private void drawVertices(Graphics2D g2) {
        for (Vertex v : graph.getAllVertices()) {
            Point p = screenCoords.get(v.getId());
            if (p == null) continue;

            boolean isStart = isStartNode(v.getId());
            boolean isWaypoint = isWaypoint(v.getId());

            int size = isStart ? 14 : (isWaypoint ? 10 : 6);
            int offset = size / 2;

            if (isStart) g2.setColor(Color.BLACK);
            else if (isWaypoint) g2.setColor(Color.DARK_GRAY);
            else g2.setColor(Color.WHITE);

            g2.fill(new Ellipse2D.Double(p.x - offset, p.y - offset, size, size));

            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(isStart || isWaypoint ? 2.0f : 1.0f));
            g2.draw(new Ellipse2D.Double(p.x - offset, p.y - offset, size, size));

            drawLabel(g2, v.getId(), p.x, p.y - offset - 4, isStart || isWaypoint);
        }
    }

    private void drawLabel(Graphics2D g2, String text, int x, int y, boolean isImportant) {
        g2.setFont(new Font("SansSerif", isImportant ? Font.BOLD : Font.PLAIN, 11));
        FontMetrics fm = g2.getFontMetrics();
        int w = fm.stringWidth(text);
        int h = fm.getHeight();

        g2.setColor(new Color(255, 255, 255, 200));
        g2.fillRect(x - w/2 - 2, y - h + 2, w + 4, h);

        g2.setColor(Color.BLACK);
        g2.drawString(text, x - w / 2, y);
    }

    private void drawSimpleEdge(Graphics2D g2, String fromId, String toId) {
        Point p1 = screenCoords.get(fromId);
        Point p2 = screenCoords.get(toId);
        if (p1 != null && p2 != null) {
            g2.draw(new Line2D.Double(p1.x, p1.y, p2.x, p2.y));
        }
    }

    // Responsive co-ordinates calculation
    private void calculateScreenCoordinates() {
        screenCoords.clear();
        double minX = Double.MAX_VALUE, maxX = -Double.MAX_VALUE;
        double minY = Double.MAX_VALUE, maxY = -Double.MAX_VALUE;

        // 1. Find Data Bounds
        for (Vertex v : graph.getAllVertices()) {
            if (v.getX() < minX) minX = v.getX();
            if (v.getX() > maxX) maxX = v.getX();
            if (v.getY() < minY) minY = v.getY();
            if (v.getY() > maxY) maxY = v.getY();
        }

        double padding = 40.0;
        double panelW = getWidth() - (padding * 2);
        double panelH = getHeight() - (padding * 2);

        double dataW = maxX - minX;
        double dataH = maxY - minY;

        if (dataW == 0) dataW = 1;
        if (dataH == 0) dataH = 1;

        // 2. Calculate Scale
        double scaleX = panelW / dataW;
        double scaleY = panelH / dataH;

        // Base scale keeps aspect ratio (fits within the tighter dimension)
        double baseScale = Math.min(scaleX, scaleY);

        // Apply stretch: Try to stretch Y by 1.5x, but don't exceed the actual panel height capability
        double finalScaleX = baseScale;
        double finalScaleY = Math.min(baseScale * 1.5, scaleY);

        // 3. Calculate actual drawn size
        double drawnW = dataW * finalScaleX;
        double drawnH = dataH * finalScaleY;

        // 4. Calculate Offset to Center
        double offsetX = (getWidth() - drawnW) / 2.0;
        double offsetY = (getHeight() - drawnH) / 2.0;

        // 5. Transform points
        for (Vertex v : graph.getAllVertices()) {
            int screenX = (int) ((v.getX() - minX) * finalScaleX + offsetX);
            int screenY = (int) ((maxY - v.getY()) * finalScaleY + offsetY);

            screenCoords.put(v.getId(), new Point(screenX, screenY));
        }
    }

    private boolean isWaypoint(String nodeId) {
        if (routes == null) return false;
        for (DetailedRoute r : routes) {
            if (r.getWaypoints().contains(nodeId)) return true;
        }
        return false;
    }

    private boolean isStartNode(String nodeId) {
        if (routes == null || routes.isEmpty()) return false;
        return routes.get(0).getWaypoints().get(0).equals(nodeId);
    }
}