package info.archinnov.achilles.entity;

import static info.archinnov.achilles.entity.metadata.PropertyType.SIMPLE;
import static info.archinnov.achilles.entity.metadata.factory.PropertyMetaFactory.factory;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import info.archinnov.achilles.dao.GenericDynamicCompositeDao;
import info.archinnov.achilles.entity.manager.CompleteBeanTestBuilder;
import info.archinnov.achilles.entity.metadata.EntityMeta;
import info.archinnov.achilles.entity.metadata.PropertyMeta;
import info.archinnov.achilles.exception.BeanMappingException;
import info.archinnov.achilles.proxy.interceptor.JpaEntityInterceptor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Id;

import mapping.entity.CompleteBean;
import mapping.entity.TweetMultiKey;
import mapping.entity.UserBean;
import me.prettyprint.cassandra.utils.TimeUUIDUtils;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.NoOp;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import parser.entity.BeanWithColumnFamilyName;
import parser.entity.ChildBean;

/**
 * EntityHelperTest
 * 
 * @author DuyHai DOAN
 * 
 */
@SuppressWarnings("unused")
@RunWith(MockitoJUnitRunner.class)
public class EntityHelperTest
{

	@Rule
	public ExpectedException expectedEx = ExpectedException.none();

	@Mock
	private EntityMeta<Long> entityMeta;

	@Mock
	private PropertyMeta<Void, Long> idMeta;

	@Mock
	private PropertyMeta<TweetMultiKey, String> wideMapMeta;

	@Mock
	private Map<Method, PropertyMeta<?, ?>> getterMetas;

	@Mock
	private Map<Method, PropertyMeta<?, ?>> setterMetas;

	@Mock
	private GenericDynamicCompositeDao<Long> dao;

	private final EntityHelper helper = new EntityHelper();

	@Test
	public void should_derive_getter() throws Exception
	{

		class Test
		{

			Boolean old;
		}

		assertThat(helper.deriveGetterName(Test.class.getDeclaredField("old"))).isEqualTo("getOld");
	}

	@Test
	public void should_derive_getter_for_boolean_primitive() throws Exception
	{

		class Test
		{

			boolean old;
		}

		assertThat(helper.deriveGetterName(Test.class.getDeclaredField("old"))).isEqualTo("isOld");
	}

	@Test
	public void should_derive_setter() throws Exception
	{
		class Test
		{

			boolean a;
		}

		assertThat(helper.deriveSetterName("a")).isEqualTo("setA");
	}

	@Test
	public void should_exception_when_no_getter() throws Exception
	{

		class Test
		{
			String name;
		}

		expectedEx.expect(BeanMappingException.class);
		expectedEx.expectMessage("The getter for field 'name' does not exist");

		helper.findGetter(Test.class, Test.class.getDeclaredField("name"));
	}

	@Test
	public void should_exception_when_no_setter() throws Exception
	{

		class Test
		{
			String name;

			public String getA()
			{
				return name;
			}
		}

		expectedEx.expect(BeanMappingException.class);
		expectedEx.expectMessage("The setter for field 'name' does not exist");

		helper.findSetter(Test.class, Test.class.getDeclaredField("name"));
	}

	@Test
	public void should_exception_when_incorrect_getter() throws Exception
	{

		class Test
		{
			String name;

			public Long getName()
			{
				return 1L;
			}

		}
		expectedEx.expect(BeanMappingException.class);
		expectedEx.expectMessage("The getter for field 'name' does not return correct type");

		helper.findGetter(Test.class, Test.class.getDeclaredField("name"));
	}

	@Test
	public void should_exception_when_setter_returning_wrong_type() throws Exception
	{

		class Test
		{
			String name;

			public String getName()
			{
				return name;
			}

			public Long setName(String name)
			{
				return 1L;
			}

		}
		expectedEx.expect(BeanMappingException.class);
		expectedEx
				.expectMessage("The setter for field 'name' does not return correct type or does not have the correct parameter");

		helper.findSetter(Test.class, Test.class.getDeclaredField("name"));
	}

