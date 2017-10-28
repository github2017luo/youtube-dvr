package com.nobodyelses.youtube.dvr;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class YoutubeDvrRunner extends Thread {
    private AtomicBoolean running = new AtomicBoolean(false);
    private String youtubeUrl = "https://www.youtube.com/watch?v=Ga3maNZ0x0w";
    private String formatId = "95";
    private long remoteDate = 0;
    private String start = null;
    private String end = null;

    private long startDate = 0;

    private SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");

    public YoutubeDvrRunner(String youtubeUrl, String start, String end) throws Exception {
        this.startDate = System.currentTimeMillis();

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
            try {
                URL url2 = new URL("http://7-dot-nobodyelses-basic-application.appspot.com/youtube");
                URLConnection conn2 = url2.openConnection();
                InputStream is2 = conn2.getInputStream();
                String string2 = convertStreamToString(is2);

                if (string2 != null && string2.trim().length() > 0) {
                    String[] split = string2.split("&");

                    String youtubeUrl2 = null;
                    String formatId2 = null;

                    for (String s : split) {
                        String[] split2 = s.split("=", 2);
                        String prop = split2[0];
                        String value = split2[1];

                        if ("uri".equals(prop)) {
                            youtubeUrl2 = "https://www.youtube.com/watch?v=" + value;
                        } else if ("format".equals(prop)) {
                            formatId2 = value;
                        } else if ("date".equals(prop)) {
                            remoteDate = Long.parseLong(value);
                        }
                    }

                    if (youtubeUrl2 != null && !youtubeUrl.equals(youtubeUrl2)) {
                        if (remoteDate >= startDate) {
                            youtubeUrl = youtubeUrl2;
                            try {
                                Runtime.getRuntime().exec(new String[] {"/bin/sh", "-c", "killall ffmpeg"});
                                Thread.sleep(2000);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            } catch (Exception e3) {
                e3.printStackTrace();
            }

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




//                    File file = new File(filename);
//                    long size2 = file.length();
                    if (size2 > size) {
                        System.out.print(".");

                        size = size2;
                        count = 0;
                    } else {
// might have stopped
                        System.out.print("-");
                        System.out.print(size2);

                        count++;

                        if (count > 5) {
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
                try {
                    print("List formats.");
                    Process process = Runtime.getRuntime().exec(new String[] {"/usr/local/bin/youtube-dl", "--list-formats", youtubeUrl});
                    InputStream source = process.getInputStream();
                    Scanner scanner = new Scanner(source);
                    try {
                        while(scanner.hasNextLine()) {
                            String line = scanner.nextLine();
                            print(line);

                            if (line.contains("(best)")) {
                                pBest = line;
                            }
                            if (line.contains("mp4") && line.contains("720") && !line.contains("video only")) {
                                p720 = line;
                            }
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

                print("Best");
                print(pBest);

                print("720p");
                print(p720);

                if (p720 != null) {
                    formatId = p720;
                } else {
                    formatId = pBest;
                }

                if (formatId == null) {
                    formatId = "95";
                }

                String[] split = formatId.split("\\s+");
                formatId = split[0];

                StringBuilder buf = new StringBuilder();
                try {
                    print(MessageFormat.format("Get stream {0}...", formatId));
                    Process process = Runtime.getRuntime().exec(new String[] {"/usr/local/bin/youtube-dl", "-f", formatId, "-g", youtubeUrl});
                    InputStream source = process.getInputStream();
                    Scanner scanner = new Scanner(source);
                    try {
                        while(scanner.hasNextLine()) {
                            String line = scanner.nextLine();
                            print(line);
                            buf.append(line);
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

                String streamUrl = buf.toString();
                try {
                    print("Starting stream...");
                    count = 0;
                    Process process = Runtime.getRuntime().exec(new String[] {"/bin/sh", "-c", MessageFormat.format("/usr/local/bin/ffmpeg -i {0} -c copy {1}", streamUrl, filename)});
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
