/*******************************************************************************
 * EPOCObject.java
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

import java.awt.Component;
import javax.swing.ImageIcon;
import java.util.*;
import java.io.*;
import java.text.DateFormat;

/*******************************************************************************
 * EPOC Builder base EPOCObject class.
 * All other EPOC object classes inherit from this.
 *
 * @author Troy Robertson
 * @company Australian Antarctic Division
 * @created 10/09/2009
 * @version 0.4.0, 01-09-2010
 ******************************************************************************/
public abstract class EPOCObject <T extends EPOCObject> extends Object implements Cloneable, Comparable, Serializable {
    // <T extends EPOCObject> allows for overriding methods with parameters
    // of classes which override T (eg Element)
    Templates templates = null;

    String uiversion = EPOC_VER;
    int uid = 0;
    int parentuid = 0;
    boolean template = false;
    String name = "";
    String shortname = "";
    String epocClassname = "";
    String morph = "";
    String epocID = "";
    String revision = "";
    Date created = new Date();
    Date modified = new Date();
    String description = "";
    String author = "";
    boolean objectModified = false;
    int position = -1;
    boolean locked = false;

    Storage storage = DerbyStorage.getInstance();
    JRIExchanger rex = JRIExchanger.getInstance();
    XMLReaderWriter xml = XMLReaderWriter.getInstance();

    public void setUIVersion(String uiver) { uiversion = uiver; }

    public void setUID(int uniUID) { uid = uniUID; }

    public void setParentUID(int parUID) { parentuid = parUID; }

    public void setTemplates(Templates templ) { templates = templ; }

    /**
     * Needs to be overloaded by classes with children
     * @param templ
     * @param recurse
     */
    public void setTemplates(Templates templ, boolean recurse) {
        templates = templ;
    }

    /**
     * Add self to the Templates object if their is no comparable object
     * already there
     * @param recurse
     */
    public void addSelfToTemplates(boolean recurse) { 
        templates.addOnceTemplateList(this);
    }

    public void setTemplate(boolean state) { template = state; }

    public void setAsTemplate() {
        template = true;
        parentuid = 0;
    }

    public void unsetAsTemplate(int puid) {
        template = false;
        parentuid = puid;
    }

    /**
     * Needs to be overloaded by classes with children
     * @param puid
     * @param recurse
     */
    public void unsetAsTemplate(int puid, boolean recurse) {
        unsetAsTemplate(puid);
    }

    /**
     * Is the object representing a broken link?
     * @return
     */
    public boolean isBroken() {
        return (uid == -1);
    }

    public void setBroken(boolean broken) {
        if (broken) uid = -1; else uid = 0;
    }

    public void setBroken() { uid = -1; }

    /**
     * Needs to be overloaded by classes with children
     */
    public boolean hasBrokenLink() {
        return false;
    }

    public void setName(String nm) { name = (nm == null ? "" : nm); }

    public void setShortName(String nm) {
        shortname = (nm == null ? "" : nm);
    }

    public void setEPOCClassName(String nm) {
        epocClassname = (nm == null ? "" : nm);
    }

    public void setMorph(String nm) {
        morph = (nm == null ? "" : nm);
    }

    public void setEPOCID(String num) {
        epocID = (num == null ? "" : num);
    }

    public void setRevision(String ver) {
        revision = (ver == null ? "" : ver);
    }

    /**
     * Set created date to that passed
     */
    public void setCreated(Date cr) {
        created = cr;
    }

    /**
     * Set modified date to now
     */
    public void setModifiedNow() {
        modified = new Date();
    }

    /**
     * Set modified date to that passed
     */
    public void setModified(Date md) {
        modified = md;
    }

    public void setDescription(String desc) {
        description = (desc == null ? "" : desc);
    }

    public void setAuthor(String aut) {
        author = (aut == null ? "" : aut);
    }

    /*
     * Set the next appropriate revision in this object
     */
    public void setNextRevision() {
        revision = storage.getNextVersion(getObjType(), revision);
    }

    public void setLocked(boolean lck) {
        locked = lck;
    }

    public void setPosition(int pos) {
        position = pos;
    }

    public String getUIVersion() {
        return uiversion;
    }

    public int getUID() {
        return uid;
    }

    public int getParentUID() {
        return parentuid;
    }

    public boolean isTemplate() {
        return template;
    }

    public Templates getTemplates() {
        return templates;
    }

    public String getName() {
        return name;
    }

    public String getShortName() {
        return shortname;
    }

