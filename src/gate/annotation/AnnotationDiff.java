/*
 *  AnnotationDiff.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Cristian URSU, 27/Oct/2000
 *
 *  $Id$
 */

package gate.annotation;

import java.util.*;
import java.awt.*;
import java.text.NumberFormat;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

import gate.util.*;
import gate.annotation.*;
import gate.*;
import gate.gui.*;
import gate.creole.*;

/**
  * This class compare two annotation sets on annotation type given by the
  * AnnotationSchema object. It also deals with graphic representation.
  */
public class AnnotationDiff extends JPanel implements VisualResource{

  /** Debug flag */
  private static final boolean DEBUG = false;

  public final int MAX_TYPES = 5;

  public final int NULL_TYPE = -1;
  public final int DEFAULT_TYPE = 0;
  public final int CORRECT_TYPE = 1;
  public final int PARTIALLY_CORRECT_TYPE = 2;
  public final int INCORRECT_TYPE = 3;
  public final int MISSING_TYPE = 4;

  /** Used to represent the result of diff*/
  Set diffSet = new HashSet();

  Set keyPartiallySet = new HashSet();
  Set responsePartiallySet = new HashSet();
  java.util.List keyAnnotList = null;
  java.util.List responseAnnotList = null;


  /** Used to store the no. of annotations from response,identified as belonging
    * to one of the previous types.
    */
  private int typeCounter[] = new int[MAX_TYPES];

  /** This document contains the key annotation set which is taken as reference
   *  in comparison*/
  private Document keyDocument = null;

  /** This document contains the response annotation set which is being
    * compared against the key annotation set.
    */
  private Document responseDocument = null;

  /** The annotation schema object used to get the annotation name
    */
  private AnnotationSchema annotationSchema = null;

  /** The Precision value (see NLP Information Extraction)*/
  private Double precision = new Double(0);

  /** The Recall value (see NLP Information Extraction)*/
  private Double recall = new Double(0);

  /** A number formater for displaying precision and recall*/
  protected static NumberFormat formatter = NumberFormat.getInstance();

  /** As Required by Resource Interface*/
  private FeatureMap featureMap = null;

  /** The components that will stay into diffPanel*/
  private XJTable diffTable = new XJTable();

  /** Constructs a AnnotationDif*/
  public AnnotationDiff(){
  }//AnnotationDiff

  /** Sets the key Document containing the annotation set taken as refference*/
  public void setKeyDocument(Document aKeyDocument) {
    keyDocument = aKeyDocument;
  }// setKeyDocument

  /** Gets the precision field*/
  public Double getPrecision(){
    return precision;
  }// getPrecision

  /** Gets the recall*/
  public Double getRecall(){
    return recall;
  }// getRecall

  /** Gets the keyDocument */
  public Document getKeyDocument(){
    return keyDocument;
  }// getKeyDocument

  /**
    * Sets the response Document(containing the annotation Set being compared)
    */
  public void setResponseDocument(Document aResponseDocument) {
    responseDocument = aResponseDocument;
  }//setResponseDocument

  /**
    * Sets the annotation type being compared. This type is found in annotation
    * Schema object as parameter.
    */
  public void setAnnotationSchema(AnnotationSchema anAnnotationSchema) {
    annotationSchema = anAnnotationSchema;
  } // setAnnotationType

  /** Returns the annotation schema object */
  public AnnotationSchema getAnnotationSchema(){
    return annotationSchema;
  }// AnnotationSchema

  /** This method is required in VisualResource Interface*/
  public JComponent getGUI(){
   // return diffPanel;
   return null;
  }// getViewer

