package model;
import com.google.gson.JsonObject;

public class Modification {
	private String oldString = "";
	private String newString = "";
	private String oldStringOriginal= "";
	private String newStringOriginal= "";
	private String commentaire = "";

	public enum ModifType { // Type de modification
		CHANGE,
		DELETE,
		INSERT,
		NO_CHANGE
	}
	private ModifType modifType; // Type de modification

	public enum ValidType { // Type de validation
		Valider,
		Refuser,
		NSP
	}
	public ValidType validType = ValidType.NSP; // Type de validation par défaut

	// Constructeurs
	public Modification(ModifType modifType) {
		this.modifType = modifType;
	} // Constructeur pour les modifications de type CHANGE, DELETE, INSERT

	public Modification(JsonObject jsonObject) { // Constructeur pour les modifications de type CHANGE, DELETE, INSERT
		oldStringOriginal = jsonObject.get("oldStringOriginal").getAsString(); // Récupérer les valeurs du JSON
		newStringOriginal = jsonObject.get("newStringOriginal").getAsString(); // Récupérer les valeurs du JSON
		oldString = jsonObject.get("oldString").getAsString(); // Récupérer les valeurs du JSON
		newString = jsonObject.get("newString").getAsString(); // Récupérer les valeurs du JSON
		modifType = ModifType.valueOf(jsonObject.get("modifType").getAsString()); // Récupérer les valeurs du JSON
		validType = ValidType.valueOf(jsonObject.get("validType").getAsString()); // Récupérer les valeurs du JSON
		commentaire = jsonObject.get("commentaire").getAsString(); // Récupérer les valeurs du JSON
	}

	// ModifType
	public ModifType getModifType() {
		return modifType;
	}

	// ValidType
	public boolean estValider() {
		return validType == ValidType.Valider;
	}

	public boolean estRefuser() {
		return validType == ValidType.Refuser;
	}

	public void setValider() {
		if (validType == Modification.ValidType.Valider) {
			validType = Modification.ValidType.NSP;
		} else {
			validType = Modification.ValidType.Valider;
		}
	}

	public void setRefuser() {
		if (validType == Modification.ValidType.Refuser) {
			validType = Modification.ValidType.NSP;
		} else {
			validType = Modification.ValidType.Refuser;
		}
	}

	// oldStringOriginalet NewString
	public String getOldString() {
		return oldString;
	}

	public String getNewString() {
		return newString;
	}

	public void addOldString(String c) {
		oldStringOriginal+= c;
		oldString += c;
	}

	public void addNewString(String c) {
		newStringOriginal+= c;
		newString += c;
	}

	/* Commentaire */
	public void setCommentaire(String commentaire) {
		this.commentaire = commentaire;
	}

	public String getCommentaire() {
		return commentaire;
	}

	/* Contre-Édition */
	public void setContreEdition(boolean oldOuNew, String txt) {
		if (oldOuNew || modifType == ModifType.NO_CHANGE)
			oldString = txt;
		if (!oldOuNew || modifType == ModifType.NO_CHANGE)
			newString = txt;
	}

	public void supprimerEdition(boolean oldOuNew) {
		if (oldOuNew || modifType == ModifType.NO_CHANGE)
			oldString = oldStringOriginal;
		if (!oldOuNew || modifType == ModifType.NO_CHANGE)
			newString = newStringOriginal;
	}

	public boolean estContreEdite(boolean oldOuNew) {
		if (oldOuNew)
			return !oldStringOriginal.equals(oldString);
		else
			return !newStringOriginal.equals(newString);
	}
	
	/* NbModifs Restantes */
	public boolean compte() {
		return modifType != ModifType.NO_CHANGE;
	}

	public boolean comptePlus() {
		return validType != ValidType.NSP;
	}

	/* JSON */
	public JsonObject getJsonObject() {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("oldStringOriginal", oldStringOriginal);
		jsonObject.addProperty("newStringOriginal", newStringOriginal);
		jsonObject.addProperty("oldString", oldString);
		jsonObject.addProperty("newString", newString);
		jsonObject.addProperty("modifType", modifType.toString());
		jsonObject.addProperty("validType", validType.toString());
		jsonObject.addProperty("commentaire", commentaire);
		return jsonObject;
	}
}