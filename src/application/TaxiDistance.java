package application;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jade.core.AID;

public class TaxiDistance implements Comparable<TaxiDistance>{

	// Variables
	private AID taxiAID;

	private int maxCapacity;
	private int capacity;
	private int distance;

	// Constructor
	public TaxiDistance(AID taxiAID, String requestContent){
		this.taxiAID = taxiAID;

		// Temporary values
		this.maxCapacity = -1;
		this.capacity = -1;
		this.distance = -1;

		// Regex to read the content of the request
		Pattern p = Pattern.compile("[a-zA-Z]\\d+");
		Matcher m = p.matcher(requestContent);

		try{
			while(m.find()){
				switch(m.group().charAt(0)){
				case 'M':
					this.maxCapacity = Integer.parseInt(m.group().substring(1));
					break;
				case 'C':
					this.capacity = Integer.parseInt(m.group().substring(1));
					break;
				case 'D':
					this.distance = Integer.parseInt(m.group().substring(1));
					break;
				default:
					throw new Exception("String not recognized");
				}
			}

			if(this.maxCapacity == -1 || this.capacity == -1 || this.distance == -1)
				throw new Exception("A variable was not initialized");

		} catch(Exception e){
			System.err.println(e.getMessage());
		}
	}

	// Getters and setters
	public AID getTaxiAID(){
		return taxiAID;
	}

	public int getMaxCapacity(){
		return maxCapacity;
	}

	public int getCapacity(){
		return capacity;
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
				&& this.maxCapacity == that.maxCapacity
				&& this.capacity == that.capacity
				&& this.distance == that.distance) return true;

		return false;
	}

	@Override
	public int compareTo(TaxiDistance that) {
		return Integer.valueOf(this.distance).compareTo(that.distance);
	}
}