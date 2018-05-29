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
        return var1.getName().compareTo(var2.getName());
    }
}