	@Test
	public void should_exception_when_setter_taking_wrong_type() throws Exception
	{

		class Test
		{
			String name;

			public String getName()
			{
				return name;
			}

			public void setName(Long name)
			{}

		}

		expectedEx.expect(BeanMappingException.class);
		expectedEx.expectMessage("The setter for field 'name' does not exist or is incorrect");

		helper.findSetter(Test.class, Test.class.getDeclaredField("name"));
	}

	@Test
	public void should_find_accessors() throws Exception
	{

		Method[] accessors = helper.findAccessors(Bean.class,
				Bean.class.getDeclaredField("complicatedAttributeName"));

		assertThat(accessors).hasSize(2);
		assertThat(accessors[0].getName()).isEqualTo("getComplicatedAttributeName");
		assertThat(accessors[1].getName()).isEqualTo("setComplicatedAttributeName");
	}

	@Test
	public void should_find_accessors_from_collection_types() throws Exception
	{

		Method[] accessors = helper.findAccessors(ComplexBean.class,
				ComplexBean.class.getDeclaredField("friends"));

		assertThat(accessors).hasSize(2);
		assertThat(accessors[0].getName()).isEqualTo("getFriends");
		assertThat(accessors[1].getName()).isEqualTo("setFriends");
	}

	@Test
	public void should_find_accessors_from_widemap_type() throws Exception
	{
		Method[] accessors = helper.findAccessors(CompleteBean.class,
				CompleteBean.class.getDeclaredField("tweets"));

		assertThat(accessors).hasSize(2);
		assertThat(accessors[0].getName()).isEqualTo("getTweets");
		assertThat(accessors[1]).isNull();
	}

	@Test
	public void should_get_value_from_field() throws Exception
	{
		Bean bean = new Bean();
		bean.setComplicatedAttributeName("test");
		Method getter = Bean.class.getDeclaredMethod("getComplicatedAttributeName");

		String value = (String) helper.getValueFromField(bean, getter);
		assertThat(value).isEqualTo("test");
	}

	@Test
	public void should_set_value_to_field() throws Exception
	{
		Bean bean = new Bean();
		Method setter = Bean.class.getDeclaredMethod("setComplicatedAttributeName", String.class);

		helper.setValueToField(bean, setter, "fecezzef");

		assertThat(bean.getComplicatedAttributeName()).isEqualTo("fecezzef");
	}

	@SuppressWarnings("unchecked")
	@Test
	public void should_get_value_from_collection_field() throws Exception
	{
		ComplexBean bean = new ComplexBean();
		bean.setFriends(Arrays.asList("foo", "bar"));
		Method getter = ComplexBean.class.getDeclaredMethod("getFriends");

		List<String> value = (List<String>) helper.getValueFromField(bean, getter);
		assertThat(value).containsExactly("foo", "bar");
	}

	@Test
	public void should_get_key() throws Exception
	{
		Bean bean = new Bean();
		bean.setComplicatedAttributeName("test");

		Method[] accessors = helper.findAccessors(Bean.class,
				Bean.class.getDeclaredField("complicatedAttributeName"));
		PropertyMeta<Void, String> idMeta = factory(Void.class, String.class).type(SIMPLE)
				.propertyName("complicatedAttributeName").accessors(accessors).build();

		String key = helper.getKey(bean, idMeta);
		assertThat(key).isEqualTo("test");
	}

	@SuppressWarnings(
	{
			"unchecked",
			"rawtypes"
	})
	@Test
	public void should_get_inherited_field_by_annotation() throws Exception
	{
		Field id = helper.getInheritedPrivateFields(ChildBean.class, Id.class);

		assertThat(id.getName()).isEqualTo("id");
		assertThat(id.getType()).isEqualTo((Class) Long.class);
	}

