package edu.stanford.hivdb.genotyper;

public interface GenotypeReference {
	
	/** calculate the distance between current reference and given sequence
	 * 
	 * @param sequence a string of DNA sequence
	 * @return distance (double, < 1.0) between the reference and given sequence
	 */
	public Double calculateDistance(String sequence);
	
	/** a getter to get current reference's genotype
	 * 
	 * @return a Genotype object that indicates the corresponding genotype
	 */
	public Genotype getGenotype();
	
}
