package fr.srosoft.neo4j.procedures;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.driver.v1.Config;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.types.Node;
import org.neo4j.harness.junit.Neo4jRule;

public class ProcedureTest {
	
	public static final String PRE_CYPHER = "CREATE (n:MyLabel)";
	// This rule starts a Neo4j instance
	@Rule
	public Neo4jRule neo4j = new Neo4jRule()
								.withFixture(PRE_CYPHER)
								.withProcedure(FooProcedures.class);

	@Test
	public void testFindByLabelProcedure() {

		// In a try-block, to make sure we close the driver after the test
		try (Driver driver = GraphDatabase.driver(neo4j.boltURI(), Config.build().withoutEncryption().toConfig())) {

			
			final List<Node> results = new ArrayList<>();
			try(Session session = driver.session()){
				final StatementResult sr = session.run("CALL srosoft.findByLabel('MyLabel') YIELD node RETURN node");
				while (sr.hasNext()) {
					final Record record = sr.next();
					results.add(record.get("node").asNode());					
				}
			}
			
			Assert.assertEquals(1, results.size());
			
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("Exception occured: " + e.getMessage());
		}
	}
}
