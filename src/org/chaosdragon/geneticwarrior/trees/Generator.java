/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chaosdragon.geneticwarrior.trees;

import java.util.Random;

/**
 *
 * @author Mighty
 */
public class Generator {

    public ValueMap map;
    private int position; //For loading from string    
    private Random randnum = new Random();    
    
    //RANGE for which constants can be generated
    public int CONSTANT_RANGE = 2;
    
    //The number of the parameter which is reserved for CONSTANTS
    public int CONSTANT_NUMBER = 21;
    

    public BinaryTree generateNonTerminal() {        
        return new BinaryTree(randnum.nextInt(map.actions.length), map);        
    }
    
    public Terminal generateTerminal() {
        
        int r = randnum.nextInt(map.parameters.length);
        
        //If we hit the constant position
        //Output a constant terminal object
        if (r==CONSTANT_NUMBER)        
            return new ConstantTerminal(
                    randnum.nextInt(CONSTANT_RANGE*2+1)-CONSTANT_RANGE, map);
        
        //Otherwise just a regular terminal
        return new Terminal(r, map);
    }
    
    private Node makeNode(int currentDepth) {

        
        //Larger to favor actions
        int rand = randnum.nextInt(2);

        Node node;
        //0 get parameter, 1 - action
        if (rand == 0 || currentDepth >= map.maxDepth) {

            node = generateTerminal();

        } else {

            node = generateNonTerminal();
            currentDepth++;
            ((BinaryTree) node).setLeft(makeNode(currentDepth));
            ((BinaryTree) node).setRight(makeNode(currentDepth));

        }

        return node;

    }

    public BinaryTree generate() {
       
        BinaryTree war = generateNonTerminal();
        war.setLeft(makeNode(0));
        war.setRight(makeNode(0));

        return war;


    }

    public Node loadNode(String[] keys) {
        Node node;
        //System.out.println(""+position);
        
        //If is a terminal, return it
        if (!isAction(keys[position])) {
            
            
            // C10 - constant 10, C-10 = constant -10
            if (keys[position].charAt(0)=='C') {
                String work = keys[position].substring(1);
                return new ConstantTerminal(Integer.parseInt(work),map);
            }
            
            return new Terminal(Integer.parseInt(keys[position++]),map);            
        }
        
        //If is an action....                         
        node = new BinaryTree(getKeyId(keys[position++]), map);
        
        ((BinaryTree) node).setLeft(loadNode(keys));    
        //position++;
        ((BinaryTree) node).setRight(loadNode(keys));
        //position++;
        
        return node;
    }

    public boolean isAction(String k) {
        for (String c : map.actions) {
            if (c.equals(k)) {
                return true;
            }


        }
        return false;
    }

    public int getKeyId(String k) {
        for (int i = 0; i < map.actions.length; i++) {
                                   
            if (map.actions[i].equals(k)) {
                return i;
            }

        }
        return -1;
    }
    
       
  
    public BinaryTree loadFromString(String input) {

        
        
        String[] keys = input.split("_");
        
        //If single element
        if (keys.length==0) return new BinaryTree(Integer.parseInt(keys[0]), map);
                
        BinaryTree war = new BinaryTree(getKeyId(keys[0]), map);
       
       Integer number = 1;        
       position=1;
                  
       war.setLeft(loadNode(keys));
       number++;
       war.setRight(loadNode(keys));                        
               
       return war;

    }
}
