package com.vaadin.addon.charts.examples.timeline;

import java.util.Random;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.ChartClickEvent;
import com.vaadin.addon.charts.ChartClickListener;
import com.vaadin.addon.charts.ChartSelectionEvent;
import com.vaadin.addon.charts.ChartSelectionListener;
import com.vaadin.addon.charts.PointClickEvent;
import com.vaadin.addon.charts.PointClickListener;
import com.vaadin.addon.charts.PointSelectEvent;
import com.vaadin.addon.charts.PointSelectListener;
import com.vaadin.addon.charts.examples.AbstractVaadinChartExample;
import com.vaadin.addon.charts.examples.timeline.util.StockPrices;
import com.vaadin.addon.charts.model.ChartType;
import com.vaadin.addon.charts.model.Configuration;
import com.vaadin.addon.charts.model.DataGrouping;
import com.vaadin.addon.charts.model.DataSeries;
import com.vaadin.addon.charts.model.OhlcItem;
import com.vaadin.addon.charts.model.PlotOptionsArearange;
import com.vaadin.addon.charts.model.PlotOptionsCandlestick;
import com.vaadin.addon.charts.model.PlotOptionsLine;
import com.vaadin.addon.charts.model.RangeSelector;
import com.vaadin.addon.charts.model.RangeSeries;
import com.vaadin.addon.charts.model.TimeUnit;
import com.vaadin.addon.charts.model.TimeUnitMultiples;
import com.vaadin.addon.charts.model.ZoomType;
import com.vaadin.ui.Component;
import com.vaadin.addon.charts.model.DataSeriesItem;
import com.vaadin.addon.charts.model.Marker;
import com.vaadin.addon.charts.model.style.Color;
import com.vaadin.addon.charts.model.style.SolidColor;
import com.vaadin.addon.charts.themes.ValoLightTheme;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ColorPicker;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Slider;
import com.vaadin.ui.VerticalLayout;


public class Candlestick extends AbstractVaadinChartExample {

	private OhlcItem ohlcItem;
	
	private DataSeriesItem selectedSegmentBegin = null; 
	private DataSeriesItem selectedSegmentEnd = null; 
	
	private DataSeriesItem lastSegment; 
	private DataSeries barSeries;
	private long date_delta = 0;
    StockPrices.OhlcData lastOhlcData = null;
	private DataSeries segmentSeries;
	private Label lastAction = new Label();
    private Label eventDetails = new Label();
	
    @Override
    public String getDescription() {
        return "Single line chart with timeline";
    }

