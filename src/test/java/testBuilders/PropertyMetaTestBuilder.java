package testBuilders;

import info.archinnov.achilles.dao.GenericCompositeDao;
import info.archinnov.achilles.entity.EntityHelper;
import info.archinnov.achilles.entity.metadata.EntityMeta;
import info.archinnov.achilles.entity.metadata.ExternalWideMapProperties;
import info.archinnov.achilles.entity.metadata.JoinProperties;
import info.archinnov.achilles.entity.metadata.MultiKeyProperties;
import info.archinnov.achilles.entity.metadata.PropertyMeta;
import info.archinnov.achilles.entity.metadata.PropertyType;
import info.archinnov.achilles.entity.type.MultiKey;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.CascadeType;

import mapping.entity.CompleteBean;
import me.prettyprint.cassandra.serializers.SerializerTypeInferer;
import me.prettyprint.hector.api.Serializer;

import org.codehaus.jackson.map.ObjectMapper;

/**
 * PropertyMetaBuilder
 * 
 * @author DuyHai DOAN
 * 
 */
public class PropertyMetaTestBuilder<T, K, V>
{
	private EntityHelper entityHelper = new EntityHelper();

	private Class<T> clazz;
	private String field;
	private PropertyType type;
	private Class<K> keyClass;
	private Class<V> valueClass;
	private EntityMeta<?> joinMeta;
	private List<CascadeType> cascadeTypes = new ArrayList<CascadeType>();

	private List<Class<?>> componentClasses;
	private List<Serializer<?>> componentSerializers;
	private List<Method> componentGetters;
	private List<Method> componentSetters;

	private String externalColumnFamilyName;
	private GenericCompositeDao<?, ?> externalWideMapDao;
	private Serializer<?> idSerializer;

	private boolean buildAccessors;

	private ObjectMapper objectMapper;

	public static <T, K, V> PropertyMetaTestBuilder<T, K, V> of(Class<T> clazz, Class<K> keyClass,
			Class<V> valueClass)
	{
		return new PropertyMetaTestBuilder<T, K, V>(clazz, keyClass, valueClass);
	}

	public static <K, V> PropertyMetaTestBuilder<CompleteBean, K, V> completeBean(
			Class<K> keyClass, Class<V> valueClass)
	{
		return new PropertyMetaTestBuilder<CompleteBean, K, V>(CompleteBean.class, keyClass,
				valueClass);
	}

	public static <K, V> PropertyMetaTestBuilder<Void, K, V> noClass(Class<K> keyClass,
			Class<V> valueClass)
	{
		return new PropertyMetaTestBuilder<Void, K, V>(Void.class, keyClass, valueClass);
	}

	public static <V> PropertyMetaTestBuilder<Void, Void, V> valueClass(Class<V> valueClass)
	{
		return new PropertyMetaTestBuilder<Void, Void, V>(Void.class, Void.class, valueClass);
	}

	public PropertyMetaTestBuilder(Class<T> clazz, Class<K> keyClass, Class<V> valueClass) {
		this.clazz = clazz;
		this.keyClass = keyClass;
		this.valueClass = valueClass;
	}

