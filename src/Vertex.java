import java.util.*;

public class Vertex {

	// As we now allow more than one vertices from
	// a fragment, the identity of a vertex is determined by
	// all six of these variables.

	String fragmentId;
	String repeatId;
	String vertexId;
	int fragmentAlignedStart;
	int fragmentAlignedEnd;
	int repeatAlignedStart;
	int repeatAlignedEnd;

	// Whether the repeat is aligned in a straight way or reverse way;
	boolean straightOriented;

	double identity;
	int alignmentLength;
	int misMatches;
	int gapOpenings;

	double eValue;
	double bitScore;

	// Length of the repeat for this vertex
	int repeatLength;

	// length of the virtual fragment
	// When a vertex is contained with a single fragment this
	// contains the length of the fragment. Otherwise, the length
	// of the part of the fragments after stitching
	int fragmentLength;

	// Information about this vertex
	int fragmentUnalignedRight;
	int fragmentUnalignedLeft;
	int repeatUnalignedRight;
	int repeatUnalignedLeft;
	int identicalBasePairNumber;

	double fitnessValue;

	// Neighbours
	HashMap<String, Vertex> leftNeighbours = null;
	HashMap<String, Vertex> rightNeighbours = null;

	// Information on the parts of the fragments due to
	// stitching

	TreeMap<Integer, FragmentPart> fragParts = null;

	public Graphs topGraph = null;

	// Constructor overloading.

	public Vertex(String fragmentId, String repeatId, int fragLength,
			int queryStart, int queryEnd, int subjectStart, int subjectEnd,
			TreeMap<Integer, FragmentPart> fragParts2, Graphs topGraph) {

		this.fragmentId = fragmentId;
		this.repeatId = repeatId;
		this.fragmentLength = fragLength;
		this.fragmentAlignedStart = queryStart;
		this.fragmentAlignedEnd = queryEnd;
		this.repeatAlignedStart = subjectStart;
		this.repeatAlignedEnd = subjectEnd;
		this.fragParts = fragParts2;
		this.topGraph = topGraph;

	}

	public Vertex(String fragmentId, String repeatId, double identity,
			int alignmentLength, int misMatches, int gapOpenings,
			int queryStart, int queryEnd, int subjectStart, int subjectEnd,
			double value, double bitScore, boolean straightOriented,
			TreeMap<Integer, FragmentPart> fragParts2) {

		// Now initialize all the elements
		this.fragmentId = fragmentId;
		this.repeatId = repeatId;
		this.identity = identity;
		this.alignmentLength = alignmentLength;
		this.misMatches = misMatches;
		this.gapOpenings = gapOpenings;
		this.fragmentAlignedStart = queryStart;
		this.fragmentAlignedEnd = queryEnd;
		this.repeatAlignedStart = subjectStart;
		this.repeatAlignedEnd = subjectEnd;
		this.eValue = value;
		this.bitScore = bitScore;

		// Set the orientation: very important
		this.straightOriented = straightOriented;

		this.leftNeighbours = new HashMap<String, Vertex>();
		this.rightNeighbours = new HashMap<String, Vertex>();

		// Assign the fragParts from the command line
		this.fragParts = fragParts2;

		// Create the vertex Id. This is important
		this.vertexId = this.fragmentId + "_" + this.repeatAlignedStart + "_"
				+ this.repeatAlignedEnd + "_" + this.fragmentAlignedStart + "_"
				+ this.fragmentAlignedEnd;
		// System.out.println("VertexId: " + this.vertexId + " orientation: " +
		// this.straightOriented);

	}

	public String getRepeatId() {
		// TODO Auto-generated method stub
		return repeatId;
	}

	public void setAlignmentData() {
		/*
		 * fragmentUnalignedRight : The number of unaligned based pairs on the
		 * right side of the fragment.
		 * 
		 * fragmentUnalignedLeft : The number of unaligned based pairs on the
		 * left side of the fragment.
		 * 
		 * repeatUnalignedRight : The number of unaligned base pairs on the
		 * right side of the repeat.
		 * 
		 * repeatUnalignedLeft: The number of unaligned base pairs on the left
		 * side of the repeat.
		 */

		fragmentUnalignedRight = fragmentLength - fragmentAlignedEnd;
		fragmentUnalignedLeft = fragmentAlignedStart - 1;

		if (true == this.straightOriented) {
			repeatUnalignedRight = repeatLength - repeatAlignedEnd;
			repeatUnalignedLeft = repeatAlignedStart - 1;
		} else {
			repeatUnalignedRight = repeatAlignedEnd - 1;
			repeatUnalignedLeft = repeatLength - repeatAlignedStart;
		}

		identicalBasePairNumber = (int) (alignmentLength * (identity / 100.0));
	}

