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

public class HIVGenotypeReference implements GenotypeReference {

	private static Map<Integer, Set<String>> sdrmCodonsByPositions;
	private static List<HIVGenotypeReference> references;
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

	}

	public static HIVGenotypeResult compareAll(String sequence, int firstNA) {
		int lastNA = firstNA + sequence.length() - 1;
		return compareAll(sequence, firstNA, lastNA);
	}

	public static HIVGenotypeResult compareAll(String sequence, int firstNA, int lastNA) {
		List<BoundGenotype> results = new ArrayList<>();
		for (HIVGenotypeReference ref : references) {
			results.add(ref.compare(sequence, firstNA, lastNA));
		}
		return new HIVGenotypeResult(results);
	}

	@Override
	public Integer getComparisonStartPosition() {
		return 2253;
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
	public Set<String> getPositionalIgnoredCodons(int position) {
		return sdrmCodonsByPositions.get(position);
	}

	@Override
	public Genotype getGenotype() {
		return HIVGenotype.getInstance(genotypeName);
	}

	public String getCountry() {
		return country;
	}

	public String getAuthorYear() {
		return authorYear;
	}

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
