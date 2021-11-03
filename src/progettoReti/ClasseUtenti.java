package progettoReti;

import java.util.ArrayList;

public class ClasseUtenti {
	
	/*
	 * OVERVIEW: Classe di gestione utenti
	 * La classe e' utile per la creazione e serializzazione del JSON in modo
	 * da poter contenere una lista degli utenti
	 */
	
	private ArrayList<Utenti> listaUtenti = new ArrayList<Utenti>(); //lista utenti
	
	/*
	 * Metodi Set/Add
	 * REQUIRES: Params sia un valore ammissibile
	 * MODIFY: This
	 * EFFECTS: settano / aggiungono valori alle variabili di istanza
	 */
	
	public void addUtente(Utenti u) {
		this.listaUtenti.add(u);
	}
	
	
	public void addListaUtenti(ArrayList<Utenti> listaUtenti) {
		this.listaUtenti = listaUtenti;
	}
	
	/*
	 * Metodi Get
	 * EFFECTS: Ritornano i valori delle variabili di istanza
	 * RETURN: il valore associato alla var di istanza
	 */
	
	
	public ArrayList<Utenti> getListaUtenti(){
		return this.listaUtenti;
	}

}
