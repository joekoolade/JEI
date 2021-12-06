package org.jam.tools;

public class Atom 
extends JObject
{
    private static final int ID_OFFSET = 8;     // Atom.id
    private static final int VAL_OFFSET = 0;    // Atom.val
    private static final int JTOC_OR_STR_OFFSET = -8;
    private int stringOrJtoc;
    private int id;
    private ByteArray  array;
    private byte[]  value;
    
    public Atom(MemoryReader memory, int address)
    {
        super(memory, address);
        if(isNull()) return;
        id = getInt(ID_OFFSET);
        array = new ByteArray(memory, getWord(VAL_OFFSET));
        value = array.array();
        stringOrJtoc = getWord(JTOC_OR_STR_OFFSET);
        // do range check on *(stringOrJtoc-4)
    }

    public String getString()
    {
        if(isNull()) return "NULL";
        return new String(value);
    }

}
