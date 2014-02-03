import java.util.*;

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

	public static void DoTurn(PlanetWars game) {
		
		// calculates the minimum number of ships needed to take or protect
		// this planet
		int[] minShipsNeeded = new int[game.NumPlanets()+1];
		int[] myPlanetExpense = new int[game.NumPlanets() + 1];

		for(Planet planet : game.MyPlanets()){
			minShipsNeeded[planet.PlanetID()] -= planet.NumShips();
			myPlanetExpense[planet.PlanetID()] += planet.NumShips();
		}
		
		for(Planet planet : game.NotMyPlanets()){
			minShipsNeeded[planet.PlanetID()] += planet.NumShips() + 20;
		}
		
		for(Fleet fleet : game.MyFleets()){
			Planet fleetDest = game.GetPlanet(fleet.DestinationPlanet());
			if(game.EnemyPlanets().contains(fleetDest)){
				minShipsNeeded[fleetDest.PlanetID()] -= fleet.NumShips() - (fleet.TurnsRemaining() * fleetDest.GrowthRate());
			} else {
				minShipsNeeded[fleetDest.PlanetID()] -= fleet.NumShips();
			}
		}

		for(Fleet fleet : game.EnemyFleets()){
			Planet fleetDest = game.GetPlanet(fleet.DestinationPlanet());
			minShipsNeeded[fleetDest.PlanetID()] += fleet.NumShips();
			myPlanetExpense[fleetDest.PlanetID()] -= fleet.NumShips();
		}

		for(Planet source : game.MyPlanets()){
			for(Graph.Node node : game.getGraph().getAdjacent(source)){
				Planet dest = node.getPlanet();
				int numShips = Calculate.calculateLeastShips(source, dest, game, minShipsNeeded, myPlanetExpense, .8);
				if(numShips > 0){
					game.IssueOrder(source, dest, numShips);
					minShipsNeeded[source.PlanetID()] += numShips;
					minShipsNeeded[dest.PlanetID()] -= numShips;
					break;	
				}
			}
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

