/*******************************************************************************
 * JGRExchanger.java
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
import java.io.*;

import org.rosuda.JGR.*;
import org.rosuda.JGR.editor.*;
import org.rosuda.REngine.*;
import org.rosuda.ibase.SVar;
import org.rosuda.ibase.toolkit.EzMenuSwing;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import jedit.syntax.JEditTextArea;

 /*******************************************************************************
 * NOTE: Not used currently!
 *
 * @author Troy Robertson
 * @company Australian Antarctic Division
 * @created 14/04/2010
 * @version 0.4.0, 01-09-2010
 ******************************************************************************/
public class JGRExchanger {

    Editor jgrEd = null;
    File tmpFile = null;

    public JGRExchanger() {
        org.rosuda.util.Platform.initPlatform("org.rosuda.JGR.toolkit.");
        org.rosuda.JGR.toolkit.JGRPrefs.initialize();

        //org.rosuda.JGR.JGRConsole console = new org.rosuda.JGR.JGRConsole();
        //JGRConsole jgr = new JGRConsole();
    }

    public void createEditor() {
        String filepath = System.getProperty("java.io.tmpdir") + File.separator + EPOC_EDIT_FNAME;
        tmpFile = new File(filepath);

        try {

            jgrEd = new Editor(tmpFile.getAbsolutePath());

            // Use RJavaClassLoader to start JGR ???
            //JGR jgr = new JGR();

            // Editor makes call to Common to get screenres and gets a null returned!
            //jgrEd = new Editor();
            //jgrEd.text
            //jgrEd.setDefaultCloseOperation(TJFrame.DISPOSE_ON_CLOSE);

            //jgrEd.open();
        } catch (Exception e) {

            jgrEd = new Editor(tmpFile.getAbsolutePath());

            //JGRConsole jgr = new JGRConsole();
            //JGR jgr = new JGR();
            //jgrEd = new Editor();
            //jgrEd.setDefaultCloseOperation(TJFrame.DISPOSE_ON_CLOSE);
            //jgrEd.open();

        }
    }

    public void setEditorText(String text) {
       //if (jgrEd == null) createEditor();

       jgrEd.setText(new StringBuffer(text));
    }

    public String getEditorText() {
        String editStr = "", line = "";

        jgrEd.exit();

        if (tmpFile.exists()) {
            try {
                BufferedReader input =  new BufferedReader(new FileReader(tmpFile));
                while ((line = input.readLine()) != null) {
                    editStr += line;
                }
            } catch (IOException ex) {

            }
        }

        return editStr;
    }
}
