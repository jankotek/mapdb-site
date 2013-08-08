package org.mapdb;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * Data Pump moves data from one source to other.
 * It can be used to import data from text file, or copy store from memory to disk.
 */
public class Pump {

    /** copies all data from first DB to second DB */
    public static void copy(DB db1, DB db2){
        copy(storeForDB(db1), storeForDB(db2));
        db2.engine.clearCache();
        db2.reinit();
    }

    /** copies all data from first store to second store */
    public static void copy(Store s1, Store s2){
        long maxRecid =s1.getMaxRecid();
        for(long recid=1;recid<=maxRecid;recid++){
            ByteBuffer bb = s1.getRaw(recid);
            //System.out.println(recid+" - "+(bb==null?0:bb.remaining()));
            if(bb==null) continue;
            s2.updateRaw(recid, bb);
        }

        //now release unused recids
        for(Iterator<Long> iter = s1.getFreeRecids(); iter.hasNext();){
            s2.delete(iter.next(), null);
        }
    }


    /** traverses {@link EngineWrapper}s and returns underlying {@link Store}*/
    public static Store storeForDB(DB db){
        return storeForEngine(db.engine);
    }

    /** traverses {@link EngineWrapper}s and returns underlying {@link Store}*/
    public static Store storeForEngine(Engine e){
        while(e instanceof EngineWrapper) e = ((EngineWrapper) e).getWrappedEngine();
        return (Store) e;
    }

    /**
     * Sorts large data set by given `Comparator`. Data are sorted with in-memory cache and temporary files.
     *
     * @param source iterator over unsorted data
     * @param batchSize how much items can fit into heap memory
     * @param comparator used to sort data
     * @param serializer used to store data in temporary files
     * @param <E> type of data
     * @return iterator over sorted data set
     */
    public static <E> Iterator<E> sort(final Iterator<E> source, final int batchSize,
            final Comparator comparator, final Serializer serializer){
        if(batchSize<=0) throw new IllegalArgumentException();

        int counter = 0;
        final SortedSet<E> presort = new TreeSet<E>(comparator);
        final List<File> presortFiles = new ArrayList<File>();
        final List<Integer> presortCount2 = new ArrayList<Integer>();

        try{
            while(source.hasNext()){
                counter++;
                presort.add(source.next());

                if(counter>=batchSize){
                    //flush presort into temporary file
                    File f = Utils.tempDbFile();
                    f.deleteOnExit();
                    presortFiles.add(f);
                    DataOutputStream out = new DataOutputStream(new FileOutputStream(f));
                    for(E e:presort){
                        serializer.serialize(out,e);
                    }
                    out.close();
                    presortCount2.add(presort.size());
                    presort.clear();
                    counter = 0;
                }
            }
            //now all records from source are fetch
            if(presortFiles.isEmpty()){
                //no presort files were created, so on-heap sorting is enough
                return presort.iterator();
            }

            final int[] presortCount = new int[presortFiles.size()];
            for(int i=0;i<presortCount.length;i++) presortCount[i] = presortCount2.get(i);
            //compose iterators which will iterate over data saved in files
            Iterator[] iterators = new Iterator[presortFiles.size()+1];
            final DataInputStream[] ins = new DataInputStream[presortFiles.size()];
            for(int i=0;i<presortFiles.size();i++){
                ins[i] = new DataInputStream(new FileInputStream(presortFiles.get(i)));
                final int pos = i;
                iterators[i] = new Iterator(){

                    @Override public boolean hasNext() {
                        return presortCount[pos]>0;
                    }

                    @Override public Object next() {
                        try {
                            Object ret =  serializer.deserialize(ins[pos],-1);
                            if(--presortCount[pos]==0){
                                ins[pos].close();
                                presortFiles.get(pos).delete();
                            }
                            return ret;
                        } catch (IOException e) {
                            throw new IOError(e);
                        }
                    }

                    @Override public void remove() {
                        //ignored
                    }

                };
            }

            //and add iterator over data on-heap
            iterators[iterators.length-1] = presort.iterator();


            //and finally sort presorted iterators and return iterators over them
            return sort(comparator, iterators);

        }catch(IOException e){
            throw new IOError(e);
        }finally{
            for(File f:presortFiles) f.delete();
        }
    }


