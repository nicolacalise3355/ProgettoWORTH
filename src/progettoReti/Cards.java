package progettoReti;

import java.util.ArrayList;

public class Cards {
	
	/*OVERVIEW:
	 * Classe che rappresenta le cards (Tasks)
	 * Le cards hanno una serie di proprieta' modificabili tramite 
	 * i metodi di classe
	 */
	
	//variabili di istanza
	
	private String nome; //nome della card
	private String desc; //descrizione estesa della cards
	private int stato = 1; //stato della cards  1 <= stato <= 4 
	private ArrayList<Integer> history = new ArrayList<Integer>(); //lista degli storici degli stati
	//metodo costruttore
	public Cards() {
		this.history.add(1);
	}
	
	//metodi GET
	
	public ArrayList<Integer> getHistory() {
		return this.history;
	}
	
	
	public String getNome(){
		return this.nome;
	}
	
	public String getDesc(){
		return this.desc;
	}
	
	public int getStato(){
		return this.stato;
	}
	
	//metodi SET
	
	public void setHistory(ArrayList<Integer> h) {
		this.history = h;
	}
	
	public void setNome(String nome) {
		this.nome = nome;
	}
	
	public void setDesc(String desc) {
		this.desc = desc;
	}
	
	/*
	 * @params: input = int stato compreso tra 1 e 4
	 * @effects: Segue lo schema di un grafo per decidere se il 
	 * cambiamento e' valido o no
	 * @modify: this
	 */
	public void setStato(int input) {
		
		if(input >= 1 && input <= 4) {
			if(this.stato == 1 && input == 2) {
				this.stato = input;
				this.history.add(input);
			}else if(this.stato == 2 && (input == 3 || input == 4)){
				this.stato = input;
				this.history.add(input);
			}else if(this.stato == 3 && (input == 2 || input == 4)) {
				this.stato = input;
				this.history.add(input);
			}else {
				//stato non modificabile
			}
		}
		
		//this.stato = input;
	}
	
	

}












