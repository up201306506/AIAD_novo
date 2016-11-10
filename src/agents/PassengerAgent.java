package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class PassengerAgent extends Agent {
	private static final long serialVersionUID = 8180459495842676730L;

	// Passenger dynamic variables
	private int xiCoord;
	private int yiCoord;
	private int xfCoord;
	private int yfCoord;
	private int number;

	protected void setup() {
		// Read from arguments
		xiCoord = 5; // Temporary values
		yiCoord = 5;
		xfCoord = 10;
		yfCoord = 10;
		number = 3;

		// Create passenger agent
		System.out.println("=P >> " + getLocalName() + " >> Just initialized");

		// --------------------------------------------
		// Yellow pages -------------------------------
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());

		try {
			DFService.register(this, dfd);
		}
		catch (FIPAException fe) {
			System.out.println("=P >> " + getLocalName() + " >> DFService register exception");
			fe.printStackTrace();
		}

		// --------------------------------------------
		// Behaviours ---------------------------------
	}

	@Override
	protected void takeDown() {
		System.out.println("=P >> " + getLocalName() + " >> Terminated");
	}
}