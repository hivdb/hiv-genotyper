package edu.stanford.hivdb.genotyper;

import static org.junit.Assert.*;

import org.junit.Test;

public class HIVGenotypeReferenceTest {

	@Test
	public void testCompareAll() {
		TestSequence seqX51 = TestSequence.loadResource("X51_full.json");
		HIVGenotypeResult result = HIVGenotypeReference.compareAll(
			seqX51.sequence, seqX51.firstNA, seqX51.lastNA);
		BoundGenotype primary = result.getFirstMatch();
		assertEquals("X51", primary.getGenotype().getIndexName());
		assertEquals("JN029801", primary.getReference().getAccession());
		assertEquals(66.0 / 2841, primary.getDistance(), 1e-10);
		assertEquals("B", result.getFallbackMatch().getGenotype().getIndexName());

		// test boundary cases
		StringBuffer buf = new StringBuffer(seqX51.sequence);
		buf.setCharAt(0, 'A');
		buf.setCharAt(2840, 'C');
		result = HIVGenotypeReference.compareAll(
			buf.toString(), seqX51.firstNA, seqX51.lastNA);
		assertEquals(68.0 / 2841,result.getFirstMatch().getDistance(), 1e-10);
	}

	@Test
	public void testGetDisplayGenotype() {
		TestSequence seqX51 = TestSequence.loadResource("X51_no_integrase.json");
		HIVGenotypeResult result = HIVGenotypeReference.compareAll(
			seqX51.sequence, seqX51.firstNA, seqX51.lastNA);
		BoundGenotype primary = result.getFirstMatch();
		assertEquals("X51", primary.getGenotype().getIndexName());
		assertEquals("B", primary.getDisplayWithoutDistance());

		seqX51 = TestSequence.loadResource("X51_full.json");
		result = HIVGenotypeReference.compareAll(
			seqX51.sequence, seqX51.firstNA, seqX51.lastNA);
		primary = result.getFirstMatch();
		assertEquals("X51", primary.getGenotype().getIndexName());
		assertEquals("CRF51_01B", primary.getDisplayWithoutDistance());
	}

	@Test
	public void testRegionOfUnknown() {
		TestSequence seqX10 = TestSequence.loadResource("X10_U_region.json");
		HIVGenotypeResult result = HIVGenotypeReference.compareAll(
			seqX10.sequence, seqX10.firstNA, seqX10.lastNA);
		BoundGenotype best = result.getBestMatch();
		assertEquals("Unknown", best.getDisplayWithoutDistance());
		assertEquals("Unknown", best.getDisplay());
	}

	@Test
	public void testSDRMs() {
		TestSequence seqB = TestSequence.loadResource("B_SDRMs.json");
		HIVGenotypeResult result = HIVGenotypeReference.compareAll(
			seqB.sequence, seqB.firstNA, seqB.lastNA);
		BoundGenotype primary = result.getFirstMatch();
		assertEquals("B", primary.getGenotype().getIndexName());
		assertEquals("U63632", primary.getReference().getAccession());
		/* removed 7 ambiguities and 4 SDRMs:
		 *   RT67N:GAC=>AAC; RT70R:AAA=>AGA;
		 *   RT184V:ATG=>GTG; RT219Q:AAA=>CAA
		 */
		assertEquals((60.0 - 11) / 1174, primary.getDistance(), 1e-10);
	}
}