    public String getDisplayName() {
        return shortname + (!revision.equals("") ? "." : "") + revision;
    }

    public String getEPOCClassName() {
        return epocClassname;
    }

    public String getMorph() {
        return morph;
    }

    public String getEPOCID() {
        return epocID;
    }

    public String getRevision() {
        return revision;
    }

    public Date getCreated() {
        return created;
    }

    public String getFormattedCreated() {
        return DateFormat.getDateTimeInstance().format(created);
    }

    public Date getModified() {
        return modified;
    }

    public String getFormattedModified() {
        return DateFormat.getDateTimeInstance().format(modified);
    }

    public String getDescription() { return description; }

    public String getAuthor() { return author; }

    /**
     * Return a title appropriate for this objects frame
     */
    public String getTitle() {
        String title = "EPOC Builder - ";

        title += getObjectTypeName()+": ";
        title += this.toString();

        return title;
    }

    /**
     * Is this object locked to editing?
     * @return
     */
    public boolean isLocked() { return locked; }

    /**
     * Return the tree display position
     * @return
     */
    public int getPosition() { return position; }

    /**
     * Get the object type name for this object.
     * @return
     */
    public String getObjectTypeName() {
        return getObjectTypeName(getObjType());
    }

    /**
     * Return a string name for the object type constant passed
     * @param objType
     * @return
     */
    static String getObjectTypeName(int objType) {
        switch (objType) {
            case OBJ_UNI:
                return "Universe";
            case OBJ_CLS:
                return "EPOC Class";
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
            case OBJ_ACT:
                return "Action";
            case OBJ_ATT:
                return "Attribute";
            case OBJ_SPA:
                return "Spatial";
            case OBJ_REP:
                return "Report";
            case OBJ_TRI:
                return "Trial";
            case OBJ_TS:
                return "Timestep";
        }

        return "EPOC";
    }

    /**
     * Return a string descriptor for the order type method passed.
     * @param method
     * @return
     */
    static String getListDisplayName(int method) {
        switch (method) {
            case DSPL_LST_NM_ASC:
                return "List by Name";
            case DSPL_LST_RV_ASC:
                return "List by Revision";
            case DSPL_TRE_NM_ASC:
                return "Tree by Name";
            case DSPL_TRE_RV_ASC:
                return "Tree by Revision";
        }

        return "Sort Order";
    }

    /**
     * Return the object type constant associated with the string passed.
     * The inverse of getObjectTypeName()
     * @param objName
     * @return
     */
    static int getObjectTypeFromName(String objName) {
        if (objName.equalsIgnoreCase("Universe")) return OBJ_UNI;
        if (objName.equalsIgnoreCase("EPOC Class")) return OBJ_CLS;
        if (objName.equalsIgnoreCase("Element")) return OBJ_ELE;
        if (objName.equalsIgnoreCase("Biota")) return OBJ_BIO;
        if (objName.equalsIgnoreCase("Environment")) return OBJ_ENV;
        if (objName.equalsIgnoreCase("Activity")) return OBJ_ATY;
        if (objName.equalsIgnoreCase("Management")) return OBJ_MAN;
        if (objName.equalsIgnoreCase("Output")) return OBJ_OUT;
        if (objName.equalsIgnoreCase("Presentation")) return OBJ_PRE;
        if (objName.equalsIgnoreCase("Action")) return OBJ_ACT;
        if (objName.equalsIgnoreCase("Attribute")) return OBJ_ATT;
        if (objName.equalsIgnoreCase("Timestep")) return OBJ_TS;
        if (objName.equalsIgnoreCase("Strategy")) return OBJ_SGY;
        if (objName.equalsIgnoreCase("Spatial")) return OBJ_SPA;
        if (objName.equalsIgnoreCase("Report")) return OBJ_REP;
        if (objName.equalsIgnoreCase("Trial")) return OBJ_TRI;

        return OBJ_EPOC;
    }

    /**
     * Return a string name for the timestep type passed
     * @param tsType
     * @return
     */
    static String getTSTypeName(int tsType) {
        switch (tsType) {
            case TS_ALL:
                return "AllPeriods";
            case TS_FST:
                return "FirstPeriod";
            case TS_LST:
                return "LastPeriod";
            case TS_BEF:
                return "Before";
            case TS_DUR:
                return "During";
            case TS_AFT:
                return "After";
        }

        return "Timestep";
    }

