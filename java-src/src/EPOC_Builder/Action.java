/*******************************************************************************
 * Action.java
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
import java.io.File;
import java.io.FileWriter;
import static au.gov.aad.erm.EPOC_Builder.Constants.*;

/*******************************************************************************
 * EPOC Builder Action class
 *
 * @author Troy Robertson
 * @company Australian Antarctic Division
 * @created 10/09/2009
 * @version 0.4.0, 01-09-2010
 *******************************************************************************/
public class Action extends EPOCObject<Action> {

    private int acttype = 1;
    private ArrayList<Timestep> timesteps = new ArrayList();
    private int datasetUID, transformUID;
    private Attribute dataset = null;
    private Action transform = null;
    private ArrayList<Integer> relatedUIDs = new ArrayList();
    private ArrayList<Element> relatedElements = new ArrayList();
    private String code = "";

    // lists to hold timesteps requiring storage deletion on save
    private ArrayList<Timestep> deleteTimestepList = new ArrayList();

    /** Creates a new instance of Action */
    public Action() {
        setNextRevision();
    }

    public Action(String rev) {
        if (rev != null) setRevision(rev); else setNextRevision();
    }

    public Action(int actUID, Templates templates) {
        uid = actUID;
        this.setTemplates(templates);
        storage.load(this);
    }

    public void addTimestep(Timestep ts) { timesteps.add(ts); }

    public void addRelatedUID(int ruid) { relatedUIDs.add(ruid); }

    public void addRelatedElement(Element ele) { relatedElements.add(ele); }

    public void addDeleteList(Timestep obj) { deleteTimestepList.add(obj); }

    public void setActType(int type) { acttype = type; }

    public void setDatasetUID(int ds) { datasetUID = ds; }

    public void setDataset(Attribute ds) {
        dataset = ds;
        datasetUID = 0;
    }

    public void setTransformUID(int tr) { transformUID = tr; }

    public void setTransform(Action tr) {
        transform = tr;
        transformUID = 0;
    }

    public void setRelatedElements(ArrayList rel) { relatedElements = rel; }

    public void setRelatedUIDsFromString(String ruids) {
        relatedUIDs.clear();
        if (!(ruids == null || ruids.equals(""))) {
            String[] ruidArr = ruids.split(",");
            for (String ruid : ruidArr) {
                addRelatedUID(Integer.parseInt(ruid));
            }
        }
    }

    public void setTransformFromList(ArrayList<Action> actList) {
        if (getTransformUID() > 0) {
            for (Action trans : actList) {
                if (getTransformUID() == trans.getUID()) setTransform(trans);
            }
        } else if (getTransformUID() == -1) {
            // broken link
            setTransform(new Action(-1, templates));
        }
    }

    public void setDatasetFromList(ArrayList<Attribute> attList) {
        if (getDatasetUID() > 0) {
            for (Attribute ds : attList) {
                if (getDatasetUID() == ds.getUID()) setDataset(ds);
            }
        } else if (getDatasetUID() == -1) {
            // broken link
            setDataset(new Attribute(-1, templates));
        }
    }

    /**
     * Uses stored related element uids to find the element object in the list passed
     * Related element objects are then stored internally
     *
     * @param eleList
     */
    public void setRelatedElementsFromList(ArrayList<Element> eleList) {
        relatedElements.clear();
        for (Integer ruid : getRelatedUIDs()) {
            if (ruid > 0) {
                for (Element rEle : eleList) {
                    if (rEle.getUID() == ruid) {
                        relatedElements.add(rEle);
                    }
                }
            } else if (ruid == -1) {
                // broken link
                relatedElements.add(new Element(-1, templates));
            }
        }
    }

    public void setTimesteps(ArrayList<Timestep> tsteps) { timesteps = tsteps; }

    public void setTimestepDatasetsFromList(ArrayList<Attribute> attList) {
        for (Timestep ts : getTimesteps()) {
            if (ts.getDatasetUID() > 0) {
                for (Attribute ds : attList) {
                    if (ts.getDatasetUID() == ds.getUID()) {
                        ts.setDataset(ds);
                    }
                }
            } else if (ts.getDatasetUID() == -1) {
                // broken link
                ts.setDataset(new Attribute(-1, templates));
            }
        }
    }

