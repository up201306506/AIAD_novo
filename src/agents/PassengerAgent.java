package agents;

import java.io.IOException;
import java.util.HashMap;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import utils.Cell;
import utils.DataSerializable;

public class PassengerAgent extends Agent {
	private static final long serialVersionUID = 8180459495842676730L;

	// Passenger dynamic variables
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
		int rowI = 4, colI = 3, rowF = 4, colF = 7;
		numberOfPassengers = 3;

		startingCell = new Cell(rowI, colI, 0, false);
		endingCell = new Cell(rowF, colF, 0, false);

		// Create passenger agent
		System.out.println("=P >> " + getLocalName() + " >> Just initialized");

		// --------------------------------------------
		// Yellow pages
		DFAgentDescription dfd = new DFAgentDescription();
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
	}

	@Override
	protected void takeDown() {
		// TODO
		System.out.println("=P >> " + getLocalName() + " >> Terminated");
	}
}