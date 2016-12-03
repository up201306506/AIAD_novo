package utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class Cell implements Serializable {
	private static final long serialVersionUID = -2826796187927123789L;

	// Variables
	private int row;
	private int col;
	private int duration;
	private boolean isWall;

	// Constructor
	public Cell(int row, int col, int duration, boolean isWall){
		this.row = row;
		this.col = col;
		this.duration = duration;
		this.isWall = isWall;
	}

	// Getters and setters
	public int getRow(){
		return row;
	}

	public int getCol(){
		return col;
	}

	public int getDuration(){
		return duration;
	}

	public boolean isWall(){
		return isWall;
	}

	// Functions
	public boolean isAdjacent(Cell that){
		if(this.row == that.row - 1
				&& this.col == that.col)
			return true;

		if(this.row == that.row
				&& this.col == that.col + 1)
			return true;

		if(this.row == that.row + 1
				&& this.col == that.col)
			return true;

		if(this.row == that.row
				&& this.col == that.col - 1)
			return true;

		return false;
	}

	// Overrides
	@Override
	public String toString() {
		return (row + " - " + col);
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) return true;

		if(!(obj instanceof Cell)) return false;

		Cell that = (Cell) obj;
		if(this.row == that.row
				&& this.col == that.col) return true;

		return false;
	}

	@Override
	public int hashCode() {
		int hash = 3;
        hash = 53 * hash + Integer.hashCode(row);
        hash = 53 * hash + Integer.hashCode(col);
        return hash;
	}

	// Static functions
	public static ArrayList<Cell> getNeighbourCells(HashMap<Cell, Cell> cellMap, Cell currentCell){
		ArrayList<Cell> neighbourCells = new ArrayList<>();

		Cell upCell = new Cell(currentCell.getRow() - 1, currentCell.getCol(), 0, false);
		Cell rightCell = new Cell(currentCell.getRow(), currentCell.getCol() + 1, 0, false);
		Cell downCell = new Cell(currentCell.getRow() + 1, currentCell.getCol(), 0, false);
		Cell leftCell = new Cell(currentCell.getRow(), currentCell.getCol() - 1, 0, false);

		if(cellMap.containsValue(upCell))
			neighbourCells.add(cellMap.get(upCell));
		if(cellMap.containsValue(rightCell))
			neighbourCells.add(cellMap.get(rightCell));
		if(cellMap.containsValue(downCell))
			neighbourCells.add(cellMap.get(downCell));
		if(cellMap.containsValue(leftCell))
			neighbourCells.add(cellMap.get(leftCell));

		return neighbourCells;
	}

	public static HashMap<Cell, Cell> mapToCellMap(byte[][] map, int[][] durationMap){
		HashMap<Cell, Cell> cellMap = new HashMap<>();

		for(int row = 0; row < map.length; row++){
			for(int col = 0; col < map[row].length; col++){
				Cell cell = new Cell(row, col, durationMap[row][col], (map[row][col] == 0));
				cellMap.put(cell, cell);
			}
		}

		return cellMap;
	}

	// ------------------------------------------------------------
	public static class CellValue implements Comparable<CellValue> {

		// Variables
		private Cell cell;
		private int score;

		// Constructor
		public CellValue(Cell cell, int score){
			this.cell = cell;
			this.score = score;
		}

		// Getters and setters
		public Cell getCell(){
			return cell;
		}

		public int getScore(){
			return score;
		}

		// Overrides
		@Override
		public boolean equals(Object obj) {
			if(obj == this) return true;

			if(!(obj instanceof CellValue)) return false;

			CellValue that = (CellValue) obj;
			if(this.cell.equals(that.cell)) return true;

			return false;
		}

		@Override
		public int compareTo(CellValue that) {
			return Integer.valueOf(this.score).compareTo(that.score);
		}
	}
}