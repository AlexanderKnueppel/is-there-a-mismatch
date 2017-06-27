package main;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.ovgu.featureide.fm.core.ExtensionManager.NoSuchExtensionException;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.conversion.CombinedConverter;
import de.ovgu.featureide.fm.core.conversion.ComplexConstraintConverter;
import util.Utils;
import util.Utils.ConstraintType;

// TODO: Auto-generated Javadoc
/**
 * The Class EliminateComplexConstraints.
 */
public class EliminateComplexConstraints {
	
	/** The Constant OUTPUT_DIR. */
	private static String OUTPUT_DIR = "./output/"; //default

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		long time = System.currentTimeMillis();

		Utils.useCache = false;

		if (args.length == 0) {
			System.err
					.println("You need to provide either files or folders of files that you would like to transform.");
			return;
		}
		
		int idx = -1;
		if((idx = Arrays.asList(args).indexOf(new String("-o"))) != -1) {
			try {
				OUTPUT_DIR = args[idx+1] + "/";
				args[idx] = "";
				args[idx+1] = "";
			} catch(Exception e) {
				System.err.println("No output folder specified!");
				return;
			}
			if(!(new File(OUTPUT_DIR).exists())) {
					new File(OUTPUT_DIR).mkdirs();
			}
		}

		for (String fileName : args) {
			
			if(fileName.isEmpty())
				continue;

			File folder = new File(fileName);
			
			if(!folder.exists()) {
				System.err.println(fileName + " does not exist.");
				continue;
			}
			
			List<File> files = new ArrayList<File>();
			
			if(folder.isFile())
				files.add(folder);
			else { // Directory
				files.addAll(Arrays.asList(folder.listFiles(new FilenameFilter() {
					public boolean accept(File dir, String name) {
						return name.endsWith("xml");
					}
				})));
			}

			for (File file : files) {
				IFeatureModel fm = null;

				try {
					fm = Utils.loadFeatureModel(file.getAbsolutePath());
				} catch (Exception e) {
					System.err.println(file.getName() + " is not a valid feature model.");
					//e.printStackTrace();
				}

				System.out.println("Convert " + file.getName() + "... ");

				int nComplexConstraints = fm.getConstraintCount() - Collections
						.frequency(new ArrayList<ConstraintType>(Utils.classify(fm).values()), ConstraintType.SIMPLE);
				System.out.println("Number of complex constraints: " + nComplexConstraints + ".");

				ComplexConstraintConverter converter = new ComplexConstraintConverter();
				/*
				 * You can choose between CNFConverter, NNFConverter, or
				 * CombinedConverter...
				 */
				IFeatureModel resultFM = converter.convert(fm, new CombinedConverter());
				System.out.println("...done!\n");

				try {
					Utils.writeFeatureModel(resultFM, "./" + OUTPUT_DIR + file.getName());
				} catch (NoSuchExtensionException e) {
					e.printStackTrace();
				}
			}
		}

		float elapsed = Math.round(((System.currentTimeMillis() - time) / 600f)) / 100f;
		System.out.println("\nTime needed: " + elapsed + " min.");
		System.out.println("--------------------------------------------------------------------\n\n");
	}
}
