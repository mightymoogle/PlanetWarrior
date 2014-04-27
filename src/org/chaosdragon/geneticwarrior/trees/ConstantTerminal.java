
package org.chaosdragon.geneticwarrior.trees;

/**
 *
 * @author Mighty
 * 
 * A TYPE OF TERMINAL THAT HAS A CONSTANT VALUE, NOT THE NUMBER OF THE PARAMETER
 * STARTS WITH A "C" IN OUTPUT
 */
public class ConstantTerminal extends Terminal {
   
   
   public ConstantTerminal(int name, ValueMap map) {
        super(name, map);
    }
   
   
   //Returns the number instead, not it's value
   @Override
    public String traverse() {
        return String.valueOf(value);
    }
   
   
    @Override
    public String toString() {
        return "C"+String.valueOf(this.value);
    }   
         
}
