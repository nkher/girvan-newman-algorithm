package INST767.GirvanNewman;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.giraph.conf.LongConfOption;
import org.apache.giraph.edge.Edge;
import org.apache.giraph.graph.BasicComputation;
import org.apache.giraph.graph.Vertex;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.LongWritable;

import tl.lin.data.array.ArrayListOfIntsWritable;

/***
 *
 * Part 1 of the community detection algorithm that creates the parent array and
 * stores it in the vertex value lists which would be backtracked in the part 2.
 *
 * @author Namesh Kher, Sarika Hegde, Pramod Chavan
 *
 */

public class GirvanNewmanAlgorithmMSBFS
    extends
    BasicComputation<LongWritable, ArrayListOfIntsWritable, FloatWritable, MyMessage> {

  public static final LongConfOption MAX_SUPERSTEPS =
      new LongConfOption("Maximum super steps", 150,
          "This sets the maximum number of super steps");


  int count = 0;

  @Override
  public void preSuperstep() {
    if (getSuperstep() == 0) {
      int maxSuperSteps = (int) MAX_SUPERSTEPS.get(getConf());
      System.out.println("Max super steps to : " + maxSuperSteps);
    }
  }

  @Override
  public void postSuperstep() {
    System.out.println("Superstep: " + getSuperstep() + "Messages sent: " + count);
    count = 0;
  }

  @Override
  public void compute(
      Vertex<LongWritable, ArrayListOfIntsWritable, FloatWritable> vertex,
      Iterable<MyMessage> messages) throws IOException {

    /*
     * Multi Source BFS
     */
    if (getSuperstep() == MAX_SUPERSTEPS.get(getConf())) {
      vertex.voteToHalt();
    } else {
      if (getSuperstep() == 0) {
        int sourceId = (int) vertex.getId().get();
        ArrayListOfIntsWritable list =
            new ArrayListOfIntsWritable((int) getTotalNumVertices());
        for (int i = 0; i <= getTotalNumVertices(); i++) {
          list.add(0);
        }
        vertex.setValue(list);
        ArrayListOfIntsWritable sourceIds = new ArrayListOfIntsWritable();
        sourceIds.add(sourceId);
        sendMessageToAllEdges(vertex, new MyMessage(sourceId, sourceIds));
        // System.out.println("Sending message of size: " + sourceIds.size() + "Content: " + sourceIds.toString());
        count++;
        vertex.voteToHalt();
      } else {
        int currentVertexId = (int) vertex.getId().get();
        ArrayListOfIntsWritable currentList = vertex.getValue();
        ArrayListOfIntsWritable toSend;
        Set<Integer> set = new HashSet<Integer>();

        /*
         * Iterate over Edges
         */
        for (Edge<LongWritable, FloatWritable> edge : vertex.getEdges()) {

          int currentEdge = (int) edge.getTargetVertexId().get(); // 1
          toSend = new ArrayListOfIntsWritable();

          /*
           * Iterate over Messages
           */
          for (MyMessage msg : messages) { // 1
            int parentId = msg.getParent();
            ArrayListOfIntsWritable sourceIds = msg.getSourceList();

            /*
             * Traversing the received source list
             */
            for (int i = 0; i < sourceIds.size(); i++) {
              if ((sourceIds.get(i) != currentVertexId) && currentList.get(sourceIds.get(i)) == 0) {
                currentList.set(sourceIds.get(i), parentId);
              }
            }
            
            /* Make the set here */
            if (parentId != currentEdge) {
              for (int i = 0; i < sourceIds.size(); i++) {
                set.add(sourceIds.get(i));
              }
            }
          }

          /* Make the actual packet here and send */
          for (Integer i : set) {
            toSend.add(i);
          }
          sendMessage(edge.getTargetVertexId(), new MyMessage(currentVertexId, toSend));
          System.out.println("VertexId: " + currentVertexId + " Sending to: " + edge.getTargetVertexId() + " Sending message of size: " + toSend.size());
          set.clear();
          toSend.clear();
          count++;
        }
        vertex.voteToHalt();
      }
    }
  }
}

