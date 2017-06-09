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

// TODO: Auto-generated Javadoc
/**
 * The Class Utils.
 */
public class Utils {
	
	/**  Configuration. */
	public static boolean useCache = true;

	/**
	 * The Enum ConstraintType.
	 *
	 * @author User
	 */
	public enum ConstraintType {
		
		/** The strict complex. */
		STRICT_COMPLEX, 
 /** The pseudo complex. */
 PSEUDO_COMPLEX, 
 /** The simple. */
 SIMPLE
	}

	/**
	 * The Class Cache.
	 *
	 * @author User
	 */
	public static class Cache {
		
		/** The cached constraints. */
		public static List<IConstraint> cachedConstraints = null;
		
		/** The cached classified. */
		public static Map<IConstraint, ConstraintType> cachedClassified = null;
		
		/** The cached strict complex constraints. */
		public static int cachedSimpleConstraints = 0, cachedPseudoComplexConstraints = 0,
				cachedStrictComplexConstraints = 0;
		
		/** The calculated. */
		public static boolean calculated = false;

		/**
		 * Count.
		 *
		 * @param type the type
		 * @return the int
		 */
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

		/**
		 * Fill count.
		 *
		 * @param classified the classified
		 */
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

	/**
	 * Classify.
	 *
	 * @param fm the fm
	 * @return the map
	 */
	public static Map<IConstraint, ConstraintType> classify(IFeatureModel fm) {
		return Utils.classify(fm.getConstraints());
	}

	/**
	 * Minimal number of literals.
	 *
	 * @param c the c
	 * @return the int
	 */
	public static int minimalNumberOfLiterals(IConstraint c) {
		int nnf = numberOfLiterals(propagateNegation(c.getNode(), false));
		int cnf = numberOfLiterals(c.getNode().toCNF());

		return nnf < cnf ? nnf : cnf;
	}

	/**
	 * Number of literals.
	 *
	 * @param n the n
	 * @return the int
	 */
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

	/**
	 * Propagate negation.
	 *
	 * @param node the node
	 * @param negated the negated
	 * @return the node
	 */
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

	/**
	 * Classify.
	 *
	 * @param constraints the constraints
	 * @return the map
	 */
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
	 * Count constraints.
	 *
	 * @param classified the classified
	 * @param type the type
	 * @return the int
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
	 * Load feature model.
	 *
	 * @param filename the filename
	 * @return the i feature model
	 * @throws Exception the exception
	 */
	public static IFeatureModel loadFeatureModel(String filename) throws Exception {
		return loadFeatureModel(filename, new XmlFeatureModelFormat());
	}

	/**
	 * Load feature model.
	 *
	 * @param filename the filename
	 * @param format the format
	 * @return the i feature model
	 * @throws Exception the exception
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

	/**
	 * Write feature model.
	 *
	 * @param fm the fm
	 * @param filename the filename
	 * @throws NoSuchExtensionException the no such extension exception
	 */
	public static void writeFeatureModel(IFeatureModel fm, String filename) throws NoSuchExtensionException {
		final ProblemList errors = FileHandler.save(Paths.get(filename), fm, new XmlFeatureModelFormat()).getErrors();

		if (!errors.isEmpty()) {
			for (Problem p : errors) {
				System.err.println(p);
			}
		}
	}

	/**
	 * Write feature model.
	 *
	 * @param fm the fm
	 * @param filename the filename
	 * @param format the format
	 * @throws NoSuchExtensionException the no such extension exception
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
