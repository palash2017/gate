// Warning: this has to alter the inputAS; the outputAS is ignored.


// Delete PMICandidate annotations that span or subspan NEs and
// Addresses.  Also, copy language feature from Sentence down to term
// candidates.

Set<String> termTypes = new HashSet<String>();
termTypes.add("Entity");
termTypes.add("Verb");
termTypes.add("VG");

Set<String> exclusionTypes = new HashSet<String>();
exclusionTypes.add("Person");
exclusionTypes.add("Organization");
exclusionTypes.add("Location");
exclusionTypes.add("Date");
exclusionTypes.add("Money");
exclusionTypes.add("Percent");
exclusionTypes.add("Address");

AnnotationSet candidates = inputAS.get(termTypes);
for (Annotation candidate : candidates) {
  // delete unwanted term candidates
  if (! gate.Utils.getCoveringAnnotations(inputAS, candidate).get(exclusionTypes).isEmpty()) {
    FeatureMap newf = Factory.newFeatureMap();
    newf.putAll(candidate.getFeatures());
    String newType = "deleted_NE_" + candidate.getType();
    inputAS.add(candidate.getStartNode(), candidate.getEndNode(), newType, newf);
    inputAS.remove(candidate);  
  }
  
  // delete single-token VG annotations
  else if ( candidate.getType().equals("VG") &&
            (gate.Utils.getContainedAnnotations(inputAS, candidate, "Token").size() < 2) ) {
    FeatureMap newf = Factory.newFeatureMap();
    newf.putAll(candidate.getFeatures());
    String newType = "deleted_VG_" + candidate.getType();
    inputAS.add(candidate.getStartNode(), candidate.getEndNode(), newType, newf);
    inputAS.remove(candidate);  
  }
  
}
