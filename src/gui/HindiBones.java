package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.LinkedList;

import javax.swing.JFrame;

import datenstruktur.Boden;
import datenstruktur.Heiltrank;
import datenstruktur.Monster;
import datenstruktur.Schluessel;
import datenstruktur.Spielelement;
import datenstruktur.Spieler;
import datenstruktur.Tuer;
import datenstruktur.Wand;

import client.engine.ClientEngine;
import client.engine.TestumgebungClientEngine;

public class HindiBones extends JFrame implements KeyListener {

	private static final long serialVersionUID = 1L;

	private Spielflaeche spielflaeche;
	private Statusleiste statusleiste;
	private Highscore highscore;
	private MenuLeiste menuLeiste;
	private Steuerung steuerung;

	public LinkedList<Monster> monsterListe;
	public Spieler spieler;
	public Spielelement[][] level;

	public int currentLevel = 0;
	public boolean spielende = false;
	public boolean verloren = false;
	public long startZeit;
	public int benoetigteZeit;
	public boolean nebelAn = true;

	private boolean spielerInHighscore = false;
	public boolean highscoreAngezeigt = false;

	public final int MAXLEVEL = 5;
	public final int WIDTH = 16;
	public final int HEIGHT = 16;
	public final int BOX = 32;

	public HindiBones(int width, int height, String title) {
		ClientEngine cEngine = new ClientEngine();
		TestumgebungClientEngine testClientEngine = new TestumgebungClientEngine();

		cEngine.chatte("<#godmode");
		cEngine.chatte("Hallo");

		testClientEngine.serverAntwort(0, "Die Hindi Bones brennen");
		System.out.println("Hallo Test");
		System.out.flush();


		initialisiereJFrame(width, height, title);
		starteNeuesSpiel();

	}

	public void initialisiereJFrame(int width, int height, String title) {
		// Layout fuer unser Fenster
		this.setLayout(new BorderLayout());
		// Erzeuge Objekte der Panels
		this.spielflaeche = new Spielflaeche(this);
		this.statusleiste = new Statusleiste(this);
		this.steuerung = new Steuerung();
		this.highscore = new Highscore();
		// Erzeuge Menuleiste
		this.menuLeiste = new MenuLeiste(this);
		// Es wird die gewuenschte Groesse angegeben
		spielflaeche.setPreferredSize(new Dimension(width, height));
		statusleiste.setPreferredSize(new Dimension(width, BOX));
		steuerung.setPreferredSize(new Dimension(width, height + BOX));
		highscore.setPreferredSize(new Dimension(width, height + BOX));
		// Erstelle das Spielfeld
		zeigeSpielfeld();
		// Zentriere das Fenster auf dem Bildschirm
		final Dimension d = this.getToolkit().getScreenSize();
		this.setLocation((int) ((d.getWidth() - this.getWidth()) / 2),
				(int) ((d.getHeight() - this.getHeight()) / 2));
		// Standardsetup
		this.addKeyListener(this);
		this.setResizable(false);
		this.setTitle(title);
		this.setVisible(true);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
	}

	public void zeigeSpielfeld() {
		// entferne alles
		highscoreAngezeigt = false;
		this.remove(highscore);
		this.remove(steuerung);
		// erstelle das Spielfeld
		this.add(spielflaeche, BorderLayout.CENTER);
		this.add(statusleiste, BorderLayout.SOUTH);
		this.add(menuLeiste, BorderLayout.NORTH);
		// aktiviere das fertige Spielfeld
		this.requestFocus();
		this.pack();
	}

	public void zeigeHighscore() {
		// entferne alles
		highscoreAngezeigt = true;
		this.remove(spielflaeche);
		this.remove(statusleiste);
		this.remove(steuerung);
		// erstelle die Highscoreanzeige
		this.add(highscore, BorderLayout.CENTER);
		// aktiviere die Highscoreanzeige
		this.requestFocus();
		this.pack();
		highscore.repaint();
	}

	public void zeigeSteuerung() {
		// entferne alles
		highscoreAngezeigt = false;
		this.remove(spielflaeche);
		this.remove(statusleiste);
		this.remove(highscore);
		// erstelle die Steuerungsanzeige
		this.add(steuerung, BorderLayout.CENTER);
		// aktiviere die Steuerungsanzeige
		this.requestFocus();
		this.pack();
		steuerung.repaint();
	}

	// Getter fuer die Spielflaeche bzw. Statusleiste
	public Spielflaeche getSpielflaeche() {return spielflaeche;}
	public Statusleiste getStatusleiste() {return statusleiste;}
	public Highscore getHighscore() {return highscore;}

	// Methoden der Schnittstelle KeyListener

	public void keyPressed(KeyEvent e) {
		// Aktuelle Position des Spielers
		int xPos = spieler.getXPos();
		int yPos = spieler.getYPos();

		// Frage Tastatureingaben auf den Pfeiltasten ab.
		// Es wird geprueft, ob der naechste Schritt zulaessig ist.
		// Bleibt die Figur innerhalb der Grenzen des Arrays?
		// Wenn ja, ist das naechste Feld begehbar?
		// Falls beides "wahr" ist, dann gehe den naechsten Schritt
		if (!spielende) {
			if (e.getKeyCode() == KeyEvent.VK_UP) {
				if (yPos > 0 && !(level[xPos][yPos - 1] instanceof Wand))
					spieler.hoch();
			} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
				if (yPos < HEIGHT - 1 && !(level[xPos][yPos + 1] instanceof Wand))
					spieler.runter();
			} else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
				if (xPos > 0 && !(level[xPos - 1][yPos] instanceof Wand))
					spieler.links();
			} else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
				if (xPos < WIDTH - 1 && !(level[xPos + 1][yPos] instanceof Wand))
					spieler.rechts();
			} else if (e.getKeyCode() == KeyEvent.VK_Q) {
				Monster m = spieler.angriffsMonster();
				if (m != null)
					m.changeHealth(-BOX / 4);
			// B f�r 'Heiltrank benutzen'
			} else if (e.getKeyCode() == KeyEvent.VK_B){
				int change = spieler.benutzeHeiltrank();
				// Heilungseffekt wird verbessert, falls neue Monster durch das Aufheben des Schl�ssels ausgel�st wurden
				if (spieler.hatSchluessel())
					spieler.changeHealth((int)(change*1.5));
				else
					spieler.changeHealth((int)(change*0.5));
			} else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
				System.exit(0);
			}
		}

		if (e.getKeyCode() == KeyEvent.VK_SPACE) {
			// Schluessel aufnehmen
			if (level[spieler.getXPos()][spieler.getYPos()] instanceof Schluessel) {
				spieler.nimmSchluessel();
				level[spieler.getXPos()][spieler.getYPos()] = new Boden();
			}
			// Heiltrank aufnehmen
			else if (level[spieler.getXPos()][spieler.getYPos()] instanceof Heiltrank) {
				spieler.nimmHeiltrank((Heiltrank) level[spieler.getXPos()][spieler.getYPos()]);		
				level[spieler.getXPos()][spieler.getYPos()] = new Boden();
			}
			// Schluessel benutzen
			if (level[spieler.getXPos()][spieler.getYPos()] instanceof Tuer) {
				if (!((Tuer) level[spieler.getXPos()][spieler.getYPos()]).istOffen() && spieler.hatSchluessel()) {
					((Tuer) level[spieler.getXPos()][spieler.getYPos()]).setOffen();
					// Nach dem Oeffnen der Tuer ist der Schluessel wieder weg
					spieler.entferneSchluessel();
					if (currentLevel < MAXLEVEL)
						nextLevel();
					else {
						spielende = true;
					}
				}
			}
		}

	}

	public void keyReleased(KeyEvent e) {}
	public void keyTyped(KeyEvent e) {}

	public void spielZuruecksetzen() {

		spieler = new Spieler("img//spieler.png", this);
		monsterListe = new LinkedList<Monster>();
		level = new Spielelement[WIDTH][HEIGHT];

		currentLevel = 0;
		spielende = false;
		verloren = false;
		nebelAn = true;
		nextLevel();
		spielerInHighscore = false;
		startZeit = System.currentTimeMillis();
	}

	// Spielschleife
	public void starteNeuesSpiel() {
		spielZuruecksetzen();

		do {

			if (!spielende) {
				// Hier wird alle 50ms neu gezeichnet
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {}

				getSpielflaeche().repaint();
				getStatusleiste().repaint();

				if (spieler.getHealth() <= 0) {
					spielende = true;
					verloren = true;
				}
			} else {
				benoetigteZeit = (int) ((System.currentTimeMillis() - startZeit) / 1000);

				if (!verloren && !spielerInHighscore) {
					getHighscore().addSpielerToHighScore(benoetigteZeit);
					getHighscore().repaint();
					spielerInHighscore = true;
				} else {
					getSpielflaeche().repaint();
				}
			}

		} while (true);

	}

	public void nextLevel() {
		currentLevel++;

		Leser leser = new Leser("lvl//level" + currentLevel + ".txt", this);
		level = leser.getLevel();

	}

}
