echo "Reading config file from command line parameter"
source $1
DIR=`pwd`
JENA_BIN=resources/jena-fuseki-0.2.6
DATA=$DATA_DIR/$CARD\_$RATIO


######
### PREPARE
######

if [ ! -d $JENA_BIN ]
then
    echo "unpacking jena"
    cd resources
    tar xzvf jena-fuseki-0.2.6.tar.gz
fi
cd $DIR

if [ ! -f dist/benchmark.jar ]
then
    echo "Building jar file"
    ant clean; ant dist
fi


function buildIDX(){
	if [ ! -d "$DATA/$1.idx" ]
	then
		echo "Building TDB indexes $DATA/$1.idx"	
		java -cp $JENA_BIN/fuseki-server.jar tdb.tdbloader --loc=$DATA/$1.idx $DATA/$1.nt
	fi
	
	echo "---------------------------"
	echo "Stopping all SPARQL servers"
	echo "---------------------------"
	pid=`pgrep -l -f 'fuseki' | grep port=$2 | awk '{print $1}'`
	kill -9 $pid

	echo "-----------------------"
	echo "starting SPARQL servers"
	echo "-----------------------"
	cd $JENA_BIN
	echo "sh fuseki-server --port=$2 --loc=$DATA/$1.idx /ds &"
	sh fuseki-server --port=$2 --loc=../../$DATA/$1.idx /ds &
	
}


echo "-------------"
echo "Creating data"
echo "-------------"
java -Xmx1G -jar dist/benchmark.jar GenerateData -d $DATA_DIR -c $CARD -r $RATIO

	
	
buildIDX data_left 3030
cd $DIR
#buildIDX data_subobj_right 3031
buildIDX data_subsub_right 3031
cd $DIR

echo "Running benchmark with $CARD and $RATIO"
for i in $JOINS
do
	
	echo "Evaluation for $i"
	#currently in-mem
	java -Xmx1G -jar dist/benchmark.jar Query -d $DATA_DIR -c $CARD -r $RATIO -j $i -o $OUTPUT_DIR
done