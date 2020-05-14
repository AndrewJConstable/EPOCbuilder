/*******************************************************************************
 * Messages.java
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

import java.util.ArrayList;

/*******************************************************************************
 * Message storage class providing static access methods.
 *
 * @author Troy Robertson
 * @company Australian Antarctic Division
 * @created 14/04/2010
 * @version 0.4.0, 01-09-2010
 ******************************************************************************/
public class Messages {
    private static ArrayList errorMsgs = new ArrayList();
    private static ArrayList readErrorMsgs = new ArrayList();
    private static ArrayList warningMsgs = new ArrayList();
    private static ArrayList readWarningMsgs = new ArrayList();
    private static ArrayList notificationMsgs = new ArrayList();
    private static ArrayList readNotificationMsgs = new ArrayList();

    public static void addErrMsg(String msg) {
        errorMsgs.add(msg);
    }

    /**
     * Return unread error messages as a string with line returns between each message
     * Clear read messages
     *
     * @return
     */
    public static String getUnreadErrMsgs() {
        return getUnreadErrMsgs(true);
    }

    public static void clearUnreadErrMsgs() {
        errorMsgs.addAll(readErrorMsgs);
        errorMsgs.clear();
    }

    /**
     * Return unread error messages as a string with line returns between each message
     * Clear read messages if indicated by boolean
     * 
     * @param clearRead
     * @return
     */
    public static String getUnreadErrMsgs(boolean clearRead) {
        String msgText = "";

        for (Object obj : errorMsgs) {
            msgText = msgText + (!msgText.equals("") ? "\n" : "") + (String)obj;
            if (clearRead) readErrorMsgs.add(obj);
        }
        if (clearRead) errorMsgs.clear();

        return msgText;
    }

    public static String getAllErrMsgs(boolean clearRead) {
        String msgText = "";

        if (clearRead) {
            readErrorMsgs.addAll(errorMsgs);
            errorMsgs.clear();
        } else {
            msgText = getUnreadErrMsgs(false);
        }

        for (Object obj : readErrorMsgs) {
            msgText = msgText + (!msgText.equals("") ? "\n" : "") + (String)obj;
        }

        return msgText;
    }

    public static void addNotificationMsg(String msg) {
        notificationMsgs.add(msg);
    }

    /**
     * Return unread Notification messages as a string with line returns between each message
     * Clear read messages
     *
     * @return
     */
    public static String getUnreadNotificationMsgs() {
        return getUnreadNotificationMsgs(true);
    }

    public static void clearUnreadNotificationMsgs() {
        notificationMsgs.addAll(readNotificationMsgs);
        notificationMsgs.clear();
    }

    /**
     * Return unread Notification messages as a string with line returns between each message
     * Clear read messages if indicated by boolean
     *
     * @param clearRead
     * @return
     */
    public static String getUnreadNotificationMsgs(boolean clearRead) {
        String msgText = "";

        for (Object obj : notificationMsgs) {
            msgText = msgText + (!msgText.equals("") ? "\n" : "") + (String)obj;
            if (clearRead) readNotificationMsgs.add(obj);
        }
        if (clearRead) notificationMsgs.clear();

        return msgText;
    }

    public static String getAllNotificationMsgs(boolean clearRead) {
        String msgText = "";

        if (clearRead) {
            readNotificationMsgs.addAll(notificationMsgs);
            notificationMsgs.clear();
        } else {
            msgText = getUnreadNotificationMsgs(false);
        }

        for (Object obj : readNotificationMsgs) {
            msgText = msgText + (!msgText.equals("") ? "\n" : "") + (String)obj;
        }

        return msgText;
    }
}
