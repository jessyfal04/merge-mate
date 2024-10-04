package model;

import java.util.List;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Fichier {
	
	private List<String> lignes;
	private String chemin;

	public void setLignes(List<String> lignes) {
		this.lignes = lignes;
	}

	public List<String> getLignes() {
		return lignes;
	}

	public void setChemin(String chemin) {
		this.chemin = chemin;
	}

	public String getChemin() {
		return chemin;
	}

	public boolean estChoisi() {
		return chemin != null;
	}
	
	// Convertir les lignes en un objet JSON
	public JsonObject getJsonObject() {
		// Concaténer les lignes en une seule chaîne JSON
		StringBuilder jsonBuilder = new StringBuilder();
		for (String line : lignes) {
			jsonBuilder.append(line);
		}
		String jsonString = jsonBuilder.toString();

		// Parser la chaîne JSON en un objet JsonObject
		return JsonParser.parseString(jsonString).getAsJsonObject();
	}
}
