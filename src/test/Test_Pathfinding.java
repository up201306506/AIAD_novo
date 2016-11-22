package test;

import static org.junit.Assert.*;

import java.util.PriorityQueue;

import org.junit.Test;

import tools.Pathfinding;
import tools.Pathfinding.*;

public class Test_Pathfinding {

	
	@Test
	public void test_Mapcell_class() {
		
		for(int i = 0; i < 50; i++){
			for(int j = 0; j <50; j++ ){
				Map_Cell TestCell = new Map_Cell(i,j);
				assert(TestCell.x == i);
				assert(TestCell.y == j);
			}
		}
		
		PriorityQueue<Map_Cell> q = new  PriorityQueue<Map_Cell>();
		Map_Cell m1 = new Map_Cell(0, 0); m1.distance = 4;
		Map_Cell m2 = new Map_Cell(0, 0); m2.distance = 3;
		Map_Cell m3 = new Map_Cell(0, 0); m3.distance = 9;
		Map_Cell m4 = new Map_Cell(0, 0); m4.distance = 9;
		Map_Cell m5 = new Map_Cell(0, 0); m5.distance = 10;
		Map_Cell m6 = new Map_Cell(0, 0); m6.distance = 1000;
		q.add(m1); q.add(m2); q.add(m3); 
		q.add(m4); q.add(m5); q.add(m6); 
		int i = 0;
		while(!q.isEmpty()){
			int current = q.remove().distance;
			assert(current >= i);
			if(current > i)
				i = current;
		}
	}
	
	@Test
	public void test_Path_finding_class() {
		

		Path_Finding_Map TestMap = new Path_Finding_Map(4, 3, (byte) 0);

		byte[][] test_datamap = {{1,1,1,1},{1,0,0,1},{1,1,1,1}};
		byte[][] test_datamap_wrong_cols = {{1,1,1},{1,1,1},{1,1,1}};
		byte[][] test_datamap_wrong_rows = {{1,1,1,1},{1,1,1,1}};
		
		//Coordinate Concordance
		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 4; j++){
				assertEquals(TestMap.datamap[i][j].y, i);
				assertEquals(TestMap.datamap[i][j].x, j);
			}
		for(int c = 0; c < 4; c++)
			for(int r = 0; r < 3; r++){
				assertEquals(TestMap.getCell(c,r).x, c);
				assertEquals(TestMap.getCell(c,r).y, r);
			}
		
		//Failsafing/Showing loaded map
		assertFalse(TestMap.load_map_blocks(test_datamap_wrong_rows));
		assertFalse(TestMap.display_map_console(0));
		assertFalse(TestMap.load_map_blocks(test_datamap_wrong_cols));
		assertFalse(TestMap.display_map_console(0));
		
		assertTrue(TestMap.load_map_blocks(test_datamap));
		
		assertTrue(TestMap.display_map_console(1));
		assertTrue(TestMap.display_map_console(2));
		assertTrue(TestMap.display_map_console(0));
		assertTrue(TestMap.display_map_console(3));
		
		//Neighbour Cell Retrieval
			//Empty map
		byte[][] test_datamap2 = {{1,1,1,1,1,1,1,1,1,1},{1,1,1,1,1,1,1,1,1,1},{1,1,1,1,1,1,1,1,1,1},{1,1,1,1,1,1,1,1,1,1}};
		TestMap = new Path_Finding_Map(10, 4, test_datamap2, (byte) 0);
		/*
		TestMap.display_map_console(0);
		System.out.println(TestMap.getNeighbours(0,0).size());
		System.out.println(TestMap.getNeighbours(9,0).size());
		System.out.println(TestMap.getNeighbours(0,3).size());
		System.out.println(TestMap.getNeighbours(9,3).size());
		*/
		for(int c = 0; c < 10; c++)
			for(int r = 0; r < 4; r++){
				if(c == 0 || c == 10-1)
					if(r == 0 || r == 4-1)			
						assertEquals(TestMap.getNeighbours(c,r).size(), 2);
					else
						assertEquals(TestMap.getNeighbours(c,r).size(), 3);
				else
					if(r == 0 || r == 4-1)				
						assertEquals(TestMap.getNeighbours(c,r).size(), 3);
					else
						assertEquals(TestMap.getNeighbours(c,r).size(), 4);
						
			}
			//Pathway of possible scenarios Map
		byte[][] test_datamap3 = {{0,0,0,1,0},{1,1,1,1,1},{0,0,1,1,0}};
		TestMap = new Path_Finding_Map(5, 3, test_datamap3, (byte) 0);
		for(int i = 0; i < 5; i++){
			switch(i){
			case 0: assertEquals(TestMap.getNeighbours(i,1).size(), 1); break;
			case 1: assertEquals(TestMap.getNeighbours(i,1).size(), 2); break;
			case 2: assertEquals(TestMap.getNeighbours(i,1).size(), 3); break;
			case 3: assertEquals(TestMap.getNeighbours(i,1).size(), 4); break;
			case 4: assertEquals(TestMap.getNeighbours(i,1).size(), 1); break;
			default: break;
			}
		}
		
		
		
	}

	@Test
	public void test_Distance_Functions() {
		byte[][] test_datamap = {{1,1,1,1},{1,0,0,1},{1,0,0,1},{1,1,1,1}};
		Path_Finding_Map TestMap = new Path_Finding_Map(4, 4, test_datamap, (byte) 0);
		
		
		assertEquals(Pathfinding.euclidean_distance(TestMap.datamap[1][1], TestMap.datamap[2][2]), Math.sqrt(2), 0);
		assertEquals(Pathfinding.manhattan_distance(TestMap.datamap[1][1], TestMap.datamap[2][2]), 2, 0);
		
		//Checking if implementations are equal
		/* 
		for(int i = 0; i < 4; i++)
			for(int j = 0; j < 4; j++){
				
				Taxi testT = new Taxi();
				Passenger testP = new Passenger();
				
				assertEquals(Pathfinding.euclidean_distance(TestMap.datamap[1][1], TestMap.datamap[2][2]), 
								Pathfinding.euclidean_distance(testT, testP) );
				assertEquals(Pathfinding.manhattan_distance(TestMap.datamap[1][1], TestMap.datamap[2][2]), 
								Pathfinding.manhattan_distance(testT, testP));
			}
		*/
	}
	
	@Test
	public void test_Path_Functions() {

		
		
		
	}

	
	@Test(expected=IndexOutOfBoundsException.class)
	public void test_IndexOutofBounds_Mapcell() {
		Map_Cell TestCell = new Map_Cell(-1,0);
		if (TestCell.x == -1)
			assertTrue(true);
	}
	@Test(expected=IndexOutOfBoundsException.class)
	public void test_IndexOutofBounds_Neighbours_x() {
		byte[][] test_datamap = {{1,1,1,1},{1,1,1,1},{1,1,1,1}};
		Path_Finding_Map TestMap = new Path_Finding_Map(4, 3, test_datamap, (byte) 0);
		TestMap.getNeighbours(-1, 0);
		assert(true);
	}
	@Test(expected=IndexOutOfBoundsException.class)
	public void test_IndexOutofBounds_Neighbours_y() {
		byte[][] test_datamap = {{1,1,1,1},{1,1,1,1},{1,1,1,1}};
		Path_Finding_Map TestMap = new Path_Finding_Map(4, 3, test_datamap, (byte) 0);
		TestMap.getNeighbours(1, 5);
		assert(true);
	}
}
