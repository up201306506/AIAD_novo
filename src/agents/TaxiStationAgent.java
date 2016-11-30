package agents;

import java.util.HashMap;

import jade.core.AID;
import jade.core.Agent;
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

	// Variable holders
	private HashMap<AID, DataSerializable.TaxiData> taxis;
	private HashMap<AID, DataSerializable.PassengerData> passengers;

	//---------------------------------------------
	@Override
	protected void setup() {
		// Variables
		System.out.println("#S >> " + getLocalName() + " >> Just initialized");

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
			System.out.println("#S >> " + getLocalName() + " >> Terminating, found another station");
			if(searchResult.length != 0) takeDown();
		} catch (FIPAException e) {
			System.out.println("#S >> " + getLocalName() + " >> Search for another station exception");
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

		// Behaviours ---------------------------------
		// Receive taxi informations
		CyclicBehaviour receiveTaxiInformation = new CyclicBehaviour(this) {
			private static final long serialVersionUID = 3525184234368919653L;

			@Override
			public void action() {
				// Defines the message template to receive
				MessageTemplate messageTemplate = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
				ACLMessage informationMessage = myAgent.receive(messageTemplate);
				if(informationMessage != null &&  informationMessage.getConversationId().equals("taxi-position")){
					try {
						if(("JavaSerialization").equals(informationMessage.getLanguage())){
							DataSerializable.TaxiData taxi = (DataSerializable.TaxiData) informationMessage.getContentObject();
							taxis.put(taxi.getAID(), taxi);
						}
					} catch (UnreadableException e) {
						e.printStackTrace();
					}
				}else{
					block();
				}
			}
		};

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

							System.out.println(passengers.get(passenger.getAID()).toString());

							// TODO process request from passenger
						}
					} catch (UnreadableException e) {
						e.printStackTrace();
					}
				}else{
					block();
				}
			}
		};

		addBehaviour(receiveTaxiInformation);
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
}