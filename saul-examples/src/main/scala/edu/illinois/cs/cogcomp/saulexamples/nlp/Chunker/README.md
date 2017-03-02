# Chunker (Shallow Parser)

Chunking (Shallow Parsing) is the identification of constituents (noun groups, verbs, verb groups etc.) in a sentence. 
The system implemented here is based of the following paper: 

```
@inproceedings{PunyakanokRo01,
    author = {V. Punyakanok and D. Roth},
    title = {The Use of Classifiers in Sequential Inference},
    booktitle = {NIPS},
    pages = {995--1001},
    year = {2001},
    publisher = {MIT Press},
    acceptance = {25/514 (4.8\%) Oral Presentations; 152/514 (29%) overall},
    url = " http://cogcomp.cs.illinois.edu/papers/nips01.pdf",
    funding = {NSF98 CAREER},
    projects = {LnI,SI,IE,NE,NLP,CCM},
    comment = {Structured, sequential output; Sequence Prediction: HMM with classifiers, Conditional Models, Constraint Satisfaction},
}
```

## Performance


The data for the experiments was extracted from the dataset for the [CONLL 2000 Chunking Shared Task](http://www.cnts.ua.ac.be/conll2000/chunking/).
The training corpus consists of 8,936 sentences composed of 210,996 tokens totally. 
The test corpus consists of 2,012 sentences composed of 47,372 tokens totally.

### Evaluation: BIO Labeling

| Label     |  Precision | Recall |  F1    | LCount | PCount |
|-----------|-----------:|-------:|-------:|-------:|-------:|
| B-ADJP    |  80.323    | 68.192 | 73.762 |   437  |  371   |
| B-ADVP    |  82.275    | 79.330 | 80.776 |   866  |  835   |
| B-CONJP   |  40.000    | 66.667 | 50.000 |     9  |   15   |
| B-INTJ    | 100.000    | 50.000 | 66.667 |     2  |    1   |
| B-LST     |   0.000    | 0.000  | 0.000  |    5   |   3    |
| B-NP      |  95.718    | 96.412 | 96.064 | 12404  | 12494  |
| B-PP      |  96.456    | 97.359 | 96.905 |  4808  | 4853   |
| B-PRT     |  79.048    | 78.302 | 78.673 |   106  | 105    |
| B-SBAR    |  87.674    | 82.430 | 84.971 |   535  |  503   |
| B-UCP     |   0.000    | 0.000  | 0.000  |    0   |  52    |
| B-VP      |  94.581    | 95.292 | 94.935 |  4652  | 4687   |
| I-ADJP    |  77.982    | 50.898 | 61.594 |   167  |  109   |
| I-ADVP    |  60.274    | 49.438 | 54.321 |    89  |   73   |
| I-CONJP   |  55.556    | 76.923 | 64.516 |    13  |   18   |
| I-LST     |   0.000    | 0.000  | 0.000  |    2   |   0    |
| I-NP      |  96.251    | 95.795 | 96.023 | 14365  | 14297  |
| I-PP      |  86.111    | 64.583 | 73.810 |    48  |   36   |
| I-PRT     |   0.000    | 0.000  | 0.000  |    0   |   1    |
| I-SBAR    |  10.526    | 50.000 | 17.391 |     4  |   19   |
| I-UCP     |   0.000    | 0.000  | 0.000  |    0   |   9    |
| I-VP      |  94.935    | 93.712 | 94.319 |  2640  | 2606   |
| O         |  95.172    |96.174  | 95.670 |  6169  | 6234   |
| **Accuracy**  |  **94.945**     |  **-**     | **-**      | **-**      | **47321**  |

### Evaluation: Span Labeling

| Label | Total Gold | Total Predicted | Correct Prediction | Precision | Recall | F1 |
| ----- | ---:| ---:| ---:| ---:| ---:| ---:|
| ADJP | 438 | 515 | 296 | 57.48 | 67.58 | 62.12 |
| ADVP | 866 | 1032 | 670 | 64.92 | 77.37 | 70.6 |
| CONJP | 9 | 19 | 6 | 31.58 | 66.67 | 42.86 |
| INTJ | 2 | 2 | 1 | 50 | 50 | 50 |
| LST | 5 | 7 | 0 | 0 | 0 | 0 |
| NP | 12422 | 13376 | 11574 | 86.53 | 93.17 | 89.73 |
| PP | 4811 | 4994 | 4684 | 93.79 | 97.36 | 95.54 |
| PRT | 106 | 138 | 86 | 62.32 | 81.13 | 70.49 |
| SBAR | 535 | 603 | 444 | 73.63 | 82.99 | 78.03 |
| UCP | 0 | 63 | 0 | 0 | 0 | 0 |
| VP | 4658 | 5014 | 4335 | 86.46 | 93.07 | 89.64 |
| **All** | **23852** | **25763** | **22096** | **85.77** | **92.64** | **89.07** |

Note: While evaluation (testing), POS tags are provided by an implementation of [POSTagger in Saul](https://github.com/IllinoisCogComp/saul/blob/master/saul-examples/src/main/scala/edu/illinois/cs/cogcomp/saulexamples/nlp/POSTagger/README.md).

## Testing the Chunker interactively

For a quick demo of the Chunker Tagger, you can run the following command in the project's root folder.

```shell
sbt "project saulExamples" "runMain edu.illinois.cs.cogcomp.saulexamples.nlp.Chunker.ChunkerApp"
```


## Related

If you are looking for an implementation of the Chunker in Java, have a look at [this repository](https://github.com/IllinoisCogComp/illinois-cogcomp-nlp/blob/master/chunker/README.md).