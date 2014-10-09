
package com.xmage.launcher;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

/**
 *
 * @author BetaSteward
 */
public abstract class DownloadTask extends SwingWorker<Void, Void> {

    private static final int BUFFER_SIZE = 4096;
    
    private final JProgressBar progressBar;
    
    public DownloadTask(JProgressBar progressBar) {
        this.progressBar = progressBar;
    }
    
    @Override
    protected abstract Void doInBackground();
    
    protected void download(URL downloadURL, String saveDirectory, String cookies) throws IOException {
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
    }

    protected void extract(File from, File to) throws IOException {
        
        TarArchiveInputStream tarIn = new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(from)));

        progressBar.setValue(0);
        TarArchiveEntry tarEntry;
        long size = 0;
        while ((tarEntry = (TarArchiveEntry) tarIn.getNextEntry()) != null) {
            size += tarEntry.getSize();
        }
        tarIn.close();

        long total = 0;
        tarIn = new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(from)));
        while ((tarEntry = (TarArchiveEntry) tarIn.getNextEntry()) != null) {
            File destPath = new File(to, tarEntry.getName());
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
        }
        tarIn.close();
        
    }

}
