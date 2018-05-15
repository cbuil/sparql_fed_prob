package join;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.MyBindingComparator;
import utils.QueryParser;
import utils.SplitQuery;
import bench.BenchmarkResult;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.core.ResultBinding;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.E_LogicalOr;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.nodevalue.NodeValueNode;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementWalker;

public class FILTER implements JoinOperator
{
    private final static Logger log = LoggerFactory.getLogger(FILTER.class);
    private final HashMap<RDFNode, Set<Binding>> l_soln = new HashMap<RDFNode, Set<Binding>>();
    private final HashMap<RDFNode, Set<Binding>> r_soln = new HashMap<RDFNode, Set<Binding>>();
    private Set<Binding> results = new TreeSet<Binding>(
            new MyBindingComparator());

    private String _joinVar;

    public BenchmarkResult executeHTTP(SplitQuery sq)
    {
        return executeHTTP(sq, false);
    }

    @Override
    public BenchmarkResult executeHTTP(SplitQuery sq, boolean debug)
    {
        BenchmarkResult res = new BenchmarkResult();
        res.setBatchSize(sq.getBatch());
        results = new TreeSet<Binding>(new MyBindingComparator());
        // Set<Binding> results = new TreeSet<Binding>(new
        // MyBindingComparator());
        long start = System.currentTimeMillis();
        long end = 0;
        try
        {
            Query p1 = QueryFactory.create();
            p1.setQueryPattern(sq.getP1());
            p1.setQuerySelectType();
            p1.setQueryResultStar(true);
            // p1.setDistinct(true);

            List<Var> p1Vars = new ArrayList<Var>();
            ElementWalker.walk(sq.getP1(), new GetVars(p1Vars));

            Query p2 = QueryFactory.create();
            p2.setQueryPattern(sq.getP2());
            p2.setQuerySelectType();
            p2.setQueryResultStar(true);
            // p2.setDistinct(true);

            List<Var> p2Vars = new ArrayList<Var>();
            ElementWalker.walk(sq.getP2(), new GetVars(p2Vars));

            _joinVar = QueryParser.findJoinVar(p1Vars, p2Vars);
            // Create variables and bindings for the VALUES block
            List<Var> variables = new ArrayList<Var>();
            variables.add(Var.alloc(_joinVar));

            List<Binding> bindings = new ArrayList<Binding>();

            List<Element> list = new ArrayList<Element>();
            // init execution of left triple pattern
            
            Query p3 = QueryFactory.create();
            if (sq.getP3() != null)
            {
                p3.setQueryPattern(sq.getP3());
                p3.setQuerySelectType();
                p3.setQueryResultStar(true);
            }

            if (sq.getBatch() > 0)
            {
                runBatch(sq, p1, p2, res, debug);
            }
            else
            {
                runSingle(sq, p1, p2, p3, res, debug);
            }


            res.setResults(results);
        }
        catch (Exception e)
        {
            res.setException(e);
            e.printStackTrace();
        }
        finally
        {
            end = System.currentTimeMillis() - start;
            log.info("[JOIN] FILTER " + res);
            res.setTotalTime(end);
        }
        return res;
    }

