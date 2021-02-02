package com.xmage.launcher;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
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
    private final JTextField txtClientJavaOpt;
    private final JTextField txtServerJavaOpt;
    private final JTextField txtXMageHome;
    private final JCheckBox chkShowClientConsole;
    private final JCheckBox chkShowServerConsole;
    private final JCheckBox chkUseSystemJava;
    private final JSpinner spnGuiSize;

    private final JComboBox<XMageBranch> cmbXMageBranch;

    public SettingsDialog() {
        ImageIcon icon = new ImageIcon(XMageLauncher.class.getResource("/icon-mage-flashed.png"));
        this.setIconImage(icon.getImage());

        Font defaultFont = new Font("SansSerif", Font.PLAIN, Config.getGuiSize());

        setTitle("XMage Launcher Settings");
        setModalityType(ModalityType.APPLICATION_MODAL);
        pack();
        setSize(400 + Config.getGuiSize() * 20, 230 + Config.getGuiSize() * 12);
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

        cmbXMageBranch = new JComboBox<>(Config.getXMageBranches());
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
        txtXMageHome.setText(Config.getXMageHome());
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
        chkShowServerConsole.setFont(defaultFont);
        chkShowServerConsole.setSelected(Config.isShowServerConsole());
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.BOTH;
        panel1.add(chkShowServerConsole, constraints);

        label = new JLabel("GUI Size:");
        constraints.anchor = GridBagConstraints.EAST;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        panel1.add(label, constraints);

        SpinnerModel guiSizemodel = new SpinnerNumberModel(Config.getGuiSize(), 10, 50, 1);
        spnGuiSize = new JSpinner(guiSizemodel);
        spnGuiSize.setValue(Config.getGuiSize());
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
        chkUseSystemJava.setSelected(Config.useSystemJava());
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.BOTH;
        panel2.add(chkUseSystemJava, constraints);

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

        // Setup tabs
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("XMage", panel1);
        tabbedPane.addTab("Java", panel2);
        add(tabbedPane, BorderLayout.CENTER);

        // Button panel
        buttonPanel = new javax.swing.JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonPanel.add(Box.createHorizontalGlue());

        JButton btnDone = new JButton("Done");
        btnDone.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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
        Config.setGuiSize((Integer) this.spnGuiSize.getValue());
        Config.setUseSystemJava(this.chkUseSystemJava.isSelected());
        Config.saveProperties();
        dispose();
    }

    private void handleHomeChange() {
        String url = txtXMageHome.getText();
        cmbXMageBranch.setSelectedItem(Config.getXMageBranchByUrl(url));
    }

}
