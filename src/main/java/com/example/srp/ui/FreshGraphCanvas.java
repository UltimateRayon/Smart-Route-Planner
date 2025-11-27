package com.example.srp.ui;

import com.example.srp.models.*;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class FreshGraphCanvas extends JPanel {
    private Graph graph;
    private List<RouteInfo> routes;
    private Map<String, Point> positions = new HashMap<>();
    private Map<Integer, Color> busColors = new HashMap<>();

    public FreshGraphCanvas() {
        initColors();
    }

    private void initColors() {
        busColors.put(0, new Color(255, 67, 54));     // Red
        busColors.put(1, new Color(66, 133, 244));    // Blue
        busColors.put(2, new Color(52, 168, 83));     // Green
        busColors.put(3, new Color(251, 188, 4));     // Yellow
        busColors.put(4, new Color(156, 39, 176));    // Purple
        busColors.put(5, new Color(0, 188, 212));     // Cyan
    }

    public void setGraph(Graph g) {
        this.graph = g;
        this.routes = null;
        calculatePositions();
        repaint();
    }

    public void setRoutes(List<RouteInfo> r) {
        this.routes = r;
        repaint();
    }

    private void calculatePositions() {
        if (graph == null) return;

        positions.clear();
        double minX = Double.MAX_VALUE, maxX = -Double.MAX_VALUE;
        double minY = Double.MAX_VALUE, maxY = -Double.MAX_VALUE;

        // Find bounds
        for (Vertex v : graph.getAllVertices()) {
            minX = Math.min(minX, v.getX());
            maxX = Math.max(maxX, v.getX());
            minY = Math.min(minY, v.getY());
            maxY = Math.max(maxY, v.getY());
        }

        double w = getWidth() - 100;
        double h = getHeight() - 100;
        if (w <= 0) w = 600;
        if (h <= 0) h = 500;

        double gw = maxX - minX;
        double gh = maxY - minY;
        double scale = Math.min(w / gw, h / gh);

        for (Vertex v : graph.getAllVertices()) {
            int x = (int) ((v.getX() - minX) * scale + 50);
            int y = (int) ((v.getY() - minY) * scale + 50);
            positions.put(v.getId(), new Point(x, y));
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (graph == null) return;

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw edges
        drawEdges(g2);

        // Draw vertices
        drawVertices(g2);

        // Draw legend if routes exist
        if (routes != null && !routes.isEmpty()) {
            drawLegend(g2);
        }
    }

    private void drawEdges(Graphics2D g2) {
        Set<String> drawn = new HashSet<>();
        Map<String, Integer> edgeRoute = buildEdgeRouteMap();

        for (Vertex v : graph.getAllVertices()) {
            for (Edge e : graph.getNeighborEdge(v.getId())) {
                String key = e.getFrom().compareTo(e.getTo()) < 0
                        ? e.getFrom() + "-" + e.getTo()
                        : e.getTo() + "-" + e.getFrom();

                if (drawn.contains(key)) continue;
                drawn.add(key);

                Point p1 = positions.get(e.getFrom());
                Point p2 = positions.get(e.getTo());

                if (p1 == null || p2 == null) continue;

                if (edgeRoute.containsKey(key)) {
                    // Route edge - thick and colored
                    int routeId = edgeRoute.get(key);
                    g2.setColor(busColors.getOrDefault(routeId, Color.GRAY));
                    g2.setStroke(new BasicStroke(4));
                } else {
                    // Normal edge - thin and dark
                    g2.setColor(new Color(100, 100, 100));
                    g2.setStroke(new BasicStroke(1.5f));
                }

                g2.drawLine(p1.x, p1.y, p2.x, p2.y);
            }
        }
    }

    private void drawVertices(Graphics2D g2) {
        for (Vertex v : graph.getAllVertices()) {
            Point p = positions.get(v.getId());
            if (p == null) continue;

            // Draw circle
            g2.setColor(new Color(255, 200, 0));
            g2.fillOval(p.x - 20, p.y - 20, 40, 40);
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(2));
            g2.drawOval(p.x - 20, p.y - 20, 40, 40);

            // Draw label
            g2.setColor(Color.BLACK);
            g2.setFont(new Font("Arial", Font.BOLD, 12));
            FontMetrics fm = g2.getFontMetrics();
            String id = v.getId();
            int sw = fm.stringWidth(id);
            int sh = fm.getAscent();
            g2.drawString(id, p.x - sw / 2, p.y + sh / 2 - 2);
        }
    }

    private void drawLegend(Graphics2D g2) {
        int x = 10;
        int y = getHeight() - (routes.size() * 25 + 30);

        g2.setColor(new Color(255, 255, 255, 240));
        g2.fillRect(x, y, 120, routes.size() * 25 + 20);
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(1));
        g2.drawRect(x, y, 120, routes.size() * 25 + 20);

        g2.setFont(new Font("Arial", Font.BOLD, 11));
        g2.drawString("Legend:", x + 5, y + 15);

        for (RouteInfo route : routes) {
            y += 25;
            g2.setColor(busColors.getOrDefault(route.getBusId(), Color.GRAY));
            g2.fillRect(x + 5, y - 12, 15, 15);
            g2.setColor(Color.BLACK);
            g2.drawRect(x + 5, y - 12, 15, 15);
            g2.setFont(new Font("Arial", Font.PLAIN, 10));
            g2.drawString("Bus " + route.getBusId(), x + 25, y);
        }
    }

    private Map<String, Integer> buildEdgeRouteMap() {
        Map<String, Integer> map = new HashMap<>();
        if (routes == null) return map;

        for (RouteInfo route : routes) {
            List<String> tour = route.getTour();
            for (int i = 0; i < tour.size() - 1; i++) {
                String from = tour.get(i);
                String to = tour.get(i + 1);
                String key = from.compareTo(to) < 0 ? from + "-" + to : to + "-" + from;
                map.put(key, route.getBusId());
            }
        }

        return map;
    }
}