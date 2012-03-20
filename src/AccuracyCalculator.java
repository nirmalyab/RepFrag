import java.util.*;
import java.io.*;
import java.util.regex.*;

/*
 * Now, this files will be integrated to that of the 
 * RepeatFinder. So we have to print those information in a file
 * from RepeatFinder that will be feed to this file.
 */

/* This class calculate the accuracy of cross_match and 
 * RepeatMasker. It takes formatted output of those two
 * software and calculate the true positive, false positive 
 * and false negative. The format of the entries in that file
 * will be as follows:
 * 
 */
public class AccuracyCalculator {

	/**
	 * 
	 * @param inFie1
	 *            This contains the path to the transposon and repeat file.
	 * @param inFile2
	 *            This contains the file contains the fragments and its
	 *            positions on the actual chromosome.
	 * @param inFile3
	 *            This contains the formatted output of either cross_match or
	 *            RepeatMasker.
	 * @param CHNum
	 *            The chromosome number.
	 */

	/* Contains a vector of the annotated fragments */
	HashMap<Integer, FragmentClass> annotatedFragments = null;

	/* Contains a map of the masked regions on the fragments */
	HashMap<Integer, FragmentClass> maskedFragments = null;

	void CalculateAccuracy(String inFile1, String inFile2, String inFile3,
			String AccuracyFile) {

		getAnnotatedRegions(inFile1, inFile2);
		getMaskedRegions(inFile2, inFile3);
		getStatistics(AccuracyFile);

	}

	void getStatistics(String AccuracyFile) {
		Set<Integer> annotatedKeys = annotatedFragments.keySet();
		Iterator<Integer> it = annotatedKeys.iterator();

		int truePositive = 0;
		int falsePositive = 0;
		int falseNegative = 0;

		while (true == it.hasNext()) {
			int id = it.next();

			FragmentClass anno = annotatedFragments.get(id);

			if (true == maskedFragments.containsKey(id)) {
				FragmentClass masked = maskedFragments.get(id);
				int commonCount = FragmentClass.getCommon(anno, masked);

				int countAnno = anno.getMaskCount();
				int countMasked = masked.getMaskCount();

				truePositive += commonCount;
				falsePositive += (countMasked - commonCount);
				falseNegative += (countAnno - commonCount);
			} else {
				int localCount = anno.getMaskCount();
				falseNegative += localCount;

			}
		}
		PrintWriter outFile = null;
		try {
			outFile = new PrintWriter(new FileWriter(AccuracyFile, true));
		} catch (Exception e) {
		}

		System.out.println("True Positive: " + truePositive);
		System.out.println("False Positive: " + falsePositive);
		System.out.println("False Negative: " + falseNegative);

		String outStr = truePositive + " " + falsePositive + " "
				+ falseNegative;
		outFile.println(outStr);

		try {
			outFile.close();
		} catch (Exception e) {
		}
	}

