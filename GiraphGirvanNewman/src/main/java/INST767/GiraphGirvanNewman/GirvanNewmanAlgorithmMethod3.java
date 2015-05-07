package INST767.GiraphGirvanNewman;

import java.io.IOException;

import org.apache.giraph.conf.LongConfOption;
import org.apache.giraph.graph.BasicComputation;
import org.apache.giraph.graph.Vertex;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.LongWritable;

import tl.lin.data.array.ArrayListOfIntsWritable;
import tl.lin.data.pair.PairOfInts;

public class GirvanNewmanAlgorithmMethod3 extends
    BasicComputation<LongWritable, ArrayListOfIntsWritable, FloatWritable, PairOfInts> {

  public static final LongConfOption MAX_SUPERSTEPS = new LongConfOption(
      "Maximum super steps", 3, "This sets the maximum number of super steps");

  @Override
  public void compute(
      Vertex<LongWritable, ArrayListOfIntsWritable, FloatWritable> vertex,
      Iterable<PairOfInts> messages) throws IOException {
    
    if (getSuperstep() > MAX_SUPERSTEPS.get(getConf())) {
      vertex.voteToHalt();
    } else {
      if (getSuperstep() == 0) {
        int sourceId = (int) vertex.getId().get();
        ArrayListOfIntsWritable list = new ArrayListOfIntsWritable((int) getTotalNumVertices());
        for (int i = 0; i <= getTotalNumVertices(); i++) {
          list.add(0);
        }
        vertex.setValue(list);
        sendMessageToAllEdges(vertex, new PairOfInts(sourceId, sourceId));
      }
      else {
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
          }

          // Send the message to all the edges by changing its parent
          PairOfInts newMessage = new PairOfInts(sourceId, currentVertexId);
          sendMessageToAllEdges(vertex, newMessage);
        }
      }
    }
  }
}
