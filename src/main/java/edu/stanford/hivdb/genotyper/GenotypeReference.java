package edu.stanford.hivdb.genotyper;

public interface GenotypeReference {
	
	/** compare given sequence with current reference
	 * 
	 * @param sequence a string of DNA sequence
	 * @param firstNA HXB2 starting position of the given sequence
	 * @param lastNA HXB2 ending position of the given sequence
	 * @return BoundGenotype object contained the comparison result
	 */
	public BoundGenotype compare(String sequence, int firstNA, int lastNA);
	
	/** a getter to get current reference's genotype
	 * 
	 * @return a Genotype object that indicates the corresponding genotype
	 */
	public Genotype getGenotype();

	public String getAccession();
	
}
