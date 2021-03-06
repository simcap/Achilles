package parser.entity;

import info.archinnov.achilles.entity.type.WideMap;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;


/**
 * BeanWithExternalWideMap
 * 
 * @author DuyHai DOAN
 * 
 */
@Entity
public class BeanWithExternalWideMap implements Serializable
{
	public static final long serialVersionUID = 1L;

	@Id
	private Long id;

	@Column
	private String name;

	@Column(table = "external_users")
	private WideMap<Integer, UserBean> users;

	public Long getId()
	{
		return id;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public WideMap<Integer, UserBean> getUsers()
	{
		return users;
	}

	public void setUsers(WideMap<Integer, UserBean> users)
	{
		this.users = users;
	}
}
