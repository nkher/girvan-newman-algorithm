package INST767.GiraphGirvanNewman;

import java.io.IOException;

import org.apache.giraph.conf.BooleanConfOption;
import org.apache.giraph.conf.LongConfOption;
import org.apache.giraph.graph.BasicComputation;
import org.apache.giraph.graph.Vertex;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.LongWritable;

import tl.lin.data.array.ArrayListOfIntsWritable;
import tl.lin.data.pair.PairOfInts;

public class GirvanNewmanAlgorithmMethod4
    extends
    BasicComputation<LongWritable, ArrayListOfIntsWritable, FloatWritable, PairOfInts> {

  public static final LongConfOption PART_RUN_LENGTH = new LongConfOption(
      "PartRunLength", 4, "Tells us number of supersteps for each part");

  public static final LongConfOption MAX_SUPERSTEPS = new LongConfOption(
      "Maximum super steps", 0, "This sets the maximum number of super steps");

  public static BooleanConfOption START_PART2 = new BooleanConfOption(
      "StartPart2", false, "Will start the part 2 of algorithm");

  public static BooleanConfOption PART2_SS0 = new BooleanConfOption(
      "Part2First", true, "Marks the first super step of part 2");

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
    if (getSuperstep() == PART_RUN_LENGTH.get(getConf())) {
      START_PART2.set(getConf(), true);
    }
    else if (getSuperstep() == (PART_RUN_LENGTH.get(getConf()) + 1)) {
      PART2_SS0.set(getConf(), false);
    }
  }

  @Override
  public void compute(
      Vertex<LongWritable, ArrayListOfIntsWritable, FloatWritable> vertex,
      Iterable<PairOfInts> messages) throws IOException {

    /*
     * Algorithm completed
     */
    if (getSuperstep() > MAX_SUPERSTEPS.get(getConf())) {
      // vertex.setValue(new ArrayListOfIntsWritable());
      vertex.voteToHalt();
    } else {
      /*
       * Part 1 will run here
       */
      if (getSuperstep() == 0) {
        int sourceId = (int) vertex.getId().get();
         ArrayListOfIntsWritable list = new ArrayListOfIntsWritable((int)getTotalNumVertices());
         for (int i = 0; i <= getTotalNumVertices(); i++) {
          list.add(0);
         }
        vertex.setValue(list);
        sendMessageToAllEdges(vertex, new PairOfInts(sourceId, sourceId));
      }
      /*
       * Part 2 will run here
       */
      else if (START_PART2.get(getConf())) {

        int currentVertexId = (int) vertex.getId().get();
        LongWritable targetVertex = new LongWritable();
        FloatWritable updatedMsgValue = new FloatWritable();
        ArrayListOfIntsWritable list;

        /*
         * Check if this is the first superstep in part 2
         */
        if (PART2_SS0.get(getConf())) {

          // System.out.println("Part 2 ss0 running.");
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
            /* Increment the edge */
            float currentEdgeValue = vertex.getEdgeValue(targetVertex).get();
            updatedMsgValue.set(currentEdgeValue + 1.0f);
            vertex.setEdgeValue(targetVertex, updatedMsgValue);
            System.out.println("Super step: " + getSuperstep() + "Vertex: "+ currentVertexId + " Edge: " + targetVertex.get()+ " Vertex Value: " + vertex.getValue() + " Edge value: "+ vertex.getEdgeValue(targetVertex));
          }
        }
        else {
          list = vertex.getValue();
          for (PairOfInts msg : messages) {
            int msgValue = msg.getLeftElement();
            int parent = list.get(msgValue);

            /* Check if the message is not the source itself */
            if (msgValue != currentVertexId) {
              /* Forward the message to parent */
              targetVertex.set(parent);
              sendMessage(targetVertex, new PairOfInts(msgValue, 0));
              /* Increment the edge */
              float currentEdgeValue = vertex.getEdgeValue(targetVertex).get();
              updatedMsgValue.set(currentEdgeValue + 1.0f);
              vertex.setEdgeValue(targetVertex, updatedMsgValue);
              System.out.println("Super step: " + getSuperstep() + "Vertex: "+ currentVertexId + " Edge: " + targetVertex.get()+ " Vertex Value: " + vertex.getValue() + " Edge value: "+ vertex.getEdgeValue(targetVertex));
            }
          }
        }
      }
      /*
       * Part 1 will run here
       */
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