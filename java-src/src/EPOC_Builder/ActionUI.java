/*******************************************************************************
 * ActionUI.java
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
import au.gov.aad.erm.RJEditor.*;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import com.toedter.calendar.*;
import java.awt.Color;
import java.util.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.awt.Font;

/*******************************************************************************
 * GUI for Action object.
 * Allows display and data entry for Action objects.
 *
 * @author Troy Robertson
 * @company Australian Antarctic Division
 * @created 10/09/2009
 * @version 0.4.0, 01-09-2010
 *******************************************************************************/
public class ActionUI extends OpposingPanelUI {
    
    private static Element element;
    private Action action;
    private Universe universe;
    EmbeddedRJEditor rje = null;
    Color defaultbgcolour;

    JCheckBox bday;
    boolean bdaySet;
    //private ArrayList timesteps = new ArrayList();
  
    /** Creates new form ActionUI */
    public ActionUI(Action act, Element ele, Universe uni) {
        universe = uni;
        element = ele;
        action = act;

        initComponents();
        rje = new EmbeddedRJEditor(true);
        jPanelEditor.add(rje);
        defaultbgcolour = jComboSetup.getBackground();
        editable(true);
        
        loadForm();
        requestFocus();
        jTextShortName.requestFocus();
    }
   
    /*
     * Set editability of form
     * @param editable
     */
    @Override
    public void editable(boolean editable) {
        super.editable(editable);
        jTextShortName.setEditable(editable);
        jComboActType.setEnabled(editable);
        jTextAreaDesc.setEditable(editable);
        jButtonDatePicker.setEnabled(editable);
        jButtonDateDelete.setEnabled(editable);
        if (!editable) {
            // Cancel opening of popups
            jComboSetup.addPopupMenuListener(new PopupMenuListener() {
                public void popupMenuWillBecomeVisible(PopupMenuEvent pme) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() { jComboSetup.hidePopup(); }
                    });
                }
                public void popupMenuWillBecomeInvisible(PopupMenuEvent pme) { }
                public void popupMenuCanceled(PopupMenuEvent pme) { }
            });
            jComboDataset.addPopupMenuListener(new PopupMenuListener() {
                public void popupMenuWillBecomeVisible(PopupMenuEvent pme) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() { jComboDataset.hidePopup(); }
                    });
                }
                public void popupMenuWillBecomeInvisible(PopupMenuEvent pme) { }
                public void popupMenuCanceled(PopupMenuEvent pme) { }
            });
        }
        //jComboSetup.setEnabled(editable);
        //jComboDataset.setEnabled(editable);
        jListRelated.setEnabled(editable);
        rje.editable(editable);
        jButtonParse.setEnabled(editable && rex.hasEngine());
    }

    private void loadForm() {
        jTextShortName.setText(action.getShortName());
        // bold templates
        if (action.isTemplate()) {
            jTextShortName.setFont(jTextShortName.getFont().deriveFont(Font.BOLD));
        } else {
            jTextShortName.setFont(jTextShortName.getFont().deriveFont(Font.PLAIN));
        }
        jTextVersion.setText(action.getRevision());
        jComboActType.setSelectedIndex(action.getActType() - 1);
        jTextModified.setText(action.getFormattedModified());
        jTextAreaDesc.setText(action.getDescription());
        loadTimesteps();
        loadTransform();
        loadDataset();
        loadRelated();
        rje.setText(action.getCode());
        updateFormLayout();
    }

    /**
     * Load parent elements attributes adn/or templated attributes.
     * List starts with a "NULL" item.
     * Set current Dataset object as selected.
     */
    private void loadDataset() {   
        //int selIndex = 0;
        Attribute selObj = null;
        ArrayList dsList = new ArrayList();

        // add dummy none item
        Attribute dummy = new Attribute("");
        dummy.setShortName("NULL");
        jComboDataset.addItem(dummy);
        selObj = dummy;

        // Add selected object
        if (action.getDataset() != null) {
            if (action.getDataset().isTemplate()) jComboDataset.setFont(jComboDataset.getFont().deriveFont(Font.BOLD));
            if (action.getDataset().isBroken()) jComboDataset.setBackground(Color.RED);
            dsList.add(action.getDataset());
            selObj = action.getDataset();
            //selIndex = dsList.indexOf(action.getDataset());
        }
        // add either existing element attributes or template ones
        if (element != null) {
            dsList.addAll(element.getAttributes());
        } else {
            dsList.addAll(action.getTemplates().getTemplateList(OBJ_ATT));
        }
        Collections.sort(dsList, new EPOCObjectListComparator());

        for (Object obj : dsList) jComboDataset.addItem((Attribute)obj);

        // set selected
        jComboDataset.setSelectedItem(selObj);
        //jComboDataset.setSelectedIndex(selIndex);

        jComboDataset.setRenderer(new EPOCObjectListRenderer());
    }

    /**
     * Load parent elements setup actions and/or templated setup actions.
     * List starts with a "NULL" item.
     * Set current Transform object as selected.
     */
    private void loadTransform() {
        Action selObj = null;
        ArrayList availList = new ArrayList();

        // add dummy none item
        Action dummy = new Action("");
        dummy.setShortName("NULL");
        jComboSetup.addItem(dummy);
        selObj = dummy;

        // get either existing element actions or template ones
        if (element != null) {
            availList.addAll(element.getActions());
        } else {
            availList.addAll(action.getTemplates().getTemplateList(OBJ_ACT));
        }

        // make sure selected object is in there, even if it is just a broken link
        if (action.getTransform() != null) {
            if (action.getTransform().isTemplate()) jComboSetup.setFont(jComboSetup.getFont().deriveFont(Font.BOLD));
            jComboSetup.setForeground(Color.PINK);
            if (action.getTransform().isBroken()) {
                jComboSetup.setBackground(Color.RED);
                jComboSetup.setForeground(Color.WHITE);
            }
            if (!availList.contains(action.getTransform())) jComboSetup.addItem(action.getTransform());
            selObj = action.getTransform();
        }
        Collections.sort(availList, new EPOCObjectListComparator());

        // but only add Actions of type setup (oh and broken links)
        for (Object actObj : availList) {
            if (((Action)actObj).isSetup() || ((Action)actObj).isBroken()) jComboSetup.addItem(actObj);
        }

        // set selected
        jComboSetup.setSelectedItem(selObj);

        jComboSetup.setRenderer(new EPOCObjectListRenderer());
    }

    /**
     * Load available template and/or local elements
     */
    private void loadRelated() {
        ArrayList availList = new ArrayList();
        ArrayList<Integer> currRel = new ArrayList();

        // Add both universe member elements and templated ones
        if (element != null) {
            availList = universe.getElements(OBJ_ELE);
            // Dont show this actions parent element as a choice
            availList.remove(element);
        }
        if (action.isTemplate()) {
            for (Element ele : universe.getTemplates().getElementTemplateList(OBJ_ELE)) {
                if (!availList.contains(ele)) availList.add(ele);
            }
        }
        Collections.sort(availList, new EPOCObjectListComparator());
        
        // Make sure currently selected are in there, even if they are just broken links
        // At the same time find indexes of currently related elements
        for (Object rObj : action.getRelatedElements()) {
            if (!availList.contains(rObj)) availList.add(rObj);
            currRel.add(availList.indexOf(rObj));
        }

        // Add available list items
        jListRelated.setListData(availList.toArray());
        
        // set selected and scroll to first of these
        int[] selArr = new int[currRel.size()];
        for (int i = 0 ; i < currRel.size() ; i++) selArr[i] = (int)currRel.get(i);
        jListRelated.setSelectedIndices(selArr);
        if (selArr.length > 0) jListRelated.ensureIndexIsVisible(selArr[0]);

        jListRelated.setCellRenderer(new EPOCObjectListRenderer());
    }
    
    private void loadTimesteps() {
        Timestep[] list = action.getTimestepArray(true);
        
        jListTimesteps.setListData(list);
        //create Renderer and display
        jListTimesteps.setCellRenderer(new TimestepCellRenderer());
    }

    public void updateFormLayout() {
        if (jComboActType.getSelectedIndex() + 1 == ACT_SET) {
           jPanelTransformParams.setVisible(true);
           jPanelSupportParams.setVisible(false);
           jPanelActionParams.setVisible(false);
           jLabelTransformReturn.setVisible(true);
           jPanelActionFields.setVisible(false);
           jLabel15.setVisible(true);
           jComboDataset.setVisible(true);
       } else if (jComboActType.getSelectedIndex() + 1 == ACT_SUP) {
           jPanelTransformParams.setVisible(false);
           jPanelSupportParams.setVisible(true);
           jPanelActionParams.setVisible(false);
           jLabelTransformReturn.setVisible(false);
           jPanelActionFields.setVisible(false);
           jLabel15.setVisible(true);
           jComboDataset.setVisible(true);
       } else {
           jPanelTransformParams.setVisible(false);
           jPanelSupportParams.setVisible(false);
           jPanelActionParams.setVisible(true);
           jLabelTransformReturn.setVisible(false);
           jPanelActionFields.setVisible(true);
           jLabel15.setVisible(false);
           jComboDataset.setVisible(false);
       }
    }

    public int saveIfModified() {
        // Check if form data has been modified, if so update object
        if (isModified()) {
            setWasModified(true);

            // Check if required fields are filled adequately
            if (!EPOCObject.testName(jTextShortName.getText())) {
                JOptionPane.showMessageDialog(this, "Please provide a shortname for action first!\n\n" +
                         "Action must be named and may only contain\n" +
                         "alphanumerics, '.' or '_'\n" +
                         "It may only start with an alphabetic character\n" +
                         "and may not end in '.' or '_'");
                return EPOC_FAIL;
            }

            if (jTextAreaDesc.getText().length() > 512) {
                JOptionPane.showMessageDialog(this, "The description field is limited to only 512 characters!");
                return EPOC_FAIL;
            }

            action.setShortName(jTextShortName.getText());
            action.setModifiedNow();
            action.setDescription(jTextAreaDesc.getText());
            action.setActType(jComboActType.getSelectedIndex() + 1);

            // Save transform
            if (jComboSetup.getSelectedIndex() <= 0) {
                action.setTransform(null);
            } else {
                action.setTransform((Action)jComboSetup.getSelectedItem());
            }

            // Save dataset
            if (jComboDataset.getSelectedIndex() <= 0) {
                action.setDataset(null);
            } else {
                action.setDataset((Attribute)jComboDataset.getSelectedItem());
            }

            // Save related elements
            action.getRelatedElements().clear();
            for (Object ele : jListRelated.getSelectedValues()) {
                action.addRelatedElement((Element)ele);
            }

            action.setCode(rje.getText());

            setModified(false);
            return EPOC_SUCC;
        }

        setWasModified(false);
        return EPOC_NONE;
    }

    /**
     * Have form values been modified when compared to stored object data
     * @return boolean
     */
    public boolean isModified() {
        if (super.isModified()) return true;    // This will be set by timestep edits
        if (!action.getShortName().equals(jTextShortName.getText())) return true;
        if (action.getActType() != jComboActType.getSelectedIndex() + 1) return true;
        if (!action.getDescription().equals(jTextAreaDesc.getText())) return true;
        
        // Transform action
        if (jComboSetup.getSelectedIndex() <= 0 && action.getTransform() != null) return true;
        if (jComboSetup.getSelectedIndex() == 0 && action.getTransform() != null) return true;
        if (jComboSetup.getSelectedIndex() > 0) {
            if (action.getTransform() == null || !action.getTransform().equals((Action)jComboSetup.getSelectedItem())) return true;
        }

        // Dataset
        if (jComboDataset.getSelectedIndex() <= 0 && action.getDataset() != null) return true;
        if (jComboDataset.getSelectedIndex() == 0 && action.getDataset() != null) return true;
        if (jComboDataset.getSelectedIndex() > 0) {
            if (action.getDataset() == null || !action.getDataset().equals((Attribute)jComboDataset.getSelectedItem())) return true;
        }

        // Related elements
        ArrayList rList = action.getRelatedElements();
        if (rList.size() != jListRelated.getSelectedIndices().length) return true;
        ArrayList sList = new ArrayList();
        for (Object sEle : jListRelated.getSelectedValues()) {
            if (!rList.contains(sEle)) return true;
            sList.add(sEle);
        }
        for (Object rEle : rList) {
            if (!sList.contains(rEle)) return true;
        }
        
        if (!action.getCode().trim().equals(rje.getText().trim())) return true;

        return false;
    }

    public EPOCObject getObject() {
        return action;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextAreaDesc = new javax.swing.JTextArea();
        jLabel2 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jTextVersion = new javax.swing.JTextField();
        jPanelActionFields = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jListTimesteps = new javax.swing.JList();
        jButtonDatePicker = new javax.swing.JButton();
        jButtonDateDelete = new javax.swing.JButton();
        jComboSetup = new javax.swing.JComboBox();
        jLabel16 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jListRelated = new javax.swing.JList();
        jLabel29 = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        jPanelActionParams = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jPanelTransformParams = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jButtonParse = new javax.swing.JButton();
        jComboDataset = new javax.swing.JComboBox();
        jLabel15 = new javax.swing.JLabel();
        jLabelTransformReturn = new javax.swing.JLabel();
        jPanelSupportParams = new javax.swing.JPanel();
        jLabel32 = new javax.swing.JLabel();
        jLabel33 = new javax.swing.JLabel();
        jLabel34 = new javax.swing.JLabel();
        jPanelEditor = new javax.swing.JPanel();
        jTextModified = new javax.swing.JTextField();
        jLabel36 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jTextShortName = new javax.swing.JTextField();
        jComboActType = new javax.swing.JComboBox();
        jLabel31 = new javax.swing.JLabel();

        setPreferredSize(new java.awt.Dimension(600, 450));

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel1.setPreferredSize(new java.awt.Dimension(580, 420));

        jTextAreaDesc.setFont(new java.awt.Font("Tahoma", 0, 11));
        jTextAreaDesc.setMaximumSize(new java.awt.Dimension(2147483647, 58));
        jScrollPane2.setViewportView(jTextAreaDesc);

        jLabel2.setText("Description:");

        jLabel7.setText("Revision:");

        jTextVersion.setEditable(false);
        jTextVersion.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jTextVersion.setPreferredSize(new java.awt.Dimension(200, 18));

        jLabel4.setText("Timesteps:");

        jLabel6.setText("Start");

        jLabel8.setText("End");

        jLabel5.setText("Type");

        jLabel14.setText("Timing");

        jScrollPane3.setHorizontalScrollBar(null);
        jScrollPane3.setPreferredSize(new java.awt.Dimension(250, 60));

        jListTimesteps.setVisibleRowCount(3);
        jScrollPane3.setViewportView(jListTimesteps);

        jButtonDatePicker.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/calendar.png"))); // NOI18N
        jButtonDatePicker.setToolTipText("Choose dates");
        jButtonDatePicker.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDatePickerActionPerformed(evt);
            }
        });

        jButtonDateDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/cross.png"))); // NOI18N
        jButtonDateDelete.setToolTipText("Remove date");
        jButtonDateDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDateDeleteActionPerformed(evt);
            }
        });

        jComboSetup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboSetupActionPerformed(evt);
            }
        });

        jLabel16.setText("Transform:");

        jListRelated.setVisibleRowCount(3);
        jScrollPane4.setViewportView(jListRelated);

        jLabel29.setText("Related:");

        jLabel30.setText("Dataset");

        org.jdesktop.layout.GroupLayout jPanelActionFieldsLayout = new org.jdesktop.layout.GroupLayout(jPanelActionFields);
        jPanelActionFields.setLayout(jPanelActionFieldsLayout);
        jPanelActionFieldsLayout.setHorizontalGroup(
            jPanelActionFieldsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelActionFieldsLayout.createSequentialGroup()
                .add(81, 81, 81)
                .add(jLabel6)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 63, Short.MAX_VALUE)
                .add(jLabel8)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 61, Short.MAX_VALUE)
                .add(jLabel5)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 61, Short.MAX_VALUE)
                .add(jLabel14)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 61, Short.MAX_VALUE)
                .add(jLabel30, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(105, Short.MAX_VALUE))
            .add(jPanelActionFieldsLayout.createSequentialGroup()
                .addContainerGap()
                .add(jPanelActionFieldsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanelActionFieldsLayout.createSequentialGroup()
                        .add(jPanelActionFieldsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel4)
                            .add(jLabel16))
                        .add(18, 18, 18)
                        .add(jPanelActionFieldsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jPanelActionFieldsLayout.createSequentialGroup()
                                .add(jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 457, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jPanelActionFieldsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(jButtonDateDelete, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 22, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(jButtonDatePicker, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 22, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                            .add(jComboSetup, 0, 485, Short.MAX_VALUE)))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanelActionFieldsLayout.createSequentialGroup()
                        .add(jLabel29)
                        .add(30, 30, 30)
                        .add(jScrollPane4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 485, Short.MAX_VALUE))))
        );
        jPanelActionFieldsLayout.setVerticalGroup(
            jPanelActionFieldsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelActionFieldsLayout.createSequentialGroup()
                .add(jPanelActionFieldsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanelActionFieldsLayout.createSequentialGroup()
                        .add(22, 22, 22)
                        .add(jLabel4))
                    .add(jPanelActionFieldsLayout.createSequentialGroup()
                        .add(jPanelActionFieldsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel6)
                            .add(jLabel14)
                            .add(jLabel30)
                            .add(jLabel8)
                            .add(jLabel5))
                        .add(0, 0, 0)
                        .add(jPanelActionFieldsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jPanelActionFieldsLayout.createSequentialGroup()
                                .add(jButtonDatePicker, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 22, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jButtonDateDelete, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 22, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(jScrollPane3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 48, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanelActionFieldsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jComboSetup, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel16))))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanelActionFieldsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(jPanelActionFieldsLayout.createSequentialGroup()
                        .add(jLabel29)
                        .add(30, 30, 30))
                    .add(jScrollPane4, 0, 0, Short.MAX_VALUE)))
        );

        jPanelActionParams.setPreferredSize(new java.awt.Dimension(200, 14));

        jLabel9.setText("Parameters:");

        jLabel10.setText(" (.Object,");
        jLabel10.setToolTipText("<html><b>Element object</b></html>");

        jLabel11.setText("universe,");
        jLabel11.setToolTipText("<html><b>Universe object</b></html>");

        jLabel12.setText("action,");
        jLabel12.setToolTipText("<html>\n<table>\n<th>ActionMat row (list)</th>\n<tr><td>Col  1  = module</td></tr>\n<tr><td>Col  2  = element</td></tr>\n<tr><td>Col  3  = period</td></tr>\n<tr><td>Col  4  = reference day in year</td></tr>\n<tr><td>Col  5  = action reference number in period (NA if no actions)</td></tr>\n<tr><td>Col  6  = number for \"before =1\", \"during = 2\", \"after = 3\" (NA if no actions)</td></tr>\n</table>\n</html>");

        jLabel13.setText("periodInfo)");
        jLabel13.setToolTipText("<html>\n<table>\n<th>Information about the active period for use in subroutines (list)</th>\n<tr><td>Number       = eTSD</tr></tr>\n<tr><td>Day             = PropYear[eTSD,1]</tr></tr>\n<tr><td>KnifeEdge   = if(PropYear[eTSD,2]==0) FALSE else TRUE</tr></tr>\n<tr><td>YearPropn  = PropYear[eTSD,3]</tr></tr>\n<tr><td>PeriodStart = PreviousDay/365 (proportion of year passed since 0 Jan</tr></tr>\n<tr><td>&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbspto beginning of time period)</tr></tr>\n<tr><td>PeriodEnd   = PreviousDay/365+PropYear[eTSD,3]</tr></tr>\n</table>\n</html>");

        org.jdesktop.layout.GroupLayout jPanelActionParamsLayout = new org.jdesktop.layout.GroupLayout(jPanelActionParams);
        jPanelActionParams.setLayout(jPanelActionParamsLayout);
        jPanelActionParamsLayout.setHorizontalGroup(
            jPanelActionParamsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelActionParamsLayout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel9)
                .add(11, 11, 11)
                .add(jLabel10)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel11)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel12)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel13)
                .addContainerGap(274, Short.MAX_VALUE))
        );
        jPanelActionParamsLayout.setVerticalGroup(
            jPanelActionParamsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelActionParamsLayout.createSequentialGroup()
                .add(jPanelActionParamsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel9)
                    .add(jLabel10)
                    .add(jLabel11)
                    .add(jLabel12)
                    .add(jLabel13))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel17.setText("Parameters:");

        jLabel18.setText(" (.Object,");
        jLabel18.setToolTipText("<html><b>Element object</b></html>");

        jLabel19.setText("calendar,");
        jLabel19.setToolTipText("<html><b>Calendar object</b></html>");

        jLabel20.setText("ptSA,");
        jLabel20.setToolTipText("<html>\n<b>untransformed action for the period derived from timestep of element</b><br>\nNote: PtSA is a list retained for concatenating to a list.<br>\nTherefore, the action is the first element in the list<br>\n</html>");

        jLabel22.setText("moduleNum,");
        jLabel22.setToolTipText("<html><b>reference module number for the element</b></html>");

        jLabel23.setText("elementNum,");
        jLabel23.setToolTipText("<html><b>relative number of the element in the universe</b></html>");

        jLabel24.setText("tstepNum,");
        jLabel24.setToolTipText("<html><b>current time step in element</b></html>");

        jLabel25.setText("a,");
        jLabel25.setToolTipText("<html><b>number of action in time step</b></html>");

        jLabel26.setText("pe,");
        jLabel26.setToolTipText("<html><b>number of the period in the calendar</b></html>");

        jLabel27.setText("firstPeriod,");
        jLabel27.setToolTipText("<html><b>logical indicating if this is the first period in the timestep</b></html>");

        jLabel21.setText("dset)");
        jLabel21.setToolTipText("<html><b>dataset to assist with transformation</b></html>");

        org.jdesktop.layout.GroupLayout jPanelTransformParamsLayout = new org.jdesktop.layout.GroupLayout(jPanelTransformParams);
        jPanelTransformParams.setLayout(jPanelTransformParamsLayout);
        jPanelTransformParamsLayout.setHorizontalGroup(
            jPanelTransformParamsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelTransformParamsLayout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel17)
                .add(11, 11, 11)
                .add(jLabel18)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel19)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel20)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel22)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel23)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel24)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel25)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel26)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel27)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel21)
                .addContainerGap(22, Short.MAX_VALUE))
        );
        jPanelTransformParamsLayout.setVerticalGroup(
            jPanelTransformParamsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelTransformParamsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(jLabel17)
                .add(jLabel18)
                .add(jLabel19)
                .add(jLabel20)
                .add(jLabel22)
                .add(jLabel24)
                .add(jLabel25)
                .add(jLabel27)
                .add(jLabel26)
                .add(jLabel21)
                .add(jLabel23))
        );

        jLabel3.setText("Code:");

        jButtonParse.setForeground(new java.awt.Color(0, 0, 255));
        jButtonParse.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/run.png"))); // NOI18N
        jButtonParse.setText("Parse");
        jButtonParse.setToolTipText("Test R code syntax");
        jButtonParse.setAlignmentX(0.5F);
        jButtonParse.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButtonParse.setMaximumSize(new java.awt.Dimension(55, 20));
        jButtonParse.setMinimumSize(new java.awt.Dimension(55, 20));
        jButtonParse.setPreferredSize(new java.awt.Dimension(55, 20));
        jButtonParse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonParseActionPerformed(evt);
            }
        });

        jComboDataset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboDatasetActionPerformed(evt);
            }
        });

        jLabel15.setText("Dataset:");

        jLabelTransformReturn.setText(" return(ptSA)");

        jPanelSupportParams.setPreferredSize(new java.awt.Dimension(200, 14));

        jLabel32.setText("Parameters:");

        jLabel33.setText(" (.Object,");
        jLabel33.setToolTipText("<html><b>Element object</b></html>");

        jLabel34.setText("universe)");
        jLabel34.setToolTipText("<html><b>Universe object</b></html>");

        org.jdesktop.layout.GroupLayout jPanelSupportParamsLayout = new org.jdesktop.layout.GroupLayout(jPanelSupportParams);
        jPanelSupportParams.setLayout(jPanelSupportParamsLayout);
        jPanelSupportParamsLayout.setHorizontalGroup(
            jPanelSupportParamsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelSupportParamsLayout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel32)
                .add(11, 11, 11)
                .add(jLabel33)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel34)
                .addContainerGap(337, Short.MAX_VALUE))
        );
        jPanelSupportParamsLayout.setVerticalGroup(
            jPanelSupportParamsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelSupportParamsLayout.createSequentialGroup()
                .add(jPanelSupportParamsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel32)
                    .add(jLabel33)
                    .add(jLabel34))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanelEditor.setBackground(new java.awt.Color(255, 255, 255));
        jPanelEditor.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelEditor.setLayout(new javax.swing.BoxLayout(jPanelEditor, javax.swing.BoxLayout.Y_AXIS));

        jTextModified.setBackground(new java.awt.Color(212, 208, 200));
        jTextModified.setEditable(false);
        jTextModified.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jTextModified.setPreferredSize(new java.awt.Dimension(200, 18));
        jTextModified.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextModifiedActionPerformed(evt);
            }
        });

        jLabel36.setText("Modified:");

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                .add(org.jdesktop.layout.GroupLayout.LEADING, jPanelTransformParams, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(org.jdesktop.layout.GroupLayout.LEADING, jPanelActionParams, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 551, Short.MAX_VALUE))
            .add(jPanelSupportParams, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 515, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanelActionFields, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel2)
                    .add(jLabel7)
                    .add(jLabel3)
                    .add(jLabel15)
                    .add(jButtonParse, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 61, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(10, 10, 10)
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jPanel1Layout.createSequentialGroup()
                                .add(jTextVersion, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 150, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 137, Short.MAX_VALUE)
                                .add(jLabel36)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jTextModified, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 150, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 485, Short.MAX_VALUE)
                            .add(jComboDataset, 0, 485, Short.MAX_VALUE)))
                    .add(jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabelTransformReturn)
                            .add(jPanelEditor, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 485, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel7)
                    .add(jTextVersion, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jTextModified, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel36))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel2)
                    .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 49, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanelActionFields, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel15)
                    .add(jComboDataset, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanelActionParams, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 14, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanelSupportParams, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 14, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanelTransformParams, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel1Layout.createSequentialGroup()
                        .add(jLabel3)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(jButtonParse, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jPanelEditor, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 45, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabelTransformReturn)
                .addContainerGap())
        );

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel1.setText("Action:");

        jTextShortName.setColumns(20);
        jTextShortName.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        jTextShortName.setToolTipText("Short Name");

        jComboActType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Timestep", "Setup", "Support" }));
        jComboActType.setMinimumSize(new java.awt.Dimension(53, 18));
        jComboActType.setPreferredSize(new java.awt.Dimension(57, 20));
        jComboActType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboActTypeActionPerformed(evt);
            }
        });

        jLabel31.setText("Action type:");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(21, 21, 21)
                        .add(jLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 39, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(33, 33, 33)
                        .add(jTextShortName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 150, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 122, Short.MAX_VALUE)
                        .add(jLabel31)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jComboActType, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 162, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(jTextShortName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jComboActType, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel31))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 406, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonDatePickerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDatePickerActionPerformed
        int stDay = 0, stMth = 0, enDay = 0, enMth = 0, sType = TS_ALL, sTime = TS_BEF;
        
        // item must be selected unless this is the first item to be added
        if (jListTimesteps.getSelectedIndex() < 0 && action.getTimesteps().size() > 0) {
            return;
        } else if (jListTimesteps.getSelectedIndex() < 0) {
            // if so then set selected to be the 'New ...' item
            jListTimesteps.setSelectedIndex(0);
        }
        Timestep selTS = (Timestep)jListTimesteps.getSelectedValue();
                
        // Construct a date picker panel
        JMonthChooser jmc = new JMonthChooser();
        bday = new JCheckBox();
        bday.setText("Use Birthday");
        bday.addItemListener(new CheckboxListener());
        JDayChooser jdc = new JDayChooser();
        //jmc.addInputMethodListener(new MonthInputListener(jdc));
        jmc.addPropertyChangeListener(new MonthPropertyListener(jdc));

        JPanel top = new JPanel();
        top.add(jmc);
        top.add(bday);
        JPanel picker = new JPanel();
        picker.setLayout(new BoxLayout(picker, BoxLayout.PAGE_AXIS));
        picker.add(top);
        picker.add(jdc);
        
        // Set current start day and month
        bdaySet = false;
        if (selTS.getStartMonth() != 0) {
            if (selTS.getStartMonth() == 99) {
                bdaySet = true;
            } else {
                jdc.setMonth(selTS.getStartMonth()-1);
                jdc.setDay(selTS.getStartDay());
                jmc.setMonth(selTS.getStartMonth()-1);
            }
        }
        bday.setSelected(bdaySet);
            
        final JOptionPane pane = new JOptionPane(picker, JOptionPane.PLAIN_MESSAGE, 
                                                                JOptionPane.DEFAULT_OPTION);
        JDialog dialog = pane.createDialog(this, "Select start day:");
        dialog.setDefaultCloseOperation(dialog.DO_NOTHING_ON_CLOSE);
        dialog.setLocationRelativeTo(this);
        dialog.pack();
        dialog.setVisible(true);
        
        if (bdaySet) {
            stDay = 99;
            stMth = 99;
        } else {
            stDay = jdc.getDay();
            stMth = jmc.getMonth() + 1;
        }
        
        // set current end day and month, else selected start day and month
        bdaySet = false;
        if (selTS.getStartMonth() != 0) {
            if (selTS.getEndMonth() == 99) {
                bdaySet = true;
            } else {
                jdc.setDay(selTS.getEndDay());
                jmc.setMonth(selTS.getEndMonth()-1);
            }
        } else {
            if (stMth == 99) {
                bdaySet = true;
            } else {
                jdc.setDay(stDay);
                jmc.setMonth(stMth-1);
            }
        }
        bday.setSelected(bdaySet);
        
        dialog = pane.createDialog(this, "Select end day:");
        dialog.setDefaultCloseOperation(dialog.DO_NOTHING_ON_CLOSE);
        dialog.setLocationRelativeTo(this);
        dialog.pack();
        dialog.setVisible(true);
        
        if (bdaySet) {
            enDay = 99;
            enMth = 99;
        } else {
            enDay = jdc.getDay();
            enMth = jmc.getMonth() + 1;
        }
        
        // before we go any further do some checking
        // that start date is earlier than end date
        //if (enMth > stMth || (enMth == stMth && enDay > stDay)) {
        //    JOptionPane.showMessageDialog(this, "Timestep end day cannot be earlier than start day!");
        //    return;
        //}
        
        //that selected dates do not overlap with existing dates
        Timestep overlap = action.getOverlappingTimestep(stDay, stMth, enDay, enMth,
                                                         (element != null ? element.getBirthDay() : 0),
                                                         (element != null ? element.getBirthMonth() : 0));
        if (overlap != null && overlap != selTS) {
            JOptionPane.showMessageDialog(this, "Timestep overlaps with an existing Timestep!");
            return;
        }
        
        // Now get input on step type if start and end are not the same
        //if (stDay != enDay || stMth != enMth) {
            String oldType = "All Periods";
            if (selTS.getStepType() == TS_FST) oldType = "First Period";
            if (selTS.getStepType() == TS_LST) oldType = "Last Period";

            Object[] types = {"All Periods", "First Period", "Last Period"};
            String s = (String)JOptionPane.showInputDialog(this,
                        "Enter period at which Action is to be executed.", "Select step type:",
                        JOptionPane.PLAIN_MESSAGE, null, types, oldType);
            if (s == null) return;
            if (s.equals("All Periods")) sType = TS_ALL;
            if (s.equals("First Period")) sType = TS_FST;
            if (s.equals("Last Period")) sType = TS_LST;
        //} else {
        //    sType = TS_LST;
        //}

        // Now get input on step timing
        String oldTiming = "Before";
        if (selTS.getStepTiming() == TS_DUR) oldTiming = "During";
        if (selTS.getStepTiming() == TS_AFT) oldTiming = "After";

        Object[] timings = {"Before", "During", "After"};
        String t = (String)JOptionPane.showInputDialog(this,
                    "Enter timing at which Action is to be executed.", "Select step timing:",
                    JOptionPane.PLAIN_MESSAGE, null, timings, oldTiming);
        if (t == null) return;
        if (t.equals("Before")) sTime = TS_BEF;
        if (t.equals("During")) sTime = TS_DUR;
        if (t.equals("After")) sTime = TS_AFT;

        // Get input for dataset
        ArrayList attrList = new ArrayList();
        // Add dummy NULL
        Attribute attr = new Attribute();
        attr.setRevision("");
        attr.setShortName("NULL");
        attrList.add(attr);
        Attribute currDS = attr;
        
        // Add element attributes or templated ones
        if (element != null) {
            attrList.addAll(element.getAttributes());
        } else {
            attrList.addAll(action.getTemplates().getTemplateList(OBJ_ATT));
        }
        // Add selected dataset
        if (selTS.getDataset() != null) {
            if (!attrList.contains(selTS.getDataset())) attrList.add(selTS.getDataset());
            currDS = selTS.getDataset();
        }

        ListSelectorUI selUI = new ListSelectorUI("Enter dataset to be passed to Action at this timestep.", attrList, currDS);
        final JOptionPane dsPane = new JOptionPane(selUI, JOptionPane.QUESTION_MESSAGE,
                JOptionPane.OK_CANCEL_OPTION);
        dialog = dsPane.createDialog(this, "Select dataset:");
        dialog.setLocationRelativeTo(this);
        dialog.pack();
        dialog.setVisible(true);

        // Make sure a valid response was returned
        Object selVal = dsPane.getValue();
        if (selVal == null || selVal == JOptionPane.UNINITIALIZED_VALUE) return;
        if(selVal instanceof Integer && ((Integer)selVal).intValue() == JOptionPane.CANCEL_OPTION) return;
        int choice = selUI.getSelectionIndex();
        if (choice < 0) return;
       
        EPOCObject sAttr = (EPOCObject)attrList.get(choice);
        if (sAttr.equals(attr)) sAttr = null;
        selTS.setParentUID(action.getUID());
        selTS.setSteps(stDay, stMth, enDay, enMth);
        selTS.setStepType(sType);
        selTS.setStepTiming(sTime);
        selTS.setDataset((Attribute)sAttr);

        // Check if "New..." selected
        if (jListTimesteps.getSelectedIndex() == 0) {
            action.addTimestep(selTS);
        }
        
        loadTimesteps();
        setModified(true);
    }//GEN-LAST:event_jButtonDatePickerActionPerformed

    private void jButtonDateDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDateDeleteActionPerformed
        if (jListTimesteps.getSelectedIndex() <= 0) {
            JOptionPane.showMessageDialog(this, "A valid Timestep must be selected first!");
            return;
        }
        
        // get timestep and put it in delete list to be deleted on action save
        Timestep ts = (Timestep)jListTimesteps.getSelectedValue();
        if (ts.getUID() > 0) {
            action.addDeleteList(ts);
        }
        
        action.getTimesteps().remove((Timestep)jListTimesteps.getSelectedValue());
        
        loadTimesteps();
        setModified(true);
    }//GEN-LAST:event_jButtonDateDeleteActionPerformed

    private void jButtonParseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonParseActionPerformed
        if (!rje.getText().equals("")) {
            if (rex.hasEngine()) {
                if (!rex.parse(rje.getText())) {
                    JOptionPane.showMessageDialog(this, Messages.getUnreadErrMsgs(), "Failed", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Passed syntax check.", "Passed", JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "No JRI Engine found!\nUnable to parse R code!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
}//GEN-LAST:event_jButtonParseActionPerformed

    private void jComboActTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboActTypeActionPerformed
       updateFormLayout();
    }//GEN-LAST:event_jComboActTypeActionPerformed

    /**
     * Set/unset background colour after selection dependant on it being
     * a broken link
     * @param evt
     */
    private void jComboDatasetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboDatasetActionPerformed
        if (jComboDataset.getSelectedIndex() > 0) {
            if (((EPOCObject)jComboDataset.getSelectedItem()).getUID() < 0) {
                jComboDataset.setBackground(Color.RED);
                return;
            }
        }
        jComboDataset.setBackground(defaultbgcolour);
    }//GEN-LAST:event_jComboDatasetActionPerformed

    /**
     * Set/unset background colour after selection dependant on it being
     * a broken link
     * @param evt
     */
    private void jComboSetupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboSetupActionPerformed
        if (jComboSetup.getSelectedIndex() > 0) {
            if (((EPOCObject)jComboSetup.getSelectedItem()).getUID() < 0) {
                jComboSetup.setBackground(Color.RED);
                return;
            }
        }
        jComboSetup.setBackground(defaultbgcolour);
    }//GEN-LAST:event_jComboSetupActionPerformed

    private void jTextModifiedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextModifiedActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_jTextModifiedActionPerformed

    public class CheckboxListener implements ItemListener {
              // now the event listeners
        public void itemStateChanged(ItemEvent ie) {

            Object source = ie.getItem();
            if (source == bday) {
                if (ie.getStateChange() == ItemEvent.SELECTED) {
                    bdaySet = true;
                } else {
                    bdaySet = false;
                }
            }
        }
    }

    public class MonthPropertyListener implements PropertyChangeListener {
        private JDayChooser jdc = null;

        public MonthPropertyListener(JDayChooser dc) {
            jdc = dc;
        }

        public void propertyChange(java.beans.PropertyChangeEvent e) {
            if (e.getPropertyName().equals("month")) {
                jdc.setMonth(((JMonthChooser)e.getSource()).getMonth());
            }
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonDateDelete;
    private javax.swing.JButton jButtonDatePicker;
    private javax.swing.JButton jButtonParse;
    private javax.swing.JComboBox jComboActType;
    private javax.swing.JComboBox jComboDataset;
    private javax.swing.JComboBox jComboSetup;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelTransformReturn;
    private javax.swing.JList jListRelated;
    private javax.swing.JList jListTimesteps;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanelActionFields;
    private javax.swing.JPanel jPanelActionParams;
    private javax.swing.JPanel jPanelEditor;
    private javax.swing.JPanel jPanelSupportParams;
    private javax.swing.JPanel jPanelTransformParams;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTextArea jTextAreaDesc;
    private javax.swing.JTextField jTextModified;
    private javax.swing.JTextField jTextShortName;
    private javax.swing.JTextField jTextVersion;
    // End of variables declaration//GEN-END:variables
    
}
