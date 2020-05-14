/*******************************************************************************
 * Spatial.java
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

import java.util.Date;
import java.util.Vector;
import java.io.*;
import static au.gov.aad.erm.EPOC_Builder.Constants.*;

/*******************************************************************************
 * EPOC Builder Spatial class.
 *
 * @author Troy Robertson
 * @company Australian Antarctic Division
 * @created 14/04/2010
 * @version 0.4.0, 01-09-2010
 ******************************************************************************/
public class Spatial extends EPOCObject<Spatial> {

    //private String polygons = "";
    private Vector<Vector> polygonsVector = new Vector();
    private Vector<Vector> overlapsVector = new Vector();

    /** Creates a new instance of Attribute */
    public Spatial() {
        setNextRevision();
        epocClassname = "Spatial";
    }

    public Spatial(int spaUID, Templates templates) {
        uid = spaUID;
        this.setTemplates(templates);
        storage.load(this);
    }

    public void setPolygonsString(String polys) {
        polygonsVector = stringToVector(polys);
    }

    public void setOverlapsString(String overs) {
        Vector<Vector> oversVec = new Vector();

        if (overs != null && !overs.equals("")) {
            for (Object obj : overs.split("\n")) {
                Vector row = new Vector();
                // need to remove excess "," at end of each line (except last line)
                for (Object rObj : ((String)obj).split(",")) {
                    if (rObj == null || rObj.toString().equals("0") || rObj.toString().equals("")
                            || rObj.toString().equalsIgnoreCase("FALSE")) {
                        row.add(Boolean.FALSE);
                    } else {
                        row.add(Boolean.TRUE);
                    }
                }
             
                oversVec.add(row);
            }
        }

        overlapsVector = oversVec;
        //overlapsVector = stringToVector(overs);
    }

    public void setPolygons2D(Object[][] polys2D) {
        //return polygons;
        polygonsVector.clear();
        for (int i = 0 ; i < polys2D.length ; i++) {
            Vector rVec = new Vector();
            for (int j = 0 ; j < polys2D[i].length ; j++) {
                rVec.add(polys2D[i][j]);
            }
            polygonsVector.add(rVec);
        }
    }

    public void setOverlaps2D(Boolean[][] overs2D) {
        //return polygons;
        overlapsVector.clear();
        for (int i = 0 ; i < overs2D.length ; i++) {
            Vector rVec = new Vector();
            for (int j = 0 ; j < overs2D[i].length ; j++) {
                rVec.add(overs2D[i][j]);
            }
            overlapsVector.add(rVec);
        }
    }

    public void setPolygonsVector(Vector<Vector> polysV) {
        polygonsVector = polysV;
    }

    public void setOverlapsVector(Vector<Vector> oversV) {
        overlapsVector = oversV;
    }

    public String getPolygonsString() {
        return vectorToString(polygonsVector);
    }

    public String getOverlapsString() {
        String pStr = null;

        for (Vector row : overlapsVector) {
            pStr = (pStr == null ? "" : pStr + ",\n");
            String rowStr = null;
            for (Object rObj : row) {
                rowStr = (rowStr == null ? "" : rowStr + ",");
                if (rObj == null || rObj.toString().equals("0") || rObj.toString().equals("")
                            || rObj.toString().equalsIgnoreCase("FALSE")) {
                    rowStr += "0";
                } else {
                     rowStr += "1";
                }
               
                //rowStr += (rObj == null || rObj.toString().equals("") ? replaceChar : rObj.toString());
            }
            pStr += rowStr;
        }

        return (pStr == null ? "" : pStr);
        //return vectorToString(overlapsVector, "0");
    }

    public Object[][] getPolygons2D() {
        int i = 0;
        Object[][]arrPolys = new Object[polygonsVector.size()][5];
        for (Object rObj : polygonsVector) {
            arrPolys[i] = ((Vector)rObj).toArray(new Object[0]);
            i++;
        }
        return arrPolys;
    }