    private void runBatch(SplitQuery sq, Query p1, Query p2,
            BenchmarkResult res, boolean debug)
    {
        QueryExecution qexec = (QueryEngineHTTP) QueryExecutionFactory
                .sparqlService(sq.getP1Endpoint().toString(), p1);
        ResultSet l_results = qexec.execSelect();
        res.epCall(0);

        Expr eor = null;
        int c = 0, c1 = 0;

        while (l_results.hasNext())
        {
            c++;
            QuerySolution l_soln = l_results.nextSolution();
            addSolution(l_soln, true);

            if (l_soln.get(_joinVar) != null)
            {
                Expr e = new E_Equals(new ExprVar(_joinVar),
                        new NodeValueNode(l_soln.get(_joinVar).asNode()));
                if (eor == null)
                    eor = e;
                else
                    eor = new E_LogicalOr(eor, e);
            }
            if (c % sq.getBatch() == 0 && eor != null)
            {
                Query p22 = p2.cloneQuery();
                System.out.println("Running batch with " + sq.getBatch());
                ElementGroup body = new ElementGroup();
                // System.out.println(eor);
                ElementFilter filter = new ElementFilter(eor);

                body.addElement(p22.getQueryPattern());
                body.addElement(filter);
                p22.setQueryPattern(body);

                if (debug)
                {
                    System.out.println("----\nExecuting query P2 " + p22);
                }

                QueryExecution p2qexec = (QueryEngineHTTP) QueryExecutionFactory
                        .sparqlService(sq.getP2Endpoint().toString(), p22);
                ResultSet p2_results = p2qexec.execSelect();
                res.epCall(1);
                while (p2_results.hasNext())
                {
                    c1++;
                    QuerySolution solnp2 = p2_results.nextSolution();
                    addSolution(solnp2, false);
                }
                p2qexec.close();
                // reset
                eor = null;
            }
        }
        if (c % sq.getBatch() != 0 && eor != null)
        {
            Query p22 = p2.cloneQuery();
            System.out.println("Running batch with " + sq.getBatch());
            ElementGroup body = new ElementGroup();

            ElementFilter filter = new ElementFilter(eor);

            body.addElement(p22.getQueryPattern());
            body.addElement(filter);
            p22.setQueryPattern(body);

            if (debug)
            {
                System.out.println("----\nExecuting query P2 " + p22);
            }

            QueryExecution p2qexec = (QueryEngineHTTP) QueryExecutionFactory
                    .sparqlService(sq.getP2Endpoint().toString(), p22);
            ResultSet p2_results = p2qexec.execSelect();
            res.epCall(1);
            while (p2_results.hasNext())
            {
                c1++;
                QuerySolution solnp2 = p2_results.nextSolution();
                addSolution(solnp2, false);
            }
            p2qexec.close();
            // reset
            eor = null;
        }
        res.setInterimResults("0", c);
        qexec.close();

        res.setInterimResults("1", c1);
        if (c1 == 0)
        {
            results = new TreeSet<Binding>(new MyBindingComparator());
        }

    }

    private void runSingle(SplitQuery sq, Query p1, Query p2, Query p3,
            BenchmarkResult res, boolean debug)
    {
        QueryExecution qexec = (QueryEngineHTTP) QueryExecutionFactory
                .sparqlService(sq.getP1Endpoint().toString(), p1);
        System.out.println("endpoint: " + sq.getP1Endpoint().toString());
        ResultSet l_results = qexec.execSelect();
        res.epCall(0);
        Expr eor = null;
        int c = 0;

        while (l_results.hasNext())
        {
            c++;
            final QuerySolution l_soln = l_results.nextSolution();
            addSolution(l_soln, true);

            if (l_soln.get(_joinVar) != null)
            {
                Expr e = new E_Equals(new ExprVar(_joinVar),
                        new NodeValueNode(l_soln.get(_joinVar).asNode()));
                if (eor == null)
                    eor = e;
                else
                    eor = new E_LogicalOr(eor, e);
            }
        }
        res.setInterimResults("0", c);
        System.out.println("Interim results P1: " + c);
        qexec.close();
        c = 0;

        if (eor != null)
        {
            Query p22 = p2.cloneQuery();
            System.out.println("Running batch with " + sq.getBatch());
            ElementGroup body = new ElementGroup();
            // System.out.println(eor);
            ElementFilter filter = new ElementFilter(eor);

            body.addElement(p22.getQueryPattern());
            body.addElement(filter);
            p22.setQueryPattern(body);

            if (debug)
            {
                System.out.println("----\nExecuting query P2 " + p22);
            }

            QueryExecution p2qexec = (QueryEngineHTTP) QueryExecutionFactory
                    .sparqlService(sq.getP2Endpoint().toString(), p22);
            ResultSet p2_results = p2qexec.execSelect();
            res.epCall(1);
            c = 0;
            while (p2_results.hasNext())
            {
                if (p3 != null)
                {
                    break;
                }
                c++;
                QuerySolution solnp2 = p2_results.nextSolution();
                addSolution(solnp2, false);
            }
            System.out.println("Interim results P2: " + c);
            c = 0;
            if (p3 != null)
            {
                while (p2_results.hasNext())
                {
                    c++;
                    final QuerySolution p2_soln = p2_results.nextSolution();
                    addSolution(p2_soln, true);

                    if (p2_soln.get(_joinVar) != null)
                    {
                        Expr e = new E_Equals(new ExprVar(_joinVar),
                                new NodeValueNode(
                                        p2_soln.get(_joinVar).asNode()));
                        if (eor == null)
                            eor = e;
                        else
                            eor = new E_LogicalOr(eor, e);
                    }
                }
                System.out.println("Interim results P2: " + c);
                p2qexec.close();
                Query p32 = p3.cloneQuery();
                System.out.println("Running batch with " + sq.getBatch());
                ElementGroup bodyP3 = new ElementGroup();
                // System.out.println(eor);
                ElementFilter filterP3 = new ElementFilter(eor);

                bodyP3.addElement(p32.getQueryPattern());
                bodyP3.addElement(filterP3);
                p32.setQueryPattern(bodyP3);

                if (debug)
                {
                    System.out.println("----\nExecuting query P3 " + p32);
                }

                QueryExecution p3qexec = (QueryEngineHTTP) QueryExecutionFactory
                        .sparqlService(sq.getP3Endpoint().toString(), p32);
                ResultSet p3_results = p3qexec.execSelect();
                c = 0;
                while (p3_results.hasNext())
                {
                    c++;
                    QuerySolution solnp3 = p3_results.nextSolution();
                    addSolution(solnp3, false);
                }
                p3qexec.close();
            }
            if (!p2qexec.isClosed())
            {
                p2qexec.close();
            }
            // reset
            eor = null;

            res.setInterimResults("1", c);
        }
        if (c == 0)
        {
            results = new TreeSet<Binding>(new MyBindingComparator());
        }

    }

