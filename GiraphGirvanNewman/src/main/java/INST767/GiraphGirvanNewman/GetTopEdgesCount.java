package INST767.GiraphGirvanNewman;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.ToolRunner;
import org.json.JSONArray;

import tl.lin.data.pair.PairOfInts;

public class GetTopEdgesCount {

  private static final String INPUT = "input";
  private static final String TOP = "top";

  JSONArray jsonArray;
  HashMap<PairOfInts, Integer> edgeCounts = new HashMap<PairOfInts, Integer>();

  public void printTopEdges(String hdfsPath, int top) throws IOException {
    System.out.println("Top Edges are: ");
    Configuration conf = new Configuration();
    org.apache.hadoop.fs.FileSystem fs =
        org.apache.hadoop.fs.FileSystem.get(conf);
    Path path = new Path(hdfsPath);

    BufferedReader bufferedReader = null;
    FSDataInputStream fsdis = null;
    InputStreamReader isr = null;

    try {
      fsdis = fs.open(path);
      isr = new InputStreamReader(fsdis);
      bufferedReader = new BufferedReader(isr);
    } catch (Exception e) {
      System.out
          .println("Some error occured while opening the file. Please check if the file is being properly made !");
    }
    try {
      String line = "";
      while ((line = bufferedReader.readLine()) != null) {
        jsonArray = new JSONArray(line);
        int left = jsonArray.getInt(0);
        JSONArray edges = (JSONArray) jsonArray.get(2);
        for (int i = 0; i < edges.length(); i++) {
          // String edge = (String) edges.get(i);
          JSONArray edge = (JSONArray) edges.get(i);
          int right = edge.getInt(0);
          int count = edge.getInt(1);
          PairOfInts pairOne = new PairOfInts(left, right);
          PairOfInts pairTwo = new PairOfInts(right, left);
          if (!edgeCounts.containsKey(pairOne) && !edgeCounts.containsKey(pairTwo)) {
            edgeCounts.put(pairOne, count);
          } else {
            if (edgeCounts.containsKey(pairOne)) {
              edgeCounts.put(pairOne, (edgeCounts.get(pairOne) + count));
            } else {
              edgeCounts.put(pairTwo, (edgeCounts.get(pairTwo) + count));
            }
          }
          // System.out.println("Left : " + left + " Right : " + right + "Count: " + count);
        }
      }
    } catch (Exception e) {
    } finally {
      bufferedReader.close();
    }
    // edgeCounts = sortEdgesHashMap();
    printEdgesHashMap();
  }

  @SuppressWarnings({ "unchecked" })
  private HashMap<PairOfInts, Integer> sortEdgesHashMap() {
    @SuppressWarnings("rawtypes")
    LinkedList linkedlist = new LinkedList(edgeCounts.keySet());
    Collections.sort(linkedlist, new CustomComparator());
    LinkedHashMap<PairOfInts, Integer> sortedHashMap = new LinkedHashMap<PairOfInts, Integer>();
    for (Iterator<PairOfInts> it = linkedlist.iterator(); it.hasNext();) {
      Map.Entry<PairOfInts, Integer> entry = (Entry<PairOfInts, Integer>) it.next();
      sortedHashMap.put(entry.getKey(), entry.getValue());
    }
    return sortedHashMap;
  }

  private void printEdgesHashMap() {
    for (PairOfInts pair : edgeCounts.keySet()) {
      System.out.println("Pair: " + pair.toString() + " Count: " + edgeCounts.get(pair));
    }
  }
  
  @SuppressWarnings("unused")
  private void printTopEdgesHashMap(int top) {
    int count = 0;
    for (PairOfInts pair : edgeCounts.keySet()) {
      count++;
      System.out.println("Pair: " + pair.toString() + " Count: " + edgeCounts.get(pair));
      if (count == top) {
        break;
      }
    }
  }

  @SuppressWarnings({ "static-access" })
  public static void main(String[] args) throws IOException {

    Options options = new Options();

    options.addOption(OptionBuilder.withArgName("path").hasArg()
        .withDescription("input path").create(INPUT));
    options.addOption(OptionBuilder.withArgName("path").hasArg()
        .withDescription("top edges").create(TOP));

    CommandLine cmdline = null;
    CommandLineParser parser = new GnuParser();

    try {
      cmdline = parser.parse(options, args);
    } catch (ParseException exp) {
      System.err.println("Error parsing command line: " + exp.getMessage());
      System.exit(-1);
    }

    if (!cmdline.hasOption(INPUT) || !cmdline.hasOption(TOP)) {
      System.out.println("args: " + Arrays.toString(args));
      HelpFormatter formatter = new HelpFormatter();
      formatter.setWidth(120);
      formatter.printHelp(GetTopEdgesCount.class.getName(), options);
      ToolRunner.printGenericCommandUsage(System.out);
      System.exit(-1);
    }

    String inputPath = cmdline.getOptionValue(INPUT);
    int top = Integer.parseInt(cmdline.getOptionValue(TOP));

    GetTopEdgesCount object = new GetTopEdgesCount();
    object.printTopEdges(inputPath, top);
  }
}

class CustomComparator implements Comparator<Map.Entry<PairOfInts, Integer>> {

  @Override
  public int compare(Entry<PairOfInts, Integer> o1,
      Entry<PairOfInts, Integer> o2) {
    return o1.getValue().compareTo(o2.getValue());
  }

}
