package controler;

import com.google.gson.JsonObject;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.scene.web.WebEngine;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import model.Diff;
import model.Fichier;
import model.Historique;
import model.Modification;
import view.Fenetre;

public class Controleur {
	// Classes perso
	Diff diff;
	Historique historique;

	//type de fichiers
	public enum TypeFichier {
		Ref,
		Modif,
		ImportEtat
	}
	ArrayList<Fichier> fichiers = new ArrayList<>();

	// Elements de la vue
	Fenetre fenetre;
	Stage primaryStage;
	WebEngine webEngineRef;
	WebEngine webEngineModif;
	boolean modeSombre = false;
	Button btnValider;
	Button btnRefuser;
	TextField txtCommentaire;
	Menu menuSauvegarder;

	MenuItem itemResultat;
	MenuBar menuBar;
	MenuItem itemUndo;
	MenuItem itemRedo;
	MenuItem itemEtat;
	MenuItem itemEnregistrer;
	MenuItem mode;
	Menu resultat;
	MenuItem itemApercu;

	Menu sauvegarde;

	Menu interfaceA;

	// Controleur
	int derniereSauvegarde = -1; // -1 : pas de sauvegarde, 0 : sauvegarde res , 1 : sauvegarde etat
	String pathDerniereSauvegarde = ""; // chemin de la dernière sauvegarde
	String nameDerniSauvegarde = "indéfini"; // nom de la dernière sauvegarde

	boolean resultatPasSauvegarde = false;
	boolean etatPasSauvegarde = false;

	boolean ctrlDown = false;
	int modifActuelle = -1;
	int contreEdit = -1; // -1 : pas de contre-édition, 0 : contre-édition old, 1 : contre-édition new, 2 : contre-édition les deux

	// Constructeur
	public Controleur(Fenetre fenetre, Stage primaryStage, Diff diff, WebEngine webEngineRef, WebEngine webEngineModif, Button btnValider, Button btnRefuser, TextField txtCommentaire, MenuItem itemResultat, MenuItem itemUndo, MenuItem itemRedo, MenuItem itemEnregistrer, MenuItem mode, MenuBar menuBar, Menu sauvegarde,  Menu interfaceA, MenuItem itemEtat, Menu menuSauvegarder, Menu resultat, MenuItem itemApercu) {
		this.fenetre = fenetre;
		this.diff = diff;
		this.webEngineRef = webEngineRef;
		this.webEngineModif = webEngineModif;
		this.primaryStage = primaryStage;
		this.btnValider = btnValider;
		this.btnRefuser = btnRefuser;
		this.txtCommentaire = txtCommentaire;
		this.itemResultat = itemResultat;
		this.itemUndo = itemUndo;
		this.itemRedo = itemRedo;
		this.itemEnregistrer = itemEnregistrer;
		this.mode = mode;
		this.menuBar = menuBar;
		this.sauvegarde = sauvegarde;
        this.itemEtat = itemEtat;
		this.interfaceA = interfaceA;
		this.menuSauvegarder = menuSauvegarder;
		historique = new Historique();
		this.resultat = resultat;
		this.itemApercu = itemApercu;

		// On crée les fichiers
		for (int i = 0; i < 3; i++)
			fichiers.add(new Fichier());
	}

	/* ModifActuelle */
	public int getModifActuelle() {
		return modifActuelle;
	}

