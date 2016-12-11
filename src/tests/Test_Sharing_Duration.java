package tests;

import java.util.ArrayList;

import jade.core.Agent;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class Test_Sharing_Duration extends Agent {
	private static final long serialVersionUID = 5899040924275590674L;

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
			String stationArgsStr = "-sharing,-duration";
			Object[] stationArgs = stationArgsStr.split(",");
			AgentController station = cc.createNewAgent("station", "agents.TaxiStationAgent", stationArgs);
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
			String taxi2ArgsStr = "22,3,4";
			Object[] taxi2Args = taxi2ArgsStr.split(",");
			AgentController taxi2 = cc.createNewAgent("taxi2", "agents.TaxiAgent", taxi2Args);
			taxi2.start();
			agents.add(taxi2);

			sleep(0.7);

			// Passenger1
			String passenger1ArgsStr = "4,3,4,6,1";
			Object[] passenger1Args = passenger1ArgsStr.split(",");
			AgentController passenger1 = cc.createNewAgent("passenger1", "agents.PassengerAgent", passenger1Args);
			passenger1.start();
			agents.add(passenger1);

			sleep(0.1);

			// Passenger2
			String passenger2ArgsStr = "4,5,4,9,1";
			Object[] passenger2Args = passenger2ArgsStr.split(",");
			AgentController passenger2 = cc.createNewAgent("passenger2", "agents.PassengerAgent", passenger2Args);
			passenger2.start();
			agents.add(passenger2);

			sleep(0.1);

			// Passenger3
			String passenger3ArgsStr = "24,5,23,25,1";
			Object[] passenger3Args = passenger3ArgsStr.split(",");
			AgentController passenger3 = cc.createNewAgent("passenger3", "agents.PassengerAgent", passenger3Args);
			passenger3.start();
			agents.add(passenger3);

			sleep(0.1);

			// Passenger4
			String passenger4ArgsStr = "26,6,25,10,1";
			Object[] passenger4Args = passenger4ArgsStr.split(",");
			AgentController passenger4 = cc.createNewAgent("passenger4", "agents.PassengerAgent", passenger4Args);
			passenger4.start();
			agents.add(passenger4);

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