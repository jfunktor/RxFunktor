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

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

/**
 * Provider
 * ======
 *
 * This is the primary annotation for designating a "Provider" when using JMattr library.
 * The Provider represents a Provider of a "type" or "Service" depending on the context.
 * The **Provider** has the following default attributes
 *
 *      Provider
 *          1. name - Name of the provider
 *          2. value - The Class implemented by the provider
 *          3. doc - Simple documentation for the provider
 *          4. attributes - One or more attributes represented by @Meta
 *
 *
 * @author Vj
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({TYPE,PARAMETER,FIELD,METHOD})
public @interface Provider {

    /**
     * Represents the **Name** of the Provider.
     * The primary attribute which can be used to look up the Provider
     * @return
     */
    String value();

    /**
     * Represents the **Type** of the Provider.
     * Usually the Interface implemented by the Implementation Provider
     * @return
     */
    Class type() default Object.class;

    /**
     * Represents short documentation about the Provider.
     * This information is available for querying at runtime.
     * @return
     */
    String doc() default "Please Document your Provider!";

    /**
     * An array of @Meta information. These attributes are also available for querying at runtime.
     * @return
     */
    Meta[] meta() default {};

}
