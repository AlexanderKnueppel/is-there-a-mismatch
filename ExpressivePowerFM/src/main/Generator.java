package main;
/* FeatureIDE - A Framework for Feature-Oriented Software Development
 * Copyright (C) 2005-2015  FeatureIDE team, University of Magdeburg, Germany
 *
 * This file is part of FeatureIDE.
 * 
 * FeatureIDE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * FeatureIDE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with FeatureIDE.  If not, see <http://www.gnu.org/licenses/>.
 *
 * See http://featureide.cs.ovgu.de/ for further information.
 */

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.prop4j.Implies;
import org.prop4j.Literal;
import org.prop4j.Not;
import org.sat4j.specs.TimeoutException;

import de.ovgu.featureide.fm.core.FeatureModelAnalyzer;
import de.ovgu.featureide.fm.core.base.IConstraint;
import de.ovgu.featureide.fm.core.base.IFeature;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.base.IFeatureStructure;
import de.ovgu.featureide.fm.core.base.impl.DefaultFeatureModelFactory;
import de.ovgu.featureide.fm.core.configuration.Configuration;
import de.ovgu.featureide.fm.core.configuration.Selection;
import de.ovgu.featureide.fm.core.configuration.SelectionNotPossibleException;
import de.ovgu.featureide.fm.core.editing.Comparison;
import de.ovgu.featureide.fm.core.editing.ModelComparator;
import util.FeatureModelCounter;
import util.Permutations;

/**
 * Generates all valid feature models for a given number of concrete features.
 * @author Jens Meinicke
 *
 */
public class Generator {

	/**
	 * The maximal length of sorted models.<br>
	 * The index equals the hash value of the model modulo this value.<br>
	 * This value is proportional to the required memory.
	 */
	private static final int MODULO = 100000;

	private static final boolean COUNT_INVALID_MODELS = false;
	private static final boolean COUNT_ABSTRACT_MODELS = true;
	
	/**
	 * The number of features with root included.
	 */
	private static int COUNT_FEATURES = 3;
	
	/**
	 * The index equals the number of features of the models at this position.<br>
	 * Does only contain models without cross-tree constraints
	 */
	private LinkedList<LinkedList<IFeatureModel>> models = new LinkedList<LinkedList<IFeatureModel>>();

	/**
	 * first index: The models with an equivalent hash value modulo the selected modulo value<br>
	 * second index: Differing models
	 */
	private ArrayList<LinkedList<FeatureModelCounter>> sortedModels = new ArrayList<LinkedList<FeatureModelCounter>>(MODULO);
	
	private Set<String> validConfigurations = new HashSet<String>();
	
	public ArrayList<LinkedList<FeatureModelCounter>> getSortedModels() {
		return sortedModels;
	}

	public static void main(String[] args) {		
		
		long time = System.currentTimeMillis();	
		final Generator builder = new Generator(COUNT_FEATURES);
		for (int i = 0;i <= COUNT_FEATURES;i++) {
			builder.getModels(i);
			builder.renameAllModels(i);			
		}			
		try {
			/* generating in multiple threads is to memory intensive */
			builder.createConstraints(COUNT_FEATURES);
		} catch (TimeoutException e1) {
			e1.printStackTrace();
		}
		time = System.currentTimeMillis() - time;
		System.out.println("Calculation for " + (COUNT_FEATURES-1) + " features finished("+time+" ms)");		
		
		for (int n = 2; n <= 6; n++) {
			
			BigInteger number = ValidModelCounter.count(n-1);
			
			if (COUNT_INVALID_MODELS) {
				number = BigInteger.valueOf((long) Math.pow(2, Math.pow(2, n-1)));
			}
			BigInteger counter = BigInteger.ZERO;
			if (n == COUNT_FEATURES) {
				builder.validConfigurations = generatePermutations(n, builder.validConfigurations);
				counter = BigInteger.valueOf(builder.validConfigurations.size());
				/*for (LinkedList<FeatureModelCounter> list : builder.getSortedModels()) {
					counter = counter.add(BigInteger.valueOf(list.size()));
				}*/
			} else {
				if (!COUNT_INVALID_MODELS) {
					counter = (n==2 ? BigInteger.valueOf(2) :
						  n==3 ? BigInteger.valueOf(10) :
						  n==4 ? BigInteger.valueOf(126) :
						  n==5 ? BigInteger.valueOf(2570) :
						  n==6 ? BigInteger.valueOf(113544) :
							  BigInteger.valueOf(-1));
				} else {
					counter = (n==2 ? BigInteger.valueOf(4) :
						  n==3 ? BigInteger.valueOf(16) :
						  n==4 ? BigInteger.valueOf(150) :
							  BigInteger.valueOf(-1));
				}
			}
			BigInteger percentage = counter.multiply(BigInteger.valueOf(100)).divide(number);
			System.out.println();
			System.out.print("Feature " + (n-1) + " -> #Models " + counter + "/" + number + " = " + percentage + "%");
			if (n == COUNT_FEATURES) {
				System.out.print(" RECALCULATED");
			
				try {
		            Files.write(Paths.get("result"+n+".txt"), builder.validConfigurations, Charset.defaultCharset());
		        } catch (IOException e) {
		            e.printStackTrace();
		        }
			}
		}
	}

