/*******************************************************************************
 * TimestepCellRenderer.java
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

import javax.swing.*;
import java.awt.*;

/*******************************************************************************
 * ListCellRenderer for display of listed EPOC Timeline objects.
 *
 * @author Troy Robertson
 * @company Australian Antarctic Division
 * @created 10/09/2009
 * @version 0.4.0, 01-09-2010
 ******************************************************************************/
public class TimestepCellRenderer extends JPanel implements ListCellRenderer{
    JLabel act, start, end, type, time, dset;
    JButton pick;
    
    TimestepCellRenderer() {
        setLayout(new GridLayout(0,5));

        act = new JLabel();
        act.setSize(40, act.getHeight());
        act.setOpaque(true);
        this.add(act);
        start = new JLabel();
        start.setSize(20, start.getHeight());
        start.setOpaque(true);
        this.add(start);
        end = new JLabel();
        end.setSize(20, end.getHeight());
        end.setOpaque(true);
        this.add(end);
        type = new JLabel();
        type.setSize(10, type.getHeight());
        type.setOpaque(true);
        this.add(type);
        time = new JLabel();
        time.setSize(10, time.getHeight());
        time.setOpaque(true);
        this.add(time);
        dset = new JLabel();
        dset.setOpaque(true);
        this.add(dset);
    }

    public Component getListCellRendererComponent(JList list, Object value, int index,
                                                boolean isSelected, boolean cellHasFocus){
        Color fcol = list.getForeground();
        Timestep step = ((Timestep)value);
        
        // Action
        act.setFont(list.getFont().deriveFont(Font.PLAIN));
        if (step.getAction() == null) {
            act.setText("NULL");
        } else {
            act.setText(step.getAction().toString());
            if (EPOC_DBG) {
                act.setToolTipText(String.valueOf(step.getAction().getUID()));
            } else {
                act.setToolTipText("<html>" + step.getAction().getDescription().replace("\n", "<br>") + "</html>");
            }

            if (step.getAction().isTemplate()) act.setFont(list.getFont().deriveFont(Font.BOLD));
            // If broken link colour RED
            if (step.getAction().getUID() < 0) fcol = Color.RED;
        }
        // Start and Ends
        start.setText(intToMonth(step.getStartMonth()) + " " + 
                (step.getStartDay() > 0 && step.getStartDay() < 99 ? step.getStartDay() : ""));
        end.setText(intToMonth(step.getEndMonth()) + " " + 
                (step.getEndDay() > 0 && step.getEndDay() < 99 ? step.getEndDay() : ""));
        type.setText(intToType(step.getStepType()));
        time.setText(intToTime(step.getStepTiming()));
        // Dataset
        dset.setFont(list.getFont().deriveFont(Font.PLAIN));
        if (step.getDataset() == null) {
            dset.setText("NULL");
        } else {
            dset.setText(step.getDataset().toString());
            if (EPOC_DBG) {
                dset.setToolTipText(String.valueOf(step.getDataset().getUID()));
            } else {
                dset.setToolTipText(step.getDataset().getDescription());
            }

            if (step.getDataset().isTemplate()) dset.setFont(list.getFont().deriveFont(Font.BOLD));
            // If broken link colour RED
            if (step.getDataset().getUID() < 0) fcol = Color.RED;
        }

        // bold new
        if (step.getStartMonth() == 0) {
            start.setFont(start.getFont().deriveFont(Font.BOLD));
            end.setFont(end.getFont().deriveFont(Font.BOLD));
        } else {
            start.setFont(start.getFont().deriveFont(Font.PLAIN));
            end.setFont(end.getFont().deriveFont(Font.PLAIN));
        }
        
        if (isSelected){
            start.setBackground(list.getSelectionBackground());
            start.setForeground(list.getSelectionForeground());
            end.setBackground(list.getSelectionBackground());
            end.setForeground(list.getSelectionForeground());
            type.setBackground(list.getSelectionBackground());
            type.setForeground(list.getSelectionForeground());
            time.setBackground(list.getSelectionBackground());
            time.setForeground(list.getSelectionForeground());
            if (fcol == Color.RED) {
                act.setBackground(Color.RED);
                dset.setBackground(Color.RED);
            } else {
                act.setBackground(list.getSelectionBackground());
                dset.setBackground(list.getSelectionBackground());
            }
            act.setForeground(list.getSelectionForeground());
            dset.setForeground(list.getSelectionForeground());
        } else {
            act.setBackground(list.getBackground());
            act.setForeground(fcol);
            start.setBackground(list.getBackground());
            start.setForeground(list.getForeground());
            end.setBackground(list.getBackground());
            end.setForeground(list.getForeground());
            type.setBackground(list.getBackground());
            type.setForeground(list.getForeground());
            time.setBackground(list.getBackground());
            time.setForeground(list.getForeground());
            dset.setBackground(list.getBackground());
            dset.setForeground(fcol);
        }
        
        setEnabled(list.isEnabled());
        setFont(list.getFont());
        return this;
    }
    
    private String intToMonth(int mth) {
        switch (mth) {
            case 0:
                return "New ...";
            case 1:
                return "January";
            case 2:
                return "February";
            case 3:
                return "March";
            case 4:
                return "April";
            case 5:
                return "May";
            case 6:
                return "June";
            case 7:
                return "July";
            case 8:
                return "August";
            case 9:
                return "September";
            case 10:
                return "October";
            case 11:
                return "November";
            case 12:
                return "December";
            case 99:
                return "Birthday";
        }
        
        return "";
    }
    
    private String intToType(int tp) {
        switch (tp) {
            case TS_ALL:
                return "All";
            case TS_FST:
                return "First";
            case TS_LST:
                return "Last";
        }
        
        return "";
    }

    private String intToTime(int tm) {
        switch (tm) {
            case TS_BEF:
                return "Before";
            case TS_DUR:
                return "During";
            case TS_AFT:
                return "After";
        }

        return "";
    }
} 