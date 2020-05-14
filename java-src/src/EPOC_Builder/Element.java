/*******************************************************************************
 * Element.java
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

import static au.gov.aad.erm.EPOC_Builder.Constants.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.io.File;
import java.io.FileWriter;

/*******************************************************************************
 * EPOC Builder Element class.
 *
 * @author Troy Robertson
 * @company Australian Antarctic Division
 * @created 10/09/2009
 * @version 0.4.0, 01-09-2010
 ******************************************************************************/
public class Element extends EPOCObject<Element> {

    private int modType = 0;
    private int birthDay = 0;
    private int birthMonth = 0;
    private int eclassUID = 0;

    private EClass eclass = null;

    private ArrayList<Attribute> attributes = new ArrayList();
    private ArrayList<Action> actions = new ArrayList();
    private ArrayList<Integer> polygons = new ArrayList();
    private ArrayList<Timestep> timesteps = new ArrayList();

    // lists to hold elements requiring storage deletion on save
    private ArrayList<Action> deleteActionList = new ArrayList();
    private ArrayList<Attribute> deleteAttributeList = new ArrayList();

    // template lists
    private ArrayList<EPOCObject> deleteTemplateList = new ArrayList();
    //private ArrayList<Action> deleteActionTemplateList = new ArrayList();
    //private ArrayList<Attribute> deleteAttributeTemplateList = new ArrayList();

    /** Creates a new instance of Element */
    public Element() {
        setNextRevision();
    }

    public Element(String rev) {
        if (rev != null) setRevision(rev); else setNextRevision();
    }

    /** Creates a new instance of Element */
    public Element(int eleType) {
        modType = eleType;
        setNextRevision();
    }

    /*
     * Instantiate Element of eleType with data loaded from storage with passed eleUID
     */
    public Element(int eleUID, Templates templates) {
        uid = eleUID;
        this.setTemplates(templates);
        storage.load(this);

        setEClassObject();
        setActionLinkedObjects();
    }

    public void setEClassObject() {
        if (getEClassUID() > 0) {
            for (EPOCObject ecObj : templates.getTemplateList(OBJ_CLS)) {
                if (getEClassUID() == ecObj.getUID()) setEClass((EClass)ecObj);
            }
        } else if (getEClassUID() == -1) {
            // broken link
            setEClass(new EClass(-1, templates));
        }
    }

    /**
     * find and set transformtoperiod action object, dataset attribute object,
     * and timestep dataset attribute objects
     * Has to be done after all actions/attributes are loaded for element so
     * that same object can be used rather than loading another copy
     */
    private void setActionLinkedObjects() {
        // For active elements
        for (Action act : actions) {
            act.setTransformFromList(actions);
            act.setDatasetFromList(attributes);
            act.setTimestepDatasetsFromList(attributes);
        }
    }

    /**
     * find and set related element objects from the passed list
     * has to be done after all elements are loaded so that same object
     * can be used rather than loading another copy.
     * Called by universe after all elements are loaded.
     *
     * @param eleList
     */
    public void setActionRelatedObjectsFromList(ArrayList eleList) {
        for (Action act : actions) act.setRelatedElementsFromList(eleList);
    }

    /**
     * Set this actions or attributes position to the highest + 1 of its type
     * @param actAtt
     */
    public void setNextHighestPosition(EPOCObject actAtt) {
        int pos = 1;
        for (EPOCObject eobj : getActAttList(actAtt.getObjType())) {
            if (eobj.getPosition() >= pos) pos = eobj.getPosition() + 1;
        }
        actAtt.setPosition(pos);
    }

    /**
     * Set the element module type of this element object
     * @param mType
     */
    public void setModType(int mType) { modType = mType; }

    /**
     * Set the EPOC Class uid for this element object
     * @param mType
     */
    public void setEClassUID(int ecUID) { eclassUID = ecUID; }

    /**
     * Set eClass object
     * @param ec
     */
    public void setEClass(EClass ec) {
        eclass = ec;
        eclassUID = 0;
    }

    /**
     * Add Templates object to this element.  If recurse then do same with
     * child Actions and Attributes.
     * @param templ - templates object
     * @param recurse - operate on object children
     */
    @Override
    public void setTemplates(Templates templ, boolean recurse) {
        setTemplates(templ);
        if (recurse) {
            if (eclass != null) eclass.setTemplates(templ, recurse);
            for (Action act : actions) act.setTemplates(templ, recurse);
            for (Attribute att : attributes) att.setTemplates(templ, recurse);
            for (Timestep ts : timesteps) {
                ts.setTemplates(templ);
                if (ts.getDataset() != null) ts.getDataset().setTemplates(templ, recurse);
            }
        }
    }

    /**
     * Recursively add self to templates object listings
     * @param recurse
     */
    @Override
    public void addSelfToTemplates(boolean recurse) {
        templates.addOnceTemplateList(this);
        if (recurse) {
            if (eclass != null) eclass.addSelfToTemplates(recurse);
            for (Action act : actions) act.addSelfToTemplates(recurse);
            for (Attribute att : attributes) att.addSelfToTemplates(recurse);
            for (Timestep ts : timesteps) {
                if (ts.getDataset() != null) ts.getDataset().addSelfToTemplates(recurse);
            }
        }
    }

    /**
     * Recursive revision of EPOCObject setTemplate
     * @param puid - parent uid
     * @param recurse - operate on object children
     */
    @Override
    public void unsetAsTemplate(int puid, boolean recurse) {
        unsetAsTemplate(puid);
        if (recurse) {
            if (eclass != null) eclass.unsetAsTemplate(this.getUID(), recurse);
            // Actions
            for (Action act : actions) act.unsetAsTemplate(this.getUID(), recurse);
            // Attributes
            for (Attribute att : attributes) att.unsetAsTemplate(this.getUID(), recurse);
            for (Timestep ts : timesteps) {
                if (ts.getDataset() != null) ts.getDataset().setTemplate(false);
            }
        }
    }

    /**
     * Set day of birth
     * @param bday
     */
    public void setBirthDay(int bday) { birthDay = bday; }

    /**
     * Set month of birth
     * @param bmth
     */
    public void setBirthMonth(int bmth) { birthMonth = bmth; }

