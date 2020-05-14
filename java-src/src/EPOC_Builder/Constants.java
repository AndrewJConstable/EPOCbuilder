/*******************************************************************************
 * Constants.java
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

import java.awt.Dimension;

/*******************************************************************************
 * Static constants and Setting variables
 *
 * @author Troy Robertson
 * @company Australian Antarctic Division
 * @created 10/09/2009
 * @version 0.4.0, 01-09-2010
 *******************************************************************************/
public class Constants {
    
    // EPOC class constants
    public static final int OBJ_EPOC = 0;       // Base class
    public static final int OBJ_UNI = 1;        // Universe
    public static final int OBJ_ELE = 2;        // Element
    public static final int OBJ_ACT = 3;        // element action
    public static final int OBJ_ATT = 4;        // element attribute
    public static final int OBJ_TS = 5;         // action timestep
    public static final int OBJ_SGY = 6;        // Strategy
    public static final int OBJ_REP = 7;        // Report
    public static final int OBJ_SPA = 8;        // Spatial
    public static final int OBJ_TRI = 9;        // Trial
    public static final int OBJ_ALL = 10;        // All Objects

    public static final int OBJ_CLS = 11;        // EPOC Class
    
    // Element module type constants
    public static final int OBJ_BIO = 20;        // Biota
    public static final int OBJ_ENV = 21;        // Environment
    public static final int OBJ_ATY = 22;        // Activity
    public static final int OBJ_MAN = 23;        // Management
    public static final int OBJ_OUT = 24;        // Output
    public static final int OBJ_PRE = 25;        // Presentation

    // Template List display methods
    public static final int DSPL_LST_NM_ASC = 1;          // List view ascending order
    public static final int DSPL_LST_RV_ASC = 2;          // List view descending order
    public static final int DSPL_TRE_NM_ASC = 3;          // Tree view ascending order
    public static final int DSPL_TRE_RV_ASC = 4;          // Tree view descending order

    // Timestep step type and timing constants
    public static final int TS_ALL = 1;          // All Periods
    public static final int TS_FST = 2;          // First Period
    public static final int TS_LST = 3;          // Last Period
    public static final int TS_BEF = 4;          // Before
    public static final int TS_DUR = 5;          // During
    public static final int TS_AFT = 6;          // After

    // Action type constants
    public static final int ACT_ACT = 1;         // Timestep Action
    public static final int ACT_SET = 2;         // Setup action
    public static final int ACT_SUP = 3;         // Element support action

    // Success constants
    public static final int EPOC_NONE = 0;
    public static final int EPOC_SUCC = 1;
    public static final int EPOC_FAIL = 2;

    // Object copy method
    public static final int EPOC_CLN = 1;
    public static final int EPOC_RPL = 2;
    public static final int EPOC_REV = 3;
    public static final int EPOC_BRK = 4;

    //*** Setting defaults ***//ivot
    // Current version number - must change if DB schema changes
    public static final String EPOC_VER = "0.4.1";
    // Default DB name
    public static final String DB_NAME = "EpocDB";
    public static final String DB_USR = "epoc";
    public static final String DB_PWD = "epoc";
    // Database version
    public static String EPOC_DBVER = EPOC_VER;
    // Debug flag
    public static boolean EPOC_DBG = false;
    // Code element name placeholder string
    public static String EPOC_ELESTR = "EPOC_ELE";
    // Missing object name placeholder string
    public static String EPOC_MISSING = "Broken link";
    // Allow editing templates
    public static boolean EPOC_EDIT_TEMPL = false;
    // Template linked objects (eg dataset, transform, related, timestep datasets)
    public static boolean EPOC_TMPL_LINK_OBJ = false;
    // Import linked objects
    public static boolean EPOC_IMP_LINK_OBJ = false;
    // Match local objects on xml import or Save As
    public static boolean EPOC_AUTO_MATCH_MEMBERS = true;
    // Perform automatic broken linked object matching to local objects
    public static boolean EPOC_AUTO_MATCH_LINK_OBJ = true;
    // Perform automatic broken linked object matching to template objects
    public static boolean EPOC_AUTO_MATCH_LINK_TEMPL_OBJ = false;
    // List ordering
    public static int EPOC_LIST_ORDERING = DSPL_LST_NM_ASC;
    // Frame size
    public static Dimension EPOC_FRAME_SIZE = new Dimension(800, 600);

    // temp filename for R parsing - NOT USED
    public static String EPOC_EDIT_FNAME = "Temp_code_edit_file.txt";
}
