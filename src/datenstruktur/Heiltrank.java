package datenstruktur;

public class Heiltrank extends Spielelement {
	private int wirkung;
	
	public Heiltrank(int wirkung){
		this.wirkung = wirkung;
	}
	
	public int getWirkung(){
		return wirkung;
	}
}
