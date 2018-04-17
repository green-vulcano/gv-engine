/*******************************************************************************
 * Copyright (c) 2009, 2016 GreenVulcano ESB Open Source Project.
 * All rights reserved.
 *
 * This file is part of GreenVulcano ESB.
 *
 * GreenVulcano ESB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GreenVulcano ESB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package it.greenvulcano.util.clazz;

import java.lang.reflect.Array;
import java.net.URL;
import java.util.Enumeration;

/**
 * @version 3.2.0 31/gen/2012
 * @author GreenVulcano Developer Team
 */
public class ClassUtils
{

    /**
     * This method return java representation of simple and complex types (es.
     * mono or multi-dimensional array)
     * 
     * @param classStr
     *        String
     * @return Class
     *         real class
     * @throws Exception
     *         when an exception occurs
     */
    public static Class<?> getRealClass(String classStr) throws ClassUtilsException
    {
        String className = classStr;
        int dimensions = 0;
        int idx = className.indexOf("[");
        while (idx != -1) {
            dimensions++;
            idx = className.indexOf("[", idx + 1);
        }
        if (dimensions > 0) {
            className = className.substring(0, className.indexOf("["));
        }
        Class<?> clazz = null;
        if (className.equals(Boolean.TYPE.getName())) {
            clazz = Boolean.TYPE;
        }
        else if (className.equals(Byte.TYPE.getName())) {
            clazz = Byte.TYPE;
        }
        else if (className.equals(Character.TYPE.getName())) {
            clazz = Character.TYPE;
        }
        else if (className.equals(Double.TYPE.getName())) {
            clazz = Double.TYPE;
        }
        else if (className.equals(Float.TYPE.getName())) {
            clazz = Float.TYPE;
        }
        else if (className.equals(Integer.TYPE.getName())) {
            clazz = Integer.TYPE;
        }
        else if (className.equals(Long.TYPE.getName())) {
            clazz = Long.TYPE;
        }
        else if (className.equals(Short.TYPE.getName())) {
            clazz = Short.TYPE;
        }
        else {
            try {
                clazz = Class.forName(className);
            }
            catch (ClassNotFoundException exc) {
                throw new ClassUtilsException("Error instantiating class : " + classStr);
            }
        }
        if (dimensions > 0) {
            int dimensionsA[] = new int[dimensions];
            for (int i = 0; i < dimensions; i++) {
                dimensionsA[i] = 0;
            }
            Object obj = Array.newInstance(clazz, dimensionsA);
            return obj.getClass();
        }
        return clazz;
    }

    public static String findResourceAsString(String resource) throws ClassUtilsException
    {
        try {
            String urls = "";
            ClassLoader classClassLoader = ClassUtils.class.getClassLoader();
            urls = "From class's ClassLoader:\n";
            urls += findResource(resource, classClassLoader);

            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            if((contextClassLoader != null) && (classClassLoader != contextClassLoader)) {
                urls += "\n\nFrom context ClassLoader:\n";
                urls += findResource(resource, contextClassLoader);
            }

            return urls;
        }
        catch (Exception exc) {
            throw new ClassUtilsException("Error finding resource: " + resource, exc);
        }
    }

    private static String findResource(String resource, ClassLoader classLoader) throws Exception
    {
        String urls = "";

        URL url = classLoader.getResource(resource);
        if(url == null) {
            urls = "\t" + resource + " not found\n";
        }
        else {
            urls = "\t" + url.toString() + "\n";
        }
        Enumeration<?> en = classLoader.getResources(resource);
        while(en.hasMoreElements()) {
            urls += "\t" + en.nextElement().toString() + "\n";
        }

        return urls;
    }

}
