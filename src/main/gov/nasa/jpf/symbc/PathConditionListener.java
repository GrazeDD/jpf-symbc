package gov.nasa.jpf.symbc;

import java.io.PrintWriter;
import java.util.ArrayList;

//import com.microsoft.z3.Solver;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.report.ConsolePublisher;
import gov.nasa.jpf.report.Publisher;
import gov.nasa.jpf.symbc.numeric.*;
import gov.nasa.jpf.symbc.numeric.solvers.ProblemGeneral;
import gov.nasa.jpf.symbc.numeric.solvers.ProblemZ3;
import gov.nasa.jpf.symbc.numeric.solvers.ProblemZ3BitVector;
import gov.nasa.jpf.vm.*;

//This really just prints out the Path Conditions of a program in SMT format to a file specified in the .jpf file as the output file.

//To get this file to work, I had to create getter methods within the ProblemZ3 and ProblemZ3BitVector classes
//To get their Solver objects. I know that if I understood how the Publisher worked a bit better, I could just
//Redirect the output in those locations to a file if I wanted to, but the addition of the getters in those 
//classes didn't really hurt anything and allow me to print just the SMT-style constraints that I'm looking for here.
//
//Additionally, investigation into the Debug class revealed that there might be an even easier way of gathering 
//the constraint information. This might also be worth looking into at some point for easing up on the
//need for this listener altogether.
public class PathConditionListener extends ListenerAdapter {

	private ArrayList<String> pcArray = new ArrayList<String>();
	
	public PathConditionListener(Config config, JPF jpf) {
		jpf.addPublisherExtension(ConsolePublisher.class, this);
	}
	
	@Override
	public void threadTerminated(VM vm, ThreadInfo terminatedThread) {
		
		ChoiceGenerator<?> cg = vm.getChoiceGenerator();
		
		if(cg instanceof PCChoiceGenerator) {
			
			String[] dp = SymbolicInstructionFactory.dp;
			
			PathCondition pc = ((PCChoiceGenerator) cg).getCurrentPC();
			if(dp[0].equalsIgnoreCase("z3")) {
				
				ProblemGeneral pb = new ProblemZ3();
				
				ProblemGeneral tempPb = PCParser.parse(pc, pb);
				
				if(tempPb != null) {
					pb = tempPb;
					pb.solve();
				}
				
				//System.out.println(((ProblemZ3)pb).getSolver().toString());
				
				pcArray.add(((ProblemZ3)pb).getSolver().toString());
				
				((ProblemZ3)pb).cleanup();
				
			} else if(dp[0].equalsIgnoreCase("z3bitvector")) {
				
				ProblemGeneral pb = new ProblemZ3BitVector();
			
				ProblemGeneral tempPb = PCParser.parse(pc, pb);
			
				if(tempPb != null) {
					pb = tempPb;
					pb.solve();
				}

				//System.out.println(((ProblemZ3BitVector)pb).getSolver().toString());
			
				pcArray.add(((ProblemZ3BitVector)pb).getSolver().toString());
					
				((ProblemZ3BitVector)pb).cleanup();
			}
		} else {
			pcArray.add(null);
			System.out.println("There was some sort of problem with this.");
			System.exit(1);
		}
		
	}
	
	@Override
	public void publishFinished(Publisher publisher) {
		
		PrintWriter pw = publisher.getOut();
		
		pw.println("FILESTART");
		pw.println("SIZE: " + pcArray.size());
		for(String pc : pcArray) {
			pw.println("START");
			pw.println(pc);
			pw.println("(check-sat)");
			pw.println("END");
		}
		pw.println("FILEEND");
	}
	
}
