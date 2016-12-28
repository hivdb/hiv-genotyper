package edu.stanford.hivdb.genotyper;

public interface Genotype {

	/** get genotype based given boundaries of partial sequence
	 * 
	 * A recombination (CRFs) of other genotypes can be divided
	 * into regions by certain breakpoints. Therefore, a partial
	 * sequence can be conditionally considered as one of the
	 * recombinant genotypes, if all of its nucleotides were
	 * presented in such a single region.
	 * 
	 * @param firstNA the start position of the partial sequence
	 * @param lastNA the end position
	 * @return Genotype object; returns itself when no better
	 * matches were found
	 */
	public Genotype getRegionalGenotype(int firstNA, int lastNA); 
	
	/** get genotype index name
	 * 
	 * @return string
	 */
	public String getIndexName();
	
	/** get genotype display name
	 *
	 * @return string
	 */
	public String getDisplayName();
	
	/** get canonical genotype
	 * 
	 * Sometime the current genotype is an alias or sub-subtype
	 * of another genotype. This function returns the canonical
	 * genotype if it exists. Else it should return the current
	 * genotype.
	 * 
	 * @return Genotype object
	 */
	public Genotype getCanonicalGenotype();
	
	/** check distance
	 * 
	 * Accepts a double value of the distance between testing
	 * sequence and the reference. Returns false when distance
	 * is too large for this genotype.
	 * 
	 * @return Boolean
	 */
	public Boolean checkDistance(double distance);
	
	public Boolean isSubSubtype();
}
