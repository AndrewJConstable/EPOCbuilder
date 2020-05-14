/*******************************************************************************
 * Templates.java
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

import java.util.*;
import static au.gov.aad.erm.EPOC_Builder.Constants.*;

/*******************************************************************************
 * EPOC Builder Templates class.  Singleton object used to store all available
 * templated objects.
 *
 * @author Troy Robertson
 * @company Australian Antarctic Division
 * @created 14/04/2010
 * @version 0.4.0, 01-09-2010
 ******************************************************************************/
public class Templates {

    // template lists
    private ArrayList<EClass> eclassTemplateList = new ArrayList();
    private ArrayList<Spatial> spatialTemplateList = new ArrayList();
    private ArrayList<Report> reportTemplateList = new ArrayList();
    private ArrayList<Trial> trialTemplateList = new ArrayList();
    private ArrayList<Element> elementTemplateList = new ArrayList();
    private ArrayList<Action> actionTemplateList = new ArrayList();
    private ArrayList<Attribute> attributeTemplateList = new ArrayList();

    // template delete lists - Not used
    private ArrayList<EPOCObject> deleteTemplateList = new ArrayList();

    Storage storage = DerbyStorage.getInstance();

    public Templates() {
        this(true);
    }

    public Templates(boolean loadFromStorage) {
        if (loadFromStorage) {
            storage.loadTemplates(this, OBJ_SPA, 0);
            storage.loadTemplates(this, OBJ_REP, 0);
            storage.loadTemplates(this, OBJ_TRI, 0);
            storage.loadTemplates(this, OBJ_CLS, 0);

            // these are loaded in reverse order
            storage.loadTemplates(this, OBJ_ATT, 0);
            storage.loadTemplates(this, OBJ_ACT, 0);
            storage.loadTemplates(this, OBJ_ELE, 0);

            setActionLinkedObjects();
        }
    }

    /**
     * Called by constructor to set up action linked objects
     */
    private void setActionLinkedObjects() {
        // For active elements
        for (Action act : actionTemplateList) {
            act.setTransformFromList(actionTemplateList);
            act.setDatasetFromList(attributeTemplateList);
            act.setTimestepDatasetsFromList(attributeTemplateList);
            act.setRelatedElementsFromList(elementTemplateList);
        }
    }

    /**
     * Has this templates object been modified from that in storage
     * @return
     */
    public boolean isModified() {
        // retrieve a copy of this universe from storage
        Templates storedTemplates = new Templates();

        // Compare them
        if (!this.compare(storedTemplates)) return true;
        return false;
    }

    /**
     * Return the template list for object type passed
     * @param templList
     * @param objType
     */
    public ArrayList<EPOCObject> getTemplateList(int objType) {
        ArrayList<EPOCObject> tempList = new ArrayList();

        switch (objType) {
            case OBJ_ALL:
                tempList.addAll(eclassTemplateList);
                tempList.addAll(spatialTemplateList);
                tempList.addAll(reportTemplateList);
                tempList.addAll(trialTemplateList);
                tempList.addAll(elementTemplateList);
                tempList.addAll(actionTemplateList);
                tempList.addAll(attributeTemplateList);
                break;
            case OBJ_CLS:
                tempList = (ArrayList)eclassTemplateList;
                break;
            case OBJ_SPA:
                tempList = (ArrayList)spatialTemplateList;
                break;
            case OBJ_REP:
                tempList = (ArrayList)reportTemplateList;
                break;
            case OBJ_TRI:
                tempList = (ArrayList)trialTemplateList;
                break;
            case OBJ_ATT:
                tempList = (ArrayList)attributeTemplateList;
                break;
            case OBJ_ACT:
                tempList = (ArrayList)actionTemplateList;
                break;
            case OBJ_BIO:
            case OBJ_ENV:
            case OBJ_ATY:
            case OBJ_MAN:
            case OBJ_OUT:
            case OBJ_PRE:
                tempList = (ArrayList)getElementTemplateList(objType);
                break;
            case OBJ_ELE:
                tempList = (ArrayList)elementTemplateList;
        }

        Collections.sort(tempList);

        return tempList;
    }

    /**
     * Return the template object of the objType and uid parameter passed.
     * If not found return null.
     * @param objType
     * @param uid
     * @return
     */
    public EPOCObject getFromTemplateList(int objType, int uid) {
        for (Object eObj : getTemplateList(objType)) {
            if (((EPOCObject)eObj).getUID() == uid) return (EPOCObject)eObj;
        }

        return null;
    }

    /**
     * Get a complete ordered listing of all templates of Element class.
     * @return
     */
    public ArrayList<Element> getElementTemplateList() {
        Collections.sort(elementTemplateList);
        return elementTemplateList;
    }

    /**
     * This DOES NOT return the list stored by this object.  A new list is made up
     * containing elements of the type requested.
     * @param eleType
     * @return
     */
    public ArrayList<Element> getElementTemplateList(int eleType) {
        ArrayList<Element> tempList = new ArrayList();

        if (eleType == OBJ_ELE) {
            return getElementTemplateList();
        } else if (eleType == OBJ_BIO || eleType == OBJ_ENV || eleType == OBJ_ATY
                || eleType == OBJ_MAN || eleType == OBJ_OUT || eleType == OBJ_PRE) {
            for (Element ele : elementTemplateList) {
                if (ele.getModType() == eleType) {
                    tempList.add(ele);
                }
            }
        }
        Collections.sort(tempList);

        return tempList;
    }

