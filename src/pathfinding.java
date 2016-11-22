
public class pathfinding {

	class map_cell{
		public int x = 0;
		public int y = 0;
		public byte block = 0;
		public boolean visited = false;
		public int effort = Integer.MAX_VALUE;
		public int distance = Integer.MAX_VALUE;

		public map_cell (int x, int y){
			this.x = x;
			this.y = y;
		}
	}

	class path_finding_map{
		public int cols = 0;
		public int rows = 0;
		public map_cell[][] datamap;

		public path_finding_map( int c, int r ){
			this.cols = c;
			this.rows = r;
			datamap = new map_cell[c][r];
			for(int i = 0; i < cols; i++){
				for(int j = 0; j < rows; j++){
					datamap[i][j].x = i;
					datamap[i][j].y = j;
					datamap[i][j].block = 0;
				}
			}
		}

		public boolean load_map_blocks( int c, int r, byte[][] data ){
			for(int i = 0; i < cols; i++){
				for(int j = 0; j < rows; j++){
					datamap[i][j].block = data[i][j];
				}
			}
			return true;
		}

		public void display_map_console(int option){
			for(int i = 0; i < cols; i++){
				System.out.print("\n[");
				for(int j = 0; j < rows; j++){
					switch(option){
					case 0: System.out.print(datamap[i][j].block);
					case 1: System.out.print(datamap[i][j].x);
					case 2: System.out.print(datamap[i][j].y);
					case 3: System.out.print(datamap[i][j].visited);
					case 4: System.out.print(datamap[i][j].effort);
					case 5: System.out.print(datamap[i][j].distance);
					default: System.out.print(datamap[i][j].block);
					}
					System.out.print(" ");
				}
				System.out.print("]");
			}
		}

	}


	public double euclidean_distance(map_cell origin, map_cell destination){
		return Math.sqrt( (destination.y-origin.y)*(destination.y-origin.y) + (destination.x-origin.x)*(destination.x-origin.x) );
	}

	public int manhattan_distance(map_cell origin, map_cell destination){
		return (Math.abs(destination.x - origin.x) + Math.abs(destination.y - origin.y));
	}
}