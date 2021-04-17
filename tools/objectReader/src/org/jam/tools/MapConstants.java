package org.jam.tools;

import java.math.BigInteger;

public class MapConstants extends MapCommon
{
    private static final boolean DEBUG = false;
    private String fullDefiningType;    // class that field is defined in
    private String keyDefiningType;
    private String name;
    private String fullFieldType;            // class of the field
    private String keyFieldType;
    private long intValue;
    private double floatValue;

    public MapConstants(String rvmMapLine[])
    {
        super(Integer.parseInt(rvmMapLine[0]), new BigInteger(rvmMapLine[1].substring(2), 16).intValue());
        parseDetail(rvmMapLine[4]);
        try
        {
            intValue = Long.decode(rvmMapLine[3]);
        }
        catch (NumberFormatException e)
        {
            // Must be a negative long so lets use big integer
            intValue = new BigInteger(rvmMapLine[3].substring(2), 16).longValue();
        }
    }

    private void parseDetail(String details)
    {
        String[] tokens = details.split("[<>\\. ]+");
        try
        {
            fullFieldType = getClass(tokens[5]);
            keyFieldType = fullFieldType.substring(fullFieldType.lastIndexOf('.')+1);
            fullDefiningType = getClass(tokens[3]);
            keyDefiningType = fullDefiningType.substring(fullDefiningType.lastIndexOf('.')+1);
            name = tokens[4];
        } catch (Exception e)
        {
            fullFieldType = "NONE";
            fullDefiningType = "NONE";
            keyDefiningType = "NONE";
            name = "NONE";
        }
        if(DEBUG) System.out.println(keyDefiningType+"."+name);
    }

    private String getClass(String typeString)
    {
        int index=0;
        
        if(typeString.charAt(index)!='L') return typeString;
        return typeString.substring(index+1, typeString.lastIndexOf(';')).replace('/', '.');
    }

    public String getFullDefiningType()
    {
        return fullDefiningType;
    }

    public String getKeyDefiningType()
    {
        return keyDefiningType;
    }

    public String getName()
    {
        return name;
    }

    public String getFullFieldType()
    {
        return fullFieldType;
    }

    public String getKeyFieldType()
    {
        return keyFieldType;
    }

    public long getIntValue()
    {
        return intValue;
    }

    public double getFloatValue()
    {
        return floatValue;
    }
    
    public String getKey()
    {
        return keyDefiningType+"."+name;
    }
}
