/*
 *  Copyright (c) 1995-2010, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Niraj Aswani, 09/March/07
 *
 *  $Id: ObjectPropertyAction.html,v 1.0 2007/03/09 16:13:01 niraj Exp $
 */
package gate.gui.ontology;

import gate.creole.ontology.*;
import gate.gui.MainFrame;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Action to create a new ObjectProperty in the ontology
 */
public class ObjectPropertyAction extends AbstractAction implements
                                                        TreeNodeSelectionListener {
  private static final long serialVersionUID = 3689632475823551285L;

  public ObjectPropertyAction(String s, Icon icon) {
    super(s, icon);

    mainPanel = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(3, 3, 3, 3);
    gbc.anchor = GridBagConstraints.WEST;

    mainPanel.add(new JLabel("Name Space:"), gbc);
    mainPanel.add(nameSpace = new JTextField(30), gbc);

    gbc.gridy = 1;
    mainPanel.add(new JLabel("Property Name:"), gbc);
    mainPanel.add(propertyName = new JTextField(30), gbc);
    mainPanel.add(domainButton = new JButton("Domain"), gbc);
    mainPanel.add(rangeButton = new JButton("Range"), gbc);

    domainAction = new ValuesSelectionAction();
    domainButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionevent) {
        String as[] = new String[ontologyClassesURIs.size()];
        for(int i = 0; i < as.length; i++)
          as[i] = ontologyClassesURIs.get(i);
        ArrayList<String> arraylist = new ArrayList<String>();
        for(int j = 0; j < selectedNodes.size(); j++) {
          DefaultMutableTreeNode defaultmutabletreenode = selectedNodes.get(j);
          if(((OResourceNode)defaultmutabletreenode.getUserObject())
                  .getResource() instanceof OClass)
            arraylist.add((((OResourceNode)defaultmutabletreenode
                    .getUserObject()).getResource()).getURI().toString());
        }
        String as1[] = new String[arraylist.size()];
        for(int k = 0; k < as1.length; k++)
          as1[k] = arraylist.get(k);
        domainAction.showGUI("Domain", as, as1, false,
          MainFrame.getIcon("ontology-object-property"));
      }
    });
    rangeAction = new ValuesSelectionAction();
    rangeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionevent) {
        String as[] = new String[ontologyClassesURIs.size()];
        for(int i = 0; i < as.length; i++)
          as[i] = ontologyClassesURIs.get(i);
        rangeAction.showGUI("Range", as, new String[0], false,
          MainFrame.getIcon("ontology-object-property"));
      }
    });
  }

  public void actionPerformed(ActionEvent actionevent) {
    nameSpace.setText(ontology.getDefaultNameSpace() == null ?
      "http://gate.ac.uk/example#" : ontology.getDefaultNameSpace());
    JOptionPane pane = new JOptionPane(mainPanel, JOptionPane.QUESTION_MESSAGE,
      JOptionPane.OK_CANCEL_OPTION,
      MainFrame.getIcon("ontology-object-property")) {
      public void selectInitialValue() {
        propertyName.requestFocusInWindow();
        propertyName.selectAll();
      }
    };
    pane.createDialog(MainFrame.getInstance(),
      "New Object Property").setVisible(true);
    Object selectedValue = pane.getValue();
    if (selectedValue != null
    && selectedValue instanceof Integer
    && (Integer) selectedValue == JOptionPane.OK_OPTION) {
      String s = nameSpace.getText();
      if(!Utils.isValidNameSpace(s)) {
        JOptionPane.showMessageDialog(MainFrame.getInstance(),
          "Invalid Name Space: " + s + "\nExample: http://gate.ac.uk/example#");
        return;
      }
      if(!Utils.isValidOntologyResourceName(propertyName.getText())) {
        JOptionPane.showMessageDialog(MainFrame.getInstance(),
          "Invalid Property Name: " + propertyName.getText());
        return;
      }
      if(Utils.getOResourceFromMap(ontology,s + propertyName.getText()) != null) {
        JOptionPane.showMessageDialog(MainFrame.getInstance(),"<html>" +
          "Resource <b>" + s+propertyName.getText() + "</b> already exists.");
        return;
      }
      String domainSelectedValues[] = domainAction.getSelectedValues();
      HashSet<OClass> domainSet = new HashSet<OClass>();
      for (String domainSelectedValue : domainSelectedValues) {
        OClass oclass = (OClass)
          Utils.getOResourceFromMap(ontology,domainSelectedValue);
        domainSet.add(oclass);
      }
      String rangeSelectedValues[] = rangeAction.getSelectedValues();
      HashSet<OClass> rangeSet = new HashSet<OClass>();
      for (String rangeSelectedValue : rangeSelectedValues) {
        OClass oclass = (OClass)
          Utils.getOResourceFromMap(ontology,rangeSelectedValue);
        rangeSet.add(oclass);
      }
      ontology.addObjectProperty(new URI(nameSpace.getText()
        + propertyName.getText(), false), domainSet, rangeSet);
    }
  }

  public Ontology getOntology() {
    return ontology;
  }

  public void setOntology(Ontology ontology) {
    this.ontology = ontology;
  }

  public void selectionChanged(ArrayList<DefaultMutableTreeNode> arraylist) {
    selectedNodes = arraylist;
  }

  public ArrayList<String> getOntologyClassesURIs() {
    return ontologyClassesURIs;
  }

  public void setOntologyClassesURIs(ArrayList<String> arraylist) {
    ontologyClassesURIs = arraylist;
  }

  protected JPanel mainPanel;
  protected JTextField nameSpace;
  protected JButton domainButton;
  protected JButton rangeButton;
  protected JTextField propertyName;
  protected ValuesSelectionAction domainAction;
  protected ValuesSelectionAction rangeAction;
  protected ArrayList<String> ontologyClassesURIs;
  protected ArrayList<DefaultMutableTreeNode> selectedNodes;
  protected Ontology ontology;
}
