package com.edgemq.bmaddon.ae2;

import java.util.Locale;

public enum BloodMagicPatternKind {
    BLOOD_ALTAR("blood_altar"),
    ALCHEMY_TABLE("alchemy_table");

    private final String serializedName;

    BloodMagicPatternKind(String serializedName) {
        this.serializedName = serializedName;
    }

    public String getSerializedName() {
        return serializedName;
    }

    public static BloodMagicPatternKind byName(String name) {
        if (name == null || name.isBlank()) {
            return BLOOD_ALTAR;
        }

        String normalized = name.toLowerCase(Locale.ROOT);

        for (BloodMagicPatternKind kind : values()) {
            if (kind.serializedName.equals(normalized)) {
                return kind;
            }
        }

        return BLOOD_ALTAR;
    }
}