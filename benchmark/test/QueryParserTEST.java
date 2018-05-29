import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import org.apache.jena.sparql.engine.binding.Binding;

import bench.BenchmarkResult;
import join.FILTER;
import join.SERVICE;
import join.SymmetricalHashJoinGroupConcat;
import join.VALUES;
import utils.QueryParser;
import utils.SplitQuery;

public class QueryParserTEST
{
    public static void main(String[] args) throws FileNotFoundException
    {

        String qFile = "resources/queries/bio/carlos_join5.rq";

        String localEP = "http://200.1.19.244:3030/mgi/sparql";
        Map<String, BenchmarkResult> results = new HashMap<String, BenchmarkResult>();
        SplitQuery sq = QueryParser.splitQuery(qFile, localEP);

        boolean debug = true;
        // System.out.println("Running " + qFile);
        // NestedLoop l = new NestedLoop();
        // // BenchmarkResult res = l.executeHTTP(sq,debug);
        // // results.put("NESTED", res);
        // SERVICE ser = new SERVICE();
        // // results.put("SERVICE", ser.executeHTTP(sq,debug));
        //
        // VALUES v = new VALUES();
        // results.put("VALUES", v.executeHTTP(sq, debug));
        //
        // UNION u = new UNION();
        // results.put("UNION", u.executeHTTP(sq, debug));

        SymmetricalHashJoinGroupConcat f = new SymmetricalHashJoinGroupConcat();
        results.put("GCSYMHASH", f.executeHTTP(sq, debug));

        // SymmetricalHashJoin s = new SymmetricalHashJoin();
        // results.put("HASH", s.executeHTTP(sq, debug));

    }

    private static void printResults(BenchmarkResult res)
    {
        for (Binding b : res.getResults())
        {
            System.out.println(b);
        }

    }
}
