package agents;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class TaxiAgent extends Agent {
	private static final long serialVersionUID = 163911234618964268L;

	// Taxi position refresh rate
	private final int _refresh_rate = 1; // seconds

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
		//capacity = maxCapacity;
		capacity = 2;

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
		// Behaviours ---------------------------------
		// Inform taxi station about this taxi position
		TickerBehaviour informPositionBehaviour = new TickerBehaviour(this, _refresh_rate * 1000) {
			private static final long serialVersionUID = -1145135997995313675L;

			@Override
			protected void onTick() {
				// Prepare search for the taxi station
				DFAgentDescription dfTemplate = new DFAgentDescription();
				ServiceDescription serviceTemplate = new ServiceDescription();
				serviceTemplate.setType("station");
				dfTemplate.addServices(serviceTemplate);

				// Search for the taxi station
				AID stationAID = null;
				try {
					DFAgentDescription[] searchResult = DFService.search(myAgent, dfTemplate);

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
					// Inform position if a station was found
					ACLMessage informPosition = new ACLMessage(ACLMessage.INFORM);
					informPosition.addReceiver(stationAID);
					informPosition.setConversationId("taxi-position");
					informPosition.setContent("X" + xCoord + "Y" + yCoord + "C" + capacity);
					myAgent.send(informPosition);

					System.out.println("-T >> " + getLocalName() + " >> State is: " + "X" + xCoord + "Y" + yCoord + "C" + capacity);
				}
			}
		};

		// Receives information requests and answers them
		CyclicBehaviour requestsHandlerBehaviour = new CyclicBehaviour(this) {
			private static final long serialVersionUID = 5168158393908888099L;

			@Override
			public void action() {
				// Defines the message template to receive
				MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
				ACLMessage msg = myAgent.receive(mt);
				if(msg != null &&  msg.getConversationId().equals("request-taxi")){
					// Message information variables holders
					boolean hasSharePolicy = false;
					boolean readSharePolicy = false;
					int xRequestCoord = -1;
					int yRequestCoord = -1;
					int capacityRequest = -1;

					// Regex to read the content of the request
					Pattern p = Pattern.compile("[a-zA-Z]\\d+");
					Matcher m = p.matcher(msg.getContent());

					try{
						while(m.find()){
							switch(m.group().charAt(0)){
							case 'S':
								readSharePolicy = true;
								if(m.group().substring(1).equals("1"))
									hasSharePolicy = true;
								else
									hasSharePolicy = false;
								break;
							case 'X':
								xRequestCoord = Integer.parseInt(m.group().substring(1));
								break;
							case 'Y':
								yRequestCoord = Integer.parseInt(m.group().substring(1));
								break;
							case 'C':
								capacityRequest = Integer.parseInt(m.group().substring(1));
								break;
							default:
								throw new Exception("String not recognized");
							}
						}

						if(!readSharePolicy || xRequestCoord == -1 || yRequestCoord == -1 || capacityRequest == -1)
							throw new Exception("A variable was not initialized");

					} catch(Exception e){
						System.err.println(e.getMessage());
					}

					// Creates reply to the request noting the conversation unique value
					ACLMessage replyRequest = msg.createReply();

					// Denies request
					if(capacity == 0 // If taxi has no capacity or
							|| (!hasSharePolicy && capacity != maxCapacity)){ // If taxi is not empty and does not have a sharing policy
						replyRequest.setPerformative(ACLMessage.REFUSE);
						replyRequest.setContent("refuse");
						myAgent.send(replyRequest);
					}

					// Answers as available for request
					replyRequest.setPerformative(ACLMessage.INFORM);

					// TODO Calculate distance from current position to request position
					// Request position coords: xRequestCoord, yRequestCoord
					// Assuming distance is always 10 to test
					int distanceToRequestPosition = 10;
					// -----------------------------------------------------------------

					replyRequest.setContent("M" + maxCapacity + "C" + capacity + "D" + distanceToRequestPosition);
					myAgent.send(replyRequest);
				}else{
					block();
				}
			}
		};

		// Process allocation request
		CyclicBehaviour allocationHandlerBehaviour = new CyclicBehaviour(this) {
			private static final long serialVersionUID = -3122694739221225940L;

			@Override
			public void action() {
				// Defines the message template to receive
				MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
				ACLMessage msg = myAgent.receive(mt);
				if(msg != null &&  msg.getConversationId().equals("allocate-taxi")){
					// Message information variables holders
					int xRequestCoord = -1;
					int yRequestCoord = -1;

					// Regex to read the content of the request
					Pattern p = Pattern.compile("[a-zA-Z]\\d+");
					Matcher m = p.matcher(msg.getContent());

					try{
						while(m.find()){
							switch(m.group().charAt(0)){
							case 'X':
								xRequestCoord = Integer.parseInt(m.group().substring(1));
								break;
							case 'Y':
								yRequestCoord = Integer.parseInt(m.group().substring(1));
								break;
							default:
								throw new Exception("String not recognized");
							}
						}

						if(xRequestCoord == -1 || yRequestCoord == -1)
							throw new Exception("A variable was not initialized");

					} catch(Exception e){
						System.err.println(e.getMessage());
					}

					// Creates reply to the request noting the conversation unique value
					ACLMessage replyAllocation = msg.createReply();
					replyAllocation.setPerformative(ACLMessage.INFORM);
					replyAllocation.setContent("ok");
					myAgent.send(replyAllocation);
				}else{
					block();
				}
			}
		};

		addBehaviour(informPositionBehaviour);
		addBehaviour(requestsHandlerBehaviour);
		addBehaviour(allocationHandlerBehaviour);
		// --------------------------------------------
	}

	@Override
	protected void takeDown() {
		System.out.println("-T >> " + getLocalName() + " >> Terminated");
	}
}