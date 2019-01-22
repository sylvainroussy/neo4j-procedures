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

- Retourner un `java.util.stream.Stream<T>` où T correspond à la classe portant les propriétés retournées par l'instruction `YIELD`

- Chaque paramètre d'entrée de la méthode doit-être prefixé de l'annotation [`@Name`](https://neo4j.com/docs/java-reference/current/javadocs/org/neo4j/procedure/Name.html) 

S'il faut lever une exception dans la procédure, elle doit-être de type `java.lang.RuntimeException` ou étendre celle-ci. 

``` 
 public Stream<YieldClass> findByLabel( @Name("label") String label) {
    	
    	log.info("Calling procedure: srosoft.findByLabel with label: "+label);
    	final ResourceIterator<Node> ri = db.findNodes(Label.label (label));    	
    	return ri.stream().map(YieldClass::new);
}
``` 

### Ecrire la YIELD classe

Cette classe va servir à exporter les résultats produits par la procédure dans la requête Cypher qui a servi à l'appel.

Chacune des propriétés qui la compose sera accessible dans la requête Cypher grâce à l'instruction `YIELD` suivi du nom de la propriété.

Chacune des propriétés qui la compose doit-être publique et non finale

Exemple d'appel : 
`CALL srosoft.findByLabel('MyLabel') YIELD node RETURN node`

```
public class YieldClass{
	public Node node;

	public YieldClass (Node node) {
		this.node = node;
	}
}
``` 

## Ecrire le test

Le projet Neo4j [harness](https://github.com/neo4j/neo4j/tree/3.5/community/neo4j-harness/) fournit un set de classes pour JUnit permettant de tester des développements Neo4j sans pour autant lever de serveur à l'extérieur de l'environnement de test.

Les règles JUnit, matérialisées par l'annotation `@Rule` permettent de regrouper des traitements pré et post tests.

La règle `org.neo4j.harness.junit.Neo4jRule` fournie par le projet [harness](https://github.com/neo4j/neo4j/tree/3.5/community/neo4j-harness/) va nous permettre de configurer un environnement serveur Neo4j pré-chargé et lancé sur un port aléatoire. Cet environnement sera détruit à l'issu des tests.

Voici un exemple de configuration :
 
```
@Rule
public Neo4jRule neo4j = new Neo4jRule()								
					.withProcedure(FooProcedures.class)
					.withFixture("CREATE (n:MyLabel)");
```
Ce qui signifie : initialise un environnement Neo4j en intégrant le jeu de procédures `FooProcedures`  et en initialisant la base avec l'ordre CYPHER `CREATE (n:MyLabel)`.

## Déployer le plugin

Lancer le build du projet puis placer le .jar généré dans le dossier _/plugins_ du serveur Neo4j. Ensuite, redémarrer ce dernier.

Tester la présence de la procédure en tapant la requête Cypher suivante :

```
CALL dbms.procedures() YIELD name, signature, description 
WITH name,signature,description  
WHERE name="srosoft.findByLabel" 
RETURN name, signature, description
```