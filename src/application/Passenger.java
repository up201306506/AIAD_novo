package application;

import jade.core.AID;

public class Passenger {

	// Passenger info
	private AID aid;
	private int xiCoord; // initial coords
	private int yiCoord;
	private int xdCoord; // destination coords
	private int ydCoord;
	private int numberOfPassengers;

	// Constructor
	public Passenger(AID aid, int xiCoord, int yiCoord, int xdCoord, int ydCoord, int numberOfPassengers){
		this.aid = aid;
		this.xiCoord = xiCoord;
		this.yiCoord = yiCoord;
		this.xdCoord = xdCoord;
		this.ydCoord = ydCoord;
		this.numberOfPassengers = numberOfPassengers;
	}

	// Getters and setters
	public int getXiCoord(){
		return xiCoord;
	}

	public int getYiCoord(){
		return yiCoord;
	}
}