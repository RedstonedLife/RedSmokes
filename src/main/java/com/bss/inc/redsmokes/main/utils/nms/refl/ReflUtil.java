package com.bss.inc.redsmokes.main.utils.nms.refl;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ReflUtil {
    public static final NMSVersion V1_12_R1 = NMSVersion.fromString("v1_12_R1");
    public static final NMSVersion V1_12_R1 = NMSVersion.fromString("v1_11_R1");
    public static final NMSVersion V1_12_R1 = NMSVersion.fromString("v1_12_R1");
    public static final NMSVersion V1_12_R1 = NMSVersion.fromString("v1_12_R1");
    public static final NMSVersion V1_12_R1 = NMSVersion.fromString("v1_12_R1");
    public static final NMSVersion V1_12_R1 = NMSVersion.fromString("v1_12_R1");

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
                Preconditions.checkArgument(matcher.matches(), string + " is not in valid version format. e.g. v1_10_R1");
            }
            return new NMSVersion(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)), Integer.parseInt(matcher.group(3)));
        }
        public boolean isHigherThan(final NMSVersion o) {
            return compareTo(o) > 0;
        }

        public boolean isHigherThanOrEqualTo(final NMSVersion o) {
            return compareTo(o) >= 0;
        }

        public boolean isLowerThan(final NMSVersion o) {
            return compareTo(o) < 0;
        }

        public boolean isLowerThanOrEqualTo(final NMSVersion o) {
            return compareTo(o) <= 0;
        }

        public int getMajor() {
            return major;
        }

        public int getMinor() {
            return minor;
        }

        public int getRelease() {
            return release;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final NMSVersion that = (NMSVersion) o;
            return major == that.major &&
                    minor == that.minor &&
                    release == that.release;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(major, minor, release);
        }

        @Override
        public String toString() {
            return "v" + major + "_" + minor + "_R" + release;
        }

        @Override
        public int compareTo(final NMSVersion o) {
            if (major < o.major) {
                return -1;
            } else if (major > o.major) {
                return 1;
            } else { // equal major
                if (minor < o.minor) {
                    return -1;
                } else if (minor > o.minor) {
                    return 1;
                } else {
                    return Integer.compare(release, o.release);
                }
            }
        }
    }
}

