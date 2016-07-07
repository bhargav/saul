# Relation Extraction

The main features used are based upon the following paper:

```
  @inproceedings{chan2010exploiting,
    title={Exploiting background knowledge for relation extraction},
    author={Chan, Yee Seng and Roth, Dan},
    booktitle={Proceedings of the 23rd International Conference on Computational Linguistics},
    pages={152--160},
    year={2010},
    organization={Association for Computational Linguistics}
  }
```

## Performance

The data for the experiments was extracted from the ACE2005 corpus. We only used the
English Newswire and Broadcast News documents and performed 5 fold CV for each scenario.

--------------------------------------------------------------------------------
Experiment 1 - Mention Type Classifier 

- Classifiers trained on SVM classifier for 5 iterations

***Classifier: Mention Coarse Classifier***

5 Fold | Precision | Recall | F1
--- | ---: | ---: | ---:
Fold 0 | 0.7757085020242915 | 0.7184101987251593 | 0.7459606774381935
Fold 1 | 0.7758722649319929 | 0.7210772190162132 | 0.7474718701039736
Fold 2 | 0.7518087448883297 | 0.6897546897546898 | 0.7194461167971102
Fold 3 | 0.7609349687572321 | 0.6857142857142857 | 0.7213690215006581
Fold 4 | 0.7932011331444759 | 0.7168254871284312 | 0.7530818079940231
***Average %*** | 

***Classifier: Mention Fine Classifier***

5 Fold | Precision | Recall | F1
--- | ---: | ---: | ---:
Fold 0 | 0.7387536112257532 | 0.6711661042369704 | 0.7033398821218075
Fold 1 | 0.73502722323049   | 0.6677658697444353 | 0.6997840172786177
Fold 2 | 0.709792477302205  | 0.6317460317460317 | 0.6684990074820583
Fold 3 | 0.7293556085918854 | 0.6373305526590198 | 0.6802448525319978
Fold 4 | 0.7617651848233187 | 0.6653392120608733 | 0.7102945642271485
***Average %*** | 

--------------------------------------------------------------------------------
Experiment 2 - Relation Type Classifier

- Classifiers trained using a SparseNetworkLearner for 5 iterations

***Classifier: Relation Coarse Classifier***

5 Fold | Precision | Recall | F1
--- | ---: | ---: | ---:
Fold 0 | 0.6861538461538461 | 0.501123595505618   | 0.5792207792207793
Fold 1 | 0.6469038208168643 | 0.44636363636363635 | 0.5282409897794513
Fold 2 | 0.6495575221238938 | 0.43483412322274884 | 0.5209368346344926
Fold 3 | 0.6524886877828054 | 0.44506172839506175 | 0.5291743119266055
Fold 4 | 0.6592627599243857 | 0.49363057324840764 | 0.5645487656819101
***Average %*** | 

***Classifier: Relation Fine Classifier***

5 Fold | Precision | Recall | F1
--- | ---: | ---: | ---:
Fold 0 | 0.6270096463022508 | 0.43820224719101125 | 0.5158730158730159
Fold 1 | 0.6074895977808599 | 0.3981818181818182  | 0.48105436573311366
Fold 2 | 0.6266666666666667 | 0.38981042654028436 | 0.4806428049671293
Fold 3 | 0.6275264677574591 | 0.4024691358024691  | 0.49040992854456567
Fold 4 | 0.6069277108433735 | 0.4278131634819533  | 0.50186799501868
***Average %*** | 

***Classifier: Relation Fine + Relation Hierarchy Constraint***

5 Fold | Precision | Recall | F1
--- | ---: | ---: | ---:
Fold 0 | 0.6298701298701299 | 0.43595505617977526 | 0.5152722443559097
Fold 1 | 0.6033519553072626 | 0.3927272727272727  | 0.47577092511013214
Fold 2 | 0.5938697318007663 | 0.36729857819905215 | 0.4538799414348463
Fold 3 | 0.611969111969112  | 0.391358024691358   | 0.4774096385542169
Fold 4 | 0.58646998982706   | 0.40799716914366596 | 0.4812186978297162
***Average %*** | 

--------------------------------------------------------------------------------
Experiment 3 - Relation Type Classifier + Brown Cluster Features

- Classifiers trained using a SparseNetworkLearner for 5 iterations

***Classifier: Relation Coarse Classifier [With Brown Cluster Features]***

5 Fold | Precision | Recall | F1
--- | ---: | ---: | ---:
Fold 0 | 0.690068493150685  | 0.45280898876404496 | 0.5468113975576663
Fold 1 | 0.6952662721893491 | 0.42727272727272725 | 0.5292792792792793
Fold 2 | 0.7016460905349794 | 0.4040284360189573  | 0.512781954887218
Fold 3 | 0.6827731092436975 | 0.4012345679012346  | 0.505443234836703
Fold 4 | 0.6780748663101605 | 0.448690728945506   | 0.5400340715502556
***Average %*** | 

***Classifier: Relation Fine Classifier [With Brown Cluster Features]***

5 Fold | Precision | Recall | F1
--- | ---: | ---: | ---:
Fold 0 | 0.6500920810313076 | 0.39662921348314606 | 0.4926727145847872
Fold 1 | 0.6405529953917051 | 0.3790909090909091  | 0.4762992575671045
Fold 2 | 0.642691415313225  | 0.3281990521327014  | 0.4345098039215686
Fold 3 | 0.6336302895322939 | 0.3512345679012346  | 0.45194598888006354
Fold 4 | 0.6061120543293718 | 0.37898089171974525 | 0.46636185499673416
***Average %*** | 

***Classifier: Relation Fine + Relation Hierarchy Constraint Classifier [With Brown Cluster Features]***

5 Fold | Precision | Recall | F1
--- | ---: | ---: | ---:
Fold 0 | 0.6333333333333333 | 0.3842696629213483  | 0.4783216783216783
Fold 1 | 0.6251968503937008 | 0.3609090909090909  | 0.4576368876080691
Fold 2 | 0.6284403669724771 | 0.3246445497630332  | 0.4281250000000001
Fold 3 | 0.622895622895623  | 0.3425925925925926  | 0.44205495818399043
Fold 4 | 0.5957200694042799 | 0.36447275300778487 | 0.4522502744237102
***Average %*** | 

--------------------------------------------------------------------------------

## Running the code for experimentation:

The experiments expect the dataset to be available at `data/ace2005` folder, according to the requirements of the [ACE Reader](https://github.com/IllinoisCogComp/illinois-cogcomp-nlp/blob/master/corpusreaders/doc/ACEReader.md)

We read the original document and the annotation XML to build a `TextAnnotation` instance
for each document and serialize it to be cached for later use. This cache enables faster loading of dataset.

To run the Mention Type Classifiers, run the following command from the project root /
or the sbt console accordingly.

```scala
  sbt runMain edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction.RelationExtractionApp MentionCV
```

To run the Relation Type Classifier CV,

```scala
  sbt runMain edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction.RelationExtractionApp RelationCV
```

To run the Relation Type Classifier With Brown Cluster Features CV,

```scala
  sbt runMain edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction.RelationExtractionApp RelationCVWithBrownClusterFeatures
```
