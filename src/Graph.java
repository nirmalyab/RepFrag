import java.util.*;

public class Graph {
	String repeatId = null;
	Graphs topGraph = null;
	RepeatFinder rf = null;
	int repeatLength;
	HashMap<String, Fragment> fragmentMap = null;
	
	HashSet<String> fragInvolved = null;

	// Four different set of vertices according to the class of the vertex
	HashMap<String, Vertex> class0Vertex = null;
	HashMap<String, Vertex> class1Vertex = null;
	HashMap<String, Vertex> class2Vertex = null;
	HashMap<String, Vertex> class3Vertex = null;

	// Also declare a common HashMap for every class of vertex

	HashMap<String, Vertex> allVertex = null;
	
	HashMap<String, HashSet<String>> fragVertexMap = null;

	// the edgeGraphs
	HashMap<String, HashSet<String>> leftNeighborGraph = null;
	HashMap<String, HashSet<String>> rightNeighborGraph = null;

	// Declare the error constants
	double error1;
	double error2;
	double error3;
	double error4;
	double error5;
	double error6;
	

	int alignLimit;

	// String highestFitnessFrag = null;
	// double highestFitnessVal = 0;

	// Fitness value for the current path

	public Graph(String repeatId, Graphs graphs, RepeatFinder rf, int alignLimit) {
		this.repeatId = repeatId;
		this.topGraph = graphs;
		this.rf = rf;

		this.alignLimit = alignLimit;
		repeatLength = rf.getRepeatMap().get(repeatId).repeatLen;
		fragmentMap = rf.getFragmentMap();

		class0Vertex = new HashMap<String, Vertex>();
		class1Vertex = new HashMap<String, Vertex>();
		class2Vertex = new HashMap<String, Vertex>();
		class3Vertex = new HashMap<String, Vertex>();
		allVertex = new HashMap<String, Vertex>();
		leftNeighborGraph = new HashMap<String, HashSet<String>>();
		rightNeighborGraph = new HashMap<String, HashSet<String>>();
		fragInvolved = new HashSet<String>();
		fragVertexMap = new HashMap<String, HashSet<String>>();

	}

	public int addVertex(Vertex localVertex) {
		// Get the class of the alignment: class 0 to class 3 and cluster it
		// accordingly.

		int repeatStart = localVertex.repeatAlignedStart;
		int repeatEnd = localVertex.repeatAlignedEnd;

		// Calculate the fitness value of the vertex
		localVertex.calculateFitnessValue();

		// if (localFitnessVal > this.highestFitnessVal) {
		// this.highestFitnessFrag = localVertex.fragmentId;
		// this.highestFitnessVal = localFitnessVal;
		// }
		// depending on the type of the vertex add it to the related category
		// It will help us later to create the graph.
		// Now depending on the orientation of the vertex, the class of the
		// vertex will also be different.

		int vertexClass = 0;
		if (true == localVertex.straightOriented) {

			if (1 == repeatStart && repeatLength == repeatEnd) {
				// Case 0: A fragment contains a complete repeat
				vertexClass = 0;
				class0Vertex.put(localVertex.vertexId, localVertex);

			} else if (1 == repeatStart && repeatEnd < repeatLength) {
				// case 1: A fragment contains start of a repeat
				vertexClass = 1;
				class1Vertex.put(localVertex.vertexId, localVertex);

			} else if (1 < repeatStart && repeatEnd == repeatLength) {
				// case 2: A fragment contains end of a repeat
				vertexClass = 2;
				class2Vertex.put(localVertex.vertexId, localVertex);

			} else if (1 < repeatStart && repeatEnd < repeatLength) {
				// case 3: A fragment contains middle side of the repeat
				vertexClass = 3;
				class3Vertex.put(localVertex.vertexId, localVertex);
			}
		} else {
			if (repeatLength == repeatStart && 1 == repeatEnd) {
				// Case 0: A fragment contains a complete repeat
				vertexClass = 0;
				class0Vertex.put(localVertex.vertexId, localVertex);

			} else if (repeatLength == repeatStart && repeatEnd > 1) {
				// case 1: A fragment contains start of a repeat
				vertexClass = 1;
				class1Vertex.put(localVertex.vertexId, localVertex);

			} else if (repeatStart < repeatLength && repeatEnd == 1) {
				// case 2: A fragment contains end of a repeat
				vertexClass = 2;
				class2Vertex.put(localVertex.vertexId, localVertex);

			} else if (repeatStart < repeatLength && repeatEnd > 1) {
				// case 3: A fragment contains middle side of the repeat
				vertexClass = 3;
				class3Vertex.put(localVertex.vertexId, localVertex);
			}
		}
		// Add the vertex to the common map
		allVertex.put(localVertex.vertexId, localVertex);
		
		//Add the id of the fragment to the fragInvolved
		
		if(false == fragInvolved.contains(localVertex.fragmentId))
			fragInvolved.add(localVertex.fragmentId);
		
		// Add the vertex to the corresponding fragment
		//Get the fragmentToVertex map
	
		
		if(false == fragVertexMap.containsKey(localVertex.fragmentId))
		{
			HashSet<String> temp = new HashSet<String>();
			fragVertexMap.put(localVertex.fragmentId, temp);
		}
		
		HashSet<String> local = fragVertexMap.get(localVertex.fragmentId);
		local.add(localVertex.vertexId);
		
		return vertexClass;

	}