	/* FICHIERS */
	public boolean choisir(TypeFichier typeFichier, Button btn) {
		Fichier fichier = fichiers.get(typeFichier.ordinal());
		// On vérifie si c'est un fichier texte ou json
		boolean txtOrJson = (typeFichier == TypeFichier.ImportEtat);
		// On ouvre une fenêtre pour choisir un fichier
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Choissiez un fichier texte");
		fileChooser.setInitialDirectory(new File("res"));
		if (txtOrJson)
			fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers json", "*.json"));
		else
			fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers texte", "*.txt"));

		File fichierRetour = fileChooser.showOpenDialog(primaryStage); // On récupère le fichier choisi
		boolean aChoisi = fichierRetour != null; // On vérifie si l'utilisateur a choisi un fichier

		if (aChoisi) {
			// On récupère le chemin du fichier et les lignes, puis on les stocke dans l'objet fichier
			String chemin = fichierRetour.getAbsolutePath();

			// Vérifier que le fichier a < 10000 caractères
			if (cheminVersTailleCaractere(chemin) > 10000) {
				btn.setText("Fichier trop grand"); // On met le texte en erreur
				btn.setStyle("-fx-background-color: #ff7979;"); // On met la couleur en rouge
				return false;
			} else if (!txtOrJson && cheminVersContientCaractereInterdit(chemin)) {
				btn.setText("Fichier avec caractères interdits"); // On met le texte en erreur
				btn.setStyle("-fx-background-color: #ff7979;"); // On met la couleur en rouge
				return false;
			}

			fichier.setChemin(chemin); // On met le chemin dans l'objet fichier
			fichier.setLignes(cheminVersLignes(chemin)); // On met les lignes dans l'objet fichier

			// On met le texte en succès avec le nom du fichier, on met la couleur en bleu
			btn.setText("Fichier sélectionné : " + fichierRetour.getName());
			btn.setStyle("-fx-background-color: #7ed6df; -fx-font-size: 15px; -fx-text-fill: black;");
		} 
		else {
			// On met le modèle à null
			fichier.setChemin(null);
			fichier.setLignes(null);

			// On met le texte en erreur, on met la couleur en rouge
			btn.setText("Sélectionnez un fichier");
			btn.setStyle("-fx-background-color: #ff7979; -fx-font-size: 15px; -fx-text-fill: black;");
		}

		return aChoisi;
	}

