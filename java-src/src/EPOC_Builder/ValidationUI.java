/*******************************************************************************
 * ValidationUI.java
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

/*******************************************************************************
 * GUI for output from JRI Validation output/error/messaging.
 *
 * @author Troy Robertson
 * @company Australian Antarctic Division
 * @created 14/04/2010
 * @version 0.4.0, 01-09-2010
 ******************************************************************************/
public class ValidationUI extends OpposingPanelUI {

    /** Creates new form ValidationUI */
    public ValidationUI() {
        initComponents();
    }

    public boolean validate(Universe uni) {
        boolean passed = true;

        if (!validatePreCheck(uni)) return false;

        rex.clear();

        if (!uni.validate(false)) {
            outputLn("              <<< ERROR", "");
            outputLn(Messages.getUnreadErrMsgs(), "\t");
            passed = false;
        } else {
            outputLn("", "");
        }

        if (passed) outputLn("Passed validation.", "\t");

        // Elements
        for (Object obj : uni.getElements(OBJ_ELE)) {
            if (!validate((Element)obj, uni)) passed = false;
        }

        return passed;
    }

    public boolean validate(Element ele, Universe uni) {
        boolean passed = true;

        if (!validatePreCheck(ele)) return false;
       
        rex.clear();

        if (!ele.validate(uni, false)) {
            outputLn("              <<< ERROR", "");
            outputLn(Messages.getUnreadErrMsgs(), "\t");
            passed = false;
        } else {
            outputLn("", "");
        }

        if (passed) outputLn("Passed validation.", "\t");

        // Attributes
        for (Attribute att : ele.getAttributes()) {
            if (!validate(att, ele, uni)) passed = false;
        }

        // Validate Actions
        for (Action act : ele.getActions()) {
            if (!validate(act, ele, uni)) passed = false;
        }

        return passed;
    }

    public boolean validate(Attribute att, Element ele, Universe uni) {
        boolean passed = true;

        if (!validatePreCheck(att)) return false;

        rex.clear();
        
        if (!att.validate(ele)) {
            outputLn("              <<< ERROR", "");
            outputLn(Messages.getUnreadErrMsgs(), "\t");
            passed = false;
        } else {
            outputLn("", "");
        }

        if (passed) outputLn("Passed validation.", "\t");
        return passed;
    }

    public boolean validate(Action act, Element ele, Universe uni) {
        boolean passed = true;

        if (!validatePreCheck(act)) return false;
        
        rex.clear();

        if (!act.validate(ele, uni)) {
            outputLn("              <<< ERROR", "");
            outputLn(Messages.getUnreadErrMsgs(), "\t");
            passed = false;
        } else {
            outputLn("", "");
        }

        if (passed) outputLn("Passed validation.", "\t");
        return passed;
    }

    private boolean validatePreCheck(EPOCObject eo) {
        String nameText = "";

        if (rex == null) {
            outputLn("Failed to create R Engine.", "");
            return false;
        }

        if (eo == null) {
            outputLn("No EPOC object submitted for validation.", "");
            return false;
        }

        nameText = eo.getObjectTypeName() + ": " + eo.getDisplayName() + ":";
        output(nameText);

        return true;
    }

    public void clear() {
        jTextAreaValidation.setText("");
    }

    public void outputLn(String text, String prependStr) {
        String[] lines = text.split("\n");
        for (int i = 0 ; i < lines.length ; i++) {
            output(prependStr + lines[i] + "\n");
        }
    }

    public void output(String text) {
        jTextAreaValidation.setText(jTextAreaValidation.getText() + text);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextAreaValidation = new javax.swing.JTextArea();
        jLabel1 = new javax.swing.JLabel();

        jTextAreaValidation.setColumns(20);
        jTextAreaValidation.setFont(new java.awt.Font("Tahoma", 0, 11));
        jTextAreaValidation.setRows(5);
        jTextAreaValidation.setTabSize(3);
        jScrollPane1.setViewportView(jTextAreaValidation);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 580, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE)
        );

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel1.setText("Validation:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 600, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addGap(9, 9, 9)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addContainerGap()))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jLabel1)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addContainerGap()))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextAreaValidation;
    // End of variables declaration//GEN-END:variables

}
