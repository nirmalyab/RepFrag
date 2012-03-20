import java.io.*;
import java.util.*;
import java.util.regex.*;

public class RepeatFinder {

	public RepeatFinder(int alignLimit) {
		/*
		 * Initially this data structure will be empty. Whenever we have a new
		 * hashed fragment we shall enter it in this data structure. If we have
		 * the one that is already old we shall map the hashed location from a
		 * vertex or non-hashed fragment to this one.
		 */
        this.alignLimit = alignLimit;
		topGraph = new Graphs(this, alignLimit);
		repeatMap = new HashMap<String, Repeat>();
		fragmentMap = new HashMap<String, Fragment>();

	}

	public static void main(String[] args) {
		/*
		 * There is only one file that are passed through the command line. All
		 * the parameters are passed through that file.
		 */
		BufferedReader inFile = null;
		try {
			inFile = new BufferedReader(new FileReader(args[0]));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/*
		 * The inputs are given in the argument name = argument value format.
		 * 
		 */

		String FragmentFile = null;
		String RepeatLibrary = null;
		String BlastPath = null;
		String RepeatAnnotation = null;
		String OutputFile = null;
		String AccuracyFile = null;
		double Error1 = 0;
		double Error2 = 0;
		double Error3 = 0;
		double Error4 = 0;
		double Error5 = 0;
		double Error6 = 0;
		int alignLimit = 0;
		String localPath = null;

		String regex = "(\\S+)\\s+(\\S+)";
		Pattern pat = Pattern.compile(regex);
		Matcher mat = null;

		String line = null;

		try {
			while (null != (line = inFile.readLine())) {
				mat = pat.matcher(line);
				if (true == mat.find()) {
					String argName = mat.group(1);
					String argVal = mat.group(2);

					if (true == argName.equals("FragmentFile")) {
						FragmentFile = argVal;
					} else if (true == argName.equals("RepeatLibrary")) {
						RepeatLibrary = argVal;

					} else if (true == argName.equals("BlastPath")) {
						BlastPath = argVal;
					} else if (true == argName.equals("RepeatAnnotation")) {
						RepeatAnnotation = argVal;
					} else if (true == argName.equals("OutputFile")) {
						OutputFile = argVal;
					} else if (true == argName.equals("AccuracyFile")) {
						AccuracyFile = argVal;
					} else if (true == argName.equals("Error1")) {
						Error1 = Double.parseDouble(argVal);
					} else if (true == argName.equals("Error2")) {
						Error2 = Double.parseDouble(argVal);
					} else if (true == argName.equals("Error3")) {
						Error3 = Double.parseDouble(argVal);
					} else if (true == argName.equals("Error4")) {
						Error4 = Double.parseDouble(argVal);
					} else if (true == argName.equals("Error5")) {
						Error5 = Double.parseDouble(argVal);
					} else if (true == argName.equals("Error6")) {
						Error6 = Double.parseDouble(argVal);
					} else if (true == argName.equals("AlignLimit")) {
						alignLimit = Integer.parseInt(argVal);
					} else if (true == argName.equals("Path")) {
						localPath = argVal;
					} else {
						System.out.println("Invalid Argument: " + argName
								+ " argVal: " + argVal);
					}
				}

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/*
		 * Create a new object of RepeatFinder
		 * 
		 */
		RepeatFinder rf = new RepeatFinder(alignLimit);
		rf.startRepeatFinder(FragmentFile, RepeatLibrary, BlastPath,
				RepeatAnnotation, OutputFile, AccuracyFile, Error1, Error2,
				Error3, Error4, Error5, Error6, localPath);

	}

	/* This is the top level function */
	public void startRepeatFinder(String FragmentFile, String RepeatLibrary,
			String blastpath, String repeatAnnotation, String outputFile,
			String AccuracyFile, double error1, double error2, double error3,
			double error4, double error5, double error6, String Path) {

		this.Path = Path;
		this.blastpath = blastpath;
		ReadRepeats(RepeatLibrary);
		ReadFragments(FragmentFile);

		int mode = 0;

		Vector<Vertex> aligned = RunBlast(FragmentFile, RepeatLibrary, mode,
				null, 0);
		topGraph.setErrors(error1, error2, error3, error4, error5, error6);
		topGraph.createGraphs(aligned, RepeatLibrary);
		boolean condition = true;
		boolean validateCondition = false;
		int loopCount = 0;

		// Delete the accuracy file for this iteration
		File file = new File(AccuracyFile);
		file.delete();
		
		
		// Prepare the accuracy calculator for the validation
		AccuracyCalculator accu = new AccuracyCalculator();
		// Run the annotation over the fragments
		accu.getAnnotatedRegions(repeatAnnotation, FragmentFile);
		// So we have annotation information over all the fragments.
		// This is a one time costly part.
		

		while (true == condition) {
			loopCount++;
			System.out.println("Yes");
			/* Find the best path from all the graphs */

			Path bestPath = topGraph.FindBestPath();
			System.out.println("Yes Next");
			if (null == bestPath) {
				/*
				 * End of the program as no best path could be found. Exit from
				 * the program.
				 */
				return;
			}
			/*
			 * Let's print some important information for debugging as detailed
			 * as possible.
			 */
			
			  System.out.println("******BestPath Information*******");
			  System.out.println("FitnessValue: " + bestPath.fitnessVal);
			  System.out.println("RepeatId: " + bestPath.repeatId); Vertex
			  localVert = bestPath.pathVertex.get(0);
			  System.out.println("FragmentAlignmentStart: " +
			  localVert.fragmentAlignedStart);
			  System.out.println("FragmentAlignmentEnd: " +
			  localVert.fragmentAlignedEnd);
			  System.out.println("RepeatAlignmentStart: " +
			  localVert.repeatAlignedStart);
			  System.out.println("RepeatAlignmentEnd: " +
			  localVert.repeatAlignedEnd); System.out.println("FragmentLength: " +
			  localVert.fragmentLength); System.out.println("RepeatLength: " +
			  localVert.repeatLength); System.out.println("FragmentUnalignLeft: " +
			  localVert.fragmentUnalignedLeft);
			  System.out.println("FragmentunalignRight: " +
			  localVert.fragmentUnalignedRight);
			  System.out.println("RepeatUnalignLeft: " +
			  localVert.repeatUnalignedLeft);
			  System.out.println("RepeatunalignRight: " +
			  localVert.repeatUnalignedRight);
			  
			  System.out.println("Size of path: " +
			  bestPath.pathVertex.size());
			  System.out.println("After  FindBestPath()");
			 
			// System.out.println("Best Path Frag: " +
			// bestPath.pathVertex.get(0).fragmentId);
			// System.out.println("Best Path size: " +
			// bestPath.pathVertex.size());
			/* Remove fragments of the path from the corresponding graphs */
			Path removed = topGraph.RemovePathFromGraphs(bestPath);
			// System.out.println("After Remove Path From Graphs()");
			/*
			 * Trim the path from each vertices. Also trim the chromosome
			 * fragments accordingly. Keep track of the regions that are masked
			 * and removed as repeat.
			 */
			TreeMap<Integer, FragmentPart> trimmed = UpdateRemovedPath(removed);
			/*
			 * Run blast only for those fragments that are affected by the
			 * trimming.
			 */

			// System.out.println("After Update Removed Path()");
			if (null == trimmed) {
				continue;
			}

			Vector<Vertex> reAligned = RunBlasPartially(trimmed, RepeatLibrary);
			// System.out.println("After RunBlastPartially()");
			/*
			 * Update the graphs. Add the fragments to the graphs according to
			 * the blast
			 */
			/* alignment. */
			if (null != reAligned) {
				topGraph.AddVertexToGraphs(reAligned);
			}

			// System.out.println("After AddVertexToGraph()");
			if (0 == loopCount % 100) {
				System.out
						.println("============================================");
				// Merge the fragments
				mergeFragmentParts();

				// Create results
				System.out.println("Entering CreateResults.");
				createResults(outputFile);
				System.out.println("Exiting CreateResults.");
				// Validation

				accu.getMaskedRegions(FragmentFile, outputFile);
				accu.getStatistics(AccuracyFile);
				//System.out.println("Ending Accuracy Calculation.");

			}
            System.out.println("Starting Calculation of available matrix.");
			int availableVertex = calculateAvailabeVertex();
			System.out.println("Available Vertex()" + availableVertex);

		}

		System.out.println("============================================");
		// Merge the fragments
		mergeFragmentParts();

		// Create results
		createResults(outputFile);
		// Validation
		accu.getMaskedRegions(FragmentFile, outputFile);
		accu.getStatistics(AccuracyFile);

	}

	private int calculateAvailabeVertex() {
		HashMap<String, Graph> localGraphs = topGraph.subGraphs;
		Set<String> localSet = localGraphs.keySet();
		Iterator<String> it = localSet.iterator();

		int totalCount = 0;
		while (true == it.hasNext()) {
			String next = it.next();
			Graph local = localGraphs.get(next);
			int localCount = local.allVertex.size();
			totalCount += localCount;
		}
		return totalCount;
	}

	/*
	 * This function creates an output file that is compatible to that of the
	 * AccuracyCalculator
	 */
	private void createResults(String outFile) {

		PrintWriter outputFile = null;
		try {
			outputFile = new PrintWriter(new FileWriter(outFile));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Set<String> keys = fragmentMap.keySet();
		Iterator<String> it = keys.iterator();

		while (true == it.hasNext()) {
			String fragmentId = it.next();
			Fragment localFrag = fragmentMap.get(fragmentId);

			// Get the hashedParts
			TreeMap<Integer, HashedPartInfo> hashedParts = localFrag.hashedParts;
			// Now print the hashed parts
			Set<Integer> localKeys = hashedParts.keySet();
			Iterator<Integer> localIt = localKeys.iterator();
			while (true == localIt.hasNext()) {
				int startPos = localIt.next();
				int endPos = hashedParts.get(startPos).endPos;
				String repeatName = hashedParts.get(startPos).repeatStr;
				int repeatStart = hashedParts.get(startPos).repeatStart;
				int repeatEnd = hashedParts.get(startPos).repeatEnd;
				if (true == hashedParts.get(startPos).straighOriented) {
					outputFile.println(fragmentId + " " + startPos + " "
							+ endPos + " " + repeatName + " + " + repeatStart
							+ " " + repeatEnd);
				} else {
					outputFile.println(fragmentId + " " + startPos + " "
							+ endPos + " " + repeatName + " - " + repeatStart
							+ " " + repeatEnd);
				}
			}
		}

		outputFile.close();

	}

	private void mergeFragmentParts() {
		System.out.println("Entered mergeFragmentParts");

		Set<String> keys = fragmentMap.keySet();
		Iterator<String> it = keys.iterator();

		while (true == it.hasNext()) {
			String nextKey = it.next();

			Fragment localFrag = fragmentMap.get(nextKey);
			localFrag.mergeParts();
		}
		System.out.println("Exit  mergeFragmentParts");

	}

	private Vector<Vertex> RunBlasPartially(
			TreeMap<Integer, FragmentPart> trimmed, String repeatLibrary) {

		/*
		 * The trimmed contained material to create a new fragment. Create that
		 * fragment. For that we have to copy the base pairs from appropriate
		 * fragments, we have to assemble them in a file and finally we have to
		 * run blast partially over that new fragment.
		 */

		/*
		 * Create the temporary fragment in a string
		 */
		String localFragStr = new String();
		Set<Integer> keySet = trimmed.keySet();
		Iterator<Integer> it = keySet.iterator();

		while (true == it.hasNext()) {
			int next = it.next();
			FragmentPart fragPart = trimmed.get(next);
			String fragId = fragPart.thatFragId;
			int startOnThat = fragPart.startPosOnThat;
			int endOnThat = fragPart.endPosOnThat;

			Fragment localFrag = fragmentMap.get(fragId);
			String localBuffer = localFrag.fragmentStr.substring(
					startOnThat - 1, endOnThat);
			localFragStr = localFragStr.concat(localBuffer);
		}

		/*
		 * Now that a temporary fragment is created we shall check its length.
		 * If that is 0, we shall return null. Otherwise we shall create a
		 * temporary file containing that fragment and shall run the blast only
		 * for that fragment.
		 */
		if (0 == localFragStr.length())
			return null;

		topGraph.highestFragIdNum++;
		// String tempFragFileName = "tempFragFile." +
		// topGraph.highestFragIdNum;
		String tempFragFileName = Path + "/tempFragFile."
				+ topGraph.highestFragIdNum;
		// System.out.println(topGraph.highestFragIdNum);
		PrintWriter tempFragFile = null;
		try {
			tempFragFile = new PrintWriter(new FileWriter(tempFragFileName));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// System.out.println("Fragment: " + localFragStr);

		int localFragLength = localFragStr.length();
		String firstFragId = trimmed.firstEntry().getValue().thatFragId;
		int startPosOnThat = trimmed.firstEntry().getValue().startPosOnThat;
		int startPosOnCh = fragmentMap.get(firstFragId).fragmentStart
				+ startPosOnThat - 1;

		String firstLine = ">fragment" + topGraph.highestFragIdNum
				+ " startPos " + startPosOnCh + " length " + localFragLength;
		// Create a fragment and and it into the fragmentmap

		String localFragId = "fragment" + topGraph.highestFragIdNum;
		Fragment localOne = new Fragment(localFragId, startPosOnCh,
				localFragLength);
		fragmentMap.put(localFragId, localOne);

		tempFragFile.println(firstLine);

		// Now write the fragment line one by one 70 characters at a time
		int count = 0;
		while (count < localFragLength) {
			int localStartPos = count;
			int localEndPos = localStartPos + 70;
			if (localEndPos > localFragLength)
				localEndPos = localFragLength;
			String subString = localFragStr.substring(localStartPos,
					localEndPos);
			tempFragFile.println(subString);
			// Update the counter
			count = localEndPos;
		}

		// close the file

		tempFragFile.close();

		// Now run partial blast over it
		int mode = 1;
		Vector<Vertex> localVertex = RunBlast(tempFragFileName, repeatLibrary,
				mode, trimmed, localFragLength);
		// Delete the file as it is no longer required

		File tempFile = new File(tempFragFileName);
		tempFile.delete();

		return localVertex;
	}

	private TreeMap<Integer, FragmentPart> UpdateRemovedPath(Path removed) {
		/*
		 * This function performs a good number of housekeeping. 1. Accurately
		 * keep track of the portions of the fragments that are masked. 2.
		 * Create a trimmed vertex by concatenating the leftover part of the
		 * fragments so that it can be used to run blast incrementally.
		 * According to our discussion if there n fragments in the path remove
		 * all of them completely except the last and fast one. For those remove
		 * only the part that is aligned.
		 * 
		 */
		this.totalRemoved += removed.pathVertex.size();
		int rem = this.topGraph.highestFragIdNum - totalRemoved;
		// System.out.println("Remaining Fragments: " + rem);

		Vector<Vertex> localRemovedOri = removed.pathVertex;

		// Modified by Nirmalya
		Vector<Vertex> localRemoved = collapsePath(localRemovedOri);
		
		boolean writeFlag = true;
		
		if(1 == localRemoved.size())
		{
			int totalAlignLength = (int)Math.abs(localRemoved.get(0).repeatAlignedEnd - localRemoved.get(0).repeatAlignedStart);
			if(totalAlignLength < 50)
				writeFlag = false;
		}

		for (int i = 0; i < localRemoved.size(); i++) {
			Vertex localVertex = localRemoved.get(i);

			int fragClass = 0;

			if (1 == localRemoved.size()) {
				/*
				 * Only one fragment is involved in the path and so this we
				 * define as class 5.
				 */
				fragClass = 4;

			} else if (0 == i) {
				/*
				 * There is at least two vertex in the path and this is the left
				 * of them.
				 */
				fragClass = 5;
			} else if (i == localRemoved.size() - 1) {
				/*
				 * There is at least two vertex in the path and this is the
				 * right of them
				 */
				fragClass = 6;
			} else {
				/*
				 * There is at least three vertex in the path and this kind is
				 * the middle, neither left nor right.
				 */

				fragClass = 7;
			}

			// This function takes care of all the part that needs to be masked.
			if(true == writeFlag)
			{
				localVertex.trimVertex(fragClass);
			}

		}

		// Once the masking is done an important job is to
		// trim the left side leftover of leftmost vertex and
		// also the right side leftover of the rightmost vertex

		// Leftmost vertex the 0th vertex

		TreeMap<Integer, FragmentPart> leftSide = localRemovedOri.get(0)
				.trimLeftSide();

		// Rightmost vertex is the size -1 vertex

		TreeMap<Integer, FragmentPart> rightSide = localRemovedOri.get(
				localRemoved.size() - 1).trimRightSide();

		TreeMap<Integer, FragmentPart> combined = CombineFragParts(leftSide,
				rightSide);

		return combined;
	}

	private Vector<Vertex> collapsePath(Vector<Vertex> localRemovedOri) {
		// This method collapses all the vertex in a single fragment
		// to a single vertex.

		int count = 0;
		Vector<Vertex> collapsed = new Vector<Vertex>();
		for (int i = 0; i < localRemovedOri.size(); i++) {
			Vertex localVertex = localRemovedOri.get(i);

			if (i == 0) {
				// This is the first vertex
				Vertex tempVertex = new Vertex(localVertex.fragmentId,
						localVertex.repeatId, localVertex.fragmentLength,
						localVertex.fragmentAlignedStart,
						localVertex.fragmentAlignedEnd,
						localVertex.repeatAlignedStart,
						localVertex.repeatAlignedEnd, localVertex.fragParts,
						localVertex.topGraph);
				tempVertex.straightOriented = localVertex.straightOriented;
				// Insert it into the collapsed
				collapsed.add(count, tempVertex);
			} else {
				Vertex lastVertex = localRemovedOri.get(i - 1);
				if (true == localVertex.fragmentId
						.equals(lastVertex.fragmentId)) {
					collapsed.get(count).fragmentAlignedEnd = localVertex.fragmentAlignedEnd;
					collapsed.get(count).repeatAlignedEnd = localVertex.repeatAlignedEnd;
				} else {
					count++;
					Vertex tempVertex = new Vertex(localVertex.fragmentId,
							localVertex.repeatId, localVertex.fragmentLength,
							localVertex.fragmentAlignedStart,
							localVertex.fragmentAlignedEnd,
							localVertex.repeatAlignedStart,
							localVertex.repeatAlignedEnd,
							localVertex.fragParts, localVertex.topGraph);
					// Insert it into the collapsed
					tempVertex.straightOriented = localVertex.straightOriented;
					collapsed.add(count, tempVertex);

				}
			}

		}

		return collapsed;
	}

	private TreeMap<Integer, FragmentPart> CombineFragParts(
			TreeMap<Integer, FragmentPart> leftSide,
			TreeMap<Integer, FragmentPart> rightSide) {

		/*
		 * Do some pre-processing to check if one or both of the arguments do
		 * not contain any valid fragments.
		 */
		if (null == leftSide && null == rightSide) {
			return null;
		} else if (null == leftSide) {
			return rightSide;
		} else if (null == rightSide) {
			return leftSide;
		} else {
			/*
			 * When both the arguments contain valid fragments merge them.
			 * 
			 */

			FragmentPart leftHighest = leftSide.get(leftSide.lastKey());
			int rightLimitOnLeft = leftHighest.endPosOnThis;

			Set<Integer> keySet = rightSide.keySet();
			Iterator<Integer> it = keySet.iterator();

			while (true == it.hasNext()) {
				int nextIt = it.next();
				FragmentPart localFragPart = rightSide.get(nextIt);
				/*
				 * Change startPosOnThis and endPosOnThis because the node will
				 * be added to the leftside.
				 */
				localFragPart.startPosOnThis += rightLimitOnLeft;
				localFragPart.endPosOnThis += rightLimitOnLeft;
				/*
				 * Add the node to the leftSide
				 */
				leftSide.put(localFragPart.startPosOnThis, localFragPart);
			}
		}
		return leftSide;
	}

	private Vector<Vertex> RunBlast(String fragmentFile, String repeatLibrary,
			int mode, TreeMap<Integer, FragmentPart> fragParts2,
			int localFragLength2) {

		HashMap<String, Integer> countMap = new HashMap<String, Integer>();
		// Run the blast for the fragmentFile and repeatLibrary
		String blastOutFile = null;
		if (0 == mode)
			blastOutFile = Path + "/blastOutFile.txt";
		else
			blastOutFile = Path + "/blastOutFile.txt";
		RunBlastScript(fragmentFile, repeatLibrary, blastOutFile, mode);

		Vector<Vertex> alignedVertex = new Vector<Vertex>();
		// Keep a local map for the alignment
		HashSet<String> localAlignment = new HashSet<String>();
		// Parse the blast output file
		BufferedReader blastOut = null;
		try {
			blastOut = new BufferedReader(new FileReader(blastOutFile));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int count = 0;

		String regex = "^(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)"
				+ "\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)";
		Pattern pat = null;

		pat = Pattern.compile(regex);
		Matcher mat = null;
		String line = null;
		int count1 =0;

		try {
			while (null != (line = blastOut.readLine())) {
				// Skip the comment lines
				if (true == line.startsWith("#"))
					continue;
				mat = pat.matcher(line);
				// Now for each of the alignment create a new vertex
				// Check if there is a new vertex
				if (true == mat.find()) {

					/**
					 * Now we are allowing all the matches that blast can find.
					 * So we are removing this checking.
					 */

					// if (true == localAlignment.contains(localVertexKey))
					// continue;
					// We have to check if this is reverse complement matching
					// In that case we shall skip it.
					int queryStart = Integer.parseInt(mat.group(7));
					int queryEnd = Integer.parseInt(mat.group(8));
					int subjectStart = Integer.parseInt(mat.group(9));
					int subjectEnd = Integer.parseInt(mat.group(10));
					int alignmentLength = Integer.parseInt(mat.group(4));
					if(alignmentLength < alignLimit)
					{
						continue;
					}

					String localKey = mat.group(1) + mat.group(2);
					if (true == countMap.containsKey(localKey)) {

						int val = countMap.get(localKey);
						if (val > 25)
							continue;
						countMap.put(localKey, val + 1);

					} else {
						int val = 0;
						countMap.put(localKey, val);
					}

					// if (queryStart > queryEnd || subjectStart > subjectEnd)
					// continue;

					// Now we are also allowing the case where subjectStart >
					// subjectEnd,
					// but not allowing the case queryStart > queryEnd.

					if (queryStart > queryEnd)
						continue;

					boolean straightOriented = true;
					if (subjectStart > subjectEnd)
						straightOriented = false;

					// Otherwise this is not reverse complement matching
					// Create a vertex with the required information

					// Pass most of the important information through the
					// constructor

					String fragmentId = mat.group(1);
					String repeatId = mat.group(2);
					double identity = Double.parseDouble(mat.group(3));
					int misMatches = Integer.parseInt(mat.group(5));
					int gapOpenings = Integer.parseInt(mat.group(6));

					double eValue = Double.parseDouble(mat.group(11));
					double bitScore = Double.parseDouble(mat.group(12));

					// Here we also have to create a fragParts data structure
					// and have to pass it to the vertex, So that it can
					// assign it to its own data structure. If the mode is 0
					// i.e. this is the first time the vertex is created
					// We need to create the fragmentPart. Or if the mode is
					// 1 we simply needs to use the fragParts2 argument.

					TreeMap<Integer, FragmentPart> fragParts = null;
					int localFragLength = 0;

					if (0 == mode) {
						fragParts = new TreeMap<Integer, FragmentPart>();

						// Create one FragmentPart and insert into this treemap
						Fragment localFrag = fragmentMap.get(fragmentId);
						int fragLength = localFrag.fragmentLength;
						String thatFragId = fragmentId;
						int startPosOnThat = 1;
						int endPosOnThat = fragLength;
						int startPosOnThis = 1;
						int endPosOnThis = fragLength;

						FragmentPart firstFragmentpart = new FragmentPart(
								thatFragId, startPosOnThat, endPosOnThat,
								startPosOnThis, endPosOnThis);

						fragParts.put(startPosOnThis, firstFragmentpart);

						localFragLength = localFrag.fragmentLength;
					} else if (1 == mode) {
						fragParts = fragParts2;
						// Here we have to find some way to get the value of the
						// virtual fragment.
						localFragLength = localFragLength2;
					} else
						;

					// Now create a local Vertex
					Vertex localVertex = new Vertex(fragmentId, repeatId,
							identity, alignmentLength, misMatches, gapOpenings,
							queryStart, queryEnd, subjectStart, subjectEnd,
							eValue, bitScore, straightOriented, fragParts);
					// Also add the other possible information into this vector
					// Add the local vertex to the alignment vector
					localVertex.topGraph = this.topGraph;

					// Assign the Repeat and Fragment object to this vertex
					int repeatLength = repeatMap.get(repeatId).repeatLen;
					localVertex.setRepeatLength(repeatLength);

					localVertex.setFragmentLength(localFragLength);
					// localVertex.fragmentLength = localFragLength;
					count++;
					localVertex.setAlignmentData();
					alignedVertex.add(localVertex);
					if (0 == mode) {
						count1++;
                                                if(0 == count1%10000)
						{
							System.out.println("Count1: " + count1 + " Adding one Vertex: " +
								localVertex.fragmentId);
						}
					}

				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Return all the aligned vertices.
		return alignedVertex;
	}

	/*
	 * This function will create the script the run blast completely with the
	 * FragmentFile and RepeatLibrary. Once that is done, it will return the
	 * name of the file that contains the result of the output.
	 */
	private void RunBlastScript(String fragmentFile, String repeatLibrary,
			String outFile, int mode) {
		// Create a local file containing the output of blast
		// If this doesn't work in case we have to use some other way
		// like writing in in a script and then running; we shall look into this
		// issue
		// during

		/* For this testing */
		// boolean flag = true;
		// if(0 == mode)
		// return;
		/* For this testing */

		String execString = blastpath + " -p blastn -q -1  -d " + repeatLibrary
				+ " -i " + fragmentFile + " -m 8 -o " + outFile;

		Process proc = null;
		// Delete if there is any previous instance of the outFile

		try {
			proc = Runtime.getRuntime().exec(execString);
			InputStream stderr = proc.getErrorStream();
			InputStreamReader isr = new InputStreamReader(stderr);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			// System.out.println("<ERROR>");
			while ((line = br.readLine()) != null)
				System.out.println(line);
			// System.out.println("</ERROR>");
			int exitVal = proc.waitFor();
			// System.out.println("Process exitValue: " + exitVal);

		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			// System.out.println("Here");
			proc.waitFor();
			// System.out.println("Here Again");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/*
	 * This function reads the length and starting, ending position on the
	 * chromosome.
	 */
	public void ReadFragments(String FragmentFile) {
		BufferedReader fragFile = null;
		try {
			fragFile = new BufferedReader(new FileReader(FragmentFile));
		} catch (FileNotFoundException e) {

			e.printStackTrace();
		}

		String regex = null;
		Pattern pat = null;
		Matcher mat = null;
		String line = null;

		regex = ">(\\S+) startPos (\\S+) length (\\S+)";
		pat = Pattern.compile(regex);
		String fragId = null;

		try {
			while (null != (line = fragFile.readLine())) {
				mat = pat.matcher(line);
				if (true == mat.find()) {
					fragId = mat.group(1);
					int startPos = Integer.parseInt(mat.group(2));
					int length = Integer.parseInt(mat.group(3));
					if (false == fragmentMap.containsKey(fragId)) {
						Fragment fragLocal = new Fragment(fragId, startPos,
								length);
						fragmentMap.put(fragId, fragLocal);
					}

				} else {
					if (null != fragId && 0 < line.length()) {
						// read also the fragment sequences into the
						// fragmentMap structure.
						// System.out.println(line);
						String modified = fragmentMap.get(fragId).fragmentStr
								.concat(line);
						// System.out.println("Modified: " + modified);
						fragmentMap.get(fragId).fragmentStr = modified;
						// String localFragStr = fragLocal.fragmentStr;
						// if(null ==localFragStr)
						// localFragStr = localFragStr.concat(line);
					}

				}

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/*
		 * Now the fragId contains the biggest id number. Store it, it will be
		 * useful to create the temporary fragments.
		 */
		regex = "(\\d+)";
		pat = Pattern.compile(regex);
		mat = pat.matcher(fragId);
		if (true == mat.find()) {
			topGraph.highestFragIdNum = Integer.parseInt(mat.group(1));
		}
	}

	/*
	 * This function will calculate the length of the repeats from the repeat
	 * library.
	 */
	public void ReadRepeats(String RepeatLibrary) {
		BufferedReader repLib = null;
		try {
			repLib = new BufferedReader(new FileReader(RepeatLibrary));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		String regex = null;
		Pattern pat = null;
		Matcher mat = null;
		String line = null;

		regex = "^>(\\S+)";
		pat = Pattern.compile(regex);
		int repeatLength = 0;
		boolean startedFlag = false;
		String repeatId = null;

		try {
			while (null != (line = repLib.readLine())) {
				mat = pat.matcher(line);
				if (true == mat.find()) {
					if (true == startedFlag) {
						if (false == repeatMap.containsKey(repeatId)) {
							Repeat localRep = new Repeat(repeatId, repeatLength);
							repeatMap.put(repeatId, localRep);
						}

					}

					// Set the variables
					repeatId = mat.group(1);
					repeatLength = 0;
					if (false == startedFlag) {
						startedFlag = true;
					}

				} else {
					// If the length of the line is more than zero
					// calculate the length of the line
					repeatLength += line.length();
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// For the last one
		if (true == startedFlag) {
			if (false == repeatMap.containsKey(repeatId)) {
				Repeat localRep = new Repeat(repeatId, repeatLength);
				repeatMap.put(repeatId, localRep);
			}

		}

	}

	HashMap<String, Repeat> getRepeatMap() {
		return repeatMap;
	}

	HashMap<String, Fragment> getFragmentMap() {
		return fragmentMap;
	}

    int alignLimit =0;
	String Path = null;
	int totalRemoved = 0;
	String blastpath = null;
	Graphs topGraph = null;
	HashMap<String, Repeat> repeatMap = null;
	HashMap<String, Fragment> fragmentMap = null;

}

class Repeat {
	public Repeat(String repeatId2, int repeatLength) {
		RepeatId = repeatId2;
		repeatLen = repeatLength;
	}

	// Name of the repeat
	String RepeatId;
	// Length of the repeat
	int repeatLen;
}

class Fragment {
	public Fragment(String fragId, int startPos, int length) {
		// TODO Auto-generated constructor stub
		this.hashedParts = new TreeMap<Integer, HashedPartInfo>();
		this.fragmentStr = new String();
		this.fragmentId = new String();
		// Assignment
		this.fragmentId = fragId;
		this.fragmentStart = startPos;
		this.fragmentLength = length;

	}

	public void mergeParts() {
		TreeMap<Integer, HashedPartInfo> localMap = new TreeMap<Integer, HashedPartInfo>();

		Set<Integer> keySet = hashedParts.keySet();
		Iterator<Integer> it = keySet.iterator();

		int localStart = -1;
		int localEnd = -1;
		int repeatStart;
		int repeatEnd;
		while (true == it.hasNext()) {
			int startPos = it.next();
			int endPos = hashedParts.get(startPos).endPos;

			if (-1 == localStart && -1 == localEnd) {
				localStart = startPos;
				localEnd = endPos;

				// Insert it into localMap

				// Create a local hashed
				String repeatStr = hashedParts.get(localStart).repeatStr;
				repeatStart = hashedParts.get(localStart).repeatStart;
				repeatEnd = hashedParts.get(localStart).repeatEnd;
				boolean straightOriented = hashedParts.get(localStart).straighOriented;
				HashedPartInfo localHashed = new HashedPartInfo(localStart,
						localEnd, repeatStart, repeatEnd, repeatStr,
						straightOriented);
				localMap.put(localStart, localHashed);
			} else {
				if (startPos <= localEnd + 1) {
					if (endPos > localEnd) {
						// remove the one from localMap
						localMap.remove(localStart);
						localEnd = endPos;

						String repeatStr = hashedParts.get(localStart).repeatStr;
						repeatStart = hashedParts.get(localStart).repeatStart;
						repeatEnd = hashedParts.get(localStart).repeatEnd;
						boolean straightOriented = hashedParts.get(localStart).straighOriented;
						HashedPartInfo localHashed = new HashedPartInfo(
								localStart, localEnd, repeatStart, repeatEnd,
								repeatStr, straightOriented);
						localMap.put(localStart, localHashed);

					}

				} else {
					// i.e. this one does not overlap with the last part
					localStart = startPos;
					localEnd = endPos;
					String repeatStr = hashedParts.get(localStart).repeatStr;
					boolean straightOriented = hashedParts.get(localStart).straighOriented;
					repeatStart = hashedParts.get(localStart).repeatStart;
					repeatEnd = hashedParts.get(localStart).repeatEnd;
					HashedPartInfo localHashed = new HashedPartInfo(localStart,
							localEnd, repeatStart, repeatEnd, repeatStr,
							straightOriented);
					localMap.put(localStart, localHashed);

				}
			}
		}

		hashedParts = localMap;

	}

	public void putHashedParts(int startPosOnThat, int endPosOnThat,
			int repeatStart, int repeatEnd, String repeatStr,
			boolean straightOriented) {
		/*
		 * Put the startPosOnThat and endPosOnThat in hashedParts, but before
		 * that make sure to marge it with conflicting parts.
		 */
		if (true == hashedParts.containsKey(startPosOnThat)) {
			HashedPartInfo local = hashedParts.get(startPosOnThat);
			int endPos = local.endPos;
			if (endPos <= endPosOnThat) {
				hashedParts.remove(startPosOnThat);

			} else {
				return;
			}
		}
		HashedPartInfo temp = new HashedPartInfo(startPosOnThat, endPosOnThat,
				repeatStart, repeatEnd, repeatStr, straightOriented);
		hashedParts.put(startPosOnThat, temp);

	}

	// Name of the fragment
	String fragmentId;
	// Starting position on the chromosome
	int fragmentStart;
	// Length of the fragment
	int fragmentLength;

	// We shall also store the fragment itself as it might be required.
	// One advantage is that the size of the fragments will not be too much.
	String fragmentStr = null;

	// We can create a TreeMap that stores the hashed out parts
	// For detailed information printing we have to keep track of some
	// more information
	TreeMap<Integer, HashedPartInfo> hashedParts = null;

}

class HashedPartInfo {
	public HashedPartInfo(int startPosOnThat, int endPosOnThat,
			int repeatStart, int repeatEnd, String repeatStr2,
			boolean straightOriented) {
		this.startPos = startPosOnThat;
		this.endPos = endPosOnThat;
		this.repeatStart = repeatStart;
		this.repeatEnd = repeatEnd;
		this.repeatStr = repeatStr2;
		this.straighOriented = straightOriented;
	}

	int startPos;
	int endPos;
	int repeatStart;
	int repeatEnd;
	String repeatStr;
	boolean straighOriented;
}
