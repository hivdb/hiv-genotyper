package edu.stanford.hivdb.genotyper;

public interface GenotypeDistance {

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
	
	/** get distance
	 * 
	 * @return distance (double, < 1.0)
	 */
	public Double get();
	
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
	
	/** get discordance positions 
	 * 
	 * @return an array of position numbers
	 */
	public Integer[] getDiscordanceList();
	
}
