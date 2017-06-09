package util;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.prop4j.And;
import org.prop4j.Node;
import org.prop4j.Not;
import org.prop4j.Or;
import org.prop4j.Literal;

import de.ovgu.featureide.fm.core.ExtensionManager.NoSuchExtensionException;
import de.ovgu.featureide.fm.core.base.IConstraint;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.base.impl.FMFactoryManager;
import de.ovgu.featureide.fm.core.conversion.ComplexConstraintConverter;
import de.ovgu.featureide.fm.core.io.IFeatureModelFormat;
import de.ovgu.featureide.fm.core.io.Problem;
import de.ovgu.featureide.fm.core.io.ProblemList;
import de.ovgu.featureide.fm.core.io.manager.FileHandler;
import de.ovgu.featureide.fm.core.io.xml.XmlFeatureModelFormat;

public class Utils {
	/** Configuration */
	public static boolean useCache = true;

	/**
	 * 
	 * @author User
	 *
	 */
	public enum ConstraintType {
		STRICT_COMPLEX, PSEUDO_COMPLEX, SIMPLE
	}

	/**
	 * 
	 * @author User
	 *
	 */
	public static class Cache {
		public static List<IConstraint> cachedConstraints = null;
		public static Map<IConstraint, ConstraintType> cachedClassified = null;
		public static int cachedSimpleConstraints = 0, cachedPseudoComplexConstraints = 0,
				cachedStrictComplexConstraints = 0;
		public static boolean calculated = false;

		public static int count(Utils.ConstraintType type) {
			switch (type) {
			case STRICT_COMPLEX:
				return cachedStrictComplexConstraints;
			case PSEUDO_COMPLEX:
				return cachedPseudoComplexConstraints;
			case SIMPLE:
				return cachedSimpleConstraints;
			default:
				return 0;
			}
		}

		public static void fillCount(Map<IConstraint, ConstraintType> classified) {
			cachedClassified = classified;
			cachedPseudoComplexConstraints = Collections.frequency(new ArrayList<ConstraintType>(classified.values()),
					ConstraintType.PSEUDO_COMPLEX);
			cachedStrictComplexConstraints = Collections.frequency(new ArrayList<ConstraintType>(classified.values()),
					ConstraintType.STRICT_COMPLEX);
			cachedSimpleConstraints = Collections.frequency(new ArrayList<ConstraintType>(classified.values()),
					ConstraintType.SIMPLE);
			calculated = true;
		}

	}

	public static Map<IConstraint, ConstraintType> classify(IFeatureModel fm) {
		return Utils.classify(fm.getConstraints());
	}

	public static int minimalNumberOfLiterals(IConstraint c) {
		int nnf = numberOfLiterals(propagateNegation(c.getNode(), false));
		int cnf = numberOfLiterals(c.getNode().toCNF());

		return nnf < cnf ? nnf : cnf;
	}

	public static int numberOfLiterals(Node n) {
		if (n.getChildren() == null) {
			return 0;
		}
		int number = 0;
		for (Node child : n.getChildren()) {
			int add = (child instanceof Literal) ? 1 : 0;
			number += add + numberOfLiterals(child);
		}
		return number;
	}

	private static Node propagateNegation(Node node, boolean negated) {
		if (node instanceof Not) {
			negated = !negated;
			return propagateNegation(node.getChildren()[0], negated);
		} else if (node instanceof And || node instanceof Or) {
			List<Node> nodelist = new ArrayList<Node>();
			for (Node tmp : node.getChildren()) {
				nodelist.add(propagateNegation(tmp, negated));
			}

			if (node instanceof And) {
				if (negated) {
					return new Or((Object[]) nodelist.toArray());
				} else {
					return new And((Object[]) nodelist.toArray());
				}
			} else {
				if (negated) {
					return new And((Object[]) nodelist.toArray());
				} else {
					return new Or((Object[]) nodelist.toArray());
				}
			}
		}

		// node is an atom
		if (negated)
			return new Not(node);

		return node;
	}

	public static Map<IConstraint, ConstraintType> classify(List<IConstraint> constraints) {
		if (useCache && constraints == Utils.Cache.cachedConstraints)
			return Utils.Cache.cachedClassified;

		Map<IConstraint, ConstraintType> result = new HashMap<IConstraint, ConstraintType>();
		int i = 0;
		for (IConstraint c : constraints) {
			if (result.containsKey(c)) {
				// System.out.println("Multiple constraints?? (id: " + i + ") ->
				// " + c);
				int j = 0;
				for (IConstraint c2 : result.keySet()) {
					if (java.util.Objects.equals(c, c2)) {
						// System.out.println("Found ya! j = " + j);
						// System.out.println(i + "'s internal id: " +
						// c.getInternalId());
						// System.out.println(j + "'s internal id: " +
						// c2.getInternalId());
					}
					j++;
				}

			}
			if (ComplexConstraintConverter.isSimple(c.getNode()))
				result.put(c, ConstraintType.SIMPLE);
			else if (ComplexConstraintConverter.isPseudoComplex(c.getNode()))
				result.put(c, ConstraintType.PSEUDO_COMPLEX);
			else
				result.put(c, ConstraintType.STRICT_COMPLEX);
			i++;
		}

		if (useCache) {
			Utils.Cache.cachedClassified = result;
			Utils.Cache.cachedConstraints = constraints;
		}
		return result;
	}

	/**
	 * 
	 * @param classified
	 * @param type
	 * @return
	 */
	public static int countConstraints(Map<IConstraint, ConstraintType> classified, ConstraintType type) {
		if (useCache) {
			if (classified != Utils.Cache.cachedClassified || (!Utils.Cache.calculated)) {
				Utils.Cache.fillCount(classified);
			}
			return Utils.Cache.count(type);
		}
		return Collections.frequency(new ArrayList<ConstraintType>(classified.values()), type);
	}

	/**
	 * 
	 * @param filename
	 * @return
	 * @throws NoSuchExtensionException
	 */
	public static IFeatureModel loadFeatureModel(String filename) throws Exception {
		return loadFeatureModel(filename, new XmlFeatureModelFormat());
	}

	/**
	 * 
	 * @param filename
	 * @param format
	 * @return
	 * @throws NoSuchExtensionException
	 */
	public static IFeatureModel loadFeatureModel(String filename, IFeatureModelFormat format) throws Exception {
		IFeatureModel fm = null;

		fm = FMFactoryManager.getFactory(filename, format).createFeatureModel();

		final ProblemList errors = FileHandler.load(Paths.get(filename), fm, format).getErrors();

		if (!errors.isEmpty()) {
			for (Problem p : errors) {
				System.err.println(p);
			}
		}

		return fm;
	}

	public static void writeFeatureModel(IFeatureModel fm, String filename) throws NoSuchExtensionException {
		final ProblemList errors = FileHandler.save(Paths.get(filename), fm, new XmlFeatureModelFormat()).getErrors();

		if (!errors.isEmpty()) {
			for (Problem p : errors) {
				System.err.println(p);
			}
		}
	}

	/**
	 * 
	 * @param filename
	 * @return
	 * @throws NoSuchExtensionException
	 */
	public static void writeFeatureModel(IFeatureModel fm, String filename, IFeatureModelFormat format)
			throws NoSuchExtensionException {
		final ProblemList errors = FileHandler.save(Paths.get(filename), fm, format).getErrors();

		if (!errors.isEmpty()) {
			for (Problem p : errors) {
				System.err.println(p);
			}
		}
	}

}
