/*******************************************************************************
 * EClass.java
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

import java.util.Date;
import java.io.*;

/*******************************************************************************
 * EPOC Builder EClass class
 *
 * @author Troy Robertson
 * @company Australian Antarctic Division
 * @created 10/09/2009
 * @version 0.4.0, 01-09-2010
 ******************************************************************************/
public class EClass extends EPOCObject<EClass> {

    private int modtype = 0;
    private String initClass = "";
    private String initTrial = "";
    private String initTransition = "";
    private String updateState = "";
    private String printState = "";

    /** Creates a new instance of Attribute */
    public EClass() {
        setNextRevision();
        epocClassname = "Element";
    }

    public EClass(int ecUID, Templates templates) {
        uid = ecUID;
        this.setTemplates(templates);
        storage.load(this);
    }

    public int getModType() { return modtype; }

    public String getInitClass() { return initClass; }

    public String getInitTrial() { return initTrial; }

    public String getInitTransition() { return initTransition; }

    public String getUpdateState() { return updateState; }

    public String getPrintState() { return printState; }

    public void setModType(int mtype) { modtype = mtype; }

    public void setInitClass(String ic) { initClass = ic; }

    public void setInitTrial(String it) { initTrial = it; }

    public void setInitTransition(String it) { initTransition = it; }

    public void setUpdateState(String us) { updateState = us; }

    public void setPrintState(String ps) { printState = ps; }

    /*
     * Attempt to save attribute to persistent storage
     * @return boolean true on success
     *  if false returned then error message can be obtained using method getErrMsg()
     */
    public boolean saveToStorage() {
        if (!EPOCObject.testName(shortname)) {
            Messages.addErrMsg("EPOC Class (" + shortname + ") must be named and may only contain\n" +
                     "alphanumerics, '.' or '_'\n" +
                     "It may only start with an alphabetic character\n" +
                     "and may not end in '.' or '_'");
            return false;
        }

        if (!super.saveToStorage()) return false;

        return true;
    }

    public boolean validate() {
        boolean passed = true;

        rex.clear();

        // Check that all methods are parseable
        if (rex.hasEngine()) {
            if (!rex.parse(getInitClass())) passed = false;
            if (!rex.parse(getInitTrial())) passed = false;
            if (!rex.parse(getInitTransition())) passed = false;
            if (!rex.parse(getUpdateState())) passed = false;
            if (!rex.parse(getPrintState())) passed = false;
        } else {
            Messages.addErrMsg("No JRI Engine found!\nUnable to test existence of EPOC R Library or EPOC Classes!");
            passed = false;
        }

        return passed;
    }

    /**
     * Copy all data members into this object effectively updating this
     * object with any changes from a copy.  This does not update any child
     * objects, but does update with linked objects.
     * @param ec
     */
    @Override
    public void updateDataMembersFrom(EClass ec) {
        super.updateDataMembersFrom(ec);

        modtype = ec.getModType();
        initClass = ec.getInitClass();
        initTrial = ec.getInitTrial();
        initTransition = ec.getInitTransition();
        updateState = ec.getUpdateState();
        printState = ec.getPrintState();
    }

    /**
     * Compares current object and its child objects with those of the object
     * passed.  If superficial then just look at EPOC data member qualities rather
     * than Epoc Builder data members.
     * @param tri Trial
     * @param superficial
     * @return boolean - true if they are equal
     */
    @Override
    public boolean compare(EClass tri, boolean superficial) {

        if (!super.compare(tri, superficial)) return false;
        
        // Do any data member comparisons
        if (!tri.getInitClass().equals(initClass)) return false;
        if (!tri.getInitTrial().equals(initTrial)) return false;
        if (!tri.getInitTransition().equals(initTransition)) return false;
        if (!tri.getUpdateState().equals(updateState)) return false;
        if (!tri.getPrintState().equals(printState)) return false;

        return true;
    }
    
    /*
     * Write self as text, in the form of an R assignment, to FileWriter
     */
    public void writeAsR(String dirPath) {
        File outputFile = new File(dirPath + File.separator + "code" + File.separator + getDisplayName() + ".R");
        Date dt = new Date();

        try {
            outputFile.createNewFile();
            FileWriter out = new FileWriter(outputFile);

            out.write("########################################################\n");
            out.write("# EPOC CLASS - " + shortname + " (" + revision + ")\n");
            out.write("# Description: " + super.prependEachLine(description, "#              ") + "\n");
            out.write("#\n");
            out.write("# Generated by EPOC Builder\n");
            out.write("# Date: " + dt.toString() + "\n");
            out.write("########################################################\n\n");

            out.write("# Extend base class\n");
            out.write("setClass(\"" + getDisplayName() + "\", contains=\"" + EPOCObject.getObjectTypeName(modtype) + "\")\n\n");

            if (!getInitClass().trim().equals("")) out.write(getSetMethodStr("initialize", getInitClass()) + "\n\n");
            if (!getInitTrial().trim().equals("")) out.write(getSetMethodStr("initialiseTrial", getInitTrial()) + "\n\n");
            if (!getInitTransition().trim().equals("")) out.write(getSetMethodStr("initialiseTransition", getInitTransition()) + "\n\n");
            if (!getUpdateState().trim().equals("")) out.write(getSetMethodStr("updateState", getUpdateState()) + "\n\n");
            if (!getPrintState().trim().equals("")) out.write(getSetMethodStr("printState", getPrintState()));

            out.close();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Produce 'setMethod' text for the appropriate methodName and code
     * @param methodName
     * @param code
     * @return
     */
    public String getSetMethodStr(String methodName, String code) {
        String setMethod = "";

        setMethod += "setMethod(\"" + methodName + "\", \"" + getDisplayName() + "\",\n";
        setMethod += "\tfunction(\n";
        setMethod += "\t\t.Object,             # access to element object\n";
        setMethod += "\t\tuniverse" + (methodName.equals("initialize") ? "," : " ") + "            # access to universe if needed\n";
        if (methodName.equals("initialize")) {
            setMethod += "\t\tdataPath             # path to input data file\n";
        }
        setMethod += "\t\t)\n";
        setMethod += "\t{\n";
        if (methodName.equals("initialize")) {
            setMethod += "\t\t# first call parents (Element) initialize method\n";
            setMethod += "\t\t.Object <- callNextMethod(.Object, dataPath)\n\n";
        }
        setMethod += "\t\t" + prependEachLine(code, "\t\t") + "\n\n";
        if (methodName.startsWith("initiali")) {
            setMethod += "\t\treturn(.Object)\n";
        }
        setMethod += "\t}\n";
        setMethod += ")";

        return setMethod;
    }
    
    /*
     * Implements toString method
     */
    @Override
    public String toString() {

        return (shortname.equals("") ? epocClassname : shortname) + (!revision.equals("") ? "(" + revision + ")" : "");
    }

    /**
     * Return a deep copy of this eclass
     * If revise then copy but reset uid and set
     * revision to next available
     *
     * @param method
     * @param uni
     * @return
     */
    @Override
    protected EClass clone(int method, Universe uni) {
        EClass ec = (EClass)super.clone(method, uni);

        // Do we just break it?
        if (method == EPOC_BRK) ec.setBroken();

        return ec;
    }
}
