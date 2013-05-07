/** CartesianChartEngine.java.

	Purpose:
		
	Description:
		
	History:
		11:10:38 AM Apr 26, 2013, Created by jumperchen

Copyright (C) 2013 Potix Corporation. All Rights Reserved.
 */
package org.zkoss.charts.impl;

import static org.zkoss.charts.util.NumericHelper.roundMinAndMax;
import static org.zkoss.charts.util.NumericHelper.roundToNearest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.zkoss.charts.Charts;
import org.zkoss.graphics.Group;
import org.zkoss.graphics.Layer;
import org.zkoss.graphics.Line;
import org.zkoss.graphics.Rect;
import org.zkoss.graphics.Text;
import org.zkoss.graphics.Animation;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.MouseEvent;
import org.zkoss.zk.ui.event.OpenEvent;
import org.zkoss.zul.CategoryModel;
import org.zkoss.zul.Popup;

/**
 * @author jumperchen
 * 
 */
public class CartesianChartEngine extends ChartEngineBase {
	private List<Comparable<?>> _series;
	private List<Comparable<?>> _categories;
	private List<Number> _values;
	private double categoryMargin = 30.0;
	private double axisXMargin = 30.0;
	private double axisYMargin = 30.0;
	private double barWidth = 15.0;
	private double tickDistance = 35.0;
	private double labelSize = 7.0;
	private int tickCount = 11;
	private List<Integer> _disableList;
	private Charts _chart;
	private Number _max;
	private int _cSize;
	private int _sSize;

	private String[] fillColors = { "#A61120", "#115FA6", "#FF8809", "#94AE0A" };
	private static String CSS_HIDDEN_TEMP = ".series_%1$s.category_%2$s {\n"
			+ "opacity: 0; y: %3$s; height: 0; transition-property: opacity, y, height;"
			+ "transition-timing-function: ease-in; transition-duration: 0.5s;}\n";
	private static String CSS_SHOW_TEMP = ".series_%1$s.category_%2$s {\n"
			+ "x: %3$s; opacity: 1; y: %4$s; height: %5$s; width: %6$s;"
			+ "transition-property: opacity, x, y, height, width;"
			+ "transition-timing-function: ease-in; transition-duration: 0.5s;}\n";

	@SuppressWarnings("rawtypes")
	public CartesianChartEngine(Charts chart) {
		CategoryModel model = (CategoryModel) chart.getModel();
		_sSize = model.getSeries().size();
		_cSize = model.getCategories().size();
		_series = new ArrayList<Comparable<?>>(_sSize);
		_categories = new ArrayList<Comparable<?>>(_cSize);
		_values = new ArrayList<Number>(_sSize * _cSize);
		_disableList = new ArrayList<Integer>(_cSize);

		for (int i = 0; i < _sSize; i++) {
			Comparable s = model.getSeries(i);
			_series.add(s);
		}

		for (int i = 0; i < _cSize; i++) {
			Comparable category = model.getCategory(i);
			_categories.add(category);
			for (int j = 0; j < _sSize; j++) {
				Comparable s = model.getSeries(j);
				_values.add(model.getValue(s, category));
			}
		}
		_chart = chart;
	}

	private double getSizeInPixel(String size) {
		if (size != null && size.endsWith("px"))
			return Double.parseDouble(size.substring(0, size.length() - 2));
		return 0;
	}

	private void initSize() {
		double width = getSizeInPixel(_chart.getWidth());
		double height = getSizeInPixel(_chart.getHeight());
		if (width <= 0)
			width = 500;
		if (height <= 0)
			height = 410;
		setWidth(width);
		setHeight(height);
		double scaleX = width / 500;
		double scaleY = height / 410;
		categoryMargin = 30.0 * scaleX;
		axisYMargin = Math.max(25, 30.0 * scaleY);
		axisXMargin = Math.max(25, 30.0 * scaleX);
		barWidth = 15.0 * scaleX;
		tickDistance = (getHeight() - axisYMargin * 2) / (tickCount - 1);
	}

