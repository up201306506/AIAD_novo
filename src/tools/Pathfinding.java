package tools;

import java.util.ArrayList;
import java.util.PriorityQueue;
import application.*;

public class Pathfinding {
	
	final static boolean _DEBUG = false;

	public static class Map_Cell implements Comparable<Map_Cell>{
		public int x = 0;
		public int y = 0;
		public byte value = 0;
		public boolean visited = false;
		public Map_Cell previous;
		public int effort = Integer.MAX_VALUE; //Effort expected to REACH the DESTINATION
		public int distance = Integer.MAX_VALUE; //Distance travelled FROM SOURCE cell
		
		

		//Constructor
		public Map_Cell (int x, int y){
			this.x = x;
			this.y = y;
			if(this.x < 0){
				throw new IndexOutOfBoundsException("Index x = "+ x +" is out of bounds!"); 
			}
			if(this.y < 0){
				throw new IndexOutOfBoundsException("Index y = "+ y +" is out of bounds!"); 
			}
		}

		//Comparator
		public int compareTo(Map_Cell another) {
			return Integer.valueOf(this.distance).compareTo(another.distance);
		}
		/* A* comparator
		public int compareTo(Map_Cell another) {
			return Integer.valueOf(this.distance + this.effort).compareTo(another.distance + another.effort);
		}
		*/
	}

	public static class Path_Finding_Map{
		public int cols = 0;
		public int rows = 0;
		public Map_Cell[][] datamap;
		public byte wall = 0;
		public boolean loaded = false;
		
		

		//Constructor
		public Path_Finding_Map( int c, int r, byte fill ){
			this.cols = c;
			this.rows = r;
			datamap = new Map_Cell[r][c];
			for(int i = 0; i < rows; i++){
				for(int j = 0; j < cols; j++){
					datamap[i][j] = new Map_Cell(j, i);
					datamap[i][j].value = fill;
				}
			}
		}
		public Path_Finding_Map(int c, int r, byte[][] data, byte wall){
			this(c, r, (byte) 0);
			this.wall = wall;
			try{
				if(!load_map_blocks(data))
					throw new IllegalArgumentException();
			}
			catch(IllegalArgumentException e){
				System.err.println("Could not load map data to Pathfinding class, invalid map");
			}
		}
		public Map_Cell getCell(int x, int y){
			return datamap[y][x];
		}
		
		public boolean valid_map_size(byte[][] data){
			if(rows != data.length)
				return false;
			
			for(int i = 0; i < rows; i++)
				if(cols != data[i].length)
					return false;
						
			return true;
		}
		
		
		public ArrayList<Map_Cell> getNeighbours(Map_Cell cell){
			return getNeighbours(cell.x, cell.y);
		}
		public ArrayList<Map_Cell> getNeighbours(int x, int y){
			ArrayList<Map_Cell> result = new ArrayList<>();
			
			if(!loaded)
				return result;
			
			if(x < 0 || x >= cols) 
				throw new IndexOutOfBoundsException("Index x = "+ x +" in neighbour search function is out of bounds!");
			if(y < 0 || y >= rows) 
				throw new IndexOutOfBoundsException("Index y = "+ y +" in neighbour search function is out of bounds!");		
			
			
			//up
			if(y != 0)
				if(getCell(x,y-1).value != wall)
					result.add(getCell(x,y-1));
			//down
			if(y != rows-1)
				if(getCell(x,y+1).value != wall)
					result.add(getCell(x,y+1));
			//left
			if(x != 0)
				if(getCell(x-1,y).value != wall)
					result.add(getCell(x-1,y));
			//right
			if(x != cols-1)
				if(getCell(x+1,y).value != wall)
					result.add(getCell(x+1,y));
			
			
			return result;
		}
		
		public boolean flushDataFromCells(){
			if(loaded)
				for(int c = 0; c < cols; c++)
					for(int r = 0; r < rows; r++){
						Map_Cell cell = getCell(c,r);
	
						cell.visited = false;
						cell.previous = null;
						cell.effort = Integer.MAX_VALUE;
						cell.distance = Integer.MAX_VALUE;
					}
			return loaded;
		}
		
