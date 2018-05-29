#!/bin/sh
#ant clean 
#ant dist
BASE=`pwd`
JAR=$BASE/dist/benchmark.jar
echo $JAR
QDIR=$BASE/resources/queries/theorem






# iterate over the queries
QDIR=$QDIR/sesame
LDIR=$QDIR/logs
mkdir $LDIR
EP=http://localhost:8080/openrdf-sesame/repositories/data1
cd $QDIR
for i in `ls -1 *rq`
do
    for join in FILTER UNION VALUES SERVICE NESTED SYMHASH
    do
        echo "java -jar $JAR Run -e $EP -q $QDIR/$i -j $join -o $QDIR/results 2>>$QDIR/results.tsv 1> $LDIR/$i-$join.log"
         java -jar $JAR Run -l -e $EP -q $QDIR/$i -j $join -o $QDIR/results 2>>$QDIR/results.tsv 1> $LDIR/$i-$join.log
    done
done
QDIR=$BASE/resources/queries/theorem/fuseki
LDIR=$QDIR/logs
mkdir $LDIR
cd $QDIR
EP=http://localhost:3030/data1/sparql
for i in `ls -1 *rq`
do
    for join in FILTER UNION VALUES SERVICE NESTED SYMHASH
    do
        echo "java -jar $JAR Run -e $EP -q $QDIR/$i -j $join -o $QDIR/results 2>>$QDIR/results.tsv 1> $LDIR/$i-$join.log"
         java -jar $JAR Run -l -e $EP -q $QDIR/$i -j $join -o $QDIR/results 2>>$QDIR/results.tsv 1> $LDIR/$i-$join.log
    done
done
