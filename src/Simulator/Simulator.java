package Simulator;

import SimulatorTrees.Generator;
import SimulatorTrees.ValueMap;
import java.io.*;
import java.util.*;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Mighty
 */
public class Simulator {

    public static double simulate(String subject, ArrayList<String> bots) {
         long startTime = System.currentTimeMillis();
        
        String path = "h:\\Source\\JAVA\\PlanetWarrior\\src\\";
        String botPath ="example_bots\\" ;
                
        //String subject = "example_bots\\PlanetWarrior.jar *_+_0_+_0_1_0\" ";
                
        //ArrayList<String> bots = new ArrayList<>();
        
//        bots.add("BullyBot");
//        bots.add("DualBot");
//        bots.add("ProspectorBot");
//        bots.add("RandomBot");
//        bots.add("RageBot");
//        
        ArrayList<Tournament> threads = new ArrayList<>();
        
        System.out.println("CONTESTANT: "+subject);
        //System.out.println();
        
        for (String bot: bots) {
        
        //Tournament t = new Tournament(path,subject,botPath+bot+".jar\"");
        Tournament t = new Tournament(path,subject,bot);
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
            System.out.println(String.format("%3d %3d %3d | %s ", t.wins, t.losses, t.draws,t.bot2));           
            totalWin += t.wins;
            totalLose += t.losses;
            totalDraw += t.draws;    
            ammount += t.wins+t.losses+t.draws;
         }
//        
      double win = ((double)totalWin)/ammount*100;
      double lose = ((double)totalLose)/ammount*100;
      double draw = ((double)totalDraw)/ammount*100;
      
            System.out.println("----------------------------------------------------");  
      System.out.println(String.format("%3d %3d %3d | %s", totalWin, totalLose, totalDraw,"TOTAL"));           
        System.out.println(String.format("%3.0f %3.0f %3.0f | %s", win, lose, draw,"TOTAL (percentage)"));           
        
        
      long stopTime = System.currentTimeMillis();
      long elapsedTime = stopTime - startTime;
      System.out.println();
      System.out.println("EXECUTION TIME: "+ elapsedTime);
      
      return win;
      
        } catch (Exception e) {
            e.printStackTrace();
        }
         
        
      return -1.0;
        
    }
    
    public static double simulateFromSuffix(String sufix) {
        String base = "example_bots\\PlanetWarrior.jar ";
          ArrayList<String> bots = new ArrayList<>();
        
        bots.add(base+"BullyBot.jar");
        bots.add(base+"DualBot.jar");
        bots.add(base+"ProspectorBot.jar");
        bots.add(base+"RandomBot.jar");
        bots.add(base+"RageBot.jar");        
        
        return simulate(base+sufix+"\" ", bots); //Space very important!        
    }
    
    public static double simulateFromSuffix(String sufix, ArrayList<String> bots) {
        String base = "example_bots\\PlanetWarrior.jar ";
        
        for (int i=0; i<bots.size(); i++)  {          
            bots.set(i, base+ bots.get(i)+" ");                  
        }
        
        return simulate(base+sufix+"\" ", bots); //Space very important!                     
    }
    
    
    public static void main(String[] args) {
             
        String base = "example_bots\\PlanetWarrior.jar ";
                
        Generator gen = new Generator();        
        ValueMap map = new ValueMap();        
        map.actions = new String[4];
        map.actions[0] = "*";
        map.actions[1] = "+";
        map.actions[2] = "-";
        map.actions[3] = "%";        
        map.parameters = new Double[5];     

        gen.map = map;
        
        String sufix;
        
        //sufix = "-_1_4";        
        
        //sufix = "+_1_*_0_-_2_2"; //40%
        
        Double win=0.0;
        
        //while (win<90) {
        sufix = gen.generate().toString();     
        //sufix = " *_2_-_1_-_-_4_-_1_0_0";   
        sufix ="+_+_*_-_-_0_3_-_4_1_%_1_0_*_*_2_1_1_1";
        //win = simulate(base+sufix+"\" "); //Space very important!
        win = simulateFromSuffix(sufix);
        //}
       
    }
}
