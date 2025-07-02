# Jeu de la Vie (Game of Life)

![Game of Life](https://upload.wikimedia.org/wikipedia/commons/e/e5/Gospers_glider_gun.gif)

## Description
Le Jeu de la Vie (Game of Life) est un automate cellulaire imaginé par John Horton Conway en 1970. Malgré des règles très simples, il est Turing-complet. Ce n'est pas un jeu au sens traditionnel, mais plutôt une simulation mathématique qui montre comment des règles simples peuvent créer des comportements complexes.

Cette application est une implémentation interactive du Jeu de la Vie avec une interface graphique JavaFX.

## Règles du jeu
Le jeu se déroule sur une grille à deux dimensions, théoriquement infinie, dont les cases (cellules) peuvent prendre deux états : vivante ou morte.

À chaque étape, l'évolution d'une cellule est déterminée par l'état de ses huit voisines, selon les règles suivantes :

### 1. Naissance
Une cellule morte avec exactement 3 voisines vivantes devient vivante.

### 2. Survie
Une cellule vivante avec 2 ou 3 voisines vivantes reste vivante.

### 3. Mort
Dans tous les autres cas, une cellule meurt ou reste morte (par solitude ou surpopulation).

### Motifs célèbres
Le Jeu de la Vie est connu pour ses nombreux motifs fascinants :

#### Oscillateurs
Des structures qui reviennent à leur état initial après un certain nombre d'étapes.

![Clignotant](https://upload.wikimedia.org/wikipedia/commons/9/95/Game_of_life_blinker.gif)
![Pulsar](https://upload.wikimedia.org/wikipedia/commons/0/07/Game_of_life_pulsar.gif)

#### Vaisseaux
Des structures qui se déplacent à travers la grille.

![Planeur](https://upload.wikimedia.org/wikipedia/commons/f/f2/Game_of_life_animated_glider.gif)
![Vaisseau léger](https://upload.wikimedia.org/wikipedia/commons/3/37/Game_of_life_animated_LWSS.gif)

#### Canons
Des structures qui produisent des vaisseaux à l'infini.

![Canon à planeurs de Gosper](https://upload.wikimedia.org/wikipedia/commons/e/e0/Game_of_life_glider_gun.svg)

## Fonctionnalités
- Interface graphique intuitive
- Contrôle de la vitesse de simulation
- Zoom et déplacement dans la grille
- Création manuelle de motifs en cliquant sur les cellules
- Génération aléatoire de cellules
- Compteur de générations
- Grille toroïdale (les bords se rejoignent)

## Prérequis
- Java 22 ou supérieur
- Maven (pour la compilation)

## Installation

### À partir des sources
1. Clonez le dépôt :
   ```
   git clone https://github.com/votre-utilisateur/GameOfLife.git
   cd GameOfLife
   ```

2. Compilez le projet avec Maven :
   ```
   mvn clean package
   ```

3. Exécutez l'application :
   ```
   mvn javafx:run
   ```

## Utilisation
- **Démarrer/Arrêter** : Lance ou met en pause la simulation
- **Avancer d'un tour** : Fait évoluer la grille d'une génération
- **Effacer** : Vide la grille
- **Aléatoire** : Remplit la grille avec des cellules aléatoires
- **Zoom** : Ajuste la taille des cellules
- **Vitesse** : Contrôle la vitesse de la simulation
- **Clic sur une cellule** : Bascule l'état de la cellule (vivante/morte)

## Structure du projet
- `GameOfLifeApplication.java` : Point d'entrée de l'application JavaFX
- `GameOfLifeController.java` : Contrôleur pour l'interface utilisateur
- `GameOfLife.java` : Modèle contenant la logique du jeu
- `game-of-life-view.fxml` : Définition de l'interface utilisateur
- `styles.css` : Styles CSS pour l'interface

## Licence
Ce projet est sous licence MIT. Voir le fichier LICENSE pour plus de détails.

## Auteur
Illumination

Je suis le créateur de ce projet. Vous pouvez me retrouver sur LinkedIn: [Mon profil LinkedIn](https://www.linkedin.com/in/fabio-guerreiro-marques-16a442272/) ou visiter mon portfolio: [www.fabioguerreiromarques.fr](http://www.fabioguerreiromarques.fr)

## Remerciements
- John Conway pour avoir inventé ce fascinant automate cellulaire
- La communauté JavaFX pour les outils et ressources