	private int cheminVersTailleCaractere(String chemin) { // On récupère la taille du fichier
		int taille = 0;
		try {
			try (BufferedReader in = new BufferedReader(new FileReader(chemin))) { // On lit le fichier
				String line = ""; // On lit ligne par ligne
				while ((line = in.readLine()) != null) { // Tant qu'il y a des lignes
					taille += line.length(); // On ajoute la taille de la ligne à la taille totale
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return taille; // On retourne la taille totale
	}

	private boolean cheminVersContientCaractereInterdit(String chemin) { // On vérifie si le fichier contient des caractères interdits
		try {
			try (BufferedReader in = new BufferedReader(new FileReader(chemin))) { // On lit le fichier
				String line = "";
				while ((line = in.readLine()) != null) {
					if (line.contains("~") || line.contains("*")) // Si la ligne contient ~ ou *
						return true; // On retourne vrai
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false; // On retourne faux
	}

	private static List<String> cheminVersLignes(String chemin) { // On récupère les lignes du fichier
		List<String> lines = new LinkedList<String>(); // On crée une liste de lignes
		String line = "";
		try {
			// Read line with utf8 encoding
			try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(chemin), "UTF8"))) { // On lit le fichier en utf8 pour les caractères spéciaux
				while ((line = in.readLine()) != null) { // Tant qu'il y a des lignes
					lines.add(line); // On ajoute la ligne à la liste
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return lines; // On retourne la liste de lignes
	}


	/* Affichage et actualisation boutons */
	public void afficherDiff() {
		actualiserBoutons();

		ArrayList<Modification> modifications = diff.getModifications(); // On récupère les modifications
		String htmlRef; // On crée une string pour le html de la référence
		String htmlModif; // On crée une string pour le html de la modification


		if(modeSombre == true){
			htmlRef = "<style>body {background:#2c3e50; color:white}</style>" + diff.getOldFormatedString();
			htmlModif = "<style>body {background:#34495e; color:white}</style>" + diff.getNewFormatedString();
		} else {
			htmlRef = "<style>body {background:#c7ecee}</style>" + diff.getOldFormatedString();
			htmlModif = "<style>body {background:#dff9fb}</style>" + diff.getNewFormatedString();
		}

		// On remplace les modifications par les nouvelles chaines avec le style
		int supIndice = 1;
		for (int i = 0; i < modifications.size(); i++) {
			Modification modification = modifications.get(i);

			String oldString = modifications.get(i).getOldString();
			String newString = modifications.get(i).getNewString();
			
			String style = "";
			String styleOld = "";
			String styleNew = "";

			if (oldString.equals("") && modification.estContreEdite(true)) // Si c'est une contre-édition
					oldString = "<sup style= \"font-family: 'Trebuchet MS', sans-serif;\">[VIDE]</sup>"; // On met [VIDE]
			if (newString.equals("") && modification.estContreEdite(false)) // Si c'est une contre-édition
				newString = "<sup style= \"font-family: 'Trebuchet MS', sans-serif;\">[VIDE]</sup>"; // On met [VIDE]
			if (newString.equals("\n")) // Si c'est un saut de ligne
				newString = "<sup style= \"font-family: 'Trebuchet MS', sans-serif;\">[SAUT]</sup>\n"; // On met [SAUT]
			if (oldString.equals("\n"))
				oldString = "<sup style= \"font-family: 'Trebuchet MS', sans-serif;\">[SAUT]</sup>\n"; // On met [SAUT]

			if (modification.getModifType() == Modification.ModifType.CHANGE) { // Si c'est une modification
				style = "color:orange;";
				oldString = "<strike>" + oldString + "</strike><sup style= \"font-family: 'Trebuchet MS', sans-serif;\">("+supIndice+")</sup>"; // On met le texte barré et l'indice
				newString = "<strong>" + newString + "</strong><sup style= \"font-family: 'Trebuchet MS', sans-serif;\">("+supIndice+")</sup>"; // On met le texte en gras et l'indice
				
				supIndice++;
			}
			else if (modification.getModifType() == Modification.ModifType.DELETE) { // Si c'est une suppression
				style = "color:red;";
				oldString = "<strike>" + oldString + "</strike>"; // On met le texte barré
			}
			else if (modification.getModifType() == Modification.ModifType.INSERT) { // Si c'est une insertion
				style = "color:green;";
				newString = "<strong>" + newString + "</strong>"; // On met le texte en gras
			}

			if (!modification.getCommentaire().equals("")) { // Si il y a un commentaire
				String pathCommentSvg = new File("config/comment.svg").getAbsolutePath(); // On récupère le chemin du svg
				oldString += "<sup><img src=\"file://"+pathCommentSvg+"\" alt=\"*\" height=\"10em\" width=\"10em\"></sup>"; // On ajoute le svg
				newString += "<sup><img src=\"file://"+pathCommentSvg+"\" alt=\"*\" height=\"10em\" width=\"10em\"></sup>"; // On ajoute le svg
			}

			// Style Background
			if (modifActuelle == i) {
				if (contreEdit == -1)
					style = "background-color:#fffa65;";
				else if (contreEdit == 0) {
					styleOld += "background-color:#7efff5;";

					styleNew += style;
					style = "";
				}
				else if (contreEdit == 1) {
					styleNew += "background-color:#7efff5;";

					styleOld += style;
					style = "";
				} 
				else if (contreEdit == 2) {
					style = "background-color:#7efff5;";
				}
			}
			else if (modification.estValider())
				style = "background-color:#badc58;";
			else if (modification.estRefuser())
				style = "background-color:#ff7979;";

			
			if (modification.estContreEdite(true))
				styleOld += "text-decoration: underline; text-decoration-color: blue;";
			if (modification.estContreEdite(false))
				styleNew += "text-decoration: underline; text-decoration-color: blue;";

			
			oldString = "<span id='"+i+"' onmousedown='controleur.clicSurModification("+i+", true)' style='"+style+styleOld+"'>"+oldString+"</span>"; // On met le texte dans un span avec l'id de la modification
			newString = "<span id='"+i+"' onmousedown='controleur.clicSurModification("+i+", false)' style='"+style+styleNew+"'>"+newString+"</span>"; // On met le texte dans un span avec l'id de la modification
			
			htmlRef = htmlRef.replace("[" + i + "]", oldString);
			htmlModif = htmlModif.replace("[" + i + "]", newString);
		}

		// On met les sauts de ligne
		htmlRef = htmlRef.replace("\n", "<br>");
		htmlModif = htmlModif.replace("\n", "<br>");

		// On met le texte dans les webviews
		webEngineRef.loadContent(htmlRef);
		webEngineModif.loadContent(htmlModif);
	}

	public void actualiserBoutons() { // On actualise les boutons
		// Résultats
		itemResultat.setText("Résultat (" + diff.getNbComptePlus() + "/" + diff.getNbCompte() + ") " + (resultatPasSauvegarde ? "*" : "")); // On met le texte du bouton résultat
		itemEtat.setText("État " + (etatPasSauvegarde ? "*" : "")); // On met le texte du bouton état

		// Items Disable
		itemResultat.setDisable(diff.getNbCompte() != diff.getNbComptePlus());
		itemUndo.setDisable(!historique.etatAvantExiste());
		itemRedo.setDisable(!historique.etatApresExiste());
		itemEnregistrer.setDisable(derniereSauvegarde == -1 || (derniereSauvegarde == 0 && diff.getNbCompte() != diff.getNbComptePlus()));
		itemEnregistrer.setText("Rapide (" + nameDerniSauvegarde + ")");

		// Modifs
		if (modifActuelle == -1) {
			btnValider.setDisable(true);
			btnRefuser.setDisable(true);
			txtCommentaire.setDisable(true);

			btnValider.setStyle("-fx-background-color: #d3d3d3;");
			btnValider.setText("Valider");
			btnRefuser.setStyle("-fx-background-color: #d3d3d3;");
			btnRefuser.setText("Refuser");
			txtCommentaire.setText("");
		}
	
	}


	/* Poursuivre */
	public void poursuivre(Button btnPoursuivre) {
		Fichier fichierRef = fichiers.get(TypeFichier.Ref.ordinal()); // On récupère le fichier de référence
		Fichier fichierModif = fichiers.get(TypeFichier.Modif.ordinal()); // On récupère le fichier de modification
		Fichier fichierImportEtat = fichiers.get(TypeFichier.ImportEtat.ordinal()); // On récupère le fichier état

		// Si l'utilisateur a choisi les deux fichiers
		if (fichierRef.estChoisi() && fichierModif.estChoisi() && !fichierRef.getChemin().equals(fichierModif.getChemin())){
			// On compare les deux fichiers et on affiche le diff
			diff.setDiff(fichierRef, fichierModif);
			historique.ajouterEtat(diff.getJsonObject());
			resultatPasSauvegarde = true; etatPasSauvegarde = true;

			afficherDiff();

			fenetre.afficherScene(1);
		}
		else if (fichierImportEtat.estChoisi()) {
			// On importe l'état
			diff.fromJson(fichierImportEtat.getJsonObject());
			historique.ajouterEtat(diff.getJsonObject());
			resultatPasSauvegarde = true; etatPasSauvegarde = true;

			afficherDiff();

			fenetre.afficherScene(1);
		}
		else {
			btnPoursuivre.setStyle("-fx-background-color: #ff7979;");
		}
	}


	/* MODIFS */	
	public void clicSurModification(int i, boolean oldOuNew) { // Quand on clique sur une modification
		if (ctrlDown == false) { // Si on ne fait pas ctrl
			if(i == modifActuelle) // Si on clique sur la même modification
				i = -1; // On met i à -1
			else if (diff.getModifications().get(i).getModifType() == Modification.ModifType.NO_CHANGE)
				i = -1;
			contreEdit = -1;
		} else {
			if(diff.getModifications().get(i).getModifType() == Modification.ModifType.NO_CHANGE) 
				contreEdit = 2;
			else
				contreEdit = oldOuNew ? 0 : 1;
		}

		modifActuelle = i;
		
		afficherDiff();

		if (i != -1) { // Si i est différent de -1
			Modification modification = diff.getModifications().get(modifActuelle);

			if (ctrlDown == false) {
				if(modeSombre == false) {
					btnValider.setStyle("-fx-background-color: #badc58; -fx-font-size: 20px; -fx-text-fill: black;");
					btnRefuser.setStyle("-fx-background-color: #ff7979; -fx-font-size: 20px; -fx-text-fill: black;");
				}else{
					btnValider.setStyle("-fx-background-color: #366136; -fx-font-size: 20px; -fx-text-fill: white;");
					btnRefuser.setStyle("-fx-background-color: #9d0000; 	-fx-font-size: 20px; -fx-text-fill: white;");
				}

				btnValider.setText("Valider");
				btnRefuser.setText("Refuser");
				btnValider.setDisable(false);
				btnRefuser.setDisable(false);
				txtCommentaire.setDisable(false);

				if(modification.estValider()) {
					btnValider.setText("Annuler Valider");
				} else if (modification.estRefuser()) {
					btnRefuser.setText("Annuler Refuser");
				}

				txtCommentaire.setText(diff.getModifications().get(i).getCommentaire());
			} else {
				// On affiche la modale
				fenetre.afficherModaleContreEdit(oldOuNew ? modification.getOldString() : modification.getNewString(), oldOuNew, modification.estContreEdite(oldOuNew));

				modifActuelle = -1;
				afficherDiff();
			}
		}
	}

	public void valider(){ 	// Quand on clique sur valider
		diff.getModifications().get(modifActuelle).setValider(); // On valide la modification
		modifActuelle = -1; // On met la modification actuelle à -1

		historique.ajouterEtat(diff.getJsonObject()); // On ajoute l'état à l'historique
		resultatPasSauvegarde = true; etatPasSauvegarde = true; // On met les sauvegardes à true

		afficherDiff();
	}

	public void refuser(){ // Quand on clique sur refuser
		diff.getModifications().get(modifActuelle).setRefuser(); // On refuse la modification
		modifActuelle = -1; // On met la modification actuelle à -1

		historique.ajouterEtat(diff.getJsonObject()); // On ajoute l'état à l'historique
		resultatPasSauvegarde = true; etatPasSauvegarde = true; // On met les sauvegardes à true

		afficherDiff();	
	}

	public void commenter() { // Quand on clique sur commenter
		int i = modifActuelle; // On récupère la modification actuelle
		if (i != -1) {
			diff.getModifications().get(i).setCommentaire(txtCommentaire.getText()); // On met le commentaire
		}

		historique.ajouterEtat(diff.getJsonObject()); // On ajoute l'état à l'historique
		resultatPasSauvegarde = true; etatPasSauvegarde = true;


		afficherDiff();
	}
	

	/* Sauvegarde */
	public void resultat(boolean derniere) { // Sauvegarde du résultat
		String resultat = "";

		for (Modification modification : diff.getModifications()) // On parcourt les modifications
			resultat += modification.estValider() ? modification.getNewString() : modification.getOldString(); // On ajoute la nouvelle chaine si elle est validée, sinon l'ancienne

		File fichierResultat; // On crée un fichier pour le résultat

		// Si c'est la première sauvegarde, on demande où sauvegarder, sinon on sauvegarde dans le même fichier
		if (!derniere) {
			// On demande à l'utilisateur où il veut sauvegarder le fichier

			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Sauvegardez le fichier résultat");
			fileChooser.setInitialDirectory(new File("res"));
			fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers texte", "*.txt"));

			fichierResultat = fileChooser.showSaveDialog(primaryStage); // On récupère le fichier choisi
		} else {
			fichierResultat = new File(pathDerniereSauvegarde);
		}

		if (fichierResultat != null) { // Si l'utilisateur a choisi un fichier
			String chemin = fichierResultat.getAbsolutePath(); // On récupère le chemin du fichier
			try {
				try (BufferedWriter out = new BufferedWriter(new FileWriter(chemin))) { // On écrit dans le fichier
					out.write(resultat); // On écrit le résultat

					derniereSauvegarde = 0;
					pathDerniereSauvegarde = chemin;
					nameDerniSauvegarde = fichierResultat.getName();
					resultatPasSauvegarde = false;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	
		afficherDiff();
	}
	public String getContenuFichierRes() { // On récupère le contenu du fichier résultat
		String resultat = "";

		for (Modification modification : diff.getModifications()) {
			if(modification.estValider())
				resultat += modification.getNewString();
			else if(modification.estRefuser() || modification.getModifType() == Modification.ModifType.NO_CHANGE)
				resultat += modification.getOldString();
			else
				resultat += "<sup style= \"font-family: 'Trebuchet MS', sans-serif;\">[A DECIDER]</sup>";
		}
		resultat = resultat.replace("\n", "<br>");
		return resultat;
	}




	public void etat(boolean derniere) { // Sauvegarde de l'état
		JsonObject jsonEtat = diff.getJsonObject();

		// Si c'est la première sauvegarde, on demande où sauvegarder, sinon on sauvegarde dans le même fichier
		File fichierEtat;
		if (!derniere) {
			// On demande à l'utilisateur où il veut sauvegarder le fichier

			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Sauvegardez l'état");
			fileChooser.setInitialDirectory(new File("res"));
			fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers json", "*.json"));

			fichierEtat = fileChooser.showSaveDialog(primaryStage);
		} else {
			fichierEtat = new File(pathDerniereSauvegarde);
		}

		if (fichierEtat != null) {
			String chemin = fichierEtat.getAbsolutePath();
			try {
				try (BufferedWriter out = new BufferedWriter(new FileWriter(chemin))) {
					out.write(jsonEtat.toString());

					derniereSauvegarde = 1;
					pathDerniereSauvegarde = chemin;
					nameDerniSauvegarde = fichierEtat.getName();
					etatPasSauvegarde = false;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	
		afficherDiff();
	}

	public void sauvegarde() { // Sauvegarde rapide
		if (derniereSauvegarde == 0 && diff.getNbCompte() == diff.getNbComptePlus())
			resultat(true);
		else if (derniereSauvegarde == 1)
			etat(true);
		else
			System.out.println("Sauvegarde rapide impossible");
	}


	/* Undo Redo */
	public void undo() { // Annuler
		if (historique.etatAvantExiste()) {
			diff.fromJson(historique.getEtatAvant());
			resultatPasSauvegarde = true; etatPasSauvegarde = true;
			afficherDiff();
		} else {
			System.out.println("Annulation impossible");
		}
		afficherDiff();
	}

	public void redo() { // Rétablir
		if (historique.etatApresExiste()) {
			diff.fromJson(historique.getEtatApres());
			resultatPasSauvegarde = true; etatPasSauvegarde = true;
			afficherDiff();
		} else {
			System.out.println("Rétablissement impossible");
		}
		afficherDiff();
	}
	
	
	/* Ctrl */
	public void toggleCtrl(boolean ctrlDown) { // Quand on appuie sur ctrl
		this.ctrlDown = ctrlDown;
	}

	public void contreEditier(boolean oldOuNew, String txt) { // Contre-édition
		diff.getModifications().get(modifActuelle).setContreEdition(oldOuNew, txt);
		historique.ajouterEtat(diff.getJsonObject());

		afficherDiff();
	}

	public void supprimerEdition(boolean oldOuNew) { // Supprimer l'édition
		diff.getModifications().get(modifActuelle).supprimerEdition(oldOuNew);
		historique.ajouterEtat(diff.getJsonObject());

		afficherDiff();
	}

	/* Mode Sombre Clair */
	public void AfficheModeSombre(MenuItem modeMenuItem) { // Afficher le mode sombre
		if(modeSombre == false) { // Si on est en mode clair
			modeSombre = true;
			modeSombre(); // Appeler la méthode pour passer en mode sombre
			modeMenuItem.setText("Mode clair");
		} else {
			modeClair(); // Appeler la méthode pour revenir en mode clair
			modeMenuItem.setText("Mode Sombre");
		}
	}

	public void modeSombre() {
		modeSombre = true;
				String darkModeStyle = "background-color: #2c3e50; color: white;";
				// Appliquer le style aux chaînes HTML
				String htmlRef = "<div style='" + darkModeStyle + "'>" + diff.getOldFormatedString() + "</div>";
				String htmlModif = "<div style='" + darkModeStyle + "'>" + diff.getNewFormatedString() + "</div>";
				// Mettre à jour les WebViews avec les nouvelles chaînes HTML
				webEngineRef.loadContent(htmlRef);
				webEngineModif.loadContent(htmlModif);
				primaryStage.getScene().getRoot().setStyle("-fx-background-color: #0c0b0b; -fx-font-size: 20px; -fx-text-fill: white;");
				menuSauvegarder.setStyle("-fx-font-size: 15px; -fx-text-fill: white;");
				btnValider.setStyle("-fx-background-color: #366136; -fx-font-size: 20px; -fx-text-fill: white;");
				btnValider.setPrefSize(200, 100);
				btnRefuser.setStyle("-fx-background-color: #9d0000; -fx-font-size: 20px; -fx-text-fill: white;");
				btnRefuser.setPrefSize(200, 100);
				txtCommentaire.setStyle("-fx-background-color: #0c0b0b; -fx-font-size: 20px; -fx-text-fill: white;");
				menuBar.setStyle(" -fx-background-color: #0c0b0b; -fx-font-size: 15px; -fx-text-fill: white;");
				itemResultat.setStyle("-fx-background-color: #0c0b0b; -fx-font-size: 15px; -fx-text-fill: white;");
				sauvegarde.setStyle(" -fx-font-size: 15px; -fx-text-fill: white;");
				interfaceA.setStyle("-fx-font-size: 15px; -fx-text-fill: white;");
				itemUndo.setStyle(" -fx-background-color: #0c0b0b;-fx-font-size: 15px; -fx-text-fill: white;");
				itemRedo.setStyle("-fx-background-color: #0c0b0b;-fx-font-size: 15px; -fx-text-fill: white;");
				itemEnregistrer.setStyle(" -fx-background-color: #0c0b0b;-fx-font-size: 15px; -fx-text-fill: white;");
				itemEtat.setStyle("-fx-background-color: #0c0b0b; -fx-font-size: 15px; -fx-text-fill: white;");
				resultat.setStyle("-fx-background-color: #0c0b0b; -fx-font-size: 15px; -fx-text-fill: white;");
				itemApercu.setStyle("-fx-background-color: #0c0b0b; -fx-font-size: 15px; -fx-text-fill: white;");
				mode.setStyle(" -fx-background-color: #0c0b0b;-fx-font-size: 15px; -fx-text-fill: white;");
				afficherDiff();

	}

	public void modeClair() {
		modeSombre = false;
		// Réinitialiser les styles et le contenu des éléments
		String htmlRef = "<div style='background-color: white; color: black;'>" + diff.getOldFormatedString() + "</div>";
		String htmlModif = "<div style='background-color: white; color: black;'>" + diff.getNewFormatedString() + "</div>";
		webEngineRef.loadContent(htmlRef);
		webEngineModif.loadContent(htmlModif);
		primaryStage.getScene().getRoot().setStyle("-fx-background-color: white; -fx-font-size: 20px; -fx-text-fill: black;");
		menuSauvegarder.setStyle("-fx-font-size: 15px; -fx-text-fill: black;");
		btnValider.setStyle("-fx-background-color: #d3d3d3; -fx-font-size: 20px; -fx-text-fill: black;");
		btnValider.setPrefSize(200, 100);
		btnRefuser.setStyle("-fx-background-color: #d3d3d3; -fx-font-size: 20px; -fx-text-fill: black;");
		btnRefuser.setPrefSize(200, 100);
		txtCommentaire.setStyle("-fx-background-color: white; -fx-font-size: 20px; -fx-text-fill: black;");
		menuBar.setStyle("-fx-background-color: white; -fx-font-size: 15px; -fx-text-fill: black;");
		itemResultat.setStyle("-fx-font-size: 15px; -fx-text-fill: black;");
		sauvegarde.setStyle("-fx-font-size: 15px; -fx-text-fill: black;");
		interfaceA.setStyle("-fx-font-size: 15px; -fx-text-fill: black;");
		itemUndo.setStyle(" -fx-font-size: 15px; -fx-text-fill: black;");
		itemRedo.setStyle(" -fx-font-size: 15px; -fx-text-fill: black;");
		itemEnregistrer.setStyle(" -fx-font-size: 15px; -fx-text-fill: black;");
		itemEtat.setStyle("-fx-font-size: 15px; -fx-text-fill: black;");
		resultat.setStyle("-fx-font-size: 15px; -fx-text-fill: black;");
		itemApercu.setStyle("-fx-font-size: 15px; -fx-text-fill: black;");
		mode.setStyle(" -fx-font-size: 15px; -fx-text-fill: black;");

		afficherDiff();
	}
	// Supposons que vous avez une méthode dans votre classe Controleur pour récupérer le chemin de la dernière sauvegarde



	public boolean estModeSombre() {
		return modeSombre;
	}
}

