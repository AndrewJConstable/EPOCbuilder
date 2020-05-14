/*******************************************************************************
 * ExtensionFileFilter.java
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

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

/*******************************************************************************
 * EPOC Builder ExtensionFileFilter class.
 * File filter.  Used primarily by EPOCBuilder to filter for *.R files.
 *
 * @author Troy Robertson
 * @company Australian Antarctic Division
 * @created 10/09/2009
 * @version 0.4.0, 01-09-2010
 ******************************************************************************/
public class ExtensionFileFilter extends javax.swing.filechooser.FileFilter {

    protected String description;

    protected ArrayList exts = new ArrayList();

    public void addType(String s) {
        exts.add(s);
    }

    /** Return true if the given file is accepted by this filter. */
    public boolean accept(File f) {
        // Little trick: if you don't do this, only directory names
        // ending in one of the extentions appear in the window.
        if (f.isDirectory()) {
            return true;

        } else if (f.isFile()) {
            Iterator it = exts.iterator();
            while (it.hasNext()) {
                if (f.getName().endsWith((String) it.next()))
                    return true;
            }
        }

        // A file that didn't match, or a weirdo (e.g. UNIX device file?).
        return false;
    }

    /** Set the printable description of this filter. */
    public void setDescription(String s) {
        description = s;
    }

    /** Return the printable description of this filter. */
    public String getDescription() {
        return description;
    }
}