	@SuppressWarnings(
	{
			"unchecked",
			"rawtypes"
	})
	@Test
	public void should_get_inherited_field_by_annotation_and_name() throws Exception
	{
		Field address = helper.getInheritedPrivateFields(ChildBean.class, Column.class, "address");

		assertThat(address.getName()).isEqualTo("address");
		assertThat(address.getType()).isEqualTo((Class) String.class);
	}

	@Test
	public void should_find_serial_version_UID() throws Exception
	{
		class Test
		{
			private static final long serialVersionUID = 1542L;
		}

		Long serialUID = helper.findSerialVersionUID(Test.class);
		assertThat(serialUID).isEqualTo(1542L);
	}

	@Test
	public void should_exception_when_no_serial_version_UID() throws Exception
	{
		class Test
		{
			private static final long fieldName = 1542L;
		}

		expectedEx.expect(BeanMappingException.class);
		expectedEx
				.expectMessage("The 'serialVersionUID' property should be declared for entity 'null'");

		helper.findSerialVersionUID(Test.class);
	}

	@Test
	public void should_infer_column_family_from_annotation() throws Exception
	{
		String cfName = helper.inferColumnFamilyName(BeanWithColumnFamilyName.class,
				"canonicalName");
		assertThat(cfName).isEqualTo("myOwnCF");
	}

	@Test
	public void should_infer_column_family_from_default_name() throws Exception
	{
		String cfName = helper.inferColumnFamilyName(CompleteBean.class, "canonicalName");
		assertThat(cfName).isEqualTo("canonicalName");
	}

	@Test
	public void should_proxy_true() throws Exception
	{
		Enhancer enhancer = new Enhancer();

		enhancer.setSuperclass(CompleteBean.class);
		enhancer.setCallback(NoOp.INSTANCE);

		CompleteBean proxy = (CompleteBean) enhancer.create();

		assertThat(helper.isProxy(proxy)).isTrue();
	}

