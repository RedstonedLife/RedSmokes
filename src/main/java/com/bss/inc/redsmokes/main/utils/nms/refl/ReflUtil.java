package com.bss.inc.redsmokes.main.utils.nms.refl;

import jdk.internal.util.Preconditions;

import java.util.regex.Pattern;

public final class ReflUtil {
    public static final NMSVersion

    public static final class NMSVersion implements Comparable<NMSVersion> {
        private static final Pattern VERSION_PATTENR = Pattern.compile("^v(\\d+)_(\\d+)_R(\\d+)");

        private final int major;
        private final int minor;
        private final int release;

        private NMSVersion(int major, int minor, int release) {
            this.major = major;
            this.minor = minor;
            this.release = release;
        }

        public static NMSVersion fromString(String string) {
            Preconditions.checkNotNull()
        }
    }
}

