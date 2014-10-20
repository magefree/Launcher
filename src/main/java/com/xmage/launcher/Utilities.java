
package com.xmage.launcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JTextArea;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

/**
 *
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
        if (OS_name.contains("win")) 
            return OS.WIN;
        if (OS_name.contains("mac")) 
            return OS.OSX;
        if (OS_name.contains("nix") || OS_name.contains("nux")) 
            return OS.NIX;
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
        InputStream is = url.openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            JSONObject json = new JSONObject(readAll(rd));
            return json;
        } finally {
            is.close();
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

    public static Process launchClientProcess(JTextArea out) {
        
        return launchProcess("mage.client.MageFrame", "-Xms256m -Xmx512m -XX:MaxPermSize=384m -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled", "mage-client", out);
        
    }

    public static Process launchServerProcess(JTextArea out) {
        
        return launchProcess("mage.server.Main", "-Xms256M -Xmx512M -XX:MaxPermSize=384m", "mage-server", out);
        
    }
    
    public static void stopProcess(Process p) {
        p.destroy();
    }
    
    private static Process launchProcess(String main, String args, String path, JTextArea out) {
        
        File installPath = Utilities.getInstallPath();
        File javaHome = new File(installPath, "/java/jre" + Config.getInstalledJavaVersion());
        File javaBin = new File(javaHome, "/bin/java");
        File xmagePath = new File(installPath, "/xmage/" + path);
        File classPath = new File(xmagePath, "/lib/*");

        ArrayList<String> command = new ArrayList<String>();
        command.add(javaBin.getAbsolutePath());
        command.addAll(Arrays.asList(args.split(" ")));
        command.add("-cp");
        command.add(classPath.getAbsolutePath());
        command.add(main);

        ProcessBuilder pb = new ProcessBuilder(command.toArray(new String[command.size()]));
        pb.environment().putAll(System.getenv());
        pb.environment().put("JAVA_HOME", javaHome.getAbsolutePath());
        pb.directory(xmagePath);
        pb.redirectErrorStream(true);
        try {
            Process p = pb.start();
            StreamGobbler outGobbler = new StreamGobbler(p.getInputStream(), out);
            outGobbler.start();
            return p;
        } catch (IOException ex) {
            logger.error("Error staring process", ex);
        }
        return null;
    }

}

class StreamGobbler extends Thread
{
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(StreamGobbler.class);
    
    private final InputStream is;
    private final JTextArea text;

    public StreamGobbler(InputStream is, JTextArea text)
    {
        this.is = is;
        this.text = text;
    }

    @Override
    public void run()
    {
        try
        {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;
            while ( (line = br.readLine()) != null)
            {
                text.append(line + "\n"); // JTextArea.append is thread safe
            }
        }
        catch (IOException ex)
        {
            text.append(ex.toString()); // note below
            logger.error("Error processing stream", ex);
        }
    }
}