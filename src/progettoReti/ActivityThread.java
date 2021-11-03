package progettoReti;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;

public class ActivityThread implements Runnable{
	
	/*
	 * OVERVIEW:
	 * La classe rappresenta un Thread lanciato dal server
	 * essendo il server che lancia il Thread multiThread ne viene lanciato
	 * uno per ogni richiesta che arriva da un qualsiasi Client
	 * 
	 * Astrazione DATO
	 * Dato un oggetto che contiene un numero di variabili di istanza per tutti i dati
	 * possibili che dobbiamo usare nella nostra applicazione (e con i relativi metodi GET e SET)
	 * Utilizziamo questa classe per racchiudere le informazioni che vogliamo spedire tra
	 * client e server, serializzandolo in JSON
	 */

	private Socket client; //socket
	private String host; //localhost
	
	String finalResponse = "none"; //response finale da mandare indietro al client
	
	private final ReentrantLock locker; //serve quando andiamo a scrivere su un file del sistema
	
	final static int DEFAULT_PORT = 30000;
	
	private HashMap<Integer, String> listaComandi = new HashMap<>(); //rappresenta una lista di comandi che 
	//il server piu ricevere
	
	NotifyServerImpl serverN; //
	
	ClasseProgetti databaseProgettiTmp;
	
	private final static int TIMEOUT_LIMIT = 5;

	//metodo costruttore
	public ActivityThread(Socket c, NotifyServerImpl s, ClasseProgetti dcptmp, ReentrantLock l) {
		this.client = c;
		this.host = this.client.getInetAddress().getHostAddress();
		this.serverN = s;
		this.databaseProgettiTmp = dcptmp;
		this.locker = l;
	}
	
	
	/*
	 * @params: s =  String
	 * @effects: Setta la risposta dal mandare al client
	 */
	public void setFinalResponse(String s) {
		this.finalResponse = s;
	}
	
	
	/*
	 * @params: d -> Dato che viene deserializzato
	 * @effects: costruisce la stringa del dato D e la restituisce, senza intaccare il file System
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
	 * @effects: controlla un file dove sono presenti tutti gli indirizzi multicast utilizzati
	 * quindi ne genera uno nuovo, lo scrive nella lista e lo prepara per essere
	 * inviato
	 * @return: stringa che rappresenta l indirizzo multicast trovato
	 */
	String findMulticastAddress(){
		String add = null;
		
		try {
		      File myObj = new File("mcaddress.txt");
		      Scanner myReader = new Scanner(myObj);
		      while (myReader.hasNextLine()) {
		        add = myReader.nextLine();
		      }
		      myReader.close();
		    } catch (FileNotFoundException e) {
		      System.out.println("Errore.");
		      e.printStackTrace();
		    }
		
		String[] splitted = add.split("\\.");
		
		int oct1 = Integer.parseInt(splitted[0]);
		int oct2 = Integer.parseInt(splitted[1]); 
		int oct3 = Integer.parseInt(splitted[2]);
		int oct4 = Integer.parseInt(splitted[3]);
		
		if(oct4 < 255){
		    oct4++;
		}else if(oct3 < 255){
		    oct4 = 1;
		    oct3++;
		}else if(oct2 < 255){
		    oct4 = 1;
		    oct3 = 1;
		    oct2++;
		}else if(oct1 < 239){
		    oct4 = 1;
		    oct3 = 1;
		    oct2 = 1;
		    oct1++;
		}
		
		String finalString = oct1 + "." + oct2 + "." + oct3 + "." + oct4;
		
		try {
		      FileWriter myWriter = new FileWriter("mcaddress.txt",false);
		      myWriter.write(finalString);
		      myWriter.close();
		    } catch (IOException e) {
		      System.out.println("An error occurred.");
		      e.printStackTrace();
		    }
		
		return finalString;
	}
	
