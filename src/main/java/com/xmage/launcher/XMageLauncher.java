
package com.xmage.launcher;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.text.DefaultCaret;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author BetaSteward
 */
public class XMageLauncher implements Runnable {
    
    private static final Logger logger = LoggerFactory.getLogger(XMageLauncher.class);
    
    private final JFrame frame;
    private final JLabel labelProgress;
    private final JProgressBar progressBar;
    private final JTextArea textArea;
    private final JButton btnLaunchClient;
    private final JLabel labelLaunchClient;
    private final JButton btnLaunchServer;
    private final JLabel labelLaunchServer;
    private final JButton btnLaunchClientServer;
    private final JLabel labelLaunchClientServer;
    private final JScrollPane scrollPane;
    
    private JSONObject config;
    private File path;

    private Process serverProcess;
    
    private XMageLauncher() {
        frame = new JFrame("XMage Launcher");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridBagLayout());
        frame.setPreferredSize(new Dimension(640, 480));
        
        URL iconURL = XMageLauncher.class.getResource("/icon-mage.png");
        ImageIcon icon = new ImageIcon(iconURL);
        frame.setIconImage(icon.getImage());
        
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(5, 5, 5, 5);
    
        Font font14 = new Font("Arial", Font.BOLD, 14);
        Font font12 = new Font("Arial", Font.PLAIN, 12);
        
