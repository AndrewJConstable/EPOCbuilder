/*******************************************************************************
 * Timestep.java
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

import java.io.*;

/*******************************************************************************
 * EPOC Builder Timestep class.
 *
 * @author Troy Robertson
 * @company Australian Antarctic Division
 * @created 10/09/2009
 * @version 0.4.0, 01-09-2010
 ******************************************************************************/
public class Timestep extends EPOCObject<Timestep> {
    
    private int startday = 0;
    private int startmonth = 0;
    private int endday = 0;
    private int endmonth = 0;
    private int steptype = 0;
    private int steptiming = 0;
    private int actionUID = 0;
    private int datasetUID = 0;
    private Action action = null;
    private Attribute dataset = null;

    /** Creates a new instance of Timestep */
    public Timestep() {
    }
    
    public Timestep(int tsUID, Templates templates) {
        uid = tsUID;
        this.setTemplates(templates);
        storage.load(this);
    }

    public void setAction(Action act) {
        action = act;
        actionUID = 0;
    }

    /*
     * Update timestep start and end date values if passed value != 0
     */
    public void setSteps(int sd, int sm, int ed, int em) {
        if (sd != 0) startday = sd;
        if (sm != 0) startmonth = sm;
        if (ed != 0) endday = ed;
        if (em != 0) endmonth = em;
    }

    public void setStartDay(int sd) {
        startday = sd;
    }

    public void setStartMonth(int sm) {
        startmonth = sm;
    }

    public void setEndDay(int ed) {
        endday = ed;
    }

    public void setEndMonth(int em) {
        endmonth = em;
    }

    public void setStepType(int stType) {
        steptype = stType;
    }

    public void setStepTiming(int stTime) {
        steptiming = stTime;
    }

    public void setDatasetUID(int ds) {
        datasetUID = ds;
    }

    public void setDataset(Attribute ds) {
        dataset = ds;
        datasetUID = 0;
    }

    public Action getAction() {
        return action;
    }

    public int getActionUID() {
        if (action != null) return action.getUID();
        return actionUID;
    }

    public int getStartDay() {
        return startday;
    }
    
    public int getStartMonth() {
        return startmonth;
    }
    
    public int getEndDay() {
        return endday;
    }
    
    public int getEndMonth() {
        return endmonth;
    }
    
    public int getStepType() {
        return steptype;
    }

    public int getStepTiming() {
        return steptiming;
    }

    public Attribute getDataset() {
        return dataset;
    }

    public int getDatasetUID() {
        if (dataset != null) return dataset.getUID();
        return datasetUID;
    }

    public boolean isKnifeEdge() {
        return (startday == endday && startmonth == endmonth);
    }

