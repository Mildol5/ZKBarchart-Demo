/** ChartEngineBase.java.

	Purpose:
		
	Description:
		
	History:
		11:11:07 AM Apr 26, 2013, Created by jumperchen

Copyright (C) 2013 Potix Corporation. All Rights Reserved.
*/
package org.zkoss.charts.impl;

import org.zkoss.graphics.Stage;

/**
 * A basic chart engine
 * @author jumperchen
 *
 */
abstract public class ChartEngineBase extends Stage {
	protected ChartEngineBase() {
	}
	public abstract ChartEngineBase build();
	public abstract ChartEngineBase redraw();
	public abstract ChartEngineBase applyStyle(String style);
}
