package tests;

import java.util.ArrayList;

import jade.core.Agent;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class Test_Custom extends Agent{
	private static final long serialVersionUID = -4958331658859600713L;

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
			String taxi2ArgsStr = "38,32,4";
			Object[] taxi2Args = taxi2ArgsStr.split(",");
			AgentController taxi2 = cc.createNewAgent("taxi2", "agents.TaxiAgent", taxi2Args);
			taxi2.start();
			agents.add(taxi2);

			sleep(0.4);

			// Passenger1
			String passenger1ArgsStr = "4,3,4,6,1";
			Object[] passenger1Args = passenger1ArgsStr.split(",");
			AgentController passenger1 = cc.createNewAgent("passenger1", "agents.PassengerAgent", passenger1Args);
			passenger1.start();
			agents.add(passenger1);

			// Passenger2
			String passenger2ArgsStr = "4,7,4,10,1";
			Object[] passenger2Args = passenger2ArgsStr.split(",");
			AgentController passenger2 = cc.createNewAgent("passenger2", "agents.PassengerAgent", passenger2Args);
			passenger2.start();
			agents.add(passenger2);

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