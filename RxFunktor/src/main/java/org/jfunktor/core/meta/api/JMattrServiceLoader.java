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



import org.jfunktor.common.Constants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

/**
 * The JMattrServiceLoader is the equivalent of **ServiceLoader** provided in the java platform.
 * The key difference is that the services are looked up from **META-INF/jmattr/services** instead of
 * the standard **META-INF/services** path in the jar files.
 *
 * The other difference is that the Class is not instantiated when loading instead the Class object is
 * available as part of the load() call
 *
 * @author vj
 */
public class JMattrServiceLoader implements Iterable<Class> {

    private static final String PREFIX = Constants.SERVICES_PATH;

    // The class or interface representing the service being loaded
    private Class service;

    // The class loader used to locate, load, and instantiate providers
    private ClassLoader loader;

    // Cached providers, in instantiation order
    private LinkedHashMap<String,Class> providers = new LinkedHashMap<String,Class>();

    // The current lazy-lookup iterator
    private LazyIterator lookupIterator;

    /**
     * Clear this finder's provider cache so that all providers will be
     * reloaded.
     *
     * <p> After invoking this method, subsequent invocations of the {@link
     * #iterator() iterator} method will lazily look up providers from scratch,
     * just as is done by a newly-created finder.
     *
     * <p> This method is intended for use in situations in which new providers
     * can be installed into a running Java virtual machine.
     */
    public void reload() {
        providers.clear();
        lookupIterator = new LazyIterator(service, loader);
    }

    private JMattrServiceLoader(Class svc, ClassLoader cl) {
        service = svc;
        loader = cl;
        reload();
    }


    /**
     * Lazily loads the available providers of this finder's service.
     *
     * <p> The iterator returned by this method first yields all of the
     * elements of the provider cache, in instantiation order.  It then lazily
     * loads any remaining providers, adding each one to the
     * cache in turn.
     *
     * <p> To achieve laziness the actual work of parsing the available
     * provider-configuration files  must be done by the iterator itself.
     * Its {@link Iterator#hasNext hasNext} and
     * {@link Iterator#next next} methods can therefore throw a
     * {@link ServiceConfigurationError} if a provider-configuration file
     * violates the specified format, or if it names a provider class that
     * cannot be found , or if any other kind of
     * exception or error is thrown as the next provider is located.
     * To write robust code it is only necessary to catch {@link
     * ServiceConfigurationError} when using a service iterator.
     *
     * <p> If such an error is thrown then subsequent invocations of the
     * iterator will make a best effort to locate the next available provider,
     * but in general such recovery cannot be guaranteed.
     *
     * <blockquote style="font-size: smaller; line-height: 1.2"><span
     * style="padding-right: 1em; font-weight: bold">Design Note</span>
     * Throwing an error in these cases may seem extreme.  The rationale for
     * this behavior is that a malformed provider-configuration file, like a
     * malformed class file, indicates a serious problem with the way the Java
     * virtual machine is configured or is being used.  As such it is
     * preferable to throw an error rather than try to recover or, even worse,
     * fail silently.</blockquote>
     *
     * <p> The iterator returned by this method does not support removal.
     * Invoking its {@link Iterator#remove() remove} method will
     * cause an {@link UnsupportedOperationException} to be thrown.
     *
     * @return  An iterator that lazily loads providers for this finder's
     *          service
     */
    public Iterator<Class> iterator() {
        return new Iterator<Class>() {

            Iterator<Map.Entry<String,Class>> knownProviders
                    = providers.entrySet().iterator();

            public boolean hasNext() {
                if (knownProviders.hasNext())
                    return true;
                return lookupIterator.hasNext();
            }

            public Class next() {
                if (knownProviders.hasNext())
                    return knownProviders.next().getValue();
                return lookupIterator.next();
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }

        };
    }

    /**
     * Creates a new service finder for the given service type and class
     * finder.
     *
     * @param  service
     *         The interface or abstract class representing the service
     *
     * @param  loader
     *         The class loader to be used to load provider-configuration files
     *         and provider classes, or <tt>null</tt> if the system class
     *         loader (or, failing that, the bootstrap class loader) is to be
     *         used
     *
     * @return A new service finder
     */
    public static JMattrServiceLoader load(Class service,
                                           ClassLoader loader)
    {
        return new JMattrServiceLoader(service, loader);
    }

