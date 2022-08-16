package com.xmage.launcher;

import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.Timer;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.*;

/**
 * @author BetaSteward
 */
public class XMageLauncher implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(XMageLauncher.class);

    private final ResourceBundle messages;
    private final Locale locale;

    private final JFrame frame;
    private final JLabel mainPanel;
    private final JLabel labelProgress;
    private final JProgressBar progressBar;
    private final JTextArea textArea;
    private final JButton btnLaunchClient;
    private final JLabel xmageLogo;
    private final JButton btnLaunchServer;
    private final JButton btnLaunchClientServer;
    private final JScrollPane scrollPane;
    private final JButton btnCheck;
    private final JButton btnUpdate;

    private JSONObject config;
    private File path;

    private Point grabPoint;

    private Process serverProcess;
    private final List<Process> clientProcesses = new LinkedList<>();
    private final XMageConsole serverConsole;
    private final XMageConsole clientConsole;

    private JToolBar toolBar;

    private boolean newJava = false;
    private boolean noJava = false;
    private boolean newXMage = false;
    private boolean noXMage = false;
    private boolean downgradeXMage = false;

    private XMageLauncher() {
        setDefaultFonts();
        locale = Locale.getDefault();
        //locale = new Locale("it", "IT");
        messages = ResourceBundle.getBundle("MessagesBundle", locale);
        localize();

        serverConsole = new XMageConsole("XMage Server console");
        clientConsole = new XMageConsole("XMage Client console");

        frame = new JFrame(messages.getString("frameTitle") + " " + Config.getInstance().getVersion());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        final int width = 700 + Config.getInstance().getGuiSize() * 20;
        final int height = 430 + Config.getInstance().getGuiSize() * 12;
        frame.setPreferredSize(new Dimension(width, height));
        frame.setResizable(false);

        createToolbar();

        ImageIcon icon = new ImageIcon(Objects.requireNonNull(XMageLauncher.class.getResource("/icon-mage-flashed.png")));
        frame.setIconImage(icon.getImage());

        Random r = new Random();
        int imageNum = 1 + r.nextInt(17);
        ImageIcon background = new ImageIcon(new ImageIcon(Objects.requireNonNull(XMageLauncher.class.getResource("/backgrounds/" + imageNum + ".jpg"))).getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));
        mainPanel = new JLabel(background) {
            @Override
            public Dimension getPreferredSize() {
                Dimension size = super.getPreferredSize();
                Dimension lmPrefSize = getLayout().preferredLayoutSize(this);
                size.width = Math.max(size.width, lmPrefSize.width);
                size.height = Math.max(size.height, lmPrefSize.height);
                return size;
            }
        };
        mainPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                grabPoint = e.getPoint();
                mainPanel.getComponentAt(grabPoint);
            }
        });
        mainPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {

                // get location of Window
                int thisX = frame.getLocation().x;
                int thisY = frame.getLocation().y;

                // Determine how much the mouse moved since the initial click
                int xMoved = (thisX + e.getX()) - (thisX + grabPoint.x);
                int yMoved = (thisY + e.getY()) - (thisY + grabPoint.y);

                // Move window to this position
                int X = thisX + xMoved;
                int Y = thisY + yMoved;
                frame.setLocation(X, Y);
            }
        });
        mainPanel.setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(10, 10, 10, 10);

        Font fontBig = new Font("SansSerif", Font.BOLD, Config.getInstance().getGuiSize() + 2);
        Font fontSmall = new Font("SansSerif", Font.PLAIN, Config.getInstance().getGuiSize() - 2);
        Font fontSmallBold = new Font("SansSerif", Font.BOLD, Config.getInstance().getGuiSize() - 2);

        mainPanel.add(Box.createRigidArea(new Dimension(250, 50)));

        ImageIcon logo = new ImageIcon(new ImageIcon(Objects.requireNonNull(XMageLauncher.class.getResource("/label-xmage.png"))).getImage().getScaledInstance(150, 75, Image.SCALE_SMOOTH));
        xmageLogo = new JLabel(logo);
        constraints.gridx = 3;
        constraints.gridy = 0;
        constraints.gridheight = 1;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.anchor = GridBagConstraints.EAST;
        mainPanel.add(xmageLogo, constraints);

        textArea = new JTextArea(5, 40);
        textArea.setEditable(false);
        textArea.setForeground(Color.WHITE);
        textArea.setBackground(Color.BLACK);
        DefaultCaret caret = (DefaultCaret) textArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        constraints.gridx = 2;
        constraints.gridy = 1;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.fill = GridBagConstraints.BOTH;
        mainPanel.add(scrollPane, constraints);

        labelProgress = new JLabel(messages.getString("progress"));
        labelProgress.setFont(fontSmall);
        labelProgress.setForeground(Color.WHITE);
        constraints.gridy = 2;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.gridwidth = 1;
        constraints.anchor = GridBagConstraints.WEST;
        mainPanel.add(labelProgress, constraints);

        progressBar = new JProgressBar(0, 100);
        progressBar.setPreferredSize(new Dimension((int) progressBar.getPreferredSize().getWidth(), Config.getInstance().getGuiSize()));
        constraints.gridx = 3;
        constraints.weightx = 1.0;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(progressBar, constraints);

        JPanel pnlButtons = new JPanel();
        pnlButtons.setLayout(new GridBagLayout());
        pnlButtons.setOpaque(false);
        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.gridheight = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.BOTH;
        mainPanel.add(pnlButtons, constraints);

        btnLaunchClient = new JButton(messages.getString("launchClient"));
        btnLaunchClient.setToolTipText(messages.getString("launchClient.tooltip"));
        btnLaunchClient.setFont(fontBig);
        btnLaunchClient.setForeground(Color.GRAY);
        btnLaunchClient.setEnabled(false);
