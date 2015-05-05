package INST767.GiraphGirvanNewman;

import java.io.IOException;

import org.apache.giraph.conf.LongConfOption;
import org.apache.giraph.edge.Edge;
import org.apache.giraph.graph.BasicComputation;
import org.apache.giraph.graph.Vertex;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.log4j.Logger;

public class SingleSourceShortestPathParentArrayCreation
    extends
    BasicComputation<LongWritable, DoubleWritable, FloatWritable, DoubleWritable> {

  private static final int MAX_SUPERSTEPS = 5;

  private static long NEXT_NODE = -1;

  /** The shortest paths id */
  public static final LongConfOption SOURCE_ID = new LongConfOption(
      "CreateParentArray.sourceId", 4, "The shortest paths id");

  /** The shortest paths id */
  public static LongConfOption LAST_NODE = new LongConfOption(
      "LastNodeVisited", -1, "The shortest paths id");

  public static LongConfOption PREV_SUPERSTEP = new LongConfOption(
      "PreviousSuperStep", 0, "Saves the number of the previous superstep.");

  public static long previousSuperStep;

  /** Class logger */
  private static final Logger LOG = Logger
      .getLogger(SingleSourceShortestPathParentArrayCreation.class);

  /**
   * Is this vertex the source id?
   *
   * @param vertex Vertex
   * @return True if the source id
   */
  private boolean isSource(Vertex<LongWritable, ?, ?> vertex) {
    return vertex.getId().get() == SOURCE_ID.get(getConf());
  }

  // Send message to all out edges which are the neighbors of this vertex
  // Message sent is the current vertex's own number
  public void BFSSend(Vertex<LongWritable, DoubleWritable, FloatWritable> vertex) {
    for (Edge<LongWritable, FloatWritable> edge : vertex.getEdges()) {
      sendMessage(edge.getTargetVertexId(), new DoubleWritable(vertex.getId()
          .get()));
    }
  }

  @Override
  public void postSuperstep() {
    System.out.println("Post super step called");
    LAST_NODE.set(getConf(), NEXT_NODE);

  };

  @Override
  public void compute(
      Vertex<LongWritable, DoubleWritable, FloatWritable> vertex,
      Iterable<DoubleWritable> messages) throws IOException {

    if (getSuperstep() == MAX_SUPERSTEPS) {
      vertex.voteToHalt();
    } else {
      if (getSuperstep() == 0) {
        if (isSource(vertex)) {
          BFSSend(vertex);
          NEXT_NODE = vertex.getId().get();
        } else {
          vertex.setValue(new DoubleWritable(0));
        }
      } else {
        double parent = LAST_NODE.get(getConf()); // here when it comes for 1,
                                                  // parent it sees is 3 instead
                                                  // of 2
        System.out.println("Parent is : " + parent + " Vertex is : "
            + vertex.getId().get() + " Next node is : " + NEXT_NODE);
        double messageRecieved = 0;
        for (DoubleWritable msg : messages) {
          messageRecieved = msg.get();
        }
        System.out.println("Message Received by " + vertex.getId().get()
            + " is : " + messageRecieved);
        if (messageRecieved == parent) {
          if (!(isSource(vertex)) && vertex.getValue().get() == 0) {
            vertex.setValue(new DoubleWritable(messageRecieved));
          }
          if (previousSuperStep < getSuperstep()) {
            BFSSend(vertex); // only send for one of the vertices, other wise
                             // everyone would send
            NEXT_NODE = vertex.getId().get();
            previousSuperStep = getSuperstep();
          }
        }
      }
    }
  }
}