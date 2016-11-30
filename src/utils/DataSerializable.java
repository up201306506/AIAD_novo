package utils;

import java.io.Serializable;

import jade.core.AID;

public class DataSerializable {

	// Passenger serializable class
	public static class PassengerData implements Serializable {
		private static final long serialVersionUID = 6136110741124038826L;

		// Passenger dynamic variables
		private AID passengerAID;
		private int xiCoord;
		private int yiCoord;
		private int xfCoord;
		private int yfCoord;
		private int numberOfPassengers;

		// Request taxis variables
		private boolean isSharingPolicy;
		private boolean isDiminishingDuration;
		private boolean isDiminishingDistance;

		// Constructor
		public PassengerData(AID passengerAID, int xiCoord, int yiCoord, int xfCoord, int yfCoord, int numberOfPassengers){
			this.passengerAID = passengerAID;
			this.xiCoord = xiCoord;
			this.yiCoord = yiCoord;
			this.xfCoord = xfCoord;
			this.yfCoord = yfCoord;
			this.numberOfPassengers = numberOfPassengers;
		}

		// Getters and setters
		public AID getAID(){
			return passengerAID;
		}

		public int getXiCoord(){
			return xiCoord;
		}

		public int getYiCoord(){
			return yiCoord;
		}

		public int getXfCoord(){
			return xfCoord;
		}

		public int getYfCoord(){
			return yfCoord;
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

		public boolean isDiminishingDistance(){
			return isDiminishingDistance;
		}

		public void setRequestBooleans(boolean isSharingPolicy, boolean isDiminishingDuration, boolean isDiminishingDistance){
			this.isSharingPolicy = isSharingPolicy;
			this.isDiminishingDuration = isDiminishingDuration;
			this.isDiminishingDistance = isDiminishingDistance;
		}

		// Aux functions
		@Override
		public String toString() {
			return passengerAID.getLocalName() + " >> State is: " + "Xi - " + xiCoord + "| Yi - " + yiCoord
					+ "Xf - " + xfCoord + "| Yf - " + yfCoord
					+ "| N - " + numberOfPassengers;
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