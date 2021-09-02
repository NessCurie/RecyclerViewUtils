package com.github.recyclerview;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;

public class HanziToPinyin {

    private static class HanziToPinyinHolder {
        private static final HanziToPinyin sInstance = new HanziToPinyin();
    }

    private HanziToPinyin() {
    }

    public static HanziToPinyin getInstance() {
        return HanziToPinyinHolder.sInstance;
    }

    public String transliterate(final String input) {
        StringBuilder sb = new StringBuilder();
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            String[] pinyinStringArray = null;
            try {
                pinyinStringArray = PinyinHelper.toHanyuPinyinStringArray(c, format);
            } catch (Exception e) {
                //maybe
            }
            if (pinyinStringArray == null) {
                sb.append(c);
            } else {
                sb.append(pinyinStringArray[0]);
            }
        }
        return sb.toString();
    }
}
