/*
 *  DocumentContentImpl.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 11/Feb/2000
 *
 *  $Id$
 */

package gate.corpora;

import java.util.*;
import java.net.*;
import java.io.*;

import gate.*;
import gate.annotation.*;
import gate.util.*;

/** Represents the commonalities between all sorts of document contents.
  */
public class DocumentContentImpl implements DocumentContent
{
  /** Debug flag */
  private static final boolean DEBUG = false;

  /** Buffer size for reading
   *  16k is 4 times the block size on most filesystems
   *  so it should be efficient for most cases
   *  */
  private static final int INTERNAL_BUFFER_SIZE  = 16*1024;

  /** Default construction */
  public DocumentContentImpl() {
    content = new String();
  } // default construction

  /** Contruction from URL and offsets. */
  public DocumentContentImpl(URL u, String encoding, Long start, Long end)
  throws IOException {

    int readLength = 0;
    char[] readBuffer = new char[INTERNAL_BUFFER_SIZE];

    BufferedReader uReader = null;
    StringBuffer buf = new StringBuffer();
    char c;
    long s = 0, e = Long.MAX_VALUE, counter = 0;
    if(start != null && end != null) {
      s = start.longValue();
      e = end.longValue();
    }

    if(encoding != null && !encoding.equalsIgnoreCase("")) {
      uReader = new BufferedReader(
        new InputStreamReader(u.openStream(), encoding), INTERNAL_BUFFER_SIZE
      );
    } else {
      uReader = new BufferedReader(
        new InputStreamReader(u.openStream()), INTERNAL_BUFFER_SIZE
      );
    };

    // 1. skip S characters
    uReader.skip(s);

    // 2. how many character shall I read?
    long toRead = e - s;

    // 3. read gtom source into buffer
    while (
      toRead > 0 &&
      (readLength = uReader.read(readBuffer, 0, INTERNAL_BUFFER_SIZE)) != -1
    ) {
      if (toRead <  readLength) {
        //well, if toRead(long) is less than readLenght(int)
        //then there can be no overflow, so the cast is safe
        readLength = (int)toRead;
      }

      buf.append(readBuffer, 0, readLength);
      toRead -= readLength;
    }

    // 4.close reader
    uReader.close();

    content = new String(buf);
  } // Contruction from URL and offsets

  /** Propagate changes to the document content. */
  void edit(Long start, Long end, DocumentContent replacement)
  {
    int s = start.intValue(), e = end.intValue();
    String repl = ((DocumentContentImpl) replacement).content;
    StringBuffer newContent = new StringBuffer(content);
    newContent.replace(s, e, repl);
    content = newContent.toString();
  } // edit(start,end,replacement)

  /** The contents under a particular span. */
  public DocumentContent getContent(Long start, Long end)
    throws InvalidOffsetException
  {
    if(! isValidOffsetRange(start, end))
      throw new InvalidOffsetException();

    return new DocumentContentImpl(
      content.substring(start.intValue(), end.intValue())
    );
  } // getContent(start, end)

  /** Returns the String representing the content in case of a textual document.
    * NOTE: this is a temporary solution until we have a more generic one.
    */
  public String toString(){
    return content;
  }

  /** The size of this content (e.g. character length for textual
    * content).
    */
  public Long size() {
    return new Long(content.length());
  } // size()

  /** Check that an offset is valid */
  boolean isValidOffset(Long offset) {
    if(offset == null)
      return false;

    long o = offset.longValue();
    long len = content.length();
    if(o > len || o < 0)
      return false;

    return true;
  } // isValidOffset

  /** Check that both start and end are valid offsets and that
    * they constitute a valid offset range
    */
  boolean isValidOffsetRange(Long start, Long end) {
    return
      isValidOffset(start) && isValidOffset(end) &&
      start.longValue() <= end.longValue();
  } // isValidOffsetRange(start,end)

  /* two documents are the same if their contents is the same
   */
  public boolean equals(Object other) {
    if (!(other instanceof DocumentContentImpl)) return false;

    DocumentContentImpl docImpl = (DocumentContentImpl) other;
    return content.equals(docImpl.toString());
  }

  public int hashCode(){ return toString().hashCode();}

  /** Just for now - later we have to cater for different types of
    * content.
    */
  String content;

  /** For ranges */
  public DocumentContentImpl(String s) { content = s; }

  /** Freeze the serialization UID. */
  static final long serialVersionUID = -1426940535575467461L;
} // class DocumentContentImpl
