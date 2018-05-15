package join;

import java.net.URI;
import java.util.Set;

import utils.SplitQuery;
import bench.BenchmarkResult;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.engine.binding.Binding;

public enum JoinOps {
		NESTED(new NestedLoop()),
		SYMHASH(new SymmetricalHashJoin()),
		VALUES( new VALUES()),
		UNION( new UNION()),
		UNIONFILTER( new UNIONFilter()),
		FILTER( new FILTER()),
		SERVICE( new SERVICE()),
		SYMHASHP(new SymmetricalHashPageJoin()),
		GCSYMHASH(new SymmetricalHashJoinGroupConcat());
		
		private JoinOperator delegate;
		private JoinOps(JoinOperator delegate) { this.delegate=delegate; }
		
//		public Set<Binding> executeInMemory(Model model_left, Model model_right,boolean samePosJoin) {
//			return delegate.executeInMemory(model_left,model_right, samePosJoin);
//		}

		public BenchmarkResult executeHTTP(SplitQuery sq, boolean debug) {
			 return delegate.executeHTTP(sq, debug);
		}
}
