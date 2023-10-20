package application;

import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Pour la gestion des interfaces de jeu¸
 * 
 * 
 * @author Christian Jeune
 * @version 1.0
 */
public class Controller {
	private Stage stage;
	private Scene scene;
	private Group root;

	private final int rangee = 20;// le nombre de rangée
	private final int caseTaille = 35;// la taille des cases
	private double speed = 0.05;// l'unité selon laquelle la vitesse de l'animation augmente

	private List<Point> corpsPos = new ArrayList<>();// coordonnées de chaque partie du corps du serpent
	private List<Rectangle> corps = new ArrayList<>();// liste qui contient tous les rectangles du corps
	private Point tete;// coordonnées de la tête du serpent

	private Circle cercle = new Circle();// le cercle que le serpent doit chasser
	private boolean gameOver;// boolean indiquant si le joueur a perdu
	private Orientation direction = Orientation.RIGHT;// direction initial du serpent
	private int cercleX;// coordonnée X du cercle à chasser
	private int cercleY;// coordonnée Y du cercle à chasser
	private int score = 0;// integer contenant le score actuel du joueur

	// Image correspondant à la tête du serpent
	private Image teteSerpent = new Image("/tete.png");
	private ImageView vueTete = new ImageView(teteSerpent);

	private Text perdu = new Text();// Text affichant Game Over et le score à la fin de la partie
	private Text scoreText = new Text("Score " + score);// Text affichant le score durant la partie
	private Button playAgain = new Button("PLAY AGAIN");// Bouton incitant le joueur à réessayer
	private Timeline timeline;// la loop pour gérer les animations

