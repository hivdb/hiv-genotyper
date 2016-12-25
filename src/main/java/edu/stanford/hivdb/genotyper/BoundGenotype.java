package edu.stanford.hivdb.genotyper;

import java.util.List;

public interface BoundGenotype {
	
	/** get the sequence tested
	 * 
	 * @return String
	 */
	public String getSequence();

	/** get first compared NA position
	 * 
	 * @return the first compared NA position
	 */
	public int getFirstNA();
	
	/** get last compared NA position
	 * 
	 * @return the last compared NA position
	 */
	public int getLastNA();
	
	/** get original distance
	 * 
	 * @return distance (double, < 1.0)
	 */
	public Double getDistance();

	/** get distance in percent form
	 * 
	 * @return String
	 */
	public String getDistancePcnt();
	
	/** get reference
	 * 
	 * @return GenotypeReference object
	 */
	public GenotypeReference getReference();
	
	/** get genotype
	 * 
	 * @return Genotype object
	 */
	public Genotype getGenotype();
	
	/** get canonical / regional genotype
	 * 
	 * @return Genotype object
	 */
	public Genotype getDisplayGenotype();
	
	/** get discordance positions 
	 * 
	 * @return an array of position numbers
	 */
	public List<Integer> getDiscordanceList();

	/** get display string
	 * 
	 * The display result normally contains the name of
	 * current genotype and the distance percent between
	 * the following parentheses. For example: "B (1.4%)".
	 * 
	 * There are two exceptions:
	 *
	 * 1. The genotype is a recombination of other types.
	 *    In this case, the given sequence can be
	 *    conditionally considered as one of the
	 *    recombinant genotypes. For example, the genotype
	 *    is CRF51_01B, the display string can be "B (2.5%)"
	 *    if the given sequence only contained PR and RT.
	 *
	 * 2. The genotype is a sub-subtype of another subtype.
	 *    In this case the subtype will be showed first.
	 *    The sub-subtype will be between the following
	 *    parentheses. For example, genotype A-FSU will be
	 *    always displayed like "A (A_FSU) (3.1%)".
	 *
	 * @return a human-friendly string
	 */
	public String getDisplay();
}
