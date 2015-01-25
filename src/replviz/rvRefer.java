package replviz;

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

public class rvRefer
{
	private final Type type;
	private final String key;
	private final int hash;

	public rvRefer (Type type, String key, int hash)
	{
		this.type = type;
		this.key = key;
		this.hash = hash;
	}
	/*
	private mxCell refCell;
	private rvEntity entity;

	public rvRefer (Object value, String type)
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
			return extractType(type);
		}
		return extractType(Object.class);
	}
	public String strType ()
	{
		return getType(type);
	}
	public String valueRef ()
	{
		if (value == null) return "Null";
			if (! (type instanceof Class) || ! ((Class)type).isPrimitive()) {
			return getType(extractType(value.getClass())) +"@"+ Integer.toHexString(value.hashCode());
		}
		return value.toString();
	}

	public void insertVar (mxGraph graph, mxCell refbox)
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
			refCell  = (mxCell) graph.insertVertex(refbox, null,
					strType() +"  "+  key +" = "+ valueRef(),
					0, y, ReplViz.VARIABLE_WIDTH, ReplViz.VARIABLE_HEIGHT);
			if (y == 0) y = 40;
			if (! (type instanceof Class) || ! ((Class)type).isPrimitive()) {
				entityCell = (mxCell) graph.insertVertex(parent, null,
						value.toString(),
						340, y, ReplViz.VARIABLE_WIDTH, ReplViz.VARIABLE_HEIGHT);
				graph.insertEdge(parent, null, null, refCell, entityCell);
			}
		}
		catch (NullPointerException e) {
			e.printStackTrace();
		}
		finally {
			graph.getModel().endUpdate();
		}
	}

	public void removeVar (mxGraph graph)
	{
		mxIGraphModel model = graph.getModel();
		model.beginUpdate();
		try {
			System.out.println("Before: "+ refCell.getEdgeCount());
			for (int i = 0, max = refCell.getEdgeCount(); i < max; i += 1) {
				model.remove(refCell.getEdgeAt(i));
			}
			model.remove(refCell);
			model.remove(entityCell);
		}
		finally {
			model.endUpdate();
		}
	}

	// Static functions
	public static String getType (Type type)
	{
		if (type instanceof Class) {
			return getType((Class)extractType(type));
		} else if (type instanceof TypeVariable) {
			return getType((TypeVariable)extractType(type));
		} else if (type instanceof ParameterizedType) {
			return getType((ParameterizedType)extractType(type));
		} else {
			return type.toString();
		}
	}
	public static String getType (Class type)
	{
		return type.getCanonicalName();
	}
	public static String getType (TypeVariable type)
	{
		return "Object";
	}
	public static String getType (ParameterizedType type)
	{
		StringBuilder sbuild = new StringBuilder();
		Type[] ts = type.getActualTypeArguments();
		for(int i = 0; i < ts.length; i += 1) {
			if (i != 0) {
				sbuild.append(",");
			}
			sbuild.append(getType(ts[i]));
		}
		return String.format("%s<%s>",
				getType(type.getRawType()),
				sbuild.toString());
	}

	public static Type extractType(Type type)
	{
		if (! (type instanceof Class)) {
			return type;
		}

		Class klass = (Class) type;
		Type genericSuperclass = klass.getGenericSuperclass();

		if (! klass.isAnonymousClass() && ! klass.isSynthetic()) {
			if (Modifier.isPrivate(klass.getModifiers())) {
				return extractType(genericSuperclass);
			}

			return klass;
		}

		if (! genericSuperclass.equals(Object.class)) {
			return extractType(klass.getGenericSuperclass());
		}

		Type[] types = klass.getGenericInterfaces();
		if (types != null) {
			return extractType(types[0]);
		}
		return extractType(Object.class);
	}
	*/
}
