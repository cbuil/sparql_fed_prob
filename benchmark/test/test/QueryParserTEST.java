package test;

import java.io.FileNotFoundException;

import org.apache.jena.sparql.engine.binding.Binding;

import join.FILTER;
import join.NestedLoop;
import join.SERVICE;
import join.SymmetricalHashJoin;
import join.UNION;
import join.VALUES;
import utils.QueryParser;
import utils.SplitQuery;
import bench.BenchmarkResult;


public class QueryParserTEST
{
    public static void main(String[] args) throws FileNotFoundException
    {

        String qFile = "resources/queries/benchmark_queries/query1.rq";
        qFile = args[0];

        // qFile = "resources/queries/bio/carlos_join1.ini";

        String localEP = "http://200.1.19.244:8888/sparql";
        localEP = args[1];

        // localEP="http://146.155.115.75:3030/mgi/sparql";
        System.out.println("QFile: " + qFile);
        System.out.println("localEP: " + localEP);
        SplitQuery sq = QueryParser.splitQuery(qFile, localEP);
        boolean debug = true;
//        NestedLoop l = new NestedLoop();
        System.out.println("Join option: " + args[2]);
        if (args[2].equals("SERVICE"))
        {
            try
            {
                SERVICE ser = new SERVICE();
                ser.executeHTTP(sq, debug);
            }
            catch (Exception e)
            {
                System.err.println("Error executing SERVICE implementation");
            }
        }

        if (args[2].equals("VALUES"))
        {
            try
            {
                VALUES v = new VALUES();
                v.executeHTTP(sq, debug);
            }
            catch (Exception e)
            {
                System.err.println("Error executing VALUES implementation");
            }
        }

        if (args[2].equals("UNION"))
        {
            try
            {
                UNION u = new UNION();
                u.executeHTTP(sq, debug);
            }
            catch (Exception e)
            {
                System.err.println("Error executing UNION implementation");
            }
        }

        if (args[2].equals("FILTER"))
        {
            try
            {
                FILTER f = new FILTER();
                f.executeHTTP(sq, debug);
            }
            catch (Exception e)
            {
                System.err.println("Error executing FILTER implementation");
            }
        }

        if (args[2].equals("HASH"))
        {
            try
            {
                SymmetricalHashJoin s = new SymmetricalHashJoin();
                s.executeHTTP(sq, debug);
            }
            catch (Exception e)
            {
                System.err
                        .println("Error executing SymmetricalHashJoin implementation");
            }
        }
    }

    private static void printResults(BenchmarkResult res)
    {
        for (Binding b : res.getResults())
        {
            System.out.println(b);
        }

    }
}
