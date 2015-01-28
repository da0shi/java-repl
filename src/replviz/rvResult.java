package replviz;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.Modifier;
import java.lang.StringBuilder;
import java.util.ArrayList;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

import replviz.Utils;

public class rvResult
{
	private final String key;
	private final Object value;
	private final Type type;

	private mxCell referCell;
	private mxCell entityCell;

	public rvResult (String key, Object value, Type type)
	{
		this.key = key;
		this.value = value;
		this.type = type;
	}

	public String key ()
	{
		return key;
	}
	public Object value ()
	{
		return value;
	}
	public Type type ()
	{
		if (value != null) {
			return Utils.extractType(type);
		}
		return Utils.extractType(Object.class);
	}
	public boolean isPrimitive ()
	{
		return Utils.isPrimitive(type);
	}
	public String strType ()
	{
		return Utils.getType(type);
	}
	public String label ()
	{
		if (value == null) {
			return "null";
		}
		if (Utils.isPrimitive(type)) {
			return value.toString();
		}

		return Utils.getType(Utils.extractType(value.getClass())) +"@"+ Utils.getHexID(value);
	}
	public String valueRef ()
	{
		if (value == null) {
			return "NULL";
		}
		if (isPrimitive()) {
			return value.toString();
		}
		return Utils.getType(Utils.extractType(value.getClass())) +"@"+ Utils.getHexID(value);
	}

	public void insertVariable (mxGraph graph, mxCell refbox)
	{
		Object parent = graph.getDefaultParent();
		graph.getModel().beginUpdate();
		try {
			int y = 0;
			int childCount = refbox.getChildCount();
			if (childCount > 0) {
				mxICell lastChild = refbox.getChildAt(childCount - 1);
				mxGeometry geo = lastChild.getGeometry();
				y = (int)(geo.getY() + geo.getHeight());
			}
			referCell  = (mxCell) graph.insertVertex(refbox, null,
					strType() +"  "+  key +" = "+ valueRef(),
					0, y, ReplViz.VARIABLE_WIDTH, ReplViz.VARIABLE_HEIGHT);
			if (y == 0) y = 40;
			if (! isPrimitive()) {
				entityCell = insertEntity(graph);
				graph.insertEdge(parent, null, null, referCell, entityCell);
			}
		}
		catch (NullPointerException e) {
			e.printStackTrace();
		}
		finally {
			graph.getModel().endUpdate();
		}
	}

	public mxCell referCell ()
	{
		return this.referCell;
	}
	public void referCell (mxCell cell)
	{
		this.referCell = cell;
	}

	public mxCell entityCell ()
	{
		return this.entityCell;
	}
	public void entityCell (mxCell cell)
	{
		this.entityCell = cell;
	}

	private mxCell insertEntity (mxGraph graph)
	{
		Object parent = graph.getDefaultParent();
		mxCell frame = null;
		graph.getModel().beginUpdate();
		try {
			frame = (mxCell) graph.insertVertex(
					parent, null, valueRef(), ReplViz.CONTAINER_WIDTH + 40, 0,
					ReplViz.CONTAINER_WIDTH, 2 * ReplViz.VARIABLE_HEIGHT,
					"shape=swimlane;foldable=0;fillColor=#999;fontColor=#000");

			// if toString has been overridden
			if (Utils.isOverridden(value, "toString")) {
				mxCell field  = (mxCell) graph.insertVertex(frame, null,
						value.toString().trim(),
						0, 0, ReplViz.VARIABLE_WIDTH, ReplViz.VARIABLE_HEIGHT);
			}
			else {
				Field[] fields = value.getClass().getFields();
				int y = 0;
				for (Field f : fields) {
					int childCount = frame.getChildCount();
					if (childCount > 0) {
						mxICell lastChild = frame.getChildAt(childCount - 1);
						mxGeometry geo = lastChild.getGeometry();
						y = (int)(geo.getY() + geo.getHeight());
					}

					mxCell field  = (mxCell) graph.insertVertex(frame, null,
							Utils.getType(f.getType()) +"  "+  f.getName() +" = "+ f.get(value),
							0, y, ReplViz.VARIABLE_WIDTH, ReplViz.VARIABLE_HEIGHT);
				}
			}
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		finally {
			graph.getModel().endUpdate();
		}


		return frame;
	}

	public void removeVariable (mxGraph graph)
	{
		graph.getModel().beginUpdate();
		try {
			Object[] edges = graph.getEdgesBetween(referCell, entityCell);
			for (Object edge: edges) {
				graph.getModel().remove(edge);
			}
			graph.getModel().remove(referCell);
			graph.getModel().remove(entityCell);
			referCell = null;
			entityCell = null;
		}
		finally {
			graph.getModel().endUpdate();
		}
	}

}
