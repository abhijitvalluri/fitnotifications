package com.abhijitvalluri.android.fitnotifications.utils;

import android.content.res.Resources;
import android.util.Log;

import com.abhijitvalluri.android.fitnotifications.R;
import com.ibm.icu.text.ReplaceableString;
import com.ibm.icu.text.Transliterator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;


/**
 * Some Latin symbols returned by ICU4J are not supported by Fitbit.
 * This class takes care of replacing them with the supported ones.
 */
public class TranslitUtil {
    private static final Transliterator ANY_TO_LATIN = Transliterator.getInstance("Any-Latin");

    private static final String TAG = "TranslitUtil";

    private final char[] symbols;
    private final String[] replacements;


    public TranslitUtil(Resources res) {
        this(res.openRawResource(R.raw.translit_data));
    }


    public TranslitUtil(InputStream is) {
        SortedSet<SymbolReplacement> symbolReplacements = loadReplacements(is);

        if (symbolReplacements.size() == 0) {
            Log.i(TAG, "No transliteration replacements loaded");
            symbols = null;
            replacements = null;
        } else {
            Log.i(TAG, "Loaded " + symbolReplacements.size() + " transliteration replacements");
            symbols = new char[symbolReplacements.size()];
            replacements = new String[symbolReplacements.size()];

            int pos = 0;
            for (SymbolReplacement sr : symbolReplacements) {
                symbols[pos] = sr.symbol;
                replacements[pos] = sr.replacement;
                pos++;
            }
        }
    }

    private static SortedSet<SymbolReplacement> loadReplacements(InputStream is) {
        SortedSet<SymbolReplacement> replacements = new TreeSet<>();

        // load the symbols and their replacements
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.length() == 0 || line.charAt(0) == '#' || line.trim().length() == 0) {
                    continue;
                }

                replacements.add(new SymbolReplacement(line.charAt(0), line.substring(1).trim()));
            }
        } catch (IOException e) {
            Log.e(TAG, "Error loading transliteration replacements", e);
        }

        try { is.close(); } catch (IOException e) { }

        return replacements;
    }


    private static class SymbolReplacement implements Comparable<SymbolReplacement> {
        private char symbol;
        private String replacement;


        public SymbolReplacement(char symbol, String replacement) {
            this.symbol = symbol;
            this.replacement = replacement;
        }

        @Override
        public int compareTo(SymbolReplacement o) {
            return Character.compare(symbol, o.symbol);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SymbolReplacement that = (SymbolReplacement) o;

            return symbol == that.symbol;
        }

        @Override
        public int hashCode() {
            return symbol;
        }
    }


    public String transliterate(CharSequence text) {
        if (text == null) {
            return null;
        }

        try {
            StringBuffer sb;
            if (symbols != null) {
                sb = new StringBuffer(text.length() * 2);
                // copy data replacing some symbols
                for (int i = 0; i < text.length(); i++) {
                    char c = text.charAt(i);
                    int pos = Arrays.binarySearch(symbols, c);
                    if (pos >= 0) {
                        sb.append(replacements[pos]);
                    } else {
                        sb.append(c);
                    }
                }
            } else {
                sb = new StringBuffer(text);
            }

            // run standard Any-to-Latin transformation
            ReplaceableString result = new ReplaceableString(sb);
            ANY_TO_LATIN.transliterate(result);
            return result.toString();
        } catch (Exception e) {
            return text.toString();
        }
    }
}