	public Generator(int countFeaturesMax) {
		for (int j  = 0;j <= countFeaturesMax; j++) {
			models.add(new LinkedList<IFeatureModel>());
		}
		DefaultFeatureModelFactory factory = DefaultFeatureModelFactory.getInstance();
		IFeatureModel model = factory.createFeatureModel();
		model.getStructure().setRoot(factory.createFeature(model, "").getStructure());
		
		models.get(1).add(model);
	}

	/**
	 * Creates all possible models without constraints.
	 * @param countFeatures
	 * @return
	 */
	private LinkedList<IFeatureModel> getModels(int countFeatures) {
		if (!models.get(countFeatures).isEmpty()) {
			return models.get(countFeatures);
		}
		
		DefaultFeatureModelFactory factory = DefaultFeatureModelFactory.getInstance();
		
		// case: root is ALTERNATIVE		
		IFeatureModel model = factory.createFeatureModel();
		model.getStructure().setRoot(factory.createFeature(model, "").getStructure());
		model.getStructure().getRoot().setAlternative();
		model.getStructure().getRoot().setAbstract(false);
		LinkedList<IFeatureModel> newModels = addChildren(countFeatures - 1, model);
		models.get(countFeatures).addAll(newModels);
		
		// case: root is OR
		model = factory.createFeatureModel();
		model.getStructure().setRoot(factory.createFeature(model, "").getStructure());
		model.getStructure().getRoot().setOr();		
		model.getStructure().getRoot().setAbstract(false);
		newModels = addChildren(countFeatures - 1, model);
		models.get(countFeatures).addAll(newModels);
		
		// case: root is AND
		model = factory.createFeatureModel();
		model.getStructure().setRoot(factory.createFeature(model, "").getStructure());
		model.getStructure().getRoot().setAnd();	
		model.getStructure().getRoot().setAbstract(false);
		newModels = addChildren(countFeatures - 1, model);
		models.get(countFeatures).addAll(newModels);
		
		if(COUNT_ABSTRACT_MODELS){
			// case: root is ALTERNATIVE and ABSTRACT	
			model = factory.createFeatureModel();
			model.getStructure().setRoot(factory.createFeature(model, "").getStructure());
			model.getStructure().getRoot().setAlternative();	
			model.getStructure().getRoot().setAbstract(true);
			newModels = addChildren(countFeatures - 1, model);
			models.get(countFeatures).addAll(newModels);
				
			// case: root is OR and ABSTRACT
			model = factory.createFeatureModel();
			model.getStructure().setRoot(factory.createFeature(model, "").getStructure());
			model.getStructure().getRoot().setOr();		
			model.getStructure().getRoot().setAbstract(true);
			newModels = addChildren(countFeatures - 1, model);
			models.get(countFeatures).addAll(newModels);
					
			// case: root is AND and ABSTRACT
			model = factory.createFeatureModel();
			model.getStructure().setRoot(factory.createFeature(model, "").getStructure());
			model.getStructure().getRoot().setAnd();		
			model.getStructure().getRoot().setAbstract(true);
			newModels = addChildren(countFeatures - 1, model);
			models.get(countFeatures).addAll(newModels);
		}
		
		return models.get(countFeatures);
	}
	