    /**
     * Set the birth day and month
     *
     * @param bDay
     * @param bMonth
     */
    public void setBirthDate(int bDay, int bMonth) {
        birthDay = bDay;
        birthMonth = bMonth;
    }

    public void addPolygon(int i) { polygons.add(i); }

    public void addTimestep(Timestep ts) { timesteps.add(ts); }

    public void setPolygonsString(String polys) {
        polygons.clear();
        if (polys != null && !polys.equals("")) {
            for (String pStr : polys.split(",")) {
                polygons.add(Integer.parseInt(pStr));
            }
        }
    }

    public void setPolygons(ArrayList<Integer> pList) { polygons = pList; }

    public void setTimesteps(ArrayList<Timestep> tsteps) { timesteps = tsteps; }

    public void setAttributes(ArrayList<Attribute> attrList) {
        attributes = attrList;
    }

    public void setActions(ArrayList<Action> actList) { actions = actList; }

    public void addDeleteList(EPOCObject actAtt) {
        if (actAtt instanceof Action) addDeleteList((Action)actAtt);
        if (actAtt instanceof Attribute) addDeleteList((Attribute)actAtt);
    }

    public void addDeleteList(Action act) { deleteActionList.add(act); }

    public void addDeleteList(Attribute att) { deleteAttributeList.add(att); }

    /**
     * Add template action/attribute to template delete list to be deleted on universe save
     * @param act/att
     */
    public void addTemplateDeleteList(EPOCObject eobj) { deleteTemplateList.add(eobj); }

    /**
     * Add template attribute to template delete list to be deleted on universe save
     * @param att
     */
    //public void addTemplateDeleteList(Attribute att) { deleteTemplateList.add(att); };

    /**
     * Return an list containing all Actions
     * @return
     */
    public ArrayList<Action> getActions() {
        Collections.sort(actions);
        return actions;
    }

    /**
     * Return a list containing all Attributes
     * @return
     */
    public ArrayList<Attribute> getAttributes() {
        Collections.sort(attributes);
        return attributes;
    }

    /**
     * Return a list containing all Actions or Attributes dependent on
     * objtype parameter
     * @param objtype
     * @return
     */
    public ArrayList<EPOCObject> getActAttList(int objtype) {
        if (objtype == OBJ_ACT) return (ArrayList)getActions();
        if (objtype == OBJ_ATT) return (ArrayList)getAttributes();
        return new ArrayList();
    }

    /**
     * Return Element module type
     * @return
     */
    public int getModType() { return modType; }

    /**
     * Return uid of EClass object
     * @return
     */
    public int getEClassUID() {
        if (eclass != null) return eclass.getUID();
        return eclassUID;
    }

    /**
     * Return EClass object or null
     * @return
     */
    public EClass getEClass() { return eclass; }

    /**
     * Is modtype passed a valid element module type
     * @param eleType
     * @return
     */
    static boolean isModuleType(int modtype) {
        return (!getModuleTypeName(modtype).equals(""));
    }

    /**
     * Get the String name associated with the modtype parameter
     * @param modtype
     * @return
     */
    static String getModuleTypeName(int modtype) {
        switch (modtype) {
            case OBJ_ELE:
                return "Element";
            case OBJ_BIO:
                return "Biota";
            case OBJ_ENV:
                return "Environment";
            case OBJ_ATY:
                return "Activity";
            case OBJ_MAN:
                return "Management";
            case OBJ_OUT:
                return "Output";
            case OBJ_PRE:
                return "Presentation";
        }

        return "";
    }

    /**
     * Test if module type name parameter is a valid name
     * @param modtypeName
     * @return
     */
    static boolean isModuleTypeName(String modtypeName) {
        return (getModuleTypeFromName(modtypeName) > 0);
    }

    /**
     * Get the mod type constant associated with the mod name parameter
     * @param modtypeName
     * @return
     */
    static int getModuleTypeFromName(String modtypeName) {
        if (modtypeName.equalsIgnoreCase("Element")) return OBJ_ELE;
        if (modtypeName.equalsIgnoreCase("Biota")) return OBJ_BIO;
        if (modtypeName.equalsIgnoreCase("Environment")) return OBJ_ENV;
        if (modtypeName.equalsIgnoreCase("Activity")) return OBJ_ATY;
        if (modtypeName.equalsIgnoreCase("Management")) return OBJ_MAN;
        if (modtypeName.equalsIgnoreCase("Output")) return OBJ_OUT;
        if (modtypeName.equalsIgnoreCase("Presentation")) return OBJ_PRE;

        return 0;
    }

    /**
     * Get element day of birth
     * @return
     */
    public int getBirthDay() { return birthDay; }

    /**
     * Get element month of birth
     * @return
     */
    public int getBirthMonth() { return birthMonth; }

    /**
     * Return polygon list as a string of comma separated indexes
     * @return
     */
    public String getPolygonsString() {
        String pStr = null;

        for (Integer pInt : polygons) {
            pStr = (pStr == null ? "" : pStr + ",");
            pStr += (pInt == null ? "" : pInt.toString());
        }

        return (pStr == null ? "" : pStr);
    }

    /**
     * Return list containing all polygon indexes
     * @return
     */
    public ArrayList<Integer> getPolygons() { return polygons; }

    public ArrayList<Timestep> getTimesteps() { return timesteps; }

    /**
     * Return Action object in list with parameter uid, else null
     * @param actUID
     * @return
     */
    public Action getAction(int actUID) {
        int i = isAction(actUID);
        if (i >= 0) return (Action)actions.get(i);

        return null;
    }

    /**
     * Deletes template from database.
     * This assumes that a check has been performed to see that the template
     * is not being used by any object.  Will result in a broken link where it
     * was a linked object.
     * @param eobj
     * @return
     */
    public void deleteTemplate(EPOCObject eobj) {
        if (eobj instanceof Action || eobj instanceof Attribute) {
            // Remove it from the template listing
            getTemplates().removeTemplateList(eobj);

            // If has been in storage then it will need deleting
            if (eobj.getUID() > 0) addTemplateDeleteList(eobj.clone());

            // Set it as broken for where it is still referenced as a linked object
            eobj.setBroken();
        }
    }

