/*******************************************************************************
 * ElementCompleteUI.java
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

import java.util.*;
import java.awt.Component;
import java.awt.Font;
import java.awt.Color;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import com.toedter.calendar.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/*******************************************************************************
 * GUI for Element object with tabs for all its child Attributes and Actions.
 * Used for display of templates and template linked children.
 * Allows display and data entry for Element objects and its child objects.
 *
 * @author Troy Robertson
 * @company Australian Antarctic Division
 * @created 10/09/2009
 * @version 0.4.0, 01-09-2010
 ******************************************************************************/
public class ElementCompleteUI extends OpposingPanelUI implements TreeSelectionListener {
    
    Universe universe;
    Element element;
    private JTree jTreeAttributes = new JTree();
    private JTree jTreeActions = new JTree();
    private JTree jTreeUnusedAttributes = new JTree();
    private JTree jTreeUnusedActions = new JTree();
    
    /** Creates new form ElementUI */
    public ElementCompleteUI(Element ele, Universe uni) {

        initComponents();
        
        universe = uni;
        element = ele;
        
        loadForm();
        loadEClasses();
        loadPolygons();

        jTabbedPane1.setSelectedIndex(0);
        jTextShortName.requestFocus();
        setDisplayButton(jButtonDisplayAction, EPOC_LIST_ORDERING);
        setDisplayButton(jButtonDisplayAttribute, EPOC_LIST_ORDERING);

        // Set up jtree view
        createTreeView(jTreeAttributes, "Attributes");
        createTreeView(jTreeUnusedAttributes, "UnusedAttributes");
        createTreeView(jTreeActions, "Actions");
        createTreeView(jTreeUnusedActions, "UnusedActions");

        // Load 'em all up
        loadAttributes();
        loadActions();
        loadUnusedAttributes();
        loadUnusedActions();
        
    }
    
    /*
     * Set editability of form
     */
    @Override
    public void editable(boolean editable) {
        super.editable(editable);
        // Element tab
        jTextShortName.setEditable(editable);
        jTextName.setEditable(editable);
        jTextID.setEditable(editable);
        //jTextClassname.setEditable(editable);
        jComboEClass.setEnabled(editable);
        jButtonBirthday.setEnabled(editable);
        jListSelectedPolygons.setEnabled(editable);
        jTextAreaDesc.setEditable(editable);
        jTextCreator.setEditable(editable);

        // Attribute tab
        jButtonUseAttribute.setEnabled(editable);
        jButtonTemplateAttribute.setVisible(false);
        //jButtonTemplateAttribute.setEnabled(editable);
        jButtonNewAttribute.setEnabled(editable);
        jButtonEditAttribute.setText((editable ? "Edit" : "View"));
        jButtonRemoveAttribute.setEnabled(editable);
        jButtonEditUnusedAttribute.setText((editable ? "Edit" : "View"));
        jButtonDeleteUnusedAttribute.setEnabled(editable);

        // Action tab
        jButtonUseAction.setEnabled(editable);
        jButtonTemplateAction.setVisible(false);
        //jButtonTemplateAction.setEnabled(editable);
        jButtonNewAction.setEnabled(editable);
        jButtonEditAction.setText((editable ? "Edit" : "View"));
        jButtonRemoveAction.setEnabled(editable);
        jButtonEditUnusedAction.setText((editable ? "Edit" : "View"));
        jButtonDeleteUnusedAction.setEnabled(editable);
    }

