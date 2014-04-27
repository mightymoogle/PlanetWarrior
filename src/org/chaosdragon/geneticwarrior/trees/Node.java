/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chaosdragon.geneticwarrior.trees;

import java.util.ArrayList;

/**
 *
 * @author Mighty
 */
public abstract class Node {    
        
    protected int value; //Index of the value
    public ValueMap map;
    public abstract String traverse(); 
    public abstract String print();
    @Override
    public abstract String toString();
    
    public abstract void toNodeArray(ArrayList<Node> arr);      
    
    public void setVaue(int value) {
        this.value = value;
    }
    
    public int getValue() {
        return value;
    }
    
    }
