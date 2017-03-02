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
The test corpus consists of 2,012 sentences composed of $$ tokens totally.

### Evaluation: BIO Labeling

### Evaluation: Span Labeling

Note: While evaluation (testing), POS tags are provided by an implementation of [POSTagger in Saul](https://github.com/IllinoisCogComp/saul/blob/master/saul-examples/src/main/scala/edu/illinois/cs/cogcomp/saulexamples/nlp/POSTagger/README.md).

## Testing the Chunker interactively

For a quick demo of the Chunker Tagger, you can run the following command in the project's root folder.

```shell
sbt "project saulExamples" "runMain edu.illinois.cs.cogcomp.saulexamples.nlp.Chunker.ChunkerApp"
```


## Related

If you are looking for an implementation of the Chunker in Java, have a look at [this repository](https://github.com/IllinoisCogComp/illinois-cogcomp-nlp/blob/master/chunker/README.md).