  /**
    * This method does the diff, P&R calculation and so on.
    */
  public Resource init() {
    // Do the diff, P&R calculation and so on
    AnnotationSet keyAnnotSet = null;
    AnnotationSet responseAnnotSet = null;

    // Get the key AnnotationSet from the keyDocument
    keyAnnotSet = keyDocument.getAnnotations().get(
                              annotationSchema.getAnnotationName());
    // The alghoritm will modify this annotation set. It is better to make a
    // separate copy of them.
    keyAnnotList = new LinkedList(keyAnnotSet);
    // Get the response AnnotationSet from the resonseDocument
    responseAnnotSet = responseDocument.getAnnotations().get(
                                        annotationSchema.getAnnotationName());
    // The same thing applies here.
    responseAnnotList = new LinkedList(responseAnnotSet);

    // Sort them ascending on Start offset (the comparator does that)
    AnnotationSetComparator asComparator = new AnnotationSetComparator();
    Collections.sort(keyAnnotList, asComparator);
    Collections.sort(responseAnnotList, asComparator);

    for (int type=0; type < MAX_TYPES; type++)
      typeCounter[type] = 0;

    // Calculate the diff Set. This set will be used later with graphic
    // visualisation.
    doDiff(keyAnnotList, responseAnnotList);

    //Show it
    // Configuring the formatter object. It will be used later to format
    // precision and recall
    formatter.setMaximumIntegerDigits(1);
    formatter.setMinimumFractionDigits(4);
    formatter.setMinimumFractionDigits(4);

    // Create an Annotation diff table model
    AnnotationDiffTableModel diffModel=new AnnotationDiffTableModel(diffSet);
    // Set the model for our table
    diffTable.setModel(diffModel);
    // Set the cell renderer.
    AnnotationDiffCellRenderer cellRenderer=new AnnotationDiffCellRenderer();
    diffTable.setDefaultRenderer(java.lang.String.class,cellRenderer);
    diffTable.setDefaultRenderer(java.lang.Long.class,cellRenderer);

    // Arange all components on a this JPanel
    arangeAllComponents();

    if (DEBUG)
      printStructure(diffSet);

    return this;
  } // init()

  /** This method aranges everything on this JPanel*/
  protected void arangeAllComponents(){
    // Setting the box layout for diffpanel
    BoxLayout boxLayout = new BoxLayout(this,BoxLayout.Y_AXIS);
    this.setLayout(boxLayout);
    // Put the table into a JScrollPanel
    JScrollPane tableScroll = new JScrollPane(diffTable);
    // Add the tableScroll to the diffPanel
    this.add(tableScroll);

    //Lay out the JLabels from left to right.
    JPanel jLabelPane = new JPanel();
    jLabelPane.setLayout(new BoxLayout(jLabelPane, BoxLayout.X_AXIS));
    // Keep the components together
    jLabelPane.add(Box.createHorizontalGlue());
    JLabel precisionLabel = new JLabel("Precision: " +
                                    formatter.format(precision));
    jLabelPane.add(precisionLabel);
    // This places a space between the two JLabel components
    jLabelPane.add(Box.createRigidArea(new Dimension(20, 0)));
    JLabel recallLabel = new JLabel("Recall: " + formatter.format(recall));
    jLabelPane.add(recallLabel);

    this.add(jLabelPane);
  }//arangeAllComponents

  protected void printStructure(Set aDiffSet){
    Iterator iterator = aDiffSet.iterator();
    String leftAnnot = null;
    String rightAnnot = null;
    while(iterator.hasNext()){
      DiffSetElement diffElem = (DiffSetElement) iterator.next();
      if (diffElem.getLeftAnnotation() == null)
        leftAnnot = "NULL ";
      else
        leftAnnot = diffElem.getLeftAnnotation().toString();
      if (diffElem.getRightAnnotation() == null)
        rightAnnot = " NULL";
      else
        rightAnnot = diffElem.getRightAnnotation().toString();
      Out.prln( leftAnnot + "|" + rightAnnot);
    }// end while
    Out.prln("Precision = " + precision + " , Recall = " + recall);
  }// printStructure

