package gov.nasa.jpf.symbc;

import java.io.File;
import org.junit.Test;


//NOTE: This testing file was scrapped after learning that threading within the JPF VM led to linking
//errors that I couldn't find any information on. Strategy for the class has changed to 
//Just using this class for the generation of testing files/is here for the documentation of the process I went through.

public class RegressionTest extends InvokeTest {

	//Creating alternative args like this and running them together in separated if statements might end up
	//working to keep the entire testing suite as just a simple Eclipse button press. Further testing
	//will need to happen for it.
	private static final String SYM_METHOD = "+symbolic.method=gov.nasa.jpf.symbc.RegressionTest.test(sym#sym)";
	//private static final String LISTENER = "+listener = gov.nasa.jpf.symbc.PathConditionListener";
	private static final String MIN = "+symbolic.min_int=-100";
	private static final String MAX = "+symbolic.max_int=100";
	private static final String DP = "+symbolic.dp=z3";
	//private static final String REPORT_CONSOLE = "+report.console.file=Test.jpf.report_new";
	//private static final String REPORT_CONSOLE_CLASS = "+report.console.class=gov.nasa.jpf.report.ConsolePublisher";
	private static final String[] JPF_ARGS = {INSN_FACTORY, SYM_METHOD, MIN, MAX, DP};//, REPORT_CONSOLE_CLASS};	

	public static void main(String[] args) {
		runTestsOfThisClass(args);
	}

	@Test
	public void mainTest() {
		if (verifyNoPropertyViolation(JPF_ARGS)) {
			test(1, 2);
		}
		verifyRegression("Test.jpf.report_new", "Test.jpf.report");
	}

	static public void test(int x, int y) {
		//This is the main method from test.jpf incorporated into the testing system.
		int z=x-y;

		if (x > y && y > 0) {
			if (z > 0) {
				System.out.println("z>0");
			} else {
				System.out.println("z<=0");
			}
		}
		//return Debug.PC4Z3();
	}
	
	//For just the testing file, it seems to run these statements 4 different times. Investigation into the
	//Way testing methods are handled by JPF would probably reveal that it happens for each of the given paths.
	//The created files might just be looking into the different z3 files. Either way, the testing suite works
	//for now. There could be larger problems here but the logic seems solid and it passes/fails when it needs
	//to do so. If needed, a simple change moving to a bash script where files are compared outside of the scope
	//of JPF can fix this if the repeated runs of this file end up being a problem. Since it just compares a
	//bunch of true outputs from SMTFileProcessor, it means that it's generating things properly even though the
	//It's happening 3 more times than expected, so there shouldn't be an issue with it. Either way, further
	//investigation will probably lead to good results with this.
	public void verifyRegression(String newFileName, String legacyFileName) {
		File newFile = new File(newFileName);
		File legacyFile = new File(legacyFileName);
		SMTFileProcessor proc = new SMTFileProcessor(newFile, legacyFile);
		boolean result = proc.runProcessor();
		//newFile.deleteOnExit(); //Clean up the newly generated file at the end of execution. - Don't uncomment
		if(result == false) {
			fail("Test.jpf - Regression Test Failed");
		} else {
			return;
		}
	}
}
