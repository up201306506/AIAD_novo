package agents;

import java.util.HashMap;

import application.Passenger;
import application.Taxi;
import behaviours.AllocatePassengerBehaviour;
import gui.StationGUI;
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

public class TaxiStation extends Agent{
	private static final long serialVersionUID = 488752805219045668L;

	// GUI
	private final int _gui_refresh_rate = 1; // seconds

	private StationGUI stationGUI;
	private HashMap<AID, Taxi> taxisTable;
	private HashMap<AID, Passenger> passengersTable;

	//---------------------------------------------
	@Override
	protected void setup() {
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
		// Variables
		taxisTable = new HashMap<>();
		passengersTable = new HashMap<>();

		// Creates station interface
		stationGUI = new StationGUI();
		System.out.println("#S >> " + getLocalName() + " >> Just initialized");

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
		// Updates GUI Information
		TickerBehaviour updateGUIInformationBehaviour = new TickerBehaviour(this, _gui_refresh_rate * 1000) {
			private static final long serialVersionUID = -3948101162657174560L;

			@Override
			protected void onTick() {
				stationGUI.updateTaxis(taxisTable);
				stationGUI.updatePassengers(passengersTable);
			}
		};

		// Receives information about taxi positions
		CyclicBehaviour receiveTaxiInformationBehaviour = new CyclicBehaviour(this) {
			private static final long serialVersionUID = -5635429047598092196L;

			@Override
			public void action() {
				// Defines the message template to receive
				MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
				ACLMessage msg = myAgent.receive(mt);
				if(msg != null &&  msg.getConversationId().equals("taxi-position")){
					// Creates or replaces the taxi information in the HashMap taxisTable
					taxisTable.put(msg.getSender(), new Taxi(msg.getSender(), msg.getContent()));
				}else{
					block();
				}
			}
		};

		// Receives request for a taxi from a passenger
		CyclicBehaviour receivePassengerRequestBehaviour = new CyclicBehaviour(this) {
			private static final long serialVersionUID = 2641462448146065923L;

			@Override
			public void action() {
				// Defines the message template to receive
				MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
				ACLMessage msg = myAgent.receive(mt);
				if(msg != null && msg.getConversationId().equals("request-taxi")){
					// Creates or replaces the taxi information in the HashMap taxisTable
					Passenger passengerToAllocate = new Passenger(msg.getSender(), msg.getContent());
					passengersTable.put(msg.getSender(), passengerToAllocate);

					// Process allocation request behaviour
					addBehaviour(new AllocatePassengerBehaviour(myAgent, passengerToAllocate));
				}else{
					block();
				}
			}
		};

		addBehaviour(updateGUIInformationBehaviour);
		addBehaviour(receiveTaxiInformationBehaviour);
		addBehaviour(receivePassengerRequestBehaviour);
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

		// Disposes GUI
		stationGUI.dispose();
		System.out.println("#S >> " + getLocalName() + " >> Terminated");
	}

	//---------------------------------------------
	// Auxiliary functions
	/*
	private Taxi allocatePassenger(Passenger passenger){

		if(offServiceTaxis.size() != 0){
			return findNearestTaxi(passenger);
		}

		return null;
	}

	private Taxi findNearestTaxi(Passenger passenger){

		class DistanceToTaxi implements Comparable<DistanceToTaxi> {
			private float distance;
			private Taxi taxi;

			public DistanceToTaxi(float distance, Taxi taxi){
				this.distance = distance;
				this.taxi = taxi;
			}

			public Taxi getTaxi(){
				return taxi;
			}

			@Override
			public boolean equals(Object obj) {
				if(obj == this) return true;

				if(!(obj instanceof DistanceToTaxi)) return false;

				DistanceToTaxi that = (DistanceToTaxi) obj;
				if(this.distance == that.distance
						&& this.taxi.equals(that.taxi)) return true;

				return false;
			}

			public int compareTo(DistanceToTaxi that) {
				return Float.valueOf(this.distance).compareTo(that.distance);
			};
		}

		PriorityQueue<DistanceToTaxi> priorityQueue = new PriorityQueue<>();

		int xCoordPassenger = passenger.getXiCoord();
		int yCoordPassenger = passenger.getYiCoord();

		for(Taxi taxi : offServiceTaxis){
			int xCoordTaxi = taxi.getXCoord();
			int yCoordTaxi = taxi.getYCoord();

			float distance = DistanceFormula(xCoordPassenger, yCoordPassenger, xCoordTaxi, yCoordTaxi);

			priorityQueue.add(new DistanceToTaxi(distance, taxi));
		}

		return priorityQueue.peek().getTaxi();
	}

	//---------------------------------------------
	// Math functions

	private float DistanceFormula(int x1, int y1, int x2, int y2){
		return (float) Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
	}
	 */
}