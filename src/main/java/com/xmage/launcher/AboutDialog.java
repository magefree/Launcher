package com.xmage.launcher;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author BetaSteward
 */
public class AboutDialog extends JDialog {

    public AboutDialog() {
        ImageIcon icon = new ImageIcon(XMageLauncher.class.getResource("/icon-mage-flashed.png"));
        this.setIconImage(icon.getImage());
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        setModalityType(ModalityType.APPLICATION_MODAL);
        setTitle("About XMage Launcher");
        setLocationRelativeTo(null);
        pack();
        setSize(300, 200);

        add(Box.createRigidArea(new Dimension(0, 10)));

        ImageIcon logo = new ImageIcon(new ImageIcon(XMageLauncher.class.getResource("/label-xmage.png")).getImage().getScaledInstance(150, 75, Image.SCALE_SMOOTH));
        JLabel label = new JLabel(logo);
        label.setAlignmentX(0.5f);
        add(label);

        add(Box.createRigidArea(new Dimension(0, 10)));

        JLabel name = new JLabel("XMage Launcher Version " + Config.getInstance().getVersion());
        name.setFont(new Font("Serif", Font.BOLD, 13));
        name.setAlignmentX(0.5f);
        add(name);

        add(Box.createRigidArea(new Dimension(0, 50)));

        JButton close = new JButton("Close");
        close.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        close.setAlignmentX(0.5f);
        add(close);

    }

}
