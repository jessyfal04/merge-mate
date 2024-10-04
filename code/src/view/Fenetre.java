package view;

import controler.Controleur;
import javafx.application.Application;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.web.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

import model.Diff;

public class Fenetre extends Application {
	private Controleur controleur;
	private Diff diff = new Diff();

	private Stage primaryStage;
	private Scene sceneChoix;
	private Scene sceneModifs;

	@Override
	public void start(Stage primaryStage) throws Exception {
		//Page d'accueil de l'application
		this.primaryStage = primaryStage;

		primaryStage.setTitle("MergeMate");
		primaryStage.setWidth(800);
		primaryStage.setHeight(600);

		Label titre = new Label("MergeMate");
		HBox hboxTitre = new HBox(titre);
		titre.setStyle("-fx-font-size: 50px; -fx-font-weight: bold; -fx-text-fill: black;");
		titre.setTranslateX(250);
		titre.setTranslateY(10);
		hboxTitre.setStyle("-fx-background-color: #abd5ec;");
		hboxTitre = new HBox(titre);

		// Choix
		Label labelBtnRef = new Label("Fichier de référence : ");
		labelBtnRef.setStyle("-fx-font-size: 20px; -fx-text-fill: black;");
		Button btnRef = new Button("Sélectionner un fichier");
		btnRef.setStyle("-fx-background-color: #24abf5;-fx-font-size: 15px;  -fx-text-fill: black;");
		btnRef.setOnAction(e -> controleur.choisir(Controleur.TypeFichier.Ref, btnRef));
		HBox hboxRef = new HBox(labelBtnRef, btnRef);
		hboxRef.setTranslateY(30);

		Label labelBtnModif = new Label("Fichier modifié : ");
		labelBtnModif.setStyle("-fx-font-size: 20px;  -fx-text-fill: black;");
		Button btnModif = new Button("Sélectionner un fichier");
		btnModif.setStyle("-fx-background-color: #24abf5; -fx-font-size: 15px; -fx-text-fill: black;");
		btnModif.setOnAction(e -> controleur.choisir(Controleur.TypeFichier.Modif, btnModif));
		HBox hboxModif = new HBox(labelBtnModif, btnModif);
		hboxModif.setStyle("-fx-background-color: #abd5ec;");
		hboxModif.setTranslateY(50);

		Label labelBtnImportEtat = new Label("Importer un état : ");
		labelBtnImportEtat.setStyle("-fx-font-size: 20px; -fx-text-fill: black;");
		Button btnImportEtat = new Button("Sélectionner un fichier");
		btnImportEtat.setStyle("-fx-background-color: #24abf5; -fx-font-size: 15px; -fx-text-fill: black;");
		btnImportEtat.setOnAction(e -> controleur.choisir(Controleur.TypeFichier.ImportEtat, btnImportEtat));
		HBox hboxImportEtat = new HBox(labelBtnImportEtat, btnImportEtat);
		hboxImportEtat.setTranslateY(60);

		Button btnPoursuivre = new Button("Poursuivre");
		btnPoursuivre.setText("Poursuivre");
		btnPoursuivre.setStyle("-fx-background-color: #195c80; -fx-font-size: 15px; -fx-text-fill: white;");
		btnPoursuivre.setTranslateY(70);

		VBox vboxChoix = new VBox( hboxTitre, hboxRef, hboxModif, hboxImportEtat, btnPoursuivre);
		vboxChoix.setStyle("-fx-background-color: #abd5ec;");

		//Page d'édition de l'app
		// Fichiers textes
		Label labelTxtRef = new Label("Texte de référence : ");
		labelTxtRef.setStyle("-fx-font-size: 20px;");
		WebView webViewRef = new WebView();
		WebEngine webEngineRef = webViewRef.getEngine();
		webEngineRef.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue == Worker.State.SUCCEEDED) { // Quand la page est chargée
				JSObject window = (JSObject) webEngineRef.executeScript("window"); // Récupérer la fenêtre
				window.setMember("controleur", controleur); // Ajouter le controleur à la fenêtre

				if (diff.modifInOld(controleur.getModifActuelle())) { // Si la modification est dans le texte de référence
					webEngineRef.executeScript("document.getElementById('"+controleur.getModifActuelle()+"').scrollIntoView();"); // Scroller jusqu'à la modification
				}
			}
		});
		VBox vboxTxtRef = new VBox(labelTxtRef, webViewRef);
		vboxTxtRef.setPrefWidth(400);

		Label labelTxtModif = new Label("Texte modifié : ");
		labelTxtModif.setStyle("-fx-font-size: 20px;");
		WebView webViewModif = new WebView();
		WebEngine webEngineModif = webViewModif.getEngine();
		webViewModif.setPrefWidth(400);
		webEngineModif.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue == Worker.State.SUCCEEDED) {
				JSObject window = (JSObject) webEngineModif.executeScript("window");
				window.setMember("controleur", controleur);

				if (diff.modifInNew(controleur.getModifActuelle())) {
					webEngineModif.executeScript("document.getElementById('"+controleur.getModifActuelle()+"').scrollIntoView();");
				}
			}
		});
		VBox vboxTxtModif = new VBox(labelTxtModif, webViewModif);
		vboxTxtModif.setPrefWidth(400);

		HBox hboxTxt = new HBox(vboxTxtRef, vboxTxtModif);

		//champs de texte pour les commentaires 
		TextField txtCommentaire = new TextField();
		txtCommentaire.setPromptText("Ecrivez un commentaire...");
		txtCommentaire.setPrefWidth(400);
		txtCommentaire.setPrefHeight(100);
		txtCommentaire.setStyle("-fx-font-size: 20px;");
		txtCommentaire.setEditable(true);
		txtCommentaire.setDisable(true);
		// when leave focus, commenter
		txtCommentaire.focusedProperty().addListener((obs, oldVal, newVal) -> {
			if (!newVal) {
				controleur.commenter();
			}
		});

		VBox vboxCommentaire = new VBox(txtCommentaire);

		// Boutons
		Button btnValider = new Button("Valider");
		btnValider.setStyle("-fx-background-color: #d3d3d3; -fx-font-size: 20px; -fx-text-fill: black;");
		btnValider.setPrefSize(200, 100);
		btnValider.setOnAction(e -> controleur.valider());
		btnValider.setDisable(true);

		Button btnRefuser = new Button("Refuser");
		btnRefuser.setStyle("-fx-background-color: #d3d3d3; -fx-font-size: 20px; -fx-text-fill: black;");
		btnRefuser.setPrefSize(200, 100);
		btnRefuser.setOnAction(e -> controleur.refuser());
		btnRefuser.setDisable(true);

		// Menus
		Menu menuSauvegarder = new Menu("Sauvegarder");
		menuSauvegarder.setStyle("-fx-font-size: 15px; -fx-text-fill: black;");
		MenuItem itemEnregistrer = new MenuItem("Rapide (indéfinie)");
		itemEnregistrer.setStyle("-fx-font-size: 15px; -fx-text-fill: black;");
		itemEnregistrer.setOnAction(e -> controleur.sauvegarde());

		Menu resultat = new Menu("Résultat");
		MenuItem itemApercue = new MenuItem("Aperçu du résultat");
		itemApercue.setStyle("-fx-font-size: 15px; -fx-text-fill: black;");
		itemApercue.setOnAction(e -> afficherContenuFichierRes());
		MenuItem itemResultat = new MenuItem("Résultat");
		itemResultat.setStyle("-fx-font-size: 15px; -fx-text-fill: black;");
		itemResultat.setOnAction(e -> controleur.resultat(false));
		resultat.getItems().addAll(itemApercue, itemResultat);

		MenuItem itemEtat = new MenuItem("État");
		itemEtat.setStyle("-fx-font-size: 15px; -fx-text-fill: black;");
		itemEtat.setOnAction(e -> controleur.etat(false));

		menuSauvegarder.getItems().addAll(itemEnregistrer, itemEtat, resultat);
		HBox hboxSauvegarder =  new HBox(btnValider, btnRefuser);

		Menu menuHistorique = new Menu("Historique");
		MenuItem itemUndo = new MenuItem("Annuler");
		MenuItem itemRedo = new MenuItem("Rétablir");
		itemUndo.setOnAction(e -> controleur.undo());
		itemRedo.setOnAction(e -> controleur.redo());
		menuHistorique.getItems().addAll(itemUndo, itemRedo);
		menuHistorique.setStyle("-fx-font-size: 15px; -fx-text-fill: black;");

		Menu interfaceA = new Menu("Interface");
		MenuItem mode = new MenuItem("Mode sombre");
		mode.setOnAction(e -> controleur.AfficheModeSombre(mode));
		interfaceA.getItems().add(mode);
		interfaceA.setStyle("-fx-font-size: 15px; -fx-text-fill: black;");


		MenuBar menuBar = new MenuBar();
		menuBar.getMenus().addAll(menuSauvegarder, menuHistorique, interfaceA);

		HBox hboxBtn = new HBox(hboxSauvegarder, vboxCommentaire);
		VBox vboxTxt = new VBox(menuBar, hboxTxt, hboxBtn);

		// Poursuivre Action :
		sceneChoix = new Scene(vboxChoix);
		sceneModifs = new Scene(vboxTxt);
		btnPoursuivre.setOnAction(e -> controleur.poursuivre(btnPoursuivre));
		controleur = new Controleur(this, primaryStage, diff, webEngineRef, webEngineModif, btnValider, btnRefuser, txtCommentaire, itemResultat, itemUndo, itemRedo, itemEnregistrer, mode, menuBar, menuSauvegarder, interfaceA, itemEtat, menuSauvegarder, resultat, itemApercue);

		// Input
		sceneModifs.setOnKeyPressed(e -> { // Detecter les touches
			// Detecter CTRL Z
			if (e.isControlDown() && e.getCode().toString().equals("Z")) {
				controleur.undo();
			}

			// Detecter CTRL Y
			if (e.isControlDown() && e.getCode().toString().equals("Y")) {
				controleur.redo();
			}

			// Detecter CTRL S
			if (e.isControlDown() && e.getCode().toString().equals("S")) {
				controleur.sauvegarde();
			}

			if (e.isControlDown()) {
				controleur.toggleCtrl(true);
			}
		});

		sceneModifs.setOnKeyReleased(e -> {
			if (!e.isControlDown()) {
				controleur.toggleCtrl(false);
			}
		});

		// Scene
		afficherScene(0);	
		primaryStage.show();
	}

	public void afficherScene(int n) { // Afficher la scène
		if (n == 0) {
			primaryStage.setScene(sceneChoix);
		} else {
			primaryStage.setScene(sceneModifs);
		}
	}

	public void afficherModaleContreEdit(String txtString, boolean oldOuNew, boolean estCoontreEdit) { // Afficher la modale de contre-édition
		Stage modalStage = new Stage();
		modalStage.initModality(Modality.APPLICATION_MODAL);
		modalStage.setTitle("Contre-Édition");

		TextArea txt = new TextArea();
		txt.setWrapText(true);
		txt.setPrefSize(400, 150);
		txt.setText(txtString);

		Button supprimerButton = new Button("Dé-Contre-Éditer");
		supprimerButton.setPrefSize(200, 50);
		supprimerButton.setOnAction(e -> {
			controleur.supprimerEdition(oldOuNew);
			modalStage.close();
		});

		supprimerButton.setDisable(!estCoontreEdit);

		Button contreEditionButton = new Button("Contre-Éditer");
		contreEditionButton.setPrefSize(200, 50);
		contreEditionButton.setOnAction(e -> {
			controleur.contreEditier(oldOuNew, txt.getText());
			modalStage.close();
		});

		if(controleur.estModeSombre() == false) {
			contreEditionButton.setStyle("-fx-background-color: #badc58; -fx-font-size: 20px; -fx-text-fill: black;");
			supprimerButton.setStyle("-fx-background-color: #ff7979; -fx-font-size: 20px; -fx-text-fill: black;");
		}else{
			contreEditionButton.setStyle("-fx-background-color: #366136; -fx-font-size: 20px; -fx-text-fill: white;");
			supprimerButton.setStyle("-fx-background-color: #9d0000; 	-fx-font-size: 20px; -fx-text-fill: white;");
		}

		HBox hboxButton = new HBox(supprimerButton, contreEditionButton);
		VBox vbox = new VBox(txt, hboxButton);

		Scene modalScene = new Scene(vbox);

		// Input ctrl
		modalScene.setOnKeyPressed(e -> {
			if (e.isControlDown()) {
				controleur.toggleCtrl(true);
			}
		});

		modalScene.setOnKeyReleased(e -> {
			if (!e.isControlDown()) {
				controleur.toggleCtrl(false);
			}
		});

		modalStage.setScene(modalScene);
		modalStage.showAndWait();
	}



	public void afficherContenuFichierRes() { // Afficher le contenu du fichier résultat
		Stage modalStage = new Stage();
		modalStage.initModality(Modality.APPLICATION_MODAL);
		modalStage.setTitle("Résultat");

		WebView webView = new WebView();
		webView.setPrefSize(600, 400);
		WebEngine webEngine = webView.getEngine();

		String contenu = controleur.getContenuFichierRes();
		contenu = contenu.replace("\n", "<br>");
		String htmlContent = "<html><body>" + contenu + (controleur.estModeSombre() ? "<style>body {background:#2c3e50; color:white} </style>" : "") + "</body></html>";
		webEngine.loadContent(htmlContent);

		VBox vbox = new VBox(webView);

		Scene modalScene = new Scene(vbox);

		modalStage.setScene(modalScene);
		modalStage.showAndWait();
	}


	public static void main(String[] args) {
		Application.launch(args);
	}


}
