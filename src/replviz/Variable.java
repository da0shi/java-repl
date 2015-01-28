package replviz;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import com.mxgraph.model.mxCell;
import com.mxgraph.view.mxGraph;

public class Variable
{
	protected static mxGraph graph;
	protected static List<Integer> containerIDs = new ArrayList<Integer>();
	protected static Map<Integer, Variable> containers = new HashMap<Integer, Variable>();
	protected static Map<Integer, Variable> contents = new HashMap<Integer, Variable>();

	protected final String name;
	protected final Object value;
	protected final int ID;

	protected mxCell cell = null;
	protected boolean isInitialized = false;

	public static void graph (mxGraph graph)
	{
		Variable.graph = graph;
	}

	protected Variable () {
		this.name = null;
		this.value = null;
		this.ID = System.identityHashCode(null);
	}

	protected Variable (String name, Object value)
	{
		this.name = name;
		this.value = value;
		this.ID = System.identityHashCode(value);
	}
	public String name ()
	{
		return this.name;
	}
	public Object value ()
	{
		return this.value;
	}
	public int ID ()
	{
		return this.ID;
	}
	public String hexID ()
	{
		return Integer.toHexString(this.ID);
	}
	public mxCell cell ()
	{
		return this.cell;
	}
	public void reset ()
	{
		containerIDs = new ArrayList<Integer>();
		containers = new HashMap<Integer, Variable>();
		contents = new HashMap<Integer, Variable>();
		graph = null;
	}
}
