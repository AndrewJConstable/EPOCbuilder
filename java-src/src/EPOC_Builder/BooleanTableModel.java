/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package au.gov.aad.erm.EPOC_Builder;

import static au.gov.aad.erm.EPOC_Builder.Constants.*;

import java.awt.event.*;
import javax.swing.table.*;
import java.util.Vector;

/**
 *
 * @author troy_rob
 */
public class BooleanTableModel extends DefaultTableModel {

    public BooleanTableModel(Vector data, Vector colNames) {
        super(data, colNames);
    }

    public Class getColumnClass(int col) {
        return Boolean.class;
    }
}