    /**
     *
     * @param templ
     * @param recurse
     */
    @Override
    public void setTemplates(Templates templ, boolean recurse) {
        setTemplates(templ);
        if (recurse) {
            if (dataset != null) dataset.setTemplates(templ, recurse);
            if (transform != null) transform.setTemplates(templ, recurse);
            for (Element rEle : relatedElements) rEle.setTemplates(templ, recurse);
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
            if (dataset != null) dataset.addSelfToTemplates(recurse);
            if (transform != null) transform.addSelfToTemplates(recurse);
            for (Element rEle : relatedElements) rEle.addSelfToTemplates(recurse);
            for (Timestep ts : timesteps) {
                if (ts.getDataset() != null) ts.getDataset().addSelfToTemplates(recurse);
            }
        }
    }

    /**
     *
     * @param puid
     * @param recurse
     */
    @Override
    public void unsetAsTemplate(int puid, boolean recurse) {
        unsetAsTemplate(puid);
        if (recurse) {
            if (dataset != null) dataset.unsetAsTemplate(puid, recurse);
            if (transform != null) transform.unsetAsTemplate(puid, recurse);
            for (Element rEle : relatedElements) {
                rEle.unsetAsTemplate(puid, recurse);
            }
            for (Timestep ts : timesteps) {
                if (ts.getDataset() != null) ts.getDataset().setTemplate(false);
            }
        }
    }

    public void clearRelated() { relatedElements.clear(); }

    public void setCode(String cd) { code = cd; }

    public boolean isSetup() { return (acttype == ACT_SET); }

    public boolean isSupport() { return (acttype == ACT_SUP); }

    public boolean isAction() { return (acttype == ACT_ACT); }

    public int getActType() { return acttype; }

    public ArrayList<Timestep> getTimesteps() { return timesteps; }

    public Attribute getDataset() { return dataset; }

    public int getDatasetUID() {
            if (dataset != null) return dataset.getUID();
            return datasetUID;
    }

    public Action getTransform() { return transform; }

    public int getTransformUID() {
        if (transform != null) return transform.getUID();
        return transformUID;
    }

    public ArrayList<Integer> getRelatedUIDs() {
        if (relatedElements.size() > 0) {
            ArrayList ruids = new ArrayList();
            for (Element rEle : relatedElements) ruids.add(rEle.getUID());
            return ruids;
        }
        return relatedUIDs;
    }

    public String getRelatedUIDsString() {
        String ruids = "";
        for (Integer ruid : getRelatedUIDs()) {
            ruids += (ruids.equals("") ? String.valueOf(ruid) : "," + String.valueOf(ruid));
        }

        return ruids;
    }

    public ArrayList<Element> getRelatedElements() { return relatedElements; }

    public String getCode() { return code; }

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
     * Check if object passed is linked to this action
     * @param obj
     * @return
     */
    public boolean isLinked(EPOCObject obj) {
        if (obj instanceof Element) {
            for (Element ele : relatedElements) if (ele.equals(obj)) return true;
        } else if (obj instanceof Action) {
            if (transform != null && transform.equals(obj)) return true;
        } else if (obj instanceof Attribute) {
            if (dataset != null && dataset.equals(obj)) return true;
            for (Timestep ts : timesteps) if (ts.isLinked(obj)) return true;
        }

        return false;
    }

    /*
     * Attempt to save action to persistent storage
     * @return boolean true on success
     *  if false returned then error message can be obtained using method getErrMsg()
     */
    @Override
    public boolean saveToStorage() {
        if (!EPOCObject.testName(shortname)) {
            Messages.addErrMsg("Action (" + shortname + ") must be named and may only contain\n" +
                     "alphanumerics, '.' or '_'\n" +
                     "It may only start with an alphabetic character\n" +
                     "and may not end in '.' or '_'");
            return false;
        }

        // check delete list for any timesteps needing deletion
        if (!doDeletes()) return false;

        if (!super.saveToStorage()) return false;

        return true;
    }

