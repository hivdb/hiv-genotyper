package edu.stanford.hivdb.genotyper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.primitives.Ints;

public interface GenotypeReference {

	public static int[] getNAIndice(char na) {
		List<Integer> naIndice = new ArrayList<>();
		String unNA = AmbiguousNATranslator.translate(na);
		for (char sNA : unNA.toCharArray()) {
			switch(sNA) {
				case 'A':
					naIndice.add(0);
					break;
				case 'C':
					naIndice.add(1);
					break;
				case 'G':
					naIndice.add(2);
					break;
				case 'T':
					naIndice.add(3);
					break;
			}
		}
		return Ints.toArray(naIndice);
	}
	
	public static int[] getInverseNAIndice(char na) {
		int[] naIndice = getNAIndice(na);
		List<Integer> inverseNAIndice = new ArrayList<>();
		for (int i = 0; i < 4; i ++) {
			boolean found = false;
			for (int naIdx : naIndice) {
				if (i == naIdx) {
					found = true;
					break;
				}
			}
			if (!found) {
				inverseNAIndice.add(i);
			}
		}
		return Ints.toArray(inverseNAIndice);
	}

	public static int[][][] buildReferenceMismatchTree(List<GenotypeReference> references, Integer firstNA, int lastNA) {
		int seqLen = lastNA - firstNA + 1;
		int numRefs = references.size();
		// int[NAPosition][NA] rootNode = [...mismatchedRefs]
		int[][][] rootNode = new int[seqLen][4][numRefs + 1];
		int[][] rootNodePointer = new int[seqLen][4];
		for (int i = 0; i < seqLen; i ++) {
			// initialize rootNode; -1 indicates the end of matched refIdx
			for (int naIdx = 0; naIdx < 4; naIdx ++) {
				rootNode[i][naIdx][0] = -1;
			}
		}
		for (int refIdx = 0; refIdx < numRefs; refIdx ++) {
			GenotypeReference ref = references.get(refIdx);
			String sequence = ref.getSequence().toUpperCase();
			// build tree for each position
			for (int i = 0; i < seqLen; i ++) {
				char na = sequence.charAt(i);	
				// should search for mismatched refs but not matched
				int[] inverseNAIndice = getInverseNAIndice(na);
				for (int naIdx : inverseNAIndice) {
					int[] naNode = rootNode[i][naIdx];
					// add the newly found refIdx and shift end by 1
					naNode[rootNodePointer[i][naIdx] ++] = refIdx;
					naNode[rootNodePointer[i][naIdx]] = -1;
				}
			}
		}
		return rootNode;
	}
	
	public static void appendCodonDiscordance(
		int codonStartNAPos, String curCodon,
		Map<Integer, List<Integer>> discordanceListPerRef,
		Map<Integer, List<Integer>> curCodonDiscordancePerRef,
		Map<Integer, Set<String>> ignoredCodons) {
		Set<String> codons = ignoredCodons.get(codonStartNAPos);
		if (codons == null || !codons.contains(curCodon)) {
			// keep the result if the current codon is not a SDRM
			for (Map.Entry<Integer, List<Integer>> entry :
					curCodonDiscordancePerRef.entrySet()) {
				int refIdx = entry.getKey();
				if (!discordanceListPerRef.containsKey(refIdx)) {
					discordanceListPerRef.put(refIdx, new ArrayList<>());
				}
				List<Integer> discordanceList = discordanceListPerRef.get(refIdx);
				discordanceList.addAll(entry.getValue());
			}
		}
	}
	
