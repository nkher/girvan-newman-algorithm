package INST767.GiraphGirvanNewman;

import org.apache.giraph.io.formats.TextVertexInputFormat;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.InputSplit;

import tl.lin.data.array.ArrayListOfIntsWritable;
import org.apache.giraph.edge.Edge;
import org.apache.giraph.edge.EdgeFactory;
import org.apache.giraph.graph.Vertex;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.json.JSONArray;
import org.json.JSONException;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.List;

public class CustomVertexInputFormat extends TextVertexInputFormat<LongWritable, ArrayListOfIntsWritable, FloatWritable> {
  
  @Override
  public TextVertexReader createVertexReader(InputSplit split,
      TaskAttemptContext context) {
    return new CustomVertexInputFormatVertexReader();
  }
  /**
   * VertexReader that features <code>double</code> vertex
   * values and <code>float</code> out-edge weights. The
   * files should be in the following JSON format:
   * JSONArray(<vertex id>, <vertex value>,
   *   JSONArray(JSONArray(<dest vertex id>, <edge value>), ...))
   * Here is an example with vertex id 1, vertex value 4.3, and two edges.
   * First edge has a destination vertex 2, edge value 2.1.
   * Second edge has a destination vertex 3, edge value 0.7.
   * [1,4.3,[[2,2.1],[3,0.7]]]
   */
  class CustomVertexInputFormatVertexReader extends TextVertexReaderFromEachLineProcessedHandlingExceptions<JSONArray,
  JSONException> {
    
    @Override
    protected JSONArray preprocessLine(Text line) throws JSONException {
      return new JSONArray(line.toString());
    }
    
    @Override
    protected LongWritable getId(JSONArray jsonVertex) throws JSONException,
    IOException {
      System.out.println("Vertex is : " + jsonVertex.getLong(0));
      return new LongWritable(jsonVertex.getLong(0));
    }
    
    @Override
    protected ArrayListOfIntsWritable getValue(JSONArray vertexValues) throws
    JSONException, IOException {
      JSONArray valuesArray = vertexValues.getJSONArray(1);
      ArrayListOfIntsWritable list = new ArrayListOfIntsWritable(vertexValues.length());
      for (int i = 0; i < valuesArray.length(); ++i) {
        list.add(valuesArray.getInt(i));
      }
      return list;
    }
    
    @Override
    protected Iterable<Edge<LongWritable, FloatWritable>> getEdges(
        JSONArray jsonVertex) throws JSONException, IOException {
      JSONArray jsonEdgeArray = jsonVertex.getJSONArray(2);
      List<Edge<LongWritable, FloatWritable>> edges =
          Lists.newArrayListWithCapacity(jsonEdgeArray.length());
      for (int i = 0; i < jsonEdgeArray.length(); ++i) {
        JSONArray jsonEdge = jsonEdgeArray.getJSONArray(i);
        edges.add(EdgeFactory.create(new LongWritable(jsonEdge.getLong(0)),
            new FloatWritable((float) jsonEdge.getDouble(1))));
      }
      return edges;
    }
    
    @Override
    protected Vertex<LongWritable, ArrayListOfIntsWritable, FloatWritable>
    handleException(Text line, JSONArray jsonVertex, JSONException e) {
      throw new IllegalArgumentException(
          "Couldn't get vertex from line " + line, e);
    }
  }
}
