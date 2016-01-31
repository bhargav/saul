package edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction.data.IOManager;

public class ResourceManager {
	
	public static String getProjectRoot() {
		String projectRoot = null;
		try {
			projectRoot = IOUtils.pwd();
		} catch (IOException e) {
			e.printStackTrace();
		}

		int len = projectRoot.length();
		if (projectRoot.substring(len - 4, len).equals("dist"))
			projectRoot += "/../";
		return projectRoot;
	}
	
	public static String getProjectResourceRoot() {
		return getProjectRoot()+"/resource/edu/illinois/cs/cogcomp/illinoisRE";
	}
	public static String getProjectResourceModelsPath() {
		return getProjectResourceRoot()+"/save_objs";
	}
	
	public static String getDataPath() {
		return getProjectRoot()+"/data/";
	}
	
	public static String getListsPath() {
		return getProjectRoot()+"/lists/";
	}
	
	public static String getJNWLFilePath() {
		return getProjectResourceRoot()+"/common/jwnl_properties.xml";
	}
	
	public static String getProjectResourcePrepPath() {
		return getProjectResourceRoot()+"/preposition";
	}
	
	public static String getIlpDir() {
		return getProjectRoot()+"/ilp";
	}
	
	public static String getCuratorHost() {
		String host = null;
		InputStream is = null;
		BufferedReader br = null;
		String line;
		
		is = ClassLoader.getSystemResourceAsStream("illinoisRE.properties");
		if(is!=null) { br = new BufferedReader(new InputStreamReader(is)); }			// get as resource
		else { br = IOManager.openReader(getProjectRoot()+"/illinoisRE.properties"); }	// get as relative file path
		
		try {
			while((line = br.readLine()) != null) {
				String[] tokens = line.split(" ");
				if(tokens[0].compareTo("curator-host")==0) {
					host = tokens[1];
					break;
				}
			}
			br.close();
		} catch(Exception e) {
			System.out.println(e);
		}
		
		System.out.println("return host as "+host);
		return host;
	}
	
	public static int getCuratorPort() {
		int port = -1;
		InputStream is = null;
		BufferedReader br = null;
		String line;
		
		is = ClassLoader.getSystemResourceAsStream("illinoisRE.properties");
		if(is!=null) { br = new BufferedReader(new InputStreamReader(is)); }			// get as resource
		else { br = IOManager.openReader(getProjectRoot()+"/illinoisRE.properties"); }	// get as relative file path
		
		try {
			while((line = br.readLine()) != null) {
				String[] tokens = line.split(" ");
				if(tokens[0].compareTo("curator-port")==0) {
					port = new Integer(tokens[1]).intValue();
					break;
				}
			}
			br.close();
		} catch(Exception e) {
			System.out.println(e);
		}
		
		System.out.println("return port as "+port);
		return port;
	}
	
	public static boolean getCuratorForceUpdate() {
		boolean forceUpdate = false;
		InputStream is = null;
		BufferedReader br = null;
		String line;
		
		is = ClassLoader.getSystemResourceAsStream("illinoisRE.properties");
		if(is!=null) { br = new BufferedReader(new InputStreamReader(is)); }			// get as resource
		else { br = IOManager.openReader(getProjectRoot()+"/illinoisRE.properties"); }	// get as relative file path
		
		try {
			while((line = br.readLine()) != null) {
				String[] tokens = line.split(" ");
				if(tokens[0].compareTo("curator-force-update")==0) {
					if(tokens[1].compareTo("true")==0) {
						forceUpdate = true;
					}
					else if(tokens[1].compareTo("false")==0) {
						forceUpdate = false;
					}
					break;
				}
			}
			br.close();
		} catch(Exception e) {
			System.out.println(e);
		}
		
		System.out.println("return curator-force-update as "+forceUpdate);
		return forceUpdate;
	}
	
	/*
	public static String getLabelOntology() {
		String labelOntology = null;
		InputStream is = null;
		BufferedReader br = null;
		String line;
		
		is = ClassLoader.getSystemResourceAsStream("illinoisRE.properties");
		if(is!=null) { br = new BufferedReader(new InputStreamReader(is)); }			// get as resource
		else { br = IOManager.openReader(getProjectRoot()+"illinoisRE.properties"); }	// get as relative file path
		
		try {
			while((line = br.readLine()) != null) {
				String[] tokens = line.split(" ");
				if(tokens[0].compareTo("label-ontology")==0) {
					labelOntology = tokens[1];
					break;
				}
			}
			br.close();
		} catch(Exception e) {
			System.out.println(e);
		}
		
		//System.out.println("return remapToIc as "+remapToIc);
		return labelOntology;
	}
	*/
	