  /** This method does the AnnotationSet diff and creates a set with
    * diffSetElement objects.
    */
  protected void doDiff( java.util.List aKeyAnnotList,
                     java.util.List aResponseAnnotList){

    // If one of the annotation sets is null then is no point in doing the diff.
    if (aKeyAnnotList == null || aResponseAnnotList == null)
      return;



    int actual = aResponseAnnotList.size();
    int possible = aKeyAnnotList.size();

    // Iterate throught all elements from keyList and find those in the response
    // List which satisfies isCompatible() and isPartiallyCompatible() relations
    Iterator keyIterator = aKeyAnnotList.iterator();
    boolean stopLoop = false;
    while(keyIterator.hasNext() && !stopLoop){
      Annotation keyAnnot = (Annotation) keyIterator.next();
      Iterator responseIterator = aResponseAnnotList.iterator();
      // There are no elements in responseSet, then quit this loop.
      if (!responseIterator.hasNext()){
        stopLoop = true;
        continue;
      } // end if
      DiffSetElement diffElement = null;
      while(responseIterator.hasNext()){
        Annotation responseAnnot = (Annotation) responseIterator.next();

        if(keyAnnot.isPartiallyCompatible(responseAnnot)){
          keyPartiallySet.add(keyAnnot);
          responsePartiallySet.add(responseAnnot);
          if (keyAnnot.coextensive(responseAnnot)){
            // Found two compatible annotations
            // Create a new DiffSetElement and add it to the diffSet
            diffElement = new DiffSetElement( keyAnnot,
                                              responseAnnot,
                                              CORRECT_TYPE,
                                              CORRECT_TYPE);

            // Add this element to the DiffSet
            addToDiffset(diffElement);
          }// End if (keyAnnot.coextensive(responseAnnot))
        }else if (keyAnnot.coextensive(responseAnnot)){
          // Found two aligned annotations. We have to find out if the response
          // is partialy compatible with another key annotation.
          // Create a new DiffSetElement and add it to the diffSet
          diffElement = new DiffSetElement( keyAnnot,
                                            responseAnnot,
                                            detectKeyType(keyAnnot),
                                            detectResponseType(responseAnnot));
          // Add this element to the DiffSet
          addToDiffset(diffElement);
        }// End if (keyAnnot.coextensive(responseAnnot)){

        if (diffElement != null){
          // Eliminate the response annotation from the list.
          responseIterator.remove();
          break;
        }// End if
      }// end while responseIterator

      if (diffElement == null)
        if (keyPartiallySet.contains(keyAnnot))
          diffElement = new DiffSetElement( keyAnnot,
                                            null,
                                            DEFAULT_TYPE,
                                            NULL_TYPE);
        else
          diffElement = new DiffSetElement( keyAnnot,
                                            null,
                                            MISSING_TYPE,
                                            NULL_TYPE);


      keyIterator.remove();
    }// end while keyIterator

    DiffSetElement diffElem = null;
    Iterator responseIter = aResponseAnnotList.iterator();
    while (responseIter.hasNext()){
      Annotation respAnnot = (Annotation) responseIter.next();
      if (responsePartiallySet.contains(respAnnot))
        diffElem = new DiffSetElement( null,
                                       respAnnot,
                                       NULL_TYPE,
                                       PARTIALLY_CORRECT_TYPE);
      else
        diffElem = new DiffSetElement( null,
                                       respAnnot,
                                       NULL_TYPE,
                                       INCORRECT_TYPE);
      responseIter.remove();
    }// End while

    if (actual != 0)
      precision = new Double(typeCounter[CORRECT_TYPE]/actual);
    if (possible != 0)
      recall = new Double(typeCounter[CORRECT_TYPE]/possible);
  }// doDiff

  private int detectKeyType(Annotation anAnnot){
    if (anAnnot == null) return NULL_TYPE;

    if (keyPartiallySet.contains(anAnnot)) return DEFAULT_TYPE;
    Iterator iter = responsePartiallySet.iterator();
    while(iter.hasNext()){
      Annotation a = (Annotation) iter.next();
      if (anAnnot.isPartiallyCompatible(a)) return DEFAULT_TYPE;
    }// End while

    iter = responseAnnotList.iterator();
    while(iter.hasNext()){
      Annotation a = (Annotation) iter.next();
      if (anAnnot.isPartiallyCompatible(a)){
         responsePartiallySet.add(a);
         keyPartiallySet.add(anAnnot);
         return DEFAULT_TYPE;
      }// End if
    }// End while

    return MISSING_TYPE;
  }//detectKeyType

  private int detectResponseType(Annotation anAnnot){
    if (anAnnot == null) return NULL_TYPE;

    if (responsePartiallySet.contains(anAnnot)) return PARTIALLY_CORRECT_TYPE;
    Iterator iter = keyPartiallySet.iterator();
    while(iter.hasNext()){
      Annotation a = (Annotation) iter.next();
      if (a.isPartiallyCompatible(anAnnot)) return PARTIALLY_CORRECT_TYPE;
    }// End while

    iter = keyAnnotList.iterator();
    while(iter.hasNext()){
      Annotation a = (Annotation) iter.next();
      if (a.isPartiallyCompatible(anAnnot)){
         responsePartiallySet.add(anAnnot);
         keyPartiallySet.add(a);
         return PARTIALLY_CORRECT_TYPE;
      }// End if
    }// End while

    return INCORRECT_TYPE;
  }//detectResponseType

  /** This method add an DiffsetElement to the DiffSet and also counts the
    * number of compatible, partialCompatible, Incorect and Missing annotations.
    */
  private void addToDiffset(DiffSetElement aDiffSetElement){
    if (aDiffSetElement == null) return;

    diffSet.add(aDiffSetElement);
    // For the Right side (response) the type can be one of the following:
    // PC, I, C
    if (NULL_TYPE != aDiffSetElement.getRightType())
      typeCounter[aDiffSetElement.getRightType()]++;
    // For the left side (key) the type can be : D or M
    if (NULL_TYPE != aDiffSetElement.getLeftType())
      typeCounter[aDiffSetElement.getLeftType()]++;
  }// addToDiffset

