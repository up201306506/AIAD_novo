package application;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jade.core.AID;

public class Passenger {

	// Passenger info
	private AID passengerAID;
	private int xiCoord; // Initial coords
	private int yiCoord;
	private int xfCoord; // Destination coords
	private int yfCoord;
	private int num; // Number of passengers

	// Constructor
	public Passenger(AID passengerAID, String info){
		this.passengerAID = passengerAID;

		// Temporary values
		this.xiCoord = -1;
		this.yiCoord = -1;
		this.xfCoord = -1;
		this.yfCoord = -1;
		this.num = -1;

		// Regex to read the content of the info
		Pattern p = Pattern.compile("[a-zA-Z][a-zA-Z]\\d+");
		Matcher m = p.matcher(info);

		try{
			while(m.find()){
				switch(m.group().substring(0, 2)){
				case "XI":
					xiCoord = Integer.parseInt(m.group().substring(2));
					break;
				case "YI":
					yiCoord = Integer.parseInt(m.group().substring(2));
					break;
				case "XF":
					xfCoord = Integer.parseInt(m.group().substring(2));
					break;
				case "YF":
					yfCoord = Integer.parseInt(m.group().substring(2));
					break;
				case "NP":
					num = Integer.parseInt(m.group().substring(2));
					break;
				default:
					throw new Exception("String not recognized: " + m.group());
				}
			}

			if(xiCoord == -1 || yiCoord == -1 || xfCoord == -1 || yfCoord == -1 || num == -1)
				throw new Exception("A variable was not initialized");

		} catch(Exception e){
			System.err.println(e.getMessage());
		}
	}

	// Getters and setters
	public AID getPassengerAID(){
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

	public int getNumberOfPassengers(){
		return num;
	}
}