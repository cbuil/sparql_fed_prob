package join;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.apache.jena.sparql.core.ResultBinding;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingHashMap;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.aggregate.AggGroupConcat;
import org.apache.jena.sparql.expr.aggregate.Aggregator;
import org.apache.jena.sparql.syntax.ElementWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.MyBindingComparator;
import utils.QueryParser;
import utils.SplitQuery;
import bench.BenchmarkResult;

public class SymmetricalHashJoinGroupConcat implements JoinOperator
{
    private final static Logger log = LoggerFactory
            .getLogger(SymmetricalHashJoinGroupConcat.class);

    private boolean left_expResults = true;
    private boolean right_expResults = true;
    private final HashMap<RDFNode, Set<Binding>> l_soln = new HashMap<RDFNode, Set<Binding>>();
    private final HashMap<RDFNode, Set<Binding>> r_soln = new HashMap<RDFNode, Set<Binding>>();
    private Set<Binding> results = new TreeSet<Binding>(
            new MyBindingComparator());

    private String _joinVar;

    synchronized public void addSolution(QuerySolution resultBindingN,
            boolean _left)
    {
        if (resultBindingN == null)
        {
            if (_left)
                left_expResults = false;
            else
                right_expResults = false;
            return;
        }
        HashMap<RDFNode, Set<Binding>> map = r_soln;
        HashMap<RDFNode, Set<Binding>> comp = l_soln;
        if (_left)
        {
            map = l_soln;
            comp = r_soln;
        }

        // Here Jurgen checks whether it exists the variable from resultBindingN
        // in the HashMap
        // with the solutions obtained from the other side execution, stored in
        // HashMap<RDFNode, Set<Binding>> map
        Set<Binding> set = map.get(resultBindingN.get(_joinVar));
        if (set == null)
        {
            // if the solution binding was not in map, new variable as Key
            set = new TreeSet<Binding>(new MyBindingComparator());
            map.put(resultBindingN.get(_joinVar), set);
            // System.out.println("Added (l:"+_left+") "+soln.get(_joinVar));
        }
        // Adds to the TreeSet the values for those variables in the solution
        // bindings from the SERVICE call
        set.add(((ResultBinding) resultBindingN).getBinding());

        if (comp.containsKey(resultBindingN.get(_joinVar)))
        {
//            Var groupConcatVar = Var.alloc("group_concat_var");
            // System.out.println("Here");
            for (Binding s : comp.get(resultBindingN.get(_joinVar)))
            {
//                if (s.contains(groupConcatVar))
//                {
//                    StringTokenizer groupConcatStringTokenizer = new StringTokenizer(
//                            s.get(groupConcatVar).getLiteral().toString(), "|");
//                    while (groupConcatStringTokenizer.hasMoreTokens())
//                    {
//                        BindingHashMap bm = new BindingHashMap();
//                        String groupConcatBinding = groupConcatStringTokenizer
//                                .nextToken();
//                        ResourceImpl groupConcatBindingResource = new ResourceImpl(
//                                groupConcatBinding);
//                        bm.add(Var.alloc(_joinVar),
//                                groupConcatBindingResource.asNode());
//                        Binding bind = Algebra.merge(s, bm);
//
//                        if (bind != null && !results.contains(bind)
//                                && bind.contains(Var.alloc(_joinVar)))
//                        {
//                            results.add(bind);
//                            // System.out.println("RESULT");
//                        }
//                    }
//                }
                Binding bind = Algebra.merge(s,
                        ((ResultBinding) resultBindingN).getBinding());

                if (bind != null && !results.contains(bind)
                        && bind.contains(Var.alloc(_joinVar)))
                {
                    results.add(bind);
                    // System.out.println("RESULT");
                }
            }
        }
    }

    public BenchmarkResult executeHTTP(SplitQuery sq)
    {
        return executeHTTP(sq, false);
    }

