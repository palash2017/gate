/*
 *  WordNetViewer.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Rosen Marinov, 29/05/2002
 *
 */
package gate.gui.wordnet;

import gate.*;
import gate.creole.*;
import gate.wordnet.*;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

public class WordNetViewer extends AbstractVisualResource
                           implements ActionListener{

  protected JLabel searchLabel = new JLabel();
  protected JTextField searchWordTextField = new JTextField();
  protected JButton searchButton = new JButton();
  protected JTextPane resultPane = new JTextPane();
  protected JLabel searchLabel2 = new JLabel();
  protected JButton nounButton = new JButton();
  protected JButton verbButton = new JButton();
  protected JButton adjectiveButton = new JButton();
  protected JButton adverbButton = new JButton();
  protected JScrollPane scrollPane = new JScrollPane();
  protected GridBagLayout gridBagLayout1 = new GridBagLayout();

  protected JPopupMenu nounPopup;
  protected JPopupMenu verbPopup;
  protected JPopupMenu adjectivePopup;
  protected JPopupMenu adverbPopup;

  private static final String propertiesFile = "D:/Gate/temp/file_properties.xml";
  private IndexFileWordNetImpl wnMain = null;

  private boolean senatnceFrames = false;
  public final static int SENTANCE_FAMES = 33001;

  public WordNetViewer(){
    jbInit();
    initResources();
  }

  private void initResources(){
    try {
      Gate.init();
      wnMain = new IndexFileWordNetImpl();
      wnMain.setPropertyFile(new File(propertiesFile));
      wnMain.init();
    } catch (Exception e){
      e.printStackTrace();
    }
  }

  void jbInit(){
    searchLabel.setText("Search Word:");
        this.setLayout(gridBagLayout1);
        searchButton.setText("Search");
        searchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                searchButton_actionPerformed(e);
            }
        });
        searchWordTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                searchWordTextField_actionPerformed(e);
            }
        });
        searchLabel2.setText("Searches for ... :");
        nounButton.setText("Noun");
        verbButton.setText("Verb");
        adjectiveButton.setText("Adjective");
        adverbButton.setText("Adverb");

        nounButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                nounButton_actionPerformed(e);
            }
        });
        verbButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                verbButton_actionPerformed(e);
            }
        });
        adjectiveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                adjectiveButton_actionPerformed(e);
            }
        });
        adverbButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                adverbButton_actionPerformed(e);
            }
        });
        nounButton.setEnabled(false);
        verbButton.setEnabled(false);
        adjectiveButton.setEnabled(false);
        adverbButton.setEnabled(false);

        resultPane.setEditable(false);
        scrollPane.getViewport().add(resultPane);

        this.add(searchLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
        this.add(searchWordTextField, new GridBagConstraints(1, 0, 5, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
        this.add(scrollPane, new GridBagConstraints(0, 2, 7, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
        this.add(searchLabel2, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
        this.add(searchButton, new GridBagConstraints(6, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
        this.add(adjectiveButton, new GridBagConstraints(4, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
        this.add(verbButton, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
        this.add(nounButton, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
        this.add(adverbButton, new GridBagConstraints(5, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
  }

  private void searchButton_actionPerformed(ActionEvent e) {
    actionSearch();
  }

  private void searchWordTextField_actionPerformed(ActionEvent e) {
    actionSearch();
  }

  private void actionSearch(){
    String text = searchWordTextField.getText().trim();
    searchLabel2.setText("Searches for " + text + ":");

    nounButton.setEnabled(false);
    verbButton.setEnabled(false);
    adjectiveButton.setEnabled(false);
    adverbButton.setEnabled(false);

    nounPopup = new JPopupMenu();
    verbPopup = new JPopupMenu();
    adjectivePopup = new JPopupMenu();
    adverbPopup = new JPopupMenu();

    StringBuffer display = new StringBuffer("");

    addToResult(display, text, WordNet.POS_NOUN);
    addToResult(display, text, WordNet.POS_VERB);
    addToResult(display, text, WordNet.POS_ADJECTIVE);
    addToResult(display, text, WordNet.POS_ADVERB);

    resultPane.setText(display.toString());

  }

  private void addToResult(StringBuffer display,  String text, int wordType) {
    java.util.List senses = null;
    try {
      wnMain.cleanup();
      senses = wnMain.lookupWord(text, wordType);
    } catch (WordNetException wne) {
      wne.printStackTrace();
    }

    if ( senses!=null && senses.size()>0){

      String wordIdentifier = "";
      switch (wordType){
        case WordNet.POS_NOUN:
          wordIdentifier = "noun";
          nounButton.setEnabled(true);
          break;
        case WordNet.POS_VERB:
          wordIdentifier = "verb";
          verbButton.setEnabled(true);
          break;
        case WordNet.POS_ADJECTIVE:
          wordIdentifier = "adjective";
          adjectiveButton.setEnabled(true);
          break;
        case WordNet.POS_ADVERB:
          wordIdentifier = "adverb";
          adverbButton.setEnabled(true);
          break;
      }

      display.append("\n");
      display.append("The " + wordIdentifier + " " + text + " has " +senses.size() + " senses:");
      display.append("\n\n");
      for (int i=0; i< senses.size(); i++) {
        WordSense currSense = (WordSense) senses.get(i);
        Synset currSynset = currSense.getSynset();
        addToPopupMenu(currSynset, wordType, senses);
        java.util.List words = currSynset.getWordSenses();
        String wordsString = "";
        for (int j = 0; j<words.size(); j++){
          WordSense word = (WordSense) words.get(j);
          wordsString = wordsString + word.getWord().getLemma();
          if (j<(words.size()-1)){
            wordsString = wordsString + ", ";
          }
        }
        display.append(" " + (i+1) + ". " + wordsString + " -- " + currSynset.getGloss());
        display.append("\n");
      }
    }
  }

  private void addToPopupMenu(Synset synset, int wordType, java.util.List senses){
    java.util.List semRelations = null;
    try {
      semRelations = synset.getSemanticRelations();
    } catch (Exception e){
      e.printStackTrace();
    }
    for (int i=0; i<semRelations.size(); i++) {
      SemanticRelation relation = (SemanticRelation) semRelations.get(i);
      switch (wordType) {
        case WordNet.POS_NOUN:
          if (false == existInPopup(nounPopup, getLabel(relation)) ){
            nounPopup.add(new RelationItem(getLabel(relation), relation.getType(), senses));
          }
          break;
        case WordNet.POS_VERB:
          if (false == existInPopup(verbPopup, getLabel(relation)) ){
            verbPopup.add(new RelationItem(getLabel(relation), relation.getType(), senses));
          }
          if (!senatnceFrames){
            verbPopup.add(new RelationItem("Senatnce Frames", SENTANCE_FAMES, senses));
            senatnceFrames = true;
          }
          break;
        case WordNet.POS_ADJECTIVE:
          if (false == existInPopup(adjectivePopup, getLabel(relation)) ){
            adjectivePopup.add(new RelationItem(getLabel(relation), relation.getType(), senses));
          }
          break;
        case WordNet.POS_ADVERB:
          if (false == existInPopup(adverbPopup, getLabel(relation)) ){
            adverbPopup.add(new RelationItem(getLabel(relation), relation.getType(), senses));
          }
          break;
      }
    }
  }

  private boolean existInPopup(JPopupMenu menu, String name){
    boolean result = false;
    for (int i=0; i<menu.getComponents().length; i++){
      if ( menu.getComponents()[i].getName().equals(name)){
        result = true;
        break;
      }
    }
    return result;
  }

  void nounButton_actionPerformed(ActionEvent e) {
    nounPopup.show(nounButton, 0, nounButton.getHeight());
  }

  void verbButton_actionPerformed(ActionEvent e) {
    verbPopup.show(verbButton, 0, verbButton.getHeight());
  }

  void adjectiveButton_actionPerformed(ActionEvent e) {
    adjectivePopup.show(adjectiveButton, 0, adjectiveButton.getHeight());
  }

  void adverbButton_actionPerformed(ActionEvent e) {
    adverbPopup.show(adverbButton, 0, adverbButton.getHeight());
  }

  public void actionPerformed(ActionEvent e){
    RelationItem ri = (RelationItem) e.getSource();
    switch (ri.getRelationType()){
      case Relation.REL_ANTONYM:
        break;
      case Relation.REL_ATTRIBUTE:
        break;
      case Relation.REL_CAUSE:
        break;
      case Relation.REL_DERIVED_FROM_ADJECTIVE:
        break;
      case Relation.REL_ENTAILMENT:
        break;
      case Relation.REL_HYPERNYM:
        relHypernym(ri.getSenses());
        break;
      case Relation.REL_HYPONYM:
        relHyponym(ri.getSenses());
        break;
      case Relation.REL_MEMBER_HOLONYM:
        break;
      case Relation.REL_MEMBER_MERONYM:
        break;
      case Relation.REL_PARTICIPLE_OF_VERB:
        break;
      case Relation.REL_PART_HOLONYM:
        relPartHolonym(ri.getSenses());
        break;
      case Relation.REL_PART_MERONYM:
        relPartMeronym(ri.getSenses());
        break;
      case Relation.REL_PERTAINYM:
        break;
      case Relation.REL_SEE_ALSO:
        break;
      case Relation.REL_SIMILAR_TO:
        break;
      case Relation.REL_SUBSTANCE_HOLONYM:
        break;
      case Relation.REL_SUBSTANCE_MERONYM:
        break;
      case Relation.REL_VERB_GROUP:
        break;
      case SENTANCE_FAMES:
        sentanceFrames(ri.getSenses());
        break;
    }
  }

  private void relHypernym(java.util.List senses){
    StringBuffer display = new StringBuffer("");
    for (int i = 0; i<senses.size(); i++){
      display.append("\n");
      display.append("Sense ");
      display.append(i+1);
      display.append("\n");

      WordSense currSense = (WordSense) senses.get(i);
      Synset currSynset = currSense.getSynset();
      recursiveHypernym(currSynset, display, "  =>");
    }

    resultPane.setText(display.toString());
  }

  private void recursiveHypernym(Synset synset, StringBuffer display, String prefix){
    java.util.List words = synset.getWordSenses();
    String wordsString = getWords(words);

    display.append(prefix);
    display.append(" ");
    display.append(wordsString);
    display.append(" -- ");
    display.append(synset.getGloss());
    display.append("\n");

    java.util.List  hList = null;
    try {
      hList = synset.getSemanticRelations(Relation.REL_HYPERNYM);
    } catch (Exception e){
      e.printStackTrace();
    }
    if (hList!=null && hList.size()>0){
      SemanticRelation rel = (SemanticRelation) hList.get(0);
      prefix = "    " + prefix;
      recursiveHypernym(rel.getTarget(), display, prefix);
    }
  }

  private void relHyponym(java.util.List senses){
    StringBuffer display = new StringBuffer("");
    for (int i = 0; i<senses.size(); i++){
      WordSense currSense = (WordSense) senses.get(i);
      Synset currSynset = currSense.getSynset();
      try {
        if (currSynset.getSemanticRelations(Relation.REL_HYPONYM).size()>0){
          display.append("\n");
          display.append("Sense ");
          display.append(i+1);
          display.append("\n");
          recursiveHyponym(currSynset, display, "  =>");
        }
      } catch (Exception e){
        e.printStackTrace();
      }
    }

    resultPane.setText(display.toString());
  }

  private void recursiveHyponym(Synset synset, StringBuffer display, String prefix){
    java.util.List words = synset.getWordSenses();
    String wordsString = getWords(words);

    display.append(prefix);
    display.append(" ");
    display.append(wordsString);
    display.append(" -- ");
    display.append(synset.getGloss());
    display.append("\n");

    java.util.List  hypoList = null;
    try {
      hypoList = synset.getSemanticRelations(Relation.REL_HYPONYM);
    } catch (Exception e){
      e.printStackTrace();
    }
    if (hypoList!=null){
      for (int i = 0; i<hypoList.size(); i++){
        SemanticRelation rel = (SemanticRelation) hypoList.get(i);
        prefix = "    " + prefix;
        recursiveHyponym(rel.getTarget(), display, prefix);
        prefix = prefix.substring(4,prefix.length());
      }
    }
  }

  private void relPartMeronym(java.util.List senses){
    StringBuffer display = new StringBuffer("");
    for (int i = 0; i<senses.size(); i++){
      WordSense currSense = (WordSense) senses.get(i);
      Synset currSynset = currSense.getSynset();
      try {
        if (currSynset.getSemanticRelations(Relation.REL_PART_MERONYM).size()>0){
          display.append("\n");
          display.append("Sense ");
          display.append(i+1);
          display.append("\n");
          recursivePartMeronym(currSynset, display, "  ", false);
        }
      } catch (Exception e){
        e.printStackTrace();
      }
    }

    resultPane.setText(display.toString());
  }

  private void recursivePartMeronym(Synset synset, StringBuffer display, String prefix, boolean symbPrefix){
    java.util.List words = synset.getWordSenses();
    String wordsString = getWords(words);

    display.append(prefix);
    if (symbPrefix){
      display.append("HAS PART: ");
    }
    display.append(wordsString);
    display.append(" -- ");
    display.append(synset.getGloss());
    display.append("\n");

    java.util.List  meroList = null;
    try {
      meroList = synset.getSemanticRelations(Relation.REL_PART_MERONYM);
    } catch (Exception e){
      e.printStackTrace();
    }
    if (meroList!=null && meroList.size()>0){
      SemanticRelation rel = (SemanticRelation) meroList.get(0);
      prefix = "    " + prefix;
      recursivePartMeronym(rel.getTarget(), display, prefix, true);
    }
  }

  private void relPartHolonym(java.util.List senses){
    StringBuffer display = new StringBuffer("");
    for (int i = 0; i<senses.size(); i++){
      WordSense currSense = (WordSense) senses.get(i);
      Synset currSynset = currSense.getSynset();
      try {
        if (currSynset.getSemanticRelations(Relation.REL_PART_HOLONYM).size()>0){
          display.append("\n");
          display.append("Sense ");
          display.append(i+1);
          display.append("\n");
          recursivePartHolonym(currSynset, display, "  ", false);
        }
      } catch (Exception e){
        e.printStackTrace();
      }
    }

    resultPane.setText(display.toString());
  }

  private void recursivePartHolonym(Synset synset, StringBuffer display, String prefix, boolean symbPrefix){
    java.util.List words = synset.getWordSenses();
    String wordsString = getWords(words);

    display.append(prefix);
    if (symbPrefix) {
      display.append("PART OF: ");
    }
    display.append(wordsString);
    display.append(" -- ");
    display.append(synset.getGloss());
    display.append("\n");

    java.util.List  holoList = null;
    try {
      holoList = synset.getSemanticRelations(Relation.REL_PART_HOLONYM);
    } catch (Exception e){
      e.printStackTrace();
    }
    if (holoList!=null && holoList.size()>0){
      SemanticRelation rel = (SemanticRelation) holoList.get(0);
      prefix = "    " + prefix;
      recursivePartHolonym(rel.getTarget(), display, prefix, true);
    }
  }

  private String getWords(java.util.List words){
    StringBuffer wordsString = new StringBuffer("");
    for (int j = 0; j<words.size(); j++){
      WordSense word = (WordSense) words.get(j);
      wordsString.append(word.getWord().getLemma());
      if (j<(words.size()-1)){
        wordsString.append(", ");
      }
    }
    return wordsString.toString();
  }

  private void sentanceFrames(java.util.List senses){
    StringBuffer display = new StringBuffer("");
    for (int i=0; i<senses.size(); i++) {
      WordSense currSense = (WordSense) senses.get(i);
      Synset currSynset = currSense.getSynset();
      Verb currVerb = (Verb) currSense;
      java.util.List frames = currVerb.getVerbFrames();

      display.append("\nSense ");
      display.append(i+1);
      display.append("\n  ");
      display.append(getWords(currSynset.getWordSenses()));
      display.append(" -- ");
      display.append(currSynset.getGloss());
      display.append("\n");

      for (int j=0; j<frames.size(); j++){
        display.append("        *> ");
        display.append(((VerbFrame) frames.get(j)).getFrame());
        display.append("\n");
      }
    }
    resultPane.setText(display.toString());
  }


  public String getLabel(Relation r){

    String result = "";
    switch (r.getType()){
      case Relation.REL_ANTONYM:
        result = "Antonym";
        break;
      case Relation.REL_ATTRIBUTE:
        result = "Attribute";
        break;
      case Relation.REL_CAUSE:
        result = "Cause";
        break;
      case Relation.REL_DERIVED_FROM_ADJECTIVE:
        result = "Derived From Adjective";
        break;
      case Relation.REL_ENTAILMENT:
        result = "Entailment";
        break;
      case Relation.REL_HYPERNYM:
        result = "Hypernym";
        break;
      case Relation.REL_HYPONYM:
        result = "Hyponym";
        break;
      case Relation.REL_MEMBER_HOLONYM:
        result = "Member Holonym";
        break;
      case Relation.REL_MEMBER_MERONYM:
        result = "Member Meronym";
        break;
      case Relation.REL_PARTICIPLE_OF_VERB:
        result = "Participle Of Verb";
        break;
      case Relation.REL_PART_HOLONYM:
        result = "Holonym";
        break;
      case Relation.REL_PART_MERONYM:
        result = "Meronym";
        break;
      case Relation.REL_PERTAINYM:
        result = "Pertainym";
        break;
      case Relation.REL_SEE_ALSO:
        result = "See Also";
        break;
      case Relation.REL_SIMILAR_TO:
        result = "Similar To";
        break;
      case Relation.REL_SUBSTANCE_HOLONYM:
        result = "Substance Holonym";
        break;
      case Relation.REL_SUBSTANCE_MERONYM:
        result = "Substance Meronym";
        break;
      case Relation.REL_VERB_GROUP:
        result = "Verb Group";
        break;
    }
    return result;
  }

  private class RelationItem extends JMenuItem{

    int relType;
    java.util.List senses;

    public RelationItem(String name, int type, java.util.List sen) {
      super(name);
      this.addActionListener(WordNetViewer.this);
      relType = type;
      senses = sen;
      setName(name);
    }

    public int getRelationType() {
      return relType;
    }

    public java.util.List getSenses(){
      return senses;
    }

  }

}