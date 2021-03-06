package info.archinnov.achilles.entity.parser;

import static org.fest.assertions.api.Assertions.assertThat;

import info.archinnov.achilles.annotations.Lazy;
import info.archinnov.achilles.entity.parser.PropertyFilter;

import java.lang.reflect.Field;

import mapping.entity.CompleteBean;

import org.junit.Test;

import parser.entity.ParentBean;

/**
 * PropertyFilterTest
 * 
 * @author DuyHai DOAN
 * 
 */
public class PropertyFilterTest
{
	private PropertyFilter filter = new PropertyFilter();

	@Test
	public void should_match() throws Exception
	{
		Field name = CompleteBean.class.getDeclaredField("name");

		assertThat(filter.matches(name)).isTrue();
	}

	@Test
	public void should_match_annotation() throws Exception
	{
		Field friends = CompleteBean.class.getDeclaredField("friends");

		assertThat(filter.matches(friends, Lazy.class)).isTrue();
	}

	@Test
	public void should_match_annotation_and_name() throws Exception
	{
		Field friends = CompleteBean.class.getDeclaredField("friends");

		assertThat(filter.matches(friends, Lazy.class, "friends")).isTrue();
	}

	@Test
	public void should_not_match() throws Exception
	{
		Field name = ParentBean.class.getDeclaredField("unmapped");

		assertThat(filter.matches(name)).isFalse();
	}

	@Test
	public void should_have_annotation() throws Exception
	{
		Field name = CompleteBean.class.getDeclaredField("name");

		assertThat(filter.hasAnnotation(name, javax.persistence.Column.class)).isTrue();
	}

	@Test
	public void should_not_have_annotation() throws Exception
	{
		Field name = CompleteBean.class.getDeclaredField("name");

		assertThat(filter.hasAnnotation(name, javax.persistence.JoinColumn.class)).isFalse();
	}
}
