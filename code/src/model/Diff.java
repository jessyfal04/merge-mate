package model;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.*;
import com.github.difflib.text.DiffRow;
import com.github.difflib.text.DiffRowGenerator;


public class Diff {  // Classe Diff

	String oldFormatedString = ""; // String formattée pour les anciennes lignes
	String newFormatedString = ""; // String formattée pour les nouvelles lignes
	ArrayList <Modification> modifications = new ArrayList<Modification>(); // Liste des modifications

	// Générer les modifications entre 2 fichiers
	public void setDiff(Fichier fichierRef, Fichier fichierModif) { // Méthode setDiff avec 2 paramètres de type Fichier
		DiffRowGenerator generator = DiffRowGenerator.create() // Créer un générateur de lignes de différence
				.showInlineDiffs(true) // Afficher les différences en ligne
				.inlineDiffByWord(true) // Différencier les mots en ligne
				.oldTag(f -> "~") // Ancienne balise
				.newTag(f -> "*") // Nouvelle balise
				.build(); // Construire

		List<DiffRow> rows = generator.generateDiffRows(fichierRef.getLignes(), fichierModif.getLignes()); // Générer les lignes de différence

		// Lignes en 1 seul string
		String oldString = "";
		String newString = "";

		// On parcourt les lignes
		for (DiffRow row : rows) {
			// On gère les avec que 1 saut d ligne
			if (row.getTag() == DiffRow.Tag.DELETE && row.getOldLine().length() == 2) {
				oldString += "~\n~";
			}
			else if (row.getTag() == DiffRow.Tag.INSERT && row.getNewLine().length() == 2) {
				newString += "*\n*";
			}

			// On gère les lignes normales
			else {
				oldString += row.getOldLine() + "\n";
				newString += row.getNewLine() + "\n";
			}
		}

		int iModif = 0; // Compteur de modifications
		Modification modification = null;

		int iOld = 0; int iOld_ = iOld; 		// Index de la string oldString
		int iNew = 0; int iNew_ = iNew; 		// Index de la string newString
		// On parcourt les 2 strings 
		while (iOld < oldString.length() || iNew < newString.length()) {

			// On gère les modifications
			if (iOld < oldString.length() && iNew < newString.length() && oldString.charAt(iOld) == '~' && newString.charAt(iNew) == '*') {
				// On passe les caractères de borne de début
				iOld++;
				iNew++;
				
				// On ajoute le token de modification à la string, on incrémente et on crée une nouvelle modification
				newFormatedString += "["+ iModif +"]";
				oldFormatedString += "["+ iModif +"]";
				iModif++;
				modification = new Modification(Modification.ModifType.CHANGE);

				boolean encore = true;
				while (encore) {
					// On récupère les modifications
					while (iOld < oldString.length() && oldString.charAt(iOld) != '~') {
						modification.addOldString(String.valueOf(oldString.charAt(iOld)));
						iOld++;
					}

					while (iNew < newString.length() && newString.charAt(iNew) != '*') {
						modification.addNewString(String.valueOf(newString.charAt(iNew)));
						iNew++;
					}

					// On passe les caractères de borne de fin
					iOld++;
					iNew++;

					// Si le suivant est un espace et que le prochain est une modification
					encore = 
						(
							iOld+1 < oldString.length() && iNew+1 < newString.length()
							&& ((oldString.charAt(iOld) == ' ' && newString.charAt(iNew) == ' ') || (oldString.charAt(iOld) == '.' && newString.charAt(iNew) == '.')) 
							&& oldString.charAt(iOld+1) == '~' && newString.charAt(iNew+1) == '*'
						) ||
						(
							iOld+2 < oldString.length() && iNew+2 < newString.length()
							&& ((oldString.charAt(iOld) == '.' && newString.charAt(iNew) == '.') && (oldString.charAt(iOld+1) == ' ' && newString.charAt(iNew+1) == ' '))
							&& oldString.charAt(iOld+2) == '~' && newString.charAt(iNew+2) == '*'
						)
					;
					if (encore){
						// On ajoute l'espace
						while (iOld < oldString.length() && oldString.charAt(iOld) != '~') {
							modification.addOldString(String.valueOf(oldString.charAt(iOld)));
							iOld++;
						}

						while (iNew < newString.length() && newString.charAt(iNew) != '*') {
							modification.addNewString(String.valueOf(newString.charAt(iNew)));
							iNew++;
						}

						// On passe les caractères de borne de début
						iOld++;
						iNew++;
					}
				}
				

				// On ajoute la modification
				modifications.add(modification);
			}
			// On gère les suppressions
			else if (iOld < oldString.length() && oldString.charAt(iOld) == '~') {

				// On passe le caractère de borne de début
				iOld++;
				
				// On ajoute le token de modification à la string, on incrémente et on crée une nouvelle modification
				oldFormatedString += "["+ iModif +"]";
				iModif++;
				modification = new Modification(Modification.ModifType.DELETE);

				// On récupère les modifications
				while (iOld < oldString.length() && oldString.charAt(iOld) != '~') {
					modification.addOldString(String.valueOf(oldString.charAt(iOld)));
					iOld++;
				}

				// On ajoute la modification
				modifications.add(modification);

				// On passe le caractère de borne de fin
				iOld++;
			}
			// On gère les insertions
			else if (iNew < newString.length() && newString.charAt(iNew) == '*') {

				// On passe le caractère de borne de début
				iNew++;
				
				// On ajoute le token de modification à la string, on incrémente et on crée une nouvelle modification
				newFormatedString += "["+ iModif +"]";
				iModif++;
				modification = new Modification(Modification.ModifType.INSERT);

				// On récupère les modifications
				while (iNew < newString.length() && newString.charAt(iNew) != '*') {
					modification.addNewString(String.valueOf(newString.charAt(iNew)));
					iNew++;
				}

				// On ajoute la modification
				modifications.add(modification);

				// On passe le caractère de borne de fin
				iNew++;
			}
			// On gère les lignes sans modifications
			else {

				// On ajoute le token de modification à la string, on incrémente et on crée une nouvelle modification
				newFormatedString += "["+ iModif +"]";
				oldFormatedString += "["+ iModif +"]";
				iModif++;
				modification = new Modification(Modification.ModifType.NO_CHANGE);

				// On récupère les modifications
				while (iOld < oldString.length() && iNew < newString.length() && oldString.charAt(iOld) != '~' && newString.charAt(iNew) != '*' ) {
					modification.addOldString(String.valueOf(oldString.charAt(iOld)));
					modification.addNewString(String.valueOf(newString.charAt(iNew)));
					iOld++;
					iNew++;
				}

				// On ajoute la modification
				modifications.add(modification);
			}
		
			if (iOld_ == iOld && iNew_ == iNew) {
				break;
			}
			iOld_ = iOld;
			iNew_ = iNew;
		}
		
	}

