package bench_carlos;

import utils.SplitQuery;

public interface JoinOperator {

	BenchmarkResult executeHTTP(SplitQuery sq, boolean debug);
}