	public ChartEngineBase build() {
		redraw0(false);
		setTooltip();
		return this;
	}

	public ChartEngineBase redraw() {
		return redraw0(true);
	}

	private ChartEngineBase redraw0(boolean isRedraw) {
		this.getChildren().clear();
		initSize();
		drawBackgroud();
		drawWatermark();
		drawLegend();
		drawAxis();
		drawSeries(isRedraw);
		return this;
	}

	private void drawWatermark() {
		Layer wm = new Layer();
		for (int y = 0; y < getHeight(); y += 50) {
			for (int x = 0; x < getWidth(); x += 100) {
				Text text = new Text();
				text.setTextContent("Powered by Potix");
				text.setFontSize("15");
				text.setFill("#808080");
				text.setOpacity(0.5);
				text.setRotate("-35deg");
				text.setY(y);
				text.setX(x);
				wm.appendChild(text);
			}
		}
		wm.setId("watermark");
		appendChild(wm);
	}

	private void setTooltip() {
		final Popup tooltip = new Popup();
		tooltip.addEventListener(Events.ON_OPEN,
				new EventListener<OpenEvent>() {
					public void onEvent(OpenEvent event) throws Exception {
						if (event.isOpen()) {
							Events.postEvent(
									_chart,
									new Event("onChartTooltip", event
											.getTarget(), event.getReference()
											.getAttribute("value")));
						}
					}
				});
		_chart.appendChild(tooltip);
		tooltip.setId("tooltip");
	}

	public void setWidth(double width) {
		super.setWidth(width);
		// sync background
		Rect background = (Rect) this.getFellowIfAny("background");
		if (background != null)
			background.setWidth(width);
	}

	public void setHeight(double height) {
		super.setHeight(height);
		// sync background
		Rect background = (Rect) this.getFellowIfAny("background");
		if (background != null)
			background.setHeight(height);
	}

	private void drawBackgroud() {
		double width = getWidth() - getMaxLegendSize() - axisXMargin * 2;
		double height = getHeight() - axisYMargin * 2;
		Layer backgroundLayer = new Layer();
		Rect background = new Rect(0, 0, getWidth(), getHeight());
		background.setId("background");
		background.setFill("#FFFFFF");
		backgroundLayer.appendChild(background);

		Rect rect = new Rect(axisXMargin, axisYMargin, width, height);
		rect.setId("frame");
		backgroundLayer.appendChild(rect);
		appendChild(backgroundLayer);
		
		drawGrid(backgroundLayer);
	}

	private Rect getFrame() {
		return (Rect) this.getFellow("frame");
	}

	private void drawGrid(Layer background) {
		Rect frame = getFrame();
		double y = frame.getHeight() + frame.getY();
		for (int i = 1; i < tickCount - 1; i++) {
			y -= tickDistance;
			Line tick = new Line();
			tick.setStroke("#dbdccc");
			tick.setStrokeWidth(1);
			tick.setY1(y - 0.5);
			tick.setX1(axisXMargin);
			tick.setX2(axisXMargin + frame.getWidth());
			tick.setY2(y - 0.5);
			tick.setId("grid_line_" + i);
			tick.setSclass("grid_line");
			background.appendChild(tick);
		}
	}

	private String getLabelByIndex(int i, int l, Number max, Number min) {
		double increm = (max.doubleValue() - min.doubleValue())
				/ (tickCount - 1);
		Number label = null;
		l -= 1;
		if (i == 0) {
			label = min;
		} else if (i == l) {
			label = max;
		} else {
			label = roundToNearest(i * increm, increm);
			label = min.doubleValue() + label.doubleValue();
		}
		return label.toString();
	}

