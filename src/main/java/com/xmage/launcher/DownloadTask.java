
package com.xmage.launcher;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

/**
 *
 * @author BetaSteward
 */
public abstract class DownloadTask extends SwingWorker<Void, Void> {

    private static final int BUFFER_SIZE = 4096;   
        
    @Override
    protected abstract Void doInBackground();
    
    protected void download(JProgressBar progressBar, URL downloadURL, String saveDirectory, String cookies) {
        try {
            Downloader dl = new Downloader();
            dl.connect(downloadURL, cookies);
            
            BufferedInputStream in = dl.getInputStream();
            
            File temp = new File(saveDirectory + File.separator + "xmage.dl");
            FileOutputStream fout = new FileOutputStream(temp);
            
            final byte data[] = new byte[BUFFER_SIZE];
            int count = -1;
            long total = 0;
            long size = dl.getSize();
            while ((count = in.read(data, 0, BUFFER_SIZE)) != -1) {
                fout.write(data, 0, count);
                total += count;
                progressBar.setValue((int)((total * 100)/size));
            }
            fout.close();
            dl.disconnect();
        }
        catch (IOException ex) {
            progressBar.setValue(0);
        }
    }
        
}
