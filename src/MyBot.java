import java.util.*;
import java.math.*;

public class MyBot {
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
	public enum STATE {
		CAREFUL, //in this state the ai is really careful with its moves
		EXPAND,  //forces the expansion of influence
		DEFAULT  //this is the default state of the AI
	}

	private static double[] influenceMap; //influence map
	private static STATE state;
	private static int[] shipRequestTable; // this will be a table that has all the ships we request to every location
	private static int[] shipAvailableTable; //this table will have all the ships that is available to us

	public static void DoTurn(PlanetWars game) {

		influenceMap = makeInfluenceMap(game);
		
		if(state == null){
			state = getState(game);
		}

		//USE LOOKAHEAD!
		shipRequestTable = new int[game.NumPlanets() + 1];
		shipAvailableTable = new int[game.NumPlanets() + 1];

		for(Planet planet : game.MyPlanets()) {
			shipRequestTable[planet.PlanetID()] -= planet.NumShips();
			shipAvailableTable[planet.PlanetID()] += planet.NumShips();

			int time = 0;
			ArrayList<Fleet> fleets = getIncomingFleets(game, planet);
			Collections.sort(fleets, new fleetComparator());

			for (Fleet fleet : fleets) {

				if(game.MyFleets().contains(fleet)){
					shipRequestTable[planet.PlanetID()] -= fleet.NumShips();
					continue;
				}

				int discreteTime = fleet.TurnsRemaining() - time;
				int planetGain = discreteTime * planet.GrowthRate() - fleet.NumShips();

				shipRequestTable[planet.PlanetID()] -= planetGain;
	
				if (planetGain < 0) {
					shipAvailableTable[planet.PlanetID()] += planetGain;
				}

				time = fleet.TurnsRemaining();
			}
		}

		for(Planet planet : game.EnemyPlanets()) {
			shipRequestTable[planet.PlanetID()] += planet.NumShips();

			int time = 0;
			ArrayList<Fleet> fleets = getIncomingFleets(game, planet);
			Collections.sort(fleets, new fleetComparator());

			for(Fleet fleet : fleets){

				if(game.EnemyFleets().contains(fleet)){
					shipRequestTable[planet.PlanetID()] += fleet.NumShips();
					continue;
				}

				int discreteTime = fleet.TurnsRemaining() - time;
				int planetGain = discreteTime * planet.GrowthRate() - fleet.NumShips();
				
				shipRequestTable[planet.PlanetID()] += planetGain;

				time = fleet.TurnsRemaining();
			}
		}

		for(Planet planet : game.NeutralPlanets()){
			shipRequestTable[planet.PlanetID()] += planet.NumShips();

			int shipsRemaining = planet.NumShips();
			ArrayList<Fleet> fleets = getIncomingFleets(game, planet);
			Collections.sort(fleets, new fleetComparator());
			int time = 0;

			for(Fleet fleet : fleets){
				if(game.MyFleets().contains(fleet)){
					shipRequestTable[planet.PlanetID()] -= fleet.NumShips();
				} else {
					shipRequestTable[planet.PlanetID()] += fleet.NumShips();
				}
			
			}
		}
		
		int moved = 0;

		for(Planet source : game.MyPlanets()){
			Planet bestDest = null;
			double bestHeuristic = 0;
			int bestNumShips = 0;
			for(Planet dest : game.Planets()){
				if(dest.PlanetID() == source.PlanetID()){
					continue;
				}

				int numShips;
				double heuristic;

				switch (state) {
					case CAREFUL:
						heuristic = getCarefulHeuristics(game, source, dest);
						numShips = getLeastShipsNeeded(game, source, dest, 2, 3);
						state = getState(game);
						break;
					default:
						heuristic = getHeuristics(dest);
						numShips = getLeastShipsNeeded(game, source, dest, 1, 3);
				}
				if(numShips > 0 && heuristic > bestHeuristic){ //this means we should send ships there
					bestDest = dest;
					bestHeuristic = heuristic;	
					bestNumShips = numShips;
				}
			}
			if(bestDest != null){
				moved++;
				game.IssueOrder(source, bestDest, bestNumShips);
				shipRequestTable[source.PlanetID()] += bestNumShips;
				shipAvailableTable[bestDest.PlanetID()] -= bestNumShips;
			}
		}
	}


	private static ArrayList<Fleet> getIncomingFleets(PlanetWars game, Planet planet){
		ArrayList<Fleet> ret = new ArrayList<Fleet>();

		for(Fleet fleet : game.Fleets()){
			if(fleet.DestinationPlanet() == planet.PlanetID()){
				ret.add(fleet);
			}
		}

		return ret;
	}

