package org.jam.runtime;

import java.util.ArrayList;

/**
 * This class is used to start up classes with threads and main()
 * @author joe
 *
 */
public class StartUp 
{
    private enum Type
    {
        MAIN,
        THREAD
    }
    
    private String className;
    private Type type;
    
    private static ArrayList<RunThread> programs = new ArrayList<RunThread>();

    private StartUp(String name, Type type)
    {
        className = name;
        this.type = type;
    }

    public final static void runMain(String clsName)
    {
        programs.add(null);
    }
    
    public final static void runThread(String clsName)
    {
        programs.add(new RunThread(clsName));
    }
    
    public final static void run()
    {
        int size = programs.size();
        for(int index=0; index < size; index++)
        {
            RunThread program = programs.get(index);
            program.run();
        }
    }
}
