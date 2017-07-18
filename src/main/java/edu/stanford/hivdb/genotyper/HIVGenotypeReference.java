package edu.stanford.hivdb.genotyper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.neovisionaries.i18n.CountryCode;

public class HIVGenotypeReference implements GenotypeReference {

	private static Map<Integer, Set<String>> sdrmCodonsByPositions;
	private static List<HIVGenotypeReference> references;
	private static Integer treeFirstNA;
	private static Integer treeLastNA;
	private static int[][][] referenceMismatchTree;
	private static int codonNAOffset = 2253;
	private String genotypeName;
	private String country;
	private String authorYear;
	private Integer year;
	private String accession;
	private Integer firstNA;
	private Integer lastNA;
	private String sequence;

	static {
		InputStream json = (
			HIVGenotypeReference.class.getClassLoader()
			.getResourceAsStream("HIVGenotypeReferences.json"));
		references = new Gson().fromJson(
				new BufferedReader(new InputStreamReader(json)),
			    new TypeToken<List<HIVGenotypeReference>>(){}.getType());
		json = (
			HIVGenotypeReference.class.getClassLoader()
			.getResourceAsStream("HIVSDRMs.json"));
		sdrmCodonsByPositions = new Gson().fromJson(
				new BufferedReader(new InputStreamReader(json)),
			    new TypeToken<Map<Integer, Set<String>>>(){}.getType());

		List<GenotypeReference> casted = new ArrayList<>();
		for (GenotypeReference ref : references) {
			casted.add(ref);
			if (treeFirstNA == null) {
				treeFirstNA = ref.getFirstNA();
			}
			if (treeLastNA == null) {
				treeLastNA = ref.getLastNA();
			}
			if (!treeFirstNA.equals(ref.getFirstNA()) || !treeLastNA.equals(ref.getLastNA())) {
				throw new IllegalArgumentException(String.format(
					"Reference %s has a different NA boundary (%d - %d) " +
					"like other references (%d - %d).",
					ref.getAccession(),
					ref.getFirstNA(), ref.getLastNA(),
					treeFirstNA, treeLastNA
				));
			}
		}
		
		referenceMismatchTree = GenotypeReference.buildReferenceMismatchTree(casted, treeFirstNA, treeLastNA);
	}

	public static HIVGenotypeResult compareAll(String sequence, int firstNA) {
		int lastNA = firstNA + sequence.length() - 1;
		return compareAll(sequence, firstNA, lastNA);
	}

	public static HIVGenotypeResult compareAll(String sequence, int firstNA, int lastNA) {
		Map<Integer, List<Integer>> discordanceListPerRef = GenotypeReference.compareWithSearchTree(
			sequence, firstNA, lastNA,
			referenceMismatchTree, treeFirstNA, treeLastNA,
			codonNAOffset, sdrmCodonsByPositions);
		int numRefs = references.size();
		List<BoundGenotype> results = new ArrayList<>();
		for (int refIdx = 0; refIdx < numRefs; refIdx ++) {
			HIVGenotypeReference ref = references.get(refIdx);
			results.add(ref.getBoundGenotype(
				sequence, firstNA, lastNA,
				discordanceListPerRef.getOrDefault(refIdx, new ArrayList<>())
			));
		}
		return new HIVGenotypeResult(results);
	}

	@Override
	public Integer getFirstNA() {
		return firstNA;
	}

	@Override
	public Integer getLastNA() {
		return lastNA;
	}

	@Override
	public Genotype getGenotype() {
		return HIVGenotype.getInstance(genotypeName);
	}

	@Override
	public String getCountry() {
		CountryCode countryCode = CountryCode.getByCode(country);
		if (countryCode == null) {
			return country;
		}
		else {
			return countryCode.getName();
		}
	}

	@Override
	public String getAuthorYear() {
		return authorYear;
	}

	@Override
	public Integer getYear() {
		return year != null ? year : null;
	}

	@Override
	public String getAccession() {
		return accession;
	}

	@Override
	public String getSequence() {
		return sequence;
	}

	@Override
	public String toString() {
		return getAccession() + " (" + genotypeName + ")";
	}
}
