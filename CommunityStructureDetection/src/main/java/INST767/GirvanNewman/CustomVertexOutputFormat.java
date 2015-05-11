package INST767.GirvanNewman;

import org.apache.giraph.edge.Edge;
import org.apache.giraph.graph.Vertex;
import org.apache.giraph.io.formats.TextVertexOutputFormat;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.json.JSONArray;
import org.json.JSONException;

import tl.lin.data.array.ArrayListOfIntsWritable;

import java.io.IOException;

public class CustomVertexOutputFormat extends TextVertexOutputFormat<LongWritable, ArrayListOfIntsWritable,
FloatWritable> {
  @Override
  public TextVertexWriter createVertexWriter(
      TaskAttemptContext context) {
    return new CustomVertexOutputFormatVertexWriter();
  }
  /**
   * VertexWriter that supports vertices with <code>double</code>
   * values and <code>float</code> out-edge weights.
   */
  private class CustomVertexOutputFormatVertexWriter extends
  TextVertexWriterToEachLine {
    @Override
    public Text convertVertexToLine(
        Vertex<LongWritable, ArrayListOfIntsWritable, FloatWritable> vertex
        ) throws IOException {
      JSONArray jsonVertex = new JSONArray();
      try {
        jsonVertex.put(vertex.getId().get());
        JSONArray vertexValues = new JSONArray();
        for (int i = 0; i < vertex.getValue().size(); i++) {
          vertexValues.put(vertex.getValue().get(i));
        }
        jsonVertex.put(vertexValues);
        JSONArray jsonEdgeArray = new JSONArray();
        for (Edge<LongWritable, FloatWritable> edge : vertex.getEdges()) {
          JSONArray jsonEdge = new JSONArray();
          jsonEdge.put(edge.getTargetVertexId().get());
          jsonEdge.put(edge.getValue().get());
          jsonEdgeArray.put(jsonEdge);
        }
        jsonVertex.put(jsonEdgeArray);
      } catch (JSONException e) {
        throw new IllegalArgumentException(
            "writeVertex: Couldn't write vertex " + vertex);
      }
      return new Text(jsonVertex.toString());
    }
  }
}