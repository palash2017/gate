/*
 *  SerialDataStore.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 19/Jan/2001
 *
 *  $Id$
 */

package gate.persist;

import java.util.*;
import java.net.*;
import java.io.*;

import gate.*;
import gate.creole.*;
import gate.util.*;

/** A data store based on Java serialisation.
  */
public class SerialDataStore
extends AbstractFeatureBearer implements DataStore {

  /** Construction requires a file protocol URL
    * pointing to the storage directory used for
    * the serialised classes.
    */
  public SerialDataStore(URL storageDirUrl) {
    this.storageDir = new File(storageDirUrl.getFile());
  } // construction from URL

  /** Default construction */
  public SerialDataStore() { };

  /** The directory used for the serialised classes.
    */
  protected File storageDir;

  /** Set method for storage URL */
  public void setStorageDir(File storageDir) { this.storageDir = storageDir; }

  /** Get method for storage URL */
  public File getStorageDir() { return storageDir; }

  /** Set the URL for the underlying storage mechanism. */
  public void setStorageUrl(URL storageUrl) throws PersistenceException {
    if(! storageUrl.getProtocol().equalsIgnoreCase("file"))
      throw new PersistenceException(
        "A serial data store needs a file URL, not " + storageUrl
      );
    this.storageDir = new File(storageUrl.getFile());
  } // setStorageUrl

  /** Get the URL for the underlying storage mechanism. */
  public URL getStorageUrl() {
    if(storageDir == null) return null;

    URL u = null;
    try { u = storageDir.toURL(); } catch(MalformedURLException e) {
      // we can assume that this never happens as storageUrl should always
      // be a valid file and therefore convertable to URL
    }

    return u;
  } // getStorageUrl()

  /** Create a new data store. This tries to create a directory in
    * the local file system. If the directory already exists, or is
    * a file, or cannot be created, PersistenceException is thrown.
    */
  public void create()
  throws PersistenceException {
    if(storageDir == null)
      throw new PersistenceException("null storage directory: cannot create");

    if(storageDir.exists())
      throw new PersistenceException(
        "directory " + storageDir + " exists: cannot create"
      );

    if(! storageDir.mkdir())
      throw new PersistenceException("cannot create directory " + storageDir);
  } // create()

  /** Delete the data store.
    */
  public void delete() throws PersistenceException {
    if(storageDir == null || ! Files.rmdir(storageDir))
      throw new PersistenceException("couldn't delete " + storageDir);

    Gate.getDataStoreRegister().remove(this);
  } // delete()

  /** Delete a resource from the data store.
    */
  public void delete(String lrClassName, String dataStoreInstanceId)
  throws PersistenceException {

    // find the subdirectory for resources of this type
    File resourceTypeDirectory = new File(storageDir, lrClassName);
    if(
      (! resourceTypeDirectory.exists()) ||
      (! resourceTypeDirectory.isDirectory())
    ) {
        throw new PersistenceException("Can't find " + resourceTypeDirectory);
    }

    // create a File to representing the resource storage file
    File resourceFile = new File(resourceTypeDirectory, dataStoreInstanceId);
    if(! resourceFile.exists() || ! resourceFile.isFile())
      throw new PersistenceException("Can't find file " + resourceFile);

    // delete the beast
    if(! resourceFile.delete())
      throw new PersistenceException("Can't delete file " + resourceFile);

    // if there are no more resources of this type, delete the dir too
    if(resourceTypeDirectory.list().length == 0)
      if(! resourceTypeDirectory.delete())
        throw new PersistenceException("Can't delete "+resourceTypeDirectory);
  } // delete(lr)

  /** Adopt a resource for persistence. */
  public LanguageResource adopt(LanguageResource lr)
  throws PersistenceException {

    // check the LR's current DS
    DataStore currentDS = lr.getDataStore();
    if(currentDS == this)         // adopted already
      return lr;
    else if(currentDS == null) {  // an orphan - do the adoption
      lr.setDataStore(this);
      return lr;
    } else {                      // someone else's child
      throw new PersistenceException(
        "Can't adopt a resource which is already in a different datastore"
      );
    }

    // set up the LR's persistent storage ID

  } // adopt(LR)

  /** Open a connection to the data store. */
  public void open() throws PersistenceException {
    if(storageDir == null)
      throw new PersistenceException("Can't open: storage dir is null");

    // check storage directory is readable
    if(! storageDir.canRead()) {
      throw new PersistenceException("Can't read " + storageDir);
    }
  } // open()

  /** Close the data store. */
  public void close() throws PersistenceException {
  } // close()

  /** Save: synchonise the in-memory image of the LR with the persistent
    * image.
    */
  public void sync(LanguageResource lr) throws PersistenceException {
    // check that this LR is one of ours (i.e. has been adopted)
    if(lr.getDataStore() != this)
      throw new PersistenceException(
        "This LR is not stored in this DataStore"
      );

    // find the resource data for this LR
    ResourceData lrData =
      (ResourceData) Gate.getCreoleRegister().get(lr.getClass().getName());

    // create a subdirectory for resources of this type if none exists
    File resourceTypeDirectory = new File(storageDir, lrData.getClassName());
    if(
      (! resourceTypeDirectory.exists()) ||
      (! resourceTypeDirectory.isDirectory())
    ) {
      if(! resourceTypeDirectory.mkdir())
        throw new PersistenceException("Can't write " + resourceTypeDirectory);
    }

    // create an indentifier for this resource
    String lrPersistenceId =
      lrData.getName() + "___" + new Date().getTime() + "___" + random();
    lr.getFeatures().put("DataStoreInstanceId", lrPersistenceId);

    // create a File to store the resource in
    File resourceFile = new File(resourceTypeDirectory, lrPersistenceId);

    // dump the LR into the new File
    try {
      ObjectOutputStream oos = new ObjectOutputStream(
        new FileOutputStream(resourceFile)
      );
      oos.writeObject(lr);
      oos.close();
    } catch(IOException e) {
      throw new PersistenceException("Couldn't write to storage file: " + e);
    }
  } // sync(LR)

  /** Get a resource from the persistent store.
    * <B>Don't use this method - use Factory.createResource with
    * DataStore and DataStoreInstanceId parameters set instead.</B>
    */
  public LanguageResource getLr(String lrClassName, String dataStoreInstanceId)
  throws PersistenceException {

    // find the subdirectory for resources of this type
    File resourceTypeDirectory = new File(storageDir, lrClassName);
    if(
      (! resourceTypeDirectory.exists()) ||
      (! resourceTypeDirectory.isDirectory())
    ) {
        throw new PersistenceException("Can't find " + resourceTypeDirectory);
    }

    // create a File to representing the resource storage file
    File resourceFile = new File(resourceTypeDirectory, dataStoreInstanceId);
    if(! resourceFile.exists() || ! resourceFile.isFile())
      throw new PersistenceException("Can't find file " + resourceFile);

    // try and read the file and deserialise it
    LanguageResource lr = null;
    try {
      FileInputStream fis = new FileInputStream(resourceFile);
      ObjectInputStream ois = new ObjectInputStream(fis);
      lr = (LanguageResource) ois.readObject();
      ois.close();
    } catch(IOException e) {
      throw
        new PersistenceException("Couldn't read file "+resourceFile+": "+e);
    } catch(ClassNotFoundException ee) {
      throw
        new PersistenceException("Couldn't find class "+lrClassName+": "+ee);
    }

    // set the dataStore property of the LR (which is transient and therefore
    // not serialised
    lr.setDataStore(this);

    return lr;
  } // getLr(id)

  /** Get a list of the types of LR that are present in the data store. */
  public List getLrTypes() throws PersistenceException {
    if(storageDir == null || ! storageDir.exists())
      throw new PersistenceException("Can't read storage directory");

    return Arrays.asList(storageDir.list());
  } // getLrTypes()

  /** Get a list of the IDs of LRs of a particular type that are present. */
  public List getLrIds(String lrType) throws PersistenceException {
    // a File to represent the directory for this type
    File resourceTypeDir = new File(storageDir, lrType);
    if(! resourceTypeDir.exists())
      return Arrays.asList(new String[0]);

    return Arrays.asList(resourceTypeDir.list());
  } // getLrIds(lrType)

  /** Get a list of the names of LRs of a particular type that are present. */
  public List getLrNames(String lrType) throws PersistenceException {
    // the list of files storing LRs of this type; an array for the names
    String[] lrFileNames = (String[]) getLrIds(lrType).toArray();
    ArrayList lrNames = new ArrayList();

    // for each lr file name, munge its name and add to the lrNames list
    for(int i = 0; i<lrFileNames.length; i++) {
      String name = getLrName(lrFileNames[i]);
      lrNames.add(name);
    }

    return lrNames;
  } // getLrNames(lrType)

  /** Get the name of an LR from its ID. */
  public String getLrName(String lrId) {
    int secondSeparator = lrId.lastIndexOf("___");
    lrId = lrId.substring(0, secondSeparator);
    int firstSeparator = lrId.lastIndexOf("___");

    return lrId.substring(0, firstSeparator);
  } // getLrName

  /** Set method for the autosaving behaviour of the data store.
    * <B>NOTE:</B> this type of datastore has no auto-save function,
    * therefore this method throws an UnsupportedOperationException.
    */
  public void setAutoSaving(boolean autoSaving)
  throws UnsupportedOperationException {
    throw new UnsupportedOperationException(
      "SerialDataStore has no auto-save capability"
    );
  } // setAutoSaving

  /** Get the autosaving behaviour of the LR. */
  public boolean isAutoSaving() { return autoSaving; }

  /** Flag for autosaving behaviour. */
  protected boolean autoSaving = false;

  /** Generate a random integer between 0 and 9999 for file naming. */
  protected static int random() {
    return randomiser.nextInt(9999);
  } // random

  /** Random number generator */
  protected static Random randomiser = new Random();

  /** String representation */
  public String toString() {
    String nl = Strings.getNl();
    StringBuffer s = new StringBuffer("SerialDataStore: ");
    s.append("autoSaving: " + autoSaving);
    s.append("; storageDir: " + storageDir);
    s.append(nl);

    return s.toString();
  } // toString()

} // class SerialDataStore
