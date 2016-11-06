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


import org.apache.commons.vfs2.FileSystemException;
import org.jfunktor.common.Constants;
import org.jfunktor.common.vfs.VirtualFileSystem;
import org.jfunktor.core.registry.api.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.jfunktor.common.utils.StringUtil.fmt;


/**
 * Instances of this class is used to scan and register the **Types** found through the
 * specified **ClassLoader**. The MetaScanner scans for the types and registers them to the
 * specified **Registry**
 *
 * @author vj
 */
public class MetaScanner {

    private RTAnnotationProcessor proc;
    private Registry registry;

    private static Logger log = LoggerFactory.getLogger(MetaScanner.class);

    /**
     * @param processor The Runtime Annotation Processor which handles the individual class processing
     * @param reg The registry which keeps track of the Registered **Types** scanned by the MetaScanner
     */
    public MetaScanner(RTAnnotationProcessor processor, Registry reg){
        proc = processor;
        registry = reg;
    }

    /**
     * Retrieves the **Registry** associated with the Scanner instance
     * @return
     */
    public Registry getRegistry() {
        return registry;
    }

    /**
     * Scan and Register the **Types** found through the Current ClassLoader (ClassLoader of this MetaScanner Class)
     * @throws FileSystemException
     */
    public void scanAndRegister() throws FileSystemException {
        scanAndRegister(MetaScanner.class.getClassLoader());
    }

    /**
     * Scan and Register the **Types** found through the given ClassLoader
     * @param loader The ClassLoader to use for scanning and loading the Types visible through the classloader
     * @throws FileSystemException
     */
    public void scanAndRegister(ClassLoader loader) throws FileSystemException {

        scanAndRegisterAttMaps(loader);

        List<Class> classes = VirtualFileSystem.listTypesFromClassPath(Constants.SERVICES_PATH, loader);

        scanAndRegister(classes);
    }

    private void scanAndRegisterAttMaps(ClassLoader loader) {

        //scan and load the function providers

        java.util.ServiceLoader<FunctionProvider> functionProviders = java.util.ServiceLoader.load(FunctionProvider.class);

        Map<String,Function<String,Object>> functionMap = new HashMap<>();

        for (FunctionProvider functionProvider : functionProviders) {
            Map<String, Function<String, Object>> funMap = functionProvider.getFunctionMap();
            functionMap.putAll(funMap);
        }

        if(!functionMap.isEmpty()) {
            //now register the function map to the registry
            registry.register(Constants.FUNCTION_MAP, functionMap);
        }

        //scan and load the attribute providers
        java.util.ServiceLoader<AttributeProvider> attributeProviders = java.util.ServiceLoader.load(AttributeProvider.class);

        Map<String,Function<Object,?>> attributeMap = new HashMap<>();

        for (AttributeProvider attributeProvider : attributeProviders) {
            attributeProvider.setContext(registry);
            Map<String, Function<Object, ?>> attMap = attributeProvider.getAttributeMap();
            attributeMap.putAll(attMap);
        }

        if(!attributeMap.isEmpty()){
            //now register the function map to the registry
            registry.register(Constants.ATTRIBUTE_MAP, attributeMap);
        }

    }

    private void validateType(Class interfaceType,Class impl) throws NotAProviderException {
        if(log.isDebugEnabled()){
            log.debug(fmt("validateType %s assignable from %s",interfaceType,impl));
        }
        if(!interfaceType.isAssignableFrom(impl)){
            throw new NotAProviderException(String.format("Not a valid Provider %s",impl.getName()));
        }
    }


    /**
     * Scan and Register the given list of **Types**
     * @param listOfTypesToScan The list of Class **Types** which need to be registered
     */
    public void scanAndRegister(List<Class> listOfTypesToScan) {
        for (Class aClass : listOfTypesToScan) {
            scanAndRegister(aClass);
        }

    }

    private void scanAndRegister(Class interfaceType) {
        for (Class impl : JMattrServiceLoader.load(interfaceType)) {
            impl.getProtectionDomain().getCodeSource().getLocation();
            if(impl.isAnnotationPresent(Provider.class)){
                //Provider annotation = (Provider) impl.getAnnotation(Provider.class);

                try {
                    validateType(interfaceType,impl);

                    //do the required binding here
                    proc.process(impl,registry);

                    if(log.isTraceEnabled()){
                        log.trace(String.format("Registered Provider %s",impl.getName()));
                    }
                } catch (NotAProviderException e) {
                    log.error(String.format("Invalid Provider Definition ", impl.getName()), e);
                }
            }else{
                if(log.isInfoEnabled()){
                    log.info(String.format("Class %s does not seem to be annotated with Provider annotations. Pls check the implementation!",impl));
                }
            }

        }

    }
}
