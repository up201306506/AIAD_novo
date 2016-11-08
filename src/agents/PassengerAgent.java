package agents;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class PassengerAgent extends Agent {
	private static final long serialVersionUID = 8180459495842676730L;

	// yellow pages service names
	public static final String _NEW_PASSENGER = "NEW-PASSENGER";

	// this passenger properties headers
	public static final String _X_PASSENGER_STARTING_COORDS = "XSTARTINGPASSENGERCOORD";
	public static final String _Y_PASSENGER_STARTING_COORDS = "YSTARTINGPASSENGERCOORD";
	public static final String _X_PASSENGER_DESTINATION_COORDS = "XDESTINATIONPASSENGERCOORD";
	public static final String _Y_PASSENGER_DESTINATION_COORDS = "YDESTINATIONPASSENGERCOORD";
	public static final String _NUMBER_OF_PASSENGERS = "NUMBEROFPASSENGERS";

	protected void setup() {
		try {
			// create passenger agent interface
			System.out.println(getLocalName() + " is waiting for a taxi!");

			// yellow pages -------------------------------
			// register this passenger as a new-passenger service
			DFAgentDescription dfd = new DFAgentDescription();
			dfd.setName(getAID());

			ServiceDescription sd = new ServiceDescription();
			sd.setName(getLocalName());
			sd.setType(_NEW_PASSENGER);

			// add this taxi service properties
			sd.addProperties(new Property(_X_PASSENGER_STARTING_COORDS, 0));
			sd.addProperties(new Property(_Y_PASSENGER_STARTING_COORDS, 0));
			sd.addProperties(new Property(_X_PASSENGER_DESTINATION_COORDS, 10));
			sd.addProperties(new Property(_Y_PASSENGER_DESTINATION_COORDS, 10));
			sd.addProperties(new Property(_NUMBER_OF_PASSENGERS, 3));

			dfd.addServices(sd);

			DFService.register(this, dfd);
			// --------------------------------------------

		} catch (Exception e) {
			System.err.println("Exception in: " + getLocalName());
			e.printStackTrace();
		}
	}
}