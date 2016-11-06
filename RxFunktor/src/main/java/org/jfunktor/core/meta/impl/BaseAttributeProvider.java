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

import org.jfunktor.common.Constants;
import org.jfunktor.core.meta.api.AttributeProvider;
import org.jfunktor.core.registry.api.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.jfunktor.common.utils.StringUtil.fmt;

/**
 * The AttributeProvider which provides support for transforming and translating
 * the attributes which are provided as part of the base framework.
 * The following attributes are supported in the base library
 *      1. name
 *      2. type
 *      3. doc
 *      4. class
 *      5. scope
 *      6. module_url
 *
 * @author vj
 */
public class BaseAttributeProvider implements AttributeProvider {

    private static Logger log = LoggerFactory.getLogger(BaseAttributeProvider.class);

    private Registry context;

    @Override
    public void setContext(Object context) {
        this.context = (Registry)context;
    }

    @Override
    public Map<String, Function<Object, ?>> getAttributeMap() {
        //Provide the list of attributes and their appropriate mapping functions
        HashMap<String,Function<Object,?>> attributeMap = new HashMap<>();

        addNameAttributeHandler(attributeMap);

        addTypeAttributeHandler(attributeMap);

        addDocAttributeHandler(attributeMap);

        addClassAttributeHandler(attributeMap);

        addScopeAttributeHandler(attributeMap);

        addModuleURLAttributeHandler(attributeMap);


        return attributeMap;
    }



    private void addModuleURLAttributeHandler(HashMap<String, Function<Object, ?>> attributeMap) {
        attributeMap.put(Constants.ATT_MODULE_URL,(value)->{
            //Scope has to be one of singleton,request nothing else
            String retVal = value.toString().trim().length() > 0 ? value.toString() : null;

            if(retVal == null){
                throw new AssertionError(fmt("invalid value \"%s\" for Attribute(%s)",value, Constants.ATT_MODULE_URL));
            }

            if(!(value instanceof URL)){
                //Try whether we can convert to an URL
                try {
                    URL url = new URL(value.toString());
                    return url;
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    throw new AssertionError(fmt("invalid value \"%s\" for Attribute(%s)",value, Constants.ATT_MODULE_URL));
                }
            }

            //just return no transformation required in this case
            //It is probably a valid URL instance already
            return value;
        });
    }

    private void addScopeAttributeHandler(HashMap<String, Function<Object, ?>> attributeMap) {
        attributeMap.put(Constants.ATT_SCOPE,(value)->{
            //Scope has to be one of singleton,request nothing else
            String retVal = value.toString().trim().length() > 0 ? value.toString() : null;

            if(retVal == null){
                throw new AssertionError(fmt("invalid value \"%s\" for Attribute(%s)",value, Constants.ATT_SCOPE));
            }

            if(!(retVal.equals(Constants.VAL_SCOPE_SINGLETON) ||
                   retVal.equals(Constants.VAL_SCOPE_REQUEST) )){
                throw new AssertionError(fmt("invalid value \"%s\" for Attribute(%s). Should be one of %s or %s",value,
                        Constants.ATT_SCOPE, Constants.VAL_SCOPE_REQUEST, Constants.VAL_SCOPE_SINGLETON));
            }

            //just return no transformation required in this case
            return retVal;
        });
    }

    /*private void addClassAttributeHandler(HashMap<String, Function<Object, ?>> attributeMap) {
        attributeMap.put(Constants.ATT_CLASS,(value)->{
            //Class is just a string so just do the base validation checks and return the value
            //TODO: maybe we can do the valid class checks later
            String retVal = value.toString().trim().length() > 0 ? value.toString() : null;

            if(retVal == null){
                throw new AssertionError(fmt("invalid value \"%s\" for Attribute(%s)",value, Constants.ATT_CLASS));
            }

            return retVal;
        });
    }*/

    private void addClassAttributeHandler(HashMap<String, Function<Object, ?>> attributeMap) {
        attributeMap.put(Constants.ATT_CLASS,(value)->{
            //Class is an instance of Class
            if(!(value instanceof Class)){
                String retVal = value.toString().trim().length() > 0 ? value.toString() : null;

                if(retVal == null){
                    throw new AssertionError(fmt("invalid value \"%s\" for Attribute(%s)",value, Constants.ATT_CLASS));
                }

                //load the class and check whether it is valid
                try {
                    Class<?> aClass = Class.forName(retVal);
                    return aClass;
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    throw new AssertionError(fmt("invalid value \"%s\" for Attribute(%s). Unable to load the specified class",value, Constants.ATT_CLASS));
                }
            }else{
                return value;
            }
        });
    }

    private void addDocAttributeHandler(HashMap<String, Function<Object, ?>> attributeMap) {
        attributeMap.put(Constants.ATT_DOC,(value)->{
            //Doc is just a string so just do the base validation checks and return the value
            String retVal = value.toString().trim().length() > 0 ? value.toString() : null;

            if(retVal == null){
                throw new AssertionError(fmt("invalid value \"%s\" for Attribute(%s)",value, Constants.ATT_DOC));
            }

            return retVal;
        });
    }


    private void addTypeAttributeHandler(HashMap<String, Function<Object, ?>> attributeMap) {
        attributeMap.put(Constants.ATT_TYPE,(value)->{
            //Type should be an instance of Class
            if(value == null){
                throw new AssertionError(fmt("invalid value \"%s\" for Attribute(%s)",value, Constants.ATT_TYPE));
            }

            if(!(value instanceof Class)){
                throw new AssertionError(fmt("invalid value \"%s\" for Attribute(%s)",value, Constants.ATT_TYPE));
            }

            return value; //return as is since it is already of Class
        });
    }

    private void addNameAttributeHandler(HashMap<String, Function<Object, ?>> attributeMap) {
        //name attribute handling is done here
        attributeMap.put(Constants.ATT_NAME,(value)->{
            //Name is just a string so just do the base validation checks and return the value
            String retVal = value.toString().trim().length() > 0 ? value.toString() : null;
            if(retVal == null){
                throw new AssertionError(fmt("invalid value \"%s\" for Attribute(%s)",value, Constants.ATT_NAME));
            }

            return retVal;
        });
    }
}
