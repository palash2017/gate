/*
 * ParseCpslTokenManager.java
 *
 * Copyright (c) 2000-2001, The University of Sheffield.
 * 
 * This file is part of GATE (see http://gate.ac.uk/), and is free
 * software, licenced under the GNU Library General Public License,
 * Version 2, June1991.
 * 
 * A copy of this licence is included in the distribution in the file
 * licence.html, and is also available at http://gate.ac.uk/gate/licence.html.
 * 
 * Hamish Cunningham, 23/02/2000
 *
 * $Id$
 */

/* Generated By:JavaCC: Do not edit this line. ParseCpslTokenManager.java */
package gate.jape.parser;
import java.io.*;
import java.util.Enumeration;
import com.objectspace.jgl.*;
import gate.util.*;
import gate.*;
import gate.jape.*;
import gate.gui.*;

public class ParseCpslTokenManager implements ParseCpslConstants
{
/** Debug flag */
private static final boolean DEBUG = false;

private final int jjStopAtPos(int pos, int kind)
{
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   return pos + 1;
}
private final int jjMoveStringLiteralDfa0_0()
{
   switch(curChar)
   {
      case 33:
         jjmatchedKind = 18;
         return jjMoveNfa_0(0, 0);
      case 40:
         jjmatchedKind = 33;
         return jjMoveNfa_0(0, 0);
      case 41:
         jjmatchedKind = 34;
         return jjMoveNfa_0(0, 0);
      case 44:
         jjmatchedKind = 30;
         return jjMoveNfa_0(0, 0);
      case 45:
         return jjMoveStringLiteralDfa1_0(0x200000000000L);
      case 46:
         jjmatchedKind = 28;
         return jjMoveNfa_0(0, 0);
      case 58:
         jjmatchedKind = 26;
         return jjMoveStringLiteralDfa1_0(0x2000000000L);
      case 59:
         jjmatchedKind = 27;
         return jjMoveNfa_0(0, 0);
      case 61:
         jjmatchedKind = 35;
         return jjMoveStringLiteralDfa1_0(0x1000000000L);
      case 73:
         return jjMoveStringLiteralDfa1_0(0x2000L);
      case 77:
         return jjMoveStringLiteralDfa1_0(0x10400L);
      case 79:
         return jjMoveStringLiteralDfa1_0(0x4000L);
      case 80:
         return jjMoveStringLiteralDfa1_0(0x21800L);
      case 82:
         return jjMoveStringLiteralDfa1_0(0x8000L);
      case 105:
         return jjMoveStringLiteralDfa1_0(0x2000L);
      case 109:
         return jjMoveStringLiteralDfa1_0(0x10400L);
      case 111:
         return jjMoveStringLiteralDfa1_0(0x4000L);
      case 112:
         return jjMoveStringLiteralDfa1_0(0x21800L);
      case 114:
         return jjMoveStringLiteralDfa1_0(0x8000L);
      case 123:
         jjmatchedKind = 31;
         return jjMoveNfa_0(0, 0);
      case 124:
         jjmatchedKind = 29;
         return jjMoveNfa_0(0, 0);
      case 125:
         jjmatchedKind = 32;
         return jjMoveNfa_0(0, 0);
      default :
         return jjMoveNfa_0(0, 0);
   }
}
private final int jjMoveStringLiteralDfa1_0(long active0)
{
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
   return jjMoveNfa_0(0, 0);
   }
   switch(curChar)
   {
      case 43:
         if ((active0 & 0x2000000000L) != 0L)
         {
            jjmatchedKind = 37;
            jjmatchedPos = 1;
         }
         break;
      case 45:
         return jjMoveStringLiteralDfa2_0(active0, 0x200000000000L);
      case 61:
         if ((active0 & 0x1000000000L) != 0L)
         {
            jjmatchedKind = 36;
            jjmatchedPos = 1;
         }
         break;
      case 65:
         return jjMoveStringLiteralDfa2_0(active0, 0x10000L);
      case 72:
         return jjMoveStringLiteralDfa2_0(active0, 0x1800L);
      case 78:
         return jjMoveStringLiteralDfa2_0(active0, 0x2000L);
      case 80:
         return jjMoveStringLiteralDfa2_0(active0, 0x4000L);
      case 82:
         return jjMoveStringLiteralDfa2_0(active0, 0x20000L);
      case 85:
         return jjMoveStringLiteralDfa2_0(active0, 0x8400L);
      case 97:
         return jjMoveStringLiteralDfa2_0(active0, 0x10000L);
      case 104:
         return jjMoveStringLiteralDfa2_0(active0, 0x1800L);
      case 110:
         return jjMoveStringLiteralDfa2_0(active0, 0x2000L);
      case 112:
         return jjMoveStringLiteralDfa2_0(active0, 0x4000L);
      case 114:
         return jjMoveStringLiteralDfa2_0(active0, 0x20000L);
      case 117:
         return jjMoveStringLiteralDfa2_0(active0, 0x8400L);
      default :
         break;
   }
   return jjMoveNfa_0(0, 1);
}
private final int jjMoveStringLiteralDfa2_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjMoveNfa_0(0, 1);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
   return jjMoveNfa_0(0, 1);
   }
   switch(curChar)
   {
      case 62:
         if ((active0 & 0x200000000000L) != 0L)
         {
            jjmatchedKind = 45;
            jjmatchedPos = 2;
         }
         break;
      case 65:
         return jjMoveStringLiteralDfa3_0(active0, 0x1800L);
      case 67:
         return jjMoveStringLiteralDfa3_0(active0, 0x10000L);
      case 73:
         return jjMoveStringLiteralDfa3_0(active0, 0x20000L);
      case 76:
         return jjMoveStringLiteralDfa3_0(active0, 0x8400L);
      case 80:
         return jjMoveStringLiteralDfa3_0(active0, 0x2000L);
      case 84:
         return jjMoveStringLiteralDfa3_0(active0, 0x4000L);
      case 97:
         return jjMoveStringLiteralDfa3_0(active0, 0x1800L);
      case 99:
         return jjMoveStringLiteralDfa3_0(active0, 0x10000L);
      case 105:
         return jjMoveStringLiteralDfa3_0(active0, 0x20000L);
      case 108:
         return jjMoveStringLiteralDfa3_0(active0, 0x8400L);
      case 112:
         return jjMoveStringLiteralDfa3_0(active0, 0x2000L);
      case 116:
         return jjMoveStringLiteralDfa3_0(active0, 0x4000L);
      default :
         break;
   }
   return jjMoveNfa_0(0, 2);
}
private final int jjMoveStringLiteralDfa3_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjMoveNfa_0(0, 2);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
   return jjMoveNfa_0(0, 2);
   }
   switch(curChar)
   {
      case 69:
         return jjMoveStringLiteralDfa4_0(active0, 0x8000L);
      case 73:
         return jjMoveStringLiteralDfa4_0(active0, 0x4000L);
      case 79:
         return jjMoveStringLiteralDfa4_0(active0, 0x20000L);
      case 82:
         return jjMoveStringLiteralDfa4_0(active0, 0x10000L);
      case 83:
         return jjMoveStringLiteralDfa4_0(active0, 0x1800L);
      case 84:
         return jjMoveStringLiteralDfa4_0(active0, 0x400L);
      case 85:
         return jjMoveStringLiteralDfa4_0(active0, 0x2000L);
      case 101:
         return jjMoveStringLiteralDfa4_0(active0, 0x8000L);
      case 105:
         return jjMoveStringLiteralDfa4_0(active0, 0x4000L);
      case 111:
         return jjMoveStringLiteralDfa4_0(active0, 0x20000L);
      case 114:
         return jjMoveStringLiteralDfa4_0(active0, 0x10000L);
      case 115:
         return jjMoveStringLiteralDfa4_0(active0, 0x1800L);
      case 116:
         return jjMoveStringLiteralDfa4_0(active0, 0x400L);
      case 117:
         return jjMoveStringLiteralDfa4_0(active0, 0x2000L);
      default :
         break;
   }
   return jjMoveNfa_0(0, 3);
}
private final int jjMoveStringLiteralDfa4_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjMoveNfa_0(0, 3);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
   return jjMoveNfa_0(0, 3);
   }
   switch(curChar)
   {
      case 58:
         if ((active0 & 0x8000L) != 0L)
         {
            jjmatchedKind = 15;
            jjmatchedPos = 4;
         }
         break;
      case 69:
         return jjMoveStringLiteralDfa5_0(active0, 0x1800L);
      case 73:
         return jjMoveStringLiteralDfa5_0(active0, 0x400L);
      case 79:
         return jjMoveStringLiteralDfa5_0(active0, 0x14000L);
      case 82:
         return jjMoveStringLiteralDfa5_0(active0, 0x20000L);
      case 84:
         return jjMoveStringLiteralDfa5_0(active0, 0x2000L);
      case 101:
         return jjMoveStringLiteralDfa5_0(active0, 0x1800L);
      case 105:
         return jjMoveStringLiteralDfa5_0(active0, 0x400L);
      case 111:
         return jjMoveStringLiteralDfa5_0(active0, 0x14000L);
      case 114:
         return jjMoveStringLiteralDfa5_0(active0, 0x20000L);
      case 116:
         return jjMoveStringLiteralDfa5_0(active0, 0x2000L);
      default :
         break;
   }
   return jjMoveNfa_0(0, 4);
}
private final int jjMoveStringLiteralDfa5_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjMoveNfa_0(0, 4);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
   return jjMoveNfa_0(0, 4);
   }
   switch(curChar)
   {
      case 58:
         if ((active0 & 0x1000L) != 0L)
         {
            jjmatchedKind = 12;
            jjmatchedPos = 5;
         }
         else if ((active0 & 0x2000L) != 0L)
         {
            jjmatchedKind = 13;
            jjmatchedPos = 5;
         }
         else if ((active0 & 0x10000L) != 0L)
         {
            jjmatchedKind = 16;
            jjmatchedPos = 5;
         }
         break;
      case 73:
         return jjMoveStringLiteralDfa6_0(active0, 0x20000L);
      case 78:
         return jjMoveStringLiteralDfa6_0(active0, 0x4000L);
      case 80:
         return jjMoveStringLiteralDfa6_0(active0, 0x400L);
      case 83:
         return jjMoveStringLiteralDfa6_0(active0, 0x800L);
      case 105:
         return jjMoveStringLiteralDfa6_0(active0, 0x20000L);
      case 110:
         return jjMoveStringLiteralDfa6_0(active0, 0x4000L);
      case 112:
         return jjMoveStringLiteralDfa6_0(active0, 0x400L);
      case 115:
         return jjMoveStringLiteralDfa6_0(active0, 0x800L);
      default :
         break;
   }
   return jjMoveNfa_0(0, 5);
}
private final int jjMoveStringLiteralDfa6_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjMoveNfa_0(0, 5);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
   return jjMoveNfa_0(0, 5);
   }
   switch(curChar)
   {
      case 58:
         if ((active0 & 0x800L) != 0L)
         {
            jjmatchedKind = 11;
            jjmatchedPos = 6;
         }
         break;
      case 72:
         return jjMoveStringLiteralDfa7_0(active0, 0x400L);
      case 83:
         return jjMoveStringLiteralDfa7_0(active0, 0x4000L);
      case 84:
         return jjMoveStringLiteralDfa7_0(active0, 0x20000L);
      case 104:
         return jjMoveStringLiteralDfa7_0(active0, 0x400L);
      case 115:
         return jjMoveStringLiteralDfa7_0(active0, 0x4000L);
      case 116:
         return jjMoveStringLiteralDfa7_0(active0, 0x20000L);
      default :
         break;
   }
   return jjMoveNfa_0(0, 6);
}
private final int jjMoveStringLiteralDfa7_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjMoveNfa_0(0, 6);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
   return jjMoveNfa_0(0, 6);
   }
   switch(curChar)
   {
      case 58:
         if ((active0 & 0x4000L) != 0L)
         {
            jjmatchedKind = 14;
            jjmatchedPos = 7;
         }
         break;
      case 65:
         return jjMoveStringLiteralDfa8_0(active0, 0x400L);
      case 89:
         return jjMoveStringLiteralDfa8_0(active0, 0x20000L);
      case 97:
         return jjMoveStringLiteralDfa8_0(active0, 0x400L);
      case 121:
         return jjMoveStringLiteralDfa8_0(active0, 0x20000L);
      default :
         break;
   }
   return jjMoveNfa_0(0, 7);
}
private final int jjMoveStringLiteralDfa8_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjMoveNfa_0(0, 7);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
   return jjMoveNfa_0(0, 7);
   }
   switch(curChar)
   {
      case 58:
         if ((active0 & 0x20000L) != 0L)
         {
            jjmatchedKind = 17;
            jjmatchedPos = 8;
         }
         break;
      case 83:
         return jjMoveStringLiteralDfa9_0(active0, 0x400L);
      case 115:
         return jjMoveStringLiteralDfa9_0(active0, 0x400L);
      default :
         break;
   }
   return jjMoveNfa_0(0, 8);
}
private final int jjMoveStringLiteralDfa9_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjMoveNfa_0(0, 8);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
   return jjMoveNfa_0(0, 8);
   }
   switch(curChar)
   {
      case 69:
         return jjMoveStringLiteralDfa10_0(active0, 0x400L);
      case 101:
         return jjMoveStringLiteralDfa10_0(active0, 0x400L);
      default :
         break;
   }
   return jjMoveNfa_0(0, 9);
}
private final int jjMoveStringLiteralDfa10_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjMoveNfa_0(0, 9);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
   return jjMoveNfa_0(0, 9);
   }
   switch(curChar)
   {
      case 58:
         if ((active0 & 0x400L) != 0L)
         {
            jjmatchedKind = 10;
            jjmatchedPos = 10;
         }
         break;
      default :
         break;
   }
   return jjMoveNfa_0(0, 10);
}
private final void jjCheckNAdd(int state)
{
   if (jjrounds[state] != jjround)
   {
      jjstateSet[jjnewStateCnt++] = state;
      jjrounds[state] = jjround;
   }
}
private final void jjAddStates(int start, int end)
{
   do {
      jjstateSet[jjnewStateCnt++] = jjnextStates[start];
   } while (start++ != end);
}
private final void jjCheckNAddTwoStates(int state1, int state2)
{
   jjCheckNAdd(state1);
   jjCheckNAdd(state2);
}
private final void jjCheckNAddStates(int start, int end)
{
   do {
      jjCheckNAdd(jjnextStates[start]);
   } while (start++ != end);
}
private final void jjCheckNAddStates(int start)
{
   jjCheckNAdd(jjnextStates[start]);
   jjCheckNAdd(jjnextStates[start + 1]);
}
static final long[] jjbitVec0 = {
   0x0L, 0x0L, 0xffffffffffffffffL, 0xffffffffffffffffL
};
private final int jjMoveNfa_0(int startState, int curPos)
{
   int strKind = jjmatchedKind;
   int strPos = jjmatchedPos;
   int seenUpto;
   input_stream.backup(seenUpto = curPos + 1);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) { throw new Error("Internal Error"); }
   curPos = 0;
   int[] nextStates;
   int startsAt = 0;
   jjnewStateCnt = 56;
   int i = 1;
   jjstateSet[0] = startState;
   int j, kind = 0x7fffffff;
   for (;;)
   {
      if (++jjround == 0x7fffffff)
         ReInitRounds();
      if (curChar < 64)
      {
         long l = 1L << curChar;
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 0:
                  if ((0x3ff000000000000L & l) != 0L)
                  {
                     if (kind > 20)
                        kind = 20;
                     jjCheckNAddStates(0, 7);
                  }
                  else if ((0x100003600L & l) != 0L)
                  {
                     if (kind > 38)
                        kind = 38;
                     jjCheckNAdd(20);
                  }
                  else if ((0x80000c0000000000L & l) != 0L)
                  {
                     if (kind > 19)
                        kind = 19;
                  }
                  else if (curChar == 47)
                     jjAddStates(8, 9);
                  else if (curChar == 35)
                     jjstateSet[jjnewStateCnt++] = 29;
                  else if (curChar == 59)
                     jjstateSet[jjnewStateCnt++] = 21;
                  else if (curChar == 46)
                     jjCheckNAdd(15);
                  else if (curChar == 34)
                     jjCheckNAddTwoStates(2, 3);
                  break;
               case 1:
                  if (curChar == 34)
                     jjCheckNAddTwoStates(2, 3);
                  break;
               case 2:
                  if ((0xfffffffbffffffffL & l) != 0L)
                     jjCheckNAddTwoStates(2, 3);
                  break;
               case 3:
                  if (curChar == 34 && kind > 21)
                     kind = 21;
                  break;
               case 13:
                  if ((0x3ff200000000000L & l) == 0L)
                     break;
                  if (kind > 23)
                     kind = 23;
                  jjstateSet[jjnewStateCnt++] = 13;
                  break;
               case 14:
                  if (curChar == 46)
                     jjCheckNAdd(15);
                  break;
               case 15:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 24)
                     kind = 24;
                  jjCheckNAddStates(10, 12);
                  break;
               case 17:
                  if ((0x280000000000L & l) != 0L)
                     jjCheckNAdd(18);
                  break;
               case 18:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 24)
                     kind = 24;
                  jjCheckNAddTwoStates(18, 19);
                  break;
               case 20:
                  if ((0x100003600L & l) == 0L)
                     break;
                  if (kind > 38)
                     kind = 38;
                  jjCheckNAdd(20);
                  break;
               case 21:
                  if (curChar == 59)
                     jjCheckNAddStates(13, 16);
                  break;
               case 22:
                  if ((0xffffffffffffdbffL & l) != 0L)
                     jjCheckNAddStates(13, 16);
                  break;
               case 23:
                  if ((0x2400L & l) != 0L && kind > 40)
                     kind = 40;
                  break;
               case 24:
                  if (curChar == 13 && kind > 40)
                     kind = 40;
                  break;
               case 25:
                  if (curChar == 10)
                     jjstateSet[jjnewStateCnt++] = 24;
                  break;
               case 26:
                  if (curChar == 10 && kind > 40)
                     kind = 40;
                  break;
               case 27:
                  if (curChar == 13)
                     jjstateSet[jjnewStateCnt++] = 26;
                  break;
               case 28:
                  if (curChar == 59)
                     jjstateSet[jjnewStateCnt++] = 21;
                  break;
               case 30:
                  if (curChar == 35)
                     jjstateSet[jjnewStateCnt++] = 29;
                  break;
               case 31:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 20)
                     kind = 20;
                  jjCheckNAddStates(0, 7);
                  break;
               case 32:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 20)
                     kind = 20;
                  jjCheckNAdd(32);
                  break;
               case 33:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(33, 34);
                  break;
               case 34:
                  if (curChar != 46)
                     break;
                  if (kind > 24)
                     kind = 24;
                  jjCheckNAddStates(17, 19);
                  break;
               case 35:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 24)
                     kind = 24;
                  jjCheckNAddStates(17, 19);
                  break;
               case 37:
                  if ((0x280000000000L & l) != 0L)
                     jjCheckNAdd(38);
                  break;
               case 38:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 24)
                     kind = 24;
                  jjCheckNAddTwoStates(38, 19);
                  break;
               case 39:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(39, 40);
                  break;
               case 41:
                  if ((0x280000000000L & l) != 0L)
                     jjCheckNAdd(42);
                  break;
               case 42:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 24)
                     kind = 24;
                  jjCheckNAddTwoStates(42, 19);
                  break;
               case 43:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddStates(20, 22);
                  break;
               case 45:
                  if ((0x280000000000L & l) != 0L)
                     jjCheckNAdd(46);
                  break;
               case 46:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(46, 19);
                  break;
               case 47:
                  if (curChar == 47)
                     jjAddStates(8, 9);
                  break;
               case 48:
                  if (curChar == 47)
                     jjCheckNAddStates(23, 26);
                  break;
               case 49:
                  if ((0xffffffffffffdbffL & l) != 0L)
                     jjCheckNAddStates(23, 26);
                  break;
               case 50:
                  if ((0x2400L & l) != 0L && kind > 39)
                     kind = 39;
                  break;
               case 51:
                  if (curChar == 13 && kind > 39)
                     kind = 39;
                  break;
               case 52:
                  if (curChar == 10)
                     jjstateSet[jjnewStateCnt++] = 51;
                  break;
               case 53:
                  if (curChar == 10 && kind > 39)
                     kind = 39;
                  break;
               case 54:
                  if (curChar == 13)
                     jjstateSet[jjnewStateCnt++] = 53;
                  break;
               case 55:
                  if (curChar == 42 && kind > 41)
                     kind = 41;
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else if (curChar < 128)
      {
         long l = 1L << (curChar & 077);
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 0:
                  if ((0x7fffffe07fffffeL & l) != 0L)
                  {
                     if (kind > 23)
                        kind = 23;
                     jjCheckNAdd(13);
                  }
                  if (curChar == 102)
                     jjstateSet[jjnewStateCnt++] = 10;
                  else if (curChar == 116)
                     jjstateSet[jjnewStateCnt++] = 6;
                  break;
               case 2:
                  jjAddStates(27, 28);
                  break;
               case 4:
                  if (curChar == 101 && kind > 22)
                     kind = 22;
                  break;
               case 5:
                  if (curChar == 117)
                     jjCheckNAdd(4);
                  break;
               case 6:
                  if (curChar == 114)
                     jjstateSet[jjnewStateCnt++] = 5;
                  break;
               case 7:
                  if (curChar == 116)
                     jjstateSet[jjnewStateCnt++] = 6;
                  break;
               case 8:
                  if (curChar == 115)
                     jjCheckNAdd(4);
                  break;
               case 9:
                  if (curChar == 108)
                     jjstateSet[jjnewStateCnt++] = 8;
                  break;
               case 10:
                  if (curChar == 97)
                     jjstateSet[jjnewStateCnt++] = 9;
                  break;
               case 11:
                  if (curChar == 102)
                     jjstateSet[jjnewStateCnt++] = 10;
                  break;
               case 12:
                  if ((0x7fffffe07fffffeL & l) == 0L)
                     break;
                  if (kind > 23)
                     kind = 23;
                  jjCheckNAdd(13);
                  break;
               case 13:
                  if ((0x7fffffe87fffffeL & l) == 0L)
                     break;
                  if (kind > 23)
                     kind = 23;
                  jjCheckNAdd(13);
                  break;
               case 16:
                  if ((0x2000000020L & l) != 0L)
                     jjAddStates(29, 30);
                  break;
               case 19:
                  if ((0x5000000050L & l) != 0L && kind > 24)
                     kind = 24;
                  break;
               case 22:
                  jjAddStates(13, 16);
                  break;
               case 29:
                  if (curChar == 124 && kind > 41)
                     kind = 41;
                  break;
               case 36:
                  if ((0x2000000020L & l) != 0L)
                     jjAddStates(31, 32);
                  break;
               case 40:
                  if ((0x2000000020L & l) != 0L)
                     jjAddStates(33, 34);
                  break;
               case 44:
                  if ((0x2000000020L & l) != 0L)
                     jjAddStates(35, 36);
                  break;
               case 49:
                  jjAddStates(23, 26);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else
      {
         int i2 = (curChar & 0xff) >> 6;
         long l2 = 1L << (curChar & 077);
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 2:
                  if ((jjbitVec0[i2] & l2) != 0L)
                     jjAddStates(27, 28);
                  break;
               case 22:
                  if ((jjbitVec0[i2] & l2) != 0L)
                     jjAddStates(13, 16);
                  break;
               case 49:
                  if ((jjbitVec0[i2] & l2) != 0L)
                     jjAddStates(23, 26);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      if (kind != 0x7fffffff)
      {
         jjmatchedKind = kind;
         jjmatchedPos = curPos;
         kind = 0x7fffffff;
      }
      ++curPos;
      if ((i = jjnewStateCnt) == (startsAt = 56 - (jjnewStateCnt = startsAt)))
         break;
      try { curChar = input_stream.readChar(); }
      catch(java.io.IOException e) { break; }
   }
   if (jjmatchedPos > strPos)
      return curPos;

   int toRet = Math.max(curPos, seenUpto);

   if (curPos < toRet)
      for (i = toRet - Math.min(curPos, seenUpto); i-- > 0; )
         try { curChar = input_stream.readChar(); }
         catch(java.io.IOException e) { throw new Error("Internal Error : Please send a bug report."); }

   if (jjmatchedPos < strPos)
   {
      jjmatchedKind = strKind;
      jjmatchedPos = strPos;
   }
   else if (jjmatchedPos == strPos && jjmatchedKind > strKind)
      jjmatchedKind = strKind;

   return toRet;
}
private final int jjMoveStringLiteralDfa0_1()
{
   return jjMoveNfa_1(1, 0);
}
private final int jjMoveNfa_1(int startState, int curPos)
{
   int[] nextStates;
   int startsAt = 0;
   jjnewStateCnt = 4;
   int i = 1;
   jjstateSet[0] = startState;
   int j, kind = 0x7fffffff;
   for (;;)
   {
      if (++jjround == 0x7fffffff)
         ReInitRounds();
      if (curChar < 64)
      {
         long l = 1L << curChar;
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 1:
                  if (curChar == 42)
                     jjstateSet[jjnewStateCnt++] = 0;
                  break;
               case 0:
                  if (curChar == 47)
                     kind = 43;
                  break;
               case 2:
                  if (curChar == 35)
                     kind = 43;
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else if (curChar < 128)
      {
         long l = 1L << (curChar & 077);
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 1:
                  if (curChar == 124)
                     jjstateSet[jjnewStateCnt++] = 2;
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else
      {
         int i2 = (curChar & 0xff) >> 6;
         long l2 = 1L << (curChar & 077);
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               default : break;
            }
         } while(i != startsAt);
      }
      if (kind != 0x7fffffff)
      {
         jjmatchedKind = kind;
         jjmatchedPos = curPos;
         kind = 0x7fffffff;
      }
      ++curPos;
      if ((i = jjnewStateCnt) == (startsAt = 4 - (jjnewStateCnt = startsAt)))
         return curPos;
      try { curChar = input_stream.readChar(); }
      catch(java.io.IOException e) { return curPos; }
   }
}
static final int[] jjnextStates = {
   32, 33, 34, 39, 40, 43, 44, 19, 48, 55, 15, 16, 19, 22, 23, 25, 
   27, 35, 36, 19, 43, 44, 19, 49, 50, 52, 54, 2, 3, 17, 18, 37, 
   38, 41, 42, 45, 46, 
};
public static final String[] jjstrLiteralImages = {
"", null, null, null, null, null, null, null, null, null, null, null, null, 
null, null, null, null, null, "\41", null, null, null, null, null, null, null, 
"\72", "\73", "\56", "\174", "\54", "\173", "\175", "\50", "\51", "\75", "\75\75", 
"\72\53", null, null, null, null, null, null, null, "\55\55\76", };
public static final String[] lexStateNames = {
   "DEFAULT", 
   "WithinComment", 
};
public static final int[] jjnewLexState = {
   -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
   -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, -1, 0, -1, -1, 
};
static final long[] jjtoToken = {
   0x303ffdfffc01L, 
};
static final long[] jjtoSkip = {
   0xbc000000000L, 
};
static final long[] jjtoSpecial = {
   0xbc000000000L, 
};
static final long[] jjtoMore = {
   0x40000000000L, 
};
private ASCII_CharStream input_stream;
private final int[] jjrounds = new int[56];
private final int[] jjstateSet = new int[112];
StringBuffer image;
int jjimageLen;
int lengthOfMatch;
protected char curChar;
public ParseCpslTokenManager(ASCII_CharStream stream)
{
   if (ASCII_CharStream.staticFlag)
      throw new Error("ERROR: Cannot use a static CharStream class with a non-static lexical analyzer.");
   input_stream = stream;
}
public ParseCpslTokenManager(ASCII_CharStream stream, int lexState)
{
   this(stream);
   SwitchTo(lexState);
}
public void ReInit(ASCII_CharStream stream)
{
   jjmatchedPos = jjnewStateCnt = 0;
   curLexState = defaultLexState;
   input_stream = stream;
   ReInitRounds();
}
private final void ReInitRounds()
{
   int i;
   jjround = 0x80000001;
   for (i = 56; i-- > 0;)
      jjrounds[i] = 0x80000000;
}
public void ReInit(ASCII_CharStream stream, int lexState)
{
   ReInit(stream);
   SwitchTo(lexState);
}
public void SwitchTo(int lexState)
{
   if (lexState >= 2 || lexState < 0)
      throw new TokenMgrError("Error: Ignoring invalid lexical state : " + lexState + ". State unchanged.", TokenMgrError.INVALID_LEXICAL_STATE);
   else
      curLexState = lexState;
}

