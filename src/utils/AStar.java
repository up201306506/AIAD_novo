package utils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Stack;

import utils.Cell.CellValue;

public class AStar {

	// Static functions
	// Calculates the cost from the origin cell to a cell
	private static int CalculateCost(CellValue current, Cell next, boolean isDiminishingDuration){
		int cost;
		if(isDiminishingDuration)
			cost = current.getCell().getDuration();
		else
			cost = 1;

		return cost;
	}

	// Calculates an estimate from a cell to the objective cell
	private static int CalculateEstimateCost(Cell next, Cell endingCell, boolean isDiminishingDuration){
		int euclideanDistance = (int)
				Math.round(
						Math.floor(
								Math.sqrt(
										(next.getRow() - endingCell.getRow()) * (next.getRow() - endingCell.getRow())
										+ (next.getCol() - endingCell.getCol()) * (next.getCol() - endingCell.getCol()))));

		int estimateCost;
		if(isDiminishingDuration)
			estimateCost = euclideanDistance * 500;
		else
			estimateCost = euclideanDistance;

		return estimateCost;
	}

	// Returns path in a list of cells
	private static LinkedList<Cell> RetrievePath(Cell startingCell, Cell endingCell, HashMap<Cell, Cell> cameFrom){
		// Temporary use of stack to reverse hashmap
		Stack<Cell> stack = new Stack<>();
		// The ending cell is the bottom of the stack
		stack.push(endingCell);

		Cell originalCell;
		do{
			// Grabs the top of the stack
			originalCell = stack.peek();
			// Retrieves the cell it came from
			stack.push(cameFrom.get(originalCell));
		}while(!originalCell.equals(startingCell));

		// Since there are two starting cells in the stack
		stack.pop();

		// List of cells, path
		LinkedList<Cell> path = new LinkedList<>();

		// Sequences the cell order
		while(!stack.isEmpty())
			path.add(stack.pop());

		return path;
	}

	// Executes AStar algorithm
	public static LinkedList<Cell> AStarAlgorithm(HashMap<Cell, Cell> cellMap, Cell startingCell, Cell endingCell, boolean isDiminishingDuration){

		// Set of cells to be evaluated
		PriorityQueue<CellValue> frontier = new PriorityQueue<>();
		// Starting cell is the first to be evaluated
		frontier.add(new CellValue(startingCell, 0));

		// Mapping each cell to a predecessor
		HashMap<Cell, Cell> cameFrom = new HashMap<>();
		// Starting cell has no predecessor
		cameFrom.put(startingCell, startingCell);

		// Mapping each cell to a score
		HashMap<Cell, CellValue> costSoFar = new HashMap<>();
		costSoFar.put(startingCell, new CellValue(startingCell, 0));

		// While there are still cells to be evaluated
		while(!frontier.isEmpty()){
			// Evaluates the cell with the lowest score
			CellValue current = frontier.remove();

			// If current cell is the end cell, a path has been found
			if(current.getCell().equals(endingCell))
				return RetrievePath(cellMap.get(startingCell), cellMap.get(endingCell), cameFrom);

			// Evaluates all neighbour cells
			for(Cell neighbour : Cell.getNeighbourCells(cellMap, current.getCell())){
				if(neighbour.isWall()) continue;

				// Calculates the cost of traveling from the starting cell to this cell
				int newCost = costSoFar.get(current.getCell()).getScore() + CalculateCost(current, neighbour, isDiminishingDuration);

				// If the cell has not been already evaluated
				if(!costSoFar.containsKey(neighbour)
						// If the cost to travel to neighbour cell is less than the value stored
						|| (newCost < costSoFar.get(neighbour).getScore())){

					// Update the cost of traveling to this cell
					if(costSoFar.containsKey(neighbour))
						costSoFar.remove(neighbour);
					costSoFar.put(neighbour, new CellValue(neighbour, newCost));

					// Adds the neighbour cell to the list of nodes to be evaluated
					frontier.add(new CellValue(neighbour, newCost + CalculateEstimateCost(neighbour, endingCell, isDiminishingDuration)));

					// Updates the predecessor cell to this neighbour
					if(cameFrom.containsKey(neighbour))
						cameFrom.remove(neighbour);
					cameFrom.put(neighbour, current.getCell());
				}
			}
		}

		// If no path was found
		return null;
	}

	// Calculates the distance of the path
	public static int PathDistance(LinkedList<Cell> path){
		return path.size();
	}

	// Calculates the duration of the path
	public static int PathDuration(LinkedList<Cell> path){
		int duration = 0;

		for(int i = 0; i < path.size() - 1; i++)
			duration += path.get(i).getDuration();

		return duration;
	}

	// Retrieves stack of moving orders
	public static Stack<Cell> GetMoveOrders(LinkedList<Cell> path){
		Stack<Cell> moveOrders = new Stack<>();

		for(int i = path.size() - 1; i > 0; i--)
			moveOrders.push(path.get(i));

		return moveOrders;
	}

	// Adds to stack of cells
	public static Stack<Cell> addStack(Stack<Cell> original, Stack<Cell> toAdd){
		Object[] toAddArr = toAdd.toArray();

		for(Object cell : toAddArr)
			original.push((Cell) cell);

		return original;
	}
}