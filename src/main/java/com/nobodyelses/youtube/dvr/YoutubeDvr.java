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
            final YoutubeDvrRunner dvr = new YoutubeDvrRunner(youtubeUrl, start, end);

            dvr.start();

            try {
                dvr.join();
            } catch (InterruptedException e1) {
            }
        }
    }

    private static String getUsage() {
        return new StringBuilder()
            .append("\nUsage:\n\n")
            .append("$ java -cp src/main/java com.nobodyelses.youtube.dvr.YoutubeDvr <youtube url> [start date/time] [end date/time]\n\n")
            .append("Date/time format: MM/dd/yyyy hh:mm:ss\n\n")
            .append("Example:\n\n")
            .append("java -cp src/main/java com.nobodyelses.youtube.dvr.YoutubeDvr https://www.youtube.com/watch?v=6C449GHTyt4\n\n")
            .toString();
    }
}
