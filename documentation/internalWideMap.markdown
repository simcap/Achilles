## Internal WideMap

 Before reading further, please make sure you read carefully the chapter on [WideMap API][wideMapAPI]

 An internal **WideMap** is simply a wide row structure stored along with other entity values:

	@Entity
	@Table(name="users_column_family")
	{
		private static final long serialVersionUID = 1L;

		@Id
		private Long id;

		@Column
		private String firstname;

		@Column
		private String lastname;
		
		@Column(name="age_in_year")
		private Integer age;
		
		@Column
		private Set<String> addresses;
		
		@Lazy
		@Column
		private List<String> favoriteTags;
		
		@Column
		private Map<Integer,String> preferences;
		
		@Column
		private WideMap<UUID, Tweet> tweets; 
		
		...
	}
	
 In the above example, the *tweets* field is a **WideMap** proxy to allow inserting, finding and removing tweets POJO inside 
 the **User** like a wide row.


 Internally, **Achilles** saves all users data ( _firstname_, *lastname*, *age*, *addresses*, *favoriteTags*, *preferences* and all
 *tweets* values) in a **same physical row** in **Cassandra** storage engine. It has some benefits to doing so, you can benefit a lot
 from **Cassandra** [row caching][rowCaching]. Consequently, the entity must exist if you want to access internal an **WideMap**.
 
 
 Simple example:
 
	User user = new User();
	user.setId(10L);
	user.setFirstname("DuyHai");
	
	// This will persist the user
	user = em.merge(user);
 
	// Get the WideMap proxy
	WideMap<UUID,Tweet> tweets = user.getTweets();
 
 
  The above example works because we persist the **User** entity first. In the below example, it will not work:

	// Will return NULL
	User foundUser = em.find(User.class,10L);
 
  
##### Some limitations
  
 Since the maximum number of columns per row in **Cassandra** is  2 billions (2.10^9), mixing wide row values with simple entity 
 values can be tricky when there is a large amount of data. If we take the above **User** entity, there will be:
 
 * 1 column used for *firstname*
 * 1 column used for *lastname*
 * 1 column used for *age*
 * M columns used for *addresses*
 * N columns used for *favoriteTags*
 * O columns used for *preferences*
 <br/>

So at most, the physical row can records up to `2.10^9 - (N+M+0+3)` values for the *tweets* internal wide row.

 Of course if we define other internal wide rows for the same entity, the available remaining space for each of them will be lesser
 than `2.10^9 - (N+M+0+3)`.
 
 The second limitation is the danger to have a row which is **too wide**. Yes, it seems paradoxical to write it but some guys at Ebay 
 have played with wide rows and recommended not to have too wide rows. (link [here][eBayBlog]). The main reasons for performance 
 issues is that too large rows 


 - create hotspots in the cluster
 - may not fit entirely in memory, limiting or worse, cancelling the benefit of **row caching** when the row size is very big to be
 of the same order of magnitude than the row cache size

<br/> 
 Long story short, it is a good idea to have wide rows but just do not make them too wide.
 
 If you want to overcome these limits or simply consider it's a bad practice to mix wide row values with entity values, just use
 [External WideMap][externalWideMap]

 
 
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
[rowCaching]: http://www.datastax.com/dev/blog/maximizing-cache-benefit-with-cassandra
[eBayBlog]: http://www.ebaytechblog.com/2012/08/14/cassandra-data-modeling-best-practices-part-2/
