package mchorse.mappet.api.scripts.user.logs;

import mchorse.mappet.api.scripts.user.IScriptFactory;

/**
 * This interface represents Mappet logger.
 *
 * <p>Use {@link IScriptFactory#getLogger()} to get Mappet logger instance.</p>
 *
 * <pre>{@code
 *    var logger = mappet.getLogger();
 *
 *    logger.info("Hello world!");
 * }</pre>
 */
public interface IMappetLogger {
    /**
     * Logging a message with <b>ERROR</b> level.
     */
    void error(String message);

    /**
     * Logging a message with <b>WARNING</b> level.
     */
    void warning(String message);

    default void warn(String message) {
        warning(message);
    }

    /**
     * Logging a message with <b>INFO</b> level.
     */
    void info(String message);

    /**
     * Logging a message with <b>DEBUG</b> level.
     */
    void debug(String message);
}