    /*
     * If the passed dates overlap with an existing timestep then return it
     * taking into account rollover of timespan across Dec 31
     */
    public Timestep getOverlappingTimestep(int stDay, int stMth, int enDay, int enMth,
                                                                    int bDay, int bMth) {
        //Timestep[] tsps = getTimestepArray(false);

        for (Timestep tsp : timesteps) {

            int stDay2 = tsp.getStartDay();
            int stMth2 = tsp.getStartMonth();
            int enDay2 = tsp.getEndDay();
            int enMth2 = tsp.getEndMonth();

            // replace birth days
            if (stDay == 99) stDay = bDay;
            if (stMth == 99) stMth = bMth;
            if (enDay == 99) enDay = bDay;
            if (enMth == 99) enMth = bMth;
            if (stDay2 == 99) stDay2 = bDay;
            if (stMth2 == 99) stMth2 = bMth;
            if (enDay2 == 99) enDay2 = bDay;
            if (enMth2 == 99) enMth2 = bMth;

            // check if dates cross Dec 31
            if (enMth < stMth || (enMth == stMth && enDay < stDay)) enMth += 12;
            if (enMth2 < stMth2 || (enMth2 == stMth2 && enDay2 < stDay2)) enMth2 += 12;

            // if start date falls on or between next timestep dates
            //           |--
            //         |------|
            if (dateCompare(stDay, stMth, stDay2, stMth2) >= 0
                    && dateCompare(stDay, stMth, enDay2, enMth2) <= 0) {
                return tsp;
            }
            // if end date falls on or between next timestep dates
            //          --|
            //         |------|
            if (dateCompare(enDay, enMth, stDay2, stMth2) >= 0
                    && dateCompare(enDay, enMth, enDay2, enMth2) <= 0) {
                return tsp;
            }
            // if start and end of date span next timestep dates
            //       |---------|
            //         |------|
            if (dateCompare(stDay, stMth, stDay2, stMth2) <= 0
                    && dateCompare(enDay, enMth, enDay2, enMth2) >= 0) {
                return tsp;
            }
            // if ONE of the end dates crosses Dec 31 (>12) then check if it is greater
            // than other start date
            // J-----------D----------D
            //  |---| |------|
            if (enMth > 12 ^ enMth2 > 12) {
                if (enMth > 12 && dateCompare(stDay2, stMth2, enDay, enMth-12) <= 0) return tsp;
                if (enMth2 > 12 && dateCompare(stDay, stMth, enDay2, enMth2-12) <= 0) return tsp;
            }
        }

        return null;
    }

    /*
     * return <0 if date1 < date2
     * return >0 if date1 > date2
     * return 0 if equal
     */
    private int dateCompare(int day1, int mth1, int day2, int mth2) {
        if (mth1 == mth2) return day1 - day2;
        return mth1 - mth2;
    }


    /*
     * Returns an ordered array of the actions timesteps
     * If addDummy is true then add an extra dummy timestep for 'New...'
     */
    public Timestep[] getTimestepArray(boolean addDummy) {
        int tSize = timesteps.size();

        if (addDummy) tSize += 1;
        Timestep[] tList = new Timestep[tSize];

        int i = 0;
        for (Timestep ts : timesteps) {
            tList[i] = ts;
            i++;
        }

        if (addDummy) {
            // create dummy one for new timestep in list
            Timestep ts = new Timestep();
            ts.setSteps(0, 0, 0, 0);
            tList[tSize-1] = ts;
        }

        // Sort list
        Arrays.sort(tList);

        return tList;
    }

    /*
     * Returns an ordered array of timesteps which are clones of the originals
     * and have birthday/month placeholders(99) replaced with actual figures passed
     */
    public Timestep[] getTimestepArrayClone(int bDay, int bMth) {
        Timestep[] tList = new Timestep[timesteps.size()];
        int i = 0;

        for (Timestep ts : timesteps) {
            Timestep newTS = ts.clone(EPOC_CLN, null);

            int sd = (newTS.getStartDay() == 99) ? bDay : newTS.getStartDay();
            int sm = (newTS.getStartMonth() == 99) ? bMth : newTS.getStartMonth();
            int ed = (newTS.getEndDay() == 99) ? bDay : newTS.getEndDay();
            int em = (newTS.getEndMonth() == 99) ? bMth : newTS.getEndMonth();
            newTS.setSteps(sd, sm, ed, em);

            tList[i] = newTS;
            i++;
        }

        // Sort list
        Arrays.sort(tList);

        return tList;
    }

    /**
     * Remove the Action passed if it exists
     * If it has a uid (been stored before) then add it to the delete list first
     * @param act Action
     * @return boolean success
     */
    public boolean removeAction(Action act) {
        // get object and put it in delete list to be deleted (or unlinked) on universe save
        if (act.getUID() > 0) addDeleteList(act);

        // Replace any linked objects point to this with a broken copy
        Action brokenAct = (Action)act.clone();
        brokenAct.setBroken();
        replaceLinkWith(act, brokenAct);

        return actions.remove(act);
    }

    /**
     * Remove the Attribute passed if it exists
     * If it has a uid (been stored before) then add it to the delete list first
     * @param att Attribute
     * @return boolean success
     */
    public boolean removeAttribute(Attribute att) {
        // get object and put it in delete list to be deleted (or unlinked) on universe save
        if (att.getUID() > 0) addDeleteList(att.clone());

        // Replace any linked objects point to this with a broken copy
        Attribute brokenAtt = (Attribute)att.clone();
        brokenAtt.setBroken();
        replaceLinkWith(att, brokenAtt);

        return attributes.remove(att);
    }

    /**
     * Add new Action if it does not already exist in it.
     *
     * @param act Action
     * @return boolean success
     */
    public boolean addAction(Action act) {
        if (actions.indexOf(act) < 0) {
            actions.add(act);
            setNextHighestPosition(act);
            return true;
        }

        return false;
    }

    /**
     * Add new Attribute if it does not already exist in it.
     *
     * @param att Attribute
     * @return boolean success
     */
    public boolean addAttribute(Attribute att) {
        if (attributes.indexOf(att) < 0) {
            attributes.add(att);
            setNextHighestPosition(att);
            return true;
        }

        return false;
    }