    /**
     * Return a 2D list of all stored element type containing 'uid' and 'shortname (revision)'
     * except this object
     */
    public ArrayList getNegStorageList() {
       return storage.getNegList(getObjType(), uid);
    }

    /**
     * Attempt to save object to persistent storage
     * @return boolean true on success
     * if false returned then error message can be obtained using method getErrMsg()
     */
    public boolean saveToStorage() { return storage.save(this); }

    /**
     * Attempt to delete object from persistent storage
     * @return boolean true on success
     *  if false returned then error message can be obtained using method getErrMsg()
     */
    public boolean delete() {
        if (!storage.delete(this)) {
            //errMsg = storage.getErrMsg();
            return false;
        }

        return true;
    }

    /**
     * Export this and all its child and linked objects as an XML document.
     * @param parentui
     * @return boolean successful
     */
    public boolean export2XML(Component parentui) {
        return xml.exportXML(parentui, this);
    }

    /**
     * Compare minor(est) revision of that passed with this revision
     * Increment this revision to have higher minor(est) part if object has same major part
     * @param obj
     */
    public void setHigherVersion(EPOCObject obj) {
        if (obj.getRevision().equals("") || revision.equals("")) return;

        String[] thisBits = revision.split("\\.");
        String[] thatBits = obj.getRevision().split("\\.");

        if (thisBits.length > thatBits.length) return;

        String newVer = "";
        for (int i = 0; i < thisBits.length - 1; i++) {
            if (!thisBits[i].equals(thatBits[i])) return;
            if (i > 0) newVer += ".";
            newVer += thisBits[i];
        }

        if (Integer.parseInt(thisBits[thisBits.length-1])
                                    <= Integer.parseInt(thatBits[thisBits.length-1])) {
            if (!newVer.equals("")) newVer += ".";
            revision = newVer + (Integer.parseInt(thatBits[thisBits.length-1]) + 1);
        }
    }

    /**
     * Checks whether revision number has only generationGap extra parts to that
     * of obj
     * @param obj
     * @param generationGap
     * @return
     */
    public boolean isDescendentOf(EPOCObject obj, int generationGap) {
        //if (obj.getRevision().equals("") || revision.equals("")) return false;

        String[] thisBits = revision.split("\\.");
        String[] thatBits = obj.getRevision().split("\\.");

        // Watch out for parent with no revision
        if (thatBits.length == 1 && thatBits[0].equals("") && thisBits.length == generationGap && !thisBits[0].equals("")) return true;
        // Should only have generationGap extra part to revision
        if (thisBits.length != thatBits.length + generationGap) return false;
        
        // Check that all revision parts are same for length of obj.revision
        for (int i = 0; i < thatBits.length; i++) {
            if (!thisBits[i].equals(thatBits[i])) return false;
        }

        return true;
    }

    /**
     * Return int epoc object type constant
     * @return
     */
    public int getObjType() {
        if (this instanceof Universe) {
            return OBJ_UNI;
        } else if (this instanceof EClass) {
            return OBJ_CLS;
        } else if (this instanceof Element) {
            return OBJ_ELE;
        } else if (this instanceof Action) {
            return OBJ_ACT;
        } else if (this instanceof Attribute) {
            return OBJ_ATT;
        } else if (this instanceof Spatial) {
            return OBJ_SPA;
        } else if (this instanceof Report) {
            return OBJ_REP;
        } else if (this instanceof Trial) {
            return OBJ_TRI;
        } else if (this instanceof Timestep) {
            return OBJ_TS;
        }

        return OBJ_EPOC;
    }

    /**
     * Copy all data members into this object effectively updating this
     * object with any changes from a copy.  This does not update any child
     * objects, but does update with linked objects.
     * @param eo
     */
    public void updateDataMembersFrom(T eo) {
        uiversion = eo.getUIVersion();
        uid = eo.getUID();
        parentuid = eo.getParentUID();
        template = eo.isTemplate();
        name = eo.getName();
        shortname = eo.getShortName();
        created = eo.getCreated();
        modified = eo.getModified();
        author = eo.getAuthor();
        revision = eo.getRevision();
        morph = eo.getMorph();
        epocID = eo.getEPOCID();
        epocClassname = eo.getEPOCClassName();
        description = eo.getDescription();
        locked = eo.isLocked();
        position = eo.getPosition();
    }

    /**
     * Compares current object and its child objects with those of the object
     * passed.
     * @param obj
     * @return boolean - true if they are equal
     */
    public boolean compare(T obj) {
        return compare(obj, false);
    }

