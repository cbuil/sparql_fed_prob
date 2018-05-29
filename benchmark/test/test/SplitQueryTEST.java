package test;

import java.io.FileNotFoundException;

import org.apache.jena.sparql.engine.binding.Binding;

import utils.QueryParser;
import utils.SplitQuery;
import bench.BenchmarkResult;

public class SplitQueryTEST
{
    public static void main(String[] args) throws FileNotFoundException
    {

        String qFile = "benchmark_queries/query1.rq";
        qFile = args[0];

        // qFile = "resources/queries/bio/carlos_join1.ini";

        String localEP = "http://localhost:8890/sparql";
        localEP = args[1];

        // localEP="http://146.155.115.75:3030/mgi/sparql";
        System.out.println("QFile: " + qFile);
        System.out.println("localEP: " + localEP);

        SplitQuery sq = QueryParser.splitQuery(qFile, localEP);
    }

    private static void printResults(BenchmarkResult res)
    {
        for (Binding b : res.getResults())
        {
            System.out.println(b);
        }

    }
}
