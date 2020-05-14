/*******************************************************************************
 * Universe.java
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

import java.io.*;
import java.util.*;
import static au.gov.aad.erm.EPOC_Builder.Constants.*;

/*******************************************************************************
 * EPOC Builder Universe class.
 *
 * @author Troy Robertson
 * @company Australian Antarctic Division
 * @created 10/09/2009
 * @version 0.4.0, 01-09-2010
 ******************************************************************************/
public class Universe extends EPOCObject<Universe> {
    
    private String controller = "";

    private Report report = null;
    private Spatial spatial = null;
    private ArrayList<Trial> trials = new ArrayList();

    private ArrayList<Element> biotaList = new ArrayList();
    private ArrayList<Element> environList = new ArrayList();
    private ArrayList<Element> activityList = new ArrayList();
    private ArrayList<Element> managementList = new ArrayList();
    private ArrayList<Element> outputList = new ArrayList();
    private ArrayList<Element> presentationList = new ArrayList();

    // lists to hold eclass requiring storage deletions on save
    private ArrayList<EClass> deleteEClassList = new ArrayList();

    // lists to hold configs requiring storage deletions on save
    private ArrayList<EPOCObject> deleteConfigList = new ArrayList();

    // lists to hold elements requiring storage deletions on save
    private ArrayList<Element> deleteElementList = new ArrayList();
    
    // delete element and configs template list
    private ArrayList<EPOCObject> deleteTemplateList = new ArrayList();

    // Temoporary list used to store new replica sub-objects
    private ArrayList<EPOCObject> replicaList = new ArrayList();
  
    /*
     * Creates a new instance of Universe containing next available revision
     */
    public Universe() {
        setNextRevision();
        epocClassname = "Universe";
        // Still want to load templates, even though universe does not exist yet in storage
        templates = new Templates();
        storage.load(this);
    }
    
    /*
     * Constructor creates universe from that found in storage with passed uid
     */
    public Universe(int uniID) {
        uid = uniID;
        epocClassname = "Universe";

        storage.load(this);
        setElementActionRelatedObjects();
    }

    private void setElementActionRelatedObjects() {
        // For active elements
        ArrayList<Element> eList = getElements(OBJ_ELE);
        for (Element ele : eList) {
            ele.setActionRelatedObjectsFromList(eList);
        }
    }

    /**
     * Set name of model controller (EPOC)
     * @param contr
     */
    public void setController(String contr) {
        controller = contr;
    }

    /**
     * Replace module list of eleType with the elist passed.
     * @param elist
     * @param eleType
     */
    protected void setModuleList(ArrayList<Element> elist, int eleType) {
        switch (eleType) {
            case OBJ_BIO:
                biotaList = elist;
                break;
            case OBJ_ENV:
                environList = elist;
                break;
            case OBJ_ATY:
                activityList = elist;
                break;
            case OBJ_MAN:
                managementList = elist;
                break;
            case OBJ_OUT:
                outputList = elist;
                break;
            case OBJ_PRE:
                presentationList = elist;
                break;
        }
    }

    /**
     * Instantiate a new trial object and add it to the
     * universe. Set its position as the last for that module.
     *
     * @return
     */
    public Trial createNewTrial() {
        Trial newTri = new Trial();
        newTri.setTemplates(templates);
        newTri.setParentUID(getUID());
        setNextAvailableVersion(newTri, false);
        setNextHighestPosition(newTri);
        setConfigObject(newTri);

        return newTri;
    }

    /**
     * Add a newly created Config object obj to the universe,
     * first setting its internal members.
     * @param obj
     */
    public void setNewConfigObject(EPOCObject newObj) {
        newObj.setTemplates(templates);
        newObj.setParentUID(getUID());
        setNextAvailableVersion(newObj, false);
        setConfigObject(newObj);
    }

    /**
     * Set/Add Config object to universe.
     * @param obj
     */
    public void setConfigObject(EPOCObject obj) {
        if (obj instanceof Spatial) {
            spatial = (Spatial)obj;
        } else if (obj instanceof Report) {
            report = (Report)obj;
        } else if (obj instanceof Trial) {
            trials.add((Trial)obj);
            setNextHighestPosition((Trial)obj);
        }
    }

    /**
     * Set this elements position to the highest + 1 of its type
     * @param ele
     */
    public void setNextHighestPosition(Element ele) {
        int pos = 1;
        for (Element eObj : getElements(ele.getModType())) {
            if (eObj.getPosition() >= pos) pos = eObj.getPosition() + 1;
        }
        ele.setPosition(pos);
    }

    /**
     * Set this trial position to the highest + 1 of its type
     * @param ele
     */
    public void setNextHighestPosition(Trial tri) {
        int pos = 1;
        for (Trial t : getTrials()) {
            if (t.getPosition() >= pos) pos = t.getPosition() + 1;
        }
        tri.setPosition(pos);
    }

