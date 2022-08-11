package com.xmage.launcher;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Objects;
import java.util.ResourceBundle;

/**
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
    private final JTextField txtXMageHome;
    private final JCheckBox chkShowClientConsole;
    private final JCheckBox chkShowServerConsole;
    private final JCheckBox chkUseSystemJava;
    private final JCheckBox chkServerTestMode;
    private final JSpinner spnGuiSize;
    private final JSpinner spnClientDelay;
    private final JComboBox<XMageBranch> cmbXMageBranch;
    private final ResourceBundle messages;


    public SettingsDialog(ResourceBundle messages) {
        this.messages = messages;

        ImageIcon icon = new ImageIcon(Objects.requireNonNull(XMageLauncher.class.getResource("/icon-mage-flashed.png")));
        this.setIconImage(icon.getImage());

        Font defaultFont = new Font("SansSerif", Font.PLAIN, Config.getInstance().getGuiSize());

        setTitle("XMage Launcher Settings");
        setModalityType(ModalityType.APPLICATION_MODAL);
        pack();
        setSize(400 + Config.getInstance().getGuiSize() * 20, 230 + Config.getInstance().getGuiSize() * 12);
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
        layout.columnWeights = new double[]{0, 1.0};
        panel1.setLayout(layout);

        constraints = new GridBagConstraints();
        constraints.insets = new Insets(10, 10, 10, 10);

        label = new JLabel("Branch:");
        constraints.anchor = GridBagConstraints.EAST;
        panel1.add(label, constraints);

        cmbXMageBranch = new JComboBox<>(Config.getInstance().getXMageBranches());
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.BOTH;
        panel1.add(cmbXMageBranch, constraints);
        cmbXMageBranch.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String url = ((XMageBranch) Objects.requireNonNull(cmbXMageBranch.getSelectedItem())).url;
                if (url != null) {
                    txtXMageHome.setText(url);
                    txtXMageHome.setEnabled(false);
                } else {
                    txtXMageHome.setEnabled(true);
                }
            }
        });

        constraints = new GridBagConstraints();
        constraints.insets = new Insets(10, 10, 10, 10);

        label = new JLabel("XMage Home:");
        constraints.anchor = GridBagConstraints.EAST;
        panel1.add(label, constraints);

        txtXMageHome = new JTextField();
        txtXMageHome.setText(Config.getInstance().getXMageHome());
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.BOTH;
        panel1.add(txtXMageHome, constraints);
        txtXMageHome.setEnabled(false);
        handleHomeChange();

        label = new JLabel("Show Client Console:");
        constraints.anchor = GridBagConstraints.EAST;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        panel1.add(label, constraints);

        chkShowClientConsole = new JCheckBox();
        chkShowClientConsole.setSelected(Config.getInstance().isShowClientConsole());
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.BOTH;
        panel1.add(chkShowClientConsole, constraints);

        label = new JLabel("Show Server Console:");
        constraints.anchor = GridBagConstraints.EAST;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        panel1.add(label, constraints);

        chkShowServerConsole = new JCheckBox();
        chkShowServerConsole.setFont(defaultFont);
        chkShowServerConsole.setSelected(Config.getInstance().isShowServerConsole());
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.BOTH;
        panel1.add(chkShowServerConsole, constraints);

        label = new JLabel("Server test mode:");
        String tip = "Test mode allows you to quickly create a game with AI and customize any " +
                "game situations and combos (use the cheat button on the player panel)";
        label.setToolTipText(tip);
        constraints.anchor = GridBagConstraints.EAST;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        panel1.add(label, constraints);

        chkServerTestMode = new JCheckBox();
        chkServerTestMode.setToolTipText(tip);
        chkServerTestMode.setFont(defaultFont);
        chkServerTestMode.setSelected(Config.getInstance().isServerTestMode());
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.BOTH;
        panel1.add(chkServerTestMode, constraints);

        label = new JLabel("Client start delay (seconds):");
        tip = "Sets the delay in seconds after which the client starts when you click on 'Launch Client and Server' button";
        label.setToolTipText(tip);
        constraints.anchor = GridBagConstraints.EAST;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        panel1.add(label, constraints);

        SpinnerModel clientStartDelayModel = new SpinnerNumberModel(Config.getInstance().getClientStartDelaySeconds(), 0, 1000, 1);
        spnClientDelay = new JSpinner(clientStartDelayModel);
        spnClientDelay.setToolTipText(tip);
        spnClientDelay.setValue(Config.getInstance().getClientStartDelaySeconds());
        spnClientDelay.setFont(defaultFont);
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.anchor = GridBagConstraints.WEST;
        panel1.add(spnClientDelay, constraints);

        label = new JLabel("GUI Size:");
        constraints.anchor = GridBagConstraints.EAST;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        panel1.add(label, constraints);

        SpinnerModel guiSizemodel = new SpinnerNumberModel(Config.getInstance().getGuiSize(), 10, 50, 1);
        spnGuiSize = new JSpinner(guiSizemodel);
        spnGuiSize.setValue(Config.getInstance().getGuiSize());
//        Component mySpinnerEditor = spnGuiSize.getEditor();
//        JFormattedTextField jftf = ((JSpinner.DefaultEditor) mySpinnerEditor).getTextField();
//        jftf.setColumns(10);
        spnGuiSize.setFont(defaultFont);
        constraints.gridwidth = 1;
        constraints.anchor = GridBagConstraints.WEST;
        panel1.add(spnGuiSize, constraints);

        constraints.gridwidth = GridBagConstraints.REMAINDER;
        panel1.add(Box.createHorizontalBox(), constraints);

        // Java settings panel
        panel2 = new JPanel();
        layout = new GridBagLayout();
        layout.columnWeights = new double[]{0, 1.0};
        panel2.setLayout(layout);

        constraints = new GridBagConstraints();
        constraints.insets = new Insets(10, 10, 10, 10);

        label = new JLabel("Use system Java:");
        constraints.anchor = GridBagConstraints.EAST;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        panel2.add(label, constraints);

        chkUseSystemJava = new JCheckBox();
        chkUseSystemJava.setFont(defaultFont);
        chkUseSystemJava.setSelected(Config.getInstance().useSystemJava());
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.BOTH;
        panel2.add(chkUseSystemJava, constraints);

        label = new JLabel("Client java options:");
        constraints.anchor = GridBagConstraints.EAST;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        panel2.add(label, constraints);

        txtClientJavaOpt = new JTextField();
        txtClientJavaOpt.setText(Config.getInstance().getClientJavaOpts());
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.BOTH;
        panel2.add(txtClientJavaOpt, constraints);

        label = new JLabel("Server java options:");
        constraints.anchor = GridBagConstraints.EAST;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        panel2.add(label, constraints);

        txtServerJavaOpt = new JTextField();
        txtServerJavaOpt.setText(Config.getInstance().getServerJavaOpts());
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.BOTH;
        panel2.add(txtServerJavaOpt, constraints);

        // Launcher settings panel
        panel3 = new JPanel();
        layout = new GridBagLayout();
        layout.columnWeights = new double[]{0, 1.0};
        panel3.setLayout(layout);

        constraints = new GridBagConstraints();
        constraints.insets = new Insets(10, 10, 10, 10);

        label = new JLabel("Reset all settings:");
        constraints.anchor = GridBagConstraints.EAST;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        panel3.add(label, constraints);

        final JButton resetBtn = new JButton("RESET");
        resetBtn.setFont(defaultFont);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        panel3.add(resetBtn, constraints);
        resetBtn.addActionListener(e -> handleReset());

        // Setup tabs
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("XMage", panel1);
        tabbedPane.addTab("Java", panel2);
        tabbedPane.addTab("Other", panel3);
        add(tabbedPane, BorderLayout.CENTER);

        // Button panel
        buttonPanel = new javax.swing.JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonPanel.add(Box.createHorizontalGlue());

        JButton btnDone = new JButton("Done");
        btnDone.addActionListener(e -> handleDone());
        buttonPanel.add(btnDone);


        add(buttonPanel, BorderLayout.PAGE_END);

    }

    private void handleReset() {
        Config.resetInstance();
        Config.getInstance().saveProperties();
        dispose();
    }

    private void handleDone() {
        Config.getInstance().setClientJavaOpts(this.txtClientJavaOpt.getText());
        Config.getInstance().setServerJavaOpts(this.txtServerJavaOpt.getText());
        if (!this.txtXMageHome.getText().equals(Config.getInstance().getXMageHome())) {
            JOptionPane.showMessageDialog(
                    this,
                    "Remember to update XMage version after changing the XMage home.");
        }
        Config.getInstance().setXMageHome(this.txtXMageHome.getText());
        Config.getInstance().setShowClientConsole(this.chkShowClientConsole.isSelected());
        Config.getInstance().setShowServerConsole(this.chkShowServerConsole.isSelected());
        Config.getInstance().setGuiSize((Integer) this.spnGuiSize.getValue());
        Config.getInstance().setUseSystemJava(this.chkUseSystemJava.isSelected());
        Config.getInstance().setServerTestMode(this.chkServerTestMode.isSelected());
        Config.getInstance().setClientStartDelaySeconds((Integer) this.spnClientDelay.getValue());
        Config.getInstance().saveProperties();
        dispose();
    }

    private void handleHomeChange() {
        String url = txtXMageHome.getText();
        cmbXMageBranch.setSelectedItem(Config.getInstance().getXMageBranchByUrl(url));
    }

}
