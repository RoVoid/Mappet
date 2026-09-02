package mchorse.mappet.utils;

/**
 * Detection for running under third-party server forks that need special
 * casing. Moved out of {@code EventHandler} — it's a platform-detection
 * concern, the same kind of thing {@link CompareVersions} already covers
 * for version compatibility, not event-handling logic.
 */
public class CompatUtils {
    private static Boolean isMohist;

    /**
     * Whether the server is running under the MohistMC fork, which
     * duplicates some player-clone behaviour that Mappet otherwise
     * handles itself.
     */
    public static boolean isMohist() {
        if (isMohist != null) return isMohist;

        try {
            Class.forName("com.mohistmc.MohistMC");
            isMohist = true;
        } catch (Exception e) {
            isMohist = false;
        }

        return isMohist;
    }
}
