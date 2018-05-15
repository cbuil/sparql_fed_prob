package ops;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.core.ResultBinding;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingHashMap;

public class Operations
{

    // A symmetric hash join
    public static TreeSet<Binding> join(TreeSet<Binding> a, TreeSet<Binding> b)
    {
        // If there aren't things to join, return null.
        if (a.size() == 0)
            return null;
        if (b.size() == 0)
            return null;
        // Get the common vars used for matching:
        TreeSet<Var> vars_a = new TreeSet<Var>();
        Iterator<Var> ita = a.first().vars();
        while (ita.hasNext())
            vars_a.add(ita.next());
        //
        TreeSet<Var> vars_b = new TreeSet<Var>();
        Iterator<Var> itb = b.first().vars();
        while (itb.hasNext())
            vars_b.add(itb.next());
        //
        TreeSet<Var> common_vars = new TreeSet<Var>();
        common_vars.addAll(vars_a);
        common_vars.retainAll(vars_b);
        ArrayList<Var> varnames = new ArrayList<Var>(common_vars);
        // Get the vars only present in b:
        TreeSet<Var> b_only_vars = new TreeSet<Var>(varnames);
        common_vars.removeAll(vars_a);
        // Save in a HashMap with key the values of the common variables:
        HashMap<ArrayList<RDFNode>, ArrayList<Binding>> b_bind = new HashMap<ArrayList<RDFNode>, ArrayList<Binding>>();
        for (Binding qb : b)
        {
            ArrayList<RDFNode> key = new ArrayList<RDFNode>();
            for (Var varname : varnames)
                key.add((RDFNode) qb.get(varname));
            ArrayList<Binding> current_list = b_bind.get(key);
            if (current_list == null)
                current_list = new ArrayList<Binding>();
            current_list.add(qb);
            b_bind.put(key, current_list);
        }
        // Create new binding for each matching pair of binding with the same
        // values for the common variables.
        TreeSet<Binding> res = new TreeSet<Binding>();
        for (Binding qa : a)
        {
            ArrayList<RDFNode> key = new ArrayList<RDFNode>();
            for (Var varname : varnames)
                key.add((RDFNode) qa.get(varname));
            //
            for (Binding qb : b_bind.get(key))
            {
                // CARLOS: cambio de Binding a BindingHashMap, es la clase
                // concreta de la clase abstracta Binding
                BindingHashMap new_binding = (BindingHashMap) qa; // TODO: I
                                                                  // MUST DO A
                                                                  // COPY!
                for (Var vari : b_only_vars)
                {
                    new_binding.add(vari, qb.get(vari));
                }
                res.add(new_binding);
            }
        }
        //
        return res;
    }

}
