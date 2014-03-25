                                                        =============
                                                        || shinFPS ||
                                                        =============

Cours de Licence 3 openGL
=> Scala
=> JMonkeyEngine
=> Sujet :
    FPS ou Doom-like
    ----------------

   	Descriptif général
   		FPS pour First Person Shooter (généralement traduit par jeu de tir subjectif – relativement à la vue) est un
   		type de jeu vidéo dans lequel le joueur est immergé dans un environnement 3D virtuel où il incarne un personnage
   		devant éliminer ses adversaires ; à cet effet il a, à sa disposition, une ou plusieurs armes (blanches ou à feu).
   		Le terme Doom-like est souvent utilisé pour qualifier ce type de jeux et fait référence aux premiers succès du
   		genre : Wolfenstein 3D, Doom et Quake.

   	Fonctionnalités attendues
   		a. Pouvoir se déplacer (donc détection de collisions) dans un environnement 3D de type labyrinthe avec sas et
   		zones à accès restreint ; l’utilisateur pourra déverrouiller l’accès via mot de passe / énigmes / clés.
   		L’ensemble de l’environnement est texturé, bumpé et éclairé. L’environnement (= un niveau) pourrait comporter
   		des étages (escaliers et/ou ascenseurs) ;

   		b. Gérer plusieurs classes d’ennemis ayant chacune ses aptitudes et stratégies de combat ;

   		c. Réaliser un éditeur et/ou un générateur de niveaux.

=> Contrainte
    - éditeur
    - texturé, bumpé et éclairé
    - labyrinthe avec sas /zones à accès restreint/déverrouiller l’accès
    - texturé, bumpé et éclairé
    - se déplacer
    - détection de collisions
    - Créer un bestiaire de deux monstres
    - Plusieurs classes d’ennemis (aptitudes, stratégies)

=> Projet : Réalisation se basant sur DungeonKeeper2 permettant d'englober les différents critères.
            Je ne développe que le minimum pour réduire le temps de developpement au minimum



Objectif de 1ère étape :
------------------------
    => Création de la dalle.
        - Je vais essayer de la créer par un ensemble de cube positionnés les uns à côté des autres.
    => Gestion d'un curseur permettant de mettre en surbrillance une dalle pour la sélectionner
        - Je prépare le terrain pour pouvoir y supprimer des éléments
    => Positionner la camera dans une vue de dessus


