/*  AnnotationEditDialog.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Cristian URSU,  26/Jan/2001
 *
 *  $Id$
 *
 */

package gate.gui;

import java.awt.Frame;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

import java.util.*;
import java.lang.reflect.*;

import gate.*;
import gate.annotation.*;
import gate.util.*;
import gate.creole.*;

/** This class visually adds/edits features from an annotation
  * Features are taken from an AnnotationSchema
  */
public class AnnotationEditDialog extends JDialog {

  // Local data
  private final static int OK = 1;
  private final static int CANCEL = 2;

  // This two fields comes form the show() method
  AnnotationSchema annotSchema = null;
  FeatureMap featureMap = null;

  /** This field is returned when a featureMap was editted or created*/
  FeatureMap responseMap = null;

  FeaturesTableModel tableModel = null;
  Map name2featureSchemaMap = null;

  DefaultListModel listModel = null;

  int buttonPressed = CANCEL;

  // Gui Components
  JTable  featuresTable = null;
  JScrollPane featuresTableScroll = null;
  JScrollPane featuresListScroll = null;
  JButton removeFeatButton = null;
  JButton addFeatButton = null;
  JList   featureSchemaList = null;
  JButton okButton = null;
  JButton cancelButton = null;

  FeaturesEditor featuresEditor;

  /** Constructs an AnnotationEditDialog
    * @param aFram the parent frame of this dialog
    * @param aModal (wheter or not this dialog is modal)
    */
  public AnnotationEditDialog( Frame aFrame, boolean aModal) {

    super(aFrame,aModal);
    this.setLocationRelativeTo(aFrame);

    buildGuiComponents();
    initListeners();
  }//AnnotationEditDialog

  /** Constructs an AnnotationEditDialog using <b>null<b> as a frame and <b>true
    *  </b> as modal value for dialog
    */
  public AnnotationEditDialog() {
    this(null, true);
  }// AnnotationEditDialog

  /** Init local data*/
  protected void initLocalData(){
    // Create the response feature Map
    responseMap = Factory.newFeatureMap();

    if (featureMap == null)
      featureMap = Factory.newFeatureMap();

    name2featureSchemaMap = new HashMap();
    // Construct a set of feature names from feature schema
    Map fSNames2FSMap = new HashMap();

    listModel = new DefaultListModel();
    // Init name2featureSchemaMap
    // If the feature map provided was null, then we are in the creation mode
    Set featuresSch = annotSchema.getFeatureSchemaSet();
    if (featuresSch != null){
      Iterator iter = featuresSch.iterator();
      while (iter.hasNext()){
        FeatureSchema fs = (FeatureSchema) iter.next();
        // If the featureMap doesn't contain the feature from FeatureSchema then
        // add the featureSchema to the list
        if (fs != null){
          fSNames2FSMap.put(fs.getFeatureName(),fs);
          if( !featureMap.containsKey(fs.getFeatureName())){
              name2featureSchemaMap.put(fs.getFeatureName(),fs);
              listModel.addElement(fs.getFeatureName());
          }// end if
        }// end if
      }// end while
      featureSchemaList.setVisibleRowCount(featuresSch.size());
    }// end if

    // Init the table model
    Set tableData = new HashSet();
    Iterator iterator = featureMap.keySet().iterator();
    while (iterator.hasNext()){
      String key = (String) iterator.next();
      // If in featureMap there is a key contained into fSNames2FSMap then
      // add this feature to the table model together with its corresponding
      // FeatureSchema
      if (fSNames2FSMap.keySet().contains(key)){
        // Add it to the table model
        Object value = featureMap.get(key);
        tableData.add(new RowData(value,(FeatureSchema)fSNames2FSMap.get(key)));
      } else
        // Add it to the responseFeatureMap
        // It might be a feature detected by the nameMatcher module, etc.
        // Those features must be preserved.
        responseMap.put(key,featureMap.get(key));
    }// end while

    tableModel = new FeaturesTableModel(tableData);

  }// initLocalData();

