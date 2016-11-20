package application;

import jade.core.AID;

public class TaxiDistance implements Comparable<TaxiDistance>{

	// Variables
	private AID taxiAID;
	private int distance;

	// Constructor
	public TaxiDistance(AID taxiAID, int distance){
		this.taxiAID = taxiAID;
		this.distance = distance;
	}

	// Getters and setters
	public AID getTaxiAID(){
		return taxiAID;
	}

	public int getDistance(){
		return distance;
	}

	// Overrides
	@Override
	public boolean equals(Object obj) {
		if(obj == this) return true;

		if(!(obj instanceof TaxiDistance)) return false;

		TaxiDistance that = (TaxiDistance) obj;
		if(this.taxiAID.equals(that.taxiAID)
				&& this.distance == that.distance) return true;

		return false;
	}

	@Override
	public int compareTo(TaxiDistance that) {
		return Integer.valueOf(this.distance).compareTo(that.distance);
	}
}