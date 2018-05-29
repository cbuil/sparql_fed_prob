import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.Map.Entry;

import utils.QueryParser;
import utils.SplitQuery;
import join.FILTER;
import join.NestedLoop;
import join.SERVICE;
import join.SymmetricalHashJoin;
import join.UNION;
import join.VALUES;
import bench.BenchmarkResult;

import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementService;


public class QueryParserTEST2 {
	public static void main(String[] args) throws FileNotFoundException {
	
		String [] queries = {
		"carlos_join1.ini",
//		"carlos_join2.ini",
		"carlos_join3.ini",
		"carlos_join4.ini",
		"carlos_join5.ini",
		"carlos_join6.txt",
		"carlos_join7.txt",
		"carlos_join8.txt"
		};
		String qFile = "benchmark_queries/query29.rq";
//		qFile = "resources/queries/bio/carlos_join2.ini";
		
		String localEP="http://localhost:3030/Affymetrix/query";
//		localEP="http://localhost:3030/mgi/sparql";
//		SplitQuery sq = QueryParser.splitQuery(qFile, localEP);
		
		SplitQuery sq1 = QueryParser.splitQuery(qFile, localEP);
		
//		System.exit(1);
		
		
		Map<String,Map<String, BenchmarkResult>> allresults = new HashMap<String, Map<String, BenchmarkResult>>(); 
		for(String q: queries){
			Map<String, BenchmarkResult> results = new HashMap<String, BenchmarkResult>();
			qFile = "resources/queries/bio/"+q;
			localEP="http://localhost:3030/mgi/sparql";
			SplitQuery sq = QueryParser.splitQuery(qFile, localEP);
			
			boolean debug = false;
			System.out.println("Running "+q);
			 NestedLoop l = new NestedLoop();
//			 BenchmarkResult res =  l.executeHTTP(sq,debug);
//			 results.put("NESTED", res);
			SERVICE ser = new SERVICE();
//			results.put("SERVICE", ser.executeHTTP(sq,debug));
			 
			 VALUES v = new VALUES();
			 results.put("VALUES", v.executeHTTP(sq,debug));
			 
			 UNION u = new UNION();
			 results.put("UNION", u.executeHTTP(sq,debug));
			 
			 FILTER f = new FILTER();
			 results.put("FILTER", f.executeHTTP(sq,debug));
			 
			 SymmetricalHashJoin s = new SymmetricalHashJoin();
			 results.put("HASH", s.executeHTTP(sq,debug));
			 allresults.put(q,results);
			 
			 
		}
		
		for(Entry<String, Map<String, BenchmarkResult>> results: allresults.entrySet()){
			System.out.println(results.getKey());
			for(Entry<String, BenchmarkResult>res: results.getValue().entrySet()){
				System.out.println(res.getKey()+","+res.getValue().getShortString(","));
			}
		}
		
		
		
//		String queryString = new Scanner(new File(qFile)).useDelimiter("\\Z").next();
//		Query q = QueryFactory.create(queryString);
//		
//		System.out.println("Query");
//		System.out.println("________");
//		System.out.println(q);
//		System.out.println("________");
//		
//		
//		
//		
//		Element p1,p2 = null; 
//		
//		ElementGroup qBody = (ElementGroup) q.getQueryPattern();;
////		System.out.println(qBody);
//		for(Element e: qBody.getElements()){
//			System.out.println("__");
//			
//			if( e instanceof ElementService){
//				ElementService es = (ElementService) e;
//				String epURI = es.getServiceURI();
//				if(epURI.equals(localEP)){
//					p1 = es.getElement();
//					System.out.println("P1:"+p1);
//				}else{
//					p2 = es.getElement();
//					System.out.println("P2:"+p2);
//				}
//			}
//		}
//		
//		
//		
//		 Query qp2 = QueryFactory.create();
//		 qp2.setQuerySelectType();
//		 qp2.setQueryResultStar(true);
//		 qp2.setDistinct(true);
//		 qp2.setQueryPattern(p2);
//		 
//		 System.out.println(qp2);
//		 ParameterizedSparqlString queryStr = new ParameterizedSparqlString(qp2.toString());
//		 queryStr.setIri("dbpedia_link", "http://ex.com");
//		 System.out.println(queryStr);
		 
//		 
//		 
		 
	}

	private static void printResults(BenchmarkResult res) {
		for(Binding b: res.getResults()){
			 System.out.println(b);
		 }
		
	}
}
