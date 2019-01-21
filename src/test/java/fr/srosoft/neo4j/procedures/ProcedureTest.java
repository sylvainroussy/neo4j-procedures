package fr.srosoft.neo4j.procedures;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import org.neo4j.driver.v1.Config;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.harness.junit.Neo4jRule;

public class ProcedureTest {
	// This rule starts a Neo4j instance
	@Rule
	public Neo4jRule neo4j = new Neo4jRule().withProcedure(FindByLabelProcedure.class);

	@Test
	public void testFindByLabelProcedure() {

		// In a try-block, to make sure we close the driver after the test
		try (Driver driver = GraphDatabase.driver(neo4j.boltURI(), Config.build().withoutEncryption().toConfig())) {

			// Given I've started Neo4j with the FullTextIndex procedure class
			// which my 'neo4j' rule above does.
			Session session = driver.session();

			StatementResult sr = session.run("CALL srosoft.findByLabel('MyLabel') YIELD node RETURN node");
			while (sr.hasNext()) {
				Record record = sr.next();
				System.out.println(record.get("nodes"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("Exception occured: " + e.getMessage());
		}
	}
}
