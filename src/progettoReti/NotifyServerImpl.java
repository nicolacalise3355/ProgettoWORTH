package progettoReti;

import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


public class NotifyServerImpl extends RemoteServer implements NotifyServerInterface{

	/* lista dei client registrati */
	private List<NotifyEventInterface> clients;
	public HashMap<String, Boolean> status = new HashMap<String, Boolean>(); //Hashmap che
	//rappresenta lo stato degli utenti, coppia Nome - Status
	
	//metodo costruttore
	public NotifyServerImpl() throws RemoteException {
		clients = new ArrayList<NotifyEventInterface>();
	}

	@Override
	public synchronized void registerForCallback(NotifyEventInterface ClientInterface) throws RemoteException {
		if (!clients.contains(ClientInterface)){
			clients.add(ClientInterface); 
			System.out.println("New client registered.");
		}
		
	}

	@Override
	public synchronized void unregisterForCallback(NotifyEventInterface ClientInterface) throws RemoteException {
		if(clients.remove(ClientInterface)){ 
			System.out.println("Client unregistered");
		}else{
			System.out.println("Unable to unregister client"); 
		}
	}
	
	
	/*
	 * @params: value = int
	 * @effects: quando chiamata esegue la callback a tutti i client registrati
	 */
	public void update(int value) throws RemoteException { 
		doCallbacks(value); 
	}
	
	/*
	 * @params: value = int
	 * @effects: scorre tutta la lista di client registrati, poi Esegue la callback
	 * @note: da notare che il metodo e' di tipo "Synchronized"
	 */
	private synchronized void doCallbacks(int value) throws RemoteException{
		System.out.println("Starting callbacks.");
		Iterator i = clients.iterator();
		while (i.hasNext()){ 
			NotifyEventInterface client = (NotifyEventInterface) i.next();
			client.notifyEvent(value);
		}
		System.out.println("Callbacks complete.");
	}
	
}
