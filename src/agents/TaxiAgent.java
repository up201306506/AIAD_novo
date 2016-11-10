package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

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
		capacity = maxCapacity;

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

		addBehaviour(new TickerBehaviour(this, 2000) {

			@Override
			protected void onTick() {
				xCoord++;
			}
		});

		addBehaviour(informPositionBehaviour);
		// --------------------------------------------


		/*
			// Setting up the taxi agent behaviours
			// Taxi State Machine
			FSMBehaviour fsm = new FSMBehaviour(this);

			// Waiting for passengers
			TickerBehaviour waitingForPassengersBehaviour = new TickerBehaviour(this, 1000){ // 1 second ticker
				private static final long serialVersionUID = -1145135997995313675L;

				protected void onTick(){
					// Do something on Tick
					System.out.println("Taxi waiting ...");

					if(getTickCount() == 3)
						stop();
				}
			};

			fsm.registerFirstState(waitingForPassengersBehaviour, _STATE_WAITING_PASSENGERS);

			// Picking up passengers
			SimpleBehaviour pickPassengerBehaviours = new SimpleBehaviour(this){
				private boolean isDone = false;

				@Override
				public void action() {
					System.out.println("It ended!!");
					isDone = true;
				}

				@Override
				public boolean done() {
					return isDone;
				}
			};

			// Servicing passengers

			SimpleBehaviour simpleEndTest = new SimpleBehaviour(this){
				private boolean isDone = false;

				@Override
				public void action() {
					System.out.println("It ended!!");
					isDone = true;
				}

				@Override
				public boolean done() {
					return isDone;
				}
			};

			fsm.registerLastState(simpleEndTest, _STATE_TEST);

			//fsm.registerState(BEHAVIOUR, STATE);
			//fsm.registerLastState(BEHAVIOUR, STATE);

			// States sequence / transitions
			fsm.registerDefaultTransition(_STATE_WAITING_PASSENGERS, _STATE_TEST);

			// Add state machine behaviour to agent
			addBehaviour(fsm);*/
	}

	@Override
	protected void takeDown() {
		System.out.println("-T >> " + getLocalName() + " >> Terminated");
	}
}