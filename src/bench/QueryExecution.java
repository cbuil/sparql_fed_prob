package bench;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import join.JoinOps;
import utils.SplitQuery;

public class QueryExecution
{
    private final static Logger log = LoggerFactory
            .getLogger(QueryExecution.class);

    public Map<String, BenchmarkResult> executeOverHTTP(SplitQuery sq,
            boolean debug, JoinOps... joinOps)
    {
        Map<String, BenchmarkResult> results = new HashMap<String, BenchmarkResult>();
        for (JoinOps jop : joinOps)
        {
            try
            {
                BenchmarkResult res = jop.executeHTTP(sq, debug);
                results.put(jop.name(), res);
            }
            catch (Exception e)
            {
                log.warn("[JOIN] " + jop.toString() + " [ERROR] "
                        + e.getClass().getSimpleName());
                e.printStackTrace();
            }
        }
        return results;
    }

    public Map<String, BenchmarkResult> executeOverHTTP(SplitQuery sq,
            boolean debug, JoinOps jop)
    {
        Map<String, BenchmarkResult> results = new HashMap<String, BenchmarkResult>();
        try
        {
            BenchmarkResult res = jop.executeHTTP(sq, debug);
            results.put(jop.name(), res);
        }
        catch (Exception e)
        {
            log.warn("[JOIN] " + jop.toString() + " [ERROR] "
                    + e.getClass().getSimpleName());
            e.printStackTrace();
        }

        return results;
    }
}
