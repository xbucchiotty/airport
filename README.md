Pré-requis
------------

Vous avez besoin :
* De java
* D'un éditeur de texte (nous recommandons sublime-text)
* D'un serveur monde

Lancer son propre serveur monde
------------
Ouvrez un terminal et allez dans le sous dossier `server`. Il s'agit d'une application Play2. Toutes les commandes habituelles de play sont disponible.
Pour démarrer le serveur faites `sbt run`.

Lancer le Hands-on
------------
Connectez-vous sur http://localhost:9000 et demandez-lui de vous assigner un aéroport.
Modifiez le fichier `client/build.sbt` pour renseigner le nom de votre aéroport. (airport := "LHR" par exemple)
Vous pouvez ensuite lancer le hands-on en tapant
* ```./handson``` sous linux/mac
* ```handson.bat``` sous windows

Ces scripts lancent SBT (scala build tool) en arrière plan pré-configuré avec des commandes pour jouer le hands-on.

Commandes
------------
* `start` lance votre aéroport, ce dernier essaye de se connecter au noeud source pour trouver le serveur monde, une fois
connecté vous pouvez interagir avec la session de votre aéroport sur l'ihm web du serveur monde.
* `test` joue les tests de votre aéroport pour vérifier que vous avez correctement implémenté la logique.



Protocole de communication avec les avions
------------

Le protocole de communication complet est décrit dans le fichier SEQUENCE.md