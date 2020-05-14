/*******************************************************************************
 * Attribute.java
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

import java.io.FileWriter;

/*******************************************************************************
 * EPOC Builder Attribute class
 *
 * @author Troy Robertson
 * @company Australian Antarctic Division
 * @created 10/09/2009
 * @version 0.4.0, 01-09-2010
 ******************************************************************************/
public class Attribute extends EPOCObject<Attribute> {

    private String value = "";
    
    /** Creates a new instance of Attribute */
    public Attribute() {
        setNextRevision();
    }

    public Attribute(String rev) {
        if (rev != null) setRevision(rev); else setNextRevision();
    }

    public Attribute(int attUID, Templates templates) {
        uid = attUID;
        this.setTemplates(templates);
        storage.load(this);
    }
  
    public void setValue(String attrVal) {
        value = attrVal;
    }
    
    public String getValue() {
        return value;
    }
    
    /*
     * Attempt to save attribute to persistent storage
     * @return boolean true on success
     *  if false returned then error message can be obtained using method getErrMsg()
     */
    public boolean saveToStorage() {
        if (!EPOCObject.testName(shortname)) {
            Messages.addErrMsg("Attribute (" + shortname + ") must be named and may only contain\n" +
                     "alphanumerics, '.' or '_'\n" + 
                     "It may only start with an alphabetic character\n" +
                     "and may not end in '.' or '_'");
            return false;
        }
       
        if (!super.saveToStorage()) return false;
        
        return true;
    }

    public boolean validate(Element ele) {
        boolean passed = true;
        int next = 0, starting = 0;

        rex.clear();

        // check all attributes used in value exist
        while ((next = getValue().indexOf(EPOC_ELESTR, starting)) != -1) {

            starting = next + (EPOC_ELESTR + "$").length();
            // pull out all characters until not alpha, numeric, underscore or period
            String attstr = getValue().substring(starting).split("[\\W&&[^\\Q.\\E]]", 2)[0];

            // polygonsN is always calculated
            if (attstr.equals("polygonsN")) continue;

            // search for attribute in element
            boolean found = false, after = false;
            for (Attribute att : ele.getAttributes()) {
                if (att.equals(this)) after = true;
                if (att.getShortName().equals(attstr)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                Messages.addErrMsg("Referenced attribute '" + attstr + "' is not present in element!");
                passed = false;
            } else if (after) {
                Messages.addErrMsg("Referenced attribute '" + attstr + "' is listed after referee attribute!");
                passed = false;
            }

            // continue search from end of last attribute identifier
            starting = starting + attstr.length();
        }

        // parse code using JRI
        if (rex.hasEngine()) {
            if (!rex.parse(getValue())) passed = false;
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
     * @param att
     */
    @Override
    public void updateDataMembersFrom(Attribute att) {
        super.updateDataMembersFrom(att);

        value = att.getValue();
    }

    /*
     * Compares current object and its child objects with those of the object
     * passed.  If superficial then just look at EPOC data member qualities rather
     * than Epoc Builder data members.
     * @param att Attribute
     * @param superficial
     * @return boolean - true if they are equal
     */
    @Override
    public boolean compare(Attribute att, boolean superficial) {
        
        if (!super.compare(att, superficial)) return false;
        
        if (!att.getValue().equals(value)) return false;
        
        return true;
    }
    
    /*
     * Write self as text, in the form of an R assignment, to FileWriter
     */
    public void writeAsR(FileWriter out, String varName) {
        try {
            out.write("### " + shortname + " (" + revision + ")\n");
            out.write("# Description: " + super.prependEachLine(description, "#             ") + "\n");
            if (!value.equals("")) {
                String val = value.replaceAll(EPOC_ELESTR, varName);
                val = super.prependEachLine(val, "\t\t\t\t\t\t");
                out.write(varName + "$" + shortname + "         <- " + val + "\n\n");
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Return a deep copy of this attribute
     * If revise then copy but reset uid and set
     * revision to next available
     *
     * @param method
     * @param uni
     * @return
     */
    @Override
    protected Attribute clone(int method, Universe uni) {    
        Attribute att = (Attribute)super.clone(method, uni);
        if (method == EPOC_CLN) return att;

        // Do we just break it?
        if (method == EPOC_BRK) att.setBroken();

        return att;
    }
}