	/*
	public static String getBrownClusterResource() {
		URL url = ResourceManager.class.getResource("/edu/illinois/cs/cogcomp/illinoisRE/save_objs/brown_clusters");
		String fullpath = url.toString();
		return fullpath.substring(fullpath.indexOf("!")+2);
		//return url;
	}
	*/
	
	public static URL getMentionModelResource() {
		URL url = ResourceManager.class.getResource("/edu/illinois/cs/cogcomp/illinoisRE/mention/mention.model");
		return url;
	}
	
	public static URL getMentionLexResource() {
		URL url = ResourceManager.class.getResource("/edu/illinois/cs/cogcomp/illinoisRE/mention/mention.lex");
		return url;
	}
	
	public static URL getREModelResource() {
		URL url = ResourceManager.class.getResource("/edu/illinois/cs/cogcomp/illinoisRE/relation/re.model");
		return url;
	}
	
	public static URL getRELexResource() {
		URL url = ResourceManager.class.getResource("/edu/illinois/cs/cogcomp/illinoisRE/relation/re.lex");
		return url;
	}
	
	
	// fine generic
	public static URL getREFineGenericModelResource() {
		URL url = ResourceManager.class.getResource("/edu/illinois/cs/cogcomp/illinoisRE/relation/re.fine.generic.model");
		return url;
	}
	public static URL getREFineGenericLexResource() {
		URL url = ResourceManager.class.getResource("/edu/illinois/cs/cogcomp/illinoisRE/relation/re.fine.generic.lex");
		return url;
	}
	// fine premod
	public static URL getREFinePremodModelResource() {
		URL url = ResourceManager.class.getResource("/edu/illinois/cs/cogcomp/illinoisRE/relation/re.fine.premod.model");
		return url;
	}
	public static URL getREFinePremodLexResource() {
		URL url = ResourceManager.class.getResource("/edu/illinois/cs/cogcomp/illinoisRE/relation/re.fine.premod.lex");
		return url;
	}
	// fine poss
	public static URL getREFinePossModelResource() {
		URL url = ResourceManager.class.getResource("/edu/illinois/cs/cogcomp/illinoisRE/relation/re.fine.poss.model");
		return url;
	}
	public static URL getREFinePossLexResource() {
		URL url = ResourceManager.class.getResource("/edu/illinois/cs/cogcomp/illinoisRE/relation/re.fine.poss.lex");
		return url;
	}
	// fine prep
	public static URL getREFinePrepModelResource() {
		URL url = ResourceManager.class.getResource("/edu/illinois/cs/cogcomp/illinoisRE/relation/re.fine.prep.model");
		return url;
	}
	public static URL getREFinePrepLexResource() {
		URL url = ResourceManager.class.getResource("/edu/illinois/cs/cogcomp/illinoisRE/relation/re.fine.prep.lex");
		return url;
	}
	// fine formula
	public static URL getREFineFormulaModelResource() {
		URL url = ResourceManager.class.getResource("/edu/illinois/cs/cogcomp/illinoisRE/relation/re.fine.formula.model");
		return url;
	}
	public static URL getREFineFormulaLexResource() {
		URL url = ResourceManager.class.getResource("/edu/illinois/cs/cogcomp/illinoisRE/relation/re.fine.formula.lex");
		return url;
	}
	