  /** This method comes from Resource Interface*/
  public void setFeatures(FeatureMap aFeatureMap){
    featureMap = aFeatureMap;
  }// setFeatures

  /** This method comes from Resource Interface*/
  public FeatureMap getFeatures(){
    return featureMap;
  }// getFeatures


  /* ********************************************************************
   * INNER CLASS
   * ********************************************************************/

  /**
   * A custom table model used to render a table containing the two annotation
   * sets. The columns will be:
   * (KEY) Type, Start, End, Features, empty column, (Response) Type,Start, End,
   * Features
   */
  protected class AnnotationDiffTableModel extends AbstractTableModel{

    /** Constructs an AnnotationDiffTableModel given a data Collection */
    public AnnotationDiffTableModel(Collection data){
      modelData = new ArrayList();
      modelData.addAll(data);
    }// AnnotationDiffTableModel

    /** Constructs an AnnotationDiffTableModel */
    public AnnotationDiffTableModel(){
      modelData = new ArrayList();
    }// AnnotationDiffTableModel

    /** Return the size of data.*/
    public int getRowCount(){
      return modelData.size();
    }//getRowCount

    /** Return the number of columns.*/
    public int getColumnCount(){
      return 9;
    }//getColumnCount

    /** Returns the name of each column in the model*/
    public String getColumnName(int column){
      switch(column){
        case 0: return "Type - Key";
        case 1: return "Start - Key";
        case 2: return "End - Key";
        case 3: return "Features - Key";
        case 4: return "   ";
        case 5: return "Type - Response";
        case 6: return "Start - Response";
        case 7: return "End -Response";
        case 8: return "Features - Response";
        default:return "?";
      }
    }//getColumnName

    /** Return the class type for each column. */
    public Class getColumnClass(int column){
      switch(column){
        case 0: return String.class;
        case 1: return Long.class;
        case 2: return Long.class;
        case 3: return String.class;
        case 4: return String.class;
        case 5: return String.class;
        case 6: return Long.class;
        case 7: return Long.class;
        case 8: return String.class;
        default:return Object.class;
      }
    }//getColumnClass

    /**Returns a value from the table model */
    public Object getValueAt(int row, int column){
      DiffSetElement diffSetElement = (DiffSetElement) modelData.get(row);
      if (diffSetElement == null) return null;
      switch(column){
        // Left Side (Key)
        //Type - Key
        case 0:{
           if (diffSetElement.getLeftAnnotation() == null) return null;
           return diffSetElement.getLeftAnnotation().getType();
        };
        // Start - Key
        case 1:{
           if (diffSetElement.getLeftAnnotation() == null) return null;
           return diffSetElement.getLeftAnnotation().getStartNode().getOffset();
        };
        // End - Key
        case 2:{
           if (diffSetElement.getLeftAnnotation() == null) return null;
           return diffSetElement.getLeftAnnotation().getEndNode().getOffset();
        };
        // Features - Key
        case 3:{
           if (diffSetElement.getLeftAnnotation() == null) return null;
           return diffSetElement.getLeftAnnotation().getFeatures().toString();
        };
        // Empty column
        case 4:{
          return "   ";
        };
        // Right Side (Response)
        //Type - Response
        case 5:{
           if (diffSetElement.getRightAnnotation() == null) return null;
           return diffSetElement.getRightAnnotation().getType();
        };
        // Start - Response
        case 6:{
           if (diffSetElement.getRightAnnotation() == null) return null;
          return diffSetElement.getRightAnnotation().getStartNode().getOffset();
        };
        // End - Response
        case 7:{
           if (diffSetElement.getRightAnnotation() == null) return null;
           return diffSetElement.getRightAnnotation().getEndNode().getOffset();
        };
        // Features - resonse
        case 8:{
           if (diffSetElement.getRightAnnotation() == null) return null;
           return diffSetElement.getRightAnnotation().getFeatures().toString();
        };
        // The hidden column
        case 9:{
          return diffSetElement;
        };
        default:{return null;}
      }// End switch
    }//getValueAt

    public Object getRawObject(int row){
      return modelData.get(row);
    }//getRawObject

    /** Holds the data for TableDiff*/
    private java.util.List modelData = null;

  }//Inner class AnnotationDiffTableModel

  /* ********************************************************************
   * INNER CLASS
   * ********************************************************************/
  /**
    * This class defines a Cell renderer for the AnnotationDiff table
    */
  public class AnnotationDiffCellRenderer extends DefaultTableCellRenderer{

    /** Constructs a randerer with a table model*/
    public AnnotationDiffCellRenderer(){
    }//AnnotationDiffCellRenderer

