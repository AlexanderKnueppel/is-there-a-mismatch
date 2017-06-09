package main;

import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.conversion.CombinedConverter;
import de.ovgu.featureide.fm.core.conversion.ComplexConstraintConverter;
import de.ovgu.featureide.fm.core.editing.Comparison;
import de.ovgu.featureide.fm.core.editing.ModelComparator;
import util.Utils;

// TODO: Auto-generated Javadoc
/**
 * The Class SanityCheck.
 */
public class SanityCheck {

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		IFeatureModel fm = null;
		if (args.length != 1) {
			System.err.println("You need to provide exactly one feature model.");
			return;
		}

		try {
			fm = Utils.loadFeatureModel(args[0]);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		ComplexConstraintConverter converter = new ComplexConstraintConverter();
		IFeatureModel resultFM = converter.convert(fm, new CombinedConverter());

		ModelComparator comparator = new ModelComparator(100000, 3);

		comparator.compare(fm, resultFM);

		System.out.println("Added features: " + comparator.getAddedFeatures() + " //Should be empty!");
		System.out.println("Removed features: " + comparator.getDeletedFeatures() + " //Should be empty!");
		System.out.println(
				"Is the feature model with complex constraints equivalent to its pendant with only simple constraints?"
						+ (Comparison.REFACTORING == comparator.getResult() ? " Yes." : " No."));
	}

}
