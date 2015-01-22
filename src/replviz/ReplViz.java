package replviz;

import java.lang.reflect.Type;

import java.awt.Color;
import java.awt.BorderLayout;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

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

	public static final int FRAME_WIDTH = 500;
	public static final int FRAME_HEIGHT = 800;
	public static final int VARIABLE_LIST_WIDTH = 280;
	public static final int VARIABLE_LIST_HEIGHT = 400;
	public static final int VARIABLE_WIDTH = VARIABLE_LIST_WIDTH;
	public static final int VARIABLE_HEIGHT = 20;

	private static mxGraph graph;
	private static mxGraphComponent graphComponent;
	private String appTitle;
	private List<ReplVizResult> results;

	private mxCell resultListCell;

	public ReplViz ()
	{
		this("ReplViz", new mxGraphComponent(new mxGraph()));
	}

	public ReplViz (String title, mxGraphComponent component)
	{
		this.appTitle = title;
		graphComponent = component;
		graph = graphComponent.getGraph();
		this.results = new ArrayList<ReplVizResult>();

		graph.setCellsEditable(false);
		graph.setCellsMovable(false);
		graph.setCellsResizable(false);
		graph.setCellsSelectable(false);
		graph.setAllowDanglingEdges(false);
		graph.setAutoSizeCells(true);

		setLayout(new BorderLayout());
		add(graphComponent, BorderLayout.CENTER);
	}

	public void addResult (String key, Object value, Type type)
	{
		if (graph == null) return;

		JFrame frame = (JFrame) SwingUtilities.windowForComponent(this);
		ReplVizResult result = new ReplVizResult(key, value, type);

		Object parent = graph.getDefaultParent();
		graph.getModel().beginUpdate();
		try {
			int y = 0;
			int childCount = resultListCell.getChildCount();
			if (childCount > 0) {
				mxICell lastChild = resultListCell.getChildAt(childCount - 1);
				mxGeometry geo = lastChild.getGeometry();
				y = (int)(geo.getY() + geo.getHeight());
			}
			mxCell var = (mxCell) graph.insertVertex(resultListCell, null,
					result.strType() +"  "+  result.key() +" = "+ result.valueRef(),
					0, y, VARIABLE_WIDTH, VARIABLE_HEIGHT);
			if (y == 0) y = 20;
			if (! (result.type() instanceof Class) || ! ((Class)result.type()).isPrimitive()) {
				mxCell inst = (mxCell) graph.insertVertex(parent, null,
						result.value().toString(),
						300, y, VARIABLE_WIDTH, VARIABLE_HEIGHT);
				graph.insertEdge(parent, null, null, var, inst);
			}
		}
		catch (NullPointerException e) {
			e.printStackTrace();
		}
		finally {
			graph.getModel().endUpdate();
		}
		frame.getContentPane().add(new mxGraphComponent(graph));
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
		frame.setSize(FRAME_HEIGHT, FRAME_WIDTH);
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
			resultListCell = (mxCell) graph.insertVertex(
					parent, null, "Variables", 20, 20,
					VARIABLE_LIST_WIDTH, VARIABLE_LIST_HEIGHT, "shape=swimlane;foldable=0;fillColor=#999;fontColor=#000");
		}
		finally {
			graph.getModel().endUpdate();
		}
		frame.getContentPane().add(new mxGraphComponent(graph));
	}

	private void initEdgeStyle ()
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
		visualizer.initEdgeStyle();
		return visualizer;
	}
	public void close ()
	{
		JFrame frame = (JFrame) SwingUtilities.windowForComponent(this);
		frame.dispose();
	}
}
