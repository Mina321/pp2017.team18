package gui;

import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

import datenstruktur.Boden;
import datenstruktur.Monster;
import datenstruktur.Schluessel;
import datenstruktur.Spielelement;
import datenstruktur.Tuer;
import datenstruktur.Wand;

public class Leser {

	private String dateiname;
	private Spielelement[][] karte;
	
	private HindiBones fenster;
	
	public Leser(String fileName, HindiBones fenster){
		this.fenster = fenster;
		this.dateiname = fileName;
		
		readLevel();
	}
	
	private void readLevel(){
		try {
			FileReader reader = new FileReader(dateiname);
		
			int i = 0, j = 0, c;
			
			karte = new Spielelement[fenster.WIDTH][fenster.HEIGHT];
			fenster.monsterListe = new LinkedList<Monster>();		
			
			while((c = reader.read()) != -1){	
				if(c == '\n'){
					i=0;
					j++;
				}else{
					if(c >= '0' && c <= '9'){
						
						switch(c){
							case '0': karte[i][j] = new Wand(); break;
							case '1': karte[i][j] = new Boden(); break;
							case '2': karte[i][j] = new Schluessel(); break;
							case '3': karte[i][j] = new Tuer(false); break;
							case '4': karte[i][j] = new Tuer(true); fenster.spieler.setPos(i, j); break;
							case '5': karte[i][j] = new Boden(); fenster.monsterListe.add(new Monster(i,j, fenster, 0)); break;
							// Monster, welche erst nach dem Aufheben des Schluessels erscheinen
							case '6': karte[i][j] = new Boden(); fenster.monsterListe.add(new Monster(i,j, fenster, 1)); break;
						}	
						i++;
					}
					
				}
				
					
			}
			
			reader.close();
			
		} catch (IOException e) {
			System.err.println(e);
		}
	}
	
	public Spielelement[][] getLevel(){
		return karte;
	}
}
