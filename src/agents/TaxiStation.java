package agents;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;

import application.Passenger;
import application.Taxi;
import application.Travel;
import gui.StationGUI;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.proto.SubscriptionInitiator;

public class TaxiStation extends Agent{
	private static final long serialVersionUID = 488752805219045668L;

	// variables used
	private StationGUI stationGUI;

	private ArrayList<Taxi> offServiceTaxis; // taxis not travelling with passengers
	private LinkedList<Passenger> passengersQueue; // passengers waiting for taxis to pick them up
	private ArrayList<Travel> currentTravel; // current travelling taxis with passengers

	//---------------------------------------------
	@Override
	protected void setup() {
		try {
			// initializes all needed variables
			offServiceTaxis = new ArrayList<>();
			passengersQueue = new LinkedList<>();
			currentTravel = new ArrayList<>();

			// creates station interface
			stationGUI = new StationGUI();
			System.out.println(getLocalName() + " just started!");

			// --------------------------------------------
			// behaviours ---------------------------------
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
			addBehaviour(subscriptionNewPassengerInitiatorBehaviour);

			addBehaviour(new CyclicBehaviour(this) {

				@Override
				public void action() {
					System.out.println(offServiceTaxis.size() + " / " + passengersQueue.size());
				}
			});

			//---------------------------------------------
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void takeDown() {
		// disposes GUI
		stationGUI.dispose();
		System.out.println(getLocalName() + " closed!");

		// unsubscribe from yellow pages
		try {
			DFService.deregister(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//---------------------------------------------
	// Auxiliary functions

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
}