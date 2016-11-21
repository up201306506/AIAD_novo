package application;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jade.core.AID;

public class Taxi {

	// Taxi info
	private AID taxiAID;
	private int xCoord;
	private int yCoord;
	private int capacity;

	// Constructor
	public Taxi(AID taxiAID, String info){
		this.taxiAID = taxiAID;

		// Temporary values
		this.xCoord = -1;
		this.yCoord = -1;
		this.capacity = -1;

		// Regex to read the content of the info
		Pattern p = Pattern.compile("[a-zA-Z]\\d+");
		Matcher m = p.matcher(info);

		try{
			while(m.find()){
				switch(m.group().charAt(0)){
				case 'X':
					this.xCoord = Integer.parseInt(m.group().substring(1));
					break;
				case 'Y':
					this.yCoord = Integer.parseInt(m.group().substring(1));
					break;
				case 'C':
					this.capacity = Integer.parseInt(m.group().substring(1));
					break;
				default:
					throw new Exception("String not recognized");
				}
			}

			if(this.xCoord == -1 || this.yCoord == -1 || this.capacity == -1)
				throw new Exception("A variable was not initialized");

		} catch(Exception e){
			System.err.println(e.getMessage());
		}
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
}