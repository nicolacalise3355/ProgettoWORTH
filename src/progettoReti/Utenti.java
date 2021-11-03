package progettoReti;

import java.util.ArrayList;

public class Utenti {
	
	/*
	 * OVERVIEW: Classe utenti rappresenta gli utenti che possono
	 * usufruire del servizio, con tutti i metodi necessari per recuperare i
	 * dati 
	 */
	
	private String username; //nome dell'utente
	private String password; //password dell'utente
	
	private boolean state;
	
	//lista dei codici dei progetti a cui appartiene
	private final ArrayList<Integer> CodiciProgetti = new ArrayList<Integer>();
	
	/*
	 * Lista metodi GET
	 * EFFECTS: Ritornani i valori delle variabili di istanza , distinguibili
	 * dal nome del metodo
	 * RETURN: This. variabile di istanza
	 */
	
	public String getUsername(){
		return this.username;
	}
	
	public String getPassword(){
		return this.password;
	}
	
	public ArrayList<Integer> getCodiciProgetti(){
		return this.CodiciProgetti;
	}
	
	/*
	 * Lista metodi SET
	 * MODIFY: This
	 * EFFECTS: Modificano i valori della variabili di istanza.
	 */
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public void setPassword(String password){
		this.password = password;
	}
	
	public void addCodiciProgetti(int code) {
		this.CodiciProgetti.add(code);
	}
	
	//metodo fine al debug, stampa tutti i valori necessari
	public void stampaTutto(){
		System.out.println("USERNAME: " + this.username + " PASSWORD: " + this.password);
	}
	
	
}