	public void CreateGraphEdges() {
		//System.out.println(class1Vertex.size() + " " + class2Vertex.size()
   //        + " " + class3Vertex.size());

		// Edges from class 1 to class 2
		createEdgeBetweenVertexClass(class1Vertex, class2Vertex);
		// Edges from class 1 to class 3
		createEdgeBetweenVertexClass(class1Vertex, class3Vertex);
		// Edges from class 3 to class 3
		createEdgeBetweenVertexClass(class3Vertex, class3Vertex);
		// Edges from class 3 to class 2
		createEdgeBetweenVertexClass(class3Vertex, class2Vertex);
	}

	private void createEdgeBetweenVertexClass(
			HashMap<String, Vertex> leftClass,
			HashMap<String, Vertex> rightClass) {
		Set<String> leftSet = null;
		Set<String> rightSet = null;
		Iterator<String> leftIt = null;
		Iterator<String> rightIt = null;

		// Edges from class 1 to class 2
		leftSet = leftClass.keySet();
		leftIt = leftSet.iterator();

		while (true == leftIt.hasNext()) {
			String leftVertexId = leftIt.next();
			Vertex leftVertex = leftClass.get(leftVertexId);

			rightSet = rightClass.keySet();
			rightIt = rightSet.iterator();

			while (true == rightIt.hasNext()) {
				String rightVertexId = rightIt.next();
				Vertex rightVertex = rightClass.get(rightVertexId);

				boolean edgeFlag = edgeCreation(leftVertex, rightVertex);

				// If the edge can be created create it for the
				// leftNeighborGraph
				// and create it for the rightNeighborGraph.
				if (true == edgeFlag) {
					//System.out.println("Edge created");
					if (false == leftNeighborGraph.containsKey(rightVertexId)) {
						HashSet<String> localSet = new HashSet<String>();
						leftNeighborGraph.put(rightVertexId, localSet);
					}
					HashSet<String> localLeftNeighborSet = leftNeighborGraph
							.get(rightVertexId);
					localLeftNeighborSet.add(leftVertexId);

					if (false == rightNeighborGraph.containsKey(leftVertexId)) {
						HashSet<String> localSet = new HashSet<String>();
						rightNeighborGraph.put(leftVertexId, localSet);
					}
					HashSet<String> localRightNeighborSet = rightNeighborGraph
							.get(leftVertexId);
					localRightNeighborSet.add(rightVertexId);
				}
			}
		}

	}

