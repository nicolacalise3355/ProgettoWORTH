package progettoReti;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.fasterxml.jackson.databind.ObjectMapper;

public class RegistererClass extends RemoteServer implements RegistererInterface{

	private ReentrantLock locker;

	public RegistererClass(ReentrantLock l) {
		this.locker = l;
	}
	
	@Override
	public boolean registerAccount(String s) throws RemoteException, InterruptedException {
		boolean ans = this.locker.tryLock(60,TimeUnit.SECONDS);
		
		if(!ans) {
			System.out.println("Errore nella Registrazione");
			return false;
		}
		
		//creazione dato tramite JSON s 
		ObjectMapper objectMapper = new ObjectMapper();
		Dato u = null;
		try {
			u = objectMapper.readValue(s, Dato.class);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		Utenti tmp = new Utenti();
		tmp.setUsername(u.getUsername());
		tmp.setPassword(u.getPassword());
		
		//-------------------------------------------------------------//
		//recupero informazione dal filesystem
		ObjectMapper objectMapper2 = new ObjectMapper();
		File file=new File("usersDatabase.json");
		ClasseUtenti cu = null;
		try {
			cu = objectMapper2.readValue(file, ClasseUtenti.class);
			for(int i = 0; i < cu.getListaUtenti().size(); i++) {
				if(cu.getListaUtenti().get(i).getUsername().equals(tmp.getUsername())) {
					return false;
				}
			}
			cu.addUtente(tmp);
			
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		//-------------------------------------------------------------//

		try {
			// Scrittura sul filesystem
			File file2=new File("usersDatabase.json");
			file2.createNewFile();
			objectMapper.writeValue(file2, cu);
			FileWriter fileWriter = new FileWriter(file2); // in alternativa
			objectMapper.writeValue(fileWriter, cu);
			fileWriter.close();
		    this.locker.unlock();
			}
			catch (IOException e) {
			e.printStackTrace();
			this.locker.unlock();
			return false;
			}
		
		return true;
	}


}