    public Object[][] getOverlaps2D() {
        return vectorTo2D(overlapsVector);
    }

    public Vector<Vector> getPolygonsVector() {
        return polygonsVector;
    }

    public Vector<Vector> getPolygonsVectorClone() {
        Vector<Vector> polygonsCopy = new Vector();
        for (Vector row : polygonsVector) {
            Vector rowCopy = new Vector();
            for (Object rObj : row) rowCopy.add((String)rObj);
            polygonsCopy.add(rowCopy);
        }

        return polygonsCopy;
    }

    public Vector<Vector> getOverlapsVector() {
        return overlapsVector;
    }

    public Vector<Vector> getOverlapsVectorClone() {
        Vector<Vector> overlapsCopy = new Vector();
        for (Vector row : overlapsVector) {
            Vector rowCopy = new Vector();
            for (Object rObj : row) rowCopy.add((Boolean)rObj);
            overlapsCopy.add(rowCopy);
        }

        return overlapsCopy;
    }

    public static Object[][] vectorTo2D(Vector vec) {
        int i = 0;
        int j = 0;

        Object[][]arrOvers = new Object[vec.size()][((Vector)vec.get(0)).size()];
        for (Object rObj : vec) {
            j = 0;
            for (Object cObj : (Vector)rObj) {
                arrOvers[i][j] = cObj;
                j++;
            }
            i++;
        }
        
        return arrOvers;
    }

    public static Vector stringToVector(String pStr) {
        Vector polyVec = new Vector();

        if (pStr != null && !pStr.equals("")) {
            for (Object obj : pStr.split("\n")) {
                Vector row = new Vector();
                // need to remove excess "," at end of each line (except last line)
                for (Object rObj : ((String)obj).split(",")) {
                    row.add((String)rObj);
                }
                /*
                String[] items = ((String)obj).split(",");
                int lastIndx = items.length;
                if (((String)obj).endsWith(",")) lastIndx = lastIndx - 1;
                for (int i = 0 ; i < items.length ; i++) {
                    row.add(items[i]);
                }
                 * */
                polyVec.add(row);
            }
        }

        return polyVec;
    }

    public static String vectorToString(Vector pVec, String replaceChar) {
        String pStr = null;

        for (Object obj : pVec) {
            pStr = (pStr == null ? "" : pStr + ",\n");
            String rowStr = null;
            for (Object rObj : (Vector)obj) {
                rowStr = (rowStr == null ? "" : rowStr + ",");
                rowStr += (rObj == null || rObj.toString().equals("") ? replaceChar : rObj.toString());
            }
            pStr += rowStr;
        }

        return(pStr == null ? "" : pStr);
    }

    public static String vectorToString(Vector pVec) {
        
        return vectorToString(pVec, "");
    }