//        btnLaunchClient.setPreferredSize(new Dimension(180, 60));
        btnLaunchClient.addActionListener(e -> handleClient());

        constraints.gridx = GridBagConstraints.RELATIVE;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.BOTH;
        pnlButtons.add(btnLaunchClient, constraints);

        btnLaunchClientServer = new JButton(messages.getString("launchClientServer"));
        btnLaunchClientServer.setToolTipText(messages.getString("launchClientServer.tooltip"));
        btnLaunchClientServer.setFont(fontSmallBold);
        btnLaunchClientServer.setEnabled(false);
        btnLaunchClientServer.setForeground(Color.GRAY);
//        btnLaunchClientServer.setPreferredSize(new Dimension(80, 40));
        btnLaunchClientServer.addActionListener(e -> {
            handleServer();
            if (serverProcess != null) {
                Timer t = new Timer(Config.getInstance().getClientStartDelayMilliseconds(), after -> this.handleClient());
                t.setInitialDelay(Config.getInstance().getClientStartDelayMilliseconds());
                t.setRepeats(false);
                t.start();
            }
        });

        constraints.fill = GridBagConstraints.HORIZONTAL;
        pnlButtons.add(btnLaunchClientServer, constraints);

        btnLaunchServer = new JButton(messages.getString("launchServer"));
        btnLaunchServer.setToolTipText(messages.getString("launchServer.tooltip"));
        btnLaunchServer.setFont(fontSmallBold);
        btnLaunchServer.setEnabled(false);
        btnLaunchServer.setForeground(Color.GRAY);
//        btnLaunchServer.setPreferredSize(new Dimension(80, 40));
        btnLaunchServer.addActionListener(e -> handleServer());

        pnlButtons.add(btnLaunchServer, constraints);

        btnUpdate = new JButton(messages.getString("update.xmage"));
        btnUpdate.setToolTipText(messages.getString("update.xmage.tooltip"));
        btnUpdate.setFont(fontSmallBold);
        btnUpdate.setForeground(Color.BLACK);
//        btnUpdate.setPreferredSize(new Dimension(80, 40));
        btnUpdate.setEnabled(true);

        btnUpdate.addActionListener(e -> handleUpdate());

        pnlButtons.add(btnUpdate, constraints);

        btnCheck = new JButton(messages.getString("check.xmage"));
        btnCheck.setToolTipText(messages.getString("check.xmage.tooltip"));
        btnCheck.setFont(fontSmallBold);
        btnCheck.setForeground(Color.BLACK);
