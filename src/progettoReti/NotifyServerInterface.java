package progettoReti;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface NotifyServerInterface extends Remote{

	/*
	 * @params: ClientInterface -> interfaccia di notifica, classe definita da noi
	 * @effects: registriamo un nuovo client al servizio di callback
	 */
	public void registerForCallback(NotifyEventInterface ClientInterface) throws RemoteException;
	
	/*
	 * @params: ClientInterface -> interfaccia di notifica, classe definita da noi
	 * @effects: disinscriviamo un client dal servizio di callback
	 */
	public void unregisterForCallback(NotifyEventInterface ClientInterface) throws RemoteException;
	
}
