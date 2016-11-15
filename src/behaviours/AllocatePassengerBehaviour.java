package behaviours;

import application.Passenger;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class AllocatePassengerBehaviour extends OneShotBehaviour {
	private static final long serialVersionUID = 3244217582858500088L;

	// Variables used
	private Agent myAgent;
	private Passenger passengerToAllocate;

	// Constructor
	public AllocatePassengerBehaviour(Agent myAgent, Passenger passengerToAllocate){
		this.myAgent = myAgent;
		this.passengerToAllocate = passengerToAllocate;
	}

	@Override
	public void action() {
		int state = 0;

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

			// TODO Comunicação com os táxis para alocar passageiro, seguir esquema de caderno
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
			ACLMessage msgInfo = myAgent.receive(/* TODO template */);
			if(msgInfo != null){
				// TODO guarda informações
			}else{
				block();
			}
			break;
		}
	}
}