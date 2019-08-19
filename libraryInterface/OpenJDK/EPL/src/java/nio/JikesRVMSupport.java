/*
 *  This file is part of the Jikes RVM project (http://jikesrvm.org).
 *
 *  This file is licensed to You under the Eclipse Public License (EPL);
 *  You may not use this file except in compliance with the License. You
 *  may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/eclipse-1.0.php
 *
 *  See the COPYRIGHT.txt file distributed with this work for information
 *  regarding copyright ownership.
 */
package java.nio;

import org.vmmagic.unboxed.Address;

/**
 * Library support interface of Jikes RVM
 */
public class JikesRVMSupport {

  public static Address getDirectBufferAddress(Buffer buffer) {
    return Address.fromLong(((DirectByteBuffer)buffer).address());
  }

  public static ByteBuffer newDirectByteBuffer(Address address, long capacity) {
    return new DirectByteBuffer(address.toLong(), (int)capacity);
  }
}