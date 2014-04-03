/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Simulator;

import SimulatorTrees.BinaryTree;
import SimulatorTrees.Generator;
import SimulatorTrees.ValueMap;

/**
 *
 * @author Mighty
 */
public class GeneticMadness {
    
    private static int populationSize = 10;
    private static int numberOfIterations =10;
    private static double crossPropability = 1;
    private static double mutationPropability = 0.01;
    private static int numberOfElites = 0;
    private static Generator gen;
    
    
    private static void startUp() {
        gen = new Generator();        
        ValueMap map = new ValueMap();        
        map.actions = new String[4];
        map.actions[0] = "*";
        map.actions[1] = "+";
        map.actions[2] = "-";
        map.actions[3] = "%";        
        map.parameters = new Double[5];   //Mmm...
        gen.map = map;
    }
    
    
    private static BinaryTree[] generateStartingPopulation() {
        
        BinaryTree[] population= new BinaryTree[populationSize];
        
        for (int i=0; i<populationSize; i++) {
            population[i] = gen.generate();
        }
        
        return population;        
        
    }
    
    public static double evaluate(BinaryTree t) {            
         return Simulator.simulateFromSuffix(t.toString());
    }
    

    public static BinaryTree crossStich(BinaryTree father, BinaryTree mother) {        
        
        return null;              
    }
    
    public static BinaryTree mutate(BinaryTree victim) {
        
        return null;
    }
    
    public static BinaryTree[] cloneElites(BinaryTree[] population) { //Will probably need a list with evaluations...
        
        return null;
    }    
    
    //Get a random one from population, based on it's evaluation
    public static BinaryTree gamble(BinaryTree[] population) { //Mighty need a list
        
        return null;
    }
    
    public static BinaryTree[] evolve(BinaryTree[] population) {
        
        //random num...
        
        //create new empty population        
        //copy elites        
        //fill other slots with new ones
            //gamble
            //gamble
            //crossstich
            //mutate
        
        //...Evaluate
        //Evolve...
        
        //Make the simulator grow if the percentage rises?
        
        return null;
    }
    
    
    public static void main(String[] args) {
        
        startUp();        
        BinaryTree[] population = generateStartingPopulation();
        
        for (BinaryTree t:population) {
            System.out.println(t.toString());
            System.out.println(""+evaluate(t));
        }
        
    }    
    
}
