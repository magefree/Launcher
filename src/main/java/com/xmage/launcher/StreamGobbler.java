package com.xmage.launcher;

import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * thanks to:  http://www.javaworld.com/jw-12-2000/jw-1229-traps.html?page=4
 *
 * @author BetaSteward
 */
public class StreamGobbler extends Thread {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(StreamGobbler.class);

    private final InputStream is;
    private final JTextArea text;

    public StreamGobbler(InputStream is, JTextArea text) {
        this.is = is;
        this.text = text;
    }

    @Override
    public void run() {
        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                appendLine(line);
            }
        } catch (IOException ex) {
            appendLine(ex.toString()); // note below
            logger.error("Error processing stream", ex);
        }
    }

    private void appendLine(final String line) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                text.append(line + "\n"); // JTextArea.append is NOT thread safe, see
                // http://stackoverflow.com/questions/8436949/thread-safety-of-jtextarea-append
            }
        });
    }
}