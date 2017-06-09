package main;

import util.Utils;
import util.output.CSVWriter;
import util.output.ExcelWriter;
import util.output.ITableWriter;
import util.output.LatexWriter;
import util.output.RBoxplot;
import util.output.StdOut;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;

import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.conversion.CombinedConverter;
import de.ovgu.featureide.fm.core.conversion.ComplexConstraintConverter;
import statistics.FMStatistics;

// TODO: Auto-generated Javadoc
/**
 * The Class Evaluation.
 */
public class Evaluation {

	/** Configuration. */
	private static int XLS = 1 << 1;
	
	/** The csv. */
	private static int CSV = 1 << 2;
	
	/** The Te X. */
	private static int TeX = 1 << 3;
	
	/** The Box plot R. */
	private static int BoxPlotR = 1 << 4;
	
	/** The Std out. */
	private static int StdOut = 1 << 5;

	/** The Config. */
	private static int Config = XLS | CSV | TeX | BoxPlotR | StdOut; 	// Generate everything...

	/** The output folder. */
	private static String outputFolder = "./Output/";
	
	/** The models folder. */
	private static String modelsFolder = "../Models/";

	/** The dataset. */
	public static DefaultBoxAndWhiskerCategoryDataset dataset;

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		long time = System.currentTimeMillis();

		Utils.useCache = false;

		File folder = new File(modelsFolder);
		File[] listOfFiles = folder.listFiles();

		if ((Config & CSV) == 0)
			RBoxplot.generateRCode(listOfFiles, true);

		String[][] output = calcDefaultOutput(listOfFiles);

		float elapsed = Math.round(((System.currentTimeMillis() - time) / 600f)) / 100f;
		System.out.println("\nCalculationTime: " + elapsed + " min.");
		System.out.println("--------------------------------------------------------------------\n\n");

