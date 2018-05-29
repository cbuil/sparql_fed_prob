package bench_carlos;

import java.util.List;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.rdf.model.Model ;
public class ResultSetBenchWrapper implements ResultSet {

	
	private final ResultSet _res;
	private final Long _start;
	private Long _end = null;
	private int results =0 ;
	
	public ResultSetBenchWrapper(final ResultSet res) {
		_res = res;
		_start = System.currentTimeMillis();
	}
	
	@Override
	public void remove() {
		_res.remove();
	}

	@Override
	public boolean hasNext() {
		return _res.hasNext();
	}

	@Override
	public QuerySolution next() {
		QuerySolution qs =_res.next();
		if(qs!=null && _end == null)
			_end = new Long(System.currentTimeMillis());
		if(qs!=null) results++;
		return qs;
	}

	@Override
	public QuerySolution nextSolution() {
		return _res.nextSolution();
	}

	@Override
	public Binding nextBinding() {
		return _res.nextBinding();
	}

	@Override
	public int getRowNumber() {
		return _res.getRowNumber();
	}

	@Override
	public List<String> getResultVars() {
		return _res.getResultVars();
	}

	@Override
	public Model getResourceModel() {
		return _res.getResourceModel();
	}

	public long timeForFirstResult() {
		if(_end == null )  return -1;
		return _end-_start;
	}

	public int results() {
		return results;
	}

	
}