    @Override
    protected Component getChart() {
        final Chart chart = new Chart(ChartType.CANDLESTICK);
        chart.setHeight("450px");
        chart.setWidth("100%");
        chart.setTimeline(true);
        

        Configuration configuration = chart.getConfiguration();
        Color[] colors = getThemeColors();
        configuration.getTitle().setText("AAPL Weekly Candles");
        configuration.getTooltip().setEnabled(false);
        chart.getConfiguration().getChart().setZoomType(ZoomType.XY);
        //chart.getConfiguration().getChart().setZoomType(ZoomType.X);
        /*RangeSelector rangeSelector = new RangeSelector();
        rangeSelector.setSelected(4);
        configuration.setRangeSelector(rangeSelector);*/

        barSeries = new DataSeries();
        PlotOptionsCandlestick plotOptionsCandlestick = new PlotOptionsCandlestick();
        //DataGrouping grouping = new DataGrouping();
        //grouping.addUnit(new TimeUnitMultiples(TimeUnit.WEEK, 1));
        //grouping.addUnit(new TimeUnitMultiples(TimeUnit.MONTH, 1, 2, 3, 4, 6));
        //plotOptionsCandlestick.setDataGrouping(grouping);
        barSeries.setPlotOptions(plotOptionsCandlestick);
        
        PlotOptionsLine plotOptionsLine = new PlotOptionsLine();
        //plotOptionsLine.setDataGrouping(grouping);
        plotOptionsLine.setColor(colors[3]);
        plotOptionsLine.setMarker(new Marker(true));
        segmentSeries = new DataSeries();
        segmentSeries.setPlotOptions(plotOptionsLine);
        segmentSeries.setName("Segments");

        int i = 0;
        for (StockPrices.OhlcData data : StockPrices.fetchAaplOhlcPrice()) {
            OhlcItem item = new OhlcItem();            
            if( lastOhlcData != null )
            {
            		date_delta = data.getDate() - lastOhlcData.getDate();
            }
            item.setX(data.getDate());
            item.setLow(data.getLow());
            item.setHigh(data.getHigh());
            item.setClose(data.getClose());
            item.setOpen(data.getOpen());
            lastOhlcData = data;
            ohlcItem = item;
            barSeries.add(item);
            
            DataSeriesItem inditem = new DataSeriesItem();
            inditem.setX(data.getDate());
            	inditem.setY(data.getClose()+10);
            segmentSeries.add(inditem);
            lastSegment = inditem;
            if(i % 2 == 1 )
            {
            		inditem = new DataSeriesItem();
            		inditem.setX(data.getDate());
            		inditem.setY(null);
            		segmentSeries.add(inditem);
            }
            i++;
        }
        configuration.addSeries(barSeries);
        configuration.addSeries(segmentSeries);


/*        chart.addChartClickListener(new ChartClickListener() {

            @Override
            public void onClick(ChartClickEvent event) {
                double x = Math.round(event.getxAxisValue());
                double y = Math.round(event.getyAxisValue());
                segmentSeries.add(new DataSeriesItem(x, y));
                lastAction.setValue("Added point " + x + "," + y);
                eventDetails.setValue(createEventString(event));
            }
        });*/
        
        chart.addPointClickListener(new PointClickListener() {

            @Override
            public void onClick(PointClickEvent event) {
                DataSeries ds = (DataSeries) event.getSeries();
                if( event.getPointIndex() < 0 )
                {
                    lastAction.setValue("Selected invalid point at index "
                            + event.getPointIndex());
                    eventDetails.setValue(createEventString(event));
                    return;
                }
                DataSeriesItem selectedSeriesItem = ds.get(event.getPointIndex());
                if(event.getPointIndex()+1 < ds.size())
                {
                		DataSeriesItem selectedItemNext = ds.get(event.getPointIndex()+1);
                		if( selectedItemNext.getY() != null )
                		{
                			selectedSegmentBegin = selectedSeriesItem;
                			selectedSegmentEnd = selectedItemNext;
                            lastAction.setValue("Selected point and next at index "
                                    + event.getPointIndex());
                            eventDetails.setValue(createEventString(event));
                		}
                }
                if(event.getPointIndex()-1 >= 0)
                {
                		DataSeriesItem selectedItemPrev = ds.get(event.getPointIndex()-1);
                		if( selectedItemPrev.getY() != null )
                		{
                			selectedSegmentBegin = selectedItemPrev;
                			selectedSegmentEnd = selectedSeriesItem;
                            lastAction.setValue("Selected point and prev at index "
                                    + event.getPointIndex());
                            eventDetails.setValue(createEventString(event));
                		}
                }
                //ds.remove(dataSeriesItem2);
            }
        });
        
        
        chart.addChartSelectionListener(new ChartSelectionListener() {

            @Override
            public void onSelection(ChartSelectionEvent event) {
            		if( selectedSegmentBegin != null && selectedSegmentEnd != null )
            		{
            			double xStart = event.getSelectionStart();
            			double xEnd = event.getSelectionEnd();
            			double yStart = event.getValueStart();
            			double yEnd = event.getValueEnd();
                
            			//if(xEnd > xStart)
            			{
            			//	selectedSegmentBegin.setX(xStart);
            				selectedSegmentBegin.setY(yStart);
            			//	selectedSegmentEnd.setX(xEnd);
            				selectedSegmentEnd.setY(yEnd);
            			}
            			/*else
            			{
            			//	selectedSegmentEnd.setX(xStart);
            				selectedSegmentEnd.setY(yStart);
            			//	selectedSegmentBegin.setX(xEnd);
            				selectedSegmentBegin.setY(yEnd);                	
            			}*/
            			segmentSeries.update(selectedSegmentBegin);
            			segmentSeries.update(selectedSegmentEnd);
            		}

                /*Number[][] data = new Number[][] { { xStart, yStart, yEnd },
                        { xEnd, yStart, yEnd } };

                PlotOptionsArearange areaRangePlot = new PlotOptionsArearange();
                areaRangePlot.setFillOpacity(0.1f);
                areaRangePlot.setLineWidth(0);

                RangeSeries selectionSeries = new RangeSeries("Selection", data);

                selectionSeries.setPlotOptions(areaRangePlot);
                chart.getConfiguration().addSeries(selectionSeries);
                chart.drawChart();
                areaRangePlot.setAnimation(false);*/
            }
        });        
        
        chart.drawChart(configuration);

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing(false);
        verticalLayout.setMargin(false);
        verticalLayout.addComponent(chart);
        verticalLayout.addComponent(lastAction);
        verticalLayout.addComponent(eventDetails);
        return verticalLayout;        
        //return chart;
    }
    