	private boolean edgeCreation(Vertex leftVertex, Vertex rightVertex) {

		// If the orientation of the two vertices are different, return false
		if (leftVertex.straightOriented != rightVertex.straightOriented)
			return false;

		
		
		// Now we are allow
		if (false == leftVertex.fragmentId.equals(rightVertex.fragmentId))
		{	

			// Calculate the constraints
			double ProbabilityMultiplier = 0.25;
			int part1 = leftVertex.fragmentUnalignedRight;
			int part2 = rightVertex.fragmentUnalignedLeft;
			double maxOfParts = part1 > part2 ? part1 : part2;
			double minOfParts = part1 < part2 ? part1 : part2;
	
			double constraint1 = maxOfParts - ProbabilityMultiplier * minOfParts;
			// Check for constraint C1
			if (constraint1 > error1)
				return false;
	
			// Take the absolute value of the constraints
	
			if (true == leftVertex.straightOriented) {
				if(rightVertex.repeatAlignedEnd <= leftVertex.repeatAlignedEnd)
					return false;
				if (rightVertex.repeatAlignedStart > leftVertex.repeatAlignedEnd) {
					int constraint2 = rightVertex.repeatAlignedStart
							- leftVertex.repeatAlignedEnd;
					if (constraint2 > error2)
						return false;
				} else {
					int constraint3 = leftVertex.repeatAlignedEnd
							- rightVertex.repeatAlignedStart;
					if (constraint3 > error3)
						return false;
				}
			} else {
				if(rightVertex.repeatAlignedEnd >= leftVertex.repeatAlignedEnd)
					return false;
	
				if (rightVertex.repeatAlignedStart < leftVertex.repeatAlignedEnd) {
					int constraint2 = leftVertex.repeatAlignedEnd
							- rightVertex.repeatAlignedStart;
					if (constraint2 > error2)
						return false;
				} else {
					int constraint3 = rightVertex.repeatAlignedStart
							- leftVertex.repeatAlignedEnd;
					if (constraint3 > error3)
						return false;
				}
			}
		}
		else
		{
			/*
			 * When we are considering two vertices that belong to the same 
			 * fragment we can directly use the formulas from Xhuhui Li's paper.
			 *  
			 */
			
			if(true == leftVertex.straightOriented)
			{
				// Check so that the right vertex partially or completely keeps right 
				// of the left vertex.
				if(rightVertex.fragmentAlignedEnd <= leftVertex.fragmentAlignedEnd)
					return false;
				
				if(rightVertex.repeatAlignedEnd <= leftVertex.repeatAlignedEnd)
					return false;
				
				if(leftVertex.fragmentAlignedEnd < rightVertex.fragmentAlignedStart
						&& leftVertex.repeatAlignedEnd < rightVertex.repeatAlignedStart)
				{
					// This is the overlapping condition
					int firstConstraintFrag = rightVertex.fragmentAlignedStart - leftVertex.fragmentAlignedEnd;
					int firstConstraintRepeat = rightVertex.repeatAlignedStart - leftVertex.repeatAlignedEnd;
					
					if(firstConstraintFrag > error4)
						return false;
					if(firstConstraintRepeat > error4)
						return false;
				}
				else if(leftVertex.fragmentAlignedStart <= rightVertex.fragmentAlignedStart 
						&& rightVertex.fragmentAlignedStart <= leftVertex.fragmentAlignedEnd
						&& leftVertex.repeatAlignedEnd < rightVertex.repeatAlignedStart)
				{
					int secConstraintFrag = leftVertex.fragmentAlignedEnd - rightVertex.fragmentAlignedStart ;
					int secConstraintRepeat = rightVertex.repeatAlignedStart - leftVertex.repeatAlignedEnd;
					
					if(secConstraintFrag > error5)
						return false;
					if(secConstraintRepeat > error5)
						return false;
					
				}
				else if(leftVertex.fragmentAlignedEnd < rightVertex.fragmentAlignedStart
						&& leftVertex.repeatAlignedStart <= rightVertex.repeatAlignedStart
						&& rightVertex.repeatAlignedStart <= leftVertex.repeatAlignedEnd)
				{
					int thirdConstraintFrag = rightVertex.fragmentAlignedStart - leftVertex.fragmentAlignedEnd;
					int thirdConstraintRepeat = leftVertex.repeatAlignedEnd - rightVertex.repeatAlignedStart;
					
					if(thirdConstraintFrag > error6)
						return false;
					if(thirdConstraintRepeat > error6)
						return false;
				}
				else
				{
					return false;
				}
					
			}
			else
			{
				
				// Check so that the right vertex partially or completely keeps right 
				// of the left vertex.
				if(rightVertex.fragmentAlignedEnd <= leftVertex.fragmentAlignedEnd)
					return false;
				
				if(rightVertex.repeatAlignedEnd >= leftVertex.repeatAlignedEnd)
					return false;
				
				if(leftVertex.fragmentAlignedEnd < rightVertex.fragmentAlignedStart
						&& leftVertex.repeatAlignedEnd > rightVertex.repeatAlignedStart)
				{
					// This is the overlapping condition
					int firstConstraintFrag = rightVertex.fragmentAlignedStart - leftVertex.fragmentAlignedEnd;
					int firstConstraintRepeat =leftVertex.repeatAlignedEnd -  rightVertex.repeatAlignedStart;
					
					if(firstConstraintFrag > error4)
						return false;
					if(firstConstraintRepeat > error4)
						return false;
				}
				else if(leftVertex.fragmentAlignedStart <= rightVertex.fragmentAlignedStart 
						&& rightVertex.fragmentAlignedStart <= leftVertex.fragmentAlignedEnd
						&& leftVertex.repeatAlignedEnd > rightVertex.repeatAlignedStart)
				{
					int secConstraintFrag = leftVertex.fragmentAlignedEnd - rightVertex.fragmentAlignedStart ;
					int secConstraintRepeat = leftVertex.repeatAlignedEnd - rightVertex.repeatAlignedStart;
					
					if(secConstraintFrag > error5)
						return false;
					if(secConstraintRepeat > error5)
						return false;
					
				}
				else if(leftVertex.fragmentAlignedEnd < rightVertex.fragmentAlignedStart
						&& leftVertex.repeatAlignedStart >= rightVertex.repeatAlignedStart
						&& rightVertex.repeatAlignedStart >= leftVertex.repeatAlignedEnd)
				{
					int thirdConstraintFrag = rightVertex.fragmentAlignedStart - leftVertex.fragmentAlignedEnd;
					int thirdConstraintRepeat = rightVertex.repeatAlignedStart - leftVertex.repeatAlignedEnd;
					
					if(thirdConstraintFrag > error6)
						return false;
					if(thirdConstraintRepeat > error6)
						return false;
				}
				else
				{
					return false;
				}
					
			}
		
		}
		topGraph.globalEdgeCount++;


		if(leftVertex.fragmentId.equals(rightVertex.fragmentId))
		{
			//System.out.println("Edge created for same fragment.");
		}
		// System.out.println("Count: " + topGraph.globalEdgeCount);
		//System.out.println("Edge Created. Orientation: " + leftVertex.straightOriented);
		return true;
		
		
	}

	/*
	 * Finds out the best path according to the leftNeighborGraph and
	 * rightNeighborGraph
	 */
	public Path FindBestPath() {
		/* Reset the path fitness value for this object */

		// First get the vertex with highest Fitness value
		/*
		 * To get the vertex with highest fitness value it's best to first scan
		 * all the vertex of this graph in order to calculate the vertex with
		 * highest fitness.
		 */
		Vertex localHighestFitnessVertex = getHighestFitnessVertex();

		if (null == localHighestFitnessVertex)
			return null;
		
		/*
		 * Once you get the vertex with highest fitness value go left and right
		 * from the starting point. Let's first go on the right.
		 */

		// Add this currentNeighbor to the sequence of best path
		Vector<Vertex> pathVector = new Vector<Vertex>();
		pathVector.add(localHighestFitnessVertex);

		String currentVertexId = localHighestFitnessVertex.vertexId;

		Set<String> currentNeighbor = rightNeighborGraph.get(currentVertexId);

		// We have to re-check if this searching on the left and right side
		// really converges. Otherwise, it might get n exception.
		int leftCount = 0;
		while (null != currentNeighbor) {
			String localNeighForPath = getBestRightNeighborForPath(
					currentVertexId, currentNeighbor);
			// Make this localNeighForpath as the currentNeighbor for the next
			// iteration and
			// create currentNeighbor for it.
			if (null != localNeighForPath) {
				if (true == pathVector.contains(allVertex
						.get(localNeighForPath)))
					break;
				currentVertexId = localNeighForPath;
				currentNeighbor = rightNeighborGraph.get(currentVertexId);
				pathVector.add(allVertex.get(currentVertexId));
			} else {
				break;
			}
			leftCount++;

		}
		// if (leftCount > 1)
		// System.out.println("LeftCount: " + leftCount);

		// Now extend the path towards the left direction
		currentVertexId = localHighestFitnessVertex.vertexId;
		currentNeighbor = leftNeighborGraph.get(currentVertexId);
		// if(null != currentNeighbor)
		// System.out.println("nonnull");
		int rightCount = 0;
		while (null != currentNeighbor) {
			String localNeighForPath = getBestLeftNeighborForPath(
					currentVertexId, currentNeighbor);
			if (null != localNeighForPath) {
				if (true == pathVector.contains(allVertex
						.get(localNeighForPath)))
					break;
				currentVertexId = localNeighForPath;
				currentNeighbor = leftNeighborGraph.get(currentVertexId);
				pathVector.add(0, allVertex.get(currentVertexId));
			} else {
				break;
			}

			rightCount++;
		}
		// if (rightCount > 1)
		// System.out.println("RightCount: " + rightCount);
		if(1 == pathVector.size())
		{
			return new Path(pathVector, localHighestFitnessVertex.fitnessValue, repeatId);
		}

		double pathFitnessValue = getFitlessValue(pathVector);
		//System.out.println("Best Path Selected. Path orientation: " + pathVector.get(0).straightOriented);

		return new Path(pathVector, pathFitnessValue, repeatId);
		
		
	}

