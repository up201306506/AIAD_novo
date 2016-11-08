package agents;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class TaxiAgent extends Agent {
	private static final long serialVersionUID = 163911234618964268L;

	// yellow pages service names
	public static final String _NEW_TAXI = "NEW-TAXI";

	// this taxi properties headers
	public static final String _X_TAXI_COORDS_PROPERTY = "XTAXICOORDS";
	public static final String _Y_TAXI_COORDS_PROPERTY = "YTAXICOORDS";
	public static final String _TAXI_CAPACITY_PROPERTY = "TAXICAPACITY";

	protected void setup(){
		try {
			// create taxi agent interface
			System.out.println(getLocalName() + " is waiting for passengers!");

			// yellow pages -------------------------------
			// register this taxi as a new-taxi service
			DFAgentDescription dfd = new DFAgentDescription();
			dfd.setName(getAID());

			ServiceDescription sd = new ServiceDescription();
			sd.setName(getLocalName());
			sd.setType(_NEW_TAXI);

			// add this taxi service properties
			sd.addProperties(new Property(_X_TAXI_COORDS_PROPERTY, 1));
			sd.addProperties(new Property(_Y_TAXI_COORDS_PROPERTY, 2));
			sd.addProperties(new Property(_TAXI_CAPACITY_PROPERTY, 4));

			dfd.addServices(sd);

			DFService.register(this, dfd);
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

		} catch (Exception e) {
			System.err.println("Exception in: " + getLocalName());
			e.printStackTrace();
		}
	}
}