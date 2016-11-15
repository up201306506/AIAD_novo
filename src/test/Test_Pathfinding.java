package test;

import static org.junit.Assert.*;

import org.junit.Test;

import tools.Pathfinding.Map_cell;
import tools.Pathfinding.Path_finding_map;

public class Test_Pathfinding {

	@Test(expected=IndexOutOfBoundsException.class)
	public void test_IndexOutofBounds_Mapcell() {
		Map_cell TestCell = new Map_cell(-1,0);
		if (TestCell.x == -1)
			assertTrue(true);
	}
	
	@Test
	public void test_Mapcell_class() {
		
		for(int i = 0; i < 50; i++){
			for(int j = 0; j <50; j++ ){
				Map_cell TestCell = new Map_cell(i,j);
				assert(TestCell.x == i);
				assert(TestCell.y == j);
			}
		}
	}
	
	@Test
	public void test_Path_finding_class() {

		Path_finding_map TestMap = new Path_finding_map(2, 7, (byte) 0);
		
		System.out.print("x"); 
		TestMap.display_map_console(1);
		System.out.print("\ny"); 
		TestMap.display_map_console(2);
		System.out.print("\nValue"); 
		TestMap.display_map_console(0);
		

		byte[][] test_datamap = {{1,1,1,1},{1,1,1,1},{1,1,1,1}};
		
	}

}
