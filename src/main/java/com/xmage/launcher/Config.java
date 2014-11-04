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
    
    private static String version = "";
    private static String installedJavaVersion = "";
    private static String installedXMageVersion = "";
    private static String homeURL = "";

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
            homeURL = props.getProperty("xmage.home", DEFAULT_URL);
        
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

    public static void setInstalledJavaVersion(String version) {
        installedJavaVersion = version;
    }

    public static void setInstalledXMageVersion(String version) {
        installedXMageVersion = version;
    }

    public static void saveProperties() {
        try {
            File properties = new File(getInstallPath(), PROPERTIES_FILE);
            FileOutputStream out = new FileOutputStream(properties);
            props.setProperty("java.version", installedJavaVersion);
            props.setProperty("xmage.version", installedXMageVersion);
            props.setProperty("xmage.home", homeURL);
            props.store(out, "---Installed versions---");
            out.close();
        } catch (IOException ex) {
            logger.error("Error: ", ex);
        }
    }
    
}