    /**
     * Merge presorted iterators into single sorted iterator.
     *
     * @param comparator used to compare data
     * @param iterators array of already sorted iterators
     * @param <E> type of data
     * @return sorted iterator
     */
    public static <E> Iterator<E> sort(final Comparator comparator, final Iterator[] iterators) {

        return new Iterator<E>(){

            final Object[] items = new Object[iterators.length];
            E next = null;

            {
                for(int i=0;i<iterators.length;i++){
                    if(iterators[i].hasNext()){
                        items[i] = iterators[i].next();
                    }
                }
                next();
            }


            @Override public boolean hasNext() {
                return next!=null;
            }

            @Override public E next() {
                E oldNext = next;

                int low=0;
                for(int i=1;i<items.length;i++){
                    if(items[i]==null) continue;
                    if(items[low]==null|| comparator.compare((E)items[i],(E)items[low])<0)
                        low = i;
                }

                next = (E) items[low];

                items[low] = iterators[low].hasNext()?iterators[low].next():null;

                return oldNext;
            }

            @Override public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }


    /**
     * Build BTreeMap  from presorted data.
     * This method is much faster than usual import using `Map.put(key,value)` method.
     * It is because tree integrity does not have to be maintained and
     * tree can be created in linear way with.
     *
     * This method expect data to be presorted in **reverse order** (highest to lowest).
     * There are technical reason for this requirement.
     * To sort unordered data use {@link Pump#sort(java.util.Iterator, int, java.util.Comparator, Serializer)}
     *
     * This method does not call commit. You should disable Write Ahead Log when this method is used {@link org.mapdb.DBMaker#transactionDisable()}
     *
     * @param source iterator over source data, must be reverse sorted
     * @param db database where Map will be created
     * @param name name of collection to create
     * @param valueExtractor transforms items from source iterator into values. If null BTreeMap will be constructed without values (as Set)
     * @param <K> Type of Map Keys
     * @param <V> Type of Map Values
     */
    public static  <K,V> void buildTreeMap(Iterator<K> source, DB db, String name, Fun.Function1<V,K> valueExtractor){
        buildTreeMap(source,db,name, valueExtractor, CC.BTREE_DEFAULT_MAX_NODE_SIZE, false, false,null,null,null);
    }

    /**
     * Build BTreeMap from presorted data.
     * This method is much faster than usual import using `Map.put(key,value)` method.
     * It is because tree integrity does not have to be maintained and
     * tree can be created in linear way with.
     *
     * This method expect data to be presorted in **reverse order** (highest to lowest).
     * There are technical reason for this requirement.
     * To sort unordered data use {@link Pump#sort(java.util.Iterator, int, java.util.Comparator, Serializer)}
     *
     * This method does not call commit. You should disable Write Ahead Log when this method is used {@link org.mapdb.DBMaker#transactionDisable()}
     *
     * @param source iterator over source data, must be reverse sorted
     * @param db database where Map will be created
     * @param name name of collection to create
     * @param valueExtractor transforms items from source iterator into values. If null BTreeMap will be constructed without values (as Set)
     * @param nodeSize maximal BTree node size before it is splited.
     * @param valuesStoredOutsideNodes if true values will not be stored as part of BTree nodes
     * @param keepCounter if true BTreeMap will keep track of number of its size, so `Map.size()` will not traverse all elements in map
     * @param keySerializer serializer for keys, use null for default value
     * @param valueSerializer serializer for value, use null for default value
     * @param comparator comparator used to compare keys, use null for 'comparable comparator'
     * @param <K> Type of Map Keys
     * @param <V> Type of Map Values
     */
    public static  <K,V> void buildTreeMap(Iterator<K> source, DB db, String name, Fun.Function1<V,K> valueExtractor,
                                           int nodeSize, boolean valuesStoredOutsideNodes,
                                           boolean keepCounter,
                                           BTreeKeySerializer<K> keySerializer,
                                           Serializer<V> valueSerializer,
                                           Comparator comparator){
        buildTreeMap(source, db, name, null, valueExtractor, nodeSize, valuesStoredOutsideNodes, keepCounter,
                keySerializer, valueSerializer, comparator);
    }


    /**
     *
     * Build BTreeMap  from presorted data.
     * This method is much faster than usual import using `Map.put(key,value)` method.
     * It is because tree integrity does not have to be maintained and
     * tree can be created in linear way with.
     *
     * This method expect data to be presorted in **reverse order** (highest to lowest).
     * There are technical reason for this requirement.
     * To sort unordered data use {@link Pump#sort(java.util.Iterator, int, java.util.Comparator, Serializer)}
     *
     * This method does not call commit. You should disable Write Ahead Log when this method is used {@link org.mapdb.DBMaker#transactionDisable()}
     *
     * @param source iterator over source of data, must be reverse sorted
     * @param db database where Map will be created
     * @param name name of collection to create
     * @param <K> Type of Map Keys
     * @param <V> Type of Map Values
     */
    public static  <K,V> void buildTreeMap(Iterator<Fun.Tuple2<K,V>> source, DB db, String name){
        buildTreeMap(source,db,name,CC.BTREE_DEFAULT_MAX_NODE_SIZE, false, false,null,null,null);
    }


    /**
     *
     * Build BTreeMap  from presorted data.
     * This method is much faster than usual import using `Map.put(key,value)` method.
     * It is because tree integrity does not have to be maintained and
     * tree can be created in linear way with.
     *
     * This method expect data to be presorted in **reverse order** (highest to lowest).
     * There are technical reason for this requirement.
     * To sort unordered data use {@link Pump#sort(java.util.Iterator, int, java.util.Comparator, Serializer)}
     *
     * This method does not call commit. You should disable Write Ahead Log when this method is used {@link org.mapdb.DBMaker#transactionDisable()}
     *
     * @param source iterator over source of data, must be reverse sorted
     * @param db database where Map will be created
     * @param name name of collection to create
     * @param nodeSize maximal BTree node size before it is splited.
     * @param valuesStoredOutsideNodes if true values will not be stored as part of BTree nodes
     * @param keepCounter if true BTreeMap will keep track of number of its size, so `Map.size()` will not traverse all elements in map
     * @param keySerializer serializer for keys, use null for default value
     * @param valueSerializer serializer for value, use null for default value
     * @param comparator comparator used to compare keys, use null for 'comparable comparator'
     * @param <K> Type of Map Keys
     * @param <V> Type of Map Values
     */
    public static  <K,V> void buildTreeMap(Iterator<Fun.Tuple2<K,V>> source, DB db, String name,
                                           int nodeSize, boolean valuesStoredOutsideNodes,
                                           boolean keepCounter,
                                           BTreeKeySerializer<K> keySerializer,
                                           Serializer<V> valueSerializer,
                                           Comparator comparator){

        Fun.Function1<K,Fun.Tuple2<K,V>> keyExtractor = Fun.keyExtractor();
        Fun.Function1<V,Fun.Tuple2<K,V>> valueExtractor = Fun.valueExtractor();

        buildTreeMap(source,db, name, keyExtractor, valueExtractor, nodeSize,
                valuesStoredOutsideNodes, keepCounter, keySerializer, valueSerializer,comparator);
    }


    /**
     * Build TreeSet  from presorted data.
     * This method is much faster than usual import using `Set.add(entry)` method.
     * It is because tree integrity does not have to be maintained and
     * tree can be created in linear way with.
     *
     * This method expect data to be presorted in **reverse order** (highest to lowest).
     * There are technical reason for this requirement.
     * To sort unordered data use {@link Pump#sort(java.util.Iterator, int, java.util.Comparator, Serializer)}
     *
     * This method does not call commit. You should disable Write Ahead Log when this method is used {@link org.mapdb.DBMaker#transactionDisable()}
     *
     * @param source iterator over source of data, must be reverse sorted
     * @param db database where Map will be created
     * @param name name of collection to create
     * @param <E> type of elements held by a set
     */
    public static  <E> void buildTreeSet(Iterator<E> source, DB db, String name){
        buildTreeSet(source, db,name, CC.BTREE_DEFAULT_MAX_NODE_SIZE, false, null,null);
    }

    /**
     * Build TreeSet  from presorted data.
     * This method is much faster than usual import using `Set.add(entry)` method.
     * It is because tree integrity does not have to be maintained and
     * tree can be created in linear way with.
     *
     * This method expect data to be presorted in **reverse order** (highest to lowest).
     * There are technical reason for this requirement.
     * To sort unordered data use {@link Pump#sort(java.util.Iterator, int, java.util.Comparator, Serializer)}
     *
     * This method does not call commit. You should disable Write Ahead Log when this method is used {@link org.mapdb.DBMaker#transactionDisable()}
     *
     * @param source iterator over source of data, must be reverse sorted
     * @param db database where Map will be created
     * @param name name of collection to create
     * @param nodeSize maximal BTree node size before it is splited.
     * @param keepCounter if true BTreeMap will keep track of number of its size, so `Map.size()` will not traverse all elements in map
     * @param serializer serializer for entries, use null for default value
     * @param comparator comparator used to compare keys, use null for 'comparable comparator'
     * @param <E> type of elements held by a set
     */
    public static  <E> void buildTreeSet(Iterator<E> source,
                                         DB db, String name,
                                         int nodeSize,
                                         boolean keepCounter,
                                         BTreeKeySerializer serializer,
                                         Comparator comparator) {
        buildTreeMap(source, db, name, null, null, nodeSize, false, keepCounter, serializer, null, comparator);
    }

    /**
     * Build BTreeMap (or TreeSet) from presorted data.
     * This method is much faster than usual import using `Map.put(key,value)` method.
     * It is because tree integrity does not have to be maintained and
     * tree can be created in linear way with.
     *
     * This method expect data to be presorted in **reverse order** (highest to lowest).
     * There are technical reason for this requirement.
     * To sort unordered data use {@link Pump#sort(java.util.Iterator, int, java.util.Comparator, Serializer)}
     *
     * This method does not call commit. You should disable Write Ahead Log when this method is used {@link org.mapdb.DBMaker#transactionDisable()}
     *
     * @param source iterator over source data, must be reverse sorted
     * @param db database where Map or Set will be created
     * @param name name of collection to create
     * @param keyExtractor transforms items from source iterator into keys. If null source items will be used directly as keys.
     * @param valueExtractor transforms items from source iterator into values. If null BTreeMap will be constructed without values (as Set)
     * @param nodeSize maximal BTree node size before it is splited.
     * @param valuesStoredOutsideNodes if true values will not be stored as part of BTree nodes
     * @param keepCounter if true BTreeMap will keep track of number of its size, so `Map.size()` will not traverse all elements in map
     * @param keySerializer serializer for keys, use null for default value
     * @param valueSerializer serializer for value, use null for default value
     * @param comparator comparator used to compare keys, use null for 'comparable comparator'
     * @param <E> Type if items returned by source iterator
     * @param <K> Type of Map Keys
     * @param <V> Type of Map Values
     *
     * @throws IllegalArgumentException if source iterator is not reverse sorted
     */
    public static  <E,K,V> void buildTreeMap(Iterator<E> source,
                                    DB db, String name,
                                    Fun.Function1<K,E> keyExtractor,
                                    Fun.Function1<V,E> valueExtractor,

                                    int nodeSize,
                                    boolean valuesStoredOutsideNodes,
                                    boolean keepCounter,
                                    BTreeKeySerializer<K> keySerializer,
                                    Serializer<V> valueSerializer,
                                    Comparator comparator) {

        final double NODE_LOAD = 0.75;

        if(keySerializer==null) keySerializer = (BTreeKeySerializer<K>) new BTreeKeySerializer.BasicKeySerializer(db.getDefaultSerializer());
        if(valueSerializer==null) valueSerializer = db.getDefaultSerializer();
        if(comparator==null) comparator = Utils.COMPARABLE_COMPARATOR;

        final boolean hasVals = valueExtractor!=null;

        Serializer<BTreeMap.BNode> nodeSerializer = new BTreeMap.NodeSerializer(valuesStoredOutsideNodes,keySerializer,valueSerializer,comparator);


        final int nload = (int) (nodeSize * NODE_LOAD);
        ArrayList<ArrayList<Object>> dirKeys = arrayList(arrayList(null));
        ArrayList<ArrayList<Long>> dirRecids = arrayList(arrayList(0L));

        long counter = 0;

        long nextNode = 0;

        //fill node with data
        List<K> keys = arrayList(null);
        ArrayList<V> values = hasVals?new ArrayList<V>() : null;
        //traverse iterator
        K oldKey = null;
        while(source.hasNext()){

            for(int i=0;i<nload && source.hasNext();i++){
                counter++;
                E next = source.next();
                K key = keyExtractor==null? (K) next : keyExtractor.run(next);
                if(oldKey!=null && comparator.compare(key, oldKey)>=0)
                    throw new IllegalArgumentException("Keys in 'source' iterator are not reverse sorted");
                oldKey = key;
                keys.add(key);
                if(hasVals) values.add(valueExtractor.run(next));
            }
            //insert node
            if(!source.hasNext()){
                keys.add(null);
                if(hasVals)values.add(null);
            }

            Collections.reverse(keys);

            V nextVal = null;
            if(hasVals){
                nextVal = values.remove(values.size()-1);
                Collections.reverse(values);
            }



            BTreeMap.LeafNode node = new BTreeMap.LeafNode(keys.toArray(),hasVals? values.toArray() : null, nextNode);
            nextNode = db.engine.put(node,nodeSerializer);
            K nextKey = keys.get(0);
            keys.clear();

            keys.add(nextKey);
            keys.add(nextKey);
            if(hasVals){
                values.clear();
                values.add(nextVal);
            }
            dirKeys.get(0).add(node.keys()[0]);
            dirRecids.get(0).add(nextNode);

            //check node sizes and split them if needed
            for(int i=0;i<dirKeys.size();i++){
                if(dirKeys.get(i).size()<nload) break;
                //tree node too big so write it down and start new one
                Collections.reverse(dirKeys.get(i));
                Collections.reverse(dirRecids.get(i));
                //put node into store
                BTreeMap.DirNode dir = new BTreeMap.DirNode(dirKeys.get(i).toArray(), dirRecids.get(i));
                long dirRecid = db.engine.put(dir,nodeSerializer);
                Object dirStart = dirKeys.get(i).get(0);
                dirKeys.get(i).clear();
                dirKeys.get(i).add(dirStart);
                dirRecids.get(i).clear();
                dirRecids.get(i).add(dirRecid); //put pointer to next node

                //update parent dir
                if(dirKeys.size()==i+1){
                    dirKeys.add(arrayList(dirStart));
                    dirRecids.add(arrayList(dirRecid));
                }else{
                    dirKeys.get(i+1).add(dirStart);
                    dirRecids.get(i+1).add(dirRecid);
                }
            }
        }

        //flush directory
        for(int i=0;i<dirKeys.size()-1;i++){
            //tree node too big so write it down and start new one
            Collections.reverse(dirKeys.get(i));
            Collections.reverse(dirRecids.get(i));
            //put node into store
            BTreeMap.DirNode dir = new BTreeMap.DirNode(dirKeys.get(i).toArray(), dirRecids.get(i));
            long dirRecid = db.engine.put(dir,nodeSerializer);
            Object dirStart = dirKeys.get(i).get(0);
            dirKeys.get(i+1).add(dirStart);
            dirRecids.get(i+1).add(dirRecid);

        }

        //and finally write root
        final int len = dirKeys.size()-1;
        Collections.reverse(dirKeys.get(len));
        Collections.reverse(dirRecids.get(len));

        //and do counter
        long counterRecidRef = keepCounter? db.engine.put(counter, Serializer.LONG) : 0L;


        BTreeMap.DirNode dir = new BTreeMap.DirNode(dirKeys.get(len).toArray(), dirRecids.get(len));
        long rootRecid = db.engine.put(dir, nodeSerializer);
        long rootRecidRef = db.engine.put(rootRecid,Serializer.LONG);

        Map cat = db.getCatalog();
        cat.put(name+".type",hasVals?"TreeMap":"TreeSet");
        cat.put(name+".rootRecidRef",rootRecidRef);
        cat.put(name+".nodeSize",nodeSize);
        cat.put(name+".valuesOutsideNodes",valuesStoredOutsideNodes);
        cat.put(name+".counterRecid",counterRecidRef);
        cat.put(name+".comparator",comparator);
        if(hasVals){
            cat.put(name+".keySerializer",keySerializer);
            cat.put(name+".valueSerializer",valueSerializer);
        }else{
            cat.put(name+".serializer",keySerializer);
        }
    }

    /** create array list with single element*/
    private static <E> ArrayList<E> arrayList(E item){
        ArrayList<E> ret = new ArrayList<E>();
        ret.add(item);
        return ret;
    }

}
