/*******************************************************************************
 * RBatchWorker.java
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

import au.gov.aad.erm.RJEditor.*;
import static au.gov.aad.erm.EPOC_Builder.Constants.*;

import javax.swing.SwingWorker;
import javax.swing.JOptionPane;
import java.io.*;

/*******************************************************************************
 * SwingWorker used for managing R Batch processing with return File.
 *
 * @author Troy Robertson
 * @company Australian Antarctic Division
 * @created 14/04/2010
 * @version 0.4.0, 01-09-2010
 ******************************************************************************/
public class RBatchWorker extends SwingWorker<Integer, Void> {

    EPOCBuilderUI parentUI;
    String cmdStr = "";
    String outputPath = "";
    String publishTitle = "";
    String publishPath = "";
    String outputStr = "", errorStr = "";

    public RBatchWorker(EPOCBuilderUI ebUI, String inputFilePath, String outputFilePath) {
        parentUI = ebUI;
        cmdStr = "RCMD BATCH --vanilla -q \"" + inputFilePath + "\"";
        outputPath = publishPath = outputFilePath;
        File tmpFile = new File(outputPath);
        publishTitle = tmpFile.getName();
        if (!outputPath.equals("")) cmdStr += " \"" + outputPath + "\"";
    }

    /**
     * Set an alternative file path to publish from once the job is complete
     * @param pubTitle
     * @param pubPath
     */
    public void setAltPublishPath(String pubPath) {
        publishPath = pubPath;
    }

    /**
     * Set an alternative publish title for the editor
     * @param pubTitle
     * @param pubPath
     */
    public void setAltPublishTitle(String pubTitle) {
        publishTitle = pubTitle;
    }

    @Override
    protected Integer doInBackground() throws Exception {
        int exitVal = 0;

        if (!cmdStr.equals("")) {
            
            Runtime rt = Runtime.getRuntime();
            if (EPOC_DBG) System.out.println("Executing: " + cmdStr);
            Process pr = rt.exec(cmdStr);

            BufferedReader output = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            BufferedReader error = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
            String line=null;

            while((line=output.readLine()) != null) {
                if (EPOC_DBG) System.out.println("OUTPUT: " + line);
                outputStr += line + "\n";
            }
            while((line=error.readLine()) != null) {
                if (EPOC_DBG) System.out.println("ERROR: " + line);
                errorStr += line + "\n";
            }

            exitVal = pr.waitFor();
            pr.destroy();

            if (EPOC_DBG) System.out.println("Exited with exit code "+exitVal);
            if (!outputStr.equals("")) JOptionPane.showMessageDialog(parentUI, "EPOC Output:\n\n" + outputStr);
        }

        return exitVal;
    }

    @Override
    protected void done() {
        Integer exitcode = 0;

        try {
            exitcode = get();

            if (exitcode == 0 && !publishPath.equals("")) {
                parentUI.publishInEditor(publishPath, publishTitle);
            } else {
                parentUI.publishInEditor(outputPath, "EPOC R Failure: " + publishTitle);
                // error message
                JOptionPane.showMessageDialog(parentUI, "EPOC R Failure:\n" + errorStr + "\nExit code: " + exitcode);
            }
        } catch(Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }

        parentUI.endProgress();
    }
}
