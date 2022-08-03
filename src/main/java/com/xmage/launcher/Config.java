package com.xmage.launcher;

import static com.xmage.launcher.Utilities.getInstallPath;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.slf4j.LoggerFactory;
import com.xmage.launcher.Utilities.OS;

/**
 *
 * @author BetaSteward
 */
public class Config {

    private static final String PROPERTIES_FILE = "installed.properties";
    private static final String VERSION_FILE = "/version.properties";
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Config.class);
    private static final Properties props = new Properties();
    private static final String DEFAULT_URL = "http://xmage.de/xmage";
    private static final String BETA_URL = "http://rkfg.me/xmage";
    private static final String DEFAULT_CLIENT_JAVA_OPTS = "-Xms256m -Xmx512m -XX:MaxPermSize=384m -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled -Dfile.encoding=UTF-8";
    private static final String DEFAULT_SERVER_JAVA_OPTS = "-Xms256M -Xmx1G -XX:MaxPermSize=384m";

    private static String version = "";
    private static String installedJavaVersion = "";
    private static String installedXMageVersion = "";
    private static String homeURL = "";
    private static boolean useTorrent = false;
    private static int torrentUpRate = 0;
    private static int torrentDownRate = 0;
    private static String clientJavaOpts = "";
    private static String serverJavaOpts = "";
    private static int guiSize = 0;
    private static boolean showClientConsole = true;
    private static boolean showServerConsole = true;
    private static XMageBranch[] xMageBranches = new XMageBranch[] { new XMageBranch("Stable", DEFAULT_URL), new XMageBranch("Beta", BETA_URL),
            new XMageBranch("Custom", null) };
    private static Map<String, XMageBranch> branchMap = new HashMap<String, XMageBranch>();

    static {
        try {
            props.load(Config.class.getResourceAsStream(VERSION_FILE));
            version = props.getProperty("xmage.launcher.version", "");

            File properties = new File(getInstallPath().getAbsolutePath(), PROPERTIES_FILE);
            if (!properties.isFile() && !properties.createNewFile()) {
                throw new IOException("Error creating properties file: " + properties.getAbsolutePath());
            }
            FileInputStream in = new FileInputStream(properties);
            props.load(in);
            in.close();
            installedJavaVersion = props.getProperty("java.version", "");
            installedXMageVersion = props.getProperty("xmage.version", "");
            clientJavaOpts = props.getProperty("xmage.client.javaopts", DEFAULT_CLIENT_JAVA_OPTS);
            serverJavaOpts = props.getProperty("xmage.server.javaopts", DEFAULT_SERVER_JAVA_OPTS);
            int screenResolution = getScreenDPI();
            logger.info("Detected screen DPI: " + screenResolution);
            guiSize = Integer.parseInt(props.getProperty("xmage.launcher.guisize", String.valueOf(screenResolution / 6)));
            homeURL = props.getProperty("xmage.home", DEFAULT_URL);
            useTorrent = Boolean.parseBoolean(props.getProperty("xmage.torrent.use", "False"));
            torrentUpRate = Integer.parseInt(props.getProperty("xmage.torrent.uprate", "50"));
            torrentDownRate = Integer.parseInt(props.getProperty("xmage.torrent.downrate", "0"));
            showClientConsole = Boolean.parseBoolean(props.getProperty("xmage.client.console", "True"));
            showServerConsole = Boolean.parseBoolean(props.getProperty("xmage.server.console", "True"));
            for (XMageBranch xMageBranch : xMageBranches) {
                if (xMageBranch.url != null) {
                    branchMap.put(xMageBranch.url, xMageBranch);
                }
            }
        } catch (IOException ex) {
            logger.error("Error: ", ex);
        }
    }

    public static String getInstalledJavaVersion() {
        return installedJavaVersion;
    }

    private static int getScreenDPI() {
        int result = 0;
        if (Utilities.getOS() == OS.NIX) { // on Linux the default method always return 96 or 93
            ProcessBuilder processBuilder = new ProcessBuilder("xrdb", "-q");
            Process process;
            try {
                process = processBuilder.start();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        if (line.startsWith("Xft.dpi:")) {
                            String dpi = line.replaceAll("[^\\d]*(\\d*)$", "$1");
                            try {
                                result = Integer.valueOf(dpi);
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

    public static String getInstalledXMageVersion() {
        return installedXMageVersion;
    }

    public static String getXMageHome() {
        return homeURL;
    }

    public static String getVersion() {
        return version;
    }

    public static String getClientJavaOpts() {
        return clientJavaOpts;
    }

    public static String getServerJavaOpts() {
        return serverJavaOpts;
    }

    public static boolean isUseTorrent() {
        return useTorrent;
    }

    public static int getTorrentUpRate() {
        return torrentUpRate;
    }

    public static int getGuiSize() {
        return guiSize;
    }

    public static int getTorrentDownRate() {
        return torrentDownRate;
    }

    public static boolean isShowClientConsole() {
        return showClientConsole;
    }

    public static boolean isShowServerConsole() {
        return showServerConsole;
    }

    public static void setInstalledJavaVersion(String version) {
        installedJavaVersion = version;
    }

    public static void setInstalledXMageVersion(String version) {
        installedXMageVersion = version;
    }

    public static void setClientJavaOpts(String opts) {
        clientJavaOpts = opts;
    }

    public static void setServerJavaOpts(String opts) {
        serverJavaOpts = opts;
    }

    public static void setXMageHome(String url) {
        homeURL = url;
    }

    public static void setUseTorrent(boolean use) {
        useTorrent = use;
    }

    public static void setTorrentUpRate(int rate) {
        torrentUpRate = rate;
    }

    public static void setGuiSize(int size) {
        guiSize = size;
    }

    public static void setTorrentDownRate(int rate) {
        torrentDownRate = rate;
    }

    public static void setShowClientConsole(boolean show) {
        showClientConsole = show;
    }

    public static void setShowServerConsole(boolean show) {
        showServerConsole = show;
    }

    public static void saveProperties() {
        try {
            File properties = new File(getInstallPath(), PROPERTIES_FILE);
            FileOutputStream out = new FileOutputStream(properties);
            props.setProperty("java.version", installedJavaVersion);
            props.setProperty("xmage.version", installedXMageVersion);
            props.setProperty("xmage.client.javaopts", clientJavaOpts);
            props.setProperty("xmage.server.javaopts", serverJavaOpts);
            props.setProperty("xmage.launcher.guisize", Integer.toString(guiSize));
            props.setProperty("xmage.home", homeURL);
            props.setProperty("xmage.client.console", Boolean.toString(showClientConsole));
            props.setProperty("xmage.server.console", Boolean.toString(showServerConsole));
            props.setProperty("xmage.torrent.use", Boolean.toString(useTorrent));
            props.setProperty("xmage.torrent.uprate", Integer.toString(torrentUpRate));
            props.setProperty("xmage.torrent.downrate", Integer.toString(torrentDownRate));
            props.store(out, "---XMage Properties---");
            out.close();
        } catch (IOException ex) {
            logger.error("Error: ", ex);
        }
    }

    public static XMageBranch[] getXMageBranches() {
        return xMageBranches;
    }

    public static XMageBranch getXMageBranchByUrl(String url) {
        XMageBranch xMageBranch = branchMap.get(url);
        if (xMageBranch == null) {
            return xMageBranches[xMageBranches.length - 1]; // custom
        }
        return xMageBranch;
    }

}
