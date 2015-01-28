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
import com.mxgraph.view.mxEdgeStyle;
import com.mxgraph.view.mxStylesheet;

public class ReplViz
	extends JPanel
{
	private static final long serialVersionUID = 10000000000001L;
	private static final String TITLE = "ReplViz";

	static final int VARIABLE_CONTAINER_OFFSET_X = 0;
	static final int ENTITY_CONTAINER_OFFSET_X = 350;
	public static final int CONTAINER_WIDTH = 260;
	public static final int CONTAINER_HEIGHT = 60;
	public static final int VARIABLE_WIDTH = CONTAINER_WIDTH;
	public static final int VARIABLE_HEIGHT = 20;
	public static final int HORIZONTAL_SPACING = 60;
	public static final int VERTICAL_SPACING = 10;

	private static ReplViz self = null;

	private mxGraph graph;
	private mxGraphComponent component;
	/* Contains Result as key, map of referrer cell and entity cell as value */
	private Container rootContainer;

	private ReplViz ()
	{
		this(new mxGraphComponent(new mxGraph()));
	}

	private ReplViz (mxGraphComponent component)
	{
		this.component = component;
		this.graph = component.getGraph();

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
		rootContainer.addContent(type, key, value);
		rootContainer.redraw();
	}

	public void close ()
	{
		self = null;
		rootContainer.reset();
		JFrame frame = (JFrame) SwingUtilities.windowForComponent(this);
		frame.dispose();
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
		Variable.graph = graph;
		rootContainer = Container.getRoot();
		rootContainer.initialize();
		rootContainer.visualize();

		JFrame frame = (JFrame) SwingUtilities.windowForComponent(this);
		// frame.pack();
	}
	private void applyEdgeStyle ()
	{
		if (graph == null) return;
		Map<String, Object> edge = new HashMap<String, Object>();
		edge.put(mxConstants.STYLE_ROUNDED, true);
		edge.put(mxConstants.STYLE_ORTHOGONAL, true);
		edge.put(mxConstants.STYLE_EDGE, mxEdgeStyle.SideToSide);
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
