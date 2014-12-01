package com.xmage.launcher;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

/**
 *
 * @author BetaSteward
 */
public class SettingsDialog extends JDialog {

    private JTabbedPane tabbedPane;
    private JPanel buttonPanel;
    private JPanel panel1;
    private JPanel panel2;
    //private JPanel panel3;
    private JTextField txtClientJavaOpt;
    private JTextField txtServerJavaOpt;
    private JCheckBox chkUseTorrent;
    private JTextField txtXMageHome;

    
    public SettingsDialog() {
        setTitle("XMage Launcher Settings");
        setModalityType(ModalityType.APPLICATION_MODAL);
	setSize(500, 200);
        setBackground(Color.gray);
        setLocationRelativeTo(null);

	setLayout(new BorderLayout());

        // Java setting panel
        panel1 = new JPanel();
        GridBagLayout layout = new GridBagLayout();
        layout.columnWeights = new double[] {0, 1.0};
        panel1.setLayout(layout);
        
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(10, 10, 10, 10);
        
        JLabel label = new JLabel( "Client java options:" );
        constraints.anchor = GridBagConstraints.EAST;
        panel1.add(label, constraints);
        
        txtClientJavaOpt = new JTextField();
        txtClientJavaOpt.setText(Config.getClientJavaOpts());
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.BOTH;
        panel1.add(txtClientJavaOpt, constraints);
        
        label = new JLabel( "Server java options:" );
        constraints.anchor = GridBagConstraints.EAST;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        panel1.add(label, constraints);
        
        txtServerJavaOpt = new JTextField();
        txtServerJavaOpt.setText(Config.getServerJavaOpts());
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.BOTH;
        panel1.add(txtServerJavaOpt, constraints);

        // Downloads panel
        panel2 = new JPanel();
        layout = new GridBagLayout();
        layout.columnWeights = new double[] {0, 1.0};
        panel2.setLayout(layout);
        
        constraints = new GridBagConstraints();
        constraints.insets = new Insets(10, 10, 10, 10);
        
        label = new JLabel( "XMage Home:" );
        constraints.anchor = GridBagConstraints.EAST;
        panel2.add(label, constraints);
        
        txtXMageHome = new JTextField();
        txtXMageHome.setText(Config.getXMageHome());
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.BOTH;
        panel2.add(txtXMageHome, constraints);
        
        label = new JLabel( "Always use torrent:" );
        constraints.anchor = GridBagConstraints.EAST;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        panel2.add(label, constraints);
        
        chkUseTorrent = new JCheckBox();
        chkUseTorrent.setSelected(Config.isUseTorrent());
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.BOTH;
        panel2.add(chkUseTorrent, constraints);
        
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Java Options", panel1);
        tabbedPane.addTab("Downloads", panel2);
        //tabbedPane.addTab("Page 3", panel3);
        add(tabbedPane, BorderLayout.CENTER);
        
        // Button panel
        buttonPanel = new javax.swing.JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        buttonPanel.add(Box.createHorizontalGlue());

        JButton btnDone = new JButton("Done");
        btnDone.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                handleDone();
            }
        });      
        buttonPanel.add(btnDone);

        add(buttonPanel, BorderLayout.PAGE_END);

    }
    
    private void handleDone() {
        Config.setClientJavaOpts(this.txtClientJavaOpt.getText());
        Config.setServerJavaOpts(this.txtServerJavaOpt.getText());
        Config.setXMageHome(this.txtXMageHome.getText());
        Config.setUseTorrent(this.chkUseTorrent.isSelected());
        Config.saveProperties();
        dispose();
    }
    
}
