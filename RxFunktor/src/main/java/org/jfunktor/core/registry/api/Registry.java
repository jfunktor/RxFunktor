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

package org.jfunktor.core.registry.api;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Predicate;


/**
 * The Registry is the abstraction of a Directory service within the JVM.
 *
 *
 * @author vj
 */
public interface Registry {

    /**
     * Registers an instance with a given set of attributes
     * @param attributes
     * @param o
     */
    void register(String name, Map attributes, Object o);

    /**
     * Register without any attributes
     * @param name
     * @param o
     */
    void register(String name, Object o);


    /**
     * The attribute version of the Class registration
     * @param type
     * @param attributes
     * @param o
     */
    void register(Class type, Map attributes, Object o);

    /**
     * Look up an instance given the expected attributes
     * IF More than one attribute all the attributes are expected to be an exact match
     * @param filter
     * @return
     */
    Object lookup(String name, Predicate<RegistryEntry> filter) throws UnknownNameException;


    /**
     * Look up an instance given the name and Type
     * @param name
     * @param type
     * @param <T>
     * @return
     * @throws UnknownNameException
     */
    <T> T lookup(String name,Class<T> type)throws UnknownNameException;

    /**
     * Looks up the given name and Type matching the given filter
     * @param name
     * @param type
     * @param filter
     * @param <T>
     * @return
     * @throws UnknownNameException
     */
    <T> T lookup(String name,Class<T> type,Predicate<RegistryEntry> filter)throws UnknownNameException;

    /**
     * Looks up all instances given the criteria
     * @param name
     * @param filter
     * @return
     * @throws UnknownNameException
     */
    List<Object> lookupAll(String name, Predicate<RegistryEntry> filter)throws UnknownNameException;


    /**
     * The lookup all instances of a given interface given the filter
     * @param type
     * @param filter
     * @return
     * @throws UnknownNameException
     */
    List<Object> lookupAll(Class type, Predicate<RegistryEntry> filter) throws UnknownNameException;


    /**
     * Looksup all instances across all Registered types matching the given criteria
     * @param filter
     * @return
     * @throws UnknownNameException
     */
    List<Object> lookupAll(Predicate<RegistryEntry> filter)throws UnknownNameException;

    /**
     * Looksup all instances across all Registered types matching the given criteria along with the sort behaviour
     * @param filter
     * @param sorter
     * @return
     * @throws UnknownNameException
     */
    List<Object> lookupAll(Predicate<RegistryEntry> filter, Comparator<RegistryEntry> sorter)throws UnknownNameException;

    /**
     * Lookup all the Entries which match a corresponding criteria
     * @param filter
     * @return
     * @throws UnknownNameException
     */
    List<RegistryEntry> lookupAllEntries(Predicate<RegistryEntry> filter)throws UnknownNameException;

    /**
     * Lookup all the Registry Entries which match a corresponding criteria and name
     * @param name
     * @param filter
     * @return
     * @throws UnknownNameException
     */
    List<RegistryEntry> lookupAllEntries(String name, Predicate<RegistryEntry> filter)throws UnknownNameException;


    /**
     * Support for iterating and doing something for each of the entry in the registry
     * What the consumer does with the entry is upto the consumer
     * @param consumer
     */
    void forEach(BiConsumer<String, List<RegistryEntry>> consumer);

    /**
     * Simpler lookup with no filters
     * @param name
     * @return
     * @throws UnknownNameException
     */
    Object lookup(String name)throws UnknownNameException;

    /**
     * Enable or Disable attribute validations
     * @param b
     */
    void validateAttributes(boolean b);
}
