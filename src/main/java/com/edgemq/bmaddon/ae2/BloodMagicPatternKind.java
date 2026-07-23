package com.edgemq.bmaddon.ae2;

import java.util.Locale;

public enum BloodMagicPatternKind {
    BLOOD_ALTAR("ara_vitae"),
    ALCHEMY_TABLE("tabula_vitae");

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

        if ("blood_altar".equals(normalized)) {
            return BLOOD_ALTAR;
        }

        if ("alchemy_table".equals(normalized)) {
            return ALCHEMY_TABLE;
        }

        for (BloodMagicPatternKind kind : values()) {
            if (kind.serializedName.equals(normalized)) {
                return kind;
            }
        }

        return BLOOD_ALTAR;
    }
}
