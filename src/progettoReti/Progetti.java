package progettoReti;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class Progetti {
	
	/*
	 * OVERVIEW: Classe che rappresenta i progetti creati dagli utenti
	 */
	
	private String name; //nome progetto
	private String multicastAddress; //indirizzo multicast
	private ArrayList<Cards> cards = new ArrayList<Cards>(); //lista delle cards del progetto
	private ArrayList<String> partecipanti = new ArrayList<String>(); //lista dei membri
	
	//
	private HashMap<String, Cards> todo = new HashMap<>();
	private HashMap<String, Cards> inprogress = new HashMap<>();
	private HashMap<String, Cards> toberevised = new HashMap<>();
	private HashMap<String, Cards> done = new HashMap<>();
	
	//
	
	/*
	 * Metodi GET/SET/ADD
	 * 
	 * Metodi che servono per recuperare una variabile di istanza (GET), settare il suo valore (SET)
	 * o aggiungere un eventuale elemento ad un ArrayList (ADD)
	 * 
	 */
	
	public HashMap<String, Cards> getTODO(){
		return this.todo;
	}
	public HashMap<String, Cards> getINPROGRESS(){
		return this.inprogress;
	}
	public HashMap<String, Cards> getTOBEREVISED(){
		return this.toberevised;
	}
	public HashMap<String, Cards> getDONE(){
		return this.done;
	}
	
	public void setMulticastAddress(String ma) {
		this.multicastAddress = ma;
	}
	
	public String getMulticastAddress() {
		return this.multicastAddress;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setPartecipanti(ArrayList<String> p) {
		this.partecipanti = p;
	}
	
	public ArrayList<String> getPartecipanti() {
		return this.partecipanti;
	}
	
	public void addPartecipanti(String p) {
		this.partecipanti.add(p);
	}
	
	public void setCards(ArrayList<Cards> c) {
		this.cards = c;
	}
	
	public ArrayList<Cards> getCards() {
		return this.cards;
	}
	
	public void addCards(Cards c) {
		for(int i = 0; i < this.cards.size(); i++) {
			if(this.cards.get(i).getNome().equals(c.getNome())) {
				return;
			}
		}
		this.cards.add(c);
		this.todo.put(c.getNome(), c);
	}
	
	
	/*
	 * @params: nome della cards da recuperare
	 * @effects: controlla tutte le strutture dati e se trova la cards che cerchiamo la restituisce
	 * @return: null se non trova nulla altrimenti la cards che stiamo cercando
	 */
	public Cards getThisCard(String name) {
		if(this.todo.containsKey(name)) {
			return this.todo.get(name);
		}
		if(this.inprogress.containsKey(name)) {
			return this.inprogress.get(name);
		}
		if(this.toberevised.containsKey(name)) {
			return this.toberevised.get(name);
		}
		if(this.done.containsKey(name)) {
			return this.done.get(name);
		}
		return null;
	}
	
	/*
	 * @params:
	 * @effects:
	 * @return:
	 */
	public boolean moveCard(String card, int input) {
		int currentCode;
		if(this.todo.containsKey(card)) {
			currentCode = 1;
		}else if(this.inprogress.containsKey(card)) {
			currentCode = 2;
		}else if(this.toberevised.containsKey(card)) {
			currentCode = 3;
		}else if(this.done.containsKey(card)) {
			currentCode = 4;
		}else {
			currentCode = -1;
			return false;
		}
		
		boolean result = getNewState(input,currentCode);
		Cards tmp = null;
		
		if(result == true) {
			if(currentCode == 1) {
				tmp = this.todo.get(card);
				this.todo.remove(card);
			}else if(currentCode == 2) {
				tmp = this.inprogress.get(card);
				this.inprogress.remove(card);
			}else if(currentCode == 3) {
				tmp = this.toberevised.get(card);
				this.toberevised.remove(card);
			}else if(currentCode == 4) {
				tmp = this.done.get(card);
				this.done.remove(card);
			}
			//da aggiunere alla lista nuova 
			if(input == 1 && tmp != null) {
				this.todo.put(card, tmp);
			}else if(input == 2 && tmp != null) {
				this.inprogress.put(card, tmp);
			}else if(input == 3 && tmp != null) {
				this.toberevised.put(card, tmp);
			}else if(input == 4 && tmp != null) {
				this.done.put(card, tmp);
			}
			
		}else {
			//non va bene il cambio
			return false;
		}
		return false;
	}
	
	
	
	public boolean getNewState(int input, int stato) {
		if(input >= 1 && input <= 4) {
			if(stato == 1 && input == 2) {
				return true;
			}else if(stato == 2 && (input == 3 || input == 4)){
				return true;
			}else if(stato == 3 && (input == 2 || input == 4)) {
				return true;
			}else {
				return true;
			}
		}
		return false;
	}
	
	

}
