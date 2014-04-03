/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package SimulatorTrees;

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
        
    
    }
