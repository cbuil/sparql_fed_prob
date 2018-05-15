package utils;

import java.util.List;

import org.apache.jena.sparql.syntax.Element;

public class SplitQuery
{

    private Element _p1;
    private String _p1EP;
    private Element _p2;
    private String _p2EP;
    private Element _p3;
    private String _p3EP;
    private int _batch;
    private List<String> _selectVars;

    public SplitQuery(Element p1, String p1EP, Element p2, String p2EP)
    {
        _p1 = p1;
        _p1EP = p1EP;
        _p2 = p2;
        _p2EP = p2EP;

    }

    public SplitQuery(Element p1, String p1EP, Element p2, String p2EP,
            List<String> resultVars)
    {
        _p1 = p1;
        _p1EP = p1EP;
        _p2 = p2;
        _p2EP = p2EP;
        _selectVars = resultVars;
    }

    public SplitQuery(Element p1, String p1EP, Element p2, String p2EP,
            Element p3, String p3EP)
    {
        _p1 = p1;
        _p1EP = p1EP;
        _p2 = p2;
        _p2EP = p2EP;
        _p3 = p3;
        _p3EP = p3EP;
    }

    public SplitQuery(Element p1, String p1EP, Element p2, String p2EP,  Element p3, String p3EP,
            List<String> resultVars)
    {
        _p1 = p1;
        _p1EP = p1EP;
        _p2 = p2;
        _p2EP = p2EP;
        _selectVars = resultVars;
        _p3 = p3;
        _p3EP = p3EP;
    }

    public Element getP1()
    {
        return _p1;
    }

    public Element getP2()
    {
        return _p2;
    }

    public String getP2Endpoint()
    {
        return _p2EP;
    }
    
    public Element getP3()
    {
        return _p3;
    }

    public String getP3Endpoint()
    {
        return _p3EP;
    }

    public String getP1Endpoint()
    {
        return _p1EP;
    }

    @Override
    public String toString()
    {
        return "SplitQuery:\n___\nP1 (EP: " + _p1EP + ")\n" + _p1 + "\nP2 (EP: "
                + _p2EP + "\n" + _p2 + ")\n___";
    }

    public void setBatch(int batch)
    {
        _batch = batch;

    }

    public int getBatch()
    {
        return _batch;
    }

    public List<String> getSelectVars()
    {
        return _selectVars;
    }
}
