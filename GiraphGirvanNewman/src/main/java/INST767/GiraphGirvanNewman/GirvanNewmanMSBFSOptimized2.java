package INST767.GiraphGirvanNewman;

import java.io.IOException;

import org.apache.giraph.conf.BooleanConfOption;
import org.apache.giraph.conf.IntConfOption;
import org.apache.giraph.conf.LongConfOption;
import org.apache.giraph.edge.Edge;
import org.apache.giraph.graph.BasicComputation;
import org.apache.giraph.graph.Vertex;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.LongWritable;

import tl.lin.data.array.ArrayListOfIntsWritable;
import tl.lin.data.pair.PairOfInts;

public class GirvanNewmanMSBFSOptimized2
extends
BasicComputation<LongWritable, ArrayListOfIntsWritable, FloatWritable, PairOfInts> {

  public static final LongConfOption MAX_SUPERSTEPS = new LongConfOption(
      "Maximum super steps", 3000, "This sets the maximum number of super steps");

  public static IntConfOption START_NODE = new IntConfOption("Start Node", 0, "Start Iteration");

  public static IntConfOption END_NODE = new IntConfOption("End Node", 15, "End Iteration");
  
  public static IntConfOption NODE_DIFF = new IntConfOption("Difference To Add", 15, "End Iteration");
  
  public static IntConfOption ITERATION_END = new IntConfOption("Iteration End", 150, "End Iteration");
  
  public static IntConfOption ITERATION_LENGTH = new IntConfOption("Iteration Length", 150, "Iteration Length");
  
  public static IntConfOption ITERATION_FIRST_SUPERSTEP = new IntConfOption("Iteration First Superstep", 0, "Iterations First Superstep");
  
  public static BooleanConfOption CHANGED = new BooleanConfOption("Changed",
      false, "Changed");
  
  int count = 0;

  @Override
  public void preSuperstep() {
    if (getSuperstep() == 0) {
      int maxSuperSteps = (int) MAX_SUPERSTEPS.get(getConf());
      MAX_SUPERSTEPS.set(getConf(), maxSuperSteps);
      System.out.println("Setting max super steps to : " + maxSuperSteps);
    }
  }

  @Override
  public void postSuperstep() {
    System.out.println("Superstep: " + getSuperstep() + "Messages sent: " + count);
    count = 0;
    if (getSuperstep() == (ITERATION_END.get(getConf()) - 1) && (CHANGED.get(getConf()) == false)) {

      START_NODE.set(getConf(), (START_NODE.get(getConf()) + NODE_DIFF.get(getConf())));
      System.out.println("Setting start node to: " + START_NODE.get(getConf()));

      int newEnd = END_NODE.get(getConf()) + NODE_DIFF.get(getConf());
      END_NODE.set(getConf(), newEnd);
      System.out.println("New end is : " + newEnd);
      System.out.println("Setting end node to: " + END_NODE.get(getConf()));

      System.out.println("Iteration end is: " + ITERATION_END.get(getConf()));
      System.out.println("Iteration length is: " + ITERATION_LENGTH.get(getConf()));
      int iterationEnd = ITERATION_END.get(getConf()) + ITERATION_LENGTH.get(getConf());
      ITERATION_END.set(getConf(), iterationEnd);
      System.out.println("Setting iteration end to: " + iterationEnd);

      int iterationFirstSS = ITERATION_FIRST_SUPERSTEP.get(getConf()) + ITERATION_LENGTH.get(getConf());
      ITERATION_FIRST_SUPERSTEP.set(getConf(), iterationFirstSS);
      System.out.println("Setting iteration first super step: " + iterationFirstSS);

      CHANGED.set(getConf(), true);
    }
  }


  @Override
  public void compute(
      Vertex<LongWritable, ArrayListOfIntsWritable, FloatWritable> vertex,
      Iterable<PairOfInts> messages) throws IOException {
    /**
     * Multi Source BFS
     */
    if (getSuperstep() == MAX_SUPERSTEPS.get(getConf())) {
      vertex.voteToHalt();
    }
    else {
      /**
       * Run this for a set of nodes
       */
      if (getSuperstep() == ITERATION_FIRST_SUPERSTEP.get(getConf())) {
        int sourceId = (int) vertex.getId().get();
        if (getSuperstep() == 0) {
          ArrayListOfIntsWritable list = new ArrayListOfIntsWritable((int) getTotalNumVertices());
          for (int i = 0; i <= getTotalNumVertices(); i++) {
            list.add(0);
          }
          vertex.setValue(list);
        }

        System.out.println("Source : " + sourceId);
        // only send for a batch of sources
        if (sourceId > START_NODE.get(getConf()) && sourceId <= (END_NODE.get(getConf()))) {
          System.out.println("Source " + sourceId + " is sending messages.  ");
          sendMessageToAllEdges(vertex, new PairOfInts(sourceId, sourceId));
          count++;
        }
        vertex.voteToHalt();
        CHANGED.set(getConf(), false);
      
      } else if (getSuperstep() < ITERATION_END.get(getConf())) {
        int currentVertexId = (int) vertex.getId().get();
        for (PairOfInts msg : messages) {
          int sourceId = msg.getLeftElement();
          int parentId = msg.getRightElement();
          if (sourceId != currentVertexId) {
            // Update parent in its list only if its parent is not set
            ArrayListOfIntsWritable currentList = vertex.getValue();
            if (currentList.get(sourceId) == 0) {
              currentList.set(sourceId, parentId);
            }
            vertex.setValue(currentList);
            // Send the message to all the edges by changing its parent
            PairOfInts newMessage = new PairOfInts(sourceId, currentVertexId);
            for (Edge<LongWritable, FloatWritable> edge : vertex.getEdges()) {
              if (edge.getTargetVertexId().get() != parentId) {
                sendMessage(edge.getTargetVertexId(), newMessage);
                count++;
              }
            }
          }
        }
        // vertex.voteToHalt();
      }
    }
  }
}
