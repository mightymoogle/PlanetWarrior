/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chaosdragon.geneticwarrior.simulator;

import org.chaosdragon.geneticwarrior.trees.BinaryTree;
import org.chaosdragon.geneticwarrior.trees.Generator;
import org.chaosdragon.geneticwarrior.trees.Node;
import org.chaosdragon.geneticwarrior.trees.Terminal;
import org.chaosdragon.geneticwarrior.trees.ValueMap;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;
import org.chaosdragon.logger.ClassPrinter;
import org.chaosdragon.logger.Logger;

/**
 *
 * @author Mighty
 */
public class GeneticMadness {

    //Size of the population, larger the better, the slower
    public int populationSize = 10;
    //How many iterations to make, same as above...
    public int numberOfIterations = 10;
    //Chance to have children, if no children, returns both parents
    public double crossPropability = 1;
    //Chance that a mutation will happen
    public double mutationPropability = 0.10;
    //Chance that a complex mutation will hapen.
    //Regular = 1-P
    public double complexMutationProbapiblity = 0.3;
    //The thing that generates everything
    private Generator gen;
    //The thing that simulates everything
    private Simulator sim;    
    //This holds the global maximum
    private BinaryTree globalMaximum;
    //IF false - checks vs other bots, if true, checks from population
    public boolean evaluateInSandBox = false;
    //Play vs yourself in evaluation if sandboxed
    public boolean mirrorInSandbox = false;
    //Check till generated vs random first, BAD IDEA
    public boolean healthyStartingPopulation = false;
    //How many %% need to win to start the checking
    public int neededWinPercentageVsRandomToAdvance = 0;
    //Check vs random in evaluation (allows to generate healthy without checking all the time)
    public boolean checkVsRandom = false;    
    //determines the selection type
    private ArrayList<BinaryTree> population;

    public enum selectionTypes {

        ROULETE_WHEEL, TOURNAMENT
    };
    //The selection type
    private static selectionTypes selectionType = selectionTypes.TOURNAMENT;
    //public selectionTypes selectionType = selectionTypes.ROULETE_WHEEL;
    //Determines the size of the tournament
    public int tournamentSize = 1;
    /**
     * *** ROULETE WHEEL SECION
     * ****************************************************
     */
    //How many top trees we copy to the next population
    public int numberOfElites = 1; //size - elites must be pair, or one will be lost and probably crash???       
     /* *** ROULETE WHEEL SECION ****************************************************
     */

    private GeneticMadness() {
        startUp();
    }
    
    
    private void startUp() {
        gen = new Generator();
        sim = new Simulator();
        
        ValueMap map = new ValueMap();
        map.actions = new String[6];
        map.actions[0] = "*";
        map.actions[1] = "+";
        map.actions[2] = "-";
        map.actions[3] = "%";
        map.actions[4] = "min";
        map.actions[5] = "max";
        map.parameters = new Double[21];   //Mmm... The number to which we generate?
        gen.map = map;
    }

    //Replace to a file later?
    public static void log(String input) {
        Logger.INSTANCE.logLine(input);
    }

    //Randomly generates the starting population
    private ArrayList<BinaryTree> generateStartingPopulation() {

        log("***GENERATING STARTING POPULATION***");
        ArrayList<BinaryTree> population = new ArrayList<>();

        if (healthyStartingPopulation) {

            for (int i = 0; i < populationSize; i++) {
                double r = 0;
                while (r < neededWinPercentageVsRandomToAdvance) {

                    BinaryTree baby;
                    baby = gen.generate();
                    r = sim.simulateVsRandom(baby.toString());


                    if (r > neededWinPercentageVsRandomToAdvance) {
                        population.add(baby);
                        log(((double)(i+1)/populationSize*100)+"% Done");
                    } else {
                        log("Dropped starting member " + baby.toString() + ", lost to Random ("
                                + r + ", need:" + neededWinPercentageVsRandomToAdvance + ")");

                    }

                }
            }

        } else {

            //Just generate random ones
            for (int i = 0; i < populationSize; i++) {
                population.add(gen.generate());
            }

        }
        Logger.INSTANCE.reOpen();
        return population;

    }

