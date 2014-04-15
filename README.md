Pré-requis
------------

Vous avez besoin :
* De java
* D'un éditeur de texte (nous recommandons sublime-text)
* D'un serveur monde (lors d'un handson les organisateurs font tourner le serveur et vous donneront l'adresse, chez vous il faut le lancer en local, voir plus bas)

Lancer le Hands-on
------------
Connectez-vous sur l'adresse du serveur et demandez-lui de vous assigner un aéroport.
Modifiez le fichier `client/build.sbt` pour renseigner le nom de votre aéroport et, si besoin, changer le noeud source
Vous pouvez ensuite lancer le hands-on en tapant
* ```./handson``` sous linux/mac
* ```handson.bat``` sous windows

Ces scripts lancent SBT (scala build tool) en arrière plan pré-configuré avec des commandes pour jouer le hands-on.

Commandes
------------
* `start` lance votre aéroport, ce dernier essaye de se connecter au noeud source pour trouver le serveur monde, une fois
connecté vous pouvez interagir avec la session de votre aéroport sur l'ihm web du serveur monde.
* `test` joue les tests de votre aéroport pour vérifier que vous avez correctement implémenté la logique.

Lancer son propre serveur monde
------------
Ouvrez un second terminal et allez dans le sous dossier `server`. Il s'agit d'une application Play2. Toutes les commandes habituelles de play sont disponible.
Pour démarrer le serveur faites `sbt run`.

Protocole de communication avec les avions
------------

Le protocole de communication complet est décrit dans le fichier SEQUENCE.md