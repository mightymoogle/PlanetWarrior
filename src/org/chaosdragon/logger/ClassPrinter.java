/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chaosdragon.logger;

import java.lang.reflect.Field;

/**
 *
 * @author Mighty
 */
public class ClassPrinter {
    
    public static void printClassInfo(Object o)  {
        
     try {
       for (Field field: o.getClass().getDeclaredFields()) {
           field.setAccessible(true);
           String name = field.getName();
           Object value = field.get(o);
           Logger.INSTANCE.logLine(""+name+"="+value);                     
       }
        } catch (Exception e) {}
        
    }
    
}
