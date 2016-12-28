#! /usr/bin/env python
from __future__ import print_function

import json
from collections import OrderedDict

import pymysql.cursors
from Bio.Data.CodonTable import standard_dna_table

conn = pymysql.connect(host='localhost',
                       user='rshafer',
                       password='rshafer',
                       db='HIVDB_Scores',
                       cursorclass=pymysql.cursors.DictCursor)

TARGET = '../src/main/resources/HIVSDRMs.json'


def iter_query(sql, *args):
    with conn.cursor() as cursor:
        cursor.execute(sql, args)
        return cursor


def export_sdrms():
    sql = "SELECT * FROM tblSDRMs"
    results = OrderedDict()
    backward_table = {}
    for codon, aa in standard_dna_table.forward_table.items():
        backward_table.setdefault(aa, []).append(codon)
    for row in iter_query(sql):
        for aa in row['AAs']:
            for codon in backward_table.get(aa, []):
                pos = 2250 + {
                    'PR': 0,
                    'RT': 297,
                    'IN': 1977
                }[row['Gene']] + row['Pos'] * 3
                results.setdefault(pos, []).append(codon)

    with open(TARGET, 'w') as fp:
        json.dump(results, fp, indent=2)


if __name__ == '__main__':
    export_sdrms()
