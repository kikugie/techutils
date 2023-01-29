package me.kikugie.techutils.utils;

import java.util.ArrayDeque;

public class ResponseMuffler {
    private static final ArrayDeque<String> muteQueue = new ArrayDeque<>();

    public static void scheduleMute(String matcher) {
        muteQueue.add(matcher);
    }

    public static void pop() {
        muteQueue.remove();
    }

    public static void clear() {
        muteQueue.clear();
    }

    public static boolean matches(String message) {
        if (muteQueue.isEmpty()) return false;
        return message.matches(muteQueue.peek());
    }
}
