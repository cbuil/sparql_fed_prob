package ops;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.binding.BindingHashMap;
import org.apache.jena.sparql.engine.binding.BindingMap;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.E_LogicalAnd;
import org.apache.jena.sparql.expr.E_LogicalOr;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.nodevalue.NodeValueNode;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementUnion;

public class Tasks
{

    // Execute SERVICE query
    public static TreeSet<BindingHashMap> service(String endpoint, Query query)
    {
        QueryExecution qexec = (QueryEngineHTTP) QueryExecutionFactory
                .sparqlService(endpoint, query);
        ResultSet results = qexec.execSelect();
        // Start iterating
        TreeSet<BindingHashMap> ret = new TreeSet<BindingHashMap>(new BindingHashMapComparator());
        while (results.hasNext())
        {
            BindingHashMap q = (BindingHashMap) results.nextBinding();
            ret.add(q);
        }
        // Close query execution
        qexec.close();
        return ret;
    }

    // Execute SERVICE query with VALUES
    public static TreeSet<BindingHashMap> serviceWithValues(String endpoint, Query query,
        TreeSet<BindingHashMap> bindings, ArrayList<Var> match_vars)
    {
        // Fill a new list of bindings with the values
        List<Binding> bindingsf = new ArrayList<Binding>();
        for (BindingHashMap q : bindings)
        {
            BindingMap m = BindingFactory.create();
            for (final Var var : match_vars)
            {
                if (q.get(var) != null)
                    m.add(var, q.get(var));
            }
            bindingsf.add(m);
        }
        // Set the values data block to a copy of the query
        Query nquery = query.cloneQuery();
        nquery.setValuesDataBlock(match_vars, bindingsf);
        // Call the service:
        return service(endpoint, nquery);
    }

    // Execute SERVICE query with a nested loop
    public static TreeSet<BindingHashMap> serviceNestedLoop(String endpoint,
            Query query, TreeSet<BindingHashMap> bindings, ArrayList<Var> match_vars)
    {
        assert (match_vars != null);
        TreeSet<BindingHashMap> ret = new TreeSet<BindingHashMap>(new BindingHashMapComparator());
        // For each binding call a SERVICE
        for (BindingHashMap q : bindings)
        {
            ParameterizedSparqlString parstr = new ParameterizedSparqlString(
                    query.toString());
            for (final Var var : match_vars)
            {
                if (q.get(var) instanceof Literal)
                {
                    parstr.setLiteral(var.toString(), (Literal) q.get(var));
                }
                if (q.get(var) instanceof Resource)
                {
                    parstr.setIri(var.toString(), q.get(var).toString()); // NOTE
                }
            }
            // Create a singleton for the element then do a join with the
            // service call.
            TreeSet<BindingHashMap> singleton = new TreeSet<BindingHashMap>(new BindingHashMapComparator());
            singleton.add(q);
            TreeSet<BindingHashMap> results = service(endpoint, parstr.asQuery());
            // Add the results together
            ret.addAll(results);
        }
        return ret;
    }

    // Execute SERVICE query with a symmetric hash join
    public static TreeSet<BindingHashMap> serviceSymmetricHash(String endpoint,
            Query query, TreeSet<BindingHashMap> bindings, ArrayList<Var> match_vars)
    {
        TreeSet<BindingHashMap> results = service(endpoint, query);
        return Operations.join(bindings, results, match_vars);
    }

    // Adds a FILTER to expr, that only selects elements that match the
    // bindings.
    public static ElementGroup filter(TreeSet<BindingHashMap> bindings,
            ArrayList<Var> match_vars, ElementGroup expr)
    {
        assert (match_vars != null);
        // Create the filtering expression:
        Expr filter_expr = null;
        for (BindingHashMap q : bindings)
        {
            Expr fexpr = matchExpr(q, match_vars);
            if (filter_expr == null)
                filter_expr = fexpr;
            else
                filter_expr = new E_LogicalOr(filter_expr, fexpr);
        }
        // Add the filter_expr to the query: // NOTE: watch!
        ElementGroup body = expr;
        if (filter_expr != null)
        {
            ElementFilter filter = new ElementFilter(filter_expr);
            body = new ElementGroup();
            body.addElement(expr);
            body.addElement(filter);
        }
        return body;
    }

    // Creates a UNION of copies of expr, one for every binding with its filter.
    public static ElementGroup union(TreeSet<BindingHashMap> bindings,
            ArrayList<Var> match_vars, ElementGroup expr)
    {
        assert (match_vars != null);
        // Create the big query
        ElementGroup body = new ElementGroup();
        for (BindingHashMap q : bindings)
        {
            ElementGroup part = new ElementGroup();
            part.addElement(expr);
            //
            Expr fexpr = matchExpr(q, match_vars);
            ElementFilter filter = new ElementFilter(fexpr);
            part.addElement(filter);
            //
            if (body.isEmpty())
            {
                body.addElement(part);
            }
            else
            {
                ElementUnion uni = new ElementUnion(part);
                body.addElement(uni);
            }
        }
        return body;
    }

    // Creates an expression that matches if has the same values than q on
    // the match_vars.
    private static Expr matchExpr(BindingHashMap q, ArrayList<Var> match_vars)
    {
        assert (match_vars != null);
        Expr match_expr = null;
        for (Var var : match_vars)
        {
            Expr expr = new E_Equals(new ExprVar(var),
                    new NodeValueNode(q.get(var)));
            if (match_expr == null)
                match_expr = expr;
            else
                match_expr = new E_LogicalAnd(match_expr, expr);
        }
        return match_expr;
    }
}
