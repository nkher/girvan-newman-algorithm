package INST767.GiraphGirvanNewman;

import java.io.IOException;

import org.apache.giraph.conf.LongConfOption;
import org.apache.giraph.edge.Edge;
import org.apache.giraph.graph.BasicComputation;
import org.apache.giraph.graph.Vertex;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.LongWritable;

import tl.lin.data.array.ArrayListOfIntsWritable;
import tl.lin.data.pair.PairOfInts;

/****
 * 
 * @author Namesh Kher
 * @author Pramod Chavan
 * @author Sarika Hegde
 *
 */

public class GirvanNewmanAlgorithmFinalMSBFS
    extends
    BasicComputation<LongWritable, ArrayListOfIntsWritable, FloatWritable, PairOfInts> {

  public static final LongConfOption MAX_SUPERSTEPS = new LongConfOption(
      "Maximum super steps", 100, "This sets the maximum number of super steps");

  int count = 0;

  @Override
  public void preSuperstep() {
    if (getSuperstep() == 0) {
      int maxSuperSteps = (int) MAX_SUPERSTEPS.get(getConf());
      // maxSuperSteps = (maxSuperSteps * 2) - 1;
      MAX_SUPERSTEPS.set(getConf(), maxSuperSteps);
      System.out.println("Setting max super steps to : " + maxSuperSteps);
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
      Iterable<PairOfInts> messages) throws IOException {

    /*
     * Multi Source BFS
     */
    if (getSuperstep() == MAX_SUPERSTEPS.get(getConf())) {
      vertex.voteToHalt();
    }
      else {
      if (getSuperstep() == 0) {
            int sourceId = (int) vertex.getId().get();
        ArrayListOfIntsWritable list =
            new ArrayListOfIntsWritable((int) getTotalNumVertices());
        for (int i = 0; i <= getTotalNumVertices(); i++) {
          list.add(0);
            }
        vertex.setValue(list);
            sendMessageToAllEdges(vertex, new PairOfInts(sourceId, sourceId));
            count++;
            vertex.voteToHalt();
      } else {
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
        vertex.voteToHalt();
      }
    }
  }
}
