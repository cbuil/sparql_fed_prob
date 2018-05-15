package join;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.MyBindingComparator;
import utils.QueryParser;
import utils.SplitQuery;
import bench.BenchmarkResult;

import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.impl.LiteralImpl;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.core.ResultBinding;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.apache.jena.sparql.syntax.ElementWalker;

public class NestedLoop implements JoinOperator{
	private final static Logger log = LoggerFactory.getLogger(NestedLoop.class);

	
	public BenchmarkResult executeHTTP(SplitQuery sq) {
		return executeHTTP(sq,false);
	}

	@Override
	public BenchmarkResult executeHTTP(SplitQuery sq, boolean debug) {
		BenchmarkResult res = new BenchmarkResult();
		res.setBatchSize(sq.getBatch());
		Set<Binding> results = new TreeSet<Binding>(new MyBindingComparator());
		long start = System.currentTimeMillis();
		long end = 0;
		try{
			Query p1 = QueryFactory.create();
			p1.setQueryPattern(sq.getP1());
			p1.setQuerySelectType();
			p1.setQueryResultStar(true);
//			p1.setDistinct(true);

			List<Var> p1Vars = new ArrayList<Var>();
			System.out.println(sq.getP1());
			ElementWalker.walk( sq.getP1(), new GetVars(p1Vars));

			Query p2 = QueryFactory.create();
			p2.setQueryPattern(sq.getP2());
			p2.setQuerySelectType();
			p2.setQueryResultStar(true);
//			p2.setDistinct(true);

			List<Var> p2Vars = new ArrayList<Var>();
			ElementWalker.walk( sq.getP2(), new GetVars(p2Vars));
			String joinVar = QueryParser.findJoinVar(p1Vars, p2Vars);

			//init execution of left triple pattern
			QueryExecution l_qexec =(QueryEngineHTTP) QueryExecutionFactory.sparqlService(sq.getP1Endpoint(), p1);
			res.epCall(0);
			ResultSet l_results = l_qexec.execSelect();
			int c=0;
			int c2 =0;
			while (l_results.hasNext()) {
				c++;
				final QuerySolution l_soln = l_results.nextSolution();
				//.replace("?join", "<"+l_soln.get("join")+">")
				//		    System.out.println(l_soln.get(joinVar).getClass());
				ParameterizedSparqlString p2Str = new ParameterizedSparqlString(p2.toString());
				if(l_soln.get(joinVar) instanceof LiteralImpl)
					p2Str.setLiteral(joinVar, l_soln.get(joinVar).asNode().toString());
				if(l_soln.get(joinVar) instanceof ResourceImpl)
					p2Str.setIri(joinVar, l_soln.get(joinVar).asNode().toString());

				if(debug){
					System.out.println("----\nExecuting query P2 "+p2Str.asQuery());
				}
				//execute the nested join
				QueryExecution r_qexec = (QueryEngineHTTP) QueryExecutionFactory.sparqlService(sq.getP2Endpoint(),p2Str.asQuery()  );
				res.epCall(1);
				
				ResultSet r_results = r_qexec.execSelect();
				
				while (r_results.hasNext()) {
					c2++;
					QuerySolution r_soln = r_results.nextSolution();
					
					Binding bind = Algebra.merge(((ResultBinding)l_soln).getBinding(), ((ResultBinding)r_soln).getBinding());
					if(bind != null && !results.contains(bind))
						results.add(bind);
				}
				r_qexec.close();
			}
			l_qexec.close();
			res.setInterimResults("0", c);
			res.setInterimResults("1", c2);
			res.setResults(results);
		}catch(Exception e){
			res.setException(e);
			e.printStackTrace(System.out);
		}finally{
			end = System.currentTimeMillis()-start;
			log.info("[JOIN] UNION "+res);
			res.setTotalTime(end);
		}
		return res;
	}


	private String findJoinVar(List<Var> p1Vars, List<Var> p2Vars) {
		for(Var v: p1Vars){
			if(p2Vars.contains(v))
				return v.toString();
		}
		return "";
	}
}
