package progettoReti;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ClientMenu {
	

	/*
	 * OVERVIEW: La classe rappresenta il menu del Client
	 * qua vengono creati tutti i metodi che il client puo eseguire per
	 * usufruire degli strumenti del client, e dove si instaura la connessione
	 * con il server
	 * 
	 * Astrazione DATO
	 * Dato un oggetto che contiene un numero di variabili di istanza per tutti i dati
	 * possibili che dobbiamo usare nella nostra applicazione (e con i relativi metodi GET e SET)
	 * Utilizziamo questa classe per racchiudere le informazioni che vogliamo spedire tra
	 * client e server, serializzandolo in JSON
	 */
	
	private int port; //porta 
	private String host; //indirizzo del server
	
	String tmpUsername; //
	
	final static int DEFAULT_PORT = 30000;
	
	private HashMap<Integer, String> listaProgetti = new HashMap<>(); //lista dei progetti con un codice
	private HashMap<String, String> listaMulticastP = new HashMap<>(); //lista degli indirizzi multicast
	
	private ArrayList<Utenti> listaUtenti = new ArrayList<Utenti>(); //lista generale degli utenti
	private HashMap<String, Boolean> statoUtenti = new HashMap<>(); // nome utente , true/false = online/offline
	
	private String username;
	
	MulticastServer ms; 
	
	//metodo costruttore
	public ClientMenu(int p, String h) {
		this.port = p;
		this.host = h;
	}
	
	public void setListaProgetti(HashMap<Integer, String> lp) {
		this.listaProgetti = lp;
	}
	
	public void setMulticastAddress(HashMap<String, String> lmp) {
		this.listaMulticastP = lmp;
	}
	
	/*
	 * @params: json -> String che rappresenta il un dato in JSON
	 * @effects: Deserializza il JSON e recupera il Dato che deve leggere il server 
	 * il JSON non e' altro che una stringa che viene passata dal client
	 * tramite quella viene costruito un oggetto che permette al server di eseguire
	 * le sue funzioni
	 * @return: Dato che e' un oggetto di tipo Dato che contiene i dati del JSON
	 */
	public Dato deserealizeData(String json) {
		ObjectMapper objectMapper = new ObjectMapper();
		Dato d = null;
		try {
			d = objectMapper.readValue(json, Dato.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return d;
	}
	
	/*
	 * @params: name -> String nome utente
	 * @effects: controlla tramite metodo remoto se un utente e' online
	 * @return: TRUE se e' online, FALSE altrimenti
	 */
	public boolean findIfOnline(String name) {
		ListerInterface serverObject;
		Remote remoteObject;
		try{
			Registry r = LocateRegistry.getRegistry(9993);
			remoteObject = r.lookup("EUSTATS-SERVER");
			serverObject = (ListerInterface) remoteObject;
			
			boolean resp = serverObject.findOnlineUser(name);
			return resp;
						
		}catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}
	
	/*
	 *	@effects: Effettua una chiamata RMI che preleva i dati dal fileSYstem e setta un
	 *	oggetto di istanza contenente la lista degli utenti
	 */
	public void setListaUtenti() {
		ListerInterface serverObject;
		Remote remoteObject;
		
		try{
			Registry r = LocateRegistry.getRegistry(9993);
			remoteObject = r.lookup("EUSTATS-SERVER");
			serverObject = (ListerInterface) remoteObject;
			
			//this.listaUtenti = 
			String resp = serverObject.getList();
			ObjectMapper objectMapper = new ObjectMapper();
			Dato u = null;
			try {
				u = objectMapper.readValue(resp, Dato.class);
				this.listaUtenti = u.getListaUtenti();
			} catch (IOException e) {
				e.printStackTrace();
			}
						
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	/*
	 * @params: json -> String che rappresenta una chats
	 * @effects: deserializza la chat json e istanzia un oggetto di tipo chats , che contiene le chats
	 * di un progetto
	 * @return: oggetto chats
	 */
	public Chats getChatFromJson(String json){
		ObjectMapper objectMapper = new ObjectMapper();
		Chats d = null;
		try {
			d = objectMapper.readValue(json, Chats.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return d;
	}
	
	/*
	 * @params: nome del progetto in cui vogliamo controllare
	 * @effects: Controlliamo di aver joinato nell indirizzo del progetto scelto
	 * se ne troviamo uno in cui non abbiamo joinato facciamo la join adesso
	 */
	public void controllerJoinerChat(String project) throws IOException {
		String add = this.listaMulticastP.get(project);
		if(this.ms.isJoined(add) == false) {
			this.ms.joiner(add);
		}
		if(this.ms.getChatsHM().containsKey(project) == false) {
			this.ms.addProjectToList(project);
		}

	}
	
	/*
	 * @params: project -> String nome del progetto
	 * @params: p -> Oggetto printwriter che serve per scrivere al server il mex
	 * @params: so -> socket che serve per ricevere la risp
	 * @effects: chiediamo al server di poter gestire una chats, quindi prima mandiamo un messaggio
	 * per richiedere l'indirizzo di quella chats, dopo entriamo in un piccolo menu
	 * che ci permette di mandare delle richieste al server multicast per poter interagire
	 * con la chats di gruppo del progetto 
	 */
	public void enterInChat(String username, String project, PrintWriter p,Socket so, MulticastServer ms) throws IOException {
		
		lookProject(username,p,so);
		
		controllerJoinerChat(project);
		String address = null;
		String getChatRequest = "///requestmessage///";
		
		Dato d = new Dato();
		d.setNameProject(project);
		d.setMessageToChat("get Address");
		d.setComando(12);
		d.setUsername(this.tmpUsername);
		String json = getDatoString(d);
		//inviamo il messaggio
		p.println(json);
		InputStreamReader isr;
		try {
			isr = new InputStreamReader(so.getInputStream());
			BufferedReader in = new BufferedReader(isr);
			String resp = in.readLine();
			address = resp;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println(address);
		InetAddress group = InetAddress.getByName(address);
		//
		int numeroMenu;
		boolean cicloMenu = true;
		DatagramSocket socket = new DatagramSocket();
		//creiamo un piccolo menu per la gestione della chat
		while(cicloMenu) {
			System.out.println("1) Inviare un messaggio alla chat di gruppo");
			System.out.println("2) Visualizzare la chat di gruppo");
			System.out.println("0) Esci dalla chat di gruppo");
			Scanner s = new Scanner(System.in);
			numeroMenu = s.nextInt();
			if(numeroMenu == 0) {
				cicloMenu = false;
			}else if(numeroMenu == 1) {
				//inviamo un messaggio al gruppo della chat del progetto
				System.out.println("Inviamo un messaggio al gruppo, inserisci il messaggio");
				String message;
				Scanner sm = new Scanner(System.in);
				message = sm.nextLine();
			
				ObjectMapper objectMapper = new ObjectMapper();
				String req = null;
				Messaggio messageObject = new Messaggio();
				messageObject.setAutore(this.tmpUsername);
				messageObject.setMessaggio(message);
				
				try {
					req = objectMapper.writeValueAsString(messageObject);
					byte[] data;
					data = req.getBytes();
		            DatagramPacket dat = new DatagramPacket(data,data.length,group,DEFAULT_PORT);
		            ms.sendMessage(project, this.listaMulticastP.get(project), message,this.username);
		            //socket.send(dat);		           	            
		        }
		        catch(IOException e){
		            e.printStackTrace();
		        }
			}else if(numeroMenu == 2) {
				//andiamo a visualizzare i messaggi del gruppo
				System.out.println("Visualizziamo i messaggi di gruppo");
				try {
					byte[] data;
					data = getChatRequest.getBytes();
		            DatagramPacket dat = new DatagramPacket(data,data.length,group,DEFAULT_PORT);
	               // socket.send(dat);
		            //ricevi la risposta   
	                // imposta il timeout
	                socket.setSoTimeout(5000);
	                byte[] buffer = new byte[256];
	                DatagramPacket receivedPacket = new DatagramPacket(buffer, buffer.length);
	                String chat = ms.getChatJson(project);
	                Chats c = getChatFromJson(chat);
	                c.printChat();
	                //socket.receive(receivedPacket);
	                //String res = new String(receivedPacket.getData());
	                //Chats c = getChatFromJson(res);
	               // c.printChat();
		        }
		        catch(IOException e){
		            e.printStackTrace();
		        }
			}
		}
	}
	
	
		/*
		 * @params: username -> String nome utente
		 * @effects: si registra ad oggetto remoto e invoca un metodo remoto
		 * per aggiornare lo stato di un utente
		 */
	public void statusServer(String username) {
		ListerInterface serverObject;
		Remote remoteObject;
		try{
			Registry r = LocateRegistry.getRegistry(9993);
			remoteObject = r.lookup("EUSTATS-SERVER");
			serverObject = (ListerInterface) remoteObject;
			serverObject.updateUserStatus(username);		
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
		/*
		 * @effects: Usa un metodo remoto per poter aggiornare la lista degli stati 
		 * degli utenti che sono online
		 */
		public void updateOnlineUser(){
			ListerInterface serverObject;
			Remote remoteObject;
			try{
				Registry r = LocateRegistry.getRegistry(9993);
				remoteObject = r.lookup("EUSTATS-SERVER");
				serverObject = (ListerInterface) remoteObject;
				
				String res = serverObject.getOnlineUsers();
				
				Dato d = deserealizeData(res);
				this.statoUtenti = d.getStatusUsers();
				
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	
		/*
		 * @effects: stampa i valori dell'hashmap che indicano lo stato
		 * degli utenti, ogni volta prima di stampare
		 * chiama un metodo per aggiornare l hashmap
		 */
		public void getOnlineUsers() {
			updateOnlineUser();
			 Iterator it = this.statoUtenti.entrySet().iterator();
			 while (it.hasNext()) {
				    // Utilizza il nuovo elemento (coppia chiave-valore)
				    // dell'hashmap
				    HashMap.Entry entry = (HashMap.Entry)it.next();
				 
				    // Stampa a schermo la coppia chiave-valore;
				    System.out.println("Nome = " + entry.getKey());
				    System.out.println("Stato = " + entry.getValue());
				    }
		}
	
		/*
		 * @params: code -> intero, codice dello status della card
		 * @effects: in base al codice crea una stringa che rappresenta lo stato della card 
		 * @return: ritorna il valore in formato string
		 */
		public String getStatusOfCard(int code) {
			if(code == 1) {
				return "TO DO";
			}else if(code == 2) {
				return "IN PROGRESS";
			}else if(code == 3) {
				return "TO BE REVISED";
			}else if(code == 4) {
				return "DONE";
			}else {
				return "Error";
			}
			
		}
	
	
		/*
		 * @effects: Stampa la lista degli utenti
		 */
		public void listUsers() {
			System.out.println("Lista utenti per username: ");
			for(int i = 0; i < this.listaUtenti.size(); i++) {
				System.out.println(this.listaUtenti.get(i).getUsername());
			}
		}
		
		/*
		 * @params: s -> String JSON che rappresenta un oggetto Dato con i dati dell utente
		 * @effects: Chiamata RMI per la registrazione dell'account
		 */
		void registerRequest(String s) {
			
			RegistererInterface serverObject;
			Remote remoteObject;
			
			try{
				Registry r = LocateRegistry.getRegistry(9994);
				remoteObject = r.lookup("EUSTATS-SERVER");
				serverObject = (RegistererInterface) remoteObject;
				
				boolean result = serverObject.registerAccount(s);
				if(result == true) {
					System.out.println("Utente registrato");
					return;
				}else {
					System.out.println("Utente Non registrato");
					return;
				}
				
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		/*
		 * @params: json deve essere una stringa che rappresenta un dato JSON , S una Socket per interfacciarsi con
		 * il server e recuperare la risposta, p serve per inviare il Messagio al server
		 * @effects: invia al server i dati dell'utente e attende una risposta da parte del server
		 */
		public void logger(Socket s, String json,PrintWriter p) {
			p.println(json);
			getResponseByServer(s,1,p);
		}
		
		/*
		 * @params: nome utente, nome progetto
		 * @effects: Manda una richiesta al server per creare un nuovo progetto
		 */
		public void createProject(String username,String nameP,PrintWriter p,Socket s) {
			//creiamo il dato
			Dato d = new Dato();
			d.setComando(3);
			d.setNameProject(nameP);
			d.setUsername(username);
			//generiamo il codice JSON#
			String json = getDatoString(d);
			//inviamo il messaggio
			p.println(json);
			InputStreamReader isr;
			try {
				isr = new InputStreamReader(s.getInputStream());
				BufferedReader in = new BufferedReader(isr);
				String resp = in.readLine();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			
		}
		
		/*
		 * @params: username -> nome utente
		 * @effects: aggiorna la lista dei progetti di un determinato utente
		 */
		public void lookProject(String username, PrintWriter p, Socket s){
			//creiamo il dato
			Dato d = new Dato();
			d.setComando(4);
			d.setUsername(username);
			//generiamo il codice JSON#
			String json = getDatoString(d);
			//inviamo il messaggio
			p.println(json);
			InputStreamReader isr;
			try {
				isr = new InputStreamReader(s.getInputStream());
				BufferedReader in = new BufferedReader(isr);
				String resp = in.readLine();
				//System.out.println(resp);
				Dato dl = deserealizeData(resp);
				setListaProgetti(dl.getListaProgetti());
				setMulticastAddress(dl.getListOfMulticastP());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		/*
		 * @params: username -> nome utente
		 * @effects: Aggiorna prima la lista dei progetti con un metodo specifico, poi
		 * stampa tutti i progetti associati ad un utente
		 */
		public void showProject(String username, PrintWriter p, Socket s) throws IOException {
			lookProject(username,p,s);
			for(int i = 1; i <= this.listaProgetti.size(); i++) {
				System.out.println("Chiave: " + i + " nome: " + this.listaProgetti.get(i));
				controllerJoinerChat(this.listaProgetti.get(i));
				System.out.println("Indirizzo Multicast = " + this.listaMulticastP.get(this.listaProgetti.get(i)));
			}
		}
		
		
		/*
		 * @params: progect -> string del nome del progetto nel quale vogliamo aggiungere un utente
		 * @params: username -> nome dell utente che vogliamo aggiungere nel progetto
		 * @effects: aggiunge un utente al gruppo dei membri del progetto
		 */
		public void addMember(String project, String username,PrintWriter p,Socket so) {
			//creiamo il dato
			Dato d = new Dato();
			d.setComando(5);
			d.setUsername(username);
			d.setNameProject(project);
			//generiamo il codice JSON#
			String json = getDatoString(d);
			//inviamo il messaggio
			p.println(json);
			InputStreamReader isr;
			try {
				isr = new InputStreamReader(so.getInputStream());
				BufferedReader in = new BufferedReader(isr);
				String resp = in.readLine();
				//System.out.println(resp);
				if(resp.equals("done")) {
					System.out.println("Membro Aggiunto al gruppo");
				}else {
					System.out.println("Membro non aggiunto!!!");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		/*
		 * @params: project -> string del nome del progetto
		 * @effects: visulizza i membri del progetto scelto
		 */
		public void listaMembriPerProgetto(String project,PrintWriter p,Socket so) {
			Dato d = new Dato();
			d.setComando(6);
			d.setNameProject(project);
			String json = getDatoString(d);
			//inviamo il messaggio
			p.println(json);
			InputStreamReader isr;
			try {
				isr = new InputStreamReader(so.getInputStream());
				BufferedReader in = new BufferedReader(isr);
				String resp = in.readLine();
				//System.out.println(resp);
				Dato tmp = deserealizeData(resp);
				System.out.println("Utenti appartenenti al progetto " + project + " : ");
				for(int i = 0; i < tmp.getListaNameUtenti().size(); i++) {
					System.out.println("Utente: " + tmp.getListaNameUtenti().get(i));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		/*
		 * @params: project -> nome del progetto nel quale aggiungere una card
		 * @params: cards -> nome della card
		 * @params: desc -> descrizione della card
		 * @effects: aggiunge una cards ad un progetto
		 */
		public void addCard(String project, String cards, String desc, PrintWriter p,Socket so) {
			Dato d = new Dato();
			d.setComando(7);
			d.setNameProject(project);
			d.setNameCard(cards);
			d.setDescCard(desc);
			String json = getDatoString(d);
			//inviamo il messaggio
			p.println(json);
			InputStreamReader isr;
			try {
				isr = new InputStreamReader(so.getInputStream());
				BufferedReader in = new BufferedReader(isr);
				String resp = in.readLine();
				if(resp.equals("done")) {
					System.out.println("Inserita con successo");
				}else {
					System.out.println("Errore nell inserimento della card");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		/*
		 * @params: x -> codice lista
		 * @effects: converte il codice nel tipo di lista
		 * @return: nome lista
		 */
		public String getListFromCode(int x) {
			switch(x) {
			case 0:
				return "TO DO";
			case 1:
				return "IN PROGRESS";
			case 2:
				return "TO BE REVISED";
			case 3:
				return "DONE";
			default:
				return "";
			}
		}
		
		/*
		 * @params: project -> nome del progetto
		 * @effects: visualizza le cards di uno specifoc progetto
		 */
		public void showCards(String project, PrintWriter p,Socket so) {
			Dato d = new Dato();
			d.setComando(8);
			d.setNameProject(project);
			String json = getDatoString(d);
			//inviamo il messaggio
			p.println(json);
			InputStreamReader isr;
			try {
				isr = new InputStreamReader(so.getInputStream());
				BufferedReader in = new BufferedReader(isr);
				String resp = in.readLine();
				//System.out.println(resp);
				Dato d2 = deserealizeData(resp);
				System.out.println("Lista Cards del progetto: " + project);
				for(int x = 0; x < d2.getLCC().size(); x++) {
					//System.out.println(d2.getLCC().get(x));
					 Iterator it1 = d2.getLCC().get(x).entrySet().iterator();
					 System.out.println("Lista " + getListFromCode(x));
					 while (it1.hasNext()) {
						    // Utilizza il nuovo elemento (coppia chiave-valore)
						    // dell'hashmap
						    HashMap.Entry entry = (HashMap.Entry)it1.next();
						 
						    // Stampa a schermo la coppia chiave-valore;
						    System.out.println("Cards = " + entry.getKey());
						    //System.out.println("Stato = " + entry.getValue());
					}
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		/*
		 * @params: project -> nome del progetto 
		 * @params: card -> nome della card
		 * @effects: visualizza lo stato e la descrizone di una card specifica di un progetto
		 */
		public void showCard(String project, String card, PrintWriter p, Socket so) {
			Dato d = new Dato();
			d.setComando(9);
			d.setNameProject(project);
			d.setNameCard(card);
			String json = getDatoString(d);
			//inviamo il messaggio
			p.println(json);
			InputStreamReader isr;
			try {
				isr = new InputStreamReader(so.getInputStream());
				BufferedReader in = new BufferedReader(isr);
				String resp = in.readLine();
				//System.out.println(resp);
				Dato d2 = deserealizeData(resp);
				if(d2.getCards() != null) {
					System.out.println("Card " + card + " del progetto " + project + " :");
					for(int i = 0; i < d2.getCards().size(); i++) {
						if(d2.getCards().get(i).getNome().equals(card)) {
							System.out.println(d2.getCards().get(i).getNome());
							System.out.println(d2.getCards().get(i).getDesc());
							//System.out.println("Stato: " + getStatusOfCard(d2.getCards().get(i).getStato()));
						}
					}
				}else {
					System.out.println("Card non esistente");
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		/*
		 * @params: project -> nome del progetto 
		 * @params: card -> nome della card
		 * @effects: visualizza lo storico di una card di un progetto specifico
		 */
		public void showHistoryCard(String project, String card, PrintWriter p, Socket so) {
			Dato d = new Dato();
			d.setComando(9);
			d.setNameProject(project);
			String json = getDatoString(d);
			//inviamo il messaggio
			p.println(json);
			InputStreamReader isr;
			try {
				isr = new InputStreamReader(so.getInputStream());
				BufferedReader in = new BufferedReader(isr);
				String resp = in.readLine();
				//System.out.println(resp);
				Dato d2 = deserealizeData(resp);
				System.out.println("Storia della card: " + card);
				for(int i = 0; i < d2.getCards().size(); i++) {
					if(d2.getCards().get(i).getNome().equals(card)) {
						for(int j = 0; j < d2.getCards().get(i).getHistory().size(); j++) {
							System.out.println("indice: " + (j+1) + " " + getStatusOfCard(d2.getCards().get(i).getHistory().get(j)));
						}
					}
				}

				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}	
		
		/*
		 * @params: project -> nome del progetto 
		 * @params: card -> nome della card
		 * @params: codice -> codice della posizione desiderata della card
		 * @effects: muove la card in uno stato specifico in base al codice in input
		 */
		public void moveCard(String project, String card, int codice, PrintWriter p,Socket so) {
			Dato d = new Dato();
			d.setComando(10);
			d.setNameProject(project);
			d.setNameCard(card);
			d.setCodiceCard(codice);
			String json = getDatoString(d);
			//inviamo il messaggio
			p.println(json);
			InputStreamReader isr;
			try {
				isr = new InputStreamReader(so.getInputStream());
				BufferedReader in = new BufferedReader(isr);
				String resp = in.readLine();
				if(resp.equals("done")) {
					System.out.println("Operazione effettuata con successo");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		/*
		 * @params: project -> String nome del progetto 
		 * @effects: cancella il progetto scelto
		 */
		public void cancelProject(String username,String project, PrintWriter p,Socket so) {
			lookProject(username,p,so);
			Dato d = new Dato();
			d.setComando(11);
			d.setNameProject(project);
			String json = getDatoString(d);
			//inviamo il messaggio
			p.println(json);
			InputStreamReader isr;
			try {
				isr = new InputStreamReader(so.getInputStream());
				BufferedReader in = new BufferedReader(isr);
				String resp = in.readLine();
				if(resp.equals("done")) {
					System.out.println("Operazione effettuata con successo");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		//-----------------------------------------------------------------------------//
	
	/*
	 * @params: userData e' un file JSON che deve essere convertito in una ClasseUtenti
	 * @effects: Converte il JSON creando una struttura dati per gestire l'utente
	 * dopo entra in un Menu che permette al client di eseguire le operazioni da Utente
	 * in base all'operazione scelta verra' settato un valore specifico in un campo dell oggetto
	 * che verra spedito al server, grazie a quello il server capira' cosa chiede il client
	 */
	void sessionSystem(String userData,PrintWriter p,Socket so) {
		
		NotifyEventInterface stub = null;
		NotifyServerInterface server = null;
		
		try{
			Registry registry = LocateRegistry.getRegistry(5000); 
			String name = "Server";
			server = (NotifyServerInterface) registry.lookup(name);
			
			 /* si registra per la callback */
			System.out.println("Registering for callback");
			
			NotifyEventInterface callbackObj = new NotifyEventImpl();
			stub = (NotifyEventInterface) UnicastRemoteObject.exportObject(callbackObj, 0);
			
			server.registerForCallback(stub);
			
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		
		setListaUtenti(); //viene settata la lista degli utentu
		listUsers(); //viene stampata la lista degli utenti
		
		ObjectMapper objectMapper = new ObjectMapper();
		ClasseUtenti cu;
		boolean menu = true;
		Utenti u;
		Scanner s;
		
		try {
			cu = objectMapper.readValue(userData, ClasseUtenti.class);
			u = cu.getListaUtenti().get(0);
			statusServer(u.getUsername());
			lookProject(u.getUsername(),p,so);
			this.ms = new MulticastServer(this.listaProgetti);
			
			Thread chats = new Thread(new Chatter(ms));
			chats.start();
			
			for(int i = 1; i <= this.listaProgetti.size(); i++) {
				//System.out.println("Indirizzo Multicast = " + this.listaMulticastP.get(this.listaProgetti.get(i)));
				ms.joiner(this.listaMulticastP.get(this.listaProgetti.get(i)));
			}
			
			this.username = u.getUsername();
			
			while(menu == true) {
				System.out.println("Benvenuto nel menu " + u.getUsername());
				System.out.println("0) Exit");
				System.out.println("1) Crea un nuovo progetto");
				System.out.println("2) Vedi la lista dei progetti a cui sei membro");
				System.out.println("3) Visualizza utenti online");
				System.out.println("4) Aggiungi un membro al progetto");
				System.out.println("5) Visualizza i membri di un progetto");
				System.out.println("6) Aggiungi una cards ad un progetto");
				System.out.println("7) Visualizza le cards di un progetto");
				System.out.println("8) Visualizza i dati di una Card");
				System.out.println("9) Visualizza lo storico di una Card");
				System.out.println("10) Muovi una cards");
				System.out.println("11) Elimina un progetto");
				System.out.println("12) Apri chat di progetto");
				s = new Scanner(System.in);
				int cmd = s.nextInt();
				if(cmd == 0) {
					System.out.println("Logout");
					server.unregisterForCallback(stub);
					menu = false;
				}else if(cmd == 1) {
					System.out.println("Creiamo un nuovo progetto, inserisci il nome del progetto");
					s = new Scanner(System.in);
					String nameP = s.nextLine();
					createProject(u.getUsername(),nameP,p,so);
				}else if(cmd == 2) {
					System.out.println("Lista dei progetti a cui sei membro");
					showProject(u.getUsername(),p,so);
				}else if(cmd == 3) {
					System.out.println("Visualizza gli utenti online");
					getOnlineUsers();
				}else if(cmd == 4) {
					String nameProject;
					String nameUser;
					System.out.println("Inserisci prima il nome di un progetto e dopo di un utente");
					s = new Scanner(System.in);
					nameProject = s.nextLine();
					s = new Scanner(System.in);
					nameUser = s.nextLine();
					for(int x = 0; x < this.listaUtenti.size(); x++) {
						if(this.listaUtenti.get(x).getUsername().equals(nameUser)) {
							if(this.listaProgetti.containsValue(nameProject)) {
								addMember(nameProject, nameUser,p,so);
							}else {
								System.out.println("Progetto non esistente");
							}
						}
					}
					
				}else if(cmd == 5) {
					String nameProject2;
					System.out.println("Inserisci il nome di un progetto");
					s = new Scanner(System.in);
					nameProject2 = s.nextLine();
					if(this.listaProgetti.containsValue(nameProject2)) {
						listaMembriPerProgetto(nameProject2,p,so);
					}
				}else if(cmd == 6) {
					String nameProject3;
					String nameCard;
					String desc;
					System.out.println("Inserisci prima il nome del progetto, poi nome cards e dopo descrizione");
					s = new Scanner(System.in);
					nameProject3 = s.nextLine();
					if(this.listaProgetti.containsValue(nameProject3)) {
						s = new Scanner(System.in);
						nameCard = s.nextLine();
						s = new Scanner(System.in);
						desc = s.nextLine();
						addCard(nameProject3,nameCard,desc,p,so);
					}else {
						System.out.println("Progetto non presente nella tua lista");
					}
				}else if(cmd == 7) {
					String nameProject4;
					System.out.println("Inserisci il nome di un progetto");
					s = new Scanner(System.in);
					nameProject4 = s.nextLine();
					if(this.listaProgetti.containsValue(nameProject4)) {
						showCards(nameProject4,p,so);
					}
				}else if(cmd == 8) {
					String nameProject5;
					String nameCard2;
					System.out.println("Inserisci nome del progetto e dopo nome della card");
					s = new Scanner(System.in);
					nameProject5 = s.nextLine();
					s = new Scanner(System.in);
					nameCard2 = s.nextLine();
					if(this.listaProgetti.containsValue(nameProject5)) {
						showCard(nameProject5,nameCard2,p,so);
					}
				}else if(cmd == 9) {
					String nameProject6;
					String nameCard3;
					System.out.println("Inserisci nome del progetto e dopo nome della card");
					s = new Scanner(System.in);
					nameProject6 = s.nextLine();
					s = new Scanner(System.in);
					nameCard3 = s.nextLine();
					if(this.listaProgetti.containsValue(nameProject6)) {
						showHistoryCard(nameProject6,nameCard3,p,so);
					}
				}else if(cmd == 10) {
					String nameProject7;
					String nameCard4;
					int codiceDest;
					System.out.println("Inserire prima nome progetto, poi nome card");
					System.out.println("Da ultimo inserire il codice che rappresenta dove vogliamo spostare la card");
					System.out.println("2 In progress - 3 to be revised - 4 done");
					s = new Scanner(System.in);
					nameProject7 = s.nextLine();
					s = new Scanner(System.in);
					nameCard4 = s.nextLine();
					s = new Scanner(System.in);
					codiceDest = s.nextInt();
					if(this.listaProgetti.containsValue(nameProject7)) {
						moveCard(nameProject7,nameCard4,codiceDest,p,so);
					}
				}else if(cmd == 11) {
					String nameProject8;
					System.out.println("Inserisci il nome del progetto da cancellare");
					s = new Scanner(System.in);
					nameProject8 = s.nextLine();
					if(this.listaProgetti.containsValue(nameProject8)) {
						cancelProject(u.getUsername(),nameProject8,p,so);
					}
				}else if(cmd == 12) {
					String nameProject9;
					System.out.println("Inserisci nome del progetto");
					s = new Scanner(System.in);
					nameProject9 = s.nextLine();
					if(this.listaProgetti.containsValue(nameProject9)) {
						enterInChat(u.getUsername(),nameProject9,p,so,ms);
					}
				}
			}
			statusServer(u.getUsername());
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/*
	 * @params: c = int che indica cosa deve aspettarsi come risposta
	 * @effects: Serve per visualizzare la risposta del server
	 * @return: Ritorna un messaggio in base a quello che il server manda
	 */
	public void getResponseByServer(Socket s, int c,PrintWriter p) {
		InputStreamReader isr;
		try {
			isr = new InputStreamReader(s.getInputStream());
			BufferedReader in = new BufferedReader(isr);
			String resp = in.readLine();
			//System.out.println("Client riceve: " + resp);
			if(c == 1) {
				if(resp.equals("null")) {
					System.out.println("Login Errato");
				}
				else {
					System.out.println("Login corretto");
					sessionSystem(resp,p,s);
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * @params: Dato -> oggetto Dato da convertire in stringa
	 * @effects: costruisce la stringa del dato D e la restituisce, senza intaccare il file System
	 * @return: stringa JSON che rappresenta il dato
	 */
	public String getDatoString(Dato d) {
		ObjectMapper objectMapper = new ObjectMapper();
		String textObject = null;
		try {
			textObject = objectMapper.writeValueAsString(d);
			}
			catch (IOException e) {
			e.printStackTrace();
			}
			return textObject;
	}
	
	
	/*
	 * @effects: Serializza il dato mandato in JSON in modo che poi il Server recuperi il dato
	 * al server viene pero m,andata una stringa contente il file JSON
	 * viene pero creata una copia in locale
	 */
	public String serializeData(Dato d){
		ObjectMapper objectMapper = new ObjectMapper();
		String textObject = null;
		try {
			// Writing to a file
			File file=new File("currentData.json");
			file.createNewFile();
			//System.out.println("Creiamo il file JSON con i dati che il server deve recuperare");
			objectMapper.writeValue(file, d);
			FileWriter fileWriter = new FileWriter(file); // in alternativa
			objectMapper.writeValue(fileWriter, d);
			fileWriter.close();
			textObject = objectMapper.writeValueAsString(d);
			//System.out.println(textObject);
			}
			catch (IOException e) {
			e.printStackTrace();
			}
			return textObject;
	}
	
	/*
	 * @effects: Controlla che il comando inviato sia valido
	 * 	esegue una delle operazioni di sistema
	 */
	public void controlloComando(PrintWriter out,int c,Dato d,Socket sock){
		switch(c) {
			case 0:
				System.out.println("Addio!");
				System.exit(0);
				break;
			case 1:
				//Scannerizziamo Username e Password
				System.out.println("Registrazione: Inserisci prima username, poi password");
				Scanner s;
				s = new Scanner(System.in);
				String username = s.nextLine();
				d.setUsername(username);
				s = new Scanner(System.in);
				String psw = s.nextLine();
				d.setPassword(psw);
				d.setComando(1);
				String json = serializeData(d);
				registerRequest(json);
				out.println(json);
				break;
			case 2:
				//Scannerizziamo Username e Password
				System.out.println("Login: Inserisci prima username, poi password");
				Scanner s2;
				s2 = new Scanner(System.in);
				String username2 = s2.nextLine();
				d.setUsername(username2);
				s2 = new Scanner(System.in);
				String psw2 = s2.nextLine();
				d.setPassword(psw2);
				d.setComando(2);
				String json2 = serializeData(d);
				boolean controlOnline = findIfOnline(username2);
				if(controlOnline == false) {
					this.tmpUsername = username2;
					logger(sock,json2,out);
				}else {
					System.out.println("User gia' online");
				}
				break;
			default:
				System.out.println("Comando non riconosciuto");
				System.exit(0);
				break;
		}
	}
	
	/*
	 * @effects: menu principale dal quale il client puo inviare richieste al server 
	 * al suo interno viene anche instaurata la connessione con il Server
	 */
	public void mainMenu() throws UnknownHostException, IOException {
		Dato dato;
		
		System.out.println("Benvenuto nel menu");
		//Spazio di codice per la connessione al server
		Socket s = new Socket(this.host, this.port);
		PrintWriter out;
		
		//
		Scanner sc;
		//realizziamo un ciclo infinito per creare un menu
		while(true) {
			out = new PrintWriter(s.getOutputStream(), true);
			dato = new Dato();
			System.out.println("0) Exit 1) Iscriviti 2) Login");
			sc = new Scanner(System.in);
			int comando = sc.nextInt();
			controlloComando(out,comando,dato,s);
			//getResponseByServer(s);
		}


	}
	
}











