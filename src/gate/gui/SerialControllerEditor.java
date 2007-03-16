/*
 *  Copyright (c) 1998-2005, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan 02/10/2001
 *
 *  $Id$
 *
 */

package gate.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.*;
import java.text.NumberFormat;
import java.util.*;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import gate.*;
import gate.creole.*;
import gate.event.*;
import gate.swing.*;
import gate.util.*;

public class SerialControllerEditor extends AbstractVisualResource
                               implements CreoleListener, ControllerListener,
                                          ActionsPublisher{

  public SerialControllerEditor() {

  }

  public void setTarget(Object target){
    if(!(target instanceof SerialController))
    throw new IllegalArgumentException(
      "gate.gui.ApplicationViewer can only be used for serial controllers\n" +
      target.getClass().toString() +
      " is not a gate.creole.SerialController!");
    if(controller != null) controller.removeControllerListener(this);
    this.controller = (SerialController)target;
    controller.addControllerListener(this);
    analyserMode = controller instanceof SerialAnalyserController ||
                   controller instanceof ConditionalSerialAnalyserController;
    conditionalMode = controller instanceof ConditionalController;
    
    initLocalData();
    initGuiComponents();
    initListeners();

    loadedPRsTableModel.fireTableDataChanged();
    memberPRsTableModel.fireTableDataChanged();
//    parametersEditor.
    
  }//setController


  public void setHandle(Handle handle) {
    this.handle = handle;

//    popup.addPopupMenuListener(new PopupMenuListener() {
//      public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
//        buildInternalMenus();
//        addMenu.setEnabled(addMenu.getItemCount() > 0);
//        removeMenu.setEnabled(removeMenu.getItemCount() > 0);
//      }
//      public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
//      }
//      public void popupMenuCanceled(PopupMenuEvent e) {
//      }
//    });

    //register the listeners
    if(handle instanceof StatusListener)
      addStatusListener((StatusListener)handle);
    if(handle instanceof ProgressListener)
      addProgressListener((ProgressListener)handle);
  }//setHandle

  public Resource init() throws ResourceInstantiationException{
    super.init();
    return this;
  }//init

  protected void initLocalData() {
    actionList = new ArrayList();
    runAction = new RunAction();
    //add the items to the popup
    actionList.add(null);
    actionList.add(runAction);
  }//initLocalData

  protected void initGuiComponents() {
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));


    JPanel topBox = new JPanel();
    topBox.setLayout(new BoxLayout(topBox, BoxLayout.X_AXIS));
    topBox.setAlignmentX(Component.LEFT_ALIGNMENT);

    loadedPRsTableModel = new LoadedPRsTableModel();
    loadedPRsTable = new XJTable();
    loadedPRsTable.setSortable(false);
    loadedPRsTable.setModel(loadedPRsTableModel);

    loadedPRsTable.setDefaultRenderer(ProcessingResource.class,
                                      new ResourceRenderer());

//    loadedPRsTable.setIntercellSpacing(new Dimension(5, 5));
    final int width1 = new JLabel("Loaded Processing resources").
                getPreferredSize().width + 30;
    JScrollPane scroller = new JScrollPane(){
      public Dimension getPreferredSize(){
        Dimension dim = super.getPreferredSize();
        dim.width = Math.max(dim.width, width1);
        return dim;
      }
      public Dimension getMinimumSize(){
        Dimension dim = super.getMinimumSize();
        dim.width = Math.max(dim.width, width1);
        return dim;
      }
    };
    scroller.getViewport().setView(loadedPRsTable);
    scroller.setBorder(BorderFactory.
                       createTitledBorder(BorderFactory.createEtchedBorder(),
                                          " Loaded Processing resources "));

    topBox.add(scroller);
    topBox.add(Box.createHorizontalGlue());

    addButon = new JButton(MainFrame.getIcon("right-arrow"));
    removeButton = new JButton(MainFrame.getIcon("left-arrow"));

    Box buttonsBox =Box.createVerticalBox();
    buttonsBox.add(Box.createVerticalGlue());
    buttonsBox.add(addButon);
    buttonsBox.add(Box.createVerticalStrut(5));
    buttonsBox.add(removeButton);
    buttonsBox.add(Box.createVerticalGlue());

    topBox.add(buttonsBox);
    topBox.add(Box.createHorizontalGlue());

    memberPRsTableModel = new MemberPRsTableModel();
    memberPRsTable = new XJTable();
    memberPRsTable.setSortable(false);
    memberPRsTable.setModel(memberPRsTableModel);
    memberPRsTable.setDefaultRenderer(ProcessingResource.class,
                                      new ResourceRenderer());
    memberPRsTable.setDefaultRenderer(JLabel.class, new LabelRenderer());
