package com.abhijitvalluri.android.fitnotifications.services;

import android.app.Notification;
import android.os.Bundle;

import java.util.regex.Pattern;


class GroupSummaryMessageExtractor extends GenericMessageExtractor {

    // TODO: also use localized version of the patterns
    private static final Pattern NEW_MESSAGES = Pattern.compile("\\d+ new messages");
    private static final Pattern NEW_MESSAGES_MULTIPLE_CHATS = Pattern.compile("\\d+ (new )?messages from \\d+ chats");

    // some apps (Telegram) put new messages at the beginning of EXTRA_TEXT_LINES, other (WhatsApp) at the end
    private final boolean newMessagesFirst;
    private int lastSeenMessageHash = 0;


    public GroupSummaryMessageExtractor(boolean newMessagesFirst) {
        this.newMessagesFirst = newMessagesFirst;
    }

    @Override
    public CharSequence[] getTitleAndText(Bundle extras, int notificationFlags) {
        CharSequence notificationTitle = extras.getCharSequence(Notification.EXTRA_TITLE);
        CharSequence notificationText = null;

        // for Telegram we only process "summary" notifications
        if ((notificationFlags & Notification.FLAG_GROUP_SUMMARY) != 0) {
            notificationText = extras.getCharSequence(Notification.EXTRA_TEXT);

            // 1. regular text - use the generic approach
            if (notificationText == null || (!NEW_MESSAGES.matcher(notificationText).matches()
                    && !NEW_MESSAGES_MULTIPLE_CHATS.matcher(notificationText).matches())) {
                lastSeenMessageHash = hash(notificationText);
                return super.getTitleAndText(extras, notificationFlags);
            }

            CharSequence[] lines = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES);
            if (lines == null) {
                return super.getTitleAndText(extras, notificationFlags);
            }

            int newestMessageIndex = newMessagesFirst ? 0 : lines.length - 1;

            if (NEW_MESSAGES_MULTIPLE_CHATS.matcher(notificationText).find()) {
                // 2. "N new messages from M chats" - pick both title and new text from EXTRA_TEXT_LINES
                // texts in EXTRA_TEXT_LINES have sender as the prefix
                notificationText = collectNewMessages(lines, true);
                notificationTitle = getSender(lines[newestMessageIndex]);

                // FIXME: what if there are new messages from multiple senders ???
            }
            else {
                // 3. "N new messages" - pick new text from EXTRA_TEXT_LINES
                // texts in EXTRA_TEXT_LINES are from one sender - no prefix
                notificationText = collectNewMessages(lines, false);
            }

            lastSeenMessageHash = hash(lines[newestMessageIndex]);

        }

        return new CharSequence[] { notificationTitle, notificationText };
    }


    private CharSequence collectNewMessages(CharSequence[] lines, boolean senderPrefix) {
        // and there could be several we haven't shown yet - scan until we find the last seen one
        int pos = lines.length - 1;
        int step = -1;
        if (newMessagesFirst) {
            pos = 0;
            step = 1;
        }

        for (; 0 <= pos && pos < lines.length; pos += step) {
            CharSequence message = senderPrefix ? stripSender(lines[pos]) : lines[pos];
            if (hash(message) == lastSeenMessageHash) {
                break;
            }
        }

        // step back to the first new message
        pos -= step;

        // collect the new messages from oldest to newest
        StringBuilder sb = new StringBuilder();
        for (; 0 <= pos && pos < lines.length; pos -= step) {
            sb.append(senderPrefix ? stripSender(lines[pos]) : lines[pos]).append(' ');
        }

        // trim trailing space
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }

        return sb;
    }


    /**
     * "Sender Name: Message text" => "Message text"
     */
    private static CharSequence stripSender(CharSequence message) {
        for (int i = 0; i < message.length() - 1; i++) {
            if (message.charAt(i) == ':' && message.charAt(i + 1) == ' ') {
                return message.subSequence(i + 2, message.length());
            }
        }

        return message;
    }


    /**
     * "Sender Name: Message text" => "Sender Name"
     */
    private static CharSequence getSender(CharSequence message) {
        for (int i = 0; i < message.length() - 1; i++) {
            if (message.charAt(i) == ':' && message.charAt(i + 1) == ' ') {
                return message.subSequence(0, i);
            }
        }

        return "";
    }


    private static int hash(CharSequence cs) {
        int h = 0;
        for (int i = 0; i < cs.length(); i++) {
            h = 31 * h + cs.charAt(i);
        }
        return h;
    }
}
