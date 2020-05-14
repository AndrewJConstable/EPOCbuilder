/*******************************************************************************
 * SpatialUI.java
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

import java.util.Vector;
import java.awt.Font;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/*******************************************************************************
 * GUI for Spatial object.
 * Allows display and data entry for Spatial objects.
 *
 * @author Troy Robertson
 * @company Australian Antarctic Division
 * @created 14/04/2010
 * @version 0.4.0, 01-09-2010
 ******************************************************************************/
public class SpatialUI extends OpposingPanelUI {

    private static Universe universe;
    private Spatial spatial;
    private Vector modifiedOverlapsVector = null;
    private OpposingPanelUI opui;

    /** Creates new form AttributeUI_OLD */
    public SpatialUI(Spatial spa, Universe uni) {
        universe = uni;
        spatial = spa;
        modifiedOverlapsVector = spatial.getOverlapsVectorClone();

        initComponents();
        loadForm();
        loadPolygons();
        jTextShortName.requestFocus();
    }
    
    /*
     * Set editability of form
     */
    public void editable(boolean editable) {
        super.editable(editable);
        jTextShortName.setEditable(editable);
        jTextName.setEditable(editable);
        jTextID.setEditable(editable);
        jTextAreaDesc.setEditable(editable);
        jTablePolygons.setEnabled(editable);
        jButtonAddPolygon.setEnabled(editable);
        jButtonDeletePolygon.setEnabled(editable);
    }
    
    private void loadForm() {
        jTextShortName.setText(spatial.getShortName());
        // bold templates
        if (spatial.isTemplate()) {
            jTextShortName.setFont(jTextShortName.getFont().deriveFont(Font.BOLD));
        } else {
            jTextShortName.setFont(jTextShortName.getFont().deriveFont(Font.PLAIN));
        }
        jTextName.setText(spatial.getName());
        jTextCreated.setText(spatial.getFormattedCreated());
        jTextModified.setText(spatial.getFormattedModified());
        jTextID.setText(spatial.getEPOCID());
        jTextVersion.setText(spatial.getRevision());
        jTextAreaDesc.setText(spatial.getDescription());
    }

    private void loadPolygons() {
        Object[] columnNames = {"Name", "Area (km2)", "Coordinates", "Coord Area (km2)", "Coord Proportions"};

        jTablePolygons.setModel(new DefaultTableModel(spatial.getPolygons2D(), columnNames));
    }

