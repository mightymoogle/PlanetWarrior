package org.chaosdragon.geneticwarrior.simulator;

import org.chaosdragon.geneticwarrior.trees.BinaryTree;
import org.chaosdragon.geneticwarrior.trees.Generator;
import org.chaosdragon.geneticwarrior.trees.ValueMap;
import java.io.*;
import java.util.*;
import org.chaosdragon.logger.Logger;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Mighty
 */
public class Simulator {

    public String path;
    public String botPath;
    
    //private Tournament regularTournament;
    //private Tournament randomTournament;
    
    
    public Simulator() {
        path = "h:\\Source\\JAVA\\PlanetWarrior\\src\\";         
        botPath ="example_bots\\" ;  
    }
    
    
    
    public double simulate(String subject, ArrayList<String> bots,boolean output, Tournament inputTourney) {
         long startTime = System.currentTimeMillis();        
                         
        ArrayList<Tournament> threads = new ArrayList<>();
        
        if (output) Logger.INSTANCE.logLine("CONTESTANT: "+subject);
        
        for (String bot: bots) {
                    
        Tournament t = new Tournament(inputTourney);
        t.bot1= subject;
        t.bot2 = bot;
        t.path = path;
            
        //Tournament t = new Tournament(path,subject,bot);
        
        
        
        t.run(); //Multhithreading breaks 2 second limit =(
        threads.add(t);
        
        }    
        
        try {
        
//        for (Thread t: threads) {
//            t.join();
//        }
         
        int totalWin = 0;
        int totalLose = 0;
        int totalDraw = 0;
        int ammount = 0;
        
        for (Tournament t: threads) {
        if (output) Logger.INSTANCE.logLine(String.format("%3d %3d %3d | %s ", t.wins, t.losses, t.draws,t.bot2));           
            totalWin += t.wins;
            totalLose += t.losses;
            totalDraw += t.draws;    
            ammount += t.wins+t.losses+t.draws;
         }
//        
      double win = ((double)totalWin)/ammount*100;
      double lose = ((double)totalLose)/ammount*100;
      double draw = ((double)totalDraw)/ammount*100;
      
        if (output) {    Logger.INSTANCE.logLine("----------------------------------------------------");  
      Logger.INSTANCE.logLine(String.format("%3d %3d %3d | %s", totalWin, totalLose, totalDraw,"TOTAL"));           
        Logger.INSTANCE.logLine(String.format("%3.0f %3.0f %3.0f | %s", win, lose, draw,"TOTAL (percentage)"));                
        
      long stopTime = System.currentTimeMillis();
      long elapsedTime = stopTime - startTime;
      Logger.INSTANCE.logLine("");
      Logger.INSTANCE.logLine("EXECUTION TIME: "+ elapsedTime);
        }
      return win;
      
        } catch (Exception e) {
            e.printStackTrace();
        }
         
        
      return -1.0;
        
    }
    

    
    
    
    public double simulateFromSuffix(String sufix) {
        String base = "example_bots\\";
        ArrayList<String> bots = new ArrayList<>();
        
        bots.add(base+"BullyBot.jar");
        bots.add(base+"DualBot.jar");
        bots.add(base+"ProspectorBot.jar");
        bots.add(base+"RandomBot.jar");
        bots.add(base+"RageBot.jar");        
        bots.add(base+"396.jar");
        
        base = "example_bots\\PlanetWarrior.jar ";
        
        Tournament t = new Tournament();
        
        return simulate(base+sufix, bots,true,t); //Space very important!        
    }    
    
    //Play vs Random on the first 3 maps
       public double simulateVsRandom(String sufix) {
                       
           Tournament t = new Tournament();                            
           t.MAP_JUMPER = 1;
           t.MAP_START = 1;
           t.MAP_END = 3;           
           
        String base = "example_bots\\";
        ArrayList<String> bots = new ArrayList<>();              
        bots.add(base+"RandomBot.jar");
        base = "example_bots\\PlanetWarrior.jar ";        
        double result = simulate(base+sufix, bots,false,t); //Space very important!              
                
        return   result;
    }
    
    
    public double simulateFromSuffix(String sufix, ArrayList<String> bots) {
        String base = "example_bots\\PlanetWarrior.jar ";
        
        for (int i=0; i<bots.size(); i++)  {          
            bots.set(i, base+ bots.get(i)+" ");                  
        }
        
        
        return simulate(base+sufix+"\" ", bots,true,new Tournament()); //Space very important!                     
    }
    
      
    
    
    public static void main(String[] args) {
             
        String base = "example_bots\\PlanetWarrior.jar ";
                
        Generator gen = new Generator();        
        ValueMap map = new ValueMap();        
        map.actions = new String[6];
        map.actions[0] = "*";
        map.actions[1] = "+";
        map.actions[2] = "-";
        map.actions[3] = "%";        
        map.actions[4] = "min";  
        map.actions[5] = "max";  
        map.parameters = new Double[21];     

        gen.map = map;
        
        String sufix;
        
        sufix = "-_1_4";        
        
        //sufix = "+_1_*_0_-_2_2"; //40%
        
        Double win=0.0;
        
        //while (win<90) {
        //sufix = gen.generate().toString();     
        //sufix = " *_2_-_1_-_-_4_-_1_0_0";   
        //sufix ="+_+_*_-_-_0_3_-_4_1_%_1_0_*_*_2_1_1_1";               
        
        sufix = "-_%_-_max_1_12_+_18_6_+_+_min_min_9_8_*_8_4_*_2_%_10_1_7_%_0_9";
        
        BinaryTree t = gen.loadFromString(sufix);
        Logger.INSTANCE.logLine(t.toString());
        
        Simulator s = new Simulator();

        
        //win = simulate(base+sufix+"\" "); //Space very important!
        //win = simulateFromSuffix(sufix);
        //}
       
    }
}
