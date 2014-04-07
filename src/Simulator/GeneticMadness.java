/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Simulator;

import SimulatorTrees.BinaryTree;
import SimulatorTrees.Generator;
import SimulatorTrees.Node;
import SimulatorTrees.Terminal;
import SimulatorTrees.ValueMap;
import java.util.*;

/**
 *
 * @author Mighty
 */
public class GeneticMadness {

    //Size of the population, larger the better, the slower
    private static int populationSize = 20;
    //How many iterations to make, same as above...
    private static int numberOfIterations = 2;
    //Chance to have children, if no children, returns both parents
    private static double crossPropability = 1;
    //Chance that a mutation will happen
    private static double mutationPropability = 0.10;
    //Chance that a complex mutation will hapen.
    //Regular = 1-P
    private static double complexMutationProbapiblity = 0.3;
    //How many top trees we copy to the next population
    private static int numberOfElites = 1; //size - elites must be pair, or one will be lost and probably crash???
    //The thing that generates everything
    private static Generator gen;
    //This holds the global maximum
    private static BinaryTree globalMaximum;
    //IF false - checks vs other bots, if true, checks from population
    private static boolean evaluateInSandBox = true;
    //Play vs yourself in evaluation if sandboxed
    private static boolean mirrorInSandbox = false;
    //determines the selection type

    private static enum selectionTypes {

        ROULETE_WHEEL, TOURNAMENT
    };
    private static selectionTypes selectionType = selectionTypes.TOURNAMENT;
    //Determines the size of the tournament
    private static int tournamentSize = 3;

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

    //Replace to a file later?
    public static void log(String input) {
        System.out.println(input);
    }

    //Randomly generates the starting population
    private static ArrayList<BinaryTree> generateStartingPopulation() {

        ArrayList<BinaryTree> population = new ArrayList<>();

        for (int i = 0; i < populationSize; i++) {
            population.add(gen.generate());
        }

        if (selectionType==selectionTypes.ROULETE_WHEEL) {     
            //evaluate the whole populatioin if it's roulete wheel
            evaluatePopulation(population);
        }
        return population;

    }

    //Load precalculated starting elements, generate missing ones
//    private static ArrayList<BinaryTree> generateStartingPopulation(String[] precalculated) {
//        //NOT IMPELMENTED
//    }
    //Evaluates a single node, setting the maximum and it's evaluation parameter.
    //Also returns the evaluation
    public static double evaluate(BinaryTree t) {
        t.evaluation = Simulator.simulateFromSuffix(t.toString());

        if (globalMaximum == null) {
            globalMaximum = t;
        }

        if (t.evaluation > globalMaximum.evaluation) {
            globalMaximum = t;
        }
        return t.evaluation;
    }

    //Probably will change to the tournament selection
    public static double evaluateSandboxed(BinaryTree t, ArrayList<BinaryTree> population) {

        ArrayList<String> prefixes = new ArrayList<>();

        for (BinaryTree c : population) {
            prefixes.add(c.toString());
        }
        if (!mirrorInSandbox) {
            prefixes.remove(t.toString());
        }
        t.evaluation = Simulator.simulateFromSuffix(t.toString(), prefixes);


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
    public static int stichHelper(int fatherStart, String[] fatherElements) {

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
    public static String stich(int fatherStart, int fatherEnd,
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
    public static void crossStich(BinaryTree father, BinaryTree mother, ArrayList<BinaryTree> result) {
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
    public static BinaryTree mutateSingleNode(BinaryTree victim) {
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
    public static BinaryTree complexMutation(BinaryTree victim) {

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
    public static BinaryTree mutate(BinaryTree victim) {

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
    public static ArrayList<BinaryTree> cloneElites(ArrayList<BinaryTree> population) { //Will probably need a list with evaluations...

        ArrayList<BinaryTree> newPopulation = new ArrayList<>();
        Collections.sort(population);

        for (int i = 0; i < numberOfElites; i++) {
            newPopulation.add(population.get(i));
            log("CLONED ELITE:" + population.get(i).toString());
        }

        return newPopulation;
    }

    public static BinaryTree tournamentSelection(ArrayList<BinaryTree> population, int size) {
                
        Random randnum = new Random();
        ArrayList<BinaryTree> contestants = new ArrayList<>();

        log("-----STARTED TOURNAMENT SELECTION------");
        
        if (size<=1) {
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
            t.evaluation = evaluateSandboxed(t, contestants);
            if (winner == null) {
                winner = t;
            }
            if (t.evaluation > winner.evaluation) {
                winner = t;
            }
        }

        log("-----Tournament winner " + winner.toString() + "-------");
        return winner;
    }

    //Get a random one from population, based on it's evaluation
    //Roulete wheel selection
    public static BinaryTree rouletteWheelSelection(ArrayList<BinaryTree> population) { //Mighty need a list

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
    public static ArrayList<BinaryTree> evolve(ArrayList<BinaryTree> population) {
        //Start with null
        ArrayList<BinaryTree> newPopulation=null;
        
        //Different algorithm depending on the type of selection
        switch (selectionType) {

            case ROULETE_WHEEL:
                //New population from elites
                newPopulation = cloneElites(population);

                //Make children, mutation inside crossStich
                for (int i = numberOfElites - 1; i < populationSize / 2; i++) {
                    crossStich(rouletteWheelSelection(population), rouletteWheelSelection(population), newPopulation);
                }

                evaluatePopulation(newPopulation);

                //Make the simulator grow if the percentage rises?
                break;
                
            case TOURNAMENT:
                
                newPopulation = new ArrayList<>();
                
                for (int i = 0; i<populationSize/2; i++) {
                    //Fight for survival!
                    BinaryTree father = tournamentSelection(population, tournamentSize);
                    BinaryTree mother = tournamentSelection(population, tournamentSize);                    
                    //Add the children to newPopulation                    
                    crossStich(father, mother,newPopulation);                    
                }               
                
        }


        return newPopulation;
    }

    //Evaluates the whole population
    public static void evaluatePopulation(ArrayList<BinaryTree> population) {
        for (BinaryTree t : population) {
            if (evaluateInSandBox) {
                evaluateSandboxed(t, population);
            } else {
                evaluate(t);
            }
        }
    }
    
    
    public static void printPopulation(ArrayList<BinaryTree> population, String text) {
           log("*************"+text+"*************");
            for (BinaryTree t : population) {
                log(t.toString());
            }
            log("*************"+text+"*************");

    }

    public static void main(String[] args) {

        //Load starting stuff
        startUp();

        //Generate the starting popuation, will need something else if need to resume
        ArrayList<BinaryTree> population = generateStartingPopulation();
        //Print out the starting population
        printPopulation(population, "STARTING POPULATION");
                
        //Evolve for the number of iterations
        for (int i = 0; i < numberOfIterations; i++) {
            population = evolve(population);
            //print the text, +2 so that first iteration is the first
            printPopulation(population, "POPULATION NR."+(i+1));
        }

        //Output the global maximum if we can
        if (globalMaximum!=null) {
            log("GLOBAL MAXIMUM: WINRATE=" + globalMaximum.evaluation + " CODE:" + globalMaximum.toString());
        }
        

    }
}
