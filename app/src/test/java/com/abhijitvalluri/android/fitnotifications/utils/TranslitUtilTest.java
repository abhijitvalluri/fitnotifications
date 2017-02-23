package com.abhijitvalluri.android.fitnotifications.utils;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

import static org.junit.Assert.*;


public class TranslitUtilTest {

    @Test
    public void testRussianTransliteration() throws UnsupportedEncodingException {
        String replacements =
                "ъ   '\n" +
                "ь   '\n" +
                "Ъ   '\n" +
                "Ь   '\n" +
                // More common way of transliterating some Russian characters
                "ё   e\n" +
                "ж   zh\n" +
                "ц   ts\n" +
                "ч   ch\n" +
                "ш   sh\n" +
                "щ   sch\n" +
                "э   e\n" +
                "ю   yu\n" +
                "я   ya\n" +
                "Ё   E\n" +
                "Ж   Zh\n" +
                "Ц   Ts\n" +
                "Ч   Ch\n" +
                "Ш   Sh\n" +
                "Щ   Sch\n" +
                "Э   E\n" +
                "Ю   Yu\n" +
                "Я   Ya\n";

        TranslitUtil tu = new TranslitUtil(new ByteArrayInputStream(replacements.getBytes("UTF-8")));

        assertEquals("a b v g d e e zh z i j k l m n o p r s t u f h ts ch sh sch ' y ' e yu ya",
                tu.transliterate("а б в г д е ё ж з и й к л м н о п р с т у ф х ц ч ш щ ъ ы ь э ю я"));

        assertEquals("A B V G D E E Zh Z I J K L M N O P R S T U F H Ts Ch Sh Sch ' Y ' E Yu Ya",
                tu.transliterate("А Б В Г Д Е Ё Ж З И Й К Л М Н О П Р С Т У Ф Х Ц Ч Ш Щ Ъ Ы Ь Э Ю Я"));
    }


    @Test
    public void testEmojiTransliteration() throws UnsupportedEncodingException {
        String replacements =
                "U+263A      :-)\n" +
                "U+1F641     :-(\n" +
                "U+1F632     o_O\n" +
                "U+1F918     \\m/\n" +
                // skin tone modifier
                "U+1F3FD\n";

        TranslitUtil tu = new TranslitUtil(new ByteArrayInputStream(replacements.getBytes("UTF-8")));

        assertEquals(":-) o_O \\m/ \uD83D\uDC4D",
                tu.transliterate("☺ \uD83D\uDE32 \uD83E\uDD18 \uD83D\uDC4D\uD83C\uDFFD"));
    }
}