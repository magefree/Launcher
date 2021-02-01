package com.xmage.launcher;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author BetaSteward
 */
public class Downloader {

    private BufferedInputStream in;
    private HttpURLConnection conn;
    private int size;

    public void connect(URL url, String cookies) throws IOException {
        conn = (HttpURLConnection) url.openConnection();
        conn.setAllowUserInteraction(false);
        conn.setInstanceFollowRedirects(true);
        conn.setRequestProperty("Cookie", cookies);
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");

        while (true) {
            int status = conn.getResponseCode();
            if (status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_MOVED_PERM || status == HttpURLConnection.HTTP_SEE_OTHER) {
                String newUrl = conn.getHeaderField("Location");
                conn = (HttpURLConnection) new URL(newUrl).openConnection();
                conn.setRequestProperty("Cookie", cookies);
            } else {
                break;
            }
        }
        size = conn.getContentLength();
        in = new BufferedInputStream(conn.getInputStream());
    }

    public BufferedInputStream getInputStream() {
        return in;
    }

    public void disconnect() throws IOException {
        in.close();
        conn.disconnect();
    }

    public int getSize() {
        return size;
    }
}
