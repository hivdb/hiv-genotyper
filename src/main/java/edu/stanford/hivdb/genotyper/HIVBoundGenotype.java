package edu.stanford.hivdb.genotyper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class HIVBoundGenotype implements BoundGenotype {
	
	private String sequence;
	private HIVGenotypeReference reference;
	private int firstNA;
	private int lastNA;
	private List<Integer> discordanceList;
	private double distance;

	protected HIVBoundGenotype(
		HIVGenotypeReference reference,
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
	
	@Override
	public String getSequence() {
		return sequence;
	}
	
	@Override
	public int getFirstNA() {
		return firstNA;
	}

	@Override
	public int getLastNA() {
		return lastNA;
	}

	@Override
	public Double getDistance() {
		return distance;
	}
	
	@Override
	public String getDistancePcnt() {
		BigDecimal pcnt = new BigDecimal(distance * 100);
		pcnt = pcnt.setScale(2, RoundingMode.HALF_UP);
		return pcnt.toPlainString() + "%";
	}

	@Override
	public GenotypeReference getReference() {
		return reference;
	}

	@Override
	public Genotype getGenotype() {
		return reference.getGenotype();
	}

	@Override
	public List<Integer> getDiscordanceList() {
		// TODO Auto-generated method stub
		return discordanceList;
	}
	
	@Override
	public Genotype getDisplayGenotype() {
		Genotype genotype = getGenotype();
		genotype = genotype.getRegionalGenotype(firstNA, lastNA);
		return genotype.getCanonicalGenotype();
	}
	
	@Override
	public Genotype getSupplementGenotype() {
		Genotype genotype = getGenotype();
		if (genotype != genotype.getCanonicalGenotype()) {
			return genotype;
		}
		return null;
	}

	@Override
	public String getDisplay() {
		StringBuffer buf = new StringBuffer();
		buf.append(getDisplayGenotype().getDisplayName());
		Genotype secondary = getSupplementGenotype();
		if (secondary != null) {
			buf.append(" (");
			buf.append(secondary.getDisplayName());
			buf.append(")");
		}
		buf.append(" (");
		buf.append(getDistancePcnt());
		buf.append(")");
		return buf.toString();
	}
	
	@Override
	public String toString() {
		return getDisplay();
	}

}
