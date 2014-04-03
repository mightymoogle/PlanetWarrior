/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Simulator;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 *
 * @author Mighty
 */
public class Tournament extends Thread {    
    private String path;
    private String bot1;
    public String bot2;     
    
    //Lazy, sorry for Public
    public volatile int wins = 0;
    public volatile int losses = 0;
    public volatile int draws = 0;
    
        
    public int MAP_START = 7; //Starting map
    public int MAP_END = 8; //Last map
    public int MAP_JUMPER = 1; //Allows to use only each Nth map
    
    public Tournament(String p, String b1, String b2) {
        path = p;
        bot1 = b1;
        bot2 = b2;        
    }
    
    public void run() {
        for (int i = MAP_START; i <= MAP_END; i=i+MAP_JUMPER) {

            try {

                String fuckingIdiots = "java -jar " + path + "tools\\PlayGame.jar "
                        + path + "maps\\map" + i + ".txt"
                        + " 1000 1000 log.txt "
                        + "\"java -jar " + path + bot1
                        + "\"java -jar " + path + bot2;


                //System.out.println(fuckingIdiots);
                Process process = Runtime.getRuntime().exec(fuckingIdiots);

                InputStream is = process.getErrorStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);

                InputStream is2 = process.getInputStream();
                InputStreamReader isr2 = new InputStreamReader(is);
                BufferedReader br2 = new BufferedReader(isr);

                String line = "";

                while (line!=null && !line.contains("Wins") && !line.contains("Draw")) {                    
//                    System.out.println("["+i+"%] "+line);
                    line = br.readLine();
                }
                
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