	private LinkedList<IFeatureModel> addChildren(int countFeatures, IFeatureModel model) {
		LinkedList<IFeatureModel> newModels = new LinkedList<IFeatureModel>();
		if (countFeatures == 0) {
			newModels.add(model);
			return newModels;
		}
		for (int j = 1;j <= countFeatures; j++) {
			for (IFeatureModel m : models.get(j)) {
				IFeature root = m.getStructure().getRoot().getFeature();
				if (model.getStructure().getRoot().isAnd()) {				
					
					// case: new child is optional
					IFeatureModel clone = model.clone();
					IFeature child = root.clone(clone, root.getStructure().cloneSubtree(clone));
					clone.getStructure().getRoot().addChild(child.getStructure());
					child.getStructure().setMandatory(false);
					newModels.addAll(addChildren(countFeatures - j, clone));

					// case: new child is mandatory
					clone = model.clone();
					child = root.clone(clone, root.getStructure().cloneSubtree(clone));
					clone.getStructure().getRoot().addChild(child.getStructure());
					child.getStructure().setMandatory(true);
					newModels.addAll(addChildren(countFeatures - j, clone));					
					
				} else if (model.getStructure().getRoot().hasChildren() || countFeatures - j != 0) {
					IFeatureModel clone = model.clone();
					IFeature child = root.clone(clone, root.getStructure().cloneSubtree(clone));
					clone.getStructure().getRoot().addChild(child.getStructure());
					newModels.addAll(addChildren(countFeatures - j, clone));
				}
			}
		}
		return newModels;
	}
	
	/**
	 * Sets the feature names of all generated Models without constraints to F0,F1,F2...
	 * @param i
	 */
	private void renameAllModels(int i) {
		for (IFeatureModel m : models.get(i)) {
			name  = 0;
			IFeature root = m.getStructure().getRoot().getFeature();
			rename(root);				
			initTable(root, m);			
		}
	}
	
	private int name = 0;
	private void rename(IFeature feature) {
		feature.setName("F" + name++);
		if (!feature.getStructure().hasChildren()) {
			return;
		}
		
		for (IFeatureStructure child : feature.getStructure().getChildren()) {
			if (child.hasChildren()) {
				rename(child.getFeature());
			} else {
				child.getFeature().setName("F" + name++);
			}
		}
	}
	
	private void initTable(IFeature feature, IFeatureModel m) {
		m.addFeature(feature);
		for (IFeatureStructure child : feature.getStructure().getChildren()) {
			initTable(child.getFeature(), m);
		}
	}
	

	private int counter = 0;
	/**
	 * Adds constraints to all generated models.
	 */
	private void createConstraints(int i) throws TimeoutException {		
		while (true) {
			IFeatureModel mc = getModel(i);
			if (mc == null) {
				return;
			}
			final LinkedList<IFeatureModel> newModelsWithConstraints = new LinkedList<IFeatureModel>();
			newModelsWithConstraints.add(mc);
			int output = 1000;
			System.out.println("Generate models for: "+print(mc));
			for (int j  = 0;j < i;j++) {
				System.out.print('*');
				for (int k = 0; k < i; k++) {
					System.out.print('.');
					if (k != j) {
						LinkedList<IFeatureModel> newModelsWithConstraints2 = new LinkedList<IFeatureModel>();
						for (IFeatureModel m : newModelsWithConstraints) {
							
							DefaultFeatureModelFactory factory = DefaultFeatureModelFactory.getInstance();
							
							// case: A ==> B
							IFeatureModel model = m.clone();							
							model.addConstraint(factory.createConstraint(model, new Implies(new Literal("F" + j),new Literal("F"+ k))));	
							FeatureModelAnalyzer analyser = model.getAnalyser();
							
							output++;
							if (output>1000) {
								output = 0;
								System.out.print('!');
							}
							if (!COUNT_INVALID_MODELS) {
								if (analyser.isValid() &&
										analyser.getDeadFeatures(1000000).isEmpty() &&
										!isRedundant(m, model)) {
									newModelsWithConstraints2.add(model);
								}
							} else {
								if (!isRedundant(model, m)) {
									newModelsWithConstraints2.add(model);
								}
							}
							if (j > k) {
								// Avoid duplicate Models
								continue;
							}
							
							output++;
							if (output>1000) {
								output = 0;
								System.out.print('!');
							}
							
							// case: A ==> !B
							model = m.clone();
							analyser = model.getAnalyser();
							model.addConstraint(factory.createConstraint(model, new Implies(new Literal("F" + j),new Not(new Literal("F"+ k)))));
							if (!COUNT_INVALID_MODELS) {
								if (analyser.isValid() &&
										analyser.getDeadFeatures(1000000).isEmpty() &&
										!isRedundant(m, model)) {
									newModelsWithConstraints2.add(model);
								}
							} else {
								if (!isRedundant(model, m)) {									
									newModelsWithConstraints2.add(model);
								}
							}
						}
						newModelsWithConstraints.addAll(newModelsWithConstraints2);
					}
				}
			}
						
			if (configurations.isEmpty()) 
				initConfigurations(newModelsWithConstraints.getFirst());
			
			System.out.println();
			System.out.print("Convert to hash (" + newModelsWithConstraints.size() + ") ");
			output = 1000;
			LinkedList<FeatureModelCounter> counterModels = new LinkedList<FeatureModelCounter>(); 
			for (IFeatureModel m : newModelsWithConstraints) {
				
				output++;
				if (output > 1000) {
					output = 0;
					System.out.print('.');
				}
				Double hashCode = hashCode(m);
				FeatureModelCounter c = new FeatureModelCounter(hashCode);
				
				boolean found = false;
				for (FeatureModelCounter fmc : counterModels) 
					if (fmc.equals(c)) 
						found = true;
					
				if (!found) 
					counterModels.add(c);
				
			}
			newModelsWithConstraints.clear();
			System.out.println();
			insert(counterModels);
			
			System.out.println("Features: " + i + " " + counter++ + "/" + models.get(i).size());	
		}
	}
	
