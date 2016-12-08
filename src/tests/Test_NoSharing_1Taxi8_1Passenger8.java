package tests;

import java.util.ArrayList;

import jade.core.Agent;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class Test_NoSharing_1Taxi8_1Passenger8 extends Agent{
	private static final long serialVersionUID = 2372556974535483181L;

	// Holder agents
	private ArrayList<AgentController> agents;

	@Override
	protected void setup() {
		// Initialize variables
		agents = new ArrayList<>();

		// Opens container
		ContainerController cc = getContainerController();

		try {
			// Creates agents
			// Station
			AgentController station = cc.createNewAgent("station", "agents.TaxiStationAgent", null);
			station.start();
			agents.add(station);

			sleep(0.3);

			// Taxi
			String taxiArgsStr = "10,14,8";
			Object[] taxiArgs = taxiArgsStr.split(",");
			AgentController taxi = cc.createNewAgent("taxi", "agents.TaxiAgent", taxiArgs);
			taxi.start();
			agents.add(taxi);

			sleep(0.7);

			// Passenger
			String passengerArgsStr = "14,21,1,24,8";
			Object[] passengerArgs = passengerArgsStr.split(",");
			AgentController passenger = cc.createNewAgent("passenger", "agents.PassengerAgent", passengerArgs);
			passenger.start();
			agents.add(passenger);

		} catch (StaleProxyException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void takeDown() {
		for(AgentController ac : agents){
			try {
				ac.kill();
			} catch (StaleProxyException e) {
				e.printStackTrace();
			}
		}
	}

	private void sleep(double seconds){
		try {
			Thread.sleep((long) (seconds * 1000));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}