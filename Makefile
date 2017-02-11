all_resources:
	@cd scripts && python export_hiv_genotypes.py
	@cd scripts && python export_hiv_sdrms.py
	@cd scripts && python export_hiv_test_sequences.py

.PHONY: all_resources
