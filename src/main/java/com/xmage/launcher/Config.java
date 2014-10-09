package com.xmage.launcher;

import static com.xmage.launcher.Utilities.getInstallPath;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author BetaSteward
 */
public class Config {
    private static final String PROPERTIES_FILE = "installed.properties";
    
    private static final Properties props = new Properties();
    
    private static String installedJavaVersion = "";
    private static String installedXMageVersion = "";
    private static String homeURL = "";

    static {
        try {
            File properties = new File(getInstallPath().getAbsolutePath(), PROPERTIES_FILE);
            if (!properties.isFile() && !properties.createNewFile()) {
                throw new IOException("Error creating properties file: " + properties.getAbsolutePath());
            }
            FileInputStream in = new FileInputStream(properties);
            props.load(in);
            in.close();
            installedJavaVersion = props.getProperty("java.version", "");
            installedXMageVersion = props.getProperty("xmage.version", "");
            homeURL = props.getProperty("xmage.home", "http://xmage.info/xmage");
        } catch (IOException ex) {
            Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
