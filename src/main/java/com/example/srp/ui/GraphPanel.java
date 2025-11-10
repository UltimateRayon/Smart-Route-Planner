package com.example.srp.ui;

import com.example.srp.models.Graph;
import com.example.srp.models.Vertex;
import com.example.srp.models.Edge;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class GraphPanel extends JPanel {

    private Graph graph;
    private Map<String, Point> vertexPositions = new HashMap<>();
    private final int VERTEX_DIAMETER = 50;

    public GraphPanel() {
        setBackground(Color.WHITE);
    }

    public void setGraph(Graph graph) {
        this.graph = graph;

        // Only initialize positions if empty (first load)
        if (vertexPositions.isEmpty()) {
            initPositions();
            runForceDirectedLayout(500); // adjust iterations if needed
        }

        repaint();
    }

    private void initPositions() {
        if (graph == null) return;
        Random rand = new Random();
        int width = getWidth() > 0 ? getWidth() : 800;
        int height = getHeight() > 0 ? getHeight() : 600;
        for (Vertex v : graph.getAllVertices()) {
            int x = rand.nextInt(width - VERTEX_DIAMETER) + VERTEX_DIAMETER / 2;
            int y = rand.nextInt(height - VERTEX_DIAMETER) + VERTEX_DIAMETER / 2;
            vertexPositions.put(v.getId(), new Point(x, y));
        }
    }

    private void runForceDirectedLayout(int iterations) {
        if (graph == null) return;
        int width = getWidth() > 0 ? getWidth() : 800;
        int height = getHeight() > 0 ? getHeight() : 600;
        double area = width * height;
        double k = Math.sqrt(area / (double) graph.getAllVertices().size());
        double t = width / 10.0; // initial temperature

        for (int iter = 0; iter < iterations; iter++) {
            Map<String, Double> dx = new HashMap<>();
            Map<String, Double> dy = new HashMap<>();
            for (Vertex v : graph.getAllVertices()) {
                dx.put(v.getId(), 0.0);
                dy.put(v.getId(), 0.0);
            }

            // Repulsive forces
            for (Vertex v : graph.getAllVertices()) {
                Point p1 = vertexPositions.get(v.getId());
                for (Vertex u : graph.getAllVertices()) {
                    if (v == u) continue;
                    Point p2 = vertexPositions.get(u.getId());
                    double dxu = p1.x - p2.x;
                    double dyu = p1.y - p2.y;
                    double dist = Math.max(1.0, Math.sqrt(dxu * dxu + dyu * dyu));
                    double force = k * k / dist;
                    dx.put(v.getId(), dx.get(v.getId()) + dxu / dist * force);
                    dy.put(v.getId(), dy.get(v.getId()) + dyu / dist * force);
                }
            }

            // Attractive forces
            for (Vertex v : graph.getAllVertices()) {
                for (Edge e : graph.getNeighborEdge(v.getId())) {
                    Point p1 = vertexPositions.get(e.getFrom());
                    Point p2 = vertexPositions.get(e.getTo());
                    double dxu = p1.x - p2.x;
                    double dyu = p1.y - p2.y;
                    double dist = Math.max(1.0, Math.sqrt(dxu * dxu + dyu * dyu));
                    double force = dist * dist / k;
                    dx.put(e.getFrom(), dx.get(e.getFrom()) - dxu / dist * force);
                    dy.put(e.getFrom(), dy.get(e.getFrom()) - dyu / dist * force);
                    dx.put(e.getTo(), dx.get(e.getTo()) + dxu / dist * force);
                    dy.put(e.getTo(), dy.get(e.getTo()) + dyu / dist * force);
                }
            }

            // Update positions
            for (Vertex v : graph.getAllVertices()) {
                Point p = vertexPositions.get(v.getId());
                double dxt = dx.get(v.getId());
                double dyt = dy.get(v.getId());
                double len = Math.sqrt(dxt * dxt + dyt * dyt);
                if (len > 0) {
                    p.x += (int) (dxt / len * Math.min(len, t));
                    p.y += (int) (dyt / len * Math.min(len, t));
                }
                // keep inside panel
                p.x = Math.min(width - VERTEX_DIAMETER / 2, Math.max(VERTEX_DIAMETER / 2, p.x));
                p.y = Math.min(height - VERTEX_DIAMETER / 2, Math.max(VERTEX_DIAMETER / 2, p.y));
            }

            t *= 0.95; // cool temperature
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (graph == null) return;

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setStroke(new BasicStroke(2));
        g2.setFont(new Font("Segoe UI", Font.BOLD, 18));

        // Draw edges
        g2.setColor(Color.BLACK);
        for (Vertex v : graph.getAllVertices()) {
            for (Edge e : graph.getNeighborEdge(v.getId())) {
                Point p1 = vertexPositions.get(e.getFrom());
                Point p2 = vertexPositions.get(e.getTo());
                if (p1 != null && p2 != null) {
                    g2.drawLine(p1.x, p1.y, p2.x, p2.y);
                }
            }
        }

        // Draw vertices
        for (Vertex v : graph.getAllVertices()) {
            Point p = vertexPositions.get(v.getId());
            if (p != null) {
                g2.setColor(Color.ORANGE);
                g2.fillOval(p.x - VERTEX_DIAMETER / 2, p.y - VERTEX_DIAMETER / 2, VERTEX_DIAMETER, VERTEX_DIAMETER);
                g2.setColor(Color.BLACK);
                g2.drawOval(p.x - VERTEX_DIAMETER / 2, p.y - VERTEX_DIAMETER / 2, VERTEX_DIAMETER, VERTEX_DIAMETER);

                // Center text properly
                FontMetrics fm = g2.getFontMetrics();
                int w = fm.stringWidth(v.getId());
                int h = fm.getAscent();
                g2.drawString(v.getId(), p.x - w / 2, p.y + h / 2 - 4); // better vertical centering
            }
        }
    }
}