	private String getBestRightNeighborForPath(String leftVertexId,
			Set<String> currentNeighbor) {
		Iterator<String> it = currentNeighbor.iterator();
		String bestRightNeigh = null;
		double bestFitnessVal = 0;
		while (true == it.hasNext()) {
			String rightVertexId = it.next();
			/*
			 * Lets check if this right vertex alignment size is less than 50 or
			 * not.
			 */
			if (allVertex.get(rightVertexId).alignmentLength < alignLimit)
				continue;
			double localFitnessVal = getFitnessOfEdge(leftVertexId,
					rightVertexId);
			if (null == bestRightNeigh || localFitnessVal > bestFitnessVal) {
				bestFitnessVal = localFitnessVal;
				bestRightNeigh = rightVertexId;
			}
		}

		return bestRightNeigh;
		// }

	}

	private String getBestLeftNeighborForPath(String rightVertexId,
			Set<String> currentNeighbor) {
		Iterator<String> it = currentNeighbor.iterator();
		String bestLeftNeigh = null;
		double bestFitnessVal = 0;
		while (true == it.hasNext()) {
			String leftVertexId = it.next();
			if (allVertex.get(leftVertexId).alignmentLength < alignLimit)
				continue;
			double localFitnessVal = getFitnessOfEdge(leftVertexId,
					rightVertexId);
			if (null == bestLeftNeigh || localFitnessVal > bestFitnessVal) {
				bestFitnessVal = localFitnessVal;
				bestLeftNeigh = leftVertexId;
			}
		}

		return bestLeftNeigh;
	
	}
	
