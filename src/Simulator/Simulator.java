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

    public static double simulate(String subject) {
         long startTime = System.currentTimeMillis();
        
        String path = "h:\\Source\\JAVA\\PlanetWarrior\\src\\";
        String botPath ="example_bots\\" ;
                
        //String subject = "example_bots\\PlanetWarrior.jar *_+_0_+_0_1_0\" ";
                
        ArrayList<String> bots = new ArrayList<>();
        
       // bots.add("BullyBot");
       // bots.add("DualBot");
       // bots.add("ProspectorBot");
        bots.add("RandomBot");
        bots.add("RageBot");
        
        ArrayList<Tournament> threads = new ArrayList<>();
        
        System.out.println("CONTESTANT: "+subject);
        System.out.println();
        
        for (String bot: bots) {
        
        Tournament t = new Tournament(path,subject,botPath+bot+".jar\"");
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
            System.out.println(String.format("%32s | %3d %3d %3d ", t.bot2, t.wins, t.losses, t.draws));           
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
      System.out.println(String.format("%32s | %3d %3d %3d ", "TOTAL:",totalWin, totalLose, totalDraw));           
        System.out.println(String.format("%32s | %3.0f %3.0f %3.0f ", "TOTAL (percentage):",win, lose, draw));           
        
        
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
        return simulate(base+sufix+"\" "); //Space very important!        
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
        
        //sufix = "*_+_0_+_0_1_0";        
        
        //sufix = "+_1_*_0_-_2_2"; //40%
        
        Double win=0.0;
        
        while (win<90) {
        sufix = gen.generate().toString();        
        win = simulate(base+sufix+"\" "); //Space very important!
        }
       
    }
}