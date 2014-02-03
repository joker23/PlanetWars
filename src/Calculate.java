import java.util.*;
import java.math.*;

/** Calculate
 * this class will store static methods that will
 * do all the calculations that is needed for the bot
 */
public class Calculate{
	
	private static final int maxTurns = 200; //maximum number of turns in the game


	/**
	 * calculate the least ships needed to attack a planet from a specific attacker
	 * with us only willing to spend % reflected by confidence
	 *
	 * if it is impossible then we will return -1;
	 */
	public static int calculateLeastShips(Planet attacker, Planet defender, PlanetWars game, int[] minShipsNeeded, int[] myPlanetExpense, double confidence){
		int shipsHave = (int)(myPlanetExpense[attacker.PlanetID()] * confidence);
		int leastShips = minShipsNeeded[defender.PlanetID()];

		if(leastShips > shipsHave){
			return -1;
		} else {
			return leastShips;
		}
	}
	/** getHeuristic
	 * calculates the heuristic of the planet
	 * heuristic is calculated by #shipCost/#shipsGained
	 *
	 * @input Planet : planet that we are looking at
	 */
	public static double getHeuristic(Planet planet){
		int heuristic = 0;

		return heuristic;
	}

	/** getInfluence
	 * calculates the influence that each planet
	 * has on eachother
	 */
	public static double updateInfluence(PlanetWars game){
		double inference = 0;
		
		return inference;
	}
}
