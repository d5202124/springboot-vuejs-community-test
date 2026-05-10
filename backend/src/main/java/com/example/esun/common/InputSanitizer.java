package com.example.esun.common;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

public class InputSanitizer {

    // 移除 HTML 標籤，防範 XSS；只用於 URL 欄位（coverImage、image）
    public static String sanitize(String value) {
        if (value == null) return null;
        return Jsoup.clean(value, Safelist.basic());
    }
}