	@Test
	public void should_proxy_false() throws Exception
	{
		CompleteBean bean = CompleteBeanTestBuilder.builder().id(1L).buid();
		assertThat(helper.isProxy(bean)).isFalse();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void should_derive_base_class() throws Exception
	{
		CompleteBean entity = CompleteBeanTestBuilder.builder().id(1L).buid();
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(entity.getClass());

		JpaEntityInterceptor<Long, CompleteBean> interceptor = new JpaEntityInterceptor<Long, CompleteBean>();
		interceptor.setTarget(entity);

		enhancer.setCallback(interceptor);

		CompleteBean proxy = (CompleteBean) enhancer.create();

		assertThat((Class<CompleteBean>) helper.deriveBaseClass(proxy)).isEqualTo(
				CompleteBean.class);
	}

	@Test
	public void should_determine_primary_key() throws Exception
	{
		Method idGetter = CompleteBean.class.getDeclaredMethod("getId");

		when(entityMeta.getIdMeta()).thenReturn(idMeta);
		when(idMeta.getGetter()).thenReturn(idGetter);

		CompleteBean bean = CompleteBeanTestBuilder.builder().id(12L).buid();

		Object key = helper.determinePrimaryKey(bean, entityMeta);

		assertThat(key).isEqualTo(12L);
	}

	@Test
	public void should_determine_null_primary_key() throws Exception
	{
		Method idGetter = CompleteBean.class.getDeclaredMethod("getId");

		when(entityMeta.getIdMeta()).thenReturn(idMeta);
		when(idMeta.getGetter()).thenReturn(idGetter);

		CompleteBean bean = CompleteBeanTestBuilder.builder().buid();

		Object key = helper.determinePrimaryKey(bean, entityMeta);

		assertThat(key).isNull();

	}

	@Test
	public void should_determine_multikey() throws Exception
	{
		Method idGetter = TweetMultiKey.class.getDeclaredMethod("getId");
		Method authorGetter = TweetMultiKey.class.getDeclaredMethod("getAuthor");
		Method retweetCountGetter = TweetMultiKey.class.getDeclaredMethod("getRetweetCount");

		TweetMultiKey multiKey = new TweetMultiKey();
		UUID uuid = TimeUUIDUtils.getUniqueTimeUUIDinMillis();

		multiKey.setId(uuid);
		multiKey.setAuthor("author");
		multiKey.setRetweetCount(12);

		List<Object> multiKeyList = helper.determineMultiKey(multiKey,
				Arrays.asList(idGetter, authorGetter, retweetCountGetter));

		assertThat(multiKeyList).hasSize(3);
		assertThat(multiKeyList.get(0)).isEqualTo(uuid);
		assertThat(multiKeyList.get(1)).isEqualTo("author");
		assertThat(multiKeyList.get(2)).isEqualTo(12);
	}

	@Test
	public void should_determine_multikey_with_null() throws Exception
	{
		Method idGetter = TweetMultiKey.class.getDeclaredMethod("getId");
		Method authorGetter = TweetMultiKey.class.getDeclaredMethod("getAuthor");
		Method retweetCountGetter = TweetMultiKey.class.getDeclaredMethod("getRetweetCount");

		TweetMultiKey multiKey = new TweetMultiKey();
		UUID uuid = TimeUUIDUtils.getUniqueTimeUUIDinMillis();

		multiKey.setId(uuid);
		multiKey.setRetweetCount(12);

		List<Object> multiKeyList = helper.determineMultiKey(multiKey,
				Arrays.asList(idGetter, authorGetter, retweetCountGetter));

		assertThat(multiKeyList).hasSize(3);
		assertThat(multiKeyList.get(0)).isEqualTo(uuid);
		assertThat(multiKeyList.get(1)).isNull();
		assertThat(multiKeyList.get(2)).isEqualTo(12);
	}

	@Test
	public void should_build_proxy() throws Exception
	{
		Method idGetter = CompleteBean.class.getDeclaredMethod("getId", (Class<?>[]) null);
		Method idSetter = CompleteBean.class.getDeclaredMethod("setId", Long.class);

		CompleteBean entity = CompleteBeanTestBuilder.builder().id(1L).name("name").buid();

		when(entityMeta.getIdMeta()).thenReturn(idMeta);
		when(idMeta.getGetter()).thenReturn(idGetter);
		when(idMeta.getSetter()).thenReturn(idSetter);

		when(entityMeta.getGetterMetas()).thenReturn(getterMetas);
		when(entityMeta.getSetterMetas()).thenReturn(setterMetas);
		when(entityMeta.getEntityDao()).thenReturn(dao);
		when(entityMeta.getIdMeta()).thenReturn(idMeta);

		when(idMeta.getGetter()).thenReturn(idGetter);
		when(idMeta.getSetter()).thenReturn(idSetter);

		CompleteBean proxy = helper.buildProxy(entity, entityMeta);

		assertThat(proxy).isNotNull();
		assertThat(proxy).isInstanceOf(Factory.class);
		Factory factory = (Factory) proxy;

		assertThat(factory.getCallbacks()).hasSize(1);
		assertThat(factory.getCallback(0)).isInstanceOf(JpaEntityInterceptor.class);

	}

	@SuppressWarnings("unchecked")
	@Test
	public void should_get_real_object_from_proxy() throws Exception
	{
		UserBean realObject = new UserBean();
		JpaEntityInterceptor<Long, UserBean> interceptor = mock(JpaEntityInterceptor.class);
		when(interceptor.getTarget()).thenReturn(realObject);

		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(UserBean.class);
		enhancer.setCallback(interceptor);
		UserBean proxy = (UserBean) enhancer.create();

		UserBean actual = helper.getRealObject(proxy);

		assertThat(actual).isSameAs(realObject);

	}

	@SuppressWarnings("unchecked")
	@Test
	public void should_get_interceptor_from_proxy() throws Exception
	{
		JpaEntityInterceptor<Long, UserBean> interceptor = mock(JpaEntityInterceptor.class);

		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(UserBean.class);
		enhancer.setCallback(interceptor);
		UserBean proxy = (UserBean) enhancer.create();

		JpaEntityInterceptor<Long, UserBean> actual = helper.getInterceptor(proxy);

		assertThat(actual).isSameAs(interceptor);

	}

	@SuppressWarnings("unchecked")
	@Test
	public void should_ensure_proxy() throws Exception
	{
		JpaEntityInterceptor<Long, UserBean> interceptor = mock(JpaEntityInterceptor.class);

		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(UserBean.class);
		enhancer.setCallback(interceptor);
		UserBean proxy = (UserBean) enhancer.create();

		helper.ensureProxy(proxy);
	}

	@Test(expected = IllegalStateException.class)
	public void should_exception_when_not_proxy() throws Exception
	{

		helper.ensureProxy(new CompleteBean());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void should_unproxy_entity() throws Exception
	{
		CompleteBean realObject = new CompleteBean();

		JpaEntityInterceptor<Long, CompleteBean> interceptor = mock(JpaEntityInterceptor.class);

		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(CompleteBean.class);
		enhancer.setCallback(interceptor);
		CompleteBean proxy = (CompleteBean) enhancer.create();

		when(interceptor.getTarget()).thenReturn(realObject);

		CompleteBean actual = helper.unproxy(proxy);

		assertThat(actual).isSameAs(realObject);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void should_unproxy_collection_of_entities() throws Exception
	{
		CompleteBean realObject = new CompleteBean();
		Collection<CompleteBean> proxies = new ArrayList<CompleteBean>();

		JpaEntityInterceptor<Long, CompleteBean> interceptor = mock(JpaEntityInterceptor.class);

		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(CompleteBean.class);
		enhancer.setCallback(interceptor);
		CompleteBean proxy = (CompleteBean) enhancer.create();
		proxies.add(proxy);

		when(interceptor.getTarget()).thenReturn(realObject);

		Collection<CompleteBean> actual = helper.unproxy(proxies);

		assertThat(actual).containsExactly(realObject);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void should_unproxy_list_of_entities() throws Exception
	{
		CompleteBean realObject = new CompleteBean();
		List<CompleteBean> proxies = new ArrayList<CompleteBean>();

		JpaEntityInterceptor<Long, CompleteBean> interceptor = mock(JpaEntityInterceptor.class);

		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(CompleteBean.class);
		enhancer.setCallback(interceptor);
		CompleteBean proxy = (CompleteBean) enhancer.create();
		proxies.add(proxy);

		when(interceptor.getTarget()).thenReturn(realObject);

		List<CompleteBean> actual = helper.unproxy(proxies);

		assertThat(actual).containsExactly(realObject);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void should_unproxy_set_of_entities() throws Exception
	{
		CompleteBean realObject = new CompleteBean();
		Set<CompleteBean> proxies = new HashSet<CompleteBean>();

		JpaEntityInterceptor<Long, CompleteBean> interceptor = mock(JpaEntityInterceptor.class);

		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(CompleteBean.class);
		enhancer.setCallback(interceptor);
		CompleteBean proxy = (CompleteBean) enhancer.create();
		proxies.add(proxy);

		when(interceptor.getTarget()).thenReturn(realObject);

		Set<CompleteBean> actual = helper.unproxy(proxies);

		assertThat(actual).containsExactly(realObject);
	}

	class Bean
	{

		private String complicatedAttributeName;

		public String getComplicatedAttributeName()
		{
			return complicatedAttributeName;
		}

		public void setComplicatedAttributeName(String complicatedAttributeName)
		{
			this.complicatedAttributeName = complicatedAttributeName;
		}
	}

	class ComplexBean
	{
		private List<String> friends;

		public List<String> getFriends()
		{
			return friends;
		}

		public void setFriends(List<String> friends)
		{
			this.friends = friends;
		}
	}
}
