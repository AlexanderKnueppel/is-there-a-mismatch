# Is There a Mismatch Between Real-World Feature Models and Product-Line Research?

Artifact repository for the FSE2017 accepted paper "Is There a Mismatch Between Real-World Feature Models and Product-Line Research?".
Authors: Alexander Knüppel, Thomas Thüm, Stephan Mennicke, Jens Meinicke, Ina Schaefer

Included are various tools for the analysis of real-world feature models with respect to their cross-tree constraints.
See index.html for further details.

Many many thanks to Thorsten Berger for the FeatureIDE exporter.

TODO: add a more sophisticated guide (and possible tool chain) for the translation from KConfig and CDL to the FeatureIDE file format.

### Overview

* Data/Output/

   Pre-generated evaluation statistics.

* Data/LargeFeatureModels/

   Contains 127 large feature models in the FeatureIDE file format.

* Data/LargeBasicFeatureModels/

   Contains 127 large feature models with only simple constraints in the FeatureIDE file format.

* Source/ExpressivePowerFM/

   Java Eclipse project for calculating the number of software product lines that can be represented with n features.

* Source/EmpericalEval/

   Java Eclipse project for generating all statistics that are presented in the paper.

* Source/KConfigTranslator/

   Extended LVAT project by Steven She for converting KConfig (exconfig) files to the FeatureIDe file format.

* Documentation/

   Assets and subpages for the documentation.
