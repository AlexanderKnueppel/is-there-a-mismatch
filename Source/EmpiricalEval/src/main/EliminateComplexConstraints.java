package main;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

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
	final static String OUTPUT_DIR = "./output/";

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

		for (String fileName : args) {

			File folder = new File(fileName);
			File[] listOfFiles = folder.listFiles();

			for (File file : listOfFiles) {

				if (file.isDirectory()) {
					continue;
				}

				IFeatureModel fm = null;

				try {
					fm = Utils.loadFeatureModel(file.getAbsolutePath());
				} catch (Exception e) {
					e.printStackTrace();
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
