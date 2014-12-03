package javarepl.replviz;

import java.awt.*;
import javax.swing.*;
import com.mxgraph.layout.*;
import com.mxgraph.swing.*;
import org.jgrapht.*;
import org.jgrapht.ext.*;
import org.jgrapht.graph.*;

public class ReplViz
	extends JApplet
{
	private static final Dimension DEFAULT_SIZE = new Dimension(530, 320);
	private JGraphXAdapter<String, DefaultEdge> adapter;

	public void init ()
	{
		ListenableGraph<String, DefaultEdge> g =
			new ListenableDirectedGraph<String, DefaultEdge>(
					DefaultEdge.class);

		// create a visualization using JGraph, via an adapter
		adapter = new JGraphXAdapter<String, DefaultEdge>(g);

		getContentPane().add(new mxGraphComponent(adapter));
		resize(DEFAULT_SIZE);

		String v1 = "v1";
		String v2 = "v2";
		String v3 = "v3";
		String v4 = "v4";

		// add some sample data (graph manipulated via JGraphX)
		g.addVertex(v1);
		g.addVertex(v2);
		g.addVertex(v3);
		g.addVertex(v4);

		g.addEdge(v1, v2);
		g.addEdge(v2, v3);
		g.addEdge(v3, v1);
		g.addEdge(v4, v3);

		// positioning via jgraphx layouts
		mxCircleLayout layout = new mxCircleLayout(adapter);
		layout.execute(adapter.getDefaultParent());

	}
}
