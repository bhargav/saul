package edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction.data;

import edu.illinois.cs.cogcomp.core.io.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

public final class ListManager {
	Set<String> collectiveNouns;
	
	Set<String> person;
	Set<String> personTitle;
	Set<String> personName;
	Set<String> personPronoun;
	Set<String> personDBpedia;
	
	Set<String> gpe;
	Set<String> gpeCommonNouns;
	Set<String> gpeCountryStateCounty;
	Set<String> gpeCity;
	Set<String> gpeCountry;
	Set<String> gpeState;
	Set<String> gpeCounty;
	Set<String> gpeMajorArea;
	
	Set<String> ethnicGroup;
	Set<String> nationality;
	
	Set<String> orgGovtMultiWords;
	Set<String> orgGovtAbbrev;
	Set<String> orgGovtSingleWord;
	Set<String> orgCommercial;
	Set<String> orgGeneric;
	Set<String> orgPolitical;
	Set<String> orgTerrorist;
	
	Set<String> facBarrier;
	Set<String> facBuilding;
	Set<String> facConduit;
	Set<String> facPath;
	Set<String> facPlant;
	Set<String> facBuildingSubArea;
	Set<String> facGeneric;
	
	Set<String> wea;
	
	Set<String> veh;
	
	Map<String, Set<String>> wikiTitleAttributes;
	
	Set<String> gpeCorefNouns;
	Set<String> wordnetNounCollocations;
	Set<String> personAndJobTitles;
	Set<String> personAndJobTitlesCollocations;	// at least two words or more
	Set<String> counties;
	Set<String> states;
	Set<String> nationalities;
	Set<String> wikiTitlesHashLen2;
	Set<String> wikiTitlesHashLen3;
	String wikiTitlesString;
	
	public ListManager() {
//		readCommonNouns(ResourceManager.getListsPath()+"collectiveNouns.list");
//		readGPECorefNouns(ResourceManager.getListsPath()+"gpe_coref.arg1.list");
//		readWordNetNounCollocations("/home/roth/chanys/data/lists/wordnet/wn3.0_nounCollocations");
		//readTitleCollocations("/home/roth/chanys/data/lists/jobs/AltafNg_COLING10_modifiedByCYS/titles");
//		readPersonAndJobTitles("/home/roth/chanys/data/lists/jobs/titles");
//		readCounties("/home/roth/chanys/data/lists/places/US_counties");
//		readStates("/home/roth/chanys/data/lists/places/US_states");
//		readNationalities("/home/roth/chanys/data/lists/Lev/known_nationalities.lst");
		//readWikiTitlesToString("/home/roth/chanys/data/lists/JeffWikiData/enwiki-20100312-title");
		//readWikiTitlesToHash("/home/roth/chanys/data/lists/JeffWikiData/enwiki-20100312-title");
		
		//WordNetHelper.wordNetPropertiesFile = ResourceManager.getJNWLFilePath();
		readCollectiveNouns();
		readPerson();
		
		readGPE();
		readEthnicAndNationality();
		
		readOrg();
		
		readFac();
		
		readWea();
		
		readVeh();
		
		//wikiTitleAttributes = new HashMap<String, Set<String>>();
		//readWikiTitleAttributeResource("edu/illinois/cs/cogcomp/illinoisRE/lists/TitleCategoriesSimplifiedWeighted.txt");
	}

