package progettoReti;


import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.concurrent.locks.ReentrantLock;

public interface RegistererInterface extends Remote{
	
	/*
	 * OVERVIEW: Interfaccie da implementare per un mini Server che modifica il file System 
	 * che contiene il Database degli utenti
	 */

	
	
	/*
	 * @params: s deve essere una stringa JSON
	 * @effects: 
	 * 	1) attraverso S viene ricorstruito il Dato che il client manda
	 * 	2) Viene ricostruito il Databse attraverso il JSON nel file system
	 * 	3) Viene aggiunto l'utente con un metodo del Dato all'oggetto del DB ricostruito
	 * 	4) Salviamo nel FileSystem
	 * 	@return: True se l'operazione ha successo False altrimenti
	 */
	boolean registerAccount(String s) throws RemoteException, InterruptedException;
	

}
