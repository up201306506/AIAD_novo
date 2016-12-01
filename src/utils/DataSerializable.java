package utils;

import java.io.Serializable;

import jade.core.AID;

public class DataSerializable {

	// Passenger serializable class
	public static class PassengerData implements Serializable {
		private static final long serialVersionUID = 6136110741124038826L;

		// Variables
		private AID passengerAID;
		private Cell startingCell;
		private Cell endingCell;
		private int numberOfPassengers;

		// Station process conditions
		private boolean isSharingPolicy;
		private boolean isDiminishingDuration;

		// Constructor
		public PassengerData(AID passengerAID, Cell startingCell, Cell endingCell, int numberOfPassengers){
			this.passengerAID = passengerAID;
			this.startingCell = startingCell;
			this.endingCell = endingCell;
			this.numberOfPassengers = numberOfPassengers;
		}

		// Getters and setters
		public AID getAID(){
			return passengerAID;
		}

		public Cell getStartingCell(){
			return startingCell;
		}

		public Cell getEndingCell(){
			return endingCell;
		}

		public int getNumberOfPassenger() {
			return numberOfPassengers;
		}

		public boolean isSharingPolicy(){
			return isSharingPolicy;
		}

		public boolean isDiminishingDuration(){
			return isDiminishingDuration;
		}

		public void setRequestBooleans(boolean isSharingPolicy, boolean isDiminishingDuration){
			this.isSharingPolicy = isSharingPolicy;
			this.isDiminishingDuration = isDiminishingDuration;
		}
	}











	// Taxi serializable class
	public static class TaxiData implements Serializable {
		private static final long serialVersionUID = 6136110741124038826L;

		// Taxi dynamic variables
		private AID taxiAID;
		private int xCoord;
		private int yCoord;
		private int capacity;
		private int maxCapacity;

		// Constructor
		public TaxiData(AID taxiAID, int xCoord, int yCoord, int capacity, int maxCapacity){
			this.taxiAID = taxiAID;
			this.xCoord = xCoord;
			this.yCoord = yCoord;
			this.capacity = capacity;
			this.maxCapacity = maxCapacity;
		}

		// Getters and setters
		public AID getAID(){
			return taxiAID;
		}

		public int getXCoord(){
			return xCoord;
		}

		public int getYCoord(){
			return yCoord;
		}

		public int getCapacity(){
			return capacity;
		}

		public int getMaxCapacity(){
			return maxCapacity;
		}

		// Aux functions
		@Override
		public String toString() {
			return taxiAID.getLocalName() + " >> State is: " + "X - " + xCoord + "| Y - " + yCoord
					+ "| C - " + capacity + "| MC - " + maxCapacity;
		}
	}
}