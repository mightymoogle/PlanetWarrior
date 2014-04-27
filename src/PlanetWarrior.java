
import org.chaosdragon.geneticwarrior.main.Planet;
import org.chaosdragon.geneticwarrior.main.Fleet;
import org.chaosdragon.geneticwarrior.main.PlanetWars;
import java.util.*;

public class PlanetWarrior {
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

    public int FindClosestAllyPlanet(PlanetWars pw, int planet) {


        return 0;
    }

    public static void DoTurn(PlanetWars pw) {

        Planet source = null;
        double sourceScore = Double.MIN_VALUE;

        ArrayList<Integer> used = new ArrayList<>();

        //SNIPE        
        for (Planet p : pw.NeutralPlanets()) {

            int needShips = 0;

            for (Fleet f : pw.EnemyFleets()) {

                if (f.NumShips() > p.NumShips()) {
                    needShips += f.NumShips() - p.NumShips();
                }
            }

            for (Fleet mf : pw.MyFleets()) {

                if (mf.DestinationPlanet() == p.PlanetID()) {
                    needShips = -mf.NumShips();
                }
            }

            if (needShips>0){

                for (Planet z : pw.MyPlanets()) {
                    
                    int needShips2= needShips+p.GrowthRate()*pw.Distance(z.PlanetID(),p.PlanetID())+2;
                    
                    
                    if (z.NumShips()>needShips2) {
                        
                        pw.IssueOrder(z.PlanetID(), p.PlanetID(), needShips2);
                        
                        
                        
                                               
                        
                    }
                    
                }

            }


        }

        //ATACK

        //DEFEND

        //REPOSITION


        for (Planet p : pw.MyPlanets()) {
            double score = (double) p.NumShips() / (1 + p.GrowthRate());
            if (score > sourceScore) {
                sourceScore = score;
                source = p;
            }
        }
        // (3) Find the weakest enemy or neutral planet.
        Planet dest = null;
        double destScore = Double.MIN_VALUE;
        for (Planet p : pw.NotMyPlanets()) {
            double score = (double) (1 + p.GrowthRate()) / p.NumShips();
            if (score > destScore) {
                destScore = score;
                dest = p;
            }
        }
        // (4) Send half the ships from my strongest planet to the weakest
        // planet that I do not own.
        if (source != null && dest != null) {
            int numShips = source.NumShips() / 2;
            pw.IssueOrder(source, dest, numShips);
        }


    }

    public static void DoTurn1(PlanetWars pw) {
        // (1) If we currently have a fleet in flight, just do nothing.
        int numFleets = 1;

        if (pw.Production(1) >= pw.Production(2)) {
            numFleets = 1;
        } else {
            numFleets = 3;
        }




        if (pw.MyFleets().size() >= numFleets) {
            return;
        }






//        
//        if (pw.MyFleets().size() >= 10) {
//	    return;
//	}
        // (2) Find my strongest planet.
        Planet source = null;
        double sourceScore = Double.MIN_VALUE;
        for (Planet p : pw.MyPlanets()) {
            double score = (double) p.NumShips() / (1 + p.GrowthRate());
            if (score > sourceScore) {
                sourceScore = score;
                source = p;
            }
        }
        // (3) Find the weakest enemy or neutral planet.
        Planet dest = null;
        double destScore = Double.MIN_VALUE;
        for (Planet p : pw.NotMyPlanets()) {
            double score = (double) (1 + p.GrowthRate()) / p.NumShips();
            if (score > destScore) {
                destScore = score;
                dest = p;
            }
        }
        // (4) Send half the ships from my strongest planet to the weakest
        // planet that I do not own.
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
