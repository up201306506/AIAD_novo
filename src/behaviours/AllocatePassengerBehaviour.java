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

	// Variables used
	private Agent myAgent;
	private Passenger passengerToAllocate;
	private boolean hasSharePolicy;

	private PriorityQueue<TaxiDistance> taxisQueue;

	// State machine
	private int state = 0;

	private MessageTemplate uniqueConversationValue;

	private int numberOfTaxisMessaged;
	private int numberOfTaxisAnswers;

	private TaxiDistance bestTaxi;

	// Constructor
	public AllocatePassengerBehaviour(Agent myAgent, Passenger passengerToAllocate, boolean hasSharePolicy){
		this.myAgent = myAgent;
		this.passengerToAllocate = passengerToAllocate;
		this.hasSharePolicy = hasSharePolicy;

		taxisQueue = new PriorityQueue<>();
	}

	@Override
	public void action() {
		switch (state) {
		case 0:
			System.err.println("State 0");

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
			ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
			// Adds all taxis as receivers
			for(DFAgentDescription agentDescription : searchResult)
				request.addReceiver(agentDescription.getName());
			// Unique value to identify this conversation
			request.setReplyWith("" + System.currentTimeMillis());
			// Conversation topic and content
			request.setConversationId("request-taxi");
			request.setContent("S" + (hasSharePolicy ? "1" : "0") +
					"X" + passengerToAllocate.getXiCoord() + "Y" + passengerToAllocate.getYiCoord() +
					"C" + passengerToAllocate.getNumberOfPassengers());
			myAgent.send(request);

			// Holds the conversation unique value for future references
			uniqueConversationValue = MessageTemplate.MatchInReplyTo(request.getReplyWith());

			state = 1;
			break;
		case 1:
			// Receive all taxis answers and refuses
			MessageTemplate messageTemplate = MessageTemplate.and(
					uniqueConversationValue,
					MessageTemplate.or(
							MessageTemplate.MatchPerformative(ACLMessage.REFUSE),
							MessageTemplate.MatchPerformative(ACLMessage.INFORM)));

			ACLMessage reply = myAgent.receive(messageTemplate);
			if(reply != null){
				if(reply.getPerformative() == ACLMessage.REFUSE){ // For refuses
					// Increments the number of the taxis that answered to this request
					numberOfTaxisAnswers++;

					// Process refuses
					if(!reply.getContent().equals("refuse"))
						System.err.println("At: AllocatePassengersBehaviour.java, state = 2, unexpected answer from refuse");

				}else if(reply.getPerformative() == ACLMessage.INFORM){ // For valid information
					// Increments the number of the taxis that answered to this request
					numberOfTaxisAnswers++;

					System.err.println(reply.getSender().getName());

					// Adds information to a priority queue to be processed
					taxisQueue.add(new TaxiDistance(reply.getSender(), reply.getContent()));
				}else // For errors
					System.err.println("At: AllocatePassengersBehaviour.java, state = 2, unexpected performative");

				// End this state if all taxis answered
				if(numberOfTaxisAnswers == numberOfTaxisMessaged){
					state = 2;
				}
			}else{
				block();
			}
			break;
		case 2:
			System.err.println("State 2");

			// Iterate through all available taxis to choose the best
			bestTaxi = null;

			// Sharing policy block
			if(hasSharePolicy){
				// TODO choose a taxi to sharing policy
			}else{
				while(!taxisQueue.isEmpty()){
					TaxiDistance taxiHolder = taxisQueue.peek();

					// Verifies if the taxi is empty
					if(taxiHolder.getMaxCapacity() > 0
							&& taxiHolder.getMaxCapacity() == taxiHolder.getCapacity()){
						bestTaxi = taxisQueue.remove();
						break;
					}
				}
			}

			// If no taxi follows the requirements
			if(taxisQueue.isEmpty() && bestTaxi == null)
				state = 0;
			else if(bestTaxi == null)
				state = 2;

			// Notifies the taxi to pick up the passenger
			// Create message to request taxis
			ACLMessage allocate = new ACLMessage(ACLMessage.REQUEST);
			allocate.addReceiver(bestTaxi.getTaxiAID());
			// Unique value to identify this conversation
			allocate.setReplyWith("" + System.currentTimeMillis());
			// Conversation topic and content
			allocate.setConversationId("allocate-taxi");
			allocate.setContent("X" + passengerToAllocate.getXiCoord() + "Y" + passengerToAllocate.getYiCoord());
			myAgent.send(allocate);

			// Holds the conversation unique value for future references
			uniqueConversationValue = MessageTemplate.MatchInReplyTo(allocate.getReplyWith());

			state = 3;
			break;
		case 3:
			// Receive taxi ok
			MessageTemplate msgTemplate = MessageTemplate.and(uniqueConversationValue, MessageTemplate.MatchPerformative(ACLMessage.INFORM));

			ACLMessage answer = myAgent.receive(msgTemplate);
			if(answer != null){
				if(answer.getPerformative() == ACLMessage.INFORM){
					// Checks  if content is correct
					if(answer.getContent().equals("ok"))
						state = 4;
					else
						System.err.println("At: AllocatePassengersBehaviour.java, state = 3, unexpected answer from inform");
				}else // For errors
					System.err.println("At: AllocatePassengersBehaviour.java, state = 3, unexpected performative");
			}else{
				block();
			}
			break;
		case 4:
			System.err.println("State 4");
			break;
		}
	}

	@Override
	public void reset() {
		super.reset();
	}

	@Override
	public boolean done() {
		// TODO
		return false;
	}
}