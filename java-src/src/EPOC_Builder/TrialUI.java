/*******************************************************************************
 * TrialUI.java
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
import java.awt.Font;

/*******************************************************************************
 * GUI for Trial object.
 * Allows display and data entry for Trial objects.
 *
 * @author Troy Robertson
 * @company Australian Antarctic Division
 * @created 14/04/2010
 * @version 0.4.0, 01-09-2010
 ******************************************************************************/
public class TrialUI extends OpposingPanelUI {

    private static Universe universe;
    private Trial trial;

    /** Creates new form AttributeUI_OLD */
    public TrialUI(Trial tri, Universe uni) {
        initComponents();

        universe = uni;
        trial = tri;

        loadForm();
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
        jTextTrialDir.setEditable(editable);
        jTextTrialStart.setEditable(editable);
        jTextTrialEnd.setEditable(editable);
        jTextFishingStart.setEditable(editable);
        jTextFishingEnd.setEditable(editable);
    }
    
    private void loadForm() {
        jTextShortName.setText(trial.getShortName());
        // bold templates
        if (trial.isTemplate()) {
            jTextShortName.setFont(jTextShortName.getFont().deriveFont(Font.BOLD));
        } else {
            jTextShortName.setFont(jTextShortName.getFont().deriveFont(Font.PLAIN));
        }
        jTextName.setText(trial.getName());
        jTextVersion.setText(trial.getRevision());
        jTextCreated.setText(trial.getFormattedCreated());
        jTextModified.setText(trial.getFormattedModified());
        jTextID.setText(trial.getEPOCID());
        jTextAreaDesc.setText(trial.getDescription());
        jTextTrialDir.setText(trial.getTrialDir());
        jTextTrialStart.setText(trial.getYearStart());
        jTextTrialEnd.setText(trial.getYearEnd());
        jTextFishingStart.setText(trial.getFishingStart());
        jTextFishingEnd.setText(trial.getFishingEnd());
    }
    