    /**
     * Compares current object and its child objects with those of the object
     * passed.  If superficial then just look at EPOC data member qualities rather
     * than Epoc Builder data members.
     * @param obj
     * @param superficial
     * @return boolean - true if they are equal
     */
    public boolean compare(T obj, boolean superficial) {
        // T allows overriding by methods with a parameter overriding T (eg Element)
        if (!superficial) {
            if (obj.isTemplate() != template) return false;
            if (obj.getUID() != uid) return false;
            if (obj.isLocked() != locked) return false;
            //if (obj.getPosition() != position) return false;
            if (!obj.getRevision().equals(revision)) return false;
        }

        if (!obj.getName().equals(name)) return false;
        if (!obj.getShortName().equals(shortname)) return false;
        if (!obj.getEPOCClassName().equals(epocClassname)) return false;
        if (!obj.getMorph().equals(morph)) return false;
        if (!obj.getEPOCID().equals(epocID)) return false;
        if (!obj.getDescription().equals(description)) return false;

        return true;
    }

    /**
     * Make sure name is not empty and only contains alphanumeric or '.' or '_'
     * but does not start with numeric or '.' or '_'
     * @param nm
     * @return
     */
    protected static boolean testName(String nm) {
        if (!nm.equals("")
                && nm.matches("^[^0-9\\._][\\w\\.]*+")
                && nm.charAt(nm.length()-1) != '_'
                && nm.charAt(nm.length()-1) != '.') {
            return true;
        }

        return false;
    }

