Composite Keys
---------------------------

BTreeMap can have composite key; an key composed from multiple components. 
Range query can get all sub-components associated with primary component.  

Here is an example; lets associate persons name (composed of surname and firstname) with age. 
We than find all persons with surname Smith (primary key component) . 

<!--- #file#doc/btreemap_tuple_age.java--->
```java
    //create new map
    BTreeMap<Tuple2, Integer> persons = db
      .treeMap("persons", Tuple2.class, Integer.class)
      .createOrOpen();

    //insert three persons into map
    persons.put(new Tuple2("Smith","John"), 45);
    persons.put(new Tuple2("Smith","Peter"), 37);
    persons.put(new Tuple2("Doe","John"), 70);

    //now lets get map which contains all Smiths
    NavigableMap<Tuple2,Integer> smiths =
      persons.prefixSubMap(
new Tuple2("Smith", null)  //null indicates wildcard for range query
      );
```

Example above can be more strongly-typed with wrapper classes and generics. In here we 
 use `Surname` and `Firstname` classes. 

<!--- #file#doc/btreemap_tuple_age2.java--->
```java
    //create new map
    BTreeMap<Tuple2<Surname, Firstname>, Integer> persons = db
      .treeMap("persons")
      .keySerializer(new Tuple2Serializer()) //specialized tuple serializer
      .valueSerializer(Serializer.INTEGER)
      .createOrOpen();

    //insert three person into map
    persons.put(new Tuple2(new Surname("Smith"),new Firstname("John")), 45);
    persons.put(new Tuple2(new Surname("Smith"),new Firstname("Peter")), 37);
    persons.put(new Tuple2(new Surname("Doe"),new Firstname("John")), 70);

    //now lets get map which contains all Smiths
    NavigableMap<Tuple2<Surname, Firstname>,Integer> smiths =
      persons.prefixSubMap(
new Tuple2(new Surname("Smith"), null)  //null indicates
      );
```

Tuples use `Comparable` interface, all key components (`Person` and `Firstname`) should implement it.
Other option is to use composite serializer with comparator method. For example 
to have `Tuple2<byte[], byte[]>` key we create tuple serializer following way: 
 `new Tuple2Serializer(Serializer.BYTE_ARRAY, Serializer.BYTE_ARRAY)`. Complete example:


<!--- #file#doc/btreemap_tuple_age_bytearray.java--->
```java
    //create new map
    BTreeMap<Tuple2<byte[], byte[]>, Integer> persons = db
      .treeMap("persons")
      .keySerializer(new Tuple2Serializer(Serializer.BYTE_ARRAY, Serializer.BYTE_ARRAY))
      .valueSerializer(Serializer.INTEGER)
      .createOrOpen();

    persons.put(new Tuple2("Smith".getBytes(),"John".getBytes()), 45);

    NavigableMap<Tuple2,Integer> smiths =
      persons.prefixSubMap(
new Tuple2("Smith".getBytes(), null)
      );
```

Range query
---------------

In examples above we used `prefixSubMap(new Tuple2("surname", null))` method. It performs range query where second component is replaced by minimal and maximal value. 
This method `BTreeMap` class and is not standard `Map` method, there is `NavigableMap.subMap` equivalent:

<!--- #file#doc/btreemap_tuple_range1.java--->
```java
    NavigableMap<Tuple2,Integer> smiths =
      persons.prefixSubMap(new Tuple2("Smith", null));

    // is equivalent to
    smiths = persons.subMap(
      new Tuple2("Smith", Integer.MIN_VALUE), true,
      new Tuple2("Smith", Integer.MAX_VALUE), true
    );
```

In example above we use `Integer` because it provides minimal and maximal values. To make this easier
`TupleSerializer` introduces special values for negative and positive infinity, those are even smaller/greater than min/max values. `null` corresponds to negative infinity, `Tuple.HI` is positibe infinity. 
 
Those two values are not serializable and can not be stored in Map. But can be used for range query:

<!--- #file#doc/btreemap_tuple_range2.java--->
```java
    persons.subMap(
      new Tuple2("Smith", null),
      new Tuple2("Smith", Tuple.HI)
    );
```

Submap returns only single range. It means that we can only query left most components. Common mistake is to put 
infinity in middle, and expect right components to be included. Tuple in example bellow has three components (surname, firstname, age). But we can not just query Surname and Age, because age is left most and it will 
be overriden by infinity component before it:
 
<!--- #file#doc/btreemap_tuple_range3.java--->
```java
    //WRONG!! null is in middle position
    persons.prefixSubMap(new Tuple3("Smith",null,11));

    //same but submap
    //WRONG!! infinity is in middle
    persons.subMap(
      new Tuple3("Smith", null,     11),
      new Tuple3("Smith", Tuple.HI, 11)
    );
```

Fixed size array tuples
------------------

Tuples can be replaced by array. In this case we do not have generics and will have to do lot of casting. Here is an example with surname/firstname. For key serializer we use `new SerializerArrayTuple(tupleSize)`. 
`null` and `Tuple.HI` will not work, but we can use shorter array for prefix:

<!--- #file#doc/btreemap_tuple_array.java--->
```java
    //create new map
    BTreeMap<Object[], Integer> persons = db
      .treeMap("persons", new SerializerArrayTuple(2), Serializer.INTEGER)
      .createOrOpen();

    //insert three person into map
    persons.put(new Object[]{"Smith", "John"}, 45);
    persons.put(new Object[]{"Smith", "Peter"}, 37);
    persons.put(new Object[]{"Doe", "John"}, 70);

    //now lets get map which contains all Smiths
    NavigableMap<Object[],Integer> smiths =
      persons.prefixSubMap(
new Object[]{"Smith"}
      );
```
//TODO null is positive infinity, Tuple.HI does not exist

Variable size array tuples
-----------------------------

MapDB also has generic array serializer which can be used for tuples. In this case `prefixSubmap` will not work. 
But we can use submap:

<!--- #file#doc/btreemap_tuple_array_val.java--->
```java
    //create new map
    BTreeMap<Object[], Integer> persons = db
      .treeMap("persons", new SerializerArrayDelta(), Serializer.INTEGER)
      .createOrOpen();

    //insert three persons into map
    persons.put(new Object[]{"Smith", "John"}, 45);
    persons.put(new Object[]{"Smith", "Peter"}, 37);
    persons.put(new Object[]{"Doe", "John"}, 70);


    NavigableMap<Object[],Integer> smiths = persons.subMap(
      new Object[]{"Smith"}, //lower bound
      new Object[]{"Smith", null} //upper bound, null is positive infinity in this serializer
    );
```
Delta compression
------------------------

All three tuples type use [delta compression](https://en.wikipedia.org/wiki/Delta_encoding). 

TODO delta compression
