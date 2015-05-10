package INST767.GiraphGirvanNewman;

import java.io.IOException;

import org.apache.giraph.conf.LongConfOption;
import org.apache.giraph.edge.Edge;
import org.apache.giraph.graph.BasicComputation;
import org.apache.giraph.graph.Vertex;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.LongWritable;

import tl.lin.data.array.ArrayListOfIntsWritable;

public class GirvanNewmanAlgorithmMSBFSOptimized
    extends
    BasicComputation<LongWritable, ArrayListOfIntsWritable, FloatWritable, MyMessage> {

  public static final LongConfOption MAX_SUPERSTEPS =
 new LongConfOption(
"Maximum super steps", 200,
          "This sets the maximum number of super steps");

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
    System.out.println("Superstep: " + getSuperstep() + "Messages sent: "
        + count);
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
        count++;
        vertex.voteToHalt();
      } else {
        int currentVertexId = (int) vertex.getId().get();
        ArrayListOfIntsWritable currentList = vertex.getValue();
        ArrayListOfIntsWritable toSend;
        for (Edge<LongWritable, FloatWritable> edge : vertex.getEdges()) {
          toSend = new ArrayListOfIntsWritable();
          for (MyMessage msg : messages) {
            int parentId = msg.getParent();
            ArrayListOfIntsWritable sourceIds = msg.getSourceList();
            for (int i = 0; i < sourceIds.size(); i++) {
              if ((sourceIds.get(i) != currentVertexId) && currentList.get(sourceIds.get(i)) == 0) {
                currentList.set(sourceIds.get(i), parentId);
              }
              if (sourceIds.get(i) != currentVertexId) {
                toSend.add(sourceIds.get(i));
              }
            }
            // if (parentId == edge.getTargetVertexId().get()) {
            // break;
            // }
          }
          // Send the packet here
          sendMessage(edge.getTargetVertexId(), new MyMessage(currentVertexId, toSend));
          count++;
        }
        vertex.voteToHalt();
      }
    }
  }
}

