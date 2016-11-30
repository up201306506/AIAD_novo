package agents;

import java.io.IOException;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import utils.DataSerializable;
import utils.DataSerializable.PassengerData;

public class TaxiAgent extends Agent {
	private static final long serialVersionUID = 163911234618964268L;

	// Taxi dynamic variables
	private int xCoord;
	private int yCoord;
	private int capacity;
	private int maxCapacity;

	private AID stationAID;

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

		// --------------------------------------------
		// Search for taxi station --------------------
		// Prepare search for the taxi station
		DFAgentDescription dfAgentDescription = new DFAgentDescription();
		ServiceDescription serviceDescription = new ServiceDescription();
		serviceDescription.setType("station");
		dfAgentDescription.addServices(serviceDescription);

		try {
			DFAgentDescription[] searchResult = DFService.search(this, dfAgentDescription);

			if(searchResult.length == 0){
				// No stations found
				System.out.println("-T >> " + getLocalName() + " >> Could not find a station");
				takeDown();
			}

			// Station found
			stationAID = searchResult[0].getName();
			System.out.println("-T >> " + getLocalName() + " >> Found station >> " + stationAID.getName());

		} catch (FIPAException fe) {
			fe.printStackTrace();
		}

		// --------------------------------------------
		// Behaviours ---------------------------------
		// Receive pick up proposes
		CyclicBehaviour receivePickupProposesBehaviour = new CyclicBehaviour(this) {
			private static final long serialVersionUID = -6899013002672695967L;

			@Override
			public void action() {
				// Defines the message template to receive
				MessageTemplate messageTemplate = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
				ACLMessage proposeMessage = myAgent.receive(messageTemplate);
				if(proposeMessage != null &&  proposeMessage.getConversationId().equals("propose-pickup")){

					try {
						if(("JavaSerialization").equals(proposeMessage.getLanguage())){
							DataSerializable.PassengerData passenger = (DataSerializable.PassengerData) proposeMessage.getContentObject();

							addBehaviour(new StationProposesBehaviour(myAgent, passenger));
						}
					} catch (UnreadableException e) {
						e.printStackTrace();
					}
				}else{
					block();
				}
			}
		};

		addBehaviour(receivePickupProposesBehaviour);
	}

	@Override
	protected void takeDown() {
		System.out.println("-T >> " + getLocalName() + " >> Terminated");
	}

	// --------------------------------------------
	// Extended behaviours ------------------------
	// Process station proposes
	private class StationProposesBehaviour extends Behaviour{
		private static final long serialVersionUID = -7519745466815575120L;

		// Behaviour states
		private String state;
		private static final String PROCESS_PROPOSE = "PROCESS_PROPOSE";
		private static final String ANSWER_PROPOSE = "ANSWER_PROPOSE";
		private static final String REFUSE_PROPOSE = "REFUSE_PROPOSE";
		private static final String DONE_PROCESS_PROPOSE = "DONE_PROCESS_PROPOSE";

		// Variables
		private DataSerializable.PassengerData passengerData;

		// Constructor
		public StationProposesBehaviour(Agent myAgent, DataSerializable.PassengerData passengerData){
			super(myAgent);

			this.passengerData = passengerData;

			state = PROCESS_PROPOSE;
		}

		@Override
		public void action() {
			switch (state) {
			case PROCESS_PROPOSE:
				if(!passengerData.isSharingPolicy()){ // Station has no sharing policy
					// Taxi is already taken by another passenger
					if(capacity != maxCapacity)
						state = REFUSE_PROPOSE;


				}else{ // Station has sharing policy

				}
				break;
			}
		}

		@Override
		public boolean done() {
			return (state == DONE_PROCESS_PROPOSE);
		}
	}
}