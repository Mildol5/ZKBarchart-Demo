/** BarChartVM.java.

	Purpose:
		
	Description:
		
	History:
		11:26:54 AM May 3, 2013, Created by jumperchen

Copyright (C) 2013 Potix Corporation. All Rights Reserved.
 */
package demo.chart.bar;


import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.charts.Charts;
import org.zkoss.util.media.AMedia;
import org.zkoss.util.media.Media;
import org.zkoss.zul.Filedownload;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zul.Button;
import org.zkoss.zul.CategoryModel;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Popup;
import org.zkoss.zul.Window;

/**
 * A barchart VM
 * @author jumperchen
 * 
 */
public class BarChartVM {

	CategoryModel model;
	boolean watermark = false;
	boolean legend = true;

	@Init
	public void init() {
		// prepare chart data
		model = ChartData.getModel();
	}

	public CategoryModel getModel() {
		return model;
	}

	public boolean isWatermark() {
		return watermark;
	}

	public boolean isLegend() {
		return legend;
	}

	@Command("watermarkChanged")
	@NotifyChange({ "watermark" })
	public void watermarkChanged(@BindingParam("watermark") Boolean watermark) {
		if (watermark != null)
			this.watermark = watermark;
	}

	@Command("legendChanged")
	@NotifyChange({ "legend" })
	public void legendChanged(@BindingParam("legend") Boolean legend) {
		if (legend != null)
			this.legend = legend;
	}

	@Command("reset")
	public void reset(@BindingParam("chart") Charts chart) {
		chart.invalidate();
	}

	@Command("resize")
	public void resize(@BindingParam("chart") Charts chart,
			@BindingParam("width") String width,
			@BindingParam("height") String height) {
		int w = Integer.parseInt(width.replace("px", ""));
		int h = Integer.parseInt(height.replace("px", ""));
		chart.resize(w - 20 + "px", h - 70 + "px");
	}

	@Command("onClick")
	public void drilldown(@BindingParam("event") Event event) {
		Object[] data = (Object[]) event.getData();
		Messagebox.show("Category: " + data[0] + "\n Series: " + data[1]
				+ "\n Value: " + data[2]);
	}

	@Command("onTooltip")
	public void tooltip(@BindingParam("event") Event event) {
		Popup tooltip = (Popup) event.getTarget();
		Object[] data = (Object[]) event.getData();
		if (tooltip.getFirstChild() == null) {
			tooltip.appendChild(new Label());
		}
		((Label) tooltip.getFirstChild()).setValue("Category: " + data[0]
				+ "\n Series: " + data[1] + "\n Value: " + data[2]);
	}

	@Command("onSaveImage")
	public void saveImage(@BindingParam("chart") Charts chart,
			@BindingParam("target") final Button btn) {
		chart.saveChartAsImage("Exporting", new EventListener<UploadEvent>() {
			public void onEvent(UploadEvent evt) throws Exception {
				Image image = new Image();
				org.zkoss.util.media.AMedia media = (AMedia) evt.getMedia();
				image.setContent(new org.zkoss.image.AImage(media.getName(),
						media.getByteData()));

				org.zkoss.zk.ui.util.Template tm = btn.getTemplate("viewImage");
				final Component[] items = tm.create((Component)btn.getSpaceOwner(), null, null, null);
				Window highlight = (Window)items[0];
				highlight.setAttribute("media", media);
				highlight.appendChild(image);
			}
		});
	}

	@Command("onFiledownload")
	public void fileDownload(@BindingParam("target") Button btn) {
		Window highlight = (Window) btn.getSpaceOwner();
		Filedownload.save((Media) highlight.getAttribute("media"), "barchart.png");
	}
}
