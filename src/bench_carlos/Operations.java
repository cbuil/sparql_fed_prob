package bench_carlos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.BindingHashMap;

public class Operations
{
    // Get the common vars:
    public static ArrayList<Var> commonVars(TreeSet<BindingHashMap> a, TreeSet<BindingHashMap> b){
        // If there aren't things, return null.
        if (a==null || a.size() == 0)
            return null;
        if (b==null || b.size() == 0)
            return null;
        //
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
        return varnames;
    }

    // A symmetric hash join
    public static TreeSet<BindingHashMap> join(
        TreeSet<BindingHashMap> a, TreeSet<BindingHashMap> b, ArrayList<Var> joinvars)
    {
        // If there aren't things, return null.
        if (a==null || a.size() == 0)
            return null;
        if (b==null || b.size() == 0)
            return null;
        // Get the vars only present in b:
        TreeSet<Var> b_only_vars = new TreeSet<Var>();
        Iterator<Var> itb = b.first().vars();
        while (itb.hasNext())
            b_only_vars.add(itb.next());
        b_only_vars.removeAll(joinvars);
        // Save in a HashMap with key the values of the common variables:
        HashMap<ArrayList<RDFNode>, ArrayList<BindingHashMap>> b_bind =
            new HashMap<ArrayList<RDFNode>, ArrayList<BindingHashMap>>();
        for (BindingHashMap qb : b)
        {
            ArrayList<RDFNode> key = new ArrayList<RDFNode>();
            for (Var varname : joinvars)
                key.add((RDFNode) qb.get(varname));
            ArrayList<BindingHashMap> current_list = b_bind.get(key);
            if (current_list == null)
                current_list = new ArrayList<BindingHashMap>();
            current_list.add(qb);
            b_bind.put(key, current_list);
        }
        // Create new binding for each matching pair of binding with the same
        // values for the common variables.
        TreeSet<BindingHashMap> res = new TreeSet<BindingHashMap>(new BindingHashMapComparator());
        for (BindingHashMap qa : a)
        {
            ArrayList<RDFNode> key = new ArrayList<RDFNode>();
            for (Var varname : joinvars)
                key.add((RDFNode) qa.get(varname));
            //
            for (BindingHashMap qb : b_bind.get(key))
            {
                BindingHashMap new_binding = new BindingHashMap(qa);
                for (Var varname : b_only_vars)
                {
                    new_binding.add(varname, qb.get(varname));
                }
                res.add(new_binding);
            }
        }
        //
        return res;
    }

}
