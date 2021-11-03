package progettoReti;

import java.util.ArrayList;
import java.util.HashMap;

public class Dato {
	
	/*
	 * OVERVIEW: Questa classe rappresenta un dato che viene inviato al Thread
	 * a seconda del comando scelto andiamo ad usare dei metodi specifici per 
	 * recuperare i dati che il client vuole inviare
	 * il server in base al comando preso sapra' a quali dati attingere da questa
	 * classe
	 * Per questo la classe contiene dati generici che possono essere utilizzati
	 * in ambiti e richieste diverse
	 * 
	 * Quando il client manda sceglie di richiedere uno specifico servizio molte
	 * delle variabili di questa classe rimangono non inizializzate, tranne il "Comando"
	 * sara' infatti grazie al comando che il Server sapra' cosa vuole il client e quindi sapra' 
	 * a quali variabili puo' accedere andando a lavorare per fornire il servizio richiesto dal client
	 * 
	 * Tutti i metodi all'interno di questa funzione non sono altro che metodi di tipo GET/SET/ADD
	 * ovvero metodi che permettono o di settare una variabile di istanza, o di recuperarla oppure di
	 * aggiungere un valore ad una lista/hashmap
	 * 
	 * La struttura della classe e' realizzata in modo che poi un oggetto di tipo Dato possa essere 
	 * serializzato e deserializzato in JSON in modo da essere inviato come una Stringa
	 */
	
	private int current_cmd = -1; //comando inviato 
	
	private String username; //username dell'utente
	private String password; //password dell'utente
	
	private String nameProject; //nome progetto
	
	private HashMap<Integer, String> listaProgetti = new HashMap<>(); //lista progetti
	
	private HashMap<String, String> listaMulticastP = new HashMap<>();
	
	private HashMap<String, Boolean> statusUsers = new HashMap<>(); //lista dei nomi degli utenti con status
 	
	private ArrayList<Utenti> listaUtenti = new ArrayList<>(); //lista utenti
	
	private ArrayList<String> listaNameUtenti = new ArrayList<>();
	
	private ArrayList<String> listaCards = new ArrayList<>();
	
	private ArrayList<Cards> cards = new ArrayList<>();
	
	private ArrayList<HashMap<String, Cards>> listaCardsC = new ArrayList<HashMap<String, Cards>>(); 
	
	private String nameCard;
	
	private String descCard;
	
	private int codiceCard;
	
	private String messageToChat;
	
	private String multicastAddress;
	
	public void setListaCardsC(ArrayList<HashMap<String, Cards>> lcc) {
		this.listaCardsC = lcc;
	}
	
	public void setMulticastAddress(String multicastAddress) {
		this.multicastAddress = multicastAddress;
	}
	
	public void setMessageToChat(String message) {
		this.messageToChat = message;
	}
	
	public void setStatusUsers(HashMap<String, Boolean> su){
		this.statusUsers = su;
	}
	
	public void setCodiceCard(int cod){
		this.codiceCard = cod;
	}
		
	public void setCards(ArrayList<Cards> l){
		this.cards = l;
	}
	
	public void setListaCards(ArrayList<String> l){
		this.listaCards = l;
	}
	
	public void setDescCard(String d){
		this.descCard = d;
	}
	
	public void setNameCard(String n){
		this.nameCard = n;
	}
	
	public void setListaNameUtenti(ArrayList<String> l){
		this.listaNameUtenti = l;
	}
	
	public void setListaUtenti(ArrayList<Utenti> l){
		this.listaUtenti = l;
	}
	
	public void addProgetto(String name){
		int size = this.listaProgetti.size();
		int index = size + 1;
		this.listaProgetti.put(index, name);		
	}
	
	public void addMulticastP(String name, String mi) {
		this.listaMulticastP.put(name, mi);
	}
	
	public String getMulticastAddress() {
		return this.multicastAddress;
	}
	
	public String getMessageToChat(){
		return this.messageToChat;
	}
	
	public HashMap<String, Boolean> getStatusUsers(){
		return this.statusUsers;
	}
	
	public int getCodiceCard() {
		return this.codiceCard;
	}
	
	public HashMap<Integer, String> getListaProgetti(){
		return this.listaProgetti;
	}
	
	public HashMap<String, String> getListOfMulticastP(){
		return this.listaMulticastP;
	}
	
	public void setNameProject(String s) {
		this.nameProject = s;
	}
	
	public void setComando(int c) {
		this.current_cmd = c;
	}

	public void setUsername(String u) {
		this.username = u;
	}
	
	public void setPassword(String p) {
		this.password = p;
	}
	 
	public ArrayList<Cards> getCards(){
		return this.cards;
	}
	
	public ArrayList<String> getListaCards(){
		return this.listaCards;
	}
	
	public String getDescCard() {
		return this.descCard;
	}
	
	public String getNameCard() {
		return this.nameCard;
	}
	
	public ArrayList<String> getListaNameUtenti(){
		return this.listaNameUtenti;
	}
	
	public ArrayList<Utenti> getListaUtenti(){
		return this.listaUtenti;
	}
	
	public String getNameProject() {
		return this.nameProject;
	}
	
	public int getComando() {
		return this.current_cmd;
	}
	
	public String getUsername(){
		return this.username;
	}
	
	public String getPassword(){
		return this.password;
	}
	
	public ArrayList<HashMap<String, Cards>> getLCC(){
		return this.listaCardsC;
	}

}
