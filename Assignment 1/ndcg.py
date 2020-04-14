import csv
import math

def csvToDict(path):
    hashMap = {}
    with open(path) as df:
        csv_reader = csv.reader(df, delimiter = ';')
        line_count = 0
        for row in csv_reader:
            hashMap[row[0]] = {'score':float(row[1].replace(',','.')),'relevance': None}
            line_count += 1
        print(f'Processed {line_count} lines.')
    return hashMap


def txtToDict(path):
    hashMap = {}
    with open(path) as df:
        data = df.readlines()
        for line in data:
            line = line.split(" ")
            key = line[0]
            value = line[1]
            value = value.replace('\n','')
            hashMap[key] = value
    return hashMap

searchResult = csvToDict('dcgAfterRelevance_Euc.csv')
avg = txtToDict('average_relevance.txt')
keys = searchResult.keys()
notInAvg = []
for key in keys:
    if key in avg:
        searchResult[key]['relevance'] = avg[key]
        #print(searchResult[key])
    else:
        searchResult[key]['relevance'] = 0
        print(key)
        notInAvg.append(key)
DCG = []
CG = 0
sum_DCG = 0.0
sortedByRel = []
for pos,key in enumerate(searchResult.keys(), start= 1):
    if key == 'Mathematics.f':
        print(key)
        continue
    relevance = float(searchResult[key]['relevance'])
    sortedByRel.append(relevance)
    DCG_value = relevance/math.log2(pos+1)
    CG += relevance
    DCG.append(DCG_value+sum_DCG)
    sum_DCG += DCG_value

sortedByRel.sort(reverse=True)
IDCG = []
sum_IDCG = 0
for pos,rel in enumerate(sortedByRel, start= 1):
    if key == 'Mathematics.f':
        print(key)
        continue
    value = rel/math.log2(pos+1)
    IDCG.append(value+sum_IDCG)
    sum_IDCG+= value

NDCG = []
for index in range(len(DCG)):
    NDCG.append(DCG[index]/IDCG[index])
    print(NDCG[index])

