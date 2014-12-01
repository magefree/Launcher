package com.xmage.launcher;

import static com.xmage.launcher.Utilities.getInstallPath;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import org.slf4j.LoggerFactory;

/**
 *
 * @author BetaSteward
 */
public class Config {
    private static final String PROPERTIES_FILE = "installed.properties";
    private static final String VERSION_FILE = "/version.properties";
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Config.class);    
    private static final Properties props = new Properties();
    private static final String DEFAULT_URL = "http://xmage.info/xmage";
    private static final String DEFAULT_CLIENT_JAVA_OPTS = "-Xms256m -Xmx512m -XX:MaxPermSize=384m -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled";
    private static final String DEFAULT_SERVER_JAVA_OPTS = "-Xms256M -Xmx1G -XX:MaxPermSize=384m";
    
    private static String version = "";
    private static String installedJavaVersion = "";
    private static String installedXMageVersion = "";
    private static String homeURL = "";
    private static boolean useTorrent = false;
    private static String clientJavaOpts = "";
    private static String serverJavaOpts = "";

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
            homeURL = props.getProperty("xmage.home", DEFAULT_URL);
            useTorrent = Boolean.parseBoolean(props.getProperty("xmage.torrent.use", "False"));
        
        } catch (IOException ex) {
            logger.error("Error: ", ex);
        }
    }

    public static String getInstalledJavaVersion() {
        return installedJavaVersion;
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

    public static void saveProperties() {
        try {
            File properties = new File(getInstallPath(), PROPERTIES_FILE);
            FileOutputStream out = new FileOutputStream(properties);
            props.setProperty("java.version", installedJavaVersion);
            props.setProperty("xmage.version", installedXMageVersion);
            props.setProperty("xmage.client.javaopts", clientJavaOpts);
            props.setProperty("xmage.server.javaopts", serverJavaOpts);
            props.setProperty("xmage.home", homeURL);
            props.setProperty("xmage.torrent.use", Boolean.toString(useTorrent));
            props.store(out, "---Installed versions---");
            out.close();
        } catch (IOException ex) {
            logger.error("Error: ", ex);
        }
    }
    
}
