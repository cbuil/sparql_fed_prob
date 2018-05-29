package ops;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

import org.apache.jena.graph.Node;
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
    	// System.out.println("b: "+b);
        // If there aren't things, return null.
        if (a==null || a.size() == 0)
            return null;
        if (b==null || b.size() == 0)
            return null;
        // Get the vars only present in b:
        VarComparator varComp = new VarComparator();
        TreeSet<Var> b_only_vars = new TreeSet<Var>(varComp);
        Iterator<Var> itb = b.first().vars();
        while (itb.hasNext())
            b_only_vars.add(itb.next());
        b_only_vars.removeAll(joinvars);
        // Save in a HashMap with key the values of the common variables:
        HashMap<ArrayList<Node>, ArrayList<BindingHashMap>> b_bind =
            new HashMap<ArrayList<Node>, ArrayList<BindingHashMap>>();
        for (BindingHashMap qb : b)
        {
            ArrayList<Node> key = new ArrayList<Node>();
            for (Var varname : joinvars)
                key.add((Node) qb.get(varname));
            ArrayList<BindingHashMap> current_list = b_bind.get(key);
            if (current_list == null) {
                current_list = new ArrayList<BindingHashMap>();
                b_bind.put(key, current_list);
            }
            current_list.add(qb);
        }
        // Create new binding for each matching pair of bindings with the same
        // values for the common variables.
        TreeSet<BindingHashMap> res = new TreeSet<BindingHashMap>(new BindingHashMapComparator());
        for (BindingHashMap qa : a)
        {
            ArrayList<Node> key = new ArrayList<Node>();
            for (Var varname : joinvars)
                key.add((Node) qa.get(varname));
            //
            ArrayList<BindingHashMap> current = b_bind.get(key); 
            if(current != null) {
	            for (BindingHashMap qb : current)
	            {
	                BindingHashMap new_binding = new BindingHashMap(qa);
	                for (Var varname : b_only_vars)
	                {
	                    new_binding.add(varname, qb.get(varname));
	                }
	                res.add(new_binding);
	            }
            }
        }
        //
        return res;
    }

}