    /*
     * Compares current object and its child objects with those of the object
     * passed.  If superficial then just look at EPOC data member qualities rather
     * than EPOC Builder data members.
     * @param ts Timestep
     * @param superficial
     * @return boolean - true if they are equal
     */
    @Override
    public boolean compare(Timestep ts, boolean superficial) {
        
        if (!super.compare(ts, superficial)) {
            return false;
        }
        
        // Compare steps
        if (ts.startday != startday || ts.startmonth != startmonth) return false;

        if (ts.endday != endday || ts.endmonth != endmonth) return false;

        if (ts.steptype != steptype) return false;

        if (ts.steptiming != steptiming) return false;

        if (!isBroken() && !ts.isBroken()) {
            // compare action object
            if (getAction() == null ^ ts.getAction() == null) return false;
            if (getAction() != null && ts.getAction() != null
                      && !getAction().compare(ts.getAction(), superficial)) return false;

            // compare dataset object
            if (getDataset() == null ^ ts.getDataset() == null) return false;
            if (getDataset() != null && ts.getDataset() != null
                      && !getDataset().compare(ts.getDataset(), superficial)) return false;
        }
        
        return true;
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
            if (action != null) action.freshen(uni, recurse);
            if (dataset != null) dataset.freshen(uni, recurse);
        }
    }

    // Write self as text, in the form of an R assignment, to FileWriter
    public void writeAsR(FileWriter out) {
        
        try {
            /*
            out.write("\t### " + name.toUpperCase() + " using Strategy - " + strategy + "\n");
            out.write("\t# Description: " + prependEachLine(description, "\t#             ") + "\n");
            out.write("\t# TIMESTEP\n");
            //out.write("\tTaxon$" + name + ".Timestep <- " + timestep + "\n\n");
            out.write("\t# CODE\n");
            out.write("\tTaxon$" + name + " <- " + code + "\n\n");
             **/
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public String prependEachLine(String txt, String preStr) {
        
        return txt.replace("\n", "\n" + preStr);
    }
    
    /*
     * method to allow sorting by start date
     * if same start date then precedence is given to non-knife-edge
     */
    @Override
    public int compareTo(Object obj) throws ClassCastException {
        if (!(obj instanceof EPOCObject)) {
            throw new ClassCastException("An EPOC Object expected.");
        }
        
        if (startmonth == ((Timestep)obj).startmonth) {
            if (startday == ((Timestep)obj).startday) {
                // check if either are knife edge timestep which gets precedent
                if (isKnifeEdge()) return -1;
                if (((Timestep)obj).isKnifeEdge()) return 1;
            }
            return startday - ((Timestep)obj).startday;
        }
        return startmonth - ((Timestep)obj).startmonth;
    }

    /**
     *
     */
    @Override
    public void breakLinks(EPOCObject root, Element parentEle) {
        // Do it by flagging as broken
        if (action != null) {
            action.setBroken();
            if (root != null && parentEle != null) {
                // search element to see attribute is local
                if (parentEle.getActions().contains(action)) action.setBroken(false);
            }
        }
        if (dataset != null) {
            dataset.setBroken();
            if (root != null && parentEle != null) {
                // search element to see attribute is local
                if (parentEle.getAttributes().contains(dataset)) dataset.setBroken(false);
            }
        }
    }

    /**
     * Check if object passed is linked to this Timestep
     * @param obj
     * @return
     */
    public boolean isLinked(EPOCObject obj) {
        if (obj instanceof Action) {
            if (action != null && action.equals(obj)) return true;
        } else if (obj instanceof Attribute) {
            if (dataset != null && dataset.equals(obj)) return true;
        }

        return false;
    }

    /**
     * Check children for any broken links
     * @return
     */
    @Override
    public boolean hasBrokenLink() {
        if (action != null && action.isBroken()) return true;
        if (dataset != null && dataset.isBroken()) return true;

        return false;
    }

    /**
     * Replace any linked objects equal to linkObj with the replacement object
     * @param linkObj
     * @param replObj
     */
    public void replaceLinkWith(EPOCObject linkObj, EPOCObject replObj) {
        if (linkObj.getClass() == replObj.getClass()) {
            if (linkObj instanceof Action && action != null && linkObj.equals(action)) {
                action = (Action)replObj;
            }
            if (linkObj instanceof Attribute && dataset != null && linkObj.equals(dataset)) {
                dataset = (Attribute)replObj;
            }
        }
    }

    /**
     * Dummy for EPOCObject to call with recurse default to false
     *
     **/
    @Override
    protected Timestep clone(int method, Universe uni) {
        return clone(method, (method == EPOC_RPL), uni);
    }

    /*
     * Return a deep copy of this timestep
     * If revise then copy timestep but reset uid
     * @param method
     * @param recurse
     * @param uni
     * @return
     */
    protected Timestep clone(int method, boolean recurse, Universe uni) {
        Timestep ts = (Timestep)super.clone(method, null);

        ts.action = null;
        ts.dataset = null;

        // Do we just break it?
        if (method == EPOC_BRK) {
            ts.setBroken();
            return ts;
        }

        //if (method == EPOC_CLN && recurse) return ts;   // Should this be here??

        if (recurse && action != null && (method == EPOC_CLN || !action.isTemplate())) {
            //ts.setDataset(dataset.clone((method == EPOC_RPL ? EPOC_BRK : method), uni));  // broken link
            ts.setAction(action.clone(method, uni));  // broken link
        } else {
            ts.setAction(action);
        }

        if (recurse && dataset != null && (method == EPOC_CLN || !dataset.isTemplate())) {
            //ts.setDataset(dataset.clone((method == EPOC_RPL ? EPOC_BRK : method), uni));  // broken link
            ts.setDataset(dataset.clone(method, uni));  // broken link
        } else {
            ts.setDataset(dataset);
        }
        
        return ts;
    }
}
