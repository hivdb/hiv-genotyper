package edu.stanford.hivdb.genotyper;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AmbiguousNATranslator {

	private static final Map<Character, String> ambiguousNAs;

	static {
		Map<Character, String> _ambiguousNAs = new HashMap<>();
		_ambiguousNAs.put('A', "A");
		_ambiguousNAs.put('C', "C");
		_ambiguousNAs.put('G', "G");
		_ambiguousNAs.put('T', "T");
		_ambiguousNAs.put('R', "AG");
		_ambiguousNAs.put('Y', "CT");
		_ambiguousNAs.put('M', "AC");
		_ambiguousNAs.put('W', "AT");
		_ambiguousNAs.put('S', "CG");
		_ambiguousNAs.put('K', "GT");
		_ambiguousNAs.put('B', "CGT");
		_ambiguousNAs.put('D', "AGT");
		_ambiguousNAs.put('H', "ACT");
		_ambiguousNAs.put('V', "ACG");
		_ambiguousNAs.put('N', "ACGT");
		ambiguousNAs = Collections.unmodifiableMap(_ambiguousNAs);
	}

	public static String translate(char na) {
		return ambiguousNAs.get(na);
	}

}
