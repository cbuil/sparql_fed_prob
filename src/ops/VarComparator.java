package ops;

import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.BindingHashMap;

class VarComparator implements Comparator<Var>
{

    @Override
    public int compare(Var var1, Var var2)
    {
//        TreeSet<Var> orderedvars = new TreeSet<Var>(new Comparator<Var>()
//        {
//            @Override
//            public int compare(Var v1, Var v2)
//            {
//                return v1.toString().compareTo(v2.toString());
//            }
//        });

//        int res = var1.getName() - var2.getName();
        return var1.getName().compareTo(var2.getName());
    }
}
