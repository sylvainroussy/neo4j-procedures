# Neo4j - Ecrire une procédure CYPHER en Java


Les procédures Cypher dites "user-defined" permettent d'accéder au service GraphDb de Neo4j sans passer par un protocole de transport.
Elles peuvent s'avérer utiles dans les cas suivants :

- utiliser les apis natives de Neo4j
- rassembler des règles métier au sein d'un composant unique

Ces procédures se déploient sous forme d'une archive Java (.jar) à déposer dans le dossier _plugins_ de Neo4j. Pour que ce nouveau plugin soit effectif, il est nécessaire de redémarrer le serveur Neo4j.

## Rappel sur les procédures


Pour rappel, l'appel d'une procédure s'effectue de la manière suivante :

``` CALL package.procedureName (args) YIELD var1, var2 RETURN var1 ```

- _CALL_ : marque l'appel d'une procédure

- _package.procedureName_ : le nom complet de la procédure à appeler

- _YIELD_ : produit pour résultat (le nom des variables de retour est fixé dans la procédure)

## Ecrire la procédure
 

Considérons trois éléments :

- La classe qui contient les procédures

- Les méthodes annotées `@Procedure` qui définissent les traitements effectués

- Les classes à propriétés publiques (généralement des classes internes à la classe qui contient les procédures) qui définissent les propriétés produites par une procédure et qui sont accessibles  grâce à l'instruction `YIELD`

### Injections contextuelles

Dans la classe principale, il est possible d'injecter trois types de données contextuelles :

- [`org.neo4j.graphdb.GraphDatabaseService`](https://neo4j.com/docs/java-reference/current/javadocs/org/neo4j/graphdb/GraphDatabaseService.html) : permet d'accéder au graphe Neo4j par le service interne qui lui est dédié

- `org.neo4j.logging.Log` : permet d'accéder au service de journalisation général de Neo4j (neo4j.log)

- `org.neo4j.procedure.TerminationGuard` : particulièrement utile sur une procédure dont le temps d'exécution est long, ce service va vérifier que la requête parente (celle qui a initié la procédure) n'a pas été interropue par l'utilisateur ou n'a pas dépassé le temps d'exécution maximale (timeout)    
 

 
```
 @Context
 public GraphDatabaseService db;
 
 @Context
 public Log log;
 
 @Context
 public TerminationGuard guard;
 
```

Ecrire le test
--------------