	void getMaskedRegions(String inFile2, String inFile3) {
		BufferedReader fragFile = null;
		BufferedReader maskedFile = null;
		// This is an important data structure, that shall be created
		// every time the function is invoked.
		
		maskedFragments = new HashMap<Integer, FragmentClass>();

		try {
			fragFile = new BufferedReader(new FileReader(inFile2));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// First read the fragment header lines from the fragFile.
		Vector<String> vec = new Vector<String>();

		String regex = null;
		Pattern pat = null;
		Matcher mat = null;
		String line = null;

		regex = "^>fragment(\\S+)\\s+\\S+\\s+(\\S+)\\s+\\S+\\s+(\\S+)";
		pat = Pattern.compile(regex);
		try {
			while (null != (line = fragFile.readLine())) {
				mat = pat.matcher(line);
				if (true == mat.find()) {
					vec.add(line);
				}

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			fragFile.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		// We can get the start and end of a fragment from a fragment
		// We shall use the start and end of a fragment as the key value
		// of the map locToNum

		regex = null;
		pat = null;
		mat = null;
		line = null;

		regex = "^>fragment(\\S+)\\s+\\S+\\s+(\\S+)\\s+\\S+\\s+(\\S+)";
		pat = Pattern.compile(regex);
		
		Iterator<String> localIt = vec.iterator();

		while (true == localIt.hasNext()) {
			line = localIt.next();
			mat = pat.matcher(line);
			if (true == mat.find()) {
				int fragId = Integer.parseInt(mat.group(1));
				int start = Integer.parseInt(mat.group(2));
				int length = Integer.parseInt(mat.group(3));
				int end = start + length - 1;

				// Create a FragmentClass object

				FragmentClass tempFrag = new FragmentClass(fragId, start,
						end);
				maskedFragments.put(fragId, tempFrag);

			}

		}
		
		// Now read the masked file and check all the repeats one by one against
		// all
		// the fragments.
		try {
			maskedFile = new BufferedReader(new FileReader(inFile3));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// regex = "^fragment(\\S+)\\s+(\\S+)\\s+(\\S+)";
		regex = "fragment(\\S+)\\s+(\\S+)\\s+(\\S+)";
		pat = Pattern.compile(regex);

		try {
			while (null != (line = maskedFile.readLine())) {
				mat = pat.matcher(line);
				if (true == mat.find()) {
					int fragId = Integer.parseInt(mat.group(1));
					int startPos = maskedFragments.get(fragId).getStart();
					int start = startPos + Integer.parseInt(mat.group(2)) - 1;
					int end = startPos + Integer.parseInt(mat.group(3)) - 1;

					FragmentClass temp = maskedFragments.get(fragId);
					temp.compareTransposon(start, end);
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			maskedFile.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Set<Integer> maskedKeys = maskedFragments.keySet();
		Iterator<Integer> it = maskedKeys.iterator();

		while (true == it.hasNext()) {
			int id = it.next();
			maskedFragments.get(id).mergeTransposon();
		}

	}

	void getAnnotatedRegions(String inFile1, String inFile2) {

		// This is a main data structure, that shall be created only once and used many.
		annotatedFragments = new HashMap<Integer, FragmentClass>();
		
		BufferedReader fragFile = null;

		try {

			fragFile = new BufferedReader(new FileReader(inFile2));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Read the file first in a vector with the appropriate regex operation
		Vector<String> vec = new Vector<String>();

		String regex = null;
		Pattern pat = null;
		Matcher mat = null;
		String line = null;

		regex = ">fragment(\\S+) startPos (\\S+) length (\\S+)";
		pat = Pattern.compile(regex);

		try {
			while (null != (line = fragFile.readLine())) {
				// System.out.println(line);
				mat = pat.matcher(line);
				if (true == mat.find()) {
					vec.add(line);

				}

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			fragFile.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		// We can get the start and end of a fragment from a fragment
		// We shall use the start and end of a fragment as the key value
		// of the map locToNum

		regex = null;
		pat = null;
		mat = null;
		line = null;
		
		Iterator<String> localIter = vec.iterator();

		regex = ">fragment(\\S+) startPos (\\S+) length (\\S+)";
		pat = Pattern.compile(regex);

		while (true == localIter.hasNext()) {
			line = localIter.next();
			// System.out.println(line);
			mat = pat.matcher(line);
			if (true == mat.find()) {
				// System.out.println("Matched");
				// System.out.println(mat.group(2));
				int fragId = Integer.parseInt(mat.group(1));
				int start = Integer.parseInt(mat.group(2));
				int length = Integer.parseInt(mat.group(3));
				int end = start + length - 1;

				// Create a FragmentClass object
				FragmentClass tempFrag = new FragmentClass(fragId, start,
						end);
				annotatedFragments.put(fragId, tempFrag);

			}

		}

		
		BufferedReader TPFile = null;

		try {
			TPFile = new BufferedReader(new FileReader(inFile1));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Now read the repeat file and check all the repeats one by one against
		// all
		// the fragments.

		regex = "^\\S+\\s+\\S+\\s+(\\S+)\\s+(\\S+)";
		pat = Pattern.compile(regex);

		try {
			while (null != (line = TPFile.readLine())) {
				mat = pat.matcher(line);
				if (true == mat.find()) {
					// System.out.println("size: " + annotatedFragments.size());
					int start = Integer.parseInt(mat.group(1));
					int end = Integer.parseInt(mat.group(2));

					Set<Integer> annoKeys = annotatedFragments.keySet();
					Iterator<Integer> it = annoKeys.iterator();

					while (true == it.hasNext()) {
						int id = it.next();
						FragmentClass temp = annotatedFragments.get(id);
						temp.compareTransposon(start, end);
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			TPFile.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Run the mergeTransposon() for

		Set<Integer> annoKeys = annotatedFragments.keySet();
		Iterator<Integer> it = annoKeys.iterator();

		while (true == it.hasNext()) {
			int id = it.next();
			annotatedFragments.get(id).mergeTransposon();
		}
	}

	
	public static void main(String[] args) {
		AccuracyCalculator ac = new AccuracyCalculator();
		ac.CalculateAccuracy(args[0], args[1], args[2], args[3]);
	}

}