    /*
     * Check delete list for any timesteps needing deletion
     */
    public boolean doDeletes() {

        for (Timestep ts : deleteTimestepList) {
            if (!storage.delete(ts)) return false;
        }
        // reset delete action list
        deleteTimestepList.clear();

        return true;
    }

    /*
     * Write self as text, in the form of an R assignment, to FileWriter
     */
    public void writeAsR(String dirPath, String elementEPOCClass) {
        File outputFile = new File(dirPath + File.separator + "code" + File.separator + getDisplayName() + ".R");
        Date dt = new Date();

        try {
            outputFile.createNewFile();
            FileWriter out = new FileWriter(outputFile);

            out.write("########################################################\n");
            out.write("# ACTION - " + shortname + " (" + revision + ")\n");
            out.write("# Description: " + super.prependEachLine(description, "#              ") + "\n");
            out.write("#\n");
            out.write("# Generated by EPOC Builder\n");
            out.write("# Date: " + dt.toString() + "\n");
            out.write("########################################################\n\n");

            out.write(getSetMethodStr(elementEPOCClass));

            out.close();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String getSetMethodStr(String elementEPOCClass) {
        String setMethod = "";

        if (isSupport()) {
            setMethod += "if (!isGeneric(\"" + shortname + "\"))\n";
            setMethod += "setGeneric(\"" + shortname + "\", function(.Object, universe=\"Universe\""
                    + ") standardGeneric(\"" + shortname + "\"))\n";
            setMethod += "setMethod(\"" + shortname + "\", \"" + elementEPOCClass + "\",\n";
            setMethod += "\tfunction(\n";
            setMethod += "\t\t.Object,             # access to element object\n";
            setMethod += "\t\tuniverse             # access to universe if needed\n";
            setMethod += "\t\t)\n";
            setMethod += "\t{\n";
        } else if (isSetup()) {
            setMethod += "if (!isGeneric(\"" + getDisplayName() + "\"))\n";
            setMethod += "setGeneric(\"" + getDisplayName() + "\", function(.Object, calendar=\"Calendar\", ptSA=\"list\", "
                    + "moduleNum=\"numeric\", elementNum=\"numeric\",\n"
                    + "\ttstepNum=\"numeric\", a=\"numeric\", pe=\"numeric\", firstPeriod=\"numeric\", "
                    + "dset=\"list\") standardGeneric(\"" + getDisplayName() + "\"))\n";
            setMethod += "setMethod(\"" + getDisplayName() + "\", \"" + elementEPOCClass + "\",\n";
            setMethod += "\tfunction(\n";
            setMethod += "\t\t.Object,             # access to element object\n";
            setMethod += "\t\tcalendar,            # calendar object\n";
            setMethod += "\t\tptSA,                # untransformed action for the period derived from timestep of element\n";
            setMethod += "\t\t                     # Note: PtSA is a list retained for concatenating to a list.\n";
            setMethod += "\t\t                     #       Therefore, the action is the first element in the list\n";
            setMethod += "\t\tmoduleNum,           # reference module number for the element\n";
            setMethod += "\t\telementNum,          # relative number of the element in the universe\n";
            setMethod += "\t\ttstepNum,            # current time step in element\n";
            setMethod += "\t\ta,                   # number of action in time step\n";
            setMethod += "\t\tpe,                  # number of the period in the calendar\n";
            setMethod += "\t\tfirstPeriod,         # logical indicating if this is the first period in the timestep\n";
            setMethod += "\t\tdset                 # dataset to assist with transformation\n";
            setMethod += "\t\t)\n";
            setMethod += "\t{\n";
        } else { //normal timestep action)
            setMethod += "if (!isGeneric(\"" + getDisplayName() + "\"))\n";
            setMethod += "setGeneric(\"" + getDisplayName() + "\", function(.Object, universe=\"Universe\", action=\"list\", "
                    + "periodInfo=\"list\") standardGeneric(\"" + getDisplayName() + "\"))\n";
            setMethod += "setMethod(\"" + getDisplayName() + "\", \"" + elementEPOCClass + "\",\n";
            setMethod += "\tfunction(\n";
            setMethod += "\t\t.Object,             # access to element object\n";
            setMethod += "\t\tuniverse,            # access to universe if needed\n";
            setMethod += "\t\taction,              # ActionMat row\n";
            setMethod += "\t\t                     # Col  1  = module\n";
            setMethod += "\t\t                     # Col  2  = element\n";
            setMethod += "\t\t                     # Col  3  = period\n";
            setMethod += "\t\t                     # Col  4  = reference day in year\n";
            setMethod += "\t\t                     # Col  5  = action reference number in period (NA if no actions)\n";
            setMethod += "\t\t                     # Col  6  = number for 'before =1', 'during = 2', 'after = 3' (NA if no actions)\n";
            setMethod += "\t\tperiodInfo           # information about the active period for use in subroutines\n";
            setMethod += "\t\t                     # Number      = eTSD\n";
            setMethod += "\t\t                     # Day         = PropYear[eTSD,1]\n";
            setMethod += "\t\t                     # KnifeEdge   = if(PropYear[eTSD,2]==0) FALSE else TRUE\n";
            setMethod += "\t\t                     # YearPropn   = PropYear[eTSD,3]\n";
            setMethod += "\t\t                     # PeriodStart = PreviousDay/365 # proportion of year passed since 0 Jan\n";
            setMethod += "\t\t                     #                               # to beginning of time period\n";
            setMethod += "\t\t                     # PeriodEnd   = PreviousDay/365+PropYear[eTSD,3]\n";
            setMethod += "\t\t)\n";
            setMethod += "\t{\n";
        }

        setMethod += "\t\t" + prependEachLine(code, "\t\t") + "\n";
        if (isSetup()) setMethod += "\t\treturn(ptSA)\n";
        setMethod += "\t}\n";
        setMethod += ")";

        return setMethod;
    }

    public boolean validate(Element ele, Universe uni) {
        boolean passed = true;
        ArrayList<Attribute> attList = ele.getAttributes();

        rex.clear();

        if (isAction()) {
            boolean found = false, hasTS = false;

            // Check that it has timesteps if it is a timestep action and that each
            // ts dataset is present
            for (Timestep ts : timesteps) {
                hasTS = true; found = false;

                // Check that ts dataset is available in element
                if (ts.getDataset() != null) {
                    if (ts.getDataset().getUID() >= 0) {
                        for (Attribute att : attList) {
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

            // Check for presence of transform action
            if (this.getTransform() != null) {
                found = false;

                if (this.getTransform().getUID() >= 0) {
                    for (Action act : ele.getActions()) {
                        if (act.isSetup() && act.getUID() == getTransformUID()) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        Messages.addErrMsg("Transform action is not present as a setup action of element!");
                        passed = false;
                    }
                } else {
                    Messages.addErrMsg("Contains broken link to missing transform action!");
                    passed = false;
                }
            }

            // Check for presence of related elements
            for (Element rEle : relatedElements) {
                found = false;
                if (rEle.getUID() >= 0) {
                    for (Element uEle : uni.getElements(OBJ_ELE)) {
                        if (uEle == rEle) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        Messages.addErrMsg("Related element is not present in universe!");
                        passed = false;
                    }
                } else {
                    Messages.addErrMsg("Contains broken link to missing related element!");
                    passed = false;
                }
            }

        } else {
            // Check dataset isn't missing
            if (this.getDataset()!= null) {
                boolean found = false;

                if (this.getDataset().getUID() >= 0) {
                    for (Attribute att : ele.getAttributes()) {
                        if (att.getUID() == getDatasetUID()) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        Messages.addErrMsg("Action contains dataset which is not present as an attribute of element!");
                        passed = false;
                    }
                } else {
                    Messages.addErrMsg("Contains broken link to missing dataset attribute!");
                    passed = false;
                }
            }
        }

        if (rex.hasEngine()) {
            if (!rex.parse(getCode())) passed = false;
        } else {
            Messages.addErrMsg("No JRI Engine found!\nUnable to parse R code!");
            passed = false;
        }

        return passed;
    }

    /**
     * Copy all data members into this object effectively updating this
     * object with any changes from a copy.  This does not update any child
     * objects, but does update with linked objects.
     * @param act
     */
    @Override
    public void updateDataMembersFrom(Action act) {
        super.updateDataMembersFrom(act);

        acttype = act.getActType();
        timesteps = act.getTimesteps();
        datasetUID = act.datasetUID;
        dataset = act.getDataset();
        transformUID = act.transformUID;
        transform = act.getTransform();
        relatedUIDs = act.relatedUIDs;
        relatedElements = act.getRelatedElements();
        code = act.getCode();

        deleteTimestepList = act.deleteTimestepList;
    }

    /*
     * Compares current object and its child objects with those of the object
     * passed.  If superficial then just look at EPOC data member qualities rather
     * than Epoc Builder data members.
     * @param act Action
     * @param superficial
     * @return boolean - true if they are equal
     */
    @Override
    public boolean compare(Action act, boolean superficial) {

        if (!super.compare(act, superficial)) return false;

        // are they both setup actions
        if (acttype != act.getActType()) return false;

        if (!isBroken() && !act.isBroken()) {
            // compare timesteps
            if (timesteps.size() != act.getTimesteps().size()) return false;
            for (int i = 0; i < act.getTimesteps().size(); i++) {
                if (!((Timestep)timesteps.get(i)).compare((Timestep)act.getTimesteps().get(i), superficial)) {
                    return false;
                }
            }

            // compare transform object
            if (getTransform() == null ^ act.getTransform() == null) return false;
            if (getTransform() != null && act.getTransform() != null
                      && !getTransform().compare(act.getTransform(), superficial)) return false;

            // compare dataset object
            if (getDataset() == null ^ act.getDataset() == null) return false;
            if (getDataset() != null && act.getDataset() != null
                      && !getDataset().compare(act.getDataset(), superficial)) return false;

            // compare related objects
            if (getRelatedElements().size() != act.getRelatedElements().size()) return false;
            for (int i = 0; i < act.getRelatedElements().size(); i++) {
                if (!((Element)relatedElements.get(i)).compare((Element)act.getRelatedElements().get(i), superficial, false)) {
                    return false;
                }
            }
        }
        /*
        ArrayList<Integer> rList = act.getRelatedUIDs();
        for (Element ele : getRelatedElements()) {
            if (!rList.contains(ele.getUID())) return false;
        }
        for (int rUID : rList) {
            if (!getRelatedUIDs().contains(rUID)) return false;
        }
        */
        // The code
        if (!act.getCode().equals(code)) return false;

        return true;
    }

    /**
     * Flag all linked objects as broken links.  This method does not clone
     * objects before breaking the links.  If you want to retain the original
     * objects by cloning then use the hollow() method.
     */
    @Override
    public void breakLinks(EPOCObject root, Element parentEle) {
        // Do it by flagging as broken
        if (dataset != null) {
            dataset.setBroken();
            if (root != null && parentEle != null) {
                // search element to see attribute is local
                if (parentEle.getAttributes().contains(dataset)) dataset.setBroken(false);
            }
        }
        if (transform != null) {
            transform.setBroken();
            if (root != null && parentEle != null) {
                // search element to see action is local
                if (parentEle.getActions().contains(transform)) transform.setBroken(false);
            }
        }
        for (Element rEle : relatedElements) {
            rEle.setBroken();
            if (root != null && root instanceof Universe) {
                // search universe to see element is local
                if (((Universe)root).getElements().contains(rEle)) rEle.setBroken(false);
            }
        }

        for (Timestep ts : getTimesteps()) ts.breakLinks(root, parentEle);
    }

    /**
     * Check children for any broken links as indicated by uid == -1
     * @return
     */
    @Override
    public boolean hasBrokenLink() {
        if (dataset != null && dataset.isBroken()) return true;
        if (transform != null && transform.isBroken()) return true;
        for (Element rEle : relatedElements) if (rEle.isBroken()) return true;
        for (Timestep ts : timesteps) if (ts.hasBrokenLink()) return true;

        return false;
    }

    /**
     * Replace any linked objects equal to linkObj with the replacement object
     * @param linkObj
     * @param replObj
     */
    @Override
    public void replaceLinkWith(EPOCObject linkObj, EPOCObject replObj) {
        if (linkObj.getClass() == replObj.getClass()) {
            if (linkObj instanceof Action && transform != null && linkObj.equals(transform)) {
                transform = (Action)replObj;
            }

            if (linkObj instanceof Attribute && dataset != null && linkObj.equals(dataset)) {
                dataset = (Attribute)replObj;
            }

            if (linkObj instanceof Element && relatedElements.indexOf(linkObj) >= 0) {
                relatedElements.set(relatedElements.indexOf(linkObj), (Element)replObj);
            }

            for (Timestep ts : timesteps) ts.replaceLinkWith(linkObj, replObj);
        }
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
        ArrayList<EPOCObject> objList = new ArrayList();

        // Dataset
        if (dataset != null && (!broken || dataset.isBroken())) {
            if (parentEle != null) objList.addAll(parentEle.getAttributes());
            if (this.isTemplate() && EPOC_AUTO_MATCH_LINK_TEMPL_OBJ) objList.addAll(this.getTemplates().getTemplateList(OBJ_ATT));
            for (EPOCObject eo : objList) {
                if (!this.isTemplate() || (this.isTemplate() && eo.isTemplate())) {
                    if (dataset.compare((Attribute)eo, true)) {
                        setDataset((Attribute)eo);
                        break;
                    }
                }
            }
        }

        // Transform Action
        objList.clear();
        if (transform != null && (!broken || transform.isBroken())) {
            if (parentEle != null) objList.addAll(parentEle.getActions());
            if (this.isTemplate() && EPOC_AUTO_MATCH_LINK_TEMPL_OBJ) objList.addAll(this.getTemplates().getTemplateList(OBJ_ACT));
            for (EPOCObject eo : objList) {
                if (((Action)eo).isSetup() && (!this.isTemplate() || (this.isTemplate() && eo.isTemplate()))) {
                    if (transform.compare((Action)eo, true)) {
                        setTransform((Action)eo);
                        break;
                    }
                }
            }
        }

        // Related Elements
        objList.clear();
        ArrayList<Element> newRelatedElements = new ArrayList();
        if (root != null && root instanceof Universe) objList.addAll(((Universe)root).getElements());
        if (this.isTemplate() && EPOC_AUTO_MATCH_LINK_TEMPL_OBJ) objList.addAll(this.getTemplates().getElementTemplateList());
        if (parentEle != null) objList.remove(parentEle); // dont select own parent
        for (Element rEle : getRelatedElements()) {
            if (!broken || rEle.isBroken()) {
                for (EPOCObject eo : objList) {
                    if (!this.isTemplate() || (this.isTemplate() && eo.isTemplate())) {
                        if (rEle.compare((Element)eo, true, false)) {
                            rEle = (Element)eo;
                            break;
                        }
                    }
                }
            }
            newRelatedElements.add(rEle);
        }
        setRelatedElements(newRelatedElements);

        // Timestep datasets
        objList.clear();
        if (parentEle != null) objList.addAll(parentEle.getAttributes());
        if (this.isTemplate() && EPOC_AUTO_MATCH_LINK_TEMPL_OBJ) objList.addAll(this.getTemplates().getTemplateList(OBJ_ATT));
        for (Timestep ts : getTimesteps()) {
            if (ts.getDataset() != null && (!broken || ts.getDataset().isBroken())) {
                for (EPOCObject eo : objList) {
                    if (!this.isTemplate() || (this.isTemplate() && eo.isTemplate())) {
                        if (ts.getDataset().compare((Attribute)eo, true)) {
                            ts.setDataset((Attribute)eo);
                            break;
                        }
                    }
                }
            }
        }
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
            if (dataset != null) dataset.freshen(uni, recurse);
            if (transform != null) transform.freshen(uni, recurse);
            for (Element rEle : getRelatedElements()) rEle.freshen(uni, recurse);
            for (Timestep ts : getTimesteps()) ts.freshen(uni, recurse);
        }
    }

    /**
     * Template oneself and add to template list
     * Template all linked objects if EPOC_TMPL_LINK_OBJ is set,
     * otherwise break linked objects if they are not a template already
     */
    @Override
    public void template() {
        if (!isTemplate() && !isBroken()) {
            setAsTemplate();
            templates.addTemplateList(this);
            if (EPOC_TMPL_LINK_OBJ) {
                if (dataset != null) dataset.template();
                if (transform != null) transform.template();
                for (Element ele : getRelatedElements()) ele.template();
                for (Timestep ts : getTimesteps()) {
                    if (ts.getDataset() != null) ts.getDataset().template();
                }
            } else {
                // We are going to replace all linked objects with a hollow clone
                // which is set as a broken link (uid=-1)
                if (dataset != null && !dataset.isTemplate()) setDataset((Attribute)dataset.hollow());
                if (transform != null && !transform.isTemplate()) setTransform((Action)transform.hollow());
                ArrayList<Element> tempREList = new ArrayList();
                tempREList.addAll(relatedElements);
                relatedElements.clear();
                for (Element ele : tempREList) {
                    if (ele.isTemplate()) {
                        relatedElements.add(ele);
                    } else {
                        relatedElements.add((Element)ele.hollow());
                    }
                }
                for (Timestep ts : getTimesteps()) {
                    if (ts.getDataset() != null && !ts.getDataset().isTemplate())
                        ts.setDataset((Attribute)ts.getDataset().hollow());
                }
            }
        }
    }

    /**
     * Dummy for EPOCObject to call with recurse default to false
     * @param method
     * @param uni
     * @return cloned Action object
     */
    @Override
    protected Action clone(int method, Universe uni) {
        return clone(method, (method == EPOC_RPL), uni);
    }

    /**
     * Return a deep copy of this action.
     * If revise then copy element but reset uid and set new
     * highest revision.
     * If recurse then clone and apply the same rules to child/linked objects
     * otherwise keep the same member objects.
     * @param method
     * @param recurse
     * @param uni
     * @return cloned Action
     */
    protected Action clone(int method, boolean recurse, Universe uni) {
        Action act = (Action)super.clone(method, uni);

        // Hollow out clone
        act.timesteps = new ArrayList();
        act.transformUID = 0;
        act.transform = null;
        act.relatedUIDs = new ArrayList();
        act.relatedElements = new ArrayList();
        act.datasetUID = 0;
        act.dataset = null;

        // Do we just break it?
        if (method == EPOC_BRK) {
            act.setBroken();
            return act;
        }

        // always clone each timestep
        for (Timestep ts : timesteps) act.addTimestep(ts.clone(method, recurse, uni));

        // transform action
        if (recurse && transform != null && (method == EPOC_CLN || !transform.isTemplate())) {
            // broken link
            act.setTransform(transform.clone((method == EPOC_RPL ? EPOC_BRK : method), uni));
            //act.setTransform(transform.clone(method, uni));
        } else {
            act.setTransform(transform);
        }

        // dataset
        if (recurse && dataset != null && (method == EPOC_CLN || !dataset.isTemplate())) {
            // broken link
            act.setDataset(dataset.clone((method == EPOC_RPL ? EPOC_BRK : method), uni));
            //act.setDataset(dataset.clone(method, uni));
        } else {
            act.setDataset(dataset);
        }

        // Related elements
        for (Element rEle : relatedElements) {
            if (recurse && (method == EPOC_CLN || !rEle.isTemplate())) {
                // broken link
                act.addRelatedElement(rEle.clone((method == EPOC_RPL ? EPOC_BRK : method), uni));
                //act.addRelatedElement(rEle.clone(method, uni));
            } else {
                act.addRelatedElement(rEle);
            }
        }

        return act;
    }
}