    /**
     * Write out signature only to output file handle passed
     * @param out
     * @param varName
     */
    public void writeSignatureAsR(FileWriter out, String varName) {
        // Signature
        try {
            out.write(varName + "$signature <- list(\n");
            if (this instanceof Element && ((Element)this).getEClass() != null) {
                out.write("\tClassName    = \"" + ((Element)this).getEClass().getDisplayName() + "\",\n");
            } else {
                out.write("\tClassName    = \"" + epocClassname + "\",\n");
            }
            out.write("\tID           = " + epocID + ",\n");
            out.write("\tName.full    = \"" + name + "\",\n");
            out.write("\tName.short   = \"" + shortname + "\",\n");
            out.write("\tMorph        = \"" + morph + "\",\n");
            out.write("\tRevision     = \"" + revision + "\",\n");
            out.write("\tAuthors      = \"" + author + "\",\n");
            out.write("\tLast.edit    = \"" + getFormattedModified() + "\"\n");
            out.write(")\n\n");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Repair newlines with newline+preStr
     * @param txt
     * @param preStr
     * @return
     */
    public String prependEachLine(String txt, String preStr) {
        return txt.replace("\n", "\n" + preStr);
    }

    /**
     * Break any linked objects which are not part of the root structure.
     * If root is null then break all linked objects.
     * @param root
     * @param parentEle
     */
    public void breakLinks(EPOCObject root, Element parentEle) {}

    /**
     * Dummy method to be overridden by inheriting objects if needed
     */
    public void substituteMembersWithTemplates() {}

    /**
     * Replace any linked objects equal to linkObj with the replacement object
     * @param linkObj
     * @param replObj
     */
    public void replaceLinkWith(EPOCObject linkObj, EPOCObject replObj) {}

    /**
     * Repair any BROKEN linked objects (dataset, transform, related) with a
     * comparable local object if one can be found, or if this object is a template,
     * then a comparable template object.
     * @param uni
     * @param parentObj
     */
    public void repairBrokenLinks(EPOCObject root, Element parentEle) {
        repairLinks(true, root, parentEle);
    }

   /**
     * Repair any linked objects (dataset, transform, related) with a
     * comparable local object if one can be found, or if this object is a template,
     * then a comparable template object.
     * Will only repair broken links if broken is true.
     * @param broken
     * @param root
     * @param parentEle
     */
    public void repairLinks(boolean broken, EPOCObject root, Element parentEle) {}

    /**
     * Try rebuild a self referential data structure. ie remove duplication of objects
     * so that links point to the one object
     */
    public void remakeLinks() {
        Templates tmpTempl = getTemplates();
        boolean tmpLinkConst = EPOC_AUTO_MATCH_LINK_TEMPL_OBJ;

        // Add an empty templates object and fill with all avail objects
        setTemplates(new Templates(false), true);
        addSelfToTemplates(true);
        // Modify this setting temporarily
        EPOC_AUTO_MATCH_LINK_TEMPL_OBJ = true;
        repairLinks(false, this, null);

        // put back temp modified
        EPOC_AUTO_MATCH_LINK_TEMPL_OBJ = tmpLinkConst;
        setTemplates(tmpTempl, true);
    }

     /**
     * Turn this object (and its children/links) into a fresh new object which
     * will be saved as such.
     * @param uni
     * @param recurse
     */
    public void freshen(Universe uni, boolean recurse) {
        uid = 0;
        uni.setNextAvailableVersion(this, false);
        uni.registerNewReplica(this);
    }

    /**
     * Template oneself and add to template list
     */
    public void template() {
        if (!isTemplate() && !isBroken()) {
            setAsTemplate();
            templates.addTemplateList(this);
        }
    }

    /**
     * Un-template object structure and then re-template as a way of adding
     * a templated object structure to the templates lists.
     */
    public void retemplate() {
        boolean tmpTemplOption = EPOC_TMPL_LINK_OBJ;
        EPOC_TMPL_LINK_OBJ = true;
        unsetAsTemplate(0, true);  // so that replicate deep copies
        template();  // causes it to be templated again and added to list
        EPOC_TMPL_LINK_OBJ = tmpTemplOption;
    }

    /**
     * Return a hollow copy of this Object.  That is one with no child objects
     * and no linked objects.  Set its uid as -1 = broken
     * @return replicated object
     */
    protected EPOCObject hollow() {
        EPOCObject eo = clone(EPOC_BRK, null);
        eo.setBroken();

        return eo;
    }

    /**
     * Return a deep copy of this Object (including child objects)
     * Reset uid to zero as new object and DONT set revision to next available
     * @return replicated object
     */
    protected EPOCObject replicate(Universe uni) {
        if (uni != null) uni.clearRegisteredReplicas();
        EPOCObject eo = clone(EPOC_RPL, uni);

        return eo;
    }

    /**
     * Return a deep copy of this Object
     * Set revision to next highest revision
     * Untemplates object if it was a template and then sets its parent
     * @return revised object
     */
    protected EPOCObject revise(Universe uni, int parentUID) {
        EPOCObject revObj = clone(EPOC_REV, uni);
        if (revObj.isTemplate()) revObj.unsetAsTemplate(parentUID);

        return revObj;
    }

    /**
     * Return a deep copy of this element, but only a shallow copy of
     * its child/linked objects
     */
    @Override
    protected EPOCObject clone() {
        return clone(EPOC_CLN, null);
    }

    /**
     * Return a deep copy of this EPOCObject
     * If revise is true then copy element but reset uid
     * Needs to be overloaded by inheriting classes and super called as first statement.
     * @param method
     * @param uni
     * @return cloned object
     */
    protected EPOCObject clone(int method, Universe uni) {
        EPOCObject eo;

        try {
             eo = (EPOCObject)super.clone();
        } catch (CloneNotSupportedException e) {
             throw new InternalError("But we are Cloneable!!!");
        }

        if (uni != null) {
            eo.setTemplates(uni.getTemplates());
        } else {
            eo.setTemplates(templates);
        }

        if (method == EPOC_RPL || method == EPOC_REV) {
            eo.setUID(0);
            if (uni != null) {
                uni.setNextAvailableVersion(eo, method == EPOC_REV);
                if (method == EPOC_RPL && !(eo instanceof Universe)) uni.registerNewReplica(eo);
            }
        }

        return eo;
    }

    /**
     * Method to allow sorting
     * @param obj
     * @return
     * @throws ClassCastException
     */
    public int compareTo(Object obj) throws ClassCastException {
        if (!(obj instanceof EPOCObject)) {
            throw new ClassCastException("An EPOC Object expected.");
        }

        // Sorting turned off ATM.  Objects to be kept ordered in their lists.
        return 0;

        /*
        // -1 position means last
        if (position == -1) return 1;
        if (((EPOCObject)obj).position == -1) return -1;

        // order by position int
        if (position != ((EPOCObject)obj).position) {
            return (position - ((EPOCObject)obj).position);
        }

        // then by shortname, and then by revision number
        if (shortname.compareToIgnoreCase(((EPOCObject)obj).shortname) == 0) {
            return revision.compareTo(((EPOCObject)obj).revision);
        }

        return shortname.compareToIgnoreCase(((EPOCObject)obj).shortname);
         */
    }

    /**
     * Implements toString method
     */
    @Override
    public String toString() {
        return (shortname.equals("") ? "New" : shortname) + (!revision.equals("") ? "(" + revision + ")" : "");
    }
}