		public boolean load_map_blocks(byte[][] data ){
			if(!valid_map_size(data))
				return false;
			for(int i = 0; i < rows; i++){
				for(int j = 0; j < cols; j++){
					datamap[i][j].value = data[i][j];
				}
			}
			loaded = true;
			return true;
		}
		public boolean display_map_console(int option){
			if(loaded)
			{
				switch(option){
					case 1: System.out.print("x");  break;
					case 2: System.out.print("y");  break;
					case 3: System.out.print("walls");  break;
					case 4: System.out.print("visited");  break;
					case 5: System.out.print("effort");  break;
					case 6: System.out.print("distance");  break;
					default: System.out.print("block");  break;
					
				}

				for(int i = 0; i < rows; i++){
					System.out.print("\n[ ");
					for(int j = 0; j < cols; j++){
						switch(option){
						case 1: System.out.print(datamap[i][j].x + " "); break;
						case 2: System.out.print(datamap[i][j].y + " "); break;
						case 3: 
							if(datamap[i][j].value == wall)
								System.out.print("#"); 
							else
								System.out.print(" "); 
							break;
						case 4: System.out.print(datamap[i][j].visited + " "); break;
						case 5: System.out.print(datamap[i][j].effort + " "); break;
						case 6: System.out.print(datamap[i][j].distance + " "); break;
						default: System.out.print(datamap[i][j].value + " "); break;
						}
						System.out.print(" ");
					}
					System.out.print("]");
				}
				System.out.println();
			}
			return loaded;
		}
	}


	/*
	 * 	Distance functions
	 */	
	//Map Cell
	public static double euclidean_distance(Map_Cell origin, Map_Cell destination){
		return Math.sqrt( (destination.y-origin.y)*(destination.y-origin.y) + (destination.x-origin.x)*(destination.x-origin.x) );	
	}
	public static int manhattan_distance(Map_Cell origin, Map_Cell destination){
		return (Math.abs(destination.x - origin.x) + Math.abs(destination.y - origin.y));	
	}
	
	//Taxi to Passenger
	public static double euclidean_distance(Taxi origin, Passenger destination){
		return Math.sqrt( (destination.getYiCoord()-origin.getYCoord())*(destination.getYiCoord()-origin.getYCoord()) + (destination.getXiCoord()-origin.getXCoord())*(destination.getXiCoord()-origin.getXCoord()) );	
	}
	public static int manhattan_distance(Taxi origin, Passenger destination){
		return (Math.abs(destination.getXiCoord() - origin.getXCoord()) + Math.abs(destination.getYiCoord() - origin.getYCoord()));	
	}

	/*
	 * 	Path Finding functions
	 */
	public static void getShortestPathDijkstra(int or_x, int or_y, int dest_x, int dest_y, Path_Finding_Map Map){
		
		//Create Queue
		PriorityQueue<Map_Cell> Q = new  PriorityQueue<Map_Cell>();
		
		//Flush Data from previous searches
		Map.flushDataFromCells();
		
		//Set origin Distance to 0
		Map_Cell origin = Map.getCell(or_x, or_y);
		origin.distance = 0;
		
		//Add origin to queue
		Q.add(origin);
		
		//Iterate through the Queue
		while(!Q.isEmpty()){
			Map_Cell cell = Q.remove(); //Remove top cell from Queue
			cell.visited = true;
			
			if(_DEBUG)System.out.println("From cell (" + cell.x + "," + cell.y + ")...");
			
			
			if(cell.x == dest_x && cell.y == dest_y) //if cell is the destination
			{
				if(_DEBUG)System.out.println("We found the destination!\nLeftover Queue size: "+Q.size());
				
				return; //Function to retrieve a path from destination goes here
				
			}
			
			//otherwise, check the neighbour cells
			ArrayList<Map_Cell> closest = Map.getNeighbours(cell);
			for(Map_Cell neighbour : closest){
				
				if(_DEBUG)System.out.println("Checked: (" + neighbour.x + "," + neighbour.y + ")");
				
				//Consider their new distance from source to current's + 1
				int new_distance = cell.distance + 1;
				
				// But only update it if this is the shortest known path to the neighbour
				// And if it is... 
				if(neighbour.distance > new_distance )
				{
					neighbour.distance = new_distance;
					neighbour.previous = cell; //...set the current cell as the previous step in the path
					Q.add(neighbour); //... and send the neighbour to the queue  
					
					if(_DEBUG)System.out.println("Added to queue!");
				}
			}
			
			
		}
		
		return;
	}
	public static void getShortestPathDijkstra(Taxi origin, Passenger destination, Path_Finding_Map Map){
		//Run
		//??? ? = getShortestPathDijkstra(origin.getXCoord(), origin.getYCoord(), destination.getXiCoord(), destination.getYiCoord(), Map);
		
		return;
	}
	public static void getShortestPathDijkstra(Map_Cell origin, Map_Cell destination, Path_Finding_Map Map){
		//Run
		//??? ? = getShortestPathDijkstra(origin.x, origin.y, destination.x, destination.y, Map);
		
		return;
	}
}


