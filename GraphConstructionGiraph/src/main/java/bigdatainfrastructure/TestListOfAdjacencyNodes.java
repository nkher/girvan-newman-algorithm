package bigdatainfrastructure;

public class TestListOfAdjacencyNodes {

  public static void main(String[] args) {
      
    ListOfAdjacencyNodes<GraphNode> adjacency =
        new ListOfAdjacencyNodes<GraphNode>();
    adjacency.add(new GraphNode(1, 1));
    adjacency.add(new GraphNode(2, 2));
    adjacency.add(new GraphNode(3, 3));
    adjacency.add(new GraphNode(4, 4));
    adjacency.add(new GraphNode(5, 5));
    
    System.out.println(adjacency.toString());
  }

}
