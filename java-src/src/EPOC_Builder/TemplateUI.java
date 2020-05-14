/*******************************************************************************
 * TemplateUI.java
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
import javax.swing.tree.*;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import java.util.*;
import java.awt.event.*;
import java.awt.Point;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/*******************************************************************************
 * GUI for Templates in either JList or JTree form.
 *
 * @author Troy Robertson
 * @company Australian Antarctic Division
 * @created 15/09/2009
 * @version 0.4.0, 01-09-2010
 ******************************************************************************/
public class TemplateUI extends OpposingPanelUI implements TreeSelectionListener {

    private Universe universe;
    private Element element;
    private EPOCBuilderUI parentUI;
    private JTree jTreeTemplates = new JTree();

    int objType;
    int modType = OBJ_ELE;
    String objTypeName = "";

    /** Creates new form jPanelElementTemplateList */
    /**
     * Constructor for TemplateUI JPanel to be used if listing is for objects
     * which are direct children of Universe
     * @param parent EPOCBuilderUI
     * @param uni Universe
     * @param oType Object type
     */
    public TemplateUI(EPOCBuilderUI parent, Universe uni, int oType) {
        parentUI = parent;
        universe = uni;
        objType = oType;
        objTypeName = EPOCObject.getObjectTypeName(objType);

        initComponents();
        editable(EPOC_EDIT_TEMPL || objType == OBJ_CLS);
        jButtonReviseTemplate.setVisible(objType == OBJ_CLS);
        setDisplayButton(EPOC_LIST_ORDERING);

        // Set up jtree view
        createTreeView(jTreeTemplates);

        if (objType != OBJ_CLS) loadTemplates();
    }

    /**
     * Constructor for TemplateUI JPanel to be used if listing is for
     * Actions or Attributes
     * @param parent EPOCBuilderUI
     * @param uni Universe
     * @param ele Parent element
     * @param oType Object type
     */
    public TemplateUI(EPOCBuilderUI parent, Universe uni, Element ele, int oType) {
        this(parent, uni, oType);
        element = ele;
    }

    /**
     * Constructor for TemplateUI JPanel to be used if listing is for
     * EClass templates
     * @param parent EPOCBuilderUI
     * @param uni Universe
     * @param oType Object type
     * @param mType Module type
     */
    public TemplateUI(EPOCBuilderUI parent, Universe uni, int oType, int mType) {
        this(parent, uni, oType);
        modType = mType;
        jButtonUseTemplate.setText("New");
        jButtonUseTemplate.setIcon(null);
        loadTemplates();
    }

    @Override
    public void editable(boolean editable) {
        super.editable(editable);
        jButtonViewTemplate.setText(((editable ? "Edit" : "View")));
    }

