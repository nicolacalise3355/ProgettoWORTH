package progettoReti;

import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;

public class NotifyEventImpl extends RemoteObject implements NotifyEventInterface{

	//Creazione della Callback
	public NotifyEventImpl() throws RemoteException{
		super(); 
	}


	@Override
	public void notifyEvent(int value) throws RemoteException {
		String returnMessage = "Update - Nuovo utente online"; 
		System.out.println(returnMessage);
	}
	
}