    /**
     * Set passed object revision to next highest revision found
     * in storage (if checkStorage is true) or held locally
     * @param obj
     * @param checkStorage
     */
    public void setNextAvailableVersion(EPOCObject obj, boolean checkStorage) {
        int objType = obj.getObjType();

        // do we need to check storage first for next revision
        if (checkStorage) obj.setNextRevision();

        // Check newly registered replica list
        for (EPOCObject rObj : replicaList) {
            if (objType == rObj.getObjType() && !rObj.equals(obj)) obj.setHigherVersion(rObj);
        }

        if (obj instanceof Spatial) {
            if (spatial != null && !spatial.equals(obj)) obj.setHigherVersion(spatial);
        } else if (obj instanceof Report) {
            if (report != null && !report.equals(obj)) obj.setHigherVersion(report);
        } else if (obj instanceof Trial) {
            for (Trial tri : getTrials()) {
                if (!tri.equals(obj)) obj.setHigherVersion(tri);
            }
        } else if (obj instanceof Element || obj instanceof Action || obj instanceof Attribute) {
            // go through local elements to see if any are same or higher
            for (Element ele : getElements(OBJ_ELE)) {
                if (obj instanceof Element) {
                    if (!ele.equals(obj)) obj.setHigherVersion(ele);
                } else if (obj instanceof Action) {
                    for (Action act : ele.getActions()) {
                        if (!act.equals(obj)) obj.setHigherVersion(act);
                    }
                } else if (obj instanceof Attribute) {
                    for (Attribute att : ele.getAttributes()) {
                        if (!att.equals(obj)) obj.setHigherVersion(att);
                    }
                }
            }
        }

        // then check local template lists
        for (Object tobj : templates.getTemplateList(objType)) {
            obj.setHigherVersion((EPOCObject)tobj);
        }
    }

    public String getController() {
        return controller;
    }

    public Spatial getSpatial() {
        return spatial;
    }

    public Report getReport() {
        return report;
    }

    public ArrayList<Trial> getTrials() {
        Collections.sort(trials);
        return trials;
    }

    /**
     * Return element, if found in appropriate element list, based on
     * uid and element type passed
     * @return null if not found
     */
    public Element getElement(int eleUID, int eleType) {
        int i = isElement(eleUID, eleType);
        if (i >= 0) {
            return (Element)getElements(eleType).get(i);
        } 
        return null;
    }
    
    /**
     * Return element, if found in appropriate element list, based on
     * index and type passed
     * @return null if not found
     */
    public Element getElementAt(int index, int eleType) {
        
        if (index < getElements(eleType).size()) {
            return (Element)getElements(eleType).get(index);
        } 
        return null;
    }

    /**
     * Instantiate a new element object of the type passed and add it to the
     * universe. Set its position as the last for that module.
     * @param eleType
     * @return
     */
    public Element createNewElement(int eleType) {
        Element newEle = new Element(eleType);
        newEle.setTemplates(templates);
        newEle.setParentUID(getUID());
        setNextAvailableVersion(newEle, false);
        setNextHighestPosition(newEle);

        addElement(newEle);

        return newEle;
    }

    /**
     * Add passed element to appropriate element list
     * @param ele
     * @return boolean true if success
     */
    public boolean addElement(Element ele) {
        
        // check that element is not already in list
        if (elementIndex(ele) < 0) {
            // add it to the list
            getElements(ele.getModType()).add(ele);
            setNextHighestPosition(ele);
            return true;
        } 
        
        return false;
    }

    /**
     * Clear all module element lists
     */
    public void clearElements() {
        biotaList = new ArrayList();
        environList = new ArrayList();
        activityList = new ArrayList();
        managementList = new ArrayList();
        outputList = new ArrayList();
        presentationList = new ArrayList();
    }

    /**
     * Deletes template from database.
     * This assumes that a check has been performed to see that the template
     * is not being used by any object.  Will result in a broken link where it
     * was a linked object.
     * @param eobj template object to be deleted
     * @return
     */
    public void deleteTemplate(EPOCObject eobj) {
        if (eobj instanceof EClass || eobj instanceof Element || eobj instanceof Spatial
                || eobj instanceof Report || eobj instanceof Trial) {
            // Remove it from the template listing
            getTemplates().removeTemplateList(eobj);

            // If has been in storage then it will need deleting
            if (eobj.getUID() > 0) addTemplateDeleteList(eobj.clone());
            
            // Set it as broken for where it is still referenced as a linked object
            eobj.setBroken();
        }
        // If its an element then remove its templated actions and attributes
        if (eobj instanceof Element) {
            // remove its actions from action template list
            for (Action act : ((Element)eobj).getActions()) ((Element)eobj).deleteTemplate(act);
            // remove its attributes from attribute template list
            for (Attribute att : ((Element)eobj).getAttributes()) ((Element)eobj).deleteTemplate(att);
        }
    }

    /**
     * Delete element passed from element list
     * If it has a uid (been stored before) then add it to the delete list first
     * @param ele Element to remove
     * @return boolean true if success
     */
    public boolean removeElement(Element ele) {

        // get element and put it in delete list to be deleted (or unlinked) on universe save
        if (ele.getUID() > 0) addDeleteList(ele.clone());

        // Replace any linked objects point to this with a broken copy
        Element brokenEle = (Element)ele.clone();
        brokenEle.setBroken();
        replaceLinkWith(ele, brokenEle);

        return getElements(ele.getModType()).remove(ele);
    }

    /**
     * Delete spatial object
     * If it has a uid (been stored before) then add it to the delete list first
     * @return
     */
    public boolean removeSpatial() {
        boolean present = (spatial != null);
        if (present && spatial.getUID() > 0) addDeleteList(spatial);
        spatial = null;
        return present;
    }

    /**
     * Delete report object
     * If it has a uid (been stored before) then add it to the delete list first
     * @return
     */
    public boolean removeReport() {
        boolean present = (report != null);
        if (present && report.getUID() > 0) addDeleteList(report);
        report = null;
        return present;
    }

    /**
     * Set the passed config object type to null
     * @param obj
     * @return
     */
    public boolean removeConfig(EPOCObject obj) {
        boolean present = false;

        if (obj instanceof Spatial) {
            present = (spatial != null);
            spatial = null;
        } else if (obj instanceof Report) {
            present = (report != null);
            report = null;
        }
        if (obj instanceof Trial) {
            //present = trials.contains((Trial)obj);
            present = trials.remove((Trial)obj);
        }

        // get object and put it in delete list to be deleted (or unlinked) on universe save
        if (present && obj.getUID() > 0) addDeleteList(obj);

        return present;
    }

