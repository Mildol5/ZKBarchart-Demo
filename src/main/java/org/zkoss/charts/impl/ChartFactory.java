/** ChartFactory.java.

	Purpose:
		
	Description:
		
	History:
		16:22:18 AM May 6, 2013, Created by jumperchen

Copyright (C) 2013 Potix Corporation. All Rights Reserved.
 */
package org.zkoss.charts.impl;

import org.zkoss.charts.Charts;

/**
 * @author jumperchen
 * 
 */
public class ChartFactory {
	public static ChartEngineBase createCartesianChart(Charts chart) {
		return new CartesianChartEngine(chart);
	}
}
