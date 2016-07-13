/** This software is released under the University of Illinois/Research and Academic Use License. See
  * the LICENSE file in the root folder for details. Copyright (c) 2016
  *
  * Developed by: The Cognitive Computations Group, University of Illinois at Urbana-Champaign
  * http://cogcomp.cs.illinois.edu/
  */
package edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction;

import edu.illinois.cs.cogcomp.core.utilities.configuration.Configurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Property;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;

public class REConfigurator extends Configurator {

    public static final Property WORD2VEC_VECTORS_PATH = new Property("word2VecVectorsPath", "/shared/experiments/mangipu2/ace05_word2vec/vector.txt");

    public static final Property WORD2VEC_CLUSTERS_PATH = new Property("word2VecClustersPath", "/shared/experiments/mangipu2/ace05_word2vec/cluster.txt");

    /**
     * Get a ResourceManager object with the default key/value pairs for this configurator
     *
     * @return a non-null ResourceManager with appropriate values set.
     */
    @Override
    public ResourceManager getDefaultConfig() {
        Property[] defaultProperties = { WORD2VEC_VECTORS_PATH, WORD2VEC_CLUSTERS_PATH };
        return new ResourceManager(generateProperties(defaultProperties));
    }

    public static ResourceManager getResourceManager() {
        return new REConfigurator().getDefaultConfig();
    }
}
