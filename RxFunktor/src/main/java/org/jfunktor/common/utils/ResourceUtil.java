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

package org.jfunktor.common.utils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.*;

/**
 * Created by S719566 on 06/06/2015.
 */
public class ResourceUtil {

    public static final String servicePath = "META-INF/services/";

    private static Logger log = LoggerFactory.getLogger(ResourceUtil.class);

    /**
     * Utility to load the specified resources indicated by the path using the specified classloader
     * @param loader
     * @param path
     * @return
     */
    public static List<URL> loadResources(ClassLoader loader,String path){
        ClassLoader classLoader = loader;
        ArrayList<URL> list = new ArrayList<>();

        try {
            Enumeration<URL> resources =
                    classLoader.getResources(path);

            while(resources.hasMoreElements()){
                URL url = resources.nextElement();
                list.add(url);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }


    /**
     * List the files under the given URI
     * @param url
     * @return
     */
    public static List<String> listFiles(URL url) throws URISyntaxException, IOException {
        ArrayList<String> files = new ArrayList();
        HashMap<String, Object> env = new HashMap();
        FileSystem fileSystem = null;
        String scheme = url.toURI().getScheme();
        if(scheme.equals("jar") || scheme.equals("zip")){
            final String[] array = url.toString().split("!");
            fileSystem = FileSystems.newFileSystem(URI.create(array[0]),env);

            if(array.length>1) {
                Path path = fileSystem.getPath(array[1]);
                if(log.isDebugEnabled()){
                    log.debug("File Root path "+path);
                    Iterator<Path> iterator = path.iterator();
                    while (iterator.hasNext())log.debug(""+iterator.next());
                }

                fileSystem = path.getFileSystem();
            }
        }else{
            fileSystem = FileSystems.newFileSystem(url.toURI(),env);
        }

        for (FileStore fileStore : fileSystem.getFileStores()) {

            files.add(fileStore.name());
        }

        return files;
    }


    public static List<Class> findImplementationsOf(Class interfaceClass,ClassLoader loader) throws IOException, ClassNotFoundException {
        String resourcePath = servicePath+interfaceClass.getName();
        log.debug("Looking up resourcePath " + resourcePath);

        /*Map<String, byte[]> loadedResources = loader.getLoadedResources();



        //look up ours
        byte[] content = loadedResources.get(resourcePath);

        ByteArrayInputStream byteStream = new ByteArrayInputStream(content);

        BufferedReader reader = new BufferedReader(new InputStreamReader(byteStream));
        String classLine = reader.readLine();
        ArrayList<Class> classList = new ArrayList<>();
        while(classLine != null){

            //ask the classloader to load the class
            Class clz = loader.loadClass(classLine);

            //add the class to the list
            classList.add(clz);

            classLine = reader.readLine();
        }
        reader.close();*/

        Enumeration<URL> resources = loader.getResources(resourcePath);
        ArrayList<Class> classList = new ArrayList<>();
        while(resources.hasMoreElements()){
            URL url = resources.nextElement();

            log.debug("URL : " + url);

            List<Class> classes = loadImplementations(url,loader);

            log.debug("Class : " + classes);
            classList.addAll(classes);
        }

        return classList;
    }

    public static List<Class> loadImplementations(URL url,ClassLoader loader) throws IOException, ClassNotFoundException {
        InputStream inputStream = url.openStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String classLine = reader.readLine();
        ArrayList<Class> classList = new ArrayList<>();
        while(classLine != null){

            //ask the classloader to load the class
            Class clz = loader.loadClass(classLine);

            //add the class to the list
            classList.add(clz);

            classLine = reader.readLine();
        }
        reader.close();
        return classList;
    }
}
