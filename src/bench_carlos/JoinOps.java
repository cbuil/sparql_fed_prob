package bench_carlos;

import utils.SplitQuery;

public enum JoinOps {
		NESTED(new NestedLoop()),
		SYMHASH(new SymmetricalHashJoin()),
		VALUES( new VALUES()),
//		UNION( new UNION()),
//		UNIONFILTER( new UNIONFilter()),
//		FILTER( new FILTER()),
//		GCSYMHASH( new SymmetricalHashJoinGroupConcat()),
//		SYMHASHP(new SymmetricalHashPageJoin()),
		SERVICE(new SERVICE());
		
		private JoinOperator delegate;
		private JoinOps(JoinOperator delegate) { this.delegate=delegate; }
		
//		public Set<Binding> executeInMemory(Model model_left, Model model_right,boolean samePosJoin) {
//			return delegate.executeInMemory(model_left,model_right, samePosJoin);
//		}

		public BenchmarkResult executeHTTP(SplitQuery sq, boolean debug) {
			 return delegate.executeHTTP(sq, debug);
		}
}
