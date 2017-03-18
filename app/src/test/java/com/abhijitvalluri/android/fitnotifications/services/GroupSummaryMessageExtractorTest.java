package com.abhijitvalluri.android.fitnotifications.services;

import android.app.Notification;
import android.os.Bundle;

import org.junit.Test;
import org.mockito.Mockito;


import static org.junit.Assert.*;


public class GroupSummaryMessageExtractorTest {

    private static final int SUMMARY = Notification.FLAG_GROUP_SUMMARY;
    private static final int REGULAR = 0;


    @Test
    public void testTelegramMessagesPattern() {
        MessageExtractor extractor = new GroupSummaryMessageExtractor();

        assertTitleAndTextEqual("Alice", null,
                extractor.getTitleAndText(notification("Alice", "Hello there!"), REGULAR));

        assertTitleAndTextEqual("Alice", "Hello there!",
                extractor.getTitleAndText(notification("Alice", "Hello there!"), SUMMARY));

        // 2nd message arrived
        assertTitleAndTextEqual("Alice", null,
                extractor.getTitleAndText(notification("Alice", "Hello there! How are you doing?"), REGULAR));

        assertTitleAndTextEqual("Alice", "How are you doing?",
                extractor.getTitleAndText(notification("Alice", "2 new messages",
                        "How are you doing?", "Hello there!"),
                        SUMMARY));

        // 3rd message arrived
        assertTitleAndTextEqual("Alice", null,
                extractor.getTitleAndText(notification("Alice", "Hello there! How are you doing? Let's meet."), REGULAR));

        assertTitleAndTextEqual("Alice", "Let's meet",
                extractor.getTitleAndText(notification("Alice", "3 new messages",
                        "Let's meet", "How are you doing?", "Hello there!"),
                        SUMMARY));

        // 4th & 5th message arrived together to another chat
        assertTitleAndTextEqual("Bob", null,
                extractor.getTitleAndText(notification("Bob", "Hi! Check this video http://..."), REGULAR));

        assertTitleAndTextEqual("Bob", "Hi! Check this video http://...",
                extractor.getTitleAndText(notification("Telegram", "5 new messages from 2 chats",
                        "Bob: Check this video http://...", "Bob: Hi!", "Alice: Let's meet",
                        "Alice: How are you doing?", "Alice: Hello there!"),
                        SUMMARY));

/*
        // TODO:
        // three more messages arrived to multiple chats
        assertTitleAndTextEqual("Bob", null,
                extractor.getTitleAndText(notification("Bob", "Hi! Check this video http://... It's hilarious!"),
                        REGULAR);

        assertTitleAndTextEqual("Alice", null,
                extractor.getTitleAndText(notification("Alice", "Hello there! How are you doing? Let's meet. Starbucks? How about at 5?"),
                        REGULAR);

        assertTitleAndTextEqual("Telegram", "Alice: Starbucks? How about at 5?; Bob: It's hilarious!",
                extractor.getTitleAndText(notification("Telegram", "8 new messages from 2 chats",
                        "Alice: How about at 5?", "Bob: It's hilarious!", "Alice: Starbucks?",
                        "Bob: Check this video http://...", "Bob: Hi!",
                        "Alice: Let's meet", "Alice: How are you doing?", "Alice: Hello there!"),
                        SUMMARY);
*/
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