    public int saveIfModified() {
        // Check if form data has been modified, if so update object
        if (isModified()) {
            // Check if required fields are filled adequately
            if (!EPOCObject.testName(jTextShortName.getText())) {
                JOptionPane.showMessageDialog(this, "Please provide a shortname for spatial first!\n\n" +
                         "Spatial must be named and may only contain\n" +
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

            // Check all polygons have been named at least
            boolean allNamed = true;
            Vector rows = ((DefaultTableModel)jTablePolygons.getModel()).getDataVector();
            for (Object row : rows) {
                if (((Vector)row).get(0) == null || ((Vector)row).get(0).equals("")) {
                    allNamed = false;
                    break;
                }
            }
            if (!allNamed) {
                JOptionPane.showMessageDialog(this, "All polygons must be named!");
                return EPOC_FAIL;
            }

            // update modified
            spatial.setModifiedNow();
            spatial.setShortName(jTextShortName.getText());
            spatial.setName(jTextName.getText());
            spatial.setEPOCID(jTextID.getText());
            spatial.setDescription(jTextAreaDesc.getText());
            spatial.setPolygonsVector(((DefaultTableModel)jTablePolygons.getModel()).getDataVector());
            spatial.setOverlapsVector(modifiedOverlapsVector);

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
        if (super.isModified()) return true;    // This will be set by timestep edits
        if (!spatial.getShortName().equals(jTextShortName.getText())) return true;
        if (!spatial.getName().equals(jTextName.getText())) return true;
        if (!spatial.getEPOCID().equals(jTextID.getText())) return true;
        if (!spatial.getDescription().equals(jTextAreaDesc.getText())) return true;
        if (jTablePolygons.getCellEditor() != null) jTablePolygons.getCellEditor().stopCellEditing();
        if (!spatial.getPolygonsString().equals(Spatial.vectorToString(((DefaultTableModel)jTablePolygons.getModel()).getDataVector()))) return true;
        if (!Spatial.vectorToString(spatial.getOverlapsVector()).equals(Spatial.vectorToString(modifiedOverlapsVector))) return true;

        return false;
    }

    @Override
    public EPOCObject getObject() {
        return spatial;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextAreaDesc = new javax.swing.JTextArea();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jTextVersion = new javax.swing.JTextField();
        jTextModified = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jTextCreated = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jTextID = new javax.swing.JTextField();
        jButtonAddPolygon = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTablePolygons = new javax.swing.JTable();
        jButtonDeletePolygon = new javax.swing.JButton();
        jButtonOverlap = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jTextShortName = new javax.swing.JTextField();
        jTextName = new javax.swing.JTextField();

        setPreferredSize(new java.awt.Dimension(600, 450));

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel2.setText("Polygons:");

        jTextAreaDesc.setColumns(20);
        jTextAreaDesc.setFont(new java.awt.Font("Tahoma", 0, 11));
        jTextAreaDesc.setRows(4);
        jScrollPane1.setViewportView(jTextAreaDesc);

        jLabel3.setText("Description:");

        jLabel4.setText("Version:");

        jTextVersion.setEditable(false);
        jTextVersion.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jTextVersion.setPreferredSize(new java.awt.Dimension(200, 18));

        jTextModified.setBackground(new java.awt.Color(212, 208, 200));
        jTextModified.setEditable(false);
        jTextModified.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jTextModified.setPreferredSize(new java.awt.Dimension(200, 18));

        jLabel9.setText("Modified:");

        jTextCreated.setBackground(new java.awt.Color(212, 208, 200));
        jTextCreated.setEditable(false);
        jTextCreated.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jTextCreated.setPreferredSize(new java.awt.Dimension(200, 18));

        jLabel8.setText("Created:");

        jLabel10.setText("EPOC ID:");

        jTextID.setPreferredSize(new java.awt.Dimension(200, 18));

        jButtonAddPolygon.setForeground(new java.awt.Color(0, 0, 255));
        jButtonAddPolygon.setText("Add");
        jButtonAddPolygon.setAlignmentX(0.5F);
        jButtonAddPolygon.setMargin(new java.awt.Insets(2, 10, 2, 10));
        jButtonAddPolygon.setMaximumSize(new java.awt.Dimension(55, 20));
        jButtonAddPolygon.setMinimumSize(new java.awt.Dimension(55, 20));
        jButtonAddPolygon.setPreferredSize(new java.awt.Dimension(55, 20));
        jButtonAddPolygon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddPolygonActionPerformed(evt);
            }
        });

        jTablePolygons.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane2.setViewportView(jTablePolygons);

        jButtonDeletePolygon.setForeground(new java.awt.Color(0, 0, 255));
        jButtonDeletePolygon.setText("Delete");
        jButtonDeletePolygon.setAlignmentX(0.5F);
        jButtonDeletePolygon.setMargin(new java.awt.Insets(2, 10, 2, 10));
        jButtonDeletePolygon.setMaximumSize(new java.awt.Dimension(55, 20));
        jButtonDeletePolygon.setMinimumSize(new java.awt.Dimension(55, 20));
        jButtonDeletePolygon.setPreferredSize(new java.awt.Dimension(55, 20));
        jButtonDeletePolygon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDeletePolygonActionPerformed(evt);
            }
        });

        jButtonOverlap.setForeground(new java.awt.Color(0, 0, 255));
        jButtonOverlap.setText("Overlaps");
        jButtonOverlap.setAlignmentX(0.5F);
        jButtonOverlap.setMargin(new java.awt.Insets(2, 5, 2, 5));
        jButtonOverlap.setMaximumSize(new java.awt.Dimension(55, 20));
        jButtonOverlap.setMinimumSize(new java.awt.Dimension(55, 20));
        jButtonOverlap.setPreferredSize(new java.awt.Dimension(55, 20));
        jButtonOverlap.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOverlapActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jLabel10)
                        .add(26, 26, 26)
                        .add(jTextID, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 150, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel8)
                            .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                .add(jPanel1Layout.createSequentialGroup()
                                    .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                        .add(jLabel4)
                                        .add(jLabel3)
                                        .add(jLabel2))
                                    .add(3, 3, 3))
                                .add(jButtonAddPolygon, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 60, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(jButtonDeletePolygon, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 60, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(jButtonOverlap, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 60, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(11, 11, 11)
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 485, Short.MAX_VALUE)
                            .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 485, Short.MAX_VALUE)
                            .add(jTextVersion, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 150, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jPanel1Layout.createSequentialGroup()
                                .add(jTextCreated, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 150, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 123, Short.MAX_VALUE)
                                .add(jLabel9)
                                .add(18, 18, 18)
                                .add(jTextModified, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 150, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel4)
                    .add(jTextVersion, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jTextCreated, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel8)
                    .add(jTextModified, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel9))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jTextID, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel10))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel3)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jLabel2)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(jButtonAddPolygon, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jButtonDeletePolygon, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jButtonOverlap, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 236, Short.MAX_VALUE))
                .addContainerGap())
        );

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel1.setText("Spatial:");

        jTextShortName.setColumns(20);
        jTextShortName.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        jTextShortName.setToolTipText("Short Name");

        jTextName.setColumns(100);
        jTextName.setToolTipText("Full Name");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(19, 19, 19)
                        .add(jLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 55, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(19, 19, 19)
                        .add(jTextShortName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 150, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jTextName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 341, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jTextShortName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1)
                    .add(jTextName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonAddPolygonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddPolygonActionPerformed
        int selected = 0;

        if ((selected = jTablePolygons.getSelectedRow()) >= 0) {
            ((DefaultTableModel)jTablePolygons.getModel()).insertRow(jTablePolygons.getSelectedRow(), new Vector());
            jTablePolygons.getSelectionModel().setSelectionInterval(selected, selected);
        } else {
            ((DefaultTableModel)jTablePolygons.getModel()).addRow(new Vector());
            selected = ((DefaultTableModel)jTablePolygons.getModel()).getRowCount() - 1;
        }
        // insert into overlaps
        for (Object row : modifiedOverlapsVector) {
            ((Vector)row).insertElementAt(Boolean.FALSE, selected);         // each rows column
        }
        Vector newRow = new Vector(modifiedOverlapsVector.size() + 1);      // make new row
        for (int i = 0 ; i < modifiedOverlapsVector.size() + 1 ; i++) {
            newRow.add(Boolean.FALSE);
        }
        modifiedOverlapsVector.insertElementAt(newRow, selected);    // add row
}//GEN-LAST:event_jButtonAddPolygonActionPerformed

    private void jButtonDeletePolygonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDeletePolygonActionPerformed
        int selected = 0, next = -1;

        if ((selected = jTablePolygons.getSelectedRow()) >= 0) {
            if (selected < jTablePolygons.getRowCount() + 1) {
                next = selected;
            } else if (selected > 0) {
                next = selected - 1;
            }

            ((DefaultTableModel)jTablePolygons.getModel()).removeRow(selected);
            jTablePolygons.getSelectionModel().setSelectionInterval(next, next);
            // and remove from overlaps
            modifiedOverlapsVector.remove(selected);    // row
            for (Object row : modifiedOverlapsVector) {
                ((Vector)row).remove(selected);         // each rows column
            }
        }
    }//GEN-LAST:event_jButtonDeletePolygonActionPerformed

    private void jButtonOverlapActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOverlapActionPerformed
       
        if (jTablePolygons.getRowCount() <= 1) {
            JOptionPane.showMessageDialog(this, "More than 1 polygon must be entered first!");
            return;
        }
        // Check all polygons have been named at least
        boolean allNamed = true;
        Vector rows = ((DefaultTableModel)jTablePolygons.getModel()).getDataVector();
        for (Object row : rows) {
            if (((Vector)row).get(0) == null || ((Vector)row).get(0).equals("")) {
                allNamed = false;
                break;
            }
        }
        if (!allNamed) {
            JOptionPane.showMessageDialog(this, "All polygons must be named!");
            return;
        }

        opui = new PolygonOverlapUI(modifiedOverlapsVector, ((DefaultTableModel)jTablePolygons.getModel()).getDataVector());
        opui.editable(isEditable());
        final JOptionPane pane = new JOptionPane(opui, JOptionPane.PLAIN_MESSAGE,
                JOptionPane.CLOSED_OPTION);
        
        final JDialog dialog = new JDialog((JFrame)SwingUtilities.getRoot(this).getParent(), true);
        dialog.setUndecorated(true);
        dialog.setContentPane(pane);
        dialog.setDefaultCloseOperation(dialog.DO_NOTHING_ON_CLOSE);
        // Hack to differentiate between PolygonOverlapUI instantiated by SpatialUI
        // dialog (ie via TemplateUI View) and directly as an OpposingPanelUI
        if (getDialogLocation() != null) {
            dialog.setLocation(getDialogLocation());
        } else {
            dialog.setLocation(this.getLocationOnScreen());
        }
        if (getDialogSize() != null) {
            dialog.setPreferredSize(getDialogSize());
        } else {
            dialog.setPreferredSize(this.getSize());
        }
        dialog.addWindowListener((PolygonOverlapUI)opui);
        pane.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                if (e.getPropertyName().equals(JOptionPane.VALUE_PROPERTY)
                        && e.getNewValue() != JOptionPane.UNINITIALIZED_VALUE) {
                    if (dialog.isVisible() && ((PolygonOverlapUI)pane.getMessage()).saveIfModified() != EPOC_FAIL) {
                        dialog.setVisible(false);
                    } else {
                        pane.setValue(JOptionPane.UNINITIALIZED_VALUE);
                    }
                }
            }
        });
        dialog.pack();
        dialog.setVisible(true);
    }//GEN-LAST:event_jButtonOverlapActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonAddPolygon;
    private javax.swing.JButton jButtonDeletePolygon;
    private javax.swing.JButton jButtonOverlap;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable jTablePolygons;
    private javax.swing.JTextArea jTextAreaDesc;
    private javax.swing.JTextField jTextCreated;
    private javax.swing.JTextField jTextID;
    private javax.swing.JTextField jTextModified;
    private javax.swing.JTextField jTextName;
    private javax.swing.JTextField jTextShortName;
    private javax.swing.JTextField jTextVersion;
    // End of variables declaration//GEN-END:variables
    
}
