/*******************************************************************************
 * EmbeddedRJEditor.java
 * =============================================================================
 * Acknowledgements to JGR code used to put this together.
 * Credit org.rosuda.JGR.*;
 * =============================================================================
 */
package au.gov.aad.erm.RJEditor;

import javax.swing.JLabel;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import javax.swing.undo.UndoManager;

import java.awt.Font;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import jedit.syntax.FindReplaceDialog;
import jedit.syntax.JEditTextArea;
import jedit.syntax.RTokenMarker;
import jedit.syntax.TextAreaDefaults;

import org.rosuda.JGR.toolkit.JGRPrefs;
import org.rosuda.JGR.editor.RInputHandler;
import org.rosuda.JGR.util.DocumentRenderer;

/*******************************************************************************
 * Embedded R Editor widget.
 * Contains jedit TextArea with R sytax highlighting and Document history
 * control.
 * Acknowledgements to JGR code used to put this together.
 *
 * @author Troy Robertson
 * @company Australian Antarctic Division
 * @created 10/09/2009
 * @version 0.4.0, 01-09-2010
 *******************************************************************************/
public class EmbeddedRJEditor extends javax.swing.JPanel implements ActionListener {

    JEditTextArea ed = null;
    CaretListenerLabel cLabel = new CaretListenerLabel();
    boolean modified = false, embedded = true;
    private String fileName = "", origCode = "";
    JFileChooser fc = new JFileChooser();
    UndoManager undoMgr = new UndoManager();
    TextAreaDefaults defaults = TextAreaDefaults.getDefaults();

    /** Creates new form EmbeddedRJEditor */
    public EmbeddedRJEditor() {
        this(true);
    }

    /** Creates new form EmbeddedRJEditor */
    public EmbeddedRJEditor(boolean emb) {
        embedded = emb;

        initComponents();
        actionButtons();
        insertEditor();
        setFocus();
    }

    /*
     * Set editability of form
     */
    public void editable(boolean editable) {
        ed.setEnabled(editable);
        jButtonOpen.setEnabled(editable);
        jButtonSave.setEnabled(editable);
        jButtonUndo.setEnabled(editable);
        jButtonRedo.setEnabled(editable);
        jButtonCut.setEnabled(editable);
        jButtonCopy.setEnabled(editable);
        jButtonPaste.setEnabled(editable);
        jButtonFind.setEnabled(editable);
    }

    public void setFocus() {
        this.requestFocus();
        ed.requestFocus();
        ed.setCaretPosition(0);
        ed.scrollTo(0,0);
    }

    private void actionButtons() {
        
        jButtonOpen.addActionListener(this);
        jButtonSave.addActionListener(this);
        jButtonUndo.addActionListener(this);
        jButtonRedo.addActionListener(this);
        jButtonCut.addActionListener(this);
        jButtonCopy.addActionListener(this);
        jButtonPaste.addActionListener(this);
        jButtonFind.addActionListener(this);
         
    }

    private void insertEditor() {
        // This is necessary to stop jedit and JGR cracking it about fonts
        // Trouble is that it also changes package directory which causes button icons to
        // be loaded from JGR.jar's icon directory instead of EPOC_Builder's
        //JGRPrefs.initialize();
        JGRPrefs.DefaultFont = new Font("Monospaced", Font.PLAIN, 11);

        // set own UndoManager so that can later have a handle on it
        defaults.undoMgr = undoMgr;
        defaults.document.addUndoableEditListener(defaults.undoMgr);
        ed = new JEditTextArea(defaults);

        ed.getDocument().setTokenMarker(new RTokenMarker());
        RInputHandler rih = new RInputHandler();
        rih.addKeyBindings();
        ed.setInputHandler(rih);
        ed.addCaretListener(cLabel);
        ed.setMinimumSize(new Dimension(100, 50));
        ed.setMaximumSize(new Dimension(2147483647, 2147483647));
        ed.setVisible(true);
        add(ed);
        setVisible(true);
    }

    public void removeUndoManager() {
        defaults.document.removeUndoableEditListener(defaults.undoMgr);
    }

    public void setText(String text) {
        origCode = text;
        ed.setText(text);
        ed.select(0, 0);
        // Now we can discard edits so that can't undo past this setText
        undoMgr.discardAllEdits();
    }

    public void addText(String text) {
        ed.append(text);
    }

    public String getText() {
        return ed.getText();
    }

    private void openFile() {
        openFile(null);
    }

    public void openFile(String file) {

        String newFile = file;

        if (file == null) {
            int returnVal = fc.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                newFile = fc.getSelectedFile().getAbsolutePath();
            } else {
                return;
            }
        }

