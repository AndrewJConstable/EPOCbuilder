/*******************************************************************************
 * OpposingPanelUI.java
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

import java.awt.*;

/*******************************************************************************
 * Abstract GUI from which all opposing gui panels can be extended.
 *
 * @author Troy Robertson
 * @company Australian Antarctic Division
 * @created 10/09/2009
 * @version 0.4.0, 01-09-2010
 ******************************************************************************/
public class OpposingPanelUI extends javax.swing.JPanel {

    private boolean modified = false;
    private boolean wasmodified = false;
    private boolean editable = true;

    // Dialog data
    private Point dialogLocation = null;
    private Dimension dialogSize = null;

    JRIExchanger rex = JRIExchanger.getInstance();

    public OpposingPanelUI() {
        //dialogLocation = this.getLocation();
        //dialogSize = this.getSize();
    }

    private void loadTemplates() { }

    /*
     * Set the location and dimensions of any sub dialogs (ie ActionUI_OLD and AttributeUI_OLD)
     */
    public void setDialogs(Point dLocation, Dimension dSize) {
        dialogLocation = dLocation;
        dialogSize = dSize;
    }

    public Point getDialogLocation() {
        return dialogLocation;
    }

    public Dimension getDialogSize() {
        return dialogSize;
    }

    public int saveIfModified() {
        if (isModified()) {
            modified = false;
            wasmodified = true;
            return EPOC_SUCC;
        } else return EPOC_NONE;
    }

    public boolean isModified() {
        return modified;
    }

    public boolean wasModified() {
        return wasmodified;
    }

    public void setModified(boolean mod) {
        modified = mod;
    }

    public void setWasModified(boolean mod) {
        wasmodified = mod;
    }

    public void editable(boolean editable) {
        this.editable = editable;
    }

    public boolean isEditable() {
        return editable;
    }

    public EPOCObject getObject() {
        return null;
    }

    public void output(String text) {
        // Write to validation screen?
        if (EPOC_DBG) System.out.println("R Output> "+text);
    }
}