        btnLaunchClient = new JButton("Launch Client");
        btnLaunchClient.setFont(font14);
        btnLaunchClient.setEnabled(false);
        btnLaunchClient.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                Utilities.launchClientProcess(textArea);
            }
        });      

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 0.0;
        constraints.weighty = 0.2;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.BOTH;
        frame.add(btnLaunchClient, constraints);

        labelLaunchClient = new JLabel("<html>Launch Client application only - use this if you will be connecting to a remote XMage server to play against others.</html>");
        labelLaunchClient.setFont(font12);
        constraints.gridx = 1;
        constraints.weightx = 1.0;
        frame.add(labelLaunchClient, constraints);
        
        btnLaunchClientServer = new JButton("Launch Client and Server");
        btnLaunchClientServer.setFont(font14);
        btnLaunchClientServer.setEnabled(false);
        btnLaunchClientServer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                handleServer();
                Utilities.launchClientProcess(textArea);
            }
        });      

        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.weightx = 0.0;
        constraints.weighty = 0.2;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.BOTH;
        frame.add(btnLaunchClientServer, constraints);

        labelLaunchClientServer = new JLabel("<html>Launch Client and Server applications - use this if you will be playing locally against an AI</html>");
        labelLaunchClientServer.setFont(font12);
        constraints.gridx = 1;
        constraints.weightx = 1.0;
        frame.add(labelLaunchClientServer, constraints);

        btnLaunchServer = new JButton("Launch Server");
        btnLaunchServer.setFont(font14);
        btnLaunchServer.setEnabled(false);
        btnLaunchServer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                handleServer();
            }
        });      

        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.weightx = 0.0;
        constraints.weighty = 0.2;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.BOTH;
        frame.add(btnLaunchServer, constraints);

        labelLaunchServer = new JLabel("<html>Launch Server application only - use this if you want run an XMage server.  Additional network configuration may be necessary to allow clients to connect.</html>");
        labelLaunchServer.setFont(font12);
        constraints.gridx = 1;
        constraints.weightx = 1.0;
        frame.add(labelLaunchServer, constraints);
        
        textArea = new JTextArea(5, 50);
        textArea.setEditable(false);
        DefaultCaret caret = (DefaultCaret)textArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        scrollPane = new JScrollPane (textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        constraints.gridx = 0;
        constraints.gridy = 4;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.gridwidth = 2;
        constraints.fill = GridBagConstraints.BOTH;
        frame.add(scrollPane, constraints);
        
        labelProgress = new JLabel("Progress:");
        constraints.gridx = 0;
        constraints.gridy = 5;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.gridwidth = 1;
        constraints.anchor = GridBagConstraints.WEST;
        frame.add(labelProgress, constraints);

        progressBar = new JProgressBar(0, 100);
        constraints.gridx = 1;
        constraints.weightx = 1.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        frame.add(progressBar, constraints);

        frame.pack();
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation(dim.width/2 - frame.getSize().width/2, dim.height/2 - frame.getSize().height/2);
    }
    
    private void handleServer() {
        if (serverProcess == null) {
            serverProcess = Utilities.launchServerProcess(textArea);
            btnLaunchServer.setText("Stop Server");
        }
        else {
            Utilities.stopProcess(serverProcess);
            serverProcess = null;
            btnLaunchServer.setText("Launch Server");
        }
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
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
                    int response = JOptionPane.showConfirmDialog(frame, "XMage server is currently running.  Do you want to stop it?  If you don't then you will need to stop it manually.", "Server is running", JOptionPane.YES_NO_OPTION);
                    if (response == JOptionPane.YES_OPTION) {
                        Utilities.stopProcess(serverProcess);
                    }
                }
                Config.saveProperties();
            }
        });
        
        try {
            URL xmageUrl = new URL(Config.getXMageHome() + "/config.json");
            try {
                textArea.append("Reading config from " + xmageUrl.toString() + "\n");
                config = Utilities.readJsonFromUrl(xmageUrl);
            } catch (IOException ex) {
                logger.error("Error reading config from " + xmageUrl.toString(), ex);
                textArea.append("Error reading config from " + xmageUrl.toString() + "\nPossible causes:  Site is offline or internet connection is unavailable.\n");
                enableButtons();
                return;
            } catch (JSONException ex) {
                logger.error("Invalid config from " + xmageUrl.toString(), ex);
                textArea.append("Invalid config from " + xmageUrl.toString() + "\n");
                enableButtons();
                return;
            }
            path = Utilities.getInstallPath();
            textArea.append("XMage folder:  " + path.getAbsolutePath() + "\n");
            CountDownLatch latch = new CountDownLatch(1);
            DownloadJavaTask java = new DownloadJavaTask(latch, progressBar);
            DownloadXMageTask xmage = new DownloadXMageTask(latch, progressBar);
            java.execute();
            xmage.execute();
        } catch (Exception ex) {
            logger.error("Error: ", ex);
            textArea.append("Error: " + ex.getMessage());
        }        
              
    }
    
    private void enableButtons() {
        String javaInstalledVersion = Config.getInstalledJavaVersion();
        if (!javaInstalledVersion.isEmpty()) {
            String xmageInstalledVersion = Config.getInstalledXMageVersion();
            if (!xmageInstalledVersion.isEmpty()) {
                btnLaunchClient.setEnabled(true);
                btnLaunchClientServer.setEnabled(true);
                btnLaunchServer.setEnabled(true);
            }
            else {
                textArea.append("XMage is not installed.  Unable to continue.");
            }
        }
        else {
            textArea.append("Java is not installed.  Unable to continue.");
        }
    }
    
    private class DownloadJavaTask extends DownloadTask {
        
        private final CountDownLatch latch;

        public DownloadJavaTask(CountDownLatch latch, JProgressBar progressBar) {
            super(progressBar);
            this.latch = latch;
        }

        @Override
        protected Void doInBackground() {
            try {
                File javaFolder = new File(path.getAbsolutePath() + File.separator + "java");
                String javaAvailableVersion = (String)config.getJSONObject("java").get(("version"));
                String javaInstalledVersion = Config.getInstalledJavaVersion();
                textArea.append("Java version installed:  " + javaInstalledVersion + "\n");
                textArea.append("Java version available:  " + javaAvailableVersion + "\n");
                if (!javaAvailableVersion.equals(javaInstalledVersion)) {
                    textArea.append("New version of Java available.  \n");
                    int response = JOptionPane.showConfirmDialog(frame, "A newer version of Java is available.  Would you like to install it?", "New Version Available", JOptionPane.YES_NO_OPTION);
                    if (response == JOptionPane.YES_OPTION) {
                        if (javaFolder.isDirectory()) {  //remove existing install
                            if (!javaFolder.delete()) {
                                textArea.append("ERROR: could not remove java folder\n");
                                logger.error("Error: could not remove java folder");
                            }
                        }
                        javaFolder.mkdirs();
                        String javaRemoteLocation = (String)config.getJSONObject("java").get(("location"));
                        URL java = new URL(javaRemoteLocation + Utilities.getOSandArch() + ".tar.gz");
                        textArea.append("Downloading Java from " + java.toString() + "\n");

                        download(java, path.getAbsolutePath(), "oraclelicense=accept-securebackup-cookie");
                        
                        File from = new File(path.getAbsolutePath() + File.separator + "xmage.dl");
                        textArea.append("Installing Java ...\n");

                        extract(from, javaFolder);
                        textArea.append("Done\n");
                        progressBar.setValue(0);
                        if (!from.delete()) {
                            textArea.append("ERROR: could not cleanup temporary files\n");
                            logger.error("Error: could not cleanup temporary files");
                        }
                        Config.setInstalledJavaVersion(javaAvailableVersion);
                    }
                }
            }
            catch (IOException | JSONException ex) {
                progressBar.setValue(0);
                this.cancel(true);
                logger.error("Error: ", ex);
            }
            return null;
        }
        
        @Override
        public void done() {
            latch.countDown();
        }
    }
    
    private class DownloadXMageTask extends DownloadTask {
        
        private final CountDownLatch latch;

        public DownloadXMageTask(CountDownLatch latch, JProgressBar progressBar) {
            super(progressBar);
            this.latch = latch;
        }

        @Override
        protected Void doInBackground() {
            try {
                latch.await();
                File xmageFolder = new File(path.getAbsolutePath() + File.separator + "xmage");
                String xmageAvailableVersion = (String)config.getJSONObject("XMage").get(("version"));
                String xmageInstalledVersion = Config.getInstalledXMageVersion();
                textArea.append("XMage version installed:  " + xmageInstalledVersion + "\n");
                textArea.append("XMage version available:  " + xmageAvailableVersion + "\n");                
                if (!xmageAvailableVersion.equals(xmageInstalledVersion)) {
                    textArea.append("New version of XMage available.  \n");
                    int response = JOptionPane.showConfirmDialog(frame, "A newer version of XMage is available.  Would you like to install it?", "New Version Available", JOptionPane.YES_NO_OPTION);
                    if (response == JOptionPane.YES_OPTION) {
                        if (xmageFolder.isDirectory()) {  //remove existing install
                            textArea.append("Removing old files ...\n");
                            removeXMageFiles(xmageFolder);
                        }
                        xmageFolder.mkdirs();
                        String xmageRemoteLocation = (String)config.getJSONObject("XMage").get(("location"));
                        URL xmage = new URL(xmageRemoteLocation);
                        textArea.append("Downloading XMage from " + xmage.toString() + "\n");

                        download(xmage, path.getAbsolutePath(), "");

                        File from = new File(path.getAbsolutePath() + File.separator + "xmage.dl");
                        textArea.append("Installing XMage ...\n");

                        unzip(from, xmageFolder);
                        textArea.append("Done\n");
                        progressBar.setValue(0);
                        if (!from.delete()) {
                            textArea.append("ERROR: could not cleanup temporary files\n");
                            logger.error("Error: could not cleanup temporary files");
                        }
                        Config.setInstalledXMageVersion(xmageAvailableVersion);
                    }
                }
            }
            catch (IOException | JSONException | InterruptedException ex) {
                progressBar.setValue(0);
                this.cancel(true);
                logger.error("Error: ", ex);
            }
            return null;
        }
        
        private void removeXMageFiles(File xmageFolder) {
            // keep images folder -- no need to make users download these again
            File[] files = xmageFolder.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(final File dir, final String name) {
                    return !name.matches("images");
                }
            } );
            for (final File file : files) {
                if (file.isDirectory()) {
                    removeXMageFiles(file);
                }
                else if (!file.delete()) {
                    logger.error("Can't remove " + file.getAbsolutePath());
               }
            }
        }
        
        @Override
        public void done() {
            enableButtons();
        }

    }

}
