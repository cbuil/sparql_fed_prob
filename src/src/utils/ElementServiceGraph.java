package utils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.jena.sparql.syntax.ElementService;

class ElementServiceGraph
{
    // Map of adjacency lists for each node
    HashMap<ElementService, LinkedList<ElementService>> adj;

    public ElementServiceGraph(List<ElementService> serviceList)
    {
        // your node labels are consecutive integers starting with one.
        // to make the indexing easier we will allocate an array of adjacency
        // one element larger than necessary
        adj = new HashMap<ElementService, LinkedList<ElementService>>();
        for (int i = 0; i < serviceList.size(); ++i)
        {
            adj.put(serviceList.get(i), new LinkedList<ElementService>());
        }
    }

    public void addNeighbor(ElementService v1, ElementService v2)
    {
        adj.get(v1).add(v2);
    }

    public List<ElementService> getNeighbors(ElementService v)
    {
        return adj.get(v);
    }

}