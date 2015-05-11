package INST767.GirvanNewman;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/***
 *
 * This function transforms the graph by assigning alias node numbers to the
 * graph. It also creates a key file which contains the node mappings from
 * original to the alias node numbers.
 *
 * @author Namesh Kher, Sarika Hegde, Pramod Chavan
 *
 */

public class TransformGraph {

  int nodeCounter = 1;
  LinkedHashMap<Integer, Integer> nodeMap = new LinkedHashMap<Integer, Integer>();
  HashMap<Integer, ArrayList<Integer>> graph =
      new LinkedHashMap<Integer, ArrayList<Integer>>();
  File file;
  FileReader fReader;
  BufferedReader bReader;
  FileWriter fWriter;
  BufferedWriter bWriter;


  public void storeNodeMappings() throws IOException {
    file = new File("enron_sample_5000_initial.txt");
    fReader = new FileReader(file);
    bReader = new BufferedReader(fReader);
    String line = "";
    while ((line = bReader.readLine()) != null) {
      String arr[] = line.split("\\s+");
      int nodeId = Integer.parseInt(arr[0]);
      if (!nodeMap.containsKey(nodeId)) {
        nodeMap.put(nodeId, nodeCounter);
        nodeCounter++;
      }
    }
    bReader.close();
    // printNodeMap();
  }

  public void storeTransformedGraph() throws NumberFormatException, IOException {
    file = new File("enron_sample_5000_initial.txt");
    fReader = new FileReader(file);
    bReader = new BufferedReader(fReader);
    String line = "";
    while ((line = bReader.readLine()) != null) {
      String arr[] = line.split("\\s+");
      int nodeId = Integer.parseInt(arr[0]);
      int edgeId = Integer.parseInt(arr[1]);
      int transformedNodeId = nodeMap.get(nodeId);
      if (!graph.containsKey(transformedNodeId)) {
        graph.put(transformedNodeId, new ArrayList<Integer>());
      }
      if (nodeMap.containsKey(edgeId)) {
        ArrayList<Integer> edges = graph.get(transformedNodeId);
        // System.out.println(transformedNodeId + " " + edgeId);
        int transformedEdgeId = nodeMap.get(edgeId);
        edges.add(transformedEdgeId);
        graph.put(transformedNodeId, edges);
      }
    }
    bReader.close();
  }

  public void printTransformedGraph() {
      for (Integer node : graph.keySet()) {
        System.out.println("Node: " + node + " Edges: " + graph.get(node).toString());
      }
  }

  public void writeTransformedGraph() throws IOException {
    file = new File("enron_sample_5000_transformed.txt");
    fWriter = new FileWriter(file);
    bWriter = new BufferedWriter(fWriter);
    for (Integer node : graph.keySet()) {
      int transformedNodeId = node;
      ArrayList<Integer> edges = graph.get(transformedNodeId);
      for (Integer edge : edges) {
        bWriter.write(node + "\t" + edge);
        bWriter.newLine();
      }
    }
    bWriter.close();
    System.out.println("Done writing Graph");
  }

  public void writeKeyFile() throws IOException {
    file = new File("key_enron_5000");
    fWriter = new FileWriter(file);
    bWriter = new BufferedWriter(fWriter);
    for (Integer i : nodeMap.keySet()) {
      bWriter.write(i + "\t" + nodeMap.get(i));
      bWriter.newLine();
    }
    bWriter.close();
    System.out.println("Done writing key file");
  }


  public void printNodeMap() {
    for (Integer node : nodeMap.keySet()) {
      System.out.println(node + "   " + nodeMap.get(node));
    }
  }

  public void createTransformedGraph() throws IOException {
    storeNodeMappings();
    // printNodeMap();
    storeTransformedGraph();
    // printTransformedGraph();
    writeTransformedGraph();
    writeKeyFile();
  }

  public static void main(String[] args) throws IOException {
    TransformGraph object = new TransformGraph();
    object.createTransformedGraph();
  }

}
