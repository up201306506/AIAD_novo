package application;

import java.util.HashMap;
import java.util.LinkedList;

import utils.AStar;
import utils.Cell;

public class Main  {

	public static void main(String[] args) {
		byte[][] map = {
				{1, 0, 0, 0},
				{1, 1, 1, 0},
				{1, 0, 1, 0},
				{1, 1, 1, 0}
		};

		int[][] durationMap = {
				{10, 0, 0, 0},
				{10, 10, 10, 0},
				{9999, 0, 10, 0},
				{10, 10, 10, 0}
		};

		HashMap<Cell, Cell> cellMap = Cell.mapToCellMap(map, durationMap);
		Cell startingCell = new Cell(0, 0, 0, false);
		Cell endingCell = new Cell(3, 0, 0, false);

		LinkedList<Cell> path = AStar.AStarAlgorithm(cellMap, startingCell, endingCell, false);

		if(path == null){
			System.out.println("NULL");
			return;
		}

		for(Cell cell : path){
			System.out.println("" + cell.getRow() + " - " + cell.getCol());
		}
	}
}