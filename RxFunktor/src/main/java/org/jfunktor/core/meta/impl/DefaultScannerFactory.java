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

import org.apache.commons.vfs2.FileSystemException;
import org.jfunktor.core.meta.api.MetaScanner;
import org.jfunktor.core.registry.impl.RegistryImpl;

/**
 * Created by vj on 3/14/16.
 */
public class DefaultScannerFactory {

    private DefaultScannerFactory(){}

    /**
     * Factory method for retrieving the Default Scanner pre configured with
     * the default annotation processor and the default Registry implementation
     * @return
     */
    public static MetaScanner getDefaultScanner() throws FileSystemException {
        MetaScanner scanner = new MetaScanner(new DefaultRTAnnotationProcessor(),new RegistryImpl());
        scanner.scanAndRegister();
        return scanner;
    }

}
