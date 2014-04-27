/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chaosdragon.geneticwarrior.trees;

import java.util.Arrays;

/**
 *
 * @author Mighty
 */
public class ValueMap {
    public Double[] parameters;   //Army X, Army Y, Growth X, Growth Y
    public String[] actions;
    public int maxDepth=3;
     
     
     @Override
    public String toString() {
        return Arrays.toString(actions)+Arrays.toString(parameters);
    }
     
}