    /**
     * Why doesn't Java do this itself?? TODO
     * @param actAtt
     * @return
     */
    public boolean addActAtt(EPOCObject actAtt) {
        if (actAtt instanceof Action) return addAction((Action)actAtt);
        if (actAtt instanceof Attribute) return addAttribute((Attribute)actAtt);
        return false;
    }

    /**
     * Instantiate a new attribute or action object of the type passed and add
     * it to the element. Set its position as the last for that module.
     *
     * @param objType
     * @return
     */
    public EPOCObject createNewActAtt(int objType, boolean asTemplate, Universe uni) {
        EPOCObject eo = null;
        if (objType == OBJ_ACT) eo = new Action();
        if (objType == OBJ_ATT) eo = new Attribute();

        eo.setTemplates(templates);
        eo.setParentUID(getUID());
        uni.setNextAvailableVersion(eo, false);
        setNextHighestPosition(eo);

        if (asTemplate) eo.template();
        addActAtt(eo);

        return eo;
    }

    /**
     * Replace original action or attribute with its edited copy.
     * If it was originally a template then this will involve unlinking the original template
     * and untemplating the modified revision
     * NOTE: Modified copy will be re-versioned.
     *
     * @param origActAtt
     * @param modActAtt
     * @param uni
     */
    public void replaceModifiedActAtt(EPOCObject origActAtt, EPOCObject modActAtt, Universe uni) {

        // if its a modified template
        if (origActAtt.isTemplate()) {
            // then remove link
            addDeleteList(origActAtt);

            // the newly created act/att can be unset as a template and then have its uid unset
            // causing it to be saved as a new object
            modActAtt.unsetAsTemplate(getUID());
            modActAtt.setUID(0);
            uni.setNextAvailableVersion(modActAtt, true);

            // Set timestep objects as new too.
            if (modActAtt instanceof Action) {
                for (Object tsObj : ((Action)modActAtt).getTimesteps()) {
                    ((Timestep)tsObj).setUID(0);
                }
            }

            // Also check this elements actions for any attribute datasets or transform actions
            // which pointed to the template object and change to point to modActAtt
            if (modActAtt instanceof Action) {
                replaceModifiedTransformActions((Action)origActAtt, (Action)modActAtt);
            } else {
                replaceModifiedDatasetAttributes((Attribute)origActAtt, (Attribute)modActAtt);
            }
        }

        replaceActAtt(origActAtt, modActAtt);
    }

    /**
     * Replace any transform action objects equal to origAct
     * with the modified modAct
     * Dont modify templated Actions ATM
     *
     * @param origAct
     * @param modAct
     */
    private void replaceModifiedTransformActions(Action origAct, Action modAct) {
        for (Action act : getActions()) {
            if (origAct == act.getTransform()) {
                if ((!act.isTemplate() || modAct.isTemplate()) && EPOC_AUTO_MATCH_LINK_OBJ) {
                    act.setTransform(modAct);
                } else {
                    act.setTransform((Action)origAct.hollow());
                }
            }
        }
    }

    /**
     * Replace any dataset attribute objects equal to origAtt
     * with the modified modAtt
     * Don't modify templated Actions ATM
     * @param origAtt
     * @param modAtt
     */
    private void replaceModifiedDatasetAttributes(Attribute origAtt, Attribute modAtt) {
        for (Action act : getActions()) {
            if (origAtt == act.getDataset()) {
                if ((!act.isTemplate() || modAtt.isTemplate()) && EPOC_AUTO_MATCH_LINK_OBJ) {
                    act.setDataset(modAtt);
                } else {
                    act.setDataset((Attribute)origAtt.hollow());
                }
            }
            // and check timestep dataset attributes
            for (Timestep ts : act.getTimesteps()) {
                if (origAtt == ts.getDataset()) {
                    if ((!act.isTemplate() || modAtt.isTemplate()) && EPOC_AUTO_MATCH_LINK_OBJ) {
                        ts.setDataset(modAtt);
                    } else {
                        ts.setDataset((Attribute)origAtt.hollow());
                    }
                }
            }
        }
    }

    /**
     * Replace Action / Attribute with passed new action based on uid
     * @return boolean true if successful
     * @param act
     * @param newAct
     * @return
     */
    public boolean replaceActAtt(EPOCObject origActAtt, EPOCObject modActAtt) {
        ArrayList list;
        if (origActAtt.getObjType() == OBJ_ACT) list = actions; else list = attributes;

        int index = list.indexOf(origActAtt);
        if (index >= 0) {
            list.remove(index);
            list.add(index, modActAtt);
            return true;
        }

        return false;
    }

    /**
     * Return index of Action with uid parameter
     * @param actUID
     * @return list position based on uid
     * @return -1 if not found in list
     */
    public int isAction(int actUID) {
        if (actUID != 0) {
            for (Action act : actions) {
                if (act.getUID() == actUID) return actions.indexOf(act);
            }
        }

        return -1;
    }

    /**
     * Return Attribute object with uid of parameter, else null
     * @param attUID
     * @return
     */
    public Attribute getAttribute(int attUID) {
        int i = isAttribute(attUID);
        if (i >= 0) {
            return (Attribute)attributes.get(i);
        }
        return null;
    }

    /**
     * Return index of Attribute with uid parameter
     * @param actUID
     * @return list position based on uid
     * @return -1 if not found in list
     */
    public int isAttribute(int attUID) {
        if (attUID != 0) {
            for (Attribute att : attributes) {
                if (att.getUID() == attUID) return attributes.indexOf(att);
            }
        }

        return -1;
    }