    //Load precalculated starting elements, generate missing ones
    //File must be ANSI or UTF-8 without BOM!!!!
    private ArrayList<BinaryTree> generateStartingPopulation(String filename) {

        log("***LOADING STARTING POPULATION FROM FILE:"+filename+"***");
        ArrayList<BinaryTree> population = new ArrayList<>();
        int i=0;
        try {
        
        
        BufferedReader br = new BufferedReader(new FileReader(filename));
        
//        BufferedReader br = new BufferedReader(
//		   new InputStreamReader(
//                      new FileInputStream(filename), "UTF8"));
        
        
        String line;        
        while ((line = br.readLine()) != null) {           
           BinaryTree t = gen.loadFromString(line);                     
           population.add(t);           
           i++;
        }        
                
        br.close(); 
        
        if (i<populationSize) {            
            log("Filling " +(populationSize-i)+ " missing individuals with new ones");
              for (int j = i; j < populationSize; j++) {                
                  population.add(gen.generate());                  
            }            
        }
        
        }   
        catch (Exception e) {
        log("***FAILED TO LOAD FROM FILE***");
        }
        Logger.INSTANCE.reOpen();
        return population; 
    }
    
    //Calls the upper function
    public void loadPopulation(String path) {
        population = generateStartingPopulation(path);
    }
    
    
    //Evaluates a single node, setting the maximum and it's evaluation parameter.
    //Also returns the evaluation
    public double evaluate(BinaryTree t) {


        //Check vs Random bot, if it fails vs him, no need to check vs others
        if (checkVsRandom) {
            Double r = sim.simulateVsRandom(t.toString());
            if (r < neededWinPercentageVsRandomToAdvance) {
                log(t.toString() + " lost vs Random, not checking further ("
                        + r + ", need:" + neededWinPercentageVsRandomToAdvance + ")");
                return 0.0;
            }
        }

        t.evaluation = sim.simulateFromSuffix(t.toString());

        if (globalMaximum == null) {
            globalMaximum = t;
        }

        if (t.evaluation > globalMaximum.evaluation) {
            globalMaximum = t;
        }
        
        return t.evaluation;
    }

    //Probably will change to the tournament selection
    public double evaluateSandboxed(BinaryTree t, ArrayList<BinaryTree> population) {

        ArrayList<String> prefixes = new ArrayList<>();

        for (BinaryTree c : population) {
            prefixes.add(c.toString());
        }
        if (!mirrorInSandbox) {
            prefixes.remove(t.toString());
        }
        t.evaluation = sim.simulateFromSuffix(t.toString(), prefixes);


        if (globalMaximum == null) {
            globalMaximum = t;
        }

        //>=, becouse newere one is more accurate, at least it should be
        if (t.evaluation >= globalMaximum.evaluation) {
            globalMaximum = t;
        }
        return t.evaluation;
    }

    //Gets a random node from a tree, not used....
//    public static Node getRandomNode(BinaryTree victim) {
//
//        ArrayList<Node> nodes = new ArrayList<>();
//        victim.toNodeArray(nodes);
//
//        Random randnum = new Random();
//
//        return nodes.get(randnum.nextInt(nodes.size()));
//    }
    public int stichHelper(int fatherStart, String[] fatherElements) {

        int foundTerminals = 0;
        int needTerminals = 1;
        int fatherEnd = -1;

        //If is a binary tree, need to find the end position = 2 numbers in a row
        for (int i = fatherStart; i < fatherElements.length; i++) {
            if (gen.isAction(fatherElements[i])) {
                needTerminals++;
            } else {
                foundTerminals++;
            }

            if (foundTerminals >= needTerminals) {
                fatherEnd = i;
                i = fatherElements.length;
            }
        }

        return fatherEnd;
    }

