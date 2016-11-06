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

package org.jfunktor.core.meta.impl;

import org.jfunktor.core.meta.api.FunctionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.jfunktor.common.utils.LogUtil.debug;

/**
 * Provides the base functions for use in simple scripting kind of requirements
 * The following functions are provided
 *
 * map [input] : Convert the input to a Map
 *
 * @author vj
 */
public class BaseFunctionProvider implements FunctionProvider {

    private static Logger log = LoggerFactory.getLogger(BaseFunctionProvider.class);


    @Override
    public Map<String, Function<String, Object>> getFunctionMap() {

        HashMap<String,Function<String,Object>> functionMap = new HashMap<>();

        addMapHandler(functionMap);

        addListHandler(functionMap);

        return functionMap;
    }

    private void addListHandler(HashMap<String, Function<String, Object>> functionMap) {
        functionMap.put("list",(value)->{
            //here input is expected in key value key value format
            String[] splitInput = value.split(" ");

            List<String> splitValues = new ArrayList<String>();

            for (String item : splitInput) {
                splitValues.add(item);
            }

            return splitValues;
        });
    }

    /**
     * Adds the Map handler for
     * map [input]
     * @param functionMap
     */
    private void addMapHandler(HashMap<String, Function<String, Object>> functionMap) {
        functionMap.put("map",(value)->{
            //here input is expected in key value key value format
            String[] splitInput = value.split(" ");

            Map<String,List<String>> retVal = new HashMap();

            String key = null;
            String val = null;
            for(int i=0;i<splitInput.length;i++){
                if((i & 1) == 0){ //bitwise is supposed to be faster than % here
                    //even
                    key = splitInput[i];

                    //reset val to null
                    val = null;
                }else{
                    //odd
                    val = splitInput[i];
                }

                if(null != key && null != val){
                    //only insert to map in this case
                    //cases where there is no value and only key is also ignored

                    //if there is a duplicate key then add to the list of values
                    if(retVal.containsKey(key)){
                        List listVal = retVal.get(key);
                        listVal.add(val);
                    }else {
                        ArrayList listVal = new ArrayList();
                        listVal.add(val);
                        retVal.put(key, listVal);
                    }
                }

            }

            debug(log,"Function Transformation Input : \"%s\" Output: \"%s\"",value,retVal);

            return retVal;
        });
    }

}
