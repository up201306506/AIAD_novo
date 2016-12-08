package tests;

import java.util.ArrayList;

import jade.core.Agent;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;

public class Test_NoSharing_2Taxi4_1Passenger8 extends Agent{
	private static final long serialVersionUID = -1180045478046218401L;

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

			// Taxi1
			String taxi1ArgsStr = "4,0,4";
			Object[] taxi1Args = taxi1ArgsStr.split(",");
			AgentController taxi1 = cc.createNewAgent("taxi1", "agents.TaxiAgent", taxi1Args);
			taxi1.start();
			agents.add(taxi1);

			// Taxi2
			String taxi2ArgsStr = "4,16,4";
			Object[] taxi2Args = taxi2ArgsStr.split(",");
			AgentController taxi2 = cc.createNewAgent("taxi2", "agents.TaxiAgent", taxi2Args);
			taxi2.start();
			agents.add(taxi2);

			sleep(0.7);

			// Passenger
			String passengerArgsStr = "10,3,18,14,8";
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
				if(ac != null)
					ac.kill();
			} catch (StaleProxyException e) {
				e.printStackTrace();
			} catch (ControllerException e) {
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