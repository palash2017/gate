/*
 *  Copyright (c) 1998-2004, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan 06/03/2001
 *
 *  $Id$
 *
 */
package gate.swing;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.tree.*;


/**
 * A TreeTable component. That is a component that looks like a table apart
 * from the first column that contains a tree.
 */
public class JTreeTable extends XJTable {

  /**The tree used to render the first column*/
  protected CustomJTree tree;

  /**The model for this component*/
  protected TreeTableModel treeTableModel;

  /**
   * Constructs a JTreeTable from a model
   */
  public JTreeTable(TreeTableModel model) {
    super();
    this.treeTableModel = model;

    initLocalData();
    initGuiComponents();
    initListeners();

    super.setSortable(false);
  }

  protected void initLocalData(){
  }

  protected void initGuiComponents(){
    // Create the tree. It will be used by the table renderer to draw the cells
    //in the first column
    tree = new CustomJTree();
    tree.setModel(treeTableModel);
    tree.setEditable(false);

    // Install a tableModel representing the visible rows in the tree.
    super.setModel(new TreeTableModelAdapter(treeTableModel));

    // Force the JTable and JTree to share their row selection models.
    tree.setSelectionModel(new DefaultTreeSelectionModel() {
      //extend the constructor
      {
        setSelectionModel(listSelectionModel);
      }
    });

    setAutoCreateColumnsFromModel(false);
    //Install the renderer and editor
    getColumnModel().getColumn(0).setCellRenderer(new TreeTableCellRenderer());
    getColumnModel().getColumn(0).setCellEditor(new TreeTableCellEditor());

    setShowGrid(false);
  }

