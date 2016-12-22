package edu.stanford.hivdb.genotyper;

public interface Genotype {

	/** get genotype based given boundaries of partial sequence
	 * 
	 * A CRF subtype was consisted of other subtypes and divided
	 * into regions by certain breakpoints. Therefore, a partial
	 * sequence can be conditionally considered as one of the
	 * recombinant subtypes, if all of its nucleotides were
	 * presented in a same region.
	 * 
	 * @param firstNA the start position of the partial sequence
	 * @param lastNA the end position
	 * @return Genotype object; returns itself when no better
	 * matches were found
	 */
	public Genotype getRegionalGenotype(int firstNA, int lastNA); 
}
