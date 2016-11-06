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

import com.google.auto.common.MoreElements;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import org.jfunktor.common.Constants;
import org.jfunktor.core.meta.api.Provider;


import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
//import static com.google.auto.common.MoreElements.getAnnotationMirror;

/**
 * The BTAnnotationProcessor is the Build time annotation processor which
 * provides the capability to generate **ServiceLoader** type entries in the
 * jar's META-INF folder. This class is invoked during the build process
 * for each class annotated with the @Provider annotation.
 *
 * The entries are generated under META-INF/jmattr/services path in the corresponding
 * jar file
 *
 * @author vj
 */
@SupportedOptions({ "debug", "verify" })
public class BTAnnotationProcessor extends AbstractProcessor {

    public static final String TYPE = "type";
    public static final String DEFAULT_SERVICE_TYPE = "DefaultServiceType";
    private Multimap<String, String> providers = HashMultimap.create();

    @Override
    public ImmutableSet<String> getSupportedAnnotationTypes() {
        return ImmutableSet.of(Provider.class.getName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }


    /**
     * <ol>
     *  <li> For each class annotated with {@link Provider}<ul>
     *      <li> Verify the {@link Provider} interface value is correct
     *      <li> Categorize the class by its service interface
     *      </ul>
     *
     *  <li> For each {@link Provider} interface <ul>
     *       <li> Create a file named {@code META-INF/app-wizard/services/<interface>}
     *       <li> For each {@link Provider} annotated class for this interface <ul>
     *           <li> Create an entry in the file
     *           </ul>
     *       </ul>
     * </ol>
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            return processImpl(annotations, roundEnv);
        } catch (Exception e) {
            // We don't allow exceptions of any kind to propagate to the compiler
            StringWriter writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            fatalError(writer.toString());
            return true;
        }
    }

    private boolean processImpl(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            generateConfigFiles();
        } else {
            processAnnotations(annotations, roundEnv);
        }

        return true;
    }

    private void processAnnotations(Set<? extends TypeElement> annotations,
                                    RoundEnvironment roundEnv) {

        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Provider.class);

        log(annotations.toString());
        log(elements.toString());

        for (Element e : elements) {
            // TODO(gak): check for error trees?
            TypeElement providerImplementer = (TypeElement) e;
            AnnotationMirror providerAnnotation = MoreElements.getAnnotationMirror(e, Provider.class).get();
            DeclaredType providerInterface = getProviderInterface(providerAnnotation);
            String providerTypeName = null;
            String providerImplementerName = null;
            if(null != providerInterface) {
                TypeElement providerType = (TypeElement) providerInterface.asElement();


                log("provider interface: " + providerType.getQualifiedName());
                log("provider implementer: " + providerImplementer.getQualifiedName());

                if (!checkImplementer(providerImplementer, providerType)) {
                    String message = "Providers must implement their provider interface. "
                            + providerImplementer.getQualifiedName() + " does not implement "
                            + providerType.getQualifiedName();
                    error(message, e, providerAnnotation);
                }

                providerTypeName = getBinaryName(providerType);
                providerImplementerName = getBinaryName(providerImplementer);


                log("provider interface binary name: " + providerTypeName);
                log("provider implementer binary name: " + providerImplementerName);

                /**
                 * Below block for defaulting service types
                 */
                /*if (providerTypeName.equals(Object.class.getName())) {
                    providerTypeName = getDefaultProviderType();

                }*/
            }else{
                //default the service type here
                providerTypeName = getDefaultServiceType();//getDefaultProviderType();
                providerImplementerName = getBinaryName(providerImplementer);
            }
            if(null == providerTypeName){
                log(String.format("Skipping the Implementation %s since no default provider is configured",providerImplementerName));
                log("Please specify a default service type for the compiler -A"+DEFAULT_SERVICE_TYPE);
            }else {
                providers.put(providerTypeName, providerImplementerName);
            }
        }
    }

    /**
     * Note : This is not used at present. leaving it here in case this is required in the future
     * @return
     */
    private String getDefaultProviderType() {
        String providerTypeName = null;
        String defaultService = processingEnv.getOptions().get(DEFAULT_SERVICE_TYPE);
        log("Default Provider Type " + defaultService);
        if ((null != defaultService) && (!defaultService.trim().isEmpty())) {
            log("Defaulting Provider interface to " + defaultService);
            providerTypeName = defaultService;
        }
        return providerTypeName;
    }

    private String getDefaultServiceType(){
        String defaultService = Object.class.getName();//Service.class.getName();
        log("Default Provider Type " + defaultService);
        return defaultService;
    }

    private void generateConfigFiles() {
        Filer filer = processingEnv.getFiler();

        for (String providerInterface : providers.keySet()) {
            String resourceFile = Constants.SERVICES_PATH + providerInterface;
            log("Working on resource file: " + resourceFile);
            try {
                SortedSet<String> allServices = Sets.newTreeSet();
                try {
                    // would like to be able to print the full path
                    // before we attempt to get the resource in case the behavior
                    // of filer.getResource does change to match the spec, but there's
                    // no good way to resolve CLASS_OUTPUT without first getting a resource.
                    FileObject existingFile = filer.getResource(StandardLocation.CLASS_OUTPUT, "",
                            resourceFile);
                    log("Looking for existing resource file at " + existingFile.toUri());
                    Set<String> oldServices = ServicesFiles.readServiceFile(existingFile.openInputStream());
                    log("Existing service entries: " + oldServices);
                    allServices.addAll(oldServices);
                } catch (IOException e) {
                    // According to the javadoc, Filer.getResource throws an exception
                    // if the file doesn't already exist.  In practice this doesn't
                    // appear to be the case.  Filer.getResource will happily return a
                    // FileObject that refers to a non-existent file but will throw
                    // IOException if you try to open an input stream for it.
                    log("Provider file did not already exist.");
                }

                Set<String> newServices = new HashSet<String>(providers.get(providerInterface));
                if (allServices.containsAll(newServices)) {
                    log("No new service entries being added.");
                    return;
                }

                allServices.addAll(newServices);
                log("New service file contents: " + allServices);
                FileObject fileObject = filer.createResource(StandardLocation.CLASS_OUTPUT, "",
                        resourceFile);
                OutputStream out = fileObject.openOutputStream();
                ServicesFiles.writeServiceFile(allServices, out);
                out.close();
                log("Wrote to: " + fileObject.toUri());
            } catch (IOException e) {
                fatalError("Unable to create " + resourceFile + ", " + e);
                return;
            }
        }
    }

    /**
     * Verifies {@link Provider} constraints on the concrete provider class.
     * Note that these constraints are enforced at runtime via the JMattrServiceLoader,
     * we're just checking them at compile time to be extra nice to our users.
     */
    private boolean checkImplementer(TypeElement providerImplementer, TypeElement providerType) {

        String verify = processingEnv.getOptions().get("verify");
        if (verify == null || !Boolean.valueOf(verify)) {
            return true;
        }

        // TODO: We're currently only enforcing the subtype relationship
        // constraint. It would be nice to enforce them all.

        Types types = processingEnv.getTypeUtils();

        return types.isSubtype(providerImplementer.asType(), providerType.asType());
    }

    /**
     * Returns the binary name of a reference type. For example,
     * {@code com.google.Foo$Bar}, instead of {@code com.google.Foo.Bar}.
     *
     */
    private String getBinaryName(TypeElement element) {
        return getBinaryNameImpl(element, element.getSimpleName().toString());
    }

    private String getBinaryNameImpl(TypeElement element, String className) {
        Element enclosingElement = element.getEnclosingElement();

        if (enclosingElement instanceof PackageElement) {
            PackageElement pkg = (PackageElement) enclosingElement;
            if (pkg.isUnnamed()) {
                return className;
            }
            return pkg.getQualifiedName() + "." + className;
        }

        TypeElement typeElement = (TypeElement) enclosingElement;
        return getBinaryNameImpl(typeElement, typeElement.getSimpleName() + "$" + className);
    }

    private DeclaredType getProviderInterface(AnnotationMirror providerAnnotation) {

        // The very simplest of way of doing this, is also unfortunately unworkable.
        // We'd like to do:
        //    Provider provider = e.getAnnotation(Provider.class);
        //    Class<?> providerInterface = provider.value();
        //
        // but unfortunately we can't load the arbitrary class at annotation
        // processing time. So, instead, we have to use the mirror to get at the
        // value (much more painful).
        //providerAnnotation.
        Map<? extends ExecutableElement, ? extends AnnotationValue> valueIndex =
                providerAnnotation.getElementValues();
        log("annotation values: " + valueIndex);
        ExecutableElement typeElement = findKey(valueIndex.keySet(), TYPE);
        log("Keys : "+valueIndex.keySet());
        log("Type Key : "+typeElement);
        if(null == typeElement){
            log("Unable to find Provider Type during build from "+valueIndex);
            return null;
        }

        AnnotationValue value = valueIndex.get(typeElement);

        return (DeclaredType) value.getValue();
    }

    private ExecutableElement findKey(Set<? extends ExecutableElement> executableElements, String s) {
        ExecutableElement returnVal = null;
        for (ExecutableElement executableElement : executableElements) {
            //log("Element name "+executableElement.getSimpleName());
            if(executableElement.getSimpleName().contentEquals(s)){
                returnVal = executableElement;
            }
        }

        return returnVal;
    }

    private void log(String msg) {
        if (processingEnv.getOptions().containsKey("debug")) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, msg);
        }
    }

    private void error(String msg, Element element, AnnotationMirror annotation) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, msg, element, annotation);
    }

    private void fatalError(String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "FATAL ERROR: " + msg);
    }
}
