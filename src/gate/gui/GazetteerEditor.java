/*
 *  Copyright (c) 1995-2010, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Thomas Heitz, 1 March 2010
 *
 *  $Id$
 */

package gate.gui;

import gate.Factory;
import gate.Resource;
import gate.creole.AbstractVisualResource;
import gate.creole.ResourceInstantiationException;
import gate.creole.gazetteer.*;
import gate.swing.XJFileChooser;
import gate.swing.XJTable;
import gate.util.Err;
import gate.util.ExtensionFileFilter;
import gate.util.Files;
import gate.util.GateRuntimeException;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.Collator;
import java.util.*;
import java.util.List;
import java.util.Timer;

/**
 * Editor for {@link gate.creole.gazetteer.Gazetteer ANNIE Gazetteer}.
<pre>
 Main features:
- left table with 4 columns (List name, Major, Minor, Language) for the
  definition
- right table with 1+n columns (Value, Feature 1...Feature n) for the lists
- 'Save' on the context menu of the resources tree and tab
- context menu on both tables to delete selected rows
- list name drop down list with .lst files in directory and button [New List]
- value text field and button [New Entry]
- for the second table: [Add Cols]
- a text field case insensitive 'Filter' at the bottom of the right table
- both tables sorted case insensitively on the first column by default
- display in red the list name when the list is modified
- for the separator character test when editing feature columns
- make feature map ordered
- remove feature/value columns when containing only spaces or empty
</pre>
*/
public class GazetteerEditor extends AbstractVisualResource
    implements GazetteerListener, ActionsPublisher {

  public GazetteerEditor() {
    definitionTableModel = new DefaultTableModel();
    definitionTableModel.addColumn("List name");
    definitionTableModel.addColumn("Major");
    definitionTableModel.addColumn("Minor");
    definitionTableModel.addColumn("Language");
    listTableModel = new ListTableModel();
    actions = new ArrayList<Action>();
    actions.add(new SaveAndReinitialiseGazetteerAction());
    actions.add(new SaveAsGazetteerAction());
  }

  public Resource init() throws ResourceInstantiationException {
    initGUI();
    initListeners();
    return this;
  }

  protected void initGUI() {
    collator = Collator.getInstance(Locale.ENGLISH);
    collator.setStrength(Collator.TERTIARY);

    // definition table pane
    JPanel definitionPanel = new JPanel(new BorderLayout());
    JPanel definitionTopPanel = new JPanel();
    newListComboBox = new JComboBox();
    newListComboBox.setEditable(true);
    newListComboBox.setPrototypeDisplayValue("123456789012345");
    newListComboBox.setToolTipText(
      "Lists available in the gazetteer directory");
    newListButton = new JButton("New List");
    // enable/disable [New] button according to the text field content
    JTextComponent listTextComponent = (JTextField)
      newListComboBox.getEditor().getEditorComponent();
    listTextComponent.getDocument().addDocumentListener(new DocumentListener() {
      public void insertUpdate(DocumentEvent e) { update(e); }
      public void removeUpdate(DocumentEvent e) { update(e); }
      public void changedUpdate(DocumentEvent e) { update(e); }
      public void update(DocumentEvent e) {
        Document document = e.getDocument();
        try {
          String value = document.getText(0, document.getLength());
          if (value.trim().length() == 0) {
            newListButton.setEnabled(false);
            newListButton.setText("New List");
          } else if (value.contains(":")) {
            newListButton.setEnabled(false);
            newListButton.setText("No colon");
          } else if (linearDefinition.getLists().contains(value)) {
            // this list already exists in the gazetteer
            newListButton.setEnabled(false);
            newListButton.setText("Existing");
          } else {
            newListButton.setEnabled(true);
            newListButton.setText("New List");
          }
        } catch (BadLocationException ble) {
          ble.printStackTrace();
        }
      }
    });
    newListComboBox.getEditor().getEditorComponent()
        .addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
          // Enter key in the text field add the entry to the table
          newListButton.doClick();
        }
      }
    });
    newListButton.setToolTipText("New list in the gazetteer");
    newListButton.setMargin(new Insets(2, 2, 2, 2));
    newListButton.addActionListener(new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        String listName = (String) newListComboBox.getEditor().getItem();
        newListComboBox.removeItem(listName);
        // update the table
        definitionTableModel.addRow(new Object[]{listName, "", "", ""});
        // update the gazetteer
        linearDefinition.add(new LinearNode(listName, "", "", ""));
        final int row = definitionTable.rowModelToView(
          definitionTable.getRowCount()-1);
        final int column = definitionTable.convertColumnIndexToView(0);
        definitionTable.setRowSelectionInterval(row, row);
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            definitionTable.scrollRectToVisible(
              definitionTable.getCellRect(row, column, true));
            definitionTable.requestFocusInWindow();
          }
        });
      }
    });
    definitionTopPanel.add(newListComboBox);
    definitionTopPanel.add(newListButton);
    definitionPanel.add(definitionTopPanel, BorderLayout.NORTH);
    definitionTable = new XJTable() {
      // shift + Delete keys delete the selected rows
      protected void processKeyEvent(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_DELETE
        && ((e.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) != 0)) {
          new DeleteSelectedLinearNodeAction().actionPerformed(null);
        } else {
          super.processKeyEvent(e);
        }
      }
    };
    definitionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    definitionTable.setRowSelectionAllowed(true);
    definitionTable.setColumnSelectionAllowed(false);
    definitionTable.setEnableHidingColumns(true);
    definitionTable.setAutoResizeMode(XJTable.AUTO_RESIZE_OFF);
    definitionTable.setModel(definitionTableModel);
    definitionTable.setSortable(true);
    definitionTable.setSortedColumn(0);
    // use red colored font for modified lists name
    definitionTable.getColumnModel().getColumn(0).setCellRenderer(
      new DefaultTableCellRenderer() {
        public Component getTableCellRendererComponent(JTable table,
            Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
          super.getTableCellRendererComponent(
            table, value, isSelected, hasFocus, row, column);
          setForeground(table.getForeground());
          LinearNode linearNode = (LinearNode)
            linearDefinition.getNodesByListNames().get(value);
          if (linearNode != null) {
            GazetteerList gazetteerList = (GazetteerList)
              linearDefinition.getListsByNode().get(linearNode);
            if (gazetteerList != null && gazetteerList.isModified()) {
              setForeground(Color.RED);
            }
          }
          return this;
        }
      });
    definitionPanel.add(new JScrollPane(definitionTable), BorderLayout.CENTER);

    // list table pane
    JPanel listPanel = new JPanel(new BorderLayout());
    JPanel listTopPanel = new JPanel();
    newEntryTextField = new JTextField(10);
    final JButton newEntryButton = new JButton("New Entry ");
    newEntryButton.setToolTipText("New entry in the list");
    newEntryButton.setMargin(new Insets(2, 2, 2, 2));
    newEntryButton.addActionListener(new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        // update the gazetteer
        GazetteerNode newGazetteerNode = new GazetteerNode(
          newEntryTextField.getText(), Factory.newFeatureMap());
        listTableModel.addRow(newGazetteerNode);
        listTableModel.setFilterText("");
        listFilterTextField.setText("");
        listTableModel.fireTableDataChanged();
        // scroll and select the new row
        final int row = listTable.rowModelToView(listTable.getRowCount()-1);
        final int column = listTable.convertColumnIndexToView(0);
        newEntryTextField.setText("");
        newEntryTextField.requestFocusInWindow();
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            listTable.scrollRectToVisible(
              listTable.getCellRect(row, column, true));
            listTable.setRowSelectionInterval(row, row);
            listTable.setColumnSelectionInterval(column, column);
            GazetteerList gazetteerList = (GazetteerList)
              linearDefinition.getListsByNode().get(selectedLinearNode);
            gazetteerList.setModified(true);
            definitionTable.repaint();
          }
        });
      }
    });
    // Enter key in the text field add the entry to the table
    newEntryTextField.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
          newEntryButton.doClick();
        }
      }
    });
    // enable/disable [New] button according to the text field content
    newEntryTextField.getDocument().addDocumentListener(new DocumentListener() {
      public void insertUpdate(DocumentEvent e) { update(e); }
      public void removeUpdate(DocumentEvent e) { update(e); }
      public void changedUpdate(DocumentEvent e) { update(e); }
      public void update(DocumentEvent e) {
        Document document = e.getDocument();
        try {
          String value = document.getText(0, document.getLength());
          if (value.trim().length() == 0) {
            newEntryButton.setEnabled(false);
            newEntryButton.setText("New Entry");
          } else if (linearDefinition.getSeparator() != null
                  && linearDefinition.getSeparator().length() > 0
                  && value.contains(linearDefinition.getSeparator())) {
            newEntryButton.setEnabled(false);
            newEntryButton.setText("No char "+linearDefinition.getSeparator());
          } else {
            // check if the entry already exists in the list
            GazetteerList gazetteerList = (GazetteerList)
              linearDefinition.getListsByNode().get(selectedLinearNode);
            boolean found = false;
            for (Object object : gazetteerList) {
              GazetteerNode node = (GazetteerNode) object;
              if (node.getEntry().equals(value)) {
                found = true;
                break;
              }
            }
            if (found) {
              newEntryButton.setEnabled(false);
              newEntryButton.setText("Existing ");
            } else {
              newEntryButton.setEnabled(true);
              newEntryButton.setText("New Entry");
            }
          }
        } catch (BadLocationException ble) {
          ble.printStackTrace();
        }
      }
    });
    final JButton addColumnsButton = new JButton("Add Cols");
    addColumnsButton.setToolTipText("Add a couple of columns Feature and Value");
    addColumnsButton.setMargin(new Insets(2, 2, 2, 2));
    addColumnsButton.addActionListener(new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        if (linearDefinition.getSeparator() == null
         || linearDefinition.getSeparator().length() == 0) {
          String separator = JOptionPane.showInputDialog(
            MainFrame.getInstance(), "Type a character separator to separate" +
              "\nfeatures in the gazetteers lists.",
            "Feature Separator", JOptionPane.QUESTION_MESSAGE);
          if (separator == null
           || separator.equals("")) {
            return;
          }
          linearDefinition.setSeparator(separator);
        }
        listTableModel.addEmptyFeatureColumns();
        // cancel filtering and redisplay the table
        listFilterTextField.setText("");
        listTableModel.setFilterText("");
        listTableModel.fireTableStructureChanged();
      }
    });
    listTopPanel.add(newEntryTextField);
    listTopPanel.add(newEntryButton);
    listTopPanel.add(addColumnsButton);
    listPanel.add(listTopPanel, BorderLayout.NORTH);
    listTable = new XJTable() {
      // shift + Delete keys delete the selected rows
      protected void processKeyEvent(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_DELETE
        && ((e.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) != 0)) {
          new DeleteSelectedGazetteerNodeAction().actionPerformed(null);
        } else {
          super.processKeyEvent(e);
        }
      }
    };
    listTable.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
    listTable.setRowSelectionAllowed(true);
    listTable.setColumnSelectionAllowed(true);
    listTable.setEnableHidingColumns(true);
    listTable.setAutoResizeMode(XJTable.AUTO_RESIZE_OFF);
    listTable.setModel(listTableModel);
    listTable.setSortable(true);
    listTable.setSortedColumn(0);
    listPanel.add(new JScrollPane(listTable), BorderLayout.CENTER);
    JPanel listBottomPanel = new JPanel(new BorderLayout());
    JPanel filterPanel = new JPanel();
    listFilterTextField = new JTextField(15);
    listFilterTextField.setToolTipText("Filter rows on all column values");
    // select all the rows containing the text from filterTextField
    listFilterTextField.getDocument().addDocumentListener(
        new DocumentListener() {
      private Timer timer = new Timer("Gazetteer list filter timer", true);
      private TimerTask timerTask;
      public void changedUpdate(DocumentEvent e) { /* do nothing */ }
      public void insertUpdate(DocumentEvent e) { update(); }
      public void removeUpdate(DocumentEvent e) { update(); }
      private void update() {
        if (timerTask != null) { timerTask.cancel(); }
        Date timeToRun = new Date(System.currentTimeMillis() + 300);
        timerTask = new TimerTask() { public void run() {
          String filter = listFilterTextField.getText().trim();
          listTableModel.setFilterText(filter);
          listTableModel.fireTableDataChanged();
        }};
        // add a delay
        timer.schedule(timerTask, timeToRun);
      }
    });
    filterPanel.add(new JLabel("Filter: "));
    filterPanel.add(listFilterTextField);
    listBottomPanel.add(filterPanel, BorderLayout.WEST);
    listBottomPanel.add(listCountLabel = new JLabel(), BorderLayout.EAST);
    listPanel.add(listBottomPanel, BorderLayout.SOUTH);

    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
    splitPane.add(definitionPanel);
    splitPane.add(listPanel);
    splitPane.setResizeWeight(0.33);
    setLayout(new BorderLayout());
    add(splitPane, BorderLayout.CENTER);
  }

  protected void initListeners() {

    // display the list corresponding to the selected row
    definitionTable.getSelectionModel().addListSelectionListener(
      new ListSelectionListener() {
        public void valueChanged(ListSelectionEvent e) {
          if (e.getValueIsAdjusting()
           || definitionTable.isEditing()) {
            return;
          }
          if (definitionTable.getSelectedRow() == -1) {
            listTableModel.setGazetteerList(new GazetteerList());
            selectedLinearNode = null;
          } else {
            String listName = (String) definitionTable.getValueAt(
              definitionTable.getSelectedRow(),
              definitionTable.convertColumnIndexToView(0));
            selectedLinearNode = (LinearNode)
              linearDefinition.getNodesByListNames().get(listName);
            if (selectedLinearNode != null) {
              listTableModel.setGazetteerList((GazetteerList)
                linearDefinition.getListsByNode().get(selectedLinearNode));
            }
          }
          if (!listFilterTextField.getText().equals("")) {
            listFilterTextField.setText("");
          }
          if (!newEntryTextField.getText().equals("")) {
            newEntryTextField.setText("");
          }
          listTableModel.setFilterText("");
          listTableModel.fireTableStructureChanged();
          if (definitionTable.getSelectedRow() != -1) {
            if (selectedLinearNode != null) {
              for (int col = 0 ; col < listTable.getColumnCount(); col++) {
                listTable.setComparator(col, collator);
              }
              // TODO: this is only to sort the rows, how to avoid it?
              listTableModel.fireTableDataChanged();
            }
          }
        }
      }
    );

    // update linear nodes with changes in the definition table
    definitionTableModel.addTableModelListener(new TableModelListener() {
      public void tableChanged(TableModelEvent e) {
        int r = e.getFirstRow();
        switch (e.getType()) {
          case TableModelEvent.UPDATE:
            int c = e.getColumn();
            if (r == -1 || c == -1) { return; }
            String newValue = (String) definitionTableModel.getValueAt(r, c);
            if (c == 0) {
              String oldValue = selectedLinearNode.getList();
              if (oldValue != null && oldValue.equals(newValue)) { return; }
              // save the previous list and copy it to the new name of the list
              try {
                GazetteerList gazetteerList = (GazetteerList)
                  linearDefinition.getListsByNode().get(selectedLinearNode);
                // save the previous list
                gazetteerList.store();
                MainFrame.getInstance().statusChanged("Previous list saved in "
                  + gazetteerList.getURL().getPath());
                File source = Files.fileFromURL(gazetteerList.getURL());
                File destination = new File(source.getParentFile(), newValue);
                // change the list URL to the new list name
                gazetteerList.setURL(destination.toURI().toURL());
                gazetteerList.setModified(false);
                // change the key of the node in the map
                linearDefinition.getNodesByListNames()
                  .remove(selectedLinearNode.getList());
                linearDefinition.getNodesByListNames()
                  .put(newValue, selectedLinearNode);
                linearDefinition.setModified(true);

              } catch (Exception ex) {
                MainFrame.getInstance().statusChanged(
                  "Unable to save the list.");
                Err.prln("Unable to save the list.\n" + ex.getMessage());
              }

              selectedLinearNode.setList(newValue);
            } else if (c == 1) {
              String oldValue = selectedLinearNode.getMajorType();
              if (oldValue != null && oldValue.equals(newValue)) { return; }
              selectedLinearNode.setMajorType(newValue);
              linearDefinition.setModified(true);
            } else if (c == 2) {
              String oldValue = selectedLinearNode.getMinorType();
              if (oldValue != null && oldValue.equals(newValue)) { return; }
              selectedLinearNode.setMinorType(newValue);
              linearDefinition.setModified(true);
            } else {
              String oldValue = selectedLinearNode.getLanguage();
              if (oldValue != null && oldValue.equals(newValue)) { return; }
              selectedLinearNode.setLanguage(newValue);
              linearDefinition.setModified(true);
            }
            break;
        }
      }
    });

    // context menu to delete a row
    definitionTable.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent me) {
        processMouseEvent(me);
      }
      public void mouseReleased(MouseEvent me) {
        processMouseEvent(me);
      }
      public void mousePressed(MouseEvent me) {
        JTable table = (JTable) me.getSource();
        int row = table.rowAtPoint(me.getPoint());
        if(me.isPopupTrigger()
        && !table.isRowSelected(row)) {
          // if right click outside the selection then reset selection
          table.getSelectionModel().setSelectionInterval(row, row);
        }
        processMouseEvent(me);
      }
      protected void processMouseEvent(MouseEvent me) {
        XJTable table = (XJTable) me.getSource();
        if (me.isPopupTrigger()
          && table.getSelectedRowCount() > 0) {
            JPopupMenu popup = new JPopupMenu();
            popup.add(new DeleteSelectedLinearNodeAction());
            popup.add(new ReloadGazetteerListAction());
            popup.show(table, me.getX(), me.getY());
        }
      }
    });

    // context menu to delete a row
    listTable.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent me) {
        processMouseEvent(me);
      }
      public void mouseReleased(MouseEvent me) {
        processMouseEvent(me);
      }
      public void mousePressed(MouseEvent me) {
        JTable table = (JTable) me.getSource();
        int row = table.rowAtPoint(me.getPoint());
        if(me.isPopupTrigger()
        && !table.isRowSelected(row)) {
          // if right click outside the selection then reset selection
          table.getSelectionModel().setSelectionInterval(row, row);
        }
        processMouseEvent(me);
      }
      protected void processMouseEvent(MouseEvent me) {
        XJTable table = (XJTable) me.getSource();
        if (me.isPopupTrigger()
          && table.getSelectedRowCount() > 0) {
            JPopupMenu popup = new JPopupMenu();
            popup.add(new DeleteSelectedGazetteerNodeAction());
            popup.show(table, me.getX(), me.getY());
        }
      }
    });

    listTableModel.addTableModelListener(new TableModelListener() {
      public void tableChanged(TableModelEvent e) {
        listCountLabel.setText(String.valueOf(listTableModel.getRowCount())
        + " entries ");
      }
    });

    // add key shortcuts for actions
    InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    ActionMap actionMap = getActionMap();
    inputMap.put(KeyStroke.getKeyStroke("control S"), "save");
    actionMap.put("save", actions.get(0));
    inputMap.put(KeyStroke.getKeyStroke("control shift S"), "save as");
    actionMap.put("save as", actions.get(1));
    inputMap.put(KeyStroke.getKeyStroke("control R"), "reload list");
    actionMap.put("reload list", new ReloadGazetteerListAction());
  }

  public void setTarget(Object target) {
    if (null == target) {
      throw new GateRuntimeException("The resource set is null.");
    }
    if (! (target instanceof Gazetteer) ) {
      throw new GateRuntimeException(
        "The resource set must be of type gate.creole.gazetteer.Gazetteer\n"+
        "and not " + target.getClass());
    }
    ((Gazetteer) target).addGazetteerListener(this);
    processGazetteerEvent(new GazetteerEvent(target, GazetteerEvent.REINIT));
  }

  public void processGazetteerEvent(GazetteerEvent e) {
    gazetteer = (Gazetteer) e.getSource();

    // read and display the definition of the gazetteer
    if (e.getType() == GazetteerEvent.REINIT) {
      linearDefinition = gazetteer.getLinearDefinition();
      if (null == linearDefinition) {
        throw new GateRuntimeException(
          "Linear definition of a gazetteer should not be null.");
      }

      // reload the lists with ordered feature maps
      try {
        if (linearDefinition.getSeparator() != null
         && linearDefinition.getSeparator().length() > 0) {
          linearDefinition.loadLists(true);
        }
      } catch (ResourceInstantiationException rie) {
        rie.printStackTrace();
        return;
      }

      // add the gazetteer definition data to the table
      definitionTableModel.setRowCount(0);
      ArrayList<String> values = new ArrayList<String>();
      for (Object object : linearDefinition.getNodes()) {
        LinearNode node = (LinearNode) object;
        values.add(node.getList() == null ? "" : node.getList());
        values.add(node.getMajorType() == null ? "" : node.getMajorType());
        values.add(node.getMinorType() == null ? "" : node.getMinorType());
        values.add(node.getLanguage() == null ? "" : node.getLanguage());
        definitionTableModel.addRow(values.toArray());
        values.clear();
      }
      for (int col = 0 ; col < definitionTable.getColumnCount(); col++) {
        definitionTable.setComparator(col, collator);
      }

      // update file list name in the drop down list
      File gazetteerDirectory = new File(
        Files.fileFromURL(gazetteer.getListsURL()).getParent());
      File[] files = gazetteerDirectory.listFiles(new FilenameFilter() {
        public boolean accept(File dir, String name) {
          return name.endsWith(".lst")
            && !linearDefinition.getLists().contains(name);
        }
      });
      String[] filenames = new String[files.length];
      int i = 0;
      for (File file : files) {
        filenames[i++] = file.getName();
      }
      Arrays.sort(filenames, collator);
      newListComboBox.setModel(new DefaultComboBoxModel(filenames));
      if (filenames.length == 0) {
        newListButton.setEnabled(false);
      }
    }
  }

  protected class ListTableModel extends AbstractTableModel {

    public ListTableModel() {
      gazetteerListFiltered = new GazetteerList();
    }

    public int getRowCount() {
      return gazetteerListFiltered.size();
    }

    public int getColumnCount() {
      if (columnCount > -1) { return columnCount; }
      if (gazetteerListFiltered == null) { return 0; }
      columnCount = 1;
      // read all the features maps to find the biggest one
      for (Object object : gazetteerListFiltered) {
        GazetteerNode node = (GazetteerNode) object;
        Map map = node.getFeatureMap();
        if (map != null && columnCount < 2*map.size()+1) {
          columnCount = 2*map.size() + 1;
        }
      }
      return columnCount;
    }

    public String getColumnName(int column) {
      if (column == 0) {
        return "Value";
      } else {
        int featureCount = (column + (column % 2)) / 2;
        if (column % 2 == 1) {
          return "Feature " + featureCount;
        } else {
          return "Value " + featureCount;
        }
      }
    }

    public boolean isCellEditable(int row, int column) {
      return true;
    }

    public Object getValueAt(int row, int column) {
      GazetteerNode node = (GazetteerNode) gazetteerListFiltered.get(row);
      if (column == 0) {
        return node.getEntry();
      } else {
        Map featureMap = node.getFeatureMap();
        if (featureMap == null
         || featureMap.size()*2 < column) {
          return "";
        }
        List<String> features = new ArrayList<String>(featureMap.keySet());
        int featureCount = (column + (column % 2)) / 2;
        if (column % 2 == 1) {
          return features.get(featureCount-1);
        } else {
          return featureMap.get(features.get(featureCount-1));
        }
      }
    }

    public void setValueAt(Object value, int row, int column) {
      if (row == -1 || column == -1) { return; }
      // remove separator characters that are contained in the value
      // and display a tooltip to explain it
      if (linearDefinition.getSeparator() != null
       && linearDefinition.getSeparator().length() > 0
       && ((String)value).contains(linearDefinition.getSeparator())) {
        final Point point = listTable.getCellRect(listTable.getSelectedRow(),
          listTable.getSelectedColumn(), true).getLocation();
        point.translate(listTable.getLocationOnScreen().x,
          listTable.getLocationOnScreen().y);
        final Timer timer = new Timer("GazetteerEditor tooltip timer", true);
        SwingUtilities.invokeLater(new Runnable() { public void run() {
          if (!listTable.isShowing()) { return; }
          JToolTip toolTip = listTable.createToolTip();
          toolTip.setTipText("No separator character allowed: [" +
            linearDefinition.getSeparator() + "]");
          PopupFactory popupFactory = PopupFactory.getSharedInstance();
          final Popup popup = popupFactory.getPopup(
            listTable, toolTip, point.x, point.y - 20);
          popup.show();
          Date timeToRun = new Date(System.currentTimeMillis() + 3000);
          timer.schedule(new TimerTask() { public void run() {
            SwingUtilities.invokeLater(new Runnable() { public void run() {
              popup.hide(); // hide the tooltip after some time
            }});
          }}, timeToRun);
        }});
        value = ((String)value).replaceAll(
          "\\Q"+linearDefinition.getSeparator()+"\\E", "");
      }
      GazetteerNode gazetteerNode =
        (GazetteerNode) gazetteerListFiltered.get(row);
      if (column == 0) {
        // update entry
        gazetteerNode.setEntry((String) value);
      } else {
        // update the whole feature map
        Map newFeatureMap = new LinkedHashMap();
        for (int col = 1; col+1 < getColumnCount(); col += 2) {
          String feature = (String) ((col == column) ?
            value : getValueAt(row, col));
          String val = (String) ((col+1 == column) ?
            value : (String) getValueAt(row, col+1));
          newFeatureMap.put(feature, val);
        }
        gazetteerNode.setFeatureMap(newFeatureMap);
        fireTableRowsUpdated(row, row);
      }
      gazetteerList.setModified(true);
      definitionTable.repaint();
    }

    public void fireTableStructureChanged() {
      columnCount = -1;
      super.fireTableStructureChanged();
    }

    public void fireTableChanged(TableModelEvent e) {
      if (filter.length() < 2) {
        gazetteerListFiltered.clear();
        gazetteerListFiltered.addAll(gazetteerList);
        super.fireTableChanged(e);
      } else {
        filterRows();
        // same as super.fireTableDataChanged() to avoid recursion
        super.fireTableChanged(new TableModelEvent(this));
      }
    }

    /**
     * Filter the table rows against this filter.
     * @param filter string used to filter rows
     */
    public void setFilterText(String filter) {
      this.filter = filter;
    }

    protected void filterRows() {
      gazetteerListFiltered.clear();
      String filterUC = filter.toUpperCase();
      for (Object object : gazetteerList) {
        GazetteerNode node = (GazetteerNode) object;
        boolean match = false;
        Map map = node.getFeatureMap();
        if (map != null) {
          for (Object key : map.keySet()) {
            if (((String)key).toUpperCase().contains(filterUC)
             || ((String)map.get(key)).toUpperCase().contains(filterUC)) {
              match = true;
              break;
            }
          }
        }
        if (match || node.getEntry().toUpperCase().contains(filterUC)) {
          // gazetteer node matches the filter
          gazetteerListFiltered.add(node);
        }
      }
    }

    public void addEmptyFeatureColumns() {
      // find the first row fully filled with value
      if (getColumnCount() == 1) {
        GazetteerNode node = (GazetteerNode) gazetteerListFiltered.get(0);
        Map<String, String> map = new HashMap<String, String>();
        // add a couple of rows
        map.put("", "");
        node.setFeatureMap(map);
      } else {
        for (Object object : gazetteerListFiltered) {
          GazetteerNode node = (GazetteerNode) object;
          Map map = node.getFeatureMap();
          if (map != null
          && (2*map.size()+1) == getColumnCount()) {
            map.put("", "");
            break;
          }
        }
      }
      for (Object object : gazetteerList) {
        GazetteerNode node = (GazetteerNode) object;
        node.setSeparator(linearDefinition.getSeparator());
      }
    }

    public void addRow(GazetteerNode gazetteerNode) {
      gazetteerList.add(gazetteerNode);
    }

    /**
     * @param row row index in the model
     */
    public void removeRow(int row) {
      gazetteerList.remove(gazetteerListFiltered.get(row));
    }

    public void setGazetteerList(GazetteerList gazetteerList) {
      this.gazetteerList = gazetteerList;
    }

    private int columnCount = -1;
    private String filter = "";
    private GazetteerList gazetteerList;
    private GazetteerList gazetteerListFiltered;
  }

  public List getActions() {
    return actions;
  }

  protected class ReloadGazetteerListAction extends AbstractAction {
    public ReloadGazetteerListAction() {
      super("Reload List");
      putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control R"));
    }
    public void actionPerformed(ActionEvent e) {
      GazetteerList gazetteerList = (GazetteerList)
        linearDefinition.getListsByNode().get(selectedLinearNode);
      gazetteerList.clear();
      try {
        gazetteerList.load(true);
      } catch (ResourceInstantiationException rie) {
        rie.printStackTrace();
        return;
      }
      // reselect the row to redisplay the list
      int row = definitionTable.getSelectedRow();
      definitionTable.clearSelection();
      definitionTable.getSelectionModel().setSelectionInterval(row, row);
    }
  }

  protected class SaveAndReinitialiseGazetteerAction extends AbstractAction {
    public SaveAndReinitialiseGazetteerAction() {
      super("Save and Reinitialise");
      putValue(SHORT_DESCRIPTION,
        "Save the definition and all the lists then reinitialise");
      putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control S"));
    }
    public void actionPerformed(ActionEvent e) {
      try {
        if (linearDefinition.isModified()) {
          linearDefinition.store();
        }
        for (Object object : linearDefinition.getListsByNode().values()) {
          GazetteerList gazetteerList = (GazetteerList) object;
          if (gazetteerList.isModified()) {
            gazetteerList.store();
          }
        }
        gazetteer.reInit();
        MainFrame.getInstance().statusChanged("Gazetteer saved in " +
          linearDefinition.getURL().getPath());
        definitionTable.repaint();

      } catch (ResourceInstantiationException re) {
        MainFrame.getInstance().statusChanged(
          "Unable to save the Gazetteer.");
        Err.prln("Unable to save the Gazetteer.\n" + re.getMessage());
      }
    }
  }

  protected class SaveAsGazetteerAction extends AbstractAction {
    public SaveAsGazetteerAction() {
      super("Save as...");
      putValue(SHORT_DESCRIPTION, "Save the definition and all the lists");
      putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control shift S"));
    }
    public void actionPerformed(ActionEvent e) {
      XJFileChooser fileChooser = MainFrame.getFileChooser();
      ExtensionFileFilter filter =
        new ExtensionFileFilter("Gazetteer files", "def");
      fileChooser.addChoosableFileFilter(filter);
      fileChooser.setMultiSelectionEnabled(false);
      fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      fileChooser.setDialogTitle("Select a file name...");
      fileChooser.setResource(GazetteerEditor.class.getName());
      int result = fileChooser.showSaveDialog(GazetteerEditor.this);
      if (result == JFileChooser.APPROVE_OPTION) {
        File selectedFile = fileChooser.getSelectedFile();
        if (selectedFile == null) { return; }
        try {
          URL previousURL = linearDefinition.getURL();
          linearDefinition.setURL(selectedFile.toURI().toURL());
          linearDefinition.store();
          linearDefinition.setURL(previousURL);
          for (Object object : linearDefinition.getListsByNode().values()) {
            GazetteerList gazetteerList = (GazetteerList) object;
            previousURL = gazetteerList.getURL();
            gazetteerList.setURL(new File(selectedFile.getParentFile(),
              Files.fileFromURL(gazetteerList.getURL()).getName())
              .toURI().toURL());
            gazetteerList.store();
            gazetteerList.setURL(previousURL);
            gazetteerList.setModified(false);
          }
          MainFrame.getInstance().statusChanged("Gazetteer saved in " +
            selectedFile.getAbsolutePath());
          definitionTable.repaint();

        } catch (ResourceInstantiationException re) {
          MainFrame.getInstance().statusChanged(
            "Unable to save the Gazetteer.");
          Err.prln("Unable to save the Gazetteer.\n" + re.getMessage());
        } catch (MalformedURLException mue) {
          mue.printStackTrace();
        }
      }
    }
  }

  protected class DeleteSelectedLinearNodeAction extends AbstractAction {
    public DeleteSelectedLinearNodeAction() {
      super("Delete Selection");
      putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("shift DELETE"));
    }

    public void actionPerformed(ActionEvent e) {
      int[] rowsToDelete = definitionTable.getSelectedRows();
      definitionTable.clearSelection();
      for (int i = 0; i < rowsToDelete.length; i++) {
        rowsToDelete[i] = definitionTable.rowViewToModel(rowsToDelete[i]);
      }
      Arrays.sort(rowsToDelete);
      for (int i = rowsToDelete.length-1; i >= 0; i--) {
        definitionTableModel.removeRow(rowsToDelete[i]);
        linearDefinition.remove(rowsToDelete[i]);
      }
    }
  }

  protected class DeleteSelectedGazetteerNodeAction extends AbstractAction {
    public DeleteSelectedGazetteerNodeAction() {
      super("Delete Selection");
      putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("shift DELETE"));
    }

    public void actionPerformed(ActionEvent e) {
      int[] rowsToDelete = listTable.getSelectedRows();
      listTable.clearSelection();
      for (int i = 0; i < rowsToDelete.length; i++) {
        rowsToDelete[i] = listTable.rowViewToModel(rowsToDelete[i]);
      }
      Arrays.sort(rowsToDelete);
      for (int i = rowsToDelete.length-1; i >= 0; i--) {
        listTableModel.removeRow(rowsToDelete[i]);
      }
      listTableModel.fireTableDataChanged();
    }
  }

  // local variables
  protected Gazetteer gazetteer;
  /** the linear definition being displayed */
  protected LinearDefinition linearDefinition;
  /** the linear node currently selected */
  protected LinearNode selectedLinearNode;
  protected Collator collator;
  protected List<Action> actions;

  // user interface components
  protected XJTable definitionTable;
  protected DefaultTableModel definitionTableModel;
  protected XJTable listTable;
  protected ListTableModel listTableModel;
  protected JComboBox newListComboBox;
  protected JButton newListButton;
  protected JTextField newEntryTextField;
  protected JTextField listFilterTextField;
  protected JLabel listCountLabel;
}