    public void runThirdSingle(SplitQuery sq, Query p1, Query p2,
            BenchmarkResult res, boolean debug)
    {
        QueryExecution qexec = (QueryEngineHTTP) QueryExecutionFactory
                .sparqlService(sq.getP1Endpoint().toString(), p1);
        ResultSet l_results = qexec.execSelect();
        res.epCall(0);
        Expr eor = null;
        int c = 0;

        while (l_results.hasNext())
        {
            c++;
            final QuerySolution l_soln = l_results.nextSolution();
            addSolution(l_soln, true);

            if (l_soln.get(_joinVar) != null)
            {
                Expr e = new E_Equals(new ExprVar(_joinVar),
                        new NodeValueNode(l_soln.get(_joinVar).asNode()));
                if (eor == null)
                    eor = e;
                else
                    eor = new E_LogicalOr(eor, e);
            }
        }
        res.setInterimResults("0", c);
        qexec.close();
        c = 0;

        if (eor != null)
        {
            Query p22 = p2.cloneQuery();
            System.out.println("Running batch with " + sq.getBatch());
            ElementGroup body = new ElementGroup();
            // System.out.println(eor);
            ElementFilter filter = new ElementFilter(eor);

            body.addElement(p22.getQueryPattern());
            body.addElement(filter);
            p22.setQueryPattern(body);

            if (debug)
            {
                System.out.println("----\nExecuting query P2 " + p22);
            }

            QueryExecution p2qexec = (QueryEngineHTTP) QueryExecutionFactory
                    .sparqlService(sq.getP2Endpoint().toString(), p22);
            ResultSet p2_results = p2qexec.execSelect();
            res.epCall(1);
            c = 0;
            while (p2_results.hasNext())
            {
                c++;
                QuerySolution solnp2 = p2_results.nextSolution();
                addSolution(solnp2, false);
            }
            p2qexec.close();
            // reset
            eor = null;

            res.setInterimResults("1", c);
        }
        if (c == 0)
        {
            results = new TreeSet<Binding>(new MyBindingComparator());
        }

    }

    synchronized public void addSolution(QuerySolution soln, boolean _left)
    {
        HashMap<RDFNode, Set<Binding>> map = r_soln;
        HashMap<RDFNode, Set<Binding>> comp = l_soln;
        if (_left)
        {
            map = l_soln;
            comp = r_soln;
        }

        Set<Binding> set = map.get(soln.get(_joinVar));
        if (set == null)
        {
            set = new TreeSet<Binding>(new MyBindingComparator());
            map.put(soln.get(_joinVar), set);
            // System.out.println("Added (l:"+_left+") "+soln.get(_joinVar));
        }
        set.add(((ResultBinding) soln).getBinding());

        if (comp.containsKey(soln.get(_joinVar)))
        {
            // System.out.println("Here");
            for (Binding s : comp.get(soln.get(_joinVar)))
            {
                Binding bind = Algebra.merge(s,
                        ((ResultBinding) soln).getBinding());
                // if(bind==null)System.out.println("What is happening?");
                if (bind != null && !results.contains(bind))
                {
                    results.add(bind);
                    // System.out.println("RESULT");
                }
            }
        }
    }
}