	@SuppressWarnings(
	{
			"unchecked",
			"rawtypes"
	})
	public PropertyMeta<K, V> build() throws Exception
	{
		PropertyMeta<K, V> propertyMeta = new PropertyMeta<K, V>();
		propertyMeta.setType(type);
		propertyMeta.setPropertyName(field);
		propertyMeta.setKeyClass(keyClass);
		propertyMeta.setValueClass(valueClass);
		propertyMeta
				.setKeySerializer((Serializer<K>) SerializerTypeInferer.getSerializer(keyClass));
		propertyMeta.setValueSerializer((Serializer<V>) SerializerTypeInferer
				.getSerializer(valueClass));

		if (buildAccessors)
		{
			Field declaredField = clazz.getDeclaredField(field);
			propertyMeta.setGetter(entityHelper.findGetter(clazz, declaredField));
			propertyMeta.setSetter(entityHelper.findSetter(clazz, declaredField));
		}

		if (joinMeta != null || !cascadeTypes.isEmpty())
		{
			JoinProperties joinProperties = new JoinProperties();
			joinProperties.setCascadeTypes(cascadeTypes);
			joinProperties.setEntityMeta(joinMeta);
			propertyMeta.setJoinProperties(joinProperties);
		}

		if (componentClasses != null || componentSerializers != null || componentGetters != null
				|| componentSetters != null)
		{
			MultiKeyProperties multiKeyProperties = new MultiKeyProperties();
			multiKeyProperties.setComponentClasses(componentClasses);
			multiKeyProperties.setComponentSerializers(componentSerializers);
			multiKeyProperties.setComponentGetters(componentGetters);
			multiKeyProperties.setComponentSetters(componentSetters);

			propertyMeta.setMultiKeyProperties(multiKeyProperties);
		}
		if (MultiKey.class.isAssignableFrom(keyClass))
		{
			propertyMeta.setSingleKey(false);
		}
		else
		{
			propertyMeta.setSingleKey(true);
		}

		if (externalColumnFamilyName != null || externalWideMapDao != null || idSerializer != null)
		{
			ExternalWideMapProperties<?> externalWideMapProperties = new ExternalWideMapProperties(
					externalColumnFamilyName, externalWideMapDao, idSerializer);

			propertyMeta.setExternalWideMapProperties(externalWideMapProperties);
		}
		objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
		propertyMeta.setObjectMapper(objectMapper);
		return propertyMeta;
	}

	public PropertyMetaTestBuilder<T, K, V> field(String field)
	{
		this.field = field;
		return this;
	}

	public PropertyMetaTestBuilder<T, K, V> type(PropertyType type)
	{
		this.type = type;
		return this;
	}

	public PropertyMetaTestBuilder<T, K, V> joinMeta(EntityMeta<?> joinMeta)
	{
		this.joinMeta = joinMeta;
		return this;
	}

	public PropertyMetaTestBuilder<T, K, V> cascadeType(CascadeType cascadeType)
	{
		this.cascadeTypes.add(cascadeType);
		return this;
	}

	public PropertyMetaTestBuilder<T, K, V> cascadeTypes(CascadeType... cascadeTypes)
	{
		this.cascadeTypes.addAll(Arrays.asList(cascadeTypes));
		return this;
	}

	public PropertyMetaTestBuilder<T, K, V> externalCf(String externalCf)
	{
		this.externalColumnFamilyName = externalCf;
		return this;
	}

	public PropertyMetaTestBuilder<T, K, V> externalDao(GenericCompositeDao<?, ?> externalDao)
	{
		this.externalWideMapDao = externalDao;
		return this;
	}

	public PropertyMetaTestBuilder<T, K, V> idSerializer(Serializer<?> idSerializer)
	{
		this.idSerializer = idSerializer;
		return this;
	}

	public PropertyMetaTestBuilder<T, K, V> compClasses(List<Class<?>> componentClasses)
	{
		this.componentClasses = componentClasses;
		return this;
	}

	public PropertyMetaTestBuilder<T, K, V> compClasses(Class<?>... componentClasses)
	{
		this.componentClasses = Arrays.asList(componentClasses);
		return this;
	}

	public PropertyMetaTestBuilder<T, K, V> compSrz(List<Serializer<?>> componentSerializers)
	{
		this.componentSerializers = componentSerializers;
		return this;
	}

	public PropertyMetaTestBuilder<T, K, V> compSrz(Serializer<?>... componentSerializers)
	{
		this.componentSerializers = Arrays.asList(componentSerializers);
		return this;
	}

	public PropertyMetaTestBuilder<T, K, V> compGetters(List<Method> componentGetters)
	{
		this.componentGetters = componentGetters;
		return this;
	}

	public PropertyMetaTestBuilder<T, K, V> compGetters(Method... componentGetters)
	{
		this.componentGetters = Arrays.asList(componentGetters);
		return this;
	}

	public PropertyMetaTestBuilder<T, K, V> compSetters(List<Method> componentSetters)
	{
		this.componentSetters = componentSetters;
		return this;
	}

	public PropertyMetaTestBuilder<T, K, V> compSetters(Method... componentSetters)
	{
		this.componentSetters = Arrays.asList(componentSetters);
		return this;
	}

	public PropertyMetaTestBuilder<T, K, V> accesors()
	{
		this.buildAccessors = true;
		return this;
	}

	public PropertyMetaTestBuilder<T, K, V> mapper(ObjectMapper mapper)
	{
		this.objectMapper = mapper;
		return this;
	}

}