//        btnCheck.setPreferredSize(new Dimension(80, 40));
        btnCheck.setEnabled(true);

        btnCheck.addActionListener(e -> handleCheckUpdates());

        pnlButtons.add(btnCheck, constraints);

        frame.add(mainPanel);
        frame.pack();
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation(dim.width / 2 - frame.getSize().width / 2, dim.height / 2 - frame.getSize().height / 2);
        removeOldLauncherFiles();
    }

    private void setDefaultFonts() {
        Font defaultFont = new Font("SansSerif", Font.PLAIN, Config.getInstance().getGuiSize());
        UIManager.put("Button.font", defaultFont);
        UIManager.put("ToggleButton.font", defaultFont);
        UIManager.put("RadioButton.font", defaultFont);
        UIManager.put("CheckBox.font", defaultFont);
        UIManager.put("ColorChooser.font", defaultFont);
        UIManager.put("ComboBox.font", defaultFont);
        UIManager.put("Label.font", defaultFont);
        UIManager.put("List.font", defaultFont);
        UIManager.put("MenuBar.font", defaultFont);
        UIManager.put("MenuItem.font", defaultFont);
        UIManager.put("RadioButtonMenuItem.font", defaultFont);
        UIManager.put("CheckBoxMenuItem.font", defaultFont);
        UIManager.put("Menu.font", defaultFont);
        UIManager.put("PopupMenu.font", defaultFont);
        UIManager.put("OptionPane.font", defaultFont);
        UIManager.put("Panel.font", defaultFont);
        UIManager.put("ProgressBar.font", defaultFont);
        UIManager.put("ScrollPane.font", defaultFont);
        UIManager.put("Viewport.font", defaultFont);
        UIManager.put("TabbedPane.font", defaultFont);
        UIManager.put("Table.font", defaultFont);
        UIManager.put("TableHeader.font", defaultFont);
        UIManager.put("TextField.font", defaultFont);
        UIManager.put("PasswordField.font", defaultFont);
        UIManager.put("TextArea.font", defaultFont);
        UIManager.put("TextPane.font", defaultFont);
        UIManager.put("EditorPane.font", defaultFont);
        UIManager.put("TitledBorder.font", defaultFont);
        UIManager.put("ToolBar.font", defaultFont);
        UIManager.put("ToolTip.font", defaultFont);
        UIManager.put("Tree.font", defaultFont);
    }

    private void createToolbar() {

        toolBar = new JToolBar();
        toolBar.setFloatable(false);
        Border emptyBorder = BorderFactory.createEmptyBorder();

        JButton toolbarButton = new JButton("Settings");
        toolbarButton.setBorder(emptyBorder);
        toolbarButton.addActionListener(e -> {
            SettingsDialog settings = new SettingsDialog(messages);
            settings.setVisible(true);
            settings.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    checkJava();
                    if (!noJava && !noXMage) {
                        enableButtons();
                    } else if (noJava) {
                        handleUpdate();
                    }
                }
            });
        });
        toolBar.add(toolbarButton);
        toolBar.addSeparator();

        toolbarButton = new JButton("About");
        toolbarButton.setBorder(emptyBorder);
        toolbarButton.addActionListener(e -> {
            AboutDialog about = new AboutDialog();
            about.setVisible(true);
        });
        toolBar.add(toolbarButton);
        toolBar.addSeparator();

        toolbarButton = new JButton("Forum");
        toolbarButton.setBorder(emptyBorder);
        toolbarButton.addActionListener(e -> openWebpage("http://www.slightlymagic.net/forum/viewforum.php?f=70"));
        toolBar.add(toolbarButton);
        toolBar.addSeparator();

        toolbarButton = new JButton("Website");
        toolbarButton.setBorder(emptyBorder);
        toolbarButton.addActionListener(e -> openWebpage("http://xmage.de"));
        toolBar.add(toolbarButton);
        toolBar.addSeparator();

        toolbarButton = new JButton("Changelog");
        toolbarButton.setBorder(emptyBorder);
        toolbarButton.addActionListener(e -> openWebpage("https://jaydi85.github.io/xmage-web-news/news.html"));
        toolBar.add(toolbarButton);

        frame.add(toolBar, BorderLayout.PAGE_START);

    }

    private static void openWebpage(String uri) {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(new URI(uri));
            } catch (URISyntaxException | IOException ex) {
                logger.error("Error: ", ex);
            }
        }
    }

    private void handleClient() {
        checkJava();
        Process p = Utilities.launchClientProcess();
        if (p == null) {
            disableButtons(true);
            JOptionPane.showMessageDialog(frame, "Try to update XMage first.", "Error with executables", JOptionPane.ERROR_MESSAGE);
            return;
        }
        clientConsole.setVisible(Config.getInstance().isShowClientConsole());
        clientConsole.start(p);
        clientProcesses.add(p);
    }

    private void handleServer() {
        if (serverProcess == null) {
            checkJava();
            if (Config.getInstance().isServerTestMode()) {
                textArea.append(messages.getString("launchServer.testMode.message") + "\n");
            }
            serverProcess = Utilities.launchServerProcess();
            if (serverProcess == null) {
                disableButtons(true);
                JOptionPane.showMessageDialog(frame, "Try to update XMage first.", "Error with executables", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                int exitValue = serverProcess.exitValue();
                logger.error("Problem during launch of server process. exit value = " + exitValue);
            } catch (IllegalThreadStateException e) {
                serverConsole.setVisible(Config.getInstance().isShowServerConsole());
                serverConsole.start(serverProcess);
                btnLaunchServer.setText(messages.getString("stopServer"));
                btnLaunchClientServer.setEnabled(false);
            }
        } else {
            Utilities.stopProcess(serverProcess);
            serverProcess = null;
            btnLaunchServer.setText(messages.getString("launchServer"));
            btnLaunchClientServer.setEnabled(true);
        }
    }

    private void handleUpdate() {
        if (serverProcess != null) {
            if (serverProcess.isAlive()) {
                JOptionPane.showMessageDialog(frame,
                        messages.getString("update.while.server.open"),
                        messages.getString("update.while.server.open.title"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                serverProcess = null;
                btnLaunchServer.setText(messages.getString("launchServer"));
                btnLaunchClientServer.setEnabled(true);
            }
        }
        while (clientProcesses.size() > 0) {
            int choice = JOptionPane.showConfirmDialog(frame, messages.getString("update.while.client.open"), messages.getString("update.while.client.open.title"), JOptionPane.OK_CANCEL_OPTION);
            if (choice == JOptionPane.CANCEL_OPTION) {
                return;
            }
            clientProcesses.removeIf(p -> !p.isAlive());
        }
        disableButtons();
        if (!getConfig()) {
            return;
        }
        checkXMage(true); // handle branch changes
        if (!newJava && !newXMage) {
            int response = JOptionPane.showConfirmDialog(frame, messages.getString("force.update.message"), messages.getString("force.update.title"), JOptionPane.YES_NO_OPTION);
            if (response == JOptionPane.YES_OPTION) {
                UpdateTask update = new UpdateTask(progressBar, true);
                update.execute();
            } else {
                enableButtons();
            }
        } else {
            UpdateTask update = new UpdateTask(progressBar, false);
            update.execute();
        }
    }

    private void handleCheckUpdates() {
        if (getConfig()) {
            checkUpdates();
            if (!newJava && !newXMage) {
                JOptionPane.showMessageDialog(frame, messages.getString("xmage.latest.message"), messages.getString("xmage.latest.title"), JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void checkUpdates() {
        checkJava();
        checkXMage(false);
        enableButtons();
    }

    private void localize() {
        UIManager.put("OptionPane.yesButtonText", messages.getString("yes"));
        UIManager.put("OptionPane.noButtonText", messages.getString("no"));
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            XMageLauncher gui = new XMageLauncher();
            SwingUtilities.invokeLater(gui);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            logger.error("Error: ", ex);
        }
    }

    @Override
    public void run() {
        frame.setVisible(true);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (serverProcess != null) {
                    int response = JOptionPane.showConfirmDialog(frame, messages.getString("serverRunning.message"), messages.getString("serverRunning.title"), JOptionPane.YES_NO_OPTION);
                    if (response == JOptionPane.YES_OPTION) {
                        Utilities.stopProcess(serverProcess);
                    }
                }
                Config.getInstance().saveProperties();
            }
        });

        SwingUtilities.invokeLater(() -> {
            if (getConfig()) {
                path = Utilities.getInstallPath();
                textArea.append(messages.getString("folder") + path.getAbsolutePath() + "\n");

                DownloadLauncherTask launcher = new DownloadLauncherTask(progressBar);
                launcher.execute();
            }
        });

    }

    private boolean getConfig() {
        String xmageConfig = Config.getInstance().getXMageHome() + "/config.json";

        try {
            URL xmageUrl = new URL(xmageConfig);
            textArea.append(messages.getString("readingConfig") + xmageUrl + "\n");
            config = Utilities.readJsonFromUrl(xmageUrl);
            return true;
        } catch (IOException ex) {
            logger.error("Error reading config from " + xmageConfig, ex);
            textArea.append(messages.getString("readingConfig.error") + xmageConfig + "\n" + messages.getString("readingConfig.error.causes") + "\n");
        } catch (JSONException ex) {
            logger.error("Invalid config from " + xmageConfig, ex);
            textArea.append(messages.getString("invalidConfig") + xmageConfig + "\n");
        }
        enableButtons();
        return false;
    }

    private boolean checkJavaFX() {
        return true; // JavaFX is not currently needed.
//        try {
//            Class.forName("javafx.application.Platform");
//            return true;
//        } catch( ClassNotFoundException e ) {
//            return false;
//        }
    }

    private void checkJava() {
        if (Config.getInstance().useSystemJava()) {
            // checks if the system java is suitable for XMage
            // as it is selected in the configuration (user settings)
            textArea.append(messages.getString("java.installed") + System.getProperty("java.home") + "\n");
            if (checkJavaFX()) {
                noJava = false;
                return;
            }
            JOptionPane.showMessageDialog(frame, messages.getString("java.system.nojavafx.message"),
                    messages.getString("java.system.nojavafx"),
                    JOptionPane.ERROR_MESSAGE);
            Config.getInstance().setUseSystemJava(false);
            Config.getInstance().saveProperties();
        }
        try {
            // checks if the currently installed java version is up-to-date
            String javaAvailableVersion = (String) config.getJSONObject("java").get(("version"));
            String javaInstalledVersion = Config.getInstance().getInstalledJavaVersion();
            textArea.append(messages.getString("java.installed") + javaInstalledVersion + "\n");
            textArea.append(messages.getString("java.available") + javaAvailableVersion + "\n");
            noJava = false;
            newJava = false;
            if (compareVersions(javaAvailableVersion, javaInstalledVersion) > 0) {
                newJava = true;
                String javaMessage;
                String javaTitle;
                // it could be that XMage java isn't installed yet
                if (javaInstalledVersion.isEmpty()) {
                    noJava = true;
                    textArea.append(messages.getString("java.none") + "\n");
                    javaMessage = messages.getString("java.none.message");
                    javaTitle = messages.getString("java.none");
                } else {
                    textArea.append(messages.getString("java.new") + "\n");
                    javaMessage = messages.getString("java.new.message");
                    javaTitle = messages.getString("java.new");
                }
                // prompt the users to select which java they want to use
                int result = JOptionPane.showOptionDialog(frame, javaMessage, javaTitle, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                        new String[]{
                                messages.getString("java.system.choice"),
                                "OK"
                        }, 1);
                if (result == 1 || result == JOptionPane.CLOSED_OPTION) {
                    // the user selected XMage java version
                    Config.getInstance().setUseSystemJava(false);
                    noJava = Config.getInstance().getInstalledJavaVersion().isEmpty();
                    if (noJava)
                    {
                        disableButtons(true);
                    }
                } else if (result == 0) {
                    // the user selected the system java version
                    if (checkJavaFX()) {
                        textArea.append(messages.getString("java.installed") + System.getProperty("java.home") + "\n");
                        noJava = false;
                        Config.getInstance().setUseSystemJava(true);
                    } else {
                        JOptionPane.showMessageDialog(frame, messages.getString("java.system.nojavafx.message"),
                                messages.getString("java.system.nojavafx"),
                                JOptionPane.ERROR_MESSAGE);
                        Config.getInstance().setUseSystemJava(false);
                    }
                }
                Config.getInstance().saveProperties();
            }
        } catch (JSONException ex) {
            logger.error("Error: ", ex);
        }
    }

    private void checkXMage(boolean silent) {
        try {
            String xmageAvailableVersion = (String) config.getJSONObject("XMage").get(("version"));
            String xmageInstalledVersion = Config.getInstance().getInstalledXMageVersion();
            textArea.append(messages.getString("xmage.installed") + xmageInstalledVersion + "\n");
            textArea.append(messages.getString("xmage.available") + xmageAvailableVersion + "\n");
            noXMage = false;
            newXMage = false;
            downgradeXMage = false;
            int compared = compareVersions(xmageAvailableVersion, xmageInstalledVersion);
            if (compared > 0) {
                newXMage = true;
                String xmageMessage;
                String xmageTitle;
                if (xmageInstalledVersion.isEmpty()) {
                    noXMage = true;
                    textArea.append(messages.getString("xmage.none") + "\n");
                    xmageMessage = messages.getString("xmage.none.message");
                    xmageTitle = messages.getString("xmage.none");
                } else {
                    textArea.append(messages.getString("xmage.new") + "\n");
                    xmageMessage = messages.getString("xmage.new.message");
                    xmageTitle = messages.getString("xmage.new");
                }
                if (!silent) {
                    JOptionPane.showMessageDialog(frame, xmageMessage, xmageTitle, JOptionPane.INFORMATION_MESSAGE);
                }
            }
            if (compared < 0) { // handle downgrade
                downgradeXMage = true;
            }
        } catch (JSONException ex) {
            logger.error("Error: ", ex);
        }
    }

    private void enableButtons() {
        if (!noJava && !noXMage) {
            btnLaunchClient.setEnabled(true);
            btnLaunchClient.setForeground(Color.BLACK);
            btnLaunchClientServer.setEnabled(true);
            btnLaunchClientServer.setForeground(Color.BLACK);
            btnLaunchServer.setEnabled(true);
            btnLaunchServer.setForeground(Color.BLACK);
        }
        btnUpdate.setEnabled(true);
        btnUpdate.setForeground(Color.BLACK);
        btnCheck.setEnabled(true);
        btnCheck.setForeground(Color.BLACK);
    }

    private void disableButtons() {
        disableButtons(false);
    }

    private void disableButtons(boolean justClientServer) {
        btnLaunchClient.setEnabled(false);
        btnLaunchClient.setForeground(Color.GRAY);
        btnLaunchClientServer.setEnabled(false);
        btnLaunchClientServer.setForeground(Color.GRAY);
        btnLaunchServer.setEnabled(false);
        btnLaunchServer.setForeground(Color.GRAY);
        if (!justClientServer) {
            btnUpdate.setEnabled(false);
            btnUpdate.setForeground(Color.GRAY);
            btnCheck.setEnabled(false);
            btnCheck.setForeground(Color.GRAY);
        }
    }

    private class DownloadLauncherTask extends DownloadTask {

        public DownloadLauncherTask(JProgressBar progressBar) {
            super(progressBar, textArea);
        }

        @Override
        protected Void doInBackground() {
            try {
                File launcherFolder = new File(path.getAbsolutePath());
                String launcherAvailableVersion = (String) config.getJSONObject("XMage").getJSONObject("Launcher").get(("version"));
                String launcherInstalledVersion = Config.getInstance().getVersion();
                publish(messages.getString("xmage.launcher.installed") + launcherInstalledVersion + "\n");
                publish(messages.getString("xmage.launcher.available") + launcherAvailableVersion + "\n");
                if (compareVersions(launcherAvailableVersion, launcherInstalledVersion) > 0) {
                    String launcherMessage;
                    String launcherTitle;
                    publish(messages.getString("xmage.launcher.new") + "\n");
                    launcherMessage = messages.getString("xmage.launcher.new.message");
                    launcherTitle = messages.getString("xmage.launcher.new");
                    int response = JOptionPane.showConfirmDialog(frame,
                            "<html>" + launcherMessage + "  " + messages.getString("installNow") + "</html>", launcherTitle,
                            JOptionPane.YES_NO_OPTION);
                    if (response == JOptionPane.YES_OPTION) {
                        String launcherRemoteLocation = (String) config.getJSONObject("XMage").getJSONObject("Launcher").get(("location"));
                        URL launcher = new URL(launcherRemoteLocation);
                        publish(messages.getString("xmage.launcher.downloading") + launcher + "\n");

                        download(launcher, path.getAbsolutePath(), "");

                        File from = new File(path.getAbsolutePath() + File.separator + "xmage.dl");
                        publish(messages.getString("xmage.launcher.installing"));
                        File to = new File(launcherFolder, "XMageLauncher-" + launcherAvailableVersion + ".jar");
                        from.renameTo(to);
                        publish(messages.getString("done") + "\n");
                        publish(0);
                        JOptionPane.showMessageDialog(frame, "<html>" + messages.getString("restartMessage") + "</html>",
                                messages.getString("restartTitle"), JOptionPane.WARNING_MESSAGE);
                        Utilities.restart(to);
                    }
                }

            } catch (IOException | JSONException ex) {
                publish(0);
                cancel(true);
                logger.error("Error: ", ex);
            }
            return null;
        }

        @Override
        public void done() {
            checkUpdates();
            if (noXMage) {
                UpdateTask update = new UpdateTask(progressBar, false);
                update.execute();
            }
        }

    }

    private class UpdateTask extends DownloadTask {

        private final boolean force;

        public UpdateTask(JProgressBar progressBar, boolean force) {
            super(progressBar, textArea);
            this.force = force;
        }

        @Override
        protected Void doInBackground() {
            if (!downgradeXMage && (force || noJava || newJava)) { // only update java on force update to the same version
                updateJava();
            }
            if (force || noXMage || newXMage) {
                updateXMage();
            }
            return null;
        }

        private boolean updateJava() {
            if (Config.getInstance().useSystemJava()) {
                publish(messages.getString("java.system.message") + " " + System.getProperty("java.home") + "\n");
                return true;
            } else {
                try {
                    disableButtons();
                    File javaFolder = new File(path.getAbsolutePath() + File.separator + "java");
                    String javaAvailableVersion = (String) config.getJSONObject("java").get(("version"));
                    if (javaFolder.isDirectory()) { // remove existing install
                        publish(messages.getString("removing") + "\n");
                        removeJavaFiles(javaFolder);
                    }
                    javaFolder.mkdirs();
                    String javaRemoteLocation = (String) config.getJSONObject("java").get(("location"));
                    URL java = new URL(javaRemoteLocation + Utilities.getOSandArch() + ".tar.gz");
                    publish(messages.getString("java.downloading") + java + "\n");

                    download(java, path.getAbsolutePath(), "oraclelicense=accept-securebackup-cookie");

                    File from = new File(path.getAbsolutePath() + File.separator + "xmage.dl");
                    publish(messages.getString("java.installing"));

                    extract(from, javaFolder);
                    publish(messages.getString("done") + "\n");
                    publish(0);
                    if (!from.delete()) {
                        publish(messages.getString("error.cleanup") + "\n");
                        logger.error("Error: could not cleanup temporary files");
                    }
                    Config.getInstance().setInstalledJavaVersion(javaAvailableVersion);
                    Config.getInstance().saveProperties();
                    return true;
                } catch (IOException | JSONException ex) {
                    publish(0);
                    cancel(true);
                    logger.error("Error: ", ex);
                }
            }
            return false;
        }

        private boolean updateXMage() {
            try {
                disableButtons();
                File xmageFolder = new File(path.getAbsolutePath() + File.separator + "xmage");
                String xmageAvailableVersion = (String) config.getJSONObject("XMage").get(("version"));
                String xmageRemoteLocation;
                String[] otherLocations;
                xmageRemoteLocation = (String) config.getJSONObject("XMage").get(("location"));
                JSONArray arr = (JSONArray) config.getJSONObject("XMage").get(("locations"));
                otherLocations = new String[arr.length()];
                for (int i = 0; i < arr.length(); i++) {
                    otherLocations[i] = (String) arr.get(i);
                }
                URL xmage = new URL(xmageRemoteLocation);
                publish(messages.getString("xmage.downloading") + xmage + "\n");

                int altCount = 0;
                boolean result = download(xmage, path.getAbsolutePath(), "");
                while (!result && altCount <= otherLocations.length) {
                    publish(messages.getString("xmage.downloading.failed") + xmage + "\n");
                    xmage = new URL(otherLocations[altCount]);
                    altCount++;
                    publish(messages.getString("xmage.downloading") + xmage + "\n");
                    result = download(xmage, path.getAbsolutePath(), "");
                }
                if (result) {
                    if (xmageFolder.isDirectory()) { // remove existing install
                        publish(messages.getString("removing") + "\n");
                        removeXMageFiles(xmageFolder);
                    }
                    xmageFolder.mkdirs();

                    File from = new File(path.getAbsolutePath() + File.separator + "xmage.dl");

                    publish(messages.getString("xmage.installing"));

                    unzip(from, xmageFolder);
                    publish(messages.getString("done") + "\n");
                    publish(0);
                    if (!from.delete()) {
                        publish(messages.getString("error.cleanup") + "\n");
                        logger.error("Error: could not cleanup temporary files");
                    }
                    Config.getInstance().setInstalledXMageVersion(xmageAvailableVersion);
                    Config.getInstance().saveProperties();
                    return true;
                }
            } catch (IOException | JSONException ex) {
                publish(0);
                cancel(true);
                logger.error("Error: ", ex);
            }
            return false;
        }

        private void removeJavaFiles(File javaFolder) {
            File[] files = javaFolder.listFiles();
            for (final File file : files) {
                if (file.isDirectory()) {
                    removeJavaFiles(file);
                }
                if (!file.delete()) {
                    logger.error("Can't remove " + file.getAbsolutePath());
                }
            }
        }

        private void removeXMageFiles(File xmageFolder) {
            // keep images folder -- no need to make users download these again
            File[] files = xmageFolder.listFiles((dir, name) -> !name.matches("images|gameLogs|backgrounds|mageclient\\.log|mageserver\\.log|.*\\.dck"));
            for (final File file : files) {
                if (file.isDirectory()) {
                    removeXMageFiles(file);
                } else if (!file.delete()) {
                    logger.error("Can't remove " + file.getAbsolutePath());
                }
            }
        }

        @Override
        public void done() {
            checkUpdates();
        }
    }

    private int compareVersions(String ver1, String ver2) {
        DefaultArtifactVersion version1 = new DefaultArtifactVersion(ver1);
        DefaultArtifactVersion version2 = new DefaultArtifactVersion(ver2);
        return version1.compareTo(version2);
    }

    private void removeOldLauncherFiles() {
        File launcherFolder = new File(Utilities.getInstallPath().getAbsolutePath());
        final String launcherInstalledVersion = Config.getInstance().getVersion();
        File[] files = launcherFolder.listFiles((dir, name) -> {
            if (name.matches("XMageLauncher.*\\.jar")) {
                return !name.equals("XMageLauncher-" + launcherInstalledVersion + ".jar");
            }
            return false;
        });
        if (files.length > 0) {
            textArea.append(messages.getString("removing") + "\n");
            for (final File file : files) {
                if (!file.isDirectory() && !file.delete()) {
                    logger.error("Can't remove " + file.getAbsolutePath());
                }
            }
        }
    }

}
