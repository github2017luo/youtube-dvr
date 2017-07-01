package com.nobodyelses.youtube.dvr;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class YoutubeDvrRunner2 extends Thread {
    private AtomicBoolean running = new AtomicBoolean(false);
    private String youtubeUrl = "https://www.youtube.com/watch?v=Ga3maNZ0x0w";
    private String formatId = "95";
    private String start = null;
    private String end = null;

    private SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");

    public YoutubeDvrRunner2(String youtubeUrl, String start, String end) throws Exception {
        this.youtubeUrl = youtubeUrl;
        this.start  = start;
        this.end = end;

        System.out.print(MessageFormat.format("Record: {0}", youtubeUrl));
        if (start != null) {
            Date date = sdf.parse(start);
            System.out.print(MessageFormat.format(" at {0}", date));
        }
        if (end != null) {
            Date date = sdf.parse(end);
            System.out.print(MessageFormat.format(" end at {0}", date));
        }

        System.out.println();
    }

    @Override
    public void run() {
        running.set(true);

        String filename = null;
        long size = 0;
        int count = 0;

        while (running.get()) {
            ArrayList<Integer> pids = check("pgrep -i ffmpeg");
            boolean isRecording = !pids.isEmpty();

            if (isRecording) {
                if (filename != null) {
                    long size2 = 0;

                    try {
                        Process process = Runtime.getRuntime().exec(new String[] {"/usr/bin/stat", "-f", "%z", filename});
                        InputStream source = process.getInputStream();
                        Scanner scanner = new Scanner(source);
                        try {
                            while(scanner.hasNextLine()) {
                                String line = scanner.nextLine();
                                size2 = Long.parseLong(line);
                            }
                        } catch (Exception e) {
                            print(e.getMessage());
                        } finally {
                            if (scanner != null) {
                                scanner.close();
                            }
                        }
                    } catch (Exception e1) {
                        print(e1.getMessage());
                    }

                    if (size2 > size) {
                        System.out.print(".");

                        size = size2;
                        count = 0;
                    } else {
                        // might have stopped
                        System.out.print("-");
                        System.out.print(size2);

                        count++;

                        if (count > 10) {
                            size = 0;
                            isRecording = false;
                            count = 0;
                            print(MessageFormat.format("Stream stopped: {0}", filename));
                            try {
                                Runtime.getRuntime().exec(new String[] {"/bin/sh", "-c", "killall ffmpeg"});
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }

            if (!isRecording) {
                if (start != null) {
                    Date date = new Date();
                    try {
                        Date date2 = sdf.parse(start);

                        if (date2.after(date)) {
                            System.out.print("-");
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException e) {}
                            continue;
                        }
                    } catch (ParseException e2) {
                    }
                }

                System.out.println();
                print("Stream not recording.");

                String pBest = null;
                String p720 = null;
                print("Starting recorder.");

                print("Looking for available filename...");
                int i = 0;
                boolean isAvailable = false;
                while(!isAvailable) {
                    filename = MessageFormat.format("output{0}.ts", i);
                    File f = new File(MessageFormat.format("./{0}", filename));
                    if (f.exists()) {
                        i++;
                        continue;
                    }
                    isAvailable = true;
                }
                print(MessageFormat.format("Recording to {0}.", filename));

                try {
                    print("Starting stream...");
                    count = 0;
                    String command = MessageFormat.format("/usr/local/bin/ffmpeg -i {0} -c copy {1}", youtubeUrl, filename);
                    System.out.println(command);
                    Process process = Runtime.getRuntime().exec(new String[] {"/bin/sh", "-c", command});
                    print("Stream started.");
                } catch (Exception e1) {
                    print(e1.getMessage());
                }
            }

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }

    public static String convertStreamToString(final InputStream is) {
        final java.util.Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    private void print(String message) {
        System.out.println(message);
    }

    private ArrayList<Integer> check(String command) {
        ArrayList<Integer> pids = new ArrayList<Integer>();

        try {
            Process process = Runtime.getRuntime().exec(new String[] {"/bin/sh", "-c", command});
            InputStream source = process.getInputStream();
            try (Scanner scanner = new Scanner(source)) {
                while(scanner.hasNextLine()) {
                    String line = scanner.nextLine();

                    int pid = Integer.parseInt(line);
                    pids.add(pid);
                }
            } catch (Exception e) {}
        } catch (Exception e1) {
        }
        return pids;
    }

    public void shutdown() {
        print("Shutdown...");
        running.set(false);
    }
}
