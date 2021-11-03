package progettoReti;

import java.util.ArrayList;

public class ClasseProgetti {

		/*
		 * OVERVIEW: Classe di gestione dei progetti
		 * La classe e' utile per la creazione e serializzazione del JSON in modo
		 * da poter contenere una lista dei progetti
		 */
	
		private ArrayList<Progetti> progetti = new ArrayList<Progetti>(); //lista dei progetti
		
		public void setProgetti(ArrayList<Progetti> p) {
			this.progetti = p;
		}
		
		//metodi GET
		
		public ArrayList<Progetti> getProgetti(){
			return this.progetti;
		}
		
		
		/*
		 * @params: p progetto
		 * @effects: aggiunge un progetto alla lista
		 */
		public void addProgetti(Progetti p) {
			this.progetti.add(p);
		}
		
		/*
		 * @params: user -> nome utente, project -> nome progetto
		 * @effects: cerca il nome di un utente dentro un progetto
		 * per verificare se e' membro
		 * @return: TRUE se lo e'. FALSE altrimenti 
		 */
		public boolean findMember(String user,String project) {
			for(int i = 0; i < this.progetti.size(); i++) {
				if(this.progetti.get(i).getName().equals(project)) {
					if(this.progetti.get(i).getPartecipanti().contains(user)) {
						return true;
					}else {
						return false;
					}
				}
			}
			return false;
		}
		
		/*
		 * @params: name -> nome progetto
		 * @effects: controlla che ci sia un progetto con quel nome
		 * @return: TRUE -> se contiene , FALSE altrimenti
		 */
		public boolean findProject(String name) {
			for(int i = 0; i < this.progetti.size(); i++) {
				if(this.progetti.get(i).getName().equals(name) == true) {
					return true;
				}
			}
			return false;
			
		}
	
}
