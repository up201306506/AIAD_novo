package agents;

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
import utils.AStar;
import utils.Cell;
import utils.DataSerializable;

public class TaxiAgent extends Agent {
	private static final long serialVersionUID = 163911234618964268L;

	// Taxi dynamic variables
	private Cell positionCell;
	private int capacity;
	private int maxCapacity;

	private AID stationAID;

	private byte[][] map;
	private int[][] durationMap;
	private HashMap<Cell, Cell> cellMap;

	protected void setup(){
		// Read from arguments
		// Temporary values TODO ler dos argumentos
		int row = 0, col = 0;
		maxCapacity = 4;
		capacity = maxCapacity;

		positionCell = new Cell(row, col, 0, false);

		map = null;
		durationMap = null;
		cellMap = Cell.mapToCellMap(map, durationMap);

		// Create taxi agent
		System.out.println("-T >> " + getLocalName() + " >> Just initialized");

		// --------------------------------------------
		// Search for taxi station --------------------
		// Prepare search for the taxi station
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
				System.out.println("-T >> " + getLocalName() + " >> Could not find a station");
			}while(searchResult.length == 0);

			// Station found
			stationAID = searchResult[0].getName();
			System.out.println("-T >> " + getLocalName() + " >> Found station >> " + stationAID.getName());

		} catch (FIPAException fe) {
			fe.printStackTrace();
		}

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

							addBehaviour(new StationProposesBehaviour(myAgent, proposeMessage, passenger));
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
		private static final String ACCEPT_PROPOSE = "ACCEPT_PROPOSE";
		private static final String REFUSE_PROPOSE = "REFUSE_PROPOSE";
		private static final String DONE_PROCESS_PROPOSE = "DONE_PROCESS_PROPOSE";

		// Variables
		private ACLMessage proposeMessage;
		private DataSerializable.PassengerData passengerData;
		private int score;

		// Constructor
		public StationProposesBehaviour(Agent myAgent, ACLMessage proposeMessage, DataSerializable.PassengerData passengerData){
			super(myAgent);

			this.proposeMessage = proposeMessage;
			this.passengerData = passengerData;

			state = PROCESS_PROPOSE;
		}

		@Override
		public void action() {
			switch (state) {
			case PROCESS_PROPOSE:
				if(!passengerData.isSharingPolicy()){ // Station has no sharing policy
					// Taxi is already taken by another passenger
					if(capacity != maxCapacity){
						state = REFUSE_PROPOSE;
						break;
					}

					// Retrieves the length of the shortest path to passenger
					score = (
							AStar.AStarAlgorithm(cellMap,
									positionCell, passengerData.getStartingCell(),
									false) // While picking up a passenger, always travel the shortest path
							).size();
				}else{ // Station has sharing policy
					// TODO
				}
				break;
			case ACCEPT_PROPOSE:
				ACLMessage acceptProposal = proposeMessage.createReply();
				acceptProposal.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
				acceptProposal.setContent("" + score);
				myAgent.send(acceptProposal);

				state = DONE_PROCESS_PROPOSE;
				break;
			case REFUSE_PROPOSE:
				ACLMessage refuseProposal = proposeMessage.createReply();
				refuseProposal.setPerformative(ACLMessage.REJECT_PROPOSAL);
				refuseProposal.setContent("refuse-pickup");
				myAgent.send(refuseProposal);

				state = DONE_PROCESS_PROPOSE;
				break;
			default:
				break;
			}
		}

		@Override
		public boolean done() {
			return (state == DONE_PROCESS_PROPOSE);
		}
	}
}