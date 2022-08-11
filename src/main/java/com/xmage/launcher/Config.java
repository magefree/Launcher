package com.xmage.launcher;

import com.xmage.launcher.Utilities.OS;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.*;
import java.util.Properties;

import static com.xmage.launcher.Utilities.getInstallPath;

/**
 * @author BetaSteward, ldeluigi
 */
public class Config {
    private static final String PROPERTIES_FILE = "installed.properties";
    private static final String VERSION_FILE = "/version.properties";
    private static final String DEFAULT_URL = "http://xmage.de/xmage";
    private static final String BETA_URL = "http://xmage.today";
    private static final String DEFAULT_CLIENT_JAVA_OPTS = "-Xmx1000m -Dfile.encoding=UTF-8";
    private static final String DEFAULT_SERVER_JAVA_OPTS = "-Xmx500m";

    // Singleton
    private static Config currentConfig = new Config(false);

    // Static methods
    public static Config getInstance() {
        return Config.currentConfig;
    }

    public static void resetInstance() {
        Config.currentConfig = new Config(true);
    }

    // Configs
    private final org.slf4j.Logger logger = LoggerFactory.getLogger(Config.class);
    private final XMageBranch[] xMageBranches = new XMageBranch[]{
            new XMageBranch("Stable", DEFAULT_URL),
            new XMageBranch("Beta", BETA_URL),
            new XMageBranch("Custom", null)
    };

    private String version = "";
    private String installedJavaVersion = "";
    private String installedXMageVersion = "";
    private String homeURL = DEFAULT_URL;
    private String clientJavaOpts = DEFAULT_CLIENT_JAVA_OPTS;
    private String serverJavaOpts = DEFAULT_SERVER_JAVA_OPTS;
    private int guiSize = getScreenDPI() / 6;
    private boolean showClientConsole = true;
    private boolean showServerConsole = true;
    private boolean useSystemJava = false;
    private boolean serverTestMode = false;
    private int clientStartDelaySeconds = 2;

    private final Properties props = new Properties();


    private Config(boolean reset) {
        try {
            props.load(Config.class.getResourceAsStream(VERSION_FILE));
            this.version = props.getProperty("xmage.launcher.version", this.version);
            final File properties = new File(getInstallPath().getAbsolutePath(), PROPERTIES_FILE);
            if (!properties.isFile() && !properties.createNewFile()) {
                throw new IOException("Error creating properties file: " + properties.getAbsolutePath());
            }
            try (FileInputStream in = new FileInputStream(properties)) {
                props.load(in);
                this.installedJavaVersion = props.getProperty("java.version", this.installedJavaVersion);
                this.installedXMageVersion = props.getProperty("xmage.version", this.installedJavaVersion);
                if (!reset) {
                    this.clientJavaOpts = props.getProperty("xmage.client.javaopts", this.clientJavaOpts);
                    this.serverJavaOpts = props.getProperty("xmage.server.javaopts", this.serverJavaOpts);
                    int screenResolution = getScreenDPI();
                    this.logger.info("Detected screen DPI: " + screenResolution);
                    this.guiSize = Integer.parseInt(props.getProperty("xmage.launcher.guisize", String.valueOf(this.guiSize)));
                    this.guiSize = Math.max(this.guiSize, 10);
                    this.homeURL = props.getProperty("xmage.home", this.homeURL);
                    this.showClientConsole = Boolean.parseBoolean(props.getProperty("xmage.client.console", Boolean.toString(this.showClientConsole)));
                    this.showServerConsole = Boolean.parseBoolean(props.getProperty("xmage.server.console", Boolean.toString(this.showServerConsole)));
                    this.useSystemJava = Boolean.parseBoolean(props.getProperty("xmage.java.usesystem", Boolean.toString(this.useSystemJava)));
                    this.serverTestMode = Boolean.parseBoolean(props.getProperty("xmage.server.testmode", Boolean.toString(this.serverTestMode)));
                    this.clientStartDelaySeconds = Integer.parseInt(props.getProperty("xmage.launcher.client.start.delay", String.valueOf(this.clientStartDelaySeconds)));
                }
            }
        } catch (IOException ex) {
            logger.error("Error: ", ex);
        }
    }

    // Getters

    public String getInstalledJavaVersion() {
        return this.installedJavaVersion;
    }

