package agents;

import java.io.IOException;
import java.util.HashMap;

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

public class TaxiStationAgent extends Agent{
	private static final long serialVersionUID = 488752805219045668L;

	// Station system conditions
	private boolean isSharingPolicy;
	private boolean isDiminishingDuration;
	private boolean isDiminishingDistance;

	// Variable holders
	private HashMap<AID, DataSerializable.TaxiData> taxis;
	private HashMap<AID, DataSerializable.PassengerData> passengers;

	//---------------------------------------------
	@Override
	protected void setup() {
		// Variables
		System.out.println("#S >> " + getLocalName() + " >> Just initialized");

		isSharingPolicy = false;
		isDiminishingDuration = false;
		isDiminishingDistance = false;

		taxis = new HashMap<>();
		passengers = new HashMap<>();

		// --------------------------------------------
		// Search for other taxi stations
		DFAgentDescription stationTemplate = new DFAgentDescription();
		ServiceDescription stationServiceTemplate = new ServiceDescription();
		stationServiceTemplate.setType("station");
		stationTemplate.addServices(stationServiceTemplate);

		try {
			DFAgentDescription[] searchResult = DFService.search(this, stationTemplate);

			// Kill this station agent if there is another station agent up
			if(searchResult.length != 0){
				System.out.println("#S >> " + getLocalName() + " >> Terminating, found another station");
				takeDown();
			}
		} catch (FIPAException e) {
			e.printStackTrace();
		}

		// --------------------------------------------
		// Yellow pages -------------------------------
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());

		// Register the station service
		ServiceDescription sd = new ServiceDescription();
		sd.setType("station");
		sd.setName(getLocalName());

		dfd.addServices(sd);

		try {
			DFService.register(this, dfd);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}

		// --------------------------------------------
		// Behaviours ---------------------------------
		// Receive request informations
		CyclicBehaviour receiveRequestsBehaviour = new CyclicBehaviour(this) {
			private static final long serialVersionUID = -1745263839997857584L;

			@Override
			public void action() {
				// Defines the message template to receive
				MessageTemplate messageTemplate = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
				ACLMessage requestMessage = myAgent.receive(messageTemplate);
				if(requestMessage != null &&  requestMessage.getConversationId().equals("request-pickup")){

					try {
						if(("JavaSerialization").equals(requestMessage.getLanguage())){
							DataSerializable.PassengerData passenger = (DataSerializable.PassengerData) requestMessage.getContentObject();
							passengers.put(passenger.getAID(), passenger);

							addBehaviour(new PassengerRequestBehaviour(myAgent, passenger));
						}
					} catch (UnreadableException e) {
						e.printStackTrace();
					}
				}else{
					block();
				}
			}
		};

		addBehaviour(receiveRequestsBehaviour);
	}

	@Override
	protected void takeDown() {
		// Deregister from yellow pages
		try {
			DFService.deregister(this);
		} catch (Exception e) {
			System.out.println("#S >> " + getLocalName() + " >> DFService deregister exception");
			e.printStackTrace();
		}

		System.out.println("#S >> " + getLocalName() + " >> Terminated");
	}

	// --------------------------------------------
	// Extended behaviours ------------------------
	// Process passengers requests
	private class PassengerRequestBehaviour extends Behaviour{
		private static final long serialVersionUID = -1143521338604148943L;

		// Behaviour states
		private String state;
		private static final String PROCESS_REQUEST = "PROCESS_REQUEST";
		private static final String RECEIVE_TAXI_INFORMATION = "RECEIVE_TAXI_INFORMATION";

		// Variables
		private DataSerializable.PassengerData passengerData;

		// Constructor
		public PassengerRequestBehaviour(Agent myAgent, DataSerializable.PassengerData passengerData){
			super(myAgent);

			this.passengerData = passengerData;

			state = PROCESS_REQUEST;
		}

		@Override
		public void action() {
			switch (state) {
			case PROCESS_REQUEST:
				// Prepare request data
				passengerData.setRequestBooleans(isSharingPolicy, isDiminishingDuration, isDiminishingDistance);

				// Prepare search taxis
				DFAgentDescription dfAgentDescription = new DFAgentDescription();
				ServiceDescription serviceDescription = new ServiceDescription();
				serviceDescription.setType("taxi");
				dfAgentDescription.addServices(serviceDescription);

				// Search for the taxi station
				DFAgentDescription[] searchResult = null;
				try {
					searchResult = DFService.search(myAgent, dfAgentDescription);
				} catch (FIPAException fe) {
					fe.printStackTrace();
				}

				ACLMessage proposePickupMessage = new ACLMessage(ACLMessage.PROPOSE);
				for(DFAgentDescription dfar : searchResult)
					proposePickupMessage.addReceiver(dfar.getName());
				proposePickupMessage.setConversationId("propose-pickup");
				try {
					proposePickupMessage.setContentObject(passengerData);
					proposePickupMessage.setLanguage("JavaSerialization");
				} catch (IOException e) {
					e.printStackTrace();
				}

				myAgent.send(proposePickupMessage);

				state = RECEIVE_TAXI_INFORMATION;
				break;
			case RECEIVE_TAXI_INFORMATION:
				// TODO
				break;
			}
		}

		@Override
		public boolean done() {
			// TODO Auto-generated method stub
			return false;
		}

	}
}