	/**getLeastShipsNeeded
	 * this method calculates the least number of ships I need to take over a planet
	 * or at least "confidently" take over a planet
	 */
	private static int getLeastShipsNeeded(PlanetWars game, Planet source, Planet dest, int div, int confidence){
		int sourceIndex = source.PlanetID();
		int destIndex = dest.PlanetID();

		int shipsHave = (int)(shipAvailableTable[sourceIndex]);
		int add = (game.NeutralPlanets().contains(dest)) ? 0 : dest.GrowthRate();

		int leastShips =(int) Math.ceil(((double)(shipRequestTable[destIndex]))/div) + add;

		if(leastShips >= Math.min(shipsHave, source.NumShips()-1) //we don't have enough ships
				|| shipsHave - leastShips < source.GrowthRate()*confidence){ //unwise
			return -1;
				}
		else {
			return leastShips + 1;
		}
	}

	/** getCarefulHeuristics
	 * this is a heuristics for the state in which we only have one planet that is super close to another
	 * planet
	 */

	private static double getCarefulHeuristics(PlanetWars game, int source, int dest, int planetNumShips){
		return (double) game.Distance(source, dest) / planetNumShips;
	}

	private static double getCarefulHeuristics(PlanetWars game, Planet source, Planet dest){
		if(game.EnemyPlanets().contains(dest)){
			return -1;
		}
		return getCarefulHeuristics(game, source.PlanetID(), dest.PlanetID(),  dest.NumShips());
	}
	/**getHueristics: this is the default heuristics
	 * this function will calculate the heuristic of a given planet
	 * the heuristic of a planet is a function of its growthrate, influence, and
	 * number of ships on the planet
	 */
	private static double getHeuristics(int planet, int growthrate, int numships){
		if (influenceMap[planet] < 0){
			return -1;
		}
		
		double numshipslog = (numships == 0) ? 0 : Math.log((double) numships) + .1;
		return growthrate * influenceMap[planet] / numships;

	}

	private static double getHeuristics(Planet planet){
		return getHeuristics(planet.PlanetID(), planet.GrowthRate(), planet.NumShips());
	}

	/**makeInfluenceMap
	 *
	 * This method will generate an influence map of the decrete time stamp of te game
	 * influence is calculated as a function of how big a planet is and how close that planet
	 * is
	 */
	private static double[] makeInfluenceMap(PlanetWars game) {
		double[] ret = new double[game.NumPlanets() + 1];
		List<Planet> planetsArr = game.Planets();

		for(Planet to : planetsArr) {
			int index = to.PlanetID();

			//my planet influence
			List<Planet> planets = game.MyPlanets();
			for(Planet from : planets) {
				double dist = (double) game.Distance(to.PlanetID(), from.PlanetID());
				double expDist = Math.exp(dist / 5);
				double influence = (double) from.NumShips();

				ret[index] += influence / expDist;
			}

			//enemy planet influence
			planets = game.EnemyPlanets();
			for(Planet from : planets) {
				double dist = (double) game.Distance(to.PlanetID(), from.PlanetID());
				double expDist = Math.exp(dist / 5);
				double influence = (double) from.NumShips();

				ret[index] -= influence / expDist;
			}
		}

		return ret;
	}

	/**getState
	 * this method returns the state in which the game is in
	 */
	private static STATE getState(PlanetWars game){
		if(game.MyPlanets().size() == 1){
			Planet myPlanet = game.MyPlanets().get(0);
			Planet enemyPlanet;
			if(game.EnemyPlanets() != null){
				enemyPlanet = game.EnemyPlanets().get(0);
			} else {
				return STATE.DEFAULT;
			}
			int dist = game.Distance(myPlanet.PlanetID(), enemyPlanet.PlanetID());

			if(enemyPlanet.NumShips()/1.5 > myPlanet.GrowthRate()*dist){
				return STATE.CAREFUL;
			} else {
				return STATE.DEFAULT;
			}

		} else {
			return STATE.DEFAULT;
		}
	}

	private static class fleetComparator implements Comparator<Fleet>{
		public int compare(Fleet f1, Fleet f2){
			return f1.TurnsRemaining() - f2.TurnsRemaining();
		}
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
						line += (char)c;
						break;
				}
			}
		} catch (Exception e) {
			// Owned.
		}
	}
}

