package util.output;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class LatexWriter implements ITableWriter {

	private String outputFile;
	private int numberOfFeatureModels;
	
	
	public LatexWriter(String path, int numOfFeatureModels){
		outputFile = path;
		numberOfFeatureModels = numOfFeatureModels;
	}
	
	@Override
	public void write(String[][] output) {
		
		initStrings();				
		
		String[][] newOut = new String[output[0].length][output.length];		
		for(int i=0; i<newOut.length; i++)
			for(int j=0; j<newOut[i].length; j++)
					newOut[i][j] = output[j][i];
		
		String outputString = pre;
		
		for(int i=0; i < newOut.length ; i++){
			
			if(i==1) outputString += featuresPrefix;
			else if(i==2) outputString += constraintsPrefix;
			else if(i==3) outputString += pseudoPrefix;
			else if(i==4) outputString += strictPrefix;
			else if(i==5) outputString += sumPrefix;
			else if(i==6) outputString += featureIncrPrefix;
			else if(i==7) outputString += constraintIncrPrefix;
			
			for(int j=1; j < newOut[i].length; j++){				
				if(i==0) outputString += namePrefix + newOut[i][j].replace("_", "\\_") + nameSuffix;
				else if (i==1 || i==2) outputString += " & " + newOut[i][j].replace("<", "$\\leq$");
				else outputString += " & " + newOut[i][j].replace("<", "$\\leq$").replace("%", "\\%");				
			}
			
			if(i==0) outputString += "\t\t\t\t\\\\ \\cline{2-"+(numberOfFeatureModels+2)+"} \n";
			else if(i==1) outputString += featuresSuffix;
			else if(i==2) outputString += constraintsSuffix;
			else if(i==3) outputString += pseudoSuffix;
			else if(i==4) outputString += strictSuffix;
			else if(i==5) outputString += sumSuffix;
			else if(i==6) outputString += featureIncrSuffix;
			else if(i==7) outputString += constraintIncrSuffix;
		}
		
		outputString += suf;
		
		try {
            Files.write(Paths.get(outputFile), outputString.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	/*
	 * 
	 */
	private void initStrings(){
		namePrefix = "\t\t\t\t& \\rot{";
		nameSuffix = "\\tnote{1}} \n";
		
		featuresPrefix = "\n\t\t\t\t\\multirow{2}{*}{\\usebox0} \n"
								 +"\t\t\t\t& Features ";
		featuresSuffix = "\\\\ \n";
		
		constraintsPrefix = "\t\t\t\t& Constraints ";
		constraintsSuffix = "\\\\\n\t\t\t\t\\cline{2-"+(numberOfFeatureModels+2)+"} \n";
		
		pseudoPrefix = "\n\t\t\t\t\\multirow{3}{*}{\\usebox1} \n"
								+"\t\t\t\t& Strict ";
		pseudoSuffix = "\\\\ \n";
		
		strictPrefix = "\t\t\t\t& Pseudo";
		strictSuffix = "\\\\ \\cline{3-"+(numberOfFeatureModels+2)+"} \n";
		
		sumPrefix = "\t\t\t\t& Sum";
		sumSuffix = "\\\\\n\t\t\t\t\\cline{2-"+(numberOfFeatureModels+2)+"} \n";
		
		featureIncrPrefix = "\n\t\t\t\t\\multirow{2}{*}{\\usebox2} \n"
								+"\t\t\t\t& Features ";
		featureIncrSuffix = "\\\\ \n";
		
		constraintIncrPrefix = "\t\t\t\t& Constraints ";
		constraintIncrSuffix = "\\\\\n\t\t\t\t\\cline{2-"+(numberOfFeatureModels+2)+"} \n";

		
		captionName = "Overview of evaluated feature models including number of features and constraints before and after applying our constraint elimination approach";
		
		pre = ""
				+"\\begin{table*} \n"
					+"\t\\label{tab:evaluation:results} \n"
					+"\t\\scriptsize \n"
					+"\t\\renewcommand{\\arraystretch}{1.2} \n"
					+"\t\\hspace*{-5cm} \n"
					+"\t\\scalebox{1}{ \n"
						+"\t\t\\begin{threeparttable} \n"
							+"\t\t\t\\setbox0\\hbox{\\tabular{@{}l}Size\\endtabular} \n"
							+"\t\t\t\\setbox1\\hbox{\\tabular{@{}l}Complex \\\\ Constraints\\endtabular} \n"
							+"\t\t\t\\setbox2\\hbox{\\tabular{@{}l}Increase\\endtabular} \n"
							+"\t\t\t\\begin{tabular}{l";
		for(int i=0; i<numberOfFeatureModels; i++) pre += "r";							
		pre					+= "c} \n"
								+"\t\t\t\t& \n";
		
		suf = ""			
							+"\t\t\t\\end{tabular} \n\n"
							
							+"\t\t\t\\begin{tablenotes} \n"
								+"\t\t\t\t\\centering \n"
								+"\t\t\t\t\\scriptsize \n"
								+"\t\t\t\t\\item[1]" + "FeatureIDE" + " model; \\item[2] KConfig; \\item[3] $\\mathit{Min} \\leq \\mathit{Mean} \\leq \\mathit{Max}$ for all 116" + " CDL Models. \n"
							+"\t\t\t\\end{tablenotes} \n"
						+"\t\t\\end{threeparttable} \n"
					+"\t} \n"
					+"\t\\caption{" + captionName + "} \n"
				+"\\end{table*} \n"; 

	}
	
	@Override
	public void close() {}
	
	private String namePrefix;
	private String nameSuffix;
	
	private String featuresPrefix;
	private String featuresSuffix;
	
	private String constraintsPrefix;
	private String constraintsSuffix;
	
	private String pseudoPrefix;							
	private String pseudoSuffix;
	
	private String strictPrefix;
	private String strictSuffix;
	
	private String sumPrefix;
	private String sumSuffix;
	
	private String featureIncrPrefix;							
	private String featureIncrSuffix;
	
	private String constraintIncrPrefix;
	private String constraintIncrSuffix;

	private String captionName;
	
	private String pre;	
	private String suf;

	

}
