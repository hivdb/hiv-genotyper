package edu.stanford.hivdb.genotyper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public interface Genotype {

	public static class RegionalGenotype {
		private Double proportion;
		private Genotype genotype;

		public RegionalGenotype(
				Genotype genotype, double proportion) {
			this.proportion = proportion;
			this.genotype = genotype;
		}

		public Genotype getGenotype() {
			return genotype;
		}

		public Double getProportion() {
			return proportion;
		}

		@Override
		public String toString() {
			int scale = 2;
			if (proportion + 1e-8 > 1.0) {
				scale = 0;
			}
			else if (proportion > 0.1) {
				scale = 1;
			}
			BigDecimal pcnt = new BigDecimal(proportion * 100);
			pcnt = pcnt.setScale(scale, RoundingMode.HALF_UP);
			return genotype + " (" + pcnt.toPlainString() + "%)";
		}

	}

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
	default public RegionalGenotype getPrimaryRegionalGenotype(int firstNA, int lastNA) {
		List<RegionalGenotype> results = getRegionalGenotypes(firstNA, lastNA);
		RegionalGenotype primary = results
			.stream()
			.sorted((r1, r2) -> r2.proportion.compareTo(r1.proportion))
			.findFirst().get();
		if (primary.proportion >= getMinPrimaryRegionalGenotypeProportion()) {
			return primary;
		}
		return new RegionalGenotype(this, 1.0);
	}

	public List<RegionalGenotype> getRegionalGenotypes(int firstNA, int LastNA);

	public Double getMinPrimaryRegionalGenotypeProportion();

	/** check distance
	 *
	 * Accepts a double value of the distance between testing
	 * sequence and the reference. Returns false when distance
	 * is too large for this genotype.
	 *
	 * @param distance
	 * @return Boolean
	 */
	public Boolean checkDistance(double distance);

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
	 * @return List of Genotype object
	 */
	public List<Genotype> getCanonicalGenotypes();

	/** get if the current genotype is treated as a recombination form
	 *
	 * @return boolean
	 */
	public Boolean isRecombination();

}