    /**
     * Creates a new service finder for the given service type, using the
     * current thread's {@linkplain Thread#getContextClassLoader
     * context class loader}.
     *
     * <p> An invocation of this convenience method of the form
     *
     * <blockquote><pre>
     * JMattrServiceLoader.load(<i>service</i>)</pre></blockquote>
     *
     * is equivalent to
     *
     * <blockquote><pre>
     * JMattrServiceLoader.load(<i>service</i>,
     *                    Thread.currentThread().getContextClassfinder())</pre></blockquote>
     *
     * @param  service
     *         The interface or abstract class representing the service
     *
     * @return A new service finder
     */
    public static JMattrServiceLoader load(Class service) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return JMattrServiceLoader.load(service, cl);
    }

    /**
     * Creates a new service finder for the given service type, using the
     * extension class loader.
     *
     * <p> This convenience method simply locates the extension class loader,
     * call it <tt><i>extClassLoader</i></tt>, and then returns
     *
     * <blockquote><pre>
     * JMattrServiceLoader.load(<i>service</i>, <i>extClassLoader</i>)</pre></blockquote>
     *
     * <p> If the extension class loader cannot be found then the system class
     * loader is used; if there is no system class loader then the bootstrap
     * class loader is used.
     *
     * <p> This method is intended for use when only installed providers are
     * desired.  The resulting service will only find and load providers that
     * have been installed into the current Java virtual machine; providers on
     * the application's class path will be ignored.
     *
     * @param  service
     *         The interface or abstract class representing the service
     *
     * @return A new service Finder
     */
    public static JMattrServiceLoader loadInstalled(Class service) {
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        ClassLoader prev = null;
        while (cl != null) {
            prev = cl;
            cl = cl.getParent();
        }
        return JMattrServiceLoader.load(service, prev);
    }


    private static void fail(Class service, String msg, Throwable cause)
            throws ServiceConfigurationError
    {
        throw new ServiceConfigurationError(service.getName() + ": " + msg,
                cause);
    }



    private static void fail(Class service, String msg)
            throws ServiceConfigurationError
    {
        throw new ServiceConfigurationError(service.getName() + ": " + msg);
    }

    private static void fail(Class service, URL u, int line, String msg)
            throws ServiceConfigurationError
    {
        fail(service, u + ":" + line + ": " + msg);
    }


    // Parse a single line from the given configuration file, adding the name
    // on the line to the names list.
    //
    private int parseLine(Class service, URL u, BufferedReader r, int lc,
                          List<String> names)
            throws IOException, ServiceConfigurationError
    {
        String ln = r.readLine();
        if (ln == null) {
            return -1;
        }
        int ci = ln.indexOf('#');
        if (ci >= 0) ln = ln.substring(0, ci);
        ln = ln.trim();
        int n = ln.length();
        if (n != 0) {
            if ((ln.indexOf(' ') >= 0) || (ln.indexOf('\t') >= 0))
                fail(service, u, lc, "Illegal configuration-file syntax");
            int cp = ln.codePointAt(0);
            if (!Character.isJavaIdentifierStart(cp))
                fail(service, u, lc, "Illegal provider-class name: " + ln);
            for (int i = Character.charCount(cp); i < n; i += Character.charCount(cp)) {
                cp = ln.codePointAt(i);
                if (!Character.isJavaIdentifierPart(cp) && (cp != '.'))
                    fail(service, u, lc, "Illegal provider-class name: " + ln);
            }
            if (!providers.containsKey(ln) && !names.contains(ln))
                names.add(ln);
        }
        return lc + 1;
    }

    // Parse the content of the given URL as a provider-configuration file.
    //
    // @param  service
    //         The service type for which providers are being sought;
    //         used to construct error detail strings
    //
    // @param  u
    //         The URL naming the configuration file to be parsed
    //
    // @return A (possibly empty) iterator that will yield the provider-class
    //         names in the given configuration file that are not yet members
    //         of the returned set
    //
    // @throws ServiceConfigurationError
    //         If an I/O error occurs while reading from the given URL, or
    //         if a configuration-file format error is detected
    //
    private Iterator<String> parse(Class service, URL u)
            throws ServiceConfigurationError
    {
        InputStream in = null;
        BufferedReader r = null;
        ArrayList<String> names = new ArrayList<String>();
        try {
            in = u.openStream();
            r = new BufferedReader(new InputStreamReader(in, "utf-8"));
            int lc = 1;
            while ((lc = parseLine(service, u, r, lc, names)) >= 0);
        } catch (IOException x) {
            fail(service, "Error reading configuration file", x);
        } finally {
            try {
                if (r != null) r.close();
                if (in != null) in.close();
            } catch (IOException y) {
                fail(service, "Error closing configuration file", y);
            }
        }
        return names.iterator();
    }

    private class LazyIterator
            implements Iterator<Class>
    {

        Class service;
        ClassLoader loader;
        Enumeration<URL> configs = null;
        Iterator<String> pending = null;
        String nextName = null;

        private LazyIterator(Class service, ClassLoader loader) {
            this.service = service;
            this.loader = loader;
        }


        public boolean hasNext() {
            if (nextName != null) {
                return true;
            }
            if (configs == null) {
                try {
                    String fullName = PREFIX + service.getName();
                    if (loader == null)
                        configs = ClassLoader.getSystemResources(fullName);
                    else
                        configs = loader.getResources(fullName);
                } catch (IOException x) {
                    fail(service, "Error locating configuration files", x);
                }
            }
            while ((pending == null) || !pending.hasNext()) {
                if (!configs.hasMoreElements()) {
                    return false;
                }
                pending = parse(service, configs.nextElement());
            }
            nextName = pending.next();
            return true;
        }

        public Class next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            String cn = nextName;
            nextName = null;
            try {
                Class p = (Class)Class.forName(cn, true, loader);

                providers.put(cn, p);
                return p;
            } catch (ClassNotFoundException x) {
                fail(service,
                        "Provider " + cn + " not found");
            }
            throw new Error();          // This cannot happen
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

    }

    /**
     * Returns a string describing this service.
     *
     * @return  A descriptive string
     */
    public String toString() {
        return "java.util.JMattrServiceLoader[" + service.getName() + "]";
    }

}
