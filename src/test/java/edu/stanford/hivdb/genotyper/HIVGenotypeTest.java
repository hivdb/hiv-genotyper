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
		Genotype typeX01 = HIVGenotype.getInstance("X01");
		assertEquals(typeX01, typeX01.getCanonicalGenotype());
		Genotype typeA_FSU = HIVGenotype.getInstance("A_FSU");
		Genotype typeA = HIVGenotype.getInstance("A");
		assertEquals(typeA, typeA_FSU.getCanonicalGenotype());
	}
	
	@Test
	public void testGetRegionalGenotype() {
		Genotype typeX51 = HIVGenotype.getInstance("X51");
		Genotype typeB = HIVGenotype.getInstance("B");
		Genotype typeX01 = HIVGenotype.getInstance("X01");
		assertEquals(typeB, typeX51.getRegionalGenotype(2263, 4100));
		assertEquals(typeB, typeX51.getRegionalGenotype(2213, 4320));
		assertEquals(typeX01, typeX51.getRegionalGenotype(4266, 5120));
		assertEquals(typeX51, typeX51.getRegionalGenotype(2255, 4400));
		Genotype typeX04 = HIVGenotype.getInstance("X04");
		assertEquals(typeX04, typeX04.getRegionalGenotype(2263, 3600));
		assertEquals(typeB, typeB.getRegionalGenotype(2333, 4555));
	}

}