	private boolean isRedundant(IFeatureModel fm, IFeatureModel dirtyModel) {
		ModelComparator comparator = new ModelComparator(10000);
		Comparison comparison = comparator.compare(fm, dirtyModel);
		if (comparison == Comparison.REFACTORING) {
			return true;
		}
		return false;
	}

	/**
	 * Insets all calculated models into sortedModels.
	 */
	private void insert(LinkedList<FeatureModelCounter> newModelsWithConstraints) {
		
		System.out.print("insert "+ newModelsWithConstraints.size() + " ");
		int output = 0;
		for (FeatureModelCounter newModel : newModelsWithConstraints) {
			int number = (int) (newModel.hashCode%MODULO);
			if (number < 0) 
				number = number*-1;
			
			while (sortedModels.size() <= number) 
				sortedModels.add(new LinkedList<FeatureModelCounter>());
			
			if (sortedModels.get(number).isEmpty()) {
				sortedModels.get(number).add(newModel);
			} else {
				boolean found = false;
				for (FeatureModelCounter hash : sortedModels.get(number)) {
					if (hash.equals(newModel.hashCode)) {
						found = true;
						break;
					}
				}
				if (!found) {
					sortedModels.get(number).add(newModel);
				}
			}
			if ((output++)%1000 == 0) {
				System.out.print('.');
			}
		}
		System.out.println();
	}
	
	private LinkedList<Configuration> configurations = new LinkedList<Configuration>();

	/**
	 * Generate a hash value and a output for the given model.
	 */
	private Double hashCode(IFeatureModel newModel) {		
		Double hashCode = 0.0;
		Double multiplier = 1.0;
		List<String> subStrings = new LinkedList<String>();
		
		for (Configuration conf : configurations) {			
			boolean possible = true;
			Configuration conf2 = new Configuration(newModel, false, !COUNT_ABSTRACT_MODELS);
			
			for(IFeature feature : newModel.getFeatures()){
				Selection selection = conf.getSelectablefeature(feature.getName()).getSelection();
				if(selection == Selection.UNDEFINED) selection = Selection.UNSELECTED;
				try{
					conf2.setManual(feature.getName(), selection);
				}catch(SelectionNotPossibleException e){
					possible = false;
					break;
				}
			}
			
			if (conf2.isValid() && possible){
				String str = "{";
				for(IFeature f : conf2.getSelectedFeatures()) 
					str += f.getName() + " ";
				
                if(str.length() > 1){
                	str = str.substring(0, str.length()-1) + "}";
                	subStrings.add(str);
                }
				hashCode += multiplier;
			}
			multiplier = multiplier*2;
		}
		if(!subStrings.isEmpty()){
			Collections.sort(subStrings);
			String result = "";
			for(String subString : subStrings)
				result += subString;
			validConfigurations.add(result);
		}		
		return hashCode;
	}
	
