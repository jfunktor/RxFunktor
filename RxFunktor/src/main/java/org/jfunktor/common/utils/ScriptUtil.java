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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jfunktor.common.utils.LogUtil.debug;
import static org.jfunktor.common.utils.LogUtil.warn;
import static org.jfunktor.common.utils.StringUtil.fmt;

import java.util.Map;
import java.util.function.Function;


/**
 * Created by Vijayakumar Mohan on 12/01/2016.
 */
public class ScriptUtil {

    private static Logger log = LoggerFactory.getLogger(ScriptUtil.class);

    /**
     * Converts a given string to a function map
     * The script input is expected to be in the below format
     * <code>
     *     function [input]
     * </code>
     * @param script
     * @return
     */
    public static String[] toFunArgArray(String script){
        if(null == script || script.trim().length() == 0){
            throw new AssertionError(fmt("invalid script %s",script));
        }

        script = script.trim(); //remove the extra spaces

        String[] splitStrings = script.split(" ",2);//split the script into function and arguments ' ' is the expected delimiter

        if(splitStrings.length > 2){
            warn(log,"Script \"%s\" has resulted in %s strings post parsing. May lead to a problem pls check the script",
                    script,splitStrings.length);
        }

        debug(log,"Function is %s and argument is %s",splitStrings[0],splitStrings[1]);

        return splitStrings;
    }

    /**
     * Evaluates and provides the result of the given script and the map of functions
     * The script is expected in the below format
     * <code>
     *     function [input]
     * </code>
     * @param script
     * @param functionMap
     * @return
     */
    public static Object eval(String script, Map<String,Function<String,Object>> functionMap){
        //parse the string
        String[] funArg = toFunArgArray(script);

        //The output of the above should be fun and args respectively in the array
        String functionName = funArg[0];
        String arguments = funArg[1].trim();

        if(arguments.startsWith("[")){ //remove the first [ if present
            arguments = arguments.substring(1);
        }

        if(arguments.endsWith("]")){//remove the last ] if present
            arguments = arguments.substring(0,arguments.length()-1);
        }

        debug(log,"About to evaluate \"%s(%s)\"",functionName,arguments);

        //look up the function in the map
        Function<String, Object> function = functionMap.get(functionName);

        if(null == function){
            throw new AssertionError(fmt("Invalid function \"%s\" specified. No such found in given function map",function));
        }

        Object retVal = function.apply(arguments);

        debug(log,"\"%s(%s)\" evaluated to %s",functionName,arguments,retVal);

        return retVal;

    }
}
