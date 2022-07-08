package com.bss.inc.redsmokes.main.utils.nms.refl;

import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;

import java.util.regex.Matcher;
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
            Preconditions.checkNotNull(string, "String cannot be null");
            Matcher matcher = VERSION_PATTENR.matcher(string);
            if(!matcher.matches()) {
                if(!Bukkit.getName().equals("RedSmoke Fake Server")) {
                    throw new IllegalArgumentException(string + " is not in valid version format. e.g. v1_10_R1");
                }
                matcher = VERSION_PATTENR.matcher(V_12_R1.toString());
            }
        }
    }
}

