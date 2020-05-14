/*******************************************************************************
 * TimelineUI.java
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

import java.util.Calendar;
import java.util.Date;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.renderer.category.GanttRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.IntervalCategoryDataset;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;

/*******************************************************************************
 * GUI for displaying an action timeline in the form of a Gantt Chart using
 * jfree.chart.
 *
 * @author Troy Robertson
 * @company Australian Antarctic Division
 * @created 15/09/2009
 * @version 0.4.0, 01-09-2010
 ******************************************************************************/
public class TimelineUI extends javax.swing.JFrame {
    
    private static Universe universe;
    
    /** Creates new form TimelineUI */
    public TimelineUI(Universe uni) {
        universe = uni;
        initComponents();
        this.setTitle("Action Gantt Chart");
        generate();
    }
  
    /**
     * Creates a new demo.
     */
    public void generate() {
        //org.jfree.chart.renderer.category.GanttRenderer.setDefaultShadowsVisible(false);
        final IntervalCategoryDataset dataset = createDataset();
        final JFreeChart chart = createChart(dataset);

        // add the chart to a panel...
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(800, 600));
        setContentPane(chartPanel);
    }

    // ****************************************************************************
    // * JFREECHART DEVELOPER GUIDE                                               *
    // * The JFreeChart Developer Guide, written by David Gilbert, is available   *
    // * to purchase from Object Refinery Limited:                                *
    // *                                                                          *
    // * http://www.object-refinery.com/jfreechart/guide.html                     *
    // *                                                                          *
    // * Sales are used to provide funding for the JFreeChart project - please    * 
    // * support us so that we can continue developing free software.             *
    // ****************************************************************************
    /**
     * Creates a dataset for a Gantt chart, comprising a TaskSeries for each module.
     *
     * @return The dataset.
     */
    private IntervalCategoryDataset createDataset() {
       
        TaskSeries biota = createTaskSeries("Biota", OBJ_BIO);
        TaskSeries environment = createTaskSeries("Environment", OBJ_ENV);
        TaskSeries activity = createTaskSeries("Activity", OBJ_ATY);
        TaskSeries management = createTaskSeries("Management", OBJ_MAN);
        TaskSeries output = createTaskSeries("Output", OBJ_OUT);
        TaskSeries presentation = createTaskSeries("Presentation", OBJ_PRE);
        
        final TaskSeriesCollection collection = new TaskSeriesCollection();
        collection.add(biota);
        collection.add(environment);
        collection.add(activity);
        collection.add(management);
        collection.add(output);
        collection.add(presentation);
        
        return collection;
    }

    /**
     * Return a TaskSeries for the module type passed.
     * @param seriesTitle
     * @param eleType
     * @return
     */
    private TaskSeries createTaskSeries(String seriesTitle, int eleType) {
        TaskSeries series = new TaskSeries(seriesTitle);
       
        for (Object eObj : universe.getElements(eleType)) {
            Element ele = (Element)eObj;
            
            for (Action act : ele.getActions()) {
                String nm = ele.getDisplayName() + " - " + act.getDisplayName();
                
                Timestep[] steps = (Timestep[])act.getTimestepArrayClone(ele.getBirthDay(), 
                                                                         ele.getBirthMonth());
                if (steps.length > 0) {
                    int stDay = steps[0].getStartDay();
                    int stMth = steps[0].getStartMonth();
                    int enDay = steps[steps.length-1].getEndDay();
                    int enMth = steps[steps.length-1].getEndMonth();
                    
                    // Need to check if timesteps rollover past Dec31
                    //if (steps[steps.length-1].getEndMonth() < steps[steps.length-1].getStartMonth()) {
                    if (enMth < stMth || (enMth == stMth && enDay < stDay)) {
                        stDay = 1; stMth = 1;           // earliest will be beginning year
                        enDay = 31; enMth = 12;         // latest will be end year
                    }
                    
                    Task tsk = new Task(nm, toStartDate(stDay, stMth), toEndDate(enDay, enMth));

                    for (int k = 0; k < steps.length; k++) {
                        stDay = steps[k].getStartDay();
                        stMth = steps[k].getStartMonth();
                        enDay = steps[k].getEndDay();
                        enMth = steps[k].getEndMonth();
                        
                        // split timestep if timestep rollsover past Dec31
                        if (steps[k].getEndMonth() < steps[k].getStartMonth()
                                || (steps[k].getEndMonth() == steps[k].getStartMonth()
                                && steps[k].getEndDay() < steps[k].getStartDay())) {
                            tsk.addSubtask(new Task(nm + String.valueOf(k) + "-1", 
                                    toStartDate(stDay, stMth), toEndDate(31, 12))); // till end of year
                            stDay = 1; stMth = 1;   // 2nd part will start back at beginning of year
                        }
                        tsk.addSubtask(new Task(nm + String.valueOf(k), 
                                    toStartDate(stDay, stMth), toEndDate(enDay, enMth)));
                    }
                    series.add(tsk);
                }
            }
        }
        
        return series;
    }

    /**
     * Take day and month and return a date with time set to 6am
     * @param day
     * @param month
     * @return
     */
    private static Date toStartDate(final int day, final int month) {
        return toDate(day, month, 6, 0);
    }

    /**
     * Take day and month and return a date with time set to 6pm
     * @param day
     * @param month
     * @return
     */
    private static Date toEndDate(final int day, final int month) {
        return toDate(day, month, 18, 0);
    }

    /**
     * Utility method for creating <code>Date</code> objects.
     *
     * @param day  the date.
     * @param month  the month.
     *
     * @return a date.
     */
    private static Date toDate(final int day, final int month, final int hour, final int min) {
        int year = Integer.parseInt(universe.getTrials().get(0).getYearStart());
        final Calendar calendar = Calendar.getInstance();
        
        if (year <= 0) year = calendar.get(Calendar.YEAR);
        calendar.set(year, month-1, day, hour, min);
        final Date result = calendar.getTime();
        return result;
    }
    
    /**
     * Creates a chart.
     * 
     * @param dataset  the dataset.
     * 
     * @return The chart.
     */
    private JFreeChart createChart(final IntervalCategoryDataset dataset) {
        final JFreeChart chart = ChartFactory.createGanttChart(
            universe.getShortName() + " Action Timeline",  // chart title
            "Action",              // domain axis label
            "Date",              // range axis label
            dataset,             // data
            true,                // include legend
            true,                // tooltips
            false                // urls
        );    
        // Set smaller font and remove bar shadows and highlights
        chart.getCategoryPlot().getDomainAxis().setTickLabelFont(chart.getCategoryPlot().getDomainAxis().getTickLabelFont().deriveFont(8f));
        ((GanttRenderer)chart.getCategoryPlot().getRenderer()).setShadowVisible(false);
        ((GanttRenderer)chart.getCategoryPlot().getRenderer()).setBarPainter(new StandardBarPainter());

        return chart;    
    }
  
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 571, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 398, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new TimelineUI(new Universe()).setVisible(true);
            }
        });
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    
}
