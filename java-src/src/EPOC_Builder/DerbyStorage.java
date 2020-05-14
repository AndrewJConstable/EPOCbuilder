/*******************************************************************************
 * DerbyStorage.java
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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.*;
import java.io.*;

/*******************************************************************************
 * Backend implementation of Storage interface using Derby Database
 *
 * @author Troy Robertson
 * @company Australian Antarctic Division
 * @created 10/09/2009
 * @version 0.4.0, 01-09-2010
 *******************************************************************************/
public class DerbyStorage implements Storage {
    
    private static DerbyStorage _instance = null;// = new DerbyStorage();
    
    static String driver = "org.apache.derby.jdbc.EmbeddedDriver";
    static String protocol = "jdbc:derby:";
    static String database = DB_NAME;
    static String attribute = ";create=true";
    static String user = DB_USR;
    static String pwd = DB_PWD;
    
    private static Connection conn;
    private String errMsg = "";
    
    /**
     * Creates a new instance of DerbyStorage
     *
     * @param dbName
     * @param attr
     */
    private DerbyStorage(String dbName, String attr) { //throws Exception {
        try {
            /*
               The driver is installed by loading its class.
               In an embedded environment, this will start up Derby, since it is not already running.
             */
            Class.forName(driver).newInstance();
            //System.out.println("Loaded the appropriate driver.");
               
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
            conn = null;
            
            // Use non-default database/attributes if passed
            if (dbName != null) database = dbName;
            if (attr != null) attribute = attr;
            
            Properties props = new Properties();
            props.put("user", user);
            props.put("password", pwd);

            /*
               The connection specifies create=true to cause
               the database to be created. To remove the database,
               remove the directory derbyDB and its contents.
               The directory derbyDB will be created under
               the directory that the system property
               derby.system.home points to, or the current
               directory if derby.system.home is not set.
             */
            if (EPOC_DBG) System.out.println(protocol + database + attribute);
            conn = DriverManager.getConnection(protocol + database + attribute, props);

            //System.out.println("Connected to and created database derbyDB");
            conn.setAutoCommit(false);
            
            if (!createSchema(conn)) {
                throw new Exception("Failed to instantiate Derby database storage.");
            }
           
            
        } catch (Throwable e) {
            
            if (e instanceof SQLException) {
                printSQLError((SQLException)e);
                
            } else {
                e.printStackTrace();
            }
        }
    }
    
    /*
     * Return a Singleton instance of class with a connection to the default DB
     */
    public static synchronized DerbyStorage getInstance() { //throws Exception {
        if (_instance == null) {
            _instance = new DerbyStorage(null, null);
        }
        
        return _instance;
    }
    
    /*
     * Return a Singleton instance of class with a connection to the passed DB
     */
    public static synchronized DerbyStorage getInstance(String dbName) { //throws Exception {
        if (_instance == null || !database.equals(dbName)) {
            if (_instance != null) shutdownDB();
            _instance = new DerbyStorage(dbName, null);
        }
        
        return _instance;
    }
    
    /*
     * Import database from path passed, shutdown existing DB and then
     * Return a Singleton instance of class with a connection to that DB
     */
    public static synchronized DerbyStorage getImportedInstance(String path) { //throws Exception {
        shutdownDB();
        _instance = new DerbyStorage(null, ";restoreFrom=" + path);
        
        return _instance;
    }
    
    /**
     * Check to see if DB contains any tables, if not then create tables
     * If tables exist then check settings table to see if DB schema revision is
     * the same as current EPOC revision.  If not then update DB schema to current
     * revision
     */
    private boolean createSchema(Connection conn) {
        try {
            Statement s = conn.createStatement();
            
            // TEMP Clear out existing tables
            /*
            s.execute("DROP TABLE settings");
            s.execute("DROP TABLE universe");
            s.execute("DROP TABLE spatial");
            s.execute("DROP TABLE report");
            s.execute("DROP TABLE trial");
            s.execute("DROP TABLE element");
            s.execute("DROP TABLE attribute");
            s.execute("DROP TABLE action");
            s.execute("DROP TABLE timestep");
            s.execute("DROP TABLE links");
            */

            // First check that tables have not already been created
            ResultSet rs = s.executeQuery("SELECT TABLENAME FROM sys.systables WHERE tabletype = 'T'");
            if (!rs.next()) {
                // create all tables
                s.execute("CREATE TABLE universe(uid INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY, " +
                        "parent_uid INT, name VARCHAR(100), shortname VARCHAR(50), classname VARCHAR(50), " +
                        "morph VARCHAR(20), epocid VARCHAR(10), version VARCHAR(50), created TIMESTAMP, " +
                        "modified TIMESTAMP, description VARCHAR(512), creator VARCHAR(50), " +
                        "controller VARCHAR(100), bparent VARCHAR(50), " +
                        "locked SMALLINT, position SMALLINT)");
                s.execute("CREATE TABLE spatial(uid INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY, " +
                        "parent_uid INT, name VARCHAR(100), shortname VARCHAR(50), " +
                        "classname VARCHAR(50), morph VARCHAR(20), epocid VARCHAR(10), version VARCHAR(50), " +
                        "created TIMESTAMP, modified TIMESTAMP, description VARCHAR(512), " +
                        "polygons LONG VARCHAR, polyoverlaps LONG VARCHAR, bparent VARCHAR(50), locked SMALLINT)");
                s.execute("CREATE TABLE report(uid INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY, " +
                        "parent_uid INT, name VARCHAR(100), shortname VARCHAR(50), " +
                        "classname VARCHAR(50), morph VARCHAR(20), epocid VARCHAR(10), version VARCHAR(50), " +
                        "created TIMESTAMP, modified TIMESTAMP, " +
                        "description VARCHAR(512), logprint SMALLINT, logfilename VARCHAR(100), " +
                        "calendarprint SMALLINT, calendarfilename VARCHAR(100), debug SMALLINT, " +
                        "headline1 VARCHAR(100), headline2 VARCHAR(100), headline3 VARCHAR(100), " +
                        "headline4 VARCHAR(100), bparent VARCHAR(50), locked SMALLINT)");
                s.execute("CREATE TABLE trial(uid INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY, " +
                        "parent_uid INT, name VARCHAR(100), shortname VARCHAR(50), " +
                        "classname VARCHAR(50), morph VARCHAR(20), epocid VARCHAR(10), version VARCHAR(50), " +
                        "created TIMESTAMP, modified TIMESTAMP, " +
                        "description VARCHAR(512), yearstart VARCHAR(4), yearend VARCHAR(4), " +
                        "firstfishingyear VARCHAR(4), lastfishingyear VARCHAR(4), trialdir VARCHAR(100), " +
                        "bparent VARCHAR(50), locked SMALLINT, position SMALLINT)");
                s.execute("CREATE TABLE eclass(uid INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY, " +
                        "parent_uid INT, modtype SMALLINT, name VARCHAR(100), shortname VARCHAR(50), version VARCHAR(50), " +
                        "created TIMESTAMP, modified TIMESTAMP, " +
                        "description VARCHAR(512), initmethod LONG VARCHAR, inittrial LONG VARCHAR, " +
                        "initTransition LONG VARCHAR, printstate LONG VARCHAR, updatestate LONG VARCHAR, " +
                        "bparent VARCHAR(50), locked SMALLINT, position SMALLINT)");
                s.execute("CREATE TABLE element(uid INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY, " +
                        "parent_uid INT, modtype SMALLINT, name VARCHAR(100), shortname VARCHAR(50), eclass_uid INT, " +
                        "classname VARCHAR(50), morph VARCHAR(20), epocid VARCHAR(10), version VARCHAR(50), " +
                        "created TIMESTAMP, modified TIMESTAMP, description VARCHAR(512), creator VARCHAR(50), " +
                        "birthday SMALLINT, birthmonth SMALLINT, polygons LONG VARCHAR, bparent VARCHAR(50), " +
                        "locked SMALLINT, position SMALLINT)");
                s.execute("CREATE TABLE action(uid INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY, " +
                        "parent_uid INT, name VARCHAR(100), shortname VARCHAR(50), version VARCHAR(50), " +
                        "created TIMESTAMP, modified TIMESTAMP, " +
                        "description VARCHAR(512), acttype SMALLINT, dataset INT, transform INT, " +
                        "related VARCHAR(100), code LONG VARCHAR, bparent VARCHAR(50), " +
                        "locked SMALLINT, position SMALLINT)");
                s.execute("CREATE TABLE attribute(uid INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY, " +
                        "parent_uid INT, name VARCHAR(100), shortname VARCHAR(50), version VARCHAR(50), " +
                        "created TIMESTAMP, modified TIMESTAMP, " +
                        "description VARCHAR(512), value LONG VARCHAR, bparent VARCHAR(50), " +
                        "locked SMALLINT, position SMALLINT)");
                s.execute("CREATE TABLE timestep(uid INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY, " +
                        "parent_uid INT, startday SMALLINT, startmonth SMALLINT, endday SMALLINT, " +
                        "endmonth SMALLINT, steptype SMALLINT, steptiming SMALLINT, dataset INT)");
                s.execute("CREATE TABLE template(template_uid INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY, " +
                         "parent_uid INT, uid INT, objecttype SMALLINT, position SMALLINT, " +
                         "CONSTRAINT tmp_unique_cst UNIQUE (parent_uid, uid, objecttype))");
                s.execute("CREATE TABLE settings(setting VARCHAR(50) PRIMARY KEY, value VARCHAR(50), " +
                        "description VARCHAR(512))");
                 
                // put the current revision into settings table
                s.executeUpdate("INSERT INTO settings(setting, value, description) " +
                                "VALUES('Version', '" + EPOC_VER + "', 'Current EPOC schema version')");

                // add indexes for the revision columns
                s.execute("CREATE INDEX uni_ver_idx ON universe(version)");
                s.execute("CREATE INDEX ecl_ver_idx ON eclass(version)");
                s.execute("CREATE INDEX spa_ver_idx ON spatial(version)");
                s.execute("CREATE INDEX rep_ver_idx ON report(version)");
                s.execute("CREATE INDEX tri_ver_idx ON trial(version)");
                s.execute("CREATE INDEX ele_ver_idx ON element(version)");
                s.execute("CREATE INDEX act_ver_idx ON action(version)");
                s.execute("CREATE INDEX att_ver_idx ON attribute(version)");

                // add indexes for the foreign parent_uid columns
                s.execute("CREATE INDEX uni_pid_idx ON universe(parent_uid)");
                s.execute("CREATE INDEX ele_pid_idx ON element(parent_uid)");
                s.execute("CREATE INDEX ele_eid_idx ON element(eclass_uid)");
                s.execute("CREATE INDEX spa_pid_idx ON spatial(parent_uid)");
                s.execute("CREATE INDEX rep_pid_idx ON report(parent_uid)");
                s.execute("CREATE INDEX tri_pid_idx ON trial(parent_uid)");
                s.execute("CREATE INDEX act_pid_idx ON action(parent_uid)");
                s.execute("CREATE INDEX att_pid_idx ON attribute(parent_uid)");
                s.execute("CREATE INDEX ts_pid_idx ON timestep(parent_uid)");

                // TR 2/6/10 objecttype added
                s.execute("CREATE INDEX tmp_uid_idx ON template(uid)");
                s.execute("CREATE INDEX tmp_pid_idx ON template(parent_uid)");
                s.execute("CREATE INDEX tmp_ot_idx ON template(objecttype)");

                conn.commit();
    
            } else {
                // check that schema revision matches current EPOC revision
                try{ 
                    rs = s.executeQuery("SELECT value FROM settings WHERE setting = 'Version'");
                } catch (SQLException e) {
                    // No settings table?
                }
                if (!rs.next()) {
                    // No revision setting?
                    s.execute("INSERT INTO settings(setting, value, description) " +
                                    "VALUES('Version', '" + EPOC_VER + "', " +
                                    "'Current EPOC schema version')");
                } else if (!rs.getString("value").equals(EPOC_VER)) {
                    if (!updateSchema(rs.getString("value"))) {
                        throw new SQLException("Failed to update database schema to version: " + EPOC_VER);
                    }
                }
                // Check individually and create any that are missing ?
            }
            
            conn.commit();
            rs.close();
            s.close();
            
            return true;
            
        } catch (Throwable e) {
            if (e instanceof SQLException) {
                printSQLError((SQLException) e);
            } else {
                e.printStackTrace();
            }
            
            return false;
        }
    }
    
    /*
     * Place a backup of the database in the passed location
     */
    public boolean exportDB(String path) {
        String sql;
        
        sql = "CALL SYSCS_UTIL.SYSCS_BACKUP_DATABASE('" + path + "')";
        try {
            Statement s = conn.createStatement();
            s.execute(sql);
            
            return true;
        } catch (Throwable e) {
            if (e instanceof SQLException) {
                printSQLError((SQLException) e);
            } else {
                e.printStackTrace();
            }
            
            return false;
        }
    }
    
    /*
     * Delete database folder from filesystem if found
     */
    public boolean deleteDB(String dbName) {
        File dbDir = new File(System.getProperty("user.dir") + "\\" + dbName);
        
        // DO not delete if it is current database
        if (database.equals(dbName)) return false;
        
        if (dbDir.exists() && dbDir.isDirectory()) {
            
            File subSP = new File(dbDir.getPath() + "\\service.properties");
            File subSeg0 = new File(dbDir.getPath() + "\\seg0");

            // assume it is db if contains this file and subdirectory
            if (subSP.exists() && subSeg0.exists()) {
                 return delDirectory(dbDir);
            }
        }
        
        return false;
    }
    
