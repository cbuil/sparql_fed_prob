package test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.BindingHashMap;
import org.junit.Assert;
import org.junit.Test;

import ops.Tasks;
import utils.QueryParser;
import utils.SplitQuery;

public class ServiceTest
{

    String q1Str = "benchmark/resources/queries/theorem/example2/query1.rq";

    String endpoint1 = "http://localhost:3030/sparql_fed_prob_1/sparql";
    int batch = 500;

    @Test
    public void serviceTest() throws FileNotFoundException
    {

        SplitQuery sq = QueryParser.splitQuery(q1Str, endpoint1);
        sq.setBatch(batch);

        Query p1 = QueryFactory.create();

        p1.setQueryPattern(sq.getP1());
        p1.setQuerySelectType();
        p1.setQueryResultStar(true);

        Query p2 = QueryFactory.create();

        p2.setQueryPattern(sq.getP2());
        p2.setQuerySelectType();
        p2.setQueryResultStar(true);

        // Execute first query:
        TreeSet<BindingHashMap> resultsService1 = Tasks.service(endpoint1, p1);

        ArrayList<Var> match_vars = new ArrayList<>();
        for (String varString : QueryParser.findJoinVars(p1.getProjectVars(),
                p2.getProjectVars()))
        {
            match_vars.add(Var.alloc(varString));
        }

        TreeSet<BindingHashMap> results1 = Tasks.serviceNestedLoop(endpoint1,
                p2, resultsService1, match_vars, batch);
        TreeSet<BindingHashMap> results2 = Tasks.serviceWithValues(endpoint1,
                p2, resultsService1, match_vars, batch);

        for (BindingHashMap bindingHashMap : results1)
        {
            for (BindingHashMap bindingHashMap2 : results2)
            {
                Iterator varIt = bindingHashMap2.vars();
                while (varIt.hasNext())
                {
                    Var var = (Var) varIt.next();
                    Node res = bindingHashMap.get(var);
                    {
                        assertEquals(res.getURI(), bindingHashMap2.get1(var).getURI());
                    }
                }
            }
        }

        // Expr expr;
        // expr = Tasks.matchExpr(resultsService1.first(), match_vars);
        // Tasks.filter(resultsService1, match_vars, expr, batch);

//        Assert.assertTrue(true);
    }

}
