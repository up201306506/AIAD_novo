package test;

import static org.junit.Assert.*;

import java.util.PriorityQueue;

import org.junit.Test;

import tools.Pathfinding;
import tools.Pathfinding.*;
import application.*;

public class Test_Pathfinding {

	@Test(expected=IndexOutOfBoundsException.class)
	public void test_IndexOutofBounds_Mapcell() {
		Map_Cell TestCell = new Map_Cell(-1,0);
		if (TestCell.x == -1)
			assertTrue(true);
	}
	
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
		
		
		//Showing loaded map
		assertFalse(TestMap.load_map_blocks(test_datamap_wrong_rows));
		assertFalse(TestMap.display_map_console(0));
		assertFalse(TestMap.load_map_blocks(test_datamap_wrong_cols));
		assertFalse(TestMap.display_map_console(0));
		
		assertTrue(TestMap.load_map_blocks(test_datamap));
		
		
		assertTrue(TestMap.display_map_console(1));
		assertTrue(TestMap.display_map_console(2));
		assertTrue(TestMap.display_map_console(0));
		

		
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

}
