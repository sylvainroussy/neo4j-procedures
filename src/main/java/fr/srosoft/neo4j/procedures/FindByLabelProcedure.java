package fr.srosoft.neo4j.procedures;

import java.util.stream.Stream;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.logging.Log;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Mode;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;
import org.neo4j.procedure.TerminationGuard;

public class FindByLabelProcedure {
	// This field declares that we need a GraphDatabaseService
    // as context when any procedure in this class is invoked
    @Context
    public GraphDatabaseService db;

    // This gives us a log instance that outputs messages to the
    // standard log, normally found under `data/log/console.log`
    @Context
    public Log log;
    
    @Context
    public TerminationGuard guard;
   
    
    @Procedure(value = "srosoft.findByLabel",mode=Mode.READ)
    @Description("return nodes by label")
    public Stream<YieldClass> findByLabel( @Name("label") String label) {
    	
    	log.info("Calling procedure: srosoft.findByLabel with label: "+label);
    	final ResourceIterator<Node> ri = db.findNodes(Label.label (label));    	
    	return ri.stream().map(YieldClass::new);
    }
    
    public class YieldClass{
    	public Node node;

		public YieldClass (Node node) {
			this.node = node;
		}
    }
    
}
