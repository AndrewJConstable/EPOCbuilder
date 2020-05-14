/*******************************************************************************
 * ReportUI.java
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
 * GUI for Report object.
 * Allows display and data entry for Report objects.
 *
 * @author Troy Robertson
 * @company Australian Antarctic Division
 * @created 14/04/2010
 * @version 0.4.0, 01-09-2010
 ******************************************************************************/
public class ReportUI extends OpposingPanelUI {

    private static Universe universe;
    private Report report;
    private boolean logPrintSelected = false;
    private boolean calendarPrintSelected = false;
    private boolean debugSelected = false;

    /** Creates new form AttributeUI_OLD */
    public ReportUI(Report rep, Universe uni) {
        initComponents();

        universe = uni;
        report = rep;

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
        jCheckBoxLog.setEnabled(editable);
        jTextLogFilename.setEditable(editable);
        jCheckBoxCalendar.setEnabled(editable);
        jTextCalendarFilename.setEditable(editable);
        jTextHeadline1.setEnabled(editable);
        jTextHeadline2.setEnabled(editable);
        jTextHeadline3.setEnabled(editable);
        jTextHeadline4.setEnabled(editable);
        jCheckBoxDebug.setEnabled(editable);
    }
    
    private void loadForm() {
        jTextShortName.setText(report.getShortName());
        // bold templates
        if (report.isTemplate()) {
            jTextShortName.setFont(jTextShortName.getFont().deriveFont(Font.BOLD));
        } else {
            jTextShortName.setFont(jTextShortName.getFont().deriveFont(Font.PLAIN));
        }
        jTextName.setText(report.getName());
        jTextVersion.setText(report.getRevision());
        jTextCreated.setText(report.getFormattedCreated());
        jTextModified.setText(report.getFormattedModified());
        jTextID.setText(report.getEPOCID());
        jTextAreaDesc.setText(report.getDescription());
        jCheckBoxLog.setSelected(report.getLogPrint());
        jTextLogFilename.setText(report.getLogFilename());
        jCheckBoxCalendar.setSelected(report.getCalendarPrint());
        jTextCalendarFilename.setText(report.getCalendarFilename());
        jTextHeadline1.setText(report.getHeadline(1));
        jTextHeadline2.setText(report.getHeadline(2));
        jTextHeadline3.setText(report.getHeadline(3));
        jTextHeadline4.setText(report.getHeadline(4));
        jCheckBoxDebug.setSelected(report.getDebug());
    }
    
