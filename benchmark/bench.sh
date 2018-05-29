#!/bin/sh
#ant clean
#ant dist

SUF=$1
# join=$2
BASE=`pwd`
JAR=$BASE/benchmark_carlos.jar
# JAR=$BASE/benchmark_carlos_timeout.jar
QDIR=$BASE/resources/queries
BIO=$QDIR/bio

LOG=$BIO/results_$SUF.tsv
LDIR=$BIO/logs_$SUF
mkdir $LDIR
files=(carlos_join8.rq carlos_join9.rq)
RUNS=3
# run="timeout 5s java -jar $JAR Run -e http://localhost:8080/rdf4j-server/repositories/mgi -q $BIO/$i -j $join -o bench.bio.results -b $batch"
# iterate over the queries
cd $BIO
# for i in `ls -1 *rq`
for i in "${files[@]}"
do
    for join in VALUES FILTER UNION
    do
        for batch in -1 250 500 750
        do
            for c in $(seq 1 $RUNS)
            do
                echo 'start: '$(date)
                echo "java -jar $JAR Run -e http://localhost:8080/rdf4j-server/repositories/mgi -q $BIO/$i -j $join -o bench.results -b $batch | [ $? -eq 124 ] && (>&2 echo $i'\t'$join'\tTIMED OUT\t'$batch)  2>$LOG 1> $LDIR/$i-$join-$batch-$c.log"
                gtimeout 10m java -jar $JAR Run -e http://localhost:8080/rdf4j-server/repositories/mgi -q $BIO/$i -j $join -o bench.bio.results -b $batch  2>>$LOG 1> $LDIR/$i-$join-$batch-$c.log
                RET="$?"
                echo $RET
                if [ "$RET" = "124" ]; then (echo $i'\t'$join'\tTIMED OUT\t'$batch >>$LOG 2>&1)
                fi
                echo 'end: '$(date)
            done
        done
    done

    for join in SYMHASHP
    do
        for batch in -1 250 500 750
        do
            for c in $(seq 1 $RUNS)
            do
                echo 'start: '$(date)
                echo "java -jar $JAR Run -e http://localhost:8080/rdf4j-server/repositories/mgi -q $BIO/$i -j $join -o bench.results -b $batch | [ $? -eq 124 ] && (>&2 echo $i'\t'$join'\tTIMED OUT\t'$batch)  2>$LOG 1> $LDIR/$i-$join-$batch-$c.log"
                gtimeout 10m java -jar $JAR Run -e http://localhost:8080/rdf4j-server/repositories/mgi -q $BIO/$i -j $join -o bench.bio.results -b $batch  2>>$LOG 1> $LDIR/$i-$join-$batch-$c.log
                RET="$?"
                echo $RET
                if [ "$RET" = "124" ]; then (echo $i'\t'$join'\tTIMED OUT\t'$batch >>$LOG 2>&1)
                fi
                echo 'end: '$(date)
            done
        done
    done

    for join in  SERVICE SYMHASH GCSYMHASH NESTED
    do
        for c in $(seq 1 $RUNS)
            do
                echo 'start: '$(date)
                echo "java -jar $JAR Run -e http://localhost:8080/rdf4j-server/repositories/mgi -q $BIO/$i -j $join -o bench.results -b $batch | [ $? -eq 124 ] && (>&2 echo $i'\t'$join'\tTIMED OUT\t'$batch)  2>$LOG 1> $LDIR/$i-$join-$batch-$c.log"
                gtimeout 10m java -jar $JAR Run -e http://localhost:8080/rdf4j-server/repositories/mgi -q $BIO/$i -j $join -o bench.bio.results -b $batch  2>>$LOG 1> $LDIR/$i-$join-$batch-$c.log
                RET="$?"
                echo $RET
                if [ "$RET" = "124" ]; then (echo $i'\t'$join'\tTIMED OUT\t'$batch >>$LOG 2>&1)
                fi
                echo 'end: '$(date)
            done
    done
done