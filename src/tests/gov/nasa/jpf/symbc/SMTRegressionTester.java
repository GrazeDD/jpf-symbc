package gov.nasa.jpf.symbc;

import static org.junit.Assert.fail;
import java.io.File;
import org.junit.Test;

//Just ensuring that the tester is running on files created by the listener with a legacy file in the same location is all this should need
//to get running without any issues.

public class SMTRegressionTester {
    
	//I think this functionality is ensured through IntTest already in JPF lol...
	
	@Test
	public void test_TestdotJPF() {
		//6-18 - Thought: Find a way to run JPF from here to programmatically make the new file as the tester is about to run.
		//6-29 - Answer to thought after a few days of trying: Yeah no not right now. SPF's testing suite is far more complicated
		//than I was giving it credit for and there are a lot of things going on in there still that I don't understand.
		//I feel like I could do so much if I understood the intricacies of the implemented testing suite JPF comes with better,
		//but as it stands, there's simply so much I don't understand just yet. This works fine, especially if I need to automate
		//something with a bash script later on.
		File legacyFile = new File("Test.jpf.report");
		File newFile = new File("Test.jpf.report_new"); //This is the name that my listener will generate. 
		SMTFileProcessor proc = new SMTFileProcessor(newFile, legacyFile);
		boolean result = proc.runProcessor();
		if(result == true) {
			return;
		} else {
			fail("Test.jpf - Regression Test Failed");
		}
	}
	
	@Test
	public void test_int_XMinusY() {
		File legacyFile = new File("XMinusY.report");
		File newFile = new File("XMinusY.report_new"); //This is the name that my listener will generate. 
		SMTFileProcessor proc = new SMTFileProcessor(newFile, legacyFile);
		boolean result = proc.runProcessor();
		if(result == true) {
			return;
		} else {
			fail("Test.jpf - Regression Test Failed");
		}
	}
	
	@Test
	public void test_int_YMinusX() {
		File legacyFile = new File("YMinusX.report");
		File newFile = new File("YMinusX.report_new"); //This is the name that my listener will generate. 
		SMTFileProcessor proc = new SMTFileProcessor(newFile, legacyFile);
		boolean result = proc.runProcessor();
		if(result == true) {
			return;
		} else {
			fail("Test.jpf - Regression Test Failed");
		}
	}
	
	@Test
	public void test_real_XMinusY() {
		File legacyFile = new File("R_XMinusY.report");
		File newFile = new File("R_XMinusY.report_new"); //This is the name that my listener will generate. 
		SMTFileProcessor proc = new SMTFileProcessor(newFile, legacyFile);
		boolean result = proc.runProcessor();
		if(result == true) {
			return;
		} else {
			fail("Test.jpf - Regression Test Failed");
		}
	}
	
	@Test
	public void test_real_YMinusX() {
		File legacyFile = new File("R_XMinusY.report");
		File newFile = new File("R_XMinusY.report_new"); //This is the name that my listener will generate. 
		SMTFileProcessor proc = new SMTFileProcessor(newFile, legacyFile);
		boolean result = proc.runProcessor();
		if(result == true) {
			return;
		} else {
			fail("Test.jpf - Regression Test Failed");
		}
	}
}
