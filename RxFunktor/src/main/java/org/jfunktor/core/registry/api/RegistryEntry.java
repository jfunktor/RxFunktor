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

package org.jfunktor.core.registry.api;


import java.util.HashMap;
import java.util.Map;

import org.jfunktor.common.Constants;

/**
 * Created by Vijayakumar Mohan on 04/12/2015.
 */
public class RegistryEntry {


    private Map attributes;


    private Object instance;


    private Class type;


    public RegistryEntry(Object inst, Map attributes) {
        if (inst instanceof Class) {
            type = (Class) inst;
        } else {
            this.instance = inst;
        }
        this.attributes = attributes;
        if (this.attributes == null) this.attributes = new HashMap<>();
    }

    public boolean hasType() {
        return type != null ? true : false;
    }

    public Map getAttributes() {
        return attributes;
    }

    public boolean isSingleton() {
        boolean retVal = false;
        if (attributes.containsKey(Constants.ATT_SCOPE)) {
            if (attribute(Constants.ATT_SCOPE).equals(Constants.VAL_SCOPE_SINGLETON)) {
                retVal = true;
            }
        }
        return retVal;
    }

    public Object getInstance() throws IllegalAccessException, InstantiationException {
        Object retVal = instance;
        if (isSingleton()) {
            if (hasType()) {
                if (null == instance) {
                    instance = type.newInstance(); //bind it to singleton scope so that next call will return this instance
                    //TODO: Will have to include lifecycle handling here
                    retVal = instance;
                }
            }
        } else {
            if (hasType()) {
                retVal = type.newInstance(); //bind it to singleton scope so that next call will return this instance
                //TODO: Will have to include lifecycle handling here
            }
        }
        return retVal;

    }


    @Override
    public String toString() {
        return "RegistryEntry{" +
                "type=" + type +
                ", instance=" + instance +
                ", attributes=" + attributes +
                '}';
    }

    public Object attribute(String att) {
        return attributes.get(att);
    }

    public Object attribute(String str,Object defaultVal){
        return attributes.getOrDefault(str,defaultVal);
    }

    public boolean hasAttribute(String att){
        return attributes.containsKey(att);
    }
}