    /**
     * Set the passed list as the template list for object type passed
     * @param templList
     * @param objType
     */
    public void setTemplateList(ArrayList templList, int objType) {

        switch (objType) {
            case OBJ_SPA:
                spatialTemplateList = templList;
            case OBJ_REP:
                reportTemplateList = templList;
            case OBJ_TRI:
                trialTemplateList = templList;
            case OBJ_CLS:
                eclassTemplateList = templList;
            case OBJ_ELE:
                elementTemplateList = templList;
            case OBJ_ATT:
                attributeTemplateList = templList;
            case OBJ_ACT:
                actionTemplateList = templList;
        }
    }

    /**
     * Add the passed list items as templates for object type passed
     * @param templList
     * @param objType
     */
    public void addTemplateList(ArrayList templList, int objType) {
        ArrayList lst = getTemplateList(objType);

        for (Object obj : templList) { lst.add(obj); }
    }

     /**
     *
     * @param obj
     */
    public void addTemplateList(EPOCObject obj) {
        // get appropriate template list and add object
        getTemplateList(obj.getObjType()).add(obj);
    }

    /**
     * Do comparison with other like objects first before adding
     * @param obj
     */
    public boolean addOnceTemplateList(EPOCObject obj) {
        boolean present = false;
        for (EPOCObject eo : getTemplateList(obj.getObjType())) {
            if (obj.compare(eo, true)) {
                present = true;
                break;
            }
        }
        if (!present) addTemplateList(obj);

        return !present;
    }

    /**
     * Add template object to template delete list to be deleted on universe save
     * @param act
     */
    public void addTemplateDeleteList(EPOCObject eObj) {
        deleteTemplateList.add(eObj);
    }

    /**
     * Remove the passed object from the relevant template list
     * @param obj
     */
    public void removeTemplateList(EPOCObject obj) {
        // get appropriate template list and remove object
        getTemplateList(obj.getObjType()).remove(obj);
    }

    /**
     * Check if there are new templates in any of the template lists
     * @return boolean true if new template(s) exists
     */
    public boolean hasNewTemplate() {
        // Check for any new templates in all template lists
        for (int oType : new int[] {OBJ_CLS, OBJ_SPA, OBJ_REP, OBJ_TRI, OBJ_ELE, OBJ_ACT, OBJ_ATT}) {
            for (Object obj : getTemplateList(oType)) {
                if (((EPOCObject)obj).getUID() == 0) return true;
            }
        }

        return false;
    }

    /**
     * Check delete list for any non-universe elements needing deletion
     * @return
     */
    public boolean doTemplateDeletes() {
                
        // Action Templates deletes
        for (EPOCObject eObj : deleteTemplateList) {

            if (!storage.delete(eObj)) return false;
        }
        // reset list to empty
        deleteTemplateList.clear();

        return true;
    }

    /*
     * Compares this complete listing of template objects with the one passed.
     * @param uni Universe
     * @param superficial
     * @return boolean - true if they are equal
     */
    public boolean compare(Templates templates) {
        ArrayList<EPOCObject> thisTemplList;
        ArrayList<EPOCObject> thatTemplList;
        
        for (int oType : new int[]{OBJ_CLS, OBJ_SPA, OBJ_REP, OBJ_TRI, OBJ_ELE, OBJ_ACT, OBJ_ATT}) {
            thisTemplList = getTemplateList(oType);
            thatTemplList = templates.getTemplateList(oType);
            if (thisTemplList.size() != thatTemplList.size()) return false;
            for (int i = 0 ; i < thisTemplList.size() ; i++) {
                if (!thisTemplList.get(i).compare(thatTemplList.get(i), false)) return false;
            }
        }

        return true;
    }

    /**
     * Create a deep clone of this templates object and of the objects in its lists
     * but not presently of any children/linked objects of them.
     * BEWARE that this does not interlink the objects.
     * @return
     */
    @Override
    protected Templates clone() {
        Templates tclone;

        try {
             tclone = (Templates)super.clone();
        } catch (CloneNotSupportedException e) {
             throw new InternalError("But we are Cloneable!!!");
        }

        tclone.eclassTemplateList.clear();
        for (EClass ec : eclassTemplateList) tclone.eclassTemplateList.add((EClass)ec.clone());
        tclone.spatialTemplateList.clear();
        for (Spatial spa : spatialTemplateList) tclone.spatialTemplateList.add((Spatial)spa.clone());
        tclone.reportTemplateList.clear();
        for (Report rep : reportTemplateList) tclone.reportTemplateList.add((Report)rep.clone());
        tclone.trialTemplateList.clear();
        for (Trial tri : trialTemplateList) tclone.trialTemplateList.add((Trial)tri.clone());
        tclone.elementTemplateList.clear();
        for (Element ele : elementTemplateList) tclone.elementTemplateList.add((Element)ele.clone());
        tclone.actionTemplateList.clear();
        for (Action act : actionTemplateList) tclone.actionTemplateList.add((Action)act.clone());
        tclone.attributeTemplateList.clear();
        for (Attribute att : attributeTemplateList) tclone.attributeTemplateList.add((Attribute)att.clone());

        return tclone;
    }
}
