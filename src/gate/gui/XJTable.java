package gate.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;

import gate.util.*;

public class XJTable extends JTable {

  public XJTable() {
    init();
  }

  public XJTable(TableModel model) {
    init();
    setModel(model);
  }

  public void setModel(TableModel model){
    if(sorter != null) sorter.setModel(model);
    else{
      sorter = new TableSorter(model);
      super.setModel(sorter);
    }
  }

  public TableModel getActualModel(){
    if(sorter != null)return sorter.getModel();
    else return super.getModel();
  }

  public void tableChanged(TableModelEvent e){
    super.tableChanged(e);
    adjustSizes(false);
  }

  protected void init(){
    //make sure we have a model
    if(sorter == null){
      sorter = new TableSorter(super.getModel());
      super.setModel(sorter);
    }
    //read the arrows icons
    upIcon = new ImageIcon(getClass().getResource(Files.getResourcePath() +
                                                  "/img/up.gif"));
    downIcon = new ImageIcon(getClass().getResource(Files.getResourcePath() +
                                                    "/img/down.gif"));

    setColumnSelectionAllowed(false);
    MouseAdapter listMouseListener = new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        TableColumnModel columnModel = getColumnModel();
        int viewColumn = columnModel.getColumnIndexAtX(e.getX());
        int column = convertColumnIndexToModel(viewColumn);
        if (column != -1) {
          if(column != sortedColumn) ascending = true;
          else ascending = !ascending;
          sorter.sortByColumn(column);
          sortedColumn = column;
        }
        adjustSizes(true);
      }
    };
    getTableHeader().addMouseListener(listMouseListener);
    setAutoResizeMode(AUTO_RESIZE_OFF);
    headerRenderer = new CustomHeaderRenderer(getTableHeader().getDefaultRenderer());
  }//init()


  protected void adjustSizes(boolean headerOnly){
    int totalWidth = 0;
    TableColumn tCol = null;
    Dimension dim;
    int cellWidth;
    int cellHeight;
    int rowMargin = getRowMargin();

    //delete the current rowModel in order to get a new updated one
    //this way we fix a bug in JTable
    setRowHeight(Math.max(getRowHeight(), 10));
    for(int column = 0; column < getColumnCount(); column ++){
      int width;
      tCol = getColumnModel().getColumn(column);
      //set the renderer
      tCol.setHeaderRenderer(headerRenderer);
      //compute the sizes
      width = headerRenderer.getTableCellRendererComponent(
                  this, tCol.getHeaderValue(), false, false,0,column
              ).getPreferredSize().width;
      if(! headerOnly){
        for(int row = 0; row < getRowCount(); row ++){
          dim = getCellRenderer(row,column).
                      getTableCellRendererComponent(
                        this, getValueAt(row, column), false, false, row, column
                      ).getPreferredSize();
          cellWidth = dim.width;
          cellHeight = dim.height;
          width = Math.max(width, cellWidth);
          if(cellHeight + rowMargin > getRowHeight(row))
            setRowHeight(row, cellHeight + rowMargin);
        }
      }
      width += getColumnModel().getColumnMargin();
      tCol.setPreferredWidth(width);
      tCol.setMinWidth(width);
      totalWidth += width;
    }
    if(! headerOnly){
      int totalHeight = 0;
      for (int row = 0; row < getRowCount(); row++)
        totalHeight += getRowHeight(row);
      dim = new Dimension(totalWidth, totalHeight);
      setPreferredScrollableViewportSize(dim);
  //System.out.println("View data size: " + getRowCount() + dim);

      Container p = getParent();
      if (p instanceof JViewport) {
          Container gp = p.getParent();
          if (gp instanceof JScrollPane) {
              JScrollPane scrollPane = (JScrollPane)gp;
              // Make certain we are the viewPort's view and not, for
              // example, the rowHeaderView of the scrollPane -
              // an implementor of fixed columns might do this.
              JViewport viewport = scrollPane.getViewport();
              if (viewport == null || viewport.getView() != this) {
                  return;
              }
              viewport.setViewSize(dim);
              viewport.setExtentSize(dim);
          }
      }
    }

  }

  public void setSortedColumn(int column){
    sortedColumn = column;
    sorter.sortByColumn(sortedColumn);
  }

  public void setAscending(boolean ascending){
    this.ascending = ascending;
  }

  protected TableSorter sorter;

  protected Icon upIcon;
  protected Icon downIcon;
  int sortedColumn = -1;
