/*******************************************************************************
 * RevisionTreeModel.java
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
import javax.swing.tree.*;
import java.util.Collections;
import java.util.Comparator;

/*******************************************************************************
 * EPOC TreeModel for organising objects into a revision tree.
 *
 * @author Troy Robertson
 * @company Australian Antarctic Division
 * @created 14/04/2010
 * @version 0.4.0, 01-09-2010
 ******************************************************************************/
public class RevisionTreeModel extends DefaultTreeModel {

    private Comparator comparator = null;

    public RevisionTreeModel(TreeNode rootNode, int compMethod) {
        super(rootNode);
        comparator = new EPOCObjectListComparator(compMethod);
        super.addTreeModelListener(new UniverseTreeModelListener());
    }

    public RevisionTreeModel(int compMethod) {
        this(new DefaultMutableTreeNode(new Attribute("")), compMethod);
    }

    /**
     * Build a tree model representing the universe data members.
     * @param list of all EPOC objects
     * @return universe tree model
     */
    public void buildModel(ArrayList<EPOCObject> list) {
        Collections.sort(list, comparator);
        // Repeat while there any orphaned objects left in list
        int i = 1;
        while (list.size() > 0) addChildTreeNodes((DefaultMutableTreeNode)super.getRoot(), list, i++);
    }

    /**
     * Add any objects from list which are revisions of parent user object (which are generationGap generations from parent),
     * as child nodes of parent.
     * @param parent tree node
     * @param list of all EPOC objects
     */
    private void addChildTreeNodes(DefaultMutableTreeNode parentNode, ArrayList<EPOCObject> list, int generationGap) {
        DefaultMutableTreeNode childNode;
        for (EPOCObject eo : (ArrayList<EPOCObject>)list.clone()) {
            // First work from
            if (eo.isDescendentOf((EPOCObject)parentNode.getUserObject(), generationGap)) {
                childNode = new DefaultMutableTreeNode(eo);
                list.remove(eo);
                addChildTreeNodes(childNode, list, 1);
                super.insertNodeInto(childNode, parentNode, findIndexFor(childNode, parentNode));
            }
        }
    }

    /**
     * Find the index point at which childNode should be inserted into parentNode
     * This is of course dependent on EPOCObjectListComparator and the compare method selected
     * @param childNode
     * @param parentNode
     * @return
     */
    private int findIndexFor(DefaultMutableTreeNode childNode, DefaultMutableTreeNode parentNode) {
        int cnt = parentNode.getChildCount();
        if (cnt == 0) return 0;
        
        return findIndexFor(childNode, parentNode, 0, cnt - 1);
    }

    private int findIndexFor(DefaultMutableTreeNode childNode, DefaultMutableTreeNode parentNode, int stIdx, int endIdx) {
        if (stIdx == endIdx) return comparator.compare((EPOCObject)childNode.getUserObject(),
                (EPOCObject)((DefaultMutableTreeNode)parentNode.getChildAt(stIdx)).getUserObject()) <= 0 ? stIdx : stIdx + 1;

        int half = (stIdx + endIdx) / 2;
        if (comparator.compare((EPOCObject)childNode.getUserObject(),
                (EPOCObject)((DefaultMutableTreeNode)parentNode.getChildAt(half)).getUserObject()) <= 0) return findIndexFor(childNode, parentNode, stIdx, half);

        return findIndexFor(childNode, parentNode, half + 1, endIdx);
    }

}
