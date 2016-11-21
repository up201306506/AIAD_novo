package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class PassengerAgent extends Agent {
	private static final long serialVersionUID = 8180459495842676730L;

	// Passenger dynamic variables
	private int xiCoord;
	private int yiCoord;
	private int xfCoord;
	private int yfCoord;
	private int number;

	protected void setup() {
		// Read from arguments
		xiCoord = 5; // Temporary values
		yiCoord = 5;
		xfCoord = 10;
		yfCoord = 10;
		number = 3;

		// Create passenger agent
		System.out.println("=P >> " + getLocalName() + " >> Just initialized");

		// --------------------------------------------
		// Yellow pages -------------------------------
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());

		try {
			DFService.register(this, dfd);
		} catch (FIPAException fe) {
			System.out.println("=P >> " + getLocalName() + " >> DFService register exception");
			fe.printStackTrace();
		}

		// --------------------------------------------
		// Behaviours ---------------------------------
		// Rquests the taxi information for a taxi
		OneShotBehaviour requestTaxiBehaviour = new OneShotBehaviour(this) {
			private static final long serialVersionUID = 8831324489911981757L;

			@Override
			public void action() {
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
						System.out.println("=P >> " + getLocalName() + " >> Found station >> " + stationAID.getName());
					}else{
						// No stations found
						System.out.println("=P >> " + getLocalName() + " >> Could not find a station");
					}
				} catch (FIPAException fe) {
					fe.printStackTrace();
				}

				if(stationAID != null){
					// Request a taxi if a taxi station was found
					ACLMessage requestTaxi = new ACLMessage(ACLMessage.REQUEST);
					requestTaxi.addReceiver(stationAID);
					requestTaxi.setConversationId("request-pickup");
					requestTaxi.setContent("XI" + xiCoord + "YI" + yiCoord + "XF" + xfCoord + "YF" + yfCoord + "NP" + number);
					myAgent.send(requestTaxi);

					System.out.println("=P >> " + getLocalName() + " >> State is: " + "XI" + xiCoord + "YI" + yiCoord + "XF" + xfCoord + "YF" + yfCoord + "NP" + number);
				}
			}
		};

		addBehaviour(requestTaxiBehaviour);
	}

	@Override
	protected void takeDown() {
		System.out.println("=P >> " + getLocalName() + " >> Terminated");
	}
}