package mines;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.security.SecureRandom;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * La classe Board représente le plateau de jeu pour le jeu de démineur.
 */
public class Board extends JPanel {
	private static final long serialVersionUID = 6195235521361212179L;

	private final int NUM_IMAGES = 13;
	private final int CELL_SIZE = 15;

	private final int COVER_FOR_CELL = 10;
	private final int MARK_FOR_CELL = 10;
	private final int EMPTY_CELL = 0;
	private final int MINE_CELL = 9;
	private final int COVERED_MINE_CELL = MINE_CELL + COVER_FOR_CELL;
	private final int MARKED_MINE_CELL = COVERED_MINE_CELL + MARK_FOR_CELL;

	private final int ROWS = 16;
	private final int COLUMNS = 16;
	private final int ALL_CELLS = ROWS * COLUMNS;

	private final int DRAW_MINE = 9;
	private final int DRAW_COVER = 10;
	private final int DRAW_MARK = 11;
	private final int DRAW_WRONG_MARK = 12;
	
	private final SecureRandom random = new SecureRandom(); 

	private int[] field;
	private boolean inGame;
	private int minesLeft;
	private Image[] images;
	private int mines = 40;
	private JLabel statusbar;


	/**
	 * Constructeur de la classe Board.
	 * Initialise le plateau de jeu et charge les images nécessaires.
	 * @param statusbar une étiquette pour afficher le statut du jeu
	 * @return none
	 */
	public Board(JLabel statusbar) {

		// Initialise l'étiquette de statut du jeu
		this.statusbar = statusbar;

		// Charge les images nécessaires pour le jeu
		images = new Image[NUM_IMAGES];
		for (int i = 0; i < NUM_IMAGES; i++) {
			images[i] = (new ImageIcon(getClass().getClassLoader().getResource((i) + ".gif"))).getImage();
		}

		// Active le double buffering pour améliorer les performances graphiques
		setDoubleBuffered(true);

		// Ajoute un écouteur de souris pour gérer les clics de l'utilisateur
		addMouseListener(new MinesAdapter());

		// Démarre une nouvelle partie
		newGame();
	}



	/**
	 * Initialise une nouvelle partie de démineur.
	 * Génère un nouveau plateau de jeu avec des mines aléatoires.
	 * @param none
	 * @return none
	 */
	public void newGame() {

		// Initialise les variables nécessaires pour la nouvelle partie
		int currentColumn;
		int i = 0;
		int position = 0;
		int cell = 0;
		inGame = true;
		minesLeft = mines;
		field = new int[ALL_CELLS];

		// Initialise chaque cellule du plateau de jeu avec une case cachée
		for (i = 0; i < ALL_CELLS; i++)
			field[i] = COVER_FOR_CELL;

		// Affiche le nombre de mines restantes dans l'étiquette de statut
		statusbar.setText(Integer.toString(minesLeft));

		// Place les mines aléatoirement sur le plateau de jeu
		i = 0;
		while (i < mines) {

			// Génère une position aléatoire pour la mine
			position = (int) (ALL_CELLS * random.nextDouble());

			// Vérifie que la position est valide et qu'il n'y a pas déjà une mine à cet endroit
			if ((position < ALL_CELLS) && (field[position] != COVERED_MINE_CELL)) {

				// Calcule la colonne actuelle de la position
				currentColumn = position % COLUMNS;

				// Place une mine à la position choisie
				field[position] = COVERED_MINE_CELL;
				i++;

				// Incrémente le nombre de mines adjacentes pour chaque cellule voisine
				if (currentColumn > 0) { 
					cell = position - 1 - COLUMNS;
					coverMineCell(cell);
					cell = position - 1;
					if (cell >= 0)
						coverMineCell(cell);

					cell = position + COLUMNS - 1;
					if (cell < ALL_CELLS)
						coverMineCell(cell);

				}

				cell = position - COLUMNS;
				coverMineCell(cell);

				cell = position + COLUMNS;
				incrementAdjacentMines(cell);

				if (currentColumn < (COLUMNS - 1)) {
					cell = position - COLUMNS + 1;
					coverMineCell(cell);

					cell = position + COLUMNS + 1;
					incrementAdjacentMines(cell);
					cell = position + 1;
					incrementAdjacentMines(cell);
				}
			}
		}
	}

	private void incrementAdjacentMines(int cell) {
		if (cell < ALL_CELLS && field[cell] != COVERED_MINE_CELL)

			field[cell] += 1;
	}

	private void coverMineCell(int cell) {
		if ((cell >= 0 && field[cell] != COVERED_MINE_CELL)  )
			field[cell] += 1;

	}


	/**
	 * Recherche toutes les cellules vides adjacentes à la cellule donnée et les révèle.
	 * Utilise la récursivité pour parcourir toutes les cellules vides adjacentes.
	 * @param j l'indice de la cellule à partir de laquelle la recherche doit commencer
	 * @return none
	 */
	public void findEmptyCells(int j) {
		int currentColumn = j % COLUMNS;
		int cell;

		// Parcourt toutes les cellules adjacentes à la cellule donnée
		if (currentColumn > 0) { 
			cell = j - COLUMNS - 1;
			iterateEmptyCell(cell);

			cell = j - 1;
			iterateEmptyCell(cell);

			cell = j + COLUMNS - 1;
			iterateCell(cell);
		}

		cell = j - COLUMNS;
		iterateEmptyCell(cell);

		cell = j + COLUMNS;
		iterateCell(cell);

		if (currentColumn < (COLUMNS - 1)) {
			cell = j - COLUMNS + 1;
			iterateEmptyCell(cell);

			cell = j + COLUMNS + 1;
			iterateCell(cell);

			cell = j + 1;
			iterateCell(cell);
		}
	}

