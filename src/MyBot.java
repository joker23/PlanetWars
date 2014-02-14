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
		DEFAULT  //this is the default state of the AI
	}

	private static double[] influenceMap; //influence map
	private static STATE state;
	private static int[] shipRequestTable; // this will be a table that has all the ships we request to every location
	private static int[] shipAvailableTable; //this table will have all the ships that is available to us
	private static int turnCount = 0;

	public static void DoTurn(PlanetWars game) {
		turnCount ++ ;
		influenceMap = makeInfluenceMap(game);
		
		state = getState(game);
		

		//USE LOOKAHEAD!
		shipRequestTable = new int[game.NumPlanets() + 1];
		shipAvailableTable = new int[game.NumPlanets() + 1];
		
		//look at my current planet and update how many ships we can 
		//actually send safely
		for(Planet planet : game.MyPlanets()) {
			shipRequestTable[planet.PlanetID()] -= planet.NumShips();
			shipAvailableTable[planet.PlanetID()] += planet.NumShips();

			int time = 0; //beam search through only times of interest
			ArrayList<Fleet> fleets = getIncomingFleets(game, planet);
			Collections.sort(fleets, new fleetComparator());

			for (Fleet fleet : fleets) {

				if(game.MyFleets().contains(fleet)){ //if it is my fleet...then we don't need to send as much
					shipRequestTable[planet.PlanetID()] -= fleet.NumShips();
					continue;
				}
				
				//else we have to check out how much the enemy is sending
				int discreteTime = fleet.TurnsRemaining() - time;
				int planetGain = discreteTime * planet.GrowthRate() - fleet.NumShips();

				shipRequestTable[planet.PlanetID()] -= planetGain;
	
				// we can only say that we have available ships if the enemy doesn't have enough to take the planet
				if (planetGain < 0) {
					shipAvailableTable[planet.PlanetID()] += planetGain;
				}

				time = fleet.TurnsRemaining();
			}
		}

		//see how many we need to send to the enemy planet to take it
		for(Planet planet : game.EnemyPlanets()) {
			shipRequestTable[planet.PlanetID()] += planet.NumShips();

			int time = 0;
			ArrayList<Fleet> fleets = getIncomingFleets(game, planet);
			Collections.sort(fleets, new fleetComparator());

			for(Fleet fleet : fleets){

				if(game.EnemyFleets().contains(fleet)){ //if it is the enemy fleet then we need more ships at this planet
					shipRequestTable[planet.PlanetID()] += fleet.NumShips();
					continue;
				}

				int discreteTime = fleet.TurnsRemaining() - time;
				int planetGain = discreteTime * planet.GrowthRate() - fleet.NumShips(); //how many ships we need at that time
				
				shipRequestTable[planet.PlanetID()] += planetGain;

				time = fleet.TurnsRemaining();
			}
		}
		
		//see what the neutral planets need
		for(Planet planet : game.NeutralPlanets()){
			shipRequestTable[planet.PlanetID()] += planet.NumShips();

			ArrayList<Fleet> fleets = getIncomingFleets(game, planet);

			for(Fleet fleet : fleets){
				if(game.MyFleets().contains(fleet)){
					shipRequestTable[planet.PlanetID()] -= fleet.NumShips();
				} else {
					shipRequestTable[planet.PlanetID()] += fleet.NumShips();
				}	
			}
		}

		//find the average influence
		double avgInf = 0;
		double[] influenceDeficit = new double[game.NumPlanets()];
		for(Planet planet : game.MyPlanets()){
			avgInf += influenceMap[planet.PlanetID()];
		}
		avgInf /= game.MyPlanets().size();

		for(Planet planet : game.MyPlanets()){
			influenceDeficit[planet.PlanetID()] = avgInf - influenceMap[planet.PlanetID()];
		}

		//find the minimum distances
		int[] minimumDistance = new int[game.NumPlanets()];
		for(Planet mine : game.MyPlanets()){
			int minDist = Integer.MAX_VALUE;
			for(Planet e : game.EnemyPlanets()){
				minDist = Math.min(minDist, game.Distance(mine.PlanetID(), e.PlanetID()));
			}

			minimumDistance[mine.PlanetID()] = minDist;
		}

		int maxEnemyPlanetSize = 0;
		for(Planet planet : game.EnemyPlanets()){
			maxEnemyPlanetSize = Math.max(maxEnemyPlanetSize, planet.NumShips());
		}

		for(Planet source : game.MyPlanets()){
			// midgame to keep things interesting
			if(turnCount > 100 && source.NumShips() > maxEnemyPlanetSize * 2 + 1){
				//if we have a planet greater than all other planets by 2x
				int t = Integer.MAX_VALUE;
				Planet d = null;
				for(Planet dest : game.EnemyPlanets()){
					if(t > game.Distance(dest.PlanetID(), source.PlanetID())){
						t = game.Distance(dest.PlanetID(), source.PlanetID());
						d = dest;
					}
				}
				if(d != null){
					game.IssueOrder(source, d, maxEnemyPlanetSize + 1);
					continue;
				}
			}
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
						numShips = getLeastShipsNeeded(game, source, dest, 1, 3);
						state = getState(game);
						break;
					default:
						heuristic = getHeuristics(dest, 0);
						numShips = getLeastShipsNeeded(game, source, dest, 1, calcConfidence(game, source));
				}
				if(numShips > 0 && heuristic > bestHeuristic){ //this means we should send ships there
					bestDest = dest;
					bestHeuristic = heuristic;	
					bestNumShips = numShips;
				}
			}
			if(bestDest != null){
				game.IssueOrder(source, bestDest, bestNumShips);
				shipRequestTable[source.PlanetID()] += bestNumShips;
				shipAvailableTable[bestDest.PlanetID()] -= bestNumShips;
			} else {
				//if we did not issue any orders...kick in the influence balancing ai
				boolean underAttack = false;
				for(Fleet fleet : game.EnemyFleets()){
					if(fleet.DestinationPlanet() == source.PlanetID()){
						underAttack = true;
						break;
					}
				}
				if(underAttack){
					continue;
				}
				int deficit = - (int)influenceDeficit[source.PlanetID()];
				if(deficit <= 0 || shipAvailableTable[source.PlanetID()] < 1){
					continue;
				}
				int numShips = Math.min (deficit, shipAvailableTable[source.PlanetID()] - 1);
				if(numShips >= source.NumShips()){
					continue;
				}
				double minDist = Integer.MAX_VALUE;
				int maxInd = -1;
				for(Planet planet : game.MyPlanets()){
					int currind = planet.PlanetID();
					if(minimumDistance[currind] < minDist && planet.PlanetID() != source.PlanetID()){
						minDist = minimumDistance[currind];
						maxInd = currind;
					}
				}
				if(maxInd > -1){
					game.IssueOrder(source.PlanetID(), maxInd, numShips);
					influenceDeficit[maxInd] = 0;
				}
			}
		}
	}

	private static int calcConfidence(PlanetWars game, Planet planet){
		//find nearest enemy planet
		int nearest = Integer.MAX_VALUE;
		for(Planet e : game.EnemyPlanets()){
			nearest = Math.min(nearest, game.Distance(planet.PlanetID(), e.PlanetID()));
		}

		if(nearest < 15){
			return 3;
		} else{
			return 2;
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
	private static double getHeuristics(int planet, int growthrate, int numships, int minInfluence){
		//if (influenceMap[planet] < minInfluence){
		//	return -1;
		//}
		
		double numshipslog = (numships == 0) ? 0 : Math.log((double) numships) + .1;
		return growthrate * influenceMap[planet] / numships;

	}

	private static double getHeuristics(Planet planet, int minInfluence){
		return getHeuristics(planet.PlanetID(), planet.GrowthRate(), planet.NumShips(), minInfluence);
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

	private static int getEnemyGrowthRate(PlanetWars game){
		int ret = 0;
		for(Planet p : game.EnemyPlanets()){
			ret += p.GrowthRate();
		}

		return ret;
	}

	private static int getMyGrowthRate(PlanetWars game){
		int ret = 0;
		for(Planet p : game.MyPlanets()){
			ret += p.GrowthRate();
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

		}
		else {
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

