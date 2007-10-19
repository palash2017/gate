/*
 *  Copyright (c) 1998-2007, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  AnnotationEditor.java
 *
 *  Valentin Tablan, Sep 10, 2007
 *
 *  $Id$
 */


package gate.gui.annedit;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;

import gate.*;
import gate.creole.*;
import gate.event.*;
import gate.gui.MainFrame;
import gate.gui.docview.AnnotationSetsView;
import gate.gui.docview.TextualDocumentView;
import gate.swing.JChoice;
import gate.util.*;

public class SchemaAnnotationEditor extends AbstractVisualResource 
    implements AnnotationEditor{
  
  /* (non-Javadoc)
   * @see gate.gui.annedit.AnnotationEditor#editAnnotation(gate.Annotation, gate.AnnotationSet)
   */
  public void editAnnotation(Annotation ann, AnnotationSet set) {
    this.annotation = ann;
    this.annSet = set;
//System.out.println("Editing: " + ann.getType() + ", id: " + ann.getId());    
    String annType = ann.getType();
    SchemaFeaturesEditor newFeaturesEditor = featureEditorsByType.get(annType);
    //if new type, we need to change the features editor and selected type 
    //button
    if(newFeaturesEditor != featuresEditor){
      typesChoice.setSelectedItem(ann.getType());
      if(featuresEditor != null){
        featuresBox.remove(featuresEditor);
        featuresEditor.editFeatureMap(null);
      }
      featuresEditor = newFeaturesEditor;
      if(featuresEditor != null){
        featuresBox.add(featuresEditor);
      }
    }
    if(featuresEditor != null){
      FeatureMap features = ann.getFeatures();
      if(features == null){
        features = Factory.newFeatureMap();
        ann.setFeatures(features);
      }
      featuresEditor.editFeatureMap(features);
    }
    if(dialog != null){
      placeDialog();
    }
  }

  /**
   * Finds the best location for the editor dialog
   */
  protected void placeDialog(){
    if(pinnedButton.isSelected()){
      //just resize
      Point where = null;
      if(dialog.isVisible()){
        where = dialog.getLocationOnScreen();
      }
      dialog.pack();
      if(where != null){
        dialog.setLocation(where);
      }
    }else{
      //calculate position
      try{
        Rectangle startRect = owner.getTextComponent().modelToView(annotation.getStartNode().
          getOffset().intValue());
        Rectangle endRect = owner.getTextComponent().modelToView(annotation.getEndNode().
              getOffset().intValue());
        Point topLeft = owner.getTextComponent().getLocationOnScreen();
        int x = topLeft.x + startRect.x;
        int y = topLeft.y + endRect.y + endRect.height;

        //make sure the window doesn't start lower 
        //than the end of the visible rectangle
        Rectangle visRect = owner.getTextComponent().getVisibleRect();
        int maxY = topLeft.y + visRect.y + visRect.height;      
        
        //make sure window doesn't get off-screen       
        dialog.pack();
//        dialog.validate();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        boolean revalidate = false;
        if(dialog.getSize().width > screenSize.width){
          dialog.setSize(screenSize.width, dialog.getSize().height);
          revalidate = true;
        }
        if(dialog.getSize().height > screenSize.height){
          dialog.setSize(dialog.getSize().width, screenSize.height);
          revalidate = true;
        }
        
        if(revalidate) dialog.validate();
        //calculate max X
        int maxX = screenSize.width - dialog.getSize().width;
        //calculate max Y
        if(maxY + dialog.getSize().height > screenSize.height){
          maxY = screenSize.height - dialog.getSize().height;
        }
        
        //correct position
        if(y > maxY) y = maxY;
        if(x > maxX) x = maxX;
        dialog.setLocation(x, y);
      }catch(BadLocationException ble){
        //this should never occur
        throw new GateRuntimeException(ble);
      }
    }
    if(!dialog.isVisible()) dialog.setVisible(true);
  }
  
  protected static final int HIDE_DELAY = 1500;
  protected static final int SHIFT_INCREMENT = 5;
  protected static final int CTRL_SHIFT_INCREMENT = 10;

  
  /**
   * The annotation currently being edited.
   */
  protected Annotation annotation;
  
  /**
   * The annotation set containing the currently edited annotation. 
   */
  protected AnnotationSet annSet;
  
  /**
   * The controlling object for this editor.
   */
  private AnnotationEditorOwner owner;
  
  
  /**
   * JChoice used for selecting the annotation type.
   */
  protected JChoice typesChoice;
  
  /**
   * The dialog used to show this annotation editor.
   */
  protected JDialog dialog;
  
  protected CreoleListener creoleListener;
  
  /**
   * Listener used to hide the editing window when the text is hidden.
   */
  protected AncestorListener textAncestorListener;
  
  /**
   * Stores the Annotation schema objects available in the system.
   * The annotation types are used as keys for the map.
   */
  protected Map<String, AnnotationSchema> schemasByType;
  
  /**
   * Caches the features editor for each annotation type.
   */
  protected Map<String, SchemaFeaturesEditor> featureEditorsByType;
  
  /**
   * The box used to host the features editor pane.
   */
  protected Box featuresBox;
  
  /**
   * The box used to host the search pane.
   */
  protected Box searchBox;
  
  /**
   * The pane containing the UI for search and anootate functionality.
   */
  protected JPanel searchPane;
  
  /**
   * Text field for searching
   */
  protected JTextField searchTextField;
  
  /**
   * Checkbox for enabling RegEx searching 
   */
  protected JCheckBox searchRegExpChk;
  
  /**
   * Checkbox for enabling case sensitive searching 
   */
  protected JCheckBox searchCaseSensChk;
  
  /**
   * Checkbox for showing the search UI.
   */
  protected JCheckBox searchEnabledCheck;
  /**
   * Toggle button used to pin down the dialog. 
   */
  protected JToggleButton pinnedButton;
  
  
  /**
   * The current features editor, one of the ones stored in 
   * {@link #featureEditorsByType}.
   */
  protected SchemaFeaturesEditor featuresEditor = null;

  public SchemaAnnotationEditor(){
  }
  
  
  /* (non-Javadoc)
   * @see gate.creole.AbstractVisualResource#init()
   */
  @Override
  public Resource init() throws ResourceInstantiationException {
    super.init();
    initData();
    initGui();
    initListeners();
    registerAncestorListener();
    return this;
  }

  protected void registerAncestorListener(){
    //the text itself may be hidden for efficiency reasons (e.g. when hiding a 
    //large number of highlights. We need to hook to the text's parent
//    Container textparent = getOwner().getTextComponent().getParent();
//    if(textparent instanceof JComponent){
//      ((JComponent)textparent).addAncestorListener(textAncestorListener);
//    }
    getOwner().getTextComponent().addAncestorListener(textAncestorListener);
  }
  
  protected void unregisterAncestorListener(){
//    Container textparent = getOwner().getTextComponent().getParent();
//    if(textparent instanceof JComponent){
//      ((JComponent)textparent).removeAncestorListener(textAncestorListener);
//    }
    getOwner().getTextComponent().removeAncestorListener(textAncestorListener);
  }
  
  protected void initData(){
    schemasByType = new TreeMap<String, AnnotationSchema>();
    for(LanguageResource aSchema : Gate.getCreoleRegister().
        getLrInstances("gate.creole.AnnotationSchema")){
      schemasByType.put(((AnnotationSchema)aSchema).getAnnotationName(), 
              (AnnotationSchema)aSchema);
    }
  }  
  
  public void cleanup(){
    Gate.getCreoleRegister().removeCreoleListener(creoleListener);
  }
  
  protected void initGui(){
    setLayout(new BorderLayout());
    //build the toolbar
    JPanel tBar = new JPanel();
    tBar.setLayout(new GridBagLayout());
//    tBar.setMargin(new Insets(0, 0, 0, 0));
//    tBar.setFloatable(false);
//    tBar.setBorderPainted(false);
//    tBar.setBorder(null);
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = GridBagConstraints.RELATIVE;
    constraints.gridy = 0;
    constraints.weightx = 0;
    
    IconOnlyButton iob = new IconOnlyButton(new StartOffsetLeftAction());
    iob.setIcon(MainFrame.getIcon("bounds-sol"));
    iob.setPressedIcon(MainFrame.getIcon("bounds-sol-pressed"));
    tBar.add(iob, constraints);
    JLabel aLabel = new JLabel(MainFrame.getIcon("bounds-left"));
    aLabel.setBorder(null);
    tBar.add(aLabel, constraints);
    iob = new IconOnlyButton(new StartOffsetRightAction());
    iob.setIcon(MainFrame.getIcon("bounds-sor"));
    iob.setPressedIcon(MainFrame.getIcon("bounds-sor-pressed"));
    tBar.add(iob, constraints);
    aLabel = new JLabel(MainFrame.getIcon("bounds-span"));
    aLabel.setBorder(null);
    tBar.add(aLabel, constraints);
    iob = new IconOnlyButton(new EndOffsetLeftAction());
    iob.setIcon(MainFrame.getIcon("bounds-eol"));
    iob.setPressedIcon(MainFrame.getIcon("bounds-eol-pressed"));
    tBar.add(iob, constraints);
    aLabel = new JLabel(MainFrame.getIcon("bounds-right"));
    aLabel.setBorder(null);
    tBar.add(aLabel, constraints);
    iob = new IconOnlyButton(new EndOffsetRightAction());
    iob.setIcon(MainFrame.getIcon("bounds-eor"));
    iob.setPressedIcon(MainFrame.getIcon("bounds-eor-pressed"));
    tBar.add(iob, constraints);
    
    tBar.add(Box.createHorizontalStrut(15), constraints);
    tBar.add(new SmallButton(new DeleteAnnotationAction()), constraints);
    tBar.add(Box.createHorizontalStrut(15), constraints);
    tBar.add(new SmallButton(new AnnotateAllAction()), constraints);

    constraints.weightx = 1;
    tBar.add(Box.createHorizontalGlue(), constraints);
    
    constraints.weightx = 0;
    pinnedButton = new JToggleButton(MainFrame.getIcon("pin"));
    pinnedButton.setSelectedIcon(MainFrame.getIcon("pin-in"));
    pinnedButton.setSelected(false);
    pinnedButton.setToolTipText("Press to pin window in place.");
    pinnedButton.setMargin(new Insets(0, 2, 0, 2));
    pinnedButton.setBorderPainted(false);
    pinnedButton.setContentAreaFilled(false);
    tBar.add(pinnedButton);
    add(tBar, BorderLayout.NORTH);
    
    //build the main pane
    JPanel mainPane = new JPanel();
    mainPane.setLayout(new BoxLayout(mainPane, BoxLayout.Y_AXIS));
    add(mainPane, BorderLayout.CENTER);
    
    featureEditorsByType = new HashMap<String, SchemaFeaturesEditor>();
    //for each schema we need to create a type button and a features editor
    for(String annType : schemasByType.keySet()){
      AnnotationSchema annSchema = schemasByType.get(annType);
      SchemaFeaturesEditor aFeaturesEditor = new SchemaFeaturesEditor(annSchema);
      featureEditorsByType.put(annType, aFeaturesEditor);
    }
    List<String> typeList = new ArrayList<String>(schemasByType.keySet());
    Collections.sort(typeList);
    String[] typesArray = new String[typeList.size()];
    typeList.toArray(typesArray);
    typesChoice = new JChoice(typesArray);
    typesChoice.setDefaultButtonMargin(new Insets(0, 2, 0, 2));
    typesChoice.setMaximumFastChoices(20);
    typesChoice.setMaximumWidth(300);
    typesChoice.setAlignmentX(Component.LEFT_ALIGNMENT);
    String aTitle = "Type ";
    typesChoice.setBorder(BorderFactory.createTitledBorder(aTitle));
    aLabel = new JLabel(aTitle);
    typesChoice.setMinimumSize(new Dimension(aLabel.getPreferredSize().width,
            Integer.MAX_VALUE));
    mainPane.add(typesChoice);
    //add the features box
    featuresBox = Box.createVerticalBox();
    aTitle = "Features "; 
    featuresBox.setBorder(BorderFactory.createTitledBorder(aTitle));
    featuresBox.setAlignmentX(Component.LEFT_ALIGNMENT);
    aLabel = new JLabel(aTitle);
    featuresBox.add(Box.createRigidArea(
            new Dimension(aLabel.getPreferredSize().width, 0)));
    mainPane.add(featuresBox);
    
    //add the search box
    searchEnabledCheck = new JCheckBox("Search & Annotate", 
            MainFrame.getIcon("closed"), false);
    searchEnabledCheck.setSelectedIcon(MainFrame.getIcon("expanded"));
    mainPane.add(searchEnabledCheck);
    
    searchBox = Box.createVerticalBox();
    searchBox.setAlignmentX(Component.LEFT_ALIGNMENT);
    mainPane.add(searchBox);
    
    searchPane = new JPanel();
    searchPane.setLayout(new BoxLayout(searchPane, BoxLayout.Y_AXIS));
    
//    searchPane.setBorder(BorderFactory.createEtchedBorder());

    searchTextField = new JTextField(20);
    //disallow vertical expansion
    searchTextField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 
            searchTextField.getPreferredSize().height));
    
    searchPane.add(searchTextField);
    Box hBox = Box.createHorizontalBox();
    searchCaseSensChk = new JCheckBox("Case sensitive", true);
    hBox.add(searchCaseSensChk);
    hBox.add(Box.createHorizontalStrut(15));
    searchRegExpChk = new JCheckBox("Regular Expression", false);
    hBox.add(searchRegExpChk);
    hBox.add(Box.createHorizontalGlue());
    searchPane.add(hBox);

    hBox = Box.createHorizontalBox();
    hBox.add(new SmallButton(new FindFirstAction()));
    hBox.add(Box.createHorizontalStrut(5));
    hBox.add(new SmallButton(new FindNextAction()));
    hBox.add(Box.createHorizontalStrut(15));
    hBox.add(new SmallButton(new AnnotateOccurrenceAction()));
    hBox.add(Box.createHorizontalStrut(5));
    hBox.add(new SmallButton(new AnnotateAllAction()));
    hBox.add(Box.createHorizontalGlue());
    searchPane.add(hBox);
    searchPane.add(Box.createVerticalGlue());

    //make the dialog
    Window parentWindow = SwingUtilities.windowForComponent(owner.getTextComponent());
    if(parentWindow != null){
      dialog = parentWindow instanceof Frame ?
              new JDialog((Frame)parentWindow, 
              "Annotation Editor Dialog", false) :
                new JDialog((Dialog)parentWindow, 
                        "Annotation Editor Dialog", false);
//      dialog.setFocusableWindowState(false);
      dialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
//      dialog.setResizable(false);
      dialog.add(this);
      dialog.pack();
    }
  }

  protected void initListeners(){
    creoleListener = new CreoleListener(){
      public void resourceLoaded(CreoleEvent e){
        Resource newResource =  e.getResource();
        if(newResource instanceof AnnotationSchema){
          AnnotationSchema aSchema = (AnnotationSchema)newResource;
          schemasByType.put(aSchema.getAnnotationName(), aSchema);
        }
      }
      
      public void resourceUnloaded(CreoleEvent e){
        Resource newResource =  e.getResource();
        if(newResource instanceof AnnotationSchema){
          AnnotationSchema aSchema = (AnnotationSchema)newResource;
          if(schemasByType.containsValue(aSchema)){
            schemasByType.remove(aSchema.getAnnotationName());
          }
        }
      }
      
      public void datastoreOpened(CreoleEvent e){
        
      }
      public void datastoreCreated(CreoleEvent e){
        
      }
      public void datastoreClosed(CreoleEvent e){
        
      }
      public void resourceRenamed(Resource resource,
                              String oldName,
                              String newName){
      }  
    };
    Gate.getCreoleRegister().addCreoleListener(creoleListener); 
    
    textAncestorListener = new AncestorListener(){
      /**
       * A flag used to mark the fact that the dialog is active and was hidden 
       * by this listener.
       */
      private boolean dialogActive = false;
      
      public void ancestorAdded(AncestorEvent event) {
        if(dialogActive){
          placeDialog();
          dialogActive = false;
        }
      }
      public void ancestorMoved(AncestorEvent event) {
        if(dialog.isVisible()){
          placeDialog();
        }
      }
      
      public void ancestorRemoved(AncestorEvent event) {
        if(dialog.isVisible()){
          dialogActive = true;
          dialog.setVisible(false);
        }
      }
    };
    
    typesChoice.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        String newType;
        if(typesChoice.getSelectedItem() == null){
          newType = "";
        }else{
          newType = typesChoice.getSelectedItem().toString();
        }
        if(annotation != null && annSet != null && 
                !annotation.getType().equals(newType)){
          //annotation type change
          Integer oldId = annotation.getId();
          Annotation oldAnn = annotation;
          annSet.remove(oldAnn);
          try{
            annSet.add(oldId, oldAnn.getStartNode().getOffset(), 
                    oldAnn.getEndNode().getOffset(), 
                    newType, oldAnn.getFeatures());
            Annotation newAnn = annSet.get(oldId); 
            editAnnotation(newAnn, annSet);
            owner.annotationChanged(newAnn, annSet, oldAnn.getType());
          }catch(InvalidOffsetException ioe){
            //this should never hapen 
            throw new LuckyException(ioe);
          }
        }
      }
    });

    searchEnabledCheck.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        if(searchEnabledCheck.isSelected()){
          if(!searchBox.isAncestorOf(searchPane)){
            searchBox.add(searchPane);
            dialog.pack();
          }
        }else{
          if(searchBox.isAncestorOf(searchPane)){
            searchBox.remove(searchPane);
            dialog.pack();
          }
        }
      }
    });
  }
  
  /**
   * @param args
   */
  public static void main(String[] args) {
    try {
      Gate.init();
      
      
      JFrame aFrame = new JFrame("New Annotation Editor");
      aFrame.setSize( 800, 600);
      aFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
      
      JDialog annDialog = new JDialog(aFrame, "Annotation Editor Dialog", false);
      annDialog.setFocusableWindowState(false);
//      annDialog.setResizable(false);
//      annDialog.setUndecorated(true);
      
      SchemaAnnotationEditor pane = new SchemaAnnotationEditor();
      annDialog.add(pane);
      annDialog.pack();
      
//      JToolBar tBar = new JToolBar("Annotation Editor", JToolBar.HORIZONTAL);
//      tBar.setLayout(new BorderLayout());
//      tBar.setMinimumSize(tBar.getPreferredSize());
//      tBar.add(pane);
//      aFrame.getContentPane().add(tBar, BorderLayout.NORTH);
      
      StringBuffer strBuf = new StringBuffer();
      for(int i = 0; i < 100; i++){
        strBuf.append("The quick brown fox jumped over the lazy dog.\n");
      }
      JTextArea aTextPane = new JTextArea(strBuf.toString());
      JScrollPane scroller = new JScrollPane(aTextPane);
      aFrame.getContentPane().add(scroller, BorderLayout.CENTER);
      
//    Box aBox = Box.createVerticalBox();
//    aFrame.getContentPane().add(aBox);
//    
//    FeatureEditor aFeatEditor = new FeatureEditor("F-nominal-small",
//            FeatureType.nominal, "val1");
//    aFeatEditor.setValues(new String[]{"val1", "val2", "val3"});
//    aBox.add(aFeatEditor.getGui());
//    
//    aFeatEditor = new FeatureEditor("F-nominal-large",
//            FeatureType.nominal, "val1");
//    aFeatEditor.setValues(new String[]{"val1", "val2", "val3", "val4", "val5", 
//            "val6", "val7", "val8", "val9"});
//    aBox.add(aFeatEditor.getGui());
//    
//    aFeatEditor = new FeatureEditor("F-boolean-true",
//            FeatureType.bool, "true");
//    aBox.add(aFeatEditor.getGui());    
//    
//    aFeatEditor = new FeatureEditor("F-boolean-false",
//            FeatureType.bool, "false");
//    aBox.add(aFeatEditor.getGui());
      
      aFrame.setVisible(true);
System.out.println("Window up");
      annDialog.setVisible(true);
      System.out.println("Dialog up");      
      
    }catch(HeadlessException e) {
      e.printStackTrace();
    }
    catch(GateException e) {
      e.printStackTrace();
    }
  }

  /**
   * Base class for actions on annotations.
   */
  protected abstract class AnnotationAction extends AbstractAction{
    public AnnotationAction(String name, Icon icon){
      super("", icon);
      putValue(SHORT_DESCRIPTION, name);
      
    }
  }

  protected class StartOffsetLeftAction extends AnnotationAction{
    public StartOffsetLeftAction(){
      super("<html><b>Extend</b><br><small>SHIFT = 5 characters, CTRL-SHIFT = 10 characters</small></html>", 
              MainFrame.getIcon("extend-left"));
    }
    
    public void actionPerformed(ActionEvent evt){
      Annotation oldAnn = annotation;
      int increment = 1;
      if((evt.getModifiers() & ActionEvent.SHIFT_MASK) > 0){
        //CTRL pressed -> use tokens for advancing
        increment = SHIFT_INCREMENT;
        if((evt.getModifiers() & ActionEvent.CTRL_MASK) > 0){
          increment = CTRL_SHIFT_INCREMENT;
        }
      }
      long newValue = annotation.getStartNode().getOffset().longValue() - increment;
      if(newValue < 0) newValue = 0;
      try{
        moveAnnotation(annSet, annotation, new Long(newValue), 
                annotation.getEndNode().getOffset());
      }catch(InvalidOffsetException ioe){
        throw new GateRuntimeException(ioe);
      }
    }
  }
  
  protected class StartOffsetRightAction extends AnnotationAction{
    public StartOffsetRightAction(){
      super("<html><b>Shrink</b><br><small>SHIFT = 5 characters, " +
            "CTRL-SHIFT = 10 characters</small></html>", 
            MainFrame.getIcon("extend-right"));
    }
    
    public void actionPerformed(ActionEvent evt){
      long endOffset = annotation.getEndNode().getOffset().longValue(); 
      int increment = 1;
      if((evt.getModifiers() & ActionEvent.SHIFT_MASK) > 0){
        //CTRL pressed -> use tokens for advancing
        increment = SHIFT_INCREMENT;
        if((evt.getModifiers() & ActionEvent.CTRL_MASK) > 0){
          increment = CTRL_SHIFT_INCREMENT;
        }
      }
      
      long newValue = annotation.getStartNode().getOffset().longValue()  + increment;
      if(newValue > endOffset) newValue = endOffset;
      try{
        moveAnnotation(annSet, annotation, new Long(newValue), 
                annotation.getEndNode().getOffset());
      }catch(InvalidOffsetException ioe){
        throw new GateRuntimeException(ioe);
      }
    }
  }

  protected class EndOffsetLeftAction extends AnnotationAction{
    public EndOffsetLeftAction(){
      super("<html><b>Shrink</b><br><small>SHIFT = 5 characters, " +
            "CTRL-SHIFT = 10 characters</small></html>",
            MainFrame.getIcon("extend-left"));
    }
    
    public void actionPerformed(ActionEvent evt){
      long startOffset = annotation.getStartNode().getOffset().longValue(); 
      int increment = 1;
      if((evt.getModifiers() & ActionEvent.SHIFT_MASK) > 0){
        //CTRL pressed -> use tokens for advancing
        increment = SHIFT_INCREMENT;
        if((evt.getModifiers() & ActionEvent.CTRL_MASK) > 0){
          increment =CTRL_SHIFT_INCREMENT;
        }
      }
      
      long newValue = annotation.getEndNode().getOffset().longValue()  - increment;
      if(newValue < startOffset) newValue = startOffset;
      try{
        moveAnnotation(annSet, annotation, annotation.getStartNode().getOffset(), 
                new Long(newValue));
      }catch(InvalidOffsetException ioe){
        throw new GateRuntimeException(ioe);
      }
    }
  }
  
  protected class EndOffsetRightAction extends AnnotationAction{
    public EndOffsetRightAction(){
      super("<html><b>Extend</b><br><small>SHIFT = 5 characters, " +
            "CTRL-SHIFT = 10 characters</small></html>", 
            MainFrame.getIcon("extend-right"));
    }
    
    public void actionPerformed(ActionEvent evt){
      long maxOffset = owner.getDocument().
          getContent().size().longValue() -1; 
//      Long newEndOffset = ann.getEndNode().getOffset();
      int increment = 1;
      if((evt.getModifiers() & ActionEvent.SHIFT_MASK) > 0){
        //CTRL pressed -> use tokens for advancing
        increment = SHIFT_INCREMENT;
        if((evt.getModifiers() & ActionEvent.CTRL_MASK) > 0){
          increment = CTRL_SHIFT_INCREMENT;
        }
      }
      long newValue = annotation.getEndNode().getOffset().longValue() + increment;
      if(newValue > maxOffset) newValue = maxOffset;
      try{
        moveAnnotation(annSet, annotation, annotation.getStartNode().getOffset(),
                new Long(newValue));
      }catch(InvalidOffsetException ioe){
        throw new GateRuntimeException(ioe);
      }
    }
  }
  
  protected class FindFirstAction extends AbstractAction{
    public FindFirstAction(){
      super("Find first");
      super.putValue(SHORT_DESCRIPTION, 
              "Finds the first occurrence.");
    }
    
    public void actionPerformed(ActionEvent evt){
    }
  }
  
  protected class FindNextAction extends AbstractAction{
    public FindNextAction(){
      super("Find next");
      super.putValue(SHORT_DESCRIPTION, 
              "Finds the next occurrence.");
    }
    
    public void actionPerformed(ActionEvent evt){
    }
  }
  
  protected class AnnotateOccurrenceAction extends AbstractAction{
    public AnnotateOccurrenceAction(){
      super("Annotate");
      super.putValue(SHORT_DESCRIPTION, 
              "Annotates the current occurrence.");
    }
    
    public void actionPerformed(ActionEvent evt){
    }
  }
  protected class AnnotateAllAction extends AbstractAction{
    public AnnotateAllAction(){
      super("Annotate all");
      super.putValue(SHORT_DESCRIPTION, 
              "Annotate all occurrences of this text.");
    }
    
    public void actionPerformed(ActionEvent evt){
      if(annotation != null){
        String docText = getOwner().getDocument().getContent().toString();
        String annText = docText.substring(
                annotation.getStartNode().getOffset().intValue(),
                annotation.getEndNode().getOffset().intValue());
        Pattern annPattern = Pattern.compile(annText, Pattern.LITERAL);
        Matcher matcher = annPattern.matcher(docText);
        while(matcher.find()){
          int start = matcher.start();
          int end = matcher.end();
          //if there isn't already an annotation of the right type at these
          //offsets, then create one.
          boolean alreadyThere = false;
          AnnotationSet oldAnnots = annSet.get(new Long(start)).
              get(annotation.getType());
          if(oldAnnots != null && oldAnnots.size() > 0){
            for(Annotation anOldAnn : oldAnnots){
              if(anOldAnn.getStartNode().getOffset().intValue() == start &&
                 anOldAnn.getEndNode().getOffset().intValue() == end &&
                 anOldAnn.getFeatures().subsumes(annotation.getFeatures())){
                alreadyThere = true;
                break;
              }
            }
          }
          if(!alreadyThere){
            //create the new annotation
            FeatureMap features = Factory.newFeatureMap();
            features.putAll(annotation.getFeatures());
            try {
              annSet.add(new Long(start), new Long(end), annotation.getType(), 
                      features);
            }catch(InvalidOffsetException e) {
              //this should not happen as the offsets are obtained from the 
              //text
              throw new LuckyException(e);
            }              
          }
        }
      }
    }
  }
  
  protected class DeleteAnnotationAction extends AnnotationAction{
    public DeleteAnnotationAction(){
      super("Delete annotation", MainFrame.getIcon("remove-annotation"));
    }
    
    public void actionPerformed(ActionEvent evt){
      annSet.remove(annotation);
      dialog.setVisible(false);
    }
  }
  /**
   * Changes the span of an existing annotation by creating a new annotation 
   * with the same ID, type and features but with the new start and end offsets.
   * @param set the annotation set 
   * @param oldAnnotation the annotation to be moved
   * @param newStartOffset the new start offset
   * @param newEndOffset the new end offset
   */
  protected void moveAnnotation(AnnotationSet set, Annotation oldAnnotation, 
          Long newStartOffset, Long newEndOffset) throws InvalidOffsetException{
    //Moving is done by deleting the old annotation and creating a new one.
    //If this was the last one of one type it would mess up the gui which 
    //"forgets" about this type and then it recreates it (with a different 
    //colour and not visible.
    //In order to avoid this problem, we'll create a new temporary annotation.
    Annotation tempAnn = null;
    if(set.get(oldAnnotation.getType()).size() == 1){
      //create a clone of the annotation that will be deleted, to act as a 
      //placeholder 
      Integer tempAnnId = set.add(oldAnnotation.getStartNode(), 
              oldAnnotation.getStartNode(), oldAnnotation.getType(), 
              oldAnnotation.getFeatures());
      tempAnn = set.get(tempAnnId);
    }

    Integer oldID = oldAnnotation.getId();
    set.remove(oldAnnotation);
    set.add(oldID, newStartOffset, newEndOffset,
            oldAnnotation.getType(), oldAnnotation.getFeatures());
    Annotation newAnn = set.get(oldID); 
    editAnnotation(newAnn, set);
    //remove the temporary annotation
    if(tempAnn != null) set.remove(tempAnn);
    owner.annotationChanged(newAnn, set, null);
  }  

  /**
   * A JButton with content are not filled and border not painted (in order to
   * save screen real estate)
   */  
  protected class SmallButton extends JButton{
    public SmallButton(Action a) {
      super(a);
//      setBorder(null);
      setMargin(new Insets(0, 2, 0, 2));
//      setBorderPainted(false);
//      setContentAreaFilled(false);
    }
  }
  
  protected class IconOnlyButton extends JButton{
    public IconOnlyButton(Action a) {
      super(a);
      setMargin(new Insets(0, 0, 0, 0));
      setBorder(null);
      setBorderPainted(false);
      setContentAreaFilled(false);
    }
  }
  
  /**
   * @return the owner
   */
  public AnnotationEditorOwner getOwner() {
    return owner;
  }

  /**
   * @param owner the owner to set
   */
  public void setOwner(AnnotationEditorOwner owner) {
    if(this.owner != null && this.owner != owner && 
       textAncestorListener != null){
      unregisterAncestorListener();
    }
    this.owner = owner;
    if(this.owner != null && textAncestorListener != null){
      registerAncestorListener();
    }
  }
}
