package ehb.instrumentation.codecoverage;

public class CoverageGlobals {
	public static int classIndex = 0;
	public static int methodIndex = 0;
	public static int branchIndex = 0;

	public static void reset(){
		classIndex++;
		methodIndex = 0;
		branchIndex = 0;
	}
}
