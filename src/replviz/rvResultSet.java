package replviz;

import java.lang.reflect.Type;
import java.lang.reflect.Field;
import java.util.List;
import java.util.ArrayList;

import com.mxgraph.layout.mxStackLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.view.mxGraph;

public class rvResultSet
{
	private rvResult result;
	private mxCell referrer;
	private mxCell entity;

	public rvResultSet (rvResult result)
	{
		this.result = result;
		this.referrer = null;
		this.entity = null;
	}
	public rvResult result ()
	{
		return this.result;
	}
	public mxCell referrer ()
	{
		return this.referrer;
	}
	public mxCell entity ()
	{
		return this.entity;
	}
	public void entity (mxCell cell)
	{
		this.entity = cell;
	}

	public mxCell insertRefer (mxGraph graph, mxCell container)
	{
		Object parent = graph.getDefaultParent();
		graph.getModel().beginUpdate();
		try {
			referrer = (mxCell) graph.insertVertex(container, null,
					result.strType() +"  "+  result.key() +" = "+ result.valueRef(),
					0, getStartOffsetHeight(container),
					ReplViz.VARIABLE_WIDTH, ReplViz.VARIABLE_HEIGHT);
		}
		catch (NullPointerException e) {
			e.printStackTrace();
		}
		finally {
			graph.getModel().endUpdate();
		}
		return referrer;
	}

	public mxCell insertEntity (mxGraph graph, mxCell container)
	{
		Object parent = graph.getDefaultParent();
		Object value = result.value();
		entity = container;
		graph.getModel().beginUpdate();
		try {

			// if toString has been overridden
			if (! Object.class.equals(value.getClass().getMethod("toString").getDeclaringClass())) {
				mxCell field  = (mxCell) graph.insertVertex(container, null,
						result.value().toString(),
						0, getStartOffsetHeight(container),
						ReplViz.VARIABLE_WIDTH, ReplViz.VARIABLE_HEIGHT);
			}
			else {
				Field[] fields = value.getClass().getFields();
				int y = 0;
				for (Field f : fields) {
					mxCell field  = (mxCell) graph.insertVertex(container, null,
							Utils.getType(f.getType()) +"  "+  f.getName() +" = "+ f.get(value),
							0, getStartOffsetHeight(container),
							ReplViz.VARIABLE_WIDTH, ReplViz.VARIABLE_HEIGHT);
				}
			}
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		finally {
			graph.getModel().endUpdate();
		}
		return container;
	}

	public void removeCells (mxGraph graph)
	{
		graph.getModel().beginUpdate();
		try {
			mxCell container = (mxCell)referrer.getParent();
			Object[] edges = graph.getEdgesBetween(referrer, entity);
			for (Object edge: edges) {
				graph.getModel().remove(edge);
			}
			referrer.removeFromParent();
			if (entity != null) {
				entity.removeFromParent();
			}
			new mxStackLayout(graph, false).execute(container);
			graph.updateCellSize(container, false);
		}
		finally {
			graph.getView().reload();
			graph.refresh();
			graph.getModel().endUpdate();
		}
	}

	public void removeEdge (mxGraph graph)
	{
		graph.getModel().beginUpdate();
		try {
			mxCell container = (mxCell)referrer.getParent();
			Object[] edges = graph.getEdgesBetween(referrer, entity);
			for (Object edge: edges) {
				graph.getModel().remove(edge);
			}
			referrer.removeFromParent();
			new mxStackLayout(graph, false).execute(container);
			graph.updateCellSize(container, false);
		}
		finally {
			graph.getView().reload();
			graph.refresh();
			graph.getModel().endUpdate();
		}
	}

	private List<mxICell> getChildren (mxCell cell)
	{
		List<mxICell> children = new ArrayList<mxICell>();
		for (int i = 0, length = cell.getChildCount(); i < length; i += 1) {
			children.add(cell.getChildAt(i));
		}
		return children;
	}
	private double getStartOffsetHeight (mxCell container)
	{
		int length = container.getChildCount();
		if (length == 0) return 0;
		mxICell last = container.getChildAt(length - 1);
		mxGeometry geo = last.getGeometry();
		return geo.getY() + geo.getHeight();
	}
}
