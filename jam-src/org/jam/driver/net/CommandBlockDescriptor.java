/**
 * Created on Oct 6, 2017
 *
 * Copyright (C) Joe Kulig, 2017
 * All rights reserved.
 */
package org.jam.driver.net;

import org.jikesrvm.VM;
import org.jikesrvm.classloader.Atom;
import org.jikesrvm.runtime.Magic;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.ObjectReference;
import org.vmmagic.unboxed.Offset;

/**
 * @author Joe Kulig
 *
 */
public class CommandBlockDescriptor {
  private final static int SIZE = 64;
  private static final boolean DEBUG_CONFIG = true;
  private static final boolean DEBUG_IASETUP = true;
  private static final boolean DEBUG_TX = true;

  private byte[] buffer;
  private Address bufferAddr;
  private CommandBlockDescriptor next,previous;
  
  private static final byte NOP = 0;
  private static final byte INTERNET_ADDR_SETUP = 1;
  private static final byte CONFIGURE = 2;
  private static final byte MULTICAST_ADDR_SETUP = 3;
  private static final byte TRANSMIT = 4;
  private static final byte LOAD_UCODE = 5;
  private static final byte DUMP = 6;
  private static final byte DIAGNOSE = 7;
  
  private static final byte      INTERRUPT            = 0x20;
  private static final byte      SUSPEND              = 0x40;
  private static final byte      EL                   = (byte) 0x80;

  private static final byte      COMPLETE             = (byte) 0x80;
  private static final byte      OK                   = 0x20;
  private static final byte SF = 0x8;
  private static final Offset LINK_OFFSET = Offset.zero().plus(4);
  
  public CommandBlockDescriptor()
  {
    buffer = new byte[SIZE];
    bufferAddr = Magic.objectAsAddress(buffer);
  }
  
  public CommandBlockDescriptor(int size)
  {
    buffer = new byte[size];
    bufferAddr = Magic.objectAsAddress(buffer);
  }
  /**
   * @param commandBlockDescriptor
   */
  public void link(CommandBlockDescriptor commandBlockDescriptor)
  {
    bufferAddr.store(commandBlockDescriptor.bufferAddr, LINK_OFFSET);
  }

  public void suspend()
  {
    buffer[3] |= SUSPEND;
  }
  
  public void unsuspend()
  {
    buffer[3] &= ~SUSPEND;
  }
  
  public void setEL()
  {
	  buffer[3] |= EL;
  }
  
  public void removeEL()
  {
	  buffer[3] &= ~EL;
  }
  /**
   * @param configureParameters  
   */
  public void configureParameters(byte[] configureParameters)
  {
    for(int i=0; i < configureParameters.length; i++)
    {
      buffer[i+8] = configureParameters[i];
    }
    buffer[2] = CONFIGURE;
    
    if(DEBUG_CONFIG)
    {
      for(int i=0; i<configureParameters.length; i++)
      {
        VM.sysWrite("config ", i); VM.sysWriteln(" = ", VM.intAsHexString(buffer[i+8]&0xFF));
      }
    }
  }

  /**
   * @param eeprom
   */
  public void configureMacAddress(short[] mac)
  {
    buffer[0] = 0;
    buffer[1] = 0;
    buffer[2] = INTERNET_ADDR_SETUP;
    buffer[3] = 0;
    buffer[8] = (byte) (mac[0] & 0xFF);
    buffer[9] = (byte) (mac[0] >> 8);
    buffer[10] = (byte) (mac[1] & 0xFF);
    buffer[11] = (byte) (mac[1] >> 8);
    buffer[12] = (byte) (mac[2] & 0xFF);
    buffer[13] = (byte) (mac[2] >> 8);
    buffer[14] = 0;
    buffer[15] = 0;
    
    if(DEBUG_IASETUP)
    {
      VM.sysWrite("IA ADDRESS: ");
      for(int i=8; i<14; i++)
      {
        VM.sysWrite(" ", VM.intAsHexString(buffer[i] & 0xFF));
      }
      VM.sysWriteln();
    }
  }

  
  /**
   * @return
   */
  public CommandBlockDescriptor previous()
  {
    return previous;
  }

  /**
   * Set the previous link with command block
   * @param cbd
   */
  public void previous(CommandBlockDescriptor cbd)
  {
    previous = cbd;
  }
  public CommandBlockDescriptor next()
  {
    return next;
  }

  /**
   * Set the next link with the command block
   * @param cbd
   */
  public void next(CommandBlockDescriptor cbd)
  {
    next = cbd;
  }

  /**
   * @return
   */
  public boolean notComplete()
  {
    return (buffer[1] & COMPLETE) == 0;
  }

  /**
   * @return
   */
  public boolean notOk()
  {
    return (buffer[1] & OK) == 0;
  }

  /**
   * @return
   */
  public byte getStatus()
  {
    return buffer[1];
  }

  public byte getControl()
  {
	  return buffer[3];
  }
  /**
   * @return integer value of command buffer address
   */
  public final int getScbPointer()
  {
    return Magic.objectAsAddress(buffer).toInt();
  }

  /**
   * @param packet
   */
  public void configureTransmitPacket(Packet packet)
  {
    bufferAddr.store(0, Offset.zero().plus(12));
	suspend();
    buffer[0] = 0;
    buffer[1] = 0;
    buffer[2] = TRANSMIT | SF;
    buffer[15] = 1;  // tbd count
    buffer[14] = (byte) 0x8;  // transmit threshold
    // setup the transmit buffer descriptor address
    bufferAddr.store(bufferAddr.plus(16).toInt(), Offset.zero().plus(8));
    // transmit buffer 0 address
    bufferAddr.store(packet.getPacketAddress().toInt(), Offset.zero().plus(16));
    bufferAddr.store(packet.getSize(), Offset.zero().plus(20));
    if(DEBUG_TX)
    {
	    VM.sysWrite("xmit packet: ", bufferAddr); 
	    VM.sysWrite(" ", packet.getAddress());
	    VM.sysWriteln(" ", packet.getSize());
	    printCbd();
//	    VM.hexDump(packet.getArray(), 0, packet.getSize());
    }
  }

  public byte getCmd()
  {
	  return buffer[2];
  }
  
    public boolean isComplete()
    {
        return (buffer[1] & COMPLETE) != 0;
    }
    
    public boolean isTransmit()
    {
    	return (buffer[2] & 0x7) == TRANSMIT;
    }
    public void cleanCbd()
    {
        bufferAddr.store(0, Offset.zero().plus(16));
    }

    public boolean hasBuffer()
    {
        return bufferAddr.loadInt(Offset.zero().plus(16)) != 0;
    }

    public int transmitBytes()
    {
        return bufferAddr.loadInt(Offset.zero().plus(20)) & 0x7FFF;
    }
  
    public void printCbd()
    {
    	VM.sysWriteln("cbd addr: ", bufferAddr);
    	VM.sysWriteln("cbd 0: ", VM.intAsHexString(bufferAddr.loadInt()));
    	VM.sysWriteln("cbd 1: ", bufferAddr.loadInt(Offset.zero().plus(4)));
    	VM.sysWriteln("cbd 2: ", bufferAddr.loadInt(Offset.zero().plus(8)));
    	if(isTransmit())
    	{
        	VM.sysWriteln("cbd 3: ", bufferAddr.loadInt(Offset.zero().plus(12)));
        	VM.sysWriteln("cbd 4: ", bufferAddr.loadInt(Offset.zero().plus(16)));
        	VM.sysWriteln("cbd 5: ", bufferAddr.loadInt(Offset.zero().plus(20)));
    	}
    }
}
