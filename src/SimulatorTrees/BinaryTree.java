/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package SimulatorTrees;

import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author Mighty
 */
public class BinaryTree extends Node implements Comparable<BinaryTree> {

    private Node root = null;
    private Node left;
    private Node right;
    public double evaluation;//For genetic stuff   
      
    

    public String traverse() {


        //If no left, return the value from parameters
        if (left == null) {
            return String.valueOf(map.parameters[root.value]);
        }


        double lefty = Double.parseDouble(left.traverse());
        double righty = Double.parseDouble(right.traverse());

        //If has left, the action is...
        String move = map.actions[root.value];
        if (move.equals("*")) {
            return String.valueOf(lefty * righty);
        }
        if (move.equals("+")) {
            return String.valueOf(lefty + righty);
        }
        if (move.equals("-")) {
            return String.valueOf(lefty - righty);
        }
        if (move.equals("%")) {
            if (righty == 0) {
                return "0";
            }
            return String.valueOf(lefty / righty);
        }
        
        if (move.equals("min")) {
            return String.valueOf(Math.min(lefty, righty));
        }
        
        if (move.equals("max")) {
            return String.valueOf(Math.max(lefty, righty));
        }

        return "ERROR";

    }

    public String print() {

        if (left == null) {
            return String.valueOf(map.parameters[value]);
        }
        return "(" + map.actions[root.value] + " " + left.print() + " " + right.print() + ")";

    }

    public BinaryTree(int node, ValueMap m) {

        root = new Terminal(node, m);
        map = m;
        //root.value = node;                

    }

    @Override
    public String toString() {

        if (left == null) {
            return String.valueOf(root.value);
        }
        return map.actions[root.value] + "_" + left.toString() + "_" + right.toString();
    }

    /**
     * @return the root
     */
    public Node getRoot() {
        return root;
    }

    /**
     * @param root the root to set
     */
    public void setRoot(Node root) {
        this.root = root;
    }

    /**
     * @return the left
     */
    public Node getLeft() {
        return left;
    }

    /**
     * @param left the left to set
     */
    public void setLeft(Node left) {
        this.left = left;
    }

    /**
     * @return the right
     */
    public Node getRight() {
        return right;
    }

    /**
     * @param right the right to set
     */
    public void setRight(Node right) {
        this.right = right;
    }

    public static void main(String[] args) {

        ValueMap maps = new ValueMap();

        maps.actions = new String[2];
        maps.actions[0] = "*";
        maps.actions[1] = "+";

        maps.parameters = new Double[2];



//        tree1.setLeft(new Action("2"));        
//        
//        BinaryTree tree2 = new BinaryTree("+");
//        tree2.setLeft(new Action("3"));
//        tree2.setRight(new Action("4"));
//        
//        tree1.setRight(tree2);
//        
//        System.out.println(tree1.traverse());

        Generator gen = new Generator();
        gen.map = maps;
//        
        BinaryTree tree = gen.generate();

        maps.parameters[0] = 4.1;
        maps.parameters[1] = 2.4;

        System.out.println(tree.print());
        System.out.println(tree.traverse());

        //ouble max = Double.parseDouble(tree.traverse());  
        System.out.println(tree.toString());
        System.out.println("NEW:");

        BinaryTree test = gen.loadFromString(tree.toString());

        System.out.println(test.toString());
        System.out.println(test.traverse());


    }

    @Override
    public int compareTo(BinaryTree o) {
        //Backwards, so that LARGEST to SMALLEST
        return (int) (o.evaluation - this.evaluation);

    }

    @Override
    public void toNodeArray(ArrayList<Node> arr) {
        
        arr.add(root);
        left.toNodeArray(arr);
        right.toNodeArray(arr);
        
    }
}
