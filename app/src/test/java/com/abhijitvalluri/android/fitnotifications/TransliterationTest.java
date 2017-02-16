package com.abhijitvalluri.android.fitnotifications;

import com.ibm.icu.text.Transliterator;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;


public class TransliterationTest {

    @Test
    public void transliterateRussian() throws Exception {
        assertEquals("alfavit", Transliterator.getInstance("Any-Latin").transform("алфавит"));
    }

    @Test
    public void transliterateJapanese() throws Exception {
        assertEquals("arufabetto", Transliterator.getInstance("Any-Latin").transform("アルファベット"));
    }

    @Test
    public void transliterateGreek() throws Exception {
        assertEquals("Alphabētikós", Transliterator.getInstance("Any-Latin").transform("Αλφαβητικός"));
    }

    @Test
    @Ignore(value = "Transliterating Thai is not supported")
    public void transliterateThai() throws Exception {
        assertEquals("tạw xạks̄ʹr", Transliterator.getInstance("Any-Latin").transform("ตัวอักษร"));
    }

    @Test
    public void transliterateArabic() throws Exception {
        assertEquals("ạlạ̉bjdyẗ", Transliterator.getInstance("Any-Latin").transform("الأبجدية"));
    }

    @Test
    public void transliterateKorean() throws Exception {
        assertEquals("alpabes", Transliterator.getInstance("Any-Latin").transform("알파벳"));
    }

    @Test
    public void transliterateChinese() throws Exception {
        assertEquals("zì mǔ", Transliterator.getInstance("Any-Latin").transform("字母"));
    }
}