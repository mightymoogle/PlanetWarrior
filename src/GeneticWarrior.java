
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
import ui.MainForm;

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

    //0 - total, 1 - player planets, 2 - enemy planets
    public static Double averageDistance(PlanetWars pw, int player) {

        double dsum = 0.0;
        int dcount = 0;

        if (player == 0) {

            for (Planet x : pw.Planets()) {

                for (Planet y : pw.Planets()) {

                    dsum += pw.Distance(x.PlanetID(), y.PlanetID());
                    dcount++;

                }

            }

            if (dcount == 0) {
                return 0.0;
            }
            return dsum / dcount;

        }

        if (player == 1) {

            for (Planet x : pw.MyPlanets()) {

                for (Planet y : pw.MyPlanets()) {

                    dsum += pw.Distance(x.PlanetID(), y.PlanetID());
                    dcount++;

                }

            }

            if (dcount == 0) {
                return 0.0;
            }
            return dsum / dcount;

        }

        if (player == 2) {

            for (Planet x : pw.EnemyPlanets()) {

                for (Planet y : pw.EnemyPlanets()) {

                    dsum += pw.Distance(x.PlanetID(), y.PlanetID());
                    dcount++;

                }

            }

            if (dcount == 0) {
                return 0.0;
            }
            return dsum / dcount;

        }

        return 0.0;
    }

    public static void DoTurn(PlanetWars pw) {

        Planet source = null;
        Planet dest = null;
        Double curEval = -99990.0;
        Double evaluation = 0.0;

        //if (pw.NumFleets()>5) return;

        //Total number of planets
        tree.map.parameters[5] = (double) pw.NumPlanets();

        //The average distance between all planets
        tree.map.parameters[16] = averageDistance(pw, 0);

        //The average distance between my planets
        tree.map.parameters[17] = averageDistance(pw, 1);

        //The average distance between enemy planets
        tree.map.parameters[18] = averageDistance(pw, 2);

        //Number of my total ships
        tree.map.parameters[8] = (double) pw.NumShips(1);
        //Number of enemy total ships
        tree.map.parameters[9] = (double) pw.NumShips(2);

        //Size of my fleets
        tree.map.parameters[14] = 0.0;
        for (Fleet f : pw.MyFleets()) {
            tree.map.parameters[14] += f.NumShips();
        }
        //Size of enemy fleets
        tree.map.parameters[15] = 0.0;
        for (Fleet f : pw.EnemyFleets()) {
            tree.map.parameters[15] += f.NumShips();
        }


        //Destination
        for (Planet targetP : pw.Planets()) {

            tree.map.parameters[0] = (double) targetP.NumShips();
            tree.map.parameters[2] = (double) targetP.GrowthRate();

            //Is target enemy planet 1, otherwise 0           
            tree.map.parameters[11] = 0.0;
            if (targetP.Owner() == 2) {
                tree.map.parameters[11]++;
            }

            //Is target my planet 1, otherwise 0           
            tree.map.parameters[12] = 0.0;
            if (targetP.Owner() == 1) {
                tree.map.parameters[12]++;
            }

            //Is target a neutral planet 1, otherwise 0           
            tree.map.parameters[13] = 0.0;
            if (targetP.Owner() == 0) {
                tree.map.parameters[13]++;
            }

            //Source
            for (Planet myP : pw.MyPlanets()) {

                tree.map.parameters[1] = (double) myP.NumShips();
                tree.map.parameters[3] = (double) myP.GrowthRate();
                tree.map.parameters[4] = (double) pw.Distance(myP.PlanetID(), targetP.PlanetID());

                //Size of fleets incoming to our planet
                tree.map.parameters[6] = 0.0;
                //Size of enemy fleets incoming to the planet we attack
                tree.map.parameters[7] = 0.0;

                //Size of our fleets incoming to our planet
                tree.map.parameters[19] = 0.0;
                //Size of our fleets incoming to the planet we attack
                tree.map.parameters[20] = 0.0;


                for (Fleet f : pw.EnemyFleets()) {
                    if (f.DestinationPlanet() == myP.PlanetID()) {
                        tree.map.parameters[6] += f.NumShips();
                    }
                    if (f.DestinationPlanet() == targetP.PlanetID()) {
                        tree.map.parameters[7] += f.NumShips();
                    }
                }

                for (Fleet f : pw.MyFleets()) {
                    if (f.DestinationPlanet() == myP.PlanetID()) {
                        tree.map.parameters[19] += f.NumShips();
                    }
                    if (f.DestinationPlanet() == targetP.PlanetID()) {
                        tree.map.parameters[20] += f.NumShips();
                    }
                }


                //Number of ships on planets closer than me
                tree.map.parameters[10] = 0.0;
                for (Planet eP : pw.EnemyPlanets()) {
                    if (pw.Distance(eP.PlanetID(), targetP.PlanetID())
                            <= pw.Distance(myP.PlanetID(), targetP.PlanetID())) {
                        tree.map.parameters[10] += eP.NumShips();
                    }
                }


                //if (curEval<=0)
                evaluation = Double.parseDouble(tree.traverse());

//                PrintWriter writer;
//                try {
//                    writer = new PrintWriter(new FileOutputStream(new File("C:\\the-file-name.txt"), true));
//                    writer.println(tree.print());
//                    writer.println(evaluation);
//                    writer.println(Arrays.toString(tree.map.parameters));
//                    writer.close();
//                } catch (Exception e) {
//                }

                // evaluation=5.0;

                if (evaluation > curEval && myP.NumShips() > 0 && targetP.Owner() != 1) {
                    source = myP;
                    dest = targetP;
                    curEval = evaluation;
                }

            }

        }


        if (source != null && dest != null) {
            int numShips = source.NumShips() / 2;
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

        if (args.length == 0) {
            /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

            MainForm form = new MainForm();
            
            form.setVisible(true);
            
        } else {

//        try {
//            writer = new PrintWriter(new FileOutputStream(new File("C:\\the-file-name.txt")));
//        } catch (Exception e) {
//        }
//
//        writer.println("STARTED WITH INPUT:" + args[0]);
//        writer.close();

            Generator gen = new Generator();
            ValueMap map = new ValueMap();
            map.actions = new String[6];
            map.actions[0] = "*";
            map.actions[1] = "+";
            map.actions[2] = "-";
            map.actions[3] = "%";
            map.actions[4] = "max";
            map.actions[5] = "min";


            map.parameters = new Double[21];
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
}