    private void createTreeView(JTree tree, String name) {
        tree.setName(name);
        tree.setRootVisible(false);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeSelectionListener(this);
        tree.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                mouseDoubleClicked(evt);
            }
        });
    }

    /**
     * Set the list or tree item that was right clicked as the selected item
     * @param me
     */
    private void setRightClickedCellSelected(MouseEvent me) {
        Component comp = me.getComponent();

        if (comp instanceof JList) {
            ((JList)comp).setSelectedIndex(((JList)comp).locationToIndex(me.getPoint()));
        } else {
            // Select right-clicked tree node first
            ((JTree)comp).setSelectionPath(((JTree)comp).getPathForLocation(me.getX(), me.getY()));
        }
    }

    /**
     * Gets the currently selected EPOCObject from the list/tree
     * @return
     */
    private EPOCObject getSelectedTemplate(Component comp) {
        EPOCObject selected = null;
        if (comp instanceof JList) {
            if (((JList)comp).getSelectedIndex() >= 0) selected = (EPOCObject)((JList)comp).getSelectedValue();
        } else {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)((JTree)comp).getLastSelectedPathComponent();
            // retrieve the node that was selected
            if (node != null) selected = (EPOCObject)node.getUserObject();
        }
     
        return selected;
    }

    private void loadForm() {
        jTextShortName.setText(element.getShortName());
        // bold templates
        if (element.isTemplate()) {
            jTextShortName.setFont(jTextShortName.getFont().deriveFont(Font.BOLD));
        } else {
            jTextShortName.setFont(jTextShortName.getFont().deriveFont(Font.PLAIN));
        }
        jTextName.setText(element.getName());
        jTextVersion.setText(element.getRevision());
        jTextCreated.setText(element.getFormattedCreated());
        jTextModified.setText(element.getFormattedModified());
        //jTextClassname.setText(element.getEPOCClassName());
        jTextID.setText(element.getEPOCID());
        jTextBDay.setText(String.valueOf(element.getBirthDay()));
        jTextBMonth.setText(String.valueOf(element.getBirthMonth()));
        jTextAreaDesc.setText(element.getDescription());
        jTextCreator.setText(element.getAuthor());
    }

    private void loadEClasses() {
        EClass selObj = null;
        ArrayList availList = new ArrayList();

        // add dummy none item
        EClass dummy = new EClass();
        dummy.setRevision("");
        dummy.setShortName("NULL");
        jComboEClass.addItem(dummy);
        selObj = dummy;

        // get templated eclasses
        availList.addAll(element.getTemplates().getTemplateList(OBJ_CLS));

        // make sure selected object is in there, even if it is just a broken link
        if (element.getEClass() != null) {
            jComboEClass.setFont(jComboEClass.getFont().deriveFont(Font.BOLD));
            if (element.getEClass().isBroken()) {
                jComboEClass.setBackground(Color.RED);
                jComboEClass.setForeground(Color.WHITE);
            }
            if (!availList.contains(element.getEClass())) jComboEClass.addItem(element.getEClass());
            selObj = element.getEClass();
        }
        Collections.sort(availList, new EPOCObjectListComparator());

        // but only add Actions of type setup (oh and broken links)
        for (Object ecObj : availList) jComboEClass.addItem(ecObj);

        // set selected
        jComboEClass.setSelectedItem(selObj);

        jComboEClass.setRenderer(new EPOCObjectListRenderer());
    }

    private void loadPolygons() {
        Vector psVector = new Vector();
        if (universe.getSpatial() != null) psVector = universe.getSpatial().getPolygonsVector();
        String[] pArr = new String[psVector.size()];
        int i = 0;

        // Extract a list of polygon names
        for (Object pObj : psVector) {
            pArr[i] = (String)((Vector)pObj).get(0);
            i++;
        }
        if (pArr.length == 0 || (pArr.length < element.getPolygons().get(element.getPolygons().size() - 1))) {
            // Universe has no polys or not enough anyway
            // Have to add some extras as dummys
            String[] dummyArr = new String[element.getPolygons().get(element.getPolygons().size() - 1)];
            for (i = 0 ; i < pArr.length ; i ++) dummyArr[i] = pArr[i];
            for ( ; i < dummyArr.length ; i++) {
                dummyArr[i] = "Missing polygon " + (i + 1);
            }
            pArr = dummyArr;
        }
        jListSelectedPolygons.setListData(pArr);
        int[] sIndxs = new int[element.getPolygons().size()];
        i = 0;
        for (int pNum : element.getPolygons()) {
            if (pNum - 1 >= 0 && pNum - 1 < pArr.length)
            sIndxs[i] = pNum - 1;
            i++;
        }
        jListSelectedPolygons.setSelectedIndices(sIndxs);
    }

    /**
     * Set the list as model data for the jlist.
     * Add CellRenderer and MouseListener for popup menus.
     * @param jlist
     * @param list
     * @return
     */
    private JList setList(JList jlist, ArrayList<EPOCObject> list) {
        // build the model
        DefaultListModel dlm = new DefaultListModel();
        for (EPOCObject item : list) dlm.addElement(item);
        jlist.setModel(dlm);
        //create Renderer and display
        jlist.setCellRenderer(new EPOCObjectListRenderer());
        //Add listener to components that can bring up popup menus.
        jlist.addMouseListener(new PopupListener(new JPopupMenu(), jlist));
        
        return jlist;
    }

    /**
     * Set the list as model data for the jtree.
     * Add CellRenderer and MouseListener for popup menus.
     * @param jtree
     * @param list
     * @return
     */
    private JTree setTree(JTree jtree, ArrayList<EPOCObject> list) {
        // build the model
        RevisionTreeModel treeModel = new RevisionTreeModel(EPOC_LIST_ORDERING);
        treeModel.buildModel(list);
        jtree.setModel(treeModel);
        //create Renderer and display
        jtree.setCellRenderer(new UniverseTreeCellRenderer());
        //Add listener to components that can bring up popup menus.
        jtree.addMouseListener(new PopupListener(new JPopupMenu(), jtree));
            
        return jtree;
    }
    
    private void loadAttributes() {
        ArrayList<EPOCObject> list = (ArrayList)element.getAttributes().clone();       
        if (EPOC_LIST_ORDERING == DSPL_LST_NM_ASC || EPOC_LIST_ORDERING == DSPL_LST_RV_ASC) {
            // Define a different sort method on just name/revision
            Collections.sort(list, new EPOCObjectListComparator(EPOC_LIST_ORDERING));
            jScrollPaneAttributes.setViewportView(setList(jListAttributes, list));
        } else {
            jScrollPaneAttributes.setViewportView(setTree(jTreeAttributes, list));
        }
    }
    
    private void loadUnusedAttributes() {
        ArrayList<EPOCObject> list = getUnusedTemplates(OBJ_ATT);
        if (EPOC_LIST_ORDERING == DSPL_LST_NM_ASC || EPOC_LIST_ORDERING == DSPL_LST_RV_ASC) {
            // Define a different sort method on just name/revision
            Collections.sort(list, new EPOCObjectListComparator(EPOC_LIST_ORDERING));
            jScrollPaneUnusedAttributes.setViewportView(setList(jListUnusedAttributes, list));
        } else {
            jScrollPaneUnusedAttributes.setViewportView(setTree(jTreeUnusedAttributes, list));
        }
    }
    
    private void loadActions() {
        ArrayList<EPOCObject> list = (ArrayList)element.getActions().clone();
        // Define a different sort method on just name/revision
        Collections.sort(list, new EPOCObjectListComparator(EPOC_LIST_ORDERING));
        if (EPOC_LIST_ORDERING == DSPL_LST_NM_ASC || EPOC_LIST_ORDERING == DSPL_LST_RV_ASC) {
            jScrollPaneActions.setViewportView(setList(jListActions, list));
        } else {
            jScrollPaneActions.setViewportView(setTree(jTreeActions, list));
        }
    }
    
    private void loadUnusedActions() {
        ArrayList<EPOCObject> list = getUnusedTemplates(OBJ_ACT);
        if (EPOC_LIST_ORDERING == DSPL_LST_NM_ASC || EPOC_LIST_ORDERING == DSPL_LST_RV_ASC) {
            // Define a different sort method on just name/revision
            Collections.sort(list, new EPOCObjectListComparator(EPOC_LIST_ORDERING));
            jScrollPaneUnusedActions.setViewportView(setList(jListUnusedActions, list));
        } else {
            jScrollPaneUnusedActions.setViewportView(setTree(jTreeUnusedActions, list));
        }
    }

    /**
     * Set the appropriate icon and text dependent on the current order method
     * @param displayMethod
     */
    private void setDisplayButton(JButton butt, int displayMethod) {
        ImageIcon buttIcon = new ImageIcon(getClass().getResource("/icons/listview.png"));
        String buttTxt = "a-z";

        if (displayMethod == DSPL_TRE_NM_ASC || displayMethod == DSPL_TRE_RV_ASC) {
            buttIcon = new ImageIcon(getClass().getResource("/icons/treeview.png"));
        }
        if (displayMethod == DSPL_LST_RV_ASC || displayMethod == DSPL_TRE_RV_ASC) {
            buttTxt = "0-9";
        }

        butt.setText(buttTxt);
        butt.setIcon(buttIcon);
        butt.setToolTipText(EPOCObject.getListDisplayName(displayMethod));
    }

    @Override
    public int saveIfModified() {
        // Check if form data has been modified, if so update object
        if (isModified()) {
            // Check if required fields are filled adequately
            if (!EPOCObject.testName(jTextShortName.getText())) {
                JOptionPane.showMessageDialog(this, "Please provide a shortname for element first!\n\n" +
                         "Element must be named and may only contain\n" +
                         "alphanumerics, '.' or '_'\n" +
                         "It may only start with an alphabetic character\n" +
                         "and may not end in '.' or '_'");
                return EPOC_FAIL;
            }

            if (!jTextID.getText().equals("")) {
                try {
                    int i = Integer.parseInt(jTextID.getText());
                    if (i <= 0) throw new NumberFormatException();
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "ID must be a valid integer.");
                    return EPOC_FAIL;
                }
            }

            if (jTextAreaDesc.getText().length() > 512) {
                JOptionPane.showMessageDialog(this, "The description field is limited to only 512 characters!");
                return EPOC_FAIL;
            }

            if (jTextBDay.getText().equals("0") || jTextBMonth.getText().equals("0")) {
                JOptionPane.showMessageDialog(this, "Please specify a birth date for element first!");
                return EPOC_FAIL;
            }

            // update modified
            element.setModifiedNow();
            element.setShortName(jTextShortName.getText());
            element.setName(jTextName.getText());
            element.setEPOCID(jTextID.getText());
            //element.setEPOCClassName(jTextClassname.getText());
            if (jComboEClass.getSelectedIndex() <= 0) {
                element.setEClass(null);
            } else {
                element.setEClass((EClass)jComboEClass.getSelectedItem());
            }
            element.setBirthDate(Integer.parseInt(jTextBDay.getText()),
                                Integer.parseInt(jTextBMonth.getText()));
            // Save selected polygon indexes
            element.getPolygons().clear();
            for (int i : jListSelectedPolygons.getSelectedIndices()) {
                element.addPolygon(i+1);
            }
            element.setDescription(jTextAreaDesc.getText());
            element.setAuthor(jTextCreator.getText());

            setModified(false);
            return EPOC_SUCC;
        }

        return EPOC_NONE;
    }

    /**
     * Have form values been modified when compared to stored object data
     * @return boolean
     */
    @Override
    public boolean isModified() {
        if (super.isModified()) return true;
        if (!element.getName().equals(jTextName.getText())) return true;
        if (!element.getShortName().equals(jTextShortName.getText())) return true;
        if (!element.getEPOCID().equals(jTextID.getText())) return true;
        //if (!element.getEPOCClassName().equals(jTextClassname.getText())) return true;
        if (element.getEClass() == null && jComboEClass.getSelectedIndex() > 0) return true;
        if (element.getEClass() != null && jComboEClass.getSelectedIndex() <= 0) return true;
        if (element.getEClass() != null && !element.getEClass().equals((EClass)jComboEClass.getSelectedItem())) return true;
        if (element.getBirthDay() != Integer.parseInt(jTextBDay.getText())) return true;
        if (element.getBirthMonth() != Integer.parseInt(jTextBMonth.getText())) return true;
        if (!element.getDescription().equals(jTextAreaDesc.getText())) return true;
        if (!element.getAuthor().equals(jTextCreator.getText())) return true;
        // Selected Polygons
        ArrayList pList = element.getPolygons();
        int[] sArr = jListSelectedPolygons.getSelectedIndices();
        if (pList.size() != sArr.length) return true;
        ArrayList sList = new ArrayList();
        for (Object sObj : sArr) {
            if (!pList.contains(Integer.parseInt(sObj.toString()))) return true;
            sList.add(Integer.parseInt(sObj.toString()));
        }
        for (Object pObj : pList) {
            if (!sList.contains(Integer.parseInt(pObj.toString()))) return true;
        }

        return false;
    }

    @Override
    public EPOCObject getObject() {
        return element;
    }

    /*
     * Return an array of template elements of the type passed
     */
    public ArrayList getUnusedTemplates(int objType) {
        ArrayList tempList = new ArrayList();
        ArrayList list;
        
        // determine which list to use
        list = universe.getTemplates().getTemplateList(objType);
        
        // eliminate any which are already used by this element
        for (EPOCObject eobj : universe.getTemplates().getTemplateList(objType)) {
            if (!element.isMember(eobj)) {
                tempList.add(eobj);
            }
        }

        return tempList;
    }

    /**
     * Implements required method for TreeSelectionListener
     * @param e
     */
    public void valueChanged(TreeSelectionEvent tse) {
        // Dummy
    }

    /*
     * Display ActionUI form filled with Action selected in list passed.
     * Set form editable if so chosen
     * @param list
     */
    private Action showActionUI(Action act, boolean editable) {
        // Open new frame with action details form filled
        ActionUI actUI = new ActionUI(act, element, universe);
        actUI.editable(editable);
       
        final JOptionPane pane = new JOptionPane(actUI, JOptionPane.PLAIN_MESSAGE,
                JOptionPane.CLOSED_OPTION);
        pane.setBorder(null);
        final JDialog dialog = new JDialog((JFrame)SwingUtilities.getRoot(this).getParent(), true);
        dialog.setUndecorated(true);
        dialog.setContentPane(pane);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setLocation(getDialogLocation());
        dialog.setPreferredSize(getDialogSize());
        pane.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                if (e.getPropertyName().equals(JOptionPane.VALUE_PROPERTY)
                        && e.getNewValue() != JOptionPane.UNINITIALIZED_VALUE) {
                    if (dialog.isVisible() && ((ActionUI)pane.getMessage()).saveIfModified() != EPOC_FAIL) {
                        dialog.setVisible(false);
                    } else {
                        pane.setValue(JOptionPane.UNINITIALIZED_VALUE);
                    }
                }
            }
        });
        dialog.pack();
        dialog.setVisible(true);

        if (editable && ((ActionUI)pane.getMessage()).wasModified()) return act;
        return null;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanelElement = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jTextShortName = new javax.swing.JTextField();
        jTextName = new javax.swing.JTextField();
        jPanel4 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jTextVersion = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextAreaDesc = new javax.swing.JTextArea();
        jButtonBirthday = new javax.swing.JButton();
        jTextBDay = new javax.swing.JTextField();
        jTextBMonth = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jTextCreated = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jTextModified = new javax.swing.JTextField();
        jTextID = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jTextCreator = new javax.swing.JTextField();
        jScrollPane5 = new javax.swing.JScrollPane();
        jListSelectedPolygons = new javax.swing.JList();
        jComboEClass = new javax.swing.JComboBox();
        jPanelAttributes = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jButtonNewAttribute = new javax.swing.JButton();
        jButtonEditAttribute = new javax.swing.JButton();
        jScrollPaneAttributes = new javax.swing.JScrollPane();
        jListAttributes = new javax.swing.JList();
        jButtonRemoveAttribute = new javax.swing.JButton();
        jButtonTemplateAttribute = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        jScrollPaneUnusedAttributes = new javax.swing.JScrollPane();
        jListUnusedAttributes = new javax.swing.JList();
        jButtonEditUnusedAttribute = new javax.swing.JButton();
        jButtonDeleteUnusedAttribute = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jButtonUseAttribute = new javax.swing.JButton();
        jButtonDisplayAttribute = new javax.swing.JButton();
        jPanelActions = new javax.swing.JPanel();
        jPanel11 = new javax.swing.JPanel();
        jButtonNewAction = new javax.swing.JButton();
        jButtonEditAction = new javax.swing.JButton();
        jScrollPaneActions = new javax.swing.JScrollPane();
        jListActions = new javax.swing.JList();
        jButtonRemoveAction = new javax.swing.JButton();
        jButtonTemplateAction = new javax.swing.JButton();
        jPanel12 = new javax.swing.JPanel();
        jScrollPaneUnusedActions = new javax.swing.JScrollPane();
        jListUnusedActions = new javax.swing.JList();
        jButtonEditUnusedAction = new javax.swing.JButton();
        jButtonDeleteUnusedAction = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jButtonUseAction = new javax.swing.JButton();
        jButtonDisplayAction = new javax.swing.JButton();

        setEnabled(false);
        setPreferredSize(new java.awt.Dimension(600, 450));

        jTabbedPane1.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
        jTabbedPane1.setPreferredSize(new java.awt.Dimension(600, 450));

        jPanelElement.setEnabled(false);

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel1.setText("Element:");
        jLabel1.setRequestFocusEnabled(false);

        jTextShortName.setColumns(20);
        jTextShortName.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        jTextShortName.setToolTipText("Short Name");

        jTextName.setColumns(100);
        jTextName.setToolTipText("Full Name");

        jPanel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel4.setPreferredSize(new java.awt.Dimension(580, 402));

        jLabel2.setText("Revision:");
        jLabel2.setRequestFocusEnabled(false);

        jTextVersion.setEditable(false);
        jTextVersion.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jTextVersion.setPreferredSize(new java.awt.Dimension(200, 18));

        jLabel3.setText("Birthday:");
        jLabel3.setRequestFocusEnabled(false);

        jLabel4.setText("/");
        jLabel4.setRequestFocusEnabled(false);

        jLabel5.setText("(dd/mm)");
        jLabel5.setRequestFocusEnabled(false);

        jLabel6.setText("Polygons:");
        jLabel6.setRequestFocusEnabled(false);

        jLabel7.setText("Description:");
        jLabel7.setRequestFocusEnabled(false);

        jTextAreaDesc.setColumns(20);
        jTextAreaDesc.setFont(new java.awt.Font("Tahoma", 0, 11));
        jTextAreaDesc.setRows(3);
        jScrollPane1.setViewportView(jTextAreaDesc);

        jButtonBirthday.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/calendar.png"))); // NOI18N
        jButtonBirthday.setToolTipText("Choose date");
        jButtonBirthday.setMaximumSize(new java.awt.Dimension(25, 25));
        jButtonBirthday.setMinimumSize(new java.awt.Dimension(25, 25));
        jButtonBirthday.setPreferredSize(new java.awt.Dimension(25, 25));
        jButtonBirthday.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonBirthdayActionPerformed(evt);
            }
        });

        jTextBDay.setEditable(false);
        jTextBDay.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jTextBMonth.setEditable(false);
        jTextBMonth.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel8.setText("Created:");

        jTextCreated.setBackground(new java.awt.Color(212, 208, 200));
        jTextCreated.setEditable(false);
        jTextCreated.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jTextCreated.setPreferredSize(new java.awt.Dimension(200, 18));

        jLabel9.setText("Modified:");

        jTextModified.setBackground(new java.awt.Color(212, 208, 200));
        jTextModified.setEditable(false);
        jTextModified.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jTextModified.setPreferredSize(new java.awt.Dimension(200, 18));

        jTextID.setPreferredSize(new java.awt.Dimension(200, 18));

        jLabel10.setText("EPOC ID:");

        jLabel11.setText("EPOC Class:");

        jLabel12.setText("Author(s):");

        jScrollPane5.setViewportView(jListSelectedPolygons);

        jComboEClass.setMinimumSize(new java.awt.Dimension(25, 18));
        jComboEClass.setPreferredSize(new java.awt.Dimension(29, 20));

        org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel4Layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel2)
                            .add(jLabel7)
                            .add(jLabel3)
                            .add(jLabel6)
                            .add(jLabel12))
                        .add(14, 14, 14)
                        .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 480, Short.MAX_VALUE)
                            .add(jScrollPane5, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 480, Short.MAX_VALUE)
                            .add(jTextCreator, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 480, Short.MAX_VALUE)))
                    .add(jPanel4Layout.createSequentialGroup()
                        .add(81, 81, 81)
                        .add(jTextBDay, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 29, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel4)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jTextBMonth, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 30, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel5)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jButtonBirthday, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jPanel4Layout.createSequentialGroup()
                        .add(10, 10, 10)
                        .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel4Layout.createSequentialGroup()
                                .add(jLabel8)
                                .add(28, 28, 28))
                            .add(jPanel4Layout.createSequentialGroup()
                                .add(jLabel10)
                                .add(26, 26, 26)))
                        .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jTextID, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 150, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jTextCreated, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 150, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jTextVersion, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 150, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 117, Short.MAX_VALUE)
                        .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel11)
                            .add(jLabel9))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(jTextModified, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 150, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jComboEClass, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 150, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jPanel4Layout.createSequentialGroup()
                        .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel2)
                            .add(jTextVersion, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel8)
                            .add(jTextCreated, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel10)
                            .add(jTextID, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(jPanel4Layout.createSequentialGroup()
                        .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jTextModified, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel9))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel11)
                            .add(jComboEClass, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel7)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 52, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jTextCreator, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel12))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(jLabel3)
                        .add(jTextBDay, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(jLabel4)
                        .add(jTextBMonth, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(jLabel5))
                    .add(jButtonBirthday, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel6)
                    .add(jScrollPane5, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 164, Short.MAX_VALUE))
                .addContainerGap())
        );

        org.jdesktop.layout.GroupLayout jPanelElementLayout = new org.jdesktop.layout.GroupLayout(jPanelElement);
        jPanelElement.setLayout(jPanelElementLayout);
        jPanelElementLayout.setHorizontalGroup(
            jPanelElementLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanelElementLayout.createSequentialGroup()
                .add(jPanelElementLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jPanelElementLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(jPanel4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 575, Short.MAX_VALUE))
                    .add(jPanelElementLayout.createSequentialGroup()
                        .add(22, 22, 22)
                        .add(jLabel1)
                        .add(22, 22, 22)
                        .add(jTextShortName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 150, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jTextName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 336, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanelElementLayout.setVerticalGroup(
            jPanelElementLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelElementLayout.createSequentialGroup()
                .addContainerGap()
                .add(jPanelElementLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jTextName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jTextShortName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 377, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Element", jPanelElement);

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Attributes:"));
        jPanel5.setPreferredSize(new java.awt.Dimension(575, 185));

        jButtonNewAttribute.setForeground(new java.awt.Color(0, 0, 255));
        jButtonNewAttribute.setText("New");
        jButtonNewAttribute.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButtonNewAttribute.setMaximumSize(new java.awt.Dimension(55, 20));
        jButtonNewAttribute.setMinimumSize(new java.awt.Dimension(55, 20));
        jButtonNewAttribute.setPreferredSize(new java.awt.Dimension(55, 20));
        jButtonNewAttribute.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonNewAttributeActionPerformed(evt);
            }
        });

        jButtonEditAttribute.setForeground(new java.awt.Color(0, 0, 255));
        jButtonEditAttribute.setText("Edit");
        jButtonEditAttribute.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButtonEditAttribute.setMaximumSize(new java.awt.Dimension(55, 20));
        jButtonEditAttribute.setMinimumSize(new java.awt.Dimension(55, 20));
        jButtonEditAttribute.setPreferredSize(new java.awt.Dimension(55, 20));
        jButtonEditAttribute.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonEditAttributeActionPerformed(evt);
            }
        });

        jScrollPaneAttributes.setPreferredSize(new java.awt.Dimension(445, 135));

        jListAttributes.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jListAttributes.setName("Attributes"); // NOI18N
        jListAttributes.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jListAttributesMouseClicked(evt);
            }
        });
        jScrollPaneAttributes.setViewportView(jListAttributes);

        jButtonRemoveAttribute.setForeground(new java.awt.Color(0, 0, 255));
        jButtonRemoveAttribute.setText("Remove");
        jButtonRemoveAttribute.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButtonRemoveAttribute.setMaximumSize(new java.awt.Dimension(55, 20));
        jButtonRemoveAttribute.setMinimumSize(new java.awt.Dimension(55, 20));
        jButtonRemoveAttribute.setPreferredSize(new java.awt.Dimension(55, 20));
        jButtonRemoveAttribute.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRemoveAttributeActionPerformed(evt);
            }
        });

        jButtonTemplateAttribute.setForeground(new java.awt.Color(0, 0, 204));
        jButtonTemplateAttribute.setText("Template");
        jButtonTemplateAttribute.setToolTipText("Create Template");
        jButtonTemplateAttribute.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButtonTemplateAttribute.setMaximumSize(new java.awt.Dimension(55, 20));
        jButtonTemplateAttribute.setMinimumSize(new java.awt.Dimension(55, 20));
        jButtonTemplateAttribute.setPreferredSize(new java.awt.Dimension(55, 20));
        jButtonTemplateAttribute.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonTemplateAttributeActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel5Layout = new org.jdesktop.layout.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel5Layout.createSequentialGroup()
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel5Layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(jButtonEditAttribute, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                .add(jButtonRemoveAttribute, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, jButtonTemplateAttribute, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel5Layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jButtonNewAttribute, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jScrollPaneAttributes, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 478, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel5Layout.createSequentialGroup()
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel5Layout.createSequentialGroup()
                        .add(jButtonNewAttribute, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jButtonEditAttribute, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jButtonRemoveAttribute, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jButtonTemplateAttribute, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jScrollPaneAttributes, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 146, Short.MAX_VALUE))
                .addContainerGap())
        );

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Unused Attributes:"));

        jScrollPaneUnusedAttributes.setPreferredSize(new java.awt.Dimension(445, 135));

        jListUnusedAttributes.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jListUnusedAttributes.setName("UnusedAttributes"); // NOI18N
        jListUnusedAttributes.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jListUnusedAttributesMouseClicked(evt);
            }
        });
        jScrollPaneUnusedAttributes.setViewportView(jListUnusedAttributes);

        jButtonEditUnusedAttribute.setForeground(new java.awt.Color(0, 0, 255));
        jButtonEditUnusedAttribute.setText("View");
        jButtonEditUnusedAttribute.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButtonEditUnusedAttribute.setMaximumSize(new java.awt.Dimension(55, 20));
        jButtonEditUnusedAttribute.setMinimumSize(new java.awt.Dimension(55, 20));
        jButtonEditUnusedAttribute.setPreferredSize(new java.awt.Dimension(55, 20));
        jButtonEditUnusedAttribute.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonEditUnusedAttributeActionPerformed(evt);
            }
        });

        jButtonDeleteUnusedAttribute.setForeground(new java.awt.Color(0, 0, 255));
        jButtonDeleteUnusedAttribute.setText("Delete");
        jButtonDeleteUnusedAttribute.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButtonDeleteUnusedAttribute.setMaximumSize(new java.awt.Dimension(55, 20));
        jButtonDeleteUnusedAttribute.setMinimumSize(new java.awt.Dimension(55, 20));
        jButtonDeleteUnusedAttribute.setPreferredSize(new java.awt.Dimension(55, 20));
        jButtonDeleteUnusedAttribute.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDeleteUnusedAttributeActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel6Layout = new org.jdesktop.layout.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jButtonEditUnusedAttribute, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jButtonDeleteUnusedAttribute, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jScrollPaneUnusedAttributes, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 478, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel6Layout.createSequentialGroup()
                .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel6Layout.createSequentialGroup()
                        .add(jButtonEditUnusedAttribute, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jButtonDeleteUnusedAttribute, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jScrollPaneUnusedAttributes, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 151, Short.MAX_VALUE))
                .addContainerGap())
        );

        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 0));

        jButtonUseAttribute.setForeground(new java.awt.Color(0, 0, 204));
        jButtonUseAttribute.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/up.png"))); // NOI18N
        jButtonUseAttribute.setText("Use");
        jButtonUseAttribute.setToolTipText("Use Template");
        jButtonUseAttribute.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButtonUseAttribute.setMaximumSize(new java.awt.Dimension(55, 20));
        jButtonUseAttribute.setMinimumSize(new java.awt.Dimension(55, 20));
        jButtonUseAttribute.setPreferredSize(new java.awt.Dimension(55, 20));
        jButtonUseAttribute.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonUseAttributeActionPerformed(evt);
            }
        });
        jPanel1.add(jButtonUseAttribute);

        jButtonDisplayAttribute.setForeground(new java.awt.Color(0, 0, 255));
        jButtonDisplayAttribute.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/listview.png"))); // NOI18N
        jButtonDisplayAttribute.setText("a-z");
        jButtonDisplayAttribute.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButtonDisplayAttribute.setMaximumSize(new java.awt.Dimension(55, 20));
        jButtonDisplayAttribute.setMinimumSize(new java.awt.Dimension(55, 20));
        jButtonDisplayAttribute.setPreferredSize(new java.awt.Dimension(55, 20));
        jButtonDisplayAttribute.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDisplayAttributeActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanelAttributesLayout = new org.jdesktop.layout.GroupLayout(jPanelAttributes);
        jPanelAttributes.setLayout(jPanelAttributesLayout);
        jPanelAttributesLayout.setHorizontalGroup(
            jPanelAttributesLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelAttributesLayout.createSequentialGroup()
                .addContainerGap()
                .add(jPanelAttributesLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel5, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jPanel6, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jPanelAttributesLayout.createSequentialGroup()
                        .add(17, 17, 17)
                        .add(jButtonDisplayAttribute, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 493, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanelAttributesLayout.setVerticalGroup(
            jPanelAttributesLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelAttributesLayout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel5, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 183, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanelAttributesLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jButtonDisplayAttribute, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel6, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Attributes", jPanelAttributes);

        jPanel11.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Actions:"));
        jPanel11.setPreferredSize(new java.awt.Dimension(575, 185));

        jButtonNewAction.setForeground(new java.awt.Color(0, 0, 255));
        jButtonNewAction.setText("New");
        jButtonNewAction.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButtonNewAction.setMaximumSize(new java.awt.Dimension(55, 20));
        jButtonNewAction.setMinimumSize(new java.awt.Dimension(55, 20));
        jButtonNewAction.setPreferredSize(new java.awt.Dimension(55, 20));
        jButtonNewAction.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonNewActionActionPerformed(evt);
            }
        });

        jButtonEditAction.setForeground(new java.awt.Color(0, 0, 255));
        jButtonEditAction.setText("Edit");
        jButtonEditAction.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButtonEditAction.setMaximumSize(new java.awt.Dimension(55, 20));
        jButtonEditAction.setMinimumSize(new java.awt.Dimension(55, 20));
        jButtonEditAction.setPreferredSize(new java.awt.Dimension(55, 20));
        jButtonEditAction.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonEditActionActionPerformed(evt);
            }
        });

        jScrollPaneActions.setPreferredSize(new java.awt.Dimension(445, 135));

        jListActions.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jListActions.setName("Actions"); // NOI18N
        jListActions.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jListActionsMouseClicked(evt);
            }
        });
        jScrollPaneActions.setViewportView(jListActions);

        jButtonRemoveAction.setForeground(new java.awt.Color(0, 0, 255));
        jButtonRemoveAction.setText("Remove");
        jButtonRemoveAction.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButtonRemoveAction.setMaximumSize(new java.awt.Dimension(55, 20));
        jButtonRemoveAction.setMinimumSize(new java.awt.Dimension(55, 20));
        jButtonRemoveAction.setPreferredSize(new java.awt.Dimension(55, 20));
        jButtonRemoveAction.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRemoveActionActionPerformed(evt);
            }
        });

        jButtonTemplateAction.setForeground(new java.awt.Color(0, 0, 204));
        jButtonTemplateAction.setText("Template");
        jButtonTemplateAction.setToolTipText("Create Template");
        jButtonTemplateAction.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButtonTemplateAction.setMaximumSize(new java.awt.Dimension(55, 20));
        jButtonTemplateAction.setMinimumSize(new java.awt.Dimension(55, 20));
        jButtonTemplateAction.setPreferredSize(new java.awt.Dimension(55, 20));
        jButtonTemplateAction.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonTemplateActionActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel11Layout = new org.jdesktop.layout.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel11Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel11Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                        .add(jPanel11Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jButtonNewAction, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jButtonEditAction, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(jButtonRemoveAction, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jButtonTemplateAction, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jScrollPaneActions, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 478, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel11Layout.createSequentialGroup()
                .add(jPanel11Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPaneActions, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 146, Short.MAX_VALUE)
                    .add(jPanel11Layout.createSequentialGroup()
                        .add(jButtonNewAction, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jButtonEditAction, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jButtonRemoveAction, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jButtonTemplateAction, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        jPanel12.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Unused Actions:"));
        jPanel12.setPreferredSize(new java.awt.Dimension(575, 185));

        jScrollPaneUnusedActions.setPreferredSize(new java.awt.Dimension(445, 135));

        jListUnusedActions.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jListUnusedActions.setName("UnusedActions"); // NOI18N
        jListUnusedActions.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jListUnusedActionsMouseClicked(evt);
            }
        });
        jScrollPaneUnusedActions.setViewportView(jListUnusedActions);

        jButtonEditUnusedAction.setForeground(new java.awt.Color(0, 0, 255));
        jButtonEditUnusedAction.setText("View");
        jButtonEditUnusedAction.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButtonEditUnusedAction.setMaximumSize(new java.awt.Dimension(55, 20));
        jButtonEditUnusedAction.setMinimumSize(new java.awt.Dimension(55, 20));
        jButtonEditUnusedAction.setPreferredSize(new java.awt.Dimension(55, 20));
        jButtonEditUnusedAction.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonEditUnusedActionActionPerformed(evt);
            }
        });

        jButtonDeleteUnusedAction.setForeground(new java.awt.Color(0, 0, 255));
        jButtonDeleteUnusedAction.setText("Delete");
        jButtonDeleteUnusedAction.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButtonDeleteUnusedAction.setMaximumSize(new java.awt.Dimension(55, 20));
        jButtonDeleteUnusedAction.setMinimumSize(new java.awt.Dimension(55, 20));
        jButtonDeleteUnusedAction.setPreferredSize(new java.awt.Dimension(55, 20));
        jButtonDeleteUnusedAction.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDeleteUnusedActionActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel12Layout = new org.jdesktop.layout.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel12Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jButtonEditUnusedAction, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jButtonDeleteUnusedAction, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jScrollPaneUnusedActions, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 478, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel12Layout.createSequentialGroup()
                .add(jPanel12Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPaneUnusedActions, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 151, Short.MAX_VALUE)
                    .add(jPanel12Layout.createSequentialGroup()
                        .add(jButtonEditUnusedAction, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jButtonDeleteUnusedAction, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        jPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 0));

        jButtonUseAction.setForeground(new java.awt.Color(0, 0, 204));
        jButtonUseAction.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/up.png"))); // NOI18N
        jButtonUseAction.setText("Use");
        jButtonUseAction.setToolTipText("Use Template");
        jButtonUseAction.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButtonUseAction.setMaximumSize(new java.awt.Dimension(55, 20));
        jButtonUseAction.setMinimumSize(new java.awt.Dimension(55, 20));
        jButtonUseAction.setPreferredSize(new java.awt.Dimension(55, 20));
        jButtonUseAction.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonUseActionActionPerformed(evt);
            }
        });
        jPanel2.add(jButtonUseAction);

        jButtonDisplayAction.setForeground(new java.awt.Color(0, 0, 255));
        jButtonDisplayAction.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/listview.png"))); // NOI18N
        jButtonDisplayAction.setText("a-z");
        jButtonDisplayAction.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButtonDisplayAction.setMaximumSize(new java.awt.Dimension(55, 20));
        jButtonDisplayAction.setMinimumSize(new java.awt.Dimension(55, 20));
        jButtonDisplayAction.setPreferredSize(new java.awt.Dimension(55, 20));
        jButtonDisplayAction.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDisplayActionActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanelActionsLayout = new org.jdesktop.layout.GroupLayout(jPanelActions);
        jPanelActions.setLayout(jPanelActionsLayout);
        jPanelActionsLayout.setHorizontalGroup(
            jPanelActionsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanelActionsLayout.createSequentialGroup()
                .addContainerGap()
                .add(jPanelActionsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel12, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel11, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanelActionsLayout.createSequentialGroup()
                        .add(17, 17, 17)
                        .add(jButtonDisplayAction, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 493, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanelActionsLayout.setVerticalGroup(
            jPanelActionsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelActionsLayout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel11, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 183, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanelActionsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jButtonDisplayAction, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel12, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 188, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Actions", jPanelActions);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonDeleteUnusedActionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDeleteUnusedActionActionPerformed
        Action act = (Action)getSelectedTemplate(jScrollPaneUnusedActions.getViewport().getView());
        if (act == null) {
            JOptionPane.showMessageDialog(this, "An Action must be selected first!");
            return;
        }

        // check that template is not already being used
        if (universe.isMemberTemplate(act)) {
            JOptionPane.showMessageDialog(this, "Cannot delete, Action template is currently in use!");
            return;
        }

        // if its already a saved object then put it in
        // template delete list to be deleted on universe save
        element.deleteTemplate(act);

        // and reload list
        loadUnusedActions();
    }//GEN-LAST:event_jButtonDeleteUnusedActionActionPerformed

    private void jButtonEditUnusedActionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonEditUnusedActionActionPerformed
        Action act = (Action)getSelectedTemplate(jScrollPaneUnusedActions.getViewport().getView());
        if (act == null) {
            JOptionPane.showMessageDialog(this, "An Action must be selected first!");
            return;
        }
        
        showActionUI(act, isEditable());

        // and reload list
        loadUnusedActions();
    }//GEN-LAST:event_jButtonEditUnusedActionActionPerformed

    /**
     * Get action and put it in delete list to be deleted on element save
     * @param evt
     */
    private void jButtonRemoveActionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRemoveActionActionPerformed
        Action act = (Action)getSelectedTemplate(jScrollPaneActions.getViewport().getView());
        if (act == null) {
            JOptionPane.showMessageDialog(this, "An Action must be selected first!");
            return;
        }
        
        // Does it need deleting from storage
        if (act.getUID() > 0) element.addDeleteList(act);
        // remove from arrayList
        element.removeAction(act);
        
        // and reload lists
        loadActions(); 
        loadUnusedActions();
    }//GEN-LAST:event_jButtonRemoveActionActionPerformed

    private void jButtonEditActionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonEditActionActionPerformed
        Action act = (Action)getSelectedTemplate(jScrollPaneActions.getViewport().getView());
        if (act == null) {
            JOptionPane.showMessageDialog(this, "An Action must be selected first!");
            return;
        }

        showActionUI(act, isEditable());

        // and reload list
        loadActions();
    }//GEN-LAST:event_jButtonEditActionActionPerformed

    private void jButtonNewActionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonNewActionActionPerformed
        if (saveIfModified() == EPOC_NONE) return;

        // Create ActionUI
        Action tempAction = new Action();
        tempAction.setAsTemplate();
        universe.setNextAvailableVersion(tempAction, false);
        ActionUI actUI = new ActionUI(tempAction, element, universe);
       
        final JOptionPane pane = new JOptionPane(actUI, JOptionPane.PLAIN_MESSAGE, 
                                                        JOptionPane.OK_CANCEL_OPTION);
        final JDialog dialog = new JDialog((JFrame)SwingUtilities.getRoot(this).getParent(), true);
        dialog.setUndecorated(true);
        dialog.setContentPane(pane);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setLocation(getDialogLocation());
        dialog.setPreferredSize(getDialogSize());
        pane.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                if (e.getPropertyName().equals(JOptionPane.VALUE_PROPERTY)
                        && e.getNewValue() != JOptionPane.UNINITIALIZED_VALUE) {
                    if (dialog.isVisible() && ((ActionUI)pane.getMessage()).saveIfModified() != EPOC_FAIL) {
                        dialog.setVisible(false);
                    } else {
                        pane.setValue(JOptionPane.UNINITIALIZED_VALUE);
                    }
                }
            }
        });
        dialog.pack();
        dialog.setVisible(true);
       
        if ((Integer)pane.getValue() == JOptionPane.OK_OPTION && ((ActionUI)pane.getMessage()).wasModified()) {
            tempAction.setTemplates(element.getTemplates());
            element.getTemplates().addTemplateList(tempAction);
            element.addAction(tempAction);
            loadActions();
        }
    }//GEN-LAST:event_jButtonNewActionActionPerformed

    private void jButtonTemplateActionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonTemplateActionActionPerformed
        Action act = (Action)getSelectedTemplate(jScrollPaneActions.getViewport().getView());
        if (act == null) {
            JOptionPane.showMessageDialog(this, "An Action must be selected first!");
            return;
        }
        
        // if its not a template
        if (!act.isTemplate()) {
            // Copy the action from the element and make it a template
            act.setAsTemplate();
            
            // add it to the action template list
            universe.getTemplates().addTemplateList(act);

            // refresh list
            loadActions();
            loadUnusedActions();
        }
    }//GEN-LAST:event_jButtonTemplateActionActionPerformed

    private void jButtonUseActionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonUseActionActionPerformed
        Action act = (Action)getSelectedTemplate(jScrollPaneUnusedActions.getViewport().getView());
        if (act == null) {
            JOptionPane.showMessageDialog(this, "An Action must be selected first!");
            return;
        }
        
        // add to element
        element.addAction(act);
        
        // refresh lists
        loadActions();
        loadUnusedActions();
    }//GEN-LAST:event_jButtonUseActionActionPerformed

     /*
     * Display AttributeUI form filled with Attribute selected in list passed.
     * Set form editable if so chosen
     * @param list
     */
    private Attribute showAttributeUI(Attribute att, boolean editable) {
        // Open new frame with action details form filled
        AttributeUI attUI = new AttributeUI(att, element);
        attUI.editable(editable);

        final JOptionPane pane = new JOptionPane(attUI, JOptionPane.PLAIN_MESSAGE,
                JOptionPane.CLOSED_OPTION);
        pane.setBorder(null);
        final JDialog dialog = new JDialog((JFrame)SwingUtilities.getRoot(this).getParent(), true);
        dialog.setUndecorated(true);
        dialog.setContentPane(pane);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setLocation(getDialogLocation());
        dialog.setPreferredSize(getDialogSize());
        pane.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                if (e.getPropertyName().equals(JOptionPane.VALUE_PROPERTY)
                        && e.getNewValue() != JOptionPane.UNINITIALIZED_VALUE) {
                    if (dialog.isVisible() && ((AttributeUI)pane.getMessage()).saveIfModified() != EPOC_FAIL) {
                        dialog.setVisible(false);
                    } else {
                        pane.setValue(JOptionPane.UNINITIALIZED_VALUE);
                    }
                }
            }
        });
        dialog.pack();
        dialog.setVisible(true);

        if (editable && ((AttributeUI)pane.getMessage()).wasModified()) return att;
        return null;
    }

    private void jButtonDeleteUnusedAttributeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDeleteUnusedAttributeActionPerformed
        Attribute att = (Attribute)getSelectedTemplate(jScrollPaneUnusedAttributes.getViewport().getView());
        if (att == null) {
            JOptionPane.showMessageDialog(this, "An Attribute must be selected first!");
            return;
        }

        // check that template is not already being used
        if (universe.isMemberTemplate(att)) {
            JOptionPane.showMessageDialog(this, "Cannot delete, Attribute template is currently in use!");
            return;
        }
        
        // if its already a saved object then put it in
        // template delete list to be deleted on universe save
        element.deleteTemplate(att);
        
        // and reload list
        loadUnusedAttributes();
    }//GEN-LAST:event_jButtonDeleteUnusedAttributeActionPerformed

    private void jButtonEditUnusedAttributeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonEditUnusedAttributeActionPerformed
        Attribute att = (Attribute)getSelectedTemplate(jScrollPaneUnusedAttributes.getViewport().getView());
        if (att == null) {
            JOptionPane.showMessageDialog(this, "An Attribute must be selected first!");
            return;
        }
        
        showAttributeUI(att, isEditable());

        // and reload list
        loadUnusedAttributes();
    }//GEN-LAST:event_jButtonEditUnusedAttributeActionPerformed

    /**
     * Get selected attribute and put it in delete list to be deleted on element save
     * @param evt
     */
    private void jButtonRemoveAttributeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRemoveAttributeActionPerformed
        Attribute att = (Attribute)getSelectedTemplate(jScrollPaneAttributes.getViewport().getView());
        if (att == null) {
            JOptionPane.showMessageDialog(this, "An Attribute must be selected first!");
            return;
        }

        // Does it need deleting from storage
        if (att.getUID() > 0) element.addDeleteList(att);
        // remove from arrayList
        element.removeAttribute(att);
        
        // and reload lists
        loadAttributes(); 
        loadUnusedAttributes();
    }//GEN-LAST:event_jButtonRemoveAttributeActionPerformed

    private void jButtonEditAttributeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonEditAttributeActionPerformed
        Attribute att = (Attribute)getSelectedTemplate(jScrollPaneAttributes.getViewport().getView());
        if (att == null) {
            JOptionPane.showMessageDialog(this, "An Attribute must be selected first!");
            return;
        }
        
        showAttributeUI(att, isEditable());

        // and reload list
        loadAttributes();
    }//GEN-LAST:event_jButtonEditAttributeActionPerformed

    private void jButtonNewAttributeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonNewAttributeActionPerformed
        if (saveIfModified() == EPOC_NONE) return;

        // Create AttributeUI
        Attribute tempAttr = new Attribute();
        tempAttr.setAsTemplate();
        universe.setNextAvailableVersion(tempAttr, false);
        AttributeUI attUI = new AttributeUI(tempAttr, element);
       
        final JOptionPane pane = new JOptionPane(attUI, JOptionPane.PLAIN_MESSAGE, 
                                                        JOptionPane.OK_CANCEL_OPTION);
        final JDialog dialog = new JDialog((JFrame)SwingUtilities.getRoot(this).getParent(), true);
        dialog.setUndecorated(true);
        dialog.setContentPane(pane);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setLocation(getDialogLocation());
        dialog.setPreferredSize(getDialogSize());
        pane.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                if (e.getPropertyName().equals(JOptionPane.VALUE_PROPERTY)
                        && e.getNewValue() != JOptionPane.UNINITIALIZED_VALUE) {
                    if (dialog.isVisible() && ((AttributeUI)pane.getMessage()).saveIfModified() != EPOC_FAIL) {
                        dialog.setVisible(false);
                    } else {
                        pane.setValue(JOptionPane.UNINITIALIZED_VALUE);
                    }
                }
            }
        });
        dialog.pack();
        dialog.setVisible(true);

        if ((Integer)pane.getValue() == JOptionPane.OK_OPTION && ((AttributeUI)pane.getMessage()).wasModified()) {
            //tempAttr.setParentUID(element.getUID());
            tempAttr.setTemplates(element.getTemplates());
            element.getTemplates().addTemplateList(tempAttr);
            element.addAttribute(tempAttr);
            loadAttributes();
        }
    }//GEN-LAST:event_jButtonNewAttributeActionPerformed

    private void jButtonTemplateAttributeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonTemplateAttributeActionPerformed
        Attribute att = (Attribute)getSelectedTemplate(jScrollPaneAttributes.getViewport().getView());
        if (att == null) {
            JOptionPane.showMessageDialog(this, "An Attribute must be selected first!");
            return;
        }
        
         // if its not a template
        if (!att.isTemplate()) {
            // Copy the attribute from the element and make it a template
            att.setAsTemplate();
            
            // add it to the attribute template list
            universe.getTemplates().addTemplateList(att);
       
            // refresh list
            loadAttributes();
            loadUnusedAttributes();
        }
    }//GEN-LAST:event_jButtonTemplateAttributeActionPerformed

    private void jButtonUseAttributeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonUseAttributeActionPerformed
        Attribute att = (Attribute)getSelectedTemplate(jScrollPaneUnusedAttributes.getViewport().getView());
        if (att == null) {
            JOptionPane.showMessageDialog(this, "An Attribute must be selected first!");
            return;
        }
        
        // add to element
        element.addAttribute(att);
        
        // refresh lists
        loadAttributes();
        loadUnusedAttributes();
    }//GEN-LAST:event_jButtonUseAttributeActionPerformed

    private void jButtonBirthdayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonBirthdayActionPerformed
        // Construct a date picker panel
        JMonthChooser jmc = new JMonthChooser();
        JDayChooser jdc = new JDayChooser();
        JPanel picker = new JPanel();
        picker.setLayout(new BoxLayout(picker, BoxLayout.PAGE_AXIS));
        picker.add(jmc);
        picker.add(jdc);

        // Set current birth day and month
        if (!jTextBDay.getText().equals("0")) {
            jdc.setDay(Integer.parseInt(jTextBDay.getText()));
            jmc.setMonth(Integer.parseInt(jTextBMonth.getText())-1);
        }

        final JOptionPane pane = new JOptionPane(picker, JOptionPane.PLAIN_MESSAGE,
                JOptionPane.DEFAULT_OPTION);
        JDialog dialog = pane.createDialog(this, "Select birthday");
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setLocationRelativeTo(this);
        dialog.pack();
        dialog.setVisible(true);

        jTextBDay.setText(String.valueOf(jdc.getDay()));
        jTextBMonth.setText(String.valueOf(jmc.getMonth()+1));
}//GEN-LAST:event_jButtonBirthdayActionPerformed

    private void reorder() {
        EPOC_LIST_ORDERING = (EPOC_LIST_ORDERING == DSPL_TRE_RV_ASC ? DSPL_LST_NM_ASC : EPOC_LIST_ORDERING + 1);
        setDisplayButton(jButtonDisplayAction, EPOC_LIST_ORDERING);
        setDisplayButton(jButtonDisplayAttribute, EPOC_LIST_ORDERING);
        loadActions();
        loadUnusedActions();
        loadAttributes();
        loadUnusedAttributes();
    }

    private void jButtonDisplayActionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDisplayActionActionPerformed
        reorder();
    }//GEN-LAST:event_jButtonDisplayActionActionPerformed

    private void jButtonDisplayAttributeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDisplayAttributeActionPerformed
        reorder();
    }//GEN-LAST:event_jButtonDisplayAttributeActionPerformed

    private void jListAttributesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListAttributesMouseClicked
        mouseDoubleClicked(evt);
    }//GEN-LAST:event_jListAttributesMouseClicked

    private void jListUnusedAttributesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListUnusedAttributesMouseClicked
        mouseDoubleClicked(evt);
    }//GEN-LAST:event_jListUnusedAttributesMouseClicked

    private void jListActionsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListActionsMouseClicked
        mouseDoubleClicked(evt);
    }//GEN-LAST:event_jListActionsMouseClicked

    private void jListUnusedActionsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListUnusedActionsMouseClicked
        mouseDoubleClicked(evt);
    }//GEN-LAST:event_jListUnusedActionsMouseClicked
    
    private void mouseDoubleClicked(java.awt.event.MouseEvent evt) {
        if (evt.getClickCount() == 2) {
            Component comp = evt.getComponent();
            EPOCObject eobj = getSelectedTemplate(comp);
            if (eobj == null) return;
            if (comp instanceof JList || (comp instanceof JTree
                    && ((DefaultMutableTreeNode)((JTree)comp).getLastSelectedPathComponent()).isLeaf())) {
                if (eobj instanceof Action) {
                    showActionUI((Action)eobj, isEditable());
                } else {
                    showAttributeUI((Attribute)eobj, isEditable());
                }
                if (isEditable() && comp instanceof JTree) {
                    ((DefaultTreeModel)((JTree)comp).getModel()).nodeChanged((DefaultMutableTreeNode)((JTree)comp).getLastSelectedPathComponent());
                }
            }
        }
    }

    /**
     * Sub class mouse adaptor for tree object right-clicks
     */
    private class PopupListener extends MouseAdapter {
        private JPopupMenu popup = new JPopupMenu();
        private String parentName = "";

        private JMenuItem openMenuItem;
        private JMenuItem delMenuItem;
        
        PopupListener(JPopupMenu ppp, final Component parent) {
            popup = ppp;
            parentName = parent.getName();
                    
            openMenuItem = new JMenuItem((isEditable() ? "Edit" : "View"));
            openMenuItem.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    if (parentName.equals("Actions")) jButtonEditActionActionPerformed(evt);
                    if (parentName.equals("UnusedActions")) jButtonEditUnusedActionActionPerformed(evt);
                    if (parentName.equals("Attributes")) jButtonEditAttributeActionPerformed(evt);
                    if (parentName.equals("UnusedAttributes")) jButtonEditUnusedAttributeActionPerformed(evt);
                }
            });

            delMenuItem = new JMenuItem(parent.getName().indexOf("Unused") >= 0 ? "Delete" : "Remove");
            delMenuItem.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    if (parentName.equals("Actions")) jButtonRemoveActionActionPerformed(evt);
                    if (parentName.equals("UnusedActions")) jButtonDeleteUnusedActionActionPerformed(evt);
                    if (parentName.equals("Attributes")) jButtonRemoveAttributeActionPerformed(evt);
                    if (parentName.equals("UnusedAttributes")) jButtonDeleteUnusedAttributeActionPerformed(evt);
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
                modifyPopup(e);
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        }

        /**
         * Modify which menuitems are shown in the pop-up
         */
        private void modifyPopup(MouseEvent e) {
            EPOCObject eObj = getSelectedTemplate(e.getComponent());

            /* if nothing is selected */
            if (eObj == null) return;

            // clear popup
            popup = new JPopupMenu();

            popup.add(openMenuItem);
            popup.add(delMenuItem);
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonBirthday;
    private javax.swing.JButton jButtonDeleteUnusedAction;
    private javax.swing.JButton jButtonDeleteUnusedAttribute;
    private javax.swing.JButton jButtonDisplayAction;
    private javax.swing.JButton jButtonDisplayAttribute;
    private javax.swing.JButton jButtonEditAction;
    private javax.swing.JButton jButtonEditAttribute;
    private javax.swing.JButton jButtonEditUnusedAction;
    private javax.swing.JButton jButtonEditUnusedAttribute;
    private javax.swing.JButton jButtonNewAction;
    private javax.swing.JButton jButtonNewAttribute;
    private javax.swing.JButton jButtonRemoveAction;
    private javax.swing.JButton jButtonRemoveAttribute;
    private javax.swing.JButton jButtonTemplateAction;
    private javax.swing.JButton jButtonTemplateAttribute;
    private javax.swing.JButton jButtonUseAction;
    private javax.swing.JButton jButtonUseAttribute;
    private javax.swing.JComboBox jComboEClass;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JList jListActions;
    private javax.swing.JList jListAttributes;
    private javax.swing.JList jListSelectedPolygons;
    private javax.swing.JList jListUnusedActions;
    private javax.swing.JList jListUnusedAttributes;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanelActions;
    private javax.swing.JPanel jPanelAttributes;
    private javax.swing.JPanel jPanelElement;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPaneActions;
    private javax.swing.JScrollPane jScrollPaneAttributes;
    private javax.swing.JScrollPane jScrollPaneUnusedActions;
    private javax.swing.JScrollPane jScrollPaneUnusedAttributes;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextArea jTextAreaDesc;
    private javax.swing.JTextField jTextBDay;
    private javax.swing.JTextField jTextBMonth;
    private javax.swing.JTextField jTextCreated;
    private javax.swing.JTextField jTextCreator;
    private javax.swing.JTextField jTextID;
    private javax.swing.JTextField jTextModified;
    private javax.swing.JTextField jTextName;
    private javax.swing.JTextField jTextShortName;
    private javax.swing.JTextField jTextVersion;
    // End of variables declaration//GEN-END:variables
    
}
