package INST767.GirvanNewman;

import java.io.IOException;

import org.apache.giraph.conf.LongConfOption;
import org.apache.giraph.graph.BasicComputation;
import org.apache.giraph.graph.Vertex;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.LongWritable;

import tl.lin.data.array.ArrayListOfIntsWritable;
import tl.lin.data.pair.PairOfInts;

/***
 *
 * Part 2 of the community detection algorithm. Travserses the parent array for
 * each vertex parallely and increments the edge counts which come on the
 * shortest paths from each source to destination pair.
 *
 * @author Namesh Kher, Sarika Hegde, Pramod Chavan
 *
 */

public class GirvanNewmanAlgorithmBacktracking
    extends
    BasicComputation<LongWritable, ArrayListOfIntsWritable, FloatWritable, PairOfInts> {

  public static final LongConfOption MAX_SUPERSTEPS = new LongConfOption(
"Maximum super steps", 150,
          "This sets the maximum number of super steps");

  int count = 0;

  @Override
  public void preSuperstep() {
  }

  @Override
  public void postSuperstep() {
    System.out.println("Superstep: " + getSuperstep() + "Messages sent: "
        + count);
    count = 0;
  }

  @Override
  public void compute(
      Vertex<LongWritable, ArrayListOfIntsWritable, FloatWritable> vertex,
      Iterable<PairOfInts> messages) throws IOException {

    /*
     * Backtracking
     */
    System.out.println("Superstep is : " + getSuperstep() + "Comes here");
    int currentVertexId = (int) vertex.getId().get();
    LongWritable targetVertex = new LongWritable();
    FloatWritable updatedMsgValue = new FloatWritable();
    ArrayListOfIntsWritable list;

    /*
     * Check if this is the first super step in part 2
     */
    if (getSuperstep() == MAX_SUPERSTEPS.get(getConf())) {
      vertex.voteToHalt();
    } else {

      if (getSuperstep() == 0) {
        System.out.println("Vertex Id: " + currentVertexId);
        int targetVertexId;
        list = vertex.getValue();
        for (int i = 1; i < list.size(); i++) {
          if (i == currentVertexId) {
            continue;
          }
          /* Send to source index */
          targetVertexId = list.get(i);
          targetVertex.set(targetVertexId);
          sendMessage(new LongWritable(targetVertexId), new PairOfInts(i, 0));
          count++;
          /* Increment the edge */
          float currentEdgeValue = vertex.getEdgeValue(targetVertex).get();
          updatedMsgValue.set(currentEdgeValue + 1.0f);
          vertex.setEdgeValue(targetVertex, updatedMsgValue);
        }
      } else {
        list = vertex.getValue();
        for (PairOfInts msg : messages) {
          int msgValue = msg.getLeftElement();
          int parent = list.get(msgValue);

          /* Check if the message is not the source itself */
          if (msgValue != currentVertexId) {
            /* Forward the message to parent */
            targetVertex.set(parent);
            sendMessage(targetVertex, new PairOfInts(msgValue, 0));
            count++;
            /* Increment the edge */
            float currentEdgeValue = vertex.getEdgeValue(targetVertex).get();
            updatedMsgValue.set(currentEdgeValue + 1.0f);
            vertex.setEdgeValue(targetVertex, updatedMsgValue);
          }
        }
      }
    }
  }
}
