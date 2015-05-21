package com.xmage.launcher;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

/**
 *
 * @author BetaSteward
 */
public class SettingsDialog extends JDialog {

    private final JTabbedPane tabbedPane;
    private final JPanel buttonPanel;
    private final JPanel panel1;
    private final JPanel panel2;
    private final JPanel panel3;
    private final JTextField txtClientJavaOpt;
    private final JTextField txtServerJavaOpt;
    private final JCheckBox chkUseTorrent;
    private final JTextField txtXMageHome;
    private final JCheckBox chkShowClientConsole;
    private final JCheckBox chkShowServerConsole;
    private final JSpinner spnUpRate;
    private final JSpinner spnDownRate;
    
    public SettingsDialog() {
        ImageIcon icon = new ImageIcon(XMageLauncher.class.getResource("/icon-mage-flashed.png"));
        this.setIconImage(icon.getImage());

        setTitle("XMage Launcher Settings");
        setModalityType(ModalityType.APPLICATION_MODAL);
	setSize(500, 300);
        setBackground(Color.gray);
        setLocationRelativeTo(null);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleDone();
            }
        });
                
	setLayout(new BorderLayout());

        GridBagLayout layout;
        GridBagConstraints constraints;
        JLabel label;
        
        // Downloads panel
        panel1 = new JPanel();
        layout = new GridBagLayout();
        layout.columnWeights = new double[] {0, 1.0};
        panel1.setLayout(layout);
        
        constraints = new GridBagConstraints();
        constraints.insets = new Insets(10, 10, 10, 10);
        
        label = new JLabel("XMage Home:");
        constraints.anchor = GridBagConstraints.EAST;
        panel1.add(label, constraints);
        
        txtXMageHome = new JTextField();
        txtXMageHome.setText(Config.getXMageHome());
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.BOTH;
        panel1.add(txtXMageHome, constraints);

        label = new JLabel("Show Client Console:");
        constraints.anchor = GridBagConstraints.EAST;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        panel1.add(label, constraints);
        
        chkShowClientConsole = new JCheckBox();
        chkShowClientConsole.setSelected(Config.isShowClientConsole());
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.BOTH;
        panel1.add(chkShowClientConsole, constraints);

        label = new JLabel("Show Server Console:");
        constraints.anchor = GridBagConstraints.EAST;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        panel1.add(label, constraints);
        
        chkShowServerConsole = new JCheckBox();
        chkShowServerConsole.setSelected(Config.isShowServerConsole());
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.BOTH;
        panel1.add(chkShowServerConsole, constraints);

        // Java settings panel
        panel2 = new JPanel();
        layout = new GridBagLayout();
        layout.columnWeights = new double[] {0, 1.0};
        panel2.setLayout(layout);
        
        constraints = new GridBagConstraints();
        constraints.insets = new Insets(10, 10, 10, 10);
        
        label = new JLabel("Client java options:");
        constraints.anchor = GridBagConstraints.EAST;
        panel2.add(label, constraints);
        
        txtClientJavaOpt = new JTextField();
        txtClientJavaOpt.setText(Config.getClientJavaOpts());
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.BOTH;
        panel2.add(txtClientJavaOpt, constraints);
        
        label = new JLabel("Server java options:");
        constraints.anchor = GridBagConstraints.EAST;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        panel2.add(label, constraints);
        
        txtServerJavaOpt = new JTextField();
        txtServerJavaOpt.setText(Config.getServerJavaOpts());
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.BOTH;
        panel2.add(txtServerJavaOpt, constraints);
        
        // Torrent settings panel
        panel3 = new JPanel();
        layout = new GridBagLayout();
        layout.columnWeights = new double[] {0, 1.0};
        panel3.setLayout(layout);
        
        constraints = new GridBagConstraints();
        constraints.insets = new Insets(10, 10, 10, 10);

        label = new JLabel("Always use torrent:");
        constraints.anchor = GridBagConstraints.EAST;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        panel3.add(label, constraints);
        
        chkUseTorrent = new JCheckBox();
        chkUseTorrent.setSelected(Config.isUseTorrent());
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.BOTH;
        panel3.add(chkUseTorrent, constraints);
        
        label = new JLabel("Upload Rate (KB/s):");
        constraints.anchor = GridBagConstraints.EAST;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        panel3.add(label, constraints);
        
        SpinnerModel model = new SpinnerNumberModel(Config.getTorrentUpRate(), 0, 100, 1);
        spnUpRate = new JSpinner(model);
        constraints.gridwidth = 1;
        constraints.anchor = GridBagConstraints.WEST;
        panel3.add(spnUpRate, constraints);
        
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        panel3.add(Box.createHorizontalBox(), constraints);

        label = new JLabel("Download Rate (KB/s):");
        constraints.anchor = GridBagConstraints.EAST;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        panel3.add(label, constraints);
        
        model = new SpinnerNumberModel(Config.getTorrentDownRate(), 0, 100, 1);
        spnDownRate = new JSpinner(model);
        constraints.gridwidth = 1;
        constraints.anchor = GridBagConstraints.WEST;
       panel3.add(spnDownRate, constraints);
        
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        panel3.add(Box.createHorizontalBox(), constraints);
        
        // Setup tabs
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("XMage", panel1);
        tabbedPane.addTab("Java", panel2);
        tabbedPane.addTab("Torrent", panel3);
        add(tabbedPane, BorderLayout.CENTER);
        
        // Button panel
        buttonPanel = new javax.swing.JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
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
        Config.setShowClientConsole(this.chkShowClientConsole.isSelected());
        Config.setShowServerConsole(this.chkShowServerConsole.isSelected());
        Config.setUseTorrent(this.chkUseTorrent.isSelected());
        Config.setTorrentUpRate((Integer)spnUpRate.getValue());
        Config.setTorrentDownRate((Integer)spnDownRate.getValue());
        Config.saveProperties();
        dispose();
    }
    
}
