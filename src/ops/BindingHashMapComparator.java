package ops;

import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.BindingHashMap;

class BindingHashMapComparator implements Comparator<BindingHashMap> {

	@Override
	public int compare(BindingHashMap o1, BindingHashMap o2) {
		TreeSet<Var> orderedvars = new TreeSet<Var>(new Comparator<Var>() {
			@Override
			public int compare(Var v1, Var v2) {
				return v1.toString().compareTo(v2.toString());
			}
		});
		Iterator<Var> vars = o1.vars();
		while(vars.hasNext()){
			orderedvars.add(vars.next());
		}

		int res = o1.size() - o2.size();
		if(res == 0) {
			for(Var var: orderedvars){
				if(o2.contains(var)){
					res=o1.get(var).toString().compareTo(o2.get(var).toString());
					if(res != 0 ) break;
				}
			}
		}
		return res;
	}
}
