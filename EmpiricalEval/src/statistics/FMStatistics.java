package statistics;

import java.util.Map;

import util.Utils;
import de.ovgu.featureide.fm.core.base.IConstraint;
import de.ovgu.featureide.fm.core.base.IFeatureModel;

public class FMStatistics {
	private int numPseudo = 0, numStrict = 0, numSimple = 0;

	private IFeatureModel model;

	public FMStatistics(IFeatureModel fm) {
		Map<IConstraint, Utils.ConstraintType> classifiedConstraints = Utils.classify(fm);
		numSimple = Utils.countConstraints(classifiedConstraints, Utils.ConstraintType.SIMPLE);
		numStrict = Utils.countConstraints(classifiedConstraints, Utils.ConstraintType.STRICT_COMPLEX);
		numPseudo = Utils.countConstraints(classifiedConstraints, Utils.ConstraintType.PSEUDO_COMPLEX);

		model = fm;
	}

	public int numPseudoComplex() {
		return numPseudo;
	}

	public int numStrictComplex() {
		return numStrict;
	}

	public int numSimple() {
		return numSimple;
	}

	public double strictRatio() {
		return (double) model.getConstraintCount() / numStrict;
	}

	public double pseudoRatio() {
		return (double) model.getConstraintCount() / numPseudo;
	}

	public double ssimpleRatio() {
		return (double) model.getConstraintCount() / numSimple;
	}

	public double increaseFeature(IFeatureModel fm) {
		return ((double) fm.getNumberOfFeatures() - model.getNumberOfFeatures()) / (double) model.getNumberOfFeatures()
				* 100.0;
	}

	public double increaseConstraints(IFeatureModel fm) {
		return ((double) fm.getConstraintCount() - model.getConstraintCount()) / (double) model.getConstraintCount()
				* 100.0;
	}
}
