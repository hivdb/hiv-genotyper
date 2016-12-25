package edu.stanford.hivdb.genotyper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class HIVGenotypeReference implements GenotypeReference {

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
	}
	
	public static List<BoundGenotype> compareAll(String sequence, int firstNA, int lastNA) {
		List<BoundGenotype> results = new ArrayList<>();
		for (HIVGenotypeReference ref : references) {
			results.add(ref.compare(sequence, firstNA, lastNA));
		}
		results.sort((r1, r2) -> r1.getDistance().compareTo(r2.getDistance()));
		return results;
	}
	
	@Override
	public BoundGenotype compare(String sequence, int firstNA, int lastNA) {
		int refFirstNA = this.firstNA;
		int refLastNA = this.lastNA;
		int maxFirstNA = firstNA > refFirstNA ? firstNA : refFirstNA;
		int minLastNA = lastNA < refLastNA ? lastNA : refLastNA;
		int refOffset = maxFirstNA - refFirstNA;
		int seqOffset = maxFirstNA - firstNA;
		int compareLength = minLastNA - maxFirstNA + 1;
		List<Integer> discordanceList = new ArrayList<>();
		for (int i = 0; i < compareLength; i++) {
			char refNA = this.sequence.charAt(refOffset + i);
			char seqNA = sequence.charAt(seqOffset + i);
			if (refNA != seqNA) {
				// TODO: check ambiguous nucleotide
				discordanceList.add(maxFirstNA + i);
			}
		}
		return new HIVBoundGenotype(
			this, sequence, maxFirstNA, minLastNA, discordanceList);
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
	public String toString() {
		return getAccession() + " (" + genotypeName + ")";
	}
}
