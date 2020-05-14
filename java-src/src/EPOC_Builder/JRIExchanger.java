/*******************************************************************************
 * JRIExchanger.java
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

import org.rosuda.JRI.Rengine;
import org.rosuda.JRI.REXP;
import org.rosuda.JRI.RMainLoopCallbacks;
//import org.rosuda.JRI.RConsoleOutputStream;
//import org.rosuda.JRI.RVector;
//import org.rosuda.JRI.RList;

/*******************************************************************************
 * JRI engine/caller.
 *
 * @author Troy Robertson
 * @company Australian Antarctic Division
 * @created 14/04/2010
 * @version 0.4.0, 01-09-2010
 ******************************************************************************/
public class JRIExchanger implements RMainLoopCallbacks {

    private static JRIExchanger _instance = null;

    public static Rengine RE = null;

    public JRIExchanger() {
        createEngine();
    }

    /*
     * Return a Singleton instance of class with a connection to the REngine
     */
    public static synchronized JRIExchanger getInstance() { //throws Exception {
        if (_instance == null) {
            _instance = new JRIExchanger();
        }

        return _instance;
    }

    public boolean createEngine() {
        // just making sure we have the right version of everything

        try {
            System.setProperty("jri.ignore.ule", "yes");
            if (!Rengine.jriLoaded) {
                Messages.addErrMsg("Cannot find JRI native library!\nMake sure rJava package has been install in R.\n");
                return false;
            }
            if (!Rengine.versionCheck()) {
                Messages.addErrMsg("Version mismatch - Java files don't match library version.\n");
                return false;
            }
        } catch (UnsatisfiedLinkError ule) {
            Messages.addErrMsg(ule.getMessage());
            return false;
        }
        if (EPOC_DBG) System.out.println("Creating Rengine (with arguments)");
		// 1) we pass the arguments from the command line
		// 2) we won't use the main loop at first, we'll start it later
		//    (that's the "false" as second argument)
		// 3) the callbacks are implemented by the TextConsole class above
        String[] args = new String[]{"--quiet"};
	RE = new Rengine(args, false, this);
        if (EPOC_DBG) System.out.println("Rengine created, waiting for R");
		// the engine creates R is a new thread, so we should wait until it's ready
        if (!RE.waitForR()) {
            Messages.addErrMsg("Cannot load R.\n");
            return false;
        }

        return true;
    }

    private REXP submit(String expr) {
        if (!hasEngine()) {
            Messages.addErrMsg("No JRI Engine found!");
            return null;
        }

        //clear();
        if (EPOC_DBG) System.out.println("R Submission:\n" + expr);

        try {
            return RE.eval(expr);

        } catch (Exception e) {
            if (EPOC_DBG) System.out.println("EX:"+e);
            if (EPOC_DBG) e.printStackTrace();
        }

        return null;
    }

    public boolean hasEngine() {
        return (RE != null);
    }

    public void clear() {
        REXP out = null;
        try {
            out = RE.eval("rm(list=ls(all=TRUE))");
        } catch (Exception e) {
            if (EPOC_DBG) System.out.println("EX:"+e);
            if (EPOC_DBG) e.printStackTrace();
        }
    }

    public String getRErrorMessage() {
        String rStr = "";

        rStr = "if (exists('error') && class(error) == 'try-error') {\n"
             + "    cat(error[[1]],'\n')\n"
             + "}";

        REXP out = RE.eval(rStr);
        if (out != null) return out.asString();

        return "";
    }

    public boolean parse(String expr) {
        File tmpFile = new File(System.getProperty("java.io.tmpdir") + File.separator + "EPOCBuilder_tmp_parse_file.R");
        // Write code to file
        try {
            tmpFile.createNewFile();
            FileWriter out = new FileWriter(tmpFile);
            out.write(expr);
            out.close();
        } catch (IOException ioe) {
            if (EPOC_DBG) ioe.printStackTrace();
            return false;
        }

        // tell R to parse file
        REXP res = submit("error<-try(parse(\"" + tmpFile.getAbsolutePath().replaceAll("\\\\", "\\\\\\\\") + "\"), silent=TRUE)");
        if (EPOC_DBG) System.out.println(res.asString());
        if (res == null || res.getType() == REXP.XT_NULL
                || res.asString().equalsIgnoreCase("null")) {
            return true;
        } else {
            String errMsg = res.asString().replace(tmpFile.getAbsolutePath(), "Failed parsing R Code at");

            if (errMsg.startsWith("Error")) {
                // R quotes first line including all code and lines breaks so ...
                // remove first line
                String[] lines = errMsg.split("\n");
                errMsg = "";
                for (int i=1 ; i < lines.length ; i++) {
                    errMsg += lines[i] + "\n";
                }
            }

            Messages.addErrMsg(errMsg);
        }

        return false;
    }

    public boolean test(String expr) {
        return submit(expr).asBool().isTRUE();
    }

    public String evaluate(String expr) {
        return submit(expr.replaceAll("\"", "\\\"")).asString();
    }

    public boolean library(String lib) {
        REXP res = submit("library('" + lib + "')");
        if (res != null && res.asString().equalsIgnoreCase(lib)) {
            return true;
        } else {
            String errMsg = res.asString();

            if (res.asString().startsWith("Error")) {
                // R quotes first line including all code and lines breaks so ...
                // remove first line
                String[] lines = errMsg.split("\n");
                errMsg = "";
                for (int i=1 ; i < lines.length ; i++) {
                    errMsg += lines[i] + "\n";
                }
            }

            Messages.addErrMsg(errMsg);
        }

        return false;
    }

    public void rWriteConsole(Rengine re, String text, int oType) {
        if (EPOC_DBG) System.out.println("> " + text);
        if (text.startsWith("Error")) Messages.addErrMsg(text);
        //Messages.addNotificationMsg(text);
    }

    public void rBusy(Rengine re, int which) {
        if (EPOC_DBG) System.out.println("rBusy("+which+")");
    }

    public String rReadConsole(Rengine re, String prompt, int addToHistory) {
        if (EPOC_DBG) System.out.print(prompt);
        try {
            BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
            String s=br.readLine();
            return (s==null||s.length()==0)?s:s+"\n";
        } catch (Exception e) {
            System.out.println("jriReadConsole exception: "+e.getMessage());
        }
        return null;
    }

    public void rShowMessage(Rengine re, String message) {
        if (EPOC_DBG) System.out.println("MESS:> " + message);
    }

    public String rChooseFile(Rengine re, int newFile) {
        /*
	FileDialog fd = new FileDialog((JFrame)SwingUtilities.getRoot(parentUI), (newFile==0)?"Select a file":"Select a new file", (newFile==0)?FileDialog.LOAD:FileDialog.SAVE);
	fd.setVisible(true);
	String res=null;
	if (fd.getDirectory()!=null) res=fd.getDirectory();
	if (fd.getFile()!=null) res=(res==null)?fd.getFile():(res+fd.getFile());
	return res;
         * */
        return null;
    }

    public void rFlushConsole(Rengine re) {
    }

    public void rLoadHistory(Rengine re, String filename) {
    }

    public void rSaveHistory(Rengine re, String filename) {
    }

    public void finalize() throws Throwable {
        if (RE != null) RE.end();
    }
}
