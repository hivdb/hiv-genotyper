#! /usr/bin/env python
from __future__ import print_function

import json
from collections import OrderedDict

import pymysql.cursors

conn = pymysql.connect(host='localhost',
                       user='rshafer',
                       password='rshafer',
                       db='HIVDB_Results',
                       cursorclass=pymysql.cursors.DictCursor)

TARGET = '../src/test/resources/testSequences/lots.json'


def iter_query(sql, *args):
    with conn.cursor() as cursor:
        cursor.execute(sql, args)
        return cursor


def export_test_sequences():
    sql = "SELECT * FROM tblSubtypeTestSeqs WHERE Subtype != 'U' ORDER BY GB"
    results = []
    for row in iter_query(sql):
        result = OrderedDict([
            ('expectedGenotypeName', row['Subtype']),
            ('testSequence', OrderedDict([
                ('sequence', row['Sequence']),
                ('firstNA', row['FirstNA']),
                ('lastNA', row['FirstNA'] + len(row['Sequence']) - 1),
                ('accession', row['GB'])
            ]))
        ])
        results.append(result)

    with open(TARGET, 'w') as fp:
        json.dump(results, fp, indent=2)


if __name__ == '__main__':
    export_test_sequences()
