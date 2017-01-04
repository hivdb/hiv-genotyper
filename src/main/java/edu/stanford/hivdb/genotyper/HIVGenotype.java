package edu.stanford.hivdb.genotyper;

import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;


public class HIVGenotype implements Genotype {

	private static final Double MIN_PRIMARY_REGIONAL_GENOTYPE_PROPORTION = 0.9; // 90%
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
	public Double getMinPrimaryRegionalGenotypeProportion() {
		return MIN_PRIMARY_REGIONAL_GENOTYPE_PROPORTION;
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
	public List<Genotype> getCanonicalGenotypes() {
		if (canonicalName != null) {
			return Arrays
				.stream(StringUtils.split(canonicalName, '|'))
				.map(n -> genotypes.get(n))
				.collect(Collectors.toList());
		}
		else {
			List<Genotype> canonicals = new ArrayList<>();
			canonicals.add(this);
			return canonicals;
		}
	}

	@Override
	public Boolean checkDistance(double distance) {
		return distance < distanceTolerance;
	}

	@Override
	public List<RegionalGenotype> getRegionalGenotypes(int firstNA, int lastNA) {
		Map<Genotype, Double> mapResults = new LinkedHashMap<>();
		double length = lastNA - firstNA;
		if (isSimpleCRF) {
			for (CRFRegion region : regions) {
				if (lastNA >= region.start && firstNA <= region.end) {
					// intersected
					int start = firstNA > region.start ? firstNA : region.start;
					int end = lastNA < region.end ? lastNA : region.end;
					Genotype genotype = genotypes.get(region.genotypeName);
					double proportion = mapResults.getOrDefault(genotype, 0.0);
					proportion += (end - start) / length;
					mapResults.put(genotype, proportion);
				}
			}
		}
		else {
			mapResults.put(this, 1.0);
		}
		return mapResults
			.entrySet().stream()
			.map(e -> new RegionalGenotype(e.getKey(), e.getValue()))
			.collect(Collectors.toList());
	}

	@Override
	public Boolean isRecombination() {
		return name.startsWith("X");
	}

	@Override
	public String toString() {
		return getDisplayName();
	}

	public HIVClassificationLevel getClassificationLevel() {
		return classificationLevel;
	}

}
