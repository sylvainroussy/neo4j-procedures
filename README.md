Neo4j - Ecrire une procédure CYPHER en Java
===========================================

Les procédures Cypher dites "user-defined" permettent d'accéder au service GraphDb de Neo4j sans passer par un protocole de transport.
Elles peuvent s'avérer utiles dans les cas suivants :

- utiliser les apis natives de Neo4J
- rassembler des règles métier au sein d'un composant unique

Ecrire la procédure
-------------------

Pour rappel, l'appel d'une procédure s'effectue de la manière suivante :

``` CALL package.procedureName (args) YIELD var1, var2 RETURN var1 ```

- _CALL_ : marque l'appel d'une procédure

- _package.procedureName_ : le nom complet de la procédure à appeler

- _YIELD_ : produit pour résultat (le nom des variables de retour est fixé dans la procédure) 



Ecrire le test
--------------