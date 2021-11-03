package progettoReti;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.fasterxml.jackson.databind.ObjectMapper;

public class DatabaseStartGetter {
	

	public static void main(String[] args) {
		
		//classe per provare la classe che gestisce gli utenti
		
		/*
		 * Questa classe non e' utile ai fini del progetto
		 * e' stata utilizzata solo per testare la gestione degli utenti
		 */
		
		ObjectMapper objectMapper = new ObjectMapper();
		File file=new File("currentData.json");
		Dato d;
		try {
			d = objectMapper.readValue(file, Dato.class);
			System.out.println("Deserialized object from JSON");
			System.out.println("-----------------------");
			System.out.println("Utente " + d.getComando());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