     /**
      * Templates the EPOCObject passed, if it is not already a template.
      * If an Element is passed, all its non-template Actions and Attributes will also be templated
      * @param obj
      */
    public void template(EPOCObject obj) {
        if (!obj.isTemplate()) obj.template();
    }

    /**
     * Replace original config item with its edited copy.
     * If it was originally a template then this will involve unlinking the original template
     * and untemplating the modified revision
     * NOTE: Modified copy will be re-versioned.
     * @param origConf
     * @param modConf
     */
    public void replaceModifiedConfig(EPOCObject origConf, EPOCObject modConf) {

        // if its a modified template
        if (origConf.isTemplate()) {

            // then remove link
            addDeleteList(origConf);

            // the newly created element can then have its uid unset causing it to be
            // saved as a new object
            modConf.unsetAsTemplate(getUID());
            modConf.setUID(0);
            setNextAvailableVersion(modConf, true);
        }

        if (origConf instanceof Spatial) {
            spatial = (Spatial)modConf;
        } else if (origConf instanceof Report) {
            report = (Report)modConf;
        } else if (origConf instanceof Trial) {
            if (trials.contains((Trial)origConf)) {
                trials.set(trials.indexOf(origConf), (Trial)modConf);
            } else {
                trials.add((Trial)modConf);
            }
        }
    }

    /**
     * Replace original element with its edited copy.
     * If it was originally a template then this will involve unlinking the original template
     * and untemplating the modified revision
     * NOTE: Modified copy will be re-versioned.
     *
     * @param origEle
     * @param modEle
     */
    public void replaceModifiedElement(Element origEle, Element modEle) {

        // if its a modified template
        if (origEle.isTemplate()) {

            // then remove link
            addDeleteList(origEle);

            // the newly created element can be unset as a template and then have its uid unset
            // causing it to be saved as a new object
            modEle.unsetAsTemplate(getUID());
            modEle.setUID(0);
            setNextAvailableVersion(modEle, true);

            // Now need to check for any element actions which used modified object
            // as a related element and point them to new modifiedEle
            replaceModifiedRelatedElements(origEle, modEle);
        }

        replaceElement(origEle, modEle);
    }

    /**
     * Replace any elements action related element objects equal to origEle
     * with the modified modEle
     *  
     * @param origEle
     * @param modEle
     */
    private void replaceModifiedRelatedElements(Element origEle, Element modEle) {
        for (Element ele : getElements()) {
            for (Action act : ele.getActions()) {
                int i = 0;
                for (Element rEle : act.getRelatedElements()) {
                    if (rEle.equals(origEle)) {
                        if ((!act.isTemplate() || modEle.isTemplate()) && EPOC_AUTO_MATCH_LINK_OBJ) {
                            act.getRelatedElements().set(i, modEle);
                        } else {
                            act.getRelatedElements().set(i, (Element)origEle.hollow());
                        }
                        break;      // as there will only be one occurance per Action
                    }
                    i++;
                }
            }
        }
    }

    /*
     * Replace element passed, if found in list, with new element passed
     * @return boolean true if success
     */
    private boolean replaceElement(Element origEle, Element modEle) {
        ArrayList list = getElements(origEle.getModType());

        int index = list.indexOf(origEle);
        if (index >= 0) {
            // then replace old element
            list.remove(index);
            list.add(index, modEle);
            return true;
        }
        
        return false;
    }
    
    /**
     * Add XML template object (and its children) which have just been imported
     * @param eobj - New imported object
     */
    public void addImportedTemplate(EPOCObject eObj) {
        // unset uid and reset revision
        eObj.freshen(this, true);
        clearRegisteredReplicas();
        // Rebuild internal structure
        eObj.remakeLinks();
        // Insert current templates object
        eObj.setTemplates(getTemplates(), true);

        // Break links to objects outside primary structure if required
        if (!EPOC_IMP_LINK_OBJ) eObj.breakLinks(eObj, null);
        // Substitute member objects with templates if required
        if (EPOC_AUTO_MATCH_MEMBERS) eObj.substituteMembersWithTemplates();

        // Cause object structure to be added to templates lists
        eObj.retemplate();

        // Repair any remaining broken links
        if (EPOC_AUTO_MATCH_LINK_OBJ) eObj.repairBrokenLinks(this, null);


        /**
         * if (!EPOC_IMP_LINK_OBJ) eObj.breakLinks();
        eObj.unsetAsTemplate(0, true);  // so that replicate deep copies
        eObj.setTemplates(getTemplates(), true);
        if (EPOC_AUTO_MATCH_MEMBERS) eObj.substituteMembersWithTemplates();
        EPOCObject newObj = eObj.replicate(this); // unset uid and reset revision
        newObj.template();  // causes it to be templated again and added to list
        if (EPOC_AUTO_MATCH_LINK_OBJ) newObj.repairLinks(this, (EPOCObject)null);
         *
         */
    }

    /**
     * Swap position values between objects passed, ensuring they are of the same 
     * object type.
     * 
     * @param fstObj
     * @param secObj
     */
    public void swapPositions(EPOCObject fstObj, EPOCObject secObj) {
        int fstPos = -1, secPos = -1;

        if (fstObj.getObjType() == secObj.getObjType()) {
            fstPos = fstObj.getPosition();
            fstObj.setPosition(secObj.getPosition());
            secObj.setPosition(fstPos);
        }
    }

