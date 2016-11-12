package agents;

import java.util.HashMap;

import application.Passenger;
import application.Taxi;
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

		// Allocate a taxi to a passenger order behaviour
		final TickerBehaviour allocateTaxiToPassengerBehaviour = new TickerBehaviour(this, 5000) {
			private static final long serialVersionUID = 666800492457248517L;

			@Override
			protected void onTick() {
				// Prepare search for taxis
				DFAgentDescription dfTemplate = new DFAgentDescription();
				ServiceDescription serviceTemplate = new ServiceDescription();
				serviceTemplate.setType("taxi");
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
					passengersTable.put(msg.getSender(), new Passenger(msg.getSender(), msg.getContent()));

					// TODO
					addBehaviour(allocateTaxiToPassengerBehaviour);
				}else{
					block();
				}
			}
		};

		addBehaviour(updateGUIInformationBehaviour);
		addBehaviour(receiveTaxiInformationBehaviour);
		addBehaviour(receivePassengerRequestBehaviour);

		/*
			// new taxi services subscription notifications behaviour
			DFAgentDescription subscriptionNewTaxiDFTemplate = new DFAgentDescription();
			ServiceDescription subscriptionNewTaxiServiceTemplate = new ServiceDescription();
			subscriptionNewTaxiServiceTemplate.setType(TaxiAgent._NEW_TAXI);
			subscriptionNewTaxiDFTemplate.addServices(subscriptionNewTaxiServiceTemplate);

			SearchConstraints scNewTaxi = new SearchConstraints();

			SubscriptionInitiator subscriptionNewTaxiInitiatorBehaviour = new SubscriptionInitiator(this,
					DFService.createSubscriptionMessage(this, getDefaultDF(), subscriptionNewTaxiDFTemplate, scNewTaxi)){
				private static final long serialVersionUID = 8934123373647737440L;

				@Override
				protected void handleInform(ACLMessage informNewTaxi) {
					try {
						DFAgentDescription[] subscriptionNewTaxiNotificationResults = DFService.decodeNotification(informNewTaxi.getContent());

						if(subscriptionNewTaxiNotificationResults.length > 0)
							for(int i = 0; i < subscriptionNewTaxiNotificationResults.length; i++){
								DFAgentDescription dfNewTaxiResultsDescription = subscriptionNewTaxiNotificationResults[i];
								AID providerNewTaxi = dfNewTaxiResultsDescription.getName();

								// search agent to check for the new-taxi service
								Iterator<?> it = dfNewTaxiResultsDescription.getAllServices();
								while(it.hasNext()){
									ServiceDescription serviceResultsDescription = (ServiceDescription) it.next();
									if(serviceResultsDescription.getType().equals(TaxiAgent._NEW_TAXI)){
										String taxiName = providerNewTaxi.getLocalName();
										int taxiXCoord = -1, taxiYCoord = -1, taxiCapacity = -1;

										Iterator<?> itProperties = serviceResultsDescription.getAllProperties();
										while(itProperties.hasNext()){
											Property property = (Property) itProperties.next();
											switch (property.getName()) {
											case TaxiAgent._X_TAXI_COORDS_PROPERTY:
												taxiXCoord = Integer.parseInt((String) property.getValue());
												break;
											case TaxiAgent._Y_TAXI_COORDS_PROPERTY:
												taxiYCoord = Integer.parseInt((String) property.getValue());
												break;
											case TaxiAgent._TAXI_CAPACITY_PROPERTY:
												taxiCapacity = Integer.parseInt((String) property.getValue());
												break;
											}
										}

										// add taxi to off service taxis
										offServiceTaxis.add(new Taxi(providerNewTaxi, taxiXCoord, taxiYCoord, taxiCapacity));

										// add taxi to GUI
										stationGUI.addTaxiToStation(taxiName, taxiXCoord, taxiYCoord, taxiCapacity);
									}
								}
							}
					} catch (FIPAException e) {
						e.printStackTrace();
					}
				}
			};

			// new passenger subscription notifications behaviour
			DFAgentDescription subscriptionNewPassengerDFTemplate = new DFAgentDescription();
			ServiceDescription subscriptionNewPassengerServiceTemplate = new ServiceDescription();
			subscriptionNewPassengerServiceTemplate.setType(PassengerAgent._NEW_PASSENGER);
			subscriptionNewPassengerDFTemplate.addServices(subscriptionNewPassengerServiceTemplate);

			SearchConstraints scNewPassenger = new SearchConstraints();

			SubscriptionInitiator subscriptionNewPassengerInitiatorBehaviour = new SubscriptionInitiator(this,
					DFService.createSubscriptionMessage(this, getDefaultDF(), subscriptionNewPassengerDFTemplate, scNewPassenger)){
				private static final long serialVersionUID = 8934123373647737440L;

				@Override
				protected void handleInform(ACLMessage informNewPassenger) {
					try {
						DFAgentDescription[] subscriptionNewPassengerNotificationResults = DFService.decodeNotification(informNewPassenger.getContent());

						if(subscriptionNewPassengerNotificationResults.length > 0)
							for(int i = 0; i < subscriptionNewPassengerNotificationResults.length; i++){
								DFAgentDescription dfNewPassengerResultsDescription = subscriptionNewPassengerNotificationResults[i];
								AID providerNewPassenger = dfNewPassengerResultsDescription.getName();

								// search agent to check for the new-passenger service
								Iterator<?> it = dfNewPassengerResultsDescription.getAllServices();
								while(it.hasNext()){
									ServiceDescription serviceResultsDescription = (ServiceDescription) it.next();
									if(serviceResultsDescription.getType().equals(PassengerAgent._NEW_PASSENGER)){
										String passengerName = providerNewPassenger.getLocalName();
										int passengerStartingXCoord = -1, passengerStartingYCoord = -1,
												passengerDestinationXCoord = -1, passengerDestinationYCoord = -1,
												numberOfPassangers = -1;

										Iterator<?> itProperties = serviceResultsDescription.getAllProperties();
										while(itProperties.hasNext()){
											Property property = (Property) itProperties.next();
											switch (property.getName()) {
											case PassengerAgent._X_PASSENGER_STARTING_COORDS:
												passengerStartingXCoord = Integer.parseInt((String) property.getValue());
												break;
											case PassengerAgent._Y_PASSENGER_STARTING_COORDS:
												passengerStartingYCoord = Integer.parseInt((String) property.getValue());
												break;
											case PassengerAgent._X_PASSENGER_DESTINATION_COORDS:
												passengerDestinationXCoord = Integer.parseInt((String) property.getValue());
												break;
											case PassengerAgent._Y_PASSENGER_DESTINATION_COORDS:
												passengerDestinationYCoord = Integer.parseInt((String) property.getValue());
												break;
											case PassengerAgent._NUMBER_OF_PASSENGERS:
												numberOfPassangers = Integer.parseInt((String) property.getValue());
												break;
											}
										}

										// add passenger to taxi station list
										passengersQueue.add(new Passenger(providerNewPassenger,
												passengerStartingXCoord, passengerStartingYCoord,
												passengerDestinationXCoord, passengerDestinationYCoord,
												numberOfPassangers));

										// add passenger to GUI
										stationGUI.addPassengerToStation(passengerName,
												passengerStartingXCoord, passengerStartingYCoord,
												passengerDestinationXCoord, passengerDestinationYCoord, numberOfPassangers);
									}
								}
							}
					} catch (FIPAException e) {
						e.printStackTrace();
					}
				}
			};

			// passengers allocation to taxis behaviour
			CyclicBehaviour passengersAllocationBehaviour = new CyclicBehaviour(this) {

				@Override
				public void action() {
					if(passengersQueue.size() != 0){
						// considering only non shared taxis
						Passenger passengerToAllocate = passengersQueue.peekFirst();
						Taxi taxiAllocated = allocatePassenger(passengerToAllocate);
						if(taxiAllocated != null){
							// Send a message to a taxi to pick up a passenger

						}
					}
				}
			};

			// --------------------------------------------
			// add behaviours to station
			addBehaviour(subscriptionNewTaxiInitiatorBehaviour);
			addBehaviour(subscriptionNewPassengerInitiatorBehaviour);*/

		/*
			addBehaviour(new CyclicBehaviour(this) {

				@Override
				public void action() {
					System.out.println(offServiceTaxis.size() + " / " + passengersQueue.size());
				}
			});*/
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