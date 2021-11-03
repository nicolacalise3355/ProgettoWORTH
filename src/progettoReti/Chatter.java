package progettoReti;

import java.io.IOException;
import java.net.InetAddress;

public class Chatter implements Runnable{
	
	/*
	 * OVERVIEW:
	 * Classe che implementa Runnable in modo da essere lanciata come Thread da parte di un client
	 * per gestire le proprie Chat
	 * Viene passato un oggetto MultiCastServer in modo da contenere le chats del client
	 */
	
	MulticastServer ms;
	
	//metodo costruttore
	public Chatter(MulticastServer m) {
		this.ms = m;
	}

	@Override
	public void run() {
		try {
			//System.out.println("Lanciato Thread per Chat");
			this.ms.starter();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	

}
