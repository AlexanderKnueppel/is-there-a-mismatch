package statistics;

import java.util.Map;

import util.Utils;
import de.ovgu.featureide.fm.core.base.IConstraint;
import de.ovgu.featureide.fm.core.base.IFeatureModel;

// TODO: Auto-generated Javadoc
/**
 * The Class FMStatistics.
 */
public class FMStatistics {
	
	/** The num simple. */
	private int numPseudo = 0, numStrict = 0, numSimple = 0;

	/** The model. */
	private IFeatureModel model;

	/**
	 * Instantiates a new FM statistics.
	 *
	 * @param fm the fm
	 */
	public FMStatistics(IFeatureModel fm) {
		Map<IConstraint, Utils.ConstraintType> classifiedConstraints = Utils.classify(fm);
		numSimple = Utils.countConstraints(classifiedConstraints, Utils.ConstraintType.SIMPLE);
		numStrict = Utils.countConstraints(classifiedConstraints, Utils.ConstraintType.STRICT_COMPLEX);
		numPseudo = Utils.countConstraints(classifiedConstraints, Utils.ConstraintType.PSEUDO_COMPLEX);

		model = fm;
	}

	/**
	 * Num pseudo complex.
	 *
	 * @return the int
	 */
	public int numPseudoComplex() {
		return numPseudo;
	}

	/**
	 * Num strict complex.
	 *
	 * @return the int
	 */
	public int numStrictComplex() {
		return numStrict;
	}

	/**
	 * Num simple.
	 *
	 * @return the int
	 */
	public int numSimple() {
		return numSimple;
	}

	/**
	 * Strict ratio.
	 *
	 * @return the double
	 */
	public double strictRatio() {
		return (double) model.getConstraintCount() / numStrict;
	}

	/**
	 * Pseudo ratio.
	 *
	 * @return the double
	 */
	public double pseudoRatio() {
		return (double) model.getConstraintCount() / numPseudo;
	}

	/**
	 * Ssimple ratio.
	 *
	 * @return the double
	 */
	public double ssimpleRatio() {
		return (double) model.getConstraintCount() / numSimple;
	}

	/**
	 * Increase feature.
	 *
	 * @param fm the fm
	 * @return the double
	 */
	public double increaseFeature(IFeatureModel fm) {
		return ((double) fm.getNumberOfFeatures() - model.getNumberOfFeatures()) / (double) model.getNumberOfFeatures()
				* 100.0;
	}

	/**
	 * Increase constraints.
	 *
	 * @param fm the fm
	 * @return the double
	 */
	public double increaseConstraints(IFeatureModel fm) {
		return ((double) fm.getConstraintCount() - model.getConstraintCount()) / (double) model.getConstraintCount()
				* 100.0;
	}
}
