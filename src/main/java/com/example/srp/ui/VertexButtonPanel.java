package com.example.srp.ui;

import javax.swing.*;
import java.awt.*;
import java.util.*;

public class VertexButtonPanel extends JPanel {
    private Map<String, JToggleButton> buttons = new HashMap<>();
    private Set<String> selected = new HashSet<>();

    public VertexButtonPanel() {
        setLayout(new GridLayout(0, 3, 5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(Color.WHITE);
    }

    public void setNodes(Set<String> nodeIds) {
        removeAll();
        buttons.clear();
        selected.clear();

        for (String id : nodeIds) {
            JToggleButton btn = new JToggleButton(id);
            btn.setFont(new Font("Arial", Font.BOLD, 12));
            btn.addActionListener(e -> {
                if (btn.isSelected()) {
                    selected.add(id);
                    btn.setBackground(new Color(255, 67, 54));
                    btn.setForeground(Color.WHITE);
                } else {
                    selected.remove(id);
                    btn.setBackground(UIManager.getColor("Button.background"));
                    btn.setForeground(Color.BLACK);
                }
            });
            buttons.put(id, btn);
            add(btn);
        }

        revalidate();
        repaint();
    }

    public Set<String> getSelectedNodes() {
        return new HashSet<>(selected);
    }
}