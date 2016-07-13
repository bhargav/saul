/** This software is released under the University of Illinois/Research and Academic Use License. See
  * the LICENSE file in the root folder for details. Copyright (c) 2016
  *
  * Developed by: The Cognitive Computations Group, University of Illinois at Urbana-Champaign
  * http://cogcomp.cs.illinois.edu/
  */
package edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction;

import edu.illinois.cs.cogcomp.core.io.LineIO;

import java.io.*;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Word2VecClusterManager {
    private String UNK = "UNK";
    private Map<String, double[]> map = new HashMap<>();
    private int vector_size;

    private static Logger logger = LoggerFactory.getLogger(Word2VecClusterManager.class);

    public Word2VecClusterManager(String clusterFilePath) {
        List<String> lines = new ArrayList<>();

        try {
            lines.addAll(LineIO.read(clusterFilePath));
        } catch (FileNotFoundException ex) {
            logger.error("Word2Vec Cluster path not found at " + clusterFilePath, ex);
        }

        boolean firstLine = true;
        this.vector_size = 0;

        for (String s : lines) {
            if (firstLine) {
                firstLine = false;
                String[] list = s.split(" ");
                vector_size = Integer.parseInt(list[1]);
                System.out.println("vector_size:" + vector_size);
                continue;
            }

            String[] list = s.split(" ");
            String key = list[0];

            double[] vector = new double[vector_size];
            for (int i = 0; i < vector_size; i++) {
                vector[i] = Double.parseDouble(list[i + 1]);
            }
            map.put(key, vector);
        }

        logger.info("Word2Vec Manager Loaded");
    }

    public double[] getWordVector(String key) {
        if (key == null) {
            double[] temp = new double[this.vector_size];
            for (int i = 0; i < this.vector_size; i++)
                temp[i] = -1;
            return temp;
        }
        if (key.equals("'s")) {
            key = "has";
        }
        if ((key.endsWith(".") || key.endsWith(",")) && key.length() > 1 && !key.equals("...")) {
            String new_key = key.substring(0, key.length() - 2);
            if (map.containsKey(new_key))
                return map.get(new_key);
        }

        if (map.containsKey(key)) {
            return map.get(key);
        }
        return new double[this.vector_size];
    }

    public int getSize() {
        return this.vector_size;
    }

    public static void main(String[] argv) {
        String path = argv.length > 0 ? argv[0] : "";
        Word2VecClusterManager manager = new Word2VecClusterManager(path);
        logger.info(Arrays.toString(manager.getWordVector("this")));
    }
}
