package com.xmage.launcher;

import com.xmage.launcher.DownloadTask.Progress;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.*;
import java.net.URL;
import java.util.List;

/**
 * @author BetaSteward
 */
public abstract class DownloadTask extends SwingWorker<Void, Progress> {

    public static class Progress {
        String text;
        Integer perc;

        public Progress(String text) {
            this.text = text;
            perc = -1;
        }

        public Progress(Integer perc) {
            this.perc = perc;
            text = null;
        }

    }

    private static final int BUFFER_SIZE = 4096;
    private static final Logger logger = LoggerFactory.getLogger(DownloadTask.class);

    private final JProgressBar progressBar;
    private final JTextArea textArea;

    public DownloadTask(JProgressBar progressBar, JTextArea textArea) {
        this.progressBar = progressBar;
        this.textArea = textArea;
    }

    protected boolean download(URL downloadURL, String saveDirectory, String cookies) throws IOException {
        try {
            Downloader dl = new Downloader();
            dl.connect(downloadURL, cookies);

            BufferedInputStream in = dl.getInputStream();

            File temp = new File(saveDirectory + File.separator + "xmage.dl");
            FileOutputStream fout = new FileOutputStream(temp);

            final byte[] data = new byte[BUFFER_SIZE];
            int count;
            long total = 0;
            long size = dl.getSize();
            publish(0);
            while ((count = in.read(data, 0, BUFFER_SIZE)) != -1) {
                fout.write(data, 0, count);
                total += count;
                publish((int) (total * 100 / size));
            }
            fout.close();
            dl.disconnect();
            return true;
        } catch (IOException ex) {
            publish(0);
            cancel(true);
            logger.error("Error: ", ex);
            return false;
        }
    }

    protected void publish(int perc) {
        publish(new Progress(perc));
    }

    protected void publish(String text) {
        publish(new Progress(text));
    }

    @Override
    protected void process(List<Progress> chunks) {
        for (Progress chunk : chunks) {
            if (chunk.perc >= 0) {
                progressBar.setValue(chunk.perc);
            }
            if (chunk.text != null) {
                textArea.append(chunk.text);
            }
        }
    }

    protected void extract(File from, File to) throws IOException {

        TarArchiveInputStream tarIn = new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(from)));

        // first calculate the aggregate size for displaying progress
        publish(0);
        TarArchiveEntry tarEntry;
        long size = 0;
        while ((tarEntry = (TarArchiveEntry) tarIn.getNextEntry()) != null) {
            size += tarEntry.getSize();
        }
        tarIn.close();

        // now write out the files
        long total = 0;
        tarIn = new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(from)));
        while ((tarEntry = (TarArchiveEntry) tarIn.getNextEntry()) != null) {
            File destPath = new File(to, tarEntry.getName());
            int mode = tarEntry.getMode();
            if (tarEntry.isDirectory()) {
                destPath.mkdirs();
            } else {
                destPath.createNewFile();
                byte[] data = new byte[BUFFER_SIZE];
                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(destPath), BUFFER_SIZE);
                int count;
                while ((count = tarIn.read(data, 0, BUFFER_SIZE)) != -1) {
                    out.write(data, 0, count);
                }
                out.close();
                total += tarEntry.getSize();
                publish((int) (total * 100 / size));
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

    private void setFilePermissions(File file, int mode) {
        if ((mode & EVERYONE_READ) == EVERYONE_READ) {
            file.setReadable(true, false);
        } else if ((mode & OWNER_READ) == OWNER_READ) {
            file.setReadable(true, true);
        } else {
            file.setReadable(false, false);
        }
        if ((mode & EVERYONE_WRITE) == EVERYONE_WRITE) {
            file.setWritable(true, false);
        } else if ((mode & OWNER_WRITE) == OWNER_WRITE) {
            file.setWritable(true, true);
        } else {
            file.setWritable(false, false);
        }
        if ((mode & EVERYONE_EXEC) == EVERYONE_EXEC) {
            file.setExecutable(true, false);
        } else if ((mode & OWNER_EXEC) == OWNER_EXEC) {
            file.setExecutable(true, true);
        } else {
            file.setExecutable(false, false);
        }
    }

    protected void unzip(File from, File to) throws IOException {

        ZipArchiveInputStream zipIn = new ZipArchiveInputStream(new FileInputStream(from));

        // first calculate the aggregate size for displaying progress
        publish(0);
        ZipArchiveEntry zipEntry;
        long size = 0;
        while ((zipEntry = (ZipArchiveEntry) zipIn.getNextEntry()) != null) {
            size += zipEntry.getSize();
        }
        zipIn.close();

        // now write out the files
        long total = 0;
        zipIn = new ZipArchiveInputStream(new FileInputStream(from));
        while ((zipEntry = (ZipArchiveEntry) zipIn.getNextEntry()) != null) {
            File destPath = new File(to, zipEntry.getName());
            if (zipEntry.isDirectory()) {
                destPath.mkdirs();
            } else {
                File pathFile = new File(destPath.getAbsolutePath().substring(0, destPath.getAbsolutePath().lastIndexOf(File.separator)));
                pathFile.mkdirs();
                destPath.createNewFile();
                byte[] data = new byte[BUFFER_SIZE];
                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(destPath), BUFFER_SIZE);
                int count;
                while ((count = zipIn.read(data, 0, BUFFER_SIZE)) != -1) {
                    out.write(data, 0, count);
                }
                out.close();
                total += zipEntry.getSize();
                publish((int) (total * 100 / size));
            }
        }
        zipIn.close();
    }

}