    //Stich together two trees (with strings), helper for crossStich function
    public String stich(int fatherStart, int fatherEnd,
            int motherStart, int motherEnd,
            String[] fatherElements, String[] motherElements) {
        //The first child
        StringBuilder st = new StringBuilder();
        for (int i = 0; i < fatherStart; i++) {
            st.append(fatherElements[i]);
            st.append("_");
        }
        for (int i = motherStart; i <= motherEnd; i++) {
            st.append(motherElements[i]);
            st.append("_");
        }
        for (int i = fatherEnd + 1; i < fatherElements.length; i++) {
            st.append(fatherElements[i]);
            st.append("_");
        }
        st.setLength(st.length() - 1);

        return st.toString();
    }

    //Stiches together two children and puts them to result
    //TODO: MAXIMUM LENGTH
    public void crossStich(BinaryTree father, BinaryTree mother, ArrayList<BinaryTree> result) {
        Random randnum = new Random();
        //If not going to cross, just return them to result
        if (crossPropability < 1) {

            Double gamble = randnum.nextDouble(); //0..1

            if (gamble >= crossPropability) {
                result.add(father);
                result.add(mother);
            }
        }

        String[] fatherElements = father.toString().split("_");
        String[] motherElements = mother.toString().split("_");

        //Find the starting index of the replacement, skip first element
        int fatherStart = randnum.nextInt(fatherElements.length - 1) + 1;
        int motherStart = randnum.nextInt(motherElements.length - 1) + 1;

        //  fatherStart = 1;
        //  motherStart = 1;

        int fatherEnd = stichHelper(fatherStart, fatherElements);
        int motherEnd = stichHelper(motherStart, motherElements);

        //The first child
        String firstChild = stich(fatherStart, fatherEnd, motherStart, motherEnd, fatherElements, motherElements);
        log("CREATED CHILD:" + firstChild + " (PARENTS=" + father.toString() + " && " + mother.toString() + ")");

        BinaryTree firstChildTree = gen.loadFromString(firstChild);
        //Try mutating, will return itself if fails
        firstChildTree = mutate(firstChildTree);
        result.add(firstChildTree);

        //The second child
        String secondChild = stich(motherStart, motherEnd, fatherStart, fatherEnd, motherElements, fatherElements);
        log("CREATED CHILD:" + secondChild + " (PARENTS=" + father.toString() + " && " + mother.toString() + ")");

        BinaryTree secondChildTree = gen.loadFromString(secondChild);
        //Try mutating, will return itself if fails
        secondChildTree = mutate(secondChildTree);
        result.add(secondChildTree);

    }

    //Makes a mutation on a single node, either changes an action or a parameter
    public BinaryTree mutateSingleNode(BinaryTree victim) {
        String[] elements = victim.toString().split("_");
        Random randnum = new Random();
        //Allow the first to get changed
        int unlucky = randnum.nextInt(elements.length);
        //If it is an action, mutate it
        if (gen.isAction(elements[unlucky])) {
            elements[unlucky] = String.valueOf(gen.map.actions[randnum.nextInt(gen.map.actions.length)]);
        } else {
            elements[unlucky] = String.valueOf(randnum.nextInt(gen.map.parameters.length));
        }

        StringBuilder st = new StringBuilder();
        for (int i = 0; i < elements.length; i++) {
            st.append(elements[i]).append("_");
        }
        st.setLength(st.length() - 1);

        log("MUTATED " + victim.toString() + " INTO" + st.toString());
        return gen.loadFromString(st.toString());
    }

    //Generates a subtree and stiches it instead of a random subtree
    //TODO: MAXIMUM LENGTH
    public BinaryTree complexMutation(BinaryTree victim) {

        String[] elements = victim.toString().split("_");
        Random randnum = new Random();

        //The ugly mutant tree
        BinaryTree mutantTree = gen.generate();
        String[] mutantElements = mutantTree.toString().split("_");

        //Find the place we mutate
        //Do not allow first node to change
        int start = randnum.nextInt(elements.length - 1) + 1;
        int end = stichHelper(start, elements);

        String mutant = stich(start, end, 0, mutantElements.length - 1, elements, mutantElements);

        log("MUTATED " + victim.toString() + " INTO " + mutant + " USING MUTANT TREE" + mutantTree.toString());
        return gen.loadFromString(mutant);

    }

