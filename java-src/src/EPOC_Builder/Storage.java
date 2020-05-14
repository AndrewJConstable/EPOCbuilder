/*******************************************************************************
 * Storage.java
 * =============================================================================
 * Copyright (c) 2009-2010 Australian Antarctic Division. All rights reserved.
 * Author can be contacted at troy.robertson@aad.gov.au.
 *
 * Every effort has been taken in making sure that the source code is
 * technically accurate, but I disclaim any and all responsibility for any loss,
 * damage or destruction of data or any other property which may arise from
 * relying on it. I will in no case be liable for any monetary damages arising
 * from such loss, damage or destruction.
 *
 * As with any code, ensure this code is tested in a development environment
 * before attempting to run it in production.
 * =============================================================================
 */
package au.gov.aad.erm.EPOC_Builder;

import java.util.ArrayList;

/*******************************************************************************
 * EPOC Builder Storage interface.  Currently implemented by DerbyStorage only.
 *
 * @author Troy Robertson
 * @company Australian Antarctic Division
 * @created 10/09/2009
 * @version 0.4.0, 01-09-2010
 ******************************************************************************/
public interface Storage {
    /**
     * Attempt to delete EPOC object data members from storage given object's uid
     * Delete all child objects
     * 
     * @return boolean true on success
     */
    boolean delete(EPOCObject obj);

    /**
     * Attempt to delete EPOC object data members from storage given object's uid
     * Delete all child objects if delChildren
     * 
     * @return boolean true on success
     */
    boolean delete(EPOCObject obj, boolean delChildren);

    boolean deleteDB(String dbName);

    boolean exportDB(String path);

    ArrayList getDBAvailableList(boolean excludeCurrent);

    String getDBName();

    /**
     * Return any outstanding error message from previous operation
     * null if none exists.
     * Reset error message to null
     */
    //String getErrMsg();

    void setErrMsg(String msg);

    /**
     * Retrieve Universe object indicated as last visited in settings table
     * else return new Universe object
     */
    Universe getLastVisited();

    /**
      * Retrieve the setting value for the setting passed
     */
    String getSetting(String setting);

    /**
     * Return a list of string arrays of size 2 which contain the uid,name strings
     * of objects of objType but which do not have the exemptuid
     * @param objType
     * @param exemptUID
     * @return
     */
    ArrayList getNegList(int objType, int exemptUID);

    /**
     * Return a list of string arrays of size 2 which contain the uid,name strings
     * of objects of objType which have no known parent and are not templates.
     * @param objType
     * @return
     */
    ArrayList getOrphanedList(int objType);

    /**
     * If parameter currVer == ""
     * Check storage for EPOC object with highest left-most component and return
     * that value incremented (eg if highest version in storage is "4.3" then return "5"
     * else if currVer != ""
     * Check storage for EPOC object with passed version and highest extra right-most component
     * and return its version incremented (eg "2.3.6" might return "2.3.6.4")
     */
    String getNextVersion(int objType, String currVer);

    boolean isConnected();

    boolean linkTemplate(int parentUID, EPOCObject obj);

    boolean load(EPOCObject obj);

    boolean load(EPOCObject obj, boolean loadChildren);

    boolean loadTemplates(Templates templates, int objType, int excludeUID);

    /**
     * Attempt to save EPOC object data members from storage given object's uid
     * Save all child objects
     * 
     * @return boolean true on success
     */
    boolean save(EPOCObject obj);

    /**
     * Attempt to save EPOC object data members from storage given object's uid
     * Save all child objects if saveChildren
     * 
     * @return boolean true on success
     */
    boolean save(EPOCObject obj, boolean saveChildren);

    /**
     * Save last visited Universe uid in settings table
     */
    void setLastVisited(int uid);

    /*
     * Save setting, value and description to settings table
     */
    void setSetting(String setting, String value, String description);

    int templateLinkCount(int pid, int uid, int objType);

    boolean templateUsedByOther(int excludePID, int uid, int objType);

    boolean unlinkTemplate(int parentUID, int uid, int objType);
}
