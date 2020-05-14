/*******************************************************************************
 * RJEditor.java
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
package au.gov.aad.erm.RJEditor;

import javax.swing.UIManager;

/*******************************************************************************
 * Main GUI class containing EmbeddedRJEditor
 *
 * @author Troy Robertson
 * @company Australian Antarctic Division
 * @created 10/09/2009
 * @version 0.4.0, 01-09-2010
 *******************************************************************************/
public class RJEditor extends javax.swing.JFrame {

    EmbeddedRJEditor erje = null;
    static boolean standalone = false;

    /** Creates new form RJEditor */
    public RJEditor() {
        this(false);
    }

    /** Creates new form RJEditor */
    public RJEditor(boolean emb) {
        erje = new EmbeddedRJEditor(emb);
        initComponents();
        jPanelMain.add(erje);

        setTitle("Editor");
        setVisible(true);
        erje.setFocus();
    }

    public EmbeddedRJEditor getEditor() {
        return erje;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanelMain = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jPanelMain.setLayout(new javax.swing.BoxLayout(jPanelMain, javax.swing.BoxLayout.Y_AXIS));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanelMain, javax.swing.GroupLayout.DEFAULT_SIZE, 686, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanelMain, javax.swing.GroupLayout.DEFAULT_SIZE, 654, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        if (erje.exit()) {
            if (standalone) {
                System.exit(1);
            } else {
                dispose();
            }
        }
    }//GEN-LAST:event_formWindowClosing

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                standalone = true;
                RJEditor rje = new RJEditor();
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanelMain;
    // End of variables declaration//GEN-END:variables

}