//  int oldSortedColumn = -1;
  boolean ascending = true;
  protected TableCellRenderer headerRenderer;
//  protected TableCellRenderer savedHeaderRenderer;

//classes

  /**
   * A sorter for TableModels. The sorter has a model (conforming to TableModel)
   * and itself implements TableModel. TableSorter does not store or copy
   * the data in the TableModel, instead it maintains an array of
   * integers which it keeps the same size as the number of rows in its
   * model. When the model changes it notifies the sorter that something
   * has changed eg. "rowsAdded" so that its internal array of integers
   * can be reallocated. As requests are made of the sorter (like
   * getValueAt(row, col) it redirects them to its model via the mapping
   * array. That way the TableSorter appears to hold another copy of the table
   * with the rows in a different order. The sorting algorthm used is stable
   * which means that it does not move around rows when its comparison
   * function returns 0 to denote that they are equivalent.
   *
   * @version 1.5 12/17/97
   * @author Philip Milne
   */

  class TableSorter extends TableMap {
    int             indexes[];
    Vector          sortingColumns = new Vector();

    public TableSorter() {
      indexes = new int[0]; // for consistency
    }

    public TableSorter(TableModel model) {
      setModel(model);
    }

    public void setModel(TableModel model) {
      super.setModel(model);
      reallocateIndexes();
    }

    public int compareRowsByColumn(int row1, int row2, int column) {
      Class type = model.getColumnClass(column);
      TableModel data = model;

      // Check for nulls.

      Object o1 = data.getValueAt(row1, column);
      Object o2 = data.getValueAt(row2, column);

      // If both values are null, return 0.
      if (o1 == null && o2 == null) {
        return 0;
      } else if (o1 == null) { // Define null less than everything.
        return -1;
      } else if (o2 == null) {
        return 1;
      }

      /*
       * We copy all returned values from the getValue call in case
       * an optimised model is reusing one object to return many
       * values.  The Number subclasses in the JDK are immutable and
       * so will not be used in this way but other subclasses of
       * Number might want to do this to save space and avoid
       * unnecessary heap allocation.
       */

      if (type.getSuperclass() == java.lang.Number.class) {
        Number n1 = (Number)data.getValueAt(row1, column);
        double d1 = n1.doubleValue();
        Number n2 = (Number)data.getValueAt(row2, column);
        double d2 = n2.doubleValue();

        if (d1 < d2) {
          return -1;
        } else if (d1 > d2) {
          return 1;
        } else {
          return 0;
        }
      } else if (type == java.util.Date.class) {
        Date d1 = (Date)data.getValueAt(row1, column);
        long n1 = d1.getTime();
        Date d2 = (Date)data.getValueAt(row2, column);
        long n2 = d2.getTime();

        if (n1 < n2) {
          return -1;
        } else if (n1 > n2) {
          return 1;
        } else {
          return 0;
        }
      } else if (type == String.class) {
        String s1 = (String)data.getValueAt(row1, column);
        String s2    = (String)data.getValueAt(row2, column);
        int result = s1.compareTo(s2);

        if (result < 0) {
          return -1;
        } else if (result > 0) {
          return 1;
        } else {
          return 0;
        }
      } else if (type == Boolean.class) {
        Boolean bool1 = (Boolean)data.getValueAt(row1, column);
        boolean b1 = bool1.booleanValue();
        Boolean bool2 = (Boolean)data.getValueAt(row2, column);
        boolean b2 = bool2.booleanValue();

        if (b1 == b2) {
          return 0;
        } else if (b1) { // Define false < true
          return 1;
        } else {
          return -1;
        }
      } else {
        Object v1 = data.getValueAt(row1, column);
        Object v2 = data.getValueAt(row2, column);
        int result;
        if(v1 instanceof Comparable){
          try{
            result = ((Comparable)v1).compareTo(v2);
          }catch(ClassCastException cce){
            String s1 = v1.toString();
            String s2 = v2.toString();
            result = s1.compareTo(s2);
          }
        }else{
          String s1 = v1.toString();
          String s2 = v2.toString();
          result = s1.compareTo(s2);
        }

        if (result < 0) {
          return -1;
        } else if (result > 0) {
          return 1;
        } else {
          return 0;
        }
      }
    }

    public int compare(int row1, int row2) {
     // compares++;
      for (int level = 0; level < sortingColumns.size(); level++) {
        Integer column = (Integer)sortingColumns.elementAt(level);
        int result = compareRowsByColumn(row1, row2, column.intValue());
        if (result != 0) {
          return ascending ? result : -result;
        }
      }
      return 0;
    }

    public void reallocateIndexes() {
      int rowCount = model.getRowCount();

      // Set up a new array of indexes with the right number of elements
      // for the new data model.
      indexes = new int[rowCount];

      // Initialise with the identity mapping.
      for (int row = 0; row < rowCount; row++) {
        indexes[row] = row;
      }
    }

    public void tableChanged(TableModelEvent e) {
      //System.out.println("Sorter: tableChanged " + model.getRowCount());
      reallocateIndexes();
      sort(sorter);
      super.tableChanged(e);
    }

    public void checkModel() {
      if (indexes.length != model.getRowCount()) {
        System.err.println("Sorter not informed of a change in model.");
      }
    }

    public void sort(Object sender) {
      checkModel();
      shuttlesort((int[])indexes.clone(), indexes, 0, indexes.length);
    }

    // This is a home-grown implementation which we have not had time
    // to research - it may perform poorly in some circumstances. It
    // requires twice the space of an in-place algorithm and makes
    // NlogN assigments shuttling the values between the two
    // arrays. The number of compares appears to vary between N-1 and
    // NlogN depending on the initial order but the main reason for
    // using it here is that, unlike qsort, it is stable.
    public void shuttlesort(int from[], int to[], int low, int high) {
      if (high - low < 2) {
          return;
      }
      int middle = (low + high)/2;
      shuttlesort(to, from, low, middle);
      shuttlesort(to, from, middle, high);

      int p = low;
      int q = middle;

      /* This is an optional short-cut; at each recursive call,
      check to see if the elements in this subset are already
      ordered.  If so, no further comparisons are needed; the
      sub-array can just be copied.  The array must be copied rather
      than assigned otherwise sister calls in the recursion might
      get out of sinc.  When the number of elements is three they
      are partitioned so that the first set, [low, mid), has one
      element and and the second, [mid, high), has two. We skip the
      optimisation when the number of elements is three or less as
      the first compare in the normal merge will produce the same
      sequence of steps. This optimisation seems to be worthwhile
      for partially ordered lists but some analysis is needed to
      find out how the performance drops to Nlog(N) as the initial
      order diminishes - it may drop very quickly.  */

      if (high - low >= 4 && compare(from[middle-1], from[middle]) <= 0) {
        for (int i = low; i < high; i++) {
          to[i] = from[i];
        }
        return;
      }

      // A normal merge.

      for (int i = low; i < high; i++) {
        if (q >= high || (p < middle && compare(from[p], from[q]) <= 0)) {
          to[i] = from[p++];
        }
        else {
          to[i] = from[q++];
        }
      }
    }

    public void swap(int i, int j) {
      int tmp = indexes[i];
      indexes[i] = indexes[j];
      indexes[j] = tmp;
    }

    // The mapping only affects the contents of the data rows.
    // Pass all requests to these rows through the mapping array: "indexes".

    public Object getValueAt(int aRow, int aColumn) {
      checkModel();
      return model.getValueAt(indexes[aRow], aColumn);
    }

    public void setValueAt(Object aValue, int aRow, int aColumn) {
      checkModel();
      model.setValueAt(aValue, indexes[aRow], aColumn);
    }

    public void sortByColumn(int column) {
      sortingColumns.removeAllElements();
      sortingColumns.addElement(new Integer(column));
      sort(this);
      super.tableChanged(new TableModelEvent(this));
      getTableHeader().repaint();
    }
  }//class TableSorter extends TableMap

  class CustomHeaderRenderer extends DefaultTableCellRenderer{
    public CustomHeaderRenderer(TableCellRenderer oldRenderer){
      this.oldRenderer = oldRenderer;
    }

    public Component getTableCellRendererComponent(JTable table,
                                             Object value,
                                             boolean isSelected,
                                             boolean hasFocus,
                                             int row,
                                             int column){

      Component res = oldRenderer.getTableCellRendererComponent(
                            table, value, isSelected, hasFocus, row, column);
      if(res instanceof JLabel){
        if(convertColumnIndexToModel(column) == sortedColumn){
          ((JLabel)res).setIcon(ascending?downIcon:upIcon);
        }else{
          ((JLabel)res).setIcon(null);
        }
        ((JLabel)res).setHorizontalTextPosition(JLabel.LEFT);
      }
      return res;
    }
    protected TableCellRenderer oldRenderer;
  }
}