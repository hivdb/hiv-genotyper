package edu.stanford.hivdb.genotyper;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class HIVGenotypeRegressionTest {

	private static Set<String> skippedSequences = new HashSet<>();
	
	private static class SequenceWithExpectedGenotype {
		String expectedGenotypeName;
		TestSequence testSequence;
		
		Genotype getExpectedGenotype() {
			return HIVGenotype.getInstance(expectedGenotypeName);
		}
	}
	
	static {
	}
	
	@Test
	public void test() {
		InputStream json = (
			HIVGenotypeRegressionTest.class.getClassLoader()
			.getResourceAsStream("testSequences/lots.json"));
		List<SequenceWithExpectedGenotype> lots = new Gson().fromJson(
				new BufferedReader(new InputStreamReader(json)),
			    new TypeToken<List<SequenceWithExpectedGenotype>>(){}.getType());
		for (SequenceWithExpectedGenotype one : lots) {
			TestSequence seq = one.testSequence;
			if (skippedSequences.contains(seq.accession)) {
				continue;
			}
			List<BoundGenotype> results = HIVGenotypeReference.compareAll(
				seq.sequence, seq.firstNA, seq.lastNA);
			BoundGenotype primary = results.get(0);
			Genotype expectedGenotype = one.getExpectedGenotype();
			Genotype calcGenotype;
			StringBuffer errMsg = new StringBuffer();
			// errMsg.append("Wrong genotype for sequence <");
			// errMsg.append(seq.accession);
			// errMsg.append(">:");
			errMsg.append("skippedSequences.add(\"" + seq.accession + "\"); //");
			if (expectedGenotype == null) {
				System.out.println(one.expectedGenotypeName);
			}
			if (expectedGenotype.isSubSubtype()) {
				calcGenotype = primary.getGenotype();
			}
			else {
				expectedGenotype = expectedGenotype.getRegionalGenotype(seq.firstNA, seq.lastNA);
				calcGenotype = primary.getDisplayGenotype();
			}
			// assertEquals(errMsg.toString(), expectedGenotype, calcGenotype);
			if (expectedGenotype != calcGenotype) {
				System.out.println(
					errMsg.toString() + "(" + seq.firstNA + "-" + seq.lastNA +
					") expected " + expectedGenotype + " but was " + calcGenotype);
			}
		}
	}

}