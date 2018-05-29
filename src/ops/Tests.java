package ops;

import java.util.ArrayList;
import java.util.TreeSet;

import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.BindingHashMap;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;

public class Tests
{

    public static TreeSet<BindingHashMap> testValues(
        String ep1, Element p1, String ep2, Element p2)
    {
        // Create first query:
        Query q1 = QueryFactory.create();
        ElementGroup body1 = new ElementGroup();
        body1.addElement(p1);
        q1.setQuerySelectType();
        q1.setQueryPattern(body1);
        q1.setQueryResultStar(true);
        // Perform first query:
        TreeSet<BindingHashMap> r1 = Tasks.service(ep1,q1);
        // Create second query:
        Query q2 = QueryFactory.create();
        ElementGroup body2 = new ElementGroup();
        body1.addElement(p2);
        q2.setQuerySelectType();
        q2.setQueryPattern(body2);
        q2.setQueryResultStar(true);
        // Ask the second query asking by values, just match by the first variable
        Var join_var = r1.first().vars().next();
        ArrayList<Var> join_vars = new ArrayList<Var>();
        join_vars.add(join_var);
        TreeSet<BindingHashMap> r2 = Tasks.serviceWithValues(ep2,q2,r1,join_vars,0);
        // Return final bindings.
        return r2;
    }

    public static void main(String[] args) {

    }
}
