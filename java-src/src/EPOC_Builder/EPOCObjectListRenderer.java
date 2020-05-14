/*******************************************************************************
 * EPOCObjectListRenderer.java
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
 * EPOC Builder ListCellRenderer class.
 * Dictates list name layout/rendering/font/color of EPOC objects.
 *
 * @author Troy Robertson
 * @company Australian Antarctic Division
 * @created 10/09/2009
 * @version 0.4.0, 01-09-2010
 ******************************************************************************/
public class EPOCObjectListRenderer extends JPanel implements ListCellRenderer{
    JLabel item;
    
    EPOCObjectListRenderer() {
        setLayout(new GridLayout());
        item = new JLabel();
        item.setOpaque(true);
        item.setAlignmentX(4.0f);
        this.add(item);
    }

    public Component getListCellRendererComponent(JList list, Object value, int index,
                                                boolean isSelected, boolean cellHasFocus){
        Color fcol = list.getForeground();
        item.setFont(list.getFont().deriveFont(Font.PLAIN));
        
        if (value == null || !(value instanceof EPOCObject)) {
            item.setText(" Missing");
            fcol = Color.RED;

        } else {
            item.setText(" " + ((EPOCObject)value).toString());
            if (EPOC_DBG) {
                item.setToolTipText(String.valueOf(((EPOCObject)value).getUID()));
            } else {
                item.setToolTipText("<html>" + ((EPOCObject)value).getDescription().replace("\n", "<br>") + "</html>");
            }

            // if template then BOLD
            if (((EPOCObject)value).isTemplate()) {
                // bold templates
                item.setFont(item.getFont().deriveFont(Font.BOLD));
            }
            // if action object then colour appropriately
            if (value instanceof Action) {
                if (((Action)value).isSetup()) {
                    fcol = Color.PINK;
                } else if (((Action)value).isSupport()) {
                    fcol = Color.ORANGE;
                }
            }
            // If broken link colour RED
            if (((EPOCObject)value).getUID() < 0) fcol = Color.RED;
        }

        // Set item color
        if (isSelected){
            if (fcol == Color.RED) {
                item.setBackground(Color.RED);
            } else {
                item.setBackground(list.getSelectionBackground());
            }
            item.setForeground(list.getSelectionForeground());
        } else {
            item.setBackground(list.getBackground());
            item.setForeground(fcol);
        }
       
        return item;
    }
} 