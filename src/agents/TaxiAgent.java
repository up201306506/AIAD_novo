package agents;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
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
import utils.Cell.CellValue;

public class TaxiAgent extends Agent {
	private static final long serialVersionUID = 163911234618964268L;

	// Static configuration value
	private static float COMPENSATION_VALUE = 0.25f;

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

	private MoveBehaviour currentMoveBehaviour;

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

		// --------------------------------------------
		// Read from arguments
		int row = 0, col = 0;

		Object[] args = getArguments();
		if (args != null && args.length > 0) {
			String[] taxiArgs = new String[args.length];

			for (int i = 0; i < args.length; i++)
				taxiArgs[i] = args[i].toString();

			Cell cellToTest = new Cell(Integer.parseInt(taxiArgs[0]), Integer.parseInt(taxiArgs[1]), 0, false);
			if (cellMap.get(cellToTest) == null || cellMap.get(cellToTest).isWall()) {
				System.out.println("-T >> " + getLocalName() + " >> Invalid row and/or column placement");
				doDelete();
				return;
			} else {
				if (Integer.parseInt(taxiArgs[2]) > 0) {
					row = Integer.parseInt(taxiArgs[0]);
					col = Integer.parseInt(taxiArgs[1]);
					maxCapacity = Integer.parseInt(taxiArgs[2]);
				} else {
					System.out.println("-T >> " + getLocalName() + " >> Invalid max capacity value");
					doDelete();
					return;
				}
			}
		} else {
			row = 4;
			col = 2;
			maxCapacity = 4;
		}

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
						if(!passenger.isSharingPolicy() || travellingPassengers.size() == 0){ // Station has no sharing policy TODO cuidado com o size
							if(travellingPassengers.size() > 0){ // Taxi is already taken by another passenger
								// Refuses order
								ACLMessage refuseOrder = orderMessage.createReply();
								refuseOrder.setPerformative(ACLMessage.DISCONFIRM);
								refuseOrder.setContent("refuse-order");
								myAgent.send(refuseOrder);
							}else{ // Taxi is free to take passengers
								// Saves important checkpoints for notifications
								travellingPassengers.add(passenger);

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

								// Makes the Taxi move
								currentMoveBehaviour = new MoveBehaviour(myAgent);
								addBehaviour(currentMoveBehaviour);
							}
						}else{ // Station has sharing policy
							if(capacity == 0){ // Taxi has no free space to take more passengers
								// Refuses order
								ACLMessage refuseOrder = orderMessage.createReply();
								refuseOrder.setPerformative(ACLMessage.DISCONFIRM);
								refuseOrder.setContent("refuse-order");
								myAgent.send(refuseOrder);
							}else{ // Taxi has free spaces
								// TODO ainda nao esta feito
								// Saves important checkpoints for notifications
								travellingPassengers.add(passenger);

								// Accepts order
								ACLMessage acceptsOrder = orderMessage.createReply();
								acceptsOrder.setPerformative(ACLMessage.CONFIRM);
								acceptsOrder.setContent("accept-order");
								myAgent.send(acceptsOrder);

								// TODO ????? vvvvv
								// Important path points
								PriorityQueue<Cell.CellValue> pathPoints = new PriorityQueue<>();

								// Iterate through all passengers that need to be pickup
								for(DataSerializable.PassengerData p : travellingPassengers){
									// Adds p starting cell to path points
									if(!p.wasPickedUp()){
										if(p.isDiminishingDuration()){
											pathPoints.add(new Cell.CellValue(p.getStartingCell(),
													AStar.PathDuration(
															AStar.AStarAlgorithm(cellMap,
																	positionCell, p.getStartingCell(),
																	p.isDiminishingDuration()))));
										}else{
											pathPoints.add(new Cell.CellValue(p.getStartingCell(),
													AStar.PathDistance(
															AStar.AStarAlgorithm(cellMap,
																	positionCell, p.getStartingCell(),
																	p.isDiminishingDuration()))));
										}
									}

									// Adds ending cell to path points
									if(p.isDiminishingDuration()){
										pathPoints.add(new Cell.CellValue(p.getEndingCell(),
												AStar.PathDuration(
														AStar.AStarAlgorithm(cellMap,
																positionCell, p.getEndingCell(),
																p.isDiminishingDuration()))));
									}else{
										pathPoints.add(new Cell.CellValue(p.getEndingCell(),
												AStar.PathDistance(
														AStar.AStarAlgorithm(cellMap,
																positionCell, p.getEndingCell(),
																p.isDiminishingDuration()))));
									}
								}

								// Creates holder for paths
								Stack<Stack<Cell>> allPaths = new Stack<>();
								// Holds last path position
								Cell lastPathPosition = positionCell;
								do{
									allPaths.push(
											AStar.GetMoveOrders(
													AStar.AStarAlgorithm(cellMap,
															lastPathPosition, pathPoints.remove().getCell(),
															passenger.isDiminishingDuration())));
								}while(!pathPoints.isEmpty());

								// Path is the bottom of the path stack
								path = allPaths.pop();

								// Add this path to current path stack
								while(!allPaths.isEmpty()){
									path = AStar.addStack(path, allPaths.pop());
								}

								// Makes the Taxi move
								currentMoveBehaviour = new MoveBehaviour(myAgent);
								addBehaviour(currentMoveBehaviour);
							}
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

		// Force the taxi to go to station position
		currentMoveBehaviour = new MoveBehaviour(this);
		addBehaviour(currentMoveBehaviour);
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
			if(DFService.search(this, dfd).length != 0 && positionCell != null)
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
		// Display movement
		System.out.println("-T >> " + getLocalName() + " >> From: " + positionCell + ", to: " + nextPosition);

		// Updates taxi position
		positionCell = nextPosition;

		// Passengers to remove
		ArrayList<DataSerializable.PassengerData> passengersToRemove = new ArrayList<>();

		// Checks if any passenger was picked up or traveled
		for(DataSerializable.PassengerData passenger : travellingPassengers){
			// If taxi is picking up passenger
			if(passenger.getStartingCell().equals(positionCell)){
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

				// Displays that this taxi picked up a passenger
				System.out.println("-T >> " + getLocalName() + " >> Just picked passenger: " + passenger.getAID().getLocalName());

				// Informs passenger that taxi will pick up
				ACLMessage pickupPassengerMessage = new ACLMessage(ACLMessage.INFORM);
				pickupPassengerMessage.addReceiver(passenger.getAID());
				pickupPassengerMessage.setConversationId("picking-passenger");
				pickupPassengerMessage.setContent("" + numberOfPassengersLeftBehind);
				send(pickupPassengerMessage);

				// If taxi is delivering a passenger
			}else if(passenger.wasPickedUp() // If passenger was already picked up
					&& passenger.getEndingCell().equals(positionCell)){ // If passenger is at destination
				// Remove passenger from traveling passengers variable
				passengersToRemove.add(passenger);
				// Free taxi capacity spaces
				capacity += passenger.getNumberOfPassengers();

				// Displays that this taxi delivered a passenger
				System.out.println("-T >> " + getLocalName() + " >> Just delivered passenger: " + passenger.getAID().getLocalName());

				// Informs passenger that taxi just traveled the passenger
				ACLMessage deliveredPassenger = new ACLMessage(ACLMessage.INFORM);
				deliveredPassenger.addReceiver(passenger.getAID());
				deliveredPassenger.setConversationId("delivering-passenger");
				deliveredPassenger.setContent("delivered");
				send(deliveredPassenger);
			}
		}

		// Remove delivered passenger from traveling passengers
		for(DataSerializable.PassengerData passenger : passengersToRemove)
			travellingPassengers.remove(passenger);
	}

