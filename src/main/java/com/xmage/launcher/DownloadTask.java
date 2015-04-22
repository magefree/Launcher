
package com.xmage.launcher;

import com.turn.ttorrent.client.Client;
import com.turn.ttorrent.client.SharedTorrent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author BetaSteward
 */
public abstract class DownloadTask extends SwingWorker<Void, Void> {

    private static final int BUFFER_SIZE = 4096;
    private static final Logger logger = LoggerFactory.getLogger(DownloadTask.class);    
    
    private final JProgressBar progressBar;
    
    public DownloadTask(JProgressBar progressBar) {
        this.progressBar = progressBar;
    }
        
    protected boolean download(URL downloadURL, String saveDirectory, String cookies) throws IOException {
        try {
            Downloader dl = new Downloader();
            dl.connect(downloadURL, cookies);

            BufferedInputStream in = dl.getInputStream();

            File temp = new File(saveDirectory + File.separator + "xmage.dl");
            FileOutputStream fout = new FileOutputStream(temp);

            final byte data[] = new byte[BUFFER_SIZE];
            int count;
            long total = 0;
            long size = dl.getSize();
            progressBar.setValue(0);
            while ((count = in.read(data, 0, BUFFER_SIZE)) != -1) {
                fout.write(data, 0, count);
                total += count;
                progressBar.setValue((int)((total * 100)/size));
            }
            fout.close();
            dl.disconnect();
            return true;
        }
        catch (IOException ex) {
            progressBar.setValue(0);
            this.cancel(true);
            logger.error("Error: ", ex);
            return false;
        }
    }

    public void torrent(File from, File to) throws IOException {
        // First, instantiate the Client object.
        SharedTorrent torrent = SharedTorrent.fromFile(from, to);
        
        Client client = new Client(InetAddress.getLocalHost(), torrent);

        client.setMaxDownloadRate((double)Config.getTorrentDownRate());
        client.setMaxUploadRate((double)Config.getTorrentUpRate());

        client.download();
        
        while (!torrent.isComplete()) {
            progressBar.setValue((int)torrent.getCompletion());
        }

    }
    
    protected void extract(File from, File to) throws IOException {
        
        TarArchiveInputStream tarIn = new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(from)));

        //first calculate the aggregate size for displaying progress
        progressBar.setValue(0);
        TarArchiveEntry tarEntry;
        long size = 0;
        while ((tarEntry = (TarArchiveEntry) tarIn.getNextEntry()) != null) {
            size += tarEntry.getSize();
        }
        tarIn.close();

        //now write out the files
        long total = 0;
        tarIn = new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(from)));
        while ((tarEntry = (TarArchiveEntry) tarIn.getNextEntry()) != null) {
            File destPath = new File(to, tarEntry.getName());
            int mode = tarEntry.getMode();
            if (tarEntry.isDirectory()) {
                destPath.mkdirs();
            } else {
                destPath.createNewFile();                
                byte data[] = new byte[BUFFER_SIZE];
                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(destPath), BUFFER_SIZE);
                int count;
                while ((count = tarIn.read(data, 0, BUFFER_SIZE)) != -1) {
                    out.write(data, 0, count);
                }
                out.close();
                total += tarEntry.getSize();
                progressBar.setValue((int)((total * 100)/size));
            }
            setFilePermissions(destPath, mode);
        }
        tarIn.close();
        
    }
    
    private static final int OWNER_READ = 256;
    private static final int OWNER_WRITE = 128;
    private static final int OWNER_EXEC = 64;

    private static final int EVERYONE_READ = 4;
    private static final int EVERYONE_WRITE = 2;
    private static final int EVERYONE_EXEC = 1;
    
    private void setFilePermissions(File file, int mode)  {
        if ((mode & EVERYONE_READ) == EVERYONE_READ) {
            file.setReadable(true, false);
        }
        else if ((mode & OWNER_READ) == OWNER_READ) {
            file.setReadable(true, true);
        }
        else {
            file.setReadable(false, false);
        }
        if ((mode & EVERYONE_WRITE) == EVERYONE_WRITE) {
            file.setWritable(true, false);
        }
        else if ((mode & OWNER_WRITE) == OWNER_WRITE) {
            file.setWritable(true, true);
        }
        else {
            file.setWritable(false, false);
        }
        if ((mode & EVERYONE_EXEC) == EVERYONE_EXEC) {
            file.setExecutable(true, false);
        }
        else if ((mode & OWNER_EXEC) == OWNER_EXEC) {
            file.setExecutable(true, true);
        }
        else {
            file.setExecutable(false, false);
        }
    }
    
    protected void unzip(File from, File to) throws IOException {
        
        ZipArchiveInputStream zipIn = new ZipArchiveInputStream(new FileInputStream(from));
        
        //first calculate the aggregate size for displaying progress
        progressBar.setValue(0);
        ZipArchiveEntry zipEntry;
        long size = 0;
        while ((zipEntry = (ZipArchiveEntry)zipIn.getNextEntry()) != null) {
            size += zipEntry.getSize();
        }
        zipIn.close();
        
        //now write out the files
        long total = 0;
        zipIn = new ZipArchiveInputStream(new FileInputStream(from));
        while ((zipEntry = (ZipArchiveEntry) zipIn.getNextEntry()) != null) {
            File destPath = new File(to, zipEntry.getName());
            if (zipEntry.isDirectory()) {
                destPath.mkdirs();
            } else {
                File pathFile = new File(destPath.getAbsolutePath().substring(0,destPath.getAbsolutePath().lastIndexOf(File.separator)));
                pathFile.mkdirs();
                destPath.createNewFile();
                byte data[] = new byte[BUFFER_SIZE];
                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(destPath), BUFFER_SIZE);
                int count;
                while ((count = zipIn.read(data, 0, BUFFER_SIZE)) != -1) {
                    out.write(data, 0, count);
                }
                out.close();
                total += zipEntry.getSize();
                progressBar.setValue((int)((total * 100)/size));
            }
        }
        zipIn.close();
    }

}
