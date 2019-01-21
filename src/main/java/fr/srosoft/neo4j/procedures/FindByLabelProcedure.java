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

public class FindByLabelProcedure {
	// This field declares that we need a GraphDatabaseService
    // as context when any procedure in this class is invoked
    @Context
    public GraphDatabaseService db;

    // This gives us a log instance that outputs messages to the
    // standard log, normally found under `data/log/console.log`
    @Context
    public Log log;
    
    
   
    
    @Procedure(value = "srosoft.findByLabel",mode=Mode.READ)
    @Description("return node by labels found nodes")
    public Stream<NodesResult> findByLabel( @Name("label") String label){
    	
    	
    	ResourceIterator<Node> ri = db.findNodes(Label.label (label));
    	log.info("Called findByLabel");
    	return ri.stream().map(NodesResult::new);
    }
    
    public  class NodesResult{
    	 public Node node;

		public NodesResult (Node node) {
			this.node = node;
		}
    }
    
}
