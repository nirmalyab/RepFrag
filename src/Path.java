import java.util.*;

public class Path {
	public Path(Vector<Vertex> pathVector, double pathFitnessValue, String repeatId) {
		this.fitnessVal = pathFitnessValue;
		this.pathVertex = pathVector;
		this.repeatId = repeatId;
		
		/* Initialize clean up data */
		//affectedMap = new HashMap<String, HashSet<String>>();
	}
	
	double fitnessVal;
	Vector<Vertex> pathVertex;
	String repeatId;
			
}
