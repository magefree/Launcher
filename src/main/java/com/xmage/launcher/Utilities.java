package com.xmage.launcher;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author BetaSteward
 */
public class Utilities {

    private static final String OS_name = System.getProperty("os.name").toLowerCase();
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Utilities.class);

    public enum OS {

        WIN,
        NIX,
        OSX,
        UNKNOWN
    }

    public static File getInstallPath() {
        File path = null;
        try {
            path = new File(Utilities.class.getProtectionDomain().getCodeSource().getLocation().toURI().getSchemeSpecificPart()).getParentFile();
        } catch (URISyntaxException ex) {
            logger.error("Error: ", ex);
        }
        return path;
    }

    public static OS getOS() {
        if (OS_name.contains("win")) {
            return OS.WIN;
        }
        if (OS_name.contains("mac")) {
            return OS.OSX;
        }
        if (OS_name.contains("nix") || OS_name.contains("nux")) {
            return OS.NIX;
        }
        return OS.UNKNOWN;
    }

    public static String getArch() {
        String arch = System.getenv("PROCESSOR_ARCHITECTURE");
        String wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432");

        return arch.endsWith("64") || wow64Arch != null && wow64Arch.endsWith("64") ? "64" : "32";
    }

    public static String getOSandArch() {
        String OS_arch = "windows-i586";
        switch (getOS()) {
            case WIN:
                OS_arch = "windows-" + (getArch().equals("64") ? "x64" : "i586");
                break;
            case OSX:
                OS_arch = "macosx-x64";
                break;
            case NIX:
                String arch = System.getProperty("os.arch");
                OS_arch = "linux-" + (arch.startsWith("i") ? "i586" : "x64"); // assume arch is same as jvm arch
                break;
        }
        return OS_arch;
    }

    //thanks to Roland Illig - http://stackoverflow.com/questions/4308554/simplest-way-to-read-json-from-a-url-in-java
    public static JSONObject readJsonFromUrl(URL url) throws IOException, JSONException {
        try (BufferedReader rd = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
            return new JSONObject(readAll(rd));
        }
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public static Process launchClientProcess() {

        return launchProcess("mage.client.MageFrame",
                "",
                "mage-client",
                Config.getInstance().getClientJavaOpts());

    }

    public static Process launchServerProcess() {

        return launchProcess("mage.server.Main",
                Config.getInstance().isServerTestMode() ? "-testMode=true" : "",
                "mage-server",
                Config.getInstance().getServerJavaOpts());

    }

    public static void stopProcess(Process p) {
        p.destroy();
    }

    private static Process launchProcess(String main, String args, String path, String javaOpts) {

        File installPath = Utilities.getInstallPath();
        File javaHome;
        if (Config.getInstance().useSystemJava()) {
            javaHome = new File(System.getProperty("java.home"));
        } else if (getOS() == OS.OSX) {
            javaHome = new File(installPath, "/java/jre" + Config.getInstance().getInstalledJavaVersion() + ".jre/Contents/Home");
        } else {
            javaHome = new File(installPath, "/java/jre" + Config.getInstance().getInstalledJavaVersion());
        }
        File javaBin = new File(javaHome, "/bin/java");
        File xmagePath = new File(installPath, "/xmage/" + path);
        File classPath = new File(xmagePath, "/lib/*");

        if (!javaBin.getParentFile().exists() || !xmagePath.isDirectory()) {
            return null;
        }

        logger.info("Launching Process:");
        logger.info("Java bin: " + javaBin);
        logger.info("XMage Path: " + xmagePath);
        logger.info("Class Path: " + classPath);

        ArrayList<String> command = new ArrayList<>();
        command.add(javaBin.getAbsolutePath());
        command.addAll(Arrays.asList(javaOpts.split(" ")));
        command.add("-cp");
        command.add(classPath.getAbsolutePath());
        command.add(main);
        command.addAll(Arrays.asList(args.split(" ")));

        ProcessBuilder pb = new ProcessBuilder(command.toArray(new String[command.size()]));
        pb.environment().putAll(System.getenv());
        pb.environment().put("JAVA_HOME", javaHome.getAbsolutePath());
        pb.directory(xmagePath);
        pb.redirectErrorStream(true);
        try {
            return pb.start();
        } catch (IOException ex) {
            logger.error("Error staring process", ex);
        }
        return null;
    }

    public static void restart(File launcherJar) {
        File installPath = Utilities.getInstallPath();
        String javaBin = System.getProperty("java.home") + "/bin/java";

        ArrayList<String> command = new ArrayList<>();
        command.add(javaBin);
        command.add("-jar");
        command.add(launcherJar.getPath());

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.environment().putAll(System.getenv());
        pb.directory(installPath);
        try {
            pb.start();
            System.exit(0);
        } catch (IOException ex) {
            logger.error("Error restarting launcher", ex);
        }
    }

}
