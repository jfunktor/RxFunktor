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
import org.jfunktor.core.meta.api.Meta;
import org.jfunktor.core.meta.api.NotAProviderException;
import org.jfunktor.core.meta.api.RTAnnotationProcessor;
import org.jfunktor.core.meta.api.Provider;
import org.jfunktor.core.registry.api.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import static org.jfunktor.common.utils.LogUtil.info;


/**
 * The Default implementation of the RTAnnotationProcessor.
 * This class processes all the Types found under META-INF/jmattr/services path and
 * registers them to the **Registry**.
 *
 * @author vj
 */
public class DefaultRTAnnotationProcessor implements RTAnnotationProcessor {

    private static Logger log = LoggerFactory.getLogger(DefaultRTAnnotationProcessor.class);

    @Override
    public void process(Class impl, Registry registry) {
        if(log.isDebugEnabled()){
            log.debug("About to process Class "+impl);
        }

        if(impl.isAnnotationPresent(Provider.class)){
            Provider annotation = (Provider) impl.getAnnotation(Provider.class);

            try {
                validateType(annotation,impl);

                Map attributes = getAttributes(annotation,impl);

                registry.register(annotation.value(),attributes,impl);
                //registry.register(annotation,(org.jmattr.service.api.Service)(impl.newInstance()));
                if(log.isTraceEnabled()){
                    log.trace(String.format("%s registered as a Provider of %s",impl.getName(),annotation.value()));
                }
            } catch (NotAProviderException e) {
                log.error(String.format("Unable to Register Provider Class ", impl.getName()), e);
            }
        }else{
            if(log.isInfoEnabled()){
                log.info(String.format("Class %s does not seem to be annotated with Provider annotations. Pls check the implementation!",impl));
            }
        }



    }

    private Map getAttributes(Provider annotation, Class impl) {
        HashMap<String,Object> attributes = new HashMap<>();

        attributes.put(Constants.ATT_NAME,annotation.value());
        attributes.put(Constants.ATT_TYPE,annotation.type()); //the interface type
        //attributes.put(Constants.ATT_CLASS,impl.getName());
        attributes.put(Constants.ATT_CLASS,impl);

        //now register the other meta data associated with the type
        attributes.put(Constants.ATT_DOC,annotation.doc());

        for (Meta meta : annotation.meta()) {
            attributes.put(meta.attribute(),meta.value());
        }

        //register the module from which the class has been loaded
        //this will be useful for debugging and other purposes later
        URL sourceOfClass = getLocation(impl);
        if(sourceOfClass != null)
            attributes.put(Constants.ATT_MODULE_URL,sourceOfClass);
        else
            info(log,"Class %s does not have a valid source URL ...",impl);

        return attributes;
    }

    /**
     * Retrieves the location of the class file
     * will be useful for debugging etc
     * @param impl
     * @return
     */
    private URL getLocation(Class impl) {
        return impl.getProtectionDomain().getCodeSource().getLocation();
    }

    /**
     * This method validates whether the Class implementation is a valid service provider of the stated interface
     * @param annotation
     * @param impl
     * @throws NotAProviderException
     */
    private void validateType(Provider annotation, Class impl) throws NotAProviderException {
        if(!annotation.type().isAssignableFrom(impl)){
            throw new NotAProviderException(String.format("%s is not a valid Provider since it does not implement %s",impl.getName(),annotation.value()));
        }
    }


}
