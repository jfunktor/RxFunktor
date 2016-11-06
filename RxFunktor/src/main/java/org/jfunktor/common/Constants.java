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

package org.jfunktor.common;

/**
 * Created by Vijayakumar Mohan on 14/01/2016.
 */
public class Constants {

    /**
     * Core Meta Constants - Attributes
     */
    public static final String ATT_NAME = "name";
    public static final String ATT_VERSION = "version";
    public static final String ATT_CLASS = "class";
    public static final String ATT_SCOPE = "scope";
    public static final String ATT_TYPE = "type";
    public static final String ATT_DOC = "doc";
    public static final String ATT_MODULE_URL = "module_url";

    public static final String VAL_VERSION_DEFAULT = "1.0";
    public static final String VAL_SCOPE_SINGLETON = "singleton";
    public static final String VAL_SCOPE_REQUEST = "request";
    public static final String VAL_SCOPE_DEFAULT = VAL_SCOPE_REQUEST;

    /**
     * The path in the jar where the app-wizard services are registered
     */
    public static final String SERVICES_PATH = "META-INF/jmattr/services/";
    public static final String FUNCTION_MAP = "function.map";
    public static final String ATTRIBUTE_MAP = "attribute.map";

}