    private void createTreeView(JTree tree) {
        tree.setRootVisible(false);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeSelectionListener(this);
        tree.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jListTemplatesMouseClicked(evt);
            }
        });
    }

    /**
     * Used by initComponents to form TitledBorder title
     * @return String border title
     */
    private String getBorderTitle() {
        return objTypeName+" Templates:";

    }

    /**
     * Gets the currently selected EPOCObject from the list/tree
     * @return
     */
    private EPOCObject getSelectedTemplate() {
        EPOCObject selected = null;

        if (EPOC_LIST_ORDERING == DSPL_LST_NM_ASC || EPOC_LIST_ORDERING == DSPL_LST_RV_ASC) {
            if (jListTemplates.getSelectedIndex() >= 0) selected = (EPOCObject)jListTemplates.getSelectedValue();
        } else {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)jTreeTemplates.getLastSelectedPathComponent();
            // retrieve the node that was selected
            if (node != null) selected = (EPOCObject)node.getUserObject();
        }

        return selected;
    }

    /**
     * Set the list or tree item that was right clicked as the selected item
     * @param me
     */
    private void setRightClickedCellSelected(MouseEvent me) {
        if (EPOC_LIST_ORDERING == DSPL_LST_NM_ASC || EPOC_LIST_ORDERING == DSPL_LST_RV_ASC) {
            jListTemplates.setSelectedIndex(jListTemplates.locationToIndex(me.getPoint()));
        } else {
            // Select right-clicked tree node first
            jTreeTemplates.setSelectionPath(jTreeTemplates.getPathForLocation(me.getX(), me.getY()));
        }
    }

    /**
     * Load the list of templates of the type proscribed from universe.
     * Include any already used templates (TR 18/3/10).
     */
    private void loadTemplates() {
        ArrayList<EPOCObject> list = new ArrayList();

        // Add templates to list.  If EClass then only add those of type
        // OBJ_ELE or modType
        for (EPOCObject eo : universe.getTemplates().getTemplateList(objType)) {
            if (objType != OBJ_CLS || (((EClass)eo).getModType() == OBJ_ELE || ((EClass)eo).getModType() == modType)) {
                list.add(eo);
            }
        }

        if (EPOC_LIST_ORDERING == DSPL_LST_NM_ASC || EPOC_LIST_ORDERING == DSPL_LST_RV_ASC) {
            jScrollPaneTemplates.setViewportView(jListTemplates);

            // Define a different sort method on just name/revision
            Collections.sort(list, new EPOCObjectListComparator(EPOC_LIST_ORDERING));
            DefaultListModel dlm = new DefaultListModel();
            for (EPOCObject item : list) dlm.addElement(item);
            jListTemplates.setModel(dlm);

            //create Renderer and display
            jListTemplates.setCellRenderer(new EPOCObjectListRenderer());
            //Add listener to components that can bring up popup menus.           
            jListTemplates.addMouseListener(new PopupListener(new JPopupMenu()));
        } else {
            jScrollPaneTemplates.setViewportView(jTreeTemplates);

            // build the model
            RevisionTreeModel treeModel = new RevisionTreeModel(EPOC_LIST_ORDERING);
            treeModel.buildModel(list);
            jTreeTemplates.setModel(treeModel);
            //create Renderer and display
            jTreeTemplates.setCellRenderer(new UniverseTreeCellRenderer());
            //Add listener to components that can bring up popup menus.
            jTreeTemplates.addMouseListener(new PopupListener(new JPopupMenu()));
        }
    }

    /**
     * Implements required method for TreeSelectionListener
     * @param e
     */
    public void valueChanged(TreeSelectionEvent tse) {
        // Dummy
    }

    /**
     * Display EPOC object form.
     * @param obj EPOC Object
     * @param editable Defines whether form is editable
     * @return Modified EPOC object.
     */
    private EPOCObject showTemplateUI(EPOCObject obj, boolean editable) {
        OpposingPanelUI opui;
        Point position = this.getLocationOnScreen();
        Dimension size = this.getSize();

        // Open new frame with element details form filled
        if (objType == OBJ_CLS) {
            opui = new EClassUI((EClass)obj, universe);
            position = getParent().getLocationOnScreen();
            size = getParent().getSize();
        } else if (objType == OBJ_SPA) {
            opui = new SpatialUI((Spatial)obj, universe);
            position = getParent().getLocationOnScreen();
            size = getParent().getSize();
        } else if (objType == OBJ_REP) {
            opui = new ReportUI((Report)obj, universe);
            position = getParent().getLocationOnScreen();
            size = getParent().getSize();
        } else if (objType == OBJ_TRI) {
            opui = new TrialUI((Trial)obj, universe);
            position = getParent().getLocationOnScreen();
            size = getParent().getSize();
        } else if (objType == OBJ_ELE || objType / 10 == OBJ_ELE) {
            opui = new ElementCompleteUI((Element)obj, universe);
            position = getParent().getLocationOnScreen();
            size = getParent().getSize();
        } else if (objType == OBJ_ACT) {
            opui = new ActionUI((Action)obj, null, universe);
        } else if (objType == OBJ_ATT) {
            opui = new AttributeUI((Attribute)obj, element);
        } else {
            return null;
        }

        opui.setDialogs(position, size);
        opui.editable(editable);

        final JOptionPane pane = new JOptionPane(opui, JOptionPane.PLAIN_MESSAGE,
                JOptionPane.CLOSED_OPTION);
        pane.setBorder(null);
        final JDialog dialog = new JDialog(parentUI, true);
        dialog.setUndecorated(true);
        dialog.setContentPane(pane);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setLocation(position);
        dialog.setPreferredSize(size);
        pane.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                if (e.getPropertyName().equals(JOptionPane.VALUE_PROPERTY)
                        && e.getNewValue() != JOptionPane.UNINITIALIZED_VALUE) {
                    if (dialog.isVisible() && ((OpposingPanelUI)pane.getMessage()).saveIfModified() != EPOC_FAIL) {
                        dialog.setVisible(false);
                    } else {
                        pane.setValue(JOptionPane.UNINITIALIZED_VALUE);
                    }
                }
            }
        });
        dialog.pack();
        dialog.setVisible(true);

        if (editable && ((OpposingPanelUI)pane.getMessage()).wasModified()) return obj;
        return null;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanelTemplates = new javax.swing.JPanel();
        jScrollPaneTemplates = new javax.swing.JScrollPane();
        jListTemplates = new javax.swing.JList();
        jButtonViewTemplate = new javax.swing.JButton();
        jButtonDeleteTemplate = new javax.swing.JButton();
        jButtonUseTemplate = new javax.swing.JButton();
        jButtonExportTemplate = new javax.swing.JButton();
        jButtonDisplay = new javax.swing.JButton();
        jButtonReviseTemplate = new javax.swing.JButton();

        jPanelTemplates.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), getBorderTitle(), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

        jListTemplates.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jListTemplates.setAlignmentX(2.0F);
        jListTemplates.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jListTemplatesMouseClicked(evt);
            }
        });
        jScrollPaneTemplates.setViewportView(jListTemplates);

        jButtonViewTemplate.setForeground(new java.awt.Color(0, 0, 255));
        jButtonViewTemplate.setText("View");
        jButtonViewTemplate.setAlignmentX(0.5F);
        jButtonViewTemplate.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButtonViewTemplate.setMaximumSize(new java.awt.Dimension(55, 20));
        jButtonViewTemplate.setMinimumSize(new java.awt.Dimension(55, 20));
        jButtonViewTemplate.setPreferredSize(new java.awt.Dimension(55, 20));
        jButtonViewTemplate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonViewTemplateActionPerformed(evt);
            }
        });

        jButtonDeleteTemplate.setForeground(new java.awt.Color(0, 0, 255));
        jButtonDeleteTemplate.setText("Delete");
        jButtonDeleteTemplate.setAlignmentX(0.5F);
        jButtonDeleteTemplate.setIconTextGap(0);
        jButtonDeleteTemplate.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButtonDeleteTemplate.setMaximumSize(new java.awt.Dimension(55, 20));
        jButtonDeleteTemplate.setMinimumSize(new java.awt.Dimension(55, 20));
        jButtonDeleteTemplate.setPreferredSize(new java.awt.Dimension(55, 20));
        jButtonDeleteTemplate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDeleteTemplateActionPerformed(evt);
            }
        });

        jButtonUseTemplate.setForeground(new java.awt.Color(0, 0, 204));
        jButtonUseTemplate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/left.png"))); // NOI18N
        jButtonUseTemplate.setText("Use");
        jButtonUseTemplate.setToolTipText("Use Template");
        jButtonUseTemplate.setAlignmentX(0.5F);
        jButtonUseTemplate.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButtonUseTemplate.setMaximumSize(new java.awt.Dimension(55, 20));
        jButtonUseTemplate.setMinimumSize(new java.awt.Dimension(55, 20));
        jButtonUseTemplate.setPreferredSize(new java.awt.Dimension(55, 20));
        jButtonUseTemplate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonUseTemplateActionPerformed(evt);
            }
        });

        jButtonExportTemplate.setForeground(new java.awt.Color(0, 0, 255));
        jButtonExportTemplate.setText("Export");
        jButtonExportTemplate.setAlignmentX(0.5F);
        jButtonExportTemplate.setIconTextGap(0);
        jButtonExportTemplate.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButtonExportTemplate.setMaximumSize(new java.awt.Dimension(55, 20));
        jButtonExportTemplate.setMinimumSize(new java.awt.Dimension(55, 20));
        jButtonExportTemplate.setPreferredSize(new java.awt.Dimension(55, 20));
        jButtonExportTemplate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonExportTemplateActionPerformed(evt);
            }
        });

        jButtonDisplay.setForeground(new java.awt.Color(0, 0, 255));
        jButtonDisplay.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/listview.png"))); // NOI18N
        jButtonDisplay.setText("a-z");
        jButtonDisplay.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButtonDisplay.setMaximumSize(new java.awt.Dimension(55, 20));
        jButtonDisplay.setMinimumSize(new java.awt.Dimension(55, 20));
        jButtonDisplay.setPreferredSize(new java.awt.Dimension(55, 20));
        jButtonDisplay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDisplayActionPerformed(evt);
            }
        });

        jButtonReviseTemplate.setForeground(new java.awt.Color(0, 0, 255));
        jButtonReviseTemplate.setText("Revise");
        jButtonReviseTemplate.setAlignmentX(0.5F);
        jButtonReviseTemplate.setIconTextGap(0);
        jButtonReviseTemplate.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButtonReviseTemplate.setMaximumSize(new java.awt.Dimension(55, 20));
        jButtonReviseTemplate.setMinimumSize(new java.awt.Dimension(55, 20));
        jButtonReviseTemplate.setPreferredSize(new java.awt.Dimension(55, 20));
        jButtonReviseTemplate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonReviseTemplateActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelTemplatesLayout = new javax.swing.GroupLayout(jPanelTemplates);
        jPanelTemplates.setLayout(jPanelTemplatesLayout);
        jPanelTemplatesLayout.setHorizontalGroup(
            jPanelTemplatesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelTemplatesLayout.createSequentialGroup()
                .addGroup(jPanelTemplatesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelTemplatesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanelTemplatesLayout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(jPanelTemplatesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jButtonViewTemplate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jButtonDeleteTemplate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jButtonUseTemplate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelTemplatesLayout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(jButtonExportTemplate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(0, 0, 0)))
                    .addGroup(jPanelTemplatesLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jButtonDisplay, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanelTemplatesLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jButtonReviseTemplate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPaneTemplates, javax.swing.GroupLayout.DEFAULT_SIZE, 508, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanelTemplatesLayout.setVerticalGroup(
            jPanelTemplatesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelTemplatesLayout.createSequentialGroup()
                .addGroup(jPanelTemplatesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPaneTemplates, javax.swing.GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanelTemplatesLayout.createSequentialGroup()
                        .addComponent(jButtonUseTemplate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonViewTemplate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonDeleteTemplate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonExportTemplate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonReviseTemplate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 73, Short.MAX_VALUE)
                        .addComponent(jButtonDisplay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanelTemplates, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanelTemplates, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanelTemplates.getAccessibleContext().setAccessibleName("Element Templates:");
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Display selected template object
     * @param evt
     */
    private void jButtonViewTemplateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonViewTemplateActionPerformed
        EPOCObject eObj = getSelectedTemplate();
        if (eObj == null) {
            JOptionPane.showMessageDialog(this, "An "+objTypeName+" must be selected first!");
            return;
        }

        showTemplateUI(eObj, isEditable());
        if (isEditable()) ((DefaultTreeModel)jTreeTemplates.getModel()).nodeChanged((DefaultMutableTreeNode)jTreeTemplates.getLastSelectedPathComponent());
}//GEN-LAST:event_jButtonViewTemplateActionPerformed

    /**
     * Delete selected template object
     * @param evt
     */
    private void jButtonDeleteTemplateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDeleteTemplateActionPerformed
        EPOCObject eObj = getSelectedTemplate();
        if (eObj == null) {
            JOptionPane.showMessageDialog(this, "An "+objTypeName+" must be selected first!");
            return;
        }

        // check that template is not already being used
        if (universe.isMemberTemplate(eObj)) {
            JOptionPane.showMessageDialog(this, "Cannot delete, "+objTypeName+" template is currently in use\n"
                    + (eObj instanceof Element
                    ? "or contains Action or Attribute templates that are in use!" : ""));
            return;
        }

        // check that template is not a linked object
        if (universe.isLinkedTemplate(eObj)) {
            if (JOptionPane.showConfirmDialog(this, "Deleting this template will create broken links!\n"
                                                  + "Delete anyway?",
                        "Broken Links", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
                return;
            }
        }

        // if its already a saved object then put it in
        // template delete list to be deleted on universe save
        if (eObj instanceof EClass || eObj instanceof Element || eObj instanceof Spatial
                || eObj instanceof Report || eObj instanceof Trial) {
            universe.deleteTemplate(eObj);
        } else if (eObj instanceof Action || eObj instanceof Attribute) {
            element.deleteTemplate(eObj);
        }

        // and reload list
        loadTemplates();
}//GEN-LAST:event_jButtonDeleteTemplateActionPerformed

    /**
     * Set selected template to be a member of parent object
     * TR 18/3/10 If already in use as a template by parent then prompt for
     * revision.
     *
     * @param evt
     */
    private void jButtonUseTemplateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonUseTemplateActionPerformed
        if (jButtonUseTemplate.getText().equals("Use")) {
            useSelectedTemplate();
        } else if (jButtonUseTemplate.getText().equals("New")) {
            addNewTemplate();
        }
    }
    
    private void useSelectedTemplate() {
        EPOCObject eObj = getSelectedTemplate();
        if (eObj == null) {
            JOptionPane.showMessageDialog(this, "An "+objTypeName+" must be selected first!");
            return;
        }
        EPOCObject newObj = eObj;
        String msg = "This template is already assigned as a member of this object.\n"
                   + "Do you want to use a revised copy of the template?";
        // add to universe
        if (eObj instanceof EClass) {

        } else if (eObj instanceof Spatial) {
            // Check if existing spatial object needs templating before it is removed
            if (universe.getSpatial() != null && !universe.getSpatial().isTemplate()) {
                if (JOptionPane.showConfirmDialog(this, "Do you want to template the existing Spatial object\n"
                                                      + "before replacing it?",
                        "Template Existing", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    universe.getSpatial().template();
                }
            }
            universe.removeSpatial();
            universe.setConfigObject(newObj);
        } else if (eObj instanceof Report) {
            // Check if existing report object needs templating before it is removed
            if (universe.getReport() != null && !universe.getReport().isTemplate()) {
                if (JOptionPane.showConfirmDialog(this, "Do you want to template the existing Report object\n"
                                                      + "before replacing it?",
                        "Template Existing", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    universe.getReport().template();
                }
            }
            universe.removeReport();
            universe.setConfigObject(newObj);
        } else if (eObj instanceof Trial) {
            // Check if object is already used by parent.  If so prompt for revision.
            if (universe.getTrials().contains((Trial)eObj)) {
                if (JOptionPane.showConfirmDialog(this, msg,
                        "Revise Template", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
                    return;
                }
                newObj = ((Trial)eObj).revise(universe, universe.getUID());
            }
            universe.setConfigObject(newObj);
        } else if (eObj instanceof Element) {
            // Check if object is already used by parent.  If so prompt for revision.
            if (universe.getElements(objType).contains((Element)eObj)) {
                if (JOptionPane.showConfirmDialog(this, msg,
                        "Revise Template", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
                    return;
                }
                newObj = ((Element)eObj).revise(universe, universe.getUID());
            }
            universe.addElement((Element)newObj);
        } else if (eObj instanceof Action) {
            // Check if object is already used by parent.  If so prompt for revision.
            if (element.getActions().contains((Action)eObj)) {
                if (JOptionPane.showConfirmDialog(this, msg,
                        "Revise Template", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
                    return;
                }
                newObj = ((Action)eObj).revise(universe, element.getUID());
            }
            element.addAction((Action)newObj);
        } else if (eObj instanceof Attribute) {
            // Check if object is already used by parent.  If so prompt for revision.
            if (element.getAttributes().contains((Attribute)eObj)) {
                if (JOptionPane.showConfirmDialog(this, msg,
                        "Revise Template", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
                    return;
                }
                newObj = ((Attribute)eObj).revise(universe, element.getUID());
            }
            element.addAttribute((Attribute)newObj);

        } else {
            return;
        }

        setModified(true);
        newObj.setPosition(-1);
        
        if (objType == OBJ_SPA || objType == OBJ_REP || objType == OBJ_TRI) {
            parentUI.reloadConfigNodes();
        } else {
            parentUI.insertAsLastChildOfSelected(newObj);
        }

        loadTemplates();
}//GEN-LAST:event_jButtonUseTemplateActionPerformed

    private void addNewTemplate() {
        EPOCObject eObj;
        if (objType == OBJ_CLS) {
            eObj = new EClass();
            ((EClass)eObj).setModType(modType);
        } else if (objType == OBJ_SPA) {
            eObj = new Spatial();
        } else if (objType == OBJ_REP) {
            eObj = new Report();
        } else if (objType == OBJ_TRI) {
            eObj = new Trial();
        } else if (objType == OBJ_ELE || objType / 10 == OBJ_ELE) {
            eObj = new Element();
        } else if (objType == OBJ_ACT) {
            eObj = new Action();
        } else if (objType == OBJ_ATT) {
            eObj = new Attribute();
        } else {
            return;
        }

        eObj.setAsTemplate();
        universe.setNextAvailableVersion(eObj, false);
        if ((eObj = showTemplateUI(eObj, true)) != null) {
            universe.getTemplates().addTemplateList(eObj);
            if (EPOC_LIST_ORDERING == DSPL_LST_NM_ASC || EPOC_LIST_ORDERING == DSPL_LST_RV_ASC) {
                ((DefaultListModel)jListTemplates.getModel()).addElement(eObj);
            } else {
                int childCnt = ((DefaultTreeModel)jTreeTemplates.getModel()).getChildCount(((DefaultTreeModel)jTreeTemplates.getModel()).getRoot());
                ((DefaultTreeModel)jTreeTemplates.getModel()).insertNodeInto(new DefaultMutableTreeNode(eObj),
                        (DefaultMutableTreeNode)((DefaultTreeModel)jTreeTemplates.getModel()).getRoot(), childCnt);
            }
        }
    }

    /**
     * Set the appropriate icon and text dependent on the current order method
     * @param displayMethod
     */
    private void setDisplayButton(int displayMethod) {
        ImageIcon buttIcon = new ImageIcon(getClass().getResource("/icons/listview.png"));
        String buttTxt = "a-z";

        if (displayMethod == DSPL_TRE_NM_ASC || displayMethod == DSPL_TRE_RV_ASC) {
            buttIcon = new ImageIcon(getClass().getResource("/icons/treeview.png"));
        }
        if (displayMethod == DSPL_LST_RV_ASC || displayMethod == DSPL_TRE_RV_ASC) {
            buttTxt = "0-9";
        }

        jButtonDisplay.setText(buttTxt);
        jButtonDisplay.setIcon(buttIcon);
        jButtonDisplay.setToolTipText(EPOCObject.getListDisplayName(displayMethod));
    }

    /**
     * Export selected template as an XML file.
     * @param evt
     */
    private void jButtonExportTemplateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExportTemplateActionPerformed
        EPOCObject eObj = getSelectedTemplate();
        if (eObj == null) {
            JOptionPane.showMessageDialog(this, "An "+objTypeName+" must be selected first!");
            return;
        }
        // And export it
        if (!eObj.export2XML(this)) {
            JOptionPane.showMessageDialog(this, Messages.getUnreadErrMsgs(), "Failed exporting object to file!", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jButtonExportTemplateActionPerformed

    private void jButtonDisplayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDisplayActionPerformed
        EPOC_LIST_ORDERING = (EPOC_LIST_ORDERING == DSPL_TRE_RV_ASC ? DSPL_LST_NM_ASC : EPOC_LIST_ORDERING + 1);
        setDisplayButton(EPOC_LIST_ORDERING);
        loadTemplates();
    }//GEN-LAST:event_jButtonDisplayActionPerformed

    private void jListTemplatesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListTemplatesMouseClicked
        if (evt.getClickCount() == 2) {
            EPOCObject eObj = getSelectedTemplate();
            if (eObj == null) return;

            if (EPOC_LIST_ORDERING == DSPL_LST_NM_ASC || EPOC_LIST_ORDERING == DSPL_LST_RV_ASC
                    || ((DefaultMutableTreeNode)jTreeTemplates.getLastSelectedPathComponent()).isLeaf()) {
                showTemplateUI(eObj, isEditable());
                if (isEditable()) ((DefaultTreeModel)jTreeTemplates.getModel()).nodeChanged((DefaultMutableTreeNode)jTreeTemplates.getLastSelectedPathComponent());
            }
        }
}//GEN-LAST:event_jListTemplatesMouseClicked

    private void jButtonReviseTemplateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonReviseTemplateActionPerformed
        EPOCObject eObj = getSelectedTemplate();
        if (eObj == null) {
            JOptionPane.showMessageDialog(this, "An "+objTypeName+" must be selected first!");
            return;
        }

        if (eObj != null && eObj instanceof EClass) {
            EPOCObject newObj = eObj.revise(universe, 0);
            newObj.template();

            if (EPOC_LIST_ORDERING == DSPL_LST_NM_ASC || EPOC_LIST_ORDERING == DSPL_LST_RV_ASC) {
                ((DefaultListModel)jListTemplates.getModel()).addElement(newObj);
            } else {
                int childCnt = ((DefaultTreeModel)jTreeTemplates.getModel()).getChildCount((DefaultMutableTreeNode)jTreeTemplates.getLastSelectedPathComponent());
                ((DefaultTreeModel)jTreeTemplates.getModel()).insertNodeInto(new DefaultMutableTreeNode(newObj),
                    (DefaultMutableTreeNode)jTreeTemplates.getLastSelectedPathComponent(), childCnt);
                jTreeTemplates.expandPath(jTreeTemplates.getSelectionPath());
            }
        }
    }//GEN-LAST:event_jButtonReviseTemplateActionPerformed

    /**
     * Sub class mouse adaptor for tree object right-clicks
     */
    private class PopupListener extends MouseAdapter {
        private JPopupMenu popup = new JPopupMenu();

        private JMenuItem openMenuItem;
        private JMenuItem delMenuItem;
        private JMenuItem revMenuItem;
        private JMenuItem expMenuItem;

        PopupListener(JPopupMenu ppp) {
            popup = ppp;

            openMenuItem = new JMenuItem((isEditable() ? "Edit" : "View"));
            openMenuItem.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jButtonViewTemplateActionPerformed(evt);
                }
            });

            delMenuItem = new JMenuItem("Delete");
            delMenuItem.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jButtonDeleteTemplateActionPerformed(evt);
                }
            });

            revMenuItem = new JMenuItem("Revise");
            revMenuItem.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jButtonReviseTemplateActionPerformed(evt);
                }
            });

            expMenuItem = new JMenuItem("Export");
            expMenuItem.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jButtonExportTemplateActionPerformed(evt);
                }
            });
        }

        @Override
        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                // Select right-clicked node first
                setRightClickedCellSelected(e);
                modifyPopup();
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        }

        /**
         * Modify which menuitems are shown in the pop-up
         */
        private void modifyPopup() {
            EPOCObject eObj = getSelectedTemplate();

            /* if nothing is selected */
            if (eObj == null) return;

            // clear popup
            popup = new JPopupMenu();

            popup.add(openMenuItem);
            popup.add(delMenuItem);
            if (eObj instanceof EClass) popup.add(revMenuItem);
            popup.add(expMenuItem);

        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonDeleteTemplate;
    private javax.swing.JButton jButtonDisplay;
    private javax.swing.JButton jButtonExportTemplate;
    private javax.swing.JButton jButtonReviseTemplate;
    private javax.swing.JButton jButtonUseTemplate;
    private javax.swing.JButton jButtonViewTemplate;
    private javax.swing.JList jListTemplates;
    private javax.swing.JPanel jPanelTemplates;
    private javax.swing.JScrollPane jScrollPaneTemplates;
    // End of variables declaration//GEN-END:variables

}
