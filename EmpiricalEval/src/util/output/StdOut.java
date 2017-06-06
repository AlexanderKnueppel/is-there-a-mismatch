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
			print += "%"+size+"s  |  ";
		}
		print += "\n";
		
		for(int i=0; i<newOut.length; i++){
			if(i==0 || i==1){
				String seperator = "";
				for(int j=0; j<length-2; j++)
					seperator += "-";
				System.out.println(seperator);
			}
			System.out.format(print, newOut[i]);
		}		
	}
	
	public void close() {}
}
