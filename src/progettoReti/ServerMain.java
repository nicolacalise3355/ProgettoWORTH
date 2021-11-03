package progettoReti;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class ServerMain {
	
	public static void main(String[] args) throws JsonParseException, JsonMappingException, IOException {
		int port = 9995; //porta 
		String localhost = "localhost";
		Server s = new Server(port,localhost);
		s.runServer();
	}
	
}
