package javarepl.replviz;

import com.mxgraph.layout.*;
import com.mxgraph.swing.*;

import java.awt.*;
import javax.swing.*;

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
	}
}
