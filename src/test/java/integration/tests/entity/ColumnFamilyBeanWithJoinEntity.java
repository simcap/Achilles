package integration.tests.entity;

import info.archinnov.achilles.annotations.ColumnFamily;
import info.archinnov.achilles.entity.type.WideMap;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;

/**
 * ColumnFamilyBeanWithJoinEntity
 * 
 * @author DuyHai DOAN
 * 
 */
@Entity
@ColumnFamily
public class ColumnFamilyBeanWithJoinEntity implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
	private Long id;

	@JoinColumn
	@ManyToMany(cascade = CascadeType.ALL)
	private WideMap<Integer, User> friends;

	public Long getId()
	{
		return id;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	public WideMap<Integer, User> getFriends()
	{
		return friends;
	}
}
