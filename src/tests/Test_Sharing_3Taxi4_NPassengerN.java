package tests;

import java.util.ArrayList;

import jade.core.Agent;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class Test_Sharing_3Taxi4_NPassengerN extends Agent {
	private static final long serialVersionUID = 3120211474419082701L;

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

			// Taxi1
			String taxi1ArgsStr = "4,0,4";
			Object[] taxi1Args = taxi1ArgsStr.split(",");
			AgentController taxi1 = cc.createNewAgent("taxi1", "agents.TaxiAgent", taxi1Args);
			taxi1.start();
			agents.add(taxi1);

			// Taxi2
			String taxi2ArgsStr = "18,27,4";
			Object[] taxi2Args = taxi2ArgsStr.split(",");
			AgentController taxi2 = cc.createNewAgent("taxi2", "agents.TaxiAgent", taxi2Args);
			taxi2.start();
			agents.add(taxi2);

			// Taxi3
			String taxi3ArgsStr = "4,29,4";
			Object[] taxi3Args = taxi3ArgsStr.split(",");
			AgentController taxi3 = cc.createNewAgent("taxi3", "agents.TaxiAgent", taxi3Args);
			taxi3.start();
			agents.add(taxi3);

			sleep(0.7);

			// Passenger1
			String passenger1ArgsStr = "10,3,18,14,8";
			Object[] passenger1Args = passenger1ArgsStr.split(",");
			AgentController passenger1 = cc.createNewAgent("passenger1", "agents.PassengerAgent", passenger1Args);
			passenger1.start();
			agents.add(passenger1);

			sleep(0.2);

			// Passenger2
			String passenger2ArgsStr = "14,12,0,6,3";
			Object[] passenger2Args = passenger2ArgsStr.split(",");
			AgentController passenger2 = cc.createNewAgent("passenger2", "agents.PassengerAgent", passenger2Args);
			passenger2.start();
			agents.add(passenger2);

			sleep(0.2);

			// Passenger3
			String passenger3ArgsStr = "35,34,30,37,1";
			Object[] passenger3Args = passenger3ArgsStr.split(",");
			AgentController passenger3 = cc.createNewAgent("passenger3", "agents.PassengerAgent", passenger3Args);
			passenger3.start();
			agents.add(passenger3);

			sleep(30);

			// Passenger4
			String passenger4ArgsStr = "37,12,23,18,5";
			Object[] passenger4Args = passenger4ArgsStr.split(",");
			AgentController passenger4 = cc.createNewAgent("passenger4", "agents.PassengerAgent", passenger4Args);
			passenger4.start();
			agents.add(passenger4);

			sleep(0.2);

			// Passenger5
			String passenger5ArgsStr = "2,32,30,2,2";
			Object[] passenger5Args = passenger5ArgsStr.split(",");
			AgentController passenger5 = cc.createNewAgent("passenger5", "agents.PassengerAgent", passenger5Args);
			passenger5.start();
			agents.add(passenger5);

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