package application;

import java.util.ArrayList;

public class Travel {

	private Taxi taxi;
	private ArrayList<Passenger> passengers;

	public Travel(Taxi taxi, Passenger passenger){
		this.taxi = taxi;
		this.passengers = new ArrayList<>();
		this.passengers.add(passenger);
	}
}