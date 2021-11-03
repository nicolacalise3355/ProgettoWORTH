package progettoReti;

import java.util.ArrayList;
import java.util.Vector;

public class Chats {
	
	/*
	 * OVERVIEW:
	 * classe che rappresenta le Chats di tutti i progetti
	 */
		
	public Vector<Messaggio> messaggi = new Vector<Messaggio>();
	
	public Vector<Messaggio> getMessaggi(){
		return this.messaggi;
	}
	
	//Metodi SET
	
	public void setMessaggi(Vector<Messaggio> msg) {
		this.messaggi = msg;
	}
	
	//Metodi GET
	
	public void addMessaggi(Messaggio msg) {
		this.messaggi.add(msg);
	}
	
	/*
	 * @effects: Stampa tutti i messaggi della chat
	 */
	public void printChat() {
		for(int i = 0; i < this.messaggi.size(); i++) {
			System.out.println("Autore: " + this.messaggi.get(i).getAutore() + " Messaggio: " + this.messaggi.get(i).getMessaggio());
		}
	}

}
