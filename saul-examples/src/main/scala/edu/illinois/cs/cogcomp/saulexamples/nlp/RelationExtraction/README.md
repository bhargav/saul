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

The data for the experiments was extracted from the ACE2004 corpus. We only used the
English Newswire documents and performed 5 fold CV for each scenario.

ACE2004-English-Newswire corpus has 128 documents, generating 2,905 sentence with 11,033 entity mentions
and 2,037 relations.

--------------------------------------------------------------------------------
Experiment 1 - Mention Type Classifier

Classifier: Mention Coarse Fold 0 - Precision: 0.7551020408163265 // Recall: 0.6036925719192786 // F1: 0.670961584347411
Classifier: Mention Coarse Fold 1 - Precision: 0.8031980319803198 // Recall: 0.6183712121212122 // F1: 0.6987693953986089
Classifier: Mention Coarse Fold 2 - Precision: 0.7604395604395604 // Recall: 0.6048951048951049 // F1: 0.673807205452775
Classifier: Mention Coarse Fold 3 - Precision: 0.7542168674698795 // Recall: 0.5999041686631529 // F1: 0.6682679476914866
Classifier: Mention Coarse Fold 4 - Precision: 0.7882147024504084 // Recall: 0.609382047812359 // F1: 0.6873569066395319
Average Precision: : 77.2234240631299
Average Recall: : 60.724902108222146
Average F1: : 67.98326079059626

Classifier: Mention Fine Fold 0 - Precision: 0.7448467966573816 // Recall: 0.5740661227994848 // F1: 0.6483996120271581
Classifier: Mention Fine Fold 1 - Precision: 0.7843388960205392 // Recall: 0.5785984848484849 // F1: 0.6659400544959129
Classifier: Mention Fine Fold 2 - Precision: 0.7360953461975028 // Recall: 0.5668706293706294 // F1: 0.6404938271604939
Classifier: Mention Fine Fold 3 - Precision: 0.7418546365914787 // Recall: 0.5673215141351222 // F1: 0.6429541134944339
Classifier: Mention Fine Fold 4 - Precision: 0.7714113389626055 // Recall: 0.5769057284618855 // F1: 0.6601290322580645
Average Precision: : 75.57094028859015
Average Recall: : 57.275249592312136
Average F1: : 65.15833278872127

--------------------------------------------------------------------------------
Experiment 2 - Relation Type Classifier

Classifier: Relation Coarse Fold 0 - Precision: 0.7323529411764705 // Recall: 0.538961038961039 // F1: 0.6209476309226932
Classifier: Relation Coarse Fold 1 - Precision: 0.7119741100323624 // Recall: 0.623229461756374 // F1: 0.6646525679758309
Classifier: Relation Coarse Fold 2 - Precision: 0.6863354037267081 // Recall: 0.5249406175771971 // F1: 0.594885598923284
Classifier: Relation Coarse Fold 3 - Precision: 0.7444794952681388 // Recall: 0.5959595959595959 // F1: 0.6619915848527349
Classifier: Relation Coarse Fold 4 - Precision: 0.710691823899371 // Recall: 0.5580246913580247 // F1: 0.6251728907330566
Average Precision: : 71.71667548206104
Average Recall: : 56.82230811224461
Average F1: : 63.35300546815199

Classifier: Relation Fine Fold 0 - Precision: 0.6064516129032258 // Recall: 0.4069264069264069 // F1: 0.48704663212435234
Classifier: Relation Fine Fold 1 - Precision: 0.5927272727272728 // Recall: 0.46175637393767704 // F1: 0.519108280254777
Classifier: Relation Fine Fold 2 - Precision: 0.5793103448275863 // Recall: 0.3990498812351544 // F1: 0.47257383966244726
Classifier: Relation Fine Fold 3 - Precision: 0.6236933797909407 // Recall: 0.45202020202020204 // F1: 0.5241581259150806
Classifier: Relation Fine Fold 4 - Precision: 0.5577557755775577 // Recall: 0.41728395061728396 // F1: 0.4774011299435028
Average Precision: : 59.19876771653166
Average Recall: : 42.74073629473449
Average F1: : 49.60576015800321

