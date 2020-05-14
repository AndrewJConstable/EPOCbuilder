/*******************************************************************************
 * Trial.java
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
 * EPOC Builder Trial class.
 *
 * @author Troy Robertson
 * @company Australian Antarctic Division
 * @created 14/04/2010
 * @version 0.4.0, 01-09-2010
 ******************************************************************************/
public class Trial extends EPOCObject<Trial> {

    private String yearStart = "";
    private String yearEnd = "";
    private String fishingStart = "";
    private String fishingEnd = "";
    private String trialDir = "";

    /** Creates a new instance of Attribute */
    public Trial() {
        setNextRevision();
        epocClassname = "Trial";
    }

    public Trial(int triUID, Templates templates) {
        uid = triUID;
        this.setTemplates(templates);
        storage.load(this);
    }

    public String getYearStart() {
        return yearStart;
    }

    public String getYearEnd() {
        return yearEnd;
    }

    public String getFishingStart() {
        return fishingStart;
    }

    public String getFishingEnd() {
        return fishingEnd;
    }

    public String getTrialDir() {
        return trialDir;
    }

    public void setYearStart(String ys) {
        yearStart = ys;
    }

    public void setYearEnd(String ye) {
        yearEnd = ye;
    }

    public void setFishingStart(String ffy) {
        fishingStart = ffy;
    }

    public void setFishingEnd(String lfy) {
        fishingEnd = lfy;
    }

    public void setTrialDir(String tDir) {
        trialDir = tDir;
    }

    /**
     * Attempt to save attribute to persistent storage
     * @return boolean true on success
     *  if false returned then error message can be obtained using method getErrMsg()
     */
    @Override
    public boolean saveToStorage() {
        if (!EPOCObject.testName(shortname)) {
            Messages.addErrMsg("Trial (" + shortname + ") must be named and may only contain\n" +
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

        try {
            if (trialDir == null || trialDir.equals("")) {
                Messages.addErrMsg("Trial object requires a directory name!");
                passed = false;
            }
            if (yearStart.equals("") || yearEnd.equals("")) {
                Messages.addErrMsg("Trial is missing start or end years!");
                passed = false;
            } else if (Integer.parseInt(yearStart) > Integer.parseInt(yearEnd)) {
                Messages.addErrMsg("Trial ends before it starts!");
                passed = false;
            }
        } catch (NumberFormatException nfe) {
            Messages.addErrMsg("Trial start or end is non-numeric!");
            passed = false;
        }

        try {
            if ((fishingStart.equals("") && !fishingEnd.equals(""))
                    || (!fishingStart.equals("") && fishingEnd.equals(""))) {
                Messages.addErrMsg("Trial is missing start or end fishing years!");
                passed = false;
            } else if ((!fishingStart.equals("") && !fishingEnd.equals(""))
                    && Integer.parseInt(fishingStart) > Integer.parseInt(fishingEnd)) {
                Messages.addErrMsg("Trial fishing ends before it starts!");
                passed = false;
            }
        } catch (NumberFormatException nfe) {
            Messages.addErrMsg("Trial fishing start or end is non-numeric!");
            passed = false;
        }

        return passed;
    }

    /**
     * Copy all data members into this object effectively updating this
     * object with any changes from a copy.  This does not update any child
     * objects, but does update with linked objects.
     * @param tri
     */
    @Override
    public void updateDataMembersFrom(Trial tri) {
        super.updateDataMembersFrom(tri);

        yearStart = tri.getYearStart();
        yearEnd = tri.getYearEnd();
        fishingStart = tri.getFishingStart();
        fishingEnd = tri.getFishingEnd();
        trialDir = tri.getTrialDir();
    }

     /*
     * Compares current object and its child objects with those of the object
     * passed.  If superficial then just look at EPOC data member qualities rather
     * than Epoc Builder data members.
     * @param tri Trial
     * @param superficial
     * @return boolean - true if they are equal
     */
    public boolean compare(Trial tri, boolean superficial) {

        if (!super.compare(tri, superficial)) {
            return false;
        }

        // Do any data member comparisons
        if (!tri.getYearStart().equals(yearStart)) return false;
        if (!tri.getYearEnd().equals(yearEnd)) return false;
        if (!tri.getFishingStart().equals(fishingStart)) return false;
        if (!tri.getFishingEnd().equals(fishingEnd)) return false;
        if (!tri.getTrialDir().equals(trialDir)) return false;

        return true;
    }

    public static FileWriter writeHeaderAsR(String dirPath, String universeName) {
        File outputFile = new File(dirPath + File.separator + "data" + File.separator + universeName + ".trials.data.R");
        Date dt = new Date();

        try {
            outputFile.createNewFile();
            FileWriter out = new FileWriter(outputFile);

            out.write("########################################################\n");
            out.write("# TRIALS of - " + universeName + "\n");
            out.write("#\n");
            out.write("# Generated by EPOC Builder\n");
            out.write("# Date: " + dt.toString() + "\n");
            out.write("########################################################\n\n");

            
            out.write("Trials <- list()\n\n");
            out.write("# specific details for each unique trial\n\n");
            return out;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    /*
     * Write self as text, in the form of an R assignment, to FileWriter
     */
    public void writeAsR(FileWriter out) {
       
        try {
            out.write("########################################################\n");
            out.write("# TRIAL - " + name + " (" + revision + ")\n");
            out.write("# Description: " + super.prependEachLine(description, "#              ") + "\n");
            out.write("########################################################\n\n");

            // TODO Only one trial currently
            out.write("Trials$" + getDisplayName() + " <- list()\n");

            // Signature
            super.writeSignatureAsR(out, "Trials$" + getDisplayName());

            int ts = 0, te = 0;
            try {
                ts = Integer.parseInt(yearStart);
                te = Integer.parseInt(yearEnd);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return;
            }

            // Details
            out.write("Trials$" + getDisplayName() + "$yearStart        <- " + yearStart + "\n");
            out.write("Trials$" + getDisplayName() + "$yearEnd          <- " + yearEnd + "\n");
            out.write("Trials$" + getDisplayName() + "$yearsN           <- " + (te - ts + 1) + "\n");
            out.write("Trials$" + getDisplayName() + "$firstFishingYear <- " + fishingStart + "\n");
            out.write("Trials$" + getDisplayName() + "$lastFishingYear  <- " + fishingEnd + "\n");
            out.write("Trials$" + getDisplayName() + "$trialDir         <- file.path(RootPath, \"" + (trialDir.equals("") ? "runtime" : trialDir) + "\")\n");
            out.write("\n");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void writeFooterAsR(FileWriter out) {
        // declare data
        try {
            out.write("Trials");
            out.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /*
     * Implements toString method
     */
    public String toString() {

        return (shortname.equals("") ? epocClassname : shortname) + (!revision.equals("") ? "(" + revision + ")" : "");
    }
}
