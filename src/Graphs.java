import java.util.*;

public class Graphs {

	// This alignedVertics contains all the vertex obtained by running blast
	// completely.
	Vector<Vertex> alignedVertices = null;
	
	// Subgraphs are all the graphs under this top graphs
	HashMap<String, Graph> subGraphs = null;
	RepeatFinder rf = null;
	
	double error1;
	double error2;
	double error3;
	double error4;
	double error5;
	double error6;
	
	
	int globalEdgeCount=0;
	
	int highestFragIdNum ;
	
	int alignLimit;

	public Graphs(RepeatFinder rf, int alignLimit) {
		subGraphs = new HashMap<String, Graph>();
		this.rf = rf;
		this.alignLimit = alignLimit;
		
	}
	
	
	public void createGraphs(Vector<Vertex> aligned, String repeatLibrary) {
		alignedVertices = aligned;
		
		// Now for each repeat in the repeat library create a separate graph
		
		Set<String> keySet = rf.repeatMap.keySet();
		Iterator<String> it = keySet.iterator();
	    int testCount =0;	
		while(true == it.hasNext())
		{
			testCount++;
			System.out.println("Yes from TopGraph: " + testCount);
			String repeatId = it.next();
			// Create one graph id for this name
			if(false == subGraphs.containsKey(repeatId))
			{
				Graph localGraph = new Graph(repeatId, this, rf, alignLimit);
				localGraph.setErrors(error1, error2, error3, error4, error5, error6);
				subGraphs.put(repeatId, localGraph);
				
			}
		}

		System.out.println("Created graphs");
					
		// Now assign the vertices to appropriate subgraphs
		for (int i=0; i < alignedVertices.size(); i++)
		{
			Vertex localVertex = alignedVertices.get(i);
			String repeatId = localVertex.getRepeatId();
			// Add the localVertex to the graph of the repeat id
			subGraphs.get(repeatId).addVertex(localVertex);
		}
		
		System.out.println("Assigned vertices");
		//Now for each of the repeatGraphs create the corresponding edges
		Set<String> graphSet = subGraphs.keySet();
		it = graphSet.iterator();
	     
        int graphCountForEdges =0;	
		while(true == it.hasNext())
		{
			graphCountForEdges++;
			String nextGraph = it.next();
			Graph localGraph = subGraphs.get(nextGraph);
			localGraph.CreateGraphEdges();
            System.out.println("graphCountForEdges: " + graphCountForEdges);
		}
		System.out.println("Created edges");
	}
	
	public Path FindBestPath() {
		// Find best path will find out the best available path from the aligned
		// vertices
		
		Set<String> graphSet = subGraphs.keySet();
		Iterator<String> it = graphSet.iterator();
		
		double bestFitnessValue =0;
		Path bestPath = null;
		while(true == it.hasNext())
		{
			String nextGraph = it.next();
			Graph localGraph = subGraphs.get(nextGraph);
			Path localPath = localGraph.FindBestPath();
			if(null == localPath)
				continue;
			//if(localPath.pathVertex.size() > 1)
			//	System.out.println("Path size: " + localPath.pathVertex.size());
			if(null == bestPath || localPath.fitnessVal > bestFitnessValue)
			{
				bestFitnessValue = localPath.fitnessVal;
				bestPath = localPath;
			}
		
		}
		return bestPath; 
	}

	/*
	 * This function accepts a path object as input. It takes every
	 * elements (Vertex) from the path and removes them from the graphs
	 */
	public Path RemovePathFromGraphs(Path bestPath) {
		Vector<Vertex> pathVector = bestPath.pathVertex;
		for(int i=0; i < pathVector.size(); i++)
		{
			/* get the fragment id and check for all the graphs.
			  If the graph contains vertices related to that 
			  fragment, remove those vertices from that graph. */
			String fragmentId = pathVector.get(i).fragmentId;
			/* Check this fragment id for all the graphs */
			Set<String> localSubGraphs = subGraphs.keySet();
			Iterator<String> it = localSubGraphs.iterator();
			
			while(true == it.hasNext())
			{
				String localGraphId = it.next();
				
				Graph localGraph = subGraphs.get(localGraphId);
				
				if(true == localGraph.isFragmentInvolved(fragmentId))
				{
					/* Remove that entry from that graph */
					localGraph.removeFragment(fragmentId);
					
					/* Add this information to the path itself */
					/* But probably this information will not add any extra value */
					//bestPath.addInvolved(fragId, localGraphId);
				}
			}
			
		}
		
		
		return bestPath;
	}

	public void AddVertexToGraphs(Vector<Vertex> reAligned) {
		// This will add all the new vertex to graphs
		
		for (int i=0; i < reAligned.size(); i++)
		{
			Vertex localVertex = reAligned.get(i);
			String repeatId = localVertex.getRepeatId();
			// Add the localVertex to the graph of the repeat id
			int vertexClass = subGraphs.get(repeatId).addVertex(localVertex);
			subGraphs.get(repeatId).addEdges(localVertex, vertexClass);
		}
		
	}


	public void setErrors(double error1, double error2, double error3,
			double error4, double error5, double error6) {
		this.error1 = error1;
		this.error2 = error2;
		this.error3 = error3;
		this.error4 = error4;
		this.error5 = error5;
		this.error6 = error6;
		
	}
	
}



