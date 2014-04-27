/*
 * DAVID GRIBERMAN 2014, RIGA TECHNICAL UNIVERSITY
 */
package org.chaosdragon.logger;

//Responsible for printing to file etc

import java.io.*;

/**
 *
 * @author Mighty
 */
public enum Logger {
   INSTANCE;
    
   private PrintWriter writer;      
   public boolean isOpen=false; 
   public boolean printable=true;
   public String lastFile;
   
   
    public void open(String filename) {
        if (isOpen)  return;
        try {
                            
                try {
                    //True - append
                    writer = new PrintWriter(new FileOutputStream(new File(filename),true));                    
                    isOpen= true;
                    lastFile = filename;
                    
                } catch (Exception e) {
                }
         
            
        } catch (Exception e) {
            e.printStackTrace();
            isOpen = false;
        }       
    }
    
    public void reOpen() {
        if (lastFile==null) return;
        
        if (isOpen) close();
        open(lastFile);
        
    }
    
    public void logLine(String s) {
        if (!isOpen)  return;
        writer.println(s);
        if (printable) {
            System.out.println(s);
        }
    }
    
    public void log(String s) {
        if (!isOpen)  return;
        writer.print(s);
        if (printable) {
            System.out.print(s);
        }
    }
    
    public void close() {
        if (!isOpen)  return;
        writer.close();
        isOpen = false;
    }
    
}
