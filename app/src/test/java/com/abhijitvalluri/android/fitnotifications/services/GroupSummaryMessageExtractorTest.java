package com.abhijitvalluri.android.fitnotifications.services;

import android.app.Notification;
import android.content.res.Resources;
import android.os.Bundle;

import com.abhijitvalluri.android.fitnotifications.R;

import org.junit.Test;
import org.mockito.Mockito;


import static org.junit.Assert.*;


public class GroupSummaryMessageExtractorTest {

    private static final int SUMMARY = Notification.FLAG_GROUP_SUMMARY;
    private static final int REGULAR = 0;


    @Test
    public void testTelegramStyleMessagesPattern() {
        MessageExtractor extractor = new GroupSummaryMessageExtractor(true);

        assertTitleAndTextEqual("Alice", null,
                getTitleAndText(extractor, REGULAR, "Alice", "Hello there!"));

        assertTitleAndTextEqual("Alice", "Hello there!",
                getTitleAndText(extractor, SUMMARY, "Alice", "Hello there!"));

        // 2nd message arrived
        assertTitleAndTextEqual("Alice", null,
                getTitleAndText(extractor, REGULAR, "Alice", "Hello there! How are you doing?"));

        assertTitleAndTextEqual("Alice", "How are you doing?",
                getTitleAndText(extractor, SUMMARY, "Alice", "2 new messages",
                        "How are you doing?", "Hello there!"));

        // 3rd message arrived
        assertTitleAndTextEqual("Alice", null,
                getTitleAndText(extractor, REGULAR, "Alice", "Hello there! How are you doing? Let's meet."));

        assertTitleAndTextEqual("Alice", "Let's meet",
                getTitleAndText(extractor, SUMMARY, "Alice", "3 new messages",
                        "Let's meet", "How are you doing?", "Hello there!"));

        // 4th & 5th message arrived together to another chat
        assertTitleAndTextEqual("Bob", null,
                getTitleAndText(extractor, REGULAR, "Bob", "Hi! Check this video http://..."));

        assertTitleAndTextEqual("Bob", "Hi! Check this video http://...",
                getTitleAndText(extractor, SUMMARY, "Telegram", "5 new messages from 2 chats",
                        "Bob: Check this video http://...", "Bob: Hi!",
                        "Alice: Let's meet", "Alice: How are you doing?", "Alice: Hello there!"));

        // three more messages arrived to multiple chats
        assertTitleAndTextEqual("Bob", null,
                getTitleAndText(extractor, REGULAR, "Bob", "Hi! Check this video http://... It's hilarious!"));

        assertTitleAndTextEqual("Alice", null,
                getTitleAndText(extractor, REGULAR, "Alice", "Hello there! How are you doing? Let's meet. Starbucks? How about at 5?"));

        assertTitleAndTextEqual("Telegram", "Alice: Starbucks? How about at 5?; Bob: It's hilarious!",
                getTitleAndText(extractor, SUMMARY, "Telegram", "8 new messages from 2 chats",
                        "Alice: How about at 5?", "Bob: It's hilarious!", "Alice: Starbucks?",
                        "Bob: Check this video http://...", "Bob: Hi!",
                        "Alice: Let's meet", "Alice: How are you doing?", "Alice: Hello there!"));
    }


    @Test
    public void testWhatsAppStyleMessagesPattern() {
        MessageExtractor extractor = new GroupSummaryMessageExtractor(false);

        assertTitleAndTextEqual("Alice", "Hello there!",
                getTitleAndText(extractor, SUMMARY, "Alice", "Hello there!"));

        // 2nd message arrived
        assertTitleAndTextEqual("Alice", null,
                getTitleAndText(extractor, REGULAR, "Alice", "Hello there!"));

        assertTitleAndTextEqual("Alice", "How are you doing?",
                getTitleAndText(extractor, SUMMARY, "Alice", "2 new messages",
                        "Hello there!", "How are you doing?"));

        // 3rd message arrived
        assertTitleAndTextEqual("Alice", null,
                getTitleAndText(extractor, REGULAR, "Alice", "Hello there!"));

        assertTitleAndTextEqual("Alice", "Let's meet",
                getTitleAndText(extractor, SUMMARY, "Alice", "3 new messages",
                        "Hello there!", "How are you doing?", "Let's meet"));

        // 4th & 5th message arrived together to another chat
        assertTitleAndTextEqual("Alice", null,
                getTitleAndText(extractor, REGULAR, "Alice", "Hello there!"));

        assertTitleAndTextEqual("Bob", null,
                getTitleAndText(extractor, REGULAR, "Bob", "Hi!"));

        assertTitleAndTextEqual("Bob", "Hi! Check this video http://...",
                getTitleAndText(extractor, SUMMARY, "WhatsApp", "5 messages from 2 chats",
                        "Alice: Hello there!", "Alice: How are you doing?", "Alice: Let's meet",
                        "Bob: Hi!", "Bob: Check this video http://..."));

        // three more messages arrived to different chats
        assertTitleAndTextEqual("Alice", null,
                getTitleAndText(extractor, REGULAR, "Alice", "Hello there!"));

        assertTitleAndTextEqual("Bob", null,
                getTitleAndText(extractor, REGULAR, "Bob", "Hi!"));

        assertTitleAndTextEqual("WhatsApp", "Alice: Starbucks? How about at 5?; Bob: It's hilarious!",
                getTitleAndText(extractor, SUMMARY, "WhatsApp", "8 messages from 2 chats",
                        "Alice: Hello there!", "Alice: How are you doing?", "Alice: Let's meet",
                        "Bob: Hi!", "Bob: Check this video http://...",
                        "Alice: Starbucks?", "Bob: It's hilarious!", "Alice: How about at 5?"));
    }