    /**
     * Swap list indexes of objects passed, ensuring they are of the same
     * object type.  Only Universe child objects (Trials and Elements)
     *
     * @param fstObj
     * @param secObj
     */
    public void swapIndexes(EPOCObject fstObj, EPOCObject secObj) {
        int fstPos = -1, secPos = -1;

        if (fstObj.getObjType() == secObj.getObjType()) {

            // swap actual list positions
            ArrayList objList = new ArrayList();
            if(fstObj instanceof Element) {
                objList = getElements(((Element)fstObj).getModType());
            } else if (fstObj instanceof Trial) {
                objList = getTrials();
            } else {
                return;
            }
            fstPos = objList.indexOf(fstObj);
            secPos = objList.indexOf(secObj);
            if (fstPos > -1 && secPos > -1) {
                objList.remove(fstObj);
                objList.remove(secObj);
                if (fstPos <= secPos) {
                    objList.add(fstPos, secObj);
                    objList.add(secPos, fstObj);
                } else {
                    objList.add(secPos, fstObj);
                    objList.add(fstPos, secObj);
                }
            }
        }
    }

    /*
     * @return list position of element if found
     * @return -1 if not found
     */
    private int elementIndex(Element ele) {
        ArrayList al = getElements(ele.getModType());
        
        return al.indexOf(ele);
    }
    
    /*
     * Check if element is already in list (by uid)
     * Elements with uid == 0 will never be matched in list
     * @return position in list
     */
    public int isElement(int eleUID, int eleType) {
        ArrayList al = getElements(eleType);
        
        if (eleUID != 0) {
            for (int i=0; i < al.size(); i++) {
                Element ele = (Element)al.get(i);
                if (ele.getUID() == eleUID) {
                    return i;
                }
            }
        }
        
        return -1;
    }

    /**
     * Return an arraylist of elements of the type passed
     * @param eleType
     * @return
     */
    public Element[] getElementArray(int eleType) {
        
        return (Element[])getElements(eleType).toArray(new Element[0]);
    }

    public ArrayList<Element> getElements() {
        return getElements(OBJ_ELE);
    }

    /**
     * Return an ordered list of element objects of the type passed
     *
     * @param eleType
     * @return
     */
    public ArrayList<Element> getElements(int eleType) {
      
        switch (eleType) {
            case OBJ_BIO:
                Collections.sort(biotaList);
                return biotaList;
            case OBJ_ENV:
                Collections.sort(environList);
                return environList;
            case OBJ_ATY:
                Collections.sort(activityList);
                return activityList;
            case OBJ_MAN:
                Collections.sort(managementList);
                return managementList;
            case OBJ_OUT:
                Collections.sort(outputList);
                return outputList;
            case OBJ_PRE:
                Collections.sort(presentationList);
                return outputList;
            case OBJ_ELE:
                // need to create a combined list
                ArrayList combList = new ArrayList();
                Collections.sort(biotaList);
                combList.addAll(biotaList);
                Collections.sort(environList);
                combList.addAll(environList);
                Collections.sort(activityList);
                combList.addAll(activityList);
                Collections.sort(managementList);
                combList.addAll(managementList);
                Collections.sort(outputList);
                combList.addAll(outputList);
                Collections.sort(presentationList);
                combList.addAll(presentationList);
                //Collections.sort(combList);
                return combList;
        }
        
        return new ArrayList();
    }

    /**
     * Return a list of templates of the type passed which are
     * unused by the Element object passed
     * 
     * @param ele
     * @param objType
     * @return
     */
    public ArrayList<EPOCObject> getUnusedTemplateList(Element ele, int objType) {
        ArrayList<EPOCObject> unusedList = new ArrayList();
        ArrayList<EPOCObject> tempList = templates.getTemplateList(objType);

        // eliminate any which are wrong type or are already used by this element
        for (EPOCObject eObj : tempList) {
            if (!ele.isMember(eObj)) unusedList.add(eObj);
        }

        return unusedList;
    }

    /**
     * Return an arraylist of unused template objects of the type passed
     *
     * @param objType
     * @return
     */
    public ArrayList<EPOCObject> getUnusedTemplateList(int objType) {
        ArrayList<EPOCObject> unusedList = new ArrayList();
        ArrayList<EPOCObject> tempList = templates.getTemplateList(objType);

        // eliminate any which are wrong type or are already used by this universe
        for (EPOCObject eObj : tempList) {
            if (!isMember(eObj)) unusedList.add(eObj);
        }

        return unusedList;
    }

    /**
     * Return an arraylist of unused template objects of all types
     *
     * @param objType
     * @return
     */
    public ArrayList<EPOCObject> getUnusedTemplateList() {
        ArrayList<EPOCObject> unusedList = new ArrayList();
        ArrayList<EPOCObject> tempList = templates.getTemplateList(OBJ_ALL);

        // eliminate any which are wrong type or are already used by this universe
        for (EPOCObject eObj : tempList) {
            if (!isMember(eObj)) unusedList.add(eObj);
        }

        return unusedList;
    }

    /**
     * Add object to delete list to be deleted on universe save
     * @param obj
     */
    public void addDeleteList(EPOCObject obj) {
        if (obj instanceof EClass) {
            deleteEClassList.add((EClass)obj);
        } else if (obj instanceof Element) {
            deleteElementList.add((Element)obj);
        }  else if (obj instanceof Spatial || obj instanceof Report || obj instanceof Trial) {
            deleteConfigList.add(obj);
        }
    }
    
    /**
     * Add template object to template delete list to be deleted on universe save
     * @param obj
     */
    public void addTemplateDeleteList(EPOCObject obj) {
        if (obj instanceof EClass || obj instanceof Element || obj instanceof Spatial
                || obj instanceof Report || obj instanceof Trial) {
            deleteTemplateList.add(obj);
        }
    }