Classifier: Relation Hierarchy Constraint Fold 0 - Precision: 0.6097560975609756 // Recall: 0.4329004329004329 // F1: 0.5063291139240506
Classifier: Relation Hierarchy Constraint Fold 1 - Precision: 0.5728813559322034 // Recall: 0.47875354107648727 // F1: 0.5216049382716049
Classifier: Relation Hierarchy Constraint Fold 2 - Precision: 0.5493421052631579 // Recall: 0.39667458432304037 // F1: 0.4606896551724138
Classifier: Relation Hierarchy Constraint Fold 3 - Precision: 0.618421052631579 // Recall: 0.47474747474747475 // F1: 0.5371428571428571
Classifier: Relation Hierarchy Constraint Fold 4 - Precision: 0.5602605863192183 // Recall: 0.4246913580246914 // F1: 0.48314606741573035
Average Precision: : 58.21322395414269
Average Recall: : 44.155347821442525
Average F1: : 50.17825263853314

--------------------------------------------------------------------------------
Experiment 3 - Relation Type Classifier + Brown Cluster Features

Classifier: Relation Coarse Fold 0 - Precision: 0.749271137026239 // Recall: 0.5562770562770563 // F1: 0.6385093167701863
Classifier: Relation Coarse Fold 1 - Precision: 0.7251655629139073 // Recall: 0.6203966005665722 // F1: 0.6687022900763359
Classifier: Relation Coarse Fold 2 - Precision: 0.7032258064516129 // Recall: 0.517814726840855 // F1: 0.5964432284541724
Classifier: Relation Coarse Fold 3 - Precision: 0.755700325732899 // Recall: 0.5858585858585859 // F1: 0.6600284495021337
Classifier: Relation Coarse Fold 4 - Precision: 0.7188498402555911 // Recall: 0.5555555555555556 // F1: 0.6267409470752089
Average Precision: : 73.04425344760499
Average Recall: : 56.71805050197249
Average F1: : 63.80848463756074

Classifier: Relation Fine Fold 0 - Precision: 0.6428571428571429 // Recall: 0.42857142857142855 // F1: 0.5142857142857143
Classifier: Relation Fine Fold 1 - Precision: 0.6297709923664122 // Recall: 0.46742209631728043 // F1: 0.5365853658536585
Classifier: Relation Fine Fold 2 - Precision: 0.6035714285714285 // Recall: 0.4014251781472684 // F1: 0.4821683309557775
Classifier: Relation Fine Fold 3 - Precision: 0.6431095406360424 // Recall: 0.4595959595959596 // F1: 0.5360824742268041
Classifier: Relation Fine Fold 4 - Precision: 0.5993031358885017 // Recall: 0.4246913580246914 // F1: 0.4971098265895954
Average Precision: : 62.37224480639056
Average Recall: : 43.63412041313257
Average F1: : 51.324634238231

Classifier: Relation Hierarchy Constraint Fold 0 - Precision: 0.6349693251533742 // Recall: 0.44805194805194803 // F1: 0.5253807106598984
Classifier: Relation Hierarchy Constraint Fold 1 - Precision: 0.5902777777777778 // Recall: 0.48158640226628896 // F1: 0.5304212168486739
Classifier: Relation Hierarchy Constraint Fold 2 - Precision: 0.5771812080536913 // Recall: 0.4085510688836104 // F1: 0.47844228094575797
Classifier: Relation Hierarchy Constraint Fold 3 - Precision: 0.6450511945392492 // Recall: 0.4772727272727273 // F1: 0.5486211901306242
Classifier: Relation Hierarchy Constraint Fold 4 - Precision: 0.5813953488372093 // Recall: 0.43209876543209874 // F1: 0.4957507082152975
Average Precision: : 60.577497087226035
Average Recall: : 44.95121823813348
Average F1: : 51.57232213600504

--------------------------------------------------------------------------------

## Running the code for experimentation:

The experiments expect the dataset to be available at `data/ace2004` folder and an
`data/ace2004/allfiles.txt` file containing the relative path to all documents to be
processed.

Sample `allfiles.txt` content:
```
  ace2004/nwire/APW20001001.2021.0521
  ace2004/nwire/APW20001002.0615.0146
  ace2004/nwire/APW20001002.1912.0524
  ace2004/nwire/APW20001006.0338.0184
```

We read the original document and the annotation XML to build a `TextAnnotation` instance
for each document and serialize it to be cached for later use. This cache is stored
in the `cache/` folder and enables faster loading of dataset.

To run the Mention Type Classifiers, run the following command from the project root /
or the sbt console accordingly.

```scala
  sbt runMain edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction.RelationExtractionApp RunMentionCV
```

To run the Relation Type Classifier CV,

```scala
  sbt runMain edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction.RelationExtractionApp RunRelationCV
```

To run the Relation Type Classifier With Brown Cluster Features CV,

```scala
  sbt runMain edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction.RelationExtractionApp RunRelationCVWithBrownClusterFeatures
```
