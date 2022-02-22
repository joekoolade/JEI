/*
 * Created on Oct 14, 2004
 *
 * Copyright (C) Joe Kulig, 2004
 * All rights reserved.
 * 
 */
package org.jam.board.pc;

import org.jam.interfaces.Timer;
import org.jam.util.PriorityQueue;
import org.jikesrvm.VM;
import org.jikesrvm.runtime.Magic;
import org.jikesrvm.runtime.Time;
import org.jikesrvm.scheduler.RVMThread;
import org.jikesrvm.scheduler.ThreadQueue;
import org.vmmagic.pragma.NonMoving;
import org.vmmagic.unboxed.Address;

/**
 * @author joe
 *
 */
@NonMoving
public class PcSystemTimer
implements Timer
{

    public long tick;                                        // in milliseconds
    private static final int  sourceFreq     = 1193180;                    // i82c54 source frequency is 1.193180 Mhz
    private static final int  ticksPerSecond = 1000;
    public int  counterDivisor = sourceFreq / ticksPerSecond;
    public int  overflow;                                    // in nanoseconds
    public int BOLT = 10;   // schedule new process
//    private int stack[];
    Address stackTop;
    private final static int STACK_SIZE = 512;
    private static final int TIMERTICKSPERNSECS = 1000000;
    private static final boolean trace = false;
	private static final boolean trace1 = false;
	private static final boolean timerTrace = false;
    private PriorityQueue timerQueue;
    private ThreadQueue threadQueue;
    
    /*
     * how many ticks to wait to reschedule
     */
    public int  scheduleTick = 20;

    public PcSystemTimer()
    {
        /*
         * Set the time for the PC system timer. Default is an interrupt every 1.193 ms.
         */
        I82c54.counter0(I82c54.MODE2, counterDivisor);
        /*
         * Allocate irq handler stack
         */
//        stack = MemoryManager.newNonMovingIntArray(STACK_SIZE); // new int[STACK_SIZE];
        /*
         * Put in the sentinel
         */
//        stack[STACK_SIZE-1] = 0;    // IP = 0
//        stack[STACK_SIZE-2] = 0;    // FP = 0
//        stack[STACK_SIZE-3] = 0;    // cmid = 0
        
        /*
         * On a stack switch, the new stack is popped so need to count for this
         * in the stackTop field. This space will contain the interrupted thread's
         * stack pointer.
         */
//        stackTop = Magic.objectAsAddress(stack).plus((STACK_SIZE-4)<<2);
        timerQueue = new PriorityQueue();
        threadQueue = new ThreadQueue("pcSystemTimer");
    }

    public final long getTime()
    {
        return tick;
    }

    public Address getHandlerStack()
    {
        
        return stackTop;
    }
    /*
     * timer interrupt handler.
     * 
     * context array is builtin into the stack.
     */
    public void handler()
    {
        tick++;
        overflow += 193180;
        if (overflow >= 1000000)
        {
            tick++;
            overflow -= 1000000;
        }

        //if (RVMThread.bootThread.isTerminated()==false) return;
        if(VM.booting==true) return;
        if(trace && (tick % 100) == 0) VM.sysWrite('H');
        checkTimers();
        schedule();
//        Platform.masterPic.eoi();
    }
    
    final private void schedule()
    {
        /*
         * If there are no threads waiting run then
         * just keep running the current one
         */
        if(Platform.scheduler.noRunnableThreads())
        {
            return;
        }
        if(trace) VM.sysWrite('R');
        /*
         * We took a timer tick and there are threads that are runnable.
         */
        
        /*
         * Current thread has had its time allotment so put it on queue 
         * and schedule a new thread
         */
        if(((int)tick % BOLT) == 0)
        {
            VM.sysWrite('R');
            RVMThread currentThread = Magic.getThreadRegister();
//            Platform.scheduler.addThread(currentThread);
            currentThread.enableYieldpoints();
//            RVMThread next = Platform.scheduler.nextThread();
//            if(next == null)
//            {
//              VM.sysWriteln("Nothing to run!");
//              return;
//            }
//            next.enableYieldpoints();
        }
    }
    
    private static int lastCheck = 0;
    private void checkTimers()
    {
    	lastCheck++;
//    	if(trace) VM.sysWrite('C');
        if(timerQueue.isEmpty())
        {
            return;
        }
        long timerExpiration = timerQueue.rootValue();
        long time = Time.nanoTime();
//		if (timerTrace && (lastCheck % 100) == 0) 
//		{
//			VM.sysWrite("\nT", time);
//			VM.sysWriteln("|", timerExpiration);
//		}
//        if(trace) VM.sysWrite('T');
        if(time < timerExpiration || timerQueue.isEmpty())
        {
            return;
        }
        
        /*
         * Remove the thread from the timer and schedule it
         */
        RVMThread thread = (RVMThread) timerQueue.deleteMin();
        if(thread == null)
        {
            VM.sysWrite("timer expire: ", timerExpiration);
            VM.sysFail(timerQueue.toString());
        }
        if(timerTrace) { VM.sysWrite("\nTimer expired! ", timerExpiration); VM.sysWriteln(" ", thread.threadSlot); }
        Platform.scheduler.addThread(thread);
    }
    
    /**
     * Set timer for a thread
     * @param time_ns time to sleep in nanoseconds.
     */
    public void startTimer(long time_ns)
    {
        long timerTicks;
        
        /*
         * convert to ticks (milliseconds)
         */
//        timerTicks = time_ns / TIMERTICKSPERNSECS;
//        if(timerTrace)
//        {
//          VM.sysWrite("\nT0: ", time_ns);
//          VM.sysWriteln("/", RVMThread.getCurrentThread().getThreadSlot());
//        }
        /*
         * set expiration time and put on the queue
         */
//        Magic.disableInterrupts();
        timerQueue.insert(time_ns, RVMThread.getCurrentThread());
//        VM.sysWriteln("startTimer: ", timerQueue.toString());
//        VM.sysWrite("/t/", RVMThread.getCurrentThreadSlot());
//        Magic.enableInterrupts();
        /*
         * give it up and schedule a new thread
         */
        Platform.scheduler.schedule();
    }
    
    /**
     * Remove timer from queue and return thread
     * @param timeKey
     * @return
     */
    public RVMThread removeTimer(long timeKey)
    {
      RVMThread t =  (RVMThread) timerQueue.remove(timeKey);
//      if(timerTrace)
//      {
//        VM.sysWrite("\nT1: ", timeKey);
//        VM.sysWriteln("/", t.getThreadSlot());
//      }
      return t;
    }
    
    /**
     * Remove timer associated with thread
     */
    public void removeTimer(RVMThread thr)
    {
      
    }
}