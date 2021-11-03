package progettoReti;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import com.fasterxml.jackson.databind.ObjectMapper;

public class MulticastServer{

	/*
	 * OVERVIEW:
	 * Avviato da un Thread Chatter serve all utente per mettersi in ascolto di eventuali messaggi che arrivano e 
	 * memorizza tutto quello che arriva nelle rispettive chats, un utente puo' usare i suoi metodi per accedere alle chats
	 */
	
	private InetAddress multicastGroup; //indirizzo multicast
	private String address; //indirizzo
	
	private HashMap<String, Vector<Messaggio>> ChatsPerProgetti = new HashMap<>();
	
	String getChatRequest = "///requestmessage///";
	
	private Vector<String> listaMgJoined;
	
	Vector<Messaggio> listaMessaggi = new Vector<>(); //lista messaggi
	
	BufferedReader in = null;
	
	private int port;
	
	MulticastSocket multicastS;
	
	
	//metodo costruttore
	public MulticastServer(HashMap<Integer, String> listaProgetti) throws IOException{
		this.port = 30000;
		Iterator it = listaProgetti.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry)it.next();
			this.ChatsPerProgetti.put((String) entry.getValue(), new Vector<Messaggio>());
		}
		this.multicastS = new MulticastSocket(this.port);
		this.listaMgJoined = new Vector<String>();
	}
	
	/*
	 * @effects: ritorna la lista delle chats
	 * @return: ChatsPerProgetti
	 */
	public HashMap<String, Vector<Messaggio>> getChatsHM(){
		return this.ChatsPerProgetti;
	}
	
	/*
	 * @params: String -> Name project
	 * @effects: aggiunge un nuovo progetto alla lista
	 */
	public void addProjectToList(String name) {
		this.ChatsPerProgetti.put(name, new Vector<Messaggio>());
	}
	

	/*
	 * @params: ma -> String indirizzo multicast da aggiungere alla lista
	 * @effects: aggiunge ma alla lista degli indirizzo multicast in cui abbiamo joinato
	 */
	public void addToListOfJoined(String ma) {
		this.listaMgJoined.add(ma);
	}
	
	/*
	 * @params: ma -> String indirizzo multicast
	 * @effects: controlla se un indirizzo multicast apparitene alla lista di quelli in cui siamo joinati
	 * @return: true se quell indirizzo appartiene, false altrimenti
	 */
	public boolean isJoined(String ma) {
		if(this.listaMgJoined.contains(ma) == true) {
			return true;
		}else {
			return false;
		}
	}
	
	/*
	 * @params: address -> String indirizzo multicast
	 * @effects: joina in un indirizzo multicast per ricevere i messaggi
	 */
	public void joiner(String address) throws IOException {
			addToListOfJoined(address);
			multicastS.joinGroup(InetAddress.getByName(address));
	}
	
	/*
	 * @params: m -> messaggio
	 * @params: projectName -> nome del progetto a cui aggiungere un messaggio
	 * @effects: aggiunge un messaggio alla lista messaggi
	 */
	public void addMessage(String progectName, Messaggio m) {
		//this.listaMessaggi.add(m);
		String tmpName = m.getProgetto();
		this.ChatsPerProgetti.get(tmpName).add(m);
	}
	
	/*
	 * @params: json -> deve essere una stringa formattata in JSON
	 * @effects: traduce la stringa e istanzia un oggetto di tipo messaggio
	 * @return: ritorna l oggetto Messaggio creato dalla stringa json
	 */
	public Messaggio deserealizeData(String json) {
		ObjectMapper objectMapper = new ObjectMapper();
		Messaggio d = null;
		try {
			d = objectMapper.readValue(json, Messaggio.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return d;
	}
	
	/*
	 * @params: mex -> Messaggio da serializzare
	 * @effects: traduce mex in una stringa
	 * @return: dato serializzato 
	 */
	public String serealizeData(Messaggio mex) {
		ObjectMapper objectMapper = new ObjectMapper();
		String textObject = null;
		try {
			textObject = objectMapper.writeValueAsString(mex);
			}
			catch (IOException e) {
			e.printStackTrace();
			}
			return textObject;
	}
	
	/*
	 * @params: project-> String nome del progetto da recuperare
	 * @effects: crea un oggetto di tipo Chats e gli viene aggiunta la lista dei messaggi
	 * chats e' realizzato in modo da poterlo poi serializzare in JSON
	 * @return: ritorniamo la stringa JSON dell'oggetto che contiene i messaggi
	 */
	public String getChatJson(String project) {
		ObjectMapper objectMapper = new ObjectMapper();
		String textObject = null;
		Chats chats = new Chats();
		chats.setMessaggi(this.ChatsPerProgetti.get(project));
		try { 
			textObject = objectMapper.writeValueAsString(chats);
			}
			catch (IOException e) {
			e.printStackTrace();
			}
			return textObject;
	}
	
	/*
	 * @params: nameProject -> nome del progetto
	 * @params: address -> indirizzo multicast
	 * @params: message -> messaggio
	 * @params: auth -> autore
	 * @effects: invia un messaggio all indirizzo address con corpo message, autore auth e nome progetto nameProject
	 */
	public void sendMessage(String nameProject, String address, String message, String auth) throws UnknownHostException {
		String toSend;
		Messaggio tmp = new Messaggio();
		tmp.setGruppo(nameProject);
		tmp.setMessaggio(message);
		tmp.setAutore(auth);
		toSend = serealizeData(tmp);
        DatagramPacket dat = new DatagramPacket(new byte[256], 256);
        InetAddress mcadd = InetAddress.getByName(address);
    	try {
			byte[] data;
			data = toSend.getBytes();
            DatagramPacket dat2 = new DatagramPacket(data,data.length,mcadd,this.port);
            //System.out.println("cosa invio :" + new String(dat2.getData(), dat2.getOffset(), dat2.getLength()));
            multicastS.send(dat2);		
                        
        }
        catch(IOException e){
            e.printStackTrace();
        }
	}
	
	/*
	 * effects: si mette in ascolto e riceve un messaggio, a seconda del tipo di messaggio
	 * o invia al client la chats di progetto, oppure aggiunge un messaggio alla chat
	 */
	public void starter() throws IOException, InterruptedException {
		byte[] buf = new byte[256];
				//ciclo infinito del server
				while(true) {
					//riceviamo un messaggio
		            DatagramPacket dat = new DatagramPacket(new byte[256], 256);
		            this.multicastS.receive(dat);
		            String resp = new String(dat.getData(), dat.getOffset(), dat.getLength());
		            //System.out.println(resp);
		            Messaggio tmp = deserealizeData(resp);
		            addMessage(tmp.getProgetto(),tmp);
		            //contrialliamo che tipo di messaggio e', se e' un messaggio specifigo per richiedere
		            //la chat allora lo verifichiamo
		            
				}

		
	}
	
	
}