    private  final Color RED = new Color(255,100,100);
    private  final Color GREEN = new Color(150,255,150);
    private  final Color WHITE = new Color(255,255,255);
    private  final Color BLUE = new Color(0,0,150);
    private  final Color YELLOW = new Color(248,243,120);
    private  final Color BLACK = new Color(0,0,0);

    private Color colors[] = new Color[MAX_TYPES];


    private Color background = GREEN;
    private Color foreground = BLACK;

    /** This method is called by JTable*/
    public Component getTableCellRendererComponent( JTable table,
                                                    Object value,
                                                    boolean isSelected,
                                                    boolean hasFocus,
                                                    int row,
                                                    int column){

      JComponent defaultComp = (JComponent) super.getTableCellRendererComponent(
                                                                  table,
                                                                  value,
                                                                  false,
                                                                  false,
                                                                  row,
                                                                  column);
      // The column number four will be randered using a blank component
      if (column == 4 || value == null)
        return new JPanel();

      if (!(table.getModel().getValueAt(row,9) instanceof DiffSetElement))
        return defaultComp;

      DiffSetElement diffSetElement =
                        (DiffSetElement) table.getModel().getValueAt(row,9);

      if (diffSetElement == null || NULL_TYPE == diffSetElement.getLeftType())
        return defaultComp;

      colors[DEFAULT_TYPE] = WHITE;
      colors[CORRECT_TYPE] = GREEN;
      colors[INCORRECT_TYPE] = RED;
      colors[PARTIALLY_CORRECT_TYPE] = BLUE;
      colors[MISSING_TYPE] = YELLOW;

      if (column < 4){
        background = colors[diffSetElement.getLeftType()];
      }else{
        background = colors[diffSetElement.getRightType()];
      }

      defaultComp.setBackground(background);
//      defaultComp.setForeground(foreground);

      defaultComp.setOpaque(true);
      return defaultComp;
    }//getTableCellRendererComponent
  }// class AnnotationDiffCellRenderer

  /* ********************************************************************
   * INNER CLASS
   * ********************************************************************/
   class AnnotationSetComparator implements java.util.Comparator {

      public AnnotationSetComparator(){}

      public int compare(Object o1, Object o2) {
        if ( !(o1 instanceof gate.Annotation) ||
             !(o2 instanceof gate.Annotation)) return 0;

        gate.Annotation a1 = (gate.Annotation) o1;
        gate.Annotation a2 = (gate.Annotation) o2;

        Long l1 = a1.getStartNode().getOffset();
        Long l2 = a2.getStartNode().getOffset();
        if (l1 != null)
          return l1.compareTo(l2);
        else
          return -1;
      }//compare
    }// class AnnotationSetComparator

  /* ********************************************************************
   * INNER CLASS
   * ********************************************************************/

  /**
    * This class is used for internal purposes. It represents a row from the
    * table.
    */
  protected class DiffSetElement{

    private Annotation leftAnnotation = null;
    private Annotation rightAnnotation = null;
    private int leftType = DEFAULT_TYPE;
    private int rightType = DEFAULT_TYPE;

    /** Constructor for DiffSetlement*/
    public DiffSetElement(){}

    /** Constructor for DiffSetlement*/
    public DiffSetElement( Annotation aLeftAnnotation,
                           Annotation aRightAnnotation,
                           int aLeftType,
                           int aRightType){
      leftAnnotation = aLeftAnnotation;
      rightAnnotation = aRightAnnotation;
      leftType = aLeftType;
      rightType = aRightType;
    }// DiffSetElement

    /** Sets the left annotation*/
    public void setLeftAnnotation(Annotation aLeftAnnotation){
      leftAnnotation = aLeftAnnotation;
    }// setLeftAnnot

    /** Gets the left annotation*/
    public Annotation getLeftAnnotation(){
      return leftAnnotation;
    }// getLeftAnnotation

    /** Sets the right annotation*/
    public void setRightAnnotation(Annotation aRightAnnotation){
      rightAnnotation = aRightAnnotation;
    }// setRightAnnot

    /** Gets the right annotation*/
    public Annotation getRightAnnotation(){
      return rightAnnotation;
    }// getRightAnnotation

    /** Sets the left type*/
    public void setLeftType(int aLeftType){
      leftType = aLeftType;
    }// setLeftType

    /** Get the left type*/
    public int getLeftType(){
      return leftType;
    }// getLeftType

    /** Sets the right type*/
    public void setRightType(int aRightType){
      rightType = aRightType;
    }// setRightType

    /** Get the right type*/
    public int getRightType(){
      return rightType;
    }// getRightType

  }// classs DiffSetElement

} // class AnnotationDiff
