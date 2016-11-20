package behaviours;

import java.util.PriorityQueue;

import application.Passenger;
import application.TaxiDistance;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class AllocatePassengerBehaviour extends Behaviour {
	private static final long serialVersionUID = 3244217582858500088L;

	// State machine
	private int state = 0;

	// Variables used
	private Agent myAgent;
	private Passenger passengerToAllocate;

	private int numberOfTaxisMessaged;
	private int numberOfTaxisAnswers;
	private PriorityQueue<TaxiDistance> distances;

	// Constructor
	public AllocatePassengerBehaviour(Agent myAgent, Passenger passengerToAllocate){
		this.myAgent = myAgent;
		this.passengerToAllocate = passengerToAllocate;

		distances = new PriorityQueue<>();
	}

	@Override
	public void action() {
		switch (state) {
		case 0:
			// Prepare search for taxis
			DFAgentDescription dfTemplate = new DFAgentDescription();
			ServiceDescription serviceTemplate = new ServiceDescription();
			serviceTemplate.setType("taxi");
			dfTemplate.addServices(serviceTemplate);

			// Search for taxis
			DFAgentDescription[] searchResult = null;
			while(searchResult == null){
				try {
					searchResult = DFService.search(myAgent, dfTemplate);
				} catch (FIPAException fe) {
					fe.printStackTrace();
				}
			}

			// Holds the number of taxis messaged for later use
			numberOfTaxisMessaged = searchResult.length;
			numberOfTaxisAnswers = 0;

			// Create message to request taxis
			ACLMessage requestInfo = new ACLMessage(ACLMessage.REQUEST);
			// Adds all taxis as receivers
			for(DFAgentDescription agentDescription : searchResult){
				requestInfo.addReceiver(agentDescription.getName());
			}
			requestInfo.setConversationId("request-taxi-info");
			requestInfo.setContent("X" + passengerToAllocate.getXiCoord() + "Y" + passengerToAllocate.getYiCoord());
			myAgent.send(requestInfo);

			state = 1;
			break;
		case 1:
			// Receive all taxis infos
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
			ACLMessage taxiReply = myAgent.receive(mt);
			if(taxiReply != null && taxiReply.getConversationId().equals("request-taxi-info-answer")){
				// Adds taxi AID and its distance to the passenger to priority queue
				distances.add(new TaxiDistance(taxiReply.getSender(), Integer.parseInt(taxiReply.getContent())));

				// Increments number of the taxis that answered to the request
				numberOfTaxisAnswers++;

				// Checks if all taxis that a request was sent, answered
				if(numberOfTaxisAnswers == numberOfTaxisMessaged)
					state = 2;
			}else{
				block();
			}
			break;
		case 2:
			System.err.println(distances.peek().getDistance());
			break;
		}
	}

	@Override
	public boolean done() {
		// TODO
		return false;
	}
}