package com.nobodyelses.youtube.dvr;

public class YoutubeDvr {
    public static void main(String[] args) {
        String youtubeUrl = args[0];
        String start = args[1];
        new YoutubeDvr().start(youtubeUrl, start);
    }

    public void start(String youtubeUrl, String start) {
        while(true) {
            final YoutubeDvrRunner dvr = new YoutubeDvrRunner(youtubeUrl, start);

            dvr.start();

            try {
                dvr.join();
            } catch (InterruptedException e1) {
            }
        }
    }
}
