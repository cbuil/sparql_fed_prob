package utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.ResultBinding;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;

public class MyResultSet implements ResultSet {

	private Iterator<Binding> iter;
	private Binding next;
	private int rowNumber;
	private List<String> vars = new ArrayList<String>();
	
	public MyResultSet(Set<Binding> results) {
		iter = results.iterator();
		if(iter.hasNext()){
			next = iter.next();
			Iterator<Var> v = next.vars();
			while(v.hasNext()){
				vars.add(v.next().toString());
			}
		}
	}

	@Override
	public void remove() {
		;
	}

	@Override
	public boolean hasNext() {
		return next!=null;
	}

	@Override
	public QuerySolution next() {
		Binding b = nextBinding();
		return new ResultBinding(null, b);
	}

	@Override
	public QuerySolution nextSolution(){ return next() ; }
	

	@Override
	public Binding nextBinding() {
		Binding b = next;
		next=null;
		if(iter.hasNext()){
			next = iter.next();
		}
		 if ( b != null )
	          rowNumber++ ;
		return b;
	}

	@Override
	public int getRowNumber() {
		return rowNumber;
	}

	@Override
	public List<String> getResultVars() {
		return vars;
	}

	@Override
	public Model getResourceModel() {
		// TODO Auto-generated method stub
		return null;
	}

}
