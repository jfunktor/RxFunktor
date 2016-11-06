/*
 *
 * Copyright (c) 2016. Vijayakumar Mohan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *   		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * JMattr - The meta attribute library for java!
 *
 */

package org.jfunktor.common.utils;

import static org.jfunktor.common.utils.StringUtil.fmt;

import org.slf4j.Logger;

/**
 * Created by Vijayakumar Mohan on 21/12/2015.
 */
public class LogUtil {

    /**
     * Logging info messages with format support and its arguments
     * @param log
     * @param message
     * @param args
     */
    public static void info(Logger log, String message, Object... args) {
        if (log.isInfoEnabled()) {
            if (args == null) {
                log.info(message);
            } else {
                log.info(fmt(message, args));
            }
        }
    }

    /**
     * Logging debug messages with format support and its arguments
     * @param log
     * @param message
     * @param args
     */
    public static void debug(Logger log, String message, Object... args) {
        if (log.isDebugEnabled()) {
            if (args == null) {
                log.debug(message);
            } else {
                log.debug(fmt(message, args));
            }
        }
    }


    /**
     * Logging trace messages with format support and its arguments
     * @param log
     * @param message
     * @param args
     */
    public static void trace(Logger log, String message, Object... args) {
        if (log.isTraceEnabled()) {
            if (args == null) {
                log.trace(message);
            } else {
                log.trace(fmt(message, args));
            }
        }
    }


    /**
     * Logging warning for the message with string formatting and its arguments
     *
     * @param log
     * @param message
     * @param args
     */
    public static void warn(Logger log, String message, Object... args) {
        //System.err.println("Warning : "+fmt(message,args));
        warn(log, null, message, args);
    }


    /**
     * Logging warning for the message with string formatting and its arguments with the exception
     *
     * @param log
     * @param err
     * @param message
     * @param args
     */
    public static void warn(Logger log, Throwable err, String message, Object... args) {
        if (log.isWarnEnabled()) {
            if (err != null) {
                if (args == null) {
                    log.warn(message, err);
                } else {
                    log.warn(fmt(message, args), err);
                }
            } else {
                if (args == null) {
                    log.warn(message);
                } else {
                    log.warn(fmt(message, args));
                }
            }
        }
    }


    /**
     * Logging errors for the message with string formatting and its arguments
     *
     * @param log
     * @param message
     * @param args
     */
    public static void error(Logger log, String message, Object... args) {
        error(log, null, message, args);
    }


    /**
     * Logging errors for the message with string formatting and its arguments
     *
     * @param log
     * @param err
     * @param message
     * @param args
     */
    public static void error(Logger log, Throwable err, String message, Object... args) {
        if (log.isErrorEnabled()) {
            if (err != null) {
                if (args == null) {
                    log.error(message, err);
                } else {
                    log.error(fmt(message, args), err);
                }
            } else {
                if (args == null) {
                    log.error(message);
                } else {
                    log.error(fmt(message, args));
                }
            }
        }
    }


}
