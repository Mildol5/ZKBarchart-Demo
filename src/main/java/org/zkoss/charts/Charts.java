package org.zkoss.charts;

import java.io.Serializable;

import org.zkoss.charts.impl.ChartEngineBase;
import org.zkoss.charts.impl.ChartFactory;
import org.zkoss.lang.Objects;
import org.zkoss.lang.Strings;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SerializableEventListener;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zul.ChartModel;
import org.zkoss.zul.Div;
import org.zkoss.zul.event.ChartDataEvent;
import org.zkoss.zul.event.ChartDataListener;

public class Charts extends Div {
	private static final long serialVersionUID = 20130425L;
	public static final String BAR = "bar";
	public static final String PIE = "pie";
	private String _type = PIE; // chart type (pie, ring, bar, line, xy, etc)
	private boolean _smartDrawChart; // whether post the smartDraw event already?
	private EventListener<Event> _smartDrawChartListener; // the smartDrawListner
	private ChartDataListener _dataListener;
	private String _chartStyle = "";
	private boolean _watermark;

	// chart data model
	private ChartModel _model; // chart data model
	private ChartEngineBase _chart;

	public Charts() {
		init();
	}

	private void init() {
		if (_smartDrawChartListener == null) {
			_smartDrawChartListener = new SmartDrawListener();
			addEventListener("onSmartDrawChart", _smartDrawChartListener);
		}
	}

	public boolean isWatermark() {
		return _watermark;
	}
	
	private boolean _legend = true;
	public void setLegend(boolean legend) {
		_legend = legend;
		if (_chart != null)
			_chart.getFellow("legend").setVisible(legend);
	}
	public boolean isLegend() {
		return _legend;
	}
	public void setWatermark(boolean watermark) {
		_watermark = watermark;
		if (_chart != null) {
			_chart.getFellow("watermark").setVisible(watermark);
		}
	}
	public void saveChartAsImage(String message,
			final EventListener<UploadEvent> listener) {
		if (_chart == null)
			throw new UiException("Chart Engine is not ready yet");
		
		_chart.saveAsImage(message, listener);
	}

	public void setChartStyle(String style) {
		if (style == null)
			style = "";
		if (!Objects.equals(_chartStyle, style)) {
			_chartStyle = style;
		}
	}

	public String getChartStyle() {
		return _chartStyle;
	}

	public void setType(String type) {
		if (Objects.equals(_type, type)) {
			return;
		}
		_type = type;
		smartDrawChart();
	}

	public String getType() {
		return _type;
	}

	/**
	 * mark a draw flag to inform that this Chart needs update.
	 */
	protected void smartDrawChart() {
		if (_smartDrawChart) { // already mark smart draw
			return;
		}
		_smartDrawChart = true;
		Events.postEvent("onSmartDrawChart", this, null);
	}

	private class SmartDrawListener implements SerializableEventListener<Event> {
		private static final long serialVersionUID = 20091008183610L;

		public void onEvent(Event event) throws Exception {
			doSmartDraw();
		}
	}

	/**
	 * Returns the chart model associated with this chart, or null if this chart
	 * is not associated with any chart data model.
	 */
	public ChartModel getModel() {
		return _model;
	}

	/**
	 * Sets the chart model associated with this chart. If a non-null model is
	 * assigned, no matter whether it is the same as the previous, it will
	 * always cause re-render.
	 * 
	 * @param model
	 *            the chart model to associate, or null to dis-associate any
	 *            previous model.
	 * @exception UiException
	 *                if failed to initialize with the model
	 */
	public void setModel(ChartModel model) {
		if (_model != model) {
			if (_model != null) {
				_model.removeChartDataListener(_dataListener);
			}
			_model = model;
			initDataListener();
		}

		// Always redraw
		smartDrawChart();
	}

	private void initDataListener() {
		if (_dataListener == null) {
			_dataListener = new MyChartDataListener();
			_model.addChartDataListener(_dataListener);
		}
	}

	private class MyChartDataListener implements ChartDataListener,
			Serializable {
		private static final long serialVersionUID = 20091008183622L;

		public void onChange(ChartDataEvent event) {
			smartDrawChart();
		}
	}

	private void doSmartDraw() {
		if (Strings.isBlank(getType()))
			throw new UiException(
					"chart must specify type (pie, bar, line, ...)");

		if (_model == null) {
			throw new UiException("chart must specify model");
		}

		try {
			// TODO: chart engine
			if (PIE.equals(getType())) {

			} else {
				initChart();
			}
		} finally {
			_smartDrawChart = false;
		}
	}
	private void initChart() {
		_chart = ChartFactory.createCartesianChart(this).build();
		_chart.applyStyle(getChartStyle());
		appendChild(_chart);
		
		_chart.getFellow("legend").setVisible(_legend);
		_chart.getFellow("watermark").setVisible(_watermark);
	}
	public void resize(String width, String height) {
		this.setWidth(width);
		this.setHeight(height);
		if (_chart != null) {
			_chart.redraw();
			_chart.getFellow("legend").setVisible(_legend);
			_chart.getFellow("watermark").setVisible(_watermark);
		}
	}
	@Override
	public void invalidate() {
		super.invalidate();
		this.getChildren().clear();
		initChart();
	}
}
