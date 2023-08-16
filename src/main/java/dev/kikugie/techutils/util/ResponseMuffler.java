package dev.kikugie.techutils.util;

import java.util.ArrayDeque;

public class ResponseMuffler {
    private static final ArrayDeque<String> muteQueue = new ArrayDeque<>();

    public static void scheduleMute(String matcher) {
        muteQueue.add(matcher);
    }

    public static boolean matches(String message) {
        if (muteQueue.isEmpty()) return false;
        return message.matches(muteQueue.peek());
    }

    public static boolean test(String string) {
        boolean matches = matches(string);
        if (matches)
            muteQueue.pop();
        return matches;
    }
}