	/*
	 * @effects: Crea un progetto e salva i dati nel fileSystem
	 */
	void createProject(String username,Writer w,String name) throws IOException, InterruptedException {
		
		Progetti p = new Progetti();
		String ma = findMulticastAddress();
		p.addPartecipanti(username);
		p.setName(name);
		p.setMulticastAddress(ma);
		
		boolean ans = this.locker.tryLock(TIMEOUT_LIMIT,TimeUnit.SECONDS);
		
		if(!ans) {
			System.out.println("Tempo scaduto! trovato bloccato");
			w.write("Errore di Timeout" + "\n");
			w.flush();
		}
		
		if(ans) {
			//this.locker.lock();
			ObjectMapper objectMapper = new ObjectMapper();
			File file=new File("projectDatabase.json");
			ClasseProgetti cp = null;
			try {
				cp = objectMapper.readValue(file, ClasseProgetti.class);
				if(cp.findProject(name) == false) {
					cp.addProgetti(p);
					try {
						// Writing to a file
						File file2=new File("projectDatabase.json");
						file2.createNewFile();
						objectMapper.writeValue(file2, cp);
						FileWriter fileWriter = new FileWriter(file2); // in alternativa
						objectMapper.writeValue(fileWriter, cp);
						fileWriter.close();
						}
						catch (IOException e) {
							System.out.println("Non ci sono progetti");
						}
					
						w.write("Tentativo di creazione progetto ricevuto" + "\n");
						w.flush();
				}else{
					w.write("Doppione Trovato" + "\n");
					w.flush();
				}

			} catch (IOException e) {
				System.out.println("Non ci sono progetti");
				ClasseProgetti cp2 = new ClasseProgetti();
				cp2.addProgetti(p);
				try {
					// Writing to a file
					File file2=new File("projectDatabase.json");
					file2.createNewFile();
					objectMapper.writeValue(file2, cp2);
					FileWriter fileWriter = new FileWriter(file2); // in alternativa
					objectMapper.writeValue(fileWriter, cp2);
					fileWriter.close();
					}
					catch (IOException e2) {
						e2.printStackTrace();
					}
					w.write("Tentativo di creazione progetto ricevuto" + "\n");
					w.flush();
			}
			this.locker.unlock();
		}
	}
	
	/*
	 * 
	 */
	public void register(Writer w) throws IOException {
		//w.write("Tentativo di registrazione ricevuto" + "\n");
		//w.flush();
	}
	
	/*
	 * @params: u1 e u2 sono username e p1 e p2 sono password
	 * @effects: testiamo se le coppie corrispondono
	 * @return: se corrispondono Login ok return true altrimenti false
	 */
	private boolean findLogin(String u1,String p1,String u2,String p2) {
		//System.out.println(u1.equals(u2) + " "  + p1.equals(p2));
		if(u1.equals(u2) == true && p1.equals(p2) == true){
			return true;
		}
		return false;
	}
	
