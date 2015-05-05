package INST767.GirvanNewmanGiraph;

import java.io.IOException;

import org.apache.giraph.Algorithm;
import org.apache.giraph.aggregators.DoubleMaxAggregator;
import org.apache.giraph.conf.LongConfOption;
import org.apache.giraph.edge.Edge;
import org.apache.giraph.graph.BasicComputation;
import org.apache.giraph.graph.Vertex;
import org.apache.giraph.master.DefaultMasterCompute;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.log4j.Logger;


@Algorithm(name = "BFS Target Oriented", description = "BFS to find target vertex from source")
public class SimpleBFSTargetComputation
    extends
    BasicComputation<LongWritable, DoubleWritable, FloatWritable, DoubleWritable> {

  public static final int MAX_SUPERSTEPS = 50;

  /**
   * Status aggregator, in this case a max is used. When the solution is found,
   * the aggregator receives the value 1.
   */
  private static String MAX_AGG = "max";

  /**
   * Indicates the first vertex to be computed in superstep 0.
   */
  public static final LongConfOption SOURCE_ID = new LongConfOption(
"SimpleBFSComputation.SOURCE_ID", 3,
          "Is the first vertex to be computed");

  /**
   * Indicates the target vertex. Once the vertex as been found, the computation
   * can converge.
   */
  public static final LongConfOption TARGET_ID = new LongConfOption(
      "SimpleBFSComputation.TARGET_ID", 4, "Indicates the target node");

  /** Class logger */
  private static final Logger LOG = Logger
      .getLogger(SimpleBFSTargetComputation.class);

  /**
   * Target Vertex Found flag definition. Is going to be compared with the
   * aggregator value by the vertices
   */
  public static final DoubleWritable FOUND = new DoubleWritable(1d);

  /**
   * Target Vertex Missing flag definition. Is going to be compared with the
   * aggregator value by the vertices
   */
  public static final DoubleWritable MISSING = new DoubleWritable(0d);

  /**
   * Is this vertex the target vertex?
   * 
   * @param vertex
   * @return true if the analyzed node is the target
   */
  private boolean isTarget(Vertex<LongWritable, ?, ?> vertex) {
    return vertex.getId().get() == TARGET_ID.get(getConf());
  }

  /**
   * Is this vertex the start vertex?
   * 
   * @param vertex
   * @return true if analysed node is the start vertex
   */
  private boolean isStart(Vertex<LongWritable, ?, ?> vertex) {
    return vertex.getId().get() == SOURCE_ID.get(getConf());
  }

  
  /**
  * Send messages to all the connected vertices. The content of the messages
  * is not important, since just the event of receiving a message removes the
  * vertex from the inactive status.
  * 
  * @param vertex
  */
  public void BFSMessages(
      Vertex<LongWritable, DoubleWritable, FloatWritable> vertex) {
    for (Edge<LongWritable, FloatWritable> edge : vertex
        .getEdges()) {
      sendMessage(edge.getTargetVertexId(), new DoubleWritable(1d));
    }
  }


  @Override
  public void compute(
      Vertex<LongWritable, DoubleWritable, FloatWritable> vertex,
      Iterable<DoubleWritable> messages) throws IOException {

    // If the target node was found, stop computation and save values
    // This was implemented to prevent graph where vertices have more than one
    // edge to run until the maximum superstep possible.
    if (FOUND.get() == ((DoubleWritable) getAggregatedValue(MAX_AGG)).get()) {
      if (!isStart(vertex) && !isTarget(vertex)) {
        vertex.setValue(new DoubleWritable(getSuperstep()));
      }
      // can know in which superstep it was halted if there was a vertex
      // being computed.
      if (LOG.isInfoEnabled()) {
        LOG.info("Target found! Converging computation: Vertex ID: "
            + vertex.getId() + " voted to halt.");
      }
    }

    // If reaches the max supersteps then volt to hault
    else if (!(getSuperstep() == MAX_SUPERSTEPS)) {
      // if first super step
      if (getSuperstep() == 0) { // Others vertices will become activate
        // when they receive messages
        if (isStart(vertex)) {
          // Identify the start vertex in the output with the value
          vertex.setValue(new DoubleWritable(10d));
          BFSMessages(vertex);
          if (LOG.isInfoEnabled()) {
            LOG.info("Vertex ID: " + vertex.getId()
                + " [START]: Start node found!");
            // LOG.info("The maximum number of Supersteps is: "
            // + TEST_MAX_SUPERSTEPS);
          }
        }
      }

      // if it is not the first Superstep (Superstep 0) :
      // Check vertex ID
      else if (isTarget(vertex)) {
        if (LOG.isInfoEnabled()) {
          LOG.info("Target vertex found! Vertex ID: " + vertex.getId()
              + " is the target node.");
        }
        // So other vertices can know that the target was found. Used
        // graphs with branch factor larger than 1
        aggregate(MAX_AGG, FOUND);
        if (LOG.isInfoEnabled()) {
          LOG.info("Vertex ID: " + vertex.getId()
              + " [TARGET]: informed aggregator to converge computation.");
        }
        // BFSMessages(vertex); //debug purposes, to test the stop
        // condition detection for non-directed graphs
        vertex.setValue(new DoubleWritable(100d));
      }

      else {
        // Non-target vertices:
        // Send messages to all connected vertices.
        BFSMessages(vertex);

        // send 0 to the aggregator. Shows that current vertex is not
        // the target.
        aggregate(MAX_AGG, MISSING);
      }
    }
    vertex.voteToHalt();
  }

  /**
   * Master compute associated with {@link SimpleBFSComputation}. It registers
   * the required max aggregator.
   */
  public static class SimpleBFSMasterCompute extends DefaultMasterCompute {
    @Override
    public void initialize() throws InstantiationException,
        IllegalAccessException {
      registerPersistentAggregator(MAX_AGG, DoubleMaxAggregator.class);
    }
  }

}
