package tools;

public class Pathfinding {

	public static class Map_cell{
		public int x = 0;
		public int y = 0;
		public byte block = 0;
		public boolean visited = false;
		public int effort = Integer.MAX_VALUE;
		public int distance = Integer.MAX_VALUE;
		
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
		
		public Path_finding_map( int c, int r, byte fill ){
			this.cols = c;
			this.rows = r;
			datamap = new Map_cell[r][c];
			for(int i = 0; i < rows; i++){
				for(int j = 0; j < cols; j++){
					datamap[i][j] = new Map_cell(j, i);
					datamap[i][j].block = fill;
				}
			}
		}
		
		public boolean valid_map(byte[][] data){
			int r = data.length;
			int c = data[0].length;
			System.out.println(c);
			System.out.println(r);
			return false;
			
		}
		public boolean load_map_blocks( int c, int r, byte[][] data ){
			for(int i = 0; i < rows; i++){
				for(int j = 0; j < cols; j++){
					datamap[i][j].block = data[i][j];
				}
			}
			return true;
		}
		public void display_map_console(int option){
			for(int i = 0; i < rows; i++){
				System.out.print("\n[ ");
				for(int j = 0; j < cols; j++){
					switch(option){
					case 0: System.out.print(datamap[i][j].block + " "); break;
					case 1: System.out.print(datamap[i][j].x + " "); break;
					case 2: System.out.print(datamap[i][j].y + " "); break;
					case 3: System.out.print(datamap[i][j].visited + " "); break;
					case 4: System.out.print(datamap[i][j].effort + " "); break;
					case 5: System.out.print(datamap[i][j].distance + " "); break;
					default: System.out.print(datamap[i][j].block + " "); break;
					}
					System.out.print(" ");
				}
				System.out.print("]");
			}
		}
		
	}
	
	
	public double euclidean_distance(Map_cell origin, Map_cell destination){
		return Math.sqrt( (destination.y-origin.y)*(destination.y-origin.y) + (destination.x-origin.x)*(destination.x-origin.x) );	
	}
	
	public int manhattan_distance(Map_cell origin, Map_cell destination){
		return (Math.abs(destination.x - origin.x) + Math.abs(destination.y - origin.y));	
	}
	
}
