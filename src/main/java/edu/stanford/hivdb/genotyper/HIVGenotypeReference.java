package edu.stanford.hivdb.genotyper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class HIVGenotypeReference implements GenotypeReference {

	private static Map<Character, String> ambiguousNAs;
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

		ambiguousNAs = new HashMap<>();
		ambiguousNAs.put('A', "A");
		ambiguousNAs.put('C', "C");
		ambiguousNAs.put('G', "G");
		ambiguousNAs.put('T', "T");
		ambiguousNAs.put('R', "AG");
		ambiguousNAs.put('Y', "CT");
		ambiguousNAs.put('M', "AC");
		ambiguousNAs.put('W', "AT");
		ambiguousNAs.put('S', "CG");
		ambiguousNAs.put('K', "GT");
		ambiguousNAs.put('B', "CGT");
		ambiguousNAs.put('D', "AGT");
		ambiguousNAs.put('H', "ACT");
		ambiguousNAs.put('V', "ACG");
		ambiguousNAs.put('N', "ACGT");
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
		StringBuffer curCodon = new StringBuffer();
		List<Integer> curCodonDiscordance = new ArrayList<>();
		for (int i = 0; i < compareLength; i++) {
			if ((maxFirstNA + i/* - 2253*/) % 3 == 0) {
				// to check if the current position is the beginning
				// of a codon. No need to subtract by 2253 since 2253 % 3 = 0
				Set<String> codons = sdrmCodonsByPositions.get(maxFirstNA + i - 3);
				if (codons == null || !codons.contains(curCodon.toString())) {
					// keep the result if the current codon is not a SDRM
					discordanceList.addAll(curCodonDiscordance);
				}
				curCodon.setLength(0);
				curCodonDiscordance.clear();
			}
			char refNA = this.sequence.charAt(refOffset + i);
			char seqNA = sequence.charAt(seqOffset + i);
			String uaSeqNA = ambiguousNAs.get(seqNA);
			if (uaSeqNA != null && uaSeqNA.indexOf(refNA) == -1) {
				// seqNA == refNA or seqNA's unambiguous NAs has refNA
				curCodonDiscordance.add(maxFirstNA + i);
			}
			curCodon.append(seqNA);
		}
		Set<String> codons = sdrmCodonsByPositions.get(minLastNA - 3);
		if (codons == null || !codons.contains(curCodon.toString())) {
			// keep the result if the current codon is not a SDRM
			discordanceList.addAll(curCodonDiscordance);
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