    //The mutation function, if mutation occurs, pics one of the mutations
    public BinaryTree mutate(BinaryTree victim) {

        Random randnum = new Random();
        Double gamble = randnum.nextDouble(); //0..1

        //If not lucky
        if (gamble >= mutationPropability) {
            return victim;
        }


        //Find out if we make a complex or regular mutation

        gamble = randnum.nextDouble(); //0..1
        if (gamble >= complexMutationProbapiblity) {
            return mutateSingleNode(victim);
        }

        return complexMutation(victim);

    }

    //Clones the elites
    public ArrayList<BinaryTree> cloneElites(ArrayList<BinaryTree> population) { //Will probably need a list with evaluations...

        ArrayList<BinaryTree> newPopulation = new ArrayList<>();
        Collections.sort(population);

        for (int i = 0; i < numberOfElites; i++) {
            newPopulation.add(population.get(i));
            log("CLONED ELITE:" + population.get(i).toString());
        }
        Logger.INSTANCE.reOpen();
        return newPopulation;
    }

    public BinaryTree tournamentSelection(ArrayList<BinaryTree> population, int size) {

        Random randnum = new Random();
        ArrayList<BinaryTree> contestants = new ArrayList<>();

        log("-----STARTED TOURNAMENT SELECTION------");

        if (size <= 1) {
            int rand = randnum.nextInt(population.size());
            log("-----TOURNAMENTSIZE TOO SMALL, RETURNED RANDOM------");
            return population.get(rand);
        }


        for (int i = 0; i < size; i++) {
            int rand = randnum.nextInt(population.size());
            contestants.add(population.get(rand));
        }

        BinaryTree winner = null;

        for (BinaryTree t : contestants) {

            //Check with radom, 0 if failed.
            if (checkVsRandom) {
                Double r = sim.simulateVsRandom(t.toString());
                if (r < neededWinPercentageVsRandomToAdvance) {
                    t.evaluation = 0;
                    log(t.toString() + "lost to Random, set to 0 ("
                            + r+" of "+neededWinPercentageVsRandomToAdvance+")");
                } else {
                    t.evaluation = evaluateSandboxed(t, contestants);
                }
            } else {
                //Just evaluate
                t.evaluation = evaluateSandboxed(t, contestants);
            }



            if (winner == null) {
                winner = t;
            }
            if (t.evaluation > winner.evaluation) {
                winner = t;
            }
        }

        log("-----Tournament winner " + winner.toString() + "-------");
        Logger.INSTANCE.reOpen();
        return winner;
    }

    //Get a random one from population, based on it's evaluation
    //Roulete wheel selection
    public BinaryTree rouletteWheelSelection(ArrayList<BinaryTree> population) { //Mighty need a list

        Double sum = 0.0;
        for (BinaryTree t : population) {
            sum += (t.evaluation + 1);
        }
        //+1 allows zeroes to participate too, but with minimal chances

        Random randnum = new Random();
        Double rand = randnum.nextDouble() * sum;

        //The most propabaly one goes first
        Collections.sort(population);

        for (BinaryTree t : population) {
            rand = rand - (t.evaluation + 1);
            if (rand <= 0) {
                return t;
            }
        }

        //Hope we dont end up here, babooom, crash
        return null;
    }

