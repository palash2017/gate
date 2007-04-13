/*
 *  Copyright (c) 1998-2005, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  AnnotatioListView.java
 *
 *  Valentin Tablan, May 25, 2004
 *
 *  $Id$
 */

package gate.gui.docview;

import gate.*;
import gate.creole.*;
import gate.event.AnnotationEvent;
import gate.event.AnnotationListener;
import gate.gui.ResizableVisualResource;
import gate.swing.XJTable;
import gate.util.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.AbstractTableModel;

/**
 * A tabular view for a list of annotations.
 * Used as part of the document viewer to display all the annotation currently
 * highlighted.
 */
public class AnnotationListView extends AbstractDocumentView
		implements AnnotationListener{
  public AnnotationListView(){
    annHandlersList = new AnnHandlerList();
  }
  
  private static class AnnHandlerList
    extends ArrayList<AnnotationHandler>{
    public int indexOfTag(Object tag) {
      for(int i = 0; i < size(); i++){
        if(get(i).tag == tag) return i;
      }
      return -1;
    }
  }

  /* (non-Javadoc)
   * @see gate.gui.docview.AbstractDocumentView#initGUI()
   */
  protected void initGUI() {
    editorsCache = new HashMap();
    tableModel = new AnnotationTableModel();
    table = new XJTable(tableModel);
    table.setAutoResizeMode(XJTable.AUTO_RESIZE_OFF);
    table.setSortable(true);
    table.setSortedColumn(START_COL);
    table.setIntercellSpacing(new Dimension(2, 0));
    scroller = new JScrollPane(table);

    mainPanel = new JPanel();
    mainPanel.setLayout(new GridBagLayout());
    GridBagConstraints constraints = new GridBagConstraints();

    constraints.gridx = 0;
    constraints.gridy = 0;
    constraints.weightx = 1;
    constraints.weighty = 1;
    constraints.fill= GridBagConstraints.BOTH;
    mainPanel.add(scroller, constraints);

    constraints.gridy = 1;
    constraints.weightx = 0;
    constraints.weighty = 0;
    constraints.fill= GridBagConstraints.NONE;
    constraints.anchor = GridBagConstraints.WEST;
    statusLabel = new JLabel();
    mainPanel.add(statusLabel, constraints);

    //get a pointer to the text view used to display
    //the selected annotations
    Iterator centralViewsIter = owner.getCentralViews().iterator();
    while(textView == null && centralViewsIter.hasNext()){
      DocumentView aView = (DocumentView)centralViewsIter.next();
      if(aView instanceof TextualDocumentView)
        textView = (TextualDocumentView)aView;
    }

    initListeners();
  }

  public Component getGUI(){
    return mainPanel;
  }
  protected void initListeners(){
//    table.addComponentListener(new ComponentAdapter(){
//      public void componentShown(ComponentEvent e){
//        //trigger a resize for the columns
//        table.adjustSizes();
//      }
//    });

    tableModel.addTableModelListener(new TableModelListener(){
      public void tableChanged(TableModelEvent e){
        statusLabel.setText(
                Integer.toString(tableModel.getRowCount()) +
                " Annotations (" +
                Integer.toString(table.getSelectedRowCount()) +
                " selected)");
      }
    });


    table.getSelectionModel().
      addListSelectionListener(new ListSelectionListener(){
        public void valueChanged(ListSelectionEvent e){
          if(!isActive())return;
          statusLabel.setText(
                  Integer.toString(tableModel.getRowCount()) +
                  " Annotations (" +
                  Integer.toString(table.getSelectedRowCount()) +
                  "selected)");
          //blink the selected annotations
          textView.removeAllBlinkingHighlights();
          showHighlights();
        }
    });

    table.addMouseListener(new MouseListener() {
      public void mouseClicked(final MouseEvent me) {
        int viewRow = table.rowAtPoint(me.getPoint());
        final int modelRow = viewRow == -1 ?
                             viewRow : 
                             table.rowViewToModel(viewRow);
        
        // right click
        if(javax.swing.SwingUtilities.isRightMouseButton(me)) {
          JPopupMenu popup = new JPopupMenu();
          
          Action deleteAction = new AbstractAction("Delete"){
            public void actionPerformed(ActionEvent evt){
              int[] rows = table.getSelectedRows();
              if(rows == null || rows.length == 0){
                //no selection -> use row under cursor
                if(modelRow == -1) return;
                rows = new int[]{modelRow};
              }

              ArrayList<AnnotationHandler> handlers = 
                new ArrayList<AnnotationHandler>();

              for(int i = 0; i < rows.length; i++){
                handlers.add(annHandlersList.get(table.rowViewToModel(rows[i])));
              }

              for(AnnotationHandler aHandler : handlers) {
                aHandler.set.remove(aHandler.ann);
                removeAnnotation(aHandler.tag);
              }
            }
          };
          popup.add(deleteAction);
          
          //add the custom edit actions
          if(modelRow != -1){
            AnnotationHandler aHandler = annHandlersList.get(modelRow);
            List editorClasses = Gate.getCreoleRegister().
              getAnnotationVRs(aHandler.ann.getType());
            if(editorClasses != null && editorClasses.size() > 0){
              popup.addSeparator();
              Iterator editorIter = editorClasses.iterator();
              while(editorIter.hasNext()){
                String editorClass = (String) editorIter.next();
                AnnotationVisualResource editor = (AnnotationVisualResource)
                  editorsCache.get(editorClass);
                if(editor == null){
                  //create the new type of editor
                  try{
                    editor = (AnnotationVisualResource)
                             Factory.createResource(editorClass);
                    editorsCache.put(editorClass, editor);
                  }catch(ResourceInstantiationException rie){
                    rie.printStackTrace(Err.getPrintWriter());
                  }
                }
                popup.add(new EditAnnotationAction(aHandler.set, 
                        aHandler.ann, editor));
              }
            }
          }
          
          
          
          popup.show(table, me.getX(), me.getY());
        }
      }
      public void mouseReleased(MouseEvent me) { }
      public void mouseEntered(MouseEvent me) { }
      public void mouseExited(MouseEvent me) { }
      public void mousePressed(MouseEvent me) { }
    });
    /* End */

  }
  /* (non-Javadoc)
   * @see gate.gui.docview.AbstractDocumentView#registerHooks()
   */
  protected void registerHooks() {
    //this is called on activation
    showHighlights();
  }

  /* (non-Javadoc)
   * @see gate.gui.docview.AbstractDocumentView#unregisterHooks()
   */
  protected void unregisterHooks() {
    //this is called on de-activation
    //remove highlights
    textView.removeAllBlinkingHighlights();
  }

  /* (non-Javadoc)
   * @see gate.gui.docview.DocumentView#getType()
   */
  public int getType() {
    return HORIZONTAL;
  }
  protected void guiShown(){
    tableModel.fireTableDataChanged();
  }

  protected void showHighlights(){
    int[] rows = table.getSelectedRows();
    AnnotationHandler aHandler = null;
    for(int i = 0; i < rows.length; i++){
      aHandler = annHandlersList.get(table.rowViewToModel(rows[i]));
      textView.addBlinkingHighlight(aHandler.ann);
    }    
    //scroll to show the last highlight
    if(aHandler != null && aHandler.ann != null)
        textView.scrollAnnotationToVisible(aHandler.ann);
  }

  public void addAnnotation(Object tag, Annotation ann, AnnotationSet set){
    AnnotationHandler aHandler = new AnnotationHandler(set, ann, tag);
    annHandlersList.add(aHandler);
    int row = annHandlersList.size() -1;
    if(tableModel != null) tableModel.fireTableRowsInserted(row, row);
    //listen for the new annotation's events
    ann.addAnnotationListener(this);
  }

  public void removeAnnotation(Object tag){
    int row = annHandlersList.indexOfTag(tag);
    if(row >= 0){
      AnnotationHandler aHandler = annHandlersList.get(row);
      aHandler.ann.removeAnnotationListener(this);
      if(tableModel != null) tableModel.fireTableRowsDeleted(row, row);
    }
  }

  /**
   * Adds a batch of annotations in one go. The tags and annotations collections
   * are accessed through their iterators which are expected to return the
   * corresponding tag for the right annotation.
   * This method does not assume it was called from the UI Thread.
   * @param tags a collection of tags
   * @param annotations a collection of annotations
   * @param set the annotation set to which all the annotations belong.
   */
  public void addAnnotations(Collection tags, Collection annotations,
          AnnotationSet set){
    if(tags.size() != annotations.size()) throw new GateRuntimeException(
            "Invalid invocation - different numbers of annotations and tags!");
    Iterator tagIter = tags.iterator();
    Iterator annIter = annotations.iterator();
    while(tagIter.hasNext()){
      Object tag = tagIter.next();
      Annotation ann = (Annotation)annIter.next();
      AnnotationHandler aHandler = new AnnotationHandler(set, ann, tag);
      annHandlersList.add(aHandler);
      //listen for the new annotation's events
      ann.addAnnotationListener(this);
    }
    if(tableModel != null) tableModel.fireTableDataChanged();
  }

  public void removeAnnotations(Collection tags){
    Iterator tagIter = tags.iterator();
    while(tagIter.hasNext()){
      Object tag = tagIter.next();
      int row = annHandlersList.indexOfTag(tag);
      if(row >= 0){
        AnnotationHandler aHandler = annHandlersList.get(row);
        aHandler.ann.removeAnnotationListener(this);
        annHandlersList.remove(row);
      }
    }
    if(tableModel != null) tableModel.fireTableDataChanged();
  }

  public void annotationUpdated(AnnotationEvent e){
    //update all occurrences of this annotation
   // if annotations tab has not been set to visible state
  	// table will be null.
  	if(table == null)	return;
    //save selection
  	int[] selection = table.getSelectedRows();
    Annotation ann = (Annotation)e.getSource();
    if(tableModel != null){
      for(int i = 0; i < annHandlersList.size(); i++){
        AnnotationHandler aHandler = annHandlersList.get(i);
        if(aHandler.ann == ann)tableModel.fireTableRowsUpdated(i, i);
      }
    }
    //restore selection
    table.clearSelection();
    if(selection != null){
      for(int i = 0; i < selection.length; i++){
        table.getSelectionModel().addSelectionInterval(selection[i], 
                selection[i]);
      }
    }
  }

  /**
   * Selects the annotation for the given tag.
   * @param tag the tag of the annotation to be selected.
   */
  public void selectAnnotationForTag(Object tag){
    int modelPosition = annHandlersList.indexOfTag(tag);
    
    if(modelPosition != -1){
      int tablePosition = table.rowModelToView(modelPosition);
      table.getSelectionModel().setSelectionInterval(tablePosition, 
              tablePosition);
      table.scrollRectToVisible(table.getCellRect(tablePosition, 0, false));
    }
  }
  
  class AnnotationTableModel extends AbstractTableModel{
    public int getRowCount(){
      return annHandlersList.size();
    }

    public int getColumnCount(){
      return 5;
    }

    public String getColumnName(int column){
      switch(column){
        case TYPE_COL: return "Type";
        case SET_COL: return "Set";
        case START_COL: return "Start";
        case END_COL: return "End";
        case FEATURES_COL: return "Features";
        default: return "?";
      }
    }

    public Class getColumnClass(int column){
      switch(column){
        case TYPE_COL: return String.class;
        case SET_COL: return String.class;
        case START_COL: return Long.class;
        case END_COL: return Long.class;
        case FEATURES_COL: return String.class;
        default: return String.class;
      }
    }

    public boolean isCellEditable(int rowIndex, int columnIndex){
      return false;
    }

    public Object getValueAt(int row, int column){
      if(row >= annHandlersList.size()) return null;
      AnnotationHandler aHandler = annHandlersList.get(row);
      switch(column){
        case TYPE_COL: return aHandler.ann.getType();
        case SET_COL: return aHandler.set.getName();
        case START_COL: return aHandler.ann.getStartNode().getOffset();
        case END_COL: return aHandler.ann.getEndNode().getOffset();
        case FEATURES_COL:
          //sort the features by name
          FeatureMap features = aHandler.ann.getFeatures();
          List keyList = new ArrayList(features.keySet());
          Collections.sort(keyList);
          StringBuffer strBuf = new StringBuffer("{");
          Iterator keyIter = keyList.iterator();
          boolean first = true;
          while(keyIter.hasNext()){
            Object key = keyIter.next();
            Object value = features.get(key);
            if(first){
              first = false;
            }else{
              strBuf.append(", ");
            }
            strBuf.append(key.toString());
            strBuf.append("=");
            strBuf.append(value == null ? "[null]" : value.toString());
          }
          strBuf.append("}");
          return strBuf.toString();
        default: return "?";
      }
    }

  }

  protected static class AnnotationHandler{
    public AnnotationHandler(AnnotationSet set, Annotation ann, Object tag){
      this.ann = ann;
      this.set = set;
      this.tag = tag;
    }

    @Override
    public boolean equals(Object obj) {
      if(obj instanceof AnnotationHandler) obj = ((AnnotationHandler)obj).tag;
      return tag == null ?
             obj == null :
             tag.equals(obj);
    }
    
    @Override
    public int hashCode() {
      return tag.hashCode();
    }
    Annotation ann;
    AnnotationSet set;
    Object tag;
  }

  protected class EditAnnotationAction extends AbstractAction{
    public EditAnnotationAction(AnnotationSet set, Annotation ann, 
            AnnotationVisualResource editor){
      this.set = set;
      this.ann = ann;
      this.editor = editor;
      ResourceData rData =(ResourceData)Gate.getCreoleRegister().
          get(editor.getClass().getName()); 
      if(rData != null){
        title = rData.getName();
        putValue(NAME, "Edit with " + title);
        putValue(SHORT_DESCRIPTION, rData.getComment());
      }
    }
    
    public void actionPerformed(ActionEvent evt){
      JScrollPane scroller = new JScrollPane((Component)editor); 
      editor.setTarget(set);
      editor.setAnnotation(ann);
      JOptionPane optionPane = new JOptionPane(scroller,
              JOptionPane.QUESTION_MESSAGE, 
              JOptionPane.OK_CANCEL_OPTION, 
              null, new String[]{"OK", "Cancel"});
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      scroller.setMaximumSize(new Dimension((int)(screenSize.width * .75), 
              (int)(screenSize.height * .75)));
      JDialog dialog = optionPane.createDialog(AnnotationListView.this.getGUI(),
              title);
      dialog.setModal(true);
      dialog.setResizable(true);
      dialog.setVisible(true);
      try{
        if(optionPane.getValue().equals("OK")) editor.okAction();
        else editor.cancelAction();
      }catch(GateException ge){
        throw new GateRuntimeException(ge);
      }
    }
    
    String title;
    Annotation ann;
    AnnotationSet set;
    AnnotationVisualResource editor;
  }
  
  protected XJTable table;
  protected AnnotationTableModel tableModel;
  protected JScrollPane scroller;

  /**
   * Stores the {@link AnnotationHandler} objects representing the annotations
   * displayed by this view.
   */
  protected AnnHandlerList annHandlersList;
  
  protected JPanel mainPanel;
  protected JLabel statusLabel;
  protected TextualDocumentView textView;
  /**
   * A map that stores instantiated annotations editors in order to avoid the 
   * delay of building them at each request;
   */
  protected Map editorsCache;

  private static final int TYPE_COL = 0;
  private static final int SET_COL = 1;
  private static final int START_COL = 2;
  private static final int END_COL = 3;
  private static final int FEATURES_COL = 4;

}