	private void iterateEmptyCell(int cell) {
		if (cell >= 0 && field[cell] > MINE_CELL) {
			uncoverCell(cell);
			if (field[cell] == EMPTY_CELL) {
				findEmptyCells(cell);
			}
		}
	}

	private void iterateCell(int cell) {
		if (cell < ALL_CELLS && field[cell] > MINE_CELL) {
			uncoverCell(cell);
			if (field[cell] == EMPTY_CELL) {
				findEmptyCells(cell);
			}
		}
	}

	private void uncoverCell(int cell) {
		field[cell] -= COVER_FOR_CELL;
	}


	/**
	 * Dessine le champ de jeu en utilisant les images appropriées pour chaque cellule.
	 * @param g l'objet Graphics utilisé pour dessiner le champ de jeu
	 * @return none
	 */
	public void paint(Graphics g) {
    int cell;
    int uncover = 0;

    for (int i = 0; i < ROWS; i++) {
        for (int j = 0; j < COLUMNS; j++) {
            cell = field[(i * COLUMNS) + j];

            if (inGame && cell == MINE_CELL) {
                inGame = false;
            }

            cell = determineCellImage(cell);

            if (inGame && cell == DRAW_COVER) {
                uncover++;
            }

            drawCellImage(g, cell, i, j);
        }
    }

    updateStatusBar(uncover);
}

private int determineCellImage(int cell) {
    if (!inGame) {
        if (cell == COVERED_MINE_CELL) {
            return DRAW_MINE;
        } else if (cell == MARKED_MINE_CELL) {
            return DRAW_MARK;
        } else if (cell > COVERED_MINE_CELL) {
            return DRAW_WRONG_MARK;
        } else if (cell > MINE_CELL) {
            return DRAW_COVER;
        }
    } else {
        if (cell > COVERED_MINE_CELL) {
            return DRAW_MARK;
        } else if (cell > MINE_CELL) {
            return DRAW_COVER;
        }
    }
    return cell;
}

private void drawCellImage(Graphics g, int cell, int row, int column) {
    g.drawImage(images[cell], (column * CELL_SIZE), (row * CELL_SIZE), this);
}

	// Vérifie si le jeu est terminé et met à jour la barre de statut en conséquence
	private void updateStatusBar(int uncover) {

		if (uncover == 0 && inGame) {
			inGame = false;
			statusbar.setText("Game won");
		} else if (!inGame)
			statusbar.setText("Game lost");
	}



	/**
	 * Classe qui gère les événements de souris pour le jeu de démineur.
	 */
	class MinesAdapter extends MouseAdapter {

		/**
		 * Gère l'événement de clic de souris.
		 * @param e l'objet MouseEvent qui contient les informations sur l'événement de clic de souris
		 * @return none
		 */
		public void mousePressed(MouseEvent e) {

			// Récupère les coordonnées de la souris
			int x = e.getX();
			int y = e.getY();

			// Calcule la colonne et la rangée de la cellule cliquée
			int cellRow = x / CELL_SIZE;
			int cellColumn = y / CELL_SIZE;

			// Variable pour indiquer si la méthode doit redessiner le champ de jeu
			boolean mustRepaint = false;

			// Si le jeu est terminé, commence une nouvelle partie et redessine le champ de jeu
			if (!inGame) {
				newGame();
				repaint();
			}

			// Vérifie si le clic de souris est dans les limites du champ de jeu
			if ((x < COLUMNS * CELL_SIZE) && (y < ROWS * CELL_SIZE)) {

				// Si le clic est avec le bouton droit de la souris
				if (e.getButton() == MouseEvent.BUTTON3) {

					// Vérifie si la cellule cliquée est marquée
					if (field[(cellColumn * COLUMNS) + cellRow] > MINE_CELL) {
						mustRepaint = true;

						// Si la cellule est couverte, ajoute un marqueur de mine
						if (field[(cellColumn * COLUMNS) + cellRow] <= COVERED_MINE_CELL) {
							if (minesLeft > 0) {
								field[(cellColumn * COLUMNS) + cellRow] += MARK_FOR_CELL;
								minesLeft--;
								statusbar.setText(Integer.toString(minesLeft));
							} else
								statusbar.setText("No marks left");
						} else {

							// Si la cellule est déjà marquée, enlève le marqueur de mine
							field[(cellColumn * COLUMNS) + cellRow] -= MARK_FOR_CELL;
							minesLeft++;
							statusbar.setText(Integer.toString(minesLeft));
						}
					}

					// Si le clic est avec le bouton gauche de la souris
				} else {

					// Vérifie si la cellule cliquée est couverte ou marquée
					if (field[(cellColumn * COLUMNS) + cellRow] > COVERED_MINE_CELL) {
						return;
					}

					// Si la cellule cliquée contient une mine, le jeu est terminé
					if ((field[(cellColumn * COLUMNS) + cellRow] > MINE_CELL) &&
							(field[(cellColumn * COLUMNS) + cellRow] < MARKED_MINE_CELL)) {

						field[(cellColumn * COLUMNS) + cellRow] -= COVER_FOR_CELL;
						mustRepaint = true;

						if (field[(cellColumn * COLUMNS) + cellRow] == MINE_CELL)
							inGame = false;
						if (field[(cellColumn * COLUMNS) + cellRow] == EMPTY_CELL)
							findEmptyCells((cellColumn * COLUMNS) + cellRow);
					}
				}

				// Si la méthode doit redessiner le champ de jeu, appelle la méthode repaint()
				if (mustRepaint)
					repaint();

			}
		}
	}

}
