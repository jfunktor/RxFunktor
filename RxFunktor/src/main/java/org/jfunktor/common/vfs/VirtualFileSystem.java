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

package org.jfunktor.common.vfs;

import org.apache.commons.vfs2.*;
import org.jfunktor.common.utils.ResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by S719566 on 16/06/2015.
 */
public class VirtualFileSystem {

    private static Logger log = LoggerFactory
            .getLogger(VirtualFileSystem.class);

    /**
     * Checks whether the given file url is valid or not
     *
     * @param location
     * @return
     */
    public static boolean exists(URL location) throws FileNotFoundException,
            VFSException {
        FileObject obj = null;
        try {
            obj = VFS.getManager().resolveFile(location.toString());
            return obj.exists();
        } catch (FileSystemException e) {
            throw new VFSException(e);
        }
    }

    private static FileObject resolveFile(URL location) {
        FileObject obj = null;
        try {
            obj = VFS.getManager().resolveFile(location.toString());
        } catch (FileSystemException e) {
            e.printStackTrace();
        }
        return obj;
    }

    /**
     * Lists the unique contents of the given search path using the given classloader
     * It searches the contenst of the ClassPath seen by the provided classloader
     * @param searchPath
     * @return
     */
    public static List<Class> listTypesFromClassPath(String searchPath,ClassLoader loader) throws FileSystemException {

        List<URL> urls = ResourceUtil.loadResources(loader, searchPath);

        if(log.isDebugEnabled()){
            log.debug(String.format("URLs %s",urls));
        }

        ArrayList<String> typeList = new ArrayList();

        for (URL url : urls) {
            for (String s : listContents(url)) {
                if(!typeList.contains(s)) {
                    if(log.isDebugEnabled()){
                        log.debug(String.format("Identified Unique Type %s",s));
                    }
                    typeList.add(s);
                }
            }

        }

        return toClassList(loader,typeList);
    }

    /**
     * Converts a list of strings to corresponding classes
     * @param loader
     * @param typeList
     * @return
     */
    private static List<Class> toClassList(ClassLoader loader,List<String> typeList) {
        ArrayList<Class> classList = new ArrayList<>();
        for (String clzName : typeList) {
            try {
                Class aClass = loader.loadClass(clzName);
                classList.add(aClass);
            } catch (ClassNotFoundException e) {
                if(log.isErrorEnabled()){
                    log.error(String.format("Unable to load class/interface %s",clzName),e);
                }
            }
        }

        return classList;
    }

    /**
     * Lists the contents of a given file location
     * It can be a URL to any location which is supported by the VFS
     * @param location
     * @return
     */
    public static List<String> listContents(URL location) throws FileSystemException {
        ArrayList<String> contents = new ArrayList<>();

        FileObject obj = VFS.getManager().resolveFile(location.toString());

        FileObject[] children = obj.getChildren();

        if(children != null && children.length > 0){
            for (int i = 0; i < children.length; i++) {
                contents.add(children[i].getName().getBaseName());
            }
        }

        return contents;
    }

    public static boolean copyContents(String fromLocation, String toLocation)
            throws FileNotFoundException, VFSException {

        FileObject fromFile = null;
        FileObject toFile = null;
        boolean retVal = false;

        try {
            URL fromLocationURL = new URL(fromLocation);

            // log.debug("fromLocationURL: " + fromLocationURL.toString());

            if (exists(fromLocationURL)) {
                // log.debug(fromLocationURL + " Exists");

                fromFile = resolveFile(fromLocationURL);
                toFile = resolveFile(new URL("file:///" + toLocation));

                // now copy the contents to the destination

                try {
                    log.info("Downloading " + fromFile);

                    FileUtil.copyContent(fromFile, toFile);

                    retVal = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else {
                log.debug("From Location " + fromLocation
                        + " does not exist. Please check if it is a valid path");
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return retVal;
    }

    public static boolean extractContentsTo(URL fromLocation, URL toLocation)
            throws FileNotFoundException, VFSException {
        FileObject fromFile = null;
        FileObject toFile = null;
        boolean retVal = false;
        if (exists(fromLocation)) {
            log.debug(fromLocation + "Exists");
            toFile = resolveFile(toLocation);
            fromFile = resolveFile(fromLocation);
            // now extract or copy the contents and children to the destination

            try {
                log.debug("About to copy from " + fromFile + " to " + toFile);
                toFile.copyFrom(fromFile, new AllFileSelector());
                retVal = true;
            } catch (FileSystemException e) {
                e.printStackTrace();
            }
        } else {
            log.debug("From Location " + fromLocation
                    + " does not exist. Please check if it is a valid path");
            retVal = false;
        }

        return retVal;
    }

    /**
     * This method extract the contents of fromPath in the classpath to the
     * destination toPath which is the location relative to the app execution
     * directory
     *
     * @param fromPath
     * @param toPath
     * @return
     */
    public static boolean extractContentsTo(String fromPath, String toPath,
                                            ClassLoader loader) throws IOException, VFSException {
        // the fromPath is assumed to be from the classpath
        // so lets use Classloader to pick it, there might be more than one

        Enumeration<URL> resources = loader.getResources(fromPath);

        File topathRelative = new File(toPath);
        URL toPathURL = topathRelative.toURI().toURL();

        boolean result = true;

        if (!resources.hasMoreElements()) {
            log.debug("Ignoring fromPath " + fromPath
                    + " since no valid resource path found within classpath");
        }
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();

            if (!extractContentsTo(resource, toPathURL)) {
                result = false;
            }
        }

        return result;
    }

    public static void printContents(String path,ClassLoader loader){
        try {
            Enumeration<URL> resources = loader.getResources(path);
            if(null != resources){
                log.debug("Source of path "+path);
                while(resources.hasMoreElements()){
                    URL resourceURL = resources.nextElement();
                    log.debug("Path "+resourceURL);
                    if(exists(resourceURL)){
                        log.debug("Listing contents of URL "+resourceURL);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (VFSException e) {
            e.printStackTrace();
        }
    }
}