	public double calculateFitnessValue() {
		/*
		 * fragmentUnalignedRight : The number of unaligned based pairs on the
		 * right side of the fragment.
		 * 
		 * fragmentUnalignedLeft : The number of unaligned based pairs on the
		 * left side of the fragment.
		 * 
		 * repeatUnalignedRight : The number of unaligned base pairs on the
		 * right side of the repeat.
		 * 
		 * repeatUnalignedLeft: The number of unaligned base pairs on the left
		 * side of the repeat.
		 */

		double penaltyZoneLeft = 0.0;

		if (fragmentUnalignedLeft < repeatUnalignedLeft)
			penaltyZoneLeft = fragmentUnalignedLeft;
		else
			penaltyZoneLeft = repeatUnalignedLeft;

		double penaltyZoneRight = 0.0;

		if (fragmentUnalignedRight < repeatUnalignedRight)
			penaltyZoneRight = fragmentUnalignedRight;
		else
			penaltyZoneRight = repeatUnalignedRight;

		double penaltyZone = penaltyZoneLeft + penaltyZoneRight;
		double probabilityValue = 0.25;

		fitnessValue = (identicalBasePairNumber + probabilityValue
				* penaltyZone)
				/ (alignmentLength + penaltyZone);
		if(true == Double.isNaN(fitnessValue))
		{
			System.out.printf("NaN fitnessValue from: Vertex.calculateFitnessValue()");
			System.exit(1);
		}
		return fitnessValue;

	}

	public void setRepeatLength(int repeatLength) {
		this.repeatLength = repeatLength;

	}

	public void trimVertex(int fragClass) {

		/*
		 * start and end denotes the staring and ending position to be hashed on
		 * this fragment.
		 */

		int start = 0;
		int end = 0;

		if (4 == fragClass) {
			start = this.fragmentAlignedStart;
			end = this.fragmentAlignedEnd;
		} else if (5 == fragClass) {
			start = this.fragmentAlignedStart;
			end = this.fragmentLength;
		} else if (6 == fragClass) {
			start = 1;
			end = this.fragmentAlignedEnd;
		} else if (7 == fragClass) {
			start = 1;
			end = this.fragmentLength;
		}

		/*
		 * Now the interesting part starts. As a fragment may consists of
		 * multiple virtual sub fragment, we shall use the structure fragParts
		 * in stead of the fragment itself. This part can be called as the
		 * masking part on the virtual fragments.
		 */
		Set<Integer> keySet = fragParts.keySet();
		Iterator<Integer> it = keySet.iterator();

		while (true == it.hasNext()) {
			int nextKey = it.next();

			FragmentPart localFragPart = fragParts.get(nextKey);

			/*
			 * Check if the start is between the startPosonThis and endPosOnThis
			 */

			int startPosOnThis = localFragPart.startPosOnThis;
			int endPosOnThis = localFragPart.endPosOnThis;
			int startPosOnThat = localFragPart.startPosOnThat;
			int endPosOnThat = localFragPart.endPosOnThat;
			String thatFragId = localFragPart.thatFragId;

			int finalStart = 0;
			int finalEnd = 0;

			if (startPosOnThis >= start) {
				if (startPosOnThis > end) {
					continue;
				} else if (endPosOnThis <= end) {
					// This is the most simple case
					// finalStart = startPosOnThis;
					// finalEnd = endPosOnThis;
					finalStart = startPosOnThat;
					finalEnd = endPosOnThat;
				} else if (endPosOnThis > end) {
					// finalStart = startPosOnThis;
					// finalEnd = end;
					finalStart = startPosOnThat;
					finalEnd = endPosOnThat - (endPosOnThis - end);
				}
			} else if (startPosOnThis < start) {
				if (endPosOnThis < start) {
					continue;
				} else if (end < endPosOnThis) {
					// finalStart = start;
					// finalEnd = end;
					finalStart = startPosOnThat + (start - startPosOnThis);
					finalEnd = endPosOnThat - (endPosOnThis - end);
				} else if (end >= endPosOnThis) {
					// finalStart = start;
					// finalEnd = endPosOnThis;
					finalStart = startPosOnThat + (start - startPosOnThis);
					finalEnd = endPosOnThat;
				}
			}

			System.out.println("Vertex is being trimmed. Orientation: "
					+ this.straightOriented);
			

			Fragment localFrag = topGraph.rf.fragmentMap.get(thatFragId);
			localFrag.putHashedParts(finalStart, finalEnd,
					this.repeatAlignedStart, this.repeatAlignedEnd,
					this.repeatId, this.straightOriented);
			// localFrag.hashedParts.put(startPosOnThat, endPosOnThat);

		}

	}

