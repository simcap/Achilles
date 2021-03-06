package info.archinnov.achilles.wrapper;

import static info.archinnov.achilles.wrapper.builder.EntryIteratorWrapperBuilder.builder;
import static info.archinnov.achilles.wrapper.builder.MapEntryWrapperBuilder.builder;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * EntrySetWrapper
 * 
 * @author DuyHai DOAN
 * 
 */
public class EntrySetWrapper<K, V> extends AbstractWrapper<K, V> implements Set<Entry<K, V>>
{

	private Set<Entry<K, V>> target;

	public EntrySetWrapper(Set<Entry<K, V>> target) {
		this.target = target;
	}

	@Override
	public boolean add(Entry<K, V> arg0)
	{
		throw new UnsupportedOperationException("This method is not supported for an Entry set");
	}

	@Override
	public boolean addAll(Collection<? extends Entry<K, V>> arg0)
	{
		throw new UnsupportedOperationException("This method is not supported for an Entry set");
	}

	@Override
	public void clear()
	{
		this.target.clear();
		this.markDirty();
	}

	@Override
	public boolean contains(Object arg0)
	{
		return this.target.contains(helper.unproxy(arg0));
	}

	@Override
	public boolean containsAll(Collection<?> arg0)
	{
		return this.target.containsAll(helper.unproxy(arg0));
	}

	@Override
	public boolean isEmpty()
	{
		return this.target.isEmpty();
	}

	@Override
	public Iterator<Entry<K, V>> iterator()
	{
		return builder(this.target.iterator()) //
				.dirtyMap(dirtyMap) //
				.setter(setter) //
				.propertyMeta(propertyMeta) //
				.helper(helper) //
				.build();
	}

	@Override
	public boolean remove(Object arg0)
	{
		boolean result = this.target.remove(helper.unproxy(arg0));
		if (result)
		{
			this.markDirty();
		}
		return result;
	}

	@Override
	public boolean removeAll(Collection<?> arg0)
	{
		boolean result = this.target.removeAll(helper.unproxy(arg0));
		if (result)
		{
			this.markDirty();
		}
		return result;
	}

	@Override
	public boolean retainAll(Collection<?> arg0)
	{
		boolean result = this.target.retainAll(helper.unproxy(arg0));
		if (result)
		{
			this.markDirty();
		}
		return result;
	}

	@Override
	public int size()
	{
		return this.target.size();
	}

	@Override
	public Object[] toArray()
	{
		if (isJoin())
		{
			Object[] array = new MapEntryWrapper[this.target.size()];
			int i = 0;
			for (Map.Entry<K, V> entry : this.target)
			{
				array[i] = builder(entry) //
						.dirtyMap(dirtyMap) //
						.setter(setter) //
						.propertyMeta(propertyMeta) //
						.helper(helper) //
						.build();
				i++;
			}

			return array;
		}
		else
		{
			return this.target.toArray();
		}
	}

	@Override
	public <T> T[] toArray(T[] arg0)
	{
		if (isJoin())
		{
			T[] array = this.target.toArray(arg0);

			for (int i = 0; i < array.length; i++)
			{
				array[i] = helper.buildProxy(array[i], joinMeta());
			}
			return array;
		}
		else
		{
			return this.target.toArray(arg0);
		}
	}

}