  protected void initListeners(){
    //install the mouse listener that will forward the mouse events to the tree
    addMouseListener(new MouseHandler());

    getColumnModel().getColumn(0).addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent e) {
        if(e.getPropertyName().equals("width")){
          int width = ((Number)e.getNewValue()).intValue();
          int height = tree.getSize().height;
          tree.setSize(width, height);
        }
      }
    });
  }

  /**
   * Overrides the setSortable() method from {@link XJTable} so the table is NOT
   * sortable. In a tree-table component the ordering for the rows is given by
   * the structure of the tree and they cannot be reordered.
   */
  public void setSortable(boolean b){
    throw new UnsupportedOperationException(
          "A JTreeTable component cannot be sortable!\n" +
          "The rows order is defined by the tree structure.");
  }

  public JTree getTree(){
    return tree;
  }

  public void expandPath(TreePath path){
    tree.expandPath(path);
  }

  public void expandRow(int row){
    tree.expandRow(row);
  }

  /**
   * The renderer used to display the table cells containing tree nodes.
   * Will use an internal JTree object to paint the nodes.
   */
  public class TreeTableCellRenderer extends DefaultTableCellRenderer {
    public Component getTableCellRendererComponent(JTable table,
                     Object value,
                     boolean isSelected,
                     boolean hasFocus,
                     int row, int column) {
      tree.setVisibleRow(row);
      return tree;
    }
  }//public class TreeTableCellRenderer extends DefaultTableCellRenderer

  /**
   * The editor used to edit the nodes in the tree. It only forwards the
   * requests to the tree's editor.
   */
  class TreeTableCellEditor extends DefaultCellEditor
                            implements TableCellEditor {
    TreeTableCellEditor(){
      super(new JTextField());
      //placeHolder = new PlaceHolder();
      editor = tree.getCellEditor();
      setClickCountToStart(0);
    }

    public Component getTableCellEditorComponent(JTable table,
                                                 Object value,
                                                 boolean isSelected,
                                                 int row,
                                                 int column) {

      editor = tree.getCellEditor();

      editor.addCellEditorListener(new CellEditorListener() {
        public void editingStopped(ChangeEvent e) {
          fireEditingStopped();
        }

        public void editingCanceled(ChangeEvent e) {
          fireEditingCanceled();
        }
      });

      editorComponent = editor.getTreeCellEditorComponent(
                    tree, tree.getPathForRow(row).getLastPathComponent(),
                    isSelected, tree.isExpanded(row),
                    tree.getModel().isLeaf(
                      tree.getPathForRow(row).getLastPathComponent()
                    ),
                    row);
      Box box = Box.createHorizontalBox();
      box.add(Box.createHorizontalStrut(tree.getRowBounds(row).x));
      box.add(editorComponent);
      return box;
//      return editorComponent;
    }

    public Object getCellEditorValue() {
      return editor == null ? null : editor.getCellEditorValue();
    }

    public boolean stopCellEditing(){
      return editor == null ? true : editor.stopCellEditing();
    }

    public void cancelCellEditing(){
      if(editor != null) editor.cancelCellEditing();
    }

    TreeCellEditor editor;
    Component editorComponent;
  }

  /**
   * Class used to convert the mouse events from the JTreeTable component space
   * into the JTree space. It is used to forward the mouse events to the tree
   * if they occured in the space used by the tree.
   */
  class MouseHandler extends MouseAdapter {
    public void mousePressed(MouseEvent e) {
      if(columnAtPoint(e.getPoint()) == 0){
        tree.dispatchEvent(convertEvent(e));
      }
    }

    public void mouseReleased(MouseEvent e) {
      if(columnAtPoint(e.getPoint()) == 0){
        tree.dispatchEvent(convertEvent(e));
      }
    }

    public void mouseClicked(MouseEvent e) {
      if(columnAtPoint(e.getPoint()) == 0){
        tree.dispatchEvent(convertEvent(e));
      }
    }


    public void mouseEntered(MouseEvent e) {
      if(columnAtPoint(e.getPoint()) == 0){
        tree.dispatchEvent(convertEvent(e));
      }
    }

    public void mouseExited(MouseEvent e) {
      if(columnAtPoint(e.getPoint()) == 0){
        tree.dispatchEvent(convertEvent(e));
      }
    }

    protected MouseEvent convertEvent(MouseEvent e){
      int column = 0;
      int row = rowAtPoint(e.getPoint());

      //move the event from table to tree coordinates
      Rectangle tableCellRect = getCellRect(row, column, false);
      Rectangle treeCellRect = tree.getRowBounds(row);
      int dx = 0;
      if(tableCellRect != null) dx = -tableCellRect.x;
      int dy = 0;
      if(tableCellRect !=null && treeCellRect != null)
        dy = treeCellRect.y -tableCellRect.y;
      e.translatePoint(dx, dy);


      return new MouseEvent(
        tree, e.getID(), e.getWhen(), e.getModifiers(),
        e.getX(), e.getY(), e.getClickCount(), e.isPopupTrigger()
      );
    }
  }

  /**
   * A wrapper that reads a TreeTableModel and behaves as a TableModel
   */
  class TreeTableModelAdapter extends AbstractTableModel{
    public TreeTableModelAdapter(TreeTableModel treeTableModel) {
      tree.addTreeExpansionListener(new TreeExpansionListener() {
        // Don't use fireTableRowsInserted() here;
        // the selection model would get  updated twice.
        public void treeExpanded(TreeExpansionEvent event) {
          fireTableDataChanged();
        }
        public void treeCollapsed(TreeExpansionEvent event) {
          fireTableDataChanged();
        }
      });
      tree.getModel().addTreeModelListener(new TreeModelListener() {
        public void treeNodesChanged(TreeModelEvent e) {
          fireTableDataChanged();
        }
        public void treeNodesInserted(TreeModelEvent e) {
          fireTableDataChanged();
        }
        public void treeNodesRemoved(TreeModelEvent e) {
          fireTableDataChanged();
        }
        public void treeStructureChanged(TreeModelEvent e) {
          fireTableDataChanged();
        }
      });
    }



    // Wrappers, implementing TableModel interface.
    public int getColumnCount() {
      return treeTableModel.getColumnCount();
    }

    public String getColumnName(int column) {
      return treeTableModel.getColumnName(column);
    }

    public Class getColumnClass(int column) {
      if(column == 0) return TreeTableModel.class;
      else return treeTableModel.getColumnClass(column);
    }

    public int getRowCount() {
      return tree.getRowCount();
    }

    protected Object nodeForRow(int row) {
      TreePath treePath = tree.getPathForRow(row);
      return treePath.getLastPathComponent();
    }

    public Object getValueAt(int row, int column) {
      if(column == 0) return treeTableModel;
      else return treeTableModel.getValueAt(nodeForRow(row), column);
    }

    public boolean isCellEditable(int row, int column) {
      return treeTableModel.isCellEditable(nodeForRow(row), column);
    }

    public void setValueAt(Object value, int row, int column) {
      Object node = nodeForRow(row);
      treeTableModel.setValueAt(value, node, column);
    }
  }//class TreeTableModelAdapter extends AbstractTableModel

  /**
   * The JTree used for rendering the first column.
   */
  class CustomJTree extends JTree {

    public void updateUI(){
      super.updateUI();
      setRowHeight(0);
    }


    public void setVisibleRow(int row){
      visibleRow = row;
    }

    /**
     * Paints only the current cell in the table
     */
    public void paint(Graphics g){
      Rectangle rect = getRowBounds(visibleRow);
      Rectangle bounds = g.getClipBounds();
      g.translate(0, -rect.y);
      g.setClip(bounds.x, rect.y, bounds.width, rect.height);
      super.paint(g);
    }


    public Dimension getPreferredSize(){
      return new Dimension(super.getPreferredSize().width,
                           getRowBounds(visibleRow).height);
    }


    public void validate(){}
    public void revalidate(){}
    public void repaint(long tm, int x, int y, int width, int height){}
    public void repaint(Rectangle r){}

    protected int visibleRow;
  }

/*
  class SmartTreeCellRenderer implements TreeCellRenderer{

    SmartTreeCellRenderer(TreeCellRenderer renderer){
      originalRenderer = renderer;
    }

    public Component getTreeCellRendererComponent(JTree tree,
                                              Object value,
                                              boolean selected,
                                              boolean expanded,
                                              boolean leaf,
                                              int row,
                                              boolean hasFocus){
      Component comp = originalRenderer.getTreeCellRendererComponent(
                       tree, value, selected, expanded, leaf, row, hasFocus);
      if(comp instanceof JComponent &&
         comp.getPreferredSize().height < getRowHeight(row)){
        ((JComponent)comp).setPreferredSize(
            new Dimension(comp.getPreferredSize().width,
            getRowHeight(row))
        );
      }
      return comp;
    }

    public TreeCellRenderer getOriginalRenderer(){
      return originalRenderer;
    }

    TreeCellRenderer originalRenderer;
  }
*/
}//public class JTreeTable extends XJTable