	private double getFitnessOfEdge(String leftVertexId, String rightVertexId) {

		double localEdgeFitness = 0;
		Vertex leftVertex = allVertex.get(leftVertexId);
		Vertex rightVertex = allVertex.get(rightVertexId);
		double ProbabilityMultiplier = 0.25;


		double leftOver = leftVertex.repeatUnalignedLeft
				+ rightVertex.repeatUnalignedRight;
			
		if(false == leftVertex.fragmentId.equals(rightVertex.fragmentId))
		{

			// Calculate the fitness value of the edge for the two vertex
			
			int part1 = leftVertex.fragmentUnalignedRight;
			int part2 = rightVertex.fragmentUnalignedLeft;
			double maxOfParts = part1 > part2 ? part1 : part2;
			double minOfParts = part1 < part2 ? part1 : part2;
	
		
			if (true == leftVertex.straightOriented) {
	
				if (rightVertex.repeatAlignedStart > leftVertex.repeatAlignedEnd) {
					int constraint2 = rightVertex.repeatAlignedStart
							- leftVertex.repeatAlignedEnd;
					double minCond = minOfParts < constraint2 ? minOfParts
							: constraint2;
					double maxCond = maxOfParts > constraint2 ? maxOfParts
							: constraint2;
					double numerator = leftVertex.identicalBasePairNumber
							+ rightVertex.identicalBasePairNumber
							+ ProbabilityMultiplier * (minCond + leftOver);
					double denominator = leftVertex.alignmentLength
							+ rightVertex.alignmentLength + maxCond + leftOver;
					localEdgeFitness = numerator / denominator;
	
				} else {
					int constraint3 = leftVertex.repeatAlignedEnd
							- rightVertex.repeatAlignedStart;
					double haOverlap = constraint3
							* ((leftVertex.identicalBasePairNumber + rightVertex.identicalBasePairNumber)
									/ (leftVertex.alignmentLength + rightVertex.alignmentLength));
					double numerator = leftVertex.identicalBasePairNumber
							+ rightVertex.identicalBasePairNumber - haOverlap
							+ ProbabilityMultiplier * leftOver;
					double denominator = leftVertex.alignmentLength
							+ rightVertex.alignmentLength + maxOfParts
							- constraint3 + leftOver;
					localEdgeFitness = numerator / denominator;
	
				}
			} else {
				if (rightVertex.repeatAlignedStart < leftVertex.repeatAlignedEnd) {
					int constraint2 = leftVertex.repeatAlignedEnd
							- rightVertex.repeatAlignedStart;
	
					double minCond = minOfParts < constraint2 ? minOfParts
							: constraint2;
					double maxCond = maxOfParts > constraint2 ? maxOfParts
							: constraint2;
					double numerator = leftVertex.identicalBasePairNumber
							+ rightVertex.identicalBasePairNumber
							+ ProbabilityMultiplier * (minCond + leftOver);
					double denominator = leftVertex.alignmentLength
							+ rightVertex.alignmentLength + maxCond + leftOver;
					localEdgeFitness = numerator / denominator;
	
				} else {
					int constraint3 = rightVertex.repeatAlignedStart
							- leftVertex.repeatAlignedEnd;
	
					double haOverlap = constraint3
							* ((leftVertex.identicalBasePairNumber + rightVertex.identicalBasePairNumber)
									/ (leftVertex.alignmentLength + rightVertex.alignmentLength));
					double numerator = leftVertex.identicalBasePairNumber
							+ rightVertex.identicalBasePairNumber - haOverlap
							+ ProbabilityMultiplier * leftOver;
					double denominator = leftVertex.alignmentLength
							+ rightVertex.alignmentLength + maxOfParts
							- constraint3 + leftOver;
					localEdgeFitness = numerator / denominator;
	
				}
			}
		}
		else
		{
			if(true == leftVertex.straightOriented)
			{
				if(leftVertex.fragmentAlignedEnd < rightVertex.fragmentAlignedStart
						&& leftVertex.repeatAlignedEnd < rightVertex.repeatAlignedStart)
				{
					int firstConstraintFrag = rightVertex.fragmentAlignedStart - leftVertex.fragmentAlignedEnd;
					int firstConstraintRepeat = rightVertex.repeatAlignedStart - leftVertex.repeatAlignedEnd ;
					
					int minPart = firstConstraintFrag < firstConstraintRepeat ?
							firstConstraintFrag : firstConstraintRepeat;
					
					int maxPart = firstConstraintFrag > firstConstraintRepeat ?
							firstConstraintFrag : firstConstraintRepeat;
					
					double numerator = (double)(leftVertex.identicalBasePairNumber + rightVertex.identicalBasePairNumber) 
						+ ProbabilityMultiplier * (minPart + leftOver);
					double denominator = (double)(leftVertex.alignmentLength 
							+ rightVertex.alignmentLength + maxPart + leftOver);
					localEdgeFitness = numerator / denominator ;
					
				}
				else if(leftVertex.fragmentAlignedStart <= rightVertex.fragmentAlignedStart 
						&& rightVertex.fragmentAlignedStart <= leftVertex.fragmentAlignedEnd
						&& leftVertex.repeatAlignedEnd < rightVertex.repeatAlignedStart)
				{
					int secondConstraintRepeat = rightVertex.repeatAlignedStart - leftVertex.repeatAlignedEnd ;
					double overlap = leftVertex.fragmentAlignedEnd - rightVertex.fragmentAlignedStart;
					double overlapMatch = 
						((double)(leftVertex.identicalBasePairNumber + rightVertex.identicalBasePairNumber)
								/(double)(leftVertex.alignmentLength + rightVertex.alignmentLength))* overlap;
					
					double numerator = (leftVertex.identicalBasePairNumber 
							+ rightVertex.identicalBasePairNumber - overlapMatch)
						+ ProbabilityMultiplier * leftOver;
					
					double denominator = (double)(leftVertex.alignmentLength + rightVertex.alignmentLength
							+ secondConstraintRepeat + leftOver);
					
					
					localEdgeFitness = numerator / denominator ;
					
				}
				else if(leftVertex.fragmentAlignedEnd < rightVertex.fragmentAlignedStart
						&& leftVertex.repeatAlignedStart <= rightVertex.repeatAlignedStart
						&& rightVertex.repeatAlignedStart <= leftVertex.repeatAlignedEnd)
				{
					int secondConstraintFrag = rightVertex.fragmentAlignedStart - leftVertex.fragmentAlignedEnd;
					int overlap = leftVertex.repeatAlignedEnd - rightVertex.repeatAlignedStart;
					double overlapMatch = 
						((double)(leftVertex.identicalBasePairNumber + rightVertex.identicalBasePairNumber)
								/(double)(leftVertex.alignmentLength + rightVertex.alignmentLength))* overlap;
					
					double numerator = (leftVertex.identicalBasePairNumber 
							+ rightVertex.identicalBasePairNumber - overlapMatch)
						+ ProbabilityMultiplier * leftOver;
					
					double denominator = (double)(leftVertex.alignmentLength + rightVertex.alignmentLength
							+ secondConstraintFrag + leftOver);
					
					
					localEdgeFitness = numerator / denominator ;
					
				}
				
			}
			else
			{			
				if(leftVertex.fragmentAlignedEnd < rightVertex.fragmentAlignedStart
						&& leftVertex.repeatAlignedEnd > rightVertex.repeatAlignedStart)
				{
					int firstConstraintFrag = rightVertex.fragmentAlignedStart - leftVertex.fragmentAlignedEnd;
					int firstConstraintRepeat =leftVertex.repeatAlignedEnd - rightVertex.repeatAlignedStart;
					
					int minPart = firstConstraintFrag < firstConstraintRepeat ?
							firstConstraintFrag : firstConstraintRepeat;
					
					int maxPart = firstConstraintFrag > firstConstraintRepeat ?
							firstConstraintFrag : firstConstraintRepeat;
					
					double numerator = (double)(leftVertex.identicalBasePairNumber + rightVertex.identicalBasePairNumber) 
						+ ProbabilityMultiplier * (minPart + leftOver);
					double denominator = (double)(leftVertex.alignmentLength 
							+ rightVertex.alignmentLength + maxPart + leftOver);
					localEdgeFitness = numerator / denominator ;
					
				}
				else if(leftVertex.fragmentAlignedStart <= rightVertex.fragmentAlignedStart 
						&& rightVertex.fragmentAlignedStart <= leftVertex.fragmentAlignedEnd
						&& leftVertex.repeatAlignedEnd > rightVertex.repeatAlignedStart)
				{
					int secondConstraintRepeat =leftVertex.repeatAlignedEnd - rightVertex.repeatAlignedStart;
					int overlap = leftVertex.fragmentAlignedEnd -rightVertex.fragmentAlignedStart;
					double overlapMatch = 
						((double)(leftVertex.identicalBasePairNumber + rightVertex.identicalBasePairNumber)
								/(double)(leftVertex.alignmentLength + rightVertex.alignmentLength))* overlap;
					
					double numerator = (leftVertex.identicalBasePairNumber 
							+ rightVertex.identicalBasePairNumber - overlapMatch)
						+ ProbabilityMultiplier * leftOver;
					
					double denominator = (double)(leftVertex.alignmentLength + rightVertex.alignmentLength
							+ secondConstraintRepeat + leftOver);
					
					
					localEdgeFitness = numerator / denominator ;
					
				}
				else if(leftVertex.fragmentAlignedEnd < rightVertex.fragmentAlignedStart
						&& leftVertex.repeatAlignedStart >= rightVertex.repeatAlignedStart
						&& rightVertex.repeatAlignedStart >= leftVertex.repeatAlignedEnd)
				{
					int secondConstraintFrag = rightVertex.fragmentAlignedStart - leftVertex.fragmentAlignedEnd;
					int overlap = rightVertex.repeatAlignedStart - leftVertex.repeatAlignedEnd;
					double overlapMatch = 
						((double)(leftVertex.identicalBasePairNumber + rightVertex.identicalBasePairNumber)
								/(double)(leftVertex.alignmentLength + rightVertex.alignmentLength))* overlap;
					
					double numerator = (leftVertex.identicalBasePairNumber 
							+ rightVertex.identicalBasePairNumber - overlapMatch)
						+ ProbabilityMultiplier * leftOver;
					
					double denominator = (double)(leftVertex.alignmentLength + rightVertex.alignmentLength
							+ secondConstraintFrag + leftOver);
					
					
					localEdgeFitness = numerator / denominator ;
					
				}	
			}
		}
	    
        if(true == Double.isNaN(localEdgeFitness))
		{
			System.out.println("NaN of fitness value from Graph.getFitnessOfEdge()");
			System.exit(1);
		}	

		return localEdgeFitness;
	}
	
	
	private double getFitlessValue(Vector<Vertex> pathVector) {
		/*
		 * First calculate the expected matches and expected alignment part
		 */

		double numerator = 0;
		double denominator = 0;
		double probConstant = 0.25;
		for (int i = 0; i < pathVector.size() - 1; i++) {
			Vertex left = pathVector.get(i);
			Vertex right = pathVector.get(i + 1);
			/*
			 * get the constraints between ith and i+1 th vertex
			 */
			//System.out.println("Left.alignmentLength: " + left.alignmentLength);
			//System.out.println("Right.alignmentLength: " + right.alignmentLength);
			
			if(false == left.fragmentId.equals(right.fragmentId))
			{

				// Constraint1 : maxpart and minpart
				int leftFragUnalignedRight = left.fragmentUnalignedRight;
				int rightFragunalignedLeft = right.fragmentUnalignedLeft;
	
				int maxPart = leftFragUnalignedRight > rightFragunalignedLeft ? leftFragUnalignedRight
						: rightFragunalignedLeft;
				int minPart = leftFragUnalignedRight < rightFragunalignedLeft ? leftFragUnalignedRight
						: rightFragunalignedLeft;
	
				// Constraint2
				int constraint2 = 0;
				int constraint3 = 0;
				if(true == left.straightOriented)
				{
					if (right.repeatAlignedStart > left.repeatAlignedEnd) {
						constraint2 = right.repeatAlignedStart - left.repeatAlignedEnd;
		
						double localNumerator = minPart < constraint2 ? minPart
								: constraint2;
						numerator += (probConstant * localNumerator);
		
						double localDenominator = maxPart > constraint2 ? maxPart
								: constraint2;
						denominator += localDenominator;
					} else {
						constraint3 = left.repeatAlignedEnd - right.repeatAlignedStart;
		
						double localHa = constraint3
								* ((left.identicalBasePairNumber + right.identicalBasePairNumber) 
										/ (left.alignmentLength + right.alignmentLength));
						double localNumerator = -localHa;
						numerator += localNumerator;
						double localDenominator = maxPart - constraint3;
						denominator += localDenominator;
					}
				}
				else
				{
					if (right.repeatAlignedStart < left.repeatAlignedEnd) {
						constraint2 = left.repeatAlignedEnd - right.repeatAlignedStart;
	
						double localNumerator = minPart < constraint2 ? minPart
								: constraint2;
						numerator += (probConstant * localNumerator);
	
						double localDenominator = maxPart > constraint2 ? maxPart
								: constraint2;
						denominator += localDenominator;
					} else {
						constraint3 = right.repeatAlignedStart - left.repeatAlignedEnd;
	
						double localHa = constraint3
								* ((left.identicalBasePairNumber + right.identicalBasePairNumber) 
										/ (left.alignmentLength + right.alignmentLength));
						double localNumerator = -localHa;
						numerator += localNumerator;
						double localDenominator = maxPart - constraint3;
						denominator += localDenominator;
					}
				}
			}
			else
			{
				if(true == left.straightOriented)
				{
					if(left.fragmentAlignedEnd < right.fragmentAlignedStart
							&& left.repeatAlignedEnd < right.repeatAlignedStart)
					{
						int firstConstraintFrag = right.fragmentAlignedStart - left.fragmentAlignedEnd;
						int firstConstraintRepeat = right.repeatAlignedStart - left.repeatAlignedEnd;
						
						int minPart = firstConstraintFrag < firstConstraintRepeat 
								? firstConstraintFrag : firstConstraintRepeat;
						int maxPart = firstConstraintFrag > firstConstraintRepeat 
								? firstConstraintFrag : firstConstraintRepeat;
						double localNumerator = probConstant* minPart;
						numerator += localNumerator;
						
						double localDenominator = maxPart;
						denominator += localDenominator;
						
					}
					else if(left.fragmentAlignedStart <= right.fragmentAlignedStart 
							&& right.fragmentAlignedStart <= left.fragmentAlignedEnd
							&& left.repeatAlignedEnd < right.repeatAlignedStart)
					{
						
						int overlap = left.fragmentAlignedEnd - right.fragmentAlignedStart;
						int secondConstraintRepeat = right.repeatAlignedStart - left.repeatAlignedEnd;
						
						double overlapALign = overlap 
						*((double)(left.identicalBasePairNumber + right.identicalBasePairNumber)
								/(double)(left.alignmentLength + right.alignmentLength)) ;
						
						double localNumerator = - overlapALign ;
						
						double localDenominator = secondConstraintRepeat;
						
						numerator += localNumerator;
						denominator += localDenominator;
											
						
					}
					else if(left.fragmentAlignedEnd < right.fragmentAlignedStart
							&& left.repeatAlignedStart <= right.repeatAlignedStart
							&& right.repeatAlignedStart <= left.repeatAlignedEnd)
					{
						
						int overlap = left.repeatAlignedEnd - right.repeatAlignedStart;
						int thirdConstraintFrag = right.fragmentAlignedStart - left.fragmentAlignedEnd;
						
						double overlapALign = overlap 
						*((double)(left.identicalBasePairNumber + right.identicalBasePairNumber)/
								(double)(left.alignmentLength + right.alignmentLength)) ;
						
						double localNumerator = - overlapALign ;
						
						double localDenominator = thirdConstraintFrag;
						
						numerator += localNumerator;
						denominator += localDenominator;
						
					}
					
				}
				else
				{
					if(left.fragmentAlignedEnd < right.fragmentAlignedStart
							&& left.repeatAlignedEnd > right.repeatAlignedStart)
					{
						int firstConstraintFrag = right.fragmentAlignedStart - left.fragmentAlignedEnd;
						int firstConstraintRepeat = left.repeatAlignedEnd - right.repeatAlignedStart;
						
						int minPart = firstConstraintFrag < firstConstraintRepeat 
								? firstConstraintFrag : firstConstraintRepeat;
						int maxPart = firstConstraintFrag > firstConstraintRepeat 
								? firstConstraintFrag : firstConstraintRepeat;
						double localNumerator = probConstant* minPart;
						numerator += localNumerator;
						
						double localDenominator = maxPart;
						denominator += localDenominator;
						
					}
					else if(left.fragmentAlignedStart <= right.fragmentAlignedStart 
							&& right.fragmentAlignedStart <= left.fragmentAlignedEnd
							&& left.repeatAlignedEnd > right.repeatAlignedStart)
					{
						
						int overlap = left.fragmentAlignedEnd - right.fragmentAlignedStart;
						int secondConstraintRepeat = left.repeatAlignedEnd -right.repeatAlignedStart;
						
						double overlapALign = overlap 
						*((double)(left.identicalBasePairNumber + right.identicalBasePairNumber)
								/(double)(left.alignmentLength + right.alignmentLength)) ;
						
						double localNumerator = - overlapALign ;
						
						double localDenominator = secondConstraintRepeat;
						
						numerator += localNumerator;
						denominator += localDenominator;
											
						
					}
					else if(left.fragmentAlignedEnd < right.fragmentAlignedStart
							&& left.repeatAlignedStart >= right.repeatAlignedStart
							&& right.repeatAlignedStart >= left.repeatAlignedEnd)
					{
						
						int overlap = right.repeatAlignedStart -left.repeatAlignedEnd;
						int thirdConstraintFrag = right.fragmentAlignedStart - left.fragmentAlignedEnd;
						
						double overlapALign = overlap 
						*((double)(left.identicalBasePairNumber + right.identicalBasePairNumber)
								/(double)(left.alignmentLength + right.alignmentLength)) ;
						
						double localNumerator = - overlapALign ;
						
						double localDenominator = thirdConstraintFrag;
						
						numerator += localNumerator;
						denominator += localDenominator;
						
					}
					
				}
				
			}
		}
		int totalIdentical = 0;
		int totalAligned = 0;
		for (int i = 0; i < pathVector.size(); i++) {
			totalIdentical += pathVector.get(i).identicalBasePairNumber;
			totalAligned += pathVector.get(i).alignmentLength;
		}
		// Now get the leftoverpart
		int pathSize = pathVector.size();
		int leftOver = pathVector.get(0).repeatUnalignedLeft
				+ pathVector.get(pathSize - 1).repeatUnalignedRight;
		// Now create the fitnessValue of the path
		//System.out.println("totalAligned: " + totalAligned + " denominator: " 
		//	+ denominator);
		//System.out.println("totalIdentical: " + totalIdentical + " numerator: " 
		//			+ numerator + " leftOver: " + leftOver);
		double fitnessValue = (totalIdentical + numerator + probConstant
				* leftOver)
				/ (totalAligned + denominator + leftOver);
		if(true == Double.isNaN(fitnessValue))
		{
			System.out.println("Nan of fitness from : Graph.getFitnessValue()");
            System.exit(1);
		}
		return fitnessValue;
	}
	
	
	