    //The evolution step
    public ArrayList<BinaryTree> evolve(ArrayList<BinaryTree> population) {
        //Start with null
        ArrayList<BinaryTree> newPopulation = null;

        //Different algorithm depending on the type of selection
        switch (selectionType) {

            case ROULETE_WHEEL:

                evaluatePopulation(population);
                
                //New population from elites
                newPopulation = cloneElites(population);

                //Make children, mutation inside crossStich
                for (int i = numberOfElites - 1; i < populationSize / 2; i++) {
                    crossStich(rouletteWheelSelection(population), rouletteWheelSelection(population), newPopulation);
                }                
                
                //Make the simulator grow if the percentage rises?
                break;

            case TOURNAMENT:

                newPopulation = new ArrayList<>();

                for (int i = 0; i < populationSize / 2; i++) {
                    log("Tournament ["+(i+1) +"/"+(populationSize/2)+"]");
                    //Fight for survival!
                    BinaryTree father = tournamentSelection(population, tournamentSize);
                    BinaryTree mother = tournamentSelection(population, tournamentSize);
                    //Add the children to newPopulation                    
                    crossStich(father, mother, newPopulation);
                    
                    Logger.INSTANCE.reOpen();
                }

        }


        return newPopulation;
    }

    //Evaluates the whole population
    public void evaluatePopulation(ArrayList<BinaryTree> population) {
        int i=1;
        
        for (BinaryTree t : population) {
            log("Evaluating ["+i++ +"/"+populationSize+"]");
            if (evaluateInSandBox) {
                //This seems to not be used...
                evaluateSandboxed(t, population);
            } else {
                evaluate(t);
            }
            Logger.INSTANCE.reOpen();
        }
    }

    public static void printPopulation(ArrayList<BinaryTree> population, String text) {                        
        
        
        log("*************" + text + "*************");
        for (BinaryTree t : population) {            
            log(t.toString());
        }
        log("*************" + text + "*************");
        
        
        //ReOpen LOG to save the current settings
        Logger.INSTANCE.reOpen();
    }
    
    
    //Prints starting parameters, what about the TOURNAMENT? MAPS ETC???
    public void printStartupData() {
        Logger.INSTANCE.logLine("<<<<<<STARTING PARAMETERS>>>>>");
        ClassPrinter.printClassInfo(gen);
        ClassPrinter.printClassInfo(sim);
        ClassPrinter.printClassInfo(this);
        ClassPrinter.printClassInfo(new Tournament()); //Fix later
        Logger.INSTANCE.logLine("<<<<<<STARTING PARAMETERS>>>>>");
        Logger.INSTANCE.logLine("");
        
        
    }
    
    //The main run method
    public void run() {        
        
      
        
        
        long totalTime = System.currentTimeMillis();   
        //Sets the log file to the current date 
        String DATE_FORMAT_NOW = "yyyy-MM-dd HH_mm_ss";
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
        String time = sdf.format(cal.getTime());              
        Logger.INSTANCE.open("C:\\geneticLog_"+time+".txt");
        
        printStartupData();
        
        
        
        
        
        if (population==null) population = generateStartingPopulation();
        
         //Print out the starting population
        printPopulation(population, "STARTING POPULATION");
        
        //Evolve for the number of iterations
        for (int i = 0; i < numberOfIterations; i++) {
            long iterationTime = System.currentTimeMillis();   
            population = evolve(population);
            //print the text, +2 so that first iteration is the first
            iterationTime = System.currentTimeMillis()-iterationTime;
            log("<<<<<ITERATION TIME: "+iterationTime+" ms>>>>>>");
                 
            printPopulation(population, "POPULATION NR." + (i + 1));            
        }

        //Output the global maximum if we can
        if (globalMaximum != null) {
            log("GLOBAL MAXIMUM: WINRATE=" + globalMaximum.evaluation + " CODE:" + globalMaximum.toString());
        }       
        
        
        
        totalTime = System.currentTimeMillis()-totalTime;;       
        
        Logger.INSTANCE.logLine("<<<<<TOTAL TIME: "+ totalTime+" ms>>>>>>");
        
        Logger.INSTANCE.close();  
        
    }    
    

    public static void main(String[] args) {           
                
        //Load starting stuff 
        GeneticMadness gm = new GeneticMadness();
        //gm.loadPopulation("C:\\bots.txt");          
       gm.run();               
                
        
    }
}
