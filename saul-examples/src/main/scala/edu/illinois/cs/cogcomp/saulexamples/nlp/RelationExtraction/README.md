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

***Classifier: Mention Coarse Classifier***

5 Fold | Precision | Recall | F1
--- | ---: | ---: | ---:
Fold 0 | 0.7551020408163265 | 0.6036925719192786 | 0.670961584347411
Fold 1 | 0.8031980319803198 | 0.6183712121212122 | 0.6987693953986089
Fold 2 | 0.7604395604395604 | 0.6048951048951049 | 0.673807205452775
Fold 3 | 0.7542168674698795 | 0.5999041686631529 | 0.6682679476914866
Fold 4 | 0.7882147024504084 | 0.609382047812359 | 0.6873569066395319
***Average %*** | 77.2234240631299 | 60.724902108222146 | 67.98326079059626

***Classifier: Mention Fine Classifier***

5 Fold | Precision | Recall | F1
--- | ---: | ---: | ---:
Fold 0 | 0.7448467966573816 | 0.5740661227994848 | 0.6483996120271581
Fold 1 | 0.7843388960205392 | 0.5785984848484849 | 0.6659400544959129
Fold 2 | 0.7360953461975028 | 0.5668706293706294 | 0.6404938271604939
Fold 3 | 0.7418546365914787 | 0.5673215141351222 | 0.6429541134944339
Fold 4 | 0.7714113389626055 | 0.5769057284618855 | 0.6601290322580645
***Average %*** | 75.57094028859015 | 57.275249592312136 | 65.15833278872127

--------------------------------------------------------------------------------
Experiment 2 - Relation Type Classifier

***Classifier: Relation Coarse Classifier***

5 Fold | Precision | Recall | F1
--- | ---: | ---: | ---:
Fold 0 | 0.7323529411764705 | 0.538961038961039 | 0.6209476309226932
Fold 1 | 0.7119741100323624 | 0.623229461756374 | 0.6646525679758309
Fold 2 | 0.6863354037267081 | 0.5249406175771971 | 0.594885598923284
Fold 3 | 0.7444794952681388 | 0.5959595959595959 | 0.6619915848527349
Fold 4 | 0.710691823899371 | 0.5580246913580247 | 0.6251728907330566
***Average %*** | 71.71667548206104 | 56.82230811224461 | 63.35300546815199

***Classifier: Relation Fine Classifier***

5 Fold | Precision | Recall | F1
--- | ---: | ---: | ---:
Fold 0 | 0.6064516129032258 | 0.4069264069264069 | 0.48704663212435234
Fold 1 | 0.5927272727272728 | 0.46175637393767704 | 0.519108280254777
Fold 2 | 0.5793103448275863 | 0.3990498812351544 | 0.47257383966244726
Fold 3 | 0.6236933797909407 | 0.45202020202020204 | 0.5241581259150806
Fold 4 | 0.5577557755775577 | 0.41728395061728396 | 0.4774011299435028
***Average %*** | 59.19876771653166 | 42.74073629473449 | 49.60576015800321

***Classifier: Relation Fine + Relation Hierarchy Constraint***

5 Fold | Precision | Recall | F1
--- | ---: | ---: | ---:
Fold 0 | 0.6097560975609756 | 0.4329004329004329 | 0.5063291139240506
Fold 1 | 0.5728813559322034 | 0.47875354107648727 | 0.5216049382716049
Fold 2 | 0.5493421052631579 | 0.39667458432304037 | 0.4606896551724138
Fold 3 | 0.618421052631579 | 0.47474747474747475 | 0.5371428571428571
Fold 4 | 0.5602605863192183 | 0.4246913580246914 | 0.48314606741573035
***Average %*** | 58.21322395414269 | 44.155347821442525 | 50.17825263853314

--------------------------------------------------------------------------------
Experiment 3 - Relation Type Classifier + Brown Cluster Features

***Classifier: Relation Coarse Classifier [With Brown Cluster Features]***

5 Fold | Precision | Recall | F1
--- | ---: | ---: | ---:
Fold 0 | 0.749271137026239 | 0.5562770562770563 | 0.6385093167701863
Fold 1 | 0.7251655629139073 | 0.6203966005665722 | 0.6687022900763359
Fold 2 | 0.7032258064516129 | 0.517814726840855 | 0.5964432284541724
Fold 3 | 0.755700325732899 | 0.5858585858585859 | 0.6600284495021337
Fold 4 | 0.7188498402555911 | 0.5555555555555556 | 0.6267409470752089
***Average %*** | 73.04425344760499 | 56.71805050197249 | 63.80848463756074

***Classifier: Relation Fine Classifier [With Brown Cluster Features]***

5 Fold | Precision | Recall | F1
--- | ---: | ---: | ---:
Fold 0 | 0.6428571428571429 | 0.42857142857142855 | 0.5142857142857143
Fold 1 | 0.6297709923664122 | 0.46742209631728043 | 0.5365853658536585
Fold 2 | 0.6035714285714285 | 0.4014251781472684 | 0.4821683309557775
Fold 3 | 0.6431095406360424 | 0.4595959595959596 | 0.5360824742268041
Fold 4 | 0.5993031358885017 | 0.4246913580246914 | 0.4971098265895954
***Average %*** | 62.37224480639056 | 43.63412041313257 | 51.324634238231

***Classifier: Relation Fine + Relation Hierarchy Constraint Classifier [With Brown Cluster Features]***

5 Fold | Precision | Recall | F1
--- | ---: | ---: | ---:
Fold 0 | 0.6349693251533742 | 0.44805194805194803 | 0.5253807106598984
Fold 1 | 0.5902777777777778 | 0.48158640226628896 | 0.5304212168486739
Fold 2 | 0.5771812080536913 | 0.4085510688836104 | 0.47844228094575797
Fold 3 | 0.6450511945392492 | 0.4772727272727273 | 0.5486211901306242
Fold 4 | 0.5813953488372093 | 0.43209876543209874 | 0.4957507082152975
***Average %*** | 60.577497087226035 | 44.95121823813348 | 51.57232213600504

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