    /**
     * Swap list indexes of objects passed, ensuring they are of the same
     * object type.  Only Element child objects (Actions and Attributes)
     *
     * @param fstObj
     * @param secObj
     */
    public void swapIndexes(EPOCObject fstObj, EPOCObject secObj) {
        int fstPos = -1, secPos = -1;

        if (fstObj.getObjType() == secObj.getObjType()) {

            // swap actual list positions
            ArrayList objList = new ArrayList();
            if(fstObj instanceof Action) {
                objList = actions;
            } else if (fstObj instanceof Attribute) {
                objList = attributes;
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

    /**
     *
     * @return
     */
    @Override
    public boolean saveToStorage() {
        if (!EPOCObject.testName(shortname)) {
            Messages.addErrMsg("Element (" + shortname + ") must be named and may only contain\n" +
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

    /*
     * Check delete list for any non-universe objects needing deletion
     */
    public boolean doDeletes() {
        boolean retVal = true;

         // for each delete list
        for (ArrayList list : new ArrayList[]{deleteActionList, deleteAttributeList, deleteTemplateList}) {
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

    /*
     * Check if object passed exists in the member list specified by objType
     * Compare by uid if object has one, otherwise by object itself
     */
    public boolean isMember(EPOCObject obj) {
        if (obj instanceof Action) {
            if (getActions().contains((Action)obj)) return true;
        } else if (obj instanceof Attribute) {
            if (getAttributes().contains((Attribute)obj)) return true;
        }

        return false;
    }

    /**
     * Check if object passed is linked to this element or any of its children
     * @param obj
     * @return
     */
    public boolean isLinked(EPOCObject obj) {
        if (obj instanceof EClass) {
            if (eclass != null && eclass.equals(obj)) return true;
        } else {
            for (Action act : getActions()) if (act.isLinked(obj)) return true;
            for (Timestep ts : timesteps) if (ts.isLinked(obj)) return true;
        }

        return false;
    }

    /*
     * Check whether template is used by another stored element
     * @return int count
     */
    public boolean templateUsedByOther(EPOCObject obj) {
        return storage.templateUsedByOther(uid, obj.getUID(), obj.getObjType());
    }

    public boolean validate(Universe uni, boolean recurse) {
        boolean passed = true;

        rex.clear();

        // Is EPOC ID unique
        if (!uni.isEPOCIDUnique(this)) {
            Messages.addErrMsg("EPOC ID '" + epocID + "' is non-unique");
            passed = false;
        }

        // Check EPOC Class name
        if (rex.hasEngine()) {
            if (!rex.library("EPOC")) {
                Messages.addErrMsg("R library 'EPOC' failed to load.  Unable to test EPOC Classname!");
                passed = false;
            } else {
                if (eclass == null) {
                    Messages.addErrMsg("No EPOC Classname selected!");
                    passed = false;
                } else if( !rex.test("isClass('" + eclass.getShortName() + "')")) {
                    // Not EPOC package EClass so must be here
                    if (!eclass.validate()) passed = false;
                }
            }
        } else {
            Messages.addErrMsg("No JRI Engine found!\nUnable to test existence of EPOC R Library or EPOC Classes!");
            passed = false;
        }

        // recurse down children?
        if (recurse) {
            // Attributes
            for (Attribute att : getAttributes()) {
                if (!att.validate(this)) passed = false;
            }

            // Validate Actions
            for (Action act : getActions()) {
                if (!act.validate(this, uni)) passed = false;
            }

            // Check that it has timesteps if it is a timestep action and that each
            // ts dataset is present
            boolean found = false, hasTS = false;
            for (Timestep ts : timesteps) {
                // Check that ts action is available in element
                hasTS = true; found = false;
                if (ts.getDataset() != null) {
                    if (ts.getDataset().getUID() >= 0) {
                        for (Action act : actions) {
                            if (act == ts.getAction()) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            Messages.addErrMsg("Timestep is for an action which is not present in this element!");
                            passed = false;
                        }
                    } else {
                        Messages.addErrMsg("Contains broken link to missing timestep action!");
                        passed = false;
                    }
                }
                // Check that ts dataset is available in element
                found = false;
                if (ts.getDataset() != null) {
                    if (ts.getDataset().getUID() >= 0) {
                        for (Attribute att : attributes) {
                            if (att == ts.getDataset()) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            Messages.addErrMsg("Timestep contains dataset which is not present as an attribute of element!");
                            passed = false;
                        }
                    } else {
                        Messages.addErrMsg("Contains broken link to missing timestep dataset!");
                        passed = false;
                    }
                }
            }
            if (!hasTS) {
                Messages.addErrMsg("Timestep action contains no timesteps!");
                passed = false;
            }

        }

        return passed;
    }

    /*
     * Write self as text, in the form of an R assignment, to FileWriter
     */
    public void writeAsR(String dirPath) {
        File outputFile = new File(dirPath + File.separator + "data" + File.separator + getDisplayName() + ".data.R");
        Date dt = new Date();

        try {
            outputFile.createNewFile();
            FileWriter out = new FileWriter(outputFile);

            out.write("########################################################\n");
            out.write("# ELEMENT - " + name + " (" + revision + ")\n");
            out.write("# Description: " + super.prependEachLine(description, "#              ") + "\n");
            out.write("#\n");
            out.write("# Generated by EPOC Builder\n");
            out.write("# Date: " + dt.toString() + "\n");
            out.write("########################################################\n\n");

            // List assignment
            out.write(shortname + " <- list()\n\n");

            // Signature
            super.writeSignatureAsR(out, shortname);

            out.write("################## POLYGONS ##################\n");
            out.write(shortname + "$polygonsN          <- " + getPolygons().size() + "\n\n");
            out.write(shortname + "$polygons           <- list(RefNumbers = c(" +  getPolygonsString() + "))\n\n");

            out.write("### BIRTHDAY\n");
            out.write("# Point of origin for updating age and for growth parameters etc.\n");
            out.write(shortname + "$birthdate          <- list(Day = " + birthDay + ", Month = " + birthMonth + ")\n\n");

            out.write("################## ATTRIBUTES ##################\n");
            for (Object obj : attributes) {
                ((Attribute)obj).writeAsR(out, shortname);
            }

            out.write("################## FUNCTIONS ##################\n");
            out.write(shortname + "$Functions           <- list(\n");
            String fnStr = "";
            for (Action sAct : getActions()) {
                String dset = "";

                if (sAct.isSupport()) {
                    Attribute dsAtt = sAct.getDataset();
                    dset = (dsAtt == null ? "NULL" : shortname + "$" + dsAtt.getShortName());

                    fnStr += (!fnStr.equals("") ? ",\n" : "");
                    fnStr += "\t" + sAct.getShortName() + " = list(actionMethod = \"" + sAct.getShortName() + "\",\n";
                    fnStr += "\t\t\t\t\tactionFile        = file.path(RootPath, \"code\", \""
                            + sAct.getDisplayName() + ".R\"),\n";
                    fnStr += "\t\t\t\t\tdset              = " + dset + ")";
                }
            }
            out.write((fnStr.equals("") ? "\tNULL" : fnStr));
            out.write("\n)\n\n");

            out.write("################### TIMESTEPS ####################\n");
            //  gather up all action timesteps, order them, slice and dice
            out.write(shortname + "$timesteps <- list(\n" + getRFromAllTimesteps() + ")\n\n");

            // declare data
            out.write(shortname);

            out.close();

            // Write out actions to their own files
            for (int i=0; i < actions.size(); i++) {
                Action act = (Action)actions.get(i);
                act.writeAsR(dirPath, (getEClass() != null ? getEClass().getDisplayName() : getDisplayName()));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

        /**
     * Return a string representing all timestep slices for all actions for this element
     */
    private String getRFromAllTimesteps() {
        ArrayList tsList = new ArrayList();
        String tsStr = "";
        int cnt = 0, actCnt = 0, currDay = 0, currMth = 0;
        boolean currKnifeEdge = false;

        // get all action timesteps, replacing birthday flags with birthdates
        for (int i = 0; i < actions.size(); i++) {
            Action act = (Action)actions.get(i);
            if (act.isAction()) {
                Timestep[] aTsps = ((Action)actions.get(i)).getTimestepArrayClone(birthDay, birthMonth);
                for (int j = 0; j < aTsps.length; j++) {
                    tsList.add(aTsps[j]);
                }
            }
        }

        // order by start date
        Collections.sort(tsList);

        for (Object tsObj : tsList) {
            // build timestep string
            if (((Timestep)tsObj).getStartDay() != currDay
                    || ((Timestep)tsObj).getStartMonth() != currMth
                    || ((Timestep)tsObj).isKnifeEdge()) {
                currDay = ((Timestep)tsObj).getStartDay();
                currMth = ((Timestep)tsObj).getStartMonth();
                actCnt = 0;
                if (cnt > 0) tsStr += "\n\t\t)\n\t),\n";
                tsStr += "\tTimestep_" + (cnt + 1) + " = list(calday = dayFromDate(" +
                            currDay + ", " + currMth + "),\n";
                tsStr += "\t\tactionsN=NULL,\n";
                tsStr += "\t\tactions=list(\n";
                cnt++;
            }
            // build dataset string
            Action act = getAction(((EPOCObject)tsObj).getParentUID());
            String dset = "", stp = "";
            Attribute dsAtt = (act.isSetup() ? act.getDataset() : ((Timestep)tsObj).getDataset());
            dset = (dsAtt == null ? "NULL" : shortname + "$" + dsAtt.getShortName());

            // build transform string
            Action stpAct = act.getTransform();
            if (stpAct == null) {
                stp = "NULL";
            } else {
                stp =  "list(actionMethod = \"" + stpAct.getDisplayName() + "\",\n";
                stp += "\t\t\t\t\t\t\t\t\t\t\t actionFile = file.path(RootPath, \"code\", \"" + stpAct.getDisplayName() + ".R\"),\n";
                stp += "\t\t\t\t\t\t\t\t\t\t\t dset       = NULL)";
            }
            //build related elements string
            String relStr = "";
            for (Object oEle : act.getRelatedElements()) {
                if (!relStr.equals("")) relStr += ", ";
                relStr += "\"" + EPOCObject.getObjectTypeName(((Element)oEle).getModType()) + "\", \"" + ((Element)oEle).getShortName() + "\"";
            }

            // prepare R string
            if (actCnt > 0) tsStr += ",\n";
            tsStr += "\t\t\t" + act.getShortName() + " = list(\n";
            tsStr += "\t\t\t\t\tactionMethod      = \"" + act.getDisplayName() + "\",\n";
            tsStr += "\t\t\t\t\tactionFile        = file.path(RootPath, \"code\", \"" + act.getDisplayName() + ".R\"),\n";
            tsStr += "\t\t\t\t\tTS_type           = \"" + EPOCObject.getTSTypeName(((Timestep)tsObj).getStepType()) + "\",\n";
            tsStr += "\t\t\t\t\tTS_timing         = \"" + EPOCObject.getTSTypeName(((Timestep)tsObj).getStepTiming()) + "\",\n";
            tsStr += "\t\t\t\t\tTransformToPeriod = " + stp + ",\n";
            tsStr += "\t\t\t\t\trelated.elements  = " + (relStr.equals("") ? "NULL,\n" : "matrix(c(" + relStr + "), ncol=2, byrow=TRUE),\n");
            tsStr += "\t\t\t\t\tdset              = " + dset + "\n";
            tsStr += "\t\t\t)";

            actCnt++;
        }
        if (actCnt > 0) tsStr += "\n\t\t)\n";   // end actions list
        if (cnt > 0) tsStr += "\t)\n";        // end timestep

        return tsStr;
    }

    /*
     * Return R string for all timesteps in list, removing earliest enders and splitting
     * all others at this point (change start dates to day after earliest enders end date)
     * Assuming all ts in list start at same date!
     * If day > 0 or mth > 0 then split at this date
     */
    private String getRFromNextEndingTS(ArrayList aList, int day, int mth) {
        String tsStr = "";

        if (aList.size() == 0) return "";
        int index = getNextEndingIndex(aList);

        if (day == 0 || mth == 0) {
            day = ((Timestep)aList.get(index)).getEndDay();
            mth = ((Timestep)aList.get(index)).getEndMonth();
        }
        Timestep activeTS = (Timestep)aList.get(index);
        // split all active ts and write out first part
        int actCnt = 0;
        for (int i = 0; i < aList.size(); i++) {
            Timestep t = (Timestep)aList.get(i);
            Action act = getAction(t.getParentUID());

            // prepare R string
            if (actCnt > 0) tsStr += ",\n";
            tsStr += "\t\t\t" + act.getShortName() + " = list(actionMethod = \"" + act.getShortName() + "\",\n";
            tsStr += "\t\t\t\t\t\t\tactionFile        = file.path(RootPath, \"code\", \"" + act.getShortName() + "." + act.getRevision() + ".R\"),\n";
            tsStr += "\t\t\t\t\t\t\tTS_type           = \"" + EPOCObject.getTSTypeName(t.getStepType()) + "\",\n";
            tsStr += "\t\t\t\t\t\t\tTS_timing         = \"" + EPOCObject.getTSTypeName(t.getStepTiming()) + "\",\n";
            tsStr += "\t\t\t\t\t\t\tTransformToPeriod = NULL, # ?????\n";
            tsStr += "\t\t\t\t\t\t\trelated.elements  = NULL, # ?????\n";
            tsStr += "\t\t\t\t\t\t\tdset              = getParam(.Object, \"" + act.getShortName() + "\")\n";
            tsStr += "\t\t\t\t\t\t)";
            // remove any which end on same end date
            if (t.getEndDay() == day && t.getEndMonth() == mth) {
                aList.remove(i);
                i--;
            // split others
            } else {
                int[] dt = incrementDate(new int[]{day, mth});
                t.setSteps(dt[0], dt[1], 0, 0);
            }
            actCnt++;
        }

        return tsStr;
    }

    /*
     * Return list index of ts with earliest end date
     * If empty list return -1
     */
    private int getNextEndingIndex(ArrayList aList) {
        int index = 0;

        if (aList.size() == 0) return -1;

        int minDay = ((Timestep)aList.get(index)).getEndDay();
        int minMth = ((Timestep)aList.get(index)).getEndMonth();

        for (int i = 1; i < aList.size(); i++) {
            Timestep aTS = (Timestep)aList.get(i);
            if (aTS.getEndMonth() < minMth
                || (aTS.getEndMonth() == minMth && aTS.getEndDay() < minDay)) {
                        index = i;
                        minDay = ((Timestep)aList.get(i)).getEndDay();
                        minMth = ((Timestep)aList.get(i)).getEndMonth();
            }
        }

        return index;
    }

    /*
     * return number of days from start date until date passed
     */
    private int daysFromDate(int stDay, int stMth, int day, int mth) {
        int days = 0;

        if (stMth == mth) return (day - stDay) + 1;
        int[] daysPerMth = {31,28,31,30,31,30,31,31,30,31,30,31};

        days = (daysPerMth[stMth-1] - stDay) + 1;
        for (int i = stMth; i < mth; i++) {
            days += daysPerMth[i-1];
        }
        days += day;

        return days;
    }

    /*
     * Increment date passed by one day
     * Date passed and returned in the form dt[0] = day, dt[1] = mth
     */
    private int[] incrementDate(int[] dt) {
        int[] daysPerMth = {31,28,31,30,31,30,31,31,30,31,30,31};

        if (dt[0] < daysPerMth[dt[1]-1]) {
            dt[0]++;
        } else {
            dt[1] = (dt[1] == 12) ? 1 : dt[1]++;
            dt[0] = 1;
        }

        return dt;
    }

    /*
     * Decrement date passed by one day
     * Date passed and returned in the form dt[0] = day, dt[1] = mth
     */
    private int[] decrementDate(int[] dt) {
        int[] daysPerMth = {31,28,31,30,31,30,31,31,30,31,30,31};

        if (dt[0] > 1) {
            dt[0]--;
        } else {
            dt[1] = (dt[1] == 1) ? 12 : dt[1]--;
            dt[0] = daysPerMth[dt[1]-1];
        }

        return dt;
    }

    /**
     * Break all links to anything but the primary tree.
     * @param root
     */
    public void pruneToPrimaryTree(EPOCObject root) {

    }

    /**
     * Break any linked objects which are not local to root passed.
     * If localTo is null then break all linked objects
     */
    @Override
    public void breakLinks(EPOCObject root, Element parentEle) {
        if (eclass != null) {
            eclass.setBroken();
            if (root != null && root instanceof Universe) {
                // search universe to see if eclass is local
                if (root.getTemplates().getTemplateList(OBJ_CLS).contains(eclass)) eclass.setBroken(false);
            }
        }

        for (Action act : getActions()) act.breakLinks(root, this);
    }

    /**
     * Substitute any child objects (actions and attributes) with a
     * comparable template if one can be found
     */
    @Override
    public void substituteMembersWithTemplates() {
        boolean replaced = false;

        // Attributes
        ArrayList<Attribute> newAttributes = new ArrayList();
        for (Attribute att : getAttributes()) {
            replaced = false;
            for (EPOCObject eo : templates.getTemplateList(OBJ_ATT)) {
                if (att.compare((Attribute)eo, true)) {
                    att = (Attribute)eo;
                    replaced = true;
                    break;
                }
            }
            if (!replaced) att.substituteMembersWithTemplates();
            newAttributes.add(att);
        }
        setAttributes(newAttributes);

        // Actions
        ArrayList<Action> newActions = new ArrayList();
        for (Action act : getActions()) {
            replaced = false;
            for (EPOCObject eo : templates.getTemplateList(OBJ_ACT)) {
                if (act.compare((Action)eo, true)) {
                    act = (Action)eo;
                    replaced = true;
                    break;
                }
            }
            if (!replaced) act.substituteMembersWithTemplates();
            newActions.add(act);
        }
        setActions(newActions);
    }

    /**
     * Copy all data members into this object effectively updating this
     * object with any changes from a copy.  This does not update any child
     * objects, but does update linked objects.
     * @param ele
     */
    @Override
    public void updateDataMembersFrom(Element ele) {
        super.updateDataMembersFrom(ele);

        author = ele.getAuthor();
        birthDay = ele.getBirthDay();
        birthMonth = ele.getBirthMonth();
        eclass = ele.getEClass();
        polygons = ele.getPolygons();
        timesteps = ele.getTimesteps();
        //actions = ele.getActions();
        //attributes = ele.getAttributes();
    }

    @Override
    public boolean compare(Element ele, boolean superficial) {
        return compare(ele, superficial, true);
    }

    /*
     * Compares current object and its child objects with those of the object
     * passed.  If superficial then just look at EPOC data member qualities rather
     * than Epoc Builder data members.
     * @param ele Element
     * @param superficial
     * @param andChildren
     * @return boolean - true if they are equal
     */
    public boolean compare(Element ele, boolean superficial, boolean andChildren) {

        if (!super.compare(ele, superficial)) return false;

        if (!ele.getAuthor().equals(author)) return false;

        if (ele.getBirthDay() != birthDay) return false;

        if (ele.getBirthMonth() != birthMonth) return false;
        // Do any data member comparisons
        if (!ele.getPolygonsString().equals(getPolygonsString())) return false;

        if (!isBroken() && !ele.isBroken()) {
            // compare eclass object
            if (getEClass() == null ^ ele.getEClass() == null) return false;
            if (getEClass() != null && ele.getEClass() != null
                      && !getEClass().compare(ele.getEClass(), superficial)) return false;

            if (andChildren) {
                // Compare Action objects
                ArrayList<Action> thisActList = getActions();
                ArrayList<Action> thatActList = ele.getActions();
                if (thisActList.size() != thatActList.size()) return false;
                // Both lists should now be ordered the same
                for (int i = 0; i < thisActList.size(); i++) {
                    if (!thisActList.get(i).compare(thatActList.get(i), superficial)) return false;
                }

                // Compare Attribute objects
                ArrayList<Attribute> thisAttList = getAttributes();
                ArrayList<Attribute> thatAttList = ele.getAttributes();
                if (thisAttList.size() != thatAttList.size()) return false;
                // Both lists should now be ordered the same
                for (int i = 0; i < thisAttList.size(); i++) {
                    if (!thisAttList.get(i).compare(thatAttList.get(i), superficial)) return false;
                }

                // compare timesteps
                if (timesteps.size() != getTimesteps().size()) return false;
                for (int i = 0; i < getTimesteps().size(); i++) {
                    if (!((Timestep)timesteps.get(i)).compare((Timestep)getTimesteps().get(i), superficial)) {
                        return false;
                    }
                }
            }
        }

        // Check delete action/attribute template list
        if (deleteTemplateList.size() > 0) return false;
        // Check delete attribute template list
        //if (deleteAttributeTemplateList.size() > 0) return false;

        return true;
    }

    /**
     * Replace any linked objects equal to linkObj with the replacement object
     * @param linkObj
     * @param replObj
     */
    public void replaceLinkWith(EPOCObject linkObj, EPOCObject replObj) {
        if (linkObj.getClass() == replObj.getClass()) {
            if (linkObj instanceof EClass && eclass != null && linkObj.equals(eclass)) {
                eclass = (EClass)replObj;
            }
        }

        for (Action act : getActions()) act.replaceLinkWith(linkObj, replObj);
        for (Attribute att : getAttributes()) att.replaceLinkWith(linkObj, replObj);
        for (Timestep ts : timesteps) ts.replaceLinkWith(linkObj, replObj);
    }

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
        // EClass
        if (eclass != null && (!broken || eclass.isBroken())) {
            for (EPOCObject eo : getTemplates().getTemplateList(OBJ_CLS)) {
                if (eclass.compare((EClass)eo, true)) {
                    setEClass((EClass)eo);
                    break;
                }
            }
        }

        for (Action act : getActions()) act.repairLinks(broken, root, this);
        for (Attribute att : getAttributes()) att.repairLinks(broken, root, this);
    }

    /**
     * Turn this object into a fresh new object which will be saved as
     * such.
     * @param uni
     * @param recurse
     */
    @Override
    public void freshen(Universe uni, boolean recurse) {
        super.freshen(uni, recurse);
        if (recurse) {
            if (eclass != null) eclass.freshen(uni, recurse);
            for (Action act : getActions()) act.freshen(uni, recurse);
            for (Attribute att : getAttributes()) att.freshen(uni, recurse);
        }
    }

    /**
     * Template oneself and add to template list
     * Template all child objects as well
     */
    @Override
    public void template() {
        if (!isTemplate() && !isBroken()) {
            setAsTemplate();
            templates.addTemplateList(this);
            if (EPOC_TMPL_LINK_OBJ) {
                if (eclass != null) eclass.template();
            } else {
                // We are going to replace all linked objects with a hollow clone
                // which is set as a broken link (uid=-1)
                if (eclass != null && !eclass.isTemplate()) setEClass((EClass)eclass.hollow());
            }
            // template member objects
            for (Action act : getActions()) act.template();
            for (Attribute att : getAttributes()) att.template();
        }
    }

    /**
     * Check children for any broken links
     * @return
     */
    @Override
    public boolean hasBrokenLink() {
        if (eclass != null && eclass.isBroken()) return true;

        for (Attribute att : attributes) if (att.hasBrokenLink()) return true;
        for (Action act : actions) if (act.hasBrokenLink()) return true;
        for (Timestep ts : timesteps) if (ts.hasBrokenLink()) return true;

        return false;
    }

    /**
     * Dummy for EPOCObject to call with recurse default to false
     *
     **/
    protected Element clone(int method, Universe uni) {
        return clone(method, (method == EPOC_RPL), uni);
    }

    /**
     * Return a deep copy of this element.
     * If revise then copy element but reset uid and set new
     * highest revision.
     * If recurse then clone and apply the same rules to attributes and actions
     * otherwise keep the same Actions and Attributes
     * @param method
     * @param recurse
     * @param uni
     * @return
     */
    protected Element clone(int method, boolean recurse, Universe uni) {
        Element ele = (Element)super.clone(method, uni);

        // Hollow out clone
        ele.eclass = null;
        ele.eclassUID = 0;
        ele.actions = new ArrayList();
        ele.attributes = new ArrayList();
        ele.timesteps = new ArrayList();

        // Do we just break it?
        if (method == EPOC_BRK) {
            ele.setBroken();
            return ele;
        }
        
        // eclass object
        if (recurse && eclass != null && (method == EPOC_CLN || !eclass.isTemplate())) {
            // broken link
            ele.setEClass(eclass.clone((method == EPOC_RPL ? EPOC_BRK : method), uni));
            //ele.setEClass(eclass.clone(method, uni));
        } else {
            ele.setEClass(eclass);
        }

        // clone each Action
        for (Action act : actions) {
            if (recurse && (method == EPOC_CLN || !act.isTemplate())) {
                ele.addAction(act.clone(method, uni));
            } else {
                ele.addAction(act);
            }
        }

        // clone each Attribute
        for (Attribute att : attributes) {
            if (recurse && (method == EPOC_CLN || !att.isTemplate())) {
                ele.addAttribute(att.clone(method, uni));
            } else {
                ele.addAttribute(att);
            }
        }

        // always clone each timestep
        for (Timestep ts : timesteps) {
            ele.addTimestep(ts.clone(method, recurse, uni));
        }

        return ele;
    }
}