    /*
     * Attempt to save attribute to persistent storage
     * @return boolean true on success
     *  if false returned then error message can be obtained using method getErrMsg()
     */
    @Override
    public boolean saveToStorage() {
        if (!EPOCObject.testName(shortname)) {
            Messages.addErrMsg("Spatial (" + shortname + ") must be named and may only contain\n" +
                     "alphanumerics, '.' or '_'\n" +
                     "It may only start with an alphabetic character\n" +
                     "and may not end in '.' or '_'");
            return false;
        }

        if (!super.saveToStorage()) return false;

        return true;
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
            out.write("# SPATIAL - " + name + " (" + revision + ")\n");
            out.write("# Description: " + super.prependEachLine(description, "#              ") + "\n");
            out.write("#\n");
            out.write("# Generated by EPOC Builder\n");
            out.write("# Date: " + dt.toString() + "\n");
            out.write("########################################################\n\n");

            out.write("# Polygons / Spatial distribution data file\n");
            out.write("Polygons <- list()\n\n");

            // Signature
            super.writeSignatureAsR(out, "Polygons");

            // Polygons output stuff
            String nameStr = "", areaStr = "", coordStr = "", cAreaStr = "", cPropStr = "";
            int i = 1;
            for (Object obj : getPolygonsVector()) {
                nameStr += (nameStr.equals("") ? "" : ",\n") + "\"" + (String)((Vector)obj).get(0) + "\"";
                areaStr += (areaStr.equals("") ? "" : ",\n") + (String)((Vector)obj).get(1);
                coordStr += (coordStr.equals("") ? "" : ",\n") + "A" + i + " = c(" + (String)((Vector)obj).get(2) + ")";
                cAreaStr += (cAreaStr.equals("") ? "" : ",\n") + "A" + i + " = " + (String)((Vector)obj).get(3);
                cPropStr += (cPropStr.equals("") ? "" : ",\n") + "A" + i + " = " + (String)((Vector)obj).get(4);
                i++;
            }
            out.write("Polygons$polygonNames <- c(\n");
            out.write("\t" + super.prependEachLine(nameStr, "\t")+"\n");
            out.write(")\n\n");
            out.write("Polygons$polygonAreas <- c(\n");
            out.write("\t" + super.prependEachLine(areaStr, "\t")+"\n");
            out.write(")\n\n");
            out.write("Polygons$coords <- list(\n");
            out.write("\t" + super.prependEachLine(coordStr, "\t")+"\n");
            out.write(")\n\n");
            out.write("Polygons$coordAreas <- list(\n");
            out.write("\t" + super.prependEachLine(cAreaStr, "\t")+"\n");
            out.write(")\n\n");
            out.write("Polygons$coordProportions <- list(\n");
            out.write("\t" + super.prependEachLine(cPropStr, "\t")+"\n");
            out.write(")\n\n");
            out.write("Polygons$overlap <- matrix(c(\n");
            out.write("\t" + super.prependEachLine(getOverlapsString(), "\t")+"\n");
            out.write("\t), nrow=" + getOverlapsVector().size() + ", byrow=TRUE)\n\n");

            // declare data
            out.write("Polygons");

            out.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /*
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

    /**
     * Copy all data members into this object effectively updating this
     * object with any changes from a copy.  This does not update any child
     * objects, but does update with linked objects.
     * @param spa
     */
    @Override
    public void updateDataMembersFrom(Spatial spa) {
        super.updateDataMembersFrom(spa);

        polygonsVector = spa.getPolygonsVector();
        overlapsVector = spa.getOverlapsVector();
    }

    /**
     * Compares current object and its child objects with those of the object
     * passed.  If superficial then just look at EPOC data member qualities rather
     * than Epoc Builder data members.
     * @param spa Spatial
     * @param superficial
     * @return boolean - true if they are equal
     */
    public boolean compare(Spatial spa, boolean superficial) {

        if (!super.compare(spa, superficial)) {
            return false;
        }

        // Do any data member comparisons
        if(!spa.getPolygonsString().equals(getPolygonsString())) return false;
        if(!spa.getOverlapsString().equals(getOverlapsString())) return false;

        return true;
    }

    /**
     * Dummy for EPOCObject to call with recurse default to false
     * @param method
     * @param uni
     * @return cloned Spatial object
     */
    @Override
    protected Spatial clone(int method, Universe uni) {
        return clone(method, (method == EPOC_RPL), uni);
    }

    /**
     * Return a deep copy of this spatial object.
     * If revise then copy element but reset uid and set new
     * highest revision.
     * @param method
     * @param recurse
     * @param uni
     * @return cloned Action
     */
    protected Spatial clone(int method, boolean recurse, Universe uni) {
        Spatial spa = (Spatial)super.clone(method, uni);

        // Hollow out clone
        spa.polygonsVector = new Vector();
        spa.overlapsVector = new Vector();

        // Do we just break it?
        if (method == EPOC_BRK) {
            spa.setBroken();
            return spa;
        }

        // Polygons
        spa.setPolygonsVector(getPolygonsVectorClone());
        // Overlaps
        spa.setOverlapsVector(getOverlapsVectorClone());

        return spa;
    }
}
