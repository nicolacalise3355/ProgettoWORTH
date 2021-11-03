package progettoReti;

import java.io.IOException;
import java.net.UnknownHostException;

public class ClientMainClass {
	
	public static void main(String[] args) throws UnknownHostException, IOException {
		int port = 9995; //porta di connessione
		String localhost = "localhost"; //localhost
		ClientMenu menu = new ClientMenu(port,localhost);
		menu.mainMenu();
	}
	
	
}
