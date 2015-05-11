package INST767.GirvanNewman;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 
 * @author Sarika Hegde, Namesh Kher, Pramod Chavan
 *
 * Note: This parameters are specified for a particular data set. 
 * If sampling is to be done on another file then parameters must be changed. 
 *  
 */
public class SamplingGraph 
{
  @SuppressWarnings({ "resource", "rawtypes", "unchecked", "unused" })
  public static void main(String[] args) throws IOException
	{
		
		/* Read from the Input File     */
	
		BufferedReader br= null;
		String currentLine;
		br = new BufferedReader(new FileReader("email-Enron.txt"));
		
		/* Genrate A Random Node Id from the Input file */
		
		Random randomnodeGenerator = new Random();
		Integer randomNode = randomnodeGenerator.nextInt(36691);
		String rs = String.valueOf(randomNode);
		
		/* Maintain a graph to hold the out graph*/
		
	    ConcurrentHashMap<String, ArrayList<String>> graph = new ConcurrentHashMap<String, ArrayList<String>>();
	    
	    /*  Maintain an edge list to hold the child nodes  */
	    Queue queue = new LinkedList();
	    
	    
		File outputFile = new File("Sampled_Graph.txt");
		FileOutputStream fos= new FileOutputStream(outputFile);
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
		Set<String> edgelist = new CopyOnWriteArraySet<String>();
		boolean status= false;
		
		/*  Iteration 1: Root node selection  */
				while(status == false)
				{
					System.out.println("First node" + rs);

					ArrayList<String> sample = new ArrayList<String>();
					while((currentLine = br.readLine()) != null)
					{
							String[] nodes = currentLine.split("\\s+");								
											if(rs.equals(nodes[0]))
												{
													sample.add(nodes[1]);
													queue.add(nodes[1]);				
												}																		
					}
					graph.put(rs, sample);
					status =true;						
				}

	
			/* All other iterations */
				BufferedReader br1= new BufferedReader(new FileReader("email-Enron.txt"));
        Set<String> edgelist1 = new CopyOnWriteArraySet<String>();
				int length =5000;
				String currentLine1;
						Iterator<String> i = edgelist.iterator();
						boolean check=true;						
						while(!queue.isEmpty() && length > 0)
						{
							System.out.println("Edges" + rs);

							ArrayList<String> sample = new ArrayList<String>();
							rs = queue.remove().toString();					
							while((currentLine1 = br1.readLine()) != null)
							{
							String[] nodes = currentLine1.split("\\s+");	
								if(rs.equals(nodes[0]))
								{
									sample.add(nodes[1]);							
									for(String key : graph.keySet())
									{
										if(key.equals(nodes[1]))
										{
											System.out.println("here" + key);
											System.out.println("here" + nodes[1]);
											check=false;
										}							
									}
									if(check)
									{
										queue.add(nodes[1]);
									}
									check=true;
								}
							}			
							graph.put(rs, sample);
							br1= new BufferedReader(new FileReader("email-Enron.txt"));
							length--;
							System.out.println("iteration " + length );
						}
						System.out.println("Write output to file");
			
				/* Write to output file */
						Iterator it = graph.entrySet().iterator();
						while(it.hasNext())
						{
					        Map.Entry pair = (Map.Entry)it.next();
			
							ArrayList<String> new1 = (ArrayList<String>) pair.getValue();
							for (int j = 0; j < new1.size(); j++) {
								bw.write(pair.getKey() + "\t" + new1.get(j));
								bw.newLine();
							}
						}
						bw.close();						
		}
}