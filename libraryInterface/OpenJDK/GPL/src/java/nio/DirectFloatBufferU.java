/*
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

// -- This file was mechanically generated: Do not edit! -- //

package java.nio;

import java.io.FileDescriptor;

import org.jikesrvm.runtime.Memory;
import org.vmmagic.unboxed.Address;

import sun.misc.Cleaner;
import sun.misc.Unsafe;
import sun.misc.VM;
import sun.nio.ch.DirectBuffer;


class DirectFloatBufferU

    extends FloatBuffer



    implements DirectBuffer
{



    // Cached array base offset
    private static final long arrayBaseOffset = 0;

    // Cached unaligned-access capability
    protected static final boolean unaligned = Bits.unaligned();

    // Base address, used in all indexing calculations
    // NOTE: moved up to Buffer.java for speed in JNI GetDirectBufferAddress
    //    protected long address;

    // An object attached to this buffer. If this buffer is a view of another
    // buffer then we use this field to keep a reference to that buffer to
    // ensure that its memory isn't freed before we are done with it.
    private final Object att;

    public Object attachment() {
        return att;
    }






































    public Cleaner cleaner() { return null; }
















































































    // For duplicates and slices
    //
    DirectFloatBufferU(DirectBuffer db,         // package-private
                               int mark, int pos, int lim, int cap,
                               int off)
    {

        super(mark, pos, lim, cap);
        address = db.address() + off;



        att = db;



    }

    public FloatBuffer slice() {
        int pos = this.position();
        int lim = this.limit();
        assert (pos <= lim);
        int rem = (pos <= lim ? lim - pos : 0);
        int off = (pos << 2);
        assert (off >= 0);
        return new DirectFloatBufferU(this, -1, 0, rem, rem, off);
    }

    public FloatBuffer duplicate() {
        return new DirectFloatBufferU(this,
                                              this.markValue(),
                                              this.position(),
                                              this.limit(),
                                              this.capacity(),
                                              0);
    }

    public FloatBuffer asReadOnlyBuffer() {

        return new DirectFloatBufferRU(this,
                                           this.markValue(),
                                           this.position(),
                                           this.limit(),
                                           this.capacity(),
                                           0);



    }



    public long address() {
        return address;
    }

    private long ix(int i) {
        return address + (i << 2);
    }

    public float get() {
        return (Address.fromLong(ix(nextGetIndex())).loadFloat());
    }

    public float get(int i) {
        return (Address.fromLong(ix(checkIndex(i))).loadFloat());
    }







    public FloatBuffer get(float[] dst, int offset, int length) {

        if ((length << 2) > Bits.JNI_COPY_TO_ARRAY_THRESHOLD) {
            checkBounds(offset, length, dst.length);
            int pos = position();
            int lim = limit();
            assert (pos <= lim);
            int rem = (pos <= lim ? lim - pos : 0);
            if (length > rem)
                throw new BufferUnderflowException();


            if (order() != ByteOrder.nativeOrder())
                Bits.copyToIntArray(ix(pos), dst,
                                          offset << 2,
                                          length << 2);
            else

                Bits.copyToArray(ix(pos), dst, arrayBaseOffset,
                                 offset << 2,
                                 length << 2);
            position(pos + length);
        } else {
            super.get(dst, offset, length);
        }
        return this;



    }



    public FloatBuffer put(float x) {

    	Address.fromLong(ix(nextPutIndex())).store(x);
        return this;



    }

    public FloatBuffer put(int i, float x) {

    	Address.fromLong(ix(checkIndex(i))).store(x);
        return this;



    }

    public FloatBuffer put(FloatBuffer src) {

        if (src instanceof DirectFloatBufferU) {
            if (src == this)
                throw new IllegalArgumentException();
            DirectFloatBufferU sb = (DirectFloatBufferU)src;

            int spos = sb.position();
            int slim = sb.limit();
            assert (spos <= slim);
            int srem = (spos <= slim ? slim - spos : 0);

            int pos = position();
            int lim = limit();
            assert (pos <= lim);
            int rem = (pos <= lim ? lim - pos : 0);

            if (srem > rem)
                throw new BufferOverflowException();
//            Bits.copyMemory(sb.ix(spos), ix(pos), srem << 2);
            Memory.aligned32Copy(Address.fromLong(ix(pos)), Address.fromLong(sb.ix(spos)), srem<<2);
            sb.position(spos + srem);
            position(pos + srem);
        } else if (src.hb != null) {

            int spos = src.position();
            int slim = src.limit();
            assert (spos <= slim);
            int srem = (spos <= slim ? slim - spos : 0);

            put(src.hb, src.offset + spos, srem);
            src.position(spos + srem);

        } else {
            super.put(src);
        }
        return this;



    }

    public FloatBuffer put(float[] src, int offset, int length) {

        if ((length << 2) > Bits.JNI_COPY_FROM_ARRAY_THRESHOLD) {
            checkBounds(offset, length, src.length);
            int pos = position();
            int lim = limit();
            assert (pos <= lim);
            int rem = (pos <= lim ? lim - pos : 0);
            if (length > rem)
                throw new BufferOverflowException();


            if (order() != ByteOrder.nativeOrder())
                Bits.copyFromIntArray(src, offset << 2,
                                            ix(pos), length << 2);
            else

                Bits.copyFromArray(src, arrayBaseOffset, offset << 2,
                                   ix(pos), length << 2);
            position(pos + length);
        } else {
            super.put(src, offset, length);
        }
        return this;



    }

    public FloatBuffer compact() {

        int pos = position();
        int lim = limit();
        assert (pos <= lim);
        int rem = (pos <= lim ? lim - pos : 0);

//        Bits.copyMemory(ix(pos), ix(0), rem << 2);
        Memory.aligned32Copy(Address.fromLong(ix(0)), Address.fromLong(ix(pos)), rem<<2);
        position(rem);
        limit(capacity());
        discardMark();
        return this;



    }

    public boolean isDirect() {
        return true;
    }

    public boolean isReadOnly() {
        return false;
    }















































    public ByteOrder order() {





        return ((ByteOrder.nativeOrder() != ByteOrder.BIG_ENDIAN)
                ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);

    }
}
