/*******************************************************************************
 * XMLReaderWriter.java
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
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;
import java.io.*;
import java.awt.Component;
import java.beans.*;
import java.awt.Cursor;

/*******************************************************************************
 * XMLReaderWriter class for XML Import and Export of EPOC Builder objects.
 *
 * @author Troy Robertson
 * @company Australian Antarctic Division
 * @created 14/04/2010
 * @version 0.4.0, 01-09-2010
 ******************************************************************************/
public class XMLReaderWriter {

    private static XMLReaderWriter _instance = null;
    private static JFileChooser fc = new JFileChooser();
    // Get users 'My Documents' path by default
    private static String lastPath = FileSystemView.getFileSystemView().getDefaultDirectory().getPath();
    private DefaultPersistenceDelegate epocPersistenceDelg = new EPOCObjectPersistenceDelegate();

    /*
     * Return a Singleton instance of class with a connection to the default DB
     */
    public static synchronized XMLReaderWriter getInstance() { //throws Exception {
        if (_instance == null) _instance = new XMLReaderWriter();

        return _instance;
    }

    private static File getFile(Component ui, boolean save, String filename, String message) {
        int returnVal = 0;
        String newPath = lastPath + File.separatorChar + (save && filename != null ? filename : "");
        File selectedFile = new File(newPath);

        if (message != null && !message.equals("")) fc.setDialogTitle(message);
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);

        if (save) {
            fc.setSelectedFile(selectedFile);
            returnVal = fc.showSaveDialog(ui);
        } else {
            fc.setCurrentDirectory(selectedFile);
            returnVal = fc.showOpenDialog(ui);
        }

        if (returnVal == JFileChooser.CANCEL_OPTION) {
            selectedFile = null;
        } else if (returnVal == JFileChooser.APPROVE_OPTION) {
            selectedFile = fc.getSelectedFile();
            if (selectedFile != null) {
                lastPath = (selectedFile.isFile() ? selectedFile.getParent() : selectedFile.getPath());
            }
        }

        return selectedFile;
    }

    public boolean exportXML(Component ui, Object epocObj) {

        File newFile = getFile( ui, true, ((EPOCObject)epocObj).getDisplayName() + ".xml", "Select where to save export");

        if (newFile == null || newFile.isDirectory()) return true;
    
        try {
            ui.setCursor(new Cursor(Cursor.WAIT_CURSOR));
            if (EPOC_DBG) System.out.println("Exporting XML to: " + newFile.getPath());
            FileOutputStream fos = new FileOutputStream(newFile);
            XMLEncoder xenc = new XMLEncoder(fos);
            xenc.setPersistenceDelegate(EPOCObject.class, epocPersistenceDelg);
            xenc.writeObject(epocObj);
            xenc.close();
            fos.close();
            ui.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            return true;

        } catch (IOException ioe) {
            Messages.addErrMsg(ioe.getMessage());
            ioe.printStackTrace();
        }

        ui.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        return false;
    }

    public EPOCObject importXML(Component ui) {
        File newFile = getFile( ui, false, "", "Select EPOC xml object to import");

        if (newFile == null || newFile.isDirectory()) return null;

        try {
            ui.setCursor(new Cursor(Cursor.WAIT_CURSOR));
            if (EPOC_DBG) System.out.println("Exporting XML to: " + newFile.getPath());
            FileInputStream fin = new FileInputStream(newFile);
            XMLDecoder xdec = new XMLDecoder(fin);
            Object obj = xdec.readObject();
            xdec.close();
            fin.close();
            ui.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            if (obj instanceof EPOCObject) return (EPOCObject)obj;
            Messages.addErrMsg("XML file did not contain a valid EPOC object!");
        } catch (IOException ioe) {
            Messages.addErrMsg(ioe.getMessage());
            ioe.printStackTrace();
        }

        ui.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        return null;
    }

    /**
     * Inner class
     */
    class EPOCObjectPersistenceDelegate extends DefaultPersistenceDelegate {

        /**
         *
         * @param type
         * @param oldInstance
         * @param newInstance
         * @param out
         */
        @Override           // initialize method in DefaultPersistenceDelegate
        protected void initialize(Class type, Object oldInstance, Object newInstance, Encoder out) {
            try {
                // Unset some data members on newInstance to ensure they are always included
                ((EPOCObject)newInstance).setUIVersion("");
                
                // then loop through all properties and set some as transient so they
                // are never encoded
                BeanInfo info = Introspector.getBeanInfo(type);
                PropertyDescriptor[] propertyDescriptors =
                                     info.getPropertyDescriptors();
                for (int i = 0; i < propertyDescriptors.length; ++i) {
                    PropertyDescriptor pd = propertyDescriptors[i];
                    if (pd.getName().equalsIgnoreCase("uid")
                            || pd.getName().equalsIgnoreCase("parentuid")
                            || pd.getName().equalsIgnoreCase("templates")
                            || pd.getName().equalsIgnoreCase("position")
                            || pd.getName().equalsIgnoreCase("locked")
                            || pd.getName().equalsIgnoreCase("objectModified")) {
                        pd.setValue("transient", Boolean.TRUE);
                    }
                }
            } catch (IntrospectionException ie) {

            }

            super.initialize(type, oldInstance,  newInstance, out);
        }
    }
}
