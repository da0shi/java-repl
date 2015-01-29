package replviz;

import java.util.List;
import java.util.ArrayList;
import java.lang.reflect.Type;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.view.mxGraph;

public class Content
	extends Variable
{
	protected final boolean isPrimitive;
	protected final boolean isNull;
	protected final boolean isArray;
	protected final Type type;

	protected Container container = null;
	protected Container refer = null;

	public Content (Container container, Type type, String name, Object value)
	{
		super(name, value);
		this.container = container;
		this.type = type;
		this.isNull = (value == null);
		this.isPrimitive = Utils.isPrimitive(type);
		this.isArray = (value == null) || value.getClass().isArray();
	}
	public void initialize ()
	{
		Variable.contents.put(ID, this);
		if (isNull || isPrimitive) return;

		if (! container.isRoot() && Utils.isJavaPackage(container.value().getClass())) return;
		if (ID == container.ID) {
			if (! isArray) {
				refer.addReferrer(this);
			}
			return;
		}
		if (Variable.containerIDs.contains(ID)) {
			refer = (Container) Variable.containers.get(ID);
			refer.addReferrer(this);
		}
		else {
			refer = new Container(Utils.getType(type) +"@"+ hexID(), value);
			refer.initialize(this);
		}
	}

	public boolean isPrimitive ()
	{
		return this.isPrimitive;
	}
	public boolean isNull ()
	{
		return this.isNull;
	}
	public boolean isArray ()
	{
		return this.isArray;
	}
	public Container container ()
	{
		return this.container;
	}
	public Container refer ()
	{
		return this.refer;
	}

	public void visualize ()
	{
		mxGraph graph = Variable.graph;
		String classname = Utils.getType(type);
		String label = "";
		if (refer != null) {
			label = classname +" "+ name +" = "+ classname +"@"+ hexID();
		}
		Content parentContent = null;
		if (! container.referrers().isEmpty()) {
			parentContent = (Content) container.referrers().get(0);
		}

		if (isPrimitive) {
			label = classname +" "+ name +" = "+ value.toString();
		}
		else if (isNull) {
			label = classname +" "+ name +" = "+ null;
		}
		else if (isArray) {
			if (refer == null) {
				label = javarepl.rendering.ValueRenderer.renderValue(value);
			}
		}
		else if (Utils.isOverridden(value, "toString")) {
			if (refer == null) {
				label += value.toString();
			}
		}
		else if (parentContent != null && Utils.isOverridden(parentContent.value, "toString")) {
			// instance of Content will be the same if container is the same.
			if (refer == null) {
				label += parentContent.value.toString();
			}
		}
		else {
			if (refer == null) {
				label += value.toString();
			}
		}

		graph.getModel().beginUpdate();
		try {
			cell  = (mxCell) graph.insertVertex(container.cell(), null,
					label,
					0, getOffsetY(),
					ReplViz.VARIABLE_WIDTH, ReplViz.VARIABLE_HEIGHT);
			if (refer != null) {
				refer.visualize();
				graph.insertEdge(graph.getDefaultParent(), null, null, cell, refer.cell());
			}
		}
		finally {
			graph.getView().reload();
			graph.refresh();
			graph.getModel().endUpdate();
		}
	}
	private double getOffsetY ()
	{
		mxCell c = container.cell();
		int length = c.getChildCount();
		if (length == 0) return 0;
		mxICell last = c.getChildAt(length - 1);
		mxGeometry geo = last.getGeometry();
		return geo.getY() + geo.getHeight();
	}
	/**
	 * Content#destroy won't remove edge to its refer.
	 */
	public void destroy()
	{
		Variable.graph.getModel().beginUpdate();
		try {
			cell.removeFromParent();
		}
		finally {
			Variable.graph.getModel().beginUpdate();
		}
		if (refer != null) {
			refer.removeReferrer(this);
			refer = null;
		}
		Variable.contents.remove(ID);
	}
}
