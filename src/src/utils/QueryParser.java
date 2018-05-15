package utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementOptional;
import org.apache.jena.sparql.syntax.ElementService;
import org.apache.jena.sparql.syntax.ElementUnion;

public class QueryParser
{

    public static SplitQuery splitQuery(String queryFile, String localEP)
            throws FileNotFoundException
    {
        // read query file
        String queryString = new Scanner(new File(queryFile))
                .useDelimiter("\\Z").next();
        // transform into Jena Query object
        Query q = QueryFactory.create(queryString);

        System.out.println("______\nParsing Query\n______\n" + q);

        boolean localEPparsed = false;
        Element p1 = null, p2 = null, p3 = null;
        String remoteEP = null;
        String remoteEP2 = null;
        ElementGroup qBody = (ElementGroup) q.getQueryPattern();
        List<ElementService> serviceList = new ArrayList<>();
        int count = 0;
        for (Element e : qBody.getElements())
        {
            if (e instanceof ElementService)
            {
                ElementService es = (ElementService) e;
                serviceList.add(es);
                Node epURINode = es.getServiceNode();
                String epURI = epURINode.getURI();
                if (epURI.equals(localEP) & !localEPparsed)
                {
                    p1 = es.getElement();
                    localEPparsed = true;
                }
                else if (count < 2)
                {
                    p2 = es.getElement();
                    remoteEP = epURI;
                }
                if(count > 1){
                    p3 = es.getElement();
                    remoteEP2 = epURI;
                }
                count++;
            }
            else if (e instanceof ElementGroup)
            {
                ElementGroup eg = (ElementGroup) e;
                List<Element> groupElementsList = eg.getElements();
            }
            else if (e instanceof ElementOptional)
            {
                ElementOptional eo = (ElementOptional) e;
                Element rightSideOptional = eo.getOptionalElement();
            }
            else if (e instanceof ElementUnion)
            {
                ElementUnion eu = (ElementUnion) e;
                List<Element> unionElementsList = eu.getElements();
                ElementGroup eg = (ElementGroup) unionElementsList.get(0);
                ElementService es = (ElementService) eg.get(0);
                Node epURINode = es.getServiceNode();
                String epURI = epURINode.getURI();
                if (epURI.equals(localEP) & !localEPparsed)
                {
                    p1 = es.getElement();
                    localEPparsed = true;
                    ElementGroup eg2 = (ElementGroup) unionElementsList.get(1);
                    p2 = (ElementService) eg2.get(0);
                    remoteEP = epURI;
                }
                else
                {
                    ElementGroup eg2 = (ElementGroup) unionElementsList.get(1);
                    p1 = (ElementService) eg2.get(0);
                    p2 = es.getElement();
                    remoteEP = epURI;
                }
            }
        }
        SplitQuery sq = new SplitQuery(p1, localEP, p2, remoteEP, p3, remoteEP2, 
                q.getResultVars());
        System.out.println(sq);
        return sq;
    }

    public static String findJoinVar(List<Var> p1Vars, List<Var> p2Vars)
    {

        for (Var v : p1Vars)
        {
            if (p2Vars.contains(v))
                return v.getName();
        }
        return "";
    }

    public static Var findJoinVarObject(List<Var> p1Vars, List<Var> p2Vars)
    {

        for (Var v : p1Vars)
        {
            if (p2Vars.contains(v))
                return v;
        }
        return null;
    }

    public static List<String> findJoinVars(List<Var> p1Vars, List<Var> p2Vars)
    {
        List<String> joinVars = new ArrayList<String>();

        for (Var v : p1Vars)
        {
            if (p2Vars.contains(v))
                joinVars.add(v.getName());
        }
        return joinVars;
    }

    public static List<String> findServiceVars(List<String> selectVars,
            List<Var> p2Vars)
    {
        List<String> joinVars = new ArrayList<String>();

        for (Var v : p2Vars)
        {
            if (selectVars.contains(v.getName()))
                joinVars.add(v.getName());
        }
        return joinVars;
    }
}