    @Override
    public BenchmarkResult executeHTTP(SplitQuery sq, boolean debug)
    {
        results = new TreeSet<Binding>(new MyBindingComparator());
        BenchmarkResult res = new BenchmarkResult();
        res.setBatchSize(sq.getBatch());
        long start = System.currentTimeMillis();
        long end = 0;
        try
        {
            Var groupConcatVar = Var.alloc("group_concat_var");

            Query p1 = QueryFactory.create();
            p1.setQueryPattern(sq.getP1());
            // p1.setQuerySelectType();
            // p1.setQueryResultStar(true);
            // p1.setDistinct(true);

            List<Var> p1Vars = new ArrayList<Var>();
            ElementWalker.walk(sq.getP1(), new GetVars(p1Vars));

            Query p2 = QueryFactory.create();
            p2.setQueryPattern(sq.getP2());
            // p2.setQuerySelectType();
            // p2.setQueryResultStar(true);
            // p2.setDistinct(true);

            List<Var> p2Vars = new ArrayList<Var>();
            ElementWalker.walk(sq.getP2(), new GetVars(p2Vars));
            _joinVar = QueryParser.findJoinVar(p1Vars, p2Vars);
            // p2.setQueryResultStar(true);
            System.out.println("----GROUP_CONCAT query----");
            System.out.println(p2.toString());
            ArrayList<Var> projectVars = new ArrayList<Var>();
            // projectVars.add(QueryParser.findJoinVarobject(p1Vars, p2Vars));
            Expr groupConcatExpr = new ExprVar(_joinVar);
            Aggregator groupConcat = new AggGroupConcat(groupConcatExpr, "|");
            for (String varName : QueryParser
                    .findServiceVars(sq.getSelectVars(), p1Vars))
            {
                if (!_joinVar.equals(varName))
                {
                    Var var = Var.alloc(varName);
                    projectVars.add(var);
                    p1.addGroupBy(var);
                }
            }
            p1.addProjectVars(projectVars);
            p1.setResultVars();
            p1.setQuerySelectType();
            p1.getProject().add(groupConcatVar, p1.allocAggregate(groupConcat));
            // p1.addGroupBy(groupConcatVar);

            projectVars.clear();
            for (String varName : QueryParser
                    .findServiceVars(sq.getSelectVars(), p2Vars))
            {
                if (!_joinVar.equals(varName))
                {
                    Var var = Var.alloc(varName);
                    projectVars.add(var);
                    p2.addGroupBy(var);
                }
            }
            p2.addProjectVars(projectVars);
            p2.setResultVars();
            p2.setQuerySelectType();
            p2.getProject().add(groupConcatVar, p2.allocAggregate(groupConcat));
            // p2.addGroupBy(groupConcatVar);

            if (debug)
            {
                System.out.println("Executing query P1 " + p1);
                System.out.println("at endpoint " + sq.getP1Endpoint());
            }
            QueryExecutorThread l_thread = new QueryExecutorThread(this,
                    sq.getP1Endpoint(), p1, true);
            l_thread.start();
            res.epCall(0);

            if (debug)
            {
                System.out.println("Executing query P2 " + p2);
                System.out.println("at endpoint " + sq.getP2Endpoint());
            }
            QueryExecutorThread r_thread = new QueryExecutorThread(this,
                    sq.getP2Endpoint(), p2, false);
            r_thread.start();
            res.epCall(1);

            while (left_expResults || right_expResults)
            {
                try
                {
                    Thread.sleep(100);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
            res.setResults(results);
            res.setInterimResults("0", l_thread.getResults());
            res.setInterimResults("1", r_thread.getResults());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            res.setException(e);
            // e.printStackTrace(System.out);
            // e.printStackTrace(System.out);
        }
        finally
        {
            end = System.currentTimeMillis() - start;
            log.info("[JOIN] SYMHASH " + res);
            res.setTotalTime(end);
        }
        return res;
    }

    class QueryExecutorThread extends Thread
    {

        private SymmetricalHashJoinGroupConcat _join;
        private String _url;
        private Query _query;
        private boolean _left;
        private int c;

        public QueryExecutorThread(
                SymmetricalHashJoinGroupConcat symmetricalHashJoinGroupConcat,
                String url, Query query, boolean left)
        {
            super();
            _url = url;
            _join = symmetricalHashJoinGroupConcat;
            _query = query;
            _left = left;
        }

        public int getResults()
        {
            // TODO Auto-generated method stub
            return c;
        }

        @Override
        public void run()
        {
            QueryExecution qexec;
            qexec = QueryExecutionFactory.sparqlService(_url, _query);

            ResultSet results = qexec.execSelect();
            c = 0;
            while (results.hasNext())
            {
                c++;
                QuerySolution soln = results.next();
                QuerySolutionMap solMap = new QuerySolutionMap();
                RDFNode node = new ResourceImpl();
                solMap.add(soln.varNames().next(), node);
                
                Model model = ModelFactory.createDefaultModel();
                if(soln.contains("group_concat_var")){
                    StringTokenizer groupConcatST = new StringTokenizer(
                            soln.get("group_concat_var").toString(), "|");
                    BindingHashMap bm0 = new BindingHashMap();
                    Iterator<String> variableIterator = soln.varNames();
                    while(variableIterator.hasNext()){
                        String bindingVariableName = variableIterator.next();
                        if(!bindingVariableName.contains("group_concat_var")){
                            RDFNode bindingVariable = soln.get(bindingVariableName);
                            bm0.add(Var.alloc(bindingVariableName),
                                    soln.get(bindingVariableName).asNode());
                        }
                    }
                    while (groupConcatST.hasMoreTokens())
                    {
                        BindingHashMap bm = new BindingHashMap();
                        String groupConcatBinding = groupConcatST.nextToken();
                        ResourceImpl groupConcatBindingResource = new ResourceImpl(
                                groupConcatBinding);
                        bm.add(Var.alloc(_joinVar),
                                groupConcatBindingResource.asNode());
                        ResultBinding rb = new ResultBinding(model, Algebra.merge(bm0, bm));
                        _join.addSolution(rb, _left);
                        if (_left)
                            System.out.print("#");
                        else
                        {
                            System.out.print(".");
                        }
                        if (c % 80 == 0)
                            System.out.println();
                    }
                } else {
                
                _join.addSolution(soln, _left);
                if (_left)
                    System.out.print("#");
                else
                {
                    System.out.print(".");
                }
                if (c % 80 == 0)
                    System.out.println();
                }
            }
            qexec.close();
            System.out.println("Done " + _left);
            _join.addSolution(null, _left);
        }
    }
}
