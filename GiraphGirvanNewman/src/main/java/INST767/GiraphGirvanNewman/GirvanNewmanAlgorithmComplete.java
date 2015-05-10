package INST767.GiraphGirvanNewman;

import java.io.IOException;

import org.apache.giraph.conf.BooleanConfOption;
import org.apache.giraph.conf.LongConfOption;
import org.apache.giraph.conf.StrConfOption;
import org.apache.giraph.edge.Edge;
import org.apache.giraph.graph.BasicComputation;
import org.apache.giraph.graph.Vertex;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.LongWritable;

import tl.lin.data.array.ArrayListOfIntsWritable;
import tl.lin.data.pair.PairOfInts;

public class GirvanNewmanAlgorithmComplete
    extends
    BasicComputation<LongWritable, ArrayListOfIntsWritable, FloatWritable, PairOfInts> {

  public static final LongConfOption PART_RUN_LENGTH = new LongConfOption(
      "PartRunLength", 50, "Tells us number of supersteps for each part");

  public static final LongConfOption MAX_SUPERSTEPS = new LongConfOption(
      "Maximum super steps", 0, "This sets the maximum number of super steps");

  BooleanConfOption USE_OUT_OF_CORE_GRAPH = new BooleanConfOption(
      "giraph.useOutOfCoreGraph", true, "Enable out-of-core graph.");

  public static StrConfOption MESSAGES_DIRECTORY = new StrConfOption(
      "giraph.messagesDirectory", "/Users/nameshkher/Desktop/Messages", "Path");

  int count = 0;

  @Override
  public void preSuperstep() {
    if (getSuperstep() == 0) {
      int maxSuperSteps = (int) PART_RUN_LENGTH.get(getConf());
      maxSuperSteps = (maxSuperSteps * 2) - 1;
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
      Iterable<PairOfInts> messages) throws IOException {

    if (getSuperstep() > MAX_SUPERSTEPS.get(getConf())) {
      // vertex.setValue(new ArrayListOfIntsWritable());
      vertex.voteToHalt();
    } else {
      /*
       * Part 1 will run here
       */
      if (getSuperstep() < PART_RUN_LENGTH.get(getConf())) {
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
      /*
       * Part 2 will run here
       */
      else {
        System.out.println("Superstep is : " + getSuperstep() + "Comes here");
        int currentVertexId = (int) vertex.getId().get();
        LongWritable targetVertex = new LongWritable();
        FloatWritable updatedMsgValue = new FloatWritable();
        ArrayListOfIntsWritable list;

        /*
         * Check if this is the first superstep in part 2
         */
        if (getSuperstep() == PART_RUN_LENGTH.get(getConf())) {

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
}