    /*
     * Recursive function used to delete passed directory and all subdirectories
     * and files
     */
    private boolean delDirectory(File dir) {
        if (dir.exists() && dir.isDirectory()) {
        
            String[] subdirs = dir.list();
            for (int i = 0; subdirs != null && i < subdirs.length; i++) {
                if (!subdirs[i].equals(".")) {
                    
                    File file = new File(dir.getPath() + "\\" + subdirs[i]);
                    if (file.isFile()) {
                        System.out.println(file.getPath());
                        if (!file.delete()) return false;
                    } else if (file.isDirectory()) {
                        if (!delDirectory(file)) return false;
                    }
                }
            }
            System.out.println(dir.getPath());
            return dir.delete();
            //return true;
        }
        
        return false;
    }
    
    /*
     * Return name of current database
     */
    public String getDBName() {
        return database;
    }
    
    /*
     * @return boolean true if a connection exists to DB
     */
    public boolean isConnected() {
        return conn != null;
    }
    
    /*
     * Return an array of database names available
     * If excludeCurrent is true then except current database
     */
    public ArrayList getDBAvailableList(boolean excludeCurrent) {
        ArrayList dbs = new ArrayList(); 
        File dir = new File(System.getProperty("user.dir"));
        
        // Do not return any files that start with '.'.
        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                File sub = new File(dir.getPath() + "\\" + name);
                return sub.isDirectory();
            }
        };
        String[] subdirs = dir.list(filter);
 
        if (subdirs != null) {
            for (int i = 0; i < subdirs.length; i++) {
                if (excludeCurrent && (subdirs[i].compareToIgnoreCase(database) == 0)) {
                    continue;
                }
                File sub = new File(dir.getPath() + "\\" + subdirs[i]);
                File subSP = new File(sub.getPath() + "\\service.properties");
                File subSeg0 = new File(sub.getPath() + "\\seg0");
                
                // assume it is db if contains this file and subdirectory
                if (subSP.exists() && subSeg0.exists()) {
                    String[] db = {dir.getPath() + "\\" + subdirs[i], subdirs[i]};
                    dbs.add(db);
                }
            }
        }

        return dbs;
    }
    
    /**
     * If parameter currVer == ""
     * Check storage for EPOC object with highest left-most component and return
     * that value incremented (eg if highest revision in storage is "4.3" then return "5"
     * else if currVer != ""
     * Check storage for EPOC object with passed revision and highest extra right-most component
     * and return its revision incremented (eg "2.3.6" might return "2.3.6.4")
     */
    public String getNextVersion(int objType, String currVer) {
        String sql;
        String tbl = getTableName(objType);
        int highest=0;
        
        try {
            // get all records beginning with same revision
            Statement s = conn.createStatement();
            sql ="SELECT version FROM " + tbl; 
            if (!currVer.equals("")) {
                sql += " WHERE version LIKE '" + currVer + ".%'";
            }
            outputSQL(sql);
            ResultSet rs = s.executeQuery(sql);
            
            String[] verBits;
            while (rs.next()) {
                
                if (currVer.equals("")) {
                    // find record with highest major revision
                    verBits = rs.getString("version").split("\\.");
     
                } else {
                    // find record with highest next minor revision
                    verBits = rs.getString("version").replaceFirst(currVer + ".", "").split("\\.");
                    
                }
                if (verBits.length > 0 && Integer.parseInt(verBits[0]) > highest) {
                    highest = Integer.parseInt(verBits[0]);
                }
            }
            
            s.close();
        } catch (SQLException se) {
            printSQLError(se);
        }
        
        // return one higher
        return (currVer.equals("") ? "" : currVer + ".") + String.valueOf(++highest);
    }
    
    /*
     * Return a 2D list of target object uid and shortname/version in the form 'name (revision)'
     * which does not include the exempt uid
     */
    public ArrayList getNegList(int objType, int exemptUID) {
        String[] item;
        String sql;
        ArrayList lst = new ArrayList();
        String tbl = getTableName(objType);
      
        sql = "SELECT uid, shortname, version " +
              "FROM " + tbl + " WHERE uid != " + exemptUID + " " +
              "ORDER BY uid";
        outputSQL(sql);

        try {
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery(sql);
            while (rs.next()) {
                item = new String[2];
                item[0] = rs.getString("uid");
                item[1] = rs.getString("shortname") + " (" + rs.getString("version") + ")";
                lst.add(item);
            }
            
            rs.close();
            s.close();
        } catch (SQLException se) {
            printSQLError(se);
        }
        
        return lst;
    }

    /**
     * Return a list of string arrays of size 2 which contain the uid,name strings
     * of objects of objType which have no known parent and are not templates.
     * @param objType
     * @return
     */
    public ArrayList getOrphanedList(int objType) {
        String[] item;
        String sql;
        ArrayList lst = new ArrayList();

        if (objType == OBJ_ALL || objType == OBJ_UNI) {
            lst.addAll(getOrphanedList(OBJ_CLS));
            lst.addAll(getOrphanedList(OBJ_SPA));
            lst.addAll(getOrphanedList(OBJ_REP));
            lst.addAll(getOrphanedList(OBJ_TRI));
            lst.addAll(getOrphanedList(OBJ_ELE));
            lst.addAll(getOrphanedList(OBJ_ACT));
            lst.addAll(getOrphanedList(OBJ_ATT));
            lst.addAll(getOrphanedList(OBJ_TS));
        } else {
            String tbl = getTableName(objType);
            String tblpar = getParentTableName(objType);

            sql = "SELECT uid, shortname, version FROM " + tbl + " " +
                  "WHERE parent_uid <> 0 AND parent_uid NOT IN (SELECT uid FROM " + tblpar + ") " +
                  "ORDER BY uid";
            outputSQL(sql);

            try {
                Statement s = conn.createStatement();
                ResultSet rs = s.executeQuery(sql);
                while (rs.next()) {
                    item = new String[2];
                    item[0] = rs.getString("uid");
                    item[1] = rs.getString("shortname") + " (" + rs.getString("version") + ")";
                    lst.add(item);
                }

                rs.close();
                s.close();
            } catch (SQLException se) {
                printSQLError(se);
            }
        }

        return lst;
    }

    /*
     * Return a list of objects of the type passed which are templates
     * ie they have parent_uid = 0, and are not already a child of the
     * passed excludeUID
     * Objects of type OBJ_ELE will include all element types
     *
     * @param objType
     * @param excludeUID
     * @return
     */
    public boolean loadTemplates(Templates templates, int objType, int excludeUID) {
        String sql;
        boolean retVal = true;
        String tbl = getTableName(objType);
        
        sql = "SELECT uid FROM " + tbl + " WHERE parent_uid = 0 ";
        
        // check if templates already linked to a parent need excluding
        if (excludeUID != 0) {
            sql += "AND uid NOT IN (SELECT uid FROM template WHERE parent_uid = " + excludeUID + " ";
            sql += "AND objecttype = " + objType + ") ";
        }
        sql += "ORDER BY uid";
        outputSQL(sql);

        try {
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery(sql);
            while (rs.next()) {
                // if its not already in templates list then instantiate it which will
                // cause it to be placed into list
                if (templates.getFromTemplateList(objType, rs.getInt("uid")) == null) {
                    EPOCObject obj = instantiateObject(objType, rs.getInt("uid"), templates);
                    obj.setTemplate(true);
                }
            }
            rs.close();
            s.close();
        } catch (SQLException se) {
            printSQLError(se);
            retVal = false;
        }
        
        return retVal;
    }

     /**
     * Return an EPOC object of the type passed loaded with data for
     * uepocid passed
     */
    private EPOCObject instantiateObject(int objType, int uid, Templates templates) {
        switch (objType) {
            case OBJ_UNI:
                return new Universe(uid);
            case OBJ_SPA:
                return new Spatial(uid, templates);
            case OBJ_REP:
                return new Report(uid, templates);
            case OBJ_TRI:
                return new Trial(uid, templates);
            case OBJ_CLS:
                return new EClass(uid, templates);
            case OBJ_ELE:
            case OBJ_BIO:
            case OBJ_ENV:
            case OBJ_ATY:
            case OBJ_MAN:
            case OBJ_OUT:
                return new Element(uid, templates);
            case OBJ_ACT:
                return new Action(uid, templates);
            case OBJ_ATT:
                return new Attribute(uid, templates);
            case OBJ_TS:
                return new Timestep(uid, templates);
        }

        return null;
    }

    /*
     * Add template link between passed parent and child uids if it
     * doesn't already exist
     * @return boolean true on success
     *
     * @param parentUID
     * @param obj
     * @return
     */
    public boolean linkTemplate(int parentUID, EPOCObject obj) {
        String sql;
        boolean retVal = true;
        
        if (parentUID > 0 && obj.getUID() > 0 && obj.getObjType() > 0 && obj.getObjType() != OBJ_UNI) {

            try {
                Statement s = conn.createStatement();
                if (templateLinkCount(parentUID, obj.getUID(), obj.getObjType()) == 0) {
                    sql ="INSERT INTO template (template_uid, parent_uid, uid, objecttype, position) " +
                         "VALUES(DEFAULT, " + parentUID + ", " + obj.getUID() + ", " + obj.getObjType() +
                         ", " + obj.getPosition() + ")";
                    
                } else {
                    sql = "UPDATE template set position = " + obj.getPosition() + " " +
                          "WHERE parent_uid = " + parentUID + " AND uid = " + obj.getUID() + " " +
                          "AND objecttype = " + obj.getObjType();
                }
                outputSQL(sql);
                s.executeUpdate(sql);
                s.close();
                conn.commit();

            } catch (SQLException se) {
                retVal = false;
                printSQLError(se);
            }
        }
        
        return retVal;
    }
    
    /*
     * Remove template link between passed parent and child uid if it exists
     *
     * @param parentUID
     * @param uid
     * @param objType
     * @return
     */
    public boolean unlinkTemplate(int parentUID, int uid, int objType) {
        String sql;
        boolean retVal = true;
        
        if (parentUID > 0 && uid > 0 && objType > 0 && objType != OBJ_UNI) {
            try {
                Statement s = conn.createStatement();
                sql ="DELETE FROM template WHERE parent_uid = " + parentUID + " " +
                     "AND uid = " + uid + " " +
                     "AND objecttype = " + objType;
                outputSQL(sql);
                s.executeUpdate(sql);
                s.close();
                conn.commit();
            } catch (SQLException se) {
                retVal = false;
                printSQLError(se);
            }
        }
        
        return retVal;
    }
    
    /*
     * Check whether template is linked to any parent except the excludePID passed
     *
     * @param excludePID
     * @param uid
     * @param objType
     * @return
     */
    public boolean templateUsedByOther(int excludePID, int uid, int objType) {
        boolean retVal = true;
        String sql;
        
        try {
            Statement s = conn.createStatement();
            sql = "SELECT Count(*) AS cnt FROM template " +
                  "WHERE parent_uid != " + excludePID + " AND uid = " + uid + 
                  " AND objecttype = " + objType;
            outputSQL(sql);
            ResultSet rs = s.executeQuery(sql);

            if (rs.next()) {
                if (rs.getInt("cnt") == 0) retVal = false;
            }
        } catch (SQLException se) {
            retVal = false;
            printSQLError(se);
        }
        
        return retVal;
    }
    
    /*
     * Return a count of the template links existing
     * If either uids is 0 then count links to all of the other
     * If all are 0 then return count of all links
     */
    public int templateLinkCount(int pid, int uid, int objType) {
        String sql, where = "";
        int retVal = 0;
        
        if (pid > 0) {
            where += "WHERE parent_uid = " + pid;
        }
        if (uid > 0) {
            if (where.equals("")) {
                where = "WHERE ";
            } else {
                where += " AND ";
            }
            where += "uid = " + uid;
        }
        if (objType > 0) {
            if (where.equals("")) {
                where = "WHERE ";
            } else {
                where += " AND ";
            }
            where += "objecttype = " + objType;
        }
        
        try {
            Statement s = conn.createStatement();
            sql = "SELECT COUNT(*) As cnt FROM template " + where;
            outputSQL(sql);
            ResultSet rs = s.executeQuery(sql);
            if (rs.next()) {
                retVal = rs.getInt("cnt");
            }
            
            rs.close();
            s.close();
        } catch (SQLException se) {
                printSQLError(se);
        }
        
        return retVal;
    }
    
    /*
     * Attempt to load EPOC object data members from storage given object's uid
     * Load all child objects
     * @return  boolean true on success
     */
    public boolean load(EPOCObject obj) {
        return load(obj, true);
    }
    
    /*
     * Attempt to load EPOC object data members from storage given object's uid
     * Load all child object if saveChildren
     * @return  boolean true on success
     */
    public boolean load(EPOCObject obj, boolean loadChildren) {
        
        if (obj instanceof Universe) {
            return loadUniverse((Universe)obj, loadChildren);
        } else if (obj instanceof Spatial) {
            return loadSpatial((Spatial)obj);
        } else if (obj instanceof Report) {
            return loadReport((Report)obj);
        } else if (obj instanceof Trial) {
            return loadTrial((Trial)obj);
        } else if (obj instanceof EClass) {
            return loadEClass((EClass)obj);
        } else if (obj instanceof Element) {
            return loadElement((Element)obj, loadChildren);
        } else if (obj instanceof Action) {
            return loadAction((Action)obj);    
        } else if (obj instanceof Attribute) {
            return loadAttribute((Attribute)obj);
        } else if (obj instanceof Timestep) {
            return loadTimestep((Timestep)obj);
        }
        
        return false;
    }
    
    /**
     * Attempt to save EPOC object data members from storage given object's uid
     * Save all child objects
     * @return  boolean true on success
     */
    public boolean save(EPOCObject obj) {
        return save(obj, true);
    }
    
    /**
     * Attempt to save EPOC object data members from storage given object's uid
     * Save all child objects if saveChildren
     * @return  boolean true on success
     */
    public boolean save(EPOCObject obj, boolean saveChildren) {
        
        if (obj instanceof Universe) {
            return saveUniverse((Universe)obj, saveChildren);
        } else if (obj instanceof Spatial) {
            return saveSpatial((Spatial)obj);
        } else if (obj instanceof Report) {
            return saveReport((Report)obj);
        } else if (obj instanceof Trial) {
            return saveTrial((Trial)obj);
        } else if (obj instanceof EClass) {
            return saveEClass((EClass)obj);
        } else if (obj instanceof Element) {
            return saveElement((Element)obj, saveChildren);
        } else if (obj instanceof Action) {
            return saveAction((Action)obj);    
        } else if (obj instanceof Attribute) {
            return saveAttribute((Attribute)obj);
        } else if (obj instanceof Timestep) {
            return saveTimestep((Timestep)obj);
        }
        
        return false;
    }
    
    /**
     * Attempt to delete EPOC object data members from storage given object's uid
     * Delete all child objects
     * @return  boolean true on success
     */
    public boolean delete(EPOCObject obj) {
        return delete(obj, true);
    }
    
    /**
     * Attempt to delete EPOC object data members from storage given object's uid
     * Delete all child objects if delChildren
     * @return  boolean true on success
     */
    public boolean delete(EPOCObject obj, boolean delChildren) {
        
        if (obj instanceof Universe) {
            return deleteUniverse(obj.getUID(), delChildren);
        } else if (obj instanceof Spatial) {
            return deleteSpatial(obj.getUID());
        } else if (obj instanceof Report) {
            return deleteReport(obj.getUID());
        } else if (obj instanceof Trial) {
            return deleteTrial(obj.getUID());
        } else if (obj instanceof EClass) {
            return deleteEClass(obj.getUID());
        } else if (obj instanceof Element) {
            return deleteElement(obj.getUID(), delChildren);
        } else if (obj instanceof Action) {
            return deleteAction(obj.getUID());
        } else if (obj instanceof Attribute) {
            return deleteAttribute(obj.getUID());
        } else if (obj instanceof Timestep) {
            return deleteTimestep(obj.getUID());
        }
        
        return false;
    }
    
    /**
     * Attempt to load universe data members from storage given object's uid
     * @return  boolean true on success
     */
    private boolean loadUniverse(Universe uni, boolean loadChildren) {
        int uid = uni.getUID();
        boolean retVal = true;
        String sql;
        
        if (uid > 0) {
            try {
                Statement s = conn.createStatement();
                sql ="SELECT * FROM universe WHERE uid = " + uid;
                outputSQL(sql);
                ResultSet rs = s.executeQuery(sql);
        
                if (rs.next()) {
                    uni.setParentUID(rs.getInt("parent_uid"));
                    uni.setName(rs.getString("name"));
                    uni.setShortName(rs.getString("shortname"));
                    uni.setEPOCClassName(rs.getString("classname"));
                    uni.setMorph(rs.getString("morph"));
                    uni.setEPOCID(rs.getString("epocid"));
                    uni.setRevision(rs.getString("version"));
                    uni.setDescription(rs.getString("description"));
                    uni.setAuthor(rs.getString("creator"));
                    uni.setCreated(new java.util.Date(rs.getTimestamp("created").getTime()));
                    uni.setModified(new java.util.Date(rs.getTimestamp("modified").getTime()));
                    uni.setController(rs.getString("controller"));
                    uni.setLocked(rs.getBoolean("locked"));
                    uni.setPosition(rs.getInt("position"));
                    
                    rs.close();

                    // Load all templates
                    uni.setTemplates(new Templates());

                    // load spatial
                    Spatial spa;
                    sql = "SELECT uid, 'uniq' as type FROM spatial WHERE parent_uid = " + uid + " " +
                          "UNION " +
                          "SELECT uid, 'tmpl' as type FROM template WHERE parent_uid = " + uid + " " +
                          "AND objecttype = " + OBJ_SPA + " " +
                          "ORDER BY uid";
                    outputSQL(sql);
                    rs = s.executeQuery(sql);
                    while (rs.next()) {
                        if (rs.getString("type").equals("uniq") || uni.getTemplates() == null) {
                            spa = new Spatial(rs.getInt("uid"), uni.getTemplates());    // causes it to be loaded
                        } else {
                            spa = (Spatial)uni.templates.getFromTemplateList(OBJ_SPA, rs.getInt("uid"));
                            spa.setTemplates(uni.getTemplates());
                        }
                        uni.setConfigObject(spa);
                    }
                    rs.close();

                    // load report
                    Report rep;
                    sql = "SELECT uid, 'uniq' as type FROM report WHERE parent_uid = " + uid + " " +
                          "UNION " +
                          "SELECT uid, 'tmpl' as type FROM template WHERE parent_uid = " + uid + " " +
                          "AND objecttype = " + OBJ_REP + " " +
                          "ORDER BY uid";
                    outputSQL(sql);
                    rs = s.executeQuery(sql);
                    while (rs.next()) {
                        if (rs.getString("type").equals("uniq") || uni.getTemplates() == null) {
                            rep = new Report(rs.getInt("uid"), uni.getTemplates());    // causes it to be loaded
                        } else {
                            rep = (Report)uni.templates.getFromTemplateList(OBJ_REP, rs.getInt("uid"));
                            rep.setTemplates(uni.getTemplates());
                        }
                        uni.setConfigObject(rep);
                    }
                    rs.close();

                    // load trial
                    Trial tri;
                    sql = "SELECT uid, position, 'uniq' as type FROM trial WHERE parent_uid = " + uid + " " +
                          "UNION " +
                          "SELECT uid, position, 'tmpl' as type FROM template WHERE parent_uid = " + uid + " " +
                          "AND objecttype = " + OBJ_TRI + " " +
                          "ORDER BY position";
                    outputSQL(sql);
                    rs = s.executeQuery(sql);
                    while (rs.next()) {
                        if (rs.getString("type").equals("uniq") || uni.getTemplates() == null) {
                            tri = new Trial(rs.getInt("uid"), uni.getTemplates());    // causes it to be loaded
                        } else {
                            tri = (Trial)uni.templates.getFromTemplateList(OBJ_TRI, rs.getInt("uid"));
                            tri.setTemplates(uni.getTemplates());
                        }
                        if (rs.getString("type").equals("tmpl")) tri.setPosition(rs.getInt("position"));
                        uni.setConfigObject(tri);
                    }
                    rs.close();

                    if (loadChildren) {
                        Element ele;
                        sql = "SELECT uid, position, 'uniq' as type FROM element WHERE parent_uid = " + uid + " " +
                              "UNION " +
                              "SELECT uid, position, 'tmpl' as type FROM template WHERE parent_uid = " + uid + " " +
                              "AND objecttype = " + OBJ_ELE + " " +
                              "ORDER BY position";
                        outputSQL(sql);
                        rs = s.executeQuery(sql);
                        while (rs.next()) {
                            if (rs.getString("type").equals("uniq") || uni.getTemplates() == null) {
                                ele = new Element(rs.getInt("uid"), uni.getTemplates()); // causes it to be loaded
                            }else {
                                ele = (Element)uni.getTemplates().getFromTemplateList(OBJ_ELE, rs.getInt("uid"));
                                ele.setTemplates(uni.getTemplates());
                            }
                            if (rs.getString("type").equals("tmpl")) ele.setPosition(rs.getInt("position"));
                            uni.addElement(ele);
                        }
                    }
                } else {
                    retVal = false;
                }
                
                rs.close();
                s.close();
            
            } catch (SQLException se) {
                retVal = false;
                printSQLError(se);
            }
        } else if (uid == -1) {
            // broken link
            uni.setShortName(EPOC_MISSING);
        }

        return retVal;
    }
    
    /**
     * Attempt to load Universe Spatial object from storage given object's uid
     * @return  boolean true on success
     */
    private boolean loadSpatial(Spatial spa) {
        int uid = spa.getUID();
        boolean retVal = true;
        String sql;

        if (uid > 0) {
            try {
                Statement s = conn.createStatement();
                sql ="SELECT * FROM spatial WHERE uid = " + uid;
                outputSQL(sql);
                ResultSet rs = s.executeQuery(sql);

                if (rs.next()) {
                    spa.setParentUID(rs.getInt("parent_uid"));
                    spa.setName(rs.getString("name"));
                    spa.setShortName(rs.getString("shortname"));
                    spa.setEPOCClassName(rs.getString("classname"));
                    spa.setMorph(rs.getString("morph"));
                    spa.setEPOCID(rs.getString("epocid"));
                    spa.setRevision(rs.getString("version"));
                    spa.setCreated(new java.util.Date(rs.getTimestamp("created").getTime()));
                    spa.setModified(new java.util.Date(rs.getTimestamp("modified").getTime()));
                    spa.setDescription(rs.getString("description"));
                    spa.setPolygonsString(rs.getString("polygons"));
                    spa.setOverlapsString(rs.getString("polyoverlaps"));
                    spa.setLocked(rs.getBoolean("locked"));

                    // Add self to templates if it is a templates
                    if (spa.getParentUID() == 0
                            && spa.getTemplates() != null && spa.getTemplates().getFromTemplateList(OBJ_SPA, spa.getUID()) == null) {
                        spa.getTemplates().addTemplateList(spa);
                    }
                } else {
                    retVal = false;
                }

                rs.close();
                s.close();
            } catch (SQLException se) {
                retVal = false;
                printSQLError(se);
            }
        } else if (uid == -1) {
            // broken link
            spa.setShortName(EPOC_MISSING);
        }

        return retVal;
    }

    /**
     * Attempt to load Universe Report object from storage given object's uid
     * @return  boolean true on success
     */
    private boolean loadReport(Report rep) {
        int uid = rep.getUID();
        boolean retVal = true;
        String sql;

        if (uid > 0) {
            try {
                Statement s = conn.createStatement();
                sql ="SELECT * FROM report WHERE uid = " + uid;
                outputSQL(sql);
                ResultSet rs = s.executeQuery(sql);

                if (rs.next()) {
                    rep.setParentUID(rs.getInt("parent_uid"));
                    rep.setName(rs.getString("name"));
                    rep.setShortName(rs.getString("shortname"));
                    rep.setEPOCClassName(rs.getString("classname"));
                    rep.setMorph(rs.getString("morph"));
                    rep.setEPOCID(rs.getString("epocid"));
                    rep.setRevision(rs.getString("version"));
                    rep.setCreated(new java.util.Date(rs.getTimestamp("created").getTime()));
                    rep.setModified(new java.util.Date(rs.getTimestamp("modified").getTime()));
                    rep.setDescription(rs.getString("description"));
                    rep.setLogPrint(rs.getBoolean("logprint"));
                    rep.setLogFilename(rs.getString("logfilename"));
                    rep.setCalendarPrint(rs.getBoolean("calendarprint"));
                    rep.setCalendarFilename(rs.getString("calendarfilename"));
                    rep.setDebug(rs.getBoolean("debug"));
                    rep.setHeadline(1, rs.getString("headline1"));
                    rep.setHeadline(2, rs.getString("headline2"));
                    rep.setHeadline(3, rs.getString("headline3"));
                    rep.setHeadline(4, rs.getString("headline4"));
                    rep.setLocked(rs.getBoolean("locked"));

                    // Add self to templates if it is a templates
                    if (rep.getParentUID() == 0
                            && rep.getTemplates() != null && rep.getTemplates().getFromTemplateList(OBJ_REP, rep.getUID()) == null) {
                        rep.getTemplates().addTemplateList(rep);
                    }
                } else {
                    retVal = false;
                }

                rs.close();
                s.close();
            } catch (SQLException se) {
                retVal = false;
                printSQLError(se);
            }
        } else if (uid == -1) {
            // broken link
            rep.setShortName(EPOC_MISSING);
        }

        return retVal;
    }

    /**
     * Attempt to load Universe Trial object from storage given object's uid
     * @return  boolean true on success
     */
    private boolean loadTrial(Trial tri) {
        int uid = tri.getUID();
        boolean retVal = true;
        String sql;

        if (uid > 0) {
            try {
                Statement s = conn.createStatement();
                sql ="SELECT * FROM trial WHERE uid = " + uid;
                outputSQL(sql);
                ResultSet rs = s.executeQuery(sql);

                if (rs.next()) {
                    tri.setParentUID(rs.getInt("parent_uid"));
                    tri.setName(rs.getString("name"));
                    tri.setShortName(rs.getString("shortname"));
                    tri.setEPOCClassName(rs.getString("classname"));
                    tri.setMorph(rs.getString("morph"));
                    tri.setEPOCID(rs.getString("epocid"));
                    tri.setRevision(rs.getString("version"));
                    tri.setCreated(new java.util.Date(rs.getTimestamp("created").getTime()));
                    tri.setModified(new java.util.Date(rs.getTimestamp("modified").getTime()));
                    tri.setDescription(rs.getString("description"));
                    tri.setYearStart(rs.getString("yearstart"));
                    tri.setYearEnd(rs.getString("yearend"));
                    tri.setFishingStart(rs.getString("firstfishingyear"));
                    tri.setFishingEnd(rs.getString("lastfishingyear"));
                    tri.setTrialDir(rs.getString("trialdir"));
                    tri.setLocked(rs.getBoolean("locked"));
                    tri.setPosition(rs.getInt("position"));

                    // Add self to templates if it is a templates
                    if (tri.getParentUID() == 0
                            && tri.getTemplates() != null && tri.getTemplates().getFromTemplateList(OBJ_TRI, tri.getUID()) == null) {
                        tri.getTemplates().addTemplateList(tri);
                    }
                } else {
                    retVal = false;
                }

                rs.close();
                s.close();
            } catch (SQLException se) {
                retVal = false;
                printSQLError(se);
            }
        } else if (uid == -1) {
            // broken link
            tri.setShortName(EPOC_MISSING);
        }

        return retVal;
    }

    /**
     * Attempt to load EClass object from storage given object's uid
     * @return  boolean true on success
     */
    private boolean loadEClass(EClass ec) {
        int uid = ec.getUID();
        boolean retVal = true;
        String sql;

        if (uid > 0) {
            try {
                Statement s = conn.createStatement();
                sql ="SELECT * FROM eclass WHERE uid = " + uid;
                outputSQL(sql);
                ResultSet rs = s.executeQuery(sql);

                if (rs.next()) {
                    ec.setParentUID(rs.getInt("parent_uid"));
                    ec.setModType(rs.getInt("modtype"));
                    ec.setShortName(rs.getString("shortname"));
                    ec.setRevision(rs.getString("version"));
                    ec.setCreated(new java.util.Date(rs.getTimestamp("created").getTime()));
                    ec.setModified(new java.util.Date(rs.getTimestamp("modified").getTime()));
                    ec.setDescription(rs.getString("description"));
                    ec.setLocked(rs.getBoolean("locked"));
                    ec.setPosition(rs.getInt("position"));
                    ec.setInitClass(rs.getString("initclass"));
                    ec.setInitTrial(rs.getString("inittrial"));
                    ec.setInitTransition(rs.getString("inittransition"));
                    ec.setPrintState(rs.getString("printstate"));
                    ec.setUpdateState(rs.getString("updatestate"));

                    // Add self to templates if it is a templates
                    if (ec.getParentUID() == 0 && ec.getTemplates() != null
                            && ec.getTemplates().getFromTemplateList(OBJ_CLS, ec.getUID()) == null) {
                        ec.getTemplates().addTemplateList(ec);
                    }

                } else {
                    retVal = false;
                }

                rs.close();
                s.close();
            } catch (SQLException se) {
                retVal = false;
                printSQLError(se);
            }
        } else if (uid == -1) {
            // broken link
            ec.setShortName(EPOC_MISSING);
        }

        return retVal;
    }

    /**
     * Attempt to load Element object from storage given object's uid
     * @return  boolean true on success
     */
    private boolean loadElement(Element ele, boolean loadChildren) {
        int uid = ele.getUID();
        boolean retVal = true;
        String sql;

        if (uid > 0) {
            try {
                Statement s = conn.createStatement();
                sql ="SELECT * FROM element WHERE uid = " + uid;
                outputSQL(sql);
                ResultSet rs = s.executeQuery(sql);

                if (rs.next()) {
                    ele.setParentUID(rs.getInt("parent_uid"));
                    ele.setModType(rs.getInt("modtype"));
                    ele.setName(rs.getString("name"));
                    ele.setShortName(rs.getString("shortname"));
                    ele.setEClassUID(rs.getInt("eclass_uid"));
                    ele.setEPOCClassName(rs.getString("classname"));
                    ele.setMorph(rs.getString("morph"));
                    ele.setEPOCID(rs.getString("epocid"));
                    ele.setRevision(rs.getString("version"));
                    ele.setCreated(new java.util.Date(rs.getTimestamp("created").getTime()));
                    ele.setModified(new java.util.Date(rs.getTimestamp("modified").getTime()));
                    ele.setDescription(rs.getString("description"));
                    ele.setAuthor(rs.getString("creator"));
                    ele.setBirthDate(rs.getInt("birthday"), rs.getInt("birthmonth"));
                    ele.setPolygonsString(rs.getString("polygons"));
                    ele.setLocked(rs.getBoolean("locked"));
                    ele.setPosition(rs.getInt("position"));

                    // Add self to templates if it is a template
                    if (ele.getParentUID() == 0 && ele.getTemplates() != null
                            && ele.getTemplates().getFromTemplateList(OBJ_ELE, ele.getUID()) == null) {
                        ele.getTemplates().addTemplateList(ele);
                    }

                    if (loadChildren) {
                        rs.close();

                        // load attributes
                        Attribute att;
                        sql = "SELECT uid, position, 'uniq' as type FROM attribute WHERE parent_uid = " + uid + " " +
                              "UNION " +
                              "SELECT uid, position, 'tmpl' as type FROM template WHERE parent_uid = " + uid + " " +
                              "AND objecttype = " + OBJ_ATT + " " +
                              "ORDER BY position";
                        outputSQL(sql);
                        rs = s.executeQuery(sql);
                        while (rs.next()) {
                            if (rs.getString("type").equals("uniq") || ele.getTemplates() == null) {
                                att = new Attribute(rs.getInt("uid"), ele.getTemplates());    // causes it to be loaded
                            } else {
                                att = (Attribute)ele.templates.getFromTemplateList(OBJ_ATT, rs.getInt("uid"));
                            }
                            if (rs.getString("type").equals("tmpl")) att.setPosition(rs.getInt("position"));
                            ele.addAttribute(att);
                        }
                        rs.close();

                        // load actions
                        Action act;
                        sql = "SELECT uid, position, 'uniq' as type FROM action WHERE parent_uid = " + uid + " " +
                              "UNION " +
                              "SELECT uid, position, 'tmpl' as type FROM template WHERE parent_uid = " + uid + " " +
                              "AND objecttype = " + OBJ_ACT + " " +
                              "ORDER BY position";
                        outputSQL(sql);
                        rs = s.executeQuery(sql);
                        while (rs.next()) {
                            if (rs.getString("type").equals("uniq") || ele.getTemplates() == null) {
                                act = new Action(rs.getInt("uid"), ele.getTemplates());    // causes it to be loaded
                            } else {
                                act = (Action)ele.templates.getFromTemplateList(OBJ_ACT, rs.getInt("uid"));
                            }
                            if (rs.getString("type").equals("tmpl")) act.setPosition(rs.getInt("position"));
                            ele.addAction(act);
                        }
                    }
                } else {
                    retVal = false;
                }

                rs.close();
                s.close();
            } catch (SQLException se) {
                retVal = false;
                printSQLError(se);
            }
        } else if (uid == -1) {
            // broken link
            ele.setShortName(EPOC_MISSING);
        }

        return retVal;
    }

    /**
     * Attempt to load Action object from storage given object's uid
     * @return  boolean true on success
     */
    private boolean loadAction(Action act) {
        int uid = act.getUID();
        boolean retVal = true;
        String sql;

        if (uid > 0) {
            try {
                Statement s = conn.createStatement();
                sql ="SELECT * FROM action WHERE uid = " + uid;
                outputSQL(sql);
                ResultSet rs = s.executeQuery(sql);

                if (rs.next()) {
                    act.setParentUID(rs.getInt("parent_uid"));
                    act.setShortName(rs.getString("shortname"));
                    act.setRevision(rs.getString("version"));
                    act.setCreated(new java.util.Date(rs.getTimestamp("created").getTime()));
                    act.setModified(new java.util.Date(rs.getTimestamp("modified").getTime()));
                    act.setDescription(rs.getString("description"));
                    act.setActType(rs.getInt("acttype"));
                    act.setDatasetUID(rs.getInt("dataset"));
                    act.setTransformUID(rs.getInt("transform"));
                    act.setRelatedUIDsFromString(rs.getString("related"));
                    act.setCode(rs.getString("code"));
                    act.setLocked(rs.getBoolean("locked"));
                    act.setPosition(rs.getInt("position"));

                    // Add self to templates if it is a template
                    if (act.getParentUID() == 0
                            && act.getTemplates() != null && act.getTemplates().getFromTemplateList(OBJ_ACT, act.getUID()) == null) {
                        act.getTemplates().addTemplateList(act);
                    }

                    // load timesteps
                    Timestep ts;
                    sql = "SELECT uid, 'used' as type FROM timestep WHERE parent_uid = " + uid + " " +
                          "UNION " +
                          "SELECT uid, 'tmpl' as type FROM template WHERE parent_uid = " + uid + " " +
                          "AND objecttype = " + OBJ_TS + " " +
                          "ORDER BY uid";
                    outputSQL(sql);
                    rs = s.executeQuery(sql);
                    while (rs.next()) {
                        if (rs.getString("type").equals("used") || act.getTemplates() == null) {
                            ts = new Timestep(rs.getInt("uid"), act.getTemplates());   // causes it to be loaded
                        } else {
                            ts = (Timestep)act.templates.getFromTemplateList(OBJ_TS, rs.getInt("uid"));
                            ts.setTemplates(act.getTemplates());
                            ts.setPosition(rs.getInt("position"));
                        }
                        act.addTimestep(ts);
                    }

                } else {
                    retVal = false;
                }

                rs.close();
                s.close();
            } catch (SQLException se) {
                retVal = false;
                printSQLError(se);
            }
        } else if (uid == -1) {
            // broken link
            act.setShortName(EPOC_MISSING);
        }

        return retVal;
    }

    /**
     * Attempt to load Attribute object from storage given object's uid
     * @return  boolean true on success
     */
    private boolean loadAttribute(Attribute att) {
        int uid = att.getUID();
        boolean retVal = true;
        String sql;

        if (uid > 0) {
            try {
                Statement s = conn.createStatement();
                sql ="SELECT * FROM attribute WHERE uid = " + uid;
                outputSQL(sql);
                ResultSet rs = s.executeQuery(sql);

                if (rs.next()) {
                    att.setParentUID(rs.getInt("parent_uid"));
                    att.setShortName(rs.getString("shortname"));
                    att.setRevision(rs.getString("version"));
                    att.setCreated(new java.util.Date(rs.getTimestamp("created").getTime()));
                    att.setModified(new java.util.Date(rs.getTimestamp("modified").getTime()));
                    att.setDescription(rs.getString("description"));
                    att.setValue(rs.getString("value"));
                    att.setLocked(rs.getBoolean("locked"));
                    att.setPosition(rs.getInt("position"));

                    // Add self to templates if it is a templates
                    if (att.getParentUID() == 0
                            && att.getTemplates() != null && att.getTemplates().getFromTemplateList(OBJ_ATT, att.getUID()) == null) {
                        att.getTemplates().addTemplateList(att);
                    }

                } else {
                    retVal = false;
                }

                rs.close();
                s.close();
            } catch (SQLException se) {
                retVal = false;
                printSQLError(se);
            }
        } else if (uid == -1) {
            // broken link
            att.setShortName(EPOC_MISSING);
        }

        return retVal;
    }

    /**
     * Attempt to load Actins timesteps from storage given object's uid
     * @return  boolean true on success
     */
    private boolean loadTimestep(Timestep ts) {
        int uid = ts.getUID();
        boolean retVal = true;
        String sql;

        if (uid > 0) {
            try {
                Statement s = conn.createStatement();
                sql ="SELECT * FROM timestep WHERE uid = " + uid;
                outputSQL(sql);
                ResultSet rs = s.executeQuery(sql);
                
                while (rs.next()) {
                    ts.setParentUID(rs.getInt("parent_uid"));
                    ts.setSteps(rs.getInt("startday"), rs.getInt("startmonth"),
                                rs.getInt("endday"), rs.getInt("endmonth"));
                    ts.setStepType(rs.getInt("steptype"));
                    ts.setStepTiming(rs.getInt("steptiming"));
                    ts.setDatasetUID(rs.getInt("dataset"));

                    // Add self to templates if it is a templates
                    if (ts.getParentUID() == 0
                            && ts.getTemplates() != null && ts.getTemplates().getFromTemplateList(OBJ_TS, ts.getUID()) == null) {
                        ts.getTemplates().addTemplateList(ts);
                    }
                }
                
                rs.close();
                s.close();
            } catch (SQLException se) {
                retVal = false;
                printSQLError(se);
            }
        } else if (uid == -1) {
            // broken link
            ts.setShortName(EPOC_MISSING);
        }
        
        return retVal;
    }
    
    /*
     * Save the passed Universe object to storage.
     * If saveChildren indicated then save universe Element objects etc.
     * This will also save any used templates and create links to them.
     * @return  boolean true on success
     */
    private boolean saveUniverse(Universe uni, boolean saveChildren) {
        int uid = uni.getUID();
        boolean retVal = true;
        String sql;
        
        try {
            if (uid < 0) return retVal; // Do not save broken objects with a uid < 0
            if (uni.isTemplate()) uni.setParentUID(0);

            // First save Universe data
            Statement s = conn.createStatement();

            if (uid == 0) {
                 // else insert data
                sql = "INSERT INTO universe (parent_uid, name, shortname, classname, morph, epocid, version, " +
                          "description, creator, created, modified, controller, locked) " +
                          "VALUES(0, '" + escStr(uni.getName()) + "', '" +
                          escStr(uni.getShortName()) + "', '" + escStr(uni.getEPOCClassName()) + "', '" +
                          escStr(uni.getMorph()) + "', '" + uni.getEPOCID() + "', '" +
                          uni.getRevision() + "', '" + escStr(uni.getDescription()) + "', '" +
                          escStr(uni.getAuthor()) + "', '" + new Timestamp(uni.getCreated().getTime()) + "', '" +
                          new Timestamp(uni.getModified().getTime()) + "', '" +
                          escStr(uni.getController()) + "', " + (uni.isLocked() ? 1 : 0) + ")";
                outputSQL(sql);
                s.execute(sql);

                // find new uid and add it to object
                uni.setUID(getLastInsertUID());
            } else if (uid > 0) {
                // then update data
                sql = "UPDATE universe SET name = '" + escStr(uni.getName()) + "', " +
                                "shortname = '" + escStr(uni.getShortName()) + "', " +
                                "classname = '" + escStr(uni.getEPOCClassName()) + "', " +
                                "morph = '" + escStr(uni.getMorph()) + "', " +
                                "epocid = '" + uni.getEPOCID() + "', " +
                                "version = '" + uni.getRevision() + "', " +
                                "description = '" + escStr(uni.getDescription()) + "', " +
                                "creator = '" + escStr(uni.getAuthor()) + "', " +
                                "created = '" + new Timestamp(uni.getCreated().getTime()) + "', " +
                                "modified = '" + new Timestamp(uni.getModified().getTime()) + "', " +
                                "controller = '" + escStr(uni.getController()) + "', " +
                                "locked = " + (uni.isLocked() ? 1 : 0) + " " +
                                "WHERE uid = " + uid;
                outputSQL(sql);
                s.executeUpdate(sql);                 
            }
            s.close();

            // Save Universe config objects
            // Spatial
            if (uni.getSpatial() != null) {
                Spatial spa = uni.getSpatial();
                // if neither is a template then make sure spatial has new pid
                if (!uni.isTemplate() && !spa.isTemplate()) spa.setParentUID(uni.getUID());
                // call save spatial
                if (!spa.saveToStorage()) retVal = false;
                if (spa.isTemplate()) {
                    // then also needs a link added
                    if (!linkTemplate(uni.getUID(), spa)) retVal = false;
                }
            }
            /*
            // Save any unused spatial templates
            for (Object obj : uni.getUnusedTemplateArray(OBJ_SPA)) {
                if (((EPOCObject)obj).isTemplate()) {
                    if (!((EPOCObject)obj).saveToStorage()) retVal = false;
                }
            }
            */
            // Report
            if (uni.getReport() != null) {
                Report rep = uni.getReport();
                // if neither is a template then make sure report has new pid
                if (!uni.isTemplate() && !rep.isTemplate()) rep.setParentUID(uni.getUID());
                // call save report
                if (!rep.saveToStorage()) retVal = false;
                if (rep.isTemplate()) {
                    // then also needs a link added
                    if (!linkTemplate(uni.getUID(), rep)) retVal = false;
                }
            }
            /*
            // Save any unused report templates
            for (Object obj : uni.getUnusedTemplateArray(OBJ_REP)) {
                if (((EPOCObject)obj).isTemplate()) {
                    if (!((EPOCObject)obj).saveToStorage()) retVal = false;
                }
            }
            */
            // Trial
            int i = 1;
            for (Trial tri : uni.getTrials()) {
                // rebuild position
                tri.setPosition(i);

                // if neither is a template then make sure trial has new pid
                if (!uni.isTemplate() && !tri.isTemplate()) tri.setParentUID(uni.getUID());
                // call save trial
                if (!tri.saveToStorage()) retVal = false;
                if (tri.isTemplate()) {
                    // then also needs a link added
                    if (!linkTemplate(uni.getUID(), tri)) retVal = false;
                }
                i++;
            }
            /*
            // Save any unused trial templates
            for (Object obj : uni.getUnusedTemplateArray(OBJ_TRI)) {
                if (((EPOCObject)obj).isTemplate()) {
                    if (!((EPOCObject)obj).saveToStorage()) retVal = false;
                }
            }
            */
            if (saveChildren) {
                // Now save each element of each element type of universe
                for (int eleType : new int[]{OBJ_ELE, OBJ_ENV, OBJ_ATY, OBJ_MAN, OBJ_OUT}) {
                    i = 1;
                    for (Element ele : uni.getElements(eleType)) {
                        // rebuild position
                        ele.setPosition(i);

                        // if neither is a template then make sure element has new pid
                        if (!uni.isTemplate() && !ele.isTemplate()) ele.setParentUID(uni.getUID());

                        // call save element
                        if (!ele.saveToStorage()) retVal = false;
                        if (ele.isTemplate()) {
                            // then also needs a link added
                            if (!linkTemplate(uni.getUID(), ele)) retVal = false;
                        }
                        i++;
                    }
                }
            }
            
            if (retVal) {
                conn.commit();
            }
            
        } catch (SQLException se) {
            retVal = false;
            printSQLError(se);
            try {
                conn.rollback();
            } catch (SQLException ser) {
                printSQLError(ser);
            }
        }
        
        return retVal;
    }

    /*
     * Save the passed Spatial object to storage
     * @return  boolean true on success
     */
    private boolean saveSpatial(Spatial spa) {
        int uid = spa.getUID();
        boolean retVal = true;
        String sql;

        try {
            if (uid < 0) return retVal;
            if (spa.isTemplate()) spa.setParentUID(0);

            // Save element data
            Statement s = conn.createStatement();

            if (uid == 0) {
                // else insert data
                sql = "INSERT INTO spatial (parent_uid, name, shortname, classname, morph, epocid, " +
                          "version, created, modified, description, polygons, polyoverlaps, locked) " +
                          "VALUES(" + spa.getParentUID() + ", '" + escStr(spa.getName()) + "', '" +
                          escStr(spa.getShortName()) + "', '" + escStr(spa.getEPOCClassName()) + "', '" +
                          escStr(spa.getMorph()) + "', '" + spa.getEPOCID() + "', '" + spa.getRevision() + "', '" +
                          new Timestamp(spa.getCreated().getTime()) + "', '" +
                          new Timestamp(spa.getModified().getTime()) + "', '" +
                          escStr(spa.getDescription()) + "', '" + escStr(spa.getPolygonsString()) + "', '" +
                          escStr(spa.getOverlapsString()) + "', " + (spa.isLocked() ? 1 : 0) + ")";
                outputSQL(sql);
                s.execute(sql);

                // find new uid and add it to object
                spa.setUID(getLastInsertUID());
            } else if (uid > 0) {
                // then update data
                sql = "UPDATE spatial SET " +
                                "parent_uid = " + spa.getParentUID() + ", " +
                                "name = '" + escStr(spa.getName()) + "', " +
                                "shortname = '" + escStr(spa.getShortName()) + "', " +
                                "classname = '" + escStr(spa.getEPOCClassName()) + "', " +
                                "morph = '" + escStr(spa.getMorph()) + "', " +
                                "epocid = '" + spa.getEPOCID() + "', " +
                                "version = '" + spa.getRevision() + "', " +
                                "created = '" + new Timestamp(spa.getCreated().getTime()) + "', " +
                                "modified = '" + new Timestamp(spa.getModified().getTime()) + "', " +
                                "description = '" + escStr(spa.getDescription()) + "', " +
                                "polygons = '" + escStr(spa.getPolygonsString()) + "', " +
                                "polyoverlaps = '" + escStr(spa.getOverlapsString()) + "', " +
                                "locked = " + (spa.isLocked() ? 1 : 0) + " " +
                                "WHERE uid = " + uid;
                outputSQL(sql);
                s.executeUpdate(sql);
            }
            s.close();

            if (retVal) {
                conn.commit();
            }
        } catch (SQLException se) {
            retVal = false;
            printSQLError(se);
            try {
                conn.rollback();
            } catch (SQLException ser) {
                printSQLError(ser);
            }
        }

        return retVal;
    }

    /*
     * Save the passed Report object to storage
     * @return  boolean true on success
     */
    private boolean saveReport(Report rep) {
        int uid = rep.getUID();
        boolean retVal = true;
        String sql;

        try {
            if (uid < 0) return retVal;
            if (rep.isTemplate()) rep.setParentUID(0);

            // Save element data
            Statement s = conn.createStatement();

            if (uid == 0) {
                // else insert data
                sql = "INSERT INTO report (parent_uid, name, shortname, classname, morph, epocid, version, " +
                          "created, modified, description, logprint, logfilename, calendarprint, " +
                          "calendarfilename, debug, headline1, headline2, headline3, headline4, locked) " +
                          "VALUES(" + rep.getParentUID() + ", '" +
                          escStr(rep.getName()) + "', '" + escStr(rep.getShortName()) + "', '" + escStr(rep.getEPOCClassName()) + "', '" +
                          escStr(rep.getMorph()) + "', '" + rep.getEPOCID() + "', '" +rep.getRevision() + "', '" +
                          new Timestamp(rep.getCreated().getTime()) + "', '" +
                          new Timestamp(rep.getModified().getTime()) + "', '" +
                          escStr(rep.getDescription()) + "', " + (rep.getLogPrint() ? 1 : 0) + ", '" +
                          escStr(rep.getLogFilename()) + "', " + (rep.getCalendarPrint() ? 1 : 0) + ", '" +
                          escStr(rep.getCalendarFilename()) + "', " + (rep.getDebug() ? 1 : 0) + ", '" +
                          escStr(rep.getHeadline(1)) + "', '" + escStr(rep.getHeadline(2)) + "', '" +
                          escStr(rep.getHeadline(3)) + "', '" + escStr(rep.getHeadline(4)) + "', " +
                          (rep.isLocked() ? 1 : 0) + ")";
                outputSQL(sql);
                s.execute(sql);

                // find new uid and add it to object
                rep.setUID(getLastInsertUID());
            } else if (uid > 0) {
                // then update data
                sql = "UPDATE report SET " +
                                "parent_uid = " + rep.getParentUID() + ", " +
                                "name = '" + escStr(rep.getName()) + "', " +
                                "shortname = '" + escStr(rep.getShortName()) + "', " +
                                "classname = '" + escStr(rep.getEPOCClassName()) + "', " +
                                "morph = '" + escStr(rep.getMorph()) + "', " +
                                "epocid = '" + rep.getEPOCID() + "', " +
                                "version = '" + rep.getRevision() + "', " +
                                "created = '" + new Timestamp(rep.getCreated().getTime()) + "', " +
                                "modified = '" + new Timestamp(rep.getModified().getTime()) + "', " +
                                "description = '" + escStr(rep.getDescription()) + "', " +
                                "logprint = " + (rep.getLogPrint() ? 1 : 0) + ", " +
                                "logfilename = '" + escStr(rep.getLogFilename()) + "', " +
                                "calendarprint = " + (rep.getCalendarPrint() ? 1 : 0) + ", " +
                                "calendarfilename = '" + escStr(rep.getCalendarFilename()) + "', " +
                                "debug = " + (rep.getDebug() ? 1 : 0) + ", " +
                                "headline1 = '" + escStr(rep.getHeadline(1)) + "', " +
                                "headline2 = '" + escStr(rep.getHeadline(2)) + "', " +
                                "headline3 = '" + escStr(rep.getHeadline(3)) + "', " +
                                "headline4 = '" + escStr(rep.getHeadline(4)) + "', " +
                                "locked = " + (rep.isLocked() ? 1 : 0) + " " +
                                "WHERE uid = " + uid;
                outputSQL(sql);
                s.executeUpdate(sql);
            }
            s.close();

            if (retVal) {
                conn.commit();
            }
        } catch (SQLException se) {
            retVal = false;
            printSQLError(se);
            try {
                conn.rollback();
            } catch (SQLException ser) {
                printSQLError(ser);
            }
        }

        return retVal;
    }

    /*
     * Save the passed Trial object to storage
     * @return  boolean true on success
     */
    private boolean saveTrial(Trial tri) {
        int uid = tri.getUID();
        boolean retVal = true;
        String sql;

        try {
            if (uid < 0) return retVal;
            if (tri.isTemplate()) tri.setParentUID(0);

            // Save element data
            Statement s = conn.createStatement();

            if (uid == 0) {
                // else insert data
                sql = "INSERT INTO trial (parent_uid, name, shortname, classname, morph, epocid, version, " +
                          "created, modified, description, yearstart, yearend, firstfishingyear, lastfishingyear, " +
                          "trialdir, locked, position) " +
                          "VALUES(" + tri.getParentUID() + ", '" +
                          escStr(tri.getName()) + "', '" + escStr(tri.getShortName()) + "', '" + escStr(tri.getEPOCClassName()) + "', '" +
                          escStr(tri.getMorph()) + "', '" + tri.getEPOCID() + "', '" + tri.getRevision() + "', '" +
                          new Timestamp(tri.getCreated().getTime()) + "', '" +
                          new Timestamp(tri.getModified().getTime()) + "', '" +
                          escStr(tri.getDescription()) + "', '" + tri.getYearStart() + "', '" +
                          tri.getYearEnd() + "', '" + tri.getFishingStart() + "', '" +
                          tri.getFishingEnd() + "', '" + escStr(tri.getTrialDir()) + "', " +
                          (tri.isLocked() ? 1 : 0) + ", " + tri.getPosition() + ")";
                outputSQL(sql);
                s.execute(sql);

                // find new uid and add it to object
                tri.setUID(getLastInsertUID());
            } else if (uid > 0) {
                // then update data
                sql = "UPDATE trial SET " +
                                "parent_uid = " + tri.getParentUID() + ", " +
                                "name = '" + escStr(tri.getName()) + "', " +
                                "shortname = '" + escStr(tri.getShortName()) + "', " +
                                "classname = '" + escStr(tri.getEPOCClassName()) + "', " +
                                "morph = '" + escStr(tri.getMorph()) + "', " +
                                "epocid = '" + tri.getEPOCID() + "', " +
                                "version = '" + tri.getRevision() + "', " +
                                "created = '" + new Timestamp(tri.getCreated().getTime()) + "', " +
                                "modified = '" + new Timestamp(tri.getModified().getTime()) + "', " +
                                "description = '" + escStr(tri.getDescription()) + "', " +
                                "yearstart = '" + tri.getYearStart() + "', " +
                                "yearend = '" + tri.getYearEnd() + "', " +
                                "firstfishingyear = '" + tri.getFishingStart() + "', " +
                                "lastfishingyear = '" + tri.getFishingEnd() + "', " +
                                "trialdir = '" + escStr(tri.getTrialDir()) + "', " +
                                "locked = " + (tri.isLocked() ? 1 : 0) + ", " +
                                "position = " + tri.getPosition() + " " +
                                "WHERE uid = " + uid;
                outputSQL(sql);
                s.executeUpdate(sql);
            }
            s.close();

            if (retVal) {
                conn.commit();
            }
        } catch (SQLException se) {
            retVal = false;
            printSQLError(se);
            try {
                conn.rollback();
            } catch (SQLException ser) {
                printSQLError(ser);
            }
        }

        return retVal;
    }

    /*
     * Save the passed EClass object to storage
     * @return  boolean true on success
     */
    private boolean saveEClass(EClass ec) {
        int uid = ec.getUID();
        boolean retVal = true;
        String sql;

        try {
            if (uid < 0) return retVal;
            if (ec.isTemplate()) ec.setParentUID(0);

            // Save element data
            Statement s = conn.createStatement();

            if (uid == 0) {
                // else insert data
                sql = "INSERT INTO eclass (parent_uid, modtype, name, shortname, " +
                      "version, created, modified, description, initclass, inittrial, " +
                      "inittransition, printstate, updatestate, locked, position) " +
                      "VALUES(" + ec.getParentUID() + ", " + ec.getModType() + ", '" +
                      escStr(ec.getName()) + "', '" + escStr(ec.getShortName()) + "', '" +
                      ec.getRevision() + "', '" + new Timestamp(ec.getCreated().getTime()) + "', '" +
                      new Timestamp(ec.getModified().getTime()) + "', '" + escStr(ec.getDescription()) + "', '" +
                      escStr(ec.getInitClass()) + "', '" + escStr(ec.getInitTrial()) + "', '" +
                      escStr(ec.getInitTransition()) + "', '" + escStr(ec.getPrintState()) + "', '" +
                      escStr(ec.getUpdateState()) + "', " +
                      (ec.isLocked() ? 1 : 0) + ", " + ec.getPosition() + ")";
                outputSQL(sql);
                s.execute(sql);

                // find new uid and add it to object
                ec.setUID(getLastInsertUID());
            } else if (uid > 0) {
                // then update data
                sql = "UPDATE eclass SET " +
                                "parent_uid = " + ec.getParentUID() + ", " +
                                "modtype = " + ec.getModType() + ", " +
                                "name = '" + escStr(ec.getName()) + "', " +
                                "shortname = '" + escStr(ec.getShortName()) + "', " +
                                "version = '" + ec.getRevision() + "', " +
                                "created = '" + new Timestamp(ec.getCreated().getTime()) + "', " +
                                "modified = '" + new Timestamp(ec.getModified().getTime()) + "', " +
                                "description = '" + escStr(ec.getDescription()) + "', " +
                                "initclass = '" + escStr(ec.getInitClass()) + "', " +
                                "inittrial = '" + escStr(ec.getInitTrial()) + "', " +
                                "inittransition = '" + escStr(ec.getInitTransition()) + "', " +
                                "printstate = '" + escStr(ec.getPrintState()) + "', " +
                                "updatestate = '" + escStr(ec.getUpdateState()) + "', " +
                                "locked = " + (ec.isLocked() ? 1 : 0) + ", " +
                                "position = " + ec.getPosition() + " " +
                                "WHERE uid = " + uid;
                outputSQL(sql);
                s.executeUpdate(sql);
            }
            s.close();

            if (retVal) {
                conn.commit();
            }
        } catch (SQLException se) {
            retVal = false;
            printSQLError(se);
            try {
                conn.rollback();
            } catch (SQLException ser) {
                printSQLError(ser);
            }
        }

        return retVal;
    }

    /*
     * Save the passed Element object to storage
     * If saveChildren indicated then save universe Element objects etc.
     * This will also save any used templates and create links to them
     * @return  boolean true on success
     */
    private boolean saveElement(Element ele, boolean saveChildren) {
        int uid = ele.getUID();
        boolean retVal = true;
        String sql;

        try {
            if (uid < 0) return retVal;
            if (ele.isTemplate()) ele.setParentUID(0);

            // Save element data
            Statement s = conn.createStatement();

            // Check first that eclass object has been assigned
            // uid (been saved),
            if (ele.getEClass() != null && ele.getEClassUID() == 0) {
                retVal = ele.getEClass().saveToStorage();
            }

            if (uid == 0) {
                // else insert data
                sql = "INSERT INTO element (parent_uid, modtype, name, shortname, " +
                      "eclass_uid, classname, morph, epocid, version, created, " +
                      "modified, description, creator, birthday, " +
                      "birthmonth, polygons, locked, position) " +
                      "VALUES(" + ele.getParentUID() + ", " +
                      ele.getModType() + ", '" + escStr(ele.getName()) + "', '" +
                      escStr(ele.getShortName()) + "', " + (ele.getEClass() != null ? ele.getEClass().getUID() : 0) + ", '" +
                      escStr(ele.getEPOCClassName()) + "', '" + escStr(ele.getMorph()) + "', '" + ele.getEPOCID() + "', '" +
                      ele.getRevision() + "', '" + new Timestamp(ele.getCreated().getTime()) + "', '" +
                      new Timestamp(ele.getModified().getTime()) + "', '" + escStr(ele.getDescription()) + "', '" +
                      escStr(ele.getAuthor()) + "', " + ele.getBirthDay() + ", " + ele.getBirthMonth() + ", '" +
                      escStr(ele.getPolygonsString()) + "', " + (ele.isLocked() ? 1 : 0) + ", "+ ele.getPosition() + ")";
                outputSQL(sql);
                s.execute(sql);

                // find new uid and add it to object
                ele.setUID(getLastInsertUID());
            } else if (uid > 0) {
                // then update data
                sql = "UPDATE element SET " +
                                "parent_uid = " + ele.getParentUID() + ", " +
                                "modtype = " + ele.getModType() + ", " +
                                "name = '" + escStr(ele.getName()) + "', " +
                                "shortname = '" + escStr(ele.getShortName()) + "', " +
                                "eclass_uid = " + (ele.getEClass() != null ? ele.getEClass().getUID() : 0) + ", " +
                                "classname = '" + escStr(ele.getEPOCClassName()) + "', " +
                                "morph = '" + escStr(ele.getMorph()) + "', " +
                                "epocid = '" + ele.getEPOCID() + "', " +
                                "version = '" + ele.getRevision() + "', " +
                                "created = '" + new Timestamp(ele.getCreated().getTime()) + "', " +
                                "modified = '" + new Timestamp(ele.getModified().getTime()) + "', " +
                                "creator = '" + escStr(ele.getAuthor()) + "', " +
                                "description = '" + escStr(ele.getDescription()) + "', " +
                                "birthday = " + ele.getBirthDay() + ", " +
                                "birthmonth = " + ele.getBirthMonth() + ", " +
                                "polygons = '" + escStr(ele.getPolygonsString()) + "', " +
                                "locked = " + (ele.isLocked() ? 1 : 0) + ", " +
                                "position = " + ele.getPosition() + " " +
                                "WHERE uid = " + uid;
                outputSQL(sql);
                s.executeUpdate(sql);
            }
            s.close();

            if (saveChildren) {

                // Now save each attribute (must happen before actions so action.dataset has uid)
                int i = 1;
                for (Attribute att : ele.getAttributes()) {
                    // rebuild position
                    att.setPosition(i);

                    // if neither is a template then make sure attribute has new pid
                    //if (!ele.isTemplate() && !att.isTemplate()) att.setParentUID(ele.getUID());
                    if (!att.isTemplate()) att.setParentUID(ele.getUID());
                    // call save attribute
                    if (!att.saveToStorage()) retVal = false;
                    if (att.isTemplate()) {
                        // then also needs a link added
                        if (!linkTemplate(ele.getUID(), att)) retVal = false;
                    }
                    i++;
                }

                // and each action of element
                i = 1;
                for (Action act : ele.getActions()) {
                    // rebuild position
                    act.setPosition(i);

                    // if neither is a template then make sure action has new pid
                    //if (!ele.isTemplate() && !act.isTemplate()) act.setParentUID(ele.getUID());
                    if (!act.isTemplate()) act.setParentUID(ele.getUID());
                    // call save action
                    if (!act.saveToStorage()) retVal = false;
                    if (act.isTemplate()) {
                        // then also needs a link added
                        if (!linkTemplate(ele.getUID(), act)) retVal = false;
                    }
                    i++;
                }
            }

            if (retVal) {
                conn.commit();
            }

        } catch (SQLException se) {
            retVal = false;
            printSQLError(se);
            try {
                conn.rollback();
            } catch (SQLException ser) {
                printSQLError(ser);
            }
        }

        return retVal;
    }

    /*
     * Save the passed action object to storage
     * @return  boolean true on success
     */
    private boolean saveAction(Action act) {
        int uid = act.getUID();
        boolean retVal = true;
        String sql;
        
        try {
            if (uid < 0) return retVal;
            if (act.isTemplate()) act.setParentUID(0);

            // Save element data
            Statement s = conn.createStatement();

            // Check first that dataset attribute, transform action and related elements have been assigned
            // uids (been saved),
            if (act.getDataset() != null && act.getDatasetUID() == 0) {
                retVal = act.getDataset().saveToStorage();
            }
            if (!act.isSetup()) {
                if (act.getTransform() != null && act.getTransformUID() == 0) {
                    retVal = act.getTransform().saveToStorage();
                }

                for (Element rEle : act.getRelatedElements()) {
                    if (rEle.getUID() == 0) {
                        if (!rEle.saveToStorage()) retVal = false;
                    }
                }
            }

            if (uid == 0) {
                // else insert data
                sql = "INSERT INTO action (parent_uid, shortname, version, created, modified, " +
                          "description, acttype, dataset, transform, related, code, locked, position) " +
                          "VALUES(" + act.getParentUID() + ", '" +
                          escStr(act.getShortName()) + "', '" + act.getRevision() + "', '" +
                          new Timestamp(act.getCreated().getTime()) + "', '" +
                          new Timestamp(act.getModified().getTime()) + "', '" +
                          escStr(act.getDescription()) + "', " + act.getActType() + ", " +
                          act.getDatasetUID() + ", " + act.getTransformUID() + ", '" +
                          act.getRelatedUIDsString() + "', '" + escStr(act.getCode()) + "', " +
                          (act.isLocked() ? 1 : 0) + ", " + act.getPosition() + ")";
                outputSQL(sql);
                s.execute(sql);

                // find new uid and add it to object
                act.setUID(getLastInsertUID());
            } else if (uid > 0) {
                // then update data
                sql = "UPDATE action SET " +
                                "parent_uid = " + act.getParentUID() + ", " +
                                "shortname = '" + escStr(act.getShortName()) + "', " +
                                "version = '" + act.getRevision() + "', " +
                                "created = '" + new Timestamp(act.getCreated().getTime()) + "', " +
                                "modified = '" + new Timestamp(act.getModified().getTime()) + "', " +
                                "description = '" + escStr(act.getDescription()) + "', " +
                                "acttype = " + act.getActType() + ", " +
                                "dataset = " + act.getDatasetUID() + ", " +
                                "transform = " + act.getTransformUID() + ", " +
                                "related = '" + act.getRelatedUIDsString() + "', " +
                                "code = '" + escStr(act.getCode()) + "', " +
                                "locked = " + (act.isLocked() ? 1 : 0) + ", " +
                                "position = " + act.getPosition() + " " +
                                "WHERE uid = " + uid;
                outputSQL(sql);
                s.executeUpdate(sql);                 
            }

            if (!act.isSetup()) {
                // Now save each timestep of action
                for (Timestep ts : act.getTimesteps()) {
                    ts.setParentUID(act.getUID());
                    if (!ts.saveToStorage()) retVal = false;
                }
            }
            s.close();
            
            if (retVal) {
                conn.commit();
            }
        } catch (SQLException se) {
            retVal = false;
            printSQLError(se);
            try {
                conn.rollback();
            } catch (SQLException ser) {
                printSQLError(ser);
            }
        }
        
        return retVal;
    }
    
    /*
     * Save the passed Attribute object to storage
     * @return  boolean true on success
     */
    private boolean saveAttribute(Attribute att) {
        int uid = att.getUID();
        boolean retVal = true;
        String sql;
        
        try {
            if (uid < 0) return retVal;
            if (att.isTemplate()) att.setParentUID(0);

            // Save element data
            Statement s = conn.createStatement();

            if (uid == 0) {
                // else insert data
                sql = "INSERT INTO attribute (parent_uid, shortname, version, created, " +
                          "modified, description, value, locked, position) " +
                          "VALUES(" + att.getParentUID() + ", '" +
                          escStr(att.getShortName()) + "', '" + att.getRevision() + "', '" +
                          new Timestamp(att.getCreated().getTime()) + "', '" +
                          new Timestamp(att.getModified().getTime()) + "', '" +
                          escStr(att.getDescription()) + "', '" + escStr(att.getValue()) + "', " +
                          (att.isLocked() ? 1 : 0) + ", " + att.getPosition() + ")";
                outputSQL(sql);
                s.execute(sql);

                // find new uid and add it to object
                att.setUID(getLastInsertUID());
            } else if (uid > 0) {
                // then update data
                sql = "UPDATE attribute SET " +
                                "parent_uid = " + att.getParentUID() + ", " +
                                "shortname = '" + escStr(att.getShortName()) + "', " +
                                "version = '" + att.getRevision() + "', " +
                                "created = '" + new Timestamp(att.getCreated().getTime()) + "', " +
                                "modified = '" + new Timestamp(att.getModified().getTime()) + "', " +
                                "description = '" + escStr(att.getDescription()) + "', " +
                                "value = '" + escStr(att.getValue()) + "', " +
                                "locked = " + (att.isLocked() ? 1 : 0) + ", " +
                                "position = " + att.getPosition() + " " +
                                "WHERE uid = " + uid;
                outputSQL(sql);
                s.executeUpdate(sql);               
            }
            s.close();
            
            if (retVal) {
                conn.commit();
            }
        } catch (SQLException se) {
            retVal = false;
            printSQLError(se);
            try {
                conn.rollback();
            } catch (SQLException ser) {
                printSQLError(ser);
            }
        }
        
        return retVal;
    }

    /*
     * Save the passed timestep object to storage
     * @return  boolean true on success
     */
    private boolean saveTimestep(Timestep ts) {
        int uid = ts.getUID();
        boolean retVal = true;
        String sql;
        
        try {
            if (uid < 0) return retVal;
            if (ts.isTemplate()) ts.setParentUID(0);

            // Save element data
            Statement s = conn.createStatement();

            // Check first that dataset attribute has been assigned
            // uid (been saved)
            int dsuid = 0;
            if (ts.getDatasetUID() == -1) {
                // Broken link
                dsuid = -1;
            } else if (ts.getDataset() != null && ts.getDatasetUID() >= 0) {
                retVal = ts.getDataset().saveToStorage();
                dsuid = ts.getDatasetUID();
            }

            if (uid == 0) {
                // else insert data
                sql = "INSERT INTO timestep (parent_uid, startday, startmonth, endday, endmonth, " +
                      "steptype, steptiming, dataset) VALUES(" + ts.getParentUID() + ", " +
                          ts.getStartDay() + ", " + ts.getStartMonth() + ", " +
                          ts.getEndDay() + ", " + ts.getEndMonth() + ", " +
                          ts.getStepType() + ", " + ts.getStepTiming() + ", " + dsuid + ")";
                outputSQL(sql);
                s.execute(sql);

                // find new uid and add it to object
                ts.setUID(getLastInsertUID());
            } else if (uid > 0) {
                // then update data
                sql = "UPDATE timestep SET " +
                                "parent_uid = " + ts.getParentUID() + ", " +
                                "startday = " + ts.getStartDay() + ", " +
                                "startmonth = " + ts.getStartMonth() + ", " +
                                "endday = " + ts.getEndDay() + ", " +
                                "endmonth = " + ts.getEndMonth() + ", " +
                                "steptype = " + ts.getStepType() + ", " +
                                "steptiming = " + ts.getStepTiming() + ", " +
                                "dataset = " + dsuid + " " +
                                "WHERE uid = " + uid;
                outputSQL(sql);
                s.executeUpdate(sql);               
            }

            s.close();
            
            if (retVal) {
                conn.commit();
            }
        } catch (SQLException se) {
            retVal = false;
            printSQLError(se);
            try {
                conn.rollback();
            } catch (SQLException ser) {
                printSQLError(ser);
            }
        }
        
        return retVal;
    }
    
     /**
     * Attempt to delete universe data members from storage given object's uid
     * @return  boolean true on success
     */
    private boolean deleteUniverse(int uid, boolean delChildren) {
        boolean retVal = true;
        String sql;
        
        if (uid > 0) {
            try {
                Statement s = conn.createStatement();
                sql ="DELETE FROM universe WHERE uid = " + uid;
                outputSQL(sql);
                s.executeUpdate(sql);



                if (delChildren) {
                    Element ele;
                    sql = "SELECT uid FROM element WHERE parent_uid = " + uid + " ORDER BY uid";
                    ResultSet rs = s.executeQuery(sql);
                    // call delete on each element of this universe
                    while (rs.next()) {
                        if (!deleteElement(rs.getInt("uid"), delChildren)) {
                            retVal = false;
                        }
                    }
                    rs.close();
                    
                    // Remove any template links to universe
                    sql = "DELETE FROM template WHERE parent_uid = " + uid + " " +
                          "AND objecttype = " + OBJ_ELE;
                    outputSQL(sql);
                    s.executeUpdate(sql);
                    
                }
                s.close();
                
                if (retVal) {
                    conn.commit();
                }
                
            } catch (SQLException se) {
                retVal = false;
                printSQLError(se);
                try {
                    conn.rollback();
                } catch (SQLException ser) {
                    printSQLError(ser);
                }
            }
        }
        
        return retVal;
    }

    /**
     * Attempt to delete EPOCClass data members from storage given object's uid
     * @return  boolean true on success
     */
    private boolean deleteEClass(int uid) {
        boolean retVal = true;
        String sql;

        if (uid > 0) {
            try {
                Statement s = conn.createStatement();
                sql ="DELETE FROM eclass WHERE uid = " + uid;
                outputSQL(sql);
                s.executeUpdate(sql);
                s.close();

                conn.commit();
            } catch (SQLException se) {
                retVal = false;
                printSQLError(se);
                try {
                    conn.rollback();
                } catch (SQLException ser) {
                    printSQLError(ser);
                }
            }
        }

        return retVal;
    }

    /**
     * Attempt to delete Element data members from storage given object's uid
     * @return  boolean true on success
     */
    private boolean deleteElement(int uid, boolean delChildren) {
        boolean retVal = true;
        String sql;
        
        if (uid > 0) {
            try {
                Statement s = conn.createStatement();
                sql ="DELETE FROM element WHERE uid = " + uid;
                outputSQL(sql);
                s.executeUpdate(sql);
                
                if (delChildren) {
                    // Actions
                    Action act;
                    sql = "SELECT uid FROM action WHERE parent_uid = " + uid + " ORDER BY uid";
                    ResultSet rs = s.executeQuery(sql);
                    // call delete on each action of this element
                    while (rs.next()) {
                        if (!deleteAction(rs.getInt("uid"))) {
                            retVal = false;
                        }
                    }
                    
                    // Remove any action template links to element
                    sql = "DELETE FROM template WHERE parent_uid = " + uid + " " +
                          "AND objecttype = " + OBJ_ACT;
                    outputSQL(sql);
                    s.executeUpdate(sql);
                    
                    // Attributes
                    Attribute att;
                    sql = "SELECT uid FROM attribute WHERE parent_uid = " + uid + " ORDER BY uid";
                    rs = s.executeQuery(sql);
                    // call delete on each attribute of this element
                    while (rs.next()) {
                        if (!deleteAttribute(rs.getInt("uid"))) {
                            retVal = false;
                        }
                    }
                    rs.close();
                    // Remove any attribute template links to element
                    sql = "DELETE FROM template WHERE parent_uid = " + uid + " " +
                          "AND objecttype = " + OBJ_ATT;
                    outputSQL(sql);
                    s.executeUpdate(sql);
                }
                s.close();
                if (retVal) {
                    conn.commit();
                }
                
            } catch (SQLException se) {
                retVal = false;
                printSQLError(se);
                try {
                    conn.rollback();
                } catch (SQLException ser) {
                    printSQLError(ser);
                }
            }
        }
        
        return retVal;
    }
    
    /**
     * Attempt to delete Action data members from storage given object's uid
     * @return  boolean true on success
     */
    private boolean deleteAction(int uid) {
        boolean retVal = true;
        String sql;
        
        if (uid > 0) {
            try {
                Statement s = conn.createStatement();
                sql ="DELETE FROM action WHERE uid = " + uid;
                outputSQL(sql);
                s.executeUpdate(sql);
                
                Timestep ts;
                sql = "SELECT uid FROM timestep WHERE parent_uid = " + uid + " ORDER BY uid";
                ResultSet rs = s.executeQuery(sql);
                // call delete on each timestep of this action
                while (rs.next()) {
                    if (!deleteTimestep(rs.getInt("uid"))) {
                        retVal = false;
                    }
                }
                rs.close();
                s.close();
                if (retVal) {
                    conn.commit();
                }
            } catch (SQLException se) {
                retVal = false;
                printSQLError(se);
                try {
                    conn.rollback();
                } catch (SQLException ser) {
                    printSQLError(ser);
                }
            }
        }
        
        return retVal;
    }
    
    /**
     * Attempt to delete Attribute data members from storage given object's uid
     * @return  boolean true on success
     */
    private boolean deleteAttribute(int uid) {
        boolean retVal = true;
        String sql;
        
        if (uid > 0) {
            try {
                Statement s = conn.createStatement();
                sql ="DELETE FROM attribute WHERE uid = " + uid;
                outputSQL(sql);
                s.executeUpdate(sql);
                s.close();
               
                conn.commit();
            } catch (SQLException se) {
                retVal = false;
                printSQLError(se);
                try {
                    conn.rollback();
                } catch (SQLException ser) {
                    printSQLError(ser);
                }
            }
        }
        
        return retVal;
    }

    /**
     * Attempt to delete Universe Spatial object from storage given object's uid
     * @return  boolean true on success
     */
    private boolean deleteSpatial(int uid) {
        boolean retVal = true;
        String sql;

        if (uid > 0) {
            try {
                Statement s = conn.createStatement();
                sql ="DELETE FROM spatial WHERE uid = " + uid;
                outputSQL(sql);
                s.executeUpdate(sql);
                s.close();

                conn.commit();
            } catch (SQLException se) {
                retVal = false;
                printSQLError(se);
                try {
                    conn.rollback();
                } catch (SQLException ser) {
                    printSQLError(ser);
                }
            }
        }

        return retVal;
    }

    /**
     * Attempt to delete Universe Report object from storage given object's uid
     * @return  boolean true on success
     */
    private boolean deleteReport(int uid) {
        boolean retVal = true;
        String sql;

        if (uid > 0) {
            try {
                Statement s = conn.createStatement();
                sql ="DELETE FROM report WHERE uid = " + uid;
                outputSQL(sql);
                s.executeUpdate(sql);
                s.close();

                conn.commit();
            } catch (SQLException se) {
                retVal = false;
                printSQLError(se);
                try {
                    conn.rollback();
                } catch (SQLException ser) {
                    printSQLError(ser);
                }
            }
        }

        return retVal;
    }

    /**
     * Attempt to delete Universe Trial object from storage given object's uid
     * @return  boolean true on success
     */
    private boolean deleteTrial(int uid) {
        boolean retVal = true;
        String sql;

        if (uid > 0) {
            try {
                Statement s = conn.createStatement();
                sql ="DELETE FROM trial WHERE uid = " + uid;
                outputSQL(sql);
                s.executeUpdate(sql);
                s.close();

                conn.commit();
            } catch (SQLException se) {
                retVal = false;
                printSQLError(se);
                try {
                    conn.rollback();
                } catch (SQLException ser) {
                    printSQLError(ser);
                }
            }
        }

        return retVal;
    }

    /**
     * Attempt to delete Timestep data members from storage given object's uid
     * @return  boolean true on success
     */
    private boolean deleteTimestep(int uid) {
        boolean retVal = true;
        String sql;
        
        if (uid > 0) {
            try {
                Statement s = conn.createStatement();
                sql ="DELETE FROM timestep WHERE uid = " + uid;
                outputSQL(sql);
                s.executeUpdate(sql);
                s.close();
                
                conn.commit();
            } catch (SQLException se) {
                retVal = false;
                printSQLError(se);
                try {
                    conn.rollback();
                } catch (SQLException ser) {
                    printSQLError(ser);
                }
            }
        }
        
        return retVal;
    }

    /**
      * Retrieve the setting value for the setting passed
     */
    public String getSetting(String setting) {
        String sql, value = "";

        try {
            Statement s = conn.createStatement();
            sql = "SELECT * FROM settings WHERE setting = '" + setting + "'";
            ResultSet rs = s.executeQuery(sql);

            if (rs.next()) {
                value = rs.getString("value");
            }
            s.close();
            rs.close();
        } catch (SQLException se) {
            printSQLError(se);
        }

        return value;
    }

    /**
     * Save setting, value and description to settings table
     *
     * @param setting
     * @param value
     * @param description
     */
    public void setSetting(String setting, String value, String description) {
        String sql;

        try {
            Statement s = conn.createStatement();
            sql = "SELECT * FROM settings WHERE setting = '" + setting + "'";
            ResultSet rs = s.executeQuery(sql);

            if (rs.next()) {

                // then update data
                sql = "UPDATE settings SET value = '" + value + "', description = '" + escStr(description) + "' " +
                                "WHERE setting = '" + setting + "'";
                outputSQL(sql);
                s.executeUpdate(sql);

            } else {

                // else insert data
                sql = "INSERT INTO settings VALUES('" + setting + "', '" + value + "', " +
                      "'" + escStr(description) + "')";
                outputSQL(sql);
                s.execute(sql);
            }
            s.close();

            conn.commit();
        } catch (SQLException se) {
            printSQLError(se);
            try {
                conn.rollback();
            } catch (SQLException ser) {
                printSQLError(ser);
            }
        }
    }

    /**
     * Save last visited Universe uid in settings table
     */
    public void setLastVisited(int uid) {
        setSetting("last_visited", String.valueOf(uid), "Last visited universe");
    }
    
     /**
      * Retrieve Universe object indicated as last visited in settings table
      * else return new Universe object
     */
    public Universe getLastVisited() {
        Universe uni = null;
        String sql, value = "";

        value = getSetting("last_visited");
        
        if (!value.equals("") && Integer.parseInt(value) > 0) {
            uni = new Universe(Integer.parseInt(value)); // caused it to be loaded
        }
        
        if (uni == null) {
            uni = new Universe();                       // return a blank one
        }
        return uni;
    }
    
    private boolean updateSchema(String dbVer) {
        // Need to switch on dbVer to find schema updates required
        try {
            if (dbVer.equals("0.1.0") || dbVer.equals("0.2.0")) {
                Statement s = conn.createStatement();

                s.execute("CREATE TABLE spatial(uid INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY, " +
                        "parent_uid INT, name VARCHAR(100), shortname VARCHAR(50), " +
                        "classname VARCHAR(50), morph VARCHAR(20), id INT, version VARCHAR(50), " +
                        "description VARCHAR(512), polygons LONG VARCHAR, bparent VARCHAR(50))");
                s.execute("CREATE TABLE report(uid INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY, " +
                        "parent_uid INT, name VARCHAR(100), shortname VARCHAR(50), " +
                        "classname VARCHAR(50), morph VARCHAR(20), id INT, version VARCHAR(50), " +
                        "description VARCHAR(512), logprint SMALLINT, logfilename VARCHAR(100), " +
                        "calendarprint SMALLINT, calendarfilename VARCHAR(100), debug SMALLINT, " +
                        "headline1 VARCHAR(100), headline2 VARCHAR(100), headline3 VARCHAR(100), " +
                        "headline4 VARCHAR(100), bparent VARCHAR(50))");
                s.execute("CREATE TABLE trial(uid INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY, " +
                        "parent_uid INT, name VARCHAR(100), shortname VARCHAR(50), " +
                        "classname VARCHAR(50), morph VARCHAR(20), id INT, version VARCHAR(50), " +
                        "description VARCHAR(512), yearstart VARCHAR(4), yearend VARCHAR(4), " +
                        "firstfishingyear VARCHAR(4), lastfishingyear VARCHAR(4), trialdir VARCHAR(100), " +
                        "bparent VARCHAR(50))");
                
                s.execute("ALTER TABLE universe ADD COLUMN shortname VARCHAR(20)");
                s.execute("ALTER TABLE element ADD COLUMN shortname VARCHAR(20)");
                s.execute("ALTER TABLE universe ADD COLUMN classname VARCHAR(50)");
                s.execute("ALTER TABLE element ADD COLUMN classname VARCHAR(50)");
                s.execute("ALTER TABLE universe ADD COLUMN morph VARCHAR(20)");
                s.execute("ALTER TABLE element ADD COLUMN morph VARCHAR(20)");
                s.execute("ALTER TABLE universe ADD COLUMN id INT");
                s.execute("ALTER TABLE element ADD COLUMN id INT");
                s.execute("ALTER TABLE element ADD COLUMN creator VARCHAR(50)");
                
                s.execute("UPDATE settings SET value = '" + EPOC_VER + "' WHERE setting = 'Version'");

                s.close();
            }

            if (dbVer.equals("0.1.0") || dbVer.equals("0.2.0") || dbVer.equals("0.3.0")) {
                Statement s = conn.createStatement();

                s.execute("ALTER TABLE element ADD COLUMN created TIMESTAMP");
                s.execute("ALTER TABLE element ADD COLUMN modified TIMESTAMP");
                s.execute("ALTER TABLE spatial ADD COLUMN created TIMESTAMP");
                s.execute("ALTER TABLE spatial ADD COLUMN modified TIMESTAMP");
                s.execute("ALTER TABLE report ADD COLUMN created TIMESTAMP");
                s.execute("ALTER TABLE report ADD COLUMN modified TIMESTAMP");
                s.execute("ALTER TABLE trial ADD COLUMN created TIMESTAMP");
                s.execute("ALTER TABLE trial ADD COLUMN modified TIMESTAMP");

                s.execute("UPDATE settings SET value = '" + EPOC_VER + "' WHERE setting = 'Version'");

                s.close();
            }

            if (dbVer.equals("0.1.0") || dbVer.equals("0.2.0") || dbVer.equals("0.3.0") || dbVer.equals("0.3.1")) {
                Statement s = conn.createStatement();

                // change column name from id to epocid
                s.execute("ALTER TABLE universe DROP COLUMN id");
                s.execute("ALTER TABLE element DROP COLUMN id");
                s.execute("ALTER TABLE spatial DROP COLUMN id");
                s.execute("ALTER TABLE report DROP COLUMN id");
                s.execute("ALTER TABLE trial DROP COLUMN id");

                s.execute("ALTER TABLE universe ADD COLUMN epocid VARCHAR(10)");
                s.execute("ALTER TABLE element ADD COLUMN epocid VARCHAR(10)");
                s.execute("ALTER TABLE spatial ADD COLUMN epocid VARCHAR(10)");
                s.execute("ALTER TABLE report ADD COLUMN epocid VARCHAR(10)");
                s.execute("ALTER TABLE trial ADD COLUMN epocid VARCHAR(10)");

                // add position column
                s.execute("ALTER TABLE universe ADD COLUMN position SMALLINT");
                s.execute("ALTER TABLE element ADD COLUMN position SMALLINT");
                s.execute("ALTER TABLE action ADD COLUMN position SMALLINT");
                s.execute("ALTER TABLE attribute ADD COLUMN position SMALLINT");
                s.execute("ALTER TABLE template ADD COLUMN position SMALLINT");
                s.execute("ALTER TABLE trial ADD COLUMN position SMALLINT");

                // add locked columns
                s.execute("ALTER TABLE universe ADD COLUMN locked SMALLINT");
                s.execute("ALTER TABLE spatial ADD COLUMN locked SMALLINT");
                s.execute("ALTER TABLE trial ADD COLUMN locked SMALLINT");
                s.execute("ALTER TABLE report ADD COLUMN locked SMALLINT");
                s.execute("ALTER TABLE element ADD COLUMN locked SMALLINT");
                s.execute("ALTER TABLE action ADD COLUMN locked SMALLINT");
                s.execute("ALTER TABLE attribute ADD COLUMN locked SMALLINT");
                s.execute("ALTER TABLE template ADD COLUMN locked SMALLINT");
                s.execute("ALTER TABLE trial ADD COLUMN position SMALLINT");

                // add some indexes
                s.execute("CREATE UNIQUE INDEX uni_ver_idx ON universe(version)");
                s.execute("CREATE UNIQUE INDEX ele_ver_idx ON element(version)");
                s.execute("CREATE UNIQUE INDEX spa_ver_idx ON spatial(version)");
                s.execute("CREATE UNIQUE INDEX rep_ver_idx ON report(version)");
                s.execute("CREATE UNIQUE INDEX tri_ver_idx ON trial(version)");
                s.execute("CREATE UNIQUE INDEX act_ver_idx ON action(version)");
                s.execute("CREATE UNIQUE INDEX att_ver_idx ON attribute(version)");
                s.execute("CREATE UNIQUE INDEX tmp_pid_idx ON template(parent_uid)");
                s.execute("CREATE UNIQUE INDEX tmp_uid_idx ON template(uid)");

                s.execute("UPDATE settings SET value = '" + EPOC_VER + "' WHERE setting = 'Version'");

                s.close();
            }

            /** 0.3.5
            // Add new eclass table
            CREATE TABLE eclass(uid INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
                        parent_uid INT, modtype INT, name VARCHAR(100), shortname VARCHAR(50), version VARCHAR(50),
                        created TIMESTAMP, modified TIMESTAMP,
                        description VARCHAR(512), initclass LONG VARCHAR, inittrial LONG VARCHAR,
                        initTransition LONG VARCHAR, printstate LONG VARCHAR, updatestate LONG VARCHAR,
                        bparent VARCHAR(50), locked SMALLINT, position SMALLINT);
            CREATE INDEX ecl_ver_idx ON eclass(version);
            // Alter element table
            ALTER TABLE element ADD COLUMN eclass_uid INT;
            CREATE INDEX ele_eid_idx ON element(eclass_uid);
            ALTER TABLE element ADD COLUMN modtype INT;
            UPDATE element SET modtype = type;
            ALTER TABLE element DROP COLUMN type;
            // Alter Action table
            ALTER TABLE action ADD COLUMN acttype INT;
            UPDATE action SET acttype = type;
            ALTER TABLE action DROP COLUMN type;
            ALTER TABLE Action ADD COLUMN created TIMESTAMP;
            ALTER TABLE Action ADD COLUMN modified TIMESTAMP;
            // And Attribute
            ALTER TABLE Action ADD COLUMN created TIMESTAMP;
            ALTER TABLE Action ADD COLUMN modified TIMESTAMP;
            // Update new TIMESTAMP columns
            UPDATE action SET created = CURRENT_TIMESTAMP WHERE created IS NULL;
            UPDATE action SET modified = CURRENT_TIMESTAMP WHERE modified IS NULL;
            UPDATE attribute SET created = CURRENT_TIMESTAMP WHERE created IS NULL;
            UPDATE attribute SET modified = CURRENT_TIMESTAMP WHERE modified IS NULL;
            **/

        } catch (Throwable e) {
            if (e instanceof SQLException) {
                printSQLError((SQLException) e);
            } else {
                e.printStackTrace();
            }

            return false;
        }

        return true;
    }
    
    /**
     * Return a string containing the db table name which holds elements of
     * type objType
     */
    private String getTableName(int objType) {
        switch (objType) {
            case OBJ_UNI:
                return "universe";
            case OBJ_CLS:
                return "eclass";
            case OBJ_ELE: 
            case OBJ_BIO: 
            case OBJ_ENV: 
            case OBJ_ATY: 
            case OBJ_MAN: 
            case OBJ_OUT:
                return "element";
            case OBJ_ACT:
                return "action";
            case OBJ_ATT:
                return "attribute";
            case OBJ_TS:
                return "timestep";
            case OBJ_SPA:
                return "spatial";
            case OBJ_REP:
                return "report";
            case OBJ_TRI:
                return "trial";
        }
        
        return null;
    }
    
    /**
     * Return the name of the table containing parent objects for the
     * objType passed.
     * @param objType
     * @return
     */
    private String getParentTableName(int objType) {
        switch (objType) {
            case OBJ_ACT:
            case OBJ_ATT:
                return "element";
            case OBJ_TS:
                return "action";
            case OBJ_UNI:
                return null;
        }
        return "universe";
    }

    private int getLastInsertUID() {
        int lastUID = 0;
        
        try {
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery("VALUES IDENTITY_VAL_LOCAL()");
        
            if (rs.next()) {
                lastUID = rs.getInt(1);
                //System.out.println("Last UID=" + lastUID);
            }

            rs.close();
            s.close();
        } catch (SQLException se) {
            printSQLError(se);
        }
        
        return lastUID;
    }

    private String escStr(String str) {
        return str.replaceAll("'", "''");
    }

    private void printSQLError(SQLException e) {
        while (e != null) {
            outputSQL(e.toString());
            Messages.addErrMsg(e.toString());
            //errMsg = e.toString() + (!errMsg.equals("") ? "\n" + errMsg : "");
            e = e.getNextException();
        }
    }
    
    /**
     * Write sql string passed to System.out if EPOC_DEBUG flag is set
     * 
     * @param sql
     */
    private void outputSQL(String sql) {
        if (EPOC_DBG) System.out.println(sql);
    }

    /**
     * Return any outstanding error message from previous operation
     * "" if none exists.
     * Reset error message to ""
     *
    public String getErrMsg() {
        String currMsg = errMsg;
        errMsg = "";
        return currMsg;
    }
*/
    public void setErrMsg(String msg) {
        Messages.addErrMsg(msg);
        //errMsg = msg + (!errMsg.equals("") ? "\n" + errMsg : "");
    }

    /*
     * Attempt to shutdown any open database
     */
    public static synchronized void shutdownDB() {
        boolean shutExCaught = false;
        
        try {
            conn.close();
            if (EPOC_DBG) System.out.println(protocol + ";shutdown=true");
            DriverManager.getConnection(protocol + ";shutdown=true");
        } catch (SQLException se) {
            shutExCaught = true;
        }
        if (!shutExCaught) {
            System.out.println("Database did not shut down normally");
        }
    }
    
    protected void finalize() throws Throwable {
        boolean shutExCaught = false;
        
        try {
            conn.close();

            /*
               In embedded mode, an application should shut down Derby.
               If the application fails to shut down Derby explicitly,
               the Derby does not perform a checkpoint when the JVM shuts down, which means
               that the next connection will be slower.
               Explicitly shutting down Derby with the URL is preferred.
               This style of shutdown will always throw an "exception".
             */
            try {
                DriverManager.getConnection(protocol + ";shutdown=true");
            } catch (SQLException se) {
                shutExCaught = true;
            }
            if (!shutExCaught) {
                System.out.println("Database did not shut down normally");
            }
        
        } finally {
            super.finalize();
        }
    }
    
}
