Pré-requis
------------

Vous avez besoin :
* De java
* D'un éditeur de texte (nous recommandons sublime-text)
* D'un serveur monde

Démarrer avec SBT
------------
Pour lancer le serveur monde ou votre client du jeu, nous utilisons SBT (Simple Build Tool). 
Le script ```handson```que vous trouvez à la racine du projet est un sbt préconfiguré avec les dépendances.

Lancez la commande dans deux terminals différents.

Intégration avec votre IDE
------------
Si vous possédez un IDE (Eclipse ou IntelliJ), vous pouvez importer les modules. Pour cela dans une session SBT, lancez:
* gen-idea pour IntelliJ
* eclipse pour Eclipse

Pour démarrer le serveur monde
------------
Dans une des sessions SBT ouverte, dans la console interactive, ```start-server```. Il s'agit d'une application Play2. 
Les logs du server monde défileront dans cette fenêtre. Vous pouvez oublier ce terminal pour le jeu.

Lancer le Hands-on
------------
Connectez-vous sur http://localhost:9000 et demandez-lui de vous assigner un aéroport.
Dans la seconde session SBT, tapez ```set airport in client := "XXX" ``` où XXX est le code de votre aéroport donné par le server.
Vous pouvez ensuite lancer le hands-on en tapant,  ```start-client```.
Vous pouvez à tout moment stopper le client avec ```CTRL+D```.

Cela vous permet par exemple de lancer les tests unitaires avec la commande ```test```.

Protocole de communication avec les avions
------------

Le protocole de communication complet est décrit dans le fichier SEQUENCE.md
