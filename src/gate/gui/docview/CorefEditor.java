/*
 *  Copyright (c) 1998-2004, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  CorefEditor.java
 *
 *  Niraj Aswani, 24-Jun-2004
 *
 *  $Id$
 */

package gate.gui.docview;

import java.awt.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.util.*;
import gate.*;
import gate.creole.*;
import java.io.*;
import java.awt.event.*;
import gate.swing.*;
import gate.event.*;
import javax.swing.text.Highlighter;
import javax.swing.text.DefaultHighlighter;

public class CorefEditor extends AbstractDocumentView 
    implements ActionListener, FeatureMapListener, gate.event.DocumentListener {

  // default AnnotationSet Name
  private final static String DEFAULT_ANNOTSET_NAME = "Default";

  private JPanel mainPanel, topPanel, subPanel;
  private JToggleButton showAnnotations;
  private JComboBox annotSets, annotTypes;
  private DefaultComboBoxModel annotSetsModel, annotTypesModel;

  // Co-reference Tree
  private JTree corefTree;

  // Root node
  private CorefTreeNode rootNode;

  // top level hashMap (corefChains)
  // AnnotationSet(CorefTreeNode) --> (CorefTreeNode type ChainNode --> ArrayList AnnotationIds)
  private HashMap corefChains;

  // This is used to store the annotationSet name and its respective corefTreeNode
  // annotationSetName --> CorefTreeNode of type (AnnotationSet)
  private HashMap corefAnnotationSetNodesMap;

  // annotationSetName --> (chainNodeString --> Boolean)
  private HashMap selectionChainsMap;

  // chainString --> Boolean
  private HashMap currentSelections;

  // annotationSetName --> (chainNodeString --> Color)
  private HashMap colorChainsMap;

  // chainNodeString --> Color
  private HashMap currentColors;

  private ColorGenerator colorGenerator;
  private TextualDocumentView textView;
  private JEditorPane textPane;

  /* ChainNode --> (HighlightedTags) */
  private HashMap highlightedTags;

  /* This arraylist stores the highlighted tags for the specific selected annotation type */
  private ArrayList typeSpecificHighlightedTags;
  private TextPaneMouseListener textPaneMouseListener;

  /* This stores Ids of the highlighted Chain Annotations*/
  private ArrayList highlightedChainAnnots = new ArrayList();
  /* This stores start and end offsets of the highlightedChainAnnotations */
  private int [] highlightedChainAnnotsOffsets;

  /* This stores Ids of the highlighted Annotations of particular type */
  private ArrayList highlightedTypeAnnots = new ArrayList();
  /* This stores start and end offsets of highlightedTypeAnnots */
  private int [] highlightedTypeAnnotsOffsets;

  /* Timer for the Chain Tool tip action */
  private ChainToolTipAction chainToolTipAction;
  private javax.swing.Timer chainToolTipTimer;

  private NewCorefAction newCorefAction;
  private javax.swing.Timer newCorefActionTimer;

  private Annotation annotToConsiderForChain = null;
  private JWindow popupWindow;
  private boolean explicitCall = false;
  private Highlighter highlighter;

  /**
   * This method intiates the GUI for co-reference editor
   */
  protected void initGUI(){

    //get a pointer to the textual view used for highlights
    Iterator centralViewsIter = owner.getCentralViews().iterator();
    while(textView == null && centralViewsIter.hasNext()){
      DocumentView aView = (DocumentView)centralViewsIter.next();
      if(aView instanceof TextualDocumentView)
        textView = (TextualDocumentView)aView;
    }
    textPane = (JEditorPane)((JScrollPane)textView.getGUI()).getViewport().getView();
    highlighter = textPane.getHighlighter();
    chainToolTipAction = new ChainToolTipAction();
    chainToolTipTimer = new javax.swing.Timer(500, chainToolTipAction);

    newCorefAction = new NewCorefAction();
    newCorefActionTimer = new javax.swing.Timer(500, newCorefAction);


    colorGenerator = new ColorGenerator();

    // main Panel
    mainPanel = new JPanel();
    mainPanel.setLayout(new BorderLayout());

    // topPanel
    topPanel = new JPanel();
    topPanel.setLayout(new BorderLayout());

    // subPanel
    subPanel = new JPanel();
    subPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

    // showAnnotations Button
    showAnnotations = new JToggleButton("show");
    showAnnotations.addActionListener(this);

    // annotSets
    annotSets = new JComboBox();
    annotSets.addActionListener(this);

    // get all the annotationSets
    Map annotSetsMap = document.getNamedAnnotationSets();
    annotSetsModel = new DefaultComboBoxModel();
    if(annotSetsMap != null) {
      annotSetsModel = new DefaultComboBoxModel(annotSetsMap.keySet().toArray());
    }
    annotSetsModel.insertElementAt(DEFAULT_ANNOTSET_NAME, 0);
    annotSets.setModel(annotSetsModel);

    // annotTypes
    annotTypesModel = new DefaultComboBoxModel();
    annotTypes = new JComboBox(annotTypesModel);
    annotTypes.addActionListener(this);
    subPanel.add(annotSets);
    subPanel.add(annotTypes);

    // intialises the Data
    initData();

    // and creating the tree
    corefTree = new JTree(rootNode);
    corefTree.putClientProperty("JTree.lineStyle", "None");
    corefTree.setRowHeight(corefTree.getRowHeight() * 2);
    corefTree.setLargeModel(true);
    corefTree.setAutoscrolls(true);

    //corefTree.setRootVisible(false);
    //corefTree.setShowsRootHandles(false);
    corefTree.addMouseListener(new CorefTreeMouseListener());
    corefTree.setCellRenderer(new CorefTreeCellRenderer());

    mainPanel.add(topPanel, BorderLayout.NORTH);
    mainPanel.add(new JScrollPane(corefTree), BorderLayout.CENTER);
    JPanel tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    tempPanel.add(showAnnotations);
    topPanel.add(tempPanel, BorderLayout.SOUTH);
    topPanel.add(subPanel, BorderLayout.CENTER);


    // get the highlighter
    textPaneMouseListener = new TextPaneMouseListener();

    if(annotSets.getModel().getSize() > 0) {
      annotSets.setSelectedIndex(0);
    }

    // finally show the tree
    //annotSetSelectionChanged();

    document.addDocumentListener(this);
    document.getFeatures().addFeatureMapListener(this);

  }




  /** This methods cleans up the memory by removing all listener registrations */
  public void cleanup() {
    document.removeDocumentListener(this);
    document.getFeatures().removeFeatureMapListener(this);
  }




  /** Given arrayList containing Ids of the annotations, and an annotationSet, this method
   * returns the annotations that has longest string among the matches
   */
  public Annotation findOutTheLongestAnnotation(ArrayList matches, AnnotationSet set) {
    if(matches == null || matches.size() == 0) {
      return null;
    }
    int length = 0;
    int index = 0;
    for (int i = 0; i < matches.size(); i++) {
      Annotation currAnn = (Annotation) set.get( (Integer) matches.get(i));
      int start = currAnn.getStartNode().getOffset().intValue();
      int end = currAnn.getEndNode().getOffset().intValue();
      if ( (end - start) > length) {
        length = end - start;
        index = i;
      }
    }
    // so now we now have the longest String annotations at index
    return (Annotation) set.get( (Integer) matches.get(index));
  }



  /**
   * This method is called when any annotationSet is removed outside the
   * co-reference editor..
   * @param de
   */
  public void annotationSetRemoved(gate.event.DocumentEvent de) {
    // this method removes the annotationSet from the annotSets
    // and all chainNodes under it

    String annotSet = de.getAnnotationSetName();
    annotSet = (annotSet == null) ? DEFAULT_ANNOTSET_NAME : annotSet;
    // find out the currently Selected annotationSetName
    String annotSetName = (String) annotSets.getSelectedItem();
    // remove it from the main data store
    corefChains.remove(corefAnnotationSetNodesMap.get(annotSet));
    // remove it from the main data store
    corefAnnotationSetNodesMap.remove(annotSet);
    // remove it from the annotationSetModel (combobox)
    annotSetsModel.removeElement(annotSet);
    annotSets.setModel(annotSetsModel);
    // remove it from the colorChainMap
    colorChainsMap.remove(annotSet);
    // remove it from the selectionChainMap
    selectionChainsMap.remove(annotSet);
    if (annotSetsModel.getSize() == 0) {
      // no annotationSet to display
      // so set visible false
      if(popupWindow != null && popupWindow.isVisible()) {
        popupWindow.setVisible(false);
      }
      corefTree.setVisible(false);
    }
    else {
      if (annotSetName.equals(annotSet)) {
        if(popupWindow != null && popupWindow.isVisible()) {
          popupWindow.setVisible(false);
        }
        corefTree.setVisible(true);
        annotSets.setSelectedIndex(0);
        //annotSetSelectionChanged();
      }
    }
  }




  /**
   * This method is called when any new annotationSet is added
   * @param de
   */
  public void annotationSetAdded(gate.event.DocumentEvent de) {
    String annotSet = de.getAnnotationSetName();
    annotSet = (annotSet == null) ? DEFAULT_ANNOTSET_NAME : annotSet;
    // find out the currently Selected annotationSetName
    String annotSetName = (String) annotSets.getSelectedItem();

    // check if newly added annotationSet is the default AnnotationSet
    CorefTreeNode annotSetNode = null;

    if (annotSet.equals(DEFAULT_ANNOTSET_NAME))
      annotSetNode = createChain(document.getAnnotations(), true);
    else
      annotSetNode = createChain(document.getAnnotations(annotSet), false);

    corefAnnotationSetNodesMap.put(annotSet, annotSetNode);
    annotSetsModel.addElement(annotSet);
    annotSets.setModel(annotSetsModel);

    if (annotSetName != null)
      annotSets.setSelectedItem(annotSetName);
    else
      annotSets.setSelectedIndex(0);

    //annotSetSelectionChanged();
  }

  /**Called when the content of the document has changed through an edit 
   * operation.
   */
  public void contentEdited(gate.event.DocumentEvent e){
    //ignore
  }




  /**
   * Called when features are changed outside the co-refEditor
   */
  public void featureMapUpdated() {
    if(explicitCall)
      return;

    // we would first save the current settings
    // 1. Current AnnotSet
    // 2. Current AnnotType
    // 3. ShowAnnotation Status
    String currentAnnotSet = (String) annotSets.getSelectedItem();
    String currentAnnotType = (String) annotTypes.getSelectedItem();
    boolean currentShowAnnotationStatus = showAnnotations.isSelected();

    // there is some change in the featureMap
    Map matchesMap = null;
    matchesMap = (Map) document.getFeatures().get(ANNIEConstants.
                                                  DOCUMENT_COREF_FEATURE_NAME);
    if(matchesMap == null) return;

    //AnnotationSetName --> List of ArrayLists
    //each ArrayList contains Ids of related annotations
    Iterator setIter = matchesMap.keySet().iterator();
    HashMap annotSetsNamesMap = new HashMap();
    for(int i=0;i<annotSets.getItemCount();i++) {
      annotSetsNamesMap.put((String) annotSets.getItemAt(i), new Boolean(false));
    }

    outer:while (setIter.hasNext()) {
      String currentSet = (String) setIter.next();
      java.util.List matches = (java.util.List) matchesMap.get(currentSet);
      currentSet = (currentSet == null) ? DEFAULT_ANNOTSET_NAME : currentSet;
      AnnotationSet currAnnotSet = getAnnotationSet(currentSet);
      annotSetsNamesMap.put(currentSet, new Boolean(true));

      if(matches == null) return;
      Iterator entitiesIter = matches.iterator();
      //each entity is a list of annotation IDs

      HashMap chains = (HashMap) corefChains.get(corefAnnotationSetNodesMap.get(
          currentSet));
      HashMap visitedList = new HashMap();
      if (chains != null) {
        Iterator chainsList = chains.keySet().iterator();

        // intially no chainHead is visited
        while (chainsList.hasNext()) {
          visitedList.put( (CorefTreeNode) chainsList.next(), new Boolean(false));
        }

        // now we need to search for chainHead of each group
        while (entitiesIter.hasNext()) {
          ArrayList ids = (ArrayList) entitiesIter.next();
          if(ids == null || ids.size() == 0) {
            explicitCall = true;
            matches.remove(ids);
            String set = currentSet.equals(DEFAULT_ANNOTSET_NAME) ? null : currentSet;
            matchesMap.put(set, matches);
            explicitCall = false;
            break;
          }

          CorefTreeNode chainHead = null;
          for (int i = 0; i < ids.size(); i++) {
            Integer id = (Integer) ids.get(i);
            // now lets find out the headnode for this, if it is available
            chainHead = findOutTheChainHead(currAnnotSet.get(id));
            if (chainHead != null) {
              visitedList.put(chainHead, new Boolean(true));
              break;
            }
          }

          if (chainHead != null) {
            // we found the chainHead for this
            // so we would replace the ids
            // but before that we would check if chainHead should be replaced
            Annotation longestAnn = findOutTheLongestAnnotation(ids, getAnnotationSet(currentSet));
            if(getString(longestAnn).equals(chainHead.toString())) {
              // no action needed
            } else {
              // we first check if new longestAnnotation String is already available as some other chain Node head
              if(currentColors.containsKey(getString(longestAnn))) {
                // yes one chainHead with this string already exists
                // so we need to merge them together
                String longestString = getString(longestAnn);
                CorefTreeNode tempChainHead = findOutChainNode(longestString);
                // now all the ids under current chainHead should be placed under the tempChainHead
                ArrayList tempIds = (ArrayList) chains.get(tempChainHead);
                ArrayList currentChainHeadIds = (ArrayList) chains.get(chainHead);
                // so lets merge them
                tempIds.addAll(currentChainHeadIds);

                // and update the chains
                chains.put(tempChainHead, tempIds);
                chains.remove(chainHead);
                corefChains.put(corefAnnotationSetNodesMap.get(currentSet), chains);
                visitedList.put(chainHead, new Boolean(false));

              } else {
                String previousString = chainHead.toString();
                String newString = getString(longestAnn);
                chainHead.setUserObject(newString);

                // we need to change the colors
                Color color = (Color) currentColors.get(previousString);
                currentColors.remove(previousString);
                currentColors.put(newString, color);
                colorChainsMap.put(newString, currentColors);

                // we need to change the selections
                Boolean val = (Boolean) currentSelections.get(previousString);
                currentSelections.remove(previousString);
                currentSelections.put(newString, val);
                selectionChainsMap.put(newString, currentSelections);

                chains.put(chainHead, ids);
                corefChains.put(corefAnnotationSetNodesMap.get(currentSet), chains);
              }
            }
            //chains.put(chainHead, ids);
            //corefChains.put(corefAnnotationSetNodesMap.get(currentSet), chains);
          }
          else {
            // this is something new addition
            // so we need to create a new chainNode
            // this is the new chain
            // get the current annotSetNode
            CorefTreeNode annotSetNode = (CorefTreeNode)
                                         corefAnnotationSetNodesMap.get(currentSet);

            // we need to find out the longest string annotation
            Annotation ann = findOutTheLongestAnnotation(ids, currAnnotSet);
            // create the new chainNode
            CorefTreeNode chainNode = new CorefTreeNode(getString(ann), false, CorefTreeNode.CHAIN_NODE);
            // add this to tree
            annotSetNode.add(chainNode);
            corefAnnotationSetNodesMap.put(currentSet, annotSetNode);

            // ArrayList matches
            HashMap newHashMap = (HashMap) corefChains.get(annotSetNode);
            newHashMap.put(chainNode, ids);
            corefChains.put(annotSetNode, newHashMap);

            // entry into the selection
            HashMap tempSelection = (HashMap) selectionChainsMap.get(currentSet);
            tempSelection.put(chainNode.toString(), new Boolean(true));
            selectionChainsMap.put(currentSet, tempSelection);

            // entry into the colors
            float components[] = colorGenerator.getNextColor().getComponents(null);
            Color color = new Color(components[0],
                                    components[1],
                                    components[2],
                                    0.5f);
            HashMap tempColors = (HashMap) colorChainsMap.get(currentSet);
            tempColors.put(chainNode.toString(), color);
            colorChainsMap.put(annotSets.getSelectedItem(), tempColors);
          }
        }

        // here we need to find out the chainNodes those are no longer needed
        Iterator visitedListIter = visitedList.keySet().iterator();
        while (visitedListIter.hasNext()) {
          CorefTreeNode chainNode = (CorefTreeNode) visitedListIter.next();
          if (! ( (Boolean) visitedList.get(chainNode)).booleanValue()) {
            // yes this should be deleted
            CorefTreeNode annotSetNode = (CorefTreeNode)
                                         corefAnnotationSetNodesMap.get(currentSet);

            // remove from the tree
            annotSetNode.remove(chainNode);
            corefAnnotationSetNodesMap.put(currentSet, annotSetNode);

            // ArrayList matches
            HashMap newHashMap = (HashMap) corefChains.get(annotSetNode);
            newHashMap.remove(chainNode);
            corefChains.put(annotSetNode, newHashMap);

            // remove from the selections
            HashMap tempSelection = (HashMap) selectionChainsMap.get(currentSet);
            tempSelection.remove(chainNode.toString());
            selectionChainsMap.put(currentSet, tempSelection);

            // remove from the colors
            HashMap tempColors = (HashMap) colorChainsMap.get(currentSet);
            tempColors.remove(chainNode.toString());
            colorChainsMap.put(currentSet, currentColors);
          }
        }
      }
    }

    Iterator tempIter = annotSetsNamesMap.keySet().iterator();
    while(tempIter.hasNext()) {
      String currentSet = (String) tempIter.next();
      if(!((Boolean) annotSetsNamesMap.get(currentSet)).booleanValue()) {
          String annotSet = currentSet;
          // find out the currently Selected annotationSetName
          String annotSetName = (String) annotSets.getSelectedItem();
          // remove it from the main data store
          corefChains.remove(corefAnnotationSetNodesMap.get(annotSet));
          // remove it from the main data store
          corefAnnotationSetNodesMap.remove(annotSet);
          // remove it from the annotationSetModel (combobox)
          annotSetsModel.removeElement(annotSet);
          annotSets.setModel(annotSetsModel);
          annotSets.updateUI();
          // remove it from the colorChainMap
          colorChainsMap.remove(annotSet);
          // remove it from the selectionChainMap
          selectionChainsMap.remove(annotSet);
      }
    }


    if (annotSetsModel.getSize() == 0) {
      // no annotationSet to display
      // so set visible false
      if(popupWindow != null && popupWindow.isVisible()) {
        popupWindow.setVisible(false);
      }
      corefTree.setVisible(false);
      highlighter.removeAllHighlights();
      /*if(highlightedTags != null) {
        Set tags = highlightedTags.keySet();
        if (tags != null) {
          Iterator iter = tags.iterator();
          while (iter.hasNext()) {
            ArrayList tags1 = (ArrayList) highlightedTags.get(iter.next());
            textView.removeHighlights(tags1);
          }
        }
      }
      if(typeSpecificHighlightedTags != null) {
        textView.removeHighlights(typeSpecificHighlightedTags);
      }*/
    }
    else {
      if(popupWindow != null && popupWindow.isVisible()) {
        popupWindow.setVisible(false);
      }
      corefTree.setVisible(true);
      explicitCall = true;
      annotSets.setSelectedItem(currentAnnotSet);
      annotSetSelectionChanged();
      annotTypes.setSelectedItem(currentAnnotType);
      showAnnotations.setSelected(currentShowAnnotationStatus);
      highlighter.removeAllHighlights();
      /*if(highlightedTags != null) {
        Set tags = highlightedTags.keySet();
        if (tags != null) {
          Iterator iter = tags.iterator();
          while (iter.hasNext()) {
            ArrayList tags1 = (ArrayList) highlightedTags.get(iter.next());
            textView.removeHighlights(tags1);
          }
        }
      }
      if(typeSpecificHighlightedTags != null) {
        textView.removeHighlights(typeSpecificHighlightedTags);
      }*/
      highlightedTags = null;
      highlightAnnotations();
      typeSpecificHighlightedTags = null;
      showTypeWiseAnnotations();
      explicitCall = false;
    }


  }


  /**
   * ActionPerformed Activity
   * @param ae
   */
  public void actionPerformed(ActionEvent ae) {
    // when annotationSet value changes
    if(ae.getSource() == annotSets) {
      if(!explicitCall) {
        annotSetSelectionChanged();
      }
    } else if(ae.getSource() == showAnnotations) {
      if(!explicitCall) {
        showTypeWiseAnnotations();
      }
    } else if(ae.getSource() == annotTypes) {
      if(!explicitCall) {
        if(typeSpecificHighlightedTags != null) {
          for(int i=0;i<typeSpecificHighlightedTags.size();i++) {
            highlighter.removeHighlight(typeSpecificHighlightedTags.get(i));
          }
        }
        typeSpecificHighlightedTags = null;
        showTypeWiseAnnotations();
      }
    }
  }


  /**
   * When user preses the show Toggle button, this will show up annotations
   * of selected Type from selected AnnotationSet
   */
  private void showTypeWiseAnnotations() {
    if(typeSpecificHighlightedTags == null) {
      typeSpecificHighlightedTags = new ArrayList();
      highlightedTypeAnnots = new ArrayList();
      typeSpecificHighlightedTags = new ArrayList();
    }

    if(showAnnotations.isSelected()) {
      // get the annotationsSet and its type
      AnnotationSet set = getAnnotationSet((String) annotSets.getSelectedItem());
      String type = (String) annotTypes.getSelectedItem();
      if(type == null) {
        try {
          JOptionPane.showMessageDialog(Main.getMainFrame(),
                                        "No annotation type found to display");
        }catch(Exception e) {
          e.printStackTrace();
        }
        showAnnotations.setSelected(false);
        return;
      }


      Color color = getColor(type);
      if(type != null) {
        AnnotationSet typeSet = set.get(type);
        Iterator iter = typeSet.iterator();
        while (iter.hasNext()) {
          Annotation ann = (Annotation) iter.next();
          highlightedTypeAnnots.add(ann);
          try {
            typeSpecificHighlightedTags.add(highlighter.addHighlight(ann.getStartNode().
                getOffset().intValue(),
                            ann.getEndNode().getOffset().intValue(),
                                         new DefaultHighlighter.
                                         DefaultHighlightPainter(color)));
          } catch(Exception e) {
            e.printStackTrace();
          }
          //typeSpecificHighlightedTags.add(textView.addHighlight(ann, getAnnotationSet((String)annotSets.getSelectedItem()),color));
        }
      }
    } else {
      for(int i=0;i<typeSpecificHighlightedTags.size();i++) {
        //textView.removeHighlight(typeSpecificHighlightedTags.get(i));
        highlighter.removeHighlight(typeSpecificHighlightedTags.get(i));
      }
      typeSpecificHighlightedTags = new ArrayList();
      highlightedTypeAnnots = new ArrayList();
      highlightedTypeAnnotsOffsets = null;
    }

    // This is to make process faster.. instead of accessing each annotation and
    // its offset, we create an array with its annotation offsets to search faster
    Collections.sort(highlightedTypeAnnots,new gate.util.OffsetComparator());
    highlightedTypeAnnotsOffsets = new int[highlightedTypeAnnots.size() * 2];
    for(int i=0,j=0;j<highlightedTypeAnnots.size();i+=2,j++) {
      Annotation ann1 = (Annotation) highlightedTypeAnnots.get(j);
      highlightedTypeAnnotsOffsets[i] = ann1.getStartNode().getOffset().intValue();
      highlightedTypeAnnotsOffsets[i+1] = ann1.getEndNode().getOffset().intValue();
    }

  }


  /**
   * Returns annotation Set
   * @param annotSet
   * @return
   */
  private AnnotationSet getAnnotationSet(String annotSet) {
    return (annotSet.equals(DEFAULT_ANNOTSET_NAME)) ? document.getAnnotations() :
        document.getAnnotations(annotSet);
  }



  /**
   * When annotationSet selection changes
   */
  private void annotSetSelectionChanged() {
    if(annotSets.getModel().getSize() == 0) {
      if(popupWindow != null && popupWindow.isVisible()) {
        popupWindow.setVisible(false);
      }
      corefTree.setVisible(false);
      return;
    }

    String currentAnnotSet = (String) annotSets.getSelectedItem();
    // get all the types of the currently Selected AnnotationSet
    if(currentAnnotSet == null) currentAnnotSet = (String) annotSets.getItemAt(0);
    AnnotationSet temp = getAnnotationSet(currentAnnotSet);
    Set types = temp.getAllTypes();
    annotTypesModel = new DefaultComboBoxModel();
    if(types != null) {
      annotTypesModel = new DefaultComboBoxModel(types.toArray());
    }
    annotTypes.setModel(annotTypesModel);
    annotTypes.updateUI();

    // and redraw the CorefTree
    rootNode.removeAllChildren();
    rootNode.add( (CorefTreeNode) corefAnnotationSetNodesMap.get(
        currentAnnotSet));
    currentSelections = (HashMap) selectionChainsMap.get(currentAnnotSet);
    currentColors = (HashMap) colorChainsMap.get(currentAnnotSet);
    if(!corefTree.isVisible()) {
      if(popupWindow != null && popupWindow.isVisible()) {
        popupWindow.setVisible(false);
      }
      corefTree.setVisible(true);
    }
    corefTree.repaint();
    corefTree.updateUI();
  }



  /**
   * This will initialise the data
   */
  private void initData() {

    //************************************************************************
    // Internal Data structure
    // top level hashMap (corefChains)
    // AnnotationSet(CorefTreeNode) --> (CorefTreeNode type ChainNode --> ArrayList AnnotationIds)
    //
    // another toplevel hashMap (corefAnnotationSetsNodesMap)
    // annotationSetName --> annotationSetNode(CorefChainTreeNode)
    //************************************************************************

    rootNode = new CorefTreeNode("Coreference Data", true, CorefTreeNode.ROOT_NODE);
    corefChains = new HashMap();
    selectionChainsMap = new HashMap();
    currentSelections = new HashMap();
    colorChainsMap = new HashMap();
    currentColors = new HashMap();
    corefAnnotationSetNodesMap = new HashMap();
    // now we need to findout the chains
    // for the defaultAnnotationSet
    corefAnnotationSetNodesMap.put(DEFAULT_ANNOTSET_NAME,
                                    createChain(document.getAnnotations(), true));

    // and for the rest AnnotationSets
    Map annotSets = document.getNamedAnnotationSets();
    if(annotSets != null) {
      Iterator annotSetsIter = annotSets.keySet().iterator();
      while (annotSetsIter.hasNext()) {
        String annotSetName = (String) annotSetsIter.next();
        corefAnnotationSetNodesMap.put(annotSetName, createChain(document.getAnnotations(annotSetName) , false));
      }
    }
  }



  /**
   * Creates the internal data structure
   * @param set
   */
  private CorefTreeNode createChain(AnnotationSet set, boolean isDefaultSet) {

    //************************************************************************
    // Internal Data structure
    // top level hashMap (corefChains)
    // AnnotationSet(CorefTreeNode) --> (CorefTreeNode type ChainNode --> ArrayList AnnotationIds)
    //
    // another toplevel hashMap (corefAnnotationSetsNodesMap)
    // annotationSetName --> annotationSetNode(CorefChainTreeNode)
    //************************************************************************


    // create the node for setName
    String setName = isDefaultSet ? DEFAULT_ANNOTSET_NAME : set.getName();
    CorefTreeNode annotSetNode = new CorefTreeNode(setName, true, CorefTreeNode.ANNOTSET_NODE);

    // create the map for all the annotations with matches feature in it
    ArrayList annotations = new ArrayList();
    Iterator iter = set.iterator();
    while(iter.hasNext()) {
      Annotation ann = (Annotation) iter.next();
      if(ann.getFeatures().get(ANNIEConstants.ANNOTATION_COREF_FEATURE_NAME) != null)
        annotations.add(ann);
    }

    // and now create the internal datastructure
    HashMap chainLinks = new HashMap();
    HashMap selectionMap = new HashMap();
    HashMap colorMap = new HashMap();

    // and take one group at a time, find out the longest string and create the chain
    ArrayList tempAnnotations = new ArrayList();
    Iterator tempIter = annotations.iterator();
    while(tempIter.hasNext()) {
      Annotation ann = (Annotation) tempIter.next();
      if(tempAnnotations.contains(ann)) {
        continue;
      }
      ArrayList matches = (ArrayList) ann.getFeatures().get(ANNIEConstants.ANNOTATION_COREF_FEATURE_NAME);
      int length = 0;
      int index = 0;
      if(matches == null) matches = new ArrayList();
      matches.add(ann.getId());
      for(int i=0;i<matches.size();i++) {
        Annotation currAnn = (Annotation) set.get((Integer) matches.get(i));
        int start = currAnn.getStartNode().getOffset().intValue();
        int end = currAnn.getEndNode().getOffset().intValue();
        if((end - start) > length) {
          length = end - start;
          index = i;
        }
        tempAnnotations.add(currAnn);
      }

      // so now we now have the longest String annotations at index
      Annotation temp = (Annotation) set.get( (Integer) matches.get(index));
      String longestString = getString(temp);
      // so this should become one of the tree node
      CorefTreeNode chainNode = new CorefTreeNode(longestString, false, CorefTreeNode.CHAIN_NODE);
      // and add it under the topNode
      annotSetNode.add(chainNode);

      // chainNode --> All related annotIds
      chainLinks.put(chainNode, matches);
      selectionMap.put(chainNode.toString(), new Boolean(false));
      // and generate the color for this chainNode
      float components[] = colorGenerator.getNextColor().getComponents(null);
      Color color = new Color(components[0],
                         components[1],
                         components[2],
                         0.5f);
      colorMap.put(chainNode.toString(), color);
    }

    corefChains.put(annotSetNode, chainLinks);
    selectionChainsMap.put(setName, selectionMap);
    colorChainsMap.put(setName, colorMap);
    return annotSetNode;
  }

  /**
   * Given an annotation, this method returns the string of that annotation
   * @param ann
   * @return
   */
  public String getString(Annotation ann) {
    return document.getContent().toString().substring(ann.
          getStartNode().getOffset().intValue(),
                             ann.getEndNode().getOffset().intValue());
  }


  /**
   * This method removes the reference of this annotatation from the current chain
   * @param ann
   */
  public void removeChainReference(Annotation annot, CorefTreeNode chainHead) {

    // so we would find out the matches
    CorefTreeNode currentNode = chainHead;
    ArrayList ids = (ArrayList) ((HashMap) corefChains.get(corefAnnotationSetNodesMap.get(annotSets.getSelectedItem()))).get(chainHead);
    // we need to update the Co-reference document feature
    Map matchesMap = null;
    matchesMap = (Map) document.getFeatures().get(ANNIEConstants.
                                                  DOCUMENT_COREF_FEATURE_NAME);
    String currentSet = (String)annotSets.getSelectedItem();
    currentSet = (currentSet.equals(DEFAULT_ANNOTSET_NAME)) ? null : currentSet;
    java.util.List matches = (java.util.List) matchesMap.get(currentSet);
    if(matches == null) matches = new ArrayList();
    int index = matches.indexOf(ids);
    if(index != -1) {
      // yes found
      ids.remove(annot.getId());
      Annotation ann = findOutTheLongestAnnotation(ids, getAnnotationSet((String) annotSets.getSelectedItem()));

      matches.set(index, ids);
      matchesMap.put(currentSet, matches);
      document.getFeatures().put(ANNIEConstants.DOCUMENT_COREF_FEATURE_NAME, matchesMap);
    }
  }


  /**
   * Given an annotation, this will find out the chainHead
   * @param ann
   * @return
   */
  private CorefTreeNode findOutTheChainHead(Annotation ann) {
    HashMap chains = (HashMap) corefChains.get(corefAnnotationSetNodesMap.get(annotSets.getSelectedItem()));
    Iterator iter = chains.keySet().iterator();
    while(iter.hasNext()) {
      CorefTreeNode head = (CorefTreeNode) iter.next();
      if(((ArrayList) chains.get(head)).contains(ann.getId())) {
        return head;
      }
    }
    return null;
  }



  /**
   * This methods highlights the annotations
   */
  public void highlightAnnotations() {

    if(highlightedTags == null) {
      highlightedTags = new HashMap();
      highlightedChainAnnots = new ArrayList();
    }

    AnnotationSet annotSet = getAnnotationSet((String) annotSets.getSelectedItem());
    HashMap chainMap = (HashMap) corefChains.get(corefAnnotationSetNodesMap.get(annotSets.getSelectedItem()));
    Iterator iter = chainMap.keySet().iterator();

    while(iter.hasNext()) {
      CorefTreeNode currentNode = (CorefTreeNode) iter.next();
      if(((Boolean)currentSelections.get(currentNode.toString())).booleanValue()) {
        if(!highlightedTags.containsKey(currentNode)) {
          // find out the arrayList
          ArrayList ids = (ArrayList) chainMap.get(currentNode);
          ArrayList highlighTag = new ArrayList();
          if (ids != null) {
            for (int i = 0; i < ids.size(); i++) {
              Annotation ann = annotSet.get( (Integer) ids.get(i));
              highlightedChainAnnots.add(ann);
              Color color = (Color) currentColors.get(currentNode.toString());
              try {
                highlighTag.add(highlighter.addHighlight(ann.getStartNode().
                    getOffset().intValue(),
                                ann.getEndNode().getOffset().intValue(),
                                             new DefaultHighlighter.
                                             DefaultHighlightPainter(color)));
              } catch(Exception e) {
                e.printStackTrace();
              }
              //highlighTag.add(textView.addHighlight(ann, getAnnotationSet((String) annotSets.getSelectedItem()), color));
            }
            highlightedTags.put(currentNode, highlighTag);
          }
        }
      } else {
        if(highlightedTags.containsKey(currentNode)) {
          ArrayList highlights = (ArrayList) highlightedTags.get(currentNode);
          for(int i=0;i<highlights.size();i++) {
            //textView.removeHighlight(highlights.get(i));
            highlighter.removeHighlight(highlights.get(i));
          }
          highlightedTags.remove(currentNode);
          ArrayList ids = (ArrayList) chainMap.get(currentNode);
          if (ids != null) {
            for (int i = 0; i < ids.size(); i++) {
              Annotation ann = annotSet.get( (Integer) ids.get(i));
              highlightedChainAnnots.remove(ann);
            }
          }
        }
      }
    }

    // This is to make process faster.. instead of accessing each annotation and
    // its offset, we create an array with its annotation offsets to search faster
    Collections.sort(highlightedChainAnnots, new gate.util.OffsetComparator());
    highlightedChainAnnotsOffsets = new int[highlightedChainAnnots.size() * 2];
    for (int i = 0, j = 0; j < highlightedChainAnnots.size(); i += 2, j++) {
      Annotation ann1 = (Annotation) highlightedChainAnnots.get(j);
      highlightedChainAnnotsOffsets[i] = ann1.getStartNode().getOffset().intValue();
      highlightedChainAnnotsOffsets[i + 1] = ann1.getEndNode().getOffset().intValue();
    }
  }


  protected void registerHooks(){
    textPane.addMouseListener(textPaneMouseListener);
    textPane.addMouseMotionListener(textPaneMouseListener);

  }

  protected void unregisterHooks(){
    textPane.removeMouseListener(textPaneMouseListener);
    textPane.removeMouseMotionListener(textPaneMouseListener);
  }

  public Component getGUI(){
    return mainPanel;
  }

  public int getType(){
    return VERTICAL;
  }

  //**********************************************
  // MouseListener and MouseMotionListener Methods
  //***********************************************

  protected class TextPaneMouseListener extends MouseInputAdapter {

    public TextPaneMouseListener() {
      chainToolTipTimer.setRepeats(false);
      newCorefActionTimer.setRepeats(false);
    }

    public void mouseMoved(MouseEvent me) {
      int textLocation = textPane.viewToModel(me.getPoint());
      chainToolTipAction.setTextLocation(textLocation);
      chainToolTipAction.setMousePointer(me.getPoint());
      chainToolTipTimer.restart();

      newCorefAction.setTextLocation(textLocation);
      newCorefAction.setMousePointer(me.getPoint());
      newCorefActionTimer.restart();
   }
  }

  public void mouseClicked(MouseEvent me) {
    if(popupWindow != null && popupWindow.isVisible()) {
      popupWindow.setVisible(false);
    }
  }

  public CorefTreeNode findOutChainNode(String chainNodeString) {
    if(corefChains == null || corefAnnotationSetNodesMap == null) {
      return null;
    }
    HashMap chains = (HashMap) corefChains.get(corefAnnotationSetNodesMap.get(annotSets.getSelectedItem()));
    if(chains == null) {
      return null;
    }
    Iterator iter = chains.keySet().iterator();
    while(iter.hasNext()) {
      CorefTreeNode currentNode = (CorefTreeNode) iter.next();
      if(currentNode.toString().equals(chainNodeString))
        return currentNode;
    }
    return null;
  }

/**
 * When user hovers over the annotations which have been highlighted by
 * show button
 */
 protected class NewCorefAction extends KeyAdapter implements ActionListener, ListSelectionListener {

   int textLocation;
   Point mousePoint;
   JLabel label = new JLabel();
   JPanel panel = new JPanel();
   JPanel subPanel = new JPanel();
   JLabel field = new JLabel("");
   JButton add = new JButton("OK");
   JButton cancel = new JButton("Cancel");
   JList list = new JList();
   JPanel mainPanel = new JPanel();
   JPopupMenu popup1 = new JPopupMenu();

   public NewCorefAction() {
     popupWindow = new JWindow(SwingUtilities.getWindowAncestor(textView.getGUI()));
     popupWindow.setBackground(UIManager.getLookAndFeelDefaults().
         getColor("ToolTip.background"));
     mainPanel.setLayout(new BorderLayout());
     mainPanel.setOpaque(true);
     mainPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
     mainPanel.setBackground(UIManager.getLookAndFeelDefaults().
         getColor("ToolTip.background"));
     popupWindow.setContentPane(mainPanel);

     panel.setLayout(new BorderLayout());
     panel.add(field, BorderLayout.NORTH);
     panel.setOpaque(false);
     panel.add(new JScrollPane(list), BorderLayout.CENTER);

     subPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
     subPanel.add(add);
     subPanel.add(cancel);
     subPanel.setOpaque(false);
     panel.add(subPanel, BorderLayout.SOUTH);
     mainPanel.add(label, BorderLayout.NORTH);
     mainPanel.add(panel,BorderLayout.CENTER);
     // and finally load the data for the list
     AddAction action = new AddAction();
     add.addActionListener(action);
     cancel.addActionListener(action);

     list.setVisibleRowCount(5);
     list.addListSelectionListener(this);
     list.addKeyListener(this);
   }

   public void actionPerformed(ActionEvent ae) {
     int index = -1;
     if (highlightedChainAnnotsOffsets != null) {
       for (int i = 0; i < highlightedChainAnnotsOffsets.length; i += 2) {
         if (textLocation >= highlightedChainAnnotsOffsets[i] &&
             textLocation <= highlightedChainAnnotsOffsets[i + 1]) {
           index = (i == 0) ? i : i / 2;
           break;
         }
       }
     }

     // yes it is put on highlighted so show the annotationType
     if (highlightedChainAnnotsOffsets != null && index < highlightedChainAnnotsOffsets.length && index >= 0) {
       return;
     }

     if (highlightedTypeAnnotsOffsets != null) {
       for (int i = 0; i < highlightedTypeAnnotsOffsets.length; i += 2) {
         if (textLocation >= highlightedTypeAnnotsOffsets[i] &&
             textLocation <= highlightedTypeAnnotsOffsets[i + 1]) {
           index = (i == 0) ? i : i / 2;
           break;
         }
       }
     }

     // yes it is put on highlighted so show the annotationType
     if (highlightedTypeAnnotsOffsets != null &&
         index < highlightedTypeAnnotsOffsets.length && index >= 0) {
       textPane.removeAll();
       annotToConsiderForChain = (Annotation) highlightedTypeAnnots.get(index);
       // now check if this annotation is already linked with something
       CorefTreeNode headNode = findOutTheChainHead(annotToConsiderForChain);
       if(headNode != null) {
         popup1 = new JPopupMenu();
         popup1.setBackground(UIManager.getLookAndFeelDefaults().
         getColor("ToolTip.background"));
         JLabel label1 = new JLabel("Annotation co-referenced to : \""+headNode.toString()+"\"");
         popup1.setLayout(new FlowLayout());
         popup1.add(label1);
         if(popupWindow != null && popupWindow.isVisible()) {
           popupWindow.setVisible(false);
         }
         popup1.setVisible(true);
         popup1.show(textPane, (int) mousePoint.getX(), (int) mousePoint.getY());
       } else {


         ArrayList set = new ArrayList(currentSelections.keySet());
         Collections.sort(set);
         set.add(0, "[New Chain]");
         list.setListData(set.toArray());
         list.updateUI();
         popupWindow.setVisible(false);
         label.setText("Add \""+getString(annotToConsiderForChain) + "\" to ");
         Point topLeft = textPane.getLocationOnScreen();
                 int x = topLeft.x + (int) mousePoint.getX();
                 int y = topLeft.y + (int) mousePoint.getY();
         popupWindow.setLocation(x,y);
         popupWindow.pack();
         if(popup1.isVisible()) {
           popup1.setVisible(false);
         }
         popupWindow.setVisible(true);

         list.requestFocus(true);
       }
     }
   }

   public void valueChanged(ListSelectionEvent lse) {
     field.setText((String) list.getSelectedValue());
   }

   public void keyTyped(KeyEvent ke) {
     if(field.getText() == null) {
       field.setText("");
     }

     if(ke.getKeyChar()== KeyEvent.VK_UP) {
       field.setText((String) list.getSelectedValue());
       field.updateUI();
       return;
     } else if(ke.getKeyChar() == KeyEvent.VK_DOWN) {
       field.setText((String) list.getSelectedValue());
       field.updateUI();
       return;
     } else if(ke.getKeyChar() == KeyEvent.VK_ENTER) {
       actionPerformed(new ActionEvent(add,ActionEvent.ACTION_PERFORMED,"add"));
       return;
     } else if(ke.getKeyChar() == KeyEvent.VK_BACK_SPACE) {
       if(field.getText().length() > 0) {
         String text = field.getText();
         field.setText(text.substring(0, text.length()-1));
         field.updateUI();
       }
     } else if(Character.isLetterOrDigit(ke.getKeyChar()) || Character.isSpaceChar(ke.getKeyChar())) {
       field.setText(field.getText() + ke.getKeyChar());
       field.updateUI();
     }

     String startWith = field.getText();
     Vector myList = new Vector();
     ArrayList set = new ArrayList(currentSelections.keySet());
     Collections.sort(set);
     set.add(0, "[New Chain]");
     boolean first = true;
     for(int i=0;i<set.size();i++) {
       String currString = (String) set.get(i);
       if(currString.toLowerCase().startsWith(startWith.toLowerCase())) {
         if(first) {
           field.setText(currString.substring(0, startWith.length()));
           first = false;
         }
         myList.add(currString);
       }
     }
     list.setListData(myList);
     list.updateUI();
   }

   public void setTextLocation(int textLocation) {
     this.textLocation = textLocation;
   }

   public void setMousePointer(Point point) {
     this.mousePoint = point;
   }

   private class AddAction extends AbstractAction {
     public void actionPerformed(ActionEvent ae) {
       if(ae.getSource() == cancel) {
         popupWindow.setVisible(false);
         return;
       } else if(ae.getSource() == add) {
         if(field.getText().length() == 0) {
           try {
             JOptionPane.showMessageDialog(Main.getMainFrame(),
                                           "No Chain Selected",
                                           "New Chain - Error",
                                           JOptionPane.ERROR_MESSAGE);
           } catch(Exception e) {
             e.printStackTrace();
           }
           return;
         } else {
           // we want to add this
           // now first find out the annotation
           Annotation ann = annotToConsiderForChain;
           if(ann == null) return;
           // yes it is available
           // find out the CorefTreeNode for the chain under which it is to be inserted
           if(field.getText().equals("[New Chain]")) {
             // we want to add this
             // now first find out the annotation
             if(ann == null) return;
             CorefTreeNode chainNode = findOutChainNode(getString(ann));
             if(chainNode != null) {
               try {
                 JOptionPane.showMessageDialog(Main.getMainFrame(),
                                               "Chain with " + getString(ann) +
                                               " title already exists",
                                               "New Chain - Error",
                                               JOptionPane.ERROR_MESSAGE);
               }catch(Exception e) {
                 e.printStackTrace();
               }
               return;
             }

             popupWindow.setVisible(false);

            Map matchesMap = null;
            matchesMap = (Map) document.getFeatures().get(ANNIEConstants.
                                              DOCUMENT_COREF_FEATURE_NAME);
            String currentSet = (String) annotSets.getSelectedItem();
            currentSet = (currentSet.equals(DEFAULT_ANNOTSET_NAME)) ? null : currentSet;
            java.util.List matches = (java.util.List) matchesMap.get(currentSet);
            ArrayList tempList = new ArrayList();
            tempList.add(ann.getId());
            if(matches == null) matches = new ArrayList();
            matches.add(tempList);
            if(matchesMap == null) matchesMap = new HashMap();
            matchesMap.put(currentSet, matches);
            document.getFeatures().put(ANNIEConstants.DOCUMENT_COREF_FEATURE_NAME, matchesMap);
            return;
           }


           CorefTreeNode chainNode = findOutChainNode(field.getText());
           HashMap chains = (HashMap) corefChains.get(corefAnnotationSetNodesMap.get(annotSets.getSelectedItem()));
           if(chainNode == null) {
             try {
               JOptionPane.showMessageDialog(Main.getMainFrame(), "Incorrect Chain Title",
                                             "New Chain - Error",
                                             JOptionPane.ERROR_MESSAGE);
             } catch(Exception e) {
               e.printStackTrace();
             }
             return;
           }
           popupWindow.setVisible(false);
           ArrayList ids = (ArrayList) chains.get(chainNode);

           Map matchesMap = null;
           matchesMap = (Map) document.getFeatures().get(ANNIEConstants.
                                             DOCUMENT_COREF_FEATURE_NAME);
           String currentSet = (String) annotSets.getSelectedItem();
           currentSet = (currentSet.equals(DEFAULT_ANNOTSET_NAME)) ? null : currentSet;
           java.util.List matches = (java.util.List) matchesMap.get(currentSet);
           if(matches == null) matches = new ArrayList();
           int index = matches.indexOf(ids);
           if(index != -1) {
             ArrayList tempIds = (ArrayList) matches.get(index);
             tempIds.add(ann.getId());
             matches.set(index, tempIds);
             if(matchesMap == null) matchesMap = new HashMap();
             matchesMap.put(currentSet, matches);
             document.getFeatures().put(ANNIEConstants.DOCUMENT_COREF_FEATURE_NAME, matchesMap);
           }
           return;
         }
       }
     }
   }
 }

  /** When user hovers over the chainnodes */
  protected class ChainToolTipAction extends AbstractAction {

    int textLocation;
    Point mousePoint;
    JPopupMenu popup = new JPopupMenu();

    public ChainToolTipAction() {
      popup.setBackground(UIManager.getLookAndFeelDefaults().
          getColor("ToolTip.background"));
    }


    public void actionPerformed(ActionEvent ae) {

      int index = -1;
      if (highlightedChainAnnotsOffsets != null) {
        for (int i = 0; i < highlightedChainAnnotsOffsets.length; i += 2) {
          if (textLocation >= highlightedChainAnnotsOffsets[i] &&
              textLocation <= highlightedChainAnnotsOffsets[i + 1]) {
            index = (i == 0) ? i : i / 2;
            break;
          }
        }
      }

      // yes it is put on highlighted so show the annotationType
      if (highlightedChainAnnotsOffsets != null && index < highlightedChainAnnotsOffsets.length && index >= 0) {

        if(popupWindow != null && popupWindow.isVisible()) {
          popupWindow.setVisible(false);
        }

        popup.setVisible(false);
        popup.removeAll();
        final int tempIndex = index;
        CorefTreeNode chainHead = findOutTheChainHead((Annotation) highlightedChainAnnots.get(index));
        final HashMap tempMap = new HashMap();
        popup.setLayout(new FlowLayout(FlowLayout.LEFT));
        if(chainHead != null) {
          JPanel tempPanel = new JPanel();
          tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
          tempPanel.add(new JLabel(chainHead.toString()));
          tempPanel.setBackground(UIManager.getLookAndFeelDefaults().
             getColor("ToolTip.background"));
          final JButton deleteButton = new JButton("Delete");
          tempPanel.add(deleteButton);
          popup.add(tempPanel);
          deleteButton.setActionCommand(chainHead.toString());
          tempMap.put(chainHead.toString(), chainHead);
          deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
              try {
                int confirm = JOptionPane.showConfirmDialog(Main.getMainFrame(),
                    "Remove Chain Reference : Are you sure?");
                if (confirm == JOptionPane.YES_OPTION) {
                  popup.setVisible(false);
                  // remove it
                  removeChainReference( (Annotation) highlightedChainAnnots.get(
                      tempIndex), (CorefTreeNode) tempMap.get(deleteButton.getActionCommand()));
                }
              }
              catch (Exception e1) {
                e1.printStackTrace();
              }
            }
          });
        }
        //label.setText("Remove \""+getString((Annotation) highlightedChainAnnots.get(index)) + "\" from \""+ findOutTheChainHead((Annotation) highlightedChainAnnots.get(index)).toString()+"\"");
        popup.revalidate();
        if(popupWindow != null && popupWindow.isVisible()) {
          popupWindow.setVisible(false);
        }
        popup.setVisible(true);
        popup.show(textPane,(int) mousePoint.getX() ,(int) mousePoint.getY());
      }
    }

    public void setTextLocation(int textLocation) {
      this.textLocation = textLocation;
    }

    public void setMousePointer(Point point) {
      this.mousePoint = point;
    }

  }



  // Class that represents each individual tree node in the corefTree
  protected class CorefTreeNode extends DefaultMutableTreeNode {
    public final static int ROOT_NODE = 0;
    public final static int ANNOTSET_NODE = 1;
    public final static int CHAIN_NODE = 2;

    private int type;

    public CorefTreeNode(Object value, boolean allowsChildren, int type) {
      super(value, allowsChildren);
      this.type = type;
    }

    public int getType() {
      return this.type;
    }

  }



  /**
   * Action for mouseClick on the Tree
   */
  protected class CorefTreeMouseListener extends MouseAdapter {

    public void mouseClicked(MouseEvent me) {
      if(popupWindow != null && popupWindow.isVisible()) {
        popupWindow.setVisible(false);
      }
      textPane.removeAll();
      // ok now find out the currently selected node
      int x = me.getX();
      int y = me.getY();
      int row = corefTree.getRowForLocation(x, y);
      TreePath path = corefTree.getPathForRow(row);

      // let us expand it if the sibling feature is on
      if (path != null) {
        CorefTreeNode node = (CorefTreeNode) path.
                                  getLastPathComponent();

        // if it only chainNode
        if (node.getType() != CorefTreeNode.CHAIN_NODE)
          return;

        boolean isSelected = ! ( (Boolean) currentSelections.get(node.toString())).
                             booleanValue();
        currentSelections.put(node.toString(), new Boolean(isSelected));

        // so now we need to highlight all the stuff
        highlightAnnotations();
        corefTree.repaint();
        corefTree.updateUI();
      }
    }
  }

  /**
   * This method uses the java.util.prefs.Preferences and get the color
   * for particular annotationType.. This color could have been saved
   * by the AnnotationSetsView
   * @param annotationType
   * @return
   */
  private Color getColor(String annotationType){
    java.util.prefs.Preferences prefRoot = null;
    try {
      prefRoot = java.util.prefs.Preferences.userNodeForPackage(Class.forName(
          "gate.gui.docview.AnnotationSetsView"));
    }catch(Exception e) {
      e.printStackTrace();
    }
    int rgba = prefRoot.getInt(annotationType, -1);
    Color colour;
    if(rgba == -1){
      //initialise and save
      float components[] = colorGenerator.getNextColor().getComponents(null);
      colour = new Color(components[0],
                         components[1],
                         components[2],
                         0.5f);
      int rgb = colour.getRGB();
      int alpha = colour.getAlpha();
      rgba = rgb | (alpha << 24);
      prefRoot.putInt(annotationType, rgba);

    }else{
      colour = new Color(rgba, true);
    }
    return colour;
  }

  /**
   * Cell renderer to add the checkbox in the tree
   */
  protected class CorefTreeCellRenderer extends JPanel implements TreeCellRenderer {

    private JCheckBox check;
    private JLabel label;

    /**
     * Constructor
     * @param owner
     */
    public CorefTreeCellRenderer() {
      setOpaque(true);
      check = new JCheckBox();
      check.setBackground(Color.white);
      label = new JLabel();
      setLayout(new BorderLayout(5, 10));
      add(check, BorderLayout.WEST);
      add(label, BorderLayout.CENTER);
    }


    /**
     * Renderer class
     */
    public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                  boolean isSelected,
                                                  boolean expanded,
                                                  boolean leaf, int row,
                                                  boolean hasFocus) {


      CorefTreeNode userObject = (CorefTreeNode) value;
      label.setText(userObject.toString());
      this.setSize(label.getWidth(),label.getFontMetrics(label.getFont()).getHeight() * 2);

      if (userObject.getType() == CorefTreeNode.ROOT_NODE || userObject.getType() == CorefTreeNode.ANNOTSET_NODE) {
        this.setBackground(Color.white);
        this.check.setVisible(false);
        return this;
      } else {
        this.setBackground((Color) currentColors.get(userObject.toString()));
        check.setVisible(true);
        check.setBackground(Color.white);
      }

      // if node should be selected
      boolean selected = ( (Boolean) currentSelections.get(userObject.toString())).booleanValue();
      check.setSelected(selected);

      return this;
    }
  }
}