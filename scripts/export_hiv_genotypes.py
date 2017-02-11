#! /usr/bin/env python
from __future__ import print_function

import json
from collections import OrderedDict

import pymysql.cursors

conn = pymysql.connect(host='localhost',
                       user='rshafer',
                       password='rshafer',
                       db='HIVDB_Scores',
                       cursorclass=pymysql.cursors.DictCursor)

TARGET = '../src/main/resources/HIVGenotypes.json'
TARGET_REFS = '../src/main/resources/HIVGenotypeReferences.json'


def iter_query(sql, *args):
    with conn.cursor() as cursor:
        cursor.execute(sql, args)
        return cursor


def export_references():
    sql = ("SELECT * FROM tblRefSeqs WHERE Include = 'Y' "
           "AND Subtype <> 'U' "
           'ORDER BY Subtype, AuthorYr, GBNum')
    results = []
    for row in iter_query(sql):
        result = OrderedDict([
            ('genotypeName', row['Subtype']),
            ('country', row['Country']),
            ('authorYear', row['AuthorYr']),
            ('year', row['Year']),
            ('accession', row['GBNum']),
            ('firstNA', row['FirstNA']),
            ('lastNA', row['LastNA']),
            ('sequence', row['Sequence'])
        ])
        results.append(result)

    with open(TARGET_REFS, 'w') as fp:
        json.dump(results, fp, indent=2)


def export_genotypes():
    sql = 'SELECT * FROM tblCRFSubtypes ORDER BY Subtype'
    genotypes = list(iter_query(sql))

    sql = 'SELECT * FROM tblCRFBreakpoints ORDER BY CRF'
    breakpoints = {}
    for row in iter_query(sql):
        crf = breakpoints.setdefault(row['CRF'], [])
        crf.append(row)

    results = OrderedDict()
    for row in genotypes:
        name = row['Subtype']
        level = 'SUBTYPE'
        if name in 'NOP':
            level = 'GROUP'
        elif name == 'HIV2':
            level = 'SPECIES'
        elif name.startswith('X'):
            level = 'CRF'
        elif row['SubSubtype'] == 'T':
            level = 'SUBSUBTYPE'
        is_simple_crf = row['SimpleCRF'] == 'T'
        regions = (
            [OrderedDict([
                ('genotypeName', r['Subtype']),
                ('start', r['Start']),
                ('end', r['End'])
            ]) for r in breakpoints[name]]
            if is_simple_crf and name in breakpoints
            else None)
        parent = row['Parent']
        result = OrderedDict([
            ('name', name),
            ('displayName', row['Synonym']),
            ('parentGenotypes', None if name == parent else parent),
            ('isSimpleCRF', is_simple_crf),
            ('classificationLevel', level),
            ('distanceUpperLimit', float(row['DistanceCutoff']) / 100),
            ('regions', regions)])
        results[name] = result

    # add "unknown" genotype
    results['U'] = OrderedDict([
        ('name', 'U'),
        ('displayName', 'Unknown'),
        ('parentGenotypes', None),
        ('isSimpleCRF', False),
        ('classificationLevel', None),
        ('distanceUpperLimit', .0),
        ('regions', None)
    ])

    with open(TARGET, 'w') as fp:
        json.dump(results, fp, indent=2)


if __name__ == '__main__':
    export_genotypes()
    export_references()
