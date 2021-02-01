package com.xmage.launcher;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;

/**
 * @author BetaSteward
 */
public class XMageConsole extends JFrame {

    private final JTextArea mainPanel;
    private final JScrollPane scrollPane;

    public XMageConsole(String title) {
        setTitle(title);
        mainPanel = new JTextArea(5, 50);
        mainPanel.setEditable(false);
        mainPanel.setForeground(Color.WHITE);
        mainPanel.setBackground(Color.BLACK);
        DefaultCaret caret = (DefaultCaret) mainPanel.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        scrollPane = new JScrollPane(mainPanel) {
            @Override
            public Dimension getPreferredSize() {
                Dimension size = super.getPreferredSize();
                Dimension lmPrefSize = getLayout().preferredLayoutSize(this);
                size.width = Math.max(size.width, lmPrefSize.width);
                size.height = Math.max(size.height, lmPrefSize.height);
                return size;
            }
        };
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        add(scrollPane);
        setPreferredSize(new Dimension(800, 400));
        pack();

    }

    public void start(Process p) {
        StreamGobbler outGobbler = new StreamGobbler(p.getInputStream(), mainPanel);
        outGobbler.start();
    }

}
