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


import org.jfunktor.core.registry.api.Registry;

/**
 * The RTAnnotationProcessor is used to process the discovered types (classes) during the scanning phase of the
 * MetaScanner.
 *
 * Custom implementations of the RTAnnotationProcessor can be passed to the MetaScanner before the scanAndRegister()
 * call is performed.
 *
 * @author vj
 */
public interface RTAnnotationProcessor {

    /**
     * The RTAnnotationProcessor interface used for processing Type registration @runtime
     * This is NOT used during the Build Phase
     * This is the equivalent of the Java Annotation Processor , during the runtime
     * @param impl
     * @param registry
     */
    void process(Class impl, Registry registry);
}
