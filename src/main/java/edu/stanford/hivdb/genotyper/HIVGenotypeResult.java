package edu.stanford.hivdb.genotyper;

import java.util.Collections;
import java.util.List;

public class HIVGenotypeResult {

	private List<BoundGenotype> genotypes;

	protected HIVGenotypeResult(List<BoundGenotype> genotypes) {
		genotypes.sort((g1, g2) -> g1.getDistance().compareTo(g2.getDistance()));
		this.genotypes = genotypes;
	}

	public BoundGenotype getFirstMatch() {
		return genotypes.get(0);
	}

	public BoundGenotype getFallbackMatch() {
		return genotypes.stream()
			.filter(bg -> !bg.getGenotype().hasParentGenotypes())
			.findFirst().get();
	}

	public List<BoundGenotype> getAllMatches() {
		return Collections.unmodifiableList(genotypes);
	}

	public BoundGenotype getBestMatch() {
		BoundGenotype first = getFirstMatch();
		BoundGenotype fallback = getFallbackMatch();

		return first.shouldFallbackTo(fallback) ? fallback : first;
	}

}
