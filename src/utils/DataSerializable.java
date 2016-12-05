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

		private boolean wasPickedUp;

		// Station process conditions
		private boolean isSharingPolicy;
		private boolean isDiminishingDuration;

		// Constructor
		public PassengerData(AID passengerAID, Cell startingCell, Cell endingCell, int numberOfPassengers){
			this.passengerAID = passengerAID;
			this.startingCell = startingCell;
			this.endingCell = endingCell;
			this.numberOfPassengers = numberOfPassengers;

			this.wasPickedUp = false;
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

		public int getNumberOfPassengers() {
			return numberOfPassengers;
		}

		public boolean wasPickedUp(){
			return wasPickedUp;
		}

		public boolean isSharingPolicy(){
			return isSharingPolicy;
		}

		public boolean isDiminishingDuration(){
			return isDiminishingDuration;
		}

		public void setNumberOfPassengers(int numberOfPassengersTravelled){
			this.numberOfPassengers = numberOfPassengersTravelled;
		}

		public void flagPickUp(){
			this.wasPickedUp = true;
		}

		public void setRequestBooleans(boolean isSharingPolicy, boolean isDiminishingDuration){
			this.isSharingPolicy = isSharingPolicy;
			this.isDiminishingDuration = isDiminishingDuration;
		}

		// Overrides
		@Override
		public boolean equals(Object obj) {
			if(obj == this) return true;

			if(!(obj instanceof PassengerData)) return false;

			PassengerData that = (PassengerData) obj;
			if(this.passengerAID.equals(that.passengerAID)) return true;

			return false;
		}

		@Override
		public int hashCode() {
			return passengerAID.hashCode();
		}
	}

	// Taxi serializable class
	public static class TaxiData implements Serializable {
		private static final long serialVersionUID = 6136110741124038826L;

		// Variables
		private AID taxiAID;
		private Cell positionCell;
		private int capacity;

		// Constructor
		public TaxiData(AID taxiAID, Cell positionCell, int capacity){
			this.taxiAID = taxiAID;
			this.positionCell = positionCell;
			this.capacity = capacity;
		}

		// Getters and setters
		public AID getAID(){
			return taxiAID;
		}

		public Cell getPosition(){
			return positionCell;
		}

		public int getCapacity(){
			return capacity;
		}

		// Overrides
		@Override
		public boolean equals(Object obj) {
			if(obj == this) return true;

			if(!(obj instanceof TaxiData)) return false;

			TaxiData that = (TaxiData) obj;
			if(this.taxiAID.equals(that.taxiAID)) return true;

			return false;
		}

		@Override
		public int hashCode() {
			return taxiAID.hashCode();
		}
	}
}