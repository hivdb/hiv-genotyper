package edu.stanford.hivdb.genotyper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public interface GenotypeReference {

	Integer getComparisonStartPosition();

	/** compare given sequence with current reference
	 *
	 * @param sequence a string of DNA sequence
	 * @param firstNA HXB2 starting position of the given sequence
	 * @param lastNA HXB2 ending position of the given sequence
	 * @return BoundGenotype object contained the comparison result
	 */
	default public BoundGenotype compare(
			String sequence, int firstNA, int lastNA) {
		int refFirstNA = getFirstNA();
		int refLastNA = getLastNA();
		int maxFirstNA = firstNA > refFirstNA ? firstNA : refFirstNA;
		int minLastNA = lastNA < refLastNA ? lastNA : refLastNA;
		int refOffset = maxFirstNA - refFirstNA;
		int seqOffset = maxFirstNA - firstNA;
		int compareLength = minLastNA - maxFirstNA + 1;
		String refSequence = getSequence();
		List<Integer> discordanceList = new ArrayList<>();
		StringBuffer curCodon = new StringBuffer();
		List<Integer> curCodonDiscordance = new ArrayList<>();
		for (int i = 0; i < compareLength; i++) {
			if ((maxFirstNA + i - getComparisonStartPosition()) % 3 == 0) {
				// to check if the current position is the beginning of a codon
				Set<String> codons = getPositionalIgnoredCodons(maxFirstNA + i - 3);
				if (codons == null || !codons.contains(curCodon.toString())) {
					// keep the result if the current codon is not a SDRM
					discordanceList.addAll(curCodonDiscordance);
				}
				curCodon.setLength(0);
				curCodonDiscordance.clear();
			}
			char refNA = refSequence.charAt(refOffset + i);
			char seqNA = sequence.charAt(seqOffset + i);
			String uaSeqNA = AmbiguousNATranslator.translate(seqNA);
			if (uaSeqNA != null && uaSeqNA.indexOf(refNA) == -1) {
				// seqNA == refNA or seqNA's unambiguous NAs has refNA
				curCodonDiscordance.add(maxFirstNA + i);
			}
			curCodon.append(seqNA);
		}
		Set<String> codons = getPositionalIgnoredCodons(minLastNA - 3);
		if (codons == null || !codons.contains(curCodon.toString())) {
			// keep the result if the current codon is not a SDRM
			discordanceList.addAll(curCodonDiscordance);
		}
		return new BoundGenotype(
			this, sequence, maxFirstNA, minLastNA, discordanceList);
	}


	public Set<String> getPositionalIgnoredCodons(int position);

	/** a getter to get current reference's genotype
	 *
	 * @return a Genotype object that indicates the corresponding genotype
	 */
	public Genotype getGenotype();

	/** a getter to get current reference's genbank accession id
	 *
	 * @return string
	 */
	public String getAccession();

	/** a getter to get current reference's start position in HXB2
	 *
	 * @return integer
	 */
	public Integer getFirstNA();

	/** a getter to get current reference's end position in HXB2
	 *
	 * @return integer
	 */
	public Integer getLastNA();

	/** a getter to get current reference's sequence
	 *
	 * @return string
	 */
	public String getSequence();

}
