package com.abhijitvalluri.android.fitnotifications;

import com.ibm.icu.text.Transliterator;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class TransliterationTest {

    @Test
    public void transliterateRussian() {
        assertEquals("alfavit", Transliterator.getInstance("Any-Latin").transform("алфавит"));
    }

    @Test
    public void transliterateJapanese() {
        assertEquals("arufabetto", Transliterator.getInstance("Any-Latin").transform("アルファベット"));
    }

    @Test
    public void transliterateGreek() {
        assertEquals("Alphabētikós", Transliterator.getInstance("Any-Latin").transform("Αλφαβητικός"));
    }

    @Test
    @Ignore(value = "Transliterating Thai is not supported")
    public void transliterateThai() {
        assertEquals("tạw xạks̄ʹr", Transliterator.getInstance("Any-Latin").transform("ตัวอักษร"));
    }

    @Test
    public void transliterateArabic() {
        assertEquals("ạlạ̉bjdyẗ", Transliterator.getInstance("Any-Latin").transform("الأبجدية"));
    }

    @Test
    public void transliterateKorean() {
        assertEquals("alpabes", Transliterator.getInstance("Any-Latin").transform("알파벳"));
    }

    @Test
    public void transliterateChinese() {
        assertEquals("zì mǔ", Transliterator.getInstance("Any-Latin").transform("字母"));
    }
}