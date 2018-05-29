package bench_carlos;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ops.Tasks;
import utils.MyBindingComparator;
import utils.QueryParser;
import utils.SplitQuery;

public class SymmetricalHashJoin implements JoinOperator
{
    private final static Logger log = LoggerFactory.getLogger(SymmetricalHashJoin.class);

    public BenchmarkResult executeHTTP(SplitQuery sq)
    {
        return executeHTTP(sq, false);
    }

    @Override
    public BenchmarkResult executeHTTP(SplitQuery sq, boolean debug)
    {
        BenchmarkResult res = new BenchmarkResult();
        res.setBatchSize(sq.getBatch());
        Set<Binding> results = new TreeSet<Binding>(new MyBindingComparator());
        long start = System.currentTimeMillis();
        long end = 0;
        try
        {

            // init execution of left triple pattern
            int interimResultsC = 0;
            int interimresultsC2 = 0;
            
            Query p1 = QueryFactory.create();

            p1.setQueryPattern(sq.getP1());
            p1.setQuerySelectType();
            p1.setQueryResultStar(true);
            
            TreeSet<BindingHashMap> resultsP1 = Tasks.service(sq.getP1Endpoint(), p1);
            
            Query p2 = QueryFactory.create();

            p2.setQueryPattern(sq.getP1());
            p2.setQuerySelectType();
            p2.setQueryResultStar(true);
            ArrayList<Var> match_vars = new ArrayList<>();
            for (String varString : QueryParser.findJoinVars(p1.getProjectVars(),
                    p2.getProjectVars()))
            {
                match_vars.add(Var.alloc(varString));
            }
            
            TreeSet<BindingHashMap> resultsP2 = Tasks.serviceSymmetricHash(sq.getP2Endpoint(), p2, resultsP1, match_vars, sq.getBatch());
            
            res.setInterimResults("0", resultsP1.size());
            res.setInterimResults("1", resultsP2.size());
            res.setResults(resultsP2);
        }
        catch (Exception e)
        {
            res.setException(e);
            e.printStackTrace(System.out);
        }
        finally
        {
            end = System.currentTimeMillis() - start;
            log.info("[JOIN] UNION " + res);
            res.setTotalTime(end);
        }
        return res;
    }
}
