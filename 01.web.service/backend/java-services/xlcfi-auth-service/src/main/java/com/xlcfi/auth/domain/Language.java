package com.xlcfi.auth.domain;

public enum Language {
    KO("한국어", "ko-KR"),
    EN("English", "en-US");
    
    private final String displayName;
    private final String locale;
    
    Language(String displayName, String locale) {
        this.displayName = displayName;
        this.locale = locale;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getLocale() {
        return locale;
    }
}