	// FormatedString
	public String getOldFormatedString() {
		return oldFormatedString;
	}

	public String getNewFormatedString() {
		return newFormatedString;
	}

	// Modifications
	public ArrayList<Modification> getModifications() {
		return modifications;
	}

	/* Nombre de modifs restantes */
	public int getNbCompte() {
		int nb = 0;
		for (Modification modification : modifications) {
			if (modification.compte()) {
				nb++;
			}
		}
		return nb;
	}

	public int getNbComptePlus() {
		int nb = 0;
		for (Modification modification : modifications) {
			if (modification.comptePlus()) {
				nb++;
			}
		}
		return nb;
	}

	/* modif in old / new */
	public boolean modifInOld(int modification) {
		return oldFormatedString.contains("["+ modification +"]");
	}

	public boolean modifInNew(int modification) {
		return newFormatedString.contains("["+ modification +"]");
	}

	/* JSON */
	public JsonObject getJsonObject(){
		JsonObject json = new JsonObject();
		json.addProperty("oldFormatedString", oldFormatedString);
		json.addProperty("newFormatedString", newFormatedString);

		JsonArray jsonModifications = new JsonArray();
		for (Modification modification : modifications) {
			jsonModifications.add(modification.getJsonObject());
		}
		json.add("modifications", jsonModifications);

		return json;
	}

	public void fromJson(JsonObject json) {
		// On récupère les strings oldFormatedString et newFormatedString
		oldFormatedString = json.get("oldFormatedString").getAsString();
		newFormatedString = json.get("newFormatedString").getAsString();

		// On fait la liste des modifications
		modifications = new ArrayList<Modification>();
		JsonArray jsonModifications = json.getAsJsonArray("modifications");
		for (JsonElement jsonModification : jsonModifications) {
			Modification modification = new Modification(jsonModification.getAsJsonObject());
			modifications.add(modification);
		}
	}
}


