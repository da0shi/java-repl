package replviz;

import java.lang.reflect.Type;

import java.awt.Color;
import java.awt.BorderLayout;
import java.util.Map;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.mxgraph.layout.mxStackLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.shape.mxSwimlaneShape;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.util.mxSwingConstants;
import com.mxgraph.util.mxConstants;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxStylesheet;

public class ReplViz
	extends JPanel
{
	private static final long serialVersionUID = 10000000000001L;

	public static final int FRAME_WIDTH = 1280;
	public static final int FRAME_HEIGHT = 800;
	public static final int VARIABLE_LIST_WIDTH = 280;
	public static final int VARIABLE_LIST_HEIGHT = 400;
	public static final int VARIABLE_WIDTH = VARIABLE_LIST_WIDTH;
	public static final int VARIABLE_HEIGHT = 20;

	private static mxGraph graph;
	private static mxGraphComponent graphComponent;
	private String appTitle;
	private Map<String, ReplVizResult> results;

	private mxCell variableListCell;

	private Map<String, mxCell> ref;

	public ReplViz ()
	{
		this("ReplViz", new mxGraphComponent(new mxGraph()));
	}

	public ReplViz (String title, mxGraphComponent component)
	{
		this.appTitle = title;
		graphComponent = component;
		graph = graphComponent.getGraph();
		this.results = new HashMap<String, ReplVizResult>();

		graph.setAllowDanglingEdges(false);
		graph.setAutoSizeCells(true);
		graph.setCellsEditable(false);
		graph.setCellsMovable(true);
		graph.setCellsResizable(true);
		graph.setCellsSelectable(false);

		setLayout(new BorderLayout());
		add(graphComponent, BorderLayout.CENTER);
	}

	public void addResult (String key, Object value, Type type)
	{
		if (graph == null) return;

		JFrame frame = (JFrame) SwingUtilities.windowForComponent(this);

		ReplVizResult result = new ReplVizResult(key, value, type);
		result.insertVariable(graph, variableListCell);
		applyEdgeStyle();
		if (results.containsKey(key)) {
			results.get(key).removeVariable(graph);
		}
		results.put(key, result);
		graph.getModel().beginUpdate();
		try {
			new mxStackLayout(graph, false).execute(variableListCell);
		}
		finally {
			graph.getModel().endUpdate();
		}
		frame.pack();
	}

	public void addResult (String key, Object value)
	{
		if (value != null) {
			addResult(key, value, value.getClass());
		}
		else {
			addResult(key, value, Object.class);
		}
	}

	private JFrame initFrame ()
	{
		JFrame frame = new JFrame();
		frame.getContentPane().add(this);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle(appTitle);

		return frame;
	}

	private void initGraphs()
	{
		if (graph == null) return;
		JFrame frame = (JFrame) SwingUtilities.windowForComponent(this);
		Object parent = graph.getDefaultParent();

		graph.getModel().beginUpdate();
		try {
			variableListCell = (mxCell) graph.insertVertex(
					parent, null, "Variables", 0, 0,
					VARIABLE_LIST_WIDTH, VARIABLE_LIST_HEIGHT, "shape=swimlane;foldable=0;fillColor=#999;fontColor=#000");
		}
		finally {
			graph.getModel().endUpdate();
		}
		frame.getContentPane().add(new mxGraphComponent(graph));
		frame.pack();
	}

	private void applyEdgeStyle ()
	{
		if (graph == null) return;
		Map<String, Object> edge = new HashMap<String, Object>();
		edge.put(mxConstants.STYLE_ROUNDED, true);
		edge.put(mxConstants.STYLE_ORTHOGONAL, false);
		edge.put(mxConstants.STYLE_EDGE, "elbowEdgeStyle");
		edge.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_CONNECTOR);
		edge.put(mxConstants.STYLE_ENDARROW, mxConstants.ARROW_CLASSIC);
		edge.put(mxConstants.STYLE_VERTICAL_ALIGN, mxConstants.ALIGN_MIDDLE);
		edge.put(mxConstants.STYLE_ALIGN, mxConstants.ALIGN_CENTER);
		edge.put(mxConstants.STYLE_STROKECOLOR, "#000000");
		edge.put(mxConstants.STYLE_FONTCOLOR, "#446299");

		mxStylesheet edgeStyle = new mxStylesheet();
		edgeStyle.setDefaultEdgeStyle(edge);
		graph.setStylesheet(edgeStyle);
	}

	public static ReplViz run ()
	{
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}

		mxSwingConstants.SHADOW_COLOR = Color.LIGHT_GRAY;
		mxConstants.W3C_SHADOWCOLOR = "#D3D3D3";

		ReplViz visualizer = new ReplViz();
		visualizer.initFrame().setVisible(true);
		visualizer.initGraphs();
		visualizer.applyEdgeStyle();
		return visualizer;
	}
	public void close ()
	{
		JFrame frame = (JFrame) SwingUtilities.windowForComponent(this);
		frame.dispose();
	}
}
