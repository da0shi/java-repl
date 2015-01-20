package replviz;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.Modifier;
import java.lang.StringBuilder;
import java.util.ArrayList;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

/*
class ReplVizResults
{
	private static final int CONTAINER_WIDTH = 240;
	private static final int CONTAINER_HEIGHT = 640;
	private static final int CONTENT_WIDTH = CONTAINER_WIDTH;
	private static final int CONTENT_HEIGHT =  20;

	mxGraph graph;
	mxCell results;
	List<Result> resultList;

	public ReplVizResults (mxGraph graph)
	{
		this.graph = graph;
		resultList = new ArrayList<Result>();
	}

	public mxGraphComponent init ()
	{
		Object parent = graph.getDefaultParent();
		graph.getModel().beginUpdate();
		try {
			results = (mxCell) graph.insertVertex(parent, null, "Variables", 20, 20, CONTAINER_HEIGHT, CONTAINER_WIDTH, "shape=swimlane;foldable=0;fillColor=#999;fontColor=#000");
		}
		finally {
			graph.getModel().endUpdate();
		}
		return new mxGraphComponent(graph);
	}

	public mxGraphComponent add (String key, Object value, Type type)
	{
		Result r = new Result(key, value, type);
//		if (filterByKey(r.key()) != null) {
//			return update(r);
//		} else {
//			resultList.add(r);
//		}
		resultList.add(r);

		graph.getModel().beginUpdate();

		try {
			int y = 0;
			int childCount = results.getChildCount();
			if (childCount > 0) {
				mxICell lastChild = results.getChildAt(childCount - 1);
				mxGeometry geo = lastChild.getGeometry();
				y = (int)(geo.getY() + geo.getHeight());
			}
			String classname = value.getClass().getName();
			mxCell var = (mxCell) graph.insertVertex(results, null,
					classname +" | "+ classname +"@"+ Integer.toHexString(value.hashCode()),
					0, y, CONTENT_WIDTH, CONTENT_HEIGHT);
		}
		catch (NullPointerException e) {
			e.printStackTrace();
		}
		finally {
			graph.getModel().endUpdate();
		}
	}
}
*/

public class ReplVizResult
{
	private final String key;
	private final Object value;
	private final Type type;

	public ReplVizResult (String key, Object value, Type type)
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
		if (value instanceof Class) {
			return getType(extractType(value.getClass())) +"@"+ Integer.toHexString(value.hashCode());
		}
		return value.toString();
	}

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

	public static Type extractType(Type type) {
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
}
