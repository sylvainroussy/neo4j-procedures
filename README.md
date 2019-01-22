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

- Les méthodes annotées [`@Procedure`](https://neo4j.com/docs/java-reference/current/javadocs/org/neo4j/procedure/Procedure.html) qui définissent les traitements effectués

- Les classes à propriétés publiques (généralement des classes internes à la classe qui contient les procédures) qui définissent les propriétés produites par une procédure et qui sont accessibles  grâce à l'instruction `YIELD`

### Injections contextuelles

Dans la classe principale, il est possible d'injecter trois types de données contextuelles :

- [`org.neo4j.graphdb.GraphDatabaseService`](https://neo4j.com/docs/java-reference/current/javadocs/org/neo4j/graphdb/GraphDatabaseService.html) : permet d'accéder au graphe Neo4j par le service interne qui lui est dédié

- [`org.neo4j.logging.Log`](https://neo4j.com/docs/java-reference/current/javadocs/org/neo4j/logging/Log.html) : permet d'accéder au service de journalisation général de Neo4j (neo4j.log)

- [`org.neo4j.procedure.TerminationGuard`](https://neo4j.com/docs/java-reference/current/javadocs/org/neo4j/procedure/TerminationGuard.html) : particulièrement utile sur une procédure dont le temps d'exécution est long, ce service va vérifier que la requête parente (celle qui a initié la procédure, ou encore la transaction) n'a pas été interrompue par l'utilisateur ou n'a pas dépassé le temps d'exécution maximal (timeout)    
 
Ces propriétés doivent-être injectées avec l'annotation [`@Context`](https://neo4j.com/docs/java-reference/current/javadocs/org/neo4j/procedure/Context.html), elles doivent-être déclarées publiques et ne doivent pas être ni statiques, ni finales.
 
```
 @Context
 public GraphDatabaseService db;
 
 @Context
 public Log log;
 
 @Context
 public TerminationGuard guard;
 
```

### Annotations @Procedure et @Description

Une procédure Cypher est donc une méthode Java annotée par `@Procedure`.

Cette annotation peut prendre jusqu'à cinq arguments :

- _value_ = nom complet de la procédure (avec le package)

- _name_ = identique à l'argument _value_

- deprecatedBy = nom d'une procédure de remplacement qui sera affichée dans le Warning de la console Web

- mode = spécifie le type d'opérations appliquées sur Neo4j : `Mode.READ` pour la lecture de données,`Mode.WRITE` pour l'écriture de données, `Mode.SCHEMA` pour la modification d'Index et de contraintes, `Mode.DBMS` pour les opérations système (utilisateurs, permissions, etc). Par défaut Mode.READ est activé.

- _eager_ =  par défaut, Cypher charge les données au dernier moment (_Lazily_) mais dans les cas de lecture/écriture au sein d'une même requête, ce comportement peut avoir des effets de bords. Positionner l'argument _eager_ à _true_ permet d'éviter ces dits effets.

L'annotation `@Description` va quant à elle permettre de documenter la procédure en question.

```
 @Procedure(value = "srosoft.findByLabel",mode=Mode.READ)
 @Description("return nodes by label")
 public Stream<NodesResult> findByLabel( @Name("label") String label) {
 	...
 }
 
```

### Ecrire la méthode d'une procédure

La méthode annotée par `@Procedure` doit respecter les règles suivantes :

- Etre publique

- Retourner un `Stream<T>` où T correspond à la classe portant les propriétés retournées par l'instruction `YIELD`

S'il faut lever une exception dans la procédure, elle doit-être de type `java.lang.RuntimeException` ou étendre celle-ci. 

``` 
public Stream<NodesResult> findByLabel( @Name("label") String label) {
    	
    	log.info("Calling procedure: srosoft.findByLabel with label: "+label);
    	final ResourceIterator<Node> ri = db.findNodes(Label.label (label));    	
    	return ri.stream().map(NodesResult::new);
    }
``` 

## Ecrire le test

## Déployer le plugin
