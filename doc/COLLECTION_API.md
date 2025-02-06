# Collections API Lab Discussion
### NAMES: Billy McCune wrm29, Jacob You jay27, Hsuan-Kai Liao hl475
### TEAM 1



## In your experience using these collections, are they hard or easy to use?
I think these collections are relatively easy to use, as they have very solid documentation and are almost universally used in Java programming, which means that there are lots of preexisting implementations and tutorials.

## In your experience using these collections, do you feel mistakes are easy to avoid?
Most mistakes are easy to avoid, as we rarely use collections for edge case situations. However, sometimes it can be a little unintuitive, like how it can’t take int and must take Integers, or such as how to cast between different lists.

## What methods are common to all collections (except Maps)?
int size()
boolean isEmpty()
boolean contains(Object o)
boolean add(E e)
boolean remove(Object o)
Iterator<E> iterator()
boolean containsAll(Collection<?> c)
boolean addAll(Collection<? extends E> c)
boolean removeAll(Collection<?> c)
boolean retainAll(Collection<?> c)
void clear()
Object[] toArray()
<T> T[] toArray(T[] a)
default Stream<E> stream()
default Stream<E> parallelStream()
default Spliterator<E> spliterator()


## What methods are common to all Deques?
boolean add(E e)
boolean addAll(Collection<? extends E> c)
void addFirst(E e)
void addLast(E e)
boolean contains(Object o)
Iterator<E> descendingIterator()
E element()
E getFirst()
E getLast()
Iterator<E> iterator()
boolean offer(E e)
boolean offerFirst(E e)
boolean offerLast(E e)
E peek()
E peekFirst()
E peekLast()
E poll()
E pollFirst()
E pollLast()
E pop()
void push(E e)
E remove()
boolean remove(Object o)
E removeFirst()
boolean removeFirstOccurrence(Object o)
E removeLast()
boolean removeLastOccurrence(Object o)
default Deque<E> reversed()
int size()

## What is the purpose of each interface implemented by LinkedList?
Serialized allows it to be serialized, Clonable allows Object.clone, it is Iterable and a Collections class, and also acts as a Queue, Deque and List, including their properties and methods. It also has Sequenced Collection in order to add order specific methods.

## How many different implementations are there for a Set?
There are three implementations, HashSet, TreeSet, LinkedHashSet

## What is the purpose of each superclass of PriorityQueue?
AbstractQueue provides a skeletal frame for all of the Queue classes. AbstractCollections does the same for all Collections, and Object is the base class of java.


## What is the purpose of the Collections utility class?
It reduces programming effort by providing existing, fast and efficient collections. It creates a global framework that all Java users can use, allowing for interoperability and easy interaction between different APIs, as they all use the same class. Finally, it promotes a standard interface that fosters good implementations that allow reuse.

## API Characterics applied to Collections API

* Easy to learn: It has extensive documentation and is relatively straightforward, with names that make sense, proper javadoc comments, and plenty of online tutorials

* Encourages extension: By creating a interface that everyone uses, it creates a foundation for more extendable collections. It also makes working with other code that use the Collection class easier, as it is not necessary to relearn their data systems.

* Leads to readable code: By building in proper error handling and implementing good names and coding techniques, it makes it so that collections methods are easy to understand and read.

* Hard to misuse: Collections has very robust error handling and in depth documentation. It is very clear how each class is meant to be used, and there are lots of examples of it online.
 