    public int saveIfModified() {

        // Check if form data has been modified, if so update object
        if (isModified()) {
            // Check if required fields are filled adequately
            if (!EPOCObject.testName(jTextShortName.getText())) {
                JOptionPane.showMessageDialog(this, "Please provide a shortname for trial first!\n\n" +
                         "Trial must be named and may only contain\n" +
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
            
            int ts = 0, te = 0, fs = 0, fe = 0;
            try {
                ts = Integer.parseInt(jTextTrialStart.getText());
                te = Integer.parseInt(jTextTrialEnd.getText());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Trial must contain valid start and end years.");
                return EPOC_FAIL;
            }

            try {
                fs = Integer.parseInt(jTextFishingStart.getText());
                fe = Integer.parseInt(jTextFishingEnd.getText());
            } catch (NumberFormatException e) {
                if (!jTextFishingStart.getText().equals("") || !jTextFishingEnd.getText().equals("")) {
                    JOptionPane.showMessageDialog(this, "Trial must contain valid start and end fishing years.");
                    return EPOC_FAIL;
                }
            }

            if (ts <= 0 || ts > 9999 || te <= 0 || te > 9999) {
                JOptionPane.showMessageDialog(this, "Trial must contain valid start and end years.");
                return EPOC_FAIL;
            }

            if (te <= ts) {
                JOptionPane.showMessageDialog(this, "Trial must start before it ends.");
                return EPOC_FAIL;
            }

            if (!jTextFishingStart.getText().equals("") || !jTextFishingEnd.getText().equals("")) {
                if (fs <= 0 || fs > 9999 || fe <= 0 || fe > 9999) {
                    JOptionPane.showMessageDialog(this, "Trial must contain valid start and end fishing years.");
                    return EPOC_FAIL;
                }

                if (fs < ts || fs > te || fe < ts || fe > te) {
                    JOptionPane.showMessageDialog(this, "Fishing start and end years must be within trial period.");
                    return EPOC_FAIL;
                }

                if (fe <= fs) {
                    JOptionPane.showMessageDialog(this, "Fishing must start before it ends.");
                    return EPOC_FAIL;
                }
            }

            // update modified
            trial.setModifiedNow();
            trial.setShortName(jTextShortName.getText());
            trial.setName(jTextName.getText());
            trial.setEPOCID(jTextID.getText());
            trial.setDescription(jTextAreaDesc.getText());
            trial.setTrialDir(jTextTrialDir.getText());
            trial.setYearStart(jTextTrialStart.getText());
            trial.setYearEnd(jTextTrialEnd.getText());
            trial.setFishingStart(jTextFishingStart.getText());
            trial.setFishingEnd(jTextFishingEnd.getText());

            setModified(false);
            return EPOC_SUCC;
        }
        
        return EPOC_NONE;
    }

    /**
     * Have form values been modified when compared to stored object data
     * @return boolean
     */
    public boolean isModified() {
        if (super.isModified()) return true;    // This will be set by timestep edits
        if (!trial.getShortName().equals(jTextShortName.getText())) return true;
        if (!trial.getName().equals(jTextName.getText())) return true;
        if (!trial.getEPOCID().equals(jTextID.getText())) return true;
        if (!trial.getDescription().equals(jTextAreaDesc.getText())) return true;
        if (!trial.getTrialDir().equals(jTextTrialDir.getText())) return true;
        if (!trial.getYearStart().equals(jTextTrialStart.getText())) return true;
        if (!trial.getYearEnd().equals(jTextTrialEnd.getText())) return true;
        if (!trial.getFishingStart().equals(jTextFishingStart.getText())) return true;
        if (!trial.getFishingEnd().equals(jTextFishingEnd.getText())) return true;

        return false;
    }

    public EPOCObject getObject() {
        return trial;
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
        jTextTrialStart = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jTextTrialEnd = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jTextFishingStart = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jTextFishingEnd = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        jTextTrialDir = new javax.swing.JTextField();
        jTextModified = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        jTextCreated = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jTextID = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jTextShortName = new javax.swing.JTextField();
        jTextName = new javax.swing.JTextField();

        setPreferredSize(new java.awt.Dimension(600, 450));

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel2.setText("Trial:");

        jTextAreaDesc.setColumns(20);
        jTextAreaDesc.setFont(new java.awt.Font("Tahoma", 0, 11));
        jTextAreaDesc.setRows(4);
        jScrollPane1.setViewportView(jTextAreaDesc);

        jLabel3.setText("Description:");

        jLabel4.setText("Version:");

        jTextVersion.setEditable(false);
        jTextVersion.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jTextVersion.setPreferredSize(new java.awt.Dimension(200, 18));

        jLabel5.setText("End Year:");

        jLabel6.setText("Start Year:");

        jLabel7.setText("Fishing:");

        jLabel8.setText("Start Year:");

        jLabel9.setText("End Year:");

        jLabel10.setText("Trial Dir:");

        jTextModified.setBackground(new java.awt.Color(212, 208, 200));
        jTextModified.setEditable(false);
        jTextModified.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jTextModified.setPreferredSize(new java.awt.Dimension(200, 18));

        jLabel13.setText("Modified:");

        jTextCreated.setBackground(new java.awt.Color(212, 208, 200));
        jTextCreated.setEditable(false);
        jTextCreated.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jTextCreated.setPreferredSize(new java.awt.Dimension(200, 18));

        jLabel14.setText("Created:");

        jLabel11.setText("EPOC ID:");

        jTextID.setPreferredSize(new java.awt.Dimension(200, 18));

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel7)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel4)
                            .add(jLabel3)
                            .add(jLabel10)
                            .add(jLabel2))
                        .add(14, 14, 14)
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 487, Short.MAX_VALUE)
                            .add(jTextVersion, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 150, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jTextTrialDir, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 487, Short.MAX_VALUE)
                            .add(jPanel1Layout.createSequentialGroup()
                                .add(10, 10, 10)
                                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                    .add(jPanel1Layout.createSequentialGroup()
                                        .add(jLabel8)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(jTextFishingStart, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 60, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .add(42, 42, 42)
                                        .add(jLabel9)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(jTextFishingEnd, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 62, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                    .add(jPanel1Layout.createSequentialGroup()
                                        .add(jLabel6)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(jTextTrialStart, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 60, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .add(42, 42, 42)
                                        .add(jLabel5)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(jTextTrialEnd, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 62, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jLabel14)
                        .add(28, 28, 28)
                        .add(jTextCreated, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 150, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 125, Short.MAX_VALUE)
                        .add(jLabel13)
                        .add(18, 18, 18)
                        .add(jTextModified, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 150, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jLabel11)
                        .add(26, 26, 26)
                        .add(jTextID, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 150, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
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
                    .add(jLabel14)
                    .add(jTextModified, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel13))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jTextID, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel11))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel3)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jTextTrialDir, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel10))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(jTextTrialEnd, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel5)
                    .add(jLabel6)
                    .add(jTextTrialStart, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel7)
                    .add(jTextFishingEnd, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel9)
                    .add(jLabel8)
                    .add(jTextFishingStart, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(175, Short.MAX_VALUE))
        );

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel1.setText("Trial:");

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
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(19, 19, 19)
                        .add(jLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 55, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(19, 19, 19)
                        .add(jTextShortName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 150, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jTextName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 343, Short.MAX_VALUE)))
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
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextAreaDesc;
    private javax.swing.JTextField jTextCreated;
    private javax.swing.JTextField jTextFishingEnd;
    private javax.swing.JTextField jTextFishingStart;
    private javax.swing.JTextField jTextID;
    private javax.swing.JTextField jTextModified;
    private javax.swing.JTextField jTextName;
    private javax.swing.JTextField jTextShortName;
    private javax.swing.JTextField jTextTrialDir;
    private javax.swing.JTextField jTextTrialEnd;
    private javax.swing.JTextField jTextTrialStart;
    private javax.swing.JTextField jTextVersion;
    // End of variables declaration//GEN-END:variables
    
}
