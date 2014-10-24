package com.xmage.launcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.swing.JTextArea;
import org.slf4j.LoggerFactory;

/**
 * thanks to:  http://www.javaworld.com/jw-12-2000/jw-1229-traps.html?page=4
 * 
 * @author BetaSteward
 */
public class StreamGobbler extends Thread
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