/*******************************************************************************
 * ValidationWorker.java
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

import javax.swing.SwingWorker;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/*******************************************************************************
 * SwingWorker used for JRI Validation processing with return File.
 *
 * @author Troy Robertson
 * @company Australian Antarctic Division
 * @created 14/04/2010
 * @version 0.4.0, 01-09-2010
 ******************************************************************************/
public class ValidationWorker extends SwingWorker<Boolean, Void> {

    EPOCBuilderUI parentUI;
    RJEditor rje = null;
    Universe universe = null;
    Element element = null;
    Action action = null;
    Attribute attribute = null;
    boolean passed = false;

    public ValidationWorker(EPOCBuilderUI ebUI, RJEditor ed, Universe uni) {
        parentUI = ebUI;
        rje = ed;
        rje.getEditor().removeUndoManager();
        universe = uni;
    }

    public ValidationWorker(EPOCBuilderUI ebUI, RJEditor ed, Universe uni, Element ele) {
        this(ebUI, ed, uni);
        element = ele;
    }

    public ValidationWorker(EPOCBuilderUI ebUI, RJEditor ed, Universe uni, Element ele, Action act) {
        this(ebUI, ed, uni, ele);
        action = act;
    }

    public ValidationWorker(EPOCBuilderUI ebUI, RJEditor ed, Universe uni, Element ele, Attribute att) {
        this(ebUI, ed, uni, ele);
        attribute = att;
    }

    @Override
    protected Boolean doInBackground() throws Exception {
        if (universe != null) {
            if (element != null) {
                if (action != null) {
                    passed = validate(action, element, universe);
                } else if (attribute != null) {
                    passed = validate(attribute, element, universe);
                } else {
                    passed = validate(element, universe);
                }
            } else {
                passed = validate(universe);
            }
        }

        return passed;
    }

    @Override
    protected void done() {

        try {
            final boolean result = get();

            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    JOptionPane.showMessageDialog(parentUI, "Object" + (result ? " passed " : " failed ") + "validation check."
                           + (passed ? "" : "\nPlease review report!"), "Validation", (result ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE));
                }
            });
        } catch(Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }

        parentUI.endProgress();
    }

    private boolean validate(Universe uni) {
        boolean passed = true;

        if (!validatePreCheck(uni)) return false;

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

    private boolean validate(Element ele, Universe uni) {
        boolean passed = true;

        if (!validatePreCheck(ele)) return false;

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

    private boolean validate(Attribute att, Element ele, Universe uni) {
        boolean passed = true;

        if (!validatePreCheck(att)) return false;

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

    private boolean validate(Action act, Element ele, Universe uni) {
        boolean passed = true;

        if (!validatePreCheck(act)) return false;

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

        if (universe.rex == null) {
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

    public void outputLn(String text, String prependStr) {
        String[] lines = text.split("\n");
        for (int i = 0 ; i < lines.length ; i++) {
            output(prependStr + lines[i] + "\n");
        }
    }

    public void output(final String text) {
        SwingUtilities.invokeLater(new Runnable()
        {
                public void run()
                {
                        rje.getEditor().addText(text);
                }
        });
        //rje.getEditor().addText(text);
    }

}
