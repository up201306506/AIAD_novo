package agents;

import java.io.IOException;
import java.util.HashMap;
import java.util.PriorityQueue;

import gui.MapGUI;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.WakerBehaviour;
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

		taxis = new HashMap<>();
		passengers = new HashMap<>();
		MapGUI gui = new MapGUI();

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
		private static final String RECEIVE_ANSWER = "RECEIVE_ANSWER";
		private static final String ORDER_PICKUP = "ORDER_PICKUP";
		private static final String RECEIVE_CONFIRMATION = "RECEIVE_CONFIRMATION";
		private static final String SLEEP = "SLEEP";
		private static final String DONE_PROCESS_REQUEST = "DONE_PROCESS_REQUEST";

		// Variables
		private DataSerializable.PassengerData passengerData;

		// Variables used on action()
		// Number of taxis messaged
		private int numberOfTaxisMessaged;
		// Number of taxis replies
		private int numberOfTaxisReplies;
		// Template used locally to verify unique values
		private MessageTemplate messageTemplate;

		// Priority queue to choose best taxi to pickup
		private class TaxiScore implements Comparable<TaxiScore> {

			// Variables
			private AID taxi;
			private int score;

			// Constructor
			public TaxiScore(AID taxi, int score){
				this.taxi = taxi;
				this.score = score;
			}

			// Getters and setters
			public AID getTaxiAID(){
				return taxi;
			}

			// Overrides
			@Override
			public int compareTo(TaxiScore that) {
				return Integer.valueOf(this.score).compareTo(that.score);
			}
		}
		// Priority queue declaration
		private PriorityQueue<TaxiScore> taxiScore;

		// Constructor
		public PassengerRequestBehaviour(Agent myAgent, DataSerializable.PassengerData passengerData){
			super(myAgent);

			this.passengerData = passengerData;

			numberOfTaxisMessaged = 0;
			numberOfTaxisReplies = 0;
			messageTemplate = null;

			taxiScore = new PriorityQueue<>();

			state = PROCESS_REQUEST;
		}

		@Override
		public void action() {
			switch (state) {
			case PROCESS_REQUEST:
				// Prepare request data
				passengerData.setRequestBooleans(isSharingPolicy, isDiminishingDuration);

				// Prepare search taxis
				DFAgentDescription dfAgentDescription = new DFAgentDescription();
				ServiceDescription serviceDescription = new ServiceDescription();
				serviceDescription.setType("taxi");
				dfAgentDescription.addServices(serviceDescription);

				// Search for the taxi station
				DFAgentDescription[] searchResult = null;
				try {
					searchResult = DFService.search(myAgent, dfAgentDescription);

					// If there are no taxis sleeps
					if(searchResult.length == 0){
						state = SLEEP;
						break;
					}

					numberOfTaxisMessaged = searchResult.length;
				} catch (FIPAException fe) {
					fe.printStackTrace();
				}

				ACLMessage proposePickupMessage = new ACLMessage(ACLMessage.PROPOSE);
				for(DFAgentDescription dfar : searchResult)
					proposePickupMessage.addReceiver(dfar.getName());
				proposePickupMessage.setConversationId("propose-pickup");
				// Unique conversation value
				proposePickupMessage.setReplyWith("propose" + System.currentTimeMillis());
				try {
					proposePickupMessage.setContentObject(passengerData);
					proposePickupMessage.setLanguage("JavaSerialization");
				} catch (IOException e) {
					e.printStackTrace();
				}

				myAgent.send(proposePickupMessage);

				// Creates template of expecting answers
				messageTemplate = MessageTemplate.and(
						MessageTemplate.or(
								MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL),
								MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL)),
						MessageTemplate.MatchInReplyTo(proposePickupMessage.getReplyWith()));

				state = RECEIVE_ANSWER;
				break;
			case RECEIVE_ANSWER:
				ACLMessage proposalAnswer = myAgent.receive(messageTemplate);
				if(proposalAnswer != null && proposalAnswer.getConversationId().equals("propose-pickup")){
					if(proposalAnswer.getPerformative() == ACLMessage.ACCEPT_PROPOSAL){ // If it was accept proposal
						// Adds taxi to a priority queue with corresponding score
						taxiScore.add(new TaxiScore(proposalAnswer.getSender(), Integer.parseInt(proposalAnswer.getContent())));
					}else if(proposalAnswer.getPerformative() == ACLMessage.REJECT_PROPOSAL){ // If it was refuse proposal
						if(!proposalAnswer.getContent().equals("refuse-pickup"))
							System.out.println("#S >> " + getLocalName() + " >> Received unexpected message");
					}else
						System.out.println("#S >> " + getLocalName() + " >> Received unexpected message");

					// Increment the value of replies counter
					numberOfTaxisReplies++;
					// If all replies were received
					if(numberOfTaxisReplies >= numberOfTaxisMessaged)
						state = ORDER_PICKUP;
				}else{
					block();
				}
				break;
			case ORDER_PICKUP:
				// If there is not any valid taxi to order the passenger's pickup
				if(taxiScore.isEmpty()) state = SLEEP;

				// Prepare request data
				passengerData.setRequestBooleans(isSharingPolicy, isDiminishingDuration);

				AID taxiToOrder = taxiScore.remove().getTaxiAID();

				ACLMessage orderPickupMessage = new ACLMessage(ACLMessage.INFORM);
				orderPickupMessage.addReceiver(taxiToOrder);
				orderPickupMessage.setConversationId("order-pickup");
				// Unique conversation value
				orderPickupMessage.setReplyWith("order" + System.currentTimeMillis());
				try {
					orderPickupMessage.setContentObject(passengerData);
					orderPickupMessage.setLanguage("JavaSerialization");
				} catch (IOException e) {
					e.printStackTrace();
				}

				myAgent.send(orderPickupMessage);

				// Creates template of expecting answers
				messageTemplate = MessageTemplate.and(
						MessageTemplate.or(
								MessageTemplate.MatchPerformative(ACLMessage.CONFIRM),
								MessageTemplate.MatchPerformative(ACLMessage.DISCONFIRM)),
						MessageTemplate.MatchInReplyTo(orderPickupMessage.getReplyWith()));

				state = RECEIVE_CONFIRMATION;
				break;
			case RECEIVE_CONFIRMATION:
				ACLMessage orderAnswer = myAgent.receive(messageTemplate);
				if(orderAnswer != null && orderAnswer.getConversationId().equals("order-pickup")){
					if(orderAnswer.getPerformative() == ACLMessage.CONFIRM){ // If order was confirmed
						if(orderAnswer.getContent().equals("accept-order"))
							state = DONE_PROCESS_REQUEST;
						else
							System.out.println("#S >> " + getLocalName() + " >> Received unexpected message");
					}else if(orderAnswer.getPerformative() == ACLMessage.DISCONFIRM){ // If order was disconfirmed
						if(orderAnswer.getContent().equals("refuse-order"))
							state = ORDER_PICKUP;
						else
							System.out.println("#S >> " + getLocalName() + " >> Received unexpected message");
					}else
						System.out.println("#S >> " + getLocalName() + " >> Received unexpected message");
				}else{
					block();
				}
				break;
			case SLEEP:
				// Resets this behaviour after 30 seconds
				myAgent.addBehaviour(new WakerBehaviour(myAgent, 30000) {
					private static final long serialVersionUID = -3004190994251093897L;

					@Override
					protected void handleElapsedTimeout() {
						myAgent.addBehaviour(new PassengerRequestBehaviour(myAgent, passengerData));
					}
				});

				// Terminates this instance
				state = DONE_PROCESS_REQUEST;
				break;
			default:
				break;
			}
		}

		@Override
		public boolean done() {
			return (state == DONE_PROCESS_REQUEST);
		}
	}
}