  /** This method creates the GUI components and paces them into the layout*/
  protected void buildGuiComponents(){
    this.getContentPane().setLayout(new BoxLayout( this.getContentPane(),
                                                   BoxLayout.Y_AXIS));

    //create the main box
    Box componentsBox = Box.createHorizontalBox();

    componentsBox.add(Box.createHorizontalStrut(5));
    // add the feature table
    featuresTable = new JTable();
    featuresTable.setSelectionMode(
                  ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    featuresTable.setModel(new FeaturesTableModel(new HashSet()));
    featuresTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
    featuresEditor = new FeaturesEditor();
    featuresTable.setDefaultEditor(java.lang.Object.class, featuresEditor);
    featuresTableScroll = new JScrollPane(featuresTable);

    Box box = Box.createVerticalBox();
    box.add(Box.createVerticalStrut(5));
    box.add(new JLabel("Current features"));
    box.add(Box.createVerticalStrut(10));
    box.add(featuresTableScroll);
    box.add(Box.createVerticalStrut(5));

    componentsBox.add(box);
    componentsBox.add(Box.createHorizontalStrut(10));

    // add the remove put buttons
    Box buttBox = Box.createVerticalBox();
    removeFeatButton = new JButton(">>");
    addFeatButton = new JButton("<<");

    buttBox.add(addFeatButton);
    buttBox.add(Box.createVerticalStrut(10));
    buttBox.add(removeFeatButton);

    componentsBox.add(buttBox);

    componentsBox.add(Box.createHorizontalStrut(10));

    // add the Feature Schema list
    featureSchemaList = new JList();
    featureSchemaList.setSelectionMode(
                  ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
//    featureSchemaList.setBorder(BorderFactory.createTitledBorder(
//                    BorderFactory.createEtchedBorder()));

    featuresListScroll = new JScrollPane(featureSchemaList);

    box = Box.createVerticalBox();
    box.add(Box.createVerticalStrut(5));
    box.add(new JLabel("Possible features"));
    box.add(Box.createVerticalStrut(10));
    box.add(featuresListScroll);
    box.add(Box.createVerticalStrut(5));

    componentsBox.add(box);

    componentsBox.add(Box.createHorizontalStrut(5));

    // Add the Ok and Cancel buttons

    this.getContentPane().add(componentsBox);
    this.getContentPane().add(Box.createVerticalStrut(5));

    Box cancelOkBox = Box.createHorizontalBox();
    okButton = new JButton("Ok");
    cancelButton = new JButton("Cancel");

    cancelOkBox.add(okButton);
    cancelOkBox.add(Box.createHorizontalStrut(25));
    cancelOkBox.add(cancelButton);

    this.getContentPane().add(cancelOkBox);
    this.getContentPane().add(Box.createVerticalStrut(5));

    setSize(500,350);
  }//buildGuiComponents();

  /** Init GUI components with values taken from local data*/
  protected void initGuiComponents(){

    featuresTable.setModel(tableModel);
    featureSchemaList.setModel(listModel);

  }//initGuiComponents()

  /** Init all the listeners*/
  protected void initListeners(){

    okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        featuresEditor.stopCellEditing();
        doOk();
      }
    });

    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        requestFocus();
        doCancel();
      }
    });

    // ->
    removeFeatButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        doRemoveFeatures();
      }
    });

    // <-
    addFeatButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        doAddFeatures();
      }
    });
  }//initListeners()

  /** This method remove a feature from the table and adds it to the list*/
  private void doRemoveFeatures(){
    int[] selectedRows = featuresTable.getSelectedRows();

    if (selectedRows.length <= 0) return;

    for (int i = (selectedRows.length - 1); i > -1 ; i--)
      doRemoveFeature(selectedRows[i]);

    tableModel.fireTableDataChanged();
  }// doRemoveFeatures();

  /** This removes the feature @ rowIndex*/
  private void doRemoveFeature(int rowIndex){
    RowData rd =  (RowData) tableModel.data.get(rowIndex);

    name2featureSchemaMap.put(rd.getFeatureSchema().getFeatureName(),
                                                      rd.getFeatureSchema());

    listModel.addElement(rd.getFeatureSchema().getFeatureName());
    tableModel.data.remove(rowIndex);
  }// doRemoveFeature();

  /** This method adds features from the list to the table*/
  private void doAddFeatures(){
    Object[] selectedFeaturesName = featureSchemaList.getSelectedValues();
    for (int i = 0 ; i < selectedFeaturesName.length; i ++){
      doAddFeature((String) selectedFeaturesName[i]);
    }// end for
    tableModel.fireTableDataChanged();
  }//doAddFeatures();

  /** This method adds a feature from the list to the table*/
  private void doAddFeature(String aFeatureName){
      FeatureSchema fs=(FeatureSchema) name2featureSchemaMap.get(aFeatureName);

      // Remove the feature schema from the list
      name2featureSchemaMap.remove(aFeatureName);
      listModel.removeElement(aFeatureName);

      Object value = null;
      if (fs.isDefault() || fs.isFixed())
        value = fs.getFeatureValue();
      if (value == null && fs.isEnumeration()){
        Iterator iter = fs.getPermissibleValues().iterator();
        if (iter.hasNext()) value = iter.next();
      }
      tableModel.data.add(new RowData(value,fs));
  }// doAddFeature();

  /** This method is called when the user press the OK button*/
  private void doOk(){
    buttonPressed = OK;
    this.hide();

    // Construct the response featutre
    Iterator iter = tableModel.data.iterator();
    while (iter.hasNext()){
      RowData rd = (RowData) iter.next();
      responseMap.put(rd.getFeatureSchema().getFeatureName(), rd.getValue());
    }// End while
  }//doOk();

  /** This method is called when the user press the CANCEL button*/
  private void doCancel(){
    buttonPressed = CANCEL;
    this.hide();
  }//doCancel();

  /** This method displays the AnnotationEditDialog in edit mode*/
  public FeatureMap show(FeatureMap aFeatMap, AnnotationSchema anAnnotSchema){
    featureMap = aFeatMap;
    annotSchema = anAnnotSchema;

    if (annotSchema == null) return null;

    if ( annotSchema.getFeatureSchemaSet() == null ||
         annotSchema.getFeatureSchemaSet().size() == 0){

      responseMap = Factory.newFeatureMap();
      if (featureMap != null)
        responseMap.putAll(featureMap);
      return responseMap;
    }// End if

    this.setTitle(annotSchema.getAnnotationName());
    initLocalData();
    initGuiComponents();
    super.show();
    if (buttonPressed == CANCEL)
      return null;
    else{
      return responseMap;
    }
  }// show()

  /** This method displays the AnnotationEditDialog in creating mode*/
  public FeatureMap show(AnnotationSchema anAnnotSchema){
    return show(null,anAnnotSchema);
  }// show()

  // Inner classes
  // TABLE MODEL
  protected class FeaturesTableModel extends AbstractTableModel{

    ArrayList data = null;

    public FeaturesTableModel(Set aData){
      data = new ArrayList(aData);
    }// FeaturesTableModel

    public void fireTableDataChanged(){
      super.fireTableDataChanged();
    }// fireTableDataChanged();

    public int getColumnCount(){return 3;}

    public Class getColumnClass(int columnIndex){
      switch(columnIndex){
        case 0: return String.class;
        case 1: return Object.class;
        case 2: return String.class;
        default: return Object.class;
      }
    }//getColumnClass()

    public String getColumnName(int columnIndex){
      switch(columnIndex){
        case 0: return "Name";
        case 1: return "Value";
        case 2: return "Type";
        default: return "?";
      }
    }//public String getColumnName(int columnIndex)

    public boolean isCellEditable( int rowIndex,
                                   int columnIndex){


        if(columnIndex == 1){
          RowData rd = (RowData) data.get(rowIndex);
          FeatureSchema fs = rd.getFeatureSchema();
          if (fs.isFixed() || fs.isProhibited()) return false;
          else return true;
        }// end if
        if(columnIndex == 0 || columnIndex == 2) return false;
        return false;
    }//isCellEditable

    public int getRowCount(){
      return data.size();
    }//getRowCount()

    /** Returns the value at row,column from Table Model */
    public Object getValueAt( int rowIndex,
                              int columnIndex){

      RowData rd = (RowData) data.get(rowIndex);

      switch(columnIndex){
        case 0: return rd.getFeatureSchema().getFeatureName();
        case 1: return (rd.getValue() == null)? new String(""): rd.getValue();
        case 2: {
                  // Show only the last substring. For example, for
                  // java.lang.Integer -> Integer
                  String type = rd.getFeatureSchema().getValueClassName();
                  if(type == null)
                      return new String("");
                  else{
                    int start = type.lastIndexOf(".");
                    if ((start > -1) && (start < type.length()))
                      return type.substring(start+1,type.length());
                    else return type;
                  }// End if
                }

        default: return "?";
      }// End Switch
    }//getValueAt

    /** Set the value from the Cell Editor into the table model*/
    public void setValueAt( Object aValue,
                            int rowIndex,
                            int columnIndex){

      RowData rd = (RowData) data.get(rowIndex);
      switch(columnIndex){
        case 0:{break;}
        case 1:{
          // Try to perform type conversion
          String className = null;
          String aValueClassName = null;
          // Need to create an object belonging to class "className" based on
          // the string object "aValue"
          if (aValue == null){
            rd.setValue("?");
            return;
          }// End if
          // Get the class name the final object must belong
          className = rd.getFeatureSchema().getValueClassName();
          // Get the class name that aValue object belongs to.
          aValueClassName = aValue.getClass().toString();
          // If there is no class to convert to, let the aValue object as it is
          // and return.
          if (className == null){
              rd.setValue(aValue);
              return;
          }// End if

          // If both classes are the same, then return. There is nothing to
          // convert to
          if (className.equals(aValueClassName)){
            rd.setValue(aValue);
            return;
          }// End if
          // If the class "aValue" object belongs to is not String then return.
          // This method tries to convert a string to various other types.
          if (!"class java.lang.String".equals(aValueClassName)){
            rd.setValue(aValue);
            return;
          }// End if

          // The aValue object belonging to java.lang.String needs to be
          // converted into onother object belonging to "className"
          Class  classObj = null;
          try{
            // Create a class object from className
            classObj = Class.forName(className);
          }catch (ClassNotFoundException cnfex){
            rd.setValue(aValue);
            return;
          }// End catch
          // Get its list of constructors
          Constructor[] constrArray = classObj.getConstructors();
          if (constrArray == null){
            rd.setValue(aValue);
            return;
          }// End if

          // Search for the constructo which takes only one String parameter
          boolean found = false;
          Constructor constructor = null;
          for (int i=0; i<constrArray.length; i++){
            constructor = constrArray[i];
            if ( constructor.getParameterTypes().length == 1 &&
                 "class java.lang.String".equals(
                                constructor.getParameterTypes()[0].toString())
               ){
                  found = true;
                  break;
            }// End if
          }// End for

          if (!found){
            rd.setValue(aValue);
            return;
          }// End if
          try{
            // Try to create an object with this constructor
            Object[] paramsArray = new Object[1];
            paramsArray[0] = aValue;
            Object newValueObject = constructor.newInstance(paramsArray);

            rd.setValue(newValueObject);

          } catch (Exception e){
            rd.setValue("");
          }// End catch

//          rd.setValue(aValue);
          break;
        }// End case
        case 2:{break;}
        case 3:{break;}
        default:{}
      }// End switch
    }// setValueAt();

  }///class FeaturesTableModel extends DefaultTableModel

  class RowData {

    private Object value = null;
    private FeatureSchema featSchema = null;

    /** Constructor*/
    RowData(Object aValue, FeatureSchema aFeatureSchema){
      value = aValue;
      featSchema = aFeatureSchema;
    }//RowData

    public void setValue(Object aValue){
      value = aValue;
    }// setValue();

    public Object getValue(){
      return value;
    }//getValue()

    public void setFeatureSchema(FeatureSchema aFeatureSchema){
      featSchema = aFeatureSchema;
    }// setFeatureSchema();

    public FeatureSchema getFeatureSchema(){
      return featSchema;
    }//getFeatureSchema()

  }// RowData

  // The EDITOR RENDERER

  class FeaturesEditor extends AbstractCellEditor  implements TableCellEditor{

     JComboBox cb = null;
     JTextField tf = null;

    public FeaturesEditor(){}

    public Component getTableCellEditorComponent(JTable table,
                                             Object value,
                                             boolean isSelected,
                                             int row,
                                             int column){
     RowData rd = (RowData) tableModel.data.get(row);
     if (rd.getFeatureSchema().isEnumeration()){
        cb = new JComboBox(rd.getFeatureSchema().getPermissibleValues().toArray());
        cb.setSelectedItem(value);
        tf = null;
        return cb;
     }

     if ( rd.getFeatureSchema().isDefault() ||
          rd.getFeatureSchema().isOptional() ||
          rd.getFeatureSchema().isRequired() ){

          tf = new JTextField(value.toString());
          cb = null;
          return tf;
     }
     return new JLabel(value.toString());
    }//getTableCellEditorComponent

    public Object getCellEditorValue(){
      if (cb != null ) return cb.getSelectedItem();
      if (tf != null ) return tf.getText();
      return new String("");
    }//getCellEditorValue

  }//FeaturesEditor
/*
  public static void main(String[] args){

    try {
      Gate.init();
      FeatureMap parameters = Factory.newFeatureMap();
      parameters.put("xmlFileUrl", AnnotationEditDialog.class.getResource(
                              "/gate/resources/creole/schema/PosSchema.xml"));

      AnnotationSchema annotSchema = (AnnotationSchema)
         Factory.createResource("gate.creole.AnnotationSchema", parameters);

      FeatureMap fm = Factory.newFeatureMap();
      fm.put("time",new Integer(10));

      fm.put("cat","V");
      fm.put("match", new Vector(3));

      AnnotationEditDialog aed = new AnnotationEditDialog(null,true);
      //aed.show(annotSchema);
      aed.show(fm,annotSchema);

    } catch (Exception e){
      e.printStackTrace(Err.getPrintWriter());
    }
  }// main
*/
}//AnnotationEditDialog