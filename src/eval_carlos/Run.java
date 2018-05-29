package eval_carlos;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.http.HttpQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bench_carlos.BenchmarkResult;
import bench_carlos.QueryExecution;
import bench_carlos.JoinOps;
import utils.QueryParser;
import utils.SplitQuery;

/**
 * 
 * @author UmbrichJ
 * 
 */
public class Run extends CLIObject
{

    private final static Logger log = LoggerFactory.getLogger(Run.class);
    private final static String descr = "Benchmark the execution of a SPARQL query against an endpoint URI";

    @Override
    public String getDescription()
    {
        return descr;
    }

    @Override
    protected void addOptions(Options opts)
    {
        opts.addOption(PARAMETERS.OPTION_OUTPUT_DIR);
        opts.addOption(PARAMETERS.OPTION_JOIN);
        opts.addOption(PARAMETERS.OPTION_DEBUG);
        opts.addOption(PARAMETERS.OPTION_ENDPOINTS);
        opts.addOption(PARAMETERS.OPTION_BATCHSIZE);
        opts.addOption(PARAMETERS.OPTION_SUBQUERIES);
    }

    @Override
    protected void execute(CommandLine cmd)
    {
        try
        {
            String[] joins = cmd.getOptionValue(PARAMETERS.PARAM_JOIN).trim()
                    .split(" ");
            System.out.println("Joins " + Arrays.toString(joins));
            File outDir = new File(
                    cmd.getOptionValue(PARAMETERS.PARAM_OUTPUT_DIR));

            if (!outDir.exists())
                outDir.mkdirs();

            String localEndpoint = cmd
                    .getOptionValue(PARAMETERS.PARAM_ENDPOINTS);
            File subQueries = new File(
                    cmd.getOptionValue(PARAMETERS.PARAM_SUBQUERIES));

            System.out.println(cmd.getOptionValue(PARAMETERS.PARAM_BATCHSIZE));
            int batch = Integer.valueOf(
                    cmd.getOptionValue(PARAMETERS.PARAM_BATCHSIZE, "-1"));
            System.out.println(batch);
            boolean debug = cmd.hasOption(PARAMETERS.PARAM_DEBUG);

            // force post
            HttpQuery.urlLimit = 0;

            if (subQueries.isDirectory())
            {

                for (File q : subQueries.listFiles())
                {
                    if (q.isFile())
                    {
                        runQuery(q, joins, localEndpoint, outDir, debug, batch);
                    }
                }
            }
            else
            {
                runQuery(subQueries, joins, localEndpoint, outDir, debug,
                        batch);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void runQuery(File q, String[] joins, String localEndpoint,
            File outDir, boolean debug, int batch)
    {
        try
        {
            SplitQuery sq = QueryParser.splitQuery(q.toString(), localEndpoint);
            sq.setBatch(batch);

            QueryExecution qe = new QueryExecution();

            PrintWriter pwAll = new PrintWriter(
                    new File(outDir, q.getName() + "_results.tsv"));
            for (String join : joins)
            {
                System.out.println("RUNNING " + join + " for " + q);
                Map<String, BenchmarkResult> results = qe.executeOverHTTP(sq,
                        debug, JoinOps.valueOf(join));
                
                writeResults(join, batch, outDir, pwAll, results);
            }
            pwAll.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    private void writeResults(String outDir, int join, File q,
            PrintWriter pwAll, Map<String, BenchmarkResult> results)
            throws FileNotFoundException
    {
        PrintWriter pw = new PrintWriter(
                new File(outDir, q.getName() + "_" + join + ".txt"));

        for (Entry<String, BenchmarkResult> ent : results.entrySet())
        {
            pw.println("#JOIN results time ep0_calls ep1_calls");
            pw.println(ent.getValue().getShortString(","));
            pw.println("#Bindings");
            System.out.println(q.getName() + "," + ent.getKey() + ","
                    + ent.getValue().getShortString(","));
            if (ent.getValue() != null && ent.getValue().getResults() != null)
            {
                for (Binding b : ent.getValue().getResults())
                {
                    pw.println(b);
                }
            }
            System.err.println(q.getName() + "\t" + ent.getKey() + "\t"
                    + ent.getValue().getShortString("\t"));
            pwAll.println(q.getName() + "\t" + ent.getKey() + "\t"
                    + ent.getValue().getShortString("\t"));
        }
        pw.close();
    }

    public static void main(String[] args)
    {
        String[] joins = {
                 "VALUES",
                // "UNION",
                 "SERVICE",
                // "FILTER",
                 "NESTED",
                // "SYMHASH",
                // "SYMHASHP",
//                "GCSYMHASH" 
                };
        System.out.println("Joins " + Arrays.toString(joins));
        File outDir = new File("test.outdir");
        if (!outDir.exists())
            outDir.mkdirs();

        // String localEndpoint = "http://localhost:3030/mgi/sparql";
        String localEndpoint = "http://localhost:3030/sparql_fed_prob_1/sparql";
        // File subQueries = new File("resources/queries/bio/carlos_join2.rq");
        File subQueries = new File("resources/queries/theorem/example2/query1.rq");
        // File subQueries = new File("resources/queries/movies/query7.txt");
        // File subQueries = new File("resources/queries/bio/carlos_join7.txt");

        boolean debug = false;
        int batch = 1000;
        Run run = new Run();
        if (subQueries.isDirectory())
        {
            for (File q : subQueries.listFiles())
            {
                if (q.isFile())
                {
                    run.runQuery(q, joins, localEndpoint, outDir, debug, batch);
                }
            }
        }
        else
        {
            run.runQuery(subQueries, joins, localEndpoint, outDir, debug,
                    batch);
        }
    }
}