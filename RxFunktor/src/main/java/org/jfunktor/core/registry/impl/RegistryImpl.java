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

package org.jfunktor.core.registry.impl;


import org.jfunktor.common.Constants;
import org.jfunktor.core.registry.api.Registry;
import org.jfunktor.core.registry.api.RegistryEntry;
import org.jfunktor.core.registry.api.UnknownNameException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.jfunktor.common.utils.LogUtil.info;
import static org.jfunktor.common.utils.LogUtil.warn;

/**
 * The Default implementation of the Registry interface.
 * This is a simple Map based implementation of the Registry interface.
 *
 * It is implemented as a name - value map of the all entities registered.
 * The key feature of this implementation is that the Registry entries are
 * a map of name and the values are a list of RegistryEntry objects.
 * This implementation can support registering more that one instance with the same name.
 * The multiple entries just for a list which are registered under the same name.
 *
 * The current implementation does not support removing any entry. Once registered
 * they remain in the map as long as the JVM process is active
 *
 * @author vj
 */
public class RegistryImpl implements Registry {

    private static Logger log = LoggerFactory.getLogger(RegistryImpl.class);


    private HashMap<String, List<RegistryEntry>> registryMap = new HashMap<>();
    private boolean attributeValidationOn = true;



    @Override
    public void register(String name, Map attributes, Object o) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("Registering %s with name %s and %s", o, name, attributes));
        }

        if (name == null || o == null) {
            throw new IllegalArgumentException("Name or Instance/Type cannot be null");
        }

        if (name.trim().length() == 0) {
            throw new IllegalArgumentException("Name cannot be empty");
        }

        if (null == attributes)
            attributes = new HashMap<>();

        if (attributeValidationOn) {
            if (!(Constants.ATTRIBUTE_MAP.equals(name) || Constants.FUNCTION_MAP.equals(name))) { //Dont process attributes for ATTRIBUTE map itself...
                //process and transform the attributes before actual registration
                attributes = processAttributes(this, attributes);
            }
        }

        createEntry(name, attributes, o);
    }

    @Override
    public void register(String name, Object o) {
        register(name, new HashMap(), o);
    }



    @Override
    public void register(Class type, Map attributes, Object o) {
        //just delegate to the name version of the register
        register(type.getName(), attributes, o);
    }

    private void createEntry(String name, Map attributes, Object o) {
        List<RegistryEntry> registryEntries = registryMap.containsKey(name) ?
                registryMap.get(name) : new ArrayList<>();

        fillDefaultAttributes(name, attributes, o);

        registryEntries.add(new RegistryEntry(o, attributes));

        //update the registry again - we may end up using a distributed cache
        registryMap.put(name, registryEntries);
    }

    private void fillDefaultAttributes(String name, Map attributes, Object o) {

        //fill ATT_NAME attribute
        attributes.put(Constants.ATT_NAME, name);
        if (!attributes.containsKey(Constants.ATT_VERSION)) {
            attributes.put(Constants.ATT_VERSION, Constants.VAL_VERSION_DEFAULT);
        }

        //fill ATT_CLASS attribute if not already done
        if (!attributes.containsKey(Constants.ATT_CLASS)) {
            if (!(o instanceof Class)) {
                attributes.put(Constants.ATT_CLASS, o.getClass().getName());
            } else {
                attributes.put(Constants.ATT_CLASS, ((Class) o).getName());
            }
        }

        //fill ATT_TYPE attribute if not already done
        if (!attributes.containsKey(Constants.ATT_TYPE)) {
            if (!(o instanceof Class)) {
                attributes.put(Constants.ATT_TYPE, o.getClass());
            } else {
                attributes.put(Constants.ATT_TYPE, (Class) o);
            }
        }

        //For object instances the ATT_SCOPE is defaulted to Singleton scope
        if (!(o instanceof Class)) {
            attributes.put(Constants.ATT_SCOPE, Constants.VAL_SCOPE_SINGLETON);
        }

        //fill ATT_SCOPE attribute if not already done
        if (!attributes.containsKey(Constants.ATT_SCOPE)) {
            attributes.put(Constants.ATT_SCOPE, Constants.VAL_SCOPE_DEFAULT);
        }
    }


    @Override
    public Object lookup(String name, Predicate<RegistryEntry> filter) throws UnknownNameException {
        if (log.isDebugEnabled()) {
            log.debug(String.format("About to lookup %s with filter %s", name, filter));
        }

        if (!registryMap.containsKey(name)) {
            throw new UnknownNameException(String.format("Lookup Name %s is unknown to me", name));
        }

        List<RegistryEntry> registryEntries = registryMap.get(name);

        RegistryEntry entry = null;

        if (null == filter) {
            //pick up the first item from the list
            entry = registryEntries.get(0);
        } else {
            //apply the filter and get the entry
            try {
                entry = applyFilter(registryEntries, filter);
            } catch (NoSuchElementException e) {
                //this means there was no match for the filter
                throw new UnknownNameException(String.format("Lookup Name %s is unknown to me, filter returned empty", name));
            }
        }

        try {
            Object retVal = entry.getInstance();
            return retVal;
        } catch (IllegalAccessException e) {
            throw new UnknownNameException(String.format("Lookup Name %s is unknown to me, Instantiation failed", name), e);
        } catch (InstantiationException e) {
            throw new UnknownNameException(String.format("Lookup Name %s is unknown to me, Instantiation failed", name), e);
        }


    }

    @Override
    public <T> T lookup(String name, Class<T> type) throws UnknownNameException {

        return (T)lookup(name,registryEntry -> {
            boolean retVal = false;
            Class clz = (Class)registryEntry.attribute(Constants.ATT_CLASS);
            //check whether the declared type is assignable to the given type
            if(type.isAssignableFrom(clz)){
                retVal = true;
            }
            return retVal;

        });
    }

    @Override
    public <T> T lookup(String name, Class<T> type, Predicate<RegistryEntry> filter) throws UnknownNameException {


        return (T)lookup(name,filter.and(registryEntry -> {
            boolean retVal = false;
            Class clz = (Class)registryEntry.attribute(Constants.ATT_CLASS);
            //check whether the declared type is assignable to the given type
            if(type.isAssignableFrom(clz)){
                retVal = true;
            }
            return retVal;
        }));
    }


    @Override
    public List<Object> lookupAll(String name, Predicate<RegistryEntry> filter) throws UnknownNameException {
        if (log.isDebugEnabled()) {
            log.debug(String.format("About to lookup %s with filter %s", name, filter));
        }

        if (!registryMap.containsKey(name)) {
            throw new UnknownNameException(String.format("Lookup Name %s is unknown to me", name));
        }

        List<RegistryEntry> registryEntries = registryMap.get(name);

        List<RegistryEntry> entriesByFilter = getEntriesByFilter(filter, registryEntries);

        List<Object> finalList = getObjects(entriesByFilter);

        return finalList;


    }


    /**
     * Lists and filters the given list of Registry entries and returns a list of Registry Entries
     *
     * @param filter
     * @param registryEntries
     * @return
     */
    private List<RegistryEntry> getEntriesByFilter(Predicate<RegistryEntry> filter, List<RegistryEntry> registryEntries) {
        Stream<RegistryEntry> registryEntryStream = registryEntries.stream().filter(filter);

        Iterator<RegistryEntry> iterator = registryEntryStream.iterator();
        ArrayList<RegistryEntry> finalList = new ArrayList<>();

        while (iterator.hasNext()) {
            finalList.add(iterator.next());
        }
        return finalList;
    }


    private List<Object> getObjects(List<RegistryEntry> registryEntries) {
        Iterator<RegistryEntry> iterator = registryEntries.iterator();
        ArrayList<Object> finalList = new ArrayList<>();

        while (iterator.hasNext()) {
            try {
                finalList.add(iterator.next().getInstance());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }
        return finalList;
    }

    @Override
    public List<Object> lookupAll(Class type, Predicate<RegistryEntry> filter) throws UnknownNameException {
        //just delegate the the name version of the same method
        return lookupAll(type.getName(), filter);
    }

    @Override
    public List<Object> lookupAll(Predicate<RegistryEntry> filter) throws UnknownNameException {
        //Get the entries filtered as a list
        //then instantiate them and provide the list of objects

        List<RegistryEntry> registryEntries = lookupAllEntries(filter);

        return getObjects(registryEntries);

    }

    @Override
    public List<Object> lookupAll(Predicate<RegistryEntry> filter, Comparator<RegistryEntry> sorter) throws UnknownNameException {
        //Get the entries filtered as a list
        //then instantiate them and provide the list of objects

        List<RegistryEntry> registryEntries = lookupAllEntries(filter);

        //sort them using the comparator / sorter
        registryEntries.sort(sorter);

        return getObjects(registryEntries);
    }

    @Override
    public List<RegistryEntry> lookupAllEntries(Predicate<RegistryEntry> filter) throws UnknownNameException {
        //get a consolidated list of values from across all the keys
        //then run through the filter for selecting the values
        Collection<List<RegistryEntry>> values = registryMap.values();


        ArrayList<RegistryEntry> entries = new ArrayList<>();

        for (List<RegistryEntry> value : values) {
            entries.addAll(value);
        }

        List<RegistryEntry> finalList = getEntriesByFilter(filter, entries);

        return finalList;
    }

    @Override
    public List<RegistryEntry> lookupAllEntries(String name, Predicate<RegistryEntry> filter) throws UnknownNameException {
        if (log.isDebugEnabled()) {
            log.debug(String.format("About to lookup %s with filter %s", name, filter));
        }

        if (!registryMap.containsKey(name)) {
            throw new UnknownNameException(String.format("Lookup Name %s is unknown to me", name));
        }

        List<RegistryEntry> registryEntries = registryMap.get(name);

        List<RegistryEntry> entriesByFilter = getEntriesByFilter(filter, registryEntries);

        return entriesByFilter;
    }

    @Override
    public void forEach(BiConsumer<String, List<RegistryEntry>> consumer) {
        this.registryMap.forEach(consumer); //simple delegation to the underlying map
    }


    @Override
    public Object lookup(String name) throws UnknownNameException {
        return lookup(name, registryEntry -> {
            return true; //match every valid entry
        });
    }


    @Override
    public void validateAttributes(boolean b) {
        attributeValidationOn = b;
    }

    private RegistryEntry applyFilter(List<RegistryEntry> registryEntries, Predicate<RegistryEntry> filter) {
        Optional<RegistryEntry> registryEntryOptional = registryEntries.stream().filter(filter).findFirst();

        return registryEntryOptional.get();
    }

    /**
     * processes the attributes
     *
     * @param registry
     * @param attributes
     */
    private Map processAttributes(Registry registry, Map attributes) {
        //check if there is a provider registered
        final Map retVal = new HashMap<>();
        try {
            Map<String, Function<Object, ?>> attributeMap = (Map<String, Function<Object, ?>>) registry
                    .lookup(Constants.ATTRIBUTE_MAP);

            //process all the attributes
            attributes.forEach((key, value) -> {
                //validate and transform the value as per the attribute map
                if (attributeMap.containsKey(key)) {
                    Function<Object, ?> attributeHandler = attributeMap.get(key);
                    try {
                        Object transformedVal = attributeHandler.apply(value);
                        retVal.put(key, transformedVal);
                    } catch (Exception e) {
                        warn(log, "Processing (%s,%s) failed due to Exception %s. So ignoring the attribute value", key, value, e);
                    }
                } else {
                    warn(log, "Seems no Attribute Provider for Attribute(%s), Please check the configuration...", key);
                }
            });

        } catch (UnknownNameException e) {
            e.printStackTrace();
            info(log, "Seems no Attribute Providers are present. So Attribute conversion failed..");
            return attributes;
        }
        return retVal;
    }


    @Override
    public String toString() {
        return "RegistryImpl{" +
                "registryMap=" + registryMap +
                ", attributeValidationOn=" + attributeValidationOn +
                '}';
    }
}
