/*
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan 23/01/2001
 *
 *  $Id$
 *
 */

package gate.gui;

import java.awt.Frame;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import javax.swing.border.*;

import java.util.*;
import java.net.URL;
import java.io.IOException;
import java.text.*;

import gate.*;
import gate.util.*;
import gate.swing.*;
import gate.creole.*;

public class NewResourceDialog extends JDialog {

  public NewResourceDialog(Frame frame, String title, boolean modal) {
    super(frame, title, modal);
    if(frame instanceof MainFrame){
      fileChooser = ((MainFrame)frame).fileChooser;
    }else{
      fileChooser = new JFileChooser();
    }
    initLocalData();
    initGuiComponents();
    initListeners();

  }// public NewResourceDialog(Frame frame, String title, boolean modal)

  protected void initLocalData(){
    params = new ArrayList();
    listeners = new HashMap();
    if(getParent() instanceof gate.event.ProgressListener)
      listeners.put("gate.event.ProgressListener", getParent());
    if(getParent() instanceof gate.event.StatusListener)
      listeners.put("gate.event.StatusListener", getParent());
  }// protected void initLocalData()

  protected void initGuiComponents(){
    this.getContentPane().setLayout(new BoxLayout(this.getContentPane(),
                                                  BoxLayout.Y_AXIS));
    //name field
    Box nameBox = Box.createHorizontalBox();
    nameBox.add(Box.createHorizontalStrut(5));
    nameBox.add(new JLabel("Name: "));
    nameBox.add(Box.createHorizontalStrut(5));
    nameField = new JTextField(30);
    nameField.setMaximumSize(
        new Dimension(Integer.MAX_VALUE, nameField.getPreferredSize().height));
    nameBox.add(nameField);
    nameBox.add(Box.createHorizontalStrut(5));
    nameBox.add(Box.createHorizontalGlue());
    this.getContentPane().add(nameBox);
    this.getContentPane().add(Box.createVerticalStrut(5));

    //parameters table
    tableModel = new ParametersTableModel();
    table = new XJTable(tableModel);
    table.setDefaultRenderer(ParameterDisjunction.class,
                             new ParameterDisjunctionRenderer());
    table.setDefaultRenderer(Boolean.class,
                             new BooleanRenderer());
    table.setDefaultRenderer(Object.class,
                             new CustomObjectRenderer());
    table.setDefaultRenderer(String.class,
                             new DefaultTableCellRenderer());

    table.setDefaultEditor(ParameterDisjunction.class,
                           new ParameterDisjunctionEditor());
    table.setDefaultEditor(Object.class,
                           new CustomEditor());

    table.setIntercellSpacing(new Dimension(5, 5));
    tableScroll = new JScrollPane(table);
    this.getContentPane().add(tableScroll);
    this.getContentPane().add(Box.createVerticalStrut(5));
    this.getContentPane().add(Box.createVerticalGlue());

    //buttons box
    JPanel buttonsBox = new JPanel();
    buttonsBox.setLayout(new BoxLayout(buttonsBox, BoxLayout.X_AXIS));
    //buttonsBox.setAlignmentX(Component.CENTER_ALIGNMENT);
    buttonsBox.add(Box.createHorizontalStrut(10));
    buttonsBox.add(okBtn = new JButton("OK"));
    buttonsBox.add(Box.createHorizontalStrut(10));
    buttonsBox.add(cancelBtn = new JButton("Cancel"));
    buttonsBox.add(Box.createHorizontalStrut(10));
    this.getContentPane().add(buttonsBox);
    this.getContentPane().add(Box.createVerticalStrut(5));
    setSize(400, 300);
  }// protected void initGuiComponents()


