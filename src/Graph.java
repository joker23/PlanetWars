import java.util.*;

/**graph
 * this is a graph representation of the game
 * it will be generated in the beginning of the game
 * it will only hold the planet and the planet and the 
 * planets that are the closest to that other planet
 */
public class Graph {

	private ArrayList<Node>[] graph;

	public Graph(List<Planet> planets, PlanetWars game){
		graph = new ArrayList[planets.size()];

		for(Planet u : planets){
			int index = u.PlanetID();
			graph[index] = new ArrayList<Node>();
			
			for(Planet v : planets){
				graph[index].add(new Node(v, game.Distance(u.PlanetID(),v.PlanetID())));
			}

			Collections.sort(graph[index]);
		}
	}

	public ArrayList<Node> getAdjacent(Planet planet){
		return graph[planet.PlanetID()];
	}

	public ArrayList<Node> getAdjacent(int planet){
		return graph[planet];
	}

	public class Node implements Comparable<Node>{
		Planet planet;
		int dist;
		
		public Node(Planet planet, int dist){
			this.planet = planet;
			this.dist = dist;
		}

		public int getDist(){
			return dist;
		}

		public Planet getPlanet(){
			return planet;
		}

		public int compareTo(Node node){
			int dist2 = node.dist;

			if(dist > dist2){
				return 1;
			} else if (dist == dist2){
				return 0;
			} else {
				return -1;
			}
			
		}
	}
}
