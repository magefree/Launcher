
package com.xmage.launcher;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author BetaSteward
 */
public class Utilities {
    private static final String OS_name = System.getProperty("os.name").toLowerCase();
    private static final int BUFFER_SIZE = 4096;
    
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
            Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
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
                OS_arch = getArch().equals("64") ? "windows-x64" : "windows-i586";
                break;
            case OSX:
                OS_arch = "macosx-x64";
                break;
            case NIX:
                OS_arch = "linux-i586";  // can't find an easy way to get OS arch
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

    
    public static void extract(File dest) {
        try {
            File temp = new File(dest, "xmage.dl");
            TarArchiveInputStream tarIn = new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(temp)));
            //InputStream in = new BufferedInputStream(oracle.openStream());
            //TarArchiveInputStream tarIn = new TarArchiveInputStream(new GzipCompressorInputStream(in));
            
            TarArchiveEntry tarEntry;
            while ((tarEntry = (TarArchiveEntry) tarIn.getNextEntry()) != null) {
                File destPath = new File(dest, tarEntry.getName());
                if (tarEntry.isDirectory()) {
                    destPath.mkdirs();
                } else {
                    destPath.createNewFile();
                    byte data[] = new byte[BUFFER_SIZE];
                    BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(destPath), BUFFER_SIZE);
                    int count = 0;
                    while ((count = tarIn.read(data, 0, BUFFER_SIZE)) != -1) {
                        out.write(data, 0, count);
                    }
                    out.close();
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
