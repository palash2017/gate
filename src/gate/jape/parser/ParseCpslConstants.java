/* Generated By:JavaCC: Do not edit this line. ParseCpslConstants.java */
package gate.jape.parser;

public interface ParseCpslConstants {

  int EOF = 0;
  int space = 1;
  int spaces = 2;
  int newline = 3;
  int digits = 4;
  int letter = 5;
  int letters = 6;
  int lettersAndDigits = 7;
  int letterOrDigitOrDash = 8;
  int lettersAndDigitsAndDashes = 9;
  int multiphase = 10;
  int phases = 11;
  int path = 12;
  int phasesWhiteSpace = 13;
  int phasesSingleLineCStyleComment = 14;
  int phasesSingleLineCpslStyleComment = 15;
  int phasesCommentStart = 16;
  int phasesCommentChars = 17;
  int phasesCommentEnd = 18;
  int phase = 19;
  int input = 20;
  int option = 21;
  int rule = 22;
  int macro = 23;
  int priority = 24;
  int pling = 25;
  int kleeneOp = 26;
  int attrOp = 27;
  int metaPropOp = 28;
  int integer = 29;
  int string = 36;
  int bool = 37;
  int ident = 38;
  int floatingPoint = 39;
  int exponent = 40;
  int colon = 41;
  int semicolon = 42;
  int period = 43;
  int bar = 44;
  int comma = 45;
  int leftBrace = 46;
  int rightBrace = 47;
  int leftBracket = 48;
  int rightBracket = 49;
  int leftSquare = 50;
  int rightSquare = 51;
  int assign = 52;
  int colonplus = 53;
  int whiteSpace = 54;
  int singleLineCStyleComment = 55;
  int singleLineCpslStyleComment = 56;
  int commentStart = 57;
  int commentChars = 58;
  int commentEnd = 59;
  int other = 60;

  int DEFAULT = 0;
  int IN_PHASES = 1;
  int PHASES_WITHIN_COMMENT = 2;
  int IN_STRING = 3;
  int WITHIN_COMMENT = 4;

  String[] tokenImage = {
    "<EOF>",
    "<space>",
    "<spaces>",
    "<newline>",
    "<digits>",
    "<letter>",
    "<letters>",
    "<lettersAndDigits>",
    "<letterOrDigitOrDash>",
    "<lettersAndDigitsAndDashes>",
    "\"Multiphase:\"",
    "\"Phases:\"",
    "<path>",
    "<phasesWhiteSpace>",
    "<phasesSingleLineCStyleComment>",
    "<phasesSingleLineCpslStyleComment>",
    "<phasesCommentStart>",
    "<phasesCommentChars>",
    "<phasesCommentEnd>",
    "\"Phase:\"",
    "\"Input:\"",
    "\"Options:\"",
    "\"Rule:\"",
    "\"Macro:\"",
    "\"Priority:\"",
    "\"!\"",
    "<kleeneOp>",
    "<attrOp>",
    "\"@\"",
    "<integer>",
    "\"\\\"\"",
    "\"\\\\n\"",
    "\"\\\\r\"",
    "\"\\\\t\"",
    "\"\\\\\\\"\"",
    "<token of kind 35>",
    "\"\\\"\"",
    "<bool>",
    "<ident>",
    "<floatingPoint>",
    "<exponent>",
    "\":\"",
    "\";\"",
    "\".\"",
    "\"|\"",
    "\",\"",
    "\"{\"",
    "\"}\"",
    "\"(\"",
    "\")\"",
    "\"[\"",
    "\"]\"",
    "\"=\"",
    "\":+\"",
    "<whiteSpace>",
    "<singleLineCStyleComment>",
    "<singleLineCpslStyleComment>",
    "<commentStart>",
    "<commentChars>",
    "<commentEnd>",
    "<other>",
    "\"-->\"",
  };

}
