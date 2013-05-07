/** ChartData.java.

	Purpose:
		
	Description:
		
	History:
		11:24:14 AM May 3, 2013, Created by jumperchen

Copyright (C) 2013 Potix Corporation. All Rights Reserved.
*/
package demo.chart.bar;

import org.zkoss.zul.CategoryModel;
import org.zkoss.zul.SimpleCategoryModel;

/**
 * A chrat model data
 * @author jumperchen
 */
public class ChartData {
	public static CategoryModel getModel(){
        CategoryModel catmodel = new SimpleCategoryModel();

    	catmodel.setValue("2011", "Quarter 1", new Integer(20));
    	catmodel.setValue("2011", "Quarter 2", new Integer(35));
    	catmodel.setValue("2011", "Quarter 3", new Integer(40));
    	catmodel.setValue("2011", "Quarter 4", new Integer(55));
    	catmodel.setValue("2012", "Quarter 1", new Integer(40));
    	catmodel.setValue("2012", "Quarter 2", new Integer(60));
    	catmodel.setValue("2012", "Quarter 3", new Integer(70));
    	catmodel.setValue("2012", "Quarter 4", new Integer(91));
    	catmodel.setValue("2013", "Quarter 1", new Integer(55));
    	catmodel.setValue("2013", "Quarter 2", new Integer(108));
    	catmodel.setValue("2013", "Quarter 3", new Integer(42));
    	catmodel.setValue("2013", "Quarter 4", new Integer(26));
    	catmodel.setValue("2014", "Quarter 1", new Integer(99));
    	catmodel.setValue("2014", "Quarter 2", new Integer(106));
    	catmodel.setValue("2014", "Quarter 3", new Integer(60));
    	catmodel.setValue("2014", "Quarter 4", new Integer(37));
        return catmodel;
    }
}
