package com.abhijitvalluri.android.fitnotifications.services;

import android.app.Notification;
import android.os.Build;
import android.os.Bundle;

import java.util.HashMap;
import java.util.Map;


class GenericMessageExtractor implements MessageExtractor {

    private Map<String, String> mNotificationStringMap = new HashMap<>();


    @Override
    public CharSequence[] getTitleAndText(String appPackageName, Bundle extras, int notificationFlags) {
        CharSequence[] titleAndText = extractTitleAndText(extras);

        String text = titleAndText[1].toString();
        String prevNotificationText = mNotificationStringMap.put(appPackageName, text);
        // TODO: add more specific checks to avoid blocking legitimate identical notifications
        if (text.equals(prevNotificationText)) {
            // do not send the duplicate notification, but only for every 2nd occurrence
            // (i.e. when the same text arrives for the 3rd time - send it)
            mNotificationStringMap.remove(appPackageName);
            titleAndText[1] = null;
        }

        return titleAndText;
    }


    protected static CharSequence[] extractTitleAndText(Bundle extras) {
        CharSequence notificationTitle = extras.getCharSequence(Notification.EXTRA_TITLE);

        CharSequence notificationText = extras.getCharSequence(Notification.EXTRA_TEXT);

        CharSequence notificationBigText = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT);
        }

        if (startsWith(notificationBigText, notificationText)) {
            // if notification "big text" starts with the short text - just use the big one
            notificationText = notificationBigText;
            notificationBigText = null;
        }

        StringBuilder sb = new StringBuilder();
        if (notificationText != null) {
            sb.append(notificationText);
        }

        if (!isBlank(notificationBigText)) {
            sb.append(" -- ").append(notificationBigText);
        }

        String text = sb.toString().trim().replaceAll("\\s+", " ");

        return new CharSequence[] { notificationTitle, text };
    }

    protected static boolean startsWith(CharSequence big, CharSequence small) {
        return big != null && small != null && big.length() >= small.length()
                && big.subSequence(0, small.length()).toString().contentEquals(small);
    }


    protected static boolean isBlank(CharSequence text) {
        if (text != null && text.length() > 0) {
            for (int i = 0; i < text.length(); i++) {
                // FIXME: isWhitespace() does not recognize some characters (e.g. non-breaking space)
                if (!Character.isWhitespace(text.charAt(i))) {
                    return false;
                }
            }
        }
        return true;
    }
}