	public Vertex getHighestFitnessVertex() {
		Vertex localHighestFitnessVertex = null;
		double localHighestFitnessVal = 0;

		Set<String> localVertexKeys = allVertex.keySet();
		// System.out.println("Size Allvertex: " + localVertexKeys.size());
		Iterator<String> localIt = localVertexKeys.iterator();

		while (true == localIt.hasNext()) {
			String nextFrag = localIt.next();
			Vertex localVertex = allVertex.get(nextFrag);
			/*
			 * Temporary fix: This fix is added because there is lots of false
			 * positive, that may come from local matching.
			 */
			if (localVertex.alignmentLength < alignLimit)
				continue;
			if (localHighestFitnessVal < localVertex.fitnessValue) {
				localHighestFitnessVal = localVertex.fitnessValue;
				localHighestFitnessVertex = localVertex;
			}
		}
		return localHighestFitnessVertex;
	}

	public boolean isVertexInvolved(String vertexId) {
		if (true == allVertex.containsKey(vertexId))
			return true;
		else
			return false;
	}

	public void removeFragment(String fragmentId) {
		/*
		 * There is quite a good amount of work to be done. 1. remove fragmentId
		 * from class0Vertex to class3Vertex. 2. remove it from
		 * leftNeighborGraph and righNeighborGraph.
		 */
		
		Set<String> localSet = fragVertexMap.get(fragmentId);
		
		if(0 < localSet.size())
		{
			Iterator<String> localIt = localSet.iterator();
			while(true == localIt.hasNext())
			{
			
				
				String vertexId= localIt.next();
				
				if (true == class0Vertex.containsKey(vertexId)) {
					class0Vertex.remove(vertexId);
				}
		
				if (true == class1Vertex.containsKey(vertexId)) {
					class1Vertex.remove(vertexId);
				}
		
				if (true == class2Vertex.containsKey(vertexId)) {
					class2Vertex.remove(vertexId);
				}
		
				if (true == class3Vertex.containsKey(vertexId)) {
					class3Vertex.remove(vertexId);
				}
		
				if (true == allVertex.containsKey(vertexId)) {
					allVertex.remove(vertexId);
				}
		
				// Clear the fragment from leftNeighborGraph and rightNeighborGraph
		
				if (true == leftNeighborGraph.containsKey(vertexId)) {
					leftNeighborGraph.remove(vertexId);
				}
		
				Set<String> leftSet = leftNeighborGraph.keySet();
				Iterator<String> leftIt = leftSet.iterator();
		
				while (true == leftIt.hasNext()) {
					String next = leftIt.next();
					Set<String> values = leftNeighborGraph.get(next);
					if (true == values.contains(vertexId)) {
						values.remove(vertexId);
					}
				}
		
				if (true == rightNeighborGraph.containsKey(vertexId)) {
					rightNeighborGraph.remove(vertexId);
				}
		
				Set<String> rightSet = rightNeighborGraph.keySet();
				Iterator<String> rightIt = rightSet.iterator();
		
				while (true == rightIt.hasNext()) {
					String next = rightIt.next();
					Set<String> values = rightNeighborGraph.get(next);
					if (true == values.contains(vertexId)) {
						values.remove(vertexId);
					}
				}
				
				
			}
			//localSet.clear();
			fragVertexMap.remove(fragmentId);
			fragInvolved.remove(fragmentId);
		}

	
	}