	/**
	 * Initializes configurations for calculation of the hash values.
	 */
	public void initConfigurations(IFeatureModel model) {
		Configuration c = new Configuration(model, false, !COUNT_ABSTRACT_MODELS);
		c.setManual(model.getStructure().getRoot().getFeature().getName(), Selection.SELECTED);
		configurations.add(c);
		
		LinkedList<String> featureNames = new LinkedList<String>();
		
		for (IFeature feature : model.getFeatures()){
			String name = feature.getName();
			if(!featureNames.contains(name)) featureNames.add(name);
		}		
		for (String feature : featureNames) {
			if (feature.equals("") || feature.equals(model.getStructure().getRoot().getFeature().getName())) {
				continue;
			}
			LinkedList<Configuration> toAdd = new LinkedList<Configuration>();
			for (Configuration conf : configurations) {
				Configuration configuration = conf.clone();
				configuration.setManual(feature, Selection.SELECTED);
				toAdd.add(configuration);
			}
			configurations.addAll(toAdd);
		}
	}
	
	private IFeatureModel getModel(int i) {
		if (models.get(i).isEmpty()) {
			return null;
		}
		return models.get(i).removeLast();		
	}
	
	private String print(IFeatureModel fm) {
		String x = printFeatures(fm.getStructure().getRoot());
		for (IConstraint c : fm.getConstraints()) {
			x +=c.toString() + " ";
		}
		return x;
	}

	private String printFeatures(IFeatureStructure feature) {
		String x = feature.getFeature().getName();
		if (!feature.hasChildren()) {
			return x;
		}
		if (feature.isOr()) {
			x += " or [";
		} else if (feature.isAlternative()) {
			x += " alt [";
		} else {
			x += " and [";
		}
		
		for (IFeatureStructure child : feature.getChildren()) {
			x += " ";
			if (feature.isAnd()) {
				if (child.isMandatory()) {
					x += "M ";
				} else {
					x += "O ";
				}
			}
			
			if (child.hasChildren()) {
				x += printFeatures(child);
			} else {
				x += child.getFeature().getName();
			}
		}
		return x + " ] ";
	}
	
	/**
	 * @param n - Number of features
	 * @param configs - Set of Strings to permute
	 * @return result - Set of Strings including input Strings and permuted Strings 
	 */	
	private static Set<String> generatePermutations(int n, Set<String> configs){
		Set<String> result = new HashSet<String>();
		
		/* Generate Arrays */
		String[] features = new String[n-1];
		for(int i=1; i<n; i++)
			features[i-1] = "F"+i;	
		
		Permutations<String> perm = new Permutations<String>(features);		
		while(perm.hasNext()){
			String[] permuted = perm.next();
			
			for(String toPerm : configs){					
				
				/* Generate Permutation */
				String permed = "";
				for(int i=0; i<toPerm.length(); i++){
					if(Character.isDigit(toPerm.charAt(i)) && toPerm.charAt(i) != '0'){
						for(int j=0; j<features.length; j++){
							if(features[j].contains(toPerm.charAt(i)+"")){
								permed += permuted[j].charAt(1);
							}
						}
					}else
						permed += toPerm.charAt(i);
				}
				
				/* Sort Permuted String */
				String clone = permed;
				String[] subStrings = new String[clone.length() - clone.replace("{", "").length()];
				
				for(int i=0; i<subStrings.length; i++){					
					int start = clone.indexOf('{');
					int end = clone.indexOf('}');

					String subStr = clone.substring(start+1, end);
					
					subStr = subStr.replaceAll("\\D", "");
					int[] indizes = new int[subStr.length()];					
					for(int j=0; j<subStr.length(); j++)
						indizes[j] = Integer.parseInt(subStr.charAt(j)+"");
					
					for(int j=0; j<indizes.length; j++)
						for(int z=0; z<indizes.length-1; z++)
							if(indizes[z] > indizes[z+1]){
								int tmp = indizes[z];
								indizes[z] = indizes[z+1];
								indizes[z+1] = tmp;
							}
					
					subStr = "{";
					for(int j=0; j<indizes.length; j++)
						subStr += "F"+indizes[j]+" ";					
					subStr = subStr.substring(0, subStr.length()-1) +"}";
					
					subStrings[i] = subStr;
					clone = clone.substring(end+1);					
				}
				List<String> list = new LinkedList<String>();
				for(int i=0; i<subStrings.length; i++)
					if(!list.contains(subStrings[i])) list.add(subStrings[i]);				
				Collections.sort(list);
				
				clone = "";
				for(String str : list)
					clone += str;
				
				result.add(clone);
			}
		}
		return result;
	}

}
