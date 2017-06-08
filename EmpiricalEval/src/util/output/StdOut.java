package util.output;

public class StdOut implements ITableWriter {

	public StdOut(){};
	
	@Override
	public void write(String[][] output) {
		
		String[][] newOut = new String[output[0].length][output.length];		
		for(int i=0; i<newOut.length; i++)
			for(int j=0; j<newOut[i].length; j++)
					newOut[i][j] = output[j][i];
		
		int length = 0;
		String print = "";
		for(int i=0; i<newOut[0].length; i++){
			int size = 0;
			for(int j=0; j<newOut.length; j++)
				if(newOut[j][i].length() > size) size = newOut[j][i].length();
			length += size+5;
			print += ",";
		}
		print += "\n";
		
		for(int i=0; i<newOut.length; i++){
			System.out.format(print, newOut[i]);
		}		
		
		
		/*
		 * , names
		 * 
		 * 
		 */
	}
	
	public void close() {}
}
