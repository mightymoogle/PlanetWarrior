/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chaosdragon.geneticwarrior.simulator;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Random;
import org.chaosdragon.logger.Logger;

/**
 *
 * @author Mighty
 */

//No need to extend Thread...
public class Tournament extends Thread {    
    public String path;
    public String bot1;
    public String bot2;     
    
    //Lazy, sorry for Public
    public volatile int wins = 0;
    public volatile int losses = 0;
    public volatile int draws = 0;    
        
    public int MAP_START = 1; //Starting map
    public int MAP_END = 1; //Last map
    public int MAP_JUMPER = 1; //Allows to use only each Nth map
    public boolean RANDOM_MAPS = false;
    
    
    
    public Tournament(String p, String b1, String b2) {
        path = p;
        bot1 = b1;
        bot2 = b2;        
    }
    
    
    //Empty constructor
    public Tournament() {
        
    }
    
    
    public Tournament(Tournament old) {
        
        this.path = old.path;
        this.bot1 = old.bot1;
        this.bot2 = old.bot2;
        
    }
    
    
    //Wanted it to be a process, but they tend to not make it 
    public void run() {
        for (int i = MAP_START; i <= MAP_END; i=i+MAP_JUMPER) {

            try {
                int temp=i;
                               
                //If random maps is on, randomize the map
                if (RANDOM_MAPS) {
                    Random r = new Random();                    
                    i=r.nextInt(100)+1;
//                            MAP_END-MAP_START+1)
//                            +MAP_START;
                }                
                
                String botLine = "java -jar " + path + "tools\\PlayGame.jar "
                        + path + "maps\\map" + i + ".txt"
                        + " 1000 1000 log.txt "
                        + "\"java -jar " + path + bot1 + "\""
                        + " \"java -jar " + path + bot2 +"\"";               
                
                
                //Now put back the i as it was if not randomized
                if (RANDOM_MAPS) {
                    i = temp;
                }
                               
                //System.out.println(botLine);                              
                
                Process process = Runtime.getRuntime().exec(botLine);

                InputStream is = process.getErrorStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);

                InputStream is2 = process.getInputStream();
                InputStreamReader isr2 = new InputStreamReader(is2);
                BufferedReader br2 = new BufferedReader(isr2);

                String line = "";

                while (line!=null && !line.contains("Wins") && !line.contains("Draw")) {                    
                   // System.out.println("["+i+"%] "+line);
                   if (line.contains("timed")) System.out.println(line);
                    line = br.readLine();
                }
                
                if (line==null) Logger.INSTANCE.logLine("WARNING:"+"nothing outputed");
                if (line.contains("Draw")) draws++;
                if (line.contains("1 Wins")) wins++;
                if (line.contains("2 Wins")) losses++;                
                
          //      System.out.println("["+i+"%] "+line);
                
                process.destroy();            


            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        
        //System.out.println(String.format("%32s | %3d %3d %3d ", bot2, wins, losses, draws));
        
    }
    
}
