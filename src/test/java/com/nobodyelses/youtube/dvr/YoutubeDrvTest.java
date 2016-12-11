package com.nobodyelses.youtube.dvr;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import junit.framework.TestCase;

public class YoutubeDrvTest extends TestCase {
    public void test() {
        while(true) {
            final YoutubeDvrRunner dvr = new YoutubeDvrRunner("https://www.youtube.com/watch?v=Ga3maNZ0x0w", "11/20/2016 17:58");

            dvr.start();

            try {
                dvr.join();
            } catch (InterruptedException e1) {
            }
        }
    }

    public void testGet2() throws Exception {
        URL url = new URL("http://localhost:8888/youtube?uri=Testxyz&id=22");
        URLConnection conn = url.openConnection();
        InputStream is = conn.getInputStream();
        String string = convertStreamToString(is);
        System.out.println(string);

        String[] split = string.split("=", 2);
        List<String> list = Arrays.asList(split);
        System.out.println(list);

        URL url2 = new URL("http://localhost:8888/youtube");
        URLConnection conn2 = url2.openConnection();
        InputStream is2 = conn2.getInputStream();
        String string2 = convertStreamToString(is2);
        System.out.println(string2);
    }

    public static String convertStreamToString(final InputStream is) {
        final java.util.Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
    public void testGet() throws Exception {
        URL url = new URL("http://www.google.com");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setUseCaches(false);
        connection.setDefaultUseCaches(false);
        connection.setDoInput(true);
        connection.setDoOutput(true);

    }
}
