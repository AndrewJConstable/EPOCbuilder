/*******************************************************************************
 * UniverseTreeCellRenderer.java
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
import javax.swing.*;
import java.awt.*;
import javax.swing.tree.*;

/*******************************************************************************
 * TreeCellRenderer for display of complete Universe tree of EPOC objects.
 *
 * @author Troy Robertson
 * @company Australian Antarctic Division
 * @created 10/09/2009
 * @version 0.4.0, 01-09-2010
 ******************************************************************************/
public class UniverseTreeCellRenderer extends DefaultTreeCellRenderer {
    ImageIcon universeIcon;
    ImageIcon configIcon;
    ImageIcon biotaIcon;
    ImageIcon environmentIcon;
    ImageIcon activityIcon;
    ImageIcon managementIcon;
    ImageIcon outputIcon;
    ImageIcon presentationIcon;
    ImageIcon documentIcon;

    public UniverseTreeCellRenderer() {
        universeIcon = new ImageIcon(getClass().getResource("/icons/earth.png"));
        configIcon = new ImageIcon(getClass().getResource("/icons/tool.png"));
        biotaIcon = new ImageIcon(getClass().getResource("/icons/fish.png"));
        environmentIcon = new ImageIcon(getClass().getResource("/icons/sun.png"));
        activityIcon = new ImageIcon(getClass().getResource("/icons/ship.png"));
        managementIcon = new ImageIcon(getClass().getResource("/icons/calculator.png"));
        outputIcon = new ImageIcon(getClass().getResource("/icons/output.png"));
        presentationIcon = new ImageIcon(getClass().getResource("/icons/printer.png"));
        documentIcon = new ImageIcon(getClass().getResource("/icons/document.png"));
    }

    public Component getTreeCellRendererComponent(
                        JTree tree,
                        Object value,
                        boolean sel,
                        boolean expanded,
                        boolean leaf,
                        int row,
                        boolean hasFocus) {

        super.getTreeCellRendererComponent(
                        tree, value, sel,
                        expanded, leaf, row,
                        hasFocus);

        // Set Icon
        try {
            ImageIcon icon = getImageIcon((DefaultMutableTreeNode)value);
            if (icon != null) setIcon(icon);
        } catch (NullPointerException e) {

        }
        
        // Set bold if template
        if (isTemplate((DefaultMutableTreeNode)value)) {
            setFont(getFont().deriveFont(Font.BOLD));
        } else {
            setFont(getFont().deriveFont(Font.PLAIN));
        }

        // if action object then colour appropriately
        if (isSetupAction((DefaultMutableTreeNode)value)) {
            setForeground(Color.PINK);
        } else if (isSupportAction((DefaultMutableTreeNode)value)) {
            setForeground(Color.ORANGE);
        } else {
            //setForeground(Color.BLACK);
            //setBackground(getBackground());
        }

        if (EPOC_DBG) {
            setToolTipText(getUID((DefaultMutableTreeNode)value));
        } else {
            setToolTipText("<html>" + getDescription((DefaultMutableTreeNode)value).replace("\n", "<br>") + "</html>");
        }

        return this;
    }

    private ImageIcon getImageIcon(DefaultMutableTreeNode node) {
        Object obj = node.getUserObject();

        if (node.isRoot()) {
            return universeIcon;
        } else if (obj.equals("Configuration")) {
            return configIcon;
        } else if (obj.equals("BIOTA")) {
            return biotaIcon;
        } else if (obj.equals("ENVIRONMENT")) {
            return environmentIcon;
        } else if (obj.equals("ACTIVITY")) {
            return activityIcon;
        } else if (obj.equals("MANAGEMENT")) {
            return managementIcon;
        } else if (obj.equals("OUTPUT")) {
            return outputIcon;
        } else if (obj.equals("PRESENTATION")) {
            return presentationIcon;
        } else if (node.isLeaf()) {
            return documentIcon;
        }

        return null;
    }

    private boolean isTemplate(DefaultMutableTreeNode node) {
        Object obj = node.getUserObject();
        if (!(obj instanceof EPOCObject)) return false;

        return ((EPOCObject)obj).isTemplate();
    }

    private boolean isSetupAction(DefaultMutableTreeNode node) {
        Object obj = node.getUserObject();
        if (obj instanceof Action && ((Action)obj).isSetup()) return true;

        return false;
    }

    private boolean isSupportAction(DefaultMutableTreeNode node) {
        Object obj = node.getUserObject();
        if (obj instanceof Action && ((Action)obj).isSupport()) return true;

        return false;
    }

    private String getUID(DefaultMutableTreeNode node) {
        Object obj = node.getUserObject();
        if (!(obj instanceof EPOCObject)) return "";

        return String.valueOf(((EPOCObject)obj).getUID());
    }

    private String getDescription(DefaultMutableTreeNode node) {
        Object obj = node.getUserObject();
        if (!(obj instanceof EPOCObject)) return "";

        return ((EPOCObject)obj).getDescription();
    }
}
