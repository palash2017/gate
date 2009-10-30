/*
 *  Copyright (c) 2009, Ontotext AD.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Thomas Heitz - 10 June 2009
 *
 *  $Id$
 */

package gate.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.Timer;
import java.text.NumberFormat;
import java.text.Collator;
import java.io.*;

import javax.swing.*;
import javax.swing.event.AncestorListener;
import javax.swing.event.AncestorEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.*;

import gate.*;
import gate.util.AnnotationDiffer;
import gate.util.Strings;
import gate.util.ExtensionFileFilter;
import gate.creole.AbstractVisualResource;
import gate.event.CorpusEvent;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.GuiType;
import gate.event.CorpusListener;
import gate.swing.XJTable;
import gate.swing.XJFileChooser;

/**
 * Quality assurance corpus view.
 * Compare two sets of annotations with optionally their features
 * globally for each annotation and for each document inside a corpus
 * with different measures notably precision, recall and F1-score.
 */
@CreoleResource(name = "Corpus Quality Assurance", guiType = GuiType.LARGE,
    resourceDisplayed = "gate.Corpus", mainViewer = false)
public class CorpusQualityAssurance extends AbstractVisualResource
  implements CorpusListener {

  public Resource init(){
    initLocalData();
    initGuiComponents();
    initListeners();
    return this;
  }

  protected void initLocalData(){
    collator = Collator.getInstance(Locale.ENGLISH);
    collator.setStrength(Collator.TERTIARY);
    documentTableModel = new DocumentTableModel();
    annotationTableModel = new AnnotationTableModel();
    types = new TreeSet<String>(collator);
    docsSetsTypesFeatures = new LinkedHashMap<String, TreeMap<String,
      TreeMap<String, TreeSet<String>>>>();
    corpusChanged = false;
    doubleComparator = new Comparator<String>() {
      public int compare(String s1, String s2) {
        if (s1 == null || s2 == null) {
          return 0;
        } else if (s1.equals("")) {
          return 1;
        } else if (s2.equals("")) {
          return -1;
        } else {
          return Double.valueOf(s1).compareTo(Double.valueOf(s2));
        }
      }
    };
    totalComparator = new Comparator<String>() {
      public int compare(String s1, String s2) {
        if (s1 == null || s2 == null) {
          return 0;
        } else if (s1.equals("Micro summary")) {
          return s2.equals("Macro summary") ? -1 : 1;
        } else if (s1.equals("Macro summary")) {
          return s2.equals("Micro summary") ? -1 : 1;
        } else if (s2.equals("Micro summary")) {
          return s1.equals("Macro summary") ? 1 : -1;
        } else if (s2.equals("Macro summary")) {
          return s1.equals("Micro summary") ? 1 : -1;
        } else {
          return s1.compareTo(s2);
        }
      }
    };
  }

  protected void initGuiComponents(){
    setLayout(new BorderLayout());

    JPanel sidePanel = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    sidePanel.add(Box.createVerticalStrut(5), gbc);
    JToolBar toolbar = new JToolBar();
    toolbar.setFloatable(false);
    toolbar.add(openDocumentAction = new OpenDocumentAction());
    openDocumentAction.setEnabled(false);
    toolbar.add(openAnnotationDiffAction = new OpenAnnotationDiffAction());
    openAnnotationDiffAction.setEnabled(false);
    toolbar.add(new ExportToHtmlAction());
    toolbar.add(reloadCacheAction = new ReloadCacheAction());
    sidePanel.add(toolbar, gbc);
    sidePanel.add(Box.createVerticalStrut(5), gbc);
    JLabel label = new JLabel("Annotation Sets A & B");
    label.setToolTipText("aka 'Key & Response sets'");
    gbc.anchor = GridBagConstraints.NORTH;
    gbc.fill = GridBagConstraints.BOTH;
    sidePanel.add(label, gbc);
    sidePanel.add(Box.createVerticalStrut(2), gbc);
    setList = new JList();
    setList.setSelectionModel(new ToggleSelectionABModel(setList));
    setList.setPrototypeCellValue("present in every document");
    setList.setVisibleRowCount(4);
    sidePanel.add(new JScrollPane(setList), gbc);
    sidePanel.add(Box.createVerticalStrut(2), gbc);
    setCheck = new JCheckBox("present in every document", false);
    setCheck.addActionListener(new AbstractAction(){
      public void actionPerformed(ActionEvent e) {
        updateSets();
      }
    });
    sidePanel.add(setCheck, gbc);
    sidePanel.add(Box.createVerticalStrut(5), gbc);
    label = new JLabel("Annotation Types");
    label.setToolTipText("Annotation types to compare");
    sidePanel.add(label, gbc);
    sidePanel.add(Box.createVerticalStrut(2), gbc);
    typeList = new JList();
    typeList.setSelectionModel(new ToggleSelectionModel());
    typeList.setPrototypeCellValue("present in every document");
    typeList.setVisibleRowCount(4);
    sidePanel.add(new JScrollPane(typeList), gbc);
    sidePanel.add(Box.createVerticalStrut(2), gbc);
    typeCheck = new JCheckBox("present in every selected set", false);
    typeCheck.addActionListener(new AbstractAction(){
      public void actionPerformed(ActionEvent e) {
        setList.getListSelectionListeners()[0].valueChanged(null);
      }
    });
    sidePanel.add(typeCheck, gbc);
    sidePanel.add(Box.createVerticalStrut(5), gbc);
    label = new JLabel("Annotation Features");
    label.setToolTipText("Annotation features to compare");
    sidePanel.add(label, gbc);
    sidePanel.add(Box.createVerticalStrut(2), gbc);
    featureList = new JList();
    featureList.setSelectionModel(new ToggleSelectionModel());
    featureList.setPrototypeCellValue("present in every document");
    featureList.setVisibleRowCount(4);
    sidePanel.add(new JScrollPane(featureList), gbc);
    sidePanel.add(Box.createVerticalStrut(2), gbc);
    featureCheck = new JCheckBox("present in every selected type", false);
    featureCheck.addActionListener(new AbstractAction(){
      public void actionPerformed(ActionEvent e) {
        typeList.getListSelectionListeners()[0].valueChanged(null);
      }
    });
    sidePanel.add(featureCheck, gbc);
    sidePanel.add(Box.createVerticalStrut(5), gbc);
    label = new JLabel("Measures");
    label.setToolTipText("Measures used to compare annotations");
    sidePanel.add(label, gbc);
    sidePanel.add(Box.createVerticalStrut(2), gbc);
    measureList = new JList();
    measureList.setSelectionModel(new ToggleSelectionModel());
    measureList.setModel(new ExtendedListModel(
      new String[]{"F1-score strict","F1-score lenient", "F1-score average"}));
    measureList.setPrototypeCellValue("present in every document");
    measureList.setVisibleRowCount(3);
    sidePanel.add(new JScrollPane(measureList), gbc);
    sidePanel.add(Box.createVerticalStrut(2), gbc);
    JButton button = new JButton(compareAction = new CompareAction());
    compareAction.setEnabled(false);
    sidePanel.add(button, gbc);
    sidePanel.add(Box.createVerticalStrut(5), gbc);
    progressBar = new JProgressBar();
    progressBar.setStringPainted(true);
    progressBar.setString("");
    sidePanel.add(progressBar, gbc);
    gbc.gridheight = GridBagConstraints.REMAINDER;
    gbc.weighty = 1;
    sidePanel.add(Box.createVerticalStrut(5), gbc);

    annotationTable = new XJTable(annotationTableModel) {
      // table header tool tips
      protected JTableHeader createDefaultTableHeader() {
        return new JTableHeader(columnModel) {
          public String getToolTipText(MouseEvent e) {
            Point p = e.getPoint();
            int index = columnModel.getColumnIndexAtX(p.x);
            if (index == -1) { return null; }
            int realIndex = columnModel.getColumn(index).getModelIndex();
            return annotationTableModel.headerTooltips.get(realIndex);
          }
        };
      }
    };
    annotationTable.setSortable(false);
    annotationTable.setEnableHidingColumns(true);
    annotationTable.setAutoResizeMode(XJTable.AUTO_RESIZE_ALL_COLUMNS);
    JScrollPane annotationScroller = new JScrollPane(annotationTable);
    annotationScroller.setHorizontalScrollBarPolicy(
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    annotationScroller.getViewport().setBackground(
      annotationTable.getBackground());

    documentTable = new XJTable(documentTableModel) {
      // table header tool tips
      protected JTableHeader createDefaultTableHeader() {
        return new JTableHeader(columnModel) {
          public String getToolTipText(MouseEvent e) {
            Point p = e.getPoint();
            int index = columnModel.getColumnIndexAtX(p.x);
            if (index == -1) { return null; }
            int realIndex = columnModel.getColumn(index).getModelIndex();
            return documentTableModel.headerTooltips.get(realIndex);
          }
        };
      }
    };
    documentTable.setSortable(false);
    documentTable.setEnableHidingColumns(true);
    documentTable.setAutoResizeMode(XJTable.AUTO_RESIZE_ALL_COLUMNS);
    JScrollPane documentScroller = new JScrollPane(documentTable);
    documentScroller.setHorizontalScrollBarPolicy(
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    documentScroller.getViewport().setBackground(documentTable.getBackground());

    JTabbedPane tabbedPane = new JTabbedPane();
    tabbedPane.addTab("Corpus statistics", null, annotationScroller,
      "Compare each annotation type for the whole corpus");
    tabbedPane.addTab("Document statistics", null, documentScroller,
      "Compare each documents in the corpus with theirs annotations");

    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    splitPane.setContinuousLayout(true);
    splitPane.setOneTouchExpandable(true);
    splitPane.setResizeWeight(0.80);
    splitPane.setLeftComponent(tabbedPane);
    splitPane.setRightComponent(sidePanel);

    add(splitPane);
  }

  protected void initListeners(){

    // when the view is shown update the tables if the corpus has changed
    addAncestorListener(new AncestorListener() {
      public void ancestorAdded(AncestorEvent event) {
        if (!isShowing() || !corpusChanged) { return; }
        if (timerTask != null) { timerTask.cancel(); }
        Date timeToRun = new Date(System.currentTimeMillis() + 1000);
        timerTask = new TimerTask() { public void run() {
          updateSets();
        }};
        timer.schedule(timerTask, timeToRun); // add a delay before updating
      }
      public void ancestorRemoved(AncestorEvent event) { /* do nothing */ }
      public void ancestorMoved(AncestorEvent event) { /* do nothing */ }
    });

    // when set list selection change
    setList.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        typeList.setModel(new ExtendedListModel());
        keySetName = ((ToggleSelectionABModel)
          setList.getSelectionModel()).getSelectedValueA();
        responseSetName = ((ToggleSelectionABModel)
          setList.getSelectionModel()).getSelectedValueB();
        if (keySetName == null
         || responseSetName == null
         || setList.getSelectionModel().getValueIsAdjusting()) {
          compareAction.setEnabled(false);
          return;
        }
        setList.setEnabled(false);
        setCheck.setEnabled(false);
        // update type UI list
        TreeSet<String> someTypes = new TreeSet<String>();
        TreeMap<String, TreeSet<String>> typesFeatures;
        boolean firstLoop = true; // needed for retainAll to work
        for (TreeMap<String, TreeMap<String, TreeSet<String>>>
            setsTypesFeatures : docsSetsTypesFeatures.values()) {
          typesFeatures = setsTypesFeatures.get(
            keySetName.equals("[Default set]") ? "" : keySetName);
          if (typesFeatures != null) {
            if (typeCheck.isSelected() && !firstLoop) {
              someTypes.retainAll(typesFeatures.keySet());
            } else {
              someTypes.addAll(typesFeatures.keySet());
            }
          }
          typesFeatures = setsTypesFeatures.get(
            responseSetName.equals("[Default set]") ? "" : responseSetName);
          if (typesFeatures != null) {
            if (typeCheck.isSelected() && !firstLoop) {
              someTypes.retainAll(typesFeatures.keySet());
            } else {
              someTypes.addAll(typesFeatures.keySet());
            }
          }
          firstLoop = false;
        }
        typeList.setVisibleRowCount(Math.min(4, someTypes.size()));
        typeList.setModel(new ExtendedListModel(someTypes.toArray()));
        setList.setEnabled(true);
        setCheck.setEnabled(true);
        compareAction.setEnabled(true);
      }
    });

    // when type list selection change
    typeList.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        // update feature UI list
        featureList.setModel(new ExtendedListModel());
        if (typeList.getSelectedValues().length == 0
         || typeList.getSelectionModel().getValueIsAdjusting()) {
          return;
        }
        final Set<String> typeNames = new HashSet<String>();
        for (Object type : typeList.getSelectedValues()) {
          typeNames.add((String) type);
        }
        typeList.setEnabled(false);
        TreeSet<String> features = new TreeSet<String>(collator);
        TreeMap<String, TreeSet<String>> typesFeatures;
        boolean firstLoop = true; // needed for retainAll to work
        for (TreeMap<String, TreeMap<String, TreeSet<String>>> sets :
             docsSetsTypesFeatures.values()) {
          typesFeatures = sets.get(keySetName.equals("[Default set]") ?
            "" : keySetName);
          if (typesFeatures != null) {
            for (String typeName : typesFeatures.keySet()) {
              if (typeNames.contains(typeName)) {
                if (featureCheck.isSelected() && !firstLoop) {
                  features.retainAll(typesFeatures.get(typeName));
                } else {
                  features.addAll(typesFeatures.get(typeName));
                }
              }
            }
          }
          typesFeatures = sets.get(responseSetName.equals("[Default set]") ?
            "" : responseSetName);
          if (typesFeatures != null) {
            for (String typeName : typesFeatures.keySet()) {
              if (typeNames.contains(typeName)) {
                if (featureCheck.isSelected() && !firstLoop) {
                  features.retainAll(typesFeatures.get(typeName));
                } else {
                  features.addAll(typesFeatures.get(typeName));
                }
              }
            }
          }
          firstLoop = false;
        }
        featureList.setVisibleRowCount(Math.min(4, features.size()));
        featureList.setModel(new ExtendedListModel(features.toArray()));
        typeList.setEnabled(true);
      }
    });

    // enable/disable toolbar icons according to the document table selection
    documentTable.getSelectionModel().addListSelectionListener(
      new ListSelectionListener() {
        public void valueChanged(ListSelectionEvent e) {
          if (e.getValueIsAdjusting()) { return; }
          boolean enabled = documentTable.getSelectedRow() != -1
            && !((String)documentTable.getValueAt(
              documentTable.getSelectedRow(),
              DocumentTableModel.COL_DOCUMENT)).endsWith("summary");
          openDocumentAction.setEnabled(enabled);
          openAnnotationDiffAction.setEnabled(enabled);
        }
      }
    );
  }

  protected class ExtendedListModel extends DefaultListModel {
    public ExtendedListModel() {
      super();
    }
    public ExtendedListModel(Object[] elements) {
      super();
      for (Object element : elements) {
        super.addElement(element);
      }
    }
  }

  protected class ToggleSelectionModel extends DefaultListSelectionModel {
    boolean gestureStarted = false;
    public void setSelectionInterval(int index0, int index1) {
      if (isSelectedIndex(index0) && !gestureStarted) {
        super.removeSelectionInterval(index0, index1);
      } else {
        super.addSelectionInterval(index0, index1);
      }
      gestureStarted = true;
    }
    public void setValueIsAdjusting(boolean isAdjusting) {
      if (!isAdjusting) {
        gestureStarted = false;
      }
    }
  }

  /**
   * Add a suffix A and B for the first and second selected item.
   * Allows only 2 items to be selected.
   */
  protected class ToggleSelectionABModel extends DefaultListSelectionModel {
    public ToggleSelectionABModel(JList list) {
      this.list = list;
    }
    public void setSelectionInterval(int index0, int index1) {
      ExtendedListModel model = (ExtendedListModel) list.getModel();
      String value = (String) model.getElementAt(index0);
      if (value.endsWith(" (A)") || value.endsWith(" (B)")) {
        // if ends with ' (A)' or ' (B)' then remove the suffix
        model.removeElementAt(index0);
        model.insertElementAt(value.substring(0,
          value.length() - " (A)".length()), index0);
        if (value.endsWith(" (A)")) {
          selectedValueA = null;
        } else {
          selectedValueB = null;
        }
        removeSelectionInterval(index0, index1);
      } else {
        // suffix with ' (A)' or ' (B)' if not already existing
        if (selectedValueA == null) {
          model.removeElementAt(index0);
          model.insertElementAt(value + " (A)", index0);
          selectedValueA = value;
          addSelectionInterval(index0, index1);
        } else if (selectedValueB == null) {
          model.removeElementAt(index0);
          model.insertElementAt(value + " (B)", index0);
          selectedValueB = value;
          addSelectionInterval(index0, index1);
        }
      }
    }
    public void clearSelection() {
      selectedValueA = null;
      selectedValueB = null;
      super.clearSelection();
    }
    public String getSelectedValueA() {
      return selectedValueA;
    }
    public String getSelectedValueB() {
      return selectedValueB;
    }
    JList list;
    String selectedValueA, selectedValueB;
  }

  public void cleanup(){
    super.cleanup();
    corpus = null;
  }

  public void setTarget(Object target){
    if(corpus != null && corpus != target){
      //we already had a different corpus
      corpus.removeCorpusListener(this);
    }
    if(!(target instanceof Corpus)){
      throw new IllegalArgumentException(
        "This view can only be used with a GATE corpus!\n" +
        target.getClass().toString() + " is not a GATE corpus!");
    }
    this.corpus = (Corpus) target;
    corpus.addCorpusListener(this);

    corpusChanged = true;
    if (!isShowing()) { return; }
    if (timerTask != null) { timerTask.cancel(); }
    Date timeToRun = new Date(System.currentTimeMillis() + 2000);
    timerTask = new TimerTask() { public void run() {
      updateSets();
    }};
    timer.schedule(timerTask, timeToRun); // add a delay before updating
  }

  public void documentAdded(final CorpusEvent e) {
    corpusChanged = true;
    if (!isShowing()) { return; }
    if (timerTask != null) { timerTask.cancel(); }
    Date timeToRun = new Date(System.currentTimeMillis() + 2000);
    timerTask = new TimerTask() { public void run() {
      updateSets();
    }};
    timer.schedule(timerTask, timeToRun); // add a delay before updating
  }

  public void documentRemoved(final CorpusEvent e) {
    corpusChanged = true;
    if (!isShowing()) { return; }
    if (timerTask != null) { timerTask.cancel(); }
    Date timeToRun = new Date(System.currentTimeMillis() + 2000);
    timerTask = new TimerTask() { public void run() {
      updateSets();
    }};
    timer.schedule(timerTask, timeToRun); // add a delay before updating
  }

  /**
   * Update set lists.
   */
  protected void updateSets() {
    if (!isShowing()) { return; }
    corpusChanged = false;
    SwingUtilities.invokeLater(new Runnable(){ public void run() {
      setList.clearSelection();
      compareAction.setEnabled(false);
      progressBar.setMaximum(corpus.size() - 1);
      progressBar.setString("Read sets, types, features");
      reloadCacheAction.setEnabled(false);
    }});
    CorpusQualityAssurance.this.setCursor(
      Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    Runnable runnable = new Runnable() { public void run() {
    if (docsSetsTypesFeatures.size() != corpus.getDocumentNames().size()
    || !docsSetsTypesFeatures.keySet().containsAll(corpus.getDocumentNames())) {
    docsSetsTypesFeatures.clear();
    TreeMap<String, TreeMap<String, TreeSet<String>>> setsTypesFeatures;
    TreeMap<String, TreeSet<String>> typesFeatures;
    TreeSet<String> features;
    for (int i = 0; i < corpus.size(); i++) {
      // fill in the lists of document, set, type and feature names
      boolean documentWasLoaded = corpus.isDocumentLoaded(i);
      Document document = (Document) corpus.get(i);
      if (document != null && document.getAnnotationSetNames() != null) {
        setsTypesFeatures =
          new TreeMap<String, TreeMap<String, TreeSet<String>>>(collator);
        HashSet<String> setNames =
          new HashSet<String>(document.getAnnotationSetNames());
        setNames.add("");
        for (String set : setNames) {
          typesFeatures = new TreeMap<String, TreeSet<String>>(collator);
          AnnotationSet annotations = document.getAnnotations(set);
          for (String type : annotations.getAllTypes()) {
            features = new TreeSet<String>(collator);
            for (Annotation annotation : annotations.get(type)) {
              for (Object featureKey : annotation.getFeatures().keySet()) {
                features.add((String) featureKey);
              }
            }
            typesFeatures.put(type, features);
          }
          setsTypesFeatures.put(set, typesFeatures);
        }
        docsSetsTypesFeatures.put(document.getName(), setsTypesFeatures);
      }
      if (!documentWasLoaded) {
        corpus.unloadDocument(document);
        Factory.deleteResource(document);
      }
      final int progressValue = i + 1;
      SwingUtilities.invokeLater(new Runnable(){ public void run() {
        if (progressBar.isShowing()) {
          progressBar.setValue(progressValue);
        }
      }});
    }
    }
    final TreeSet<String> setsNames = new TreeSet<String>(collator);
    Set<String> sets;
    boolean firstLoop = true; // needed for retainAll to work
    for (String document : docsSetsTypesFeatures.keySet()) {
      // get the list of set names
      sets = docsSetsTypesFeatures.get(document).keySet();
      if (setCheck.isSelected() && !firstLoop) {
        setsNames.retainAll(sets);
      } else {
        setsNames.addAll(sets);
      }
      firstLoop = false;
    }
    SwingUtilities.invokeLater(new Runnable(){ public void run(){
      // update the UI lists of sets
      setsNames.remove("");
      setsNames.add("[Default set]");
      setList.setVisibleRowCount(
        Math.min(4, setsNames.size()));
      setList.setModel(
        new ExtendedListModel(setsNames.toArray()));
      progressBar.setValue(progressBar.getMinimum());
      progressBar.setString("");
      CorpusQualityAssurance.this.setCursor(
        Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      reloadCacheAction.setEnabled(true);
    }});
    }};
    Thread thread = new Thread(runnable, "updateSets");
    thread.setPriority(Thread.MIN_PRIORITY);
    thread.start();
  }

  protected void compareAnnotation() {
    SwingUtilities.invokeLater(new Runnable(){ public void run(){
      progressBar.setMaximum(corpus.size() - 1);
      progressBar.setString("Compare annotations");
    }});
    differsByDocThenType.clear();
    documentNames.clear();
    // for each document
    for (int row = 0; row < corpus.size(); row++) {
      boolean documentWasLoaded = corpus.isDocumentLoaded(row);
      Document document = (Document) corpus.get(row);
      documentNames.add(document.getName());
      Set<Annotation> keys = new HashSet<Annotation>();
      Set<Annotation> responses = new HashSet<Annotation>();
      // get annotations from selected annotation sets
      if (keySetName.equals("[Default set]")) {
        keys = document.getAnnotations();
      } else if (document.getAnnotationSetNames() != null
      && document.getAnnotationSetNames().contains(keySetName)) {
        keys = document.getAnnotations(keySetName);
      }
      if (responseSetName.equals("[Default set]")) {
        responses = document.getAnnotations();
      } else if (document.getAnnotationSetNames() != null
      && document.getAnnotationSetNames()
        .contains(responseSetName)) {
        responses = document.getAnnotations(responseSetName);
      }
      if (!documentWasLoaded) {
        corpus.unloadDocument(document);
        Factory.deleteResource(document);
      }
      types.clear();
      for (Object type : typeList.getSelectedValues()) {
        types.add((String) type);
      }
      if (typeList.isSelectionEmpty()) {
        for (int i = 0; i < typeList.getModel().getSize(); i++) {
          types.add((String) typeList.getModel().getElementAt(i));
        }
      }
      Set<String> featureSet = new HashSet<String>();
      for (Object feature : featureList.getSelectedValues()) {
        featureSet.add((String) feature);
      }
      HashMap<String, AnnotationDiffer> differsByType =
        new HashMap<String, AnnotationDiffer>();
      AnnotationDiffer differ;
      Set<Annotation> keysIter = new HashSet<Annotation>();
      Set<Annotation> responsesIter = new HashSet<Annotation>();
      for (String type : types) {
        if (keys instanceof AnnotationSet && !types.isEmpty()) {
          keysIter = ((AnnotationSet)keys).get(type);
        }
        if (responses instanceof AnnotationSet && !types.isEmpty()) {
          responsesIter = ((AnnotationSet)responses).get(type);
        }
        differ = new AnnotationDiffer();
        differ.setSignificantFeaturesSet(featureSet);
        differ.calculateDiff(keysIter, responsesIter); // compare
        differsByType.put(type, differ);
      }
      differsByDocThenType.add(differsByType);
      final int progressValue = row + 1;
      SwingUtilities.invokeLater(new Runnable(){ public void run(){
        progressBar.setValue(progressValue);
      }});
    }
    SwingUtilities.invokeLater(new Runnable(){ public void run(){
      progressBar.setValue(progressBar.getMinimum());
      progressBar.setString("");
    }});
  }

  protected double getMeasureValue(int column, AnnotationDiffer differ,
                                   int columnCount) {
    int colMeasure = (column - columnCount) % 3;
    int measureIndex = (column  - columnCount) / 3;
    String measure = (String)
      measureList.getSelectedValues()[measureIndex];
    switch (colMeasure) {
      case 0: // recall
        if (measure.equals("F1-score strict")) {
          return differ.getRecallStrict();
        } else if (measure.equals("F1-score lenient")) {
          return differ.getRecallLenient();
        } else if (measure.equals("F1-score average")) {
          return differ.getRecallAverage();
        }
      case 1: // precision
        if (measure.equals("F1-score strict")) {
          return differ.getPrecisionStrict();
        } else if (measure.equals("F1-score lenient")) {
          return differ.getPrecisionLenient();
        } else if (measure.equals("F1-score average")) {
          return differ.getPrecisionAverage();
        }
      case 2: // f-measure
        if (measure.equals("F1-score strict")) {
          return differ.getFMeasureStrict(1.0);
        } else if (measure.equals("F1-score lenient")) {
          return differ.getFMeasureLenient(1.0);
        } else if (measure.equals("F1-score average")) {
          return differ.getFMeasureAverage(1.0);
        }
      default:
        return 0.0;
    }
  }

  protected class AnnotationTableModel extends AbstractTableModel{

    public AnnotationTableModel() {
      f.setMaximumFractionDigits(2);
      f.setMinimumFractionDigits(2);
    }

    public void updateColumns() {
      columnNames = new ArrayList<String>(Arrays.asList(COLUMN_NAMES));
      headerTooltips = new ArrayList<String>(Arrays.asList(HEADER_TOOLTIPS));
      for (Object measure : measureList.getSelectedValues()) {
        if (measure.equals("F1-score strict")) {
          columnNames.addAll(Arrays.asList("Rec.B/A", "Prec.B/A", "F1-strict"));
        } else if (measure.equals("F1-score lenient")) {
          columnNames.addAll(Arrays.asList("Rec.B/A", "Prec.B/A", "F1-lenient"));
        } else if (measure.equals("F1-score average")) {
          columnNames.addAll(Arrays.asList("Rec.B/A", "Prec.B/A", "F1-average"));
        }
        headerTooltips.addAll(Arrays.asList("Recall for B relative to A",
          "Precision for B relative to A",
          "Combine precision and recall with the same weight for each"));
      }
    }

    public int getColumnCount() {
      return columnNames.size();
    }

    public int getRowCount() {
      return (types.isEmpty()) ? 0 : types.size() + 2;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
      // get the counts and measures for the current type/row
      if (rowIndex < types.size()) {
        String type = (String) types.toArray()[rowIndex];
        ArrayList<AnnotationDiffer> differs =
          new ArrayList<AnnotationDiffer>();
        for (HashMap<String, AnnotationDiffer> differsByType :
            differsByDocThenType) {
          differs.add(differsByType.get(type));
        }
        AnnotationDiffer differ = new AnnotationDiffer(differs);
        switch(columnIndex) {
          case COL_ANNOTATION:
            return type;
          case COL_CORRECT:
            return Integer.toString(differ.getCorrectMatches());
          case COL_MISSING:
            return Integer.toString(differ.getMissing());
          case COL_SPURIOUS:
            return Integer.toString(differ.getSpurious());
          case COL_PARTIAL:
            return Integer.toString(differ.getPartiallyCorrectMatches());
          default:
            return f.format(getMeasureValue(columnIndex, differ, COLUMN_COUNT));
        }
      } else if (rowIndex == types.size()) {
        // get the sum counts and average measures by type
        switch(columnIndex) {
          case COL_ANNOTATION:
            return "Macro summary";
          case COL_CORRECT:
          case COL_MISSING:
          case COL_SPURIOUS:
          case COL_PARTIAL:
            return "";
          default:
            double sumDbl= 0;
            for (int row = 0; row < types.size(); row++) {
              sumDbl += Double.valueOf((String) getValueAt(row, columnIndex));
            }
            return f.format(sumDbl / types.size());
        }
      } else if (rowIndex == types.size() + 1) {
        // get the sum counts and average measures by corpus
        switch(columnIndex) {
          case COL_ANNOTATION:
            return "Micro summary";
          case COL_CORRECT:
          case COL_MISSING:
          case COL_SPURIOUS:
          case COL_PARTIAL:
            int sumInt = 0;
            for (int row = 0; row < types.size(); row++) {
              sumInt += Integer.valueOf((String) getValueAt(row, columnIndex));
            }
            return Integer.toString(sumInt);
          default:
            ArrayList<AnnotationDiffer> differs =
              new ArrayList<AnnotationDiffer>();
            for (HashMap<String, AnnotationDiffer> differsByType :
                differsByDocThenType) {
              differs.addAll(differsByType.values());
            }
            AnnotationDiffer differ = new AnnotationDiffer(differs);
            return f.format(getMeasureValue(columnIndex, differ, COLUMN_COUNT));
        }
      } else {
        return "";
      }
    }

    public Class<?> getColumnClass(int columnIndex) {
      return String.class;
    }

    public String getColumnName(int column) {
      return columnNames.get(column);
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
      return false;
    }

    private NumberFormat f = NumberFormat.getInstance(Locale.ENGLISH);

    private final String[] COLUMN_NAMES = {
      "Annotation", "Match", "Only A", "Only B", "Overlap"};
    private final String[] HEADER_TOOLTIPS = {null,
      "aka 'Correct'", "aka 'Missing'", "aka 'Spurious'", "aka 'Partial'"};
    private ArrayList<String> columnNames =
      new ArrayList<String>(Arrays.asList(COLUMN_NAMES));
    public ArrayList<String> headerTooltips =
      new ArrayList<String>(Arrays.asList(HEADER_TOOLTIPS));

    public static final int COL_ANNOTATION = 0;
    public static final int COL_CORRECT = 1;
    public static final int COL_MISSING = 2;
    public static final int COL_SPURIOUS = 3;
    public static final int COL_PARTIAL = 4;
    public static final int COLUMN_COUNT = 5;
    public static final int COL_RECALL = 0;
    public static final int COL_PRECISION = 1;
    public static final int COL_FMEASURE = 2;
  }

  protected class DocumentTableModel extends AbstractTableModel{

    public DocumentTableModel() {
      f.setMaximumFractionDigits(2);
      f.setMinimumFractionDigits(2);
    }

    public void updateColumns() {
      columnNames = new ArrayList<String>(Arrays.asList(COLUMN_NAMES));
      headerTooltips = new ArrayList<String>(Arrays.asList(HEADER_TOOLTIPS));
      for (Object measure : measureList.getSelectedValues()) {
        if (measure.equals("F1-score strict")) {
          columnNames.addAll(Arrays.asList("Rec.B/A", "Prec.B/A", "F1-strict"));
        } else if (measure.equals("F1-score lenient")) {
          columnNames.addAll(Arrays.asList("Rec.B/A", "Prec.B/A", "F1-lenient"));
        } else if (measure.equals("F1-score average")) {
          columnNames.addAll(Arrays.asList("Rec.B/A", "Prec.B/A", "F1-average"));
        }
        headerTooltips.addAll(Arrays.asList("Recall for B relative to A",
          "Precision for B relative to A",
          "Combine precision and recall with the same weight for each"));
      }
    }

    public int getColumnCount() {
      return columnNames.size();
    }

    public int getRowCount() {
      return (differsByDocThenType.isEmpty()) ?
        0 : differsByDocThenType.size() + 2;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
      if (rowIndex < differsByDocThenType.size()) {
        // get the counts and measures for the current document/row
        HashMap<String, AnnotationDiffer> differsByType =
          differsByDocThenType.get(rowIndex);
        AnnotationDiffer differ = new AnnotationDiffer(differsByType.values());
        switch(columnIndex) {
          case COL_DOCUMENT:
            return documentNames.get(rowIndex);
          case COL_CORRECT:
            return Integer.toString(differ.getCorrectMatches());
          case COL_MISSING:
            return Integer.toString(differ.getMissing());
          case COL_SPURIOUS:
            return Integer.toString(differ.getSpurious());
          case COL_PARTIAL:
            return Integer.toString(differ.getPartiallyCorrectMatches());
          default:
            return f.format(getMeasureValue(columnIndex, differ, COLUMN_COUNT));
        }
      } else if (rowIndex == differsByDocThenType.size()) {
        // get the sum counts and average measures by document
        switch(columnIndex) {
          case COL_DOCUMENT:
            return "Macro summary";
          case COL_CORRECT:
          case COL_MISSING:
          case COL_SPURIOUS:
          case COL_PARTIAL:
            return "";
          default:
            double sumDbl= 0;
            for (int row = 0; row < differsByDocThenType.size(); row++) {
              sumDbl += Double.valueOf((String) getValueAt(row, columnIndex));
            }
            return f.format(sumDbl / differsByDocThenType.size());
        }
      } else if (rowIndex == differsByDocThenType.size() + 1) {
        // get the sum counts and average measures by corpus
        switch(columnIndex) {
          case COL_DOCUMENT:
            return "Micro summary";
          case COL_CORRECT:
          case COL_MISSING:
          case COL_SPURIOUS:
          case COL_PARTIAL:
            int sumInt = 0;
            for (int row = 0; row < differsByDocThenType.size(); row++) {
              sumInt += Integer.valueOf((String) getValueAt(row, columnIndex));
            }
            return Integer.toString(sumInt);
          default:
            ArrayList<AnnotationDiffer> differs =
              new ArrayList<AnnotationDiffer>();
            for (HashMap<String, AnnotationDiffer> differsByType :
                differsByDocThenType) {
              differs.addAll(differsByType.values());
            }
            AnnotationDiffer differ = new AnnotationDiffer(differs);
            return f.format(getMeasureValue(columnIndex, differ, COLUMN_COUNT));
        }
      } else {
        return "";
      }
    }

    public Class<?> getColumnClass(int columnIndex) {
      return String.class;
    }

    public String getColumnName(int column) {
      return columnNames.get(column);
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
      return false;
    }

    private NumberFormat f = NumberFormat.getInstance(Locale.ENGLISH);

    private final String[] COLUMN_NAMES = {
      "Document", "Match", "Only A", "Only B", "Overlap"};
    private final String[] HEADER_TOOLTIPS = {null,
      "aka 'Correct'", "aka 'Missing'", "aka 'Spurious'", "aka 'Partial'"};
    private ArrayList<String> columnNames =
      new ArrayList<String>(Arrays.asList(COLUMN_NAMES));
    public ArrayList<String> headerTooltips =
      new ArrayList<String>(Arrays.asList(HEADER_TOOLTIPS));

    public static final int COL_DOCUMENT = 0;
    public static final int COL_CORRECT = 1;
    public static final int COL_MISSING = 2;
    public static final int COL_SPURIOUS = 3;
    public static final int COL_PARTIAL = 4;
    public static final int COLUMN_COUNT = 5;
    public static final int COL_RECALL = 0;
    public static final int COL_PRECISION = 1;
    public static final int COL_FMEASURE = 2;
  }

  /**
   * Update document table.
   */
  protected class CompareAction extends AbstractAction{
    public CompareAction(){
      super("Compare");
      putValue(SHORT_DESCRIPTION,
        "Compare annotations between sets A and B");
      putValue(MNEMONIC_KEY, KeyEvent.VK_ENTER);
      putValue(SMALL_ICON, MainFrame.getIcon("crystal-clear-action-run"));
    }
    public void actionPerformed(ActionEvent evt){
      CorpusQualityAssurance.this.setCursor(
        Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      setEnabled(false);

      Runnable runnable = new Runnable() { public void run() {
      // update data
      compareAnnotation();
      documentTableModel.updateColumns();
      annotationTableModel.updateColumns();

      SwingUtilities.invokeLater(new Runnable(){ public void run(){
        // redraw document table
        documentTable.setSortable(false);
        documentTableModel.fireTableStructureChanged();
        for (int col = 0; col < documentTable.getColumnCount(); col++) {
          documentTable.setComparator(col, doubleComparator);
        }
        documentTable.setComparator(
          DocumentTableModel.COL_DOCUMENT, totalComparator);
        documentTable.setSortedColumn(DocumentTableModel.COL_DOCUMENT);
        documentTable.setSortable(true);
        // redraw annotation table
        annotationTable.setSortable(false);
        annotationTableModel.fireTableStructureChanged();
        for (int col = 0; col < annotationTable.getColumnCount(); col++) {
          annotationTable.setComparator(col, doubleComparator);
        }
        annotationTable.setComparator(
          AnnotationTableModel.COL_ANNOTATION, totalComparator);
        annotationTable.setSortedColumn(AnnotationTableModel.COL_ANNOTATION);
        annotationTable.setSortable(true);
        CorpusQualityAssurance.this.setCursor(
          Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        setEnabled(true);
      }});
      }};
      Thread thread = new Thread(runnable,  "CompareAction");
      thread.setPriority(Thread.MIN_PRIORITY);
      thread.start();
    }
  }

  class OpenDocumentAction extends AbstractAction{
    public OpenDocumentAction(){
      super("Open documents", MainFrame.getIcon("document"));
      putValue(SHORT_DESCRIPTION,
        "Opens document for the selected row in a document editor");
      putValue(MNEMONIC_KEY, KeyEvent.VK_UP);
    }
    public void actionPerformed(ActionEvent e){
      Document doc = (Document) corpus.get(
        documentTable.rowViewToModel(documentTable.getSelectedRow()));
      MainFrame.getInstance().select(doc);
    }
  }

  class OpenAnnotationDiffAction extends AbstractAction{
    public OpenAnnotationDiffAction(){
      super("Open annotation diff", MainFrame.getIcon("annDiff"));
      putValue(SHORT_DESCRIPTION,
        "Opens annotation diff for the selected row in the document table");
      putValue(MNEMONIC_KEY, KeyEvent.VK_RIGHT);
    }
    public void actionPerformed(ActionEvent e){
      String documentName = (String) documentTable.getValueAt(
        documentTable.getSelectedRow(), DocumentTableModel.COL_DOCUMENT);
      String annotationType = (String) typeList.getSelectedValue();
      Set<String> featureSet = new HashSet<String>();
      for (Object feature : featureList.getSelectedValues()) {
        featureSet.add((String) feature);
      }
      AnnotationDiffGUI frame = new AnnotationDiffGUI("Annotation Difference",
        documentName, documentName, keySetName,
        responseSetName, annotationType, featureSet);
      frame.pack();
      frame.setLocationRelativeTo(MainFrame.getInstance());
      frame.setVisible(true);
    }
  }

  protected class ExportToHtmlAction extends AbstractAction{
    public ExportToHtmlAction(){
      super("Export to HTML");
      putValue(SHORT_DESCRIPTION, "Export the tables to HTML");
      putValue(SMALL_ICON,
        MainFrame.getIcon("crystal-clear-app-download-manager"));
    }
    public void actionPerformed(ActionEvent evt){
      XJFileChooser fileChooser = MainFrame.getFileChooser();
      fileChooser.setAcceptAllFileFilterUsed(true);
      fileChooser.setDialogTitle("Choose a file where to export the tables");
      fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      ExtensionFileFilter filter = new ExtensionFileFilter("HTML files","html");
      fileChooser.addChoosableFileFilter(filter);
      String title = corpus.getName();
        title += "_" + keySetName;
        title += "-" + responseSetName;
      fileChooser.setFileName(title + ".html");
      fileChooser.setResource(CorpusQualityAssurance.class.getName());
      int res = fileChooser.showSaveDialog(CorpusQualityAssurance.this);
      if (res != JFileChooser.APPROVE_OPTION) { return; }

      File saveFile = fileChooser.getSelectedFile();
      Writer fw = null;
      try{
        fw = new BufferedWriter(new FileWriter(saveFile));

        // Header, Title
        fw.write(BEGINHTML + nl);
        fw.write(BEGINHEAD);
        fw.write(title);
        fw.write(ENDHEAD + nl);
        fw.write("<H1>Corpus Quality Assurance</H1>" + nl);
        fw.write("<P>Corpus: " + corpus.getName() + "<BR>" + nl);
        fw.write("Key set: " + keySetName + "<BR>" + nl);
        fw.write("Response set: " + responseSetName + "</P>" + nl);
        fw.write("<P>&nbsp;</P>" + nl);

        // annotation table
        fw.write(BEGINTABLE + nl + "<TR>" + nl);
        for(int col = 0; col < annotationTable.getColumnCount(); col++){
          fw.write("<TH align=\"left\">"
            + annotationTable.getColumnName(col) + "</TH>" + nl);
        }
        fw.write("</TR>" + nl);
        for(int row = 0; row < annotationTableModel.getRowCount(); row ++){
          fw.write("<TR>" + nl);
          for(int col = 0; col < annotationTable.getColumnCount(); col++){
            fw.write("<TD>"
              + annotationTable.getValueAt(row, col) + "</TD>" + nl);
          }
          fw.write("</TR>" + nl);
        }
        fw.write(ENDTABLE + nl);

        fw.write("<P>&nbsp;</P>" + nl);

        // document table
        fw.write(BEGINTABLE + nl + "<TR>" + nl);
        for(int col = 0; col < documentTable.getColumnCount(); col++){
          fw.write("<TH align=\"left\">"
            + documentTable.getColumnName(col) + "</TH>" + nl);
        }
        fw.write("</TR>" + nl);
        for(int row = 0; row < documentTable.getRowCount(); row ++){
          fw.write("<TR>" + nl);
          for(int col = 0; col < documentTable.getColumnCount(); col++){
            fw.write("<TD>"
              + documentTable.getValueAt(row, col) + "</TD>" + nl);
          }
          fw.write("</TR>" + nl);
        }
        fw.write(ENDTABLE + nl);

        fw.write(ENDHTML + nl);
        fw.flush();

      } catch(IOException ioe){
        JOptionPane.showMessageDialog(CorpusQualityAssurance.this,
          ioe.toString(), "GATE", JOptionPane.ERROR_MESSAGE);
        ioe.printStackTrace();

      } finally {
        if (fw != null) {
          try {
            fw.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    }

    final String nl = Strings.getNl();
    static final String BEGINHTML =
      "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">" +
      "<html>";
    static final String ENDHTML = "</body></html>";
    static final String BEGINHEAD = "<head>" +
      "<meta content=\"text/html; charset=utf-8\" http-equiv=\"content-type\">"
      + "<title>";
    static final String ENDHEAD = "</title></head><body>";
    static final String BEGINTABLE = "<table cellpadding=\"0\" border=\"1\">";
    static final String ENDTABLE = "</table>";
  }

  class ReloadCacheAction extends AbstractAction{
    public ReloadCacheAction(){
      super("Reload cache", MainFrame.getIcon("crystal-clear-action-reload"));
      putValue(SHORT_DESCRIPTION,
        "Reload cache for set, type and feature names list");
    }
    public void actionPerformed(ActionEvent e){
      docsSetsTypesFeatures.clear();
      updateSets();
    }
  }

  protected XJTable documentTable;
  protected DocumentTableModel documentTableModel;
  protected XJTable annotationTable;
  protected AnnotationTableModel annotationTableModel;
  protected Corpus corpus;
  protected TreeSet<String> types;
  /** cache for document*set*type*feature names */
  protected LinkedHashMap<String, TreeMap<String, TreeMap<String,
    TreeSet<String>>>> docsSetsTypesFeatures;
  /** ordered by document as in the <code>corpus</code>
   *  then contains (annotation type * AnnotationDiffer) */
  private ArrayList<HashMap<String, AnnotationDiffer>> differsByDocThenType =
    new ArrayList<HashMap<String, AnnotationDiffer>>();
  private ArrayList<String> documentNames = new ArrayList<String>();
  protected String keySetName;
  protected String responseSetName;
  protected JList setList;
  protected JList typeList;
  protected JList featureList;
  protected JList measureList;
  protected JCheckBox setCheck;
  protected JCheckBox typeCheck;
  protected JCheckBox featureCheck;
  protected Collator collator;
  protected Comparator<String> doubleComparator;
  protected Comparator<String> totalComparator;
  protected boolean corpusChanged;
  protected OpenDocumentAction openDocumentAction;
  protected OpenAnnotationDiffAction openAnnotationDiffAction;
  protected CompareAction compareAction;
  protected ReloadCacheAction reloadCacheAction;
  protected JProgressBar progressBar;
  protected Timer timer = new Timer("CorpusQualityAssurance", true);
  protected TimerTask timerTask;
}