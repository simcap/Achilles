## Compatible JPA Annotations

 For bean mapping, only **field-based access type** is supported by **Achilles**. Furthermore, there is no default mapping 
 for fields. If you want a field to be mapped, you must annotate it with *@Column* or *@JoinColumn*. All fields that are not
 annotated by either annotationsis considered transient by **Achilles**
 
 Below is a list of all JPA annotations supported by **Achilles**.    
<br/>
---------------------------------------  
##### @Entity

 Indicates that an entity is candidate for persistence. By default **Achilles** creates a column family whose name is the **class name**
 of the entity. If you want to specify a specific column family name, add the *@Table* annotation with the *name* attribute (see below).
 
 Example:
 
	@Entity
	public class User implements Serializable
	{
		private static final long serialVersionUID = 1L;
		...
		...
	}

>	Please note that all entities must implement the `java.io.Serializable`	interface and provide a **serialVersionUID**.
	Failing to meet this requirement will trigger a **BeanMappingException**.

<br/>   
---------------------------------------	
##### @Table
When then *name* attribute is filled, it indicates the name of the column family used by by the **Cassandra** engine to store this entity.
 

 Example:
 
	@Entity
	@Table(name = "users_column_family")
	public class User implements Serializable
	{
		private static final long serialVersionUID = 1L;
		...
		...
	}

>	** Please note that Cassandra limits the column family name to 48 characters max.**
	
<br/>   
---------------------------------------	
##### @Id

 Marks a field as the primary key for the entity. The primary key can be of any type, even a plain POJO. However it must 
 implement the `java.io.Serializable` interface otherwise an **AchillesException** will be raised.

 Under the hood, the primary key will be serialized to bytes array and  used as row key (partition key) by the **Cassandra**
 engine.
   
<br/>
---------------------------------------
##### @Column

 Marks a field to be mapped by **Achilles**. When the *name* attribute of *@Column* is given, the field
 will be mapped to this name. Otherwise the field name will be used.

 Example:

	@Column(name = "age_in_years")
	private Long age; 

 When put on a **WideMap** field, the *table* attribute of *@Column* annotation indicates the external column family to
 be used for the wide map. For more details, check [External wide row][externalWideRow]

 Example:

	@Column(table="my_tweets_column_family")
	private WideMap<UUID,Tweet> tweets;
    
<br/>  
---------------------------------------	
##### @OneToOne, @ManyToOne, @OneToMany, @ManyToMany

 These annotations should be used only along with *@JoinColumn*.
 
 *@ManyToOne* should only be used with *simple* join columns. 

 Example:
 
	@Table
	public class Tweet implements Serializable
	{

		private static final long serialVersionUID = 1L;

		@Id
		private UUID id;

		@ManyToOne
		@JoinColumn
		private User creator;
		
		...
		...
	}	

	
 *@OneToMany* and *@ManyToMany* should only be used wide **WideMap** join columns. 

 Example: 
 
	@Table
	public class User implements Serializable
	{

		private static final long serialVersionUID = 1L;

		@Id
		private Long id;

		...

		@ManyToMany(cascade = CascadeType.ALL)
		@JoinColumn
		private WideMap<Integer, Tweet> timeline;

		...
	}	

 Suppported JPA cascade styles are:
 
 * ALL
 * PERSIST
 * MERGE
 * REFRESH 

 *CascadeType.REMOVE* is not supported by **Achilles** as per design (check [Join columns][joinColumns] for more details)
 *CascadeType.RESFRESH* is implicit for join columns.
 *CascadeType.ALL* in **Achilles** is just a shortcut for `{CascadeType.PERSIST,CascadeType.MERGE}`
   
<br/>
---------------------------------------	
##### @JoinColumn	

 Marks a field to be mapped by **Achilles**. When the *name* attribute of *@JoinColumn* is given, the field
 will be mapped to this name. Otherwise the field name will be used.
 
 For a join column, only the primary key of the join entity is persisted by the **Cassandra** storage engine. 
 **Achilles** will take care of loading the join entity at runtime in the background. For more details, check
 [Join columns][joinColumns]
 
 **By definition, all join columns are lazy**
 
 Example:

	@ManyToMany(cascade = CascadeType.ALL)
	@JoinColumn(table="timeline_column_family")
	private WideMap<UUID,Tweet> timeline;

<br/>
## Specific Achilles Annotations	

##### @ColumnFamily

 The *@ColumnFamily* custom annotation should be put on an entity, along with the JPA *@Entity* annotation. 
 
 Example is better than words:
 
    @Entity
	@ColumnFamily
	@Table("good_old_column_family")
	public class ColumnFamilyEntity implements Serializable
	{
		private static final long serialVersionUID = 1L;

		@Id
		private Long id;

		@Column
		private WideMap<Integer, String> wideMap;
	} 

 For more details, please report to the [Direct Column Family Mapping][cfDirectMapping] page.

<br/>
---------------------------------------	 
##### @Lazy

 When put on a *@Column* field, this annotation makes it lazy. When the entity is loaded by **Achilles**, only eager 
 fields are loaded. Lazy fields will be loaded at runtime when the getter is invoked.

 By design, all **WideMap** fields are lazy.
 
 A *lazy* Collection or Map field will be loaded entirely when the getter is invoked.
 
 It does not make sense to put a *@Lazy* annotation on a *@JoinColumn* since the latter is lazy by definition. However
 doing so will not raise error, it will be silently ignored by **Achilles**

<br/>
---------------------------------------	 
##### @Key

 This annotation is used to define multi component column keys. It should be used in a class which extends the **MultiKey**
 interface.
 
 The annotation exposes one mandatory *order* attribute to indicates the order of the current component in the key.

<br/> 
>	Unlike Java indexes, the ordering for @Key start at 1. It is a design choice since it is more natural for human being to start
 counting at 1

<br/>

 For more detail on this annotation and its usage, please refer to  [Multi components for wide row][multiComponentKey]
 
 
[quickTuto]: /documentation/quickTuto.markdown
[annotations]: /documentation/annotations.markdown
[emOperations]: /documentation/emOperations.markdown
[collectionsAndMaps]: /documentation/collectionsAndMaps.markdown
[dirtyCheck]: /documentation/dirtyCheck.markdown
[wideMapAPI]: /documentation/wideMapAPI.markdown
[internalWideMap]: /documentation/internalWideMap.markdown
[externalWideMap]: /documentation/externalWideMap.markdown
[cfDirectMapping]: /documentation/cfDirectMapping.markdown
[multiComponentKey]: /documentation/multiComponentKey.markdown
[joinColumns]: /documentation/joinColumns.markdown
[manualCFCreation]:  /documentation/manualCFCreation.markdown