        ed.loadFile(newFile);
        origCode = ed.getText();
        fileName = newFile;
    }

    private boolean saveFile() {
        
        if (fileName == null || fileName.equals("")) {
            return saveFileAs();
        }
        ed.saveFile(fileName);
        origCode = ed.getText();
        
        return true;
    }

    private boolean saveFileAs() {

        int returnVal = fc.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            fileName = fc.getSelectedFile().getAbsolutePath();
        } else {
            return false;
        }

        ed.saveFile(fileName);
        origCode = ed.getText();

        return true;
    }

    public boolean exit() {
        if (!embedded && !ed.getText().equals(origCode)) {
            int choice = JOptionPane.showConfirmDialog(this, "Save to file?", "Exit", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (choice == JOptionPane.YES_OPTION) {
                return saveFile();
            } else if (choice == JOptionPane.CANCEL_OPTION) {
                return false;
            }
        }

        return true;
    }

    public void print() {
        DocumentRenderer docrender = new DocumentRenderer();
        docrender.print(ed.getDocument());
    }

    public void actionPerformed(ActionEvent e) {

        if ("open".equalsIgnoreCase(e.getActionCommand()))
                openFile();
        if ("save".equalsIgnoreCase(e.getActionCommand()))
                saveFile();
        if ("saveas".equalsIgnoreCase(e.getActionCommand()))
                saveFileAs();

        if ("copy".equalsIgnoreCase(e.getActionCommand()))
                ed.copy();
        if ("cut".equalsIgnoreCase(e.getActionCommand()))
                ed.cut();
        if ("paste".equalsIgnoreCase(e.getActionCommand()))
                ed.paste();

        if ("undo".equalsIgnoreCase(e.getActionCommand()))
                ed.undo();
        if ("redo".equalsIgnoreCase(e.getActionCommand()))
                ed.redo();

        if ("find".equalsIgnoreCase(e.getActionCommand()))
                FindReplaceDialog.findExt((JFrame)SwingUtilities.getRoot(this).getParent(), ed);

        if ("commentcode".equalsIgnoreCase(e.getActionCommand())) {
            if (ed.getSelectedText() != null && ed.getSelectedText().trim().length() > 0)
                try {
                    ed.commentSelection(true);
                } catch (BadLocationException e1) {
                }
        } else if ("uncommentcode".equalsIgnoreCase(e.getActionCommand())) {
            if (ed.getSelectedText() != null && ed.getSelectedText().trim().length() > 0)
                try {
                    ed.commentSelection(false);
                } catch (BadLocationException e1) {
                }
        }

        if ("print".equalsIgnoreCase(e.getActionCommand()))
            print();

        if ("help".equalsIgnoreCase(e.getActionCommand()))
            // TODO

        if ("shiftleft".equalsIgnoreCase(e.getActionCommand()))
            try {
                ed.shiftSelection(-1);
            } catch (BadLocationException e1) {
            }
        else if ("shiftright".equalsIgnoreCase(e.getActionCommand()))
            try {
                ed.shiftSelection(1);
            } catch (BadLocationException e2) {
            }

        if ("quit".equalsIgnoreCase(e.getActionCommand())) {
                //exit();
        }
    }

    protected class CaretListenerLabel extends JLabel implements CaretListener {

        private static final long serialVersionUID = -4451331086216529945L;

        public CaretListenerLabel() {
        }

        public void caretUpdate(CaretEvent e) {
                modified = true;
                //setModified(modified);
                displayInfo(e);
        }

        protected void displayInfo(final CaretEvent e) {
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    try {
                        int currentpos = ed.getCaretPosition();
                        int lastnewline = ed.getText().lastIndexOf("\n", currentpos - 1);
                        int chars = ed.getText(0, lastnewline < 0 ? 0 : lastnewline).length();
                        int currentline = ed.getLineOfOffset(ed.getCaretPosition())+1;
                        currentpos -= chars;
                        setText(currentline + ":" + (currentline == 1 ? currentpos + 1 : currentpos)+"   ");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jToolBarEditor = new javax.swing.JToolBar();
        jButtonOpen = new javax.swing.JButton();
        jButtonSave = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        jButtonUndo = new javax.swing.JButton();
        jButtonRedo = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        jButtonCut = new javax.swing.JButton();
        jButtonCopy = new javax.swing.JButton();
        jButtonPaste = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JToolBar.Separator();
        jButtonFind = new javax.swing.JButton();
        jSeparator4 = new javax.swing.JSeparator();

        setMaximumSize(new java.awt.Dimension(32767, 32767));
        setPreferredSize(new java.awt.Dimension(32767, 32767));
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS));

        jToolBarEditor.setFloatable(false);
        jToolBarEditor.setRollover(true);
        jToolBarEditor.setMaximumSize(new java.awt.Dimension(22822, 25));
        jToolBarEditor.setMinimumSize(new java.awt.Dimension(100, 25));

        jButtonOpen.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/open.png"))); // NOI18N
        jButtonOpen.setToolTipText("Open from file");
        jButtonOpen.setActionCommand("open");
        jButtonOpen.setFocusable(false);
        jButtonOpen.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonOpen.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButtonOpen.setMaximumSize(new java.awt.Dimension(25, 25));
        jButtonOpen.setMinimumSize(new java.awt.Dimension(25, 25));
        jButtonOpen.setPreferredSize(new java.awt.Dimension(25, 25));
        jButtonOpen.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBarEditor.add(jButtonOpen);

        jButtonSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/save.png"))); // NOI18N
        jButtonSave.setToolTipText("Save to file");
        jButtonSave.setActionCommand("save");
        jButtonSave.setFocusable(false);
        jButtonSave.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonSave.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButtonSave.setMaximumSize(new java.awt.Dimension(25, 25));
        jButtonSave.setMinimumSize(new java.awt.Dimension(25, 25));
        jButtonSave.setPreferredSize(new java.awt.Dimension(25, 25));
        jButtonSave.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBarEditor.add(jButtonSave);
        jToolBarEditor.add(jSeparator1);

        jButtonUndo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/undo.png"))); // NOI18N
        jButtonUndo.setToolTipText("Undo");
        jButtonUndo.setActionCommand("undo");
        jButtonUndo.setFocusable(false);
        jButtonUndo.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonUndo.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButtonUndo.setMaximumSize(new java.awt.Dimension(25, 25));
        jButtonUndo.setMinimumSize(new java.awt.Dimension(25, 25));
        jButtonUndo.setPreferredSize(new java.awt.Dimension(25, 25));
        jButtonUndo.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBarEditor.add(jButtonUndo);

        jButtonRedo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/redo.png"))); // NOI18N
        jButtonRedo.setToolTipText("Redo");
        jButtonRedo.setActionCommand("redo");
        jButtonRedo.setFocusable(false);
        jButtonRedo.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonRedo.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButtonRedo.setMaximumSize(new java.awt.Dimension(25, 25));
        jButtonRedo.setMinimumSize(new java.awt.Dimension(25, 25));
        jButtonRedo.setPreferredSize(new java.awt.Dimension(25, 25));
        jButtonRedo.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBarEditor.add(jButtonRedo);
        jToolBarEditor.add(jSeparator2);

        jButtonCut.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/cut.png"))); // NOI18N
        jButtonCut.setToolTipText("Cut");
        jButtonCut.setActionCommand("cut");
        jButtonCut.setFocusable(false);
        jButtonCut.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonCut.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButtonCut.setMaximumSize(new java.awt.Dimension(25, 25));
        jButtonCut.setMinimumSize(new java.awt.Dimension(25, 25));
        jButtonCut.setPreferredSize(new java.awt.Dimension(25, 25));
        jButtonCut.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBarEditor.add(jButtonCut);

        jButtonCopy.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/copy.png"))); // NOI18N
        jButtonCopy.setToolTipText("Copy");
        jButtonCopy.setActionCommand("copy");
        jButtonCopy.setFocusable(false);
        jButtonCopy.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonCopy.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButtonCopy.setMaximumSize(new java.awt.Dimension(25, 25));
        jButtonCopy.setMinimumSize(new java.awt.Dimension(25, 25));
        jButtonCopy.setPreferredSize(new java.awt.Dimension(25, 25));
        jButtonCopy.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBarEditor.add(jButtonCopy);

        jButtonPaste.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/paste.png"))); // NOI18N
        jButtonPaste.setToolTipText("Paste");
        jButtonPaste.setActionCommand("paste");
        jButtonPaste.setFocusable(false);
        jButtonPaste.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonPaste.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButtonPaste.setMaximumSize(new java.awt.Dimension(25, 25));
        jButtonPaste.setMinimumSize(new java.awt.Dimension(25, 25));
        jButtonPaste.setPreferredSize(new java.awt.Dimension(25, 25));
        jButtonPaste.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBarEditor.add(jButtonPaste);
        jToolBarEditor.add(jSeparator3);

        jButtonFind.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/find.png"))); // NOI18N
        jButtonFind.setToolTipText("Find");
        jButtonFind.setActionCommand("find");
        jButtonFind.setFocusable(false);
        jButtonFind.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonFind.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButtonFind.setMaximumSize(new java.awt.Dimension(25, 25));
        jButtonFind.setMinimumSize(new java.awt.Dimension(25, 25));
        jButtonFind.setPreferredSize(new java.awt.Dimension(25, 25));
        jButtonFind.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBarEditor.add(jButtonFind);

        add(jToolBarEditor);

        jSeparator4.setMaximumSize(new java.awt.Dimension(32767, 2));
        jSeparator4.setMinimumSize(new java.awt.Dimension(50, 2));
        add(jSeparator4);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonCopy;
    private javax.swing.JButton jButtonCut;
    private javax.swing.JButton jButtonFind;
    private javax.swing.JButton jButtonOpen;
    private javax.swing.JButton jButtonPaste;
    private javax.swing.JButton jButtonRedo;
    private javax.swing.JButton jButtonSave;
    private javax.swing.JButton jButtonUndo;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JToolBar jToolBarEditor;
    // End of variables declaration//GEN-END:variables

}
