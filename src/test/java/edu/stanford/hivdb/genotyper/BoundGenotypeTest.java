package edu.stanford.hivdb.genotyper;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import edu.stanford.hivdb.genotyper.HIVGenotypeRegressionTest.SequenceWithExpectedGenotype;

public class BoundGenotypeTest {

	@Test
	public void testGetDisplayGenotypes() {
		InputStream json = (
			HIVGenotypeRegressionTest.class.getClassLoader()
			.getResourceAsStream("testSequences/lots.json"));
		List<SequenceWithExpectedGenotype> lots = new Gson().fromJson(
				new BufferedReader(new InputStreamReader(json)),
			    new TypeToken<List<SequenceWithExpectedGenotype>>(){}.getType());
		for (SequenceWithExpectedGenotype eseq : lots) {
			TestSequence seq = eseq.testSequence;
			if (seq.accession.equals("AB356209")) {
				List<BoundGenotype> results = HIVGenotypeReference.compareAll(
					seq.sequence, seq.firstNA, seq.lastNA);
				BoundGenotype primary = results.get(0);
				assertEquals("B + F (3.88%)", primary.getDisplay());
			}
			else if (seq.accession.equals("DQ345008-DQ345037")) {
				List<BoundGenotype> results = HIVGenotypeReference.compareAll(
					seq.sequence, seq.firstNA, seq.lastNA);
				BoundGenotype primary = results.get(0);
				assertEquals("CRF01_AE (6.11%)", primary.getDisplay());
			}
		}
	}

}
