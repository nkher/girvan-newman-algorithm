------------------------------------------------------------------------------
STEPS TO RUN THE ALGORITHM FOR THE GIVEN DATA - EnronSample300
------------------------------------------------------------------------------


<B><I>This is a step wise tutorial to run the structural hole detection algorithm on the provided dataset. You do not need to perform Steps 2 - 4 as they have already been performed on the provided sample data. Hence you must clone the repo and do step 1 (clean the package) and then follow step 5, 6 and 7</I></B>

--------------------------
Step 1: Clean the package
--------------------------

(Profile For Cluster)
mvn -Phadoop_2 -fae -DskipTests clean package 

(Profile For Local Machine)
mvn package -DskipTests -Dhadoop=non_secure -P hadoop_2.0.0 


-------------------------
Step 2: Sample the Graph
-------------------------

This step samples a small graph from the big graph. The graph provided in the Enron folder is already sampled.

------------------------
Step 3: Transform Graph
------------------------

This step transforms the sampled graph by giving it alias node numbers and also generates a key file which contains the mappings. This mapping is required for the code to run. The key file is also provided along in the Enron folder.

--------------------------------
Step 4: Construct Giraph Graph
--------------------------------

This step constructs the graph in the format that giraph likes. The final result is a JSON format. 

[VertexID, [VertexValues], [[EdgeId1,EdgeVal1][EdgeId2,EdgeVal2]....]]

hadoop jar target/CommunityStructureDetection-0.0.1-SNAPSHOT-fatjar.jar INST767.GirvanNewman.ConstructGiraphGraph \
  -input enron_sample_300_transformed.txt -output Enron300Transformed -numReducers 1
  
This step is also already performedon the input sampled and transformed graph.

--------------------------------------
Step 5: Put the data files into hdfs
--------------------------------------

After step 1 you a have fresh clone of the repo. You must cd into the CommunityStructureDetection folder and execute the following commands.

hadoop fs -mkdir Input_EnronSample

hadoop fs -put EnronSample300/key_enron_300 Input_EnronSample/

hadoop fs -put EnronSample300/enron_sample_300.txt Input_EnronSample/

Note: 

1. The 'key_enron_300' file is the mappings from actual to alias node mappings
2. The 'enron_sample_300.txt' file is the sampled graph. These files are created from the above steps 2, 3 and 4.
3. Both the files are required for remaining steps.

  
--------------------------------------------
Step 6: Running the Girvan Newman Algorithm
--------------------------------------------

Part One - Multi Source BFS - Parent Array Creation
-------------------------------------------------------


hadoop jar target/CommunityStructureDetection-0.0.1-SNAPSHOT-fatjar.jar org.apache.giraph.GiraphRunner \
-D mapred.child.java.opts="-Xms10240m -Xmx15360m" \
-D giraph.useSuperstepCounters=false \
-D mapreduce.job.counters.limit=500 \
-D giraph.messageStoreFactoryClass="org.apache.giraph.comm.messages.out_of_core.DiskBackedMessageStoreFactory" \
INST767.GirvanNewman.GirvanNewmanAlgorithmMSBFS \
-vif INST767.GirvanNewman.CustomVertexInputFormat \
-vip Input_EnronSample/enron_sample_300.txt \
-vof INST767.GirvanNewman.CustomVertexOutputFormat \
-op Enron300_MSBFS -w 1 -ca mapred.job.tracker=localhost:5431


Part Two - Backtracking on Parent Array
-----------------------------------------


hadoop jar target/CommunityStructureDetection-0.0.1-SNAPSHOT-fatjar.jar org.apache.giraph.GiraphRunner \
-D mapred.child.java.opts="-Xms10240m -Xmx15360m" \
-D giraph.useSuperstepCounters=false \
-D mapreduce.job.counters.limit=500 \
-D giraph.messageStoreFactoryClass="org.apache.giraph.comm.messages.out_of_core.DiskBackedMessageStoreFactory" \
INST767.GirvanNewman.GirvanNewmanAlgorithmBacktracking \
-vif INST767.GirvanNewman.CustomVertexInputFormat \
-vip Enron300_MSBFS/part-m-00000 \
-vof INST767.GirvanNewman.CustomVertexOutputFormat \
-op Enron300_Results -w 1 -ca mapred.job.tracker=localhost:5431


------------------------
Step 7: Final Results
------------------------

This step generates the edges that have the highest count. Highest count means that this edge has occured maximum number of times in the MS BFS traversal of the whole graph run above. This step is a plain java file and makes use of two other files for running. The files are:

1. enron_sample_300.txt (The graph on which the algorithm would run)
2. key_enron_300 (The key containing the actual to alias number node mappings)

Command to run:
----------------

mvn exec:java -Dexec.mainClass=INST767.GirvanNewman.GetTopEdgesCount -Dexec.args="-input PATH_OF_YOUR_RESULTS_FILE -top 10 -key PATH_OF_YOUR_KEY_FILE"