	// The last two parts are somehow complex; we need to be careful during
	// debugging;

	public TreeMap<Integer, FragmentPart> trimLeftSide() {

		TreeMap<Integer, FragmentPart> localFragPartMap = new TreeMap<Integer, FragmentPart>();
		int rightLimitOnThis = this.fragmentAlignedStart - 1;

		if (0 == rightLimitOnThis)
			return null;

		Set<Integer> keys = fragParts.keySet();
		Iterator<Integer> it = keys.iterator();

		while (true == it.hasNext()) {
			int next = it.next();
			FragmentPart tempFragPart = fragParts.get(next);
			FragmentPart localFragPart = new FragmentPart(
					tempFragPart.thatFragId, tempFragPart.startPosOnThat,
					tempFragPart.endPosOnThat, tempFragPart.startPosOnThis,
					tempFragPart.endPosOnThis);

			if (localFragPart.endPosOnThis <= rightLimitOnThis) {
				localFragPartMap.put(localFragPart.startPosOnThis,
						localFragPart);
			} else {
				if (localFragPart.startPosOnThis <= rightLimitOnThis) {
					int decreased = localFragPart.endPosOnThis
							- rightLimitOnThis;
					localFragPart.endPosOnThis = rightLimitOnThis;
					localFragPart.endPosOnThat -= decreased;
					localFragPartMap.put(localFragPart.startPosOnThis,
							localFragPart);
				}

				break;

			}

		}
		return localFragPartMap;

	}

	public TreeMap<Integer, FragmentPart> trimRightSide() {

		TreeMap<Integer, FragmentPart> localFragPartMap = new TreeMap<Integer, FragmentPart>();

		int leftLimitOnThis = this.fragmentAlignedEnd + 1;

		if (leftLimitOnThis == this.fragmentLength + 1) {
			return null;
		}

		Set<Integer> keys = fragParts.keySet();

		Iterator<Integer> it = keys.iterator();

		while (true == it.hasNext()) {
			int next = it.next();
			FragmentPart tempFragPart = fragParts.get(next);
			FragmentPart localFragPart = new FragmentPart(
					tempFragPart.thatFragId, tempFragPart.startPosOnThat,
					tempFragPart.endPosOnThat, tempFragPart.startPosOnThis,
					tempFragPart.endPosOnThis);

			if (localFragPart.endPosOnThis < leftLimitOnThis) {
				continue;
			} else {
				if (localFragPart.startPosOnThis >= leftLimitOnThis) {
					localFragPart.startPosOnThis -= (leftLimitOnThis - 1);
					localFragPart.endPosOnThis -= (leftLimitOnThis - 1);
				} else {
					int decreased = leftLimitOnThis
							- localFragPart.startPosOnThis;
					localFragPart.startPosOnThis = 1;
					localFragPart.endPosOnThis -= (leftLimitOnThis - 1);
					localFragPart.startPosOnThat += decreased;

				}
				localFragPartMap.put(localFragPart.startPosOnThis,
						localFragPart);
			}

		}

		return localFragPartMap;

	}

	public void setFragmentLength(int localFragLength) {
		this.fragmentLength = localFragLength;

	}

}

class FragmentPart {

	// Information on that fragment

	String thatFragId;
	int startPosOnThat;
	int endPosOnThat;
	// int lengthOnThat;

	// Information on this fragment

	int startPosOnThis;
	int endPosOnThis;

	// int lengthOnThis;

	public FragmentPart(String thatFragId, int startPosOnThat,
			int endPosOnThat, int startPosOnThis, int endPosOnThis) {

		this.thatFragId = thatFragId;
		this.startPosOnThat = startPosOnThat;
		this.endPosOnThat = endPosOnThat;
		// this.lengthOnThat = endPosOnThat - startPosOnThat + 1;

		this.startPosOnThis = startPosOnThis;
		this.endPosOnThis = endPosOnThis;
		// this.lengthOnThis = endPosOnThis - startPosOnThis + 1;

	}

}
