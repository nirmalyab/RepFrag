import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class FragmentClass {
	
	public FragmentClass(int fragId, int start2, int end2) {
		fragmentNum = fragId;
		start = start2;
		end = end2;
		length = end -length + 1;
		transposons = new TreeMap<Integer, Integer>();
	}
	
	
	int getStart()
	{
		return start;
	}
	
	int getEnd()
	{
		return end;
	}
	
	int getLength()
	{
		return length;
	}

	public void compareTransposon(int start2, int end2) {
		/* Check if the transposon is within the boundary of this fragment */
		if(start2 > end || start > end2)
		{
			return;
		}
		else
		{
			start2 = start>start2 ? start : start2;
			end2 = end < end2 ? end : end2;
		}
		
		if(false ==transposons.containsKey(start2))
		{
			transposons.put(start2, end2);
			//System.out.println("From IF loop: ");
			//System.out.println(start2 + " " + end2);
		}
		else
		{
			int localEnd = transposons.get(start2);
			if(end2 > localEnd)
			{
				//Replace the old one with the new one
				transposons.remove(start2);
				//Add the new one
				transposons.put(start2, end2);
				//System.out.println("From ELSE loop: ");
				//System.out.println(start2 + " " + end2);
			}
		}
			
	}
	
	public void mergeTransposon()
	{
		//Traverse the elements of treemap "transposons" 
		Set<Integer> keys = transposons.keySet();
		if(1 >= keys.size())
			return;
		Iterator<Integer> it = keys.iterator();
		
		TreeMap<Integer, Integer> newtrans = new TreeMap<Integer, Integer>();
		int lastStart = -1;
		int lastEnd = -1;
		
		//System.out.println("******");
		while(true == it.hasNext())
		{
			int nextStart = it.next();
			//System.out.println("Next Start: " + nextStart);
			int nextEnd = transposons.get(nextStart);
			if(lastStart == -1 && lastEnd == -1)
			{
				lastStart = nextStart;
				lastEnd = nextEnd;
				newtrans.put(lastStart, lastEnd);
			}
			else
			{
				if(lastEnd >= nextStart -1)
				{
					//remove the last one
					newtrans.remove(lastStart);
					lastEnd = (nextEnd > lastEnd)? nextEnd: lastEnd;
					newtrans.put(lastStart, lastEnd);
					//System.out.println("hi");
				}
				else
				{
					lastStart = nextStart;
					lastEnd = nextEnd;
					newtrans.put(lastStart, lastEnd);
					
				}
			}
			
		}
		//Now assign the new - transposns to the old one.
		transposons = newtrans;
	}
	
	int getMaskCount()
	{
		//Traverse the elements of treemap "transposons" 
		Set<Integer> keys = transposons.keySet();
		Iterator<Integer> it = keys.iterator();
		
		int count =0;
		while(true == it.hasNext())
		{
			int localStart = it.next();
			int localEnd = transposons.get(localStart);
			int incre = localEnd - localStart + 1;
			count += incre;
		}
		return count;
	}
	
	static int getCommon(FragmentClass first, FragmentClass second)
	{
		// This will create do a paitwise comparison
		
		int localFragid = -1;
		int localStartPos = first.getStart();
		int localEndPos = first.getEnd();
		FragmentClass localFrag = new FragmentClass(localFragid, localStartPos,localEndPos );

		TreeMap<Integer, Integer> transposonFirst = first.transposons;
		Set<Integer> keysFirst = transposonFirst.keySet();
		Iterator<Integer> itFirst = keysFirst.iterator();
		
		while(true == itFirst.hasNext())
		{
			int firstStart = itFirst.next();
			int firstEnd = transposonFirst.get(firstStart);
			
			// Get the iterator for the second one.						
			TreeMap<Integer, Integer> transposonSecond = second.transposons;
			Set<Integer> keysSecond =transposonSecond.keySet();
			Iterator<Integer> itSecond = keysSecond.iterator();

			while(true == itSecond.hasNext())
			{
				int secStart = itSecond.next();
				int secEnd = transposonSecond.get(secStart);
				
				//Count the start and end of the overlap in between them
				
				if(secStart > firstEnd || firstStart > secEnd)
				{
					//No overlap
					continue;
				}
				else
				{
					int overStart = (firstStart > secStart) ? firstStart : secStart;
					int overEnd = (firstEnd < secEnd) ? firstEnd : secEnd;
					localFrag.compareTransposon(overStart, overEnd);
				}
				
				//Try to insert it into a commonFragment
				
				
			}
		}
		
		localFrag.mergeTransposon();
		
		int maskCount = localFrag.getMaskCount();
		
		//Here the maskCount is the true positive
		return maskCount;
			
	}
	
	/* Data fields */
	int fragmentNum;
	int start;
	int end;
	int length;
	TreeMap<Integer, Integer> transposons = null;
	

}
