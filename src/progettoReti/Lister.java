package progettoReti;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Lister extends RemoteServer implements ListerInterface{
	
	public HashMap<String, Boolean> status = new HashMap<String, Boolean>(); //hashMap che rappresenta
	//una coppia Nome - Stato per dirci se un utente e' online o no
	
	//metodo costruttore
	public Lister() throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		File file=new File("usersDatabase.json");
		ClasseUtenti cu = objectMapper.readValue(file, ClasseUtenti.class);
		for(int i = 0; i < cu.getListaUtenti().size(); i++) {
			this.status.put(cu.getListaUtenti().get(i).getUsername(), false);
		}
	}
	
	/*
	 * @effects: effettua un "Refresh" della struttura dati per aggiornare nel caso
	 * sia stato inserito un nuovo utente
	 */
	public void updateList() throws JsonParseException, JsonMappingException, IOException{
		ObjectMapper objectMapper = new ObjectMapper();
		File file=new File("usersDatabase.json");
		ClasseUtenti cu = objectMapper.readValue(file, ClasseUtenti.class);
		for(int i = 0; i < cu.getListaUtenti().size(); i++) {
			if(!this.status.containsKey(cu.getListaUtenti().get(i).getUsername())) {
				this.status.put(cu.getListaUtenti().get(i).getUsername(), true);
			}
		}
	}


	@Override
	public String getList() throws RemoteException{
		ObjectMapper objectMapper2 = new ObjectMapper();
		File file=new File("usersDatabase.json");
		ClasseUtenti cu = null;
		Dato d = new Dato();
		String resp = null;
		try {
			cu = objectMapper2.readValue(file, ClasseUtenti.class);
			d.setListaUtenti(cu.getListaUtenti()); 
			resp = objectMapper2.writeValueAsString(d);
			return resp;
		}catch(IOException e){
			e.printStackTrace();
		
		}
		return null;
	}
	

	@Override
	public String getOnlineUsers() throws RemoteException {
		ObjectMapper objectMapper = new ObjectMapper();
		Dato d = new Dato();
		d.setStatusUsers(this.status);
		String resp = null;
		try {
			resp = objectMapper.writeValueAsString(d);
			return resp;
		}catch(IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void updateUserStatus(String name) throws RemoteException {
		try {
			this.updateList();
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		boolean tmp = this.status.get(name);
		if(tmp == true){
			this.status.put(name, false);
		}else{
			this.status.put(name, true);
		}
	}

	@Override
	public boolean findOnlineUser(String user) throws RemoteException {
		 if(this.status.containsKey(user)) {
			 return this.status.get(user);
		 }
		return false;
	}

}