	/*
	 * @params: d Dato inviato dal client
	 * @effects: fa un controllo verficiando se i dati del parametro sono uguali
	 * ai dati nel file System grazie ad un metodo ausiliario, se tutto corrisponde
	 * e il Login e' corretto viene Cambiata la variabile di risposta al client
	 * passandogli il file Json che rappresenta i dati completi dell'utente
	 */
	public void logger(Writer w, Dato d) {
		Writer writer = null;
		File file=new File("usersDatabase.json");
		ObjectMapper objectMapper = new ObjectMapper();
		ClasseUtenti u;
		setFinalResponse("null");
		try {
			writer = new OutputStreamWriter(this.client.getOutputStream());
			u = objectMapper.readValue(file, ClasseUtenti.class);
			for(int index = 0; index < u.getListaUtenti().size(); index++) {
				if(findLogin(d.getUsername(),d.getPassword(),u.getListaUtenti().get(index).getUsername(),u.getListaUtenti().get(index).getPassword()) == true) {
					ClasseUtenti tmpC = new ClasseUtenti();
					tmpC.addUtente(u.getListaUtenti().get(index));
					//settare l utente online
					setFinalResponse(objectMapper.writeValueAsString(tmpC));
					this.serverN.update(0);
					w.write(objectMapper.writeValueAsString(tmpC) + "\n");
					w.flush();
					return;
				}
			}
			w.write("null" + "\n");
			w.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * @params: username -> string nome dell utente di cui vuoi vedere i progetti
	 * @effects: aggiorna la lista progetti e poi manda al client una stringa JSON con i progetti
	 * a cui fa parte
	 */
	public void listProject(String username, Writer w) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		File file=new File("projectDatabase.json");
		ClasseProgetti cp = null;
		Dato d = new Dato();
		d.setUsername(username);
		try {
			cp = objectMapper.readValue(file, ClasseProgetti.class);
			if(cp != null) {
				for(int i = 0; i < cp.getProgetti().size(); i++){
					if(cp.getProgetti().get(i).getPartecipanti().contains(username)) {
						d.addProgetto(cp.getProgetti().get(i).getName());
						d.addMulticastP(cp.getProgetti().get(i).getName(), cp.getProgetti().get(i).getMulticastAddress());
						System.out.println(cp.getProgetti().get(i).getName());
					}
				}
			}
			String res = getDatoString(d);
			
			w.write(res + "\n");
			w.flush();
			
		} catch (IOException e) {
			System.out.println("NON CI SONO PROGETTI");
			String res = getDatoString(d);
			w.write(res + "\n");
			w.flush();
		}
		
	}
	
	/*
	 * @params: username -> nome di un utente, project ->nome di un progetto
	 * @effects: aggiunge username alla lista dei membri del progetto project
	 */
	public void addMemberToProject(String username, String project, Writer w) throws InterruptedException, IOException {
		
		boolean ans = this.locker.tryLock(TIMEOUT_LIMIT,TimeUnit.SECONDS);
		
		if(!ans) {
			System.out.println("Tempo scaduto! trovato bloccato");
			w.write("none" + "\n");
			w.flush();
		}
		
		if(ans) {
			//this.locker.lock();
			ObjectMapper objectMapper = new ObjectMapper();
			File file=new File("projectDatabase.json");
			ClasseProgetti cp = null;
			try {
				cp = objectMapper.readValue(file, ClasseProgetti.class);
				for(int i = 0;i < cp.getProgetti().size(); i++){
					if(cp.getProgetti().get(i).getName().equals(project)) {
						cp.getProgetti().get(i).addPartecipanti(username);
					}
				}
				try {
					// Writing to a file
					File file2=new File("projectDatabase.json");
					file2.createNewFile();
					objectMapper.writeValue(file2, cp);
					FileWriter fileWriter = new FileWriter(file2); // in alternativa
					objectMapper.writeValue(fileWriter, cp);
					fileWriter.close();
					w.write("done" + "\n");
					w.flush();
					
					}
					catch (IOException e) {
						e.printStackTrace();
						w.write("none" + "\n");
						w.flush();
					}
				
			} catch (IOException e) {
				e.printStackTrace();
				w.write("none" + "\n");
				w.flush();
			}
			this.locker.unlock();
		}
		
	}
	

	/*
	 * @params: project -> String nome di un progetto
	 * @effects: manda al client un json con la lista dei membri di un progetto
	 */
	public void showMembers(String project, Writer w) {
		ObjectMapper objectMapper = new ObjectMapper();
		File file=new File("projectDatabase.json");
		ClasseProgetti cp = null;
		ArrayList<String> tmp = null;
		Dato d = new Dato();
		try {
			cp = objectMapper.readValue(file, ClasseProgetti.class);
			for(int i = 0; i < cp.getProgetti().size(); i++) {
				if(cp.getProgetti().get(i).getName().equals(project)) {
					tmp = cp.getProgetti().get(i).getPartecipanti();
				}
			}
		d.setListaNameUtenti(tmp);
		String json = getDatoString(d);
		w.write(json + "\n");
		w.flush();
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * @params: project -> nome del progetto nel quale vogliamo aggiungere la card
	 * @params: card -> nome della card
	 * @params: desc -> descrizione della card 
	 * @effects: Aggiunge una card al progetto che abbiamo scelto e salva i dati nel file system
	 */
	public void addCard(String project, String card, String desc, Writer w) throws IOException, InterruptedException {
		System.out.println(this.locker.isLocked());
		boolean ans = this.locker.tryLock(TIMEOUT_LIMIT,TimeUnit.SECONDS);
		
		if(!ans) {
			System.out.println("Tempo scaduto! trovato bloccato");
			w.write("none" + "\n");
			w.flush();
		}
		
		if(ans) {
			//this.locker.lock();
			ObjectMapper objectMapper = new ObjectMapper();
			File file=new File("projectDatabase.json");
			ClasseProgetti cp = null;
			Cards tmp = new Cards();
			tmp.setNome(card);
			tmp.setDesc(desc);
			try {
				cp = objectMapper.readValue(file, ClasseProgetti.class);
				for(int i = 0; i < cp.getProgetti().size(); i++) {
					if(cp.getProgetti().get(i).getName().equals(project)) {
						cp.getProgetti().get(i).addCards(tmp);
					}
				}
				
				try {
					// Writing to a file
					File file2=new File("projectDatabase.json");
					file2.createNewFile();
					objectMapper.writeValue(file2, cp);
					FileWriter fileWriter = new FileWriter(file2); // in alternativa
					objectMapper.writeValue(fileWriter, cp);
					fileWriter.close();
					//Thread.sleep(10000);
					w.write("done" + "\n");
					w.flush();
					
					}
					catch (IOException e) {
					e.printStackTrace();
					w.write("none" + "\n");
					w.flush();
					}
			}catch (IOException e) {
				e.printStackTrace();
				w.write("none" + "\n");
				w.flush();
			}
			this.locker.unlock();
		}
	}
	
	/*
	 * @params: project -> nome di un progetto
	 * @effects: manda al client la lista delle cards per il progetto scelto
	 */
	public void showCards(String project, Writer w) {
		ObjectMapper objectMapper = new ObjectMapper();
		File file=new File("projectDatabase.json");
		ClasseProgetti cp = null;
		Dato d = new Dato();
		ArrayList<String> listaCards = new ArrayList<String>();
		ArrayList<HashMap<String, Cards>> listaCardsC = new ArrayList<HashMap<String, Cards>>();
		try {
				cp = objectMapper.readValue(file, ClasseProgetti.class);
				for(int i = 0; i < cp.getProgetti().size(); i++) {
					if(cp.getProgetti().get(i).getName().equals(project)) {
						//for(int j = 0; j < cp.getProgetti().get(i).getCards().size(); j++) {
							//listaCards.add(cp.getProgetti().get(i).getCards().get(j).getNome());
						//}
						listaCardsC.add(cp.getProgetti().get(i).getTODO());
						listaCardsC.add(cp.getProgetti().get(i).getINPROGRESS());
						listaCardsC.add(cp.getProgetti().get(i).getTOBEREVISED());
						listaCardsC.add(cp.getProgetti().get(i).getDONE());
					}
				}
				d.setListaCardsC(listaCardsC);
				String json = getDatoString(d);
				w.write(json + "\n");
				w.flush();
			}catch(IOException e) {
				e.printStackTrace();
			}
		
	}
	
	/*
	 * @params: project -> stringa nome del progetto
	 * @params: card -> nome della crd
	 * @effects: manda al client la descrizione e lo stato della card scelta per il progetto scelto
	 */
	public void showCard(String project, String card, Writer w) {
		ObjectMapper objectMapper = new ObjectMapper();
		File file=new File("projectDatabase.json");
		ClasseProgetti cp = null;
		Dato d = new Dato();
		ArrayList<Cards> listaCards = new ArrayList<Cards>();
		try {
			cp = objectMapper.readValue(file, ClasseProgetti.class);
			for(int i = 0; i < cp.getProgetti().size(); i++) {
				if(cp.getProgetti().get(i).getName().equals(project)) {
					//for(int j = 0; j < cp.getProgetti().get(i).getCards().size(); j++) {
							//listaCards.add(cp.getProgetti().get(i).getCards().get(j));	
							listaCards.add(cp.getProgetti().get(i).getThisCard(card));
							System.out.println(cp.getProgetti().get(i).getThisCard(card));
							System.out.println("Nome card " + card);
					//}
				}
			}
			d.setCards(listaCards);
			String json = getDatoString(d);
			w.write(json + "\n");
			w.flush();
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * @params: project -> nome progetto
	 * @params: card -> nome card
	 * @params: codice -> intero che rappresenta il codice dello stato in cui vogliamo 
	 * spostare la card
	 * @effects: sposta la card (se possibile) nello stato che abbiamo inserito come input e salva
	 * il risultato nel filesystem
	 */
	public void moveCard(String project, String card,int codice, Writer w) throws IOException, InterruptedException {
		
		boolean ans = this.locker.tryLock(TIMEOUT_LIMIT,TimeUnit.SECONDS);
		
		if(!ans) {
			System.out.println("Tempo scaduto! trovato bloccato");
			w.write("none" + "\n");
			w.flush();
		}
		
		if(ans) {
			//this.locker.lock();
			ObjectMapper objectMapper = new ObjectMapper();
			File file=new File("projectDatabase.json");
			ClasseProgetti cp = null;
			try {
				cp = objectMapper.readValue(file, ClasseProgetti.class);
				for(int i = 0; i < cp.getProgetti().size(); i++) {
					if(cp.getProgetti().get(i).getName().equals(project)) {
						for(int j = 0; j < cp.getProgetti().get(i).getCards().size(); j++) {
								if(cp.getProgetti().get(i).getCards().get(j).getNome().equals(card)) {
									cp.getProgetti().get(i).getCards().get(j).setStato(codice);
									cp.getProgetti().get(i).moveCard(card, codice);
								}
						}
					}
				}
				try {
					// Writing to a file
					File file2=new File("projectDatabase.json");
					file2.createNewFile();
					objectMapper.writeValue(file2, cp);
					FileWriter fileWriter = new FileWriter(file2); // in alternativa
					objectMapper.writeValue(fileWriter, cp);
					fileWriter.close();
					
					w.write("done" + "\n");
					w.flush();
					
					}
					catch (IOException e) {
					e.printStackTrace();
					w.write("none" + "\n");
					w.flush();
					}
				
			}catch(IOException e) {
				e.printStackTrace();
				w.write("nope" + "\n");
				w.flush();
			}
			this.locker.unlock();
		}
	}
	
	/*
	 * @params: project -> nome progetto da cancellare
	 * @effects: cancella il progetto scelto
	 */
	public void cancelProject(String project, Writer w) throws InterruptedException, IOException {
		
		boolean ans = this.locker.tryLock(TIMEOUT_LIMIT,TimeUnit.SECONDS);
		
		if(!ans) {
			System.out.println("Tempo scaduto! trovato bloccato");
			w.write("none" + "\n");
			w.flush();
		}
		
		if(ans) {
			//this.locker.lock();
			ObjectMapper objectMapper = new ObjectMapper();
			File file=new File("projectDatabase.json");
			ClasseProgetti cp = null;
			try {
				cp = objectMapper.readValue(file, ClasseProgetti.class);
				for(int i = 0; i < cp.getProgetti().size(); i++) {
					if(cp.getProgetti().get(i).getName().equals(project)) {
						int count = 0;
						int num = cp.getProgetti().get(i).getCards().size();
						for(int j = 0; j < num; j++) {
							if(cp.getProgetti().get(i).getCards().get(j).getStato() == 4) {
								count = count + 1; 
							}	
						}
						if(count == num) {
							//cancelliamo il progetto
							cp.getProgetti().remove(i);
							try {
								// Writing to a file
								File file2=new File("projectDatabase.json");
								file2.createNewFile();
								objectMapper.writeValue(file2, cp);
								FileWriter fileWriter = new FileWriter(file2); // in alternativa
								objectMapper.writeValue(fileWriter, cp);
								fileWriter.close();
								
								w.write("done" + "\n");
								w.flush();
								
								}
								catch (IOException e) {
								e.printStackTrace();
								w.write("none" + "\n");
								w.flush();
								}
						}else {
							w.write("nope" + "\n");
							w.flush();
						}
					}
				}
				}catch(IOException e) {
					e.printStackTrace();
				}
			this.locker.unlock();
		}
		
	}
	
	/*
	 * @effects: recupera e manda al client l indirizzo multicast della chat
	 */
	public void getAddressOfChat(String username,String message,String project, Writer w) throws IOException {
		Messaggio msgTmp = new Messaggio();
		ObjectMapper objectMapper = new ObjectMapper();
		String address = null;
		File file=new File("projectDatabase.json");
		ClasseProgetti cp = null;
		try {
			cp = objectMapper.readValue(file, ClasseProgetti.class);
			if(cp != null) {
				for(int i = 0; i < cp.getProgetti().size(); i++){
					if(cp.getProgetti().get(i).getName().equals(project)) {
						address = cp.getProgetti().get(i).getMulticastAddress();
					}
				}
			}
		} catch (IOException e) {
			System.out.println("NON CI SONO PROGETTI");
			w.write("Errore, nessun progetto trovato" + "\n");
			w.flush();
		}

		w.write(address + "\n");
		w.flush();
		
	}
	
	
	/*---------------------------------------------------------------------*/
	
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
			System.out.println("Deserialized object from JSON");
			System.out.println("-----------------------");
			System.out.println("Utente " + d.getComando());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return d;
	}
	
	/*
	 * @params: c deve essere un comando scelto, verificato dal client
	 * @effects: viene eseguito il comando scelto dal client 
	 */
	public void switcherComandi(String c,Writer w) throws IOException, InterruptedException {
		Dato d = deserealizeData(c);
		switch(d.getComando()) {
			case 1:
				register(w);
				break;
			case 2:
				logger(w,d);
				break;
			case 3:
				createProject(d.getUsername(),w,d.getNameProject());
				break;
			case 4:
				listProject(d.getUsername(),w);
				break;
			case 5:
				addMemberToProject(d.getUsername(),d.getNameProject(),w);
				break;
			case 6:
				showMembers(d.getNameProject(),w);
				break;
			case 7:
				addCard(d.getNameProject(),d.getNameCard(),d.getDescCard(),w);
				break;
			case 8:
				showCards(d.getNameProject(),w);
				break;
			case 9:
				showCard(d.getNameProject(),d.getNameCard(),w);
				break;
			case 10:
				moveCard(d.getNameProject(),d.getNameCard(),d.getCodiceCard(),w);
				break;
			case 11:
				cancelProject(d.getNameProject(),w);
				break;
			case 12:
				getAddressOfChat(d.getUsername(),d.getMessageToChat(),d.getNameProject(),w);
			default:
		}
	}
	
	
	/*
	 * @effects: Metodo che ci permette di effettuare i compiti del Thread 
	 */
	@Override
	public void run() {
		
		BufferedReader reader = null;
		Writer writer = null; 
		
		try
		{	
			//viene letto il messaggio del client
			reader = new BufferedReader(
				new InputStreamReader(this.client.getInputStream()));
			writer = new OutputStreamWriter(this.client.getOutputStream());

			while(true)
			{
				String response = reader.readLine();
				System.out.println("RESP " + response);
				System.out.println("---");
				
				switcherComandi(response,writer); //metodo che serve per decidere quale operazione effettuare
				if(response == null ||
				response.equalsIgnoreCase("quit"))
					break;
				String output = "[" + this.host + "] " + response;
				System.out.println(output);
				//writer.write(this.finalResponse + "\n");
				writer.flush();
				
			}
		}
		catch(IOException ex){
			// possibile disconnessione del nodo
			// ignoro
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		finally{
			// Clean
			try{
				reader.close();
				writer.close();
				this.client.close();
			}
			catch(IOException ex){
				ex.printStackTrace();
			}		
			System.out.println("[" + this.host + "] " +	">> Connessione terminata <<");
		}
	}

}
