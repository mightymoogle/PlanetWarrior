
import SimulatorTrees.Terminal;
import SimulatorTrees.BinaryTree;
import SimulatorTrees.Generator;
import SimulatorTrees.ValueMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GeneticWarrior {
    // The DoTurn function is where your code goes. The PlanetWars object
    // contains the state of the game, including information about all planets
    // and fleets that currently exist. Inside this function, you issue orders
    // using the pw.IssueOrder() function. For example, to send 10 ships from
    // planet 3 to planet 8, you would say pw.IssueOrder(3, 8, 10).
    //
    // There is already a basic strategy in place here. You can use it as a
    // starting point, or you can throw it out entirely and replace it with
    // your own. Check out the tutorials and articles on the contest website at
    // http://www.ai-contest.com/resources.

    Generator gen;
    static BinaryTree tree;
    static PrintWriter writer;
    
    
    public int FindClosestAllyPlanet(PlanetWars pw, int planet) {


        return 0;
    }

    public static void DoTurn(PlanetWars pw) {

        Planet source=null;
        Planet dest=null;                        
        Double curEval = -99990.0;        
        Double evaluation=0.0;
                
        
        //if (pw.NumFleets()>5) return;
        
        for (Planet enemyP: pw.Planets()) {
          
           tree.map.parameters[0] =(double) enemyP.NumShips();
           tree.map.parameters[2] = (double) enemyP.GrowthRate();
                       
            for (Planet myP: pw.MyPlanets()) {    
            
             tree.map.parameters[1] = (double) myP.NumShips();
             tree.map.parameters[3] = (double) myP.GrowthRate();
             tree.map.parameters[4] = (double) pw.Distance(myP.PlanetID(), enemyP.PlanetID());

                
                
               //if (curEval<=0)
               evaluation = Double.parseDouble(tree.traverse());                
               
//               PrintWriter writer;
//                try {
//                    writer = new PrintWriter(new FileOutputStream(new File("C:\\the-file-name.txt"),true));
//                         writer.println(tree.print());
//                         writer.println(evaluation);
//                          writer.close();                
//                } catch (Exception e) {}          
             
               // evaluation=5.0;
                
                 if (evaluation>curEval && myP.NumShips()>0 && enemyP.Owner()!=1) {
                    source = myP;
                    dest = enemyP;
                    curEval  = evaluation;                    
                }
                
            }                                     
        
        }
        
           
                
                
        if (source != null && dest != null) {
            int numShips = source.NumShips()/2;
            pw.IssueOrder(source, dest, numShips);
        }        

    }

 
    public static Double evaluate(Planet source, Planet destination, int numShips, PlanetWars pw) {

        //Distance to planet
        //It's forces
        //It's production (size)
        //Average distance to enemy
        //Average distance to myself                     




        return 0.0;
    }

    public static void main(String[] args) {
        
        try {
        writer = new PrintWriter(new FileOutputStream(new File("C:\\the-file-name.txt")));
        } catch (Exception e) {}
        
        writer.println("STARTED WITH INPUT:"+args[0]);
         writer.close();
        
        Generator gen = new Generator();        
        ValueMap map = new ValueMap();        
        map.actions = new String[4];
        map.actions[0] = "*";
        map.actions[1] = "+";
        map.actions[2] = "-";
        map.actions[3] = "%";        
        map.parameters = new Double[5];     
        //map.parameters[0]=1.5;
        //map.parameters[1]=2.5;
        
        gen.map = map;                                     
                
        tree = gen.loadFromString(args[0]);
        
//        tree = gen.generate();                       
        
        String line = "";
        String message = "";
        int c;
        try {
            while ((c = System.in.read()) >= 0) {
                switch (c) {
                    case '\n':
                        if (line.equals("go")) {
                            PlanetWars pw = new PlanetWars(message);
                            DoTurn(pw);
                            pw.FinishTurn();
                            message = "";
                        } else {
                            message += line + "\n";
                        }
                        line = "";
                        break;
                    default:
                        line += (char) c;
                        break;
                }
            }
           
        } catch (Exception e) {
            // Owned.
            
        }
    }
}