	private boolean isAdvantageousAux(DataSerializable.PassengerData a, DataSerializable.PassengerData b){
		// Hold path distances
		int originalDistanceA = 0, originalDistanceB = 0;

		// Calculates original path for each passenger
		LinkedList<Cell> pathA = AStar.AStarAlgorithm(cellMap, a.getStartingCell(), a.getEndingCell(), a.isDiminishingDuration());
		LinkedList<Cell> pathB = AStar.AStarAlgorithm(cellMap, b.getStartingCell(), b.getEndingCell(), b.isDiminishingDuration());

		// Hold path combinations distances
		int distanceABA = 0, distanceABBA = 0, distanceBAB = 0;

		// Calculates oath combinations for each passenger
		LinkedList<Cell> pathAiBi = AStar.AStarAlgorithm(cellMap, a.getStartingCell(), b.getStartingCell(), a.isDiminishingDuration());
		LinkedList<Cell> pathBiAj = AStar.AStarAlgorithm(cellMap, b.getStartingCell(), a.getEndingCell(), a.isDiminishingDuration());
		LinkedList<Cell> pathBjAj = AStar.AStarAlgorithm(cellMap, b.getEndingCell(), a.getEndingCell(), a.isDiminishingDuration());

		// Calculates path costs
		if(a.isDiminishingDuration()){
			originalDistanceA = AStar.PathDuration(pathA);
			originalDistanceB = AStar.PathDuration(pathB);

			distanceABA = AStar.PathDuration(pathAiBi) + AStar.PathDuration(pathBiAj);
			distanceABBA = AStar.PathDuration(pathAiBi) + AStar.PathDuration(pathB) + AStar.PathDuration(pathBjAj);

			distanceBAB = AStar.PathDuration(pathBiAj) + AStar.PathDuration(pathBjAj);
		}else{
			originalDistanceA = AStar.PathDistance(pathA);
			originalDistanceB = AStar.PathDistance(pathB);

			distanceABA = AStar.PathDistance(pathAiBi) + AStar.PathDistance(pathBiAj);
			distanceABBA = AStar.PathDistance(pathAiBi) + AStar.PathDistance(pathB) + AStar.PathDistance(pathBjAj);

			distanceBAB = AStar.PathDistance(pathBiAj) + AStar.PathDistance(pathBjAj);
		}

		// Minimal distance to travel both passengers for A and B, respectively
		int forA = Math.min(distanceABA, distanceABBA);
		int forB = distanceBAB;

		// If minimal distance to travel is a lot bigger than the distance of original path
		// It is not worthy to travel another passenger
		if((forA / originalDistanceA) - 1 > COMPENSATION_VALUE)
			return false;

		if((forB / originalDistanceB) - 1 > COMPENSATION_VALUE)
			return false;

		// Returns true if it is advantageous for both passengers
		return true;
	}

