package join;

import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.MyBindingComparator;
import utils.SplitQuery;
import bench.BenchmarkResult;
import bench.CONSTANTS;






















import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.core.ResultBinding;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.binding.BindingMap;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.E_LogicalOr;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.nodevalue.NodeValueNode;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.sparql.syntax.ElementUnion;
import org.apache.jena.sparql.syntax.ElementWalker;

public class UNIONFilter implements JoinOperator{
	private final static Logger log = LoggerFactory.getLogger(UNIONFilter.class);
	
	

	@Override
	public BenchmarkResult executeHTTP(SplitQuery sq, boolean debug) {
		BenchmarkResult res = new BenchmarkResult();

		Set<Binding> results = new TreeSet<Binding>(new MyBindingComparator());
//
//
//		
//		Query r_q = QueryFactory.create(s2);
//		ElementGroup qBody = (ElementGroup) r_q.getQueryPattern();;
//		Triple tJoin = null;
//		for(Element e: qBody.getElements()){
//			if(e instanceof ElementPathBlock){
//				ElementPathBlock etb = (ElementPathBlock) e;
//				for( TriplePath tp: etb.getPattern()){
//
//					if ( tp.getSubject().equals( join ) || tp.getPredicate().equals( join )|| tp.getObject().equals( join )) {
//						tJoin = tp.asTriple();
//					}}
//			}
//		}
//		
//		//body for r_query
//		ElementGroup body = new ElementGroup();
//		long start = System.currentTimeMillis();
//		//init execution of left triple pattern
//		QueryExecution l_qexec =(QueryEngineHTTP) QueryExecutionFactory.sparqlService(endpoints[0].toString(), s1);
//		res.epCall(0);
//		ResultSet l_results = l_qexec.execSelect();
//		Expr eor = null;
//		while (l_results.hasNext()) {
//			final QuerySolution l_soln = l_results.nextSolution();
//			l_soln.get("join");
//			
//			Expr e = new E_Equals(new ExprVar("join"), new NodeValueNode(l_soln.get("join").asNode()));
//			if(eor ==null)
//				eor = e;
//			else
//				eor = new E_LogicalOr(eor, e);
//			
//			
//		}
//		l_qexec.close();
//		ElementFilter filter = new ElementFilter(eor);
//		ElementTriplesBlock block = new ElementTriplesBlock();
//		block.addTriple(tJoin);
//		body.addElement(block);
//		body.addElement(filter);
//
//		Query r_qT = QueryFactory.create();
//		r_qT.setQuerySelectType();
//		r_qT.setQueryPattern(body);
//
//		r_qT.setQueryResultStar(true);
//		r_qT.setDistinct(true);
//
//		
//		QueryExecution r_qexec = (QueryEngineHTTP) QueryExecutionFactory.sparqlService(endpoints[1].toString(), r_qT);
//		res.epCall(1);
//		ResultSet r_results = r_qexec.execSelect();
//		while (r_results.hasNext()) {
//			final QuerySolution soln = r_results.nextSolution();
//			Binding bind = Algebra.merge(((ResultBinding)soln).getBinding(), ((ResultBinding)soln).getBinding());
//			if(!results.contains(bind))
//				results.add(bind);
//		}
//		r_qexec.close();
//		//stop time measure
//		long end = System.currentTimeMillis();
//		log.info("[JOIN] NestedLoop [RESULTS] "+results.size()+" [TIME] "+(end-start)+" ms");
//		res.setResults(results);
		return res;
	}

	private Triple createTriple(RDFNode rdfNode, Triple tJoin) {
		if(tJoin.getSubject().equals(tJoin))
			return Triple.create(rdfNode.asNode(), tJoin.getPredicate(), tJoin.getObject());
		if(tJoin.getPredicate().equals(tJoin))
			return Triple.create(tJoin.getSubject(), rdfNode.asNode(), tJoin.getObject());
		if(tJoin.getObject().equals(tJoin))
			return Triple.create(tJoin.getSubject(), tJoin.getPredicate(), rdfNode.asNode());
		return null;
	}

	
}