  protected void initListeners(){
    addComponentListener(new ComponentAdapter() {
      public void componentResized(ComponentEvent e) {
        //fire a resize on table. It will automatically resize itself to the
        //right size
        table.setSize(table.getSize().width + 1,
                      table.getSize().height + 1);
      }// public void componentResized(ComponentEvent e)
    });

    okBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        try {
          if(table.getEditingColumn() != -1 && table.getEditingRow() != -1){
            table.editingStopped(new ChangeEvent(
                                  table.getCellEditor(table.getEditingRow(),
                                                    table.getEditingColumn())));
          }
        } catch(Exception ex) {
          return;
        }
        userCanceled = false;
        String name = nameField.getText();
        if(name == null || name.length() == 0){
          JOptionPane.showMessageDialog(getOwner(),
                                        "Please give a name for the new resource!\n",
                                        "Gate", JOptionPane.ERROR_MESSAGE);
        }else{
          hide();
        }
      }//public void actionPerformed(ActionEvent e)
    });

    cancelBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        userCanceled = true;
        if(table.getEditingColumn() != -1 && table.getEditingRow() != -1){
          table.editingCanceled(new ChangeEvent(
                                table.getCellEditor(table.getEditingRow(),
                                                    table.getEditingColumn())));
        }
        hide();
      }//public void actionPerformed(ActionEvent e)
    });
  }//protected void initListeners()

  ParametersTableModel tableModel;
  XJTable table;
  JScrollPane tableScroll;
  JComboBox parametersCombo;
  JButton okBtn, cancelBtn;
  JTextField nameField;
  ResourceData resourceData;
  Resource resource;


  boolean userCanceled;
  ArrayList params;
  JFileChooser fileChooser;
  Map listeners;

  public synchronized Resource show(ResourceData rData) {
    this.resourceData = rData;
    setLocationRelativeTo(getParent());
    nameField.setText("");
    ParameterList pList = rData.getParameterList();
    Iterator parIter = pList.getInitimeParameters().iterator();
    params.clear();
    while(parIter.hasNext()){
      params.add(new ParameterDisjunction((List)parIter.next()));
    }
    tableModel.fireTableDataChanged();

    table.setPreferredSize(null);

    Dimension dim = table.getPreferredSize();
    if(dim != null){
      dim.height += table.getTableHeader().getPreferredSize().height +
                    tableScroll.getInsets().top +
                    tableScroll.getInsets().bottom;
      dim.width +=  tableScroll.getInsets().left +
                    tableScroll.getInsets().right;
      tableScroll.setPreferredSize(dim);
    }
    pack();
    super.show();
    if(userCanceled) return null;
    else{
      //create the new resource
      FeatureMap params = Factory.newFeatureMap();
      for(int i=0; i< tableModel.getRowCount(); i++){
        ParameterDisjunction pDisj = (ParameterDisjunction)
                                     tableModel.getValueAt(i,0);
        if(pDisj.getValue() != null){
          params.put(pDisj.getName(), pDisj.getValue());
        }
      }
      Resource res;
      gate.event.StatusListener sListener = (gate.event.StatusListener)
                                  listeners.get("gate.event.StatusListener");
      if(sListener != null) sListener.statusChanged("Loading " +
                                                    nameField.getText() +
                                                    "...");

      gate.event.ProgressListener pListener = (gate.event.ProgressListener)
                                  listeners.get("gate.event.ProgressListener");
      if(pListener != null){
        pListener.progressChanged(0);
      }

      try {
        long startTime = System.currentTimeMillis();
        FeatureMap features = Factory.newFeatureMap();
        Gate.setName(features, nameField.getText());
        res = Factory.createResource(rData.getClassName(), params,
                                     features, listeners);
        long endTime = System.currentTimeMillis();
        if(sListener != null) sListener.statusChanged(
            nameField.getText() + " loaded in " +
            NumberFormat.getInstance().format(
            (double)(endTime - startTime) / 1000) + " seconds");
        if(pListener != null) pListener.processFinished();
      } catch(ResourceInstantiationException rie) {
        JOptionPane.showMessageDialog(getOwner(),
                                      "Resource could not be created!\n" +
                                      rie.toString(),
                                      "Gate", JOptionPane.ERROR_MESSAGE);
        rie.printStackTrace(Err.getPrintWriter());
        res = null;
        if(sListener != null) sListener.statusChanged("Error loading " +
                                                      nameField.getText() +
                                                      "!");
        if(pListener != null) pListener.processFinished();
      }
      return res;
    }
  }// public synchronized Resource show(ResourceData rData)

  int getRowCnt(){
    return params.size();
  }

  //inner classes
  protected class ParametersTableModel extends AbstractTableModel{

    public ParametersTableModel(){
    }

    public void fireTableDataChanged(){
      super.fireTableDataChanged();
    }// public void fireTableDataChanged()

    public int getColumnCount(){return 4;}

    public Class getColumnClass(int columnIndex){
      switch(columnIndex){
        case 0: return ParameterDisjunction.class;
        case 1: return String.class;
        case 2: return Boolean.class;
        case 3: return Object.class;
        default: return Object.class;
      }
    }// public Class getColumnClass(int columnIndex)

    public String getColumnName(int columnIndex){
      switch(columnIndex){
        case 0: return "Name";
        case 1: return "Type";
        case 2: return "Required";
        case 3: return "Value";
        default: return "?";
      }
    }//public String getColumnName(int columnIndex)

    public boolean isCellEditable(int rowIndex,
                              int columnIndex) {
        if(columnIndex == 3) return true;
        if(columnIndex == 1 || columnIndex == 2) return false;
        ParameterDisjunction pDisj =
                      (ParameterDisjunction)params.get(rowIndex);
        return pDisj.size() > 1;
    }// public boolean isCellEditable

    public int getRowCount(){
      return getRowCnt();
      /*
      if(params == null){
        System.out.println("Null indeed!");
        return 0;
      }else{
        System.out.println("Like hell null!");
        return params.size();
      }
      */
    }// public int getRowCount()

    public Object getValueAt(int rowIndex,
                         int columnIndex) {
      ParameterDisjunction pDisj =
                    (ParameterDisjunction)params.get(rowIndex);
      switch(columnIndex){
        case 0: return pDisj;
        case 1: return pDisj.getType();
        case 2: return pDisj.getRequired();
        case 3: return pDisj.getValue();
        default: return "?";
      }
    }// public Object getValueAt

    public void setValueAt(Object aValue,
                       int rowIndex,
                       int columnIndex){
      ParameterDisjunction pDisj =
                    (ParameterDisjunction)params.get(rowIndex);
      switch(columnIndex){
        case 0:{
          pDisj.setSelectedIndex(((Integer)aValue).intValue());
          break;
        }
        case 1:{
          break;
        }
        case 2:{
          break;
        }
        case 3:{
          pDisj.setValue((String)aValue);
          break;
        }
        default:{}
      }
    }// public void setValueAt
  }///class FeaturesTableModel extends DefaultTableModel

  class ParameterDisjunctionRenderer extends DefaultTableCellRenderer {
    public Component getTableCellRendererComponent(JTable table,
                                                   Object value,
                                                   boolean isSelected,
                                                   boolean hasFocus,
                                                   int row,
                                                   int column) {
      ParameterDisjunction pDisj = (ParameterDisjunction)value;
      String text = pDisj.getName();
      if(pDisj.size() > 1) text += " [more...]";
      //prepare the renderer
      Component comp = super.getTableCellRendererComponent(table,
                                                           text,
                                                           isSelected, hasFocus,
                                                           row, column);
      String type = pDisj.getType();
      String iconName = "param.gif";
      if(Gate.getCreoleRegister().containsKey(type)){
        ResourceData rData = (ResourceData)Gate.getCreoleRegister().get(type);
        if(rData != null) iconName = rData.getIcon();
      }
      setIcon(MainFrame.getIcon(iconName));
      return this;
/*
      if(comp instanceof JLabel){
        try{
          JLabel label = (JLabel)comp;
          label.setToolTipText(pDisj.getComment());
          label.setHorizontalTextPosition(JLabel.LEFT);
          if(pDisj.size() > 1){
            label.setIcon(MainFrame.getIcon("down.gif"));
          }else{
            label.setIcon(null);
          }
        }catch(Exception e){}
      }
      return comp;
*/
    }// public Component getTableCellRendererComponent
  }//class ParameterDisjunctionRenderer


  class CustomObjectRenderer extends ObjectRenderer {
    CustomObjectRenderer() {
      button = new JButton(MainFrame.getIcon("loadFile.gif"));
      button.setToolTipText("Set from file...");
      textButtonBox = new JPanel();
      textButtonBox.setLayout(new BoxLayout(textButtonBox, BoxLayout.X_AXIS));
      textButtonBox.setOpaque(false);
    }// CustomObjectRenderer()

    public Component getTableCellRendererComponent(JTable table,
                                                   Object value,
                                                   boolean isSelected,
                                                   boolean hasFocus,
                                                   int row,
                                                   int column) {
      //prepare the renderer
      if(value == null){
        //ensure a reasonable space is reserved (20 spaces)
        super.getTableCellRendererComponent(table, "                    ",
                                            isSelected, hasFocus, row, column);
      }else{
        super.getTableCellRendererComponent(table, value, isSelected,
                                            hasFocus, row, column);
      }

      String type = (String)table.getValueAt(row, 1);
      if(type.equals("java.net.URL")){
        setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        textButtonBox.removeAll();
        textButtonBox.add(this);
        textButtonBox.add(Box.createHorizontalGlue());
        textButtonBox.add(button);
        return textButtonBox;
      }else{
        setMaximumSize(getPreferredSize());
        return this;
      }

//      return this;
    }// public Component getTableCellRendererComponent

    JButton button;
    JPanel textButtonBox;
  }//class ObjectRenderer extends DefaultTableCellRenderer

  class ParameterDisjunctionEditor extends DefaultCellEditor{
    public ParameterDisjunctionEditor(){
      super(new JComboBox());
      combo = (JComboBox)super.getComponent();
    }// public ParameterDisjunctionEditor()

    public Component getTableCellEditorComponent(JTable table,
                                             Object value,
                                             boolean isSelected,
                                             int row,
                                             int column){
     ParameterDisjunction pDisj = (ParameterDisjunction)value;

     combo.setModel(new DefaultComboBoxModel(pDisj.getNames()));
     return combo;
    }// public Component getTableCellEditorComponent

    public Object getCellEditorValue(){
      return new Integer(combo.getSelectedIndex());
    }
    JComboBox combo;
  }// class ParameterDisjunctionEditor extends DefaultCellEditor

  class CustomEditor extends DefaultCellEditor{
    CustomEditor(){
      super(new JTextField(10));
      setClickCountToStart(1);
      textField = (JTextField)getComponent();
      button = new JButton(MainFrame.getIcon("loadFile.gif"));
      button.setToolTipText("Set from file...");
      textButtonBox = Box.createHorizontalBox();

      button.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          fileChooser.setDialogTitle("Select file");
          fileChooser.setFileSelectionMode(fileChooser.FILES_AND_DIRECTORIES);
          int res = fileChooser.showOpenDialog(NewResourceDialog.this);
          if(res == fileChooser.APPROVE_OPTION){
            try{
              textField.setText(fileChooser.getSelectedFile().
                                toURL().toExternalForm());
            }catch(IOException ioe){}
          }
        }//public void actionPerformed(ActionEvent e)
      });
    }// CustomEditor()

    public Component getTableCellEditorComponent(JTable table,
                                                 Object value,
                                                 boolean isSelected,
                                                 int row,
                                                 int column) {
      String type = (String)table.getValueAt(row, 1);
      if(type.equals("java.net.URL")){
        textButtonBox.removeAll();
        textButtonBox.add(super.getTableCellEditorComponent(table, value,
                                                            isSelected,
                                                            row, column));
        textButtonBox.add(button);
        return textButtonBox;
      } else {
        return super.getTableCellEditorComponent(table, value, isSelected,
                                                 row, column);
      }
    }// public Component getTableCellEditorComponent

    JButton button;
    JTextField textField;
    Box textButtonBox;
  }// class CustomEditor extends DefaultCellEditor

  class ParameterDisjunction {
    /**
     * gets a list of {@link gate.creole.Parameter}
     */
    public ParameterDisjunction(List options){
      this.options = options;
      Iterator paramsIter = options.iterator();
      names = new String[options.size()];
      int i = 0;
      while(paramsIter.hasNext()){
        names[i++] = ((Parameter)paramsIter.next()).getComment();
      }
      values = new Object[options.size()];
      setSelectedIndex(0);
    }

    public void setSelectedIndex(int index){
      selectedIndex = index;
      currentParameter = (Parameter)options.get(selectedIndex);
      if(values[selectedIndex] == null){
        try{
          values[selectedIndex] = currentParameter.getDefaultValue();
        }catch(Exception e){
          values[selectedIndex] = "";
        }
      }
      tableModel.fireTableDataChanged();
    }

    public int size(){
      return options.size();
    }

    public Boolean getRequired(){
      return new Boolean(!currentParameter.isOptional());
    }

    public String getName(){
      return currentParameter.getName();
    }

    public String getComment(){
      return currentParameter.getComment();
    }

    public String getType(){
      return currentParameter.getTypeName();
    }

    public String[] getNames(){
      return names;
    }

    public void setValue(String stringValue){
      Object oldValue = values[selectedIndex];
      try{
        values[selectedIndex] = currentParameter.
                                calculateValueFromString(stringValue);
      }catch(Exception e){
        values[selectedIndex] = oldValue;
        JOptionPane.showMessageDialog(getOwner(),
                                      "Invalid value!\n" +
                                      "Is it the right type?",
                                      "Gate", JOptionPane.ERROR_MESSAGE);
      }
    }

    public Object getValue(){
      return values[selectedIndex];
    }


    int selectedIndex;
    List options;
    boolean required;
    String typeName;
    String name;
    String[] names;
    Parameter currentParameter;
    Object[] values;
  }// class ParameterDisjunction

}//class NewResourceDialog