	private boolean isAdvantageous(DataSerializable.PassengerData passenger){
		// Array of all passengers
		ArrayList<DataSerializable.PassengerData> passengersToComapareTo = new ArrayList<>(travellingPassengers);
		passengersToComapareTo.add(passenger);

		// Gets all combinations for passengers
		ArrayList<ArrayList<DataSerializable.PassengerData>> temp = powerSet(passengersToComapareTo);
		ArrayList<ArrayList<DataSerializable.PassengerData>> passengersCombinations = new ArrayList<>();
		for(ArrayList<DataSerializable.PassengerData> arr : temp){
			if(arr.size() == 2)
				passengersCombinations.add(arr);
		}

		// Holds results of advantages
		ArrayList<Boolean> result = new ArrayList<>();
		for(ArrayList<DataSerializable.PassengerData> passengerCombination : passengersCombinations){
			result.add(isAdvantageousAux(passengerCombination.get(0), passengerCombination.get(1)));
		}

		// Verifies results
		for(int i = 0; i < result.size(); i++){
			// Returns false if it is not advantageous for at least one passenger
			if(!result.get(i))
				return false;
		}

		// Returns true if all passenger agree that taking the new passenger is advantageous
		return true;
	}

	// Aux functions
	private ArrayList<ArrayList<DataSerializable.PassengerData>> powerSet(ArrayList<DataSerializable.PassengerData> originalArrayList) {
		ArrayList<ArrayList<DataSerializable.PassengerData>> arrayLists = new ArrayList<>();

		if (originalArrayList.isEmpty()) {
			arrayLists.add(new ArrayList<DataSerializable.PassengerData>());
			return arrayLists;
		}

		List<DataSerializable.PassengerData> list = new ArrayList<>(originalArrayList);
		DataSerializable.PassengerData head = list.get(0);

		ArrayList<DataSerializable.PassengerData> rest = new ArrayList<>(list.subList(1, list.size()));
		for (ArrayList<DataSerializable.PassengerData> set : powerSet(rest)) {
			ArrayList<DataSerializable.PassengerData> newArrayList = new ArrayList<>();
			newArrayList.add(head);
			newArrayList.addAll(set);

			arrayLists.add(newArrayList);
			arrayLists.add(set);
		}

		return arrayLists;
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

			// Set initial variables
			this.proposeMessage = proposeMessage;
			this.passengerData = passengerData;

			// Set initial state
			state = PROCESS_PROPOSE;
		}

		// Overrides
		@Override
		public void action() {
			switch (state) {
			case PROCESS_PROPOSE:
				if(!passengerData.isSharingPolicy() || travellingPassengers.size() == 0){ // Station has no sharing policy TODO cuidado com o size
					// Taxi is already taken by another passenger
					if(travellingPassengers.size() != 0){
						state = REFUSE_PROPOSE;
						break;
					}

					// Calculates the fastest path to the passenger
					LinkedList<Cell> path = AStar.AStarAlgorithm(cellMap, positionCell, passengerData.getStartingCell(), true);
					// Calculates the duration of the path
					score = AStar.PathDuration(path);

					state = ACCEPT_PROPOSE;
				}else{ // Station has sharing policy
					// Calculates the number of spacesOccupied in taxi
					int spacesOccupied = 0;
					for(int i = 0; i < travellingPassengers.size(); i++){
						spacesOccupied += travellingPassengers.get(i).getNumberOfPassengers();
					}

					// Taxi has no free spaces
					if(spacesOccupied >= maxCapacity){
						state = REFUSE_PROPOSE;
						break;
					}

					// Verifies if it is advantageous to travel this passenger
					if(!isAdvantageous(passengerData)){
						state = REFUSE_PROPOSE;
						break;
					}

					// Calculates the fastest path to the passenger
					LinkedList<Cell> path = AStar.AStarAlgorithm(cellMap, positionCell, passengerData.getStartingCell(), true);
					// Calculates the duration of the path
					score = AStar.PathDuration(path);

					state = ACCEPT_PROPOSE;
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
		private long uniqueValue;

		private Cell nextCell;

		// Constructor
		public MoveBehaviour(Agent myAgent){
			super(myAgent);

			// Set unique value
			uniqueValue = System.currentTimeMillis();

			// Set initial variables
			nextCell = null;

			// Set initial state
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
				nextCell = path.peek();

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
							if(nextCell.equals(path.peek())){
								// Updates path
								path.pop();

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
								currentMoveBehaviour = new MoveBehaviour(myAgent);
								myAgent.addBehaviour(currentMoveBehaviour);
							}
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
			return ((state == DONE_MOVING) || (this.uniqueValue < currentMoveBehaviour.uniqueValue));
		}
	}
}