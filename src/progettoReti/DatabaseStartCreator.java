package progettoReti;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import com.fasterxml.jackson.databind.ObjectMapper;

public class DatabaseStartCreator {
	
	/**
	 *	OVERVIEW:
	 *
	 *	Questa classe serve per generare il file JSON iniziale che contenga un numero di utenti
	 *	Questa classe non e' utile ai fini del progetto, ma e' stata utilizzata solo la prima volta per generare
	 *  un file JSON per poter iniziare a provare le funzioni e le classi
	 * 
	 */

	public static void main(String[] args) {
		
		ObjectMapper objectMapper = new ObjectMapper();
		
		
		ArrayList<Progetti> p = new ArrayList<Progetti>();
		Progetti p1 = new Progetti();
		p1.setName("Progetto1");
		p1.addPartecipanti("Naigel");
		
		Progetti p2 = new Progetti();
		p2.setName("Progetto2");
		p2.addPartecipanti("Naigel");
		
		p.add(p1);
		p.add(p2);
		ClasseProgetti cp = new ClasseProgetti();
		cp.setProgetti(p);
		
		try {
			// Writing to a file
			File file=new File("projectDatabase.json");
			file.createNewFile();
			System.out.println("Writing JSON object to file");
			System.out.println("-----------------------");
			objectMapper.writeValue(file, cp);
			FileWriter fileWriter = new FileWriter(file); // in alternativa
			objectMapper.writeValue(fileWriter, cp);
			fileWriter.close();
			System.out.println("Writing JSON object to string");
			System.out.println(objectMapper.writeValueAsString(cp));
			}
			catch (IOException e) {
			e.printStackTrace();
			}
		
	}

}
