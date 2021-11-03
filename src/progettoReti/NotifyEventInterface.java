package progettoReti;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface NotifyEventInterface extends Remote{
	
	/*
	 * @params: value = int 
	 * @effects: Metodo invocato dal server per notificare che un nuovo utente e' online
	 */
	public void notifyEvent(int value) throws RemoteException;

}
