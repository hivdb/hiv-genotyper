package edu.stanford.hivdb.genotyper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import edu.stanford.hivdb.genotyper.Genotype.RegionalGenotype;

public class BoundGenotype {

	private static final Double MAX_FALLBACK_TO_SECONDARY_DISTANCE_DIFF = 0.01; // 1%
	private String sequence;
	private GenotypeReference reference;
	private int firstNA;
	private int lastNA;
	private List<Integer> discordanceList;
	private double distance;

	protected BoundGenotype(
		GenotypeReference reference,
		String sequence, int firstNA, int lastNA,
		List<Integer> discordanceList
	) {
		this.reference = reference;
		this.sequence = sequence;
		this.firstNA = firstNA;
		this.lastNA = lastNA;
		this.discordanceList = discordanceList;
		this.distance = (
				(double) discordanceList.size() /
				(lastNA - firstNA + 1));
	}

	/** get the sequence tested
	 *
	 * @return String
	 */
	public String getSequence() {
		return sequence;
	};

	/** get first compared NA position
	 *
	 * @return the first compared NA position
	 */
	public int getFirstNA() {
		return firstNA;
	}

	/** get last compared NA position
	 *
	 * @return the last compared NA position
	 */
	public int getLastNA() {
		return lastNA;
	}

	/** get original distance
	 *
	 * @return distance (double, < 1.0)
	 */
	public Double getDistance() {
		return distance;
	}

	/** get reference
	 *
	 * @return GenotypeReference object
	 */
	public GenotypeReference getReference() {
		return reference;
	}

	public String getReferenceAccession() {
		return reference.getAccession();
	}

	/** get genotype
	 *
	 * @return Genotype object
	 */
	public Genotype getGenotype() {
		return reference.getGenotype();
	}

	/** get discordance positions
	 *
	 * @return an array of position numbers
	 */
	public List<Integer> getDiscordanceList() {
		return discordanceList;
	}

	/** get distance in percent form
	 *
	 * @return String
	 */
	public String getDistancePcnt() {
		int scale = 2; // 0.00% ~ 9.99%
		if (distance + 1e-8 > 1.0) {
			// 100%
			scale = 0;
		}
		else if (distance > 0.1) {
			// > 10.0%
			scale = 1;
		}
		BigDecimal pcnt = new BigDecimal(distance * 100);
		pcnt = pcnt.setScale(scale, RoundingMode.HALF_UP);
		return pcnt.toPlainString() + "%";
	}

	/** get canonical / regional genotype
	 *
	 * @return List of Genotype object
	 */
	public List<Genotype> getDisplayGenotypes() {
		Genotype origGenotype = getGenotype();
		Genotype regionalGenotype = getPrimaryRegionalGenotype().getGenotype();
		if (!checkDistance() && regionalGenotype == origGenotype) {
			// distance is too far and no regional Genotype found
			return origGenotype.getCanonicalGenotypes();
		}
		List<Genotype> displayGenotypes = new ArrayList<>();
		displayGenotypes.add(regionalGenotype);
		return displayGenotypes;
	}

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
	public String getDisplay() {
		StringBuffer buf = new StringBuffer();
		buf.append(getDisplayWithoutDistance());
		buf.append(" (");
		buf.append(getDistancePcnt());
		buf.append(")");
		return buf.toString();
	}

	public String getDisplayWithoutDistance() {
		StringBuffer buf = new StringBuffer();
		for (Genotype genotype : getDisplayGenotypes()) {
			buf.append(genotype.getDisplayName());
			buf.append(" + ");
		}
		buf.setLength(buf.length() - 3);
		return buf.toString();
	}

	/** get genotype based given boundaries of partial sequence
	 *
	 * A recombination (CRFs) of other genotypes can be divided
	 * into regions by certain breakpoints. Therefore, a partial
	 * sequence can be conditionally considered as one of the
	 * recombinant genotypes, if all of its nucleotides were
	 * presented in such a single region.
	 *
	 * @return Genotype object; returns itself when no better
	 * matches were found
	 */
	public RegionalGenotype getPrimaryRegionalGenotype() {
		return getGenotype()
			.getPrimaryRegionalGenotype(getFirstNA(), getLastNA());
	}

	public List<RegionalGenotype> getRegionalGenotypes() {
		return getGenotype()
			.getRegionalGenotypes(getFirstNA(), getLastNA());
	}

	/** check distance
	 *
	 * Check the distance between current testing sequence
	 * and the reference. Returns false when distance is too
	 * large for this genotype.
	 *
	 * @return Boolean
	 */
	public Boolean checkDistance() {
		return getGenotype().checkDistance(getDistance());
	}

	public List<Genotype> getCanonicalGenotypes() {
		return getGenotype().getCanonicalGenotypes();
	}

	public Boolean shouldFallbackTo(BoundGenotype fallback) {
		if (checkDistance() ||
				fallback.getDistance() - getDistance() >
				MAX_FALLBACK_TO_SECONDARY_DISTANCE_DIFF) {

			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return getDisplay();
	}

}