    /**
     * Save any unused template objects to storage in case they are new or modified and
     * are not in any object list for this universe.
     * This should be performed after a universe save which will save any
     * new templates which are currently in use by universe.
     */
    public boolean saveUnusedTemplateObjects() {
        boolean retVal = true;

        // Save any unused templates, this will also save any action/attribute
        // templates which are used by element templates
        for (EPOCObject eObj : getUnusedTemplateList()) {
            if (eObj instanceof EClass) {
                if (!((EClass)eObj).saveToStorage()) retVal = false;
            } else if (eObj instanceof Spatial) {
                if (!((Spatial)eObj).saveToStorage()) retVal = false;
            } else if (eObj instanceof Report) {
                if (!((Report)eObj).saveToStorage()) retVal = false;
            } else if (eObj instanceof Trial) {
                if (!((Trial)eObj).saveToStorage()) retVal = false;
            } else if (eObj instanceof Element) {
                if (!((Element)eObj).saveToStorage()) retVal = false;
            } else if (eObj instanceof Action) {
                if (!((Action)eObj).saveToStorage()) retVal = false;
            } else if (eObj instanceof Attribute) {
                if (!((Attribute)eObj).saveToStorage()) retVal = false;
            }
        }
      
        return retVal;
    }

    /**
     * Save universe and child objects to storage, including linked templates
     */
    @Override
    public boolean saveToStorage() {
        if (!EPOCObject.testName(shortname)) {
            Messages.addErrMsg("Universe (" + shortname + ") must be named and may only contain\n" +
                     "alphanumerics, '.' or '_'\n" + 
                     "It may only start with an alphabetic character\n" +
                     "and may not end in '.' or '_'");
            return false;
        }

        // check delete list for any non-universe elements needing deletion
        if (!doDeletes()) return false;

        if (!super.saveToStorage()) return false;
        
        return true;
    }
    
    /**
     * Check delete list for any non-universe elements needing deletion
     */
    private boolean doDeletes() {
        boolean retVal = true;

         // for each delete list
        for (ArrayList list : new ArrayList[]{deleteEClassList, deleteConfigList, deleteElementList, deleteTemplateList}) {
            for (Object obj : list) {
                if (!list.equals(deleteTemplateList) && ((EPOCObject)obj).isTemplate()) {
                    // its a template
                    if (!storage.unlinkTemplate(uid, ((EPOCObject)obj).getUID(), ((EPOCObject)obj).getObjType())) {
                        retVal = false;
                    }
                } else if (!storage.delete((EPOCObject)obj)) retVal = false;
            }
            // reset list to empty
            list.clear();
        }
        
        return retVal;
    }

    /**
     * Does Universe have all initialised data members
     * @return
     */
    public boolean isEmpty() {
        return (uid == 0 && name.equals("") && shortname.equals("") && author.equals("")
                && description.equals("") && getElements(OBJ_ELE).isEmpty());
    }

    /**
     * Test if universe has been modified from that stored in database
     * @return boolean true if modified
     */
    public boolean isModified() {

        // First check if universe is empty
        if (!isEmpty()) {

            // retrieve a copy of this universe from storage
            Universe storedUni = new Universe(uid);

            //storedUni.setElementActionRelatedObjects();
            
            // Compare them
            if (!this.compare(storedUni, false)) return true;
        }

        return false;
    }

     /**
     * Check whether template is used as member of current universe
     * as well as any shown in storage by other universes.
     * @param eobj template object
     * @return boolean in use
     */
    public boolean isMemberTemplate(EPOCObject eobj) {
        if (isMember(eobj)) return true;
        if (templateUsedByOther(eobj)) return true;

        // if element then need to also check that no template actions/attributes of
        // this template element are in use
        if (eobj instanceof Element) {
            Element ele = (Element)eobj;
            for (Action act : ele.getActions()) {
                if (isMember(act)) return true;
                if (ele.templateUsedByOther(act)) return true;
            }
            for (Attribute att : ele.getAttributes()) {
                if (isMember(att)) return true;
                if (ele.templateUsedByOther(att)) return true;
            }
        }

        return false;
    }

    /**
     * Check whether template is used as a linked object in current universe.
     * @param eobj template object
     * @return boolean in use
     */
    public boolean isLinkedTemplate(EPOCObject eobj) {
        if (isLinked(eobj)) return true;
        // TODO check storage for linked in
        //if (templateUsedByOther(eobj)) return true;

        // if element then need to also check that no template actions/attributes of
        // this template element are linked
        if (eobj instanceof Element) {
            Element ele = (Element)eobj;
            for (Action act : ele.getActions()) {
                if (isLinked(act)) return true;
                //if (ele.templateUsedByOther(act)) return true;
            }
            for (Attribute att : ele.getAttributes()) {
                if (isLinked(att)) return true;
                //if (ele.templateUsedByOther(att)) return true;
            }
        }

        return false;
    }

    /**
     * Check if object passed is a member of this universe
     * @param obj
     * @return
     */
    public boolean isMember(EPOCObject obj) {

        if (obj instanceof Universe || obj.getObjType() == OBJ_ALL) return false;

        if (obj instanceof EClass) {
            // Not a member just a linked object
        } else if (obj instanceof Spatial && spatial != null) {
            if (spatial.equals(obj)) return true;
        } else if (obj instanceof Report && report != null) {
            if (report.equals(obj)) return true;
        } else if (obj instanceof Trial) {
            if (trials.contains((Trial)obj)) return true;
        // checking an element type list
        } else if (obj instanceof Element) {
            if (getElements(((Element)obj).getModType()).contains((Element)obj)) return true;
        // else if checking attribute or action
        } else if (obj instanceof Action || obj instanceof Attribute) {
            for (Element ele : getElements()) {
                if (ele.isMember(obj)) return true;
            }
        }
        
        return false;
    }

