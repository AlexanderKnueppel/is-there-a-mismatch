package main;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.prop4j.And;
import org.prop4j.Implies;
import org.prop4j.Literal;
import org.prop4j.Node;
import org.prop4j.NodeReader;
import org.prop4j.Not;
import org.prop4j.Or;
import org.prop4j.SatSolver;
import org.prop4j.solver.ModifiableSolver;
import org.prop4j.solver.SatInstance;
import org.prop4j.solver.ISatSolver.SatResult;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.TimeoutException;

import de.ovgu.featureide.fm.core.ConstraintAttribute;
import de.ovgu.featureide.fm.core.ExtensionManager.NoSuchExtensionException;
import de.ovgu.featureide.fm.core.FeatureModelAnalyzer;
import de.ovgu.featureide.fm.core.base.FeatureUtils;
import de.ovgu.featureide.fm.core.base.IConstraint;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.editing.AdvancedNodeCreator;
import de.ovgu.featureide.fm.core.editing.AdvancedNodeCreator.CNFType;
import de.ovgu.featureide.fm.core.editing.AdvancedNodeCreator.ModelType;
import de.ovgu.featureide.fm.core.functional.Functional;
import de.ovgu.featureide.fm.core.io.dimacs.DIMACSFormat;

public class RemoveRedundantConstraints {
	
	private static Node makeRegular(Node node) {
		Node regularCNFNode = node.toCNF();
		if (regularCNFNode instanceof And) {
			final Node[] children = regularCNFNode.getChildren();
			for (int i = 0; i < children.length; i++) {
				final Node child = children[i];
				if (child instanceof Literal) {
					children[i] = new Or(child);
				}
			}
		} else if (regularCNFNode instanceof Or) {
			regularCNFNode = new And(regularCNFNode);
		} else if (regularCNFNode instanceof Literal) {
			regularCNFNode = new And(new Or(regularCNFNode));
		}
		return regularCNFNode;
	}
	
	protected static IFeatureModel removeVoid(IFeatureModel fm)  {
		IFeatureModel clone = fm.clone();
		AdvancedNodeCreator nodeCreator = new AdvancedNodeCreator(fm);
		nodeCreator.setCnfType(CNFType.Regular);
		nodeCreator.setIncludeBooleanValues(false);
		nodeCreator.setUseOldNames(false);
		nodeCreator.setModelType(ModelType.OnlyStructure);
		SatInstance si = new SatInstance(nodeCreator.createNodes(), FeatureUtils.getFeatureNamesPreorder(fm));
		ModifiableSolver unsat = null;
		try {
			unsat = new ModifiableSolver(si);
		} catch (ContradictionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		for (IConstraint constraint : fm.getConstraints()) {
			Node cnf = makeRegular(constraint.getNode());

			List<IConstr> constraintMarkers = null;
			boolean satisfiable;
			try {
				constraintMarkers = unsat.addClauses(cnf);
				satisfiable = unsat.isSatisfiable() == SatResult.TRUE;
			} catch (ContradictionException e) {
				satisfiable = false;
			}

			if (!satisfiable) {
				if (constraintMarkers != null) {
					for (IConstr constr : constraintMarkers) {
						if (constr != null) {
							unsat.removeConstraint(constr);
						}
					}
					clone.removeConstraint(constraint);
				} else {
					clone.removeConstraint(constraint);
				}
			}
		}
		return clone;
	}
	
	protected static IFeatureModel removeRedundant3(IFeatureModel fm) {
		IFeatureModel clone = fm.clone();
		while(clone.getConstraintCount() > 0) {
			clone.removeConstraint(0);
		}
		int removed = 0, i = 0;
		for (IConstraint constraint : fm.getConstraints()) {
			Node cnf = makeRegular(constraint.getNode());
			AdvancedNodeCreator nodeCreator = new AdvancedNodeCreator(clone);
			Node check = new Implies(nodeCreator.createNodes(), cnf);
			SatSolver satsolver = new SatSolver(new Not(check), 10000);

			try {
				if(!satsolver.isSatisfiable()) {
					removed++;
				} else {
					clone.addConstraint(constraint);
				}
			} catch (TimeoutException e) {
				System.err.println(">Timeout: " + constraint + "\n" + e.getMessage());
			}
			System.out.println((++i) + "/" + fm.getConstraintCount());
		}
		return clone;
	}
	
	protected static IFeatureModel removeRedundant1(IFeatureModel fm) {
		FeatureModelAnalyzer analyzer = fm.getAnalyser();
		
		analyzer.calculateFeatures = true;
		analyzer.calculateConstraints = false;
		analyzer.calculateRedundantConstraints = true;
		analyzer.calculateTautologyConstraints = false;
		
		analyzer.analyzeFeatureModel(null);
		List<IConstraint> toRemove = new LinkedList<IConstraint>();
		int index = 0, i = 0;
		for (IConstraint c : fm.getConstraints()) {
			ConstraintAttribute attribute = c.getConstraintAttribute();
			
			if (attribute == ConstraintAttribute.REDUNDANT || attribute == ConstraintAttribute.TAUTOLOGY) {
				toRemove.add(c);
			} 
			System.out.println((++i) + "/" + fm.getConstraintCount());
		}

		for (IConstraint c : toRemove) {
			fm.removeConstraint(c);
		}
		
		System.out.println(toRemove.size() + " removed!");
		return fm;
	}
	
	protected static IFeatureModel removeRedundant2(IFeatureModel fm) {
		IFeatureModel clone = fm.clone();
		int removed = 0;
		int index = 0, i = 0;
		while(index < fm.getConstraintCount()) {
			IConstraint c = fm.getConstraints().get(index);
			clone.removeConstraint(c);
			final AdvancedNodeCreator nodeCreator = new AdvancedNodeCreator(clone);
			
			Node cnf = c.getNode().toCNF();
			Node check = new Implies(nodeCreator.createNodes(), cnf);

			SatSolver satsolver = new SatSolver(new Not(check), 10000);
			
			try {
				if(!satsolver.isSatisfiable()) {
					removed++;
				} else {
					clone.addConstraint(c, index);
				}
			} catch (TimeoutException e) {
				System.err.println(">Timeout: " + c + "\n" + e.getMessage());
			}
			System.out.println((++i) + "/" + fm.getConstraintCount());
			index++;
		}
		
		System.out.println(removed + " constraints removed.");
		return fm;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String path = "models/";
		
		File folder = new File(path);
		File[] listOfFiles = folder.listFiles();
		
		try {
			for(File file : listOfFiles) {
				System.out.println("Converting " + file.getName());
				IFeatureModel fm1 = util.Utils.loadFeatureModel(file.getAbsolutePath());
				IFeatureModel result = removeRedundant1(removeVoid(fm1));
				util.Utils.writeFeatureModel(result, "models.clean/" + file.getName());
			}
				
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}
}