	public void addEdges(Vertex localVertex, int vertexClass) {

		// So, in this function we shall carefully examine
		// the type of the vertex and shall add
		// the edges to and from to this vertex accordingly
		HashMap<String, Vertex> localMap = new HashMap<String, Vertex>();
		// insert the vertex into it
		localMap.put(localVertex.vertexId, localVertex);

		if (1 == vertexClass) {
			// Edges from class 1 to class 2
			createEdgeBetweenVertexClass(localMap, class2Vertex);
			// Edges from class 1 to class 3
			createEdgeBetweenVertexClass(localMap, class3Vertex);
		} else if (2 == vertexClass) {
			// Edges from class 1 to class 2
			createEdgeBetweenVertexClass(class1Vertex, localMap);
			// Edges from class 3 to class 2
			createEdgeBetweenVertexClass(class3Vertex, localMap);
		} else if (3 == vertexClass) {

			// Edges from class 1 to class 3
			createEdgeBetweenVertexClass(class1Vertex, localMap);
			// Edges from class 3 to class 3
			createEdgeBetweenVertexClass(localMap, class3Vertex);
			createEdgeBetweenVertexClass(class3Vertex, localMap);
			// Edges from class 3 to class 2
			createEdgeBetweenVertexClass(localMap, class2Vertex);
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

	public boolean isFragmentInvolved(String fragId) {
		if(true == fragInvolved.contains(fragId))
			return true;
		else
			return false;
	}

}
