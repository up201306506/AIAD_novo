package application;

import jade.core.AID;

public class Taxi {

	// Taxi info
	private AID aid;
	private int xCoord;
	private int yCoord;
	private int capacity;

	// Constructor
	public Taxi(AID aid, int xCoord, int yCoord, int capacity){
		this.aid = aid;
		this.xCoord = xCoord;
		this.yCoord = yCoord;
		this.capacity = capacity;
	}

	// Getters and setters
	public int getXCoord(){
		return xCoord;
	}

	public int getYCoord(){
		return yCoord;
	}
}