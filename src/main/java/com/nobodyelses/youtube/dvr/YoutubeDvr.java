package com.nobodyelses.youtube.dvr;

public class YoutubeDvr {
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println(getUsage());
            return;
        }

        String youtubeUrl = args[0];
        String start = null;
        String end = null;

        if (args.length > 1) {
            start = args[1];
        }

        if (args.length > 2) {
            end = args[2];
        }

        new YoutubeDvr().start(youtubeUrl, start, end);
    }

    public void start(String youtubeUrl, String start, String end) throws Exception {
        while(true) {
            final YoutubeDvrRunner2 dvr = new YoutubeDvrRunner2(youtubeUrl, start, end);

            dvr.start();

            try {
                dvr.join();
            } catch (InterruptedException e1) {
            }
        }
    }

    private static String getUsage() {
        return new StringBuilder()
            .append("Usage:\n\n")
            .append("$ java -cp src/java com.nobodyelses.youtube.dvr.YoutubeDvr <youtube url> [start date/time]")
            .toString();
    }
}
