package com.pk.jsonmodel;

public class Premium
{
    private Integer finish;

    private Integer start;

    public Integer getFinish ()
    {
        return finish;
    }

    public void setFinish (Integer finish)
    {
        this.finish = finish;
    }

    public Integer getStart ()
    {
        return start;
    }

    public void setStart (Integer start)
    {
        this.start = start;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [finish = "+finish+", start = "+start+"]";
    }
}