private final Token jjFillToken()
{
   Token t = Token.newToken(jjmatchedKind);
   t.kind = jjmatchedKind;
   String im = jjstrLiteralImages[jjmatchedKind];
   t.image = (im == null) ? input_stream.GetImage() : im;
   t.beginLine = input_stream.getBeginLine();
   t.beginColumn = input_stream.getBeginColumn();
   t.endLine = input_stream.getEndLine();
   t.endColumn = input_stream.getEndColumn();
   return t;
}

int curLexState = 0;
int defaultLexState = 0;
int jjnewStateCnt;
int jjround;
int jjmatchedPos;
int jjmatchedKind;

public final Token getNextToken() 
{
  int kind;
  Token specialToken = null;
  Token matchedToken;
  int curPos = 0;

  EOFLoop :
  for (;;)
  {   
   try   
   {     
      curChar = input_stream.BeginToken();
   }     
   catch(java.io.IOException e)
   {        
      jjmatchedKind = 0;
      matchedToken = jjFillToken();
      matchedToken.specialToken = specialToken;
      return matchedToken;
   }
   image = null;
   jjimageLen = 0;

   for (;;)
   {
     switch(curLexState)
     {
       case 0:
         jjmatchedKind = 0x7fffffff;
         jjmatchedPos = 0;
         curPos = jjMoveStringLiteralDfa0_0();
         if (jjmatchedPos == 0 && jjmatchedKind > 44)
         {
            jjmatchedKind = 44;
         }
         break;
       case 1:
         jjmatchedKind = 0x7fffffff;
         jjmatchedPos = 0;
         curPos = jjMoveStringLiteralDfa0_1();
         if (jjmatchedPos == 0 && jjmatchedKind > 42)
         {
            jjmatchedKind = 42;
         }
         break;
     }
     if (jjmatchedKind != 0x7fffffff)
     {
        if (jjmatchedPos + 1 < curPos)
           input_stream.backup(curPos - jjmatchedPos - 1);
        if ((jjtoToken[jjmatchedKind >> 6] & (1L << (jjmatchedKind & 077))) != 0L)
        {
           matchedToken = jjFillToken();
           matchedToken.specialToken = specialToken;
       if (jjnewLexState[jjmatchedKind] != -1)
         curLexState = jjnewLexState[jjmatchedKind];
           return matchedToken;
        }
        else if ((jjtoSkip[jjmatchedKind >> 6] & (1L << (jjmatchedKind & 077))) != 0L)
        {
           if ((jjtoSpecial[jjmatchedKind >> 6] & (1L << (jjmatchedKind & 077))) != 0L)
           {
              matchedToken = jjFillToken();
              if (specialToken == null)
                 specialToken = matchedToken;
              else
              {
                 matchedToken.specialToken = specialToken;
                 specialToken = (specialToken.next = matchedToken);
              }
              SkipLexicalActions(matchedToken);
           }
           else 
              SkipLexicalActions(null);
         if (jjnewLexState[jjmatchedKind] != -1)
           curLexState = jjnewLexState[jjmatchedKind];
           continue EOFLoop;
        }
        jjimageLen += jjmatchedPos + 1;
      if (jjnewLexState[jjmatchedKind] != -1)
        curLexState = jjnewLexState[jjmatchedKind];
        curPos = 0;
        jjmatchedKind = 0x7fffffff;
        try {
           curChar = input_stream.readChar();
           continue;
        }
        catch (java.io.IOException e1) { }
     }
     int error_line = input_stream.getEndLine();
     int error_column = input_stream.getEndColumn();
     String error_after = null;
     boolean EOFSeen = false;
     try { input_stream.readChar(); input_stream.backup(1); }
     catch (java.io.IOException e1) {
        EOFSeen = true;
        error_after = curPos <= 1 ? "" : input_stream.GetImage();
        if (curChar == '\n' || curChar == '\r') {
           error_line++;
           error_column = 0;
        }
        else
           error_column++;
     }
     if (!EOFSeen) {
        input_stream.backup(1);
        error_after = curPos <= 1 ? "" : input_stream.GetImage();
     }
     throw new TokenMgrError(EOFSeen, curLexState, error_line, error_column, error_after, curChar, TokenMgrError.LEXICAL_ERROR);
   }
  }
}

final void SkipLexicalActions(Token matchedToken)
{
   switch(jjmatchedKind)
   {
      default :
         break;
   }
}
}