    /**
     * Check if object passed is linked to any member of this universe
     * @param obj
     * @return
     */
    public boolean isLinked(EPOCObject obj) {

        if (obj instanceof Universe || obj.getObjType() == OBJ_ALL) return false;

        for (Element ele : getElements()) if (ele.isLinked(obj)) return true;

        return false;
    }

    /**
     * Check whether template is used by another stored universe
     * @param obj
     * @return int count
     */
    public boolean templateUsedByOther(EPOCObject obj) {
        return storage.templateUsedByOther(uid, obj.getUID(), obj.getObjType());
    }

    /**
     * Is the passed objects EPOCID unique to  this object
     * @param eo
     * @return
     */
    public boolean isEPOCIDUnique(EPOCObject eo) {
        if (eo instanceof Element) {
            for (Element ele : this.getElements(OBJ_ELE)) {
                if (!ele.equals(eo) && (ele.getEPOCID().equals(((EPOCObject)eo).getEPOCID()))) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Does this universe pass all validation checks
     * @param recurse should child objects be checked
     * @return
     */
    public boolean validate(boolean recurse) {
        boolean passed = true;

        // Check spatial
        if (spatial != null) {
            if (spatial.getPolygonsVector() == null || spatial.getPolygonsVector().size() <= 0) {
                Messages.addErrMsg("Spatial object contains no polygons!");
                passed = false;
            }
            if (spatial.getOverlapsVector() == null || spatial.getOverlapsVector().size() <= 0) {
                Messages.addErrMsg("Spatial object contains no polygons overlaps!");
                passed = false;
            }
        } else {
            Messages.addErrMsg("Universe requires a Spatial object!");
            passed = false;
        }

        // Reporting
        if (report == null) {
            Messages.addErrMsg("Universe requires a Report object!");
            passed = false;
        }

        // Trials
        boolean hasTrial = false;
        for (Trial tri : getTrials()) {
            if (!tri.validate()) passed = false;
            hasTrial = true;
        }
        if (!hasTrial) {
            Messages.addErrMsg("Universe requires a Trial object!");
            passed = false;
        }

        // recurse down children?
        if (recurse) {
            for (Element ele : getElements(OBJ_ELE)) {
                if (!ele.validate(this, true)) passed = false;
            }
        }

        return passed;
    }

    /**
     * Returns a template object that superficially compares with that passed
     * if found, otherwise null.
     * @param obj
     * @return EPOCObject comparable template object
     */
    public EPOCObject findComparableTemplateObject(EPOCObject obj) {
        if (obj instanceof Universe) {
            return null;
        } else if (obj instanceof Element) {
            for (Element ele : getTemplates().getElementTemplateList(((Element)obj).getModType())) {
                if (obj.compare(ele, true)) return ele;
            }
        } else {
            for (EPOCObject tobj : getTemplates().getTemplateList(obj.getObjType())) {
                if (obj.compare(tobj, true)) return tobj;
            }
        }

        return null;
    }

    /**
     * Copy all data members into this object effectively updating this
     * object with any changes from a copy.  This does not update any child
     * objects, but does update with linked objects.
     * @param uni
     */
    @Override
    public void updateDataMembersFrom(Universe uni) {
        super.updateDataMembersFrom(uni);

        controller = uni.getController();
        deleteConfigList = uni.deleteConfigList;
        deleteElementList = uni.deleteElementList;
        deleteTemplateList = uni.deleteTemplateList;
        replicaList = uni.replicaList;
    }

    /*
     * Compares current object and its child objects with those of the object
     * passed.  If superficial then just look at EPOC data member qualities rather
     * than Epoc Builder data members.
     * @param uni Universe
     * @param superficial
     * @return boolean - true if they are equal
     */
    @Override
    public boolean compare(Universe uni, boolean superficial) {
        
        if (!super.compare(uni, superficial)) return false;
        
        if (!uni.getAuthor().equals(author)) return false;
        
        if (!uni.getController().equals(controller)) return false;

        if (!isBroken() && !uni.isBroken()) {
            // Compare Config items
            if (spatial == null ^ uni.getSpatial() == null) return false;
            if (spatial != null && uni.getSpatial() != null
                && !spatial.compare(uni.getSpatial(), superficial)) return false;

            if (report == null ^ uni.getReport() == null) return false;
            if (report != null && uni.getReport() != null
                && !report.compare(uni.getReport(), superficial)) return false;

            ArrayList<Trial> thisTriList = getTrials();
            ArrayList<Trial> thatTriList = uni.getTrials();
            if (thisTriList.size() != thatTriList.size()) return false;
            for (int i = 0; i < thisTriList.size(); i++) {
                if (!(thisTriList.get(i)).compare(thatTriList.get(i), superficial)) return false;
            }

            // Compare Elements
            for (int eleType : new int[]{OBJ_BIO, OBJ_ENV, OBJ_ATY, OBJ_MAN, OBJ_OUT, OBJ_PRE}) {
                ArrayList<Element> thisList = getElements(eleType);
                ArrayList<Element> thatList = uni.getElements(eleType);
                if (thisList.size() != thatList.size()) return false;
                for (int i = 0; i < thisList.size(); i++) {
                    if (!(thisList.get(i)).compare(thatList.get(i), superficial)) return false;
                }
            }
        }

        // Check delete element template list
        if (deleteTemplateList.size() > 0) return false;
        
        return true;
    }
    
    /*
     * Write self as text, in the form of an R assignment, to FileWriter
     */
    public void writeAsR(String dirPath, boolean writeElements) {
        File outputFile = new File(dirPath + File.separator + "data" + File.separator + getDisplayName() + ".data.R");
      
        try {
            outputFile.createNewFile();
            FileWriter out = new FileWriter(outputFile);
       
            Date dt = new Date();
            
            out.write("########################################################\n");
            out.write("# UNIVERSE - " + name + "." + revision + "\n");
            out.write("# Controller: " + controller + "\n");
            out.write("# Description: " + super.prependEachLine(description, "#              ") + "\n");
            out.write("#\n");
            out.write("# Generated by EPOC Builder\n");
            out.write("# Date: " + dt.toString() + "\n");
            out.write("########################################################\n\n");
            
            out.write(shortname + " <- list()\n\n");

            // Signature
            super.writeSignatureAsR(out, shortname);

            // Config file paths
            out.write("# Module and Element class and data inclusions/paths\n");
            out.write(shortname + "$inputPaths <- list(\n");
            out.write("\tConfig = list(\n");
            out.write("\t\tSourceFns = list(\n");
            out.write("\t\t\tPolygons     = file.path(RootPath, \"data\", \"" + spatial.getDisplayName() + ".data.R\"),\n");
            out.write("\t\t\tTrials       = file.path(RootPath, \"data\", \"" + getDisplayName() + ".trials.data.R\")\n");
            out.write("\t\t)\n");
            out.write("\t),\n");

            // For each element type loop through adding elements to list
            // If required by parameter also call writeAsR for each Element
            boolean notFirstMod = false;
            for (int eleType : new int[]{OBJ_BIO, OBJ_ENV, OBJ_ATY, OBJ_MAN, OBJ_OUT, OBJ_PRE}) {
                if (notFirstMod) out.write(",\n");
                notFirstMod = true;
                out.write("\t" + getObjectTypeName(eleType) + " = list(\n");
                if (getElements(eleType).size() > 0) {
                    boolean notFirstEle = false;
                    for (Element ele : getElements(eleType)) {
                        if (notFirstEle) out.write(",\n");
                        notFirstEle = true;
                        out.write("\t\t# ID = " + ele.getEPOCID() + "\n");
                        out.write("\t\t" + ele.getShortName() + " = list(className = \"" 
                                + (ele.getEClass() != null ? ele.getEClass().getDisplayName() : "???") + "\",\n");
                        out.write("\t\t               classFile = file.path(RootPath, \"code\", \"" 
                                + (ele.getEClass() != null ? ele.getEClass().getDisplayName() : "???") + ".R\"),\n");
                        out.write("\t\t               classData = file.path(RootPath, \"data\", \"" 
                                + ele.getDisplayName() + ".data.R\"))");
                        
                        if (writeElements) {
                            // Make call to element to write itself out
                            ele.writeAsR(dirPath);
                            // And then to EPOC Class to do the same
                            if (ele.getEClass() != null) ele.getEClass().writeAsR(dirPath);
                        }
                    }
                } else {
                    out.write("\t\tNULL");
                }
                out.write("\n\t)");
            }

            out.write("\n)\n\n");

            // Reporting details
            if (report != null) report.writeAsR(out, shortname);
            
            // Spatial data file
            if (spatial != null) spatial.writeAsR(dirPath);
            
            // Trial data file
            FileWriter triOut = Trial.writeHeaderAsR(dirPath, getDisplayName());
            for (Trial tri : getTrials()) tri.writeAsR(triOut);
            Trial.writeFooterAsR(triOut);

            out.write("\n");
            // Global parameters
            out.write("# Global (Universal) Parameters\n");
            // TODO Add a for loop to iterate over global universe attributes?
            out.write(shortname + "$monthDays = c(31,28,31,30,31,30,31,31,30,31,30,31)\n\n");

            // declare data
            out.write(shortname);

            out.close();

            writeRunScript(dirPath, EPOC_DBG, false);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public File writeRunScript(String dirPath, boolean debug, boolean justCalendar) {
        File startFile = new File(dirPath + File.separator + "Run_" + (justCalendar ? "Calendar.R" : "EPOC.R"));

        try {
            // Now write a startup script file
            startFile.createNewFile();
            FileWriter out = new FileWriter(startFile);

            Date dt = new Date();

            out.write("########################################################\n");
            out.write("# EPOC Start script for Universe - " + name + "." + revision + "\n");
            out.write("# Description: " + super.prependEachLine(description, "#              ") + "\n");
            out.write("#\n");
            out.write("# Generated by EPOC Builder\n");
            out.write("# Date: " + dt.toString() + "\n");
            out.write("########################################################\n\n");

            out.write("# Clear workspace\n");
            out.write("rm(list=ls(all=TRUE))\n\n");

            out.write("# Get zipped commandline arg\n");
            out.write("zipped <- length(commandArgs(TRUE)) > 0 && commandArgs(TRUE)[[1]] == \"-zipped\"\n\n");

            // Debugging?
            out.write("# Debugging\n");
            out.write((!debug ? "#" : "") + "options(error=recover)\n\n");
            out.write("# Load EPOC package\n");
            out.write((debug ? "#" : "") + "library(EPOC)\n\n");

            // Set the working directory/RootPath
            out.write("# Set the RootPath to the working directory which should be where this file is located\n");
            out.write("if (!zipped) setwd(file.path(\"" + dirPath.replace("\\", "/") + "\"))\n");
            out.write("RootPath <- file.path(getwd())\n\n");

            out.write("# Unzip directory structure if required\n");
            out.write("if (zipped) system(\"unzip -qo EPOC.zip\")\n\n");

            out.write("# Perform EPOC setup\n");
            out.write((!debug ? "#" : "") + "source(file=file.path(RootPath, \"base\", \"EPOC.Setup.R\"))\n\n");

            out.write("# Specify the universe for the epoc scenario and load input data\n");
            out.write("universe <- new(\"Universe\", dataPath=file.path(RootPath, \"data\", \"" + getDisplayName() + ".data.R\"))\n\n");

            out.write("# Start controller, this creates/sets up both the universe and calendar\n");
            out.write("controller <- new(\"Controller\", universe=universe)\n\n");

            out.write("# Display calendar\n");
            out.write("displayCalendar(controller, toScreen=FALSE)\n\n");

            out.write("# Start simulation\n");
            out.write("runSimulation(controller)\n\n");

            out.write("# Zip it all up again\n");
            out.write((justCalendar ? "#" : "") + "if (zipped) system(\"zip -qur EPOC.zip runtime\")\n");

            out.close();

            return startFile;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    /**
     * Add newly replicated object to a temporary list so that any new replicas
     * after this will take temporarily listed ones into account when determining
     * next available revision.
     * @param eo
     */
    public void registerNewReplica(EPOCObject eo) { replicaList.add(eo); }

    /**
     * Clear the temporary replica list
     */
    public void clearRegisteredReplicas() { replicaList.clear(); }

    /**
     * Substitute any child objects (elements) with a
     * comparable template if one can be found
     */
    @Override
    public void substituteMembersWithTemplates() {
        boolean replaced = false;

        for (int eleType : new int[]{OBJ_BIO, OBJ_ENV, OBJ_ATY, OBJ_MAN, OBJ_OUT, OBJ_PRE}) {
            ArrayList<Element> newElements = new ArrayList();
            for (Element ele : getElements(eleType)) {
                replaced = false;
                for (Element et : templates.getElementTemplateList(eleType)) {
                    if (ele.compare(et, true)) {
                        ele = et;
                        replaced = true;
                        break;
                    }
                }
                if (!replaced) ele.substituteMembersWithTemplates();
                newElements.add(ele);
            }

            setModuleList(newElements, eleType);
        }
    }

    /**
     * Replace any linked objects equal to linkObj with the replacement object
     * @param linkObj
     * @param replObj
     */
    @Override
    public void replaceLinkWith(EPOCObject linkObj, EPOCObject replObj) {
        if (linkObj.getClass() == replObj.getClass()) {
            for (Element ele : getElements()) ele.replaceLinkWith(linkObj, replObj);
        }
    }

    /**
     * Repair any broken linked objects in child (member) objects with a
     * comparable template if one can be found, or if object is not a template,
     * then a comparable local object.  Calls override method.
     */
    public void repairBrokenLinks() { repairBrokenLinks(this, null); }

    /**
     * Repair any broken linked objects (dataset, transform, related) with a
     * comparable local object if one can be found, or if this object is a template,
     * then a comparable template object.
     * Will only repair broken links if broken is true.
     * @param broken
     * @param root
     * @param parentEle
     */
    @Override
    public void repairLinks(boolean broken, EPOCObject root, Element parentEle) {
        for (Element ele : getElements()) ele.repairLinks(broken, root, null);
    }

    /**
     * Turn this object into a fresh new object which will be saved as
     * such.
     * @param uni
     * @param recurse
     */
    public void freshen(boolean recurse) {
        super.freshen(this, recurse);
        if (spatial != null) spatial.freshen(this, recurse);
        if (report != null) report.freshen(this, recurse);
        for (Trial tri : getTrials()) tri.freshen(this, recurse);
        for (Element ele : getElements()) ele.freshen(this, recurse);
    }

    /**
     * Template this universe object, including templating all child objects
     */
    @Override
    public void template() {
        if (!isTemplate()) {
            setAsTemplate();
            templates.addTemplateList(this);
            // template member objects
            if (spatial != null) spatial.template();
            if (report != null) report.template();
            for (Trial tri : getTrials()) tri.template();
            for (Element ele : getElements()) ele.template();
        }
    }

    /**
     * Check children for any broken links
     * @return
     */
    @Override
    public boolean hasBrokenLink() {
        for (Element ele : getElements()) if (ele.hasBrokenLink()) return true;

        return false;
    }

    /**
     * Dummy for EPOCObject to call with recurse default to false
     *
     **/
    @Override
    protected Universe clone(int method, Universe uni) {
        return clone(method, (method == EPOC_RPL));
    }

    /*
     * Return a deep copy of this universe
     */ 
    public Universe clone(int method, boolean recurse) {
        Universe uni = (Universe)super.clone(method, this);

        // Hollow out clone
        uni.spatial = null;
        uni.report = null;
        uni.trials = new ArrayList();
        uni.clearElements();

        // Do we just break it?
        if (method == EPOC_BRK) {
            uni.setBroken();
            return uni;
        }

        // Clone templates if necessary 
        // (BEWARE cloned templates are not linked together)
        if (method == EPOC_CLN && recurse) uni.setTemplates(templates.clone());

        // clone Config objects
        if (spatial != null) {
            if (recurse && (method == EPOC_CLN || !spatial.isTemplate())) {
                uni.setConfigObject(spatial.clone(method, this));
            } else {
                uni.setConfigObject(spatial);
            }
        }
        if (report != null) {
            if (recurse && (method == EPOC_CLN || !report.isTemplate())) {
                uni.setConfigObject(report.clone(method, this));
            } else {
                uni.setConfigObject(report);
            }
        }
        for (Trial tri : getTrials()) {
            if (recurse && (method == EPOC_CLN || !tri.isTemplate())) {
               uni.setConfigObject(tri.clone(method, this));
            } else {
               uni.setConfigObject(tri);
            }
        }

        // clone each Element
        for (Element ele : getElements()) {
            if (recurse && (method == EPOC_CLN || !ele.isTemplate())) {
               uni.addElement(ele.clone(method, this));
            } else {
               uni.addElement(ele);
            }
        }
       
        return uni;
    }
}