	// binary generic
	public static URL getREBinaryGenericModelResource() {
		URL url = ResourceManager.class.getResource("/edu/illinois/cs/cogcomp/illinoisRE/relation/re.binary.generic.model");
		return url;
	}
	public static URL getREBinaryGenericLexResource() {
		URL url = ResourceManager.class.getResource("/edu/illinois/cs/cogcomp/illinoisRE/relation/re.binary.generic.lex");
		return url;
	}
	// binary premod
	public static URL getREBinaryPremodModelResource() {
		URL url = ResourceManager.class.getResource("/edu/illinois/cs/cogcomp/illinoisRE/relation/re.binary.premod.model");
		return url;
	}
	public static URL getREBinaryPremodLexResource() {
		URL url = ResourceManager.class.getResource("/edu/illinois/cs/cogcomp/illinoisRE/relation/re.binary.premod.lex");
		return url;
	}
	// binary poss
	public static URL getREBinaryPossModelResource() {
		URL url = ResourceManager.class.getResource("/edu/illinois/cs/cogcomp/illinoisRE/relation/re.binary.poss.model");
		return url;
	}
	public static URL getREBinaryPossLexResource() {
		URL url = ResourceManager.class.getResource("/edu/illinois/cs/cogcomp/illinoisRE/relation/re.binary.poss.lex");
		return url;
	}
	// binary prep
	public static URL getREBinaryPrepModelResource() {
		URL url = ResourceManager.class.getResource("/edu/illinois/cs/cogcomp/illinoisRE/relation/re.binary.prep.model");
		return url;
	}
	public static URL getREBinaryPrepLexResource() {
		URL url = ResourceManager.class.getResource("/edu/illinois/cs/cogcomp/illinoisRE/relation/re.binary.prep.lex");
		return url;
	}
	// binary formula
	public static URL getREBinaryFormulaModelResource() {
		URL url = ResourceManager.class.getResource("/edu/illinois/cs/cogcomp/illinoisRE/relation/re.binary.formula.model");
		return url;
	}
	public static URL getREBinaryFormulaLexResource() {
		URL url = ResourceManager.class.getResource("/edu/illinois/cs/cogcomp/illinoisRE/relation/re.binary.formula.lex");
		return url;
	}
	
	// coarseUn generic
	public static URL getRECoarseUnGenericModelResource() {
		URL url = ResourceManager.class.getResource("/edu/illinois/cs/cogcomp/illinoisRE/relation/re.coarseUn.generic.model");
		return url;
	}
	public static URL getRECoarseUnGenericLexResource() {
		URL url = ResourceManager.class.getResource("/edu/illinois/cs/cogcomp/illinoisRE/relation/re.coarseUn.generic.lex");
		return url;
	}
	// coarseUn premod
	public static URL getRECoarseUnPremodModelResource() {
		URL url = ResourceManager.class.getResource("/edu/illinois/cs/cogcomp/illinoisRE/relation/re.coarseUn.premod.model");
		return url;
	}
	public static URL getRECoarseUnPremodLexResource() {
		URL url = ResourceManager.class.getResource("/edu/illinois/cs/cogcomp/illinoisRE/relation/re.coarseUn.premod.lex");
		return url;
	}
	// coarseUn poss
	public static URL getRECoarseUnPossModelResource() {
		URL url = ResourceManager.class.getResource("/edu/illinois/cs/cogcomp/illinoisRE/relation/re.coarseUn.poss.model");
		return url;
	}
	public static URL getRECoarseUnPossLexResource() {
		URL url = ResourceManager.class.getResource("/edu/illinois/cs/cogcomp/illinoisRE/relation/re.coarseUn.poss.lex");
		return url;
	}
	// coarseUn prep
	public static URL getRECoarseUnPrepModelResource() {
		URL url = ResourceManager.class.getResource("/edu/illinois/cs/cogcomp/illinoisRE/relation/re.coarseUn.prep.model");
		return url;
	}
	public static URL getRECoarseUnPrepLexResource() {
		URL url = ResourceManager.class.getResource("/edu/illinois/cs/cogcomp/illinoisRE/relation/re.coarseUn.prep.lex");
		return url;
	}
	// coarseUn formula
	public static URL getRECoarseUnFormulaModelResource() {
		URL url = ResourceManager.class.getResource("/edu/illinois/cs/cogcomp/illinoisRE/relation/re.coarseUn.formula.model");
		return url;
	}
	public static URL getRECoarseUnFormulaLexResource() {
		URL url = ResourceManager.class.getResource("/edu/illinois/cs/cogcomp/illinoisRE/relation/re.coarseUn.formula.lex");
		return url;
	}
	
	
	// ========== Prepositions =============
	public static URL getPrepModelResource(String prep) {
		URL url = ResourceManager.class.getResource("/edu/illinois/cs/cogcomp/illinoisRE/preposition/"+prep+".model");
		return url;
	}
	
	public static URL getPrepLexResource(String prep) {
		URL url = ResourceManager.class.getResource("/edu/illinois/cs/cogcomp/illinoisRE/preposition/"+prep+".lex");
		return url;
	}
	
}