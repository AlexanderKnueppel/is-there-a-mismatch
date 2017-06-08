package util.output;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class CSVWriter implements ITableWriter {
	private String outputFile;
	
	public CSVWriter(String path){
		outputFile = path;
	}
	
	@Override
	public void write(String[][] output) {
		String result = "";
		for(int i = 0; i < output.length; i++) {
			for(int  j = 0; j < output[i].length; j++) {
				result = output[i][j] + ",";
			}
			result = result.substring(0, result.length()-1) + "\n";
		}
		
		try {
			Files.write( Paths.get(outputFile), result.getBytes(), StandardOpenOption.CREATE);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void close() {
//		output[0][0] = "";
//		output[0][1] = "Features";
//		output[0][2] = "Constraints";
//		output[0][3] = "Strict";
//		output[0][4] = "Pseudo";
//		output[0][5] = "Sum";
//		output[0][6] = "Feature-Increase";
//		output[0][7] = "Constraint-Increase";
				
		

	}

}