    private int getScreenDPI() {
        int result = 0;
        if (Utilities.getOS() == OS.NIX) { // on Linux the default method always return 96 or 93
            ProcessBuilder processBuilder = new ProcessBuilder("xrdb", "-q");
            Process process;
            try {
                process = processBuilder.start();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (line.startsWith("Xft.dpi:")) {
                            String dpi = line.replaceAll("[^\\d]*(\\d*)$", "$1");
                            try {
                                result = Integer.parseInt(dpi);
                            } catch (NumberFormatException e) {
                                // do nothing, something's wrong, resorting to the default method
                            }
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                logger.warn("Can't get DPI via xrdb!");
                e.printStackTrace();
            }
            if (result > 0) {
                return result;
            }
        }
        return Toolkit.getDefaultToolkit().getScreenResolution();
    }

    public String getInstalledXMageVersion() {
        return this.installedXMageVersion;
    }

    public String getXMageHome() {
        return this.homeURL;
    }

    public String getVersion() {
        return this.version;
    }

    public String getClientJavaOpts() {
        return this.clientJavaOpts;
    }

    public String getServerJavaOpts() {
        return this.serverJavaOpts;
    }

    public int getGuiSize() {
        return this.guiSize;
    }

    public boolean isShowClientConsole() {
        return this.showClientConsole;
    }

    public boolean isShowServerConsole() {
        return this.showServerConsole;
    }

    public XMageBranch[] getXMageBranches() {
        return this.xMageBranches;
    }

    public XMageBranch getXMageBranchByUrl(String url) {
        for (XMageBranch b : this.xMageBranches) {
            if (b.url != null && b.url.equals(url)) {
                return b;
            }
        }
        return this.xMageBranches[this.xMageBranches.length - 1]; // Custom
    }

    public boolean useSystemJava() {
        return this.useSystemJava;
    }

    public boolean isServerTestMode() {
        return this.serverTestMode;
    }

    public int getClientStartDelaySeconds() {
        return this.clientStartDelaySeconds;
    }

    public int getClientStartDelayMilliseconds() {
        return this.getClientStartDelaySeconds() * 1000;
    }

    // Setters

    public void setInstalledJavaVersion(String version) {
        this.installedJavaVersion = version;
    }

    public void setInstalledXMageVersion(String version) {
        this.installedXMageVersion = version;
    }

    public void setClientJavaOpts(String opts) {
        this.clientJavaOpts = opts;
    }

    public void setServerJavaOpts(String opts) {
        this.serverJavaOpts = opts;
    }

    public void setXMageHome(String url) {
        this.homeURL = url;
    }

    public void setGuiSize(int size) {
        this.guiSize = size;
    }

    public void setShowClientConsole(boolean show) {
        this.showClientConsole = show;
    }

    public void setShowServerConsole(boolean show) {
        this.showServerConsole = show;
    }

    public void setUseSystemJava(boolean b) {
        this.useSystemJava = b;
    }

    public void setServerTestMode(boolean serverTestMode) {
        this.serverTestMode = serverTestMode;
    }

    public void setClientStartDelaySeconds(final int seconds) {
        this.clientStartDelaySeconds = seconds;
    }

    public void saveProperties() {
        File properties = new File(getInstallPath(), PROPERTIES_FILE);
        try (final FileOutputStream out = new FileOutputStream(properties)) {
            props.setProperty("java.version", installedJavaVersion);
            props.setProperty("xmage.version", installedXMageVersion);
            props.setProperty("xmage.client.javaopts", clientJavaOpts);
            props.setProperty("xmage.server.javaopts", serverJavaOpts);
            props.setProperty("xmage.launcher.guisize", Integer.toString(guiSize));
            props.setProperty("xmage.home", homeURL);
            props.setProperty("xmage.client.console", Boolean.toString(showClientConsole));
            props.setProperty("xmage.server.console", Boolean.toString(showServerConsole));
            props.setProperty("xmage.java.usesystem", Boolean.toString(useSystemJava));
            props.setProperty("xmage.server.testmode", Boolean.toString(serverTestMode));
            props.setProperty("xmage.launcher.client.start.delay", Integer.toString(clientStartDelaySeconds));
            props.store(out, "---XMage Properties---");
        } catch (IOException ex) {
            logger.error("Error: ", ex);
        }
    }

}
