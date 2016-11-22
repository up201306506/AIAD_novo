package agents;

import java.io.IOException;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import utils.DataSerializable;

public class TaxiAgent extends Agent {
	private static final long serialVersionUID = 163911234618964268L;

	// Taxi dynamic variables
	private int xCoord;
	private int yCoord;
	private int capacity;
	private int maxCapacity;

	protected void setup(){
		// Read from arguments
		xCoord = 1; // Temporary values
		yCoord = 1;
		maxCapacity = 4;
		capacity = maxCapacity;

		// Create taxi agent
		System.out.println("-T >> " + getLocalName() + " >> Just initialized");

		// --------------------------------------------
		// Yellow pages -------------------------------
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());

		// Register the taxi service
		ServiceDescription sd = new ServiceDescription();
		sd.setType("taxi");
		sd.setName(getLocalName());

		dfd.addServices(sd);

		try {
			DFService.register(this, dfd);
		}
		catch (FIPAException fe) {
			System.out.println("-T >> " + getLocalName() + " >> DFService register exception");
			fe.printStackTrace();
		}

		// Behaviours ---------------------------------
		// Send taxi information
		CyclicBehaviour sendTaxiInformation = new CyclicBehaviour(this) {
			private static final long serialVersionUID = -6899013002672695967L;

			@Override
			public void action() {
				// Prepare search for the taxi station
				DFAgentDescription dfAgentDescription = new DFAgentDescription();
				ServiceDescription serviceDescription = new ServiceDescription();
				serviceDescription.setType("station");
				dfAgentDescription.addServices(serviceDescription);

				// Search for the taxi station
				AID stationAID = null;
				try {
					DFAgentDescription[] searchResult = DFService.search(myAgent, dfAgentDescription);

					if(searchResult.length != 0){
						// Station found
						stationAID = searchResult[0].getName();
						System.out.println("-T >> " + getLocalName() + " >> Found station >> " + stationAID.getName());
					}else{
						// No stations found
						System.out.println("-T >> " + getLocalName() + " >> Could not find a station");
					}
				} catch (FIPAException fe) {
					fe.printStackTrace();
				}

				if(stationAID != null){
					// Inform message if a station was found
					ACLMessage informMessage = new ACLMessage(ACLMessage.INFORM);
					informMessage.addReceiver(stationAID);
					informMessage.setConversationId("taxi-position");

					try {
						informMessage.setContentObject(new DataSerializable.TaxiData(myAgent.getAID(), xCoord, yCoord, capacity, maxCapacity));
						informMessage.setLanguage("JavaSerialization");
					} catch (IOException e) {
						e.printStackTrace();
					}

					myAgent.send(informMessage);

					System.out.println("-T >> " + getLocalName() + " >> State is: " + "X - " + xCoord + "| Y - " + yCoord
							+ "| C - " + capacity + "| MC - " + maxCapacity);
				}
			}
		};

		addBehaviour(sendTaxiInformation);
	}

	@Override
	protected void takeDown() {
		System.out.println("-T >> " + getLocalName() + " >> Terminated");
	}
}