//    memberPRsTable.setIntercellSpacing(new Dimension(5, 5));

    final int width2 = new JLabel("Selected Processing resources").
                           getPreferredSize().width + 30;
    scroller = new JScrollPane(){
      public Dimension getPreferredSize(){
        Dimension dim = super.getPreferredSize();
        dim.width = Math.max(dim.width, width2);
        return dim;
      }
      public Dimension getMinimumSize(){
        Dimension dim = super.getMinimumSize();
        dim.width = Math.max(dim.width, width2);
        return dim;
      }      
    };
    scroller.getViewport().setView(memberPRsTable);
    scroller.setBorder(BorderFactory.
                       createTitledBorder(BorderFactory.createEtchedBorder(),
                                          " Selected Processing resources "));


    topBox.add(scroller);

    moveUpButton = new JButton(MainFrame.getIcon("up"));
    moveDownButton = new JButton(MainFrame.getIcon("down"));

    buttonsBox =Box.createVerticalBox();
    buttonsBox.add(Box.createVerticalGlue());
    buttonsBox.add(moveUpButton);
    buttonsBox.add(Box.createVerticalStrut(5));
    buttonsBox.add(moveDownButton);
    buttonsBox.add(Box.createVerticalGlue());

    topBox.add(buttonsBox);
    topBox.add(Box.createHorizontalGlue());

    add(topBox);

    if(conditionalMode){
      strategyPanel = new JPanel();
      strategyPanel.setLayout(new BoxLayout(strategyPanel, BoxLayout.X_AXIS));
      strategyPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
      runBtnGrp = new ButtonGroup();
      yes_RunRBtn = new JRadioButton("Yes", true);
      yes_RunRBtn.setHorizontalTextPosition(AbstractButton.LEFT);
      runBtnGrp.add(yes_RunRBtn);
      no_RunRBtn = new JRadioButton("No", false);
      no_RunRBtn.setHorizontalTextPosition(AbstractButton.LEFT);
      runBtnGrp.add(no_RunRBtn);
      conditional_RunRBtn = new JRadioButton("If value of feature", false);
      conditional_RunRBtn.setHorizontalTextPosition(AbstractButton.LEFT);
      runBtnGrp.add(conditional_RunRBtn);

      featureNameTextField = new JTextField("", 25);
      featureNameTextField.setMaximumSize(
                           new Dimension(Integer.MAX_VALUE,
                                         featureNameTextField.getPreferredSize().
                                         height));
      featureValueTextField = new JTextField("", 25);
      featureValueTextField.setMaximumSize(
                           new Dimension(Integer.MAX_VALUE,
                                         featureValueTextField.getPreferredSize().
                                         height));

      strategyPanel.add(new JLabel(MainFrame.getIcon("greenBall")));
      strategyPanel.add(yes_RunRBtn);
      strategyPanel.add(Box.createHorizontalStrut(5));

      strategyPanel.add(new JLabel(MainFrame.getIcon("redBall")));
      strategyPanel.add(no_RunRBtn);
      strategyPanel.add(Box.createHorizontalStrut(5));

      strategyPanel.add(new JLabel(MainFrame.getIcon("yellowBall")));
      strategyPanel.add(conditional_RunRBtn);
      strategyPanel.add(Box.createHorizontalStrut(5));

      strategyPanel.add(featureNameTextField);
      strategyPanel.add(Box.createHorizontalStrut(5));
      strategyPanel.add(new JLabel("is"));
      strategyPanel.add(Box.createHorizontalStrut(5));
      strategyPanel.add(featureValueTextField);
      strategyPanel.add(Box.createHorizontalStrut(5));
      strategyBorder = BorderFactory.createTitledBorder(
          BorderFactory.createEtchedBorder(),
          " No processing resource selected... ");
      strategyPanel.setBorder(strategyBorder);

      add(strategyPanel);
    }//if conditional mode
    if(analyserMode){
      //we need to add the corpus combo
      corpusCombo = new JComboBox(corpusComboModel = new CorporaComboModel());
      corpusCombo.setRenderer(new ResourceRenderer());
      corpusCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                                               corpusCombo.getPreferredSize().
                                               height));
      Corpus corpus = null;
      if(controller instanceof SerialAnalyserController){
        corpus = ((SerialAnalyserController)controller).getCorpus();
      }else if(controller instanceof ConditionalSerialAnalyserController){
        corpus = ((ConditionalSerialAnalyserController)controller).getCorpus();
      }else{
        throw new GateRuntimeException("Controller editor in analyser mode " +
                                       "but the target controller is not an " +
                                       "analyser!");
      }

      if(corpus != null){
        corpusCombo.setSelectedItem(corpus);
      }else{
        if(corpusCombo.getModel().getSize() > 1) corpusCombo.setSelectedIndex(1);
        else corpusCombo.setSelectedIndex(0);
      }
      JPanel horBox = new JPanel();
      horBox.setLayout(new BoxLayout(horBox, BoxLayout.X_AXIS));
      horBox.setAlignmentX(Component.LEFT_ALIGNMENT);
      horBox.add(new JLabel("Corpus:"));
      horBox.add(Box.createHorizontalStrut(5));
      horBox.add(corpusCombo);
      horBox.add(Box.createHorizontalStrut(5));
      horBox.add(Box.createHorizontalGlue());
      add(horBox);
      JLabel warningLbl = new JLabel(
        "<HTML>The <b>corpus</b> and <b>document</b> parameters are not " +
        "available as they are automatically set by the controller!</HTML>");
      warningLbl.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
      add(warningLbl);
    }

    parametersPanel = new JPanel();
    parametersPanel.setLayout(new BoxLayout(parametersPanel, BoxLayout.Y_AXIS));
    parametersPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    parametersBorder = BorderFactory.createTitledBorder(
                                      BorderFactory.createEtchedBorder(),
                                      " No selected processing resource ");
    parametersPanel.setBorder(parametersBorder);
    parametersEditor = new ResourceParametersEditor();
    parametersEditor.init(null, null);
    parametersPanel.add(new JScrollPane(parametersEditor));
    add(Box.createVerticalStrut(5));
    add(parametersPanel);


    add(Box.createVerticalStrut(5));
    add(Box.createVerticalGlue());
    JPanel horBox = new JPanel();
    horBox.setLayout(new BoxLayout(horBox, BoxLayout.X_AXIS));
    horBox.setAlignmentX(Component.LEFT_ALIGNMENT);
    horBox.add(Box.createHorizontalGlue());
    horBox.add(new JButton(runAction));
    horBox.add(Box.createHorizontalStrut(10));
    add(horBox);
    add(Box.createVerticalStrut(10));

    addMenu = new XJMenu("Add");
    removeMenu = new XJMenu("Remove");
  }// initGuiComponents()

  protected void initListeners() {
    Gate.getCreoleRegister().addCreoleListener(this);

    this.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if(SwingUtilities.isRightMouseButton(e)){
          if(handle != null && handle.getPopup()!= null)
            handle.getPopup().show(SerialControllerEditor.this, e.getX(), e.getY());
        }
      }
    });

    addButon.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int rows[] = loadedPRsTable.getSelectedRows();
        if(rows == null || rows.length == 0){
          JOptionPane.showMessageDialog(
              SerialControllerEditor.this,
              "Please select some components from the list of available components!\n" ,
              "GATE", JOptionPane.ERROR_MESSAGE);
        } else {
          List actions = new ArrayList();
          for(int i = 0; i < rows.length; i++) {
            Action act =(Action)new AddPRAction((ProcessingResource)
                                     loadedPRsTable.getValueAt(rows[i], 0));
            if(act != null) actions.add(act);
          }
          Iterator actIter = actions.iterator();
          while(actIter.hasNext()){
            ((Action)actIter.next()).actionPerformed(null);
          }
        }
      }
    });

    removeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int rows[] = memberPRsTable.getSelectedRows();
        if(rows == null || rows.length == 0){
          JOptionPane.showMessageDialog(
              SerialControllerEditor.this,
              "Please select some components to be removed "+
              "from the list of used components!\n" ,
              "GATE", JOptionPane.ERROR_MESSAGE);
        } else {
          List actions = new ArrayList();
          for(int i = 0; i < rows.length; i++){
            Action act =(Action)new RemovePRAction((ProcessingResource)
                                     memberPRsTable.getValueAt(rows[i], 1));
            if(act != null) actions.add(act);
          }
          Iterator actIter = actions.iterator();
          while(actIter.hasNext()){
            ((Action)actIter.next()).actionPerformed(null);
          }
        }// else
      }//  public void actionPerformed(ActionEvent e)
    });

    moveUpButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int rows[] = memberPRsTable.getSelectedRows();
        if(rows == null || rows.length == 0){
          JOptionPane.showMessageDialog(
              SerialControllerEditor.this,
              "Please select some components to be moved "+
              "from the list of used components!\n" ,
              "GATE", JOptionPane.ERROR_MESSAGE);
        } else {
          //we need to make sure the rows are sorted
          Arrays.sort(rows);
          //get the list of PRs
          for(int i = 0; i < rows.length; i++){
            int row = rows[i];
            if(row > 0){
              //move it up
              ProcessingResource value = controller.remove(row);
              controller.add(row - 1, value);
            }
          }
//          memberPRsTableModel.fireTableDataChanged();
          //restore selection
          for(int i = 0; i < rows.length; i++){
            int newRow = -1;
            if(rows[i] > 0) newRow = rows[i] - 1;
            else newRow = rows[i];
            memberPRsTable.addRowSelectionInterval(newRow, newRow);
          }
        }

      }//public void actionPerformed(ActionEvent e)
    });


    moveDownButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int rows[] = memberPRsTable.getSelectedRows();
        if(rows == null || rows.length == 0){
          JOptionPane.showMessageDialog(
              SerialControllerEditor.this,
              "Please select some components to be moved "+
              "from the list of used components!\n" ,
              "GATE", JOptionPane.ERROR_MESSAGE);
        } else {
          //we need to make sure the rows are sorted
          Arrays.sort(rows);
          //get the list of PRs
          for(int i = rows.length - 1; i >= 0; i--){
            int row = rows[i];
            if(row < controller.getPRs().size() -1){
              //move it down
              ProcessingResource value = controller.remove(row);
              controller.add(row + 1, value);
            }
          }
//          memberPRsTableModel.fireTableDataChanged();
          //restore selection
          for(int i = 0; i < rows.length; i++){
            int newRow = -1;
            if(rows[i] < controller.getPRs().size() - 1) newRow = rows[i] + 1;
            else newRow = rows[i];
            memberPRsTable.addRowSelectionInterval(newRow, newRow);
          }
        }

      }//public void actionPerformed(ActionEvent e)
    });

    loadedPRsTable.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        int row = loadedPRsTable.rowAtPoint(e.getPoint());
        //load modules on double click
        ProcessingResource pr = (ProcessingResource)
                                loadedPRsTableModel.getValueAt(row, 0);
        if(SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2){
          new AddPRAction(pr).actionPerformed(null);
        }else if(SwingUtilities.isRightMouseButton(e)){
            JPopupMenu popup = new XJPopupMenu();
            popup.add(new AddPRAction(pr){
              {
                putValue(NAME, "Add \"" + this.pr.getName() +
                               "\" to the \"" + controller.getName() +
                               "\" application");
              }
            });
            popup.show(loadedPRsTable, e.getPoint().x, e.getPoint().y);
          }
      }

      public void mousePressed(MouseEvent e) {
      }

      public void mouseReleased(MouseEvent e) {
      }

      public void mouseEntered(MouseEvent e) {
      }

      public void mouseExited(MouseEvent e) {
      }
    });

    memberPRsTable.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        final int row = memberPRsTable.rowAtPoint(e.getPoint());
        if(row != -1){
          //edit parameters on click
          if(SwingUtilities.isLeftMouseButton(e) /*&& e.getClickCount() == 2*/){
            ProcessingResource pr = (ProcessingResource)
                                    memberPRsTableModel.getValueAt(row, 1);
            selectPR(row);
          }else if(SwingUtilities.isRightMouseButton(e)){
            JPopupMenu popup = new XJPopupMenu();
            popup.add(new AbstractAction("Edit parameters"){
              public void actionPerformed(ActionEvent e){
                ProcessingResource pr = (ProcessingResource)
                                        memberPRsTableModel.getValueAt(row, 1);
                selectPR(row);
              }
            });
            popup.show(memberPRsTable, e.getPoint().x, e.getPoint().y);
          }
        }
      }

      public void mousePressed(MouseEvent e) {
      }

      public void mouseReleased(MouseEvent e) {
      }

      public void mouseEntered(MouseEvent e) {
      }

      public void mouseExited(MouseEvent e) {
      }
    });

    addMenu.addMenuListener(new MenuListener() {
      public void menuCanceled(MenuEvent e) {

      }

      public void menuDeselected(MenuEvent e) {
      }

      public void menuSelected(MenuEvent e) {
        buildInternalMenus();
      }
    });

    removeMenu.addMenuListener(new MenuListener() {
      public void menuCanceled(MenuEvent e) {
      }

      public void menuDeselected(MenuEvent e) {
      }

      public void menuSelected(MenuEvent e) {
        buildInternalMenus();
      }
    });

    if(conditionalMode){
      final ActionListener executionModeActionListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if(selectedPRRunStrategy != null &&
             selectedPRRunStrategy instanceof AnalyserRunningStrategy){
            AnalyserRunningStrategy strategy =
              (AnalyserRunningStrategy)selectedPRRunStrategy;
            if(yes_RunRBtn.isSelected()){
              strategy.setRunMode(RunningStrategy.RUN_ALWAYS);
              featureNameTextField.setEditable(false);
              featureValueTextField.setEditable(false);
            }else if(no_RunRBtn.isSelected()){
              strategy.setRunMode(RunningStrategy.RUN_NEVER);
              featureNameTextField.setEditable(false);
              featureValueTextField.setEditable(false);
            }else if(conditional_RunRBtn.isSelected()){
              strategy.setRunMode(RunningStrategy.RUN_CONDITIONAL);
              featureNameTextField.setEditable(true);
              featureValueTextField.setEditable(true);

              String str = featureNameTextField.getText();
              strategy.setFeatureName(str == null || str.length()==0 ?
                                      null : str);
              str = featureValueTextField.getText();
              strategy.setFeatureValue(str == null || str.length()==0 ?
                                      null : str);
            }
          }
          memberPRsTable.repaint();
        }
      };

      yes_RunRBtn.addActionListener(executionModeActionListener);

      no_RunRBtn.addActionListener(executionModeActionListener);

      conditional_RunRBtn.addActionListener(executionModeActionListener);

      featureNameTextField.getDocument().addDocumentListener(
      new javax.swing.event.DocumentListener() {
        public void insertUpdate(javax.swing.event.DocumentEvent e) {
          changeOccured(e);
        }

        public void removeUpdate(javax.swing.event.DocumentEvent e) {
          changeOccured(e);
        }

        public void changedUpdate(javax.swing.event.DocumentEvent e) {
          changeOccured(e);
        }

        protected void changeOccured(javax.swing.event.DocumentEvent e){
          if(selectedPRRunStrategy != null &&
             selectedPRRunStrategy instanceof AnalyserRunningStrategy){
            AnalyserRunningStrategy strategy =
              (AnalyserRunningStrategy)selectedPRRunStrategy;
            strategy.setFeatureName(featureNameTextField.getText());
          }
        }
      });

      featureValueTextField.getDocument().addDocumentListener(
      new javax.swing.event.DocumentListener() {
        public void insertUpdate(javax.swing.event.DocumentEvent e) {
          changeOccured(e);
        }

        public void removeUpdate(javax.swing.event.DocumentEvent e) {
          changeOccured(e);
        }

        public void changedUpdate(javax.swing.event.DocumentEvent e) {
          changeOccured(e);
        }

        protected void changeOccured(javax.swing.event.DocumentEvent e){
          if(selectedPRRunStrategy != null &&
             selectedPRRunStrategy instanceof AnalyserRunningStrategy){
            AnalyserRunningStrategy strategy =
              (AnalyserRunningStrategy)selectedPRRunStrategy;
            strategy.setFeatureValue(featureValueTextField.getText());
          }
        }
      });
    }//if conditional
    if(analyserMode){
      corpusCombo.addPopupMenuListener(new PopupMenuListener() {
                    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                      corpusComboModel.fireDataChanged();
                    }

                    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                    }

                    public void popupMenuCanceled(PopupMenuEvent e) {
                    }
                  });
    }
  }//protected void initListeners()


  public List getActions(){
    return actionList;
  }

  /**
   * Cleans the internal data and prepares this object to be collected
   */
  public void cleanup(){
    Gate.getCreoleRegister().removeCreoleListener(this);
    controller.removeControllerListener(this);
    controller = null;
    progressListeners.clear();
    statusListeners.clear();
    parametersEditor.cleanup();
    addMenu.removeAll();
    removeMenu.removeAll();
    handle = null;
  }

  protected void buildInternalMenus(){
    addMenu.removeAll();
    Iterator prIter = Gate.getCreoleRegister().getPrInstances().iterator();
    while(prIter.hasNext()){
      ProcessingResource pr = (ProcessingResource)prIter.next();
      if(Gate.getHiddenAttribute(pr.getFeatures())){
        //ignore this resource
      }else{
        Action act = new AddPRAction(pr);
        if(act.isEnabled()) addMenu.add(act);
      }
    }// while

    removeMenu.removeAll();
    prIter = Gate.getCreoleRegister().getPrInstances().iterator();
    while(prIter.hasNext()){
      ProcessingResource pr = (ProcessingResource)prIter.next();
      if(Gate.getHiddenAttribute(pr.getFeatures())){
        //ignore this resource
      }else{
        Action act = new RemovePRAction(pr);
        if(act.isEnabled()) removeMenu.add(act);
      }
    }// while
  }

  /**
   * Called when a PR has been selected in the memeber PRs table;
   */
  protected void selectPR(int index){
    ProcessingResource pr = (ProcessingResource)
                            ((java.util.List)controller.getPRs()).get(index);
    showParamsEditor(pr);
    selectedPR = pr;
    if(conditionalMode){
      strategyBorder.setTitle(" Run \"" + pr.getName() + "\"? ");
      //update the state of the run strategy buttons
      selectedPRRunStrategy = (RunningStrategy)
                                 ((List)((ConditionalController)controller).
                                          getRunningStrategies()).get(index);
      int runMode = selectedPRRunStrategy.getRunMode();

      if(selectedPRRunStrategy instanceof AnalyserRunningStrategy){
        yes_RunRBtn.setEnabled(true);
        no_RunRBtn.setEnabled(true);
        conditional_RunRBtn.setEnabled(true);

        featureNameTextField.setText(
              ((AnalyserRunningStrategy)selectedPRRunStrategy).
              getFeatureName());
        featureValueTextField.setText(
              ((AnalyserRunningStrategy)selectedPRRunStrategy).
              getFeatureValue());
      }else{
        yes_RunRBtn.setEnabled(false);
        no_RunRBtn.setEnabled(false);
        conditional_RunRBtn.setEnabled(false);

        featureNameTextField.setText("");
        featureValueTextField.setText("");
      }

      featureNameTextField.setEditable(false);
      featureValueTextField.setEditable(false);

      switch(selectedPRRunStrategy.getRunMode()){
        case RunningStrategy.RUN_ALWAYS:{
          yes_RunRBtn.setSelected(true);
          break;
        }

        case RunningStrategy.RUN_NEVER:{
          no_RunRBtn.setSelected(true);
          break;
        }

        case RunningStrategy.RUN_CONDITIONAL:{
          conditional_RunRBtn.setSelected(true);
          if(selectedPRRunStrategy instanceof AnalyserRunningStrategy){
            featureNameTextField.setEditable(true);
            featureValueTextField.setEditable(true);
          }
          break;
        }
      }//switch
    }
  }

  /**
   * Stops the current edits for parameters; sets the paarmeters for the
   * resource currently being edited and diplays the editor for the new
   * resource
   * @param pr the new resource
   */
  protected void showParamsEditor(ProcessingResource pr){
    try{
      if(parametersEditor.getResource() != null) parametersEditor.setParameters();
    }catch(ResourceInstantiationException rie){
      JOptionPane.showMessageDialog(
          SerialControllerEditor.this,
          "Failed to set parameters for \"" + pr.getName() +"\"!\n" ,
          "GATE", JOptionPane.ERROR_MESSAGE);
      rie.printStackTrace(Err.getPrintWriter());
    }

    if(pr != null){
      ResourceData rData = (ResourceData)Gate.getCreoleRegister().
                                         get(pr.getClass().getName());

      parametersBorder.setTitle(" Parameters for the \"" + pr.getName() +
                                "\" " + rData.getName() + " ");

      //this is a list of lists
      List parameters = rData.getParameterList().getRuntimeParameters();

      if(analyserMode){
        //remove corpus and document
        //create a new list so we don't change the one from CreoleReg.
        List newParameters = new ArrayList();
        Iterator pDisjIter = parameters.iterator();
        while(pDisjIter.hasNext()){
          List aDisjunction = (List)pDisjIter.next();
          List newDisjunction = new ArrayList(aDisjunction);
          Iterator internalParIter = newDisjunction.iterator();
          while(internalParIter.hasNext()){
            Parameter parameter = (Parameter)internalParIter.next();
            if(parameter.getName().equals("corpus") ||
               parameter.getName().equals("document")) internalParIter.remove();
          }
          if(!newDisjunction.isEmpty()) newParameters.add(newDisjunction);
        }
        parametersEditor.init(pr, newParameters);
      }else{
        parametersEditor.init(pr, parameters);
      }
    }else{
      parametersBorder.setTitle("No selected processing resource");
      parametersEditor.init(null, null);
    }
    SerialControllerEditor.this.validate();
    SerialControllerEditor.this.repaint(100);
  }

  //CreoleListener implementation
  public void resourceLoaded(CreoleEvent e) {
    if(Gate.getHiddenAttribute(e.getResource().getFeatures())) return;
    if(e.getResource() instanceof ProcessingResource){
      loadedPRsTableModel.fireTableDataChanged();
      memberPRsTableModel.fireTableDataChanged();
//      repaint(100);
    }else if(e.getResource() instanceof LanguageResource){
      if(e.getResource() instanceof Corpus && analyserMode){
        corpusComboModel.fireDataChanged();
      }
    }
  }// public void resourceLoaded

  public void resourceUnloaded(CreoleEvent e) {
    if(Gate.getHiddenAttribute(e.getResource().getFeatures())) return;
    if(e.getResource() instanceof ProcessingResource){
      ProcessingResource pr = (ProcessingResource)e.getResource();
      if(controller.getPRs().contains(pr)){
        new RemovePRAction(pr).actionPerformed(null);
      }
      loadedPRsTableModel.fireTableDataChanged();
      memberPRsTableModel.fireTableDataChanged();
//      repaint(100);
    }
    else if(e.getResource() instanceof LanguageResource) {
      if(e.getResource() instanceof Corpus && analyserMode) {
        Corpus c = (Corpus)e.getResource();
        if(controller instanceof CorpusController) {
          if(c == ((CorpusController)controller).getCorpus()) {
            ((CorpusController)controller).setCorpus(null);
          }
        }
        else {
          throw new GateRuntimeException("Controller editor in analyser mode " +
                                         "but the target controller is not an " +
                                         "analyser!");
        }
        corpusComboModel.fireDataChanged();
      }
    }
  }//public void resourceUnloaded(CreoleEvent e)

  public void resourceRenamed(Resource resource, String oldName,
                              String newName){
    if(Gate.getHiddenAttribute(resource.getFeatures())) return;
    if(resource instanceof ProcessingResource){
      repaint(100);
    }
  }

  public void datastoreOpened(CreoleEvent e) {
  }
  public void datastoreCreated(CreoleEvent e) {
  }
  public void datastoreClosed(CreoleEvent e) {
  }
  public synchronized void removeStatusListener(StatusListener l) {
    if (statusListeners != null && statusListeners.contains(l)) {
      Vector v = (Vector) statusListeners.clone();
      v.removeElement(l);
      statusListeners = v;
    }
  }
  
  /* (non-Javadoc)
   * @see gate.event.ControllerListener#resourceAdded(gate.event.ControllerEvent)
   */
  public void resourceAdded(ControllerEvent evt){
    loadedPRsTableModel.fireTableDataChanged();
    memberPRsTableModel.fireTableDataChanged();

  }
  
  /* (non-Javadoc)
   * @see gate.event.ControllerListener#resourceRemoved(gate.event.ControllerEvent)
   */
  public void resourceRemoved(ControllerEvent evt){
    loadedPRsTableModel.fireTableDataChanged();
    memberPRsTableModel.fireTableDataChanged();
  }
  
  
  
  public synchronized void addStatusListener(StatusListener l) {
    Vector v = statusListeners == null ? new Vector(2) :
                                  (Vector) statusListeners.clone();
    if (!v.contains(l)) {
      v.addElement(l);
      statusListeners = v;
    }
  }



  /**
   * Table model for all the loaded processing resources that are not part of
   * the controller.
   */
  class LoadedPRsTableModel extends AbstractTableModel{
    public int getRowCount(){
      List loadedPRs = new ArrayList(Gate.getCreoleRegister().getPrInstances());
      if(controller != null) loadedPRs.removeAll(controller.getPRs());
      Iterator prsIter = loadedPRs.iterator();
      while(prsIter.hasNext()){
        ProcessingResource aPR = (ProcessingResource)prsIter.next();
        if(Gate.getHiddenAttribute(aPR.getFeatures())) prsIter.remove();
      }

      return loadedPRs.size();
    }

    public Object getValueAt(int row, int column){
      List loadedPRs = new ArrayList(Gate.getCreoleRegister().getPrInstances());
      if(controller != null) loadedPRs.removeAll(controller.getPRs());
      Iterator prsIter = loadedPRs.iterator();
      while(prsIter.hasNext()){
        ProcessingResource aPR = (ProcessingResource)prsIter.next();
        if(Gate.getHiddenAttribute(aPR.getFeatures())) prsIter.remove();
      }

      Collections.sort(loadedPRs, nameComparator);
      ProcessingResource pr = (ProcessingResource)loadedPRs.get(row);
      switch(column){
        case 0 : return pr;
        case 1 : {
          ResourceData rData = (ResourceData)Gate.getCreoleRegister().
                                    get(pr.getClass().getName());
          if(rData == null) return pr.getClass();
          else return rData.getName();
        }
        default: return null;
      }
    }

    public int getColumnCount(){
      return 2;
    }

    public String getColumnName(int columnIndex){
      switch(columnIndex){
        case 0 : return "Name";
        case 1 : return "Type";
        default: return "?";
      }
    }

    public Class getColumnClass(int columnIndex){
      switch(columnIndex){
        case 0 : return ProcessingResource.class;
        case 1 : return String.class;
        default: return Object.class;
      }
    }

    public boolean isCellEditable(int rowIndex, int columnIndex){
      return false;
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex){
    }
    NameComparator nameComparator = new NameComparator();
  }//protected class LoadedPRsTableModel extends AbstractTableModel

  /**
   * A model for a combobox containing the loaded corpora in the system
   */
  protected class CorporaComboModel extends AbstractListModel
                                  implements ComboBoxModel{
    public int getSize(){
      //get all corpora regardless of their actual type
      java.util.List loadedCorpora = null;
      try{
        loadedCorpora = Gate.getCreoleRegister().
                               getAllInstances("gate.Corpus");
      }catch(GateException ge){
        ge.printStackTrace(Err.getPrintWriter());
      }

      return loadedCorpora == null ? 1 : loadedCorpora.size() + 1;
    }

    public Object getElementAt(int index){
      if(index == 0) return "<none>";
      else{
        //get all corpora regardless of their actual type
        java.util.List loadedCorpora = null;
        try{
          loadedCorpora = Gate.getCreoleRegister().
                                 getAllInstances("gate.Corpus");
        }catch(GateException ge){
          ge.printStackTrace(Err.getPrintWriter());
        }
        return loadedCorpora == null? "" : loadedCorpora.get(index - 1);
      }
    }

    //use the controller for data caching
    public void setSelectedItem(Object anItem){
      if(controller instanceof SerialAnalyserController)
      ((SerialAnalyserController)controller).
        setCorpus((Corpus)(anItem.equals("<none>") ? null : anItem));
      else if(controller instanceof ConditionalSerialAnalyserController)
      ((ConditionalSerialAnalyserController)controller).
        setCorpus((Corpus)(anItem.equals("<none>") ? null : anItem));
    }

    public Object getSelectedItem(){
      Corpus corpus = null;
      if(controller instanceof SerialAnalyserController){
        corpus = ((SerialAnalyserController)controller).getCorpus();
      }else if(controller instanceof ConditionalSerialAnalyserController){
        corpus = ((ConditionalSerialAnalyserController)controller).getCorpus();
      }else{
        throw new GateRuntimeException("Controller editor in analyser mode " +
                                       "but the target controller is not an " +
                                       "analyser!");
      }
      return (corpus == null ? (Object)"<none>" : (Object)corpus);
    }

    void fireDataChanged(){
      fireContentsChanged(this, 0, getSize());
    }
  }

  /**
   *  Renders JLabel by simply displaying them
   */
  class LabelRenderer implements TableCellRenderer{
    public Component getTableCellRendererComponent(JTable table,
                                                   Object value,
                                                   boolean isSelected,
                                                   boolean hasFocus,
                                                   int row,
                                                   int column){
      return (JLabel) value;
    }
  }

  /**
   * Table model for all the processing resources in the controller.
   */
  class MemberPRsTableModel extends AbstractTableModel{
    MemberPRsTableModel(){
      green = new JLabel(MainFrame.getIcon("greenBall"));
      red = new JLabel(MainFrame.getIcon("redBall"));
      yellow = new JLabel(MainFrame.getIcon("yellowBall"));
    }
    public int getRowCount(){
      return controller == null ? 0 : controller.getPRs().size();
    }

    public Object getValueAt(int row, int column){
      ProcessingResource pr = (ProcessingResource)
                              ((List)controller.getPRs()).get(row);
      switch(column){
        case 0 : {
          if(conditionalMode){
            RunningStrategy strategy = (RunningStrategy)
                                 ((List)((ConditionalController)controller).
                                          getRunningStrategies()).get(row);
            switch(strategy.getRunMode()){
              case RunningStrategy.RUN_ALWAYS : return green;
              case RunningStrategy.RUN_NEVER : return red;
              case RunningStrategy.RUN_CONDITIONAL : return yellow;
            }
          }
          return green;
        }
        case 1 : return pr;
        case 2 : {
          ResourceData rData = (ResourceData)Gate.getCreoleRegister().
                                    get(pr.getClass().getName());
          if(rData == null) return pr.getClass();
          else return rData.getName();
        }
        default: return null;
      }
    }

    public int getColumnCount(){
      return 3;
    }

    public String getColumnName(int columnIndex){
      switch(columnIndex){
        case 0 : return "!";
        case 1 : return "Name";
//        case 1 : return "!";
        case 2 : return "Type";
        default: return "?";
      }
    }

    public Class getColumnClass(int columnIndex){
      switch(columnIndex){
        case 0 : return JLabel.class;
        case 1 : return ProcessingResource.class;
//        case 1 : return Boolean.class;
        case 2 : return String.class;
        default: return Object.class;
      }
    }

    public boolean isCellEditable(int rowIndex, int columnIndex){
      return false;
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex){
    }

    protected JLabel green, red, yellow;
  }//protected class MemeberPRsTableModel extends AbstractTableModel

  /** Adds a PR to the controller*/
  class AddPRAction extends AbstractAction {
    AddPRAction(ProcessingResource aPR){
      super(aPR.getName());
      this.pr = aPR;
      setEnabled(!controller.getPRs().contains(aPR));
    }

    public void actionPerformed(ActionEvent e){
      controller.add(pr);
//      loadedPRsTableModel.fireTableDataChanged();
//      memberPRsTableModel.fireTableDataChanged();
//      SerialControllerEditor.this.validate();
//      SerialControllerEditor.this.repaint(100);
    }

    ProcessingResource pr;
  }

  /** Removes a PR from the controller*/
  class RemovePRAction extends AbstractAction {
    RemovePRAction(ProcessingResource pr){
      super(pr.getName());
      this.pr = pr;
      setEnabled(controller.getPRs().contains(pr));
    }

    public void actionPerformed(ActionEvent e){
      if(controller.remove(pr)){
//        loadedPRsTableModel.fireTableDataChanged();
//        memberPRsTableModel.fireTableDataChanged();
        if(parametersEditor.getResource() == pr){
          parametersEditor.init(null, null);
          parametersBorder.setTitle("No selected processing resource");
        }
//        SerialControllerEditor.this.validate();
//        SerialControllerEditor.this.repaint(100);
      }
    }

    ProcessingResource pr;
  }


  /** Runs the Application*/
  class RunAction extends AbstractAction {
    RunAction(){
      super("Run");
    }

    public void actionPerformed(ActionEvent e){
      Runnable runnable = new Runnable(){
        public void run(){
          //stop editing the parameters
          try{
            parametersEditor.setParameters();
          }catch(ResourceInstantiationException rie){
            JOptionPane.showMessageDialog(
              SerialControllerEditor.this,
              "Could not set parameters for the \"" +
              parametersEditor.getResource().getName() +
              "\" processing resource:\nSee \"Messages\" tab for details!",
              "GATE", JOptionPane.ERROR_MESSAGE);
              rie.printStackTrace(Err.getPrintWriter());
              return;
          }

          if(analyserMode){
            //set the corpus
            Object value = corpusCombo.getSelectedItem();
            Corpus corpus = value.equals("<none>") ? null : (Corpus)value;
            if(corpus == null){
              JOptionPane.showMessageDialog(
                SerialControllerEditor.this,
                "No corpus provided!\n" +
                "Please select a corpus and try again!",
                "GATE", JOptionPane.ERROR_MESSAGE);
              return;
            }
            if(controller instanceof SerialAnalyserController)
              ((SerialAnalyserController)controller).setCorpus(corpus);
            else if(controller instanceof ConditionalSerialAnalyserController)
              ((ConditionalSerialAnalyserController)controller).setCorpus(corpus);
          }
          //check the runtime parameters
          List badPRs;
          try{
            badPRs = controller.getOffendingPocessingResources();
          }catch(ResourceInstantiationException rie){
            JOptionPane.showMessageDialog(
              SerialControllerEditor.this,
              "Could not check runtime parameters for " +
              "the processing resources:\n" + rie.toString(),
              "GATE", JOptionPane.ERROR_MESSAGE);
            return;
          }
          if(badPRs != null && !badPRs.isEmpty()){
            //we know what PRs have problems so it would be nice to show
            //them in red or something
            JOptionPane.showMessageDialog(
              SerialControllerEditor.this,
              "Some required runtime parameters are not set!",
              "GATE", JOptionPane.ERROR_MESSAGE);
            return;
          }

          //set the listeners
          StatusListener sListener = new InternalStatusListener();
          ProgressListener pListener = new InternalProgressListener();

          controller.addStatusListener(sListener);
          controller.addProgressListener(pListener);

          Gate.setExecutable(controller);

          MainFrame.lockGUI("Running " + controller.getName() + "...");
          //execute the thing
          long startTime = System.currentTimeMillis();
          fireStatusChanged("Running " +
                            controller.getName());
          fireProgressChanged(0);

          try {
            controller.execute();
          }catch(ExecutionInterruptedException eie){
            MainFrame.unlockGUI();
            JOptionPane.showMessageDialog(
              SerialControllerEditor.this,
              "Interrupted!\n" + eie.toString(),
              "GATE", JOptionPane.ERROR_MESSAGE);
          }catch(ExecutionException ee) {
            ee.printStackTrace(Err.getPrintWriter());
            MainFrame.unlockGUI();
            JOptionPane.showMessageDialog(
              SerialControllerEditor.this,
              "Execution error while running \"" + controller.getName() +
              "\" :\nSee \"Messages\" tab for details!",
              "GATE", JOptionPane.ERROR_MESSAGE);
          }catch(Exception e){
            MainFrame.unlockGUI();
            JOptionPane.showMessageDialog(SerialControllerEditor.this,
                                          "Unhandled execution error!\n " +
                                          "See \"Messages\" tab for details!",
                                          "GATE", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace(Err.getPrintWriter());
          }finally{
            MainFrame.unlockGUI();
            Gate.setExecutable(null);
          }//catch

          //remove the listeners
          controller.removeStatusListener(sListener);
          controller.removeProgressListener(pListener);

          long endTime = System.currentTimeMillis();
          fireProcessFinished();
          fireStatusChanged(controller.getName() +
                            " run in " +
                            NumberFormat.getInstance().format(
                            (double)(endTime - startTime) / 1000) + " seconds");
        }
      };
      Thread thread = new Thread(Thread.currentThread().getThreadGroup(),
                                 runnable,
                                 "ApplicationViewer1");
      thread.setPriority(Thread.MIN_PRIORITY);
      thread.start();
    }//public void actionPerformed(ActionEvent e)
  }//class RunAction

  /**
   * A simple progress listener used to forward the events upstream.
   */
  protected class InternalProgressListener implements ProgressListener{
    public void progressChanged(int i){
      fireProgressChanged(i);
    }

    public void processFinished(){
      fireProcessFinished();
    }
  }//InternalProgressListener

  /**
   * A simple status listener used to forward the events upstream.
   */
  protected class InternalStatusListener implements StatusListener{
    public void statusChanged(String message){
      fireStatusChanged(message);
    }
  }//InternalStatusListener



  /** The controller this editor edits */
  protected SerialController controller;

  /** The {@link Handle} that created this view */
  protected Handle handle;

  /**
   * The list of actions provided by this editor
   */
  protected List actionList;
  /**
   * Contains all the PRs loaded in the sytem that are not already part of the
   * serial controller
   */
  protected XJTable loadedPRsTable;

  /**
   * model for the {@link #loadedPRsTable} JTable.
   */
  protected LoadedPRsTableModel loadedPRsTableModel;

  /**
   * Displays the PRs in the controller
   */
  protected XJTable memberPRsTable;

  /** model for {@link #memberPRsTable}*/
  protected MemberPRsTableModel memberPRsTableModel;

  /** Adds one or more PR(s) to the controller*/
  protected JButton addButon;

  /** Removes one or more PR(s) from the controller*/
  protected JButton removeButton;

  /** Moves the module up in the controller list*/
  protected JButton moveUpButton;

  /** Moves the module down in the controller list*/
  protected JButton moveDownButton;

  /** A component for editing the parameters of the currently selected PR*/
  protected ResourceParametersEditor parametersEditor;

  /** A JPanel containing the {@link #parametersEditor}*/
  protected JPanel parametersPanel;

  /** A border for the {@link #parametersPanel} */
  protected TitledBorder parametersBorder;


  /** A JPanel containing the running strategy options*/
  protected JPanel strategyPanel;

  /** A border for the running strategy options box */
  protected TitledBorder strategyBorder;

  /**
   * Button for run always.
   */
  protected JRadioButton yes_RunRBtn;

  /**
   * Button for never run.
   */
  protected JRadioButton no_RunRBtn;

  /**
   * Button for conditional run.
   */
  protected JRadioButton conditional_RunRBtn;

  /**
   * The group for run strategy buttons;
   */
  protected ButtonGroup runBtnGrp;

  /**
   * Text field for the feature name for conditional run.
   */
  protected JTextField featureNameTextField;

  /**
   * Text field for the feature value for conditional run.
   */
  protected JTextField featureValueTextField;

  /**
   * A combobox that allows selection of a corpus from the list of loaded
   * corpora.
   */
  protected JComboBox corpusCombo;

  protected CorporaComboModel corpusComboModel;

  /**The "Add PR" menu; part of the popup menu*/
  protected JMenu addMenu;

  /**The "Remove PR" menu; part of the popup menu*/
  protected JMenu removeMenu;

  /** Action that runs the application*/
  protected RunAction runAction;

  /**
   * Is the controller displayed an analyser controller?
   */
  protected boolean analyserMode = false;

  /**
   * Is the controller displayed conditional?
   */
  protected boolean conditionalMode = false;

  /**
   * The PR currently selected (having its parameters set)
   */
  protected ProcessingResource selectedPR = null;

  /**
   * The running strategy for the selected PR.
   */
  protected RunningStrategy selectedPRRunStrategy = null;

  private transient Vector statusListeners;
  private transient Vector progressListeners;



  protected void fireStatusChanged(String e) {
    if (statusListeners != null) {
      Vector listeners = statusListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((StatusListener) listeners.elementAt(i)).statusChanged(e);
      }
    }
  }
  public synchronized void removeProgressListener(ProgressListener l) {
    if (progressListeners != null && progressListeners.contains(l)) {
      Vector v = (Vector) progressListeners.clone();
      v.removeElement(l);
      progressListeners = v;
    }
  }
  public synchronized void addProgressListener(ProgressListener l) {
    Vector v = progressListeners == null ? new Vector(2) : (Vector) progressListeners.clone();
    if (!v.contains(l)) {
      v.addElement(l);
      progressListeners = v;
    }
  }
  protected void fireProgressChanged(int e) {
    if (progressListeners != null) {
      Vector listeners = progressListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((ProgressListener) listeners.elementAt(i)).progressChanged(e);
      }
    }
  }
  protected void fireProcessFinished() {
    if (progressListeners != null) {
      Vector listeners = progressListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((ProgressListener) listeners.elementAt(i)).processFinished();
      }
    }
  }
  }//SerialControllerEditor
