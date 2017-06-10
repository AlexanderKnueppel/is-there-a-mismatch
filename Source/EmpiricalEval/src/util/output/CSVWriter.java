package util.output;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

// TODO: Auto-generated Javadoc
/**
 * The Class CSVWriter.
 */
public class CSVWriter implements ITableWriter {
	
	/** The output file. */
	private String outputFile;

	/**
	 * Instantiates a new CSV writer.
	 *
	 * @param path the path
	 */
	public CSVWriter(String path) {
		outputFile = path;
	}

	/* (non-Javadoc)
	 * @see util.output.ITableWriter#write(java.lang.String[][])
	 */
	@Override
	public void write(String[][] output) {
		String result = "";
		for (int i = 0; i < output.length; i++) {
			for (int j = 0; j < output[i].length; j++) {
				result += output[i][j] + ",";
			}
			result = result.substring(0, result.length() - 1) + "\n";
			
		}
		System.out.println(result);
		try {
			Files.write(Paths.get(outputFile), result.getBytes(), StandardOpenOption.CREATE);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see util.output.ITableWriter#close()
	 */
	@Override
	public void close() {
	}

}