    private static InputStream getResourceAsStream(String resourceFileName) {
        InputStream is = null;
        try {
            List<URL> resources = IOUtils.lsResources(ClassLoader.getSystemClassLoader().getClass(), resourceFileName);
            assert resources.size() == 1;

            is = resources.get(0).openStream();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return is;
    }
	
	private void readCollectiveNouns() {
		BufferedReader br = null;
		InputStream is = getResourceAsStream("edu/illinois/cs/cogcomp/illinoisRE/lists/collectiveNouns");
		if(is!=null)
			br = new BufferedReader(new InputStreamReader(is));
		else {
			System.out.println("Cannot get collectiveNouns as a resource!");
			return;
		}

		collectiveNouns = new HashSet<String>();
		String line;

		try {
			while((line = br.readLine()) != null) {
				collectiveNouns.add(line.toLowerCase());
			}
			br.close();
		} catch(Exception e) {
			System.out.println(e);
		}
	}
	public boolean isCollectiveNoun(String n) {
		if(collectiveNouns.contains(n.toLowerCase()))
			return true;
		else
			return false;
	}

	public void readPerson() {
		person = new HashSet<String>();
		personTitle = new HashSet<String>();
		personName = new HashSet<String>();
		personPronoun = new HashSet<String>();
		personDBpedia = new HashSet<String>();

		readWnIndividual();		// recall=1511/12302=0.12282555682002927 precision=1511/2018=0.7487611496531219 f1=0.21103351955307265
		readTitles();				// recall=2093/12302=0.17013493740855146 precision=2093/2196=0.9530965391621129 f1=0.288729479928266
		readLevJobsTitles();		// recall=1799/12302=0.14623638432775157 precision=1799/1890=0.9518518518518518 f1=0.2535231116121759
		readJobTitles();

//		readPersonNames();		// recall=3956/12302=0.32157372784913024 precision=3956/5737=0.6895590029632211 f1=0.43860524419313707
//		readLevKnownName();		// recall=1936/12302=0.15737278491302228 precision=1936/2520=0.7682539682539683 f1=0.26123330184860344
		//readLevKnownNamesBig();	// recall=641/12302=0.05210534872378475 precision=641/1199=0.5346121768140116 f1=0.09495592919043035
		//readLevKnownNames();		// recall=2152/12302=0.17493090554381402 precision=2152/3172=0.6784363177805801 f1=0.27814398345611996	// LevKnownNames + LevKnownNamesBig
		readLevWikiPeople();		// recall=957/12302=0.07779222890586897 precision=957/1088=0.8795955882352942 f1=0.14294249439880508

		readDBpediaPersonHyposFull();	// recall=2004/12302=0.16290034140790116 precision=2004/2636=0.7602427921092565 f1=0.26830901057705187
		//readDBpediaPersonHypos();	// recall=608/12302=0.049422858071858235 precision=608/683=0.890190336749634 f1=0.09364651520985753	// subset of readDBpediaPersonHyposFull

		readPronoun();			// recall=3706/12302=0.301251828970899 precision=3706/4215=0.8792408066429419 f1=0.44874977296119145

		readPersonMisc();

		// (readTitles + readLevJobsTitles + readLevWikiPeople + readPronoun) + readLevKnownName + readDBpediaPersonHyposFull:
		// 		recall=8630/12302=0.7015119492765404 precision=8630/10528=0.8197188449848024 f1=0.7560227770477441
		// + readWnIndividual: recall=8652/12302=0.7033002763778248 precision=8652/10620=0.8146892655367232 f1=0.7549079486955763
		// + readPersonNames: recall=9289/12302=0.7550804747195577 precision=9289/12502=0.7430011198208286 f1=0.7489920980487017
		// + readLevKnownNamesBig: recall=8749/12302=0.7111851731425785 precision=8749/11064=0.790762834417932 f1=0.7488658734913977
		// + readLevKnownNames: recall=8749/12302=0.7111851731425785 precision=8749/11064=0.790762834417932 f1=0.7488658734913977
		// + readDBpediaPersonHypos: recall=8630/12302=0.7015119492765404 precision=8630/10528=0.8197188449848024 f1=0.7560227770477441
	}
	public boolean isPerson(String n) {
		String target = n.toLowerCase();
		if(person.contains(target))
			return true;
		else {
			/*
			if(target.contains("_")) {		// a collocation
				for(Iterator<String> it=person.iterator(); it.hasNext();) {
					String v = it.next();
					//if(v.startsWith(target) || v.endsWith(target)) {
					if(v.endsWith(target)) {
						return true;
					}
				}
			}
			*/
			return false;
		}
	}
	public boolean isPersonTitle(String n) {
		String target = n.toLowerCase();
		if(personTitle.contains(target))
			return true;
		else
			return false;
	}
	public boolean isPersonName(String n) {
		String target = n.toLowerCase();
		if(personName.contains(target))
			return true;
		else
			return false;
	}
	public boolean isPersonPronoun(String n) {
		String target = n.toLowerCase();
		if(personPronoun.contains(target))
			return true;
		else
			return false;
	}
	public boolean isPersonDBpedia(String n) {
		String target = n.toLowerCase();
		if(personDBpedia.contains(target))
			return true;
		else
			return false;
	}
	private void readPersonResource(String path) {
		BufferedReader br = null;
		InputStream is = getResourceAsStream(path);
		if(is!=null)
			br = new BufferedReader(new InputStreamReader(is));
		else {
			System.out.println("Cannot get " + path + " as a resource!");
			return;
		}

		String line;
		try {
			while((line = br.readLine()) != null) {
				line = line.trim();
				line = line.replaceAll(" ", "_");
				person.add(line.toLowerCase());
			}
			br.close();
		} catch(Exception e) {
			System.out.println(e);
		}
	}
	private void readPersonTitleResource(String path) {
		BufferedReader br = null;
		InputStream is = getResourceAsStream(path);
		if(is!=null)
			br = new BufferedReader(new InputStreamReader(is));
		else {
			System.out.println("Cannot get " + path + " as a resource!");
			return;
		}

		String line;
		try {
			while((line = br.readLine()) != null) {
				line = line.trim();
				line = line.replaceAll(" ", "_");
				personTitle.add(line.toLowerCase());
			}
			br.close();
		} catch(Exception e) {
			System.out.println(e);
		}
	}
	private void readPersonNameResource(String path) {
		BufferedReader br = null;
		InputStream is = getResourceAsStream(path);
		if(is!=null)
			br = new BufferedReader(new InputStreamReader(is));
		else {
			System.out.println("Cannot get " + path + " as a resource!");
			return;
		}

		String line;
		try {
			while((line = br.readLine()) != null) {
				line = line.trim();
				line = line.replaceAll(" ", "_");
				personName.add(line.toLowerCase());
			}
			br.close();
		} catch(Exception e) {
			System.out.println(e);
		}
	}
	private void readPersonPronounResource(String path) {
		BufferedReader br = null;
		InputStream is = getResourceAsStream(path);
		if(is!=null)
			br = new BufferedReader(new InputStreamReader(is));
		else {
			System.out.println("Cannot get " + path + " as a resource!");
			return;
		}

		String line;
		try {
			while((line = br.readLine()) != null) {
				line = line.trim();
				line = line.replaceAll(" ", "_");
				personPronoun.add(line.toLowerCase());
			}
			br.close();
		} catch(Exception e) {
			System.out.println(e);
		}
	}
	private void readPersonDBpediaResource(String path) {
		BufferedReader br = null;
		InputStream is = getResourceAsStream(path);
		if(is!=null)
			br = new BufferedReader(new InputStreamReader(is));
		else {
			System.out.println("Cannot get " + path + " as a resource!");
			return;
		}

		String line;
		try {
			while((line = br.readLine()) != null) {
				line = line.trim();
				line = line.replaceAll(" ", "_");
				personDBpedia.add(line.toLowerCase());
			}
			br.close();
		} catch(Exception e) {
			System.out.println(e);
		}
	}
	private void readWnIndividual() {
		readPersonResource("edu/illinois/cs/cogcomp/illinoisRE/lists/person.wn.hypo");
	}
	private void readTitles() {
		readPersonResource("edu/illinois/cs/cogcomp/illinoisRE/lists/titles");
	}
	private void readJobTitles() {
		readPersonResource("edu/illinois/cs/cogcomp/illinoisRE/lists/job_titles");
	}
	private void readPersonNames() {
		readPersonResource("edu/illinois/cs/cogcomp/illinoisRE/lists/personNames");
	}
	private void readLevJobsTitles() {
		readPersonResource("edu/illinois/cs/cogcomp/illinoisRE/lists/Lev.known_job+title");
	}
	private void readLevKnownName() {
		readPersonResource("edu/illinois/cs/cogcomp/illinoisRE/lists/Lev.known_name");
	}
	private void readLevKnownNamesBig() {
		readPersonResource("edu/illinois/cs/cogcomp/illinoisRE/lists/Lev.known_names.big");
	}
	private void readLevKnownNames() {
		readPersonResource("edu/illinois/cs/cogcomp/illinoisRE/lists/Lev.known_names");
	}
	private void readLevWikiPeople() {
		readPersonResource("edu/illinois/cs/cogcomp/illinoisRE/lists/Lev.WikiPeople.modified");
	}
	private void readDBpediaPersonHyposFull() {
		readPersonResource("edu/illinois/cs/cogcomp/illinoisRE/lists/dbpedia.person.hypos.full");
	}
	private void readDBpediaPersonHypos() {
		readPersonResource("edu/illinois/cs/cogcomp/illinoisRE/lists/dbpedia.person.hypos");
	}
	private void readPronoun() {
		readPersonResource("edu/illinois/cs/cogcomp/illinoisRE/lists/pronouns");
	}
	private void readPersonMisc() {
		readPersonResource("edu/illinois/cs/cogcomp/illinoisRE/lists/person.misc");
	}


	public void readGPE() {
		gpe = new HashSet<String>();
		gpeCommonNouns = new HashSet<String>();
		gpeCountryStateCounty = new HashSet<String>();
		gpeCity = new HashSet<String>();
		gpeCountry = new HashSet<String>();
		gpeState = new HashSet<String>();
		gpeCounty = new HashSet<String>();
		gpeMajorArea = new HashSet<String>();

		readGPECommonNouns();
		readGPECountryStateCounty();
		readGPECity();
		readGPECountry();
		readGPEState();
		readGPECounty();
		//readGPEResource("edu/illinois/cs/cogcomp/illinoisRE/lists/gpe.commonNouns");
		//readGPEResource("edu/illinois/cs/cogcomp/illinoisRE/lists/gpe.dbpedia.populatedPlace");
		//readGPEResource("edu/illinois/cs/cogcomp/illinoisRE/lists/gpe.countryStateCounty");
		readGPEMajorArea();
	}

	public boolean isGPE(String n) {
		if(gpe.contains(n.toLowerCase()))
			return true;
		else
			return false;
	}
	public boolean isGPECommonNoun(String n) {
		if(gpeCommonNouns.contains(n.toLowerCase()))
			return true;
		else
			return false;
	}
	public boolean isGPEMajorArea(String n) {
		if(gpeMajorArea.contains(n.toLowerCase()))
			return true;
		else
			return false;
	}
	public boolean isGPECountryStateCounty(String n) {
		String s = null;
		if(n.contains(" "))
			s = n.toLowerCase();
		else
			s = n;
		if(gpeCountryStateCounty.contains(s))
			return true;
		else
			return false;
	}
	public boolean isGPECity(String n) {
		if(gpeCity.contains(n.toLowerCase()))
			return true;
		else
			return false;
	}
	public boolean isGPECountry(String n) {
		if(gpeCountry.contains(n))
			return true;
		else
			return false;
	}
	public boolean isGPEState(String n) {
		if(gpeState.contains(n.toLowerCase()))
			return true;
		else
			return false;
	}
	public boolean isGPECounty(String n) {
		if(gpeCounty.contains(n.toLowerCase()))
			return true;
		else
			return false;
	}
	private void readGPECommonNouns() {
		readGPECommonNounsResource("edu/illinois/cs/cogcomp/illinoisRE/lists/gpe.commonNouns");
		readGPECommonNounsResource("edu/illinois/cs/cogcomp/illinoisRE/lists/gpe.dbpedia.populatedPlace");
	}
	private void readGPECountryStateCounty() {
		readGPECountryStateCountyResource("edu/illinois/cs/cogcomp/illinoisRE/lists/gpe.countryStateCounty");
	}
	private void readGPECity() {
		readGPECityResource("edu/illinois/cs/cogcomp/illinoisRE/lists/gpe.cities");
		readGPECityResource("edu/illinois/cs/cogcomp/illinoisRE/lists/US.city");
		readGPECityResource("edu/illinois/cs/cogcomp/illinoisRE/lists/europe.city");
		readGPECityResource("edu/illinois/cs/cogcomp/illinoisRE/lists/canada.city");
		readGPECityResource("edu/illinois/cs/cogcomp/illinoisRE/lists/japan.city");
		readGPECityResource("edu/illinois/cs/cogcomp/illinoisRE/lists/india.city");
		readGPECityResource("edu/illinois/cs/cogcomp/illinoisRE/lists/worldLargest.city");
	}
	private void readGPECountry() {
		readGPECountryResource("edu/illinois/cs/cogcomp/illinoisRE/lists/gpe.country");
	}
	private void readGPEState() {
		readGPEStateResource("edu/illinois/cs/cogcomp/illinoisRE/lists/gpe.state");
	}
	private void readGPECounty() {
		readGPECountyResource("edu/illinois/cs/cogcomp/illinoisRE/lists/US_counties.top100");
	}
	private void readGPEMajorArea() {
		readGPEMajorAreaResource("edu/illinois/cs/cogcomp/illinoisRE/lists/gpe.major_areas");
	}


	private void readGPEResource(String path) {
		BufferedReader br = null;
		InputStream is = getResourceAsStream(path);
		if(is!=null)
			br = new BufferedReader(new InputStreamReader(is));
		else {
			System.out.println("Cannot get " + path + " as a resource!");
			return;
		}

		String line;
		try {
			while((line = br.readLine()) != null) {
				line = line.trim();
				line = line.replaceAll(" ", "_");
				gpe.add(line.toLowerCase());
			}
			br.close();
		} catch(Exception e) {
			System.out.println(e);
		}
	}
	private void readGPECommonNounsResource(String path) {
		BufferedReader br = null;
		InputStream is = getResourceAsStream(path);
		if(is!=null)
			br = new BufferedReader(new InputStreamReader(is));
		else {
			System.out.println("Cannot get " + path + " as a resource!");
			return;
		}

		String line;
		try {
			while((line = br.readLine()) != null) {
				line = line.trim();
				line = line.replaceAll(" ", "_");
				gpeCommonNouns.add(line.toLowerCase());
			}
			br.close();
		} catch(Exception e) {
			System.out.println(e);
		}
	}
	private void readGPEMajorAreaResource(String path) {
		BufferedReader br = null;
		InputStream is = getResourceAsStream(path);
		if(is!=null)
			br = new BufferedReader(new InputStreamReader(is));
		else {
			System.out.println("Cannot get " + path + " as a resource!");
			return;
		}

		String line;
		try {
			while((line = br.readLine()) != null) {
				line = line.trim();
				line = line.replaceAll(" ", "_");
				gpeMajorArea.add(line.toLowerCase());
			}
			br.close();
		} catch(Exception e) {
			System.out.println(e);
		}
	}
	private void readGPECountryStateCountyResource(String path) {
		BufferedReader br = null;
		InputStream is = getResourceAsStream(path);
		if(is!=null)
			br = new BufferedReader(new InputStreamReader(is));
		else {
			System.out.println("Cannot get " + path + " as a resource!");
			return;
		}

		String line;
		try {
			while((line = br.readLine()) != null) {
				line = line.trim();
				line = line.replaceAll(" ", "_");
				if(line.contains("_"))
					gpeCountryStateCounty.add(line.toLowerCase());
				else
					gpeCountryStateCounty.add(line);
			}
			br.close();
		} catch(Exception e) {
			System.out.println(e);
		}
	}
	private void readGPECityResource(String path) {
		BufferedReader br = null;
		InputStream is = getResourceAsStream(path);
		if(is!=null)
			br = new BufferedReader(new InputStreamReader(is));
		else {
			System.out.println("Cannot get " + path + " as a resource!");
			return;
		}

		String line;
		try {
			while((line = br.readLine()) != null) {
				line = line.trim();
				line = line.replaceAll(" ", "_");
				gpeCity.add(line.toLowerCase());
			}
			br.close();
		} catch(Exception e) {
			System.out.println(e);
		}
	}
	private void readGPECountryResource(String path) {
		BufferedReader br = null;
		InputStream is = getResourceAsStream(path);
		if(is!=null)
			br = new BufferedReader(new InputStreamReader(is));
		else {
			System.out.println("Cannot get " + path + " as a resource!");
			return;
		}

		String line;
		try {
			while((line = br.readLine()) != null) {
				line = line.trim();
				line = line.replaceAll(" ", "_");
				gpeCountry.add(line);
			}
			br.close();
		} catch(Exception e) {
			System.out.println(e);
		}
	}
	private void readGPEStateResource(String path) {
		BufferedReader br = null;
		InputStream is = getResourceAsStream(path);
		if(is!=null)
			br = new BufferedReader(new InputStreamReader(is));
		else {
			System.out.println("Cannot get " + path + " as a resource!");
			return;
		}

		String line;
		try {
			while((line = br.readLine()) != null) {
				line = line.trim();
				line = line.replaceAll(" ", "_");
				if(line.contains("_"))
					gpeState.add(line.toLowerCase());
				else {
					if(line.length()==2 || (line.length()==3 && line.endsWith(".")) )
						gpeState.add(line);
					else
						gpeState.add(line.toLowerCase());
				}
			}
			br.close();
		} catch(Exception e) {
			System.out.println(e);
		}
	}
	private void readGPECountyResource(String path) {
		BufferedReader br = null;
		InputStream is = getResourceAsStream(path);
		if(is!=null)
			br = new BufferedReader(new InputStreamReader(is));
		else {
			System.out.println("Cannot get " + path + " as a resource!");
			return;
		}

		String line;
		try {
			while((line = br.readLine()) != null) {
				line = line.trim();
				line = line.replaceAll(" ", "_");
				gpeCounty.add(line.toLowerCase());
			}
			br.close();
		} catch(Exception e) {
			System.out.println(e);
		}
	}


	public void readEthnicAndNationality() {
		ethnicGroup = new HashSet<String>();
		nationality = new HashSet<String>();

		readEthnicGroup();
		readNationality();
	}

	public boolean isEthnicGroup(String n) {
		if(ethnicGroup.contains(n.toLowerCase()))
			return true;
		else
			return false;
	}

	public boolean isNationality(String n) {
		if(nationality.contains(n.toLowerCase()))
			return true;
		else
			return false;
	}

	private void readEthnicGroup() {
		readEthnicGroupResource("edu/illinois/cs/cogcomp/illinoisRE/lists/ethnicGroups");
	}
	private void readNationality() {
		readNationalityResource("edu/illinois/cs/cogcomp/illinoisRE/lists/nationalities");
	}

	private void readEthnicGroupResource(String path) {
		BufferedReader br = null;
		InputStream is = getResourceAsStream(path);
		if(is!=null)
			br = new BufferedReader(new InputStreamReader(is));
		else {
			System.out.println("Cannot get " + path + " as a resource!");
			return;
		}

		String line;
		try {
			while((line = br.readLine()) != null) {
				line = line.trim();
				line = line.replaceAll(" ", "_");
				if(line.length() > 2)
					ethnicGroup.add(line.toLowerCase());
			}
			br.close();
		} catch(Exception e) {
			System.out.println(e);
		}
	}
	private void readNationalityResource(String path) {
		BufferedReader br = null;
		InputStream is = getResourceAsStream(path);
		if(is!=null)
			br = new BufferedReader(new InputStreamReader(is));
		else {
			System.out.println("Cannot get " + path + " as a resource!");
			return;
		}

		String line;
		try {
			while((line = br.readLine()) != null) {
				line = line.trim();
				line = line.replaceAll(" ", "_");
				nationality.add(line.toLowerCase());
			}
			br.close();
		} catch(Exception e) {
			System.out.println(e);
		}
	}

	public void readOrg() {
		orgGovtMultiWords = new HashSet<String>();
		orgGovtAbbrev = new HashSet<String>();
		orgGovtSingleWord = new HashSet<String>();
		orgCommercial = new HashSet<String>();
		orgGeneric = new HashSet<String>();
		orgPolitical = new HashSet<String>();
		orgTerrorist = new HashSet<String>();

		readOrgGovt();
		readOrgCommercial();
		readOrgGeneric();
		readOrgPolitical();
		readOrgTerrorist();
	}
	/*
	public boolean isOrgGovtMultiWords(String n) {
		if(orgGovtMultiWords.contains(n.toLowerCase()))
			return true;
		else
			return false;
	}
	*/

	public boolean isOrgGovtMultiWords(String n) {
		boolean found = false;
		String t = n.toLowerCase();
		t = t.replaceAll(" ", "_");
		for(Iterator<String> it=orgGovtMultiWords.iterator(); it.hasNext();) {
			String w = it.next();
			if(t.endsWith(w)) {
				found = true;
				break;
			}
		}
		return found;
	}
	public boolean isOrgGovtAbbrev(String n) {
		if(orgGovtAbbrev.contains(n))
			return true;
		else
			return false;
	}
	public boolean isOrgGovtSingleWord(String n) {
		if(orgGovtSingleWord.contains(n))
			return true;
		else
			return false;
	}
	private void readOrgGovt() {
		readOrgGovtMultiWordsResource("edu/illinois/cs/cogcomp/illinoisRE/lists/US.govt.multiWords");
		readOrgGovtMultiWordsResource("edu/illinois/cs/cogcomp/illinoisRE/lists/UK.govt");
		readOrgGovtMultiWordsResource("edu/illinois/cs/cogcomp/illinoisRE/lists/HK.govt");

		readOrgGovtAbbrevResource("edu/illinois/cs/cogcomp/illinoisRE/lists/US.govt.abbrev");
		readOrgGovtSingleWordResource("edu/illinois/cs/cogcomp/illinoisRE/lists/US.govt.singleWord");
	}
	private void readOrgGovtMultiWordsResource(String path) {
		BufferedReader br = null;
		InputStream is = getResourceAsStream(path);
		if(is!=null)
			br = new BufferedReader(new InputStreamReader(is));
		else {
			System.out.println("Cannot get " + path + " as a resource!");
			return;
		}

		String line;
		try {
			while((line = br.readLine()) != null) {
				line = line.trim();
				line = line.replaceAll(" ", "_");
				orgGovtMultiWords.add(line.toLowerCase());
			}
			br.close();
		} catch(Exception e) {
			System.out.println(e);
		}
	}
	private void readOrgGovtAbbrevResource(String path) {
		BufferedReader br = null;
		InputStream is = getResourceAsStream(path);
		if(is!=null)
			br = new BufferedReader(new InputStreamReader(is));
		else {
			System.out.println("Cannot get " + path + " as a resource!");
			return;
		}

		String line;
		try {
			while((line = br.readLine()) != null) {
				line = line.trim();
				orgGovtAbbrev.add(line.toLowerCase());
			}
			br.close();
		} catch(Exception e) {
			System.out.println(e);
		}
	}
	private void readOrgGovtSingleWordResource(String path) {
		BufferedReader br = null;
		InputStream is = getResourceAsStream(path);
		if(is!=null)
			br = new BufferedReader(new InputStreamReader(is));
		else {
			System.out.println("Cannot get " + path + " as a resource!");
			return;
		}

		String line;
		try {
			while((line = br.readLine()) != null) {
				line = line.trim();
				orgGovtSingleWord.add(line.toLowerCase());
			}
			br.close();
		} catch(Exception e) {
			System.out.println(e);
		}
	}

	public boolean isOrgCommercial(String n) {
		if(orgCommercial.contains(n))
			return true;
		else
			return false;
	}
	private void readOrgCommercial() {
		readOrgCommercialResource("edu/illinois/cs/cogcomp/illinoisRE/lists/org.onStockExchange");
		readOrgCommercialResource("edu/illinois/cs/cogcomp/illinoisRE/lists/known_corporations.lst");
	}
	private void readOrgCommercialResource(String path) {
		BufferedReader br = null;
		InputStream is = getResourceAsStream(path);
		if(is!=null)
			br = new BufferedReader(new InputStreamReader(is));
		else {
			System.out.println("Cannot get " + path + " as a resource!");
			return;
		}

		String line;
		try {
			while((line = br.readLine()) != null) {
				line = line.trim();
				line = line.replaceAll(" ", "_");
				//orgCommercial.add(" "+line+" ");
				orgCommercial.add(line);
			}
			br.close();
		} catch(Exception e) {
			System.out.println(e);
		}
	}


	public boolean isOrgGeneric(String n) {
		boolean found = false;
		String t = n.trim();
		t = t.replaceAll(" ", "_");
		t = " "+t+" ";
		for(Iterator<String> it=orgGeneric.iterator(); it.hasNext();) {
			String w = it.next();
			if(t.contains(w)) {
				found = true;
				break;
			}
		}
		return found;
	}
	private void readOrgGeneric() {
		readOrgGenericResource("edu/illinois/cs/cogcomp/illinoisRE/lists/WikiOrganizations.lst");
	}
	private void readOrgGenericResource(String path) {
		BufferedReader br = null;
		InputStream is = getResourceAsStream(path);
		if(is!=null)
			br = new BufferedReader(new InputStreamReader(is));
		else {
			System.out.println("Cannot get " + path + " as a resource!");
			return;
		}

		String line;
		try {
			while((line = br.readLine()) != null) {
				line = line.trim();
				line = line.replaceAll(" ", "_");
				orgGeneric.add(" "+line+" ");
			}
			br.close();
		} catch(Exception e) {
			System.out.println(e);
		}
	}


	public boolean isOrgPolitical(String n) {
		boolean found = false;
		String t = n.trim();
		t = t.replaceAll(" ", "_");
		t = " "+t+" ";
		for(Iterator<String> it=orgPolitical.iterator(); it.hasNext();) {
			String w = it.next();
			if(t.contains(w)) {
				found = true;
				break;
			}
		}
		return found;
	}
	private void readOrgPolitical() {
		readOrgPoliticalResource("edu/illinois/cs/cogcomp/illinoisRE/lists/political_parties.wiki");
	}
	private void readOrgPoliticalResource(String path) {
		BufferedReader br = null;
		InputStream is = getResourceAsStream(path);
		if(is!=null)
			br = new BufferedReader(new InputStreamReader(is));
		else {
			System.out.println("Cannot get " + path + " as a resource!");
			return;
		}

		String line;
		try {
			while((line = br.readLine()) != null) {
				line = line.trim();
				line = line.replaceAll(" ", "_");
				orgPolitical.add(" " + line.toLowerCase() + " ");
			}
			br.close();
		} catch(Exception e) {
			System.out.println(e);
		}
	}

	public boolean isOrgTerrorist(String n) {
		boolean found = false;
		String t = n.trim();
		t = t.replaceAll(" ", "_");
		t = " "+t+" ";
		for(Iterator<String> it=orgTerrorist.iterator(); it.hasNext();) {
			String w = it.next();
			if(t.contains(w)) {
				found = true;
				break;
			}
		}
		return found;
	}
	private void readOrgTerrorist() {
		readOrgTerroristResource("edu/illinois/cs/cogcomp/illinoisRE/lists/terrorist.wiki");
	}
	private void readOrgTerroristResource(String path) {
		BufferedReader br = null;
		InputStream is = getResourceAsStream(path);
		if(is!=null)
			br = new BufferedReader(new InputStreamReader(is));
		else {
			System.out.println("Cannot get " + path + " as a resource!");
			return;
		}

		String line;
		try {
			while((line = br.readLine()) != null) {
				line = line.trim();
				line = line.replaceAll(" ", "_");
				orgPolitical.add(" " + line.toLowerCase() + " ");
			}
			br.close();
		} catch(Exception e) {
			System.out.println(e);
		}
	}



	public void readFac() {
		facBarrier = new HashSet<String>();
		facBuilding = new HashSet<String>();
		facConduit = new HashSet<String>();
		facPath = new HashSet<String>();
		facPlant = new HashSet<String>();
		facBuildingSubArea = new HashSet<String>();
		facGeneric = new HashSet<String>();

		readFacBarrier();
		readFacBuilding();
		readFacConduit();
		readFacPath();
		readFacPlant();
		readFacBuildingSubArea();
		readFacGeneric();
	}

	public boolean isFacBarrier(String n) {
		if(facBarrier.contains(n.toLowerCase()))
			return true;
		else
			return false;
	}
	public boolean isFacBuilding(String n) {
		if(facBuilding.contains(n.toLowerCase()))
			return true;
		else
			return false;
	}
	public boolean isFacConduit(String n) {
		if(facConduit.contains(n.toLowerCase()))
			return true;
		else
			return false;
	}
	public boolean isFacPath(String n) {
		if(facPath.contains(n.toLowerCase()))
			return true;
		else
			return false;
	}
	public boolean isFacPlant(String n) {
		if(facPlant.contains(n.toLowerCase()))
			return true;
		else
			return false;
	}
	public boolean isFacBuildingSubArea(String n) {
		if(facBuildingSubArea.contains(n.toLowerCase()))
			return true;
		else
			return false;
	}
	public boolean isFacGeneric(String n) {
		if(facGeneric.contains(n.toLowerCase()))
			return true;
		else
			return false;
	}
	private void readFacBarrier() {
		readFacBarrierResource("edu/illinois/cs/cogcomp/illinoisRE/lists/fac.barrier.wordnet");
	}
	private void readFacBuilding() {
		readFacBuildingResource("edu/illinois/cs/cogcomp/illinoisRE/lists/fac.building.wordnet");
		//readFacBuildingResource("edu/illinois/cs/cogcomp/illinoisRE/lists/fac.building.dbpedia");
	}
	private void readFacConduit() {
		readFacConduitResource("edu/illinois/cs/cogcomp/illinoisRE/lists/fac.conduit.wordnet");
	}
	private void readFacPath() {
		readFacPathResource("edu/illinois/cs/cogcomp/illinoisRE/lists/fac.path.wordnet");
		//readFacPathResource("edu/illinois/cs/cogcomp/illinoisRE/lists/fac.path.dbpedia");
	}
	private void readFacPlant() {
		readFacPlantResource("edu/illinois/cs/cogcomp/illinoisRE/lists/fac.plant.wordnet");
	}
	private void readFacBuildingSubArea() {
		readFacBuildingSubAreaResource("edu/illinois/cs/cogcomp/illinoisRE/lists/fac.building_subarea.wordnet");
	}
	private void readFacGeneric() {
		readFacGenericResource("edu/illinois/cs/cogcomp/illinoisRE/lists/fac.generic.wordnet");
	}
	private void readFacBarrierResource(String path) {
		BufferedReader br = null;
		InputStream is = getResourceAsStream(path);
		if(is!=null)
			br = new BufferedReader(new InputStreamReader(is));
		else {
			System.out.println("Cannot get " + path + " as a resource!");
			return;
		}

		String line;
		try {
			while((line = br.readLine()) != null) {
				line = line.trim();
				line = line.replaceAll(" ", "_");
				facBarrier.add(line.toLowerCase());
			}
			br.close();
		} catch(Exception e) {
			System.out.println(e);
		}
	}
	private void readFacBuildingResource(String path) {
		BufferedReader br = null;
		InputStream is = getResourceAsStream(path);
		if(is!=null)
			br = new BufferedReader(new InputStreamReader(is));
		else {
			System.out.println("Cannot get " + path + " as a resource!");
			return;
		}

		String line;
		try {
			while((line = br.readLine()) != null) {
				line = line.trim();
				line = line.replaceAll(" ", "_");
				facBuilding.add(line.toLowerCase());
			}
			br.close();
		} catch(Exception e) {
			System.out.println(e);
		}
	}
	private void readFacConduitResource(String path) {
		BufferedReader br = null;
		InputStream is = getResourceAsStream(path);
		if(is!=null)
			br = new BufferedReader(new InputStreamReader(is));
		else {
			System.out.println("Cannot get " + path + " as a resource!");
			return;
		}

		String line;
		try {
			while((line = br.readLine()) != null) {
				line = line.trim();
				line = line.replaceAll(" ", "_");
				facConduit.add(line.toLowerCase());
			}
			br.close();
		} catch(Exception e) {
			System.out.println(e);
		}
	}
	private void readFacPathResource(String path) {
		BufferedReader br = null;
		InputStream is = getResourceAsStream(path);
		if(is!=null)
			br = new BufferedReader(new InputStreamReader(is));
		else {
			System.out.println("Cannot get " + path + " as a resource!");
			return;
		}

		String line;
		try {
			while((line = br.readLine()) != null) {
				line = line.trim();
				line = line.replaceAll(" ", "_");
				facPath.add(line.toLowerCase());
			}
			br.close();
		} catch(Exception e) {
			System.out.println(e);
		}
	}
	private void readFacPlantResource(String path) {
		BufferedReader br = null;
		InputStream is = getResourceAsStream(path);
		if(is!=null)
			br = new BufferedReader(new InputStreamReader(is));
		else {
			System.out.println("Cannot get " + path + " as a resource!");
			return;
		}

		String line;
		try {
			while((line = br.readLine()) != null) {
				line = line.trim();
				line = line.replaceAll(" ", "_");
				facPlant.add(line.toLowerCase());
			}
			br.close();
		} catch(Exception e) {
			System.out.println(e);
		}
	}
	private void readFacBuildingSubAreaResource(String path) {
		BufferedReader br = null;
		InputStream is = getResourceAsStream(path);
		if(is!=null)
			br = new BufferedReader(new InputStreamReader(is));
		else {
			System.out.println("Cannot get " + path + " as a resource!");
			return;
		}

		String line;
		try {
			while((line = br.readLine()) != null) {
				line = line.trim();
				line = line.replaceAll(" ", "_");
				facBuildingSubArea.add(line.toLowerCase());
			}
			br.close();
		} catch(Exception e) {
			System.out.println(e);
		}
	}
	private void readFacGenericResource(String path) {
		BufferedReader br = null;
		InputStream is = getResourceAsStream(path);
		if(is!=null)
			br = new BufferedReader(new InputStreamReader(is));
		else {
			System.out.println("Cannot get " + path + " as a resource!");
			return;
		}

		String line;
		try {
			while((line = br.readLine()) != null) {
				line = line.trim();
				line = line.replaceAll(" ", "_");
				facGeneric.add(line.toLowerCase());
			}
			br.close();
		} catch(Exception e) {
			System.out.println(e);
		}
	}


	private void readWea() {
		wea = new HashSet<String>();

		readWeaResource("edu/illinois/cs/cogcomp/illinoisRE/lists/wea.wordnet");
		readWeaResource("edu/illinois/cs/cogcomp/illinoisRE/lists/wea.aircraft");
		readWeaResource("edu/illinois/cs/cogcomp/illinoisRE/lists/wea.artillery");
		readWeaResource("edu/illinois/cs/cogcomp/illinoisRE/lists/wea.firearms");
		readWeaResource("edu/illinois/cs/cogcomp/illinoisRE/lists/wea.melee");
		readWeaResource("edu/illinois/cs/cogcomp/illinoisRE/lists/wea.missile");
		readWeaResource("edu/illinois/cs/cogcomp/illinoisRE/lists/wea.torpedoe");
	}
	public boolean isWea(String n) {
		if(wea.contains(n))
			return true;
		else
			return false;
	}
	private void readWeaResource(String path) {
		BufferedReader br = null;
		InputStream is = getResourceAsStream(path);
		if(is!=null)
			br = new BufferedReader(new InputStreamReader(is));
		else {
			System.out.println("Cannot get " + path + " as a resource!");
			return;
		}

		String line;
		try {
			while((line = br.readLine()) != null) {
				line = line.trim();
				line = line.replaceAll(" ", "_");
				wea.add(line);
			}
			br.close();
		} catch(Exception e) {
			System.out.println(e);
		}
	}


	private void readVeh() {
		veh = new HashSet<String>();

		readVehResource("edu/illinois/cs/cogcomp/illinoisRE/lists/veh.wordnet");
	}
	public boolean isVeh(String n) {
		if(veh.contains(n))
			return true;
		else
			return false;
	}
	private void readVehResource(String path) {
		BufferedReader br = null;
		InputStream is = getResourceAsStream(path);
		if(is!=null)
			br = new BufferedReader(new InputStreamReader(is));
		else {
			System.out.println("Cannot get " + path + " as a resource!");
			return;
		}

		String line;
		try {
			while((line = br.readLine()) != null) {
				line = line.trim();
				line = line.replaceAll(" ", "_");
				veh.add(line.toLowerCase());
			}
			br.close();
		} catch(Exception e) {
			System.out.println(e);
		}
	}


	public Set<String> getAttributeForWikiTitle(String title) {
		return wikiTitleAttributes.get(title);
	}

	private void readWikiTitleAttributeResource(String path) {
		BufferedReader br = null;
		InputStream is = getResourceAsStream(path);
		if(is!=null) 
			br = new BufferedReader(new InputStreamReader(is)); 
		else {
			System.out.println("Cannot get " + path + " as a resource!");
			return;
		}
		
		String line;
		try {
			while((line = br.readLine()) != null) {
				line = line.trim();
				String[] tokens = line.split("\t");
				String wikiTitle = tokens[1];
				Set<String> attributes = new HashSet<String>();
				for(int i=2; i<tokens.length; i+=2) {
					attributes.add(tokens[i]);
				}
				wikiTitleAttributes.put(wikiTitle, attributes);
			}
			br.close();
		} catch(Exception e) {
			System.out.println(e);
		}
	}
	
	
	/*
	private void readCommonNouns(String filename) {
		collectiveNouns = new HashSet<String>();
		List<String> mylist = IOManager.readLines(filename);
		for(int i=0; i<mylist.size(); i++) {
			collectiveNouns.add(mylist.get(i));
		}
	}
	*/

	/*
	public boolean isCollectiveNoun(String n) {
		if(collectiveNouns.contains(n.toLowerCase())) {
			return true;
		}
		else {
			return false;
		}
	}
	*/
	
	private void readGPECorefNouns(String filename) {
		gpeCorefNouns = new HashSet<String>();
		List<String> mylist = IOManager.readLines(filename);
		for(int i=0; i<mylist.size(); i++) {
			gpeCorefNouns.add(mylist.get(i));
		}
	}
	
	public boolean isGPECorefNoun(String n) {
		if(gpeCorefNouns.contains(n.toLowerCase())) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public void readWordNetNounCollocations(String filename) {
		wordnetNounCollocations = new HashSet<String>();
		List<String> mylist = IOManager.readLines(filename);
		for(int i=0; i<mylist.size(); i++) {
			wordnetNounCollocations.add(mylist.get(i).replaceAll("_", " "));
		}
	}
	
	public boolean isWordnetNounCollocation(String n) {
		if(wordnetNounCollocations.contains(n.toLowerCase())) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public void readPersonAndJobTitles(String filename) {
		personAndJobTitles = new HashSet<String>();
		personAndJobTitlesCollocations = new HashSet<String>();
		List<String> mylist = IOManager.readLines(filename);
		for(int i=0; i<mylist.size(); i++) {
			String s = mylist.get(i).toLowerCase();
			personAndJobTitles.add(s);
			String[] tokens = s.split(" ");
			if(tokens.length>=2) {
				personAndJobTitlesCollocations.add(s);
			}
		}
	}
	
	public boolean isPersonAndJobTitles(String n) {
		String targetTitle = n.toLowerCase();
		String[] tokens = targetTitle.split(" ");
		String lastToken = tokens[tokens.length-1];
		
		if(personAndJobTitles.contains(targetTitle) || personAndJobTitles.contains(lastToken)) {
			return true;
		}
		else {
			for(Iterator<String> it=personAndJobTitlesCollocations.iterator(); it.hasNext();) {
				String currentTitle = it.next();
				if(targetTitle.endsWith(currentTitle)) {
					return true;
				}
			}
			return false;
		}
	}
	public boolean isPersonAndJobTitlesEntireMatch(String n) {
		String targetTitle = n.toLowerCase();
		String[] tokens = targetTitle.split(" ");
		String lastToken = tokens[tokens.length-1];
		
		if(personAndJobTitles.contains(targetTitle)) {
			return true;
		}
		else {
			for(Iterator<String> it=personAndJobTitlesCollocations.iterator(); it.hasNext();) {
				String currentTitle = it.next();
				if(targetTitle.endsWith(currentTitle)) {
					return true;
				}
			}
			return false;
		}
	}
	
	
	public void readCounties(String filename) {
		counties = new HashSet<String>();
		List<String> mylist = IOManager.readLines(filename);
		for(int i=0; i<mylist.size(); i++) {
			String county = mylist.get(i);
			if(county.indexOf(" County")!=-1) {
				county = county.substring(0, county.indexOf(" County"));
				counties.add(county.toLowerCase());
			}
		}
	}
	
	public boolean isCounty(String n) {
		if(counties.contains(n.toLowerCase())) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public void readStates(String filename) {
		states = new HashSet<String>();
		List<String> mylist = IOManager.readLines(filename);
		for(int i=0; i<mylist.size(); i++) {
			String state = mylist.get(i);
			String[] tokens = state.split("\t");
			states.add(tokens[0].toLowerCase());
		}
	}
	
	public boolean isState(String n) {
		if(states.contains(n.toLowerCase())) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public void readNationalities(String filename) {
		nationalities = new HashSet<String>();
		List<String> mylist = IOManager.readLines(filename);
		for(int i=0; i<mylist.size(); i++) {
			nationalities.add(mylist.get(i).toLowerCase());
		}
	}
/*	
	public boolean isNationality(String n) {
		if(nationalities.contains(n.toLowerCase())) {
			return true;
		}
		else {
			return false;
		}
	}
*/
	public void readWikiTitlesToHash(String filename) {
		wikiTitlesHashLen2 = new HashSet<String>();
		wikiTitlesHashLen3 = new HashSet<String>();
		List<String> mylist = IOManager.readLines(filename);
		for(int i=0; i<mylist.size(); i++) {
			String[] tokens = mylist.get(i).toLowerCase().split(" ");
			//for(int j=0; j<tokens.length; j++) {
			//	wikiTitlesHash.add(tokens[j]);
			//}
			for(int startIndex=0; startIndex<(tokens.length-1); startIndex++) {
				wikiTitlesHashLen2.add(tokens[startIndex] + " " + tokens[startIndex+1]);
			}
			for(int startIndex=0; startIndex<(tokens.length-2); startIndex++) {
				wikiTitlesHashLen3.add(tokens[startIndex] + " " + tokens[startIndex+1] + " " + tokens[startIndex+2]);
			}
			//wikiTitlesHash.add(mylist.get(i).toLowerCase());
		}
	}
	// string of length 1 will not come in to this method; strings of length 2 are covered by the wikiTitlesHash
	public void readWikiTitlesToString(String filename) {
		StringBuffer s = new StringBuffer("|");
		List<String> mylist = IOManager.readLines(filename);
		for(int i=0; i<mylist.size(); i++) {
			String currentString = mylist.get(i).toLowerCase();
			String[] tokens = currentString.split(" ");
			if(tokens.length>=4) {
				s.append(currentString+"|");
			}
		}
		wikiTitlesString = s.toString();
	}
	/*
	public boolean isPartOfWikiTitle(String n) {
		String targetString = n.toLowerCase();
		if(wikiTitlesString.contains(targetString)) 
			return true;
		else
			return false;
	}
	*/
	public boolean isPartOfWikiTitle(String n) {
		String s = n.toLowerCase();
		String[] tokens = s.split(" ");
		
		if(tokens.length==2 && wikiTitlesHashLen2.contains(s)) {
			return true;
		}
		else if(tokens.length==3 && wikiTitlesHashLen3.contains(s)) {
			return true;
		}
		else if(tokens.length>=4) {
			if(wikiTitlesString.contains(s)) 
				return true;
			else
				return false;
		}
		else {
			return false;
		}
	}
	
}