    public int saveIfModified() {
        // Check if form data has been modified, if so update object
        if (isModified()) {
            // Check if required fields are filled adequately
            if (!EPOCObject.testName(jTextShortName.getText())) {
                JOptionPane.showMessageDialog(this, "Please provide a shortname for report first!\n\n" +
                         "Report must be named and may only contain\n" +
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

            // update modified
            report.setModifiedNow();
            report.setShortName(jTextShortName.getText());
            report.setName(jTextName.getText());
            report.setEPOCID(jTextID.getText());
            report.setDescription(jTextAreaDesc.getText());
            report.setLogPrint(logPrintSelected);
            report.setLogFilename(jTextLogFilename.getText());
            report.setCalendarPrint(calendarPrintSelected);
            report.setCalendarFilename(jTextCalendarFilename.getText());
            report.setHeadline(1, jTextHeadline1.getText());
            report.setHeadline(2, jTextHeadline2.getText());
            report.setHeadline(3, jTextHeadline3.getText());
            report.setHeadline(4, jTextHeadline4.getText());
            report.setDebug(debugSelected);

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
        if (!report.getShortName().equals(jTextShortName.getText())) return true;
        if (!report.getName().equals(jTextName.getText())) return true;
        if (!report.getEPOCID().equals(jTextID.getText())) return true;
        if (!report.getDescription().equals(jTextAreaDesc.getText())) return true;
        if (report.getLogPrint() != logPrintSelected) return true;
        if (!report.getLogFilename().equals(jTextLogFilename.getText())) return true;
        if (report.getCalendarPrint() != calendarPrintSelected) return true;
        if (!report.getCalendarFilename().equals(jTextCalendarFilename.getText())) return true;
        if (!report.getHeadline(1).equals(jTextHeadline1.getText())) return true;
        if (!report.getHeadline(2).equals(jTextHeadline2.getText())) return true;
        if (!report.getHeadline(3).equals(jTextHeadline3.getText())) return true;
        if (!report.getHeadline(4).equals(jTextHeadline4.getText())) return true;
        if (report.getDebug() != debugSelected) return true;

        return false;
    }

    public EPOCObject getObject() {
        return report;
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
        jCheckBoxLog = new javax.swing.JCheckBox();
        jLabel5 = new javax.swing.JLabel();
        jTextLogFilename = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jCheckBoxCalendar = new javax.swing.JCheckBox();
        jLabel7 = new javax.swing.JLabel();
        jTextCalendarFilename = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jTextHeadline1 = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jTextHeadline2 = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        jTextHeadline3 = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        jTextHeadline4 = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jCheckBoxDebug = new javax.swing.JCheckBox();
        jTextModified = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        jTextCreated = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jTextID = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jTextShortName = new javax.swing.JTextField();
        jTextName = new javax.swing.JTextField();

        setPreferredSize(new java.awt.Dimension(602, 420));

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel2.setText("Log:");

        jTextAreaDesc.setColumns(20);
        jTextAreaDesc.setFont(new java.awt.Font("Tahoma", 0, 11));
        jTextAreaDesc.setRows(4);
        jScrollPane1.setViewportView(jTextAreaDesc);

        jLabel3.setText("Description:");

        jLabel4.setText("Version:");

        jTextVersion.setEditable(false);
        jTextVersion.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jTextVersion.setPreferredSize(new java.awt.Dimension(200, 18));

        jCheckBoxLog.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBoxLogItemStateChanged(evt);
            }
        });

        jLabel5.setText("Filename:");

        jLabel6.setText("Calendar:");

        jCheckBoxCalendar.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBoxCalendarItemStateChanged(evt);
            }
        });

        jLabel7.setText("Filename:");

        jLabel8.setText("Headline 1:");

        jTextHeadline1.setFont(new java.awt.Font("Monospaced", 0, 11));

        jLabel9.setText("Headline 2:");

        jTextHeadline2.setFont(new java.awt.Font("Monospaced", 0, 11));

        jLabel10.setText("Headline 3:");

        jTextHeadline3.setFont(new java.awt.Font("Monospaced", 0, 11));

        jLabel11.setText("Headline 4:");

        jTextHeadline4.setFont(new java.awt.Font("Monospaced", 0, 11));

        jLabel12.setText("Debug:");

        jCheckBoxDebug.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBoxDebugItemStateChanged(evt);
            }
        });

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

        jLabel15.setText("EPOC ID:");

        jTextID.setPreferredSize(new java.awt.Dimension(200, 18));

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel4)
                            .add(jLabel3)
                            .add(jLabel2)
                            .add(jLabel10)
                            .add(jLabel9)
                            .add(jLabel8)
                            .add(jLabel6))
                        .add(14, 14, 14)
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 486, Short.MAX_VALUE)
                            .add(jTextVersion, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 150, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jPanel1Layout.createSequentialGroup()
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(jPanel1Layout.createSequentialGroup()
                                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                            .add(jPanel1Layout.createSequentialGroup()
                                                .add(jCheckBoxLog)
                                                .add(18, 18, 18)
                                                .add(jLabel5))
                                            .add(jPanel1Layout.createSequentialGroup()
                                                .add(jCheckBoxCalendar)
                                                .add(18, 18, 18)
                                                .add(jLabel7)))
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                            .add(jTextCalendarFilename, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 397, Short.MAX_VALUE)
                                            .add(jTextLogFilename, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 397, Short.MAX_VALUE)))
                                    .add(jTextHeadline1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 486, Short.MAX_VALUE)
                                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jTextHeadline2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 486, Short.MAX_VALUE)
                                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jTextHeadline3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 486, Short.MAX_VALUE)
                                    .add(jTextHeadline4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 486, Short.MAX_VALUE)
                                    .add(jCheckBoxDebug)))))
                    .add(jLabel11)
                    .add(jLabel12)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jLabel14)
                        .add(28, 28, 28)
                        .add(jTextCreated, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 150, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 124, Short.MAX_VALUE)
                        .add(jLabel13)
                        .add(18, 18, 18)
                        .add(jTextModified, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 150, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jLabel15)
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
                    .add(jLabel15))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel3)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(jLabel5)
                        .add(jTextLogFilename, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jLabel2)
                    .add(jCheckBoxLog))
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jTextCalendarFilename, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel7)))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(3, 3, 3)
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel6)
                            .add(jCheckBoxCalendar))))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jTextHeadline1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel8))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jTextHeadline2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel9))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jTextHeadline3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel10))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jLabel11)
                    .add(jTextHeadline4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 26, Short.MAX_VALUE)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jLabel12)
                    .add(jCheckBoxDebug))
                .addContainerGap())
        );

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel1.setText("Report:");

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
                        .add(jTextName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 342, Short.MAX_VALUE)))
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

    private void jCheckBoxLogItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCheckBoxLogItemStateChanged
        if (evt.getStateChange() == evt.SELECTED) {
            logPrintSelected = true;
        } else if (evt.getStateChange() == evt.DESELECTED) {
            logPrintSelected = false;
        }
    }//GEN-LAST:event_jCheckBoxLogItemStateChanged

    private void jCheckBoxCalendarItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCheckBoxCalendarItemStateChanged
        if (evt.getStateChange() == evt.SELECTED) {
            calendarPrintSelected = true;
        } else if (evt.getStateChange() == evt.DESELECTED) {
            calendarPrintSelected = false;
        }
    }//GEN-LAST:event_jCheckBoxCalendarItemStateChanged

    private void jCheckBoxDebugItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCheckBoxDebugItemStateChanged
        if (evt.getStateChange() == evt.SELECTED) {
            debugSelected = true;
        } else if (evt.getStateChange() == evt.DESELECTED) {
            debugSelected = false;
        }
    }//GEN-LAST:event_jCheckBoxDebugItemStateChanged
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox jCheckBoxCalendar;
    private javax.swing.JCheckBox jCheckBoxDebug;
    private javax.swing.JCheckBox jCheckBoxLog;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
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
    private javax.swing.JTextField jTextCalendarFilename;
    private javax.swing.JTextField jTextCreated;
    private javax.swing.JTextField jTextHeadline1;
    private javax.swing.JTextField jTextHeadline2;
    private javax.swing.JTextField jTextHeadline3;
    private javax.swing.JTextField jTextHeadline4;
    private javax.swing.JTextField jTextID;
    private javax.swing.JTextField jTextLogFilename;
    private javax.swing.JTextField jTextModified;
    private javax.swing.JTextField jTextName;
    private javax.swing.JTextField jTextShortName;
    private javax.swing.JTextField jTextVersion;
    // End of variables declaration//GEN-END:variables
    
}