		if ((Config & StdOut) == 0) {
			ITableWriter stdOut = new StdOut();
			stdOut.write(output);
		}
		if ((Config & XLS) == 0) {
			ITableWriter jxl = new ExcelWriter(outputFolder + "eval.xls");
			jxl.write(output);
			jxl.close();
		}
		if ((Config & CSV) == 0) {
			ITableWriter csv = new CSVWriter(outputFolder + "eval.xls");
			csv.write(output);
		}
		if ((Config & TeX) == 0) {
			ITableWriter latex = new LatexWriter(outputFolder + "eval.tex", listOfFiles.length);
			latex.write(output);
		}
	}

	/**
	 * Calc default output.
	 *
	 * @param listOfFiles the list of files
	 * @return the string[][]
	 */
	public static String[][] calcDefaultOutput(File[] listOfFiles) {
		String[][] output = new String[listOfFiles.length + 1][8];
		output[0][0] = "";
		output[0][1] = "Features";
		output[0][2] = "Constraints";
		output[0][3] = "Strict";
		output[0][4] = "Pseudo";
		output[0][5] = "Sum";
		output[0][6] = "Feature-Increase";
		output[0][7] = "Constraint-Increase";

		int numOfDirs = 0;
		for (int i = 0; i < listOfFiles.length; i++) {
			File file = listOfFiles[i];
			int index = (file.isDirectory()) ? listOfFiles.length - (numOfDirs++) : i + 1 - numOfDirs;

			IFeatureModel fm = null;
			if (!file.isDirectory()) {
				try {
					fm = Utils.loadFeatureModel(file.getAbsolutePath());
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}

			String[] row = new String[8];

			// name
			if (file.isDirectory())
				row[0] = file.getName();
			else
				row[0] = file.getName().substring(0, file.getName().lastIndexOf('.'));
			System.out.println("Calculating statistics for " + row[0]);

			// number of features and constraints
			System.out.println("... calculating number of features and constraints.");
			if (file.isDirectory()) {
				List<Integer> features = new ArrayList<Integer>();
				List<Integer> constraints = new ArrayList<Integer>();
				for (File inFile : file.listFiles()) {
					IFeatureModel inFm;
					try {
						inFm = Utils.loadFeatureModel(inFile.getAbsolutePath());
					} catch (Exception e) {
						e.printStackTrace();
						continue;
					}
					features.add(inFm.getNumberOfFeatures());
					constraints.add(inFm.getConstraintCount());
				}
				Collections.sort(features);
				Collections.sort(constraints);
				row[1] = features.get(0) + " < " + features.get(features.size() / 2) + " < "
						+ features.get(features.size() - 1);
				row[2] = constraints.get(0) + " < " + constraints.get(constraints.size() / 2) + " < "
						+ constraints.get(constraints.size() - 1);
			} else {
				row[1] = "" + fm.getNumberOfFeatures();
				row[2] = "" + fm.getConstraintCount();
			}

			// percent of pseudo-, strict-complex & simple constraints
			System.out.println("... calculating percent of pseudo- and strict-constraints.");
			if (file.isDirectory()) {
				List<Float> pseudo = new ArrayList<Float>();
				List<Float> strict = new ArrayList<Float>();
				for (File inFile : file.listFiles()) {
					IFeatureModel inFm;
					try {
						inFm = Utils.loadFeatureModel(inFile.getAbsolutePath());
					} catch (Exception e) {
						e.printStackTrace();
						continue;
					}
					FMStatistics stats = new FMStatistics(inFm);
					pseudo.add(Math.round(stats.numPseudoComplex() * 100f) / 100f);
					strict.add(Math.round(stats.numStrictComplex() * 100f) / 100f);
				}
				Collections.sort(pseudo);
				Collections.sort(strict);
				row[3] = pseudo.get(0) + "% < " + pseudo.get(pseudo.size() / 2) + "% < " + pseudo.get(pseudo.size() - 1)
						+ "%";
				row[4] = strict.get(0) + "% < " + strict.get(strict.size() / 2) + "% < " + strict.get(strict.size() - 1)
						+ "%";
				row[5] = (strict.get(0) + pseudo.get(0)) + "% < "
						+ (pseudo.get(pseudo.size() / 2) + strict.get(strict.size() / 2)) + "% < "
						+ (strict.get(strict.size() - 1) + pseudo.get(pseudo.size() - 1)) + "%";
			} else {
				FMStatistics stats = new FMStatistics(fm);
				row[3] = "" + Math.round(stats.numPseudoComplex() * 100f) / 100f + "%";
				row[4] = "" + Math.round(stats.numStrictComplex() * 100f) / 100f + "%";
				row[5] = "" + Math.round((stats.numPseudoComplex() + stats.numStrictComplex()) * 100f) / 100f + "%";
			}
			// increase of features & constraints
			System.out.println("... calculating percent of feature- & constraint-increase.");
			if (file.isDirectory()) {
				List<Float> featureInc = new ArrayList<Float>();
				List<Float> constraintInc = new ArrayList<Float>();
				for (File inFile : file.listFiles()) {
					IFeatureModel inFm;
					try {
						inFm = Utils.loadFeatureModel(inFile.getAbsolutePath());
					} catch (Exception e) {
						e.printStackTrace();
						continue;
					}
					FMStatistics stats = new FMStatistics(inFm);
					ComplexConstraintConverter converter = new ComplexConstraintConverter();
					IFeatureModel result = converter.convert(inFm, new CombinedConverter());

					featureInc.add(Math.round(stats.increaseFeature(result) * 100f) / 100f);
					constraintInc.add(Math.round(stats.increaseConstraints(result) * 100f) / 100f);
				}
				Collections.sort(featureInc);
				Collections.sort(constraintInc);
				row[6] = featureInc.get(0) + "% < " + featureInc.get(featureInc.size() / 2) + "% < "
						+ featureInc.get(featureInc.size() - 1) + "%";
				row[7] = constraintInc.get(0) + "% < " + constraintInc.get(constraintInc.size() / 2) + "% < "
						+ constraintInc.get(constraintInc.size() - 1) + "%";
			} else {
				FMStatistics stats = new FMStatistics(fm);
				ComplexConstraintConverter converter = new ComplexConstraintConverter();
				IFeatureModel result = converter.convert(fm, new CombinedConverter());
				row[6] = "" + Math.round(stats.increaseFeature(result) * 100f) / 100f + "%";
				row[7] = "" + Math.round(stats.increaseConstraints(result) * 100f) / 100f + "%";
			}
			output[index] = row;
		}
		return output;
	}
}
