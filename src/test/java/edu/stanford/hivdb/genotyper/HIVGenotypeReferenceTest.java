package edu.stanford.hivdb.genotyper;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

public class HIVGenotypeReferenceTest {

	@Test
	public void testCompareAll() {
		TestSequence seqX51 = TestSequence.loadResource("X51_full.json");
		List<BoundGenotype> results = HIVGenotypeReference.compareAll(
			seqX51.sequence, seqX51.firstNA, seqX51.lastNA);
		BoundGenotype primary = results.get(0);
		assertEquals("X51", primary.getGenotype().getIndexName());
		assertEquals("JN029801", primary.getReference().getAccession());
		assertEquals(0.02358, primary.getDistance(), 1e-5);
		assertEquals("B", results.get(1).getGenotype().getIndexName());
	}

}