    static final Color[] colors = new ValoLightTheme().getColors();

    static final Color COLOR_NORMAL = colors[0];

    /*private DataSeriesItem createBasicPoint(int i, int j) {
        DataSeriesItem dataSeriesItem2 = new DataSeriesItem(1, 4);
        return dataSeriesItem2;
    }*/

    @Override
    protected void setup() {
        super.setup();

        FormLayout formLayout = new FormLayout();
        formLayout
                .setCaption("Special point settings, only updated point state is sent to client.");
        formLayout.setMargin(true);

        final Slider sliderX = new Slider();
        sliderX.setMin(80);
        sliderX.setMax(140);
        sliderX.setResolution(1);
        sliderX.setValue(100d);
        sliderX.setCaption("X");
        sliderX.addValueChangeListener(event -> {
        			ohlcItem.setClose(sliderX.getValue());
        			ohlcItem.setX(barSeries.get(barSeries.size()-1).getX().longValue() + date_delta );
        			lastSegment.setX(barSeries.get(barSeries.size()-1).getX().longValue() + date_delta);
        			lastSegment.setY(sliderX.getValue()+10);
                barSeries.add(ohlcItem);
                segmentSeries.add(lastSegment);

        });
        sliderX.setWidth("200px");
        formLayout.addComponent(sliderX);

        final Slider sliderY = new Slider();
        sliderY.setMin(80);
        sliderY.setMax(140);
        sliderY.setResolution(1);
        sliderY.setValue(100d);
        sliderY.setCaption("Y");
        sliderY.addValueChangeListener(event -> {
        			ohlcItem.setLow(sliderY.getValue());
                //lastSegment.setY(sliderY.getValue());
                lastSegment.setY(null);
                updateItemInChart();
        });
        sliderY.setWidth("200px");
        formLayout.addComponent(sliderY);

        final ColorPicker colorPicker = new ColorPicker();
        colorPicker.setValue(new com.vaadin.shared.ui.colorpicker.Color(255, 0,
                0));
        colorPicker.setCaption("Marker color");

        colorPicker.addValueChangeListener(event -> {
        			ohlcItem.getMarker().setFillColor(
                    new SolidColor(event.getValue().getCSS()));
                updateItemInChart();
        });
        formLayout.addComponent(colorPicker);

        Button c = new Button("Pseudorandom", new Button.ClickListener() {
            Random r = new Random(0);

            @Override
            public void buttonClick(ClickEvent event) {
                sliderX.setValue(r.nextDouble() * 60 + 80);
                sliderY.setValue(r.nextDouble() * 60 + 80);
                colorPicker
                        .setValue(new com.vaadin.shared.ui.colorpicker.Color(r
                                .nextInt(255), r.nextInt(255), r.nextInt(255)));
            }
        });
        c.setId("random");
        c.setClickShortcut(KeyCode.R);
        formLayout.addComponent(c);

        addComponentAsFirst(formLayout);

    }

    private String createEventString(Event event) {
        ObjectMapper mapper = new ObjectMapper()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .enable(SerializationFeature.INDENT_OUTPUT)
                .setVisibility(PropertyAccessor.ALL,
                        JsonAutoDetect.Visibility.NONE)
                .setVisibility(PropertyAccessor.FIELD,
                        JsonAutoDetect.Visibility.ANY);

        try {
            return mapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "";
        }
    }    
    private void updateItemInChart() {
        barSeries.update(ohlcItem);
        segmentSeries.update(lastSegment);
    }    
}
