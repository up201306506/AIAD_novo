package tools;

import application.*;

public class Pathfinding {

	public static class Map_cell{
		public int x = 0;
		public int y = 0;
		public byte value = 0;
		public boolean visited = false;
		public int effort = Integer.MAX_VALUE;
		public int distance = Integer.MAX_VALUE;

		//Constructor
		public Map_cell (int x, int y){
			this.x = x;
			this.y = y;
			if(this.x < 0){
				throw new IndexOutOfBoundsException("Index x = "+ x +" is out of bounds!"); 
			}
			if(this.y < 0){
				throw new IndexOutOfBoundsException("Index y = "+ y +" is out of bounds!"); 
			}
		}
	}

	public static class Path_finding_map{
		public int cols = 0;
		public int rows = 0;
		public Map_cell[][] datamap;
		public boolean loaded = false;

		//Constructor
		public Path_finding_map( int c, int r, byte fill ){
			this.cols = c;
			this.rows = r;
			datamap = new Map_cell[r][c];
			for(int i = 0; i < rows; i++){
				for(int j = 0; j < cols; j++){
					datamap[i][j] = new Map_cell(j, i);
					datamap[i][j].value = fill;
				}
			}
		}
		public Path_finding_map(int c, int r, byte[][] data){
			this(c, r, (byte) 0);
			try{
				if(!load_map_blocks(data))
					throw new IllegalArgumentException();
			}
			catch(IllegalArgumentException e){
				System.err.println("Could not load map data to Pathfinding class, invalid map");
			}
		}

		public boolean valid_map_size(byte[][] data){
			if(rows != data.length)
				return false;
			
			for(int i = 0; i < rows; i++)
				if(cols != data[i].length)
					return false;
						
			return true;
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
					case 3: System.out.print("visited");  break;
					case 4: System.out.print("effort");  break;
					case 5: System.out.print("distance");  break;
					default: System.out.print("block");  break;
					
				}

				for(int i = 0; i < rows; i++){
					System.out.print("\n[ ");
					for(int j = 0; j < cols; j++){
						switch(option){
						case 1: System.out.print(datamap[i][j].x + " "); break;
						case 2: System.out.print(datamap[i][j].y + " "); break;
						case 3: System.out.print(datamap[i][j].visited + " "); break;
						case 4: System.out.print(datamap[i][j].effort + " "); break;
						case 5: System.out.print(datamap[i][j].distance + " "); break;
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
	public static double euclidean_distance(Map_cell origin, Map_cell destination){
		return Math.sqrt( (destination.y-origin.y)*(destination.y-origin.y) + (destination.x-origin.x)*(destination.x-origin.x) );	
	}
	public static int manhattan_distance(Map_cell origin, Map_cell destination){
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
	
	
	//change return type to whatever the path type is
	public static void getPath(Taxi origin, Passenger destination){
		return;
	}
	
}
