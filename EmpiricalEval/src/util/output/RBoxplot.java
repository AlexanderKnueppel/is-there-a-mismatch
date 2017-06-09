package util.output;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

import de.ovgu.featureide.fm.core.base.IConstraint;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import util.Utils;

public class RBoxplot {

	private static String _defaultPath = "eval.r";
	private static boolean _defaultLogAxis = false;

	public static void generateRCode(File[] listOfFiles) {
		generateRCode(_defaultPath, listOfFiles);
	}

	public static void generateRCode(String path, File[] listOfFiles) {
		generateRCode(path, listOfFiles, _defaultLogAxis);
	}

	public static void generateRCode(File[] listOfFiles, boolean logAxis) {
		generateRCode(_defaultPath, listOfFiles, logAxis);
	}

	public static void generateRCode(String path, File[] listOfFiles, boolean logAxis) {
		String ret = "";
		String names = "names <- c( ";
		int length = getMaxLength(listOfFiles);

		for (File file : listOfFiles) {
			String name = file.isDirectory() ? file.getName()
					: file.getName().substring(0, file.getName().lastIndexOf('.'));
			name = name.replace('.', '_').replace('-', '_');
			names += "\"" + name + "\", ";
			ret += "\t" + name + "=c(" + generateRVector(file, length);
			ret = ret.substring(0, ret.lastIndexOf(',')) + ")\n\n";
		}
		names = names.substring(0, names.lastIndexOf(',')) + ")";

		if (logAxis)
			ret = ret.replace(" 0", " 1");

		ret += "\npdf(file='eval_boxplot.pdf', onefile=T, paper='A4r')" + "\n\t" + names
				+ "\n\tdataList <- lapply(names, get, envir=environment())" + "\n\tnames(dataList) <- names"
				+ "\n\tboxplot(dataList, ylim=c(1,200), ylab=\"Literals\", xaxt=\"n\", xlab=\"\"";
		if (logAxis)
			ret += ", log=\"y\"";
		ret += ")" + "\n\taxis(1, at=1:" + listOfFiles.length + ", labels = FALSE)" + "\n\ttext(x=1:"
				+ listOfFiles.length + ", y=0.7, srt=30, adj=1, names, xpd=TRUE) " + "; dev.off()\n";

		try (PrintWriter out = new PrintWriter(path)) {
			out.println(ret);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private static int getMaxLength(File[] listOfFiles) {

		int maxLength = 0;
		for (File file : listOfFiles) {
			int length;
			if (file.isDirectory())
				length = getMaxLength(file.listFiles());
			else {
				try {
					IFeatureModel fm = Utils.loadFeatureModel(file.getAbsolutePath());
					length = fm.getConstraintCount();
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
			}
			if (maxLength < length)
				maxLength = length;
		}
		return maxLength;
	}

	private static String generateRVector(File file, int length) {
		String ret = "";
		if (file.isDirectory())
			for (File inFile : file.listFiles())
				ret += generateRVector(inFile, length);

		else {

			IFeatureModel fm = null;
			try {
				fm = Utils.loadFeatureModel(file.getAbsolutePath());
			} catch (Exception e) {
				e.printStackTrace();
				return "";
			}
			for (int i = 0; i < fm.getConstraintCount(); i++) {
				IConstraint constraint = fm.getConstraints().get(i);
				ret += Utils.numberOfLiterals(constraint.getNode()) + ", ";
				if (i % 100 == 0 && i != 0)
					ret += "\n ";
			}
		}

		return ret;
	}
}
