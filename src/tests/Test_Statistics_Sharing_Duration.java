package tests;

import java.util.ArrayList;

import jade.core.Agent;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class Test_Statistics_Sharing_Duration extends Agent {
	private static final long serialVersionUID = -6326670146919069695L;

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
			AgentController station = cc.createNewAgent("station-sharing-duration", "agents.TaxiStationAgent", stationArgs);
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
			String taxi2ArgsStr = "19,1,4";
			Object[] taxi2Args = taxi2ArgsStr.split(",");
			AgentController taxi2 = cc.createNewAgent("taxi2", "agents.TaxiAgent", taxi2Args);
			taxi2.start();
			agents.add(taxi2);

			// Taxi3
			String taxi3ArgsStr = "0,32,4";
			Object[] taxi3Args = taxi3ArgsStr.split(",");
			AgentController taxi3 = cc.createNewAgent("taxi3", "agents.TaxiAgent", taxi3Args);
			taxi3.start();
			agents.add(taxi3);

			// Taxi4
			String taxi4ArgsStr = "31,4,4";
			Object[] taxi4Args = taxi4ArgsStr.split(",");
			AgentController taxi4 = cc.createNewAgent("taxi4", "agents.TaxiAgent", taxi4Args);
			taxi4.start();
			agents.add(taxi4);

			// Taxi5
			String taxi5ArgsStr = "23,17,4";
			Object[] taxi5Args = taxi5ArgsStr.split(",");
			AgentController taxi5 = cc.createNewAgent("taxi5", "agents.TaxiAgent", taxi5Args);
			taxi5.start();
			agents.add(taxi5);

			sleep(0.7);

			// Passenger1
			String passenger1ArgsStr = "4,6,4,11,1";
			Object[] passenger1Args = passenger1ArgsStr.split(",");
			AgentController passenger1 = cc.createNewAgent("passenger1", "agents.PassengerAgent", passenger1Args);
			passenger1.start();
			agents.add(passenger1);

			sleep(0.1);

			// Passenger2
			String passenger2ArgsStr = "4,8,4,10,1";
			Object[] passenger2Args = passenger2ArgsStr.split(",");
			AgentController passenger2 = cc.createNewAgent("passenger2", "agents.PassengerAgent", passenger2Args);
			passenger2.start();
			agents.add(passenger2);

			sleep(0.1);

			// Passenger3
			String passenger3ArgsStr = "4,32,4,28,5";
			Object[] passenger3Args = passenger3ArgsStr.split(",");
			AgentController passenger3 = cc.createNewAgent("passenger3", "agents.PassengerAgent", passenger3Args);
			passenger3.start();
			agents.add(passenger3);

			sleep(0.1);

			// Passenger4
			String passenger4ArgsStr = "34,4,34,11,1";
			Object[] passenger4Args = passenger4ArgsStr.split(",");
			AgentController passenger4 = cc.createNewAgent("passenger4", "agents.PassengerAgent", passenger4Args);
			passenger4.start();
			agents.add(passenger4);

			sleep(0.1);

			// Passenger5
			String passenger5ArgsStr = "36,5,34,10,1";
			Object[] passenger5Args = passenger5ArgsStr.split(",");
			AgentController passenger5 = cc.createNewAgent("passenger5", "agents.PassengerAgent", passenger5Args);
			passenger5.start();
			agents.add(passenger5);

			sleep(0.1);

			// Passenger6
			String passenger6ArgsStr = "24,19,30,23,1";
			Object[] passenger6Args = passenger6ArgsStr.split(",");
			AgentController passenger6 = cc.createNewAgent("passenger6", "agents.PassengerAgent", passenger6Args);
			passenger6.start();
			agents.add(passenger6);

			sleep(0.1);

			// Passenger7
			String passenger7ArgsStr = "26,25,23,23,1";
			Object[] passenger7Args = passenger7ArgsStr.split(",");
			AgentController passenger7 = cc.createNewAgent("passenger7", "agents.PassengerAgent", passenger7Args);
			passenger7.start();
			agents.add(passenger7);

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