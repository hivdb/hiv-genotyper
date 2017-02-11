package edu.stanford.hivdb.genotyper;

import static org.junit.Assert.*;

import org.junit.Test;

public class HIVGenotypeTest {

	@Test
	public void testSingleton() {
		Genotype typeB = HIVGenotype.getInstance("B");
		assertEquals(typeB, HIVGenotype.getInstance("B"));
	}

	@Test
	public void testGetNames() {
		Genotype typeX01 = HIVGenotype.getInstance("X01");
		assertEquals("X01", typeX01.getIndexName());
		assertEquals("CRF01_AE", typeX01.getDisplayName());
	}

	@Test
	public void testGetCanonicalGenotype() {
		Genotype typeA = HIVGenotype.getInstance("A");
		Genotype typeX01 = HIVGenotype.getInstance("X01");
		assertEquals(typeA, typeX01.getParentGenotypes().get(0));
		Genotype typeA_FSU = HIVGenotype.getInstance("A_FSU");
		assertEquals(typeA, typeA_FSU.getParentGenotypes().get(0));
	}

	@Test
	public void testGetRegionalGenotype() {
		Genotype typeX51 = HIVGenotype.getInstance("X51");
		Genotype typeB = HIVGenotype.getInstance("B");
		assertEquals("B (100%)", typeX51.getPrimaryRegionalGenotype(2263, 4100).toString());
		assertEquals("B (97.2%)", typeX51.getPrimaryRegionalGenotype(2213, 4320).toString());
		assertEquals("CRF01_AE (92.9%)", typeX51.getPrimaryRegionalGenotype(4266, 5120).toString());
		assertEquals("B (95.4%)", typeX51.getPrimaryRegionalGenotype(2255, 4400).toString());
		Genotype typeX04 = HIVGenotype.getInstance("X04");
		assertEquals("CRF04_cpx (100%)", typeX04.getPrimaryRegionalGenotype(2263, 3600).toString());
		assertEquals("B (100%)", typeB.getPrimaryRegionalGenotype(2333, 4555).toString());
	}

}