	/**
	 * Créer l'interface de jeu et démarre la partie
	 * 
	 * 
	 * @param event
	 * @throws IOException
	 */
	public void playInitial(ActionEvent event) throws IOException {
		stage = (Stage) ((Node) event.getSource()).getScene().getWindow();// le stage correspond au stage de l'interface
																			// initial, la page d'accueil
		root = new Group();
		scene = new Scene(root, 700, 700, Color.BLACK);
		stage.setScene(scene);
		stage.centerOnScreen();
		stage.setResizable(false);
		stage.show();

		cercle.setFill(Color.RED);
		cercle.setRadius(caseTaille / 2 - 1);

		perdu.setFont(Font.font("Times New Roman", 30));
		perdu.setFill(Color.RED);
		perdu.setTextOrigin(VPos.CENTER);
		perdu.setX(8 * caseTaille);
		perdu.setY(8 * caseTaille);
		perdu.setVisible(false); // le Text est mis à invisible et s'affiche seulement quand le joueur perd

		scoreText.setFont(Font.font("Times New Roman", 30));
		scoreText.setFill(Color.WHITE);
		perdu.setTextOrigin(VPos.CENTER);
		scoreText.setX(8.5 * caseTaille);
		scoreText.setY(50);

		playAgain.setFont(Font.font("System", FontWeight.BOLD, 20));
		playAgain.setTextFill(Color.WHITE);
		playAgain.setBackground(new Background(new BackgroundFill(Color.LIMEGREEN, null, null)));
		playAgain.setLayoutX(8 * caseTaille);
		playAgain.setLayoutY(10 * caseTaille);
		playAgain.setVisible(false);// le bouton est mis à invisible et s'affiche seulement quand le joueur perd
		playAgain.setDisable(true);
		playAgain.setFocusTraversable(true);
		playAgain.setDefaultButton(true);// la touche entrée génère un évènement

		/*
		 * Lors d'un évènement sur le bouton playAgain on crée un nouveau controller
		 * nous permettant de créer une nouvelle interface de jeu
		 */
		playAgain.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				try {
					Controller newJeu = new Controller();
					newJeu.playInitial(event);
				} catch (IOException e) {
					e.printStackTrace();
				}

			}

		});

		root.getChildren().addAll(perdu, playAgain, scoreText); // ajout des composantes à la racine

		// initialisation des coordonnées des trois premières parties du corps du
		// serpent
		for (int i = 0; i < 3; i++) {
			corpsPos.add(new Point(5, rangee / 2));
		}
		tete = corpsPos.get(0);

		/*
		 * Selon les arrows keys et les keys WASD appuyés la direction change et la tête
		 * du serpent tourne
		 */
		scene.setOnKeyPressed(new EventHandler<KeyEvent>() {

			@Override
			public void handle(KeyEvent event) {
				KeyCode code = event.getCode();
				if (code == KeyCode.RIGHT || code == KeyCode.D) {
					if (direction != Orientation.LEFT) {
						direction = Orientation.RIGHT;
						vueTete.setRotate(270);
					}
				} else if (code == KeyCode.LEFT || code == KeyCode.A) {
					if (direction != Orientation.RIGHT) {
						direction = Orientation.LEFT;
						vueTete.setRotate(90);
					}
				} else if (code == KeyCode.UP || code == KeyCode.W) {
					if (direction != Orientation.DOWN) {
						direction = Orientation.UP;
						vueTete.setRotate(180);
					}
				} else if (code == KeyCode.DOWN || code == KeyCode.S) {
					if (direction != Orientation.UP) {
						direction = Orientation.DOWN;
						vueTete.setRotate(360);
					}
				}

			}

		});
		genererCercle();
		creerTete();

		// création et démarrage de la loop
		timeline = new Timeline(new KeyFrame(Duration.millis(130), e -> play()));
		timeline.setCycleCount(Animation.INDEFINITE);
		timeline.play();
	}

	/**
	 * Gère le déplacement du serpent sur l'interface
	 */
	public void play() {

		// si le joueur perd, rend toutes les composantes invisibles, visibles et arrête
		// la loop
		if (gameOver) {
			perdu.setVisible(gameOver);
			playAgain.setVisible(gameOver);
			playAgain.setDisable(false);
			timeline.stop();
			perdu.setText(String.format("Game Over!\n    Score " + score));
		}

		else {

			// met à jour les coordonnées du corps du serpent et déplace les rectangles
			// les coordonnées d'un noeud sont affectées aux coordonnées du noeud suivante
			for (int i = corpsPos.size() - 1; i >= 1; i--) {
				corpsPos.get(i).x = corpsPos.get(i - 1).x;
				corpsPos.get(i).y = corpsPos.get(i - 1).y;
				corps.get(i - 1).setX((int) corpsPos.get(i).getX() * caseTaille);
				corps.get(i - 1).setY((int) corpsPos.get(i).getY() * caseTaille);
				corps.get(i - 1).toBack();

			}

			// dépandemment de la direction met à jour les coordonnées de la tête et la
			// déplace
			switch (direction) {
			case RIGHT:
				if (direction != Orientation.LEFT) {
					direction = Orientation.RIGHT;
					tete.x++;
					vueTete.setX((int) tete.getX() * caseTaille);
					vueTete.setY((int) tete.getY() * caseTaille);
					vueTete.toBack();
				}
				break;
			case LEFT:
				if (direction != Orientation.RIGHT) {
					direction = Orientation.LEFT;
					tete.x--;
					vueTete.setX((int) tete.getX() * caseTaille);
					vueTete.setY((int) tete.getY() * caseTaille);
					vueTete.toBack();
				}
				break;
			case UP:
				if (direction != Orientation.DOWN) {
					direction = Orientation.UP;
					tete.y--;
					vueTete.setX((int) tete.getX() * caseTaille);
					vueTete.setY((int) tete.getY() * caseTaille);
					vueTete.toBack();
				}
				break;
			case DOWN:
				if (direction != Orientation.UP) {
					direction = Orientation.DOWN;
					tete.y++;
					vueTete.setX((int) tete.getX() * caseTaille);
					vueTete.setY((int) tete.getY() * caseTaille);
					vueTete.toBack();
				}
				break;
			}

			gameOver();
			chasserCercle();
		}
	}

	/**
	 * Choisi au hasard les coordonnées en X et en Y du cercle et l'ajoute à
	 * l'interface
	 */
	public void genererCercle() {
		start: while (true) {
			cercleX = (int) (Math.random() * rangee);
			cercleY = (int) (Math.random() * rangee);

			// vérifie si les coordonnées choisis ne correspondent pas à une
			// coordonnée du serpent
			for (Point snake : corpsPos) {
				if (snake.getX() == cercleX && snake.getY() == cercleY) {
					continue start;
				}
			}
			break;
		}
		cercle.setLayoutX(cercleX * caseTaille + caseTaille / 2);
		cercle.setLayoutY(cercleY * caseTaille + caseTaille / 2);
		root.getChildren().add(cercle);
	}

	/**
	 * Ajoute la tête du serpent et les premières parties de son corps à l'interface
	 * 
	 */
	public void creerTete() {
		vueTete.setPreserveRatio(false);
		vueTete.setFitWidth(caseTaille);
		vueTete.setFitHeight(caseTaille);
		vueTete.setRotate(270);

		vueTete.setX((int) tete.getX() * caseTaille);
		vueTete.setY((int) tete.getY() * caseTaille);
		vueTete.toBack();

		root.getChildren().add(vueTete);

		for (int i = 1; i < corpsPos.size(); i++) {
			creerRectangle();
		}

	}

	/**
	 * Crée un nouveau rectangle pour le serpent et l'ajoute à l'interface
	 */
	public void creerRectangle() {
		Rectangle rect = new Rectangle();
		rect.setFill(Color.LIMEGREEN);
		rect.setArcHeight(25);
		rect.setArcWidth(25);
		rect.setWidth(caseTaille - 3);
		rect.setHeight(caseTaille - 3);
		rect.setX((int) tete.getX() * caseTaille);
		rect.setY((int) tete.getY() * caseTaille);
		rect.toBack();
		root.getChildren().add(rect);
		corps.add(rect);
	}

	/**
	 * Vérifie si le joueur a perdu
	 */
	public void gameOver() {

		// si le serpent frappe une des bordures
		if (tete.x < 0 || tete.y < 0 || tete.x * caseTaille >= 700 || tete.y * caseTaille >= 700) {
			gameOver = true;
		}

		// si le serpent se tue lui-même
		for (int i = 1; i < corpsPos.size(); i++) {
			if (tete.x == corpsPos.get(i).getX() && tete.y == corpsPos.get(i).getY()) {
				gameOver = true;
				break;
			}
		}
	}

	/**
	 * Crée un nouveau rectangle pour le serpent, enlève le cercle de l'interface
	 * génère un autre cercle, met à jour le score et augmente la vitesse du
	 * serpent
	 */
	public void chasserCercle() {
		if (tete.getX() == cercleX && tete.getY() == cercleY) {
			corpsPos.add(new Point(-1, -1));
			creerRectangle();
			root.getChildren().remove(cercle);
			genererCercle();
			score++;
			scoreText.setText("Score " + score);
			timeline.setRate(1 + speed);
			speed += 0.05;
		}
	}

}
