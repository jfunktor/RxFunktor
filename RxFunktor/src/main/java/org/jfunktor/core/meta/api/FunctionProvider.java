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
 * A provider abstraction for registering functions which can transform a given input to an out
 * These can be looked up by a key which is expected to be the function name
 * Used for simple scripting and other purposes
 *
 * @author vj
 */
public interface FunctionProvider {

    /**
     * The function map which is used for transforming complex Attribute values
     *
     * @return
     */
    Map<String,Function<String,Object>> getFunctionMap();

}
