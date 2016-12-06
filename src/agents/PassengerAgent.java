package agents;

import java.io.IOException;
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
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import utils.Cell;
import utils.DataSerializable;

public class PassengerAgent extends Agent {
	private static final long serialVersionUID = 8180459495842676730L;

	// Passenger dynamic variables
	private DFAgentDescription dfd;

	private Cell startingCell;
	private Cell endingCell;
	private int numberOfPassengers;

	private AID stationAID;

	private HashMap<Cell, Cell> cellMap;

	protected void setup() {
		// Search for taxi station
		DFAgentDescription dfAgentDescription = new DFAgentDescription();
		ServiceDescription serviceDescription = new ServiceDescription();
		serviceDescription.setType("station");
		dfAgentDescription.addServices(serviceDescription);

		try {
			DFAgentDescription[] searchResult = null;

			do{
				// Tries to find a station every 30 seconds
				if(searchResult != null)
					blockingReceive(30000);

				searchResult = DFService.search(this, dfAgentDescription);
				if(searchResult.length == 0) System.out.println("=P >> " + getLocalName() + " >> Could not find a station");
			}while(searchResult.length == 0);

			// Station found
			stationAID = searchResult[0].getName();
			System.out.println("=P >> " + getLocalName() + " >> Found station >> " + stationAID.getLocalName());

		} catch (FIPAException fe) {
			fe.printStackTrace();
		}

		// Asks station for a map
		ACLMessage askMapMessage = new ACLMessage(ACLMessage.REQUEST);
		askMapMessage.addReceiver(stationAID);
		askMapMessage.setReplyWith("request" + System.currentTimeMillis());
		askMapMessage.setConversationId("request-map");
		askMapMessage.setContent("map");
		send(askMapMessage);

		// Waits for map answer
		ACLMessage replyMapMessage = blockingReceive( // Blocks until it receives a message
				MessageTemplate.and(
						MessageTemplate.MatchPerformative(ACLMessage.INFORM),
						MessageTemplate.and(
								MessageTemplate.MatchInReplyTo(askMapMessage.getReplyWith()),
								MessageTemplate.and(
										MessageTemplate.MatchConversationId("request-map"),
										MessageTemplate.MatchLanguage("JavaSerialization")))));
		if(replyMapMessage != null){
			try {
				cellMap = (HashMap<Cell, Cell>) replyMapMessage.getContentObject();
			} catch (UnreadableException e) {
				e.printStackTrace();
			}
		}else{
			System.err.println("=P >> " + getLocalName() + " >> Unexpected error receiving map");
			takeDown();
		}

		// --------------------------------------------

		// Temporary values TODO ler dos argumentos
		int rowI = 0, colI = 6, rowF = 0, colF = 3;
		numberOfPassengers = 3;

		startingCell = new Cell(rowI, colI, 0, false);
		endingCell = new Cell(rowF, colF, 0, false);

		// Create passenger agent
		System.out.println("=P >> " + getLocalName() + " >> Just initialized");

		// --------------------------------------------
		// Yellow pages
		dfd = new DFAgentDescription();
		dfd.setName(getAID());

		try {
			DFService.register(this, dfd);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}

		// --------------------------------------------
		// Request pick up from taxi
		ACLMessage requestTaxi = new ACLMessage(ACLMessage.REQUEST);
		requestTaxi.addReceiver(stationAID);
		requestTaxi.setConversationId("request-pickup");
		try {
			requestTaxi.setContentObject(
					new DataSerializable.PassengerData(getAID(),
							startingCell,
							endingCell,
							numberOfPassengers)
					);
			requestTaxi.setLanguage("JavaSerialization");
		} catch (IOException e) {
			e.printStackTrace();
		}

		send(requestTaxi);

		// TODO start timer, parallel behaviour

		System.out.println("=P >> " + getLocalName() + " >> State is: I: "
				+ startingCell.toString() + " | F: " + endingCell.toString()
				+ " | N - " + numberOfPassengers);

		// --------------------------------------------
		// Behaviours
		// Waits for pick up
		CyclicBehaviour waitForPickUpBehaviour = new CyclicBehaviour(this) {
			private static final long serialVersionUID = 8831324489911981757L;

			@Override
			public void action() {
				// Defines the message template to receive
				MessageTemplate messageTemplate = MessageTemplate.and(
						MessageTemplate.MatchPerformative(ACLMessage.INFORM),
						MessageTemplate.MatchConversationId("picking-passenger"));
				ACLMessage pickingMessage = myAgent.receive(messageTemplate);
				if(pickingMessage != null){
					// Taxi has no capacity to travel all number of passengers
					if(Integer.parseInt(pickingMessage.getContent()) != 0){
						// Decreases number of passengers to travel
						numberOfPassengers -= Integer.parseInt(pickingMessage.getContent());
						// Args of new agent
						// TODO
						String newAgentArgs = "row col lalalala";
						Object[] args = newAgentArgs.split(" "); // TODO deve dar erro
						// Create new passenger with same information but number of passengers
						ContainerController cc = getContainerController();
						AgentController ac = null;
						try {
							ac = cc.createNewAgent(getLocalName() + pickingMessage.getContent(), "agents.PassengerAgent" , args);
							ac.start();
						} catch (StaleProxyException e) {
							e.printStackTrace();
						}
					}

					// Displays that this passenger was picked up by a taxi
					System.out.println("=P >> " + getLocalName() + " >> Was picked up by taxi: " + pickingMessage.getSender().getLocalName());

					// Inform station that this passenger was picked up
					ACLMessage pickedInformMessage = new ACLMessage(ACLMessage.INFORM);
					pickedInformMessage.addReceiver(stationAID);
					pickedInformMessage.setConversationId("picked-passenger");
					pickedInformMessage.setContent("" + numberOfPassengers);
					send(pickedInformMessage);
				}else{
					block();
				}
			}
		};

		// Waits for finished traveling
		CyclicBehaviour waitForFinishBehaviour = new CyclicBehaviour(this) {
			private static final long serialVersionUID = -1506263584532711286L;

			@Override
			public void action() {
				// Defines the message template to receive
				MessageTemplate messageTemplate = MessageTemplate.and(
						MessageTemplate.MatchPerformative(ACLMessage.INFORM),
						MessageTemplate.MatchConversationId("delivering-passenger"));
				ACLMessage finishedTravelMessage = myAgent.receive(messageTemplate);
				if(finishedTravelMessage != null){
					// Verifies content
					if(!finishedTravelMessage.getContent().equals("delivered")){
						try{
							throw new Exception("=P >> " + getLocalName() + " >> Received unexpected message");
						}catch(Exception e){
							System.err.println(e.getMessage());
						}
					}

					// Displays that this passenger was delivered by a taxi
					System.out.println("=P >> " + getLocalName() + " >> Was delivered by taxi: " + finishedTravelMessage.getSender().getLocalName());

					// Deletes agent
					myAgent.doDelete();
				}else{
					block();
				}
			}
		};

		addBehaviour(waitForPickUpBehaviour);
		addBehaviour(waitForFinishBehaviour);
	}

	@Override
	protected void takeDown() {
		// Informs station about take down
		ACLMessage takedownMessage = new ACLMessage(ACLMessage.CANCEL);
		takedownMessage.addReceiver(stationAID);
		takedownMessage.setConversationId("takedown-passenger");
		takedownMessage.setContent("takedown");
		send(takedownMessage);
		// Deregister from the yellow pages
		try {
			if(DFService.search(this, dfd).length != 0)
				DFService.deregister(this);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		// Printout a dismissal message
		System.out.println("=P >> " + getLocalName() + " >> Terminated");
	}
}