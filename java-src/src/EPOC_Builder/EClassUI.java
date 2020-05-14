/*******************************************************************************
 * EClassUI.java
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
import java.awt.Font;

/*******************************************************************************
 * GUI for Attribute object.
 * Allows display and data entry for Attribute objects.
 *
 * @author Troy Robertson
 * @company Australian Antarctic Division
 * @created 10/09/2009
 * @version 0.4.0, 01-09-2010
 ******************************************************************************/
public class EClassUI extends OpposingPanelUI {

    private static Universe universe;
    private EClass eclass;
    EmbeddedRJEditor rjeInit = new EmbeddedRJEditor(true);;
    EmbeddedRJEditor rjeInitTrial = new EmbeddedRJEditor(true);;
    EmbeddedRJEditor rjeInitTrans = new EmbeddedRJEditor(true);;
    EmbeddedRJEditor rjePrState = new EmbeddedRJEditor(true);;
    EmbeddedRJEditor rjeUpState = new EmbeddedRJEditor(true);;

    public EClassUI(EClass ec, Universe uni) {
        universe = uni;
        eclass = ec;

        initComponents();
        //rjeInit = new EmbeddedRJEditor(true);
        jPanelEdInit.add(rjeInit);
        jPanelEdInitTrial.add(rjeInitTrial);
        jPanelEdInitTrans.add(rjeInitTrans);
        jPanelEdPrState.add(rjePrState);
        jPanelEdUpState.add(rjeUpState);
        editable(true);

        loadForm();
        requestFocus();
        jTextShortName.requestFocus();
    }
    
    /*
     * Set editability of form
     */
    @Override
    public void editable(boolean editable) {
        super.editable(editable);
        jComboModType.setEnabled(editable);
        jTextShortName.setEditable(editable);
        jTextAreaDesc.setEditable(editable);
        jButtonParse.setEnabled(editable && rex.hasEngine());
        rjeInit.editable(editable);
        rjeInitTrial.editable(editable);
        rjeInitTrans.editable(editable);
        rjePrState.editable(editable);
        rjeUpState.editable(editable);
    }
    
    private void loadForm() {
        int selIdx = eclass.getModType();
        if (eclass.getModType() >= OBJ_BIO) selIdx = eclass.getModType() - (OBJ_BIO - 1);
        jComboModType.setSelectedIndex(selIdx);
        jTextShortName.setText(eclass.getShortName());
        // bold templates
        if (eclass.isTemplate()) {
            jTextShortName.setFont(jTextShortName.getFont().deriveFont(Font.BOLD));
        } else {
            jTextShortName.setFont(jTextShortName.getFont().deriveFont(Font.PLAIN));
        }
        jTextVersion.setText(eclass.getRevision());
        jTextModified.setText(eclass.getFormattedModified());
        jTextAreaDesc.setText(eclass.getDescription());
        rjeInit.setText(eclass.getInitClass());
        rjeInitTrial.setText(eclass.getInitTrial());
        rjeInitTrans.setText(eclass.getInitTransition());
        rjePrState.setText(eclass.getPrintState());
        rjeUpState.setText(eclass.getUpdateState());
    }
    
    public int saveIfModified() {
        // Check if form data has been modified, if so update object
        if (isModified()) {
            setWasModified(true);

            // Check if required fields are filled adequately
            if (!EPOCObject.testName(jTextShortName.getText())) {
                JOptionPane.showMessageDialog(null, "Please provide a shortname for EPOC Class first!\n\n" +
                     "EPOC Class must be named and may only contain\n" +
                     "alphanumerics, '.' or '_'\n" +
                     "It may only start with an alphabetic character\n" +
                     "and may not end in '.' or '_'");
                return EPOC_FAIL;
            }

            if (jTextAreaDesc.getText().length() > 512) {
                JOptionPane.showMessageDialog(this, "The description field is limited to only 512 characters!");
                return EPOC_FAIL;
            }

            eclass.setModType((jComboModType.getSelectedIndex() > 0 ? jComboModType.getSelectedIndex() + (OBJ_BIO - 1) : OBJ_ELE));
            eclass.setShortName(jTextShortName.getText());
            eclass.setModifiedNow();
            eclass.setDescription(jTextAreaDesc.getText());
            eclass.setInitClass(rjeInit.getText());
            eclass.setInitTrial(rjeInitTrial.getText());
            eclass.setInitTransition(rjeInitTrans.getText());
            eclass.setPrintState(rjePrState.getText());
            eclass.setUpdateState(rjeUpState.getText());

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
        int mtype = (jComboModType.getSelectedIndex() > 0 ? jComboModType.getSelectedIndex() + (OBJ_BIO - 1) : OBJ_ELE);
        if (eclass.getModType() != mtype) return true;
        if (!eclass.getShortName().equals(jTextShortName.getText())) return true;
        if (!eclass.getDescription().equals(jTextAreaDesc.getText())) return true;
        if (!eclass.getInitClass().equals(rjeInit.getText())) return true;
        if (!eclass.getInitTrial().equals(rjeInitTrial.getText())) return true;
        if (!eclass.getInitTransition().equals(rjeInitTrans.getText())) return true;
        if (!eclass.getPrintState().equals(rjePrState.getText())) return true;
        if (!eclass.getUpdateState().equals(rjeUpState.getText())) return true;

        return false;
    }

    public EPOCObject getObject() {
        return eclass;
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
        jButtonParse = new javax.swing.JButton();
        jTextModified = new javax.swing.JTextField();
        jLabel36 = new javax.swing.JLabel();
        jTabbedPanelMethods = new javax.swing.JTabbedPane();
        jPanelInit = new javax.swing.JPanel();
        jPanelEdInit = new javax.swing.JPanel();
        jPanelActionParams = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jPanelInitTrial = new javax.swing.JPanel();
        jPanelEdInitTrial = new javax.swing.JPanel();
        jPanelActionParams2 = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jPanelInitTransition = new javax.swing.JPanel();
        jPanelEdInitTrans = new javax.swing.JPanel();
        jPanelActionParams3 = new javax.swing.JPanel();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jPanelPrState = new javax.swing.JPanel();
        jPanelEdPrState = new javax.swing.JPanel();
        jPanelActionParams6 = new javax.swing.JPanel();
        jLabel29 = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        jLabel32 = new javax.swing.JLabel();
        jPanelUpState = new javax.swing.JPanel();
        jPanelEdUpState = new javax.swing.JPanel();
        jPanelActionParams5 = new javax.swing.JPanel();
        jLabel26 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jTextShortName = new javax.swing.JTextField();
        jLabel31 = new javax.swing.JLabel();
        jComboModType = new javax.swing.JComboBox();

        setPreferredSize(new java.awt.Dimension(600, 450));

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel1.setPreferredSize(new java.awt.Dimension(580, 420));

        jLabel2.setText("Value:");

        jTextAreaDesc.setFont(new java.awt.Font("Tahoma", 0, 11));
        jTextAreaDesc.setMaximumSize(new java.awt.Dimension(2147483647, 58));
        jScrollPane1.setViewportView(jTextAreaDesc);

        jLabel3.setText("Description:");

        jLabel4.setText("Revision:");

        jTextVersion.setEditable(false);
        jTextVersion.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jTextVersion.setPreferredSize(new java.awt.Dimension(200, 18));

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

        jTextModified.setBackground(new java.awt.Color(212, 208, 200));
        jTextModified.setEditable(false);
        jTextModified.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jTextModified.setPreferredSize(new java.awt.Dimension(200, 18));

        jLabel36.setText("Modified:");

        jPanelEdInit.setBackground(new java.awt.Color(255, 255, 255));
        jPanelEdInit.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelEdInit.setLayout(new javax.swing.BoxLayout(jPanelEdInit, javax.swing.BoxLayout.Y_AXIS));

        jPanelActionParams.setPreferredSize(new java.awt.Dimension(200, 14));

        jLabel9.setText("Parameters:");

        jLabel10.setText(" (.Object,");
        jLabel10.setToolTipText("<html><b>Element object</b></html>");

        jLabel11.setText("universe,");
        jLabel11.setToolTipText("<html><b>Universe object</b></html>");

        jLabel12.setText("dataPath)");
        jLabel12.setToolTipText("<html><b>Path to input data file</b></html>");

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
                .addContainerGap(248, Short.MAX_VALUE))
        );
        jPanelActionParamsLayout.setVerticalGroup(
            jPanelActionParamsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelActionParamsLayout.createSequentialGroup()
                .add(jPanelActionParamsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel9)
                    .add(jLabel10)
                    .add(jLabel11)
                    .add(jLabel12))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout jPanelInitLayout = new org.jdesktop.layout.GroupLayout(jPanelInit);
        jPanelInit.setLayout(jPanelInitLayout);
        jPanelInitLayout.setHorizontalGroup(
            jPanelInitLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelEdInit, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 480, Short.MAX_VALUE)
            .add(jPanelActionParams, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 480, Short.MAX_VALUE)
        );
        jPanelInitLayout.setVerticalGroup(
            jPanelInitLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelInitLayout.createSequentialGroup()
                .addContainerGap()
                .add(jPanelActionParams, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 14, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanelEdInit, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 232, Short.MAX_VALUE))
        );

        jTabbedPanelMethods.addTab("initialize", jPanelInit);

        jPanelEdInitTrial.setBackground(new java.awt.Color(255, 255, 255));
        jPanelEdInitTrial.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelEdInitTrial.setLayout(new javax.swing.BoxLayout(jPanelEdInitTrial, javax.swing.BoxLayout.Y_AXIS));

        jPanelActionParams2.setPreferredSize(new java.awt.Dimension(200, 14));

        jLabel17.setText("Parameters:");

        jLabel18.setText(" (.Object,");
        jLabel18.setToolTipText("<html><b>Element object</b></html>");

        jLabel19.setText("universe)");
        jLabel19.setToolTipText("<html><b>Universe object</b></html>");

        org.jdesktop.layout.GroupLayout jPanelActionParams2Layout = new org.jdesktop.layout.GroupLayout(jPanelActionParams2);
        jPanelActionParams2.setLayout(jPanelActionParams2Layout);
        jPanelActionParams2Layout.setHorizontalGroup(
            jPanelActionParams2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelActionParams2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel17)
                .add(11, 11, 11)
                .add(jLabel18)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel19)
                .addContainerGap(302, Short.MAX_VALUE))
        );
        jPanelActionParams2Layout.setVerticalGroup(
            jPanelActionParams2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelActionParams2Layout.createSequentialGroup()
                .add(jPanelActionParams2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel17)
                    .add(jLabel18)
                    .add(jLabel19))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout jPanelInitTrialLayout = new org.jdesktop.layout.GroupLayout(jPanelInitTrial);
        jPanelInitTrial.setLayout(jPanelInitTrialLayout);
        jPanelInitTrialLayout.setHorizontalGroup(
            jPanelInitTrialLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelActionParams2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 480, Short.MAX_VALUE)
            .add(jPanelEdInitTrial, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 480, Short.MAX_VALUE)
        );
        jPanelInitTrialLayout.setVerticalGroup(
            jPanelInitTrialLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelInitTrialLayout.createSequentialGroup()
                .addContainerGap()
                .add(jPanelActionParams2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 14, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanelEdInitTrial, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 232, Short.MAX_VALUE))
        );

        jTabbedPanelMethods.addTab("initialiseTrial", jPanelInitTrial);

        jPanelEdInitTrans.setBackground(new java.awt.Color(255, 255, 255));
        jPanelEdInitTrans.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelEdInitTrans.setLayout(new javax.swing.BoxLayout(jPanelEdInitTrans, javax.swing.BoxLayout.Y_AXIS));

        jPanelActionParams3.setPreferredSize(new java.awt.Dimension(200, 14));

        jLabel20.setText("Parameters:");

        jLabel21.setText(" (.Object,");
        jLabel21.setToolTipText("<html><b>Element object</b></html>");

        jLabel22.setText("universe)");
        jLabel22.setToolTipText("<html><b>Universe object</b></html>");

        org.jdesktop.layout.GroupLayout jPanelActionParams3Layout = new org.jdesktop.layout.GroupLayout(jPanelActionParams3);
        jPanelActionParams3.setLayout(jPanelActionParams3Layout);
        jPanelActionParams3Layout.setHorizontalGroup(
            jPanelActionParams3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelActionParams3Layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel20)
                .add(11, 11, 11)
                .add(jLabel21)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel22)
                .addContainerGap(302, Short.MAX_VALUE))
        );
        jPanelActionParams3Layout.setVerticalGroup(
            jPanelActionParams3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelActionParams3Layout.createSequentialGroup()
                .add(jPanelActionParams3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel20)
                    .add(jLabel21)
                    .add(jLabel22))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout jPanelInitTransitionLayout = new org.jdesktop.layout.GroupLayout(jPanelInitTransition);
        jPanelInitTransition.setLayout(jPanelInitTransitionLayout);
        jPanelInitTransitionLayout.setHorizontalGroup(
            jPanelInitTransitionLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelActionParams3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 480, Short.MAX_VALUE)
            .add(jPanelEdInitTrans, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 480, Short.MAX_VALUE)
        );
        jPanelInitTransitionLayout.setVerticalGroup(
            jPanelInitTransitionLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelInitTransitionLayout.createSequentialGroup()
                .addContainerGap()
                .add(jPanelActionParams3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 14, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanelEdInitTrans, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 232, Short.MAX_VALUE))
        );

        jTabbedPanelMethods.addTab("initialiseTransition", jPanelInitTransition);

        jPanelEdPrState.setBackground(new java.awt.Color(255, 255, 255));
        jPanelEdPrState.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelEdPrState.setLayout(new javax.swing.BoxLayout(jPanelEdPrState, javax.swing.BoxLayout.Y_AXIS));

        jPanelActionParams6.setPreferredSize(new java.awt.Dimension(200, 14));

        jLabel29.setText("Parameters:");

        jLabel30.setText(" (.Object,");
        jLabel30.setToolTipText("<html><b>Element object</b></html>");

        jLabel32.setText("universe)");
        jLabel32.setToolTipText("<html><b>Universe object</b></html>");

        org.jdesktop.layout.GroupLayout jPanelActionParams6Layout = new org.jdesktop.layout.GroupLayout(jPanelActionParams6);
        jPanelActionParams6.setLayout(jPanelActionParams6Layout);
        jPanelActionParams6Layout.setHorizontalGroup(
            jPanelActionParams6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelActionParams6Layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel29)
                .add(11, 11, 11)
                .add(jLabel30)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel32)
                .addContainerGap(302, Short.MAX_VALUE))
        );
        jPanelActionParams6Layout.setVerticalGroup(
            jPanelActionParams6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelActionParams6Layout.createSequentialGroup()
                .add(jPanelActionParams6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel29)
                    .add(jLabel30)
                    .add(jLabel32))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout jPanelPrStateLayout = new org.jdesktop.layout.GroupLayout(jPanelPrState);
        jPanelPrState.setLayout(jPanelPrStateLayout);
        jPanelPrStateLayout.setHorizontalGroup(
            jPanelPrStateLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelActionParams6, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 480, Short.MAX_VALUE)
            .add(jPanelEdPrState, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 480, Short.MAX_VALUE)
        );
        jPanelPrStateLayout.setVerticalGroup(
            jPanelPrStateLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelPrStateLayout.createSequentialGroup()
                .addContainerGap()
                .add(jPanelActionParams6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 14, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanelEdPrState, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 232, Short.MAX_VALUE))
        );

        jTabbedPanelMethods.addTab("printState", jPanelPrState);

        jPanelEdUpState.setBackground(new java.awt.Color(255, 255, 255));
        jPanelEdUpState.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelEdUpState.setLayout(new javax.swing.BoxLayout(jPanelEdUpState, javax.swing.BoxLayout.Y_AXIS));

        jPanelActionParams5.setPreferredSize(new java.awt.Dimension(200, 14));

        jLabel26.setText("Parameters:");

        jLabel27.setText(" (.Object,");
        jLabel27.setToolTipText("<html><b>Element object</b></html>");

        jLabel28.setText("universe)");
        jLabel28.setToolTipText("<html><b>Universe object</b></html>");

        org.jdesktop.layout.GroupLayout jPanelActionParams5Layout = new org.jdesktop.layout.GroupLayout(jPanelActionParams5);
        jPanelActionParams5.setLayout(jPanelActionParams5Layout);
        jPanelActionParams5Layout.setHorizontalGroup(
            jPanelActionParams5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelActionParams5Layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel26)
                .add(11, 11, 11)
                .add(jLabel27)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel28)
                .addContainerGap(302, Short.MAX_VALUE))
        );
        jPanelActionParams5Layout.setVerticalGroup(
            jPanelActionParams5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelActionParams5Layout.createSequentialGroup()
                .add(jPanelActionParams5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel26)
                    .add(jLabel27)
                    .add(jLabel28))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout jPanelUpStateLayout = new org.jdesktop.layout.GroupLayout(jPanelUpState);
        jPanelUpState.setLayout(jPanelUpStateLayout);
        jPanelUpStateLayout.setHorizontalGroup(
            jPanelUpStateLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelActionParams5, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 480, Short.MAX_VALUE)
            .add(jPanelEdUpState, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 480, Short.MAX_VALUE)
        );
        jPanelUpStateLayout.setVerticalGroup(
            jPanelUpStateLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelUpStateLayout.createSequentialGroup()
                .addContainerGap()
                .add(jPanelActionParams5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 14, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanelEdUpState, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 232, Short.MAX_VALUE))
        );

        jTabbedPanelMethods.addTab("updateState", jPanelUpState);

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel4)
                    .add(jLabel3)
                    .add(jLabel2)
                    .add(jButtonParse, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 60, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(11, 11, 11)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jTabbedPanelMethods, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 485, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 485, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel1Layout.createSequentialGroup()
                        .add(jTextVersion, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 150, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 137, Short.MAX_VALUE)
                        .add(jLabel36)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jTextModified, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 150, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel4)
                    .add(jTextVersion, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jTextModified, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel36))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel3)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 58, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel1Layout.createSequentialGroup()
                        .add(jLabel2)
                        .add(66, 66, 66)
                        .add(jButtonParse, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jTabbedPanelMethods, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 288, Short.MAX_VALUE))
                .addContainerGap())
        );

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel1.setText("EPOC Class:");

        jTextShortName.setColumns(20);
        jTextShortName.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        jTextShortName.setToolTipText("Short Name");

        jLabel31.setText("Inherits from:");

        jComboModType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Element", "Biota", "Environment", "Activity", "Management", "Output", "Presentation" }));
        jComboModType.setMinimumSize(new java.awt.Dimension(53, 18));
        jComboModType.setPreferredSize(new java.awt.Dimension(57, 20));

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
                        .add(jLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 64, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(jTextShortName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 150, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 115, Short.MAX_VALUE)
                        .add(jLabel31)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jComboModType, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 162, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jTextShortName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1)
                    .add(jComboModType, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel31))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 402, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonParseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonParseActionPerformed
        EmbeddedRJEditor rje;
        // Find which tab is open
        int tab = jTabbedPanelMethods.getSelectedIndex();
        if (tab == 0) {
            rje = rjeInit;
        } else if (tab == 1) {
            rje = rjeInitTrial;
        } else if (tab == 2) {
            rje = rjeInitTrans;
        } else if (tab == 3) {
            rje = rjePrState;
        } else if (tab == 4) {
            rje = rjeUpState;
        } else {
            return;
        }

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
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonParse;
    private javax.swing.JComboBox jComboModType;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanelActionParams;
    private javax.swing.JPanel jPanelActionParams2;
    private javax.swing.JPanel jPanelActionParams3;
    private javax.swing.JPanel jPanelActionParams5;
    private javax.swing.JPanel jPanelActionParams6;
    private javax.swing.JPanel jPanelEdInit;
    private javax.swing.JPanel jPanelEdInitTrans;
    private javax.swing.JPanel jPanelEdInitTrial;
    private javax.swing.JPanel jPanelEdPrState;
    private javax.swing.JPanel jPanelEdUpState;
    private javax.swing.JPanel jPanelInit;
    private javax.swing.JPanel jPanelInitTransition;
    private javax.swing.JPanel jPanelInitTrial;
    private javax.swing.JPanel jPanelPrState;
    private javax.swing.JPanel jPanelUpState;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane jTabbedPanelMethods;
    private javax.swing.JTextArea jTextAreaDesc;
    private javax.swing.JTextField jTextModified;
    private javax.swing.JTextField jTextShortName;
    private javax.swing.JTextField jTextVersion;
    // End of variables declaration//GEN-END:variables
    
}
