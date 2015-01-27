package replviz;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.Modifier;
import java.lang.StringBuilder;
import java.util.List;
import java.util.ArrayList;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxGeometry;

public class Utils
{
	public static String toString(Object val)
	{
		if (val == null) return "NULL";
		return val.toString();
	}
	public static boolean isPrimitive (Type type)
	{
		return (! (type instanceof Class) || ((Class)type).isPrimitive());
	}
	public static String getPackageName (Class klass)
	{
		return (klass.getPackage() == null) ? "" : klass.getPackage().getName();
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

}
