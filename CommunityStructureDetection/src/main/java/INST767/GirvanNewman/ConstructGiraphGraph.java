package INST767.GirvanNewman;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;


public class ConstructGiraphGraph extends Configured implements Tool {

  private static final Logger LOG = Logger
      .getLogger(ConstructGiraphGraph.class);

  // Mapper
  private static class ConstructGiraphGraph_Mapper extends  Mapper<LongWritable, Text, IntWritable, IntWritable> {
    
    private static IntWritable NODE_ID = new IntWritable();
    private static IntWritable EDGE_NODE_ID = new IntWritable();

    @Override
    public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {

      String line = value.toString();
      String array[] = line.split("\\s+");
      NODE_ID.set(Integer.parseInt(array[0]));
      EDGE_NODE_ID.set(Integer.parseInt(array[1]));
      
      context.write(NODE_ID, EDGE_NODE_ID);
    }
  }
  
  // Reducer
  private static class ConstructGiraphGraph_Reducer extends Reducer<IntWritable, IntWritable, IntsPair, ListOfAdjacencyNodes<GraphNode>> {
    
    private static ListOfAdjacencyNodes<GraphNode> adjacencyList = new ListOfAdjacencyNodes<GraphNode>();
    private static final int edgeWeight = 0;
    IntsPair outputKey = new IntsPair();

    @Override
    public void reduce(IntWritable key, Iterable<IntWritable> values,
        Context context) throws IOException, InterruptedException {
      Iterator<IntWritable> iter = values.iterator();
      while (iter.hasNext()) {
        int adjacentNodeId = iter.next().get();
        adjacencyList.add(new GraphNode(adjacentNodeId, edgeWeight));
      }

      // Write the node to the graph
      outputKey.set(key.get(), 0);
      context.write(outputKey, adjacencyList);

      // Clearing the list for the next node
      adjacencyList = new ListOfAdjacencyNodes<GraphNode>();
    }
  }

  private static final String INPUT = "input";
  private static final String OUTPUT = "output";
  private static final String NUM_REDUCERS = "numReducers";

  @Override
  @SuppressWarnings({ "static-access" })
  public int run(String[] args) throws Exception {

    Options options = new Options();

    options.addOption(OptionBuilder.withArgName("path").hasArg()
        .withDescription("input path").create(INPUT));
    options.addOption(OptionBuilder.withArgName("path").hasArg()
        .withDescription("output path").create(OUTPUT));
    options.addOption(OptionBuilder.withArgName("num").hasArg()
        .withDescription("number of reducers").create(NUM_REDUCERS));

    CommandLine cmdline;
    CommandLineParser parser = new GnuParser();

    try {
      cmdline = parser.parse(options, args);
    } catch (ParseException exp) {
      System.err.println("Error parsing command line: " + exp.getMessage());
      return -1;
    }

    if (!cmdline.hasOption(INPUT) || !cmdline.hasOption(OUTPUT)) {
      System.out.println("args: " + Arrays.toString(args));
      HelpFormatter formatter = new HelpFormatter();
      formatter.setWidth(120);
      formatter.printHelp(this.getClass().getName(), options);
      ToolRunner.printGenericCommandUsage(System.out);
      return -1;
    }

    String inputPath = cmdline.getOptionValue(INPUT);
    String outputPath = cmdline.getOptionValue(OUTPUT);

    int reduceTasks =
        cmdline.hasOption(NUM_REDUCERS) ? Integer.parseInt(cmdline
            .getOptionValue(NUM_REDUCERS)) : 1;

    LOG.info("Tool name: " + ConstructGiraphGraph.class.getSimpleName());
    LOG.info(" - input path: " + inputPath);
    LOG.info(" - output path: " + outputPath);
    LOG.info(" - num reducers: " + reduceTasks);

    Job job = Job.getInstance(getConf());
    job.setJobName(ConstructGiraphGraph.class.getSimpleName());
    job.setJarByClass(ConstructGiraphGraph.class);
    // job.getConfiguration().set("mapreduce.textoutputformat.separator", "");
    // job.getConfiguration().set("mapreduce.output.key.field.separator", "");
    // job.getConfiguration().set("mapreduce.output.textoutputformat.separator","");

    job.setNumReduceTasks(reduceTasks);

    FileInputFormat.setInputPaths(job, new Path(inputPath));
    FileOutputFormat.setOutputPath(job, new Path(outputPath));

    job.setMapOutputKeyClass(IntWritable.class);
    job.setMapOutputValueClass(IntWritable.class);
    job.setOutputKeyClass(IntsPair.class);
    job.setOutputValueClass(ListOfAdjacencyNodes.class);
    // job.setOutputFormatClass(MapFileOutputFormat.class);

    job.setMapperClass(ConstructGiraphGraph_Mapper.class);
    job.setReducerClass(ConstructGiraphGraph_Reducer.class);

    // Delete the output directory if it exists already.
    Path outputDir = new Path(outputPath);
    FileSystem.get(getConf()).delete(outputDir, true);

    long startTime = System.currentTimeMillis();
    job.waitForCompletion(true);
    System.out.println("Job Finished in "
        + (System.currentTimeMillis() - startTime) / 1000.0 + " seconds");

    return 0;
  }

  public static void main(String[] args) throws Exception {
    ToolRunner.run(new ConstructGiraphGraph(), args);
  }


}
