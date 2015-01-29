package replviz;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.lang.reflect.Type;
import java.lang.reflect.Field;

import com.mxgraph.layout.mxStackLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.view.mxGraph;

public class Container
	extends Variable
{
	protected static Container root = null;

	protected List<Content> members = new ArrayList<Content>();
	protected List<Content> referrers = new ArrayList<Content>();

	protected int depth = 0;
	protected boolean isRoot = false;

	public static Container getRoot ()
	{
		if (root == null) root = new Container();
		return root;
	}

	protected Container ()
	{
		super("Variables", null);
		isRoot = true;
	}
	public Container (String name, Object value)
	{
		super(name, value);
	}
	public void initialize ()
	{
		if (! isRoot) return;
		if (! Variable.containerIDs.contains(ID)) {
			Variable.containerIDs.add(ID);
			Variable.containers.put(ID, this);
		}
	}
	public void initialize (Content content)
	{
		referrers.add(content);
		if (referrers != null && referrers.size() != 0) {
			depth = referrers.get(0).container().depth + 1;
		}
		Field[] fields = value.getClass().getFields();
		for (Field f : fields) {
			try {
				Content c = new Content(this, f.getType(), f.getName(), f.get(value));
				c.initialize();
				members.add(c);
			}
			catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
			catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		if (fields.length == 0) {
			Content c = new Content(this, Utils.extractType(value.getClass()), name, value);
			c.initialize();
			members.add(c);
		}
		if (! Variable.containerIDs.contains(ID)) {
			Variable.containerIDs.add(ID);
			Variable.containers.put(ID, this);
		}
	}
	public static Container root ()
	{
		return Container.root;
	}
	public List<Content> members ()
	{
		return this.members;
	}
	public List<Content> referrers ()
	{
		return this.referrers;
	}
	public int depth ()
	{
		return this.depth;
	}
	public boolean isRoot ()
	{
		return this.isRoot;
	}
	public void visualize ()
	{
		mxGraph graph = Variable.graph;
		Object parent = graph.getDefaultParent();
		graph.getModel().beginUpdate();
		try {
			cell = (mxCell) graph.insertVertex(parent, null, name,
					getOffsetX(), getOffsetY(),
					ReplViz.CONTAINER_WIDTH, ReplViz.CONTAINER_HEIGHT,
					"shape=swimlane;foldable=0;fillColor=#999;fontColor=#000;");
			for (Content c : members) {
				c.visualize();
			}

		}
		finally {
			Variable.graph.getView().reload();
			Variable.graph.refresh();
			graph.getModel().endUpdate();
		}
	}
	public void addContent (Type type, String name, Object value)
	{
		if (! isRoot) return;
		removeSameNameMember(name);
		Content c = new Content(this, type, name, value);
		c.initialize();
		c.visualize();
		members.add(c);
	}
	public void addReferrer (Content content)
	{
		if (referrers.contains(content)) return;
		referrers.add(content);
	}
	public void removeSameNameMember (String name)
	{
		if (! isRoot) return;
		Content content = null;
		Variable.graph.getModel().beginUpdate();
		try {
		for (Content c : members) {
			if (name.equals(c.name())) {
				c.destroy();
			}
		}
		if (content == null) return;
		}
		finally {
			Variable.graph.getModel().endUpdate();
		}
	}
	public double getOffsetX ()
	{
		if (isRoot) return 0;

		return (ReplViz.CONTAINER_WIDTH + ReplViz.HORIZONTAL_SPACING )* depth;
	}
	public double getOffsetY ()
	{
		double offsetY = 0;
		Container c = null;
		for (Integer containerID : Variable.containerIDs) {
			if (containerID == ID) return offsetY;
			c = (Container) Variable.containers.get(containerID);
			if (c.depth != depth) continue;
			offsetY += c.cell.getGeometry().getHeight() + ReplViz.VERTICAL_SPACING;
		}
		return offsetY;
	}
	public void reset ()
	{
		root = null;
		super.reset();
	}
	public void removeReferrer (Content content)
	{
		mxGraph graph = Variable.graph;
		referrers.remove(content);
		graph.getModel().beginUpdate();
		try {
			Object[] edges = graph.getEdgesBetween(this.cell, content.cell);
			for (Object edge: edges) {
				graph.getModel().remove(edge);
			}
		}
		finally {
			graph.getModel().endUpdate();
		}
		if (referrers.size() == 0) this.destroy();
	}
	/**
	 * This method acts as it has no referrers. Remove referrers before destroy.
	 */
	protected void destroy ()
	{
		for (Content c : members) {
			c.destroy();
		}
		Variable.graph.getModel().beginUpdate();
		try {
			cell.removeFromParent();
		}
		finally {
			Variable.graph.getModel().endUpdate();
		}
		members = null;
		Variable.containerIDs.remove(new Integer(ID));
		Variable.containers.remove(ID);
	}
	public void redraw ()
	{
		mxIGraphModel model = Variable.graph.getModel();
		model.beginUpdate();
		try {
			new mxStackLayout(graph, false).execute(root.cell);
			List<Integer> done = new ArrayList<Integer>();
			for (Variable v : Variable.containers.values()) {
				Container c = (Container) v;
				if (c.isRoot) continue;
				if (done.contains(c.ID)) continue;
				mxGeometry geo = (mxGeometry) ((mxGeometry) model.getGeometry(c.cell)).clone();
				geo.setX(c.getOffsetX());
				geo.setY(c.getOffsetY());
				model.setGeometry(c.cell, geo);
				done.add(c.ID);
			}
		}
		finally {
			graph.updateCellSize(root.cell, false);
			graph.getView().reload();
			graph.refresh();
			graph.getModel().endUpdate();
		}
	}
}
