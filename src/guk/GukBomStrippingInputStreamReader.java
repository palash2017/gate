/*
 *  Copyright (c) 1995-2010, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *  
 *  Benson Margulies 28/07/2010
 *
 *  $Id$
 */

package guk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

/**
 * <p>StreamReader that removes the Unicode BOM, even when Sun/Oracle is
 * too lazy to do so. Since a buffer is required, and since most of GATE
 * was coded to use BufferedReaders around the InputStreamReader, this
 * 'isa' BufferedReader.</p>
 *
 * <p>Note that there are differences in exception behaviour on the
 * different InputStreamReader constructors, so this has to be careful
 * to call the right one.</p>
 * 
 * <p><b>Note</b> This class is a copy of BomStrippingInputStreamReader
 * from gate.util.  GUK cannot reference gate.util directly at runtime
 * due to classloader issues, and we do not want to force GATE embedded
 * to depend on GUK, therefore copying the class was considered the
 * least bad option.  If the gate.util class is modified, this class
 * should also be modified to match.</p>
 *
 */
public class GukBomStrippingInputStreamReader extends BufferedReader {
  private IOException pendingConstructionException;

  private boolean pendingEOF;

  private boolean pendingChecked;

  public GukBomStrippingInputStreamReader(InputStream in) {
    this(new InputStreamReader(in));
  }

  public GukBomStrippingInputStreamReader(InputStream in, String charsetName)
          throws UnsupportedEncodingException {
    this(new InputStreamReader(in, charsetName));
  }

  public GukBomStrippingInputStreamReader(InputStream in, String charsetName,
          int bufferSize) throws UnsupportedEncodingException {
    this(new InputStreamReader(in, charsetName), bufferSize);
  }

  public GukBomStrippingInputStreamReader(InputStream in, Charset cs) {
    this(new InputStreamReader(in, cs));
  }

  public GukBomStrippingInputStreamReader(InputStream in, int bufferSize) {
    this(new InputStreamReader(in), bufferSize);
  }

  public GukBomStrippingInputStreamReader(InputStream in, CharsetDecoder dec,
          int bufferSize) {
    this(new InputStreamReader(in, dec), bufferSize);
  }

  private GukBomStrippingInputStreamReader(InputStreamReader isr, int bufferSize) {
    super(isr, bufferSize);
    stripBomIfPresent();
  }

  private GukBomStrippingInputStreamReader(InputStreamReader isr) {
    super(isr);
    stripBomIfPresent();
  }

  public GukBomStrippingInputStreamReader(InputStream in, CharsetDecoder dec) {
    super(new InputStreamReader(in, dec));
    stripBomIfPresent();
  }

  /**
   * Checks whether the first character is BOM and positions the input stream 
   * past it, if that's the case.
   */
  private void stripBomIfPresent() {
    try {
      super.mark(1);
      int firstChar = super.read();
      if(firstChar == -1) {
        pendingEOF = true; /*
                            * If we hit EOF, note to return it from next
                            * call.
                            */
      }
      else if(firstChar != 0xfeff) {
        super.reset(); /* if we read non-BOM, push back */
      }
      /* otherwise leave it consumed */

    }
    catch(IOException e) {
      pendingConstructionException = e;
    }
  }

  public int hashCode() {
    return super.hashCode();
  }

  public boolean equals(Object obj) {
    return super.equals(obj);
  }

  private boolean checkPending() throws IOException {
    if(!pendingChecked) {
      if(pendingEOF) {
        return true;
      }
      else if(pendingConstructionException != null) {
        throw pendingConstructionException;
      }
      pendingChecked = true;
    }
    return false;
  }

  public int read(CharBuffer target) throws IOException {
    if(checkPending()) {
      return -1;
    }
    return super.read(target);
  }

  public int read(char[] cbuf) throws IOException {
    if(checkPending()) {
      return -1;
    }
    return super.read(cbuf);
  }

  public int read() throws IOException {
    if(checkPending()) {
      return -1;
    }
    return super.read();
  }

  public int read(char[] cbuf, int off, int len) throws IOException {
    if(checkPending()) {
      return -1;
    }
    return super.read(cbuf, off, len);
  }

  public String readLine() throws IOException {
    if(checkPending()) {
      return null;
    }
    return super.readLine();
  }

  public long skip(long n) throws IOException {
    if(checkPending()) {
      return 0;
    }
    return super.skip(n);
  }

  public boolean ready() throws IOException {
    if(checkPending()) {
      return false;
    }
    return super.ready();
  }

  public boolean markSupported() {
    return super.markSupported();
  }

  public void mark(int readAheadLimit) throws IOException {
    checkPending();
    super.mark(readAheadLimit);
  }

  public void reset() throws IOException {
    checkPending();
    super.reset();
  }

  public void close() throws IOException {
    // go ahead and close on this call even if we have an IOException
    // sitting around.
    super.close();
  }

}