	/** compare given sequence with a set of references (provided by mismatchTree)
	 *
	 * @param sequence a string of DNA sequence
	 * @param seqFirstNA starting position of the given sequence
	 * @param seqLastNA ending position of the given sequence
	 * @param mismatchTree a fast negative search tree build by buildReferenceMismatchTree()
	 * @param treeFirstNA starting position of the tree references
	 * @param treeLastNA ending position of the tree references
	 * @param codonNAOffset in case to yield discordance when a codon ends, the method
	 * 	needs to know which position in the system used by previous NA positions are the
	 *  bp0 of each codon.
	 * @param ignoredCodons certain codons at certain position can be ignored
	 * 
	 * @return A list of discordance for each given reference
	 * 
	 */
	public static Map<Integer, List<Integer>> compareWithSearchTree(
			String sequence, int seqFirstNA, int seqLastNA,
			int[][][] mismatchTree, int treeFirstNA, int treeLastNA,
			int codonNAOffset, Map<Integer, Set<String>> ignoredCodons) {
		int maxFirstNA = Math.max(seqFirstNA, treeFirstNA);
		int minLastNA = Math.min(seqLastNA, treeLastNA);
		int treeOffset = maxFirstNA - treeFirstNA;
		int seqOffset = maxFirstNA - seqFirstNA;
		int compareLength = minLastNA - maxFirstNA + 1;
		Map<Integer, List<Integer>> discordanceListPerRef = new HashMap<>();
		Map<Integer, List<Integer>> curCodonDiscordancePerRef = new HashMap<>();
		StringBuffer curCodon = new StringBuffer();
		for (int i = 0; i < compareLength; i ++) {
			if ((maxFirstNA + i - codonNAOffset) % 3 == 0) {
				// to check if the current position is the beginning of a codon
				appendCodonDiscordance(
					/* codonStartNAPos */ maxFirstNA + i - 3,
					curCodon.toString(),
					discordanceListPerRef, curCodonDiscordancePerRef,
					ignoredCodons
				);
				curCodon.setLength(0);
				curCodonDiscordancePerRef.clear();
			}
			int[][] treeNAs = mismatchTree[treeOffset + i];
			char seqNA = sequence.charAt(seqOffset + i);
			int[] naIndice = getNAIndice(seqNA);
			Map<Integer, Integer> mismatchRefs = new HashMap<>();
			for (int naIdx : naIndice) {
				for (int mismatchRef : treeNAs[naIdx]) {
					if (mismatchRef < 0) {
						// the end
						break;
					}
					int mismatchCount = mismatchRefs.getOrDefault(mismatchRef, 0) + 1;
					mismatchRefs.put(mismatchRef, mismatchCount);
				}
			}
			for (Map.Entry<Integer, Integer> e : mismatchRefs.entrySet()) {
				if (e.getValue() < naIndice.length) {
					// only get counted as discordance when no unambiguous NA was matched
					continue;
				}
				int mismatchRef = e.getKey();
				if (!curCodonDiscordancePerRef.containsKey(mismatchRef)) {
					curCodonDiscordancePerRef.put(mismatchRef, new ArrayList<>());
				}
				curCodonDiscordancePerRef.get(mismatchRef).add(maxFirstNA + i);
			}
			curCodon.append(seqNA);
		}
		appendCodonDiscordance(
			/* codonStartNAPos */ minLastNA - 3,
			curCodon.toString(),
			discordanceListPerRef, curCodonDiscordancePerRef,
			ignoredCodons
		);
		return discordanceListPerRef;
	}

	/** get BoundGenotype object of given sequence
	 *
	 * @param sequence a string of DNA sequence
	 * @param firstNA starting position of the given sequence
	 * @param lastNA ending position of the given sequence
	 * @param discordanceList discordance from the comparison
	 * @return BoundGenotype object contained the comparison result
	 */
	default public BoundGenotype getBoundGenotype(
			String sequence, int firstNA, int lastNA,
			List<Integer> discordanceList) {
		firstNA = Math.max(firstNA, getFirstNA());
		lastNA = Math.min(lastNA, getLastNA());
		return new BoundGenotype(
			this, sequence, firstNA, lastNA, discordanceList);
	}

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
	
	public String getCountry();

	public String getAuthorYear();

	public Integer getYear();

}