	private Layer drawYAxis(double y) {
		Layer yl = new Layer();
		for (int i = 0; i < tickCount; i++) {
			Text text = new Text();
			Line tick = new Line();
			String label = getLabelByIndex(i, tickCount, getMaxValue(), 0);
			text.setTextContent(label);
			text.setFontSize("11");
			text.setFill("#808080");
			text.setY(y - labelSize);
			text.setX(axisXMargin - labelSize
					- label.replace(".0", "").length() * labelSize);
			text.setId("tick_label_" + i);
			text.setSclass("tick_label");

			tick.setStroke("black");
			tick.setStrokeWidth(1);
			tick.setY1(y - 0.5);
			tick.setX1(axisXMargin - 5);
			tick.setX2(axisXMargin);
			tick.setY2(y - 0.5);
			tick.setId("tick_line_" + i);
			tick.setSclass("tick_line");

			Line line = new Line();
			line.setStroke("black");
			line.setStrokeWidth(1);
			line.setY1(y);
			line.setX1(axisXMargin - 0.5);
			line.setX2(axisXMargin - 0.5);
			line.setSclass("tick_line");
			y -= tickDistance;

			line.setY2(y);

			yl.appendChild(text);
			yl.appendChild(tick);
			if (i + 1 != tickCount)
				yl.appendChild(line);
		}
		return yl;
	}

	private Layer drawXAxis(double x, double y) {
		Layer xl = new Layer();
		x += categoryMargin;
		for (int i = 0; i < _cSize; i++) {
			Text text = new Text();
			text.setTextContent(_categories.get(i).toString());
			text.setFontSize("11");
			text.setFill("#808080");
			text.setY(y + labelSize);
			text.setX(x);
			text.setTextAnchor("middle");
			text.setWidth(_sSize * barWidth);
			text.setId("category_label_" + i);
			text.setSclass("category_label");

			x += barWidth * _sSize + categoryMargin;
			xl.appendChild(text);
		}
		return xl;
	}

	private void initMaxValue() {
		ArrayList<Number> sort = new ArrayList<Number>(_values);
		Collections.sort((ArrayList) sort);
		double[] minMax = roundMinAndMax(sort.get(0).doubleValue(),
				sort.get(sort.size() - 1).doubleValue(), tickCount);
		_max = minMax[1];
	}

	private Number getMaxValue() {
		return _max;
	}

	@SuppressWarnings("unchecked")
	private void drawAxis() {
		initMaxValue();
		Rect frame = getFrame();
		double y = frame.getHeight() + frame.getY();
		double x = frame.getX();
		appendChild(drawYAxis(y));
		appendChild(drawXAxis(x, y));

	}

	private void redrawSeries() {
		String style = this.redrawSeries0();
		appendStyle(style);
	}

	private String redrawSeries0() {
		Layer series = (Layer) this.getFellow("series");
		StringBuilder sb = new StringBuilder(32);
		Rect frame = getFrame();
		double y = frame.getHeight() + frame.getY();
		double x = frame.getX() + categoryMargin;
		double hgh = frame.getHeight();
		int width = _sSize - _disableList.size() == 0 ? 0 : (int) (barWidth
				* _sSize / (_sSize - _disableList.size()));
		for (int i = 0; i < _cSize; i++) {

			for (int j = 0; j < _sSize; j++) {
				int index = _cSize * i + j;
				Rect bar = (Rect) series.getChildren().get(index);
				if (_disableList.contains(new Integer(j))) {
					sb.append(String.format(CSS_HIDDEN_TEMP, j, i, y));
				} else {
					final Number v = _values.get(index);
					int h = (int) (hgh * (v.doubleValue() / getMaxValue()
							.doubleValue()));
					sb.append(String.format(CSS_SHOW_TEMP, j, i, x, y - h, h,
							width));
					x += width;

					// reset.
					bar.setY(y);
					bar.setHeight(0);
					bar.drawLayer();
				}
			}
			x += categoryMargin;
		}
		return sb.toString();
	}

