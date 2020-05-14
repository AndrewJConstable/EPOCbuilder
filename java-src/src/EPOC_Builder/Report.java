/*******************************************************************************
 * Report.java
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
 * EPOC Builder Report class.
 *
 * @author Troy Robertson
 * @company Australian Antarctic Division
 * @created 14/04/2010
 * @version 0.4.0, 01-09-2010
 ******************************************************************************/
public class Report extends EPOCObject<Report> {

    private boolean logPrint = false;
    private String logFilename = "";
    private boolean calendarPrint = false;
    private String calendarFilename = "";
    private boolean debug = false;
    private String[] headlines = {"", "", "", ""};

    /** Creates a new instance of Attribute */
    public Report() {
        setNextRevision();
        epocClassname = "Report";
    }

    public Report(int repUID, Templates templates) {
        uid = repUID;
        this.setTemplates(templates);
        storage.load(this);
    }

    public void setLogPrint(boolean state) { logPrint = state; }

    public void setLogFilename(String fname) { logFilename = fname; }

    public void setCalendarPrint(boolean state) { calendarPrint = state; }

    public void setCalendarFilename(String fname) { calendarFilename = fname; }

    public void setDebug(boolean state) { debug = state; }

    public void setHeadline(int line, String lineText) { headlines[line-1] = lineText; }

    public boolean getLogPrint() { return logPrint; }

    public String getLogFilename() { return logFilename; }

    public boolean getCalendarPrint() { return calendarPrint; }

    public String getCalendarFilename() { return calendarFilename; }

    public boolean getDebug() { return debug; }

    public String getHeadline(int line) { return headlines[line-1]; }

    public String[] getHeadlines() { return headlines; }

    /*
     * Attempt to save attribute to persistent storage
     * @return boolean true on success
     *  if false returned then error message can be obtained using method getErrMsg()
     */
    @Override
    public boolean saveToStorage() {
        if (!EPOCObject.testName(shortname)) {
            Messages.addErrMsg("Report (" + shortname + ") must be named and may only contain\n" +
                     "alphanumerics, '.' or '_'\n" +
                     "It may only start with an alphabetic character\n" +
                     "and may not end in '.' or '_'");
            return false;
        }

        if (!super.saveToStorage()) return false;

        return true;
    }

    /**
     * Copy all data members into this object effectively updating this
     * object with any changes from a copy.  This does not update any child
     * objects, but does update with linked objects.
     * @param rep
     */
    @Override
    public void updateDataMembersFrom(Report rep) {
        super.updateDataMembersFrom(rep);

        logPrint = rep.getLogPrint();
        logFilename = rep.getLogFilename();
        calendarPrint = rep.getCalendarPrint();
        calendarFilename = rep.getCalendarFilename();
        debug = rep.getDebug();
        headlines = rep.getHeadlines();
    }

    /**
     * Compares current object and its child objects with those of the object
     * passed.  If superficial then just look at EPOC data member qualities rather
     * than Epoc Builder data members.
     * @param rep Report
     * @param superficial
     * @return boolean - true if they are equal
     */
    @Override
    public boolean compare(Report rep, boolean superficial) {

        if (!super.compare(rep, superficial)) return false;

        // Do any data member comparisons
        if (rep.getLogPrint() != logPrint) return false;
        if (!rep.getLogFilename().equals(logFilename)) return false;
        if (rep.getCalendarPrint() != calendarPrint) return false;
        if (!rep.getCalendarFilename().equals(calendarFilename)) return false;
        if (!rep.getHeadline(1).equals(getHeadline(1))) return false;
        if (!rep.getHeadline(2).equals(getHeadline(2))) return false;
        if (!rep.getHeadline(3).equals(getHeadline(3))) return false;
        if (!rep.getHeadline(4).equals(getHeadline(4))) return false;
        if (rep.getDebug() != debug) return false;

        return true;
    }

    /*
     * Write self as text, in the form of an R assignment, to FileWriter
     */
    public void writeAsR(FileWriter out, String varName) {
        try {
            out.write("# Reporting parameters\n");
            out.write(varName + "$report <- list(\n");
            out.write("\tDiagnostics  = list(\n");
            out.write("\t\t\t\t\tGeneral.output = list(Print = " + (logPrint ? "TRUE" : "FALSE") +
                      ", Filename = " + (logFilename.equals("") ? "NULL" : "\"" + logFilename) + "\"),\n");
            out.write("\t\t\t\t\tCalendar = list(Print = " + (calendarPrint ? "TRUE" : "FALSE") +
                      ", Filename = " + (calendarFilename.equals("") ? "NULL" : "\"" + calendarFilename) + "\")\n");
            out.write("\t\t\t\t\t),\n");
            out.write("\tDebug        = " + (debug ? "TRUE" : "FALSE") + ",\n");
            out.write("\tHeadingLines = list(\n");
            out.write("\t\t\t\t\tHeading1 = " + (getHeadline(1).equals("") ? "NULL" : "\"" + getHeadline(1) + "\"") + ",\n");
            out.write("\t\t\t\t\tHeading2 = " + (getHeadline(2).equals("") ? "NULL" : "\"" + getHeadline(2) + "\"") + ",\n");
            out.write("\t\t\t\t\tHeading3 = " + (getHeadline(3).equals("") ? "NULL" : "\"" + getHeadline(3) + "\"") + ",\n");
            out.write("\t\t\t\t\tHeading4 = " + (getHeadline(4).equals("") ? "NULL" : "\"" + getHeadline(4) + "\"") + "\n");
            out.write("\t\t\t\t\t)\n");
            out.write(")\n");
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Implements toString method
     */
    @Override
    public String toString() {

        if (shortname.equals("")) {
            return epocClassname;
        } else {
            return epocClassname + (!revision.equals("") ? "(" + revision + ")" : "");
        }
    }
}
