package com.example.srp.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

public class VertexSelectionPanel extends JPanel {

    private JPanel boxesPanel;
    private Set<String> vertices = new HashSet<>();
    private Set<String> mandatoryVertices = new HashSet<>();

    public VertexSelectionPanel() {
        setLayout(new BorderLayout());
        boxesPanel = new JPanel();
        boxesPanel.setLayout(new GridLayout(0, 3, 5, 5)); // 3 columns
        JScrollPane scrollPane = new JScrollPane(boxesPanel);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void setVertices(Set<String> vertexIds) {
        vertices.clear();
        vertices.addAll(vertexIds);
        mandatoryVertices.clear();
        boxesPanel.removeAll();

        for (String id : vertices) {
            JButton btn = new JButton(id);
            btn.setBackground(Color.LIGHT_GRAY);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 20));
            btn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (mandatoryVertices.contains(id)) {
                        mandatoryVertices.remove(id);
                        btn.setBackground(Color.LIGHT_GRAY);
                    } else {
                        mandatoryVertices.add(id);
                        btn.setBackground(Color.RED);
                    }
                }
            });
            boxesPanel.add(btn);
        }

        revalidate();
        repaint();
    }

    public Set<String> getMandatoryVertices() {
        return new HashSet<>(mandatoryVertices);
    }
}