	private void drawSeries(boolean resize) {
		Layer series = new Layer();
		series.setId("series");
		Rect frame = getFrame();
		double y = frame.getHeight() + frame.getY();
		double x = frame.getX() + categoryMargin;
		double hgh = frame.getHeight();
		int width = (int) (barWidth * _sSize / (_sSize - _disableList.size()));
		for (int i = 0; i < _cSize; i++) {
			for (int j = 0; j < _sSize; j++) {
				final Number v = _values.get(_cSize * i + j);
				int h = (int) (hgh * (v.doubleValue() / getMaxValue()
						.doubleValue()));
				Rect bar = new Rect(x, y - h, width, h);
				bar.setFill(fillColors[j]);
				bar.setTooltip("tooltip,x=(zk.currentPointer[0] + 10),y=(zk.currentPointer[1] + 10)");
				bar.setSclass("series category series_" + j + " " + "category_"
						+ i);

				final Comparable<?> c = _categories.get(i);
				final Comparable<?> s = _series.get(j);
				final Object[] data = new Object[] { c, s, v };
				bar.addEventListener(Events.ON_CLICK,
						new EventListener<MouseEvent>() {
							public void onEvent(MouseEvent event)
									throws Exception {
								Events.postEvent(
										_chart,
										new Event("onChartClick", event
												.getTarget(), data));
							}
						});
				bar.setAttribute("value", data);

				if (_disableList.contains(new Integer(j))) {
					bar.setOpacity(0);
					continue;
				} else {
					if (!resize)
						bar.setOpacity(0);
					x += width;
				}

				series.appendChild(bar);
			}
			x += categoryMargin;
		}
		appendChild(series);
	}

	private double getMaxLegendSize() {
		int maxLen = 0;
		for (int i = 0; i < _sSize; i++) {
			String label = _series.get(i).toString();
			if (maxLen < label.length())
				maxLen = label.length();
		}
		return maxLen * 6.0 + 30;
	}

	private void drawLegend() {
		Rect frame = getFrame();
		double y = frame.getHeight() + frame.getY();
		double x = frame.getX() + frame.getWidth();

		Layer legend = new Layer();
		Rect border = new Rect();
		legend.appendChild(border);

		int height = 0;
		for (int i = 0; i < _sSize; i++) {
			Group g = new Group();
			Rect color = new Rect(0, 0, 10, 10);
			color.setFill(fillColors[i]);
			Text text = new Text();
			String label = _series.get(i).toString();
			text.setTextContent(label);
			text.setFontSize("11");
			text.setFill("black");
			if (_disableList.contains(new Integer(i))) {
				text.setOpacity(0.2);
			}
			text.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

				public void onEvent(Event event) throws Exception {
					Text txt = (Text) event.getTarget();
					Component parent = txt.getParent();
					Integer index = parent.getParent().getChildren()
							.indexOf(parent) - 1;
					if (_disableList.remove(index)) {
						txt.setOpacity(1);
						txt.drawLayer();
					} else {
						txt.setOpacity(0.2);
						_disableList.add(index);
						txt.drawLayer();
					}
					redrawSeries();
				}
			});
			text.setX(15);
			g.setSclass("legend legend_" + i);
			g.setY(5 + (i * 20));
			g.setX(8);

			height += 5 + (i * 10);

			g.appendChild(color);
			g.appendChild(text);
			legend.appendChild(g);
		}
		border.setHeight(height);
		border.setWidth(getMaxLegendSize());
		border.setStroke("black");
		border.setStrokeWidth(1);
		legend.setId("legend");
		legend.setX(x + (axisXMargin / 2));
		legend.setY(y / 2 - (height / 2));
		appendChild(legend);
	}

	private Animation checkAnimation() {
		Animation anima = this.getAnimation();
		if (anima == null) {
			anima = new Animation();
			this.appendChild(anima);
		}
		return anima;
	}

	public ChartEngineBase appendStyle(String style) {
		checkAnimation().appendContent(style);
		return this;
	}

	@Override
	public ChartEngineBase applyStyle(String style) {
		checkAnimation().setContent(style);
		return this;
	}

}
