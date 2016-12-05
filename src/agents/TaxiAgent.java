package agents;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Stack;

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
import utils.AStar;
import utils.Cell;
import utils.DataSerializable;

public class TaxiAgent extends Agent {
	private static final long serialVersionUID = 163911234618964268L;

	// Taxi dynamic variables
	private DFAgentDescription dfd;

	private Cell positionCell;
	private int capacity;
	private int maxCapacity;

	private AID stationAID;

	private HashMap<Cell, Cell> cellMap;

	// Variables
	private Stack<Cell> path;

	private ArrayList<DataSerializable.PassengerData> travellingPassengers;

	protected void setup(){
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
				if(searchResult.length == 0) System.out.println("-T >> " + getLocalName() + " >> Could not find a station");
			}while(searchResult.length == 0);

			// Station found
			stationAID = searchResult[0].getName();
			System.out.println("-T >> " + getLocalName() + " >> Found station >> " + stationAID.getLocalName());
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}

		// --------------------------------------------
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
			System.err.println("-T >> " + getLocalName() + " >> Unexpected error receiving map");
			// Delete agent
			doDelete();
		}

		// --------------------------------------------
		// Variables initialization
		path = new Stack<>(); // To hold this taxi path
		travellingPassengers = new ArrayList<>(); // To hold passengers travelling

		// Read from arguments
		// Temporary values TODO ler dos argumentos
		int row = 1, col = 6;
		maxCapacity = 4;
		capacity = maxCapacity;

		positionCell = new Cell(row, col, 0, false);

		// Create taxi agent
		System.out.println("-T >> " + getLocalName() + " >> Just initialized");

		// --------------------------------------------
		// Yellow pages
		dfd = new DFAgentDescription();
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
		// Behaviours
		// Receive pick up proposes
		CyclicBehaviour receivePickupProposesBehaviour = new CyclicBehaviour(this) {
			private static final long serialVersionUID = -6899013002672695967L;

			@Override
			public void action() {
				// Defines the message template to receive
				MessageTemplate messageTemplate = MessageTemplate.and(
						MessageTemplate.MatchPerformative(ACLMessage.PROPOSE),
						MessageTemplate.and(
								MessageTemplate.MatchConversationId("propose-pickup"),
								MessageTemplate.MatchLanguage("JavaSerialization")));
				ACLMessage proposeMessage = myAgent.receive(messageTemplate);
				if(proposeMessage != null){
					try {
						DataSerializable.PassengerData passenger = (DataSerializable.PassengerData) proposeMessage.getContentObject();

						// Processes the taxi station's proposal
						addBehaviour(new StationProposesBehaviour(myAgent, proposeMessage, passenger));
					} catch (UnreadableException e) {
						e.printStackTrace();
					}
				}else{
					block();
				}
			}
		};

		CyclicBehaviour receivePickupOrderBehaviour = new CyclicBehaviour(this) {
			private static final long serialVersionUID = 5168158393908888099L;

			@Override
			public void action() {
				// Defines the message template to receive
				MessageTemplate messageTemplate = MessageTemplate.and(
						MessageTemplate.MatchPerformative(ACLMessage.INFORM),
						MessageTemplate.and(
								MessageTemplate.MatchConversationId("order-pickup"),
								MessageTemplate.MatchLanguage("JavaSerialization")));
				ACLMessage orderMessage = myAgent.receive(messageTemplate);
				if(orderMessage != null){
					try {
						DataSerializable.PassengerData passenger = (DataSerializable.PassengerData) orderMessage.getContentObject();

						// Re-verifies order
						if(!passenger.isSharingPolicy()){ // Station has no sharing policy
							// Taxi is already taken by another passenger
							if(travellingPassengers.size() != 0){
								// Refuses order
								ACLMessage refuseOrder = orderMessage.createReply();
								refuseOrder.setPerformative(ACLMessage.DISCONFIRM);
								refuseOrder.setContent("refuse-order");
								myAgent.send(refuseOrder);
							}

							// Accepts order
							ACLMessage acceptsOrder = orderMessage.createReply();
							acceptsOrder.setPerformative(ACLMessage.CONFIRM);
							acceptsOrder.setContent("accept-order");
							myAgent.send(acceptsOrder);

							// Calculates the path to travel the passenger
							Stack<Cell> pathToTravelPassenger = AStar.GetMoveOrders( // Adds the path to the existent one
									AStar.AStarAlgorithm(cellMap, passenger.getStartingCell(), passenger.getEndingCell(), true));

							// Calculates path from current position to passenger starting cell
							Stack<Cell> pathToPassenger = AStar.GetMoveOrders(
									AStar.AStarAlgorithm(cellMap, positionCell, passenger.getStartingCell(), true));

							// Path to travel the passenger is the bottom of the path stack
							path = pathToTravelPassenger;

							// Add this path to current path stack
							path = AStar.addStack(path, pathToPassenger);

							// Saves important checkpoints for notifications
							travellingPassengers.add(passenger);

							// Makes the Taxi move
							myAgent.removeBehaviour(new MoveBehaviour(null));
							addBehaviour(new MoveBehaviour(myAgent));
						}else{ // Station has sharing policy
							// TODO
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
		addBehaviour(receivePickupOrderBehaviour);
	}

	@Override
	protected void takeDown() {
		// Informs station about take down
		ACLMessage takedownMessage = new ACLMessage(ACLMessage.CANCEL);
		takedownMessage.addReceiver(stationAID);
		takedownMessage.setConversationId("takedown-taxi");
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
		System.out.println("-T >> " + getLocalName() + " >> Terminated");
	}

	// Functions
	private void changeTaxiPosition(Cell nextPosition){
		// TODO debugging vvvv
		System.out.println("From: " + positionCell + ", to: " + nextPosition);

		// Updates taxi position
		positionCell = nextPosition;

		// Passengers to remove
		ArrayList<DataSerializable.PassengerData> passengersToRemove = new ArrayList<>();

		// Checks if any passenger was picked up or traveled
		for(DataSerializable.PassengerData passenger : travellingPassengers){
			if(passenger.getStartingCell().equals(positionCell)){ // If taxi is picking up passenger
				int numberOfPassengersToTravel;
				int numberOfPassengersLeftBehind;

				// Number of passengers to travel
				if(passenger.getNumberOfPassengers() <= capacity){
					numberOfPassengersToTravel = passenger.getNumberOfPassengers();
					numberOfPassengersLeftBehind = 0;
					capacity -= numberOfPassengersToTravel;
				}else{
					numberOfPassengersToTravel = capacity;
					numberOfPassengersLeftBehind = passenger.getNumberOfPassengers() - numberOfPassengersToTravel;
					capacity = 0;
				}

				// Updates number of passengers traveled
				passenger.setNumberOfPassengers(numberOfPassengersToTravel);
				// Flags passenger pickup
				passenger.flagPickUp();

				// Informs passenger that taxi will pick up
				ACLMessage pickupPassengerMessage = new ACLMessage(ACLMessage.INFORM);
				pickupPassengerMessage.addReceiver(passenger.getAID());
				pickupPassengerMessage.setConversationId("picking-passenger");
				pickupPassengerMessage.setContent("" + numberOfPassengersLeftBehind);
				send(pickupPassengerMessage);
			}else if(passenger.getEndingCell().equals(positionCell)){ // If taxi is delivering a passenger
				// Informs passenger that taxi just traveled the passenger
				ACLMessage deliveredPassenger = new ACLMessage(ACLMessage.INFORM);
				deliveredPassenger.addReceiver(passenger.getAID());
				deliveredPassenger.setConversationId("delivering-passenger");
				deliveredPassenger.setContent("delivered");
				send(deliveredPassenger);

				// Remove passenger from traveling passengers variable
				passengersToRemove.add(passenger);
				// Free taxi capacity spaces
				capacity += passenger.getNumberOfPassengers();
			}
		}

		// Remove delivered passenger from traveling passengers
		for(DataSerializable.PassengerData passenger : passengersToRemove)
			travellingPassengers.remove(passenger);
	}

	// --------------------------------------------
	// Extended behaviours
	// Process station proposes
	private class StationProposesBehaviour extends Behaviour {
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

		// Overrides
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

					// Calculates the fastest path to the passenger
					LinkedList<Cell> path = AStar.AStarAlgorithm(cellMap, positionCell, passengerData.getStartingCell(), true);
					// Calculates the duration of the path
					score = AStar.PathDuration(path);

					state = ACCEPT_PROPOSE;
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

	// Move taxi process
	private class MoveBehaviour extends Behaviour {
		private static final long serialVersionUID = -8992101062346403501L;

		// Behaviour states
		private String state;
		private static final String STATUS = "STATUS";
		private static final String MOVE = "MOVE";
		private static final String TRAVELLING = "TRAVELLING";
		private static final String DONE_MOVING = "DONE_MOVING";

		// Variables
		private Cell nextCell;

		// Constructor
		public MoveBehaviour(Agent myAgent){
			super(myAgent);

			nextCell = null;

			state = STATUS;
		}

		// Overrides
		@Override
		public void action() {
			switch (state) {
			case STATUS:
				// If taxi has no path to travel
				if(path.isEmpty()){
					if(positionCell.equals(new Cell(0, 0, 0, false))){ // If taxi is in origin position, in taxi station
						// The taxi does not need to travel more
						state = DONE_MOVING;
						break;
					}else{ // Taxi has no move orders and it is not in taxi station
						path = AStar.GetMoveOrders(AStar.AStarAlgorithm(cellMap, positionCell, new Cell(0, 0, 0, false), true));
					}
				}

				// Path is filled, moves taxi
				state = MOVE;
				break;
			case MOVE:
				// Verifies the stack before continuing
				if(path.isEmpty()){
					state = STATUS;
					break;
				}

				// Retrieves next cell taxi has to travel to
				nextCell = path.pop();

				// Verifies that next cell is an adjacent cell
				if(positionCell.isAdjacent(nextCell)){
					state = TRAVELLING;
					break;
				}else{
					try{
						throw new Exception("-T >> " + getLocalName() + " >> Was attempting to travel to a not adjacent cell\n"
								+ "Current cell was: " + positionCell + ", attempting to go to: " + nextCell);
					}catch(Exception e){
						System.err.println(e.getMessage());
						state = DONE_MOVING;
					}
				}
				break;
			case TRAVELLING:
				// Verifies that the next cell is initialized
				if(nextCell != null){
					// Waits the duration of the cell
					myAgent.addBehaviour(new WakerBehaviour(myAgent, positionCell.getDuration()) {
						private static final long serialVersionUID = 7806259233409706677L;

						@Override
						protected void handleElapsedTimeout() {
							// Updates taxi position
							changeTaxiPosition(nextCell);

							// Send information to taxi station
							ACLMessage informPositionMessage = new ACLMessage(ACLMessage.INFORM);
							informPositionMessage.addReceiver(stationAID);
							informPositionMessage.setConversationId("inform-state");
							try {
								informPositionMessage.setContentObject(
										new DataSerializable.TaxiData(myAgent.getAID(), positionCell, capacity));
								informPositionMessage.setLanguage("JavaSerialization");
							} catch (IOException e) {
								e.printStackTrace();
							}

							myAgent.send(informPositionMessage);

							// Adds the execution of this behaviour again
							myAgent.addBehaviour(new MoveBehaviour(myAgent));
						}
					});
				}else{
					try{
						throw new Exception("-T >> " + getLocalName() + " >> Was attempting to travel to null cell");
					}catch(Exception e){
						System.err.println(e.getMessage());
					}
				}

				// Stops this behaviour
				state = DONE_MOVING;
				break;
			default:
				break;
			}
		}

		@Override
		public boolean done() {
			return (state == DONE_MOVING);
		}

		@Override
		public boolean equals(Object obj) {
			if(obj == this) return true;

			if(!(obj instanceof MoveBehaviour)) return false;
			else return true;
		}
	}
}