package model;

import java.util.ArrayList;

import com.google.gson.JsonObject;

public class Historique {
	/*
	 
	Ajouter un état
	Faire un nb max d'état
	Récupérer un état après ou avant
	Vérifier qu'un état avant ou après est récupérable

	 
	 */

	private ArrayList<JsonObject> etatsListe = new ArrayList<JsonObject>();
	private int etatCourant = -1;
	
	public void ajouterEtat(JsonObject etat) {
		// Retirer tous les état après le courant
		if (etatCourant != -1) {
			for (int i = etatsListe.size() - 1; i > etatCourant; i--) {
				etatsListe.remove(i);
			}
		}

		// Ajouter l'état
		etatsListe.add(etat);

		// Mettre à jour l'état courant
		etatCourant = etatsListe.size() - 1;

		// Si le nombre d'état est supérieur à 10, retirer le premier
		if (etatsListe.size() > 10) {
			etatsListe.remove(0);
			etatCourant--;
		}
	}

	public boolean etatApresExiste() {
		return etatCourant < etatsListe.size() - 1;
	}

	public boolean etatAvantExiste() {
		return etatCourant > 0;
	}

	public JsonObject getEtatApres() {
		if (etatApresExiste()) {
			etatCourant++;
			return etatsListe.get(etatCourant);
		}
		return null;
	}

	public JsonObject getEtatAvant() {
		if (etatAvantExiste()) {
			etatCourant--;
			return etatsListe.get(etatCourant);
		}
		return null;
	}
}
