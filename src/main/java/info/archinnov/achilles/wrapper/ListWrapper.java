package info.archinnov.achilles.wrapper;

import static info.archinnov.achilles.wrapper.builder.ListIteratorWrapperBuilder.builder;
import static info.archinnov.achilles.wrapper.builder.ListWrapperBuilder.builder;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

/**
 * ListWrapper
 * 
 * @author DuyHai DOAN
 * 
 */
public class ListWrapper<V> extends CollectionWrapper<V> implements List<V>
{

	public ListWrapper(List<V> target) {
		super(target);
	}

	@Override
	public void add(int arg0, V arg1)
	{
		((List<V>) super.target).add(arg0, helper.unproxy(arg1));
		super.markDirty();
	}

	@Override
	public boolean addAll(int arg0, Collection<? extends V> arg1)
	{
		boolean result = ((List<V>) super.target).addAll(arg0, helper.unproxy(arg1));
		if (result)
		{
			super.markDirty();
		}
		return result;
	}

	@Override
	public V get(int arg0)
	{
		V result = ((List<V>) super.target).get(arg0);
		if (isJoin())
		{
			return helper.buildProxy(result, joinMeta());
		}
		else
		{

			return result;
		}
	}

	@Override
	public int indexOf(Object arg0)
	{
		return ((List<V>) super.target).indexOf(arg0);
	}

	@Override
	public int lastIndexOf(Object arg0)
	{
		return ((List<V>) super.target).lastIndexOf(arg0);
	}

	@Override
	public ListIterator<V> listIterator()
	{
		ListIterator<V> target = ((List<V>) super.target).listIterator();

		return builder(target) //
				.dirtyMap(dirtyMap) //
				.setter(setter) //
				.propertyMeta(propertyMeta) //
				.helper(helper) //
				.build();
	}

	@Override
	public ListIterator<V> listIterator(int arg0)
	{
		ListIterator<V> target = ((List<V>) super.target).listIterator(arg0);

		return builder(target) //
				.dirtyMap(dirtyMap) //
				.setter(setter) //
				.propertyMeta(propertyMeta) //
				.helper(helper) //
				.build();
	}

	@Override
	public V remove(int arg0)
	{
		V result = ((List<V>) super.target).remove(arg0);
		super.markDirty();
		return result;
	}

	@Override
	public V set(int arg0, V arg1)
	{
		V result = ((List<V>) super.target).set(arg0, helper.unproxy(arg1));
		super.markDirty();
		return result;
	}

	@Override
	public List<V> subList(int arg0, int arg1)
	{
		List<V> target = ((List<V>) super.target).subList(arg0, arg1);

		return builder(target) //
				.dirtyMap(dirtyMap) //
				.setter(setter) //
				.propertyMeta(propertyMeta) //
				.helper(helper) //
				.build();
	}

	@Override
	public List<V> getTarget()
	{
		return ((List<V>) super.target);
	}

}