    @Test
    public void testSameMessageInMultipleChats() {
        MessageExtractor extractor = new GroupSummaryMessageExtractor(false);

        assertTitleAndTextEqual("Alice", "Ok",
                getTitleAndText(extractor, SUMMARY, "Alice", "Ok"));

        // then two more messages arrive to another chat
        assertTitleAndTextEqual("Bob", null,
                getTitleAndText(extractor, REGULAR, "Bob", "I'll see what I can do"));

        assertTitleAndTextEqual("Bob", "I'll see what I can do Ok",
                getTitleAndText(extractor, SUMMARY, "WhatsApp", "3 messages from 2 chats",
                        "Alice: Ok",
                        "Bob: I'll see what I can do", "Bob: Ok"));
    }


    @Test
    public void testLocalizedSummary() {
        Resources resourcesMock = Mockito.mock(Resources.class);

        Mockito.doReturn("\\d+ neue Nachrichten")
                .when(resourcesMock).getString(R.string.new_messages_summary_pattern);
        Mockito.doReturn("\\d+ (neue )?Nachrichten (aus|von) \\d+ Chats")
                .when(resourcesMock).getString(R.string.new_messages_multiple_chats_summary_pattern);


        MessageExtractor extractor = new GroupSummaryMessageExtractor(resourcesMock, false);

        assertTitleAndTextEqual("Alice", "Marco",
                getTitleAndText(extractor, SUMMARY, "Alice", "Marco"));

        assertTitleAndTextEqual("Bob", null,
                getTitleAndText(extractor, REGULAR, "Bob", "Polo"));

        assertTitleAndTextEqual("Bob", "Polo",
                getTitleAndText(extractor, SUMMARY, "WhatsApp", "2 neue Nachrichten von 2 Chats",
                        "Alice: Marco", "Bob: Polo"));

        assertTitleAndTextEqual("Bob", null,
                getTitleAndText(extractor, REGULAR, "Bob", "Polo"));

        assertTitleAndTextEqual("Bob", "I win!",
                getTitleAndText(extractor, SUMMARY, "WhatsApp", "3 Nachrichten aus 2 Chats",
                        "Alice: Marco", "Bob: Polo", "Bob: I win!"));
    }


    private static void assertTitleAndTextEqual(CharSequence expectedTitle, CharSequence expectedText,
                                                CharSequence[] actualTitleAndText) {
        assertContentEquals("title", expectedTitle, actualTitleAndText[0]);
        assertContentEquals("text", expectedText, actualTitleAndText[1]);
    }


    private static void assertContentEquals(String label, CharSequence expected, CharSequence actual) {

        if (expected == null) {
            assertNull("Null " + label + " was expected but was: " + actual, actual);
        } else {
            assertNotNull("Unexpected null " + label, actual);

            assertEquals("Mismatching " + label, expected.toString(), actual.toString());
        }
    }


    private static CharSequence[] getTitleAndText(MessageExtractor extractor, int messageType,
                                                  CharSequence title, CharSequence text, CharSequence ... lines) {
        return extractor.getTitleAndText("org.example", notification(title, text, lines), messageType);
    }


    // have to mock Android dependencies
    // https://developer.android.com/training/testing/unit-testing/local-unit-tests.html
    private static Bundle notification(CharSequence title, CharSequence text, CharSequence ... lines) {
        Bundle bundleMock = Mockito.mock(Bundle.class);

        Mockito.doReturn(title).when(bundleMock).getCharSequence(Notification.EXTRA_TITLE);
        Mockito.doReturn(text) .when(bundleMock).getCharSequence(Notification.EXTRA_TEXT);
        Mockito.doReturn(lines).when(bundleMock).getCharSequenceArray(Notification.EXTRA_TEXT_LINES);

        return bundleMock;
    }
}