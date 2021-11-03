package progettoReti;

public class Messaggio {
	
	/*
	 * OVERVIEW:
	 * La classe messaggio rappresenta un oggetto messaggio che viene inviato 
	 * in una chat di un progetto
	 */
	
	private String autore; //autore del messaggio
	private String messaggio; //stringa del messaggio
	private String progetto;
	
	//Metodi SET
	
	public void setGruppo(String progetto) {
		this.progetto = progetto;
	}
	
	public void setAutore(String autore) {
		this.autore = autore;
	}
	
	public void setMessaggio(String messaggio) {
		this.messaggio = messaggio;
	}
	
	//Metodi GET
	
	public String getProgetto() {
		return this.progetto;
	}
	
	public String getAutore() {
		return this.autore;
	}
	
	public String getMessaggio() {
		return this.messaggio;
	}

}
