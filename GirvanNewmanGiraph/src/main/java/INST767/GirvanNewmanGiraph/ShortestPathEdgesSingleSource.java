package INST767.GirvanNewmanGiraph;

import java.io.IOException;

import org.apache.giraph.conf.LongConfOption;
import org.apache.giraph.edge.Edge;
import org.apache.giraph.graph.BasicComputation;
import org.apache.giraph.graph.Vertex;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.log4j.Logger;

public class ShortestPathEdgesSingleSource
    extends
    BasicComputation<LongWritable, DoubleWritable, FloatWritable, DoubleWritable> {

  private static final int MAX_SUPERSTEPS = 10;

  private static long NEXT_NODE = -1;

  private static boolean runpart2 = false;

  public static final LongConfOption PART2_SS0 = new LongConfOption(
      "PART2_SS0 Checker", 0, "Checks if this is the first super step of Part 2");

  /** The shortest paths id */
  public static final LongConfOption SOURCE_ID = new LongConfOption(
      "CreateParentArray.sourceId", 1, "The shortest paths id");

  /** The shortest paths id */
  public static LongConfOption LAST_NODE = new LongConfOption(
      "LastNodeVisited", -1, "The shortest paths id");
  
  public static LongConfOption PREV_SUPERSTEP = new LongConfOption(
      "PreviousSuperStep", 0, "Saves the number of the previous superstep.");

  public static long previousSuperStep;

  /** Class logger */
  private static final Logger LOG = Logger
      .getLogger(ShortestPathEdgesSingleSource.class);

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
         sendMessage(edge.getTargetVertexId(), new DoubleWritable(vertex.getId().get()));
    }
  }
  

  @Override
  public void postSuperstep() {
    System.out.println("Post super step called");
    LAST_NODE.set(getConf(), NEXT_NODE);
    PART2_SS0.set(getConf(), 0);
    if ((getSuperstep() + 1) % 5 == 0) { // Means part 1 is complete
      System.out.println("In barrier superstep is: " + getSuperstep());
      runpart2 = true;
      PART2_SS0.set(getConf(), 1);
    }
  };

  @Override
  public void compute(
      Vertex<LongWritable, DoubleWritable, FloatWritable> vertex,
      Iterable<DoubleWritable> messages) throws IOException {

    // End of part 1 and part 2 
    if (getSuperstep() == MAX_SUPERSTEPS) {
      vertex.voteToHalt();
    } 
    // Part - 2 : Back tracking
    else if (runpart2) {

      System.out.println("Superstep: " + getSuperstep());

      if (vertex.getId().get() == SOURCE_ID.get(getConf())) {
        vertex.voteToHalt();
      }
      else {
        /* *
         * Logic to receive incoming message from children
         */
        double finalIncomingMessage = 0;
        for (DoubleWritable msg : messages) {
          finalIncomingMessage += msg.get();
        }

        FloatWritable updatedValue = new FloatWritable(0);
        long vertexParent = (long) vertex.getValue().get();
        LongWritable vrtxParent = new LongWritable(vertexParent);

        /* Updates the parents edge value with incoming message */
        System.out.println("Vertex Before Updation: " + vertex.getId().get());
        if (finalIncomingMessage > 0) {
          updatedValue.set((vertex.getEdgeValue(vrtxParent).get()) + (float) finalIncomingMessage);
          vertex.setEdgeValue(vrtxParent, updatedValue);
          sendMessage(vrtxParent, new DoubleWritable(1.0));
          System.out.println("After updation, Vertex : " + vertex.getId().get() + " Value: " + vertex.getValue()  );
        } else if (PART2_SS0.get(getConf()) == 1) {
          /* Updates the parent edge with zero as this is the first time */
          updatedValue.set((float) (vertex.getEdgeValue(vrtxParent).get() + 1.0));
          vertex.setEdgeValue(vrtxParent, updatedValue);
          System.out.println("After updation, Vertex : " + vertex.getId().get() + " Value: " + vertex.getValue()  );
          sendMessage(vrtxParent, new DoubleWritable(1.0));

        }

        /*
         * Logic to send message to parent for the next superstep
         */
        System.out.println("Paremnts Edge Value: " + vertex.getEdgeValue(vrtxParent));
      }

    }
    // Part - 1 : Calculating Parent Array
    else {
        if (getSuperstep() == 0) {
          if (isSource(vertex)) {
            BFSSend(vertex);
          NEXT_NODE = vertex.getId().get();
          } else {
          vertex.setValue(new DoubleWritable(0));
          }
      } else {
            double parent = LAST_NODE.get(getConf()); // here when it comes for 1, parent it sees is 3 instead of 2
            System.out.println("Parent is : " + parent + " Vertex is : " + vertex.getId().get() + " Next node is : " + NEXT_NODE);
            double messageRecieved = 0;
            for (DoubleWritable msg : messages) {
              messageRecieved = msg.get();
        }
        System.out.println("Message Received by " + vertex.getId().get()  + " is : " + messageRecieved);
            if (messageRecieved == parent) {
                if (!(isSource(vertex)) && vertex.getValue().get() == 0) {
                    vertex.setValue(new DoubleWritable(messageRecieved));
                }
                if (PREV_SUPERSTEP.get(getConf()) < getSuperstep()) {
                    BFSSend(vertex); // only send for one of the vertices, other wise everyone would send
                    NEXT_NODE = vertex.getId().get();
                    PREV_SUPERSTEP.set(getConf(), getSuperstep());
                } 
            }
        }
    }
  }
}