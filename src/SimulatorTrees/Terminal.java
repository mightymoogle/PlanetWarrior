/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package SimulatorTrees;

import java.util.ArrayList;

/**
 *
 * @author Mighty
 */
public class Terminal extends Node {
    
    public String traverse() {
        return String.valueOf(map.parameters[value]);
    }
    
    public Terminal(int name, ValueMap map) {
        value = name;
        this.map=map;
    }
    
    @Override
    public String toString() {
        return String.valueOf(this.value);
    }
    
    public String print() {
        return String.valueOf(map.parameters[value]);
    }

    @Override
    public void toNodeArray(ArrayList<Node> arr) {
        
        arr.add(this);
        
    }    
    
    
    
}
