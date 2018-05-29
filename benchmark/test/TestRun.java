import java.io.File;
import java.io.FileNotFoundException;

import join.JoinOps;

import bench.DataGenerator;
import bench.QueryExecution;


public class TestRun {

	
	public static void main(String[] args) {
		File outDir = new File("resources/");
		
		int left_card = 10;
		int right_card_ratio = 2;
		boolean samePosJoin= true;
		
		//Generate data
		File dataDir = new File(outDir,+left_card+"_"+right_card_ratio);
		if(!dataDir.exists()){
			DataGenerator dg = new DataGenerator();
			try {
				dg.generate(left_card, right_card_ratio, dataDir);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		File outputDir = new File(outDir,"results");
		outputDir = new File(outputDir,+left_card+"_"+right_card_ratio);
		outputDir.mkdirs();
		QueryExecution qe = new QueryExecution();
		qe.executeOverInMemory(dataDir,outputDir, samePosJoin 
				,JoinOps.NESTED 
				,JoinOps.SYMHASH
				,JoinOps.VALUES
				,JoinOps.UNION
				,JoinOps.FILTER
				);
		
//		qe.executeOverHTTP(dataDir,outputDir, samePosJoin 
//				,JoinOps.NESTED 
//				,JoinOps.SYMHASH
//				,JoinOps.VALUES
//				,JoinOps.UNION
//				,JoinOps.FILTER
//				);
	}
}
