package replviz;

import java.lang.reflect.Type;

import java.awt.Color;
import java.awt.BorderLayout;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.mxgraph.layout.mxStackLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.shape.mxSwimlaneShape;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.util.mxSwingConstants;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxStylesheet;

public class ReplViz
	extends JPanel
{
	private static final long serialVersionUID = 10000000000001L;
	private static final String TITLE = "ReplViz";

	static final int VARIABLE_CONTAINER_OFFSET_X = 0;
	static final int ENTITY_CONTAINER_OFFSET_X = 350;
	public static final int CONTAINER_WIDTH = 280;
	public static final int CONTAINER_HEIGHT = 60;
	public static final int VARIABLE_WIDTH = CONTAINER_WIDTH;
	public static final int VARIABLE_HEIGHT = 20;
	public static final int ENTITY_SPACING = 10;

	private static ReplViz self = null;

	private mxGraph graph;
	private mxGraphComponent component;
	/* Contains Result as key, map of referrer cell and entity cell as value */
	private Map<String, rvResultSet> results;

	private mxCell variableListCell;

	private ReplViz ()
	{
		this(new mxGraphComponent(new mxGraph()));
	}

	private ReplViz (mxGraphComponent component)
	{
		this.component = component;
		this.graph = component.getGraph();
		this.results = new HashMap<String, rvResultSet>();

		this.graph.setAllowDanglingEdges(false);
		this.graph.setAutoSizeCells(true);
		this.graph.setCellsEditable(false);
		this.graph.setCellsMovable(true);
		this.graph.setCellsResizable(true);
		this.graph.setCellsSelectable(false);

		/* JPanel Layout */
		setLayout(new BorderLayout());
		add(component, BorderLayout.CENTER);
	}

	public void addResult (String key, Object value, Type type)
	{
		if (graph == null) return;

		rvResult result = new rvResult(key, value, type);
		rvResultSet resultset = null;
		if (results.containsKey(key)) {
			if (searchValue(results.get(key).result().value()) == null && searchValue(value) == null) {
				results.get(key).removeCells(graph);
			} else {
				results.get(key).removeEdge(graph);
			}
			results.remove(key);
		}
		resultset = new rvResultSet(result);

		mxCell referrer = resultset.insertRefer(graph, variableListCell);
		rvResultSet foundResult = searchValue(value);
		mxCell entity = null;
		if (foundResult != null) {
			entity = foundResult.entity();
		}
		if (result.value() != null && ! result.isPrimitive()) {
			if (entity == null) {
				mxCell entityContainer =
					insertContainer(null, result.valueRef(), ENTITY_CONTAINER_OFFSET_X, 0);
				entity = resultset.insertEntity(graph, entityContainer);
			} else {
				resultset.entity(entity);
			}
		}
		results.put(key, resultset);
		if (entity != null) {
			graph.getModel().beginUpdate();
			try {
				graph.insertEdge(graph.getDefaultParent(), null, null, referrer, entity);
			}
			finally {
				graph.getModel().endUpdate();
			}
			updateEntityLayout();
		}
		JFrame frame = (JFrame) SwingUtilities.windowForComponent(this);
		// frame.pack();
	}

	public mxCell insertContainer (Object parent, String title, double x, double y)
	{
		if (parent == null) {
			parent = graph.getDefaultParent();
		} else if (parent instanceof mxGraph) {
			parent = ((mxGraph) parent).getDefaultParent();
		}
		mxCell cell = null;

		graph.getModel().beginUpdate();
		try {
			cell = (mxCell) graph.insertVertex(
					parent, null,title, x, y,
					CONTAINER_WIDTH, CONTAINER_HEIGHT, "shape=swimlane;foldable=0;fillColor=#999;fontColor=#000;");
		}
		finally {
			graph.getModel().endUpdate();
		}
		return cell;
	}

	public void close ()
	{
		self = null;
		JFrame frame = (JFrame) SwingUtilities.windowForComponent(this);
		frame.dispose();
	}

	private void updateEntityLayout ()
	{
		Object parent = graph.getDefaultParent();
		mxIGraphModel model = graph.getModel();
		mxRectangle tmp = new mxRectangle();
		mxGeometry pgeo = new mxGeometry(0, 0, tmp.getWidth(), tmp.getHeight());
		mxGeometry last = null;
		List<String> done = new ArrayList<String>();
		try {
			for (rvResultSet resultset: results.values()) {
				if (resultset.entity() == null) continue;
				if (done.contains(resultset.result().value().toString())) continue;
				done.add(resultset.result().value().toString());
				mxGeometry geo = (mxGeometry) model.getGeometry(resultset.entity());
				geo = (mxGeometry) geo.clone();
				geo.setX(ENTITY_CONTAINER_OFFSET_X);
				if (last == null) {
					geo.setY(0);
				} else {
					geo.setY(last.getY() + last.getHeight() + ENTITY_SPACING);
				}
				model.setGeometry(resultset.entity(), geo);
				last = geo;
			}
			if (last != null) {
				pgeo = (mxGeometry) pgeo.clone();
				pgeo.setHeight(last.getY() + last.getHeight() + ENTITY_SPACING);
				model.setGeometry(parent, pgeo);
			}
		}
		finally {
			graph.getView().reload();
			graph.refresh();
			model.endUpdate();
		}
	}
	private rvResultSet searchValue (Object value)
	{
		if (value == null) return null;
		for (rvResultSet resultset: results.values()) {
			if (value.equals(resultset.result().value())) {
				return resultset;
			}
		}
		return null;
	}

	public static ReplViz run ()
	{
		if (self != null) return self;
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}

		mxSwingConstants.SHADOW_COLOR = Color.LIGHT_GRAY;
		mxConstants.W3C_SHADOWCOLOR = "#D3D3D3";

		self = new ReplViz();
		self.initFrame().setVisible(true);
		self.initGraphs();
		self.applyEdgeStyle();
		return self;
	}
	private JFrame initFrame ()
	{
		JFrame frame = new JFrame();
		frame.getContentPane().add(this);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(640, 800);
		frame.setTitle(TITLE);
		// frame.pack();

		return frame;
	}
	private void initGraphs()
	{
		if (graph == null) return;
		Object parent = graph.getDefaultParent();
		graph.getModel().beginUpdate();
		try {
			variableListCell = insertContainer(null, "Variables", VARIABLE_CONTAINER_OFFSET_X, 0);
		}
		finally {
			graph.getModel().endUpdate();
		}

		JFrame frame = (JFrame) SwingUtilities.windowForComponent(this);
		// frame.pack();
	}
	private void applyEdgeStyle ()
	{
		if (graph == null) return;
		Map<String, Object> edge = new HashMap<String, Object>();
		edge.put(mxConstants.STYLE_ROUNDED, true);
		edge.put(mxConstants.STYLE_ORTHOGONAL, true);
		edge.put(mxConstants.STYLE_EDGE, "orthogonalEdgeStyle");
		edge.put(mxConstants.STYLE_PORT_CONSTRAINT, mxConstants.DIRECTION_EAST + mxConstants.DIRECTION_WEST);
		edge.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_CONNECTOR);
		edge.put(mxConstants.STYLE_ENDARROW, mxConstants.ARROW_CLASSIC);
		edge.put(mxConstants.STYLE_VERTICAL_ALIGN, mxConstants.ALIGN_LEFT);
		edge.put(mxConstants.STYLE_ALIGN, mxConstants.ALIGN_CENTER);
		edge.put(mxConstants.STYLE_STROKECOLOR, "#000000");
		edge.put(mxConstants.STYLE_FONTCOLOR, "#446299");

		mxStylesheet edgeStyle = new mxStylesheet();
		edgeStyle.setDefaultEdgeStyle(edge);
		graph.setStylesheet(edgeStyle);
	}
}
