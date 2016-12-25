package edu.stanford.hivdb.genotyper;

import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.LinkedHashMap;
import java.util.List;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;


public class HIVGenotype implements Genotype {

	private static int CRF_REGION_TOLERANCE_SIZE = 50;
	private static Map<String, HIVGenotype> genotypes = new LinkedHashMap<>();
	private String name;
	private Boolean isSimpleCRF;
	private String displayName;
	private String canonicalName;
	private HIVClassificationLevel classificationLevel;
	private Double distanceTolerance;
	private List<CRFRegion> regions;

	private static class CRFRegion {
		String genotypeName;
		int start;
		int end;
	}

	static {
		InputStream json = HIVGenotype.class.getClassLoader().getResourceAsStream("HIVGenotypes.json");
		genotypes = new Gson().fromJson(
				new BufferedReader(new InputStreamReader(json)),
			    new TypeToken<Map<String, HIVGenotype>>(){}.getType());
	}

	private HIVGenotype() {}

	public static HIVGenotype getInstance(String name) {
		return genotypes.get(name);
	}

	@Override
	public String getIndexName() {
		return name;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}

	@Override
	public HIVGenotype getCanonicalGenotype() {
		if (classificationLevel ==
				HIVClassificationLevel.SUBSUBTYPE) {
			return genotypes.get(canonicalName);
		}
		else {
			return this;
		}
	}

	@Override
	public Boolean checkDistance(double distance) {
		return distance < this.distanceTolerance;
	}

	@Override
	public HIVGenotype getRegionalGenotype(int firstNA, int lastNA) {
		if (isSimpleCRF) {
			int tolerance = CRF_REGION_TOLERANCE_SIZE;
			for (CRFRegion region : regions) {
				if (firstNA >= region.start - tolerance &&
						lastNA <= region.end + tolerance) {
					return genotypes.get(region.genotypeName);
				}
			}
			return this;
		}
		else {
			return this;
		}
	}
	
	@Override
	public String toString() {
		return getDisplayName();
	}

}