package com.abhijitvalluri.android.fitnotifications.services;

import android.app.Notification;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;

import com.abhijitvalluri.android.fitnotifications.R;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


/**
 * A special extractor that ignores all non-summary notifications and extracts the new messages
 * from the group summary "lines" property. It can also handle messages from multiple chats
 * (when the message is prefixed with the chat/sender name).
 */
class GroupSummaryMessageExtractor extends BasicMessageExtractor {

    private static final Pattern NEW_MESSAGES = Pattern.compile("\\d+ new messages");
    private static final Pattern NEW_MESSAGES_MULTIPLE_CHATS = Pattern.compile("\\d+ (new )?messages from \\d+ chats");
    private static final Pattern PHOTO_MESSAGE = Pattern.compile("(sent you a photo|\uD83D\uDCF7 Photo)");

    private final Pattern[] allNewMessagesPatterns;
    private final Pattern[] newMessagesMultipleChatsPatterns;
    private final Pattern[] photoMessagePatterns;

    // some apps (Telegram) put new messages at the beginning of EXTRA_TEXT_LINES, other (WhatsApp) at the end
    private final boolean newMessagesFirst;
    private int lastSeenMessageHash = 0;


    public GroupSummaryMessageExtractor(boolean newMessagesFirst) {
        this.newMessagesFirst = newMessagesFirst;

        allNewMessagesPatterns = new Pattern[] { NEW_MESSAGES, NEW_MESSAGES_MULTIPLE_CHATS };
        newMessagesMultipleChatsPatterns = new Pattern[] { NEW_MESSAGES_MULTIPLE_CHATS };
        photoMessagePatterns = new Pattern[] { PHOTO_MESSAGE };
    }


    public GroupSummaryMessageExtractor(Resources res, boolean newMessagesFirst) {
        this.newMessagesFirst = newMessagesFirst;

        // avoid doubling the patterns in case of missing translation
        Pattern newMessagesPatternLocalized =
                getLocalizedPattern(NEW_MESSAGES, res.getString(R.string.new_messages_summary_pattern));

        Pattern newMessagesMultipleChatsPatternLocalized =
                getLocalizedPattern(NEW_MESSAGES_MULTIPLE_CHATS, res.getString(R.string.new_messages_multiple_chats_summary_pattern));


        // always check against the English version too (e.g. Telegram lacks Russian translation)
        allNewMessagesPatterns = new Pattern[] {
                NEW_MESSAGES, NEW_MESSAGES_MULTIPLE_CHATS,
                newMessagesPatternLocalized, newMessagesMultipleChatsPatternLocalized
        };

        newMessagesMultipleChatsPatterns = new Pattern[] {
                NEW_MESSAGES_MULTIPLE_CHATS,
                newMessagesMultipleChatsPatternLocalized
        };

        photoMessagePatterns = new Pattern[] {
                PHOTO_MESSAGE,
                getLocalizedPattern(PHOTO_MESSAGE, res.getString(R.string.notification_message_photo))
        };
    }


    private static Pattern getLocalizedPattern(Pattern referencePattern, String localizedPattern) {
        if (!referencePattern.pattern().equals(localizedPattern)) {
            try {
                return Pattern.compile(localizedPattern);
            } catch (Exception e) {
                Log.e("GroupSummary", "Error compiling localized pattern: " + localizedPattern, e);
            }
        }

        return null;
    }


    @Override
    public CharSequence[] getTitleAndText(String appPackageName, Bundle extras, int notificationFlags) {
        CharSequence notificationTitle = extras.getCharSequence(Notification.EXTRA_TITLE);
        CharSequence notificationText = null;

        // we only process "summary" notifications
        if ((notificationFlags & Notification.FLAG_GROUP_SUMMARY) != 0) {
            notificationText = extras.getCharSequence(Notification.EXTRA_TEXT);

            // 1. regular text - use the generic approach
            if (notificationText == null || !matchesAnyPattern(notificationText, allNewMessagesPatterns)) {
                lastSeenMessageHash = hash(notificationTitle, notificationText);
                return super.getTitleAndText(appPackageName, extras, notificationFlags);
            }

            CharSequence[] lines = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES);
            if (lines == null) {
                lastSeenMessageHash = hash(notificationTitle, notificationText);
                return super.getTitleAndText(appPackageName, extras, notificationFlags);
            }

            int newestMessageIndex = newMessagesFirst ? 0 : lines.length - 1;

            if (matchesAnyPattern(notificationText, newMessagesMultipleChatsPatterns)) {
                // 2. "N new messages from M chats" - pick both title and new text from EXTRA_TEXT_LINES
                // texts in EXTRA_TEXT_LINES have sender as the prefix
                int pos = findFirstNewMessage(lines, null);

                if (pos < 0) {
                    notificationText = buildMultiMessage(lines, -pos - 1, newMessagesFirst ? -1 : 1);
                }
                else {
                    notificationText = buildMessage(lines, pos, newMessagesFirst ? -1 : 1, true);
                    notificationTitle = getSender(lines[newestMessageIndex]);
                }

                lastSeenMessageHash = hash(lines[newestMessageIndex], 0);
            }
            else {
                // 3. "N new messages" - pick new text from EXTRA_TEXT_LINES
                // texts in EXTRA_TEXT_LINES are from one sender - no prefix
                int pos = findFirstNewMessage(lines, notificationTitle);
                notificationText = buildMessage(lines, pos, newMessagesFirst ? -1 : 1, false);

                lastSeenMessageHash = hash(notificationTitle, lines[newestMessageIndex]);
            }
        }

        return new CharSequence[] { notificationTitle, notificationText };
    }


    private static boolean matchesAnyPattern(CharSequence text, Pattern ... patterns) {
        for (Pattern p : patterns) {
            if (p != null && p.matcher(text).matches()) {
                return true;
            }
        }
        return false;
    }


    /**
     * Returns the index of the first new message in the <code>lines</code>.
     *
     * The index is negative in case the new messages are from several different senders.
     * In such case it can be converted to the actual position like this: <code>-pos - 1</code>.
     * This is needed to make sure the returned value is negative even when the first message is at position 0.
     */
    private int findFirstNewMessage(CharSequence[] lines, CharSequence title) {
        // and there could be several we haven't shown yet - scan until we find the last seen one
        int pos = 0;
        int step = 1;
        if (newMessagesFirst) {
            pos = lines.length - 1;
            step = -1;
        }

        boolean multipleSenders = false;
        String previousSender = null;
        for (; 0 <= pos && pos < lines.length; pos += step) {
            int messageHash = title == null ? hash(lines[pos], 0) : hash(title, lines[pos]);
            if (messageHash == lastSeenMessageHash) {
                break;
            }

            // detect if new messages are from different senders
            if (title == null && !multipleSenders) {
                String sender = getSender(lines[pos]).toString();
                if (previousSender != null && !sender.equals(previousSender)) {
                    multipleSenders = true;
                }
                previousSender = sender;
            }
        }

        if (pos < 0 || pos == lines.length) {
            // the last seen message was not found - consider all messages new
            pos = newMessagesFirst ? lines.length - 1 : 0;
        }
        else {
            // advance to the first new message
            pos += step;
        }

        return multipleSenders ? -(pos + 1) : pos;
    }


    // collect the new messages from oldest to newest
    private CharSequence buildMessage(CharSequence[] lines, int pos, int step, boolean senderPrefixPresent) {
        StringBuilder sb = new StringBuilder();
        for (; 0 <= pos && pos < lines.length; pos += step) {
            CharSequence message = senderPrefixPresent ? stripSender(lines[pos]) : lines[pos];
            if (!matchesAnyPattern(message, photoMessagePatterns) || !endsWith(sb, message)) {
                if (sb.length() > 0) {
                    sb.append(' ');
                }
                sb.append(message);
            }
        }

        return sb;
    }


    // collect the new messages from oldest to newest grouping them per sender
    private CharSequence buildMultiMessage(CharSequence[] lines, int pos, int step) {
        List<StringBuilder> allSenderMessages = new ArrayList<>();

        for (; 0 <= pos && pos < lines.length; pos += step) {
            CharSequence senderPrefix = getSender(lines[pos]) + ": ";

            // find the message to append to
            StringBuilder senderMessages = null;
            for (StringBuilder sb : allSenderMessages) {
                if (startsWith(sb, senderPrefix)) {
                    senderMessages = sb;
                    break;
                }
            }

            if (senderMessages == null) {
                senderMessages = new StringBuilder();
                // do not add trailing space as it will be added with the message later
                senderMessages.append(senderPrefix, 0, senderPrefix.length() - 1);
                allSenderMessages.add(senderMessages);
            }

            CharSequence message = stripSender(lines[pos]);
            if (!matchesAnyPattern(message, photoMessagePatterns) || !endsWith(senderMessages, message)) {
                senderMessages.append(' ').append(message);
            }
        }

        StringBuilder result = new StringBuilder();
        for (StringBuilder senderMessage : allSenderMessages) {
            if (result.length() > 0) {
                result.append("; ");
            }
            result.append(senderMessage);
        }

        return result;
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


    private static int hash(CharSequence title, CharSequence text) {
        int h = hash(title, 0);

        if (text != null) {
            h = hash(": ", h);
            h = hash(text, h);
        }

        return h;
    }


    private static int hash(CharSequence cs, int h) {
        for (int i = 0; i < cs.length(); i++) {
            h = 31 * h + cs.charAt(i);
        }
        return h;
    }
}
