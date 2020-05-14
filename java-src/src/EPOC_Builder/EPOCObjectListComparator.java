/*******************************************************************************
 * EPOCObjectListComparator.java
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

import java.util.Comparator;

/*******************************************************************************
 * EPOC Builder list Comparator class.
 * Allows comparison (ordering) of EPOC Object lists based on an
 * ordering method.
 *
 * @author Troy Robertson
 * @company Australian Antarctic Division
 * @created 10/09/2009
 * @version 0.4.0, 01-09-2010
 ******************************************************************************/
public class EPOCObjectListComparator implements Comparator<EPOCObject> {

    int displayMethod = DSPL_LST_NM_ASC;

    public EPOCObjectListComparator() {
        super();
    }

    public EPOCObjectListComparator(int method) {
        this();
        displayMethod = method;
    }

    public int compare(EPOCObject o1, EPOCObject o2) {
        // First by action type (if its an action)
        if (o1 instanceof Action && o2 instanceof Action) {
            if (((Action)o1).getActType() != ((Action)o2).getActType()) {
                return ((Action)o1).getActType() - ((Action)o2).getActType();
            }
        }

        if (displayMethod == DSPL_LST_NM_ASC || displayMethod == DSPL_TRE_NM_ASC) {
            // then by shortname,
            if (o1.getShortName().compareToIgnoreCase(o2.getShortName()) == 0) {
                return compareRevision(o1.getRevision(), o2.getRevision());
            }
            // and then by revision number
            return o1.getShortName().compareToIgnoreCase(o2.getShortName());
        } else {
            // then by shortname,
            if (o1.getRevision().compareToIgnoreCase(o2.getRevision()) == 0) {
                return o1.getShortName().compareToIgnoreCase(o2.getShortName());
            }

            return compareRevision(o1.getRevision(), o2.getRevision());
        }
    }

    public static int compareRevision(String rev1, String rev2) {
        final int BEFORE = -1;
        final int EQUAL = 0;
        final int AFTER = 1;

        if (rev1.equals(rev2)) return EQUAL;
        if (rev1.equals("")) return BEFORE;
        if (rev2.equals("")) return AFTER;

        String[] rev1Bits = rev1.split("\\.");
        String[] rev2Bits = rev2.split("\\.");
        int parts = (rev1Bits.length < rev2Bits.length ? rev1Bits.length : rev2Bits.length);

        for (int i = 0 ; i < parts ; i++) {
            if (Integer.parseInt(rev1Bits[i]) < Integer.parseInt(rev2Bits[i])) return BEFORE;
            if (Integer.parseInt(rev1Bits[i]) > Integer.parseInt(rev2Bits[i])) return AFTER;
        }

        if (rev1Bits.length == rev2Bits.length) return EQUAL;
        return (rev1Bits.length < rev2Bits.length ? BEFORE : AFTER);
    }
}
