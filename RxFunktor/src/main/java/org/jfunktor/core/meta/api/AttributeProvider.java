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

package org.jfunktor.core.meta.api;

import java.util.Map;
import java.util.function.Function;

/**
 * This interface is an abstraction of the Attribute Provider.
 * To support the extensions the users of the library can add arbitrary attributes.
 * To support the validation and data transformation of these attributes the providers of
 * new attributes need to implement these interfaces and register them through the standard
 * ServerLoader mechanism supported in Java
 * @author vj
 */
public interface AttributeProvider {


    /**
     * The context for the Attribute Provider. This is usually the **Registry** which serves
     * as the context of the AttributeProvider.
     * @param context
     */
    void setContext(Object context);

    /**
     * The attribute map provided by the Attribute Provider. This is a map of named Functions
     * which validates and transforms the type of the Attribute from String to other appropriate ones.
     * @return
     */
    public Map<String,Function<Object,?>> getAttributeMap();

}
