package tests;

import java.util.ArrayList;

import jade.core.Agent;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class Test_Sharing_1Taxi4_3Passenger1_Not_Shared extends Agent {
	private static final long serialVersionUID = -1541366564851970486L;

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
			String stationArgsStr = "-sharing,-distance";
			Object[] stationArgs = stationArgsStr.split(",");
			AgentController station = cc.createNewAgent("station", "agents.TaxiStationAgent", stationArgs);
			station.start();
			agents.add(station);

			sleep(0.3);

			// Taxi
			String taxiArgsStr = "4,0,4";
			Object[] taxiArgs = taxiArgsStr.split(",");
			AgentController taxi = cc.createNewAgent("taxi", "agents.TaxiAgent", taxiArgs);
			taxi.start();
			agents.add(taxi);

			sleep(0.7);

			// Passenger1
			String passenger1ArgsStr = "4,6,15,6,1";
			Object[] passenger1Args = passenger1ArgsStr.split(",");
			AgentController passenger1 = cc.createNewAgent("passenger1", "agents.PassengerAgent", passenger1Args);
			passenger1.start();
			agents.add(passenger1);

			sleep(0.2);

			// Passenger2
			String passenger2ArgsStr = "7,6,10,3,1";
			Object[] passenger2Args = passenger2ArgsStr.split(",");
			AgentController passenger2 = cc.createNewAgent("passenger2", "agents.PassengerAgent", passenger2Args);
			passenger2.start();
			agents.add(passenger2);

			sleep(0.2);

			// Passenger3
			String passenger3ArgsStr = "4,17,15,6,1";
			Object[] passenger3Args = passenger3ArgsStr.split(",");
			AgentController passenger3 = cc.createNewAgent("passenger3", "agents.PassengerAgent", passenger3Args);
			passenger3.start();
			agents.add(passenger3);

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