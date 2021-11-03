package progettoReti;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;

public interface ListerInterface extends Remote{
	
	/*
	 * OVERVIEW: Interfaccia da implementare per una classe che tramite RMI creera' una lista
	 * di utenti da restituire al client
	 */

	/*
	 * @effects: recupera dal file system i dati degli utenti e costruisce una lista
	 * da mandare al client, genera una string JSON
	 * @return: ArrayList di utenti
	 */
	 String getList() throws RemoteException;
	 
	 /*
	  * @effects: recupera la lista di utenti online e costruire una stringa JSON con la lista
	  * di utenti online
	  * @return: String con lista di utenti
	  */
	 String getOnlineUsers() throws RemoteException;
	 
	 /*
	  * @params: name = String -> nome dell'utente da aggiornare 
	  * @effects: Aggiorna lo stato dell'utente con il nome del param
	  */
	 void updateUserStatus(String name) throws RemoteException;
	 
	 /*
	  * @params: String -> nome utente
	  * @effects: controlliamo se l'utente e' gia online
	  * @return: TRUE se online, FALSE altrimenti
	  */
	 boolean findOnlineUser(String user) throws RemoteException;
	 
}
