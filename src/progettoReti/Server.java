package progettoReti;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Server {

	/*
	 * OVERVIEW: classe che rappresenta il Server TCP MultiThread
	 * 
	 * Il server oltre a ricevere le richiesta dal client istanzia altri servizi utili
	 * al funzionamento dell'applicazione, come la creazione dei thread per le chats oppure
	 * la creazione degli oggetti remoti 
	 * 
	 */
	
	private int port; //porta
	private String host; //hostaname
	
	ServerSocket server; //socket per il server
	
	final static int DEFAULT_PORT = 30000; //porta per il multicast
	
	private ThreadPoolExecutor executor; //esecutore per i Thread
	
	ClasseProgetti databaseProgetti = null; //database dei progetti
	
	private final ReentrantLock locker=new ReentrantLock();
	
	//private int counterWorker = 0; //serve per segnare se stiamo lavornado sul databse dei progetti
	
	//metodo costruttore
	public Server(int p, String h) {
		this.port = p;
		this.host = h;
		this.executor=(ThreadPoolExecutor)Executors.newCachedThreadPool(); 
	}
	
	/*
	 * 
	 */
	public boolean findCritRequest(String json) {
		Dato d = deserealizeData(json);
		int comando = d.getComando();
		if(comando == 3 || comando == 5 || comando == 7 || comando == 10 || comando == 11) {
			return true;
		}else {
			return false;
		}
	}
	
	
	/*
	 * @params: json -> string in formato json
	 * @effects: Deserializza il JSON e recupera il Dato che deve leggere il server 
	 * il JSON non e' altro che una stringa che viene passata dal client
	 * tramite quella viene costruito un oggetto che permette al server di eseguire
	 * le sue funzioni
	 * @return: d = dato creato dalla stringa json
	 */
	public Dato deserealizeData(String json) {
		ObjectMapper objectMapper = new ObjectMapper();
		File file=new File("currentData.json");
		Dato d = null;
		try {
			d = objectMapper.readValue(json, Dato.class);
			//System.out.println("Deserialized object from JSON");
			//System.out.println("-----------------------");
			//System.out.println("Utente " + d.getComando());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return d;
	}
	
	/*
	 * @effects:
	 */
	public void readDatabase() throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		File file = new File("projectDatabase.json");
		this.databaseProgetti = objectMapper.readValue(file, ClasseProgetti.class);
	}
	
	/*
	 * @effects:
	 */
	public void printDatabase(){
		ArrayList<Progetti> progetti = this.databaseProgetti.getProgetti();
		System.out.println("Lista Progetti");
		for(int i = 0; i < progetti.size(); i++) {
			System.out.println("P: " + progetti.get(i).getName());
		}
	}
	
	
	/*
	 * @effects: Crea un server attraverso la socket e poi crea un Thread e 
	 * Invia la risposta al client tramite Thread quando arriva una richiesta da 
	 * un Client
	 * Allo stesso tempo vengono create tre interfacce remote , una che sfrutta RMI callback 
	 * per inviare delle notifiche al client, e le altre servono invece per la registrazione
	 * degli utenti e per inviare al client una lista di tutti gli utenti quando effettua il login
	 * Ovviamente su 3 porte diverse
	 */
	public void runServer() throws JsonParseException, JsonMappingException, IOException {
		
		NotifyServerImpl serverN = null;
		
		//RMI callback
		try{
			//registrazione presso registry
			serverN = new NotifyServerImpl(); 
			NotifyServerInterface stub=(NotifyServerInterface) UnicastRemoteObject.exportObject(serverN,39000);
			
			String name = "Server";
			
			LocateRegistry.createRegistry(5000);
			Registry registry=LocateRegistry.getRegistry(5000);
			
			registry.bind(name, stub);
			    
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		//RMI
		try{
		    RegistererClass statsService = new RegistererClass(this.locker);
		    
		    RegistererInterface stub = (RegistererInterface) UnicastRemoteObject.exportObject(statsService, 0);
		    
		    LocateRegistry.createRegistry(9994);
		    Registry r = LocateRegistry.getRegistry(9994);
		    
		    r.rebind("EUSTATS-SERVER", stub);
		    
		    
		}catch(RemoteException e){
			e.printStackTrace();
		}
		
		//RMI
		try{
			Lister statsService = new Lister();
				    
			ListerInterface stu = (ListerInterface) UnicastRemoteObject.exportObject(statsService, 1);
				    
			LocateRegistry.createRegistry(9993);
			Registry r = LocateRegistry.getRegistry(9993);
				    
			r.rebind("EUSTATS-SERVER", stu);
				    
			}catch(RemoteException e){
				e.printStackTrace();
			}		
		
		
		//---------------------------------------------------//
		
		//creiamo il server TCP
		try {
			this.server = new ServerSocket(this.port);
		} catch (IOException e) {
			e.printStackTrace();
		}
		//---------------------------------------------------------//
		BufferedReader reader;
		readDatabase();
		System.out.println("Server Avviato in Attesa di comandi da parte del client: ");
		while(true){
			try {
				Socket client = server.accept();
				
				ActivityThread t = new ActivityThread(client,serverN,this.databaseProgetti,this.locker);
				this.executor.execute(t);
				
			}catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
}
