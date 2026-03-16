package com.thebank.util;

import java.util.Arrays;
import java.util.List;

public class NameFormatter {

    private static final List<String> EXEMPT_WORDS = Arrays.asList("van", "de", "bin", "binti");


    private NameFormatter() {}


    public static String format(String rawName) {
        String[] words = rawName.trim().split("\\s+");

        StringBuilder name = new StringBuilder();

        for (int i = 0; i < words.length; i++) {

            String word = words[i].toLowerCase();

            boolean isFirstWord = (i == 0);
            boolean isExempt = EXEMPT_WORDS.contains(word);

            if (isFirstWord || !isExempt) {
                word = Character.toUpperCase(word.charAt(0)) + word.substring(1);
            }


            name.append(word);

            if(i < words.length - 1){
                name.append(" ");
            }

        }
        return name.toString();
    }
}