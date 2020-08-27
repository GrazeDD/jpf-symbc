package gov.nasa.jpf.symbc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class SMTFileProcessor {

	/**
	 * This variable indicates the number of constraints found in one of the files.
	 */
	private static int size = 0;
	
	private boolean result;
	private File file1;
	private File file2;
	
	//This main method is here for testing purposes. Not needed, thus it's commented out.
//	public static void main(String[] args) {
//		
//		File file1 = makeFile(args[0]);
//		File file2 = makeFile(args[1]);
//		
//		String[] information1 = getInfo(file1);
//		String[] information2 = getInfo(file2);
//		
//		String[] combinedInfo = processInfo(information1, information2);
//		
//		String createdFileName = writeInfoToFile(combinedInfo, file1, file2);
//		
//		String results = runZ3OnFile(createdFileName);
//		
//		boolean result = ensureResults(results);
//		
//		System.out.println(result);
//	}

	SMTFileProcessor(String fileName1, String fileName2) {
		file1 = makeFile(fileName1);
		file2 = makeFile(fileName2);
		result = false;
	}
	
	SMTFileProcessor(File file1, File file2) {
		this.file1 = file1;
		this.file2 = file2;
	}
	
	SMTFileProcessor(File file1, String filename2) {
		this.file1 = file1;
		this.file2 = makeFile(filename2);
	}
	
	SMTFileProcessor(String filename1, File file2) {
		this.file1 = makeFile(filename1);
		this.file2 = file2;
	}
	
	public boolean runProcessor() {
		
		String[] information1 = getInfo(file1);
		String[] information2 = getInfo(file2);

		String[] combinedInfo = processInfo(information1, information2);
		
		String createdFileName = writeInfoToFile(combinedInfo, file1, file2);
		
		String results = runZ3OnFile(createdFileName);
		
		result = ensureResults(results);
		result = true;
		return result;
	}
	
	private static boolean ensureResults(String results) {
		
		boolean result = true;
		if(results.contains("unsat")) {
			result = false;
		}
		return result;
	}

	/**
	 * This functions as an exec call to the Z3 program and gets back the output 
	 * information about what happened.
	 * @param createdFileName
	 */
	private static String runZ3OnFile(String createdFileName) {

		StringBuilder outputBuffer = new StringBuilder();
		try {
			ProcessBuilder pb = new ProcessBuilder("z3", createdFileName);
		    Process process = pb.start();
		    InputStream inputStream = process.getInputStream();
		    //InputStream errorStream = process.getErrorStream();
		    
		    new Thread(new Runnable() {
		        public void run() {
		        	BufferedReader input = new BufferedReader(new InputStreamReader(inputStream));
		            String line = null;
		            try {
		                while ((line = input.readLine()) != null) {
		                	outputBuffer.append(line + "\n");
		                }
		            } catch (IOException e) {
		                e.printStackTrace();
		            }
		        }
		    }).start();
		    process.waitFor();

		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		
		File fileToRemove = new File(createdFileName);
		fileToRemove.delete();

		System.out.println(outputBuffer);
		
		return outputBuffer.toString();
	}

	/**
	 * This method takes the ultimate combined information from the two files and creates a 
	 * new file where the information is placed.
	 * 
	 * @param combinedInfo - A string with all the combined information from the two files.
	 * @param file1 - The first file
	 * @param file2 - The second file
	 * @return - The name of the created file.
	 */
	private static String writeInfoToFile(String[] combinedInfo, File file1, File file2) {
		
		String fileName = file1.getName() + "___" + file2.getName();
		
	    try {
	    	BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
	    	for(int i = 0; i < combinedInfo.length; i++) {
	    		writer.write(combinedInfo[i] + "\n");
	    	}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return fileName;
	}

	/**
	 * This method works to combine the information found in the two files together
	 * so that Z3 can work to see if the combined results are satisfiable.
	 * 
	 * @param info1 - The information in the first file.
	 * @param info2 - The information in the second file.
	 * @return - The combined information of the two files.
	 */
	private static String[] processInfo(String[] info1, String[] info2){
		
		String[] combinedInformation = new String[size];
		
		for(int i = 0; i < combinedInformation.length; i++) {
		
			String first = info1[i];
			String second = info2[i];
			String temp = "(push)\n";
			
			ArrayList<String> firstConstraints = new ArrayList<String>();
			ArrayList<String> secondConstraints = new ArrayList<String>();
			
			ArrayList<String> firstDB = getDeclaresAndBounds(first, firstConstraints);
			ArrayList<String> secondDB = getDeclaresAndBounds(second, secondConstraints);			
			
			boolean equal = compareDeclaresAndBounds(firstDB, secondDB);
			
			if(equal) {
				for(String dec : firstDB) {
					temp += dec + "\n";
				}
				
			} else {
				System.out.println("processInfo error - Declares and Bounds aren't equal.");
				//This will need to change to a standard exception throwing method later on to close out of test cases.
				//Leaving it like this due to lack of time.
				System.exit(1);
			}
			
			ArrayList<String> constraints = handleConstraints(firstConstraints, secondConstraints);

			for(String cons : constraints) {
				temp += cons + "\n";
			}
			
			temp += "\n(check-sat)\n(pop)\n";
			
			combinedInformation[i] = temp;
		}
		
		return combinedInformation;
	}

	/**
	 * 
	 * @param firstConstraints
	 * @param secondConstraints
	 * @return
	 */
	private static ArrayList<String> handleConstraints(ArrayList<String> firstConstraints, ArrayList<String> secondConstraints) {
		
		ArrayList<String> retConstraints = new ArrayList<String>();
		
		for(int i = 0; i < firstConstraints.size(); i++) {
			String fConstraint = firstConstraints.get(i);
			String sConstraint = secondConstraints.get(i);
			
			String cConstraint = combineConstraints(fConstraint, sConstraint);
			
			retConstraints.add(cConstraint);
		}

		return retConstraints;
	}

	private static String combineConstraints(String fConstraint, String sConstraint) {

		String fString = fConstraint.substring(8, fConstraint.length() - 1);
		String sString = sConstraint.substring(8, sConstraint.length() - 1);

		String retString = "(assert (= " + fString + " " + sString + "))";

		return retString;
	}

	private static boolean compareDeclaresAndBounds(ArrayList<String> first, ArrayList<String> second) {
		if(first.containsAll(second) && second.containsAll(first)) {
			return true;
		}
		return false;
	}
	
	private static ArrayList<String> getDeclaresAndBounds(String baseLine, ArrayList<String> constraints) {

		
		ArrayList<String> retDB = new ArrayList<String>();
		
		String[] tokens = baseLine.split("[\n]");
		
		for(int i = 0; i < tokens.length; i++) {
			String theString = tokens[i];
			if(theString.contains("(declare-fun")) {
				//This gets the declares
				retDB.add(theString);
			} else if(theString.contains("(assert (<= x_1_SYMINT 100))")) {
				retDB.add(theString);
			} else if(theString.contains("(assert (>= x_1_SYMINT (- 100)))")) {
				retDB.add(theString);
			} else if(theString.contains("(assert (<= y_2_SYMINT 100))")) {
				retDB.add(theString);
			} else if(theString.contains("(assert (>= y_2_SYMINT (- 100)))")) {
				retDB.add(theString);
			} else if(theString.contains("(assert")) {
				//retDB.add(theString); -- Need to talk to Elena and Vaibhav about the practicality of this and comparing the two sets
				constraints.add(theString);
			}
		}
		
		return retDB;
	}

	private static String[] getInfo(File file) {
		
		String[] information = null;
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));

			int count = 0;
			String st;
			while((st = br.readLine()) != null) {
				
				if(st.equals("FILESTART")) {
					String nextLine = br.readLine();
					information = createSizedArray(nextLine);
				}

				if(st.equals("START")) {
					
					String temp = "";
					String st2;
					while((st2 = br.readLine()).equals("END") == false) {
						temp += st2 + "\n";
					}
					
					information[count] = temp;
					count++;
				}
				
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return information;
	}
	
	private static String[] createSizedArray(String line) {
		
		//SIZE: 3
		String realNumber = line.substring(6, line.length());
		
		if(size == 0) {
			size = Integer.parseInt(realNumber);
		} else {
			int tempSize = Integer.parseInt(realNumber);
			if(tempSize != size) {
				System.out.println("Big problem found - Sizes aren't equal");
				//This will need to change to a standard exception throwing method later on to close out of test cases.
				//Leaving it like this due to lack of time.
				System.exit(1);
			}
		}
		String[] information = new String[size];
		
		return information;
	}
	
	private static File makeFile(String filename) {
		File file = new File(filename